/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.searchlauncher;

import java.util.HashMap;
import java.util.Map;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlBuilder;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletUrlBuilder;
import org.jasig.portal.url.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.EventMapping;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * SearchPortletController produces both a search form and results for configured
 * search services.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Controller
@RequestMapping("VIEW")
public class SearchLauncherPortletController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());    
    private IPortalUrlProvider portalUrlProvider;
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.portalUrlProvider = urlProvider;
    }
    
    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        Validate.notNull(portalRequestUtils);
        this.portalRequestUtils = portalRequestUtils;
    }
    
    /**
     * Display the main user-facing view of the portlet.
     * 
     * @param request
     * @return
     */
    @RequestMapping
    public ModelAndView getView(PortletRequest portletRequest) {
        final HttpServletRequest httpRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        final IPortalUrlBuilder portalUrlBuilder = this.portalUrlProvider.getPortalUrlBuilderByPortletFName(httpRequest, "search", UrlType.ACTION);
        final IPortletUrlBuilder portletUrlBuilder = portalUrlBuilder.getTargetedPortletUrlBuilder();

        final Map<String,Object> model = new HashMap<String, Object>();
    	String url = portalUrlBuilder.getUrlString();
    	model.put("searchUrl", url);
    	return new ModelAndView("/jsp/SearchLauncher/searchLauncher", model);
    }
}
