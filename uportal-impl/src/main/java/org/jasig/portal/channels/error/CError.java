/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels.error;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.IResetableChannel;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.MediaManager;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.channels.error.error2xml.IThrowableToElement;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.XML;
import org.jasig.portal.utils.XSLT;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * CError is the error channel, also known as the null channel; it is designed
 * to render in place of other channels when something goes wrong.
 * <p>
 * Possible conditions when CError is invoked are:
 * <ul>
 * <li>Channel has thrown a Throwable from one of the IChannel or
 * IPrivilegedChannel methods.</li>
 * <li>Channel has timed out on rendering and was terminated.</li>
 * <li>uPortal has rejected a channel for some reason. In this case a general
 * message is constructed by the portal.</li>
 * </ul>
 * 
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5.  Prior to 2.5, CError existed only as org.jasig.portal.channels.CError.
 */
public final class CError extends BaseChannel implements IPrivilegedChannel, ICacheable, ICharacterChannel {

    private static final Log log = LogFactory.getLog(CError.class);

    /**
     * An ErrorDocument representing the error about which we are reporting and
     * providing a source for XML to be rendered by our XSLT.
     */
    private ErrorDocument errorDocument = new ErrorDocument();
    
    /**
     * The channel instance that failed.
     */
    private IChannel targetChannel = null;

    /**
     * CError is a placeholder when it is taking the place of a channel that no
     * longer exists or that the user doesn't have permission to render. CError
     * is not a placeholder when it represents the failure of a channel that
     * actually tried to render.
     */
    private boolean placeHolder = false;

    /**
     * True if we should display the stack trace of the stored Throwable, if
     * any, at rendering.
     */
    private boolean showStackTrace = false;

    /**
     * The title of the stylesheet we should use to render.
     */
    private String ssTitle = null;

    private PortalControlStructures portcs;

    /**
     * The location of our our .ssl file.
     */
    private static final String sslLocation = "CError/CError.ssl";

    private static final MediaManager MEDIAMANAGER=MediaManager.getMediaManager(true);

    /**
     * Construct an uninitialized instance of the CError channel.
     */
    public CError() {
        // inject into our ErrorDocument the configured IThrowableToElement that
        // will translate from Throwables to XML that we can render

        try {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            IThrowableToElement throwableToElement = (IThrowableToElement) applicationContext.getBean("throwableToElement", IThrowableToElement.class);

            this.errorDocument.setThrowableToElement(throwableToElement);
        }
        catch (Exception e) {
            // do not allow a Beans failure to break CError
            log.warn("Failed to retrieve mapping from throwables to Elements for CError rendering from the WebApplicationContext, the default mapping will be used.", e);
            // since our ErrorDocument has a default mapping from Throwables to Elements
            // we can fall back on that default by not doing anything.
        }
    }

    /**
     * Construct an instance of the Error channel representing a failure to
     * render of a particular subscribed channel for reason of having thrown a
     * Throwable.
     * 
     * @param errorCode one of the static error codes of this class
     * @param throwable cause of failed channel's failure
     * @param channelSubscribeId identifies the failed channel
     * @param channelInstance the failed channel
     */
    public CError(ErrorCode errorCode, Throwable throwable, String channelSubscribeId, IChannel channelInstance) {
        this();
        
        if (log.isTraceEnabled()) {
            log.trace("CError(" + errorCode + ", throwable=[" + throwable
                    + "], chanSubId=" + channelSubscribeId
                    + ", channelInstance=[" + channelInstance + "]");
        }

        
        this.errorDocument.setChannelSubscribeId(channelSubscribeId);
        this.errorDocument.setThrowable(throwable);
        this.targetChannel = channelInstance;
        this.errorDocument.setCode(errorCode);

        if (log.isTraceEnabled()) {
            log.trace("Instantiated CError: " + this);
        }
    }

    /**
     * Instantiate a CError representing a particular channel's failure,
     * including a message and errorCode, but not a Throwable.
     * 
     * @param errorCode one of the static error codes of this class
     * @param message describes error
     * @param channelSubscribeId identifies failed channel
     * @param channelInstance failed channel
     */
    public CError(ErrorCode errorCode, String message, String channelSubscribeId, IChannel channelInstance) {
        this();
        
        if (log.isTraceEnabled()) {
            log.trace("CError(" + errorCode + ", message=[" + message + 
                    "], chanSubId=" + channelSubscribeId + 
                    ", channelInstance=[" + channelInstance + "]");
        }
        
        this.errorDocument.setChannelSubscribeId(channelSubscribeId);
        this.targetChannel = channelInstance;
        this.errorDocument.setCode(errorCode);
        this.errorDocument.setMessage(message);
        
        if (log.isTraceEnabled()) {
            log.trace("Instantiated CError: " + this);
        }
    }

