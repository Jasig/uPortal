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

package org.jasig.portal.security;

import java.util.Enumeration;

/**
 * <p>This is the main interface for the JASIG portal effort's security
 * mechanism. We endeavor here to provide considerable encapsulation of
 * the data we are trying to present.</p>
 *
 * @author Andrew Newman
 * @version $Revision$
 */

public interface SecurityContext {

  /**
   * Returns the canonical authentication type for this flavor of
   * authentication. Each value returned should be either a globally registered
   * auth flavor or a local variant.
   *
   * @return The unique authentication value identifier. Values with the
   * high order 16 bits clear are local (0x0000 - 0x00FF) where values with the
   * high order 16 bits set (0xFF00 - 0xFFFF are foundation types distributed
   * by JASIG. All other should be registered and globally unique.
   */
  public int getAuthType();

  /**
   * Returns an empty object reference to an object implementing the
   * Principal interface. By operating on this returned object the
   * implementation class for the credentials type will be able to access
   * any values set in the instance without exposing an interface method that
   * would allow others (inappropriate) acces to the fields.
   *
   * @return An empty principal container.
   *
   * @see Principal
   */
   public Principal getPrincipalInstance();

  /**
   * Returns an empty object reference to an object implementing the
   * OpaqueCredentials interface. By operating on this returned object
   * the implementation class for the credentials type will be able to
   * access any values set in the Opaque credentials without exposing an
   * interface method that would allow others to access the fields.
   *
   * @return An empty credentials container.
   *
   * @see OpaqueCredentials
   */
  public OpaqueCredentials getOpaqueCredentialsInstance();

  /**
   * Performs the operation of authentication. To perform this operation, the
   * values set in the Principal object (whose reference is
   * returned by <code>getPrincipalInstance()</code>) and the
   * OpaqueCredentials object (whose reference is returned
   * by <code>getOpaqueCredentialsInstance()</code>).
   *
   *  @see #getPrincipalInstance
   *  @see #getOpaqueCredentialsInstance
   */
  public void authenticate();

  /**
   * Returns the currently authenticated principal if we are currently
   * authenticated. Note that merely testing this for a non-null pointer
   * is not sufficient to verify authenticated status. The isAuthenticated()
   * call should be used. In some authentication schemes, an asyncronous
   * event could potentially change one's authentication status.
   *
   * @return The currently authenticated principal.
   */
  public Principal getPrincipal();

  /**
   * Returns any credentials that an authenticated principal currently
   * has. Note that opaque credentials don't have any methods for examination
   * of the credentials contents. This call would primarily be useful to
   * chain authentication manually within the same authentication schem.
   *
   * @return The currently authenticated credentials object.
   *
   * @see OpaqueCredentials
   */
  public OpaqueCredentials getOpaqueCredentials();

  /**
   * Returns any additional descriptor information that might have been acquired
   * during the process of authentication. Note that this interface has no
   * methods and the object returned will have to be cast to some concrete
   * type or alternate interface to be useful.
   *
   * @return An object containing any additional descriptor information.
   *
   * @see AdditionalDescriptor
   */
  public AdditionalDescriptor getAdditionalDescriptor();

  /**
   * Returns a boolean status as to whether the descriptor corresponds to an
   * authenticated principal. Note that the get(Principaal|OpaqueCredentials)
   * calls return null until isAuthenticated first returns <code>true</code>.
   */
  public boolean isAuthenticated();

  /**
   * Returns a <code>SecurityContext</code> for the named subserviant security
   * context.
   *
   * @return The security context object reference associated with the
   * name specified as the first parameter.
   *
   * @param ctx The non-compound name of the subserviant security context.
   */
  public SecurityContext getSubContext(String ctx);

  /**
   * Returns an enumeration of the security contexts currently registered as
   * being subserviant to this one.
   *
   * @return The enumeration object containing all of the contexts.
   */
  public Enumeration getSubContexts();

  /**
   * Adds a named sub context to the list of subserviant subcontexts.
   *
   * @param name The non-compound name of the subserviant context. Note that
   * under normal circumstances the establishment of the InitialSecurityContext
   * will automatically register all subcontext.
   *
   * @param ctx The security context object to register.
   *
   */
  public void addSubContext(String name, SecurityContext ctx);

  /**
   * Returns the reason for authentication failure:
   * eg. authentication server not available
   *
   */

  public String getAuthenticationFailure();

}






