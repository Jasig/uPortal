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
 */

package org.jasig.portal;

import javax.servlet.http.*;
import java.text.*;

/**
 * Provides methods useful for the portal.  Later on, it may be necessary
 * to create an org.jasig.portal.util package
 * and several utilities classes.
 * @author Ken Weiner
 * @version $Revision$
 */
public class UtilitiesBean extends GenericPortalBean
{
  /**
   * Prevents an html page from being cached by the browser
   * @param the servlet response object
   */
  public static void preventPageCaching (HttpServletResponse res)
  {
    try
    {
      res.setHeader("pragma", "no-cache");
      res.setHeader( "Cache-Control","no-cache" );
      res.setHeader( "Cache-Control","no-store" );
      res.setDateHeader( "Expires", 0 );
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Gets the current date/time
   * @return a formatted date and time string
   */
  public static String getDate ()
  {
    try
    {
      // Format the current time.
      SimpleDateFormat formatter = new SimpleDateFormat ("EEEE, MMM d, yyyy 'at' hh:mm a");
      java.util.Date currentTime = new java.util.Date();
      return formatter.format(currentTime);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }

    return "&nbsp;";
  }

  /**
   * Allows the hrefs in each .ssl file to be entered in one
   * of 3 ways:
   * 1) http://...
   * 2) An absolute file system path optionally beginning with file://
   *    e.g. C:\WinNT\whatever.xsl or /usr/local/whatever.xsl
   *    or file://C:\WinNT\whatever.xsl or file:///usr/local/whatever.xsl
   * 3) A path relative to the portal base dir as determined from
   *    GenericPortalBean.getPortalBaseDir()
   */
  public static String fixURI (String str)
  {
    boolean bWindows = (System.getProperty ("os.name").indexOf ("Windows") != -1) ? true : false;
    char ch0 = str.charAt (0);
    char ch1 = str.charAt (1);

    if (str.indexOf ("://") == -1 && ch1 != ':')
    {
      // Relative path was specified, so prepend portal base dir
      str = (bWindows ? "file:/" : "file://") + GenericPortalBean.getPortalBaseDir () + str;
    }
    else if (bWindows && str.startsWith ("file://"))
    {
      // Replace "file://" with "file:/" on Windows machines
      str = "file:/" + str.substring (7);
    }
    else if (ch0 == java.io.File.separatorChar || ch1 == ':')
    {
      // It's a full path without "file://"
      str = (bWindows ? "file:/" : "file://") + str;
    }

    // Handle platform-dependent strings
    str = str.replace (java.io.File.separatorChar, '/');

    return str;
  }

  public static String escapeString(String source)
  {
    StringBuffer sb = new StringBuffer ();

    for (int i = 0 ; i < source.length() ; i++)
    {
      sb.append(escapeChar(source.charAt (i)));
    }

    return sb.toString ();
  }

  private static String escapeChar(char ch)
  {
    StringBuffer sb = new StringBuffer ();
    String charRef;

    // If there is a suitable entity reference for this character, print it.
    charRef = getEntityRef (ch);

    if ( charRef != null )
    {
      sb.append ('&');
      sb.append (charRef);
      sb.append (';');
    }
    else
    {
      if(( ch >= ' ' && ch <= 0x7E && ch != 0xF7 ) || ch == '\n' || ch == '\r' || ch == '\t' )
      {
        // If the character is not printable, print as character reference.
        // Non printables are below ASCII space but not tab or line
        // terminator, ASCII delete, or above a certain Unicode threshold.
        sb.append (ch);
      }
      else
      {
        sb.append ("&#");
        sb.append (Integer.toString (ch));
        sb.append (';');
      }
    }

    return sb.toString ();
  }

  private static String getEntityRef (char ch)
  {
    // Encode special XML characters into the equivalent character references.
    // These five are defined by default for all XML documents.
    switch( ch )
    {
      case '<':
        return "lt";
      case '>':
        return "gt";
      case '"':
        return "quot";
      case '\'':
        return "apos";
      case '&':
        return "amp";
    }
    return null;
  }

  /**
   * Removes any XML unfriendly characters from a string
   */
  public static String removeSpecialChars(String source)
  {
    StringBuffer sb = new StringBuffer ();
    char ch;

    for (int i = 0 ; i < source.length() ; i++)
    {
      ch = source.charAt (i);

      if (!SpecialChar (ch))
        sb.append (ch);
    }

    return sb.toString ();
  }

  private static boolean SpecialChar (char ch)
  {
    switch( ch )
    {
      case '<':
        return true;
      case '>':
        return true;
      case '"':
        return true;
      case '\'':
        return true;
      case '&':
        return true;
    }
    return false;
  }

}