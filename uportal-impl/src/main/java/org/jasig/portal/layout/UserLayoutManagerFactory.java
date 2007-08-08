/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.lang.reflect.Constructor;

import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.immutable.ImmutableUserLayoutManagerWrapper;
import org.jasig.portal.layout.simple.SimpleUserLayoutManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.services.stats.StatsRecorderLayoutEventListener;


/**
 * A factory class for obtaining {@link IUserLayoutManager} implementations.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class UserLayoutManagerFactory {
    
	private static final Log LOG = LogFactory.getLog(UserLayoutManagerFactory.class);
    
    private static Class coreUserLayoutManagerImpl=SimpleUserLayoutManager.class;

    static {
        // Retrieve the class name of the core IUserLayoutManager implementation
        String className = PropertiesManager.getProperty("org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation", null);
        if (className == null)
            LOG.error( "UserLayoutManagerFactory: org.jasig.portal.layout.UserLayoutManagerFactory.coreImplementation must be specified in portal.properties");
        try {
            Class newClass = Class.forName(className);
            coreUserLayoutManagerImpl=newClass;
            LOG.info("UserLayoutManagerFactory: Layout Manager Factory successfully loaded: "+className);
        } catch (Exception e) {
            LOG.error( "UserLayoutManagerFactory: Could not instantiate " + className, e);
        }
    }

    /**
     * Obtain a regular user layout manager implementation
     *
     * @return an <code>IUserLayoutManager</code> value
     */
    public static IUserLayoutManager getUserLayoutManager(IPerson person, UserProfile profile) throws PortalException {
        try {
            Class[] cArgsClasses={IPerson.class,UserProfile.class,IUserLayoutStore.class};
            Constructor c=coreUserLayoutManagerImpl.getConstructor(cArgsClasses);
            Object[] cArgs={person,profile,UserLayoutStoreFactory.getUserLayoutStoreImpl()};
            IUserLayoutManager ulm = (IUserLayoutManager)c.newInstance(cArgs);
            ulm.addLayoutEventListener(new StatsRecorderLayoutEventListener(person, profile));
            
            // Wrap the implementation to provide lookup by fname
            // support which basically merges a non-persisted channel
            // into the layout
            IUserLayoutManager ulmWrapper = new TransientUserLayoutManagerWrapper(ulm);
            return ulmWrapper;
        } catch (Exception e) {
            throw new PortalException("Unable to instantiate a \""+coreUserLayoutManagerImpl.getName()+"\"",e);
        }
    }

    /**
     * Returns an immutable version of a user layout manager.
     *
     * @param man an <code>IUserLayoutManager</code> value
     * @return an immutable <code>IUserLayoutManager</code> value
     * @exception PortalException if an error occurs
     */
    public static IUserLayoutManager immutableUserLayoutManager(IUserLayoutManager man) throws PortalException {
        return new ImmutableUserLayoutManagerWrapper(man);
    }
}
