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

package org.jasig.portal.xmlchannels;

import org.jasig.portal.*;
import org.apache.xalan.xslt.*;
import org.xml.sax.DocumentHandler;
import java.io.*;

/** <p>A number guessing game which asks the user to enter a number within
 * a certain range as determined by this channel's parameters.</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CNumberGuess implements IXMLChannel
{
  ChannelStaticData staticData = null;
  ChannelRuntimeData runtimeData = null;
  StylesheetSet set = null;

  private static final String fs = File.separator;
  private static final String portalBaseDir = GenericPortalBean.getPortalBaseDir ();
  String stylesheetDir = portalBaseDir + fs + "webpages" + fs + "stylesheets" + fs + "org" + fs + "jasig" + fs + "portal" + fs + "channels" + fs + "CNumberGuess";

  private int iMinNum = 0;
  private int iMaxNum = 0;
  private int iGuess = 0;
  private int iGuesses = 0;
  private int iAnswer = 0;
  private boolean bFirstTime = true;
  
  /** Constructs a CNumberGuess.
   */
  public CNumberGuess ()
  {
    this.staticData = new ChannelStaticData ();
    this.runtimeData = new ChannelRuntimeData ();
    this.set = new StylesheetSet (stylesheetDir + fs + "CNumberGuess.ssl");
    this.set.setMediaProps (portalBaseDir + fs + "properties" + fs + "media.properties");
  }

  /** Returns static channel properties to the portal
   * @return handle to subscription properties
   */
  public ChannelSubscriptionProperties getSubscriptionProperties ()
  {
    ChannelSubscriptionProperties csb = new ChannelSubscriptionProperties ();
    
    // Properties which are not specifically set here will assume default
    // values as determined by ChannelSubscriptionProperties
    csb.setName ("Number Guessing Game");
    return csb;
  }

  /** Returns channel runtime properties
   * @return handle to runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties ()
  {
    // Channel will always render, so the default values are ok
    return new ChannelRuntimeProperties ();
  }

  /** Processes layout-level events coming from the portal
   * @param ev a portal layout event
   */
  public void receiveEvent (LayoutEvent ev)
  {
    // no events for this channel
  }

  /** Receive static channel data from the portal
   * @param sd static channel data
   */
  public void setStaticData (ChannelStaticData sd)
  { 
    this.staticData = sd;
    String sMinNum = null;
    String sMaxNum = null;
    
    try
    {
      if ((sMinNum = sd.getParameter ("minNum")) != null )
        iMinNum = Integer.parseInt (sMinNum);

      if ((sMaxNum = sd.getParameter ("maxNum")) != null)
        iMaxNum = Integer.parseInt (sMaxNum);
      
      iAnswer = getRandomNumber (iMinNum, iMaxNum);
    }
    catch (NumberFormatException nfe)
    {
      iMinNum = 0;
      iMaxNum = 100;
      Logger.log (Logger.WARN, "org.jasig.portal.xmlchannels.CNumberGuess: Either " + sMinNum + " or " + sMaxNum + " (minNum, maxNum) is not a valid integer. Defaults " + iMinNum + " and " + iMaxNum + " will be used instead.");
    }
   }


  /** Receives channel runtime data from the portal and processes actions 
   * passed to it.  The names of these parameters are entirely up to the channel. 
   * @param rd handle to channel runtime data
   */
  public void setRuntimeData (ChannelRuntimeData rd)
  {
    this.runtimeData = rd;
    String sGuess = runtimeData.getParameter ("guess");   
   
    if (sGuess != null)
    {
      try
      {
        iGuess = Integer.parseInt (sGuess);
      }
      catch (NumberFormatException nfe)
      {
        // Assume that the guess was the same as last time
      }
      
      bFirstTime = false;
      iGuesses++;
    }
  }

  /** Output channel content to the portal
   * @param out a sax document handler
   */
  public void renderXML (DocumentHandler out)
  {
    String sSuggest = null;
    
    if (iGuess < iAnswer)
      sSuggest = "higher";
    else if (iGuess > iAnswer)
      sSuggest = "lower";
   
    try
    {
      if (set != null)
      {
        StringWriter w = new StringWriter ();
        w.write ("<?xml version='1.0'?>\n");
        w.write ("<content>\n");
        w.write ("  <minNum>" + iMinNum + "</minNum>\n");
        w.write ("  <maxNum>" + iMaxNum + "</maxNum>\n");
        w.write ("  <guesses>" + iGuesses + "</guesses>\n");
        w.write ("  <guess>" + iGuess + "</guess>\n");

        if (bFirstTime)
          ;
        else if (iGuess == iAnswer)
        {
          w.write ("  <answer>" + iAnswer + "</answer>\n");
          bFirstTime = true;
          iGuesses = 0;
          iAnswer = getRandomNumber (iMinNum, iMaxNum);
        }
        else
          w.write ("  <suggest>" + sSuggest + "</suggest>\n");

        w.write ("</content>\n");
        
        processXML (w.toString (), out);
      }
    } 
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e); 
    }
  }
  
  private void processXML (String sXml, DocumentHandler out) throws org.xml.sax.SAXException
  {
    XSLTInputSource xmlSource = new XSLTInputSource (new StringReader(sXml));
    XSLTInputSource xslSource = set.getStylesheet("main", runtimeData.getHttpRequest());
    XSLTResultTarget xmlResult = new XSLTResultTarget(out);

    // This allows relative paths in the stylesheet set
    // Remove in version 2.0!!!
    xslSource.setSystemId (portalBaseDir + xslSource.getSystemId());

    if (xslSource != null)
    {
      XSLTProcessor processor = XSLTProcessorFactory.getProcessor ();
      processor.setStylesheetParam("baseActionURL", processor.createXString(runtimeData.getBaseActionURL()));        
      processor.process (xmlSource, xslSource, xmlResult);
    }
    else 
      Logger.log(Logger.ERROR, "org.jasig.portal.channels.CNumberGuess: unable to find a stylesheet for rendering");
  }
  
  private int getRandomNumber (int min, int max)
  {
    return new Double ((max - min) * Math.random () + min).intValue ();
  }
}