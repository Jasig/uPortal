/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.xmlchannels;

import  org.jasig.portal.IXMLChannel;
import  org.jasig.portal.RdbmServices;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.ChannelRuntimeProperties;
import  org.jasig.portal.ChannelSubscriptionProperties;
import  org.jasig.portal.LayoutEvent;
import  org.jasig.portal.StylesheetSet;
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.GenericPortalBean;
import  org.jasig.portal.Logger;
import  org.jasig.portal.security.IPerson;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.apache.xerces.parsers.DOMParser;
import  org.apache.xerces.parsers.SAXParser;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xalan.xslt.XSLTInputSource;
import  org.apache.xalan.xslt.XSLTResultTarget;
import  org.apache.xalan.xslt.XSLTProcessor;
import  org.apache.xalan.xslt.XSLTProcessorFactory;
import  org.xml.sax.InputSource;
import  org.xml.sax.DocumentHandler;
import  org.xml.sax.EntityResolver;
import  java.sql.Connection;
import  java.sql.Statement;
import  java.sql.ResultSet;
import  java.sql.SQLException;
import  java.io.StringReader;
import  java.io.StringWriter;
import  java.util.Hashtable;
import  java.util.Enumeration;


/**
 *
 * @author Peter Kharchenko
 * @author Steven Toth
 * @author Bernie Durfee
 * @version $Revision$
 */
public class CBookmarks implements IXMLChannel {
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;

  // A DOM document where all the bookmark information will be contained
  protected DocumentImpl m_bookmarksXML;
  // This is the cached content
  String m_cachedContent = null;
  // Initialize StylesheetSet
  StylesheetSet m_styleSheetSet;
  // Define some constants to keep the state of the channel
  private final int VIEWMODE = 0;
  private final int EDITMODE = 1;
  private final int EDITNODEMODE = 3;
  private final int ADDNODEMODE = 4;
  private final int MOVENODEMODE = 5;
  private final int DELETENODEMODE = 6;
  // Start out in view mode by default
  private int m_currentState = 0;
  // Keep track of the node that the user is currently working with
  String m_activeNodeID = null;
  String m_activeNodeType = null;

  /**
   * put your documentation comment here
   */
  public CBookmarks () {
    String fs = System.getProperty("file.separator");
    // Location of the stylesheet files
    String stylesheetDir = GenericPortalBean.getPortalBaseDir() + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig"
        + fs + "portal" + fs + "channels" + fs + "CBookmarks" + fs;
    m_styleSheetSet = new StylesheetSet(stylesheetDir + "CBookmarks.ssl");
    m_styleSheetSet.setMediaProps(GenericPortalBean.getPortalBaseDir() + "properties" + fs + "media.properties");
  }

  /**
   * put your documentation comment here
   * @return
   */
  private DocumentImpl getBookmarkXML () {
    Connection connection = null;
    // Return the cached bookmarks if they've already been read in
    if (m_bookmarksXML != null) {
      return  (m_bookmarksXML);
    }
    try {
      String inputXML = null;
      // Create a new parser for the incoming bookmarks document
      DOMParser domParser = new DOMParser();
      // Get a connection to the database
      connection = getConnection();
      // Get the current user's ID
      String userName = runtimeData.getPerson().getID();
      // Attempt to retrieve the user's bookmark's
      String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM PORTAL_BOOKMARKS WHERE PORTAL_USER_ID=" + getUserId(userName);
      // Get the result set
      ResultSet rs = connection.createStatement().executeQuery(query);
      if (rs.next()) {
        // If a result came back then use that for the XML...
        inputXML = rs.getString("BOOKMARK_XML");
      }
      if (inputXML == null || inputXML.length() == 0) {
        // ...or else use the bookmarks from the default user
        inputXML = getDefaultBookmarks();
      }
      // Turn validation on for the DOM parser to make sure it reads the DTD
      domParser.setFeature("http://xml.org/sax/features/validation", true);
      domParser.setEntityResolver(new DTDResolver("xbel-1.0.dtd"));
      // Parse the XML document containing the user's bookmarks
      domParser.parse(new InputSource(new StringReader(inputXML)));
      // Cache the bookmarks DOM locally
      m_bookmarksXML = (DocumentImpl)domParser.getDocument();
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    } finally {
      // Release the database connection
      if (connection != null) {
        releaseConnection(connection);
      }
    }
    // Return what is cached
    return  (m_bookmarksXML);
  }

