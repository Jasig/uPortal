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

import  javax.servlet.http.HttpServlet;
import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpServletResponse;
import  javax.servlet.http.HttpSession;
import  javax.servlet.ServletException;
import  java.io.IOException;
import  java.util.Enumeration;
import  java.util.HashMap;
import  org.jasig.portal.services.Authentication;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.PersonManagerFactory;
import  org.jasig.portal.services.LogService;


/**
 * Receives the username and password and tries to authenticate the user.
 * The form presented by org.jasig.portal.channels.CLogin is typically used
 * to generate the post to this servlet.
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @version $Revision$
 */
public class AuthenticationServlet extends HttpServlet {
  private static final String redirectString;
  private Authentication m_authenticationService = null;

    static {
      String upFile=UPFileSpec.RENDER_URL_ELEMENT+UPFileSpec.PORTAL_URL_SEPARATOR+UserInstance.USER_LAYOUT_ROOT_NODE+UPFileSpec.PORTAL_URL_SEPARATOR+UPFileSpec.PORTAL_URL_SUFFIX;
      try {
          upFile = UPFileSpec.buildUPFile(null,UPFileSpec.RENDER_METHOD,UserInstance.USER_LAYOUT_ROOT_NODE,null,null);
      } catch(PortalException pe) {
          LogService.log(LogService.ERROR,"AuthenticationServlet::static "+pe);
      }
      redirectString=upFile;
    }

  /**
   * Initializes the servlet
   * @exception ServletException
   */
  public void init () throws ServletException {
    m_authenticationService = new Authentication();

  }

  /**
   * Forwards request to doGet() method
   * @param req
   * @param res
   * @exception ServletException, IOException
   */
  public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // Just handle this request in the doGet() method
    doGet(req, res);
  }

  /**
   * Process the incoming HttpServletRequest
   * @param req
   * @param res
   * @exception ServletException, IOException
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Clear out the existing session for the user
    request.getSession().invalidate();
    // Retrieve the user's session
    request.getSession(true);
    IPerson person = null;
    try {
      // Get the person object associated with the request
      person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
      // Grab all of the principals from the request
      // NOTE: This should refer to a properties file
      HashMap principals = new HashMap(1);
      principals.put("username", request.getParameter("userName"));
      // Grab all of the credentials from the request
      // NOTE: This should refer to a properties file
      HashMap credentials = new HashMap(1);
      credentials.put("password", request.getParameter("password"));
      // Attempt to authenticate using the incoming request
      m_authenticationService.authenticate(principals, credentials, person);
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
      // Reset everything
      person = null;
      request.getSession(false).invalidate();
      // Add the authentication failure
      request.getSession(true).setAttribute("up_authenticationError", "true");
    }
    // Check to see if the person has been authenticated
    if (person != null && person.getSecurityContext().isAuthenticated()) {
      // Send the now authenticated user back to the PortalSessionManager servlet
      response.sendRedirect(request.getContextPath() + '/' + redirectString);
    }
    else {
      // Store the fact that this user has attempted authentication in the session
      request.getSession(false).setAttribute("up_authenticationAttempted", "true");
      // Preserve the attempted username so it can be redisplayed to the user by CLogin
      String attemptedUserName = request.getParameter("userName");
      if (attemptedUserName != null)
        request.getSession(false).setAttribute("up_attemptedUserName", request.getParameter("userName"));
      // Send the unauthenticated user back to the PortalSessionManager servlet
      response.sendRedirect(request.getContextPath() + '/' + redirectString);
    }
  }
}



