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

package org.jasig.portal.tools;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.IUserIdentityStore;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.RDBMUserIdentityStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.provider.PersonImpl;

/**
 * Title:        Delete Portal User
 * Description:  Deletes all traces of a portal user from the database
 * Company:
 * @author  Atul Shenoy and Susan Bramhall
 * @version $Revision$
 */

public class DeleteUser {

  public static void main(String[] args) {
    if (args.length < 1)
    {
        System.err.println("Usage \"DeleteUser <username>\"");
        return;
    }
    int portalUID=-1;
    IPerson per= new PersonImpl();
    per.setAttribute(IPerson.USERNAME,args[0]);

    IUserIdentityStore rdbmuser = new RDBMUserIdentityStore();

    try
    {
        portalUID=rdbmuser.getPortalUID(per, false);
        System.out.println("DeleteUser.main(): Got portal UID for " + args[0] +
          ": " + portalUID);
    }
    catch(AuthorizationException e)
    {
        System.err.println( "DeleteUser.main(): Attempting to get portal UID for "
          + args[0] + " - Authorization exception: " + e.getMessage() );
        return;
    }

    if (portalUID > -1)
    {
        try
        {
            rdbmuser.removePortalUID(portalUID);
            System.out.println("DeleteUser.main(): Removed " + portalUID +
              " from portal db.");
        }

        catch(Exception e)
        {
            System.err.println("DeleteUser.main(): Attempting to delete user: " +
              "error removing user from user identity store.");
            e.printStackTrace();
            return;
        }
    }
    else
    {
        System.err.println("DeleteUser.removePortalUid(): " +
          "Attempting to delete " + portalUID + "; unable to find user.");
        return;
    }

    /* remove from all groups */
    try
        { removeUserMemberships(args[0]); }
    catch (Exception e)
    {
        System.err.println("DeleteUser.main(): error removing user from groups: " +
          e.getMessage());
        return;
    }

     // Still left: bookmarks
     // No current way to notify channels
     // So directly access the database
         // This is the wrong way to do this
         // We need a way for channels to remove
         // data for a user when the user is deleted

      try
          { deleteBookmarks(portalUID); }
      catch(SQLException e)
      {
          System.err.println("DeleteUser.main(): Error deleting bookmarks: " +
            e.getMessage());
      }
      return;
  }
  /**
   * This method operates directly on the local group store to get 
   * and delete user memberships rather than through the group service.  
   * Since groups are cached in the JVM, the portal may have to be 
   * restarted for the changes to take effect.  
   * 
   * @param userName String - the IPerson.USERNAME of the ex-user.
   * @throws SQLException
   */
  
  public static void removeUserMemberships(String userName) throws SQLException
  {
    Connection con = null;
    Statement stmt = null;

    try
    {
        con = RDBMServices.getConnection ();
        if (con == null)
        {
            throw new SQLException("DeleteUser.removeUserMemberships(): " +
              "Unable to get a database connection.");
        }
        try 
        {
            stmt = con.createStatement();
            String sql = 
              "DELETE FROM UP_GROUP_MEMBERSHIP " +
              "WHERE MEMBER_KEY = '" + userName + "' AND GROUP_ID IN " +
              "(SELECT M.GROUP_ID " +
                "FROM UP_GROUP_MEMBERSHIP M, UP_GROUP G, UP_ENTITY_TYPE E " +
                "WHERE M.GROUP_ID = G.GROUP_ID " + 
                "  AND G.ENTITY_TYPE_ID = E.ENTITY_TYPE_ID " +
                "  AND  E.ENTITY_TYPE_NAME = 'org.jasig.portal.security.IPerson'" +
                "  AND  M.MEMBER_KEY = '" + userName + "'" +
                "  AND  M.MEMBER_IS_GROUP = 'F')";     
            
            int rows = stmt.executeUpdate(sql);
            System.out.println("Deleted " + rows + " memberships for " + userName + " from UP_GROUP_MEMBERSHIP.");
        }
        finally 
        {
            try { stmt.close(); } catch (Exception e) {}
        }
    }
    finally
    {
        try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
    }
    return;
}

  public static void deleteBookmarks(int uid) throws SQLException
  {
      DatabaseMetaData metadata = null;
      Connection con = null;
      try
      {
          con = RDBMServices.getConnection ();
          if (con == null)
          {
              throw new SQLException("DeleteUser.deleteBookmarks(): " +
                "Unable to get a database connection.");
          }
          Statement stmt = null;
          try 
          {
              metadata = con.getMetaData();
              String[] names = {"TABLE"};
              ResultSet tableNames = null;
              try
              {
                  tableNames = metadata.getTables(null,"%", "UPC_BOOKMARKS", names);
        
                  if (tableNames.next())
                  {
                      stmt = con.createStatement();
                      System.out.println("Deleting bookmarks from UPC_BOOKMARKS");
                      String bookmarksSql =
                        "DELETE FROM UPC_BOOKMARKS WHERE PORTAL_USER_ID=" + uid;
                      boolean b= stmt.execute(bookmarksSql);
                  }
              }
              finally
              {
                  try { tableNames.close(); } catch (Exception e) {}
              }
          }
          finally 
          {
              try { stmt.close(); } catch (Exception e) {}
          }
      }
      finally
      {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }
      return;
  }
}
