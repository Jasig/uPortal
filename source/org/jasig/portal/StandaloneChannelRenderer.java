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


package  org.jasig.portal;


import java.io.*;
import java.util.*;
import java.lang.SecurityManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import java.security.AccessController;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.UPFileSpec;
import org.xml.sax.*;

import  org.jasig.portal.serialize.*;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * StandaloneChannelRenderer is meant to be used as a base class for channels
 * that might be rendered outside of the standard user-layout driven scheme.
 * (for example CSelectSystemProfile).
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */

public class StandaloneChannelRenderer extends BaseChannel {
    private StylesheetSet set;
    private MediaManager mediaM;
    private String channelName;
    private PortalControlStructures pcs;
    private BrowserInfo binfo;
    private boolean hasEdit = false;
    private boolean hasAbout = false;
    private boolean hasHelp = false;
    private long timeOut;                 // 10 seconds is the default timeout value
    private boolean dataIsSet=false;
    private static final String chanID="singleton";
    private static final String fs = File.separator;
    private static final String relativeSSLLocation = "/org/jasig/portal/tools/ChannelServlet/ChannelServlet.ssl";


    /**
     * Initializes the channel and calls setStaticData() on the channel.
     * @param params a hastable of channel publish/subscribe parameters (<parameter> elements
     * @param channelName channel name
     * @param hasHelp determines if the channel supports "help" layout event
     * @param hasAbout determines if the channel supports "about" layout event
     * @param hasEdit determines if the channel supports "edit" layout event
     * @param timeOut channel timeout value in milliseconds
     * @param person a user IPerson object
     */
    public void initialize(Hashtable params,String channelName,boolean hasHelp, boolean hasAbout, boolean hasEdit, long timeOut,IPerson person) throws PortalException {
	this.set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), relativeSSLLocation));
        String mediaPropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/media.properties");
        String mimePropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/mime.properties");
        String serializerPropsUrl = ResourceLoader.getResourceAsURLString(this.getClass(), "/properties/serializer.properties");
	this.set.setMediaProps(mediaPropsUrl);
        this.mediaM = new MediaManager(mediaPropsUrl, mimePropsUrl, serializerPropsUrl);
	this.channelName=channelName;
	this.hasHelp=hasHelp;
	this.hasAbout=hasAbout;
	this.hasEdit=hasEdit;
	this.timeOut=timeOut;
	this.pcs=pcs;

        ChannelStaticData sd = new ChannelStaticData ();
        sd.setChannelSubscribeId (chanID);
        sd.setTimeout (timeOut);
        sd.setParameters (params);
        // get person object from UsreLayoutManager
        sd.setPerson(person);
	this.setStaticData(sd);
    }

    /**
     * This request will cause setRuntimeData() method called on the channel. If this method is invoked,
     * the render() method, which usually invokes setRuntimeData() method will omit the call.
     * @param req http request
     * @param res http response
     */
    public void prepare(HttpServletRequest req) throws Exception {
	if(this instanceof IPrivilegedChannel) {
	    ((IPrivilegedChannel) this).setPortalControlStructures(pcs);
	}
	this.setRuntimeData(getRuntimeData(req));
	dataIsSet=true;
    }


    /**
     * This method will output channel content into the HttpServletResponse's
     * out stream. Note that setRuntimeData() method is called only if there was
     * no prior call to prepare() method.
     * @param req http request
     * @param res http response
     */
    public void render(HttpServletRequest req,HttpServletResponse res) throws Throwable {
	ChannelRuntimeData rd=null;
	if(!dataIsSet) {
	    if(this instanceof IPrivilegedChannel) {
		((IPrivilegedChannel) this).setPortalControlStructures(pcs);
	    }
	    rd=getRuntimeData(req);
	} else {
	    dataIsSet=false;
	}

	// start rendering
	ChannelRenderer cr = new ChannelRenderer (this,rd);
        cr.setTimeout (timeOut);
        cr.startRendering ();

	// set the output mime type
	res.setContentType(mediaM.getReturnMimeType(req));
	// set up the serializer
	BaseMarkupSerializer ser = mediaM.getSerializer(mediaM.getMedia(req), res.getWriter());
	ser.asContentHandler();
	// get the framing stylesheet
	String xslURI = ResourceLoader.getResourceAsURLString(this.getClass(), set.getStylesheetURI(req));
	try {
            TransformerHandler th=XSLT.getTransformerHandler(xslURI);
            th.setResult(new SAXResult(ser));
	    org.xml.sax.helpers.AttributesImpl atl = new org.xml.sax.helpers.AttributesImpl();
	    atl.addAttribute("","name","name", "CDATA", channelName);
	    // add other attributes: hasHelp, hasAbout, hasEdit
            th.startDocument();
	    th.startElement("","channel","channel", atl);
	    ChannelSAXStreamFilter custodian = new ChannelSAXStreamFilter(th);
	    int out=cr.outputRendering(custodian);
	    if(out==cr.RENDERING_TIMED_OUT) {
		throw new InternalTimeoutException("The channel has timed out");
	    }
	    th.endElement("","channel","channel");
	    th.endDocument();
	} catch (InternalPortalException ipe) {
	    throw ipe.getException();
	}
    }

    private ChannelRuntimeData getRuntimeData(HttpServletRequest req) {
	// construct runtime data
        this.binfo=new BrowserInfo(req);

        Hashtable targetParams = new Hashtable();
        UPFileSpec upfs=new UPFileSpec(req);
	String channelTarget = upfs.getTargetNodeId();

        LogService.instance().log(LogService.DEBUG,"StandaloneRenderer::render() : channelTarget=\""+channelTarget+"\".");
        Enumeration en = req.getParameterNames();
        if (en != null) {
            while (en.hasMoreElements()) {
                String pName= (String) en.nextElement();
                Object[] val= (Object[]) req.getParameterValues(pName);
                if (val == null) {
                    val = ((PortalSessionManager.RequestParamWrapper)req).getObjectParameterValues(pName);
                }
                targetParams.put (pName, val);
            }
        }

	ChannelRuntimeData rd= new ChannelRuntimeData();
	rd.setBrowserInfo(binfo);
	if(channelTarget!=null && chanID.equals(channelTarget)) {
	    rd.setParameters(targetParams);
        }

        try {
            rd.setUPFile(new UPFileSpec(null,UPFileSpec.RENDER_METHOD,"servletRoot",chanID,null));
        } catch (Exception e) {
            LogService.instance().log(LogService.DEBUG,"StandaloneRenderer::render() : unable to generate baseActionURL. "+e);
        }

	return rd;

    }
}