    /**
     * Instantiate a CError instance representing the failure of some particular
     * channel, including an error code, message, and the Throwable.
     * 
     * @param errorCode one of the static error codes of this class
     * @param exception thrown by the failed channel
     * @param channelSubscribeId identifies failed channel
     * @param channelInstance the failed channel instance
     * @param message message describing failure
     */
    public CError(ErrorCode errorCode, Throwable exception, String channelSubscribeId, IChannel channelInstance, String message) {
        this(errorCode, exception, channelSubscribeId, channelInstance);
        this.errorDocument.setMessage(message);
        
        if (log.isTraceEnabled()) {
            log.trace("Instantiated CError: " + this);
        }
    }

    /**
     * Resets internal state of CError.
     * 
     * @param errorCode new errorCode value
     * @param throwable new stored Throwable
     * @param channelSubscribeId new channelSubscribeId
     * @param channelInstance new failed channel
     * @param message new failure message
     */
    private void resetCError(ErrorCode errorCode, Throwable throwable, String channelSubscribeId, IChannel channelInstance, String message) {
        this.errorDocument.setCode(errorCode);
        this.errorDocument.setThrowable(throwable);
        this.errorDocument.setChannelSubscribeId(channelSubscribeId);

        this.targetChannel = channelInstance;

        this.errorDocument.setMessage(message);
        
        if (log.isTraceEnabled()) {
            log.trace("Reset CError to: " + this);
        }
    }

    public void setPortalControlStructures(PortalControlStructures pcs) {
        this.portcs = pcs;
    }
    
    public void receiveEvent(PortalEvent ev) {
        if (targetChannel != null) {
            if (this.targetChannel instanceof IPrivilegedChannel) {
                ((IPrivilegedChannel) this.targetChannel).setPortalControlStructures(this.portcs);
            }

            // propagate the portal events to the normal channel
            targetChannel.receiveEvent(ev);
        }
        super.receiveEvent(ev);
    }


    /*
     * This is so CError can be used by getUserLayout() as a placeholder for
     * channels that have either been deleted from the portal database or the
     * users permission to use the channel has been removed (permanently or
     * temporarily).
     */
    public void setStaticData(ChannelStaticData sd) {
        if (log.isTraceEnabled()) {
            log.trace("setStaticData(" + sd + ")");
        }
        if (sd == null) {
            log.error("ChannelStaticData argument to setStaticData() illegally null.");
            return;
        }
        
        try {
            this.errorDocument.setMessage(sd.getParameter("CErrorMessage"));
            this.errorDocument.setChannelSubscribeId(sd.getParameter("CErrorChanId"));
            
            final String value = sd.getParameter("CErrorErrorId");
            if (value != null) {
                this.errorDocument.setCode(ErrorCode.codeForInt(Integer.parseInt(value)));
            }
            this.placeHolder = true; // Should only get here if we are a
                                            // "normal channel"
        }
        catch (Throwable t) {
            log.error("Error setting static data of CError instance", t);
        }
    }
    
    protected void renderChannel(IChannel channel, ContentHandler contentHandler, PrintWriter printWriter) throws Exception {
        if (contentHandler != null) {
            channel.renderXML(contentHandler);
        }
        else {
            if (channel instanceof ICharacterChannel) {
                ((ICharacterChannel) channel).renderCharacters(printWriter);
            }
            else {
                final IUserPreferencesManager userPreferencesManager = this.portcs.getUserPreferencesManager();
                final ThemeStylesheetDescription tsd = userPreferencesManager.getThemeStylesheetDescription();
                final String serializerName = tsd.getSerializerName();
                
                final BaseMarkupSerializer serOut = MEDIAMANAGER.getSerializerByName(serializerName, printWriter);
                channel.renderXML(serOut);
            }
        }
    }
    
