/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.utils.CounterStoreFactory;

/**
 * SQL implementation for managing creation and removal of User Portal Data
 * @author Susan Bramhall, Yale University (modify by Julien Marchal, University Nancy 2; Eric Dalquist - edalquist@unicon.net)
 * @version $Revision$
 */
public class RDBMUserIdentityStore  implements IUserIdentityStore {

    private static final Log log = LogFactory.getLog(RDBMUserIdentityStore.class);

  //*********************************************************************
  // Constants
    private static final String defaultTemplateUserName = PropertiesManager.getProperty("org.jasig.portal.services.Authentication.defaultTemplateUserName");
    private static final String templateAttrName = "uPortalTemplateUserName";
    private static final int guestUID = 1;
    static int DEBUG = 0;
    private static final Map userLocks = Collections.synchronizedMap(new HashMap());
    
    private static synchronized Object getLock(IPerson person) {
        String username = (String)person.getAttribute(IPerson.USERNAME);
        Object lock = userLocks.get(username);
        if (lock == null) {
            lock = new Object();
            userLocks.put(username, lock);
        }
        return lock;
    }
    
    private static synchronized void removeLock(IPerson person) {
        String username = (String)person.getAttribute(IPerson.USERNAME);
        userLocks.remove(username);
    }



 /**
   * getuPortalUID -  return a unique uPortal key for a user.
   *    calls alternate signature with createPortalData set to false.
   * @param person the person object
   * @return uPortalUID number
   * @throws Exception if no user is found.
   */
  public int getPortalUID (IPerson person) throws Exception {
    int uPortalUID=-1;
    uPortalUID=this.getPortalUID(person, false);
    return uPortalUID;
    }

