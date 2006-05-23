/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.serialize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.AddressTester;
import org.jasig.portal.utils.CommonUtils;

/**
 * This Class allows appending PROXY_REWRITE_PREFIX String in front of all the references to images, javascript files, etc..
 * that are on a remote location. This allows the browser while portal is running in https assume that these resources
 * are secure resources(are referenced by https rather than http). This is because the resource URI insteadof
 * http://www.abc.com/image.gif will be rewriten as as https://[portal address]/PROXY_REWRITE_PREFIX/www.abc.com/image.gif
 * This class does the proxy rewrite in the following exceptional situations as well:
 * 1. If the return code poting to the image is 3XX (the image refrence, refrences is a mapping to a diffrent location)
 *    In this case the final destination iddress n which the image or the resource is located is e
 *    and then the rewrite points to this location.
 * 2. If the content of a channel is an include javascript file the file is rewriten to a location on a local virtual host
 *    and at the same time the image or other resources references are rewritten.
 *
 *
 * @author <a href="mailto:kazemnaderi@yahoo.ca">Kazem Naderi</a>
 * @version $Revision$
 * @since uPortal 2.2
 */

public class ProxyWriter {

    private static final Log log = LogFactory.getLog(ProxyWriter.class);
    
    /**
     * True if allow rewriting certain elements for proxying.
     */
    protected boolean _proxying;

    /**
     * The list of elemnets which src attribute is rewritten with proxy.
     */
    private static final String[] _proxiableElements = {"image","img","script","input","applet","iframe"};

    /*
     * If enabled the references to images or any external browser loadable resources will be proxied.
     */
    private static boolean PROXY_ENABLED=PropertiesManager.getPropertyAsBoolean("org.jasig.portal.serialize.ProxyWriter.resource_proxy_enabled");

   /*
    * The URI of location on virtual host on the same server as portal. This URI is used for rewriting proxied files.
    */
    private static String PROXIED_FILES_URI=PropertiesManager.getProperty("org.jasig.portal.serialize.ProxyWriter.proxy_files_uri");

   /*
    * The path of location on virtual host on the same server as portal. This path is used for rewriting proxied files.
    */
    private static String PROXIED_FILES_PATH=PropertiesManager.getProperty("org.jasig.portal.serialize.ProxyWriter.proxy_files_path");

    /*
     * The prefix used for proxying
     */
    private static final String PROXY_REWRITE_PREFIX=PropertiesManager.getProperty("org.jasig.portal.serialize.ProxyWriter.resource_proxy_rewrite_prefix");

    /*
     * The local domain that does not do redirection
     */
    private static final String PROXY_REWRITE_NO_REDIRECT_DOMAIN = PropertiesManager.
      getProperty("org.jasig.portal.serialize.ProxyWriter.no_redirect_domain");

    /**
     * Examines whther or not the proxying should be done and if so handles differnt situations by delegating
     * the rewrite to other methods n the class.
     * @param name
     * @param localName
     * @param value
     * @return value
     */
    protected static String considerProxyRewrite(String name, String localName, String value)
    {
        if (PROXY_ENABLED && (name.equalsIgnoreCase("src") || name.equalsIgnoreCase("archive")) &&
            value.indexOf("http://")!=-1)
        {

            //capture any resource redirect and set the value to the real address while proxying it
            value = capture3XXCodes(value);

            //if there is a script element with a src attribute the src should be rewriten
            if(localName.equalsIgnoreCase("script")){
                value = reWrite(value);
                return value;
            }

            //handle normal proxies
            for (int i=0; i<_proxiableElements.length; i++)
            {
                if (localName.equalsIgnoreCase(_proxiableElements[i]))
                {
                    value = PROXY_REWRITE_PREFIX + value.substring(7);
                    break;
                }
            }
        }
        return value;
    }


   /**
    * Capture 3xx return codes - specifically, if 301/302, then go tp the
    * redirected url - note, this in turn may also be redirected.
    * Note - do as little network connecting as possible. So as a start, assume
    * "ubc.ca" domain images will not be redirected so skip these ones.
    * @param value
    * @return value
    */
    private static String capture3XXCodes(String value){
        try{
            String skip_protocol = value.substring(7);
            String domain_only = skip_protocol.substring(0,skip_protocol.indexOf("/"));
            String work_value = value;
            if (PROXY_REWRITE_NO_REDIRECT_DOMAIN.length() > 0 && !domain_only.endsWith(PROXY_REWRITE_NO_REDIRECT_DOMAIN)) {
              while (true) {
                AddressTester tester = new AddressTester(work_value, true);
                try {
                  int responseCode = tester.getResponseCode();
                  if (responseCode != HttpURLConnection.HTTP_MOVED_PERM &&
                      responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                    log.debug(
                      "ProxyWriter::capture3XXCodes(): could not get deeper in getting the image.");
                    return work_value;
                  }

                  /* At this point we will have a redirect directive */

                  HttpURLConnection httpUrlConnect = (HttpURLConnection) tester.getConnection();
                  httpUrlConnect.connect();

                  work_value = httpUrlConnect.getHeaderField("Location"); ;
                } finally {
                  tester.disconnect();
                }
              }
            }

            return value;

         } catch(Exception e) {
             log.error("Failed to rewrite the value", e);
             return value;
         }
    }