    /**
     * Logic common to both renderXml and renderCharacters for handling a request for an errored out channel.
     */
    protected void doCommonErrorHandling(ContentHandler contentHandler, PrintWriter printWriter) {
        // runtime data processing needs to be done here, otherwise replaced
        // channel will get duplicated setRuntimeData() calls

        log.trace("Entering doCommonErrorHandling()");

        final String channelSubscribeId = this.errorDocument.getChannelSubscribeId();
        if (channelSubscribeId != null) {
            
            final String chFate = this.runtimeData.getParameter("action");
            if (log.isDebugEnabled()) {
                log.debug("Channel fate is [" + chFate + "] for chanSubscribeId=" + channelSubscribeId);
            }
            
            if (chFate != null) {
                // a fate has been chosen
                if (chFate.equals("retry")) {
                    try {
                        // clean things up for the channel
                        if (this.targetChannel instanceof IResetableChannel) {
                            if (this.targetChannel instanceof IPrivilegedChannel) {
                                ((IPrivilegedChannel) this.targetChannel).setPortalControlStructures(this.portcs);
                            }
                            
                            this.targetChannel.setRuntimeData(this.runtimeData);

                            ((IResetableChannel) this.targetChannel).prepareForRefresh();
                        }

                        ChannelRuntimeData crd = (ChannelRuntimeData) this.runtimeData.clone();
                        crd.clear(); // Remove parameters

                        if (this.targetChannel instanceof IPrivilegedChannel) {
                            ((IPrivilegedChannel) this.targetChannel).setPortalControlStructures(this.portcs);
                        }
                        
                        this.targetChannel.setRuntimeData(crd);
                        
                        final ChannelManager cm = this.portcs.getChannelManager();
                        cm.setChannelInstance(channelSubscribeId, this.targetChannel);
                        
                        this.renderChannel(this.targetChannel, contentHandler, printWriter);

                        return;
                    }
                    catch (Exception e) {
                        // if any of the above didn't work, fall back to the error channel
                        resetCError(ErrorCode.SET_RUNTIME_DATA_EXCEPTION, e, channelSubscribeId, this.targetChannel, "Channel failed a refresh attempt.");
                    }
                }
                else if (chFate.equals("restart")) {
                    final ChannelManager cm = this.portcs.getChannelManager();

                    try {
                        //Clean things up for the channel
                        if (this.targetChannel instanceof IResetableChannel) {
                            if (this.targetChannel instanceof IPrivilegedChannel) {
                                ((IPrivilegedChannel) this.targetChannel).setPortalControlStructures(this.portcs);
                            }
                            
                            this.targetChannel.setRuntimeData(this.runtimeData);

                            ((IResetableChannel) this.targetChannel).prepareForReset();
                        }

                        final ChannelRuntimeData crd = (ChannelRuntimeData) this.runtimeData.clone();
                        crd.clear();
                        
                        if ((this.targetChannel = cm.instantiateChannel(this.portcs.getHttpServletRequest(), this.portcs.getHttpServletResponse(), channelSubscribeId)) == null) {
                            resetCError(ErrorCode.GENERAL_ERROR, null, channelSubscribeId, null, "Channel failed to reinstantiate!");
                        }
                        else {
                            try {
                                if (this.targetChannel instanceof IPrivilegedChannel) {
                                    ((IPrivilegedChannel) this.targetChannel).setPortalControlStructures(this.portcs);
                                }
                                
                                this.targetChannel.setRuntimeData(crd);
                                
                                this.renderChannel(this.targetChannel, contentHandler, printWriter);
                                
                                return;
                            }
                            catch (Exception e) {
                                // if any of the above didn't work, fall back to the error channel
                                resetCError(ErrorCode.SET_RUNTIME_DATA_EXCEPTION, e, channelSubscribeId, this.targetChannel, "Channel failed a reload attempt.");
                                cm.setChannelInstance(channelSubscribeId, this);
                                log.error("an error occurred during channel reinitialization.", e);
                            }
                        }
                    }
                    catch (Exception e) {
                        resetCError(ErrorCode.GENERAL_ERROR, e, channelSubscribeId, null, "Channel failed to reinstantiate!");
                        log.error("an error occurred during channel reinstantiation.", e);
                    }
                }
                else if (chFate.equals("toggle_stack_trace")) {
                    this.showStackTrace = !this.showStackTrace;
                }
            }
        }

        // if channel's render method was to be called, we would've returned by now
        if (contentHandler != null) {
            localRenderXML(contentHandler);
        }
        else {
            BaseMarkupSerializer serOut = null;
            try {
                final IUserPreferencesManager userPreferencesManager = this.portcs.getUserPreferencesManager();
                final ThemeStylesheetDescription tsd = userPreferencesManager.getThemeStylesheetDescription();
                serOut = MEDIAMANAGER.getSerializerByName(tsd.getSerializerName(), printWriter);
            }
            catch (Exception e) {
                log.error("unable to obtain proper markup serializer : ", e);
            }

            if (serOut == null) {
                // default to XML serializer
                final OutputFormat frmt = new OutputFormat("XML", "UTF-8", true);
                serOut = new XMLSerializer(printWriter, frmt);
            }

            localRenderXML(serOut);
        }
    }