  /**
   * Retrieve the XML for the default set of bookmarks
   * @return XML Representation of the default user bookmarks
   */
  private String getDefaultBookmarks () {
    Connection connection = null;
    String inputXML = null;
    try {
      // Get a connection to the database
      connection = getConnection();
      // Get the bookmarks for the 'default' user
      String userName = "default";
      String query = "SELECT BOOKMARK_XML, PORTAL_USER_ID FROM PORTAL_BOOKMARKS WHERE PORTAL_USER_ID=" + getUserId(userName);
      // Try to get the 'default' bookmarks from the database
      ResultSet rs = connection.createStatement().executeQuery(query);
      if (rs.next()) {
        // Use the 'default' user's bookmarks...
        inputXML = rs.getString("BOOKMARK_XML");
      }
      else {
        // Generate the XML here as a last resort
        inputXML = "<?xml version=\"1.0\"?>" + "<!DOCTYPE xbel PUBLIC \"+//IDN python.org//DTD XML Bookmark Exchange Language 1.0//EN//XML\" \"http://www.python.org/topics/xml/dtds/xbel-1.0.dtd\">"
            + "<xbel>" + "  <title>Default Bookmarks</title>" + "  <info>" + "    <metadata owner=\'" + runtimeData.getPerson().getID()
            + "\'/>" + "  </info>" + "</xbel>";
        Logger.log(Logger.WARN, "CBookmarks.getDefaultBookmarks(): Could not find bookmarks for 'default' user");
      }
      // Now add a row to the database for the user
      String insert = "INSERT INTO PORTAL_BOOKMARKS (ID, PORTAL_USER_ID, BOOKMARK_XML) " + "VALUES (" + getUserId(runtimeData.getPerson().getID())
          + ", " + getUserId(runtimeData.getPerson().getID()) + ",'" + inputXML + "')";
      //Logger.log(Logger.DEBUG, insert);
      connection.createStatement().executeUpdate(insert);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    } finally {
      if (connection != null) {
        releaseConnection(connection);
      }
      if (inputXML == null) {
        // ...or else just start with an empty set of bookmarks
        Logger.log(Logger.ERROR, "CBookmarks.getDefaultBookmarks() - Could not retrieve default bookmark xml, using blank xml.");
        inputXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xbel></xbel>";
      }
    }
    return  (inputXML);
  }

  /**
   * put your documentation comment here
   */
  protected void saveXML () {
    Connection connection = null;
    if (m_bookmarksXML == null) {
      return;
    }
    try {
      StringWriter stringWriter = new StringWriter();
      // Serialize the DOM tree to a string
      XMLSerializer xmlSerializer = new XMLSerializer(stringWriter, new OutputFormat(m_bookmarksXML));
      xmlSerializer.serialize(m_bookmarksXML);
      // Get a connection to the database
      connection = getConnection();
      String update = "UPDATE PORTAL_BOOKMARKS SET BOOKMARK_XML = '" + stringWriter.toString() + "' WHERE PORTAL_USER_ID = "
          + getUserId(runtimeData.getPerson().getID());
      connection.createStatement().executeUpdate(update);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    } finally {
      releaseConnection(connection);
    }
  }

  /**
   * Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  public void setStaticData (ChannelStaticData sd)
  {
    this.staticData = sd;
  }

  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
  }

  /**
   * Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (LayoutEvent ev)
  {
    // no events for this channel
  }

  /** Returns static channel properties to the portal
   * @return handle to subscription properties
   */
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();

