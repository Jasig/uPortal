/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IAuthorizationService;
import org.jasig.portal.security.IAuthorizationServiceFactory;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.PortalSecurityException;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class AuthorizationService
{
    
    private static final Log log = LogFactory.getLog(AuthorizationService.class);
    
  private static AuthorizationService m_instance;
  protected IAuthorizationService m_authorization = null;
  protected static String s_factoryName = null;
  protected static IAuthorizationServiceFactory m_Factory = null;

  static {
    // Get the security properties file
    java.io.InputStream secprops = AuthorizationService.class.getResourceAsStream("/properties/security.properties");
    // Get the properties from the security properties file
    Properties pr = new Properties();
    try {
      pr.load(secprops);
		secprops.close();
      // Look for our authorization factory and instantiate an instance of it or die trying.
      if ((s_factoryName = pr.getProperty("authorizationProvider")) == null) {
        log.error("AuthorizationProvider not specified or incorrect in security.properties", new PortalSecurityException("AuthorizationProvider not specified or incorrect in security.properties"));
      }
      else {
        try {
          m_Factory = (IAuthorizationServiceFactory)Class.forName(s_factoryName).newInstance();
        } catch (Exception e) {
          log.error("Failed to instantiate " + s_factoryName,  new PortalSecurityException("Failed to instantiate " + s_factoryName));
        }
      }
    } catch (IOException e) {
      log.error("Error loading security properties", e);
    } finally {
			try {
				if (secprops != null)
					secprops.close();
			} catch (IOException ioe) {
				log.error("Error closing security properties file.", ioe);
			}
		}
  }

  /**
   *  
   */
  private AuthorizationService () throws AuthorizationException
  {
      // From our factory get an actual authorization instance
      m_authorization = m_Factory.getAuthorization();
  }
  
 /**
   * @return org.jasig.portal.groups.IGroupMember
   * @param principal IAuthorizationPrincipal
   * @exception org.jasig.portal.groups.GroupsException
   */
  public IGroupMember getGroupMember(IAuthorizationPrincipal principal)
         throws GroupsException
   {
       return m_authorization.getGroupMember(principal);
   }
   
  /**
   * @return Authorization
   */
  public final static synchronized AuthorizationService instance() throws AuthorizationException
  {
          if ( m_instance == null )
                  { m_instance = new AuthorizationService(); }
          return m_instance;
  }

  /**
   * @param owner java.lang.String
   * @return org.jasig.portal.security.IPermissionManager
   * @exception org.jasig.portal.AuthorizationException
   */
  public IPermissionManager newPermissionManager(String owner)
         throws AuthorizationException
  {
       return m_authorization.newPermissionManager(owner);
  }

  /**
   * @param key java.lang.String
   * @param type java.lang.Class
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.AuthorizationException
   */
  public IAuthorizationPrincipal newPrincipal(String key, Class type)
         throws AuthorizationException
  {
       return m_authorization.newPrincipal(key, type);
  }
  
  /**
   * @param groupMember
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.groups.GroupsException
   */
   public IAuthorizationPrincipal newPrincipal(IGroupMember groupMember)
          throws GroupsException
   {
       return m_authorization.newPrincipal(groupMember);
   }
   
  /**
   * @param permission
   * @return org.jasig.portal.security.IAuthorizationPrincipal
   * @exception org.jasig.portal.AuthorizationException
   */
  public IAuthorizationPrincipal newPrincipal(IPermission permission)
         throws AuthorizationException
  {
       return m_authorization.getPrincipal(permission);
  }

  /**
   * @param owner java.lang.String
   * @return org.jasig.portal.security.IUpdatingPermissionManager
   * @exception org.jasig.portal.AuthorizationException
   */
  public IUpdatingPermissionManager newUpdatingPermissionManager(String owner)
         throws AuthorizationException
  {
       return m_authorization.newUpdatingPermissionManager(owner);
  }
}
