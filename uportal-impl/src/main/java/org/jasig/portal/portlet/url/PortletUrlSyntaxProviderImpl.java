/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlet.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.ITransientPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.Tuple;
import org.springframework.beans.factory.annotation.Required;

/**
 * Contains the logic and string constants for generating and parsing portlet URL parameters.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletUrlSyntaxProviderImpl implements IPortletUrlSyntaxProvider {
    private static final String SEPERATOR = "_";
    private static final String PORTLET_CONTROL_PREFIX = "pltc" + SEPERATOR;
    private static final String PORTLET_PARAM_PREFIX = "pltp" + SEPERATOR;

    private static final String PARAM_REQUEST_TARGET = PORTLET_CONTROL_PREFIX + "target";
    private static final String PARAM_REQUEST_TYPE = PORTLET_CONTROL_PREFIX + "type";
    private static final String PARAM_WINDOW_STATE = PORTLET_CONTROL_PREFIX + "state";
    private static final String PARAM_PORTLET_MODE = PORTLET_CONTROL_PREFIX + "mode";
    
    private static final Pattern URL_PARAM_NAME = Pattern.compile("&([^&?=\n]*)");
   
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private String defaultEncoding = "UTF-8";
    private int bufferLength = 512;
    private ITransientPortletWindowRegistry portletWindowRegistry;
    private IPortalRequestUtils portalRequestUtils;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IUserInstanceManager userInstanceManager;
    private boolean useAnchors = true;
    private Set<WindowState> transientWindowStates = new HashSet<WindowState>(Arrays.asList(IPortletAdaptor.EXCLUSIVE, IPortletAdaptor.DETACHED));
    private Set<WindowState> anchoringWindowStates = new HashSet<WindowState>(Arrays.asList(WindowState.MINIMIZED, WindowState.NORMAL));
    
    
    /**
     * @return the useAnchors
     */
    public boolean isUseAnchors() {
        return this.useAnchors;
    }
    /**
     * If anchors should be added to generated URLs
     */
    public void setUseAnchors(boolean useAnchors) {
        this.useAnchors = useAnchors;
    }
    /**
     * @return the portalRequestUtils
     */
    public IPortalRequestUtils getPortalRequestUtils() {
        return portalRequestUtils;
    }
    /**
     * @param portalRequestUtils the portalRequestUtils to set
     */
    @Required
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /**
     * @return the defaultEncoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }
    /**
     * @param defaultEncoding the defaultEncoding to set
     */
    public void setDefaultEncoding(String defaultEncoding) {
        Validate.notNull(defaultEncoding, "defaultEncoding can not be null");
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * @return the bufferLength
     */
    public int getBufferLength() {
        return bufferLength;
    }
    /**
     * @param bufferLength the bufferLength to set
     */
    public void setBufferLength(int bufferLength) {
        if (bufferLength < 1) {
            throw new IllegalArgumentException("bufferLength must be at least 1");
        }

        this.bufferLength = bufferLength;
    }

    /**
     * @return the portletWindowRegistry
     */
    public ITransientPortletWindowRegistry getPortletWindowRegistry() {
        return portletWindowRegistry;
    }
    /**
     * @param portletWindowRegistry the portletWindowRegistry to set
     */
    @Required
    public void setPortletWindowRegistry(ITransientPortletWindowRegistry portletWindowRegistry) {
        Validate.notNull(portletWindowRegistry, "portletWindowRegistry can not be null");
        this.portletWindowRegistry = portletWindowRegistry;
    }
    
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Required
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    public IPortletEntityRegistry getPortletEntityRegistry() {
        return portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Required
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    public IUserInstanceManager getUserInstanceManager() {
        return userInstanceManager;
    }
    /**
     * @param userInstanceManager the userInstanceManager to set
     */
    @Required
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    public Set<WindowState> getTransientWindowStates() {
        return this.transientWindowStates;
    }
    /**
     * {@link WindowState}s that have transient {@link IPortletWindow}s. These states must be the ONLY
     * content rendering links on the page. Defaults to EXCLUSIVE and DETACHED.
     */
    public void setTransientWindowStates(Set<WindowState> transientWindowStates) {
        if (transientWindowStates == null) {
            this.transientWindowStates = Collections.emptySet();
        }
        else {
            this.transientWindowStates = new LinkedHashSet<WindowState>(transientWindowStates);
        }
    }

    public Set<WindowState> getAnchoringWindowStates() {
        return this.anchoringWindowStates;
    }
    /**
     * {@link WindowState}s where anchors should be added to the ends of the generated URLS, only if
     * {@link #setUseAnchors(boolean)} is true. Defaults to MINIMIZED and NORMAL
     */
    public void setAnchoringWindowStates(Set<WindowState> anchoringWindowStates) {
        if (anchoringWindowStates == null) {
            this.anchoringWindowStates = Collections.emptySet();
        }
        else {
            this.anchoringWindowStates = new LinkedHashSet<WindowState>(anchoringWindowStates);
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#parsePortletParameters(javax.servlet.http.HttpServletRequest)
     */
    public Tuple<IPortletWindowId, PortletUrl> parsePortletParameters(HttpServletRequest request) {
        Validate.notNull(request, "request can not be null");
        
        final IPortletWindowId targetedPortletWindowId;
        
        final String targetedPortletWindowIdStr = request.getParameter(PARAM_REQUEST_TARGET);
        if (targetedPortletWindowIdStr != null) {
            targetedPortletWindowId = this.portletWindowRegistry.getPortletWindowId(targetedPortletWindowIdStr);
        }
        else {
            //Fail over to looking for a fname
            final String targetedFname = request.getParameter("uP_fname");
            if (targetedFname == null) {
                return null;
            }
            
            //Get the user's layout manager
            final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
            final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
            final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
            
            //Determine the subscribe ID
            final String channelSubscribeId = userLayoutManager.getSubscribeId(targetedFname);
            if (channelSubscribeId == null) {
                this.logger.info("No channel subscribe ID found for fname '" + targetedFname + "'. skipping portlet parameter processing");
                return null;
            }
            
            //Find the channel and portlet definitions
            final IUserLayoutChannelDescription channelNode = (IUserLayoutChannelDescription)userLayoutManager.getNode(channelSubscribeId);
            final String channelPublishId = channelNode.getChannelPublishId();
            final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(Integer.parseInt(channelPublishId));
            if (portletDefinition == null) {
                this.logger.info("No portlet defintion found for channel definition '" + channelPublishId + "' with fname '" + targetedFname + "'. skipping portlet parameter processing");
                return null;
            }
            
            //Determine the appropriate portlet window ID
            final IPerson person = userInstance.getPerson();
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinition.getPortletDefinitionId(), channelSubscribeId, person.getID());
            final IPortletWindow defaultPortletWindow = this.portletWindowRegistry.createDefaultPortletWindow(request, portletEntity.getPortletEntityId());
            targetedPortletWindowId = this.portletWindowRegistry.createTransientPortletWindowId(request, defaultPortletWindow.getPortletWindowId());
        }
        
        final PortletUrl portletUrl = new PortletUrl();
        
        final String requestTypeStr = request.getParameter(PARAM_REQUEST_TYPE);
        if (requestTypeStr != null) {
            final RequestType requestType = RequestType.valueOf(requestTypeStr);
            portletUrl.setRequestType(requestType);
        }
        else {
            //Default to RENDER request if no request type was specified
            portletUrl.setRequestType(RequestType.RENDER);
        }
        
        final String windowStateStr = request.getParameter(PARAM_WINDOW_STATE);
        if (windowStateStr != null) {
            final WindowState windowState = new WindowState(windowStateStr);
            portletUrl.setWindowState(windowState);
        }
        
        final String portletModeStr = request.getParameter(PARAM_PORTLET_MODE);
        if (portletModeStr != null) {
            final PortletMode portletMode = new PortletMode(portletModeStr);
            portletUrl.setPortletMode(portletMode);
        }
        
        final Map<String, String[]> requestParameters = request.getParameterMap();
        final Set<String> urlParameterNames = this.getUrlParameterNames(request);
        
        final Map<String, String[]> portletParameters = new HashMap<String, String[]>(requestParameters.size());
        for (final Map.Entry<String, String[]> parameterEntry : requestParameters.entrySet()) {
            final String parameterName = parameterEntry.getKey();
            
            //If the parameter starts with the param prefix add it to the Map
            if (parameterName.startsWith(PORTLET_PARAM_PREFIX)) {
                final String portletParameterName = parameterName.substring(PORTLET_PARAM_PREFIX.length());
                final String[] portletParameterValues = parameterEntry.getValue();

                portletParameters.put(portletParameterName, portletParameterValues);
            }
            //If it did not appear on the URL it must be a submit parameter so add it to the Map
            else if (urlParameterNames != null && !urlParameterNames.contains(parameterName)) {
                final String[] portletParameterValues = parameterEntry.getValue();

                portletParameters.put(parameterName, portletParameterValues);
            }
        }
        portletUrl.setParameters(portletParameters);
        
        portletUrl.setSecure(request.isSecure());
        
        return new Tuple<IPortletWindowId, PortletUrl>(targetedPortletWindowId, portletUrl);
    }
    


    /**
     * Parses the request URL to return a Set of the parameter names that appeared on the URL string.
     * 
     * @param request The request to look at.
     * @return The Set of parameter names from the URL.
     */
    protected Set<String> getUrlParameterNames(HttpServletRequest request) {
        // Only posts can have parameters not in the URL, ignore non-post requests.
        final String method = request.getMethod();
        if (!"POST".equals(method)) {
            return null;
        }
        
        final Set<String> urlParameterNames = new HashSet<String>();
        
        final String queryString = request.getQueryString();
        final Matcher paramNameMatcher = URL_PARAM_NAME.matcher("&" + queryString);

        final String encoding = this.getEncoding(request);
        
        while (paramNameMatcher.find()) {
            final String paramName = paramNameMatcher.group(1);
            String decParamName;
            try {
                decParamName = URLDecoder.decode(paramName, encoding);
            }
            catch (UnsupportedEncodingException uee) {
                decParamName = paramName;
            }
            
            urlParameterNames.add(decParamName);
        }
        
        return urlParameterNames;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
     */
    public String generatePortletUrl(HttpServletRequest request, IPortletWindow portletWindow, PortletUrl portletUrl) {
        Validate.notNull(request, "request can not be null");
        Validate.notNull(portletWindow, "portletWindow can not be null");
        Validate.notNull(portletUrl, "portletUrl can not be null");
        
        //Convert the callback request to the portal request
        request = this.portalRequestUtils.getOriginalPortletAdaptorRequest(request);
        
        //Get the channel runtime data from the request attributes, it should have been set there by the portlet adapter
        final ChannelRuntimeData channelRuntimeData = (ChannelRuntimeData)request.getAttribute(IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA);
        if (channelRuntimeData == null) {
            throw new IllegalStateException("No ChannelRuntimeData was found as a request attribute for key '" + IPortletAdaptor.ATTRIBUTE__RUNTIME_DATA + "' on request '" + request + "'");
        } 
        
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
        final IPortletEntity parentPortletEntity = this.portletWindowRegistry.getParentPortletEntity(request, portletWindowId);
        final String channelSubscribeId = parentPortletEntity.getChannelSubscribeId();

        //Get the encoding to use for the URL
        final String encoding = this.getEncoding(request);
        
        
        // TODO Need to decide how to deal with 'secure' URL requests


        //Build the base of the URL with the context path
        final StringBuilder url = new StringBuilder(this.bufferLength);
        final String contextPath = request.getContextPath();
        url.append(contextPath).append("/");
        
        final WindowState windowState = portletUrl.getWindowState();
        final WindowState previousWindowState = portletWindow.getWindowState();
        
        // Determine the base path for the URL
        // If the next state is EXCLUSIVE or there is no state change and the current state is EXCLUSIVE use the worker URL base
        if (IPortletAdaptor.EXCLUSIVE.equals(windowState) || (windowState == null && IPortletAdaptor.EXCLUSIVE.equals(previousWindowState))) {
            final String urlBase = channelRuntimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
            url.append(urlBase);
        }
        //In detached, need to make sure the URL is right
        else if (IPortletAdaptor.DETACHED.equals(windowState) || (windowState == null && IPortletAdaptor.DETACHED.equals(previousWindowState))) {
            final UPFileSpec upFileSpec = new UPFileSpec(channelRuntimeData.getUPFile());
            upFileSpec.setMethodNodeId(channelSubscribeId);
            final String urlBase = upFileSpec.getUPFile();
            url.append(urlBase);
        }
        //Switching back from detached to a normal state
        else if (IPortletAdaptor.DETACHED.equals(previousWindowState) && windowState != null && !previousWindowState.equals(windowState)) {
            final UPFileSpec upFileSpec = new UPFileSpec(channelRuntimeData.getUPFile());
            upFileSpec.setMethodNodeId(UPFileSpec.USER_LAYOUT_ROOT_NODE);
            final String urlBase = upFileSpec.getUPFile();
            url.append(urlBase);
        }
        //No special handling, just use the base action URL
        else {
            final String urlBase = channelRuntimeData.getBaseActionURL();
            url.append(urlBase);
        }
        
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Using root url base '" + url + "'");
        }

        //Set the request target, creating a transient window ID if needed
        final String portletWindowIdString;
        if (this.transientWindowStates.contains(windowState) && !this.transientWindowStates.contains(previousWindowState)) {
            final IPortletWindowId transientPortletWindowId = this.portletWindowRegistry.createTransientPortletWindowId(request, portletWindowId);
            portletWindowIdString = transientPortletWindowId.toString();
        }
        else if (this.portletWindowRegistry.isTransient(request, portletWindowId) && !this.transientWindowStates.contains(windowState) &&
                      (windowState != null || !this.transientWindowStates.contains(previousWindowState))) {
            //Get non-transient version of id
            final IPortletEntityId portletEntityId = portletWindow.getPortletEntityId();
            final IPortletWindowId defaultPortletWindowId = this.portletWindowRegistry.getDefaultPortletWindowId(portletEntityId);
            portletWindowIdString = defaultPortletWindowId.getStringId();
        }
        else {
            portletWindowIdString = portletWindowId.getStringId();
        }
        this.encodeAndAppend(url.append("?"), encoding, PARAM_REQUEST_TARGET, portletWindowIdString);
        
        //Set the request type
        final RequestType requestType = portletUrl.getRequestType();
        final String requestTypeString = requestType != null ? requestType.toString() : RequestType.RENDER.toString();
        this.encodeAndAppend(url.append("&"), encoding, PARAM_REQUEST_TYPE, requestTypeString);
        
        // If set add the window state
        if (windowState != null && !previousWindowState.equals(windowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE, windowState.toString());
            
            //Add the parameters needed by the portal structure & theme to render the correct window state 
            if (WindowState.MAXIMIZED.equals(windowState)) {
                this.encodeAndAppend(url.append("&"), encoding, "uP_root", channelSubscribeId);
            }
            else if (WindowState.NORMAL.equals(windowState)) {
                this.encodeAndAppend(url.append("&"), encoding, "uP_root", IUserLayout.ROOT_NODE_NAME);
                this.encodeAndAppend(url.append("&"), encoding, "uP_tcattr", "minimized");
                this.encodeAndAppend(url.append("&"), encoding, "minimized_channelId", channelSubscribeId);
                this.encodeAndAppend(url.append("&"), encoding, "minimized_" + channelSubscribeId + "_value", "false");
            }
            else if (WindowState.MINIMIZED.equals(windowState)) {
                this.encodeAndAppend(url.append("&"), encoding, "uP_root", IUserLayout.ROOT_NODE_NAME);
                this.encodeAndAppend(url.append("&"), encoding, "uP_tcattr", "minimized");
                this.encodeAndAppend(url.append("&"), encoding, "minimized_channelId", channelSubscribeId);
                this.encodeAndAppend(url.append("&"), encoding, "minimized_" + channelSubscribeId + "_value", "true");
            }
            else if (IPortletAdaptor.DETACHED.equals(windowState)) {
                this.encodeAndAppend(url.append("&"), encoding, "uP_detach_target", channelSubscribeId);
            }
        }
        //Or for any transient state always add the window state
        else if (this.transientWindowStates.contains(windowState) || this.transientWindowStates.contains(previousWindowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_WINDOW_STATE, previousWindowState.toString());
        }
        
        //If set add the portlet mode
        final PortletMode portletMode = portletUrl.getPortletMode();
        if (portletMode != null) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE, portletMode.toString());
        }
        //Or for any transient state always add the portlet mode
        else if (this.transientWindowStates.contains(windowState) || this.transientWindowStates.contains(previousWindowState)) {
            this.encodeAndAppend(url.append("&"), encoding, PARAM_PORTLET_MODE, portletWindow.getPortletMode().toString());
        }
        
        //Add the parameters to the URL
        final Map<String, String[]> parameters = portletUrl.getParameters();
        if (parameters != null) {
            for (final Map.Entry<String, String[]> parameterEntry : parameters.entrySet()) {
                final String name = parameterEntry.getKey();
                final String[] values = parameterEntry.getValue();

                this.encodeAndAppend(url.append("&"), encoding, PORTLET_PARAM_PREFIX + name, values);
            }
        }
       
        //Add the anchor if anchoring is enabled
        if (this.useAnchors && !RequestType.ACTION.equals(requestType) && ((windowState != null && this.anchoringWindowStates.contains(windowState)) || (windowState == null && this.anchoringWindowStates.contains(previousWindowState)))) {
            url.append("#").append(channelSubscribeId);
        }
 
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Generated portlet URL '" + url + "' for IPortletWindow='" + portletWindow + "' and PortletUrl='" + portletUrl + "'. StringBuilder started with length " + this.bufferLength + " and ended with length " + url.capacity() + ".");
        }
        
        return url.toString();
    }
    
    /**
     * Tries to determine the encoded from the request, if not available falls back to configured default.
     * 
     * @param request The current request.
     * @return The encoding to use.
     */
    protected String getEncoding(HttpServletRequest request) {
        final String encoding = request.getCharacterEncoding();
        if (encoding != null) {
            return encoding;
        }
        
        return this.defaultEncoding;
    }
    
    /**
     * Encodes parameter name and value(s) on to the url using the specified encoding. The option to pass more than one
     * value is provided to avoid encoding the same name multiple times.  
     * 
     * @param url The URL StringBuilder to append the parameters to
     * @param encoding The encoding to use.
     * @param name The name of the parameter
     * @param values The values for the parameter, a & will be appeneded between each name/value pair added when multiple values are passed.
     */
    protected void encodeAndAppend(StringBuilder url, String encoding, String name, String... values) {
        try {
            name = URLEncoder.encode(name, encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode portlet URL parameter name '" + name + "' for encoding '" + encoding + "'");
        }
        
        if (values.length == 0) {
            url.append(name).append("=");
        }
        else {
            for (int index = 0; index < values.length; index++) {
                String value = values[index];
                
                if (value == null) {
                    value = "";
                }
                
                try {
                    value = URLEncoder.encode(value, encoding);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Failed to encode portlet URL parameter value '" + value + "' for encoding '" + encoding + "'");
                }
                
                if (index > 0) {
                    url.append("&");
                }
                
                url.append(name).append("=").append(value);
            }
        }
    }
}
