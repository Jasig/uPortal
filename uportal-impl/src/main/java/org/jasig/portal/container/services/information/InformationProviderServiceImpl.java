/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.jasig.portal.container.services.PortletContainerService;

/**
 * Implementation of Apache Pluto InformationProviderService.
 * @author Michael Ivanov, mivanov@unicon.net
 * @version $Revision$
 */
public class InformationProviderServiceImpl implements PortletContainerService, InformationProviderService {
    
    private static DynamicInformationProvider dynamicProvider;
	private static StaticInformationProviderImpl staticInfoProvider;
    
    private static final String dynamicInformationProviderRequestParameterName = "org.apache.pluto.services.information.DynamicInformationProvider";

    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        if ( staticInfoProvider == null ) {
		 staticInfoProvider = new StaticInformationProviderImpl();
         staticInfoProvider.init(servletConfig, properties);
        } 
    }
    
    public void destroy() throws Exception {
		staticInfoProvider = null;
    }    
    
    // InformationProviderService methods
    
    public StaticInformationProvider getStaticProvider() {
	   return staticInfoProvider;
    }

    public synchronized DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
      if ( dynamicProvider == null ) 
      	dynamicProvider = new DynamicInformationProviderImpl();
        return dynamicProvider;
    }

}