  /**
   *
   * removeuPortalUID
   * @param   uPortalUID integer key to uPortal data for a user
   * @throws SQLException exception if a sql error is encountered
   */
  public void removePortalUID(int uPortalUID) throws Exception {
    Connection con = RDBMServices.getConnection();
    java.sql.PreparedStatement ps = null;
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.createStatement();
      if (RDBMServices.getDbMetaData().supportsTransactions())
        con.setAutoCommit(false);

      // START of Addition after bug declaration (bug id 1516)
      // Permissions delete
      // must be made before delete user in UP_USER
      rs = stmt.executeQuery("SELECT USER_NAME FROM UP_USER WHERE USER_ID="+uPortalUID);
      String name = "";
      if ( rs.next() )
        name = rs.getString(1);
      rs.close();
      rs = stmt.executeQuery("SELECT ENTITY_TYPE_ID FROM UP_ENTITY_TYPE WHERE ENTITY_TYPE_NAME = 'org.jasig.portal.security.IPerson'");
      int type = -1;
      if ( rs.next() )
        type = rs.getInt(1);
      rs.close();
      rs = null;
      String SQLDelete = "DELETE FROM UP_PERMISSION WHERE PRINCIPAL_KEY='"+name+"' AND PRINCIPAL_TYPE="+type;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      rs = stmt.executeQuery("SELECT M.GROUP_ID " +
			"FROM UP_GROUP_MEMBERSHIP M, UP_GROUP G, UP_ENTITY_TYPE E " +
			"WHERE M.GROUP_ID = G.GROUP_ID " +
			"  AND G.ENTITY_TYPE_ID = E.ENTITY_TYPE_ID " +
			"  AND  E.ENTITY_TYPE_NAME = 'org.jasig.portal.security.IPerson'" +
			"  AND  M.MEMBER_KEY ='"+name+"' AND  M.MEMBER_IS_GROUP = 'F'");
      java.util.Vector groups = new java.util.Vector();
      while ( rs.next() )
        groups.add(rs.getString(1));
      rs.close();
      rs = null;

      // Remove from local group
      // Delete from DeleteUser.java and place here
      // must be made before delete user in UP_USER
      ps = con.prepareStatement("DELETE FROM UP_GROUP_MEMBERSHIP WHERE MEMBER_KEY='"+name+"' AND GROUP_ID=?");
      for ( int i = 0; i < groups.size(); i++ ) {
        String group = (String) groups.get(i);
        ps.setString(1,group);
        ps.executeUpdate();
      }
      if ( ps != null ) ps.close();
      // END of Addition after bug declaration (bug id 1516)

      SQLDelete = "DELETE FROM UP_USER WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_LAYOUT  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_PARAM WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_PROFILE  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_LAYOUT    WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_SS_USER_PARM  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_LAYOUT_PARAM WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      // START of Addition after bug declaration (bug id 1516)
      SQLDelete = "DELETE FROM UP_USER_LOCALE WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_PROFILE_MDATA WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_PROFILE_LOCALE WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_LAYOUT_AGGR WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_USER_LAYOUT_MDATA WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT_AGGR  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_LAYOUT_STRUCT_MDATA  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);

      SQLDelete = "DELETE FROM UP_LAYOUT_RESTRICTIONS  WHERE USER_ID = " + uPortalUID;
      if (log.isDebugEnabled())
          log.debug("RDBMUserIdentityStore::removePortalUID(): " + SQLDelete);
      stmt.executeUpdate(SQLDelete);
      // END of Addition after bug declaration (bug id 1516)

      if (RDBMServices.getDbMetaData().supportsTransactions())
        con.commit();

      try {
          IPortletPreferencesStore portletPrefStore = PortletPreferencesStoreFactory.getPortletPreferencesStoreImpl();
          portletPrefStore.deletePortletPreferencesByUser(uPortalUID);
      }
      catch (Exception e) { }

    }
    catch (SQLException se) {
      try {
      	log.error( "RDBMUserIdentityStore::removePortalUID(): " + se);
        if (RDBMServices.getDbMetaData().supportsTransactions())
          con.rollback();
      }
      catch (SQLException e) {
      	log.error( "RDBMUserIdentityStore::removePortalUID(): " + e);
      }
        if (DEBUG>0) {
         System.err.println("SQLException: " + se.getMessage());
         System.err.println("SQLState:  " + se.getSQLState());
         System.err.println("Message:  " + se.getMessage());
         System.err.println("Vendor:  " + se.getErrorCode());
        }

        throw se;
      }
    finally {
      RDBMServices.closeResultSet(rs);
      RDBMServices.closeStatement(stmt);
      RDBMServices.closeStatement(ps);
      RDBMServices.releaseConnection(con);
    }
    }

   /**
    * Return the username to be used for authorization (exit hook)
    * @param person
    * @return usernmae
    */
   public String getUsername(IPerson person) {
	   return (String)person.getAttribute(IPerson.USERNAME);
   }

   /**
    * Get the portal user ID for this person object.
    * @param person
    * @param createPortalData indicating whether to try to create all uPortal data for this user from template prototype
    * @return uPortalUID number or -1 if unable to create user.
    * @throws AuthorizationException if createPortalData is false and no user is found
    *  or if a sql error is encountered
    */
   public int getPortalUID (IPerson person, boolean createPortalData) throws AuthorizationException {
       int uid;
       String username = (String)person.getAttribute(IPerson.USERNAME);

       // only synchronize a non-guest request.
       if (PersonFactory.GUEST_USERNAME.equals(username)) {
           uid = __getPortalUID(person, createPortalData);
       } else {
           Object lock = getLock(person);
           synchronized (lock) {
               uid = __getPortalUID(person, createPortalData);
           }
           removeLock(person);
       }
       return uid;
   }
   
   private int __getPortalUID (IPerson person, boolean createPortalData) throws AuthorizationException {
       PortalUser portalUser = null;

       try {
           String userName = getUsername(person);
           String templateName = getTemplateName(person);
           portalUser = getPortalUser(userName);

           if (createPortalData) {
               //If we are allowed to modify the database

               if (portalUser != null) {
                   //If the user has logged in we may have to update their template user information

                   boolean hasSavedLayout = userHasSavedLayout(portalUser.getUserId());
                   if (!hasSavedLayout) {

                       TemplateUser templateUser = getTemplateUser(templateName);
                       if (portalUser.getDefaultUserId() != templateUser.getUserId()) {
                           //Update user data with new template user's data
                           updateUser(portalUser.getUserId(), person, templateUser);
                       }
                   }
               }
               else {
                   //User hasn't logged in before, some data needs to be created for them based on their template user

                   // Retrieve the information for the template user
                   TemplateUser templateUser = getTemplateUser(templateName);
                   if (templateUser == null) {
                       throw new AuthorizationException("No information found for template user = " + templateName + ". Cannot create new account for " + userName);
                   }

                   // Get a new user ID for this user
                   int newUID = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_USER");

                   // Add new user to all appropriate tables
                   int newPortalUID = addNewUser(newUID, person, templateUser);
                   portalUser = new PortalUser();
                   portalUser.setUserId(newPortalUID);
               }
           }
           else if (portalUser == null) {
               //If this is a new user and we can't create them
               throw new AuthorizationException("No portal information exists for user " + userName);
           }

       } catch (Exception e) {
           log.error(e.getMessage(), e);
           throw new AuthorizationException(e);
       }

       return portalUser.getUserId();
   }

  static final protected void commit (Connection connection) {
    try {
      if (RDBMServices.getDbMetaData().supportsTransactions())
        connection.commit();
    } catch (Exception e) {
      log.error( "RDBMUserIdentityStore::commit(): " + e);
    }
  }

  static final protected void rollback (Connection connection) {
    try {
      if (RDBMServices.getDbMetaData().supportsTransactions())
        connection.rollback();
    } catch (Exception e) {
      log.error( "RDBMUserIdentityStore::rollback(): " + e);
    }
  }

  /**
   * Gets the PortalUser data store object for the specified user name.
   *
   * @param userName The user's name
   * @return A PortalUser object or null if the user doesn't exist.
   * @throws Exception
   */
  protected PortalUser getPortalUser(String userName) throws Exception {
      PortalUser portalUser = null;

      Connection con = null;
      try {
          con = RDBMServices.getConnection();
          PreparedStatement pstmt = null;

          try {
              String query = "SELECT USER_ID, USER_DFLT_USR_ID FROM UP_USER WHERE USER_NAME=?";

              pstmt = con.prepareStatement(query);
              pstmt.setString(1, userName);

              ResultSet rs = null;
              try {
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::getPortalUID(userName=" + userName + "): " + query);
                  rs = pstmt.executeQuery();
                  if (rs.next()) {
                      portalUser = new PortalUser();
                      portalUser.setUserId(rs.getInt("USER_ID"));
                      portalUser.setUserName(userName);
                      portalUser.setDefaultUserId(rs.getInt("USER_DFLT_USR_ID"));
                  }
              } finally {
                  try { rs.close(); } catch (Exception e) {}
              }
          } finally {
              try { pstmt.close(); } catch (Exception e) {}
          }
      } finally {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }

      return portalUser;
  }

  protected String getTemplateName(IPerson person) {
      String templateName = (String)person.getAttribute(templateAttrName);
      // Just use the default template if requested template not populated
      if (templateName == null || templateName.equals("")) {
          templateName = defaultTemplateUserName;
      }
      return templateName;
  }

  /**
   * Gets the TemplateUser data store object for the specified template user name.
   *
   * @param templateUserName The template user's name
   * @return A TemplateUser object or null if the user doesn't exist.
   * @throws Exception
   */
  protected TemplateUser getTemplateUser(String templateUserName) throws Exception {
      TemplateUser templateUser = null;

      Connection con = null;
      try {
          con = RDBMServices.getConnection();
          PreparedStatement pstmt = null;
          try {
              String query = "SELECT USER_ID, USER_DFLT_LAY_ID FROM UP_USER WHERE USER_NAME=?";

              pstmt = con.prepareStatement(query);
              pstmt.setString(1, templateUserName);

              ResultSet rs = null;
              try {
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::getTemplateUser(templateUserName=" + templateUserName + "): " + query);
                  rs = pstmt.executeQuery();
                  if (rs.next()) {
                      templateUser = new TemplateUser();
                      templateUser.setUserName(templateUserName);
                      templateUser.setUserId(rs.getInt("USER_ID"));
                      templateUser.setDefaultLayoutId(rs.getInt("USER_DFLT_LAY_ID"));
                  } else {
                      if (!templateUserName.equals(defaultTemplateUserName)) {
                          templateUser = getTemplateUser(defaultTemplateUserName);
                      }
                  }
              } finally {
                  try { rs.close(); } catch (Exception e) {}
              }
          } finally {
              try { pstmt.close(); } catch (Exception e) {}
          }
      } finally {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }

      return templateUser;
  }

  protected boolean userHasSavedLayout(int userId) throws Exception {
      boolean userHasSavedLayout = false;
      Connection con = null;
      try {
          con = RDBMServices.getConnection();
          PreparedStatement pstmt = null;
          try {
              String query = "SELECT * FROM UP_USER_PROFILE WHERE USER_ID=? AND LAYOUT_ID IS NOT NULL AND LAYOUT_ID!=0";

              pstmt = con.prepareStatement(query);
              pstmt.setInt(1, userId);

              ResultSet rs = null;
              try {
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::getTemplateUser(userId=" + userId + "): " + query);
                  rs = pstmt.executeQuery();
                  if (rs.next()) {
                      userHasSavedLayout = true;
                  }
              } finally {
                  try { rs.close(); } catch (Exception e) {}
              }
          } finally {
              try { pstmt.close(); } catch (Exception e) {}
          }
      } finally {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }

      return userHasSavedLayout;
  }
  
  private ILockableEntityGroup getSafeLockableGroup(IEntityGroup eg, IGroupMember gm) {
      if (log.isTraceEnabled()) {
          log.trace("Creating lockable group for group/member: " + eg + "/" + gm);
      }
      
      ILockableEntityGroup leg = null;
      
      try {
          if (eg.isEditable()) {
	          leg = GroupService.findLockableGroup(eg.getKey(), gm.getKey());
          }
      } catch (Exception e) {
          // Bummer.  but the only thing to do is to press on
          log.error("Unable to create lockable group for group/member: " + eg + "/" + gm, e);
      }
      
      return leg;
  }
  
  /**
   * Remove a person from a group.  This method catches and logs exceptions
   * exceptions encountered performing the removal.
   * @param person person to be removed (used for logging)
   * @param me member representing the person
   * @param eg group from which the user should be removed
   */
  private void removePersonFromGroup(IPerson person, IGroupMember me, IEntityGroup eg) {
      if (log.isTraceEnabled()) {
          log.trace("Removing " + person + " from group " + eg);
      }
      try {
          if (eg.isEditable()) {
              eg.removeMember(me);
              eg.updateMembers();
          }
      } catch (Exception e) {
          // Bummer.  but the only thing to do is to press on
          log.error("Unable to remove " + person + " from group " + eg, e);
      }
  }
  
  /**
   * Add a person to a group. This method catches and logs exceptions encountered
   * performing the removal.
   * @param person person to be added (used for logging)
   * @param me member representing the person
   * @param eg group to which the user should be added
   */
  private void addPersonToGroup(IPerson person, IGroupMember me, IEntityGroup eg) {
      if (log.isTraceEnabled()) {
          log.trace("Adding " + person + " to group " + eg);
      }
      try {
          if (eg.isEditable()) {
              eg.addMember(me);
              eg.updateMembers();
          }
      } catch (Exception e) {
          log.error("Unable to add " + person + " to group " + eg, e);
      }
  }

  protected void updateUser(int userId, IPerson person, TemplateUser templateUser) throws Exception {
      // Remove my existing group memberships
      IGroupMember me = GroupService.getGroupMember(person.getEntityIdentifier());
      Iterator myExistingGroups = me.getContainingGroups();
      while (myExistingGroups.hasNext()) {
          IEntityGroup eg = (IEntityGroup)myExistingGroups.next();
          ILockableEntityGroup leg = getSafeLockableGroup(eg, me);
          if (leg != null) {
              removePersonFromGroup(person, me, leg);
          }
      }

      // Copy template user's groups memberships
      IGroupMember template = GroupService.getEntity(templateUser.getUserName(), Class.forName("org.jasig.portal.security.IPerson"));
      Iterator templateGroups = template.getContainingGroups();
      while (templateGroups.hasNext()) {
          IEntityGroup eg = (IEntityGroup)templateGroups.next();
          ILockableEntityGroup leg = getSafeLockableGroup(eg, me);
          if (leg != null) {
              addPersonToGroup(person, me, leg);
          }
      }

      Connection con = null;
      try {
          con = RDBMServices.getConnection();
          // Turn off autocommit if the database supports it
          if (RDBMServices.getDbMetaData().supportsTransactions())
              con.setAutoCommit(false);

          PreparedStatement deleteStmt = null;
          PreparedStatement queryStmt = null;
          PreparedStatement insertStmt = null;
          try {
              // Update UP_USER
              String update =
                  "UPDATE UP_USER " +
                  "SET USER_DFLT_USR_ID=?, " +
                      "USER_DFLT_LAY_ID=?, " +
                      "NEXT_STRUCT_ID=null " +
                  "WHERE USER_ID=?";

              insertStmt = con.prepareStatement(update);
              insertStmt.setInt(1, templateUser.getUserId());
              insertStmt.setInt(2, templateUser.getDefaultLayoutId());
              insertStmt.setInt(3, userId);

              if (log.isDebugEnabled())
                  log.debug("RDBMUserIdentityStore::addNewUser(): " + update);
              insertStmt.executeUpdate();
              insertStmt.close();

              // Start copying...
              ResultSet rs = null;
              String delete = null;
              String query = null;
              String insert = null;
              try {
                  // Update UP_USER_PARAM
                  delete =
                      "DELETE FROM UP_USER_PARAM " +
                      "WHERE USER_ID=?";
                  deleteStmt = con.prepareStatement(delete);
                  deleteStmt.setInt(1, userId);
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + "): " + delete);
                  deleteStmt.executeUpdate();
                  deleteStmt.close();

                  query =
                      "SELECT USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE " +
                      "FROM UP_USER_PARAM " +
                      "WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {
                      insert =
                          "INSERT INTO UP_USER_PARAM (USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE) " +
                          "VALUES(?, ?, ?)";

                      String userParamName = rs.getString("USER_PARAM_NAME");
                      String userParamValue = rs.getString("USER_PARAM_VALUE");

                      insertStmt.setInt(1, userId);
                      insertStmt.setString(2, userParamName);
                      insertStmt.setString(3, userParamValue);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + ", USER_PARAM_NAME=" + userParamName + ", USER_PARAM_VALUE=" + userParamValue + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  queryStmt.close();
                  insertStmt.close();


                  // Update UP_USER_PROFILE
                  delete =
                      "DELETE FROM UP_USER_PROFILE " +
                      "WHERE USER_ID=?";
                  deleteStmt = con.prepareStatement(delete);
                  deleteStmt.setInt(1, userId);
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + "): " + delete);
                  deleteStmt.executeUpdate();
                  deleteStmt.close();

                  query =
                      "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION " +
                      "FROM UP_USER_PROFILE " +
                      "WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insert =
                      "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID) " +
                      "VALUES(?, ?, ?, ?, NULL, NULL, NULL)";
                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {

                      int profileId = rs.getInt("PROFILE_ID");
                      String profileName = rs.getString("PROFILE_NAME");
                      String description = rs.getString("DESCRIPTION");

                      insertStmt.setInt(1, userId);
                      insertStmt.setInt(2, profileId);
                      insertStmt.setString(3, profileName);
                      insertStmt.setString(4, description);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + ", PROFILE_ID=" + profileId + ", PROFILE_NAME=" + profileName + ", DESCRIPTION=" + description + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  queryStmt.close();
                  insertStmt.close();


                  // Update UP_USER_UA_MAP
                  delete =
                      "DELETE FROM UP_USER_UA_MAP " +
                      "WHERE USER_ID=?";
                  deleteStmt = con.prepareStatement(delete);
                  deleteStmt.setInt(1, userId);
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + "): " + delete);
                  deleteStmt.executeUpdate();
                  deleteStmt.close();

                  query =
                      "SELECT USER_ID, USER_AGENT, PROFILE_ID " +
                      "FROM UP_USER_UA_MAP WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insert =
                      "INSERT INTO UP_USER_UA_MAP (USER_ID, USER_AGENT, PROFILE_ID) " +
                      "VALUES(?, ?, ?)";
                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {
                      String userAgent = rs.getString("USER_AGENT");
                      String profileId = rs.getString("PROFILE_ID");

                      insertStmt.setInt(1, userId);
                      insertStmt.setString(2, userAgent);
                      insertStmt.setString(3, profileId);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::updateUser(USER_ID=" + userId + ", USER_AGENT=" + userAgent + ", PROFILE_ID=" + profileId + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  queryStmt.close();
                  insertStmt.close();

                  // If we made it all the way though, commit the transaction
                  if (RDBMServices.getDbMetaData().supportsTransactions())
                      con.commit();

              }
              finally {
                  try { rs.close(); } catch (Exception e) {}
              }
          }
          finally {
              try { deleteStmt.close(); } catch (Exception e) {}
              try { queryStmt.close(); } catch (Exception e) {}
              try { insertStmt.close(); } catch (Exception e) {}
          }
      }
      catch (SQLException sqle) {
          if (RDBMServices.getDbMetaData().supportsTransactions())
              con.rollback();
          throw new AuthorizationException("SQL database error while retrieving user's portal UID", sqle);
      }
      finally {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }

      return;
  }

  protected int addNewUser(int newUID, IPerson person, TemplateUser templateUser) throws Exception {
      // Copy template user's groups memberships
      IGroupMember me = GroupService.getGroupMember(person.getEntityIdentifier());
      IGroupMember template = GroupService.getEntity(templateUser.getUserName(), Class.forName("org.jasig.portal.security.IPerson"));
      Iterator templateGroups = template.getContainingGroups();
      while (templateGroups.hasNext()) {
          IEntityGroup eg = (IEntityGroup)templateGroups.next();
          ILockableEntityGroup leg = getSafeLockableGroup(eg, me);
          if (leg != null) {
              addPersonToGroup(person, me, leg);
          }
      }

      int uPortalUID = -1;
      Connection con = null;
      try {
          con = RDBMServices.getConnection();
          // Turn off autocommit if the database supports it
          if (RDBMServices.getDbMetaData().supportsTransactions())
              con.setAutoCommit(false);

          PreparedStatement queryStmt = null;
          PreparedStatement insertStmt = null;
          try {
              // Add to UP_USER
              String insert =
                  "INSERT INTO UP_USER (USER_ID, USER_NAME, USER_DFLT_USR_ID, USER_DFLT_LAY_ID, NEXT_STRUCT_ID, LST_CHAN_UPDT_DT)" +
                  "VALUES (?, ?, ?, ?, null, null)";

              String userName = getUsername(person);

              insertStmt = con.prepareStatement(insert);
              insertStmt.setInt(1, newUID);
              insertStmt.setString(2, userName);
              insertStmt.setInt(3, templateUser.getUserId());
              insertStmt.setInt(4, templateUser.getDefaultLayoutId());

              if (log.isDebugEnabled())
                  log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + newUID + ", USER_NAME=" + userName + ", USER_DFLT_USR_ID=" + templateUser.getUserId() + ", USER_DFLT_LAY_ID=" + templateUser.getDefaultLayoutId() + "): " + insert);
              insertStmt.executeUpdate();
              insertStmt.close();
              insertStmt = null;


              // Start copying...
              ResultSet rs = null;
              String query = null;
              try {
                  // Add to UP_USER_PARAM
                  query =
                      "SELECT USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE " +
                      "FROM UP_USER_PARAM " +
                      "WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insert =
                      "INSERT INTO UP_USER_PARAM (USER_ID, USER_PARAM_NAME, USER_PARAM_VALUE) " +
                      "VALUES(?, ?, ?)";
                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {
                      String userParamName = rs.getString("USER_PARAM_NAME");
                      String userParamValue = rs.getString("USER_PARAM_VALUE");

                      insertStmt.setInt(1, newUID);
                      insertStmt.setString(2, userParamName);
                      insertStmt.setString(3, userParamValue);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + newUID + ", USER_PARAM_NAME=" + userParamName + ", USER_PARAM_VALUE=" + userParamValue + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  queryStmt.close();

                  if (insertStmt != null) {
                    insertStmt.close();
                    insertStmt = null;
                  }


                  // Add to UP_USER_PROFILE
                  query =
                      "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION " +
                      "FROM UP_USER_PROFILE " +
                      "WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insert =
                      "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION, LAYOUT_ID, STRUCTURE_SS_ID, THEME_SS_ID) " +
                      "VALUES(?, ?, ?, ?, NULL, NULL, NULL)";
                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {

                      int profileId = rs.getInt("PROFILE_ID");
                      String profileName = rs.getString("PROFILE_NAME");
                      String description = rs.getString("DESCRIPTION");

                      insertStmt.setInt(1, newUID);
                      insertStmt.setInt(2, profileId);
                      insertStmt.setString(3, profileName);
                      insertStmt.setString(4, description);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + newUID + ", PROFILE_ID=" + profileId + ", PROFILE_NAME=" + profileName + ", DESCRIPTION=" + description + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  queryStmt.close();

                  if (insertStmt != null) {
                    insertStmt.close();
                    insertStmt = null;
                  }


                  query =
                      "SELECT USER_ID, USER_AGENT, PROFILE_ID " +
                      "FROM UP_USER_UA_MAP WHERE USER_ID=?";
                  queryStmt = con.prepareStatement(query);
                  queryStmt.setInt(1, templateUser.getUserId());
                  if (log.isDebugEnabled())
                      log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + templateUser.getUserId() + "): " + query);
                  rs = queryStmt.executeQuery();

                  insert =
                      "INSERT INTO UP_USER_UA_MAP (USER_ID, USER_AGENT, PROFILE_ID) " +
                      "VALUES(?, ?, ?)";
                  insertStmt = con.prepareStatement(insert);
                  while (rs.next()) {

                      String userAgent = rs.getString("USER_AGENT");
                      String profileId = rs.getString("PROFILE_ID");

                      insertStmt.setInt(1, newUID);
                      insertStmt.setString(2, userAgent);
                      insertStmt.setString(3, profileId);

                      if (log.isDebugEnabled())
                          log.debug("RDBMUserIdentityStore::addNewUser(USER_ID=" + newUID + ", USER_AGENT=" + userAgent + ", PROFILE_ID=" + profileId + "): " + insert);
                      insertStmt.executeUpdate();
                  }
                  rs.close();
                  rs = null;
                  queryStmt.close();
                  queryStmt = null;

                  if (insertStmt != null) {
                    insertStmt.close();
                    insertStmt = null;
                  }


                  // If we made it all the way though, commit the transaction
                  if (RDBMServices.getDbMetaData().supportsTransactions())
                      con.commit();

                  uPortalUID = newUID;

              } finally {
                  try { if (rs != null) rs.close(); } catch (Exception e) {}
              }
          } finally {
              try { if (queryStmt != null) queryStmt.close(); } catch (Exception e) {}
              try { if (insertStmt != null) insertStmt.close(); } catch (Exception e) {}
          }
      } catch (SQLException sqle) {
          if (RDBMServices.getDbMetaData().supportsTransactions())
              con.rollback();
          throw new AuthorizationException("SQL database error while retrieving user's portal UID", sqle);
      } finally {
          try { RDBMServices.releaseConnection(con); } catch (Exception e) {}
      }

      return uPortalUID;
  }

  protected class PortalUser {
      String userName;
      int userId;
      int defaultUserId;
      public String getUserName() { return userName; }
      public int getUserId() { return userId; }
      public int getDefaultUserId() { return defaultUserId; }
      public void setUserName(String userName) { this.userName = userName; }
      public void setUserId(int userId) { this.userId = userId; }
      public void setDefaultUserId(int defaultUserId) { this.defaultUserId = defaultUserId; }
  }

  protected class TemplateUser {
      String userName;
      int userId;
      int defaultLayoutId;
      public String getUserName() { return userName; }
      public int getUserId() { return userId; }
      public int getDefaultLayoutId() { return defaultLayoutId; }
      public void setUserName(String userName) { this.userName = userName; }
      public void setUserId(int userId) { this.userId = userId; }
      public void setDefaultLayoutId(int defaultLayoutId) { this.defaultLayoutId = defaultLayoutId; }
  }

}