    public void renderXML(ContentHandler out) {
        this.doCommonErrorHandling(out, null);
    }

    private void localRenderXML(ContentHandler out) {
        // note: this method should be made very robust. Optimally, it should
        // not rely on XSLT to do the job. That means that mime-type dependent
        // output should be generated directly within the method.
        // For now, we'll just do it the usual way.

        if (log.isTraceEnabled()) {
            log.trace("Entering localRenderXML() for CError " + this);
        }

        final String channelSubscribeId = this.errorDocument.getChannelSubscribeId();

        final IUserPreferencesManager userPreferencesManager = this.portcs.getUserPreferencesManager();
        if (channelSubscribeId != null) {
            try {
                final IUserLayoutManager userLayoutManager = userPreferencesManager.getUserLayoutManager();
                final IUserLayoutNodeDescription channelNode = userLayoutManager.getNode(channelSubscribeId);
                this.errorDocument.setChannelName(channelNode.getName());
            }
            catch (Throwable t) {
                log.error("Error determining name of channel with subscribe id [" + channelSubscribeId + "]", t);
            }
        }

        // defaults to refresh and reload not allowed.
        final RefreshPolicy policy;
        if (channelSubscribeId != null) {
            policy = computeRefreshPolicy();
        }
        else {
            policy = new RefreshPolicy();
        }

        // Decide whether to render a friendly or detailed screen
        this.ssTitle = "friendly";
        try {
            final AuthorizationService authService = AuthorizationService.instance();
            final IPerson person = userPreferencesManager.getPerson();
            final EntityIdentifier ei = person.getEntityIdentifier();
            final IAuthorizationPrincipal ap = authService.newPrincipal(ei.getKey(), ei.getType());
            
            if (ap.hasPermission(SupportedPermissions.OWNER, SupportedPermissions.VIEW_ACTIVITY, SupportedPermissions.DETAILS_TARGET)) {
                this.ssTitle = "detailed";
            }
        }
        catch (Throwable t) {
            log.error("Exception checking whether user authorized to view detailed CError view.  Defaulting to friendly view.", t);
        }

        if (log.isTraceEnabled()) {
            log.trace("SSL title is " + this.ssTitle);
        }

        final Document doc = this.errorDocument.getDocument();

        if (log.isWarnEnabled()) {
            try {
                log.warn("ErrorDocument XML is \n" + XML.serializeNode(doc));
            }
            catch (Exception e) {
                log.error("Failed to write error document XML to logger.", e);
            }
        }

        try {
            XSLT xslt = XSLT.getTransformer(this, this.runtimeData.getLocales());
            xslt.setXML(doc);
            xslt.setXSL(sslLocation, this.ssTitle, this.runtimeData.getBrowserInfo());
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", this.runtimeData.getBaseActionURL());
            xslt.setStylesheetParameter("showStackTrace", String.valueOf(this.showStackTrace));
            xslt.setStylesheetParameter("allowRefresh", Boolean.toString(policy.allowRefresh));
            xslt.setStylesheetParameter("allowReinstantiation", Boolean.toString(policy.allowReinstantiation));
            xslt.transform();
        }
        catch (Exception e) {
            log.error("Things are bad. Error channel threw Exception rendering its stylesheet.", e);
        }
    }

    public ChannelCacheKey generateKey() {
        // check if either restart or refresh command has been given, otherwise
        // generate key
        if (this.runtimeData != null && this.runtimeData.getParameter("action") != null) {
            return null;
        }

        ChannelCacheKey k = new ChannelCacheKey();
        StringBuilder sbKey = new StringBuilder(1024);

        // assume that errors can be cached system-wide
        k.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);