    /**
     * This method rewrites include javascript files and replaces the refrences in these files
     * to images' sources to use proxy.
     * @param scriptUri: The string representing the address of script
     * @return value: The new address of the script file which image sources have been rewritten
     */
     private static String  reWrite(String scriptUri){
        String filePath = null;
        String fileName = null;
        try{
            fileName = fileNameGenerator(scriptUri);
            filePath = PROXIED_FILES_PATH + fileName;
            File outputFile = new File(filePath);
            if (!outputFile.exists() || (System.currentTimeMillis()-outputFile.lastModified() > 1800 * 1000))
            {
              try{
                AddressTester tester = new AddressTester(scriptUri);
                try {
                  if (!tester.URLAvailable()){
                    log.error("ProxyWriter::rewrite(): The adress " + scriptUri + " is not available. ");
                    return scriptUri;
                  }
                  HttpURLConnection httpUrlConnect = (HttpURLConnection) tester.getConnection();
                  httpUrlConnect.connect();
                  BufferedReader in = new BufferedReader(new InputStreamReader(httpUrlConnect.getInputStream()));
                  try {
                    FileWriter out = new FileWriter(outputFile);
                    try {
                      String line;
                      while ((line = in.readLine()) != null) {
                        out.write(processLine(line) + "\t\n");
                      }
                    } finally {
                      out.close();
                    }
                  } finally {
                    in.close();
                  }
                } finally {
                  tester.disconnect();
                }
              } catch(Exception e) {
                 log.error(
                                "ProxyWriter::rewrite():Failed to rewrite the file for: " +
                                scriptUri, e);
                 outputFile.delete();
                 return scriptUri;
               } //end catch
             }
             String newScriptPath = PROXIED_FILES_URI + fileName;
             AddressTester tester = new AddressTester(newScriptPath);
             try {
               if (!tester.URLAvailable()) {
                 log.error(
                                "ProxyWriter::rewrite(): The file  " + filePath +
                                " is written but cannot be reached at " +
                                newScriptPath);
                 return scriptUri;
               } else {
                 return PROXY_REWRITE_PREFIX + PROXIED_FILES_URI.substring(7) +
                   fileName;
               }
             } finally {
               tester.disconnect();
             }

         } catch(Exception e) {
             log.error("Failed to read the file at : "  + filePath, e);
             return scriptUri;
          }
    }

   /**
    * This method uses a URI and creates an html file name by simply omiting some characters from the URI.
    * The purpose of using the address for the file name is that the file names will be unique and map to addresses.
    * @param addr: is the address of the file
    * @newName: is the name built form the address
    */
    private static String fileNameGenerator(String addr)
    {
        String newName = CommonUtils.replaceText(addr, "/", "");
        newName = CommonUtils.replaceText(newName, "http:", "");
        newName = CommonUtils.replaceText(newName, "www.", "");
        newName = CommonUtils.replaceText(newName, ".", "");
        newName = CommonUtils.replaceText(newName, "?", "");
        newName = CommonUtils.replaceText(newName, "&", "");

        return newName.substring(0, Math.min(16, newName.length())) + ".html";
    }


   /**
    * This method parses a line recursivley and replaces all occurances of image references
    * with a proxied reference.
    * @param line - is the portion of the line or the whole line to be processed.
    * @return line - is the portion of the line or the line that has been processed.
    */
    private static String processLine(String line) throws Exception
    {
      try {
        if (line.indexOf(" src") != -1 && line.indexOf("http://") != -1) {
          String srcValue = extractURL(line);
          String srcNewValue = createProxyURL(srcValue);
          line = CommonUtils.replaceText(line, srcValue, srcNewValue);
          int firstPartIndex = line.lastIndexOf(srcNewValue) + srcNewValue.length();
          String remaining = line.substring(firstPartIndex);
          return line.substring(0,firstPartIndex) + "  " + processLine(remaining);
        } else {
          return line;
        }
      } catch(Exception e) {

        log.error("Failed to process a line : "  + line, e);
        throw e;
       }
    }

    /**
     *
     * This method takes a String (line) and parses out the value of  src attribute
     * in that string.
     * @param line - String
     * @return srcValue - String
     */
    private static String extractURL(String line)
    {
      int URLStartIndex = 0;
      int URLEndIndex = 0;
      //need this to make sure only image paths are pointed to and not href.
      int srcIndex = line.indexOf(" src");
      if (line.indexOf("https://", srcIndex) != -1) {
        return "";
      }
      if (line.indexOf("http://", srcIndex) != -1) {
        URLStartIndex = line.indexOf("http", srcIndex);
      } else {
        return "";
      }

      URLEndIndex = line.indexOf(" ", URLStartIndex);
      String srcValue = line.substring(URLStartIndex, URLEndIndex);
      return srcValue;
    }

    /**
     *
     * This method receives an image source URL and modified
     * it to be proxied.
     * @param srcValue - String
     * @return srcNewValue - String
     */
    private static String createProxyURL(String srcValue)
    {
      String srcNewValue = "";
      if (srcValue.indexOf("https://") != -1) {
        return srcValue;
      } else if (srcValue.indexOf("http://") != -1) {
        srcNewValue = CommonUtils.replaceText(srcValue, "http://",
                                              PROXY_REWRITE_PREFIX);
      }  else {
        srcNewValue = "";
      }
      return srcNewValue;
    }

}
