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


package  org.jasig.portal.security.provider;

import  org.jasig.portal.security.PermissionManager;
import  org.jasig.portal.security.Permission;
import  org.jasig.portal.RdbmServices;
import  org.jasig.portal.Logger;
import  org.jasig.portal.AuthorizationException;
import  java.sql.Connection;
import  java.sql.Statement;
import  java.sql.ResultSet;
import  java.util.ArrayList;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 */
public class ReferencePermissionManager extends PermissionManager {

  /**
   * This constructor ensures that the PermissionManager will be created with an owner specified
   * @param owner
   */
  public ReferencePermissionManager (String owner) {
    // Make sure to call the constructor of the PermissionManager class
    super(owner);
  }

  /**
   * Add a new Permission to the system.
   * @param newPermission
   */
  public void setPermission (Permission newPermission) {
    Connection connection = RdbmServices.getConnection();
    try {
      String updateStatement = "INSERT INTO UP_PERMISSIONS (OWNER, PRINCIPAL, ACTIVITY, TARGET, EFFECTIVE, EXPIRES) VALUES (";
      updateStatement += "'" + m_owner + "',";
      updateStatement += "'" + newPermission.getPrincipal() + "',";
      updateStatement += "'" + newPermission.getActivity() + "',";
      updateStatement += "'" + newPermission.getTarget() + "',";
      updateStatement += "null, null";
      updateStatement += ")";
      Statement statement = connection.createStatement();
      statement.executeUpdate(updateStatement);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      RdbmServices.releaseConnection(connection);
    }
  }

  /**
   * Add a new set of Permission objects to the system.
   * @param newPermissions
   */
  public void setPermissions (Permission[] newPermissions) {
    for (int i = 0; i < newPermissions.length; i++) {
      setPermission(newPermissions[i]);
    }
  }

  /**
   * Retrieve an array of Permission objects based on the given parameters. Any null parameters
   * will be ignored. So to retrieve a set of Permission objects for a given principal you would call
   * this method like pm.getPermissions('principal name', null, null)
   * @param principal
   * @param activity
   * @param target
   * @return 
   * @exception AuthorizationException
   */
  public Permission[] getPermissions (String principal, String activity, String target) throws AuthorizationException {
    Connection connection = RdbmServices.getConnection();
    try {
      StringBuffer queryString = new StringBuffer(255);
      queryString.append("SELECT * FROM UP_PERMISSIONS WHERE OWNER = '" + m_owner.toUpperCase() + "'");
      if (principal != null) {
        queryString.append(" AND PRINCIPAL = '" + principal.toUpperCase() + "'");
      }
      if (activity != null) {
        queryString.append(" AND ACTIVITY = '" + activity.toUpperCase() + "'");
      }
      if (target != null) {
        queryString.append(" AND TARGET = '" + target.toUpperCase() + "'");
      }
      Statement statement = connection.createStatement();
      System.out.println(queryString.toString());
      ResultSet rs = statement.executeQuery(queryString.toString());
      ArrayList permissions = new ArrayList();
      while (rs.next()) {
        Permission permission = new ReferencePermission(m_owner);
        permission.setPrincipal(rs.getString("PRINCIPAL"));
        permission.setActivity(rs.getString("ACTIVITY"));
        permission.setTarget(rs.getString("TARGET"));
        permission.setEffective(rs.getDate("EFFECTIVE"));
        permission.setExpires(rs.getDate("EXPIRES"));
        permissions.add(permission);
      }
      if (permissions.size() > 0) {
        return((Permission[])permissions.toArray(new Permission[0])); 
      } 
      else {
        return  (null);
      }
    } catch (Exception e) {
      throw  new AuthorizationException(e.getMessage());
    }
  }
}



