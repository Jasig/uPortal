/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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

 /**
 * Filter incorporating channel attributes for the second
 * (theme) XSLT transformation.
 * @author Peter Kharchenko
 * @version $Revision$
 */

package org.jasig.portal;

import java.util.Enumeration;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class ThemeAttributesIncorporationFilter extends SAX2FilterImpl
{
    protected ThemeStylesheetUserPreferences ssup;

    // downward
    public ThemeAttributesIncorporationFilter(ContentHandler handler, ThemeStylesheetUserPreferences prefs) {
        super(handler);
        this.ssup=prefs;
    }

    // upward
    public ThemeAttributesIncorporationFilter(XMLReader parent, ThemeStylesheetUserPreferences prefs) {
        super(parent);
        this.ssup=prefs;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // recognizing "channel"
        if (qName.equals("channel")) {
            AttributesImpl attsImpl=new AttributesImpl(atts);
            String channelSubscribeId = attsImpl.getValue("ID");
            for(Enumeration ca=ssup.getChannelAttributeNames(); ca.hasMoreElements(); ) {
                String attrName=(String) ca.nextElement();
                attsImpl.addAttribute("",attrName,attrName,"CDATA",ssup.getChannelAttributeValue(channelSubscribeId,attrName));
                //		LogService.instance().log(LogService.DEBUG,"ThemeAttributesIncorporationFilter::startElement() : adding attribute to channel="+channelID+" "+attrName+"="+ssup.getChannelAttributeValue(channelID,attrName));
            }
            super.startElement(uri,localName,qName,attsImpl);
        } else {
            super.startElement(uri,localName,qName, atts);
        }
    }
}

