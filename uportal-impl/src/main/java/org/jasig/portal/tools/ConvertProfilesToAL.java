/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.StructureStylesheetDescription;
import org.jasig.portal.ThemeStylesheetDescription;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;

/**
 * ConvertProfilesToAL prepares and upgraded 2.1 database to work with
 * Aggregated Layouts and Integrated Modes.  In order to do this, it
 * converts the existing template user profiles to use the AL/IM structure and
 * theme, and deletes profiles for non-template users.  This results in the
 * resetting of layouts for normal users.
 *
 * @author Al Wold (alwold@asu.edu)
 *
 * @version $Revision$
 */
public class ConvertProfilesToAL {
   private static IUserLayoutStore uls;

   public static void main(String[] args) throws Exception {
       RDBMServices.setGetDatasourceFromJndi(false); /*don't try jndi when not in web app */
      uls = UserLayoutStoreFactory.getUserLayoutStoreImpl();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      System.out.print("Please enter the max ID for a template user => ");
      int lastTemplateUser = Integer.parseInt(br.readLine());

      Hashtable structSsList = uls.getStructureStylesheetList();
      for (Enumeration e = structSsList.keys(); e.hasMoreElements(); ) {
         Integer id = (Integer)e.nextElement();
         StructureStylesheetDescription ssd = uls.getStructureStylesheetDescription(id.intValue());
         System.out.println(id+": "+ssd.getStylesheetName());
      }
      System.out.println("==================================================");
      System.out.print("Please enter the original structure stylesheet => ");
      int simpleSsId = Integer.parseInt(br.readLine());
      System.out.print("Please enter the new structure stylesheet => ");
      int alSsId = Integer.parseInt(br.readLine());

      Hashtable themeSsList = uls.getThemeStylesheetList();
      for (Enumeration e = themeSsList.keys(); e.hasMoreElements(); ) {
         Integer id = (Integer)e.nextElement();
         ThemeStylesheetDescription tsd = uls.getThemeStylesheetDescription(id.intValue());
         System.out.println(id+": "+tsd.getStylesheetName());
      }
      System.out.println("==================================================");
      System.out.print("Please enter the new theme stylesheet => ");
      int imSsId = Integer.parseInt(br.readLine());

      List ids = getUserIds(lastTemplateUser);
      // convert template users
      for (Iterator i = ids.iterator(); i.hasNext(); ) {
         int id = ((Integer)i.next()).intValue();
         convertProfiles(id, simpleSsId, alSsId, imSsId);
      }
      // delete the rest
      deleteUserProfiles(true, lastTemplateUser);
      createTemplateProfiles(lastTemplateUser);
   }

   public static List getUserIds(int lastTemplateUser) throws PortalException {
      List userIds = new ArrayList();
      Connection con = RDBMServices.getConnection();
      try {
         String query = "SELECT USER_ID FROM UP_USER WHERE USER_ID <= ?";
         PreparedStatement ps = con.prepareStatement(query);
         ps.setInt(1, lastTemplateUser);
         try {
            ResultSet rs = ps.executeQuery();
            try {
               while (rs.next()) {
                  userIds.add(new Integer(rs.getInt(1)));
               }
               return userIds;
            } catch (SQLException sqle) {
               throw new PortalException(sqle);
            } finally {
               rs.close();
            }
         } catch (SQLException sqle) {
            throw new PortalException(sqle);
         } finally {
            ps.close();
         }
      } catch (SQLException sqle) {
         throw new PortalException(sqle);
      } finally {
         RDBMServices.releaseConnection(con);
      }
   }

