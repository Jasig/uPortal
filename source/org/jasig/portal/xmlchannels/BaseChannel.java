/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or withoutu
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

import  org.xml.sax.DocumentHandler;
import  org.jasig.portal.IXMLChannel;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.ChannelRuntimeProperties;
import  org.jasig.portal.PortalException;
import  org.jasig.portal.ChannelStaticData;
import  org.jasig.portal.LayoutEvent;


/**
 * A base class from which channels implementing IChannel interface can be derived.
 * Use this only if you are familiar with IChannel interface.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public abstract class BaseChannel
    implements IXMLChannel {
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;

  /**
   * put your documentation comment here
   * @return 
   */
  public ChannelRuntimeProperties getRuntimeProperties () {
    return  new ChannelRuntimeProperties();
  }

  /**
   * put your documentation comment here
   * @param ev
   */
  public void receiveEvent (LayoutEvent ev) {}

  /**
   * put your documentation comment here
   * @param sd
   * @exception PortalException
   */
  public void setStaticData (ChannelStaticData sd) {
    this.staticData = sd;
  }

  /**
   * put your documentation comment here
   * @param rd
   * @exception PortalException
   */
  public void setRuntimeData (ChannelRuntimeData rd) {
    this.runtimeData = rd;
  }

  /**
   * put your documentation comment here
   * @param out
   * @exception PortalException
   */
  public void renderXML (DocumentHandler out) {}
}



