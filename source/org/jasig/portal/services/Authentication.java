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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.services;

import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.PersonImpl;
import  org.jasig.portal.UserIdentityStoreFactory;
import  org.jasig.portal.IUserIdentityStore;
import  org.jasig.portal.AuthorizationException;
import  org.jasig.portal.PropertiesManager;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Enumeration;
import  java.util.Map;
import  java.util.Set;
import  java.util.Iterator;


/**
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class Authentication {
  protected org.jasig.portal.security.IPerson m_Person = null;
  protected ISecurityContext ic = null;

  /**
   * Attempts to authenticate a given IPerson based on a set of principals and credentials
   * @param principals
   * @param credentials
   * @param person
   * @exception PortalSecurityException
   */
  public void authenticate (HashMap principals, HashMap credentials, IPerson person) throws PortalSecurityException {
    // Retrieve the security context for the user
    ISecurityContext securityContext = person.getSecurityContext();

    // NOTE: At this point the service should be looking in a properties file
    //       to determine what tokens to look for that represent the principals
    //       and credentials.
    // Retrieve the username and principal
    String username = (String)principals.get("username");
    String password = (String)credentials.get("password");

    // Retrieve and populate an instance of the principal object
    IPrincipal principalInstance = securityContext.getPrincipalInstance();
    if (username != null) {
      principalInstance.setUID(username);
      // prime userid in person object before authenticated so can be
      // displayed after failure if desired
      person.setAttribute("username",principalInstance.getUID());
    }

    // Retrieve and populate an instance of the credentials object
    IOpaqueCredentials credentialsInstance = securityContext.getOpaqueCredentialsInstance();
    credentialsInstance.setCredentials(password);

    // Attempt to authenticate the user
    securityContext.authenticate();

    // Check to see if the user was authenticated
    if (securityContext.isAuthenticated()) {

      // Add the authenticated username to the person object
      // the login name may have been provided or reset by the security provider
      // so this needs to be done after authentication.
      person.setAttribute("username",securityContext.getPrincipal().getUID());

      // Retrieve the additional descriptor from the security context
      IAdditionalDescriptor addInfo = person.getSecurityContext().getAdditionalDescriptor();
      // Process the additional descriptor if one was created
      if (addInfo != null) {
        // Replace the passed in IPerson with the additional descriptor if the
        // additional descriptor is an IPerson object created by the security context
        // NOTE: This is not the preferred method, creation of IPerson objects should be
        //       handled by the PersonManager.
        if (addInfo instanceof IPerson) {
          IPerson newPerson = (IPerson)addInfo;
          person.setFullName(newPerson.getFullName());
          for (Enumeration e = newPerson.getAttributeNames(); e.hasMoreElements();) {
            String attributeName = (String)e.nextElement();
            person.setAttribute(attributeName, newPerson.getAttribute(attributeName));
          }
        }
        // If the additional descriptor is a map then we can
        // simply copy all of these additional attributes into the IPerson
        else if (addInfo instanceof Map) {
          // Cast the additional descriptor as a Map
          Map additionalAttributes = (Map)addInfo;
          // Copy each additional attribute into the person object
          for (Iterator keys = additionalAttributes.keySet().iterator(); keys.hasNext();) {
            // Get a key
            String key = (String)keys.next();
            // Set the attribute
            person.setAttribute(key, additionalAttributes.get(key));
          }
        }
        else {
          LogService.instance().log(LogService.WARN, "Authentication Service recieved unknown additional descriptor");
        }
      }
      // Populate the person object using the PersonDirectory if applicable
      if (PropertiesManager.getPropertyAsBoolean("org.jasig.portal.services.Authentication.usePersonDirectory")) {
        // Retrieve all of the attributes associated with the person logging in
        Hashtable attribs = (new PersonDirectory()).getUserDirectoryInformation((String)person.getAttribute("username"));
        // Add each of the attributes to the IPerson
        Enumeration en = attribs.keys();
        while (en.hasMoreElements()) {
          String key = (String)en.nextElement();
          String value = (String)attribs.get(key);
          person.setAttribute(key, value);
        }
      }
      // Make sure the the user's fullname is set
      if (person.getFullName() == null) {
        // Use portal display name if one exists
        if (person.getAttribute("portalDisplayName") != null) {
          person.setFullName((String)person.getAttribute("portalDisplayName"));
        }
        // If not try the eduPerson displyName
        else if (person.getAttribute("displayName") != null) {
          person.setFullName((String)person.getAttribute("displayName"));
        }
        // If still no FullName use an unrecognized string
        if (person.getFullName() == null) {
          person.setFullName("Unrecognized person: " + person.getAttribute("username"));
        }
      }
      // Find the uPortal userid for this user or flunk authentication if not found
      // The template username should actually be derived from directory information.
      // The reference implemenatation sets the uPortalTemplateUserName to the default in
      // the portal.properties file.
      // A more likely template would be staff or faculty or undergraduate.
      boolean autocreate = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.services.Authentication.autoCreateUsers");
      // If we are going to be auto creating accounts then we must find the default template to use
      if (autocreate && person.getAttribute("uPortalTemplateUserName") == null) {
        String defaultTemplateUserName = PropertiesManager.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName");
        person.setAttribute("uPortalTemplateUserName", defaultTemplateUserName);
      }
      try {
        // Attempt to retrieve the UID
        int newUID = UserIdentityStoreFactory.getUserIdentityStoreImpl().getPortalUID(person, autocreate);
        person.setID(newUID);
      } catch (AuthorizationException ae) {
        LogService.instance().log(LogService.ERROR, ae);
        throw new PortalSecurityException("Authentication Service: Exception retrieving UID");
      }
    }
  }


  /**
   * Returns an IPerson object that can be used to hold site-specific attributes
   * about the logged on user.  This information is established during
   * authentication.
   * @return An object that implements the
   * <code>org.jasig.portal.security.IPerson</code> interface.
   */
 public IPerson getPerson () {
   return  m_Person;
  }

  /**
   * Returns an ISecurityContext object that can be used
   * later. This object is passed as part of the IChannel Interface.
   * The security context may be used to gain authorized access to
   * services.
   * @return An object that implements the
   * <code>org.jasig.portal.security.ISecurityContext</code> interface.
   */
  public ISecurityContext getSecurityContext () {
    return  ic;
  }
}