        sbKey.append("org.jasig.portal.channels.CError: errorDocument=").append(this.errorDocument);
        sbKey.append(" strace=").append(Boolean.toString(this.showStackTrace));
        sbKey.append(", mode=").append(this.ssTitle);
        sbKey.append(", locales=").append(LocaleManager.stringValueOf(this.runtimeData.getLocales()));
        k.setKey(sbKey.toString());
        return k;
    }

    public boolean isCacheValid(Object validity) {
        return true;
    }

    public void renderCharacters(PrintWriter out) throws PortalException {
        this.doCommonErrorHandling(null, out);
    }
    
    /**
     * Compute the refresh policy.
     * Assumes channel subcribe ID is not null, since in that case there is no
     * question about the policy - you cannot reinstantiate or refresh
     * unknown channels.
     * @return a RefreshPolicy suitable to our state.
     */
    private RefreshPolicy computeRefreshPolicy() {
        log.trace("entering computeRefreshPolicy()");
        RefreshPolicy policy = new RefreshPolicy();
        
        if (this.placeHolder) {
            // We are just displaying a message.
            // No channel to refresh or reload
            policy.allowRefresh = false;
            policy.allowReinstantiation = false;
            if (log.isTraceEnabled())
                log.trace("policy is [" + policy
                        + "] because we are a placeholder.");
        } else {
            // allow the PortalException, if any, to configure refresh and
            // reload
            Throwable errorThrowable = this.errorDocument.getThrowable();
            if (errorThrowable != null
                    && errorThrowable instanceof PortalException) {

                PortalException portalException = (PortalException) errorThrowable;

                policy.allowRefresh = portalException.isRefreshable();
                policy.allowReinstantiation = portalException.isReinstantiable();
                
                if (log.isTraceEnabled()){
                    log.trace("PortalException [" + portalException + 
                            "] implied refresh policy [" + policy + "]");
                }
            }
        }

        // allow the ErrorCode to veto refresh
        if (policy.allowRefresh) {
            ErrorCode code = this.errorDocument.getCode();
            if (!code.isRefreshAllowed()) {
                policy.allowRefresh = false;
                if (log.isTraceEnabled())
                    log.trace("ErrorCode " + code + " vetoed allowing refresh.");
            }
        }
        
        if (log.isTraceEnabled())
            log.trace("computed refresh plolicy: " + policy);
        
        return policy;
    }
    
    /**
     * Class to represent policy about whether channel refresh and 
     * reinstantiation is allowed.
     */
    private class RefreshPolicy{
        /**
         * Whether refreshing the channel is allowed.
         */
        boolean allowRefresh = true;
        
        /**
         * Whether reloading the channel is allowed.
         */
        boolean allowReinstantiation = true;
        
        public String toString() {
            return "refresh=" + this.allowRefresh 
                + " reinstantiate=" + this.allowReinstantiation;
        }
    }
    
    /**
     * @return Returns the errorDocument.
     */
    public ErrorDocument getErrorDocument() {
        return this.errorDocument;
    }
    
    /**
     * @param errorDocument The errorDocument to set.
     */
    public void setErrorDocument(ErrorDocument errorDocument) {
        this.errorDocument = errorDocument;
    }
    
    /**
     * Returns true iff this CError instance is acting as a placeholder.
     * @return Returns true iff this CError instance is acting as a placeholder.
     */
    boolean isPlaceHolder() {
        return this.placeHolder;
    }
    
    /**
     * Configure this CError instance to act as a placeholder.  In placeholder
     * mode, we do not present refresh and restart controls.  Instead, we 
     * display a message about why we have taken the place of a channel -
     * perhaps because the user is not authorized to view the channel or 
     * because the channel no longer exists.
     * @param placeHolder true to suppress refresh and renew controls, false otherwise
     */
    void setPlaceHolder(boolean placeHolder) {
        this.placeHolder = placeHolder;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" errorDocument:[").append(this.errorDocument).append("]");
        sb.append(" placeholder:").append(this.placeHolder);
        sb.append(" showStackTrace:").append(this.showStackTrace);
        sb.append(" sslTitle:[").append(this.ssTitle).append("]");
        return sb.toString();
    }
}