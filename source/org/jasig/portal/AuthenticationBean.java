/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;
import org.jasig.portal.security.*;

/**
 * @author Ken Weiner
 */
public class AuthenticationBean extends GenericPortalBean implements IAuthenticationBean
{
  protected org.jasig.portal.security.IPerson m_Person = null;

  /**
   * Authenticate a user.
   * @param sUserName User name
   * @param sPassword User password
   * @return true if successful, otherwise false.
   */
  public boolean authenticate (String sUserName, String sPassword)
  {
    SecurityContext ic;
    Principal me;
    OpaqueCredentials op;

    ic = new InitialSecurityContext("root");
    me = ic.getPrincipalInstance();
    op = ic.getOpaqueCredentialsInstance();

    me.setUID(sUserName);
    op.setCredentials(sPassword);
    ic.authenticate();

    boolean bAuthenticated = ic.isAuthenticated ();

    if(bAuthenticated)
    {
      AdditionalDescriptor addInfo = ic.getAdditionalDescriptor();

      if (addInfo == null || !(addInfo instanceof PersonImpl))
      {
        m_Person = new PersonImpl ();
        m_Person.setID(sUserName);
        m_Person.setFullName(me.getFullName());
        m_Person.setAttribute("globalUID", me.getGlobalUID());
      }
      else
      {
        m_Person = (IPerson)addInfo;
      }
    }

    return (bAuthenticated);
  }

  /**
   * Returns a Person object that can be used to hold site-specific attributes
   * about the logged on user.  This information is established during
   * authentication.
   * @return An object that implements the
   * <code>org.jasig.portal.security.Person</code> interface.
   */
  public IPerson getPerson ()
  {
    return m_Person;
  }
}