    // Properties which are not specifically set here will assume default
    // values as determined by ChannelSubscriptionProperties
    csb.setDetachable (false);
    csb.setName ("My Bookmarks");
    return csb;
  }

  /**
   *  Render the user's layout based on the current state of the channel
   */
  public void renderXML (DocumentHandler out) {
    // Retrieve the command passed in by the user
    String command = runtimeData.getParameter("command");
    // Output the cached content if the user has not interacted
    if (command == null && m_cachedContent != null) {
      outputContent(out);
      return;
    }
    if (command != null) {
      if (command.equals("fold")) {
        // Get the ID of the specified folder to close
        String folderID = runtimeData.getParameter("ID");
        if (folderID != null) {
          Element folderElement = getBookmarkXML().getElementById(folderID);
          if (folderElement != null && folderElement.getNodeName().equals("folder")) {
            folderElement.setAttribute("folded", "yes");
          }
        }
      }
      else if (command.equals("unfold")) {
        // Get the ID of the specified folder to open
        String folderID = runtimeData.getParameter("ID");
        if (folderID != null) {
          Element folderElement = getBookmarkXML().getElementById(folderID);
          if (folderElement != null && folderElement.getNodeName().equals("folder")) {
            folderElement.setAttribute("folded", "no");
          }
        }
      }
      else if (command.equals("View")) {
        // Switch to view mode
        m_currentState = VIEWMODE;
        m_activeNodeID = null;
      }
      else if (command.equals("Edit")) {
        // Switch to edit mode
        m_currentState = EDITMODE;
        m_activeNodeID = null;
      }
      else if (command.equals("AddBookmark")) {
        if (m_currentState != ADDNODEMODE) {
          // Switch to add bookmark mode
          m_currentState = ADDNODEMODE;
          m_activeNodeID = null;
          m_activeNodeType = "bookmark";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null && submitButton.equals("Cancel")) {
            // The user pressed the cancel button so return to view mode
            m_activeNodeID = null;
            m_activeNodeType = null;
            m_currentState = VIEWMODE;
          }
          else if (submitButton != null && submitButton.equals("Add")) {
            // Check for the incoming parameters
            String bookmarkTitle = runtimeData.getParameter("BookmarkTitle");
            String bookmarkURL = runtimeData.getParameter("BookmarkURL");
            String bookmarkDesc = runtimeData.getParameter("BookmarkDescription");
            String folderID = runtimeData.getParameter("FolderRadioButton");
            if (bookmarkTitle == null || bookmarkTitle.length() < 1) {}
            else if (bookmarkURL == null || bookmarkURL.length() < 1) {}
            else if (folderID == null || folderID.length() < 1) {}
            else {
              Element folderElement;
              if (folderID.equals("RootLevel")) {
                folderElement = (Element)m_bookmarksXML.getElementsByTagName("xbel").item(0);
              }
              else {
                folderElement = m_bookmarksXML.getElementById(folderID);
              }
              if (folderElement == null) {}
              else {
                // Build the bookmark XML DOM
                Element bookmarkElement = m_bookmarksXML.createElement("bookmark");
                bookmarkElement.setAttribute("href", bookmarkURL);
                bookmarkElement.setAttribute("id", createUniqueID());
                // Create the title element
                Element titleElement = m_bookmarksXML.createElement("title");
                titleElement.appendChild(m_bookmarksXML.createTextNode(bookmarkTitle));
                bookmarkElement.appendChild(titleElement);
                // Create the desc element
                Element descElement = m_bookmarksXML.createElement("desc");
                descElement.appendChild(m_bookmarksXML.createTextNode(bookmarkDesc));
                bookmarkElement.appendChild(descElement);
                folderElement.appendChild(bookmarkElement);
                // Notify the DOM of the new ID
                m_bookmarksXML.putIdentifier(bookmarkElement.getAttribute("id"), bookmarkElement);
                // The user pressed the cancel button so return to view mode
                m_activeNodeID = null;
                m_activeNodeType = null;
                // Return to view mode
                m_currentState = VIEWMODE;
                // Clear the content cache
                m_cachedContent = null;
                // Save the user's XML
                saveXML();
              }
            }
          }
        }
      }
      else if (command.equals("AddFolder"))
      {
        if (m_currentState != ADDNODEMODE) {
          // Switch to add bookmark mode
          m_currentState = ADDNODEMODE;
          m_activeNodeID = null;
          m_activeNodeType = "folder";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null && submitButton.equals("Cancel")) {
            // The user pressed the cancel button so return to view mode
            m_activeNodeID = null;
            m_activeNodeType = null;
            m_currentState = VIEWMODE;
          }
          else if (submitButton != null && submitButton.equals("Add")) {
            // Check for the incoming parameters
            String folderTitle = runtimeData.getParameter("FolderTitle");
            String folderID = runtimeData.getParameter("FolderRadioButton");
            if (folderTitle == null || folderTitle.length() < 1) {}
            else if (folderID == null || folderID.length() < 1) {}
            else {
              Element folderElement;
              if (folderID.equals("RootLevel")) {
                folderElement = (Element)m_bookmarksXML.getElementsByTagName("xbel").item(0);
              }
              else {
                folderElement = m_bookmarksXML.getElementById(folderID);
              }
              if (folderElement == null) {}
              else {
                // Build the new folder XML node
                Element newFolderElement = m_bookmarksXML.createElement("folder");
                newFolderElement.setAttribute("id", createUniqueID());
                // Create the title element
                Element titleElement = m_bookmarksXML.createElement("title");
                titleElement.appendChild(m_bookmarksXML.createTextNode(folderTitle));
                newFolderElement.appendChild(titleElement);
                folderElement.appendChild(newFolderElement);
                // Notify the DOM of the new ID
                m_bookmarksXML.putIdentifier(newFolderElement.getAttribute("id"), newFolderElement);
                // The user pressed the cancel button so return to view mode
                m_activeNodeID = null;
                m_activeNodeType = null;
                // Return to view mode
                m_currentState = VIEWMODE;
                // Clear the content cache
                m_cachedContent = null;
                // Save the user's XML
                saveXML();
              }
            }
          }
        }
      }
      else if (command.equals("MoveNode")) {
        m_activeNodeID = runtimeData.getParameter("ID");
        m_currentState = MOVENODEMODE;
      }
      else if (command.equals("EditNode")) {
        m_activeNodeID = runtimeData.getParameter("ID");
        m_currentState = EDITNODEMODE;
      }
      else if (command.equals("DeleteBookmark")) {
        if (m_currentState != DELETENODEMODE) {
          m_currentState = DELETENODEMODE;
          m_activeNodeType = "bookmark";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null) {
            if (submitButton.equals("Cancel")) {
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("Delete")) {
              // Run through the passed in parameters and delete the bookmarks
              Enumeration e = runtimeData.keys();
              while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                if (key.startsWith("BookmarkCheckbox#")) {
                  String bookmarkID = key.substring(17);
                  Element bookmarkElement = m_bookmarksXML.getElementById(bookmarkID);
                  if (bookmarkElement != null && bookmarkElement.getNodeName().equals("bookmark")) {
                    bookmarkElement.getParentNode().removeChild(bookmarkElement);
                  }
                }
              }
              // Clear the content cache
              m_cachedContent = null;
              saveXML();
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("ConfirmDelete")) {}
          }
        }
      }
      else if (command.equals("DeleteFolder")) {
        if (m_currentState != DELETENODEMODE) {
          m_currentState = DELETENODEMODE;
          m_activeNodeType = "folder";
        }
        else {
          String submitButton = runtimeData.getParameter("SubmitButton");
          if (submitButton != null) {
            if (submitButton.equals("Cancel")) {
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("Delete")) {
              // Run through the passed in parameters and delete the bookmarks
              Enumeration e = runtimeData.keys();
              while (e.hasMoreElements()) {
                String key = (String)e.nextElement();
                if (key.startsWith("FolderCheckbox#")) {
                  // The ID should come after the FolderCheckbox# part
                  String bookmarkID = key.substring(15);
                  // Find the folder in the DOM tree
                  Element folderElement = m_bookmarksXML.getElementById(bookmarkID);
                  // Remove the folder from the DOM tree
                  if (folderElement != null && folderElement.getNodeName().equals("folder")) {
                    folderElement.getParentNode().removeChild(folderElement);
                  }
                }
              }
              // Clear the content cache
              m_cachedContent = null;
              saveXML();
              m_currentState = VIEWMODE;
              m_activeNodeType = null;
            }
            else if (submitButton.equals("ConfirmDelete")) {}
          }
        }
      }
    }
    try {
      // Render content based on the current state of the channel
      switch (m_currentState) {
        case VIEWMODE:
          renderViewModeXML(out);
          break;
        case EDITMODE:
          renderEditModeXML(out);
          break;
        case EDITNODEMODE:
          renderEditNodeXML(out);
          break;
        case ADDNODEMODE:
          renderAddNodeXML(out);
          break;
        case MOVENODEMODE:
          renderMoveNodeXML(out);
          break;
        case DELETENODEMODE:
          renderDeleteNodeXML(out);
          break;
      }
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderViewModeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    transformXML(out, "view_mode", getBookmarkXML());
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderEditModeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "EditMode");
    transformXML(out, "edit_mode", getBookmarkXML(), parameters);
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderEditNodeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "DeleteNode");
    transformXML(out, "delete_node", getBookmarkXML(), parameters);
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderAddNodeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    Hashtable parameters = new Hashtable(1);
    if (m_activeNodeType == null) {
      Logger.log(Logger.ERROR, "CBookmarks.renderAddNodeXML: No active node type has been set");
      renderViewModeXML(out);
    }
    else if (m_activeNodeType.equals("bookmark")) {
      parameters.put("EditMode", "AddBookmark");
      transformXML(out, "add_node", getBookmarkXML(), parameters);
    }
    else if (m_activeNodeType.equals("folder")) {
      parameters.put("EditMode", "AddFolder");
      transformXML(out, "add_node", getBookmarkXML(), parameters);
    }
    else {
      Logger.log(Logger.ERROR, "CBookmarks.renderAddNodeXML: Unknown active node type - " + m_activeNodeType);
      renderViewModeXML(out);
    }
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderMoveNodeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    Hashtable parameters = new Hashtable(2);
    parameters.put("NodeType", m_activeNodeType);
    parameters.put("TreeMode", "MoveNode");
    transformXML(out, "move_node", getBookmarkXML(), parameters);
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception org.xml.sax.SAXException
   */
  private void renderDeleteNodeXML (DocumentHandler out) throws org.xml.sax.SAXException {
    Hashtable parameters = new Hashtable(1);
    if (m_activeNodeType == null) {
      Logger.log(Logger.ERROR, "CBookmarks.renderDeleteNodeXML: No active node type has been set");
      renderViewModeXML(out);
    }
    else if (m_activeNodeType.equals("bookmark")) {
      parameters.put("EditMode", "DeleteBookmark");
      transformXML(out, "delete_node", getBookmarkXML(), parameters);
    }
    else if (m_activeNodeType.equals("folder")) {
      parameters.put("EditMode", "DeleteFolder");
      transformXML(out, "delete_node", getBookmarkXML(), parameters);
    }
    else {
      Logger.log(Logger.ERROR, "CBookmarks.renderDeleteNodeXML: Unknown active node type - " + m_activeNodeType);
      renderViewModeXML(out);
    }
  }

  /**
   * put your documentation comment here
   * @param out
   */
  private void outputContent (DocumentHandler out) {
    SAXParser saxParser = new SAXParser();
    saxParser.setDocumentHandler(out);
    try {
      saxParser.parse(new InputSource(new StringReader(m_cachedContent)));
    } catch (java.io.IOException ioe) {
      // Clear the cache because there's something wrong with it
      m_cachedContent = null;
      Logger.log(Logger.ERROR, ioe);
    } catch (org.xml.sax.SAXException se) {
      // Clear the cache because there's something wrong with it
      m_cachedContent = null;
      Logger.log(Logger.ERROR, se);
    }
  }

  /**
   * put your documentation comment here
   * @param out
   * @param styleSheetName
   * @param inputXML
   */
  private void transformXML (DocumentHandler out, String styleSheetName, DocumentImpl inputXML) {
    transformXML(out, styleSheetName, inputXML, null);
  }

  /**
   * put your documentation comment here
   * @param out
   * @param styleSheetName
   * @param inputXML
   * @param parameters
   */
  private void transformXML (DocumentHandler out, String styleSheetName, DocumentImpl inputXML, Hashtable parameters) {
    try {
      XSLTInputSource stylesheet = m_styleSheetSet.getStylesheet(styleSheetName, runtimeData.getHttpRequest());
      if (stylesheet != null) {
        StringWriter stringWriter = new StringWriter();
        XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new org.apache.xalan.xpath.xdom.XercesLiaison());
        if (parameters != null) {
          Enumeration keys = parameters.keys();
          while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            processor.setStylesheetParam(key, processor.createXString((String)parameters.get(key)));
          }
        }
        // Pass the baseActionURL down to the stylesheet
        processor.setStylesheetParam("baseActionURL", processor.createXString(runtimeData.getBaseActionURL()));

        // Pass the location of the image files down to the stylesheet
        //String imagesURL = "stylesheets/org/jasig/portal/channels/CBookmarks/";
        //processor.setStylesheetParam("imagesURL", processor.createXString(imagesURL));

        // Perform the XSLT transformation and store the result in a string writer
        processor.process(new XSLTInputSource(inputXML), stylesheet, new XSLTResultTarget(stringWriter));
        // Cache the result of the XSLT transformation
        m_cachedContent = stringWriter.toString();
        // Display the content to the user
        outputContent(out);
      }
      else {
        Logger.log(Logger.ERROR, "BookmarksChannel.processXML() - Unable to load stylesheet: " + styleSheetName);
      }
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * put your documentation comment here
   * @return
   */
  private String createUniqueID () {
    String uniqueID = "n" + System.currentTimeMillis();
    while (m_bookmarksXML.getElementById(uniqueID) != null) {
      uniqueID = "n" + System.currentTimeMillis();
    }
    return  (uniqueID);
  }

  /**
   * put your documentation comment here
   * @param url
   * @return
   */
  private static String makeUrlSafe (String url) {
    // Return if the url is correctly formed
    if (url.toLowerCase().startsWith("http://")) {
      return  (url);
    }
    // Make sure the URL is well formed
    if (url.toLowerCase().startsWith("http:/")) {
      url = url.substring(0, 6) + "/" + url.substring(7);
      return  (url);
    }
    // If it's a mail link then be sure mailto: is on the front
    if (url.indexOf('@') != -1) {
      if (!url.toLowerCase().startsWith("mailto:")) {
        url = "mailto:" + url;
      }
      return  (url);
    }
    // Make sure http:// is on the front
    url = "http://" + url;
    return  (url);
  }

  /**
   * put your documentation comment here
   * @return
   */
  private Connection getConnection () {
    try {
      RdbmServices rdbmServices = new RdbmServices();
      return  (rdbmServices.getConnection());
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
      return  (null);
    }
  }

  /**
   * put your documentation comment here
   * @param connection
   */
  private void releaseConnection (Connection connection) {
    try {
      RdbmServices rdbmServices = new RdbmServices();
      rdbmServices.releaseConnection(connection);
    } catch (Exception e) {
      Logger.log(Logger.ERROR, e);
    }
  }

  private int getUserId (String userName) throws SQLException
  {
    Connection connection = getConnection();
    String query = "SELECT ID FROM PORTAL_USERS WHERE USER_NAME='" + userName + "'";
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    rs.next();
    int userid = rs.getInt(1);
    releaseConnection(connection);
    return userid;
  }

  private String getUserName (int userId) throws SQLException
  {
    Connection connection = getConnection();
    String query = "SELECT USER_NAME FROM PORTAL_USERS WHERE ID=" + userId;
    Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    rs.next();
    String userName = rs.getString(1);
    releaseConnection(connection);
    return userName;
  }

  /**
   * Provides a means to resolve uPortal DTDs, stolen from 2.0
   * @author Peter Kharchenko, peterk@interactivebusiness.com
   * @author Ken Weiner, kweiner@interactivebusiness.com
   * @version $Revision$
   */
  public class DTDResolver implements EntityResolver
  {
    private String pathToDtd = org.jasig.portal.UtilitiesBean.getPortalBaseDir() + "webpages" + java.io.File.separator + "dtd" + java.io.File.separator;
    private String dtdName = null;

    /**
     * Constructor for DTDResolver
     */
    public DTDResolver ()
    {
    }

    /**
     * Constructor for DTDResolver
     * @param dtdName the name of the dtd
     */
    public DTDResolver (String dtdName)
    {
      this.dtdName = dtdName;
    }

    /**
     * Sets up a new input source based on the dtd specified in the xml document
     * @param publicId the public ID
     * @param systemId the system ID
     * @return an input source based on the dtd specified in the xml document
     */
    public InputSource resolveEntity (String publicId, String systemId)
    {
      java.io.FileReader fr = null;
      InputSource inSrc = null;

      try
      {
        if (systemId != null)
        {
          if (dtdName != null && systemId.indexOf(dtdName) != -1)
            fr = new java.io.FileReader(pathToDtd + dtdName);

          inSrc = new InputSource(fr);
        }
      }
      catch (java.io.FileNotFoundException fnfe)
      {
        Logger.log(Logger.ERROR, "Problem opening " + dtdName + ". " + fnfe.getMessage());
      }

      return inSrc;
    }
  }
}