   public static void deleteUserProfiles(boolean deleteLayouts, int lastTemplateUser) throws Exception {
      System.out.print("deleting user profiles...");
      Connection con = RDBMServices.getConnection();
      try {
         String query1 = "DELETE FROM UP_USER_PROFILE WHERE USER_ID > ?";
         String query2 = "DELETE FROM UP_USER_UA_MAP WHERE USER_ID > ?";
         String query3 = "DELETE FROM UP_SS_USER_PARM WHERE USER_ID > ?";
         String query4 = "DELETE FROM UP_SS_USER_ATTS WHERE USER_ID > ?";
         String query5 = "DELETE FROM UP_USER_LAYOUT WHERE USER_ID > ?";
         String query6 = "DELETE FROM UP_LAYOUT_PARAM WHERE USER_ID > ?";
         String query7 = "DELETE FROM UP_LAYOUT_STRUCT WHERE USER_ID > ?";
         PreparedStatement ps1 = con.prepareStatement(query1);
         PreparedStatement ps2 = con.prepareStatement(query2);
         PreparedStatement ps3 = con.prepareStatement(query3);
         PreparedStatement ps4 = con.prepareStatement(query4);
         PreparedStatement ps5 = con.prepareStatement(query5);
         PreparedStatement ps6 = con.prepareStatement(query6);
         PreparedStatement ps7 = con.prepareStatement(query7);
         ps1.setInt(1, lastTemplateUser);
         ps2.setInt(1, lastTemplateUser);
         ps3.setInt(1, lastTemplateUser);
         ps4.setInt(1, lastTemplateUser);
         ps5.setInt(1, lastTemplateUser);
         ps6.setInt(1, lastTemplateUser);
         ps7.setInt(1, lastTemplateUser);
         try {
            System.out.print("Deleting profiles...");
            ps1.executeUpdate();
            System.out.println("done");
            System.out.print("Deleting profile mappings...");
            ps2.executeUpdate();
            System.out.println("done");
            System.out.print("Deleting structure parameters...");
            ps3.executeUpdate();
            System.out.println("done");
            System.out.print("Deleting theme parameters...");
            ps4.executeUpdate();
            System.out.println("done");
            if (deleteLayouts) {
               System.out.print("Deleting layouts...");
               ps5.executeUpdate();
               System.out.println("done");
               System.out.print("Deleting layout parameters...");
               ps6.executeUpdate();
               System.out.println("done");
               System.out.print("Deleting layout data...");
               ps7.executeUpdate();
               System.out.println("done");
            }
         } catch (SQLException sqle) {
            throw new PortalException(sqle);
         } finally {
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();
         }
      } catch (SQLException sqle) {
         throw new PortalException(sqle);
      } finally {
         RDBMServices.releaseConnection(con);
      }
      System.out.println("done");
   }

   public static void convertProfiles(int id, int simpleSsId, int alSsId, int imSsId) throws Exception {
      System.out.print("converting profiles for ID "+id+"...");
      IPerson person = PersonFactory.createPerson();
      person.setID(id);
      Hashtable list = uls.getUserProfileList(person);
      for (Enumeration e = list.keys(); e.hasMoreElements(); ) {
         UserProfile profile = (UserProfile)list.get(e.nextElement());
         if (profile.getStructureStylesheetId() == simpleSsId) {
            profile.setStructureStylesheetId(alSsId);
            profile.setThemeStylesheetId(imSsId);
            uls.updateUserProfile(person, profile);
         }
      }
      System.out.println("done");
   }

   public static void createTemplateProfiles(int lastTemplateUser) throws PortalException {
      System.out.println("creating template profiles...");
      Connection con = RDBMServices.getConnection();
      try {
         String query = "SELECT USER_ID, USER_DFLT_USR_ID FROM UP_USER WHERE USER_ID > ?";
         String templateQuery = "SELECT USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION "+
                                "FROM UP_USER_PROFILE WHERE USER_ID = ?";
         String insert = "INSERT INTO UP_USER_PROFILE (USER_ID, PROFILE_ID, PROFILE_NAME, DESCRIPTION) "+
                         "VALUES (?, ?, ?, ?)";
         PreparedStatement selectPs = con.prepareStatement(query);
         PreparedStatement templatePs = con.prepareStatement(templateQuery);
         PreparedStatement insertPs = con.prepareStatement(insert);
         selectPs.setInt(1, lastTemplateUser);
         int templateId;
         try {
            ResultSet rs = selectPs.executeQuery();
            while (rs.next()) {
               int id = rs.getInt(1);
               templateId = rs.getInt(2);
               templatePs.setInt(1, templateId);
               ResultSet rs2 = templatePs.executeQuery();
               while(rs2.next()) {
                  insertPs.setInt(1, id);
                  insertPs.setInt(2, rs2.getInt(2));
                  insertPs.setString(3, rs2.getString(3));
                  insertPs.setString(4, rs2.getString(4));
                  insertPs.executeUpdate();
               }
               rs2.close();
            }
         } catch (SQLException sqle) {
            throw new PortalException(sqle);
         } finally {
            selectPs.close();
            templatePs.close();
            insertPs.close();
         }
      } catch (SQLException sqle) {
         throw new PortalException(sqle);
      } finally {
         RDBMServices.releaseConnection(con);
      }
   }
}
