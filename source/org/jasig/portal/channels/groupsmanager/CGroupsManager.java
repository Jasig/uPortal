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

package  org.jasig.portal.channels.groupsmanager;

import  java.util.*;
import  java.io.*;
import  org.jasig.portal.services.*;
import  org.apache.log4j.Priority;
import  org.jasig.portal.*;
import  org.jasig.portal.security.*;
import  org.jasig.portal.security.provider.*;
import  org.jasig.portal.utils.*;
import  org.jasig.portal.channels.permissionsmanager.*;
import  org.jasig.portal.groups.*;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Text;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.XMLSerializer;
import  org.apache.xml.serialize.OutputFormat;
import  org.xml.sax.ContentHandler;
import  org.xml.sax.InputSource;

/**
 * CGroupsManager allows users to graphically administer all groups for which
 * user has administrtaive permissions.
 * @author Don Fracapane
 * @version $Revision$
 */
public class CGroupsManager
      implements org.jasig.portal.IMultithreadedChannel, GroupsManagerConstants, IPermissible, IMultithreadedCacheable {
   // Location of the stylesheet list file
   protected static final String sslLocation = "CGroupsManager.ssl";
   protected static HashMap activities = null;
   protected HashMap targets = null;
   protected HashMap sessionsMap = new HashMap();

   /** Creates new GroupsManagerChannel */
   public CGroupsManager () {
      init();
   }

   /**
    * put your documentation comment here
    */
   protected synchronized void init () {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::init(): this = " + this);
      if (activities == null) {
         activities = new HashMap();
         try {
            activities.put(CREATE_PERMISSION, "Create a group in this context");
            activities.put(VIEW_PERMISSION, "View this group");
            activities.put(UPDATE_PERMISSION, "Rename this group");
            activities.put(DELETE_PERMISSION, "Delete this group");
            activities.put(SELECT_PERMISSION, "Select this group");
            activities.put(ADD_REMOVE_PERMISSION, "Manage this group's members");
            activities.put(ASSIGN_PERMISSION, "Assign Permissions for this group");
         } catch (Exception e) {
            Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set activities"
                  + e);
         }
      }
      try {
         if (targets == null) {
            targets = new HashMap();
            IEntityGroup everyone = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
            targets.put(everyone.getKey(), everyone.getName());
            Iterator allgroups = everyone.getAllMembers();
            while (allgroups.hasNext()) {
               IGroupMember g = (IGroupMember)allgroups.next();
               if (g.isGroup()) {
                  if (targets.get(g.getKey()) == null) {
                     try {
                        targets.put(g.getKey(), ((IEntityGroup)g).getName());
                     } catch (Exception e) {
                        Utility.logMessage("ERROR", "CGroupsManager.init():: unable to add target"
                              + e);
                     }
                  }
               }
            }

            IEntityGroup allChans = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
            targets.put(allChans.getKey(), allChans.getName());
            Iterator allcgroups = allChans.getAllMembers();
            while (allcgroups.hasNext()) {
               IGroupMember g = (IGroupMember)allcgroups.next();
               if (g.isGroup()) {
                  if (targets.get(g.getKey()) == null) {
                     try {
                        targets.put(g.getKey(), ((IEntityGroup)g).getName());
                     } catch (Exception e) {
                        Utility.logMessage("ERROR", "CGroupsManager.init():: unable to add target"
                              + e);
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "CGroupsManager.init():: unable to set targets"
               + e);
      }
   }

   /**
    * Acquires ChannelRuntimeProperites from the channel.
    * This function may be called by the portal framework throughout the session.
    * @see ChannelRuntimeProperties
    * @param uid
    * @return ChannelRuntimeProperties
    */
   public ChannelRuntimeProperties getRuntimeProperties (String uid) {
      return  new ChannelRuntimeProperties();
   }

   /**
    * Passes an outside event to a channel.
    * Events should normally come from the LayoutBean.
    * @param ev PortalEvent object
    * @param uid
    * @see PortalEvent
    */
   public void receiveEvent (PortalEvent ev, String uid)
   //public void receiveEvent(LayoutEvent ev)
   {
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
         try{
            CGroupsManagerSessionData sd = getSessionData(uid);
            if (sd.lockedGroup != null){
               sd.lockedGroup.getLock().release();
               sd.lockedGroup = null;
               //GroupsManagerCommandFactory.get("Unlock").execute(sd);
            }
            if (sd.servantChannel != null){
               sd.servantChannel.isFinished();
            }
            sessionsMap.remove(uid); // Clean up
         } catch (Exception e){
            Utility.logMessage("ERROR", this.getClass().getName() + "::receiveEvent(): Exception = " + e);
         }
      }
   }

   /**
    * Ask channel to render its content.
    * @param out the SAX ContentHandler to output content to
    * @exception PortalException
    * @param uid
    */
   public void renderXML (ContentHandler out, String uid) throws PortalException {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      long time1 = Calendar.getInstance().getTime().getTime();
      long time2 = 0;
      Document viewDoc = null;
      try {
         if (sessionData.servantChannel != null) {
            ((IChannel)sessionData.servantChannel).renderXML(out);
            Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML(): Defering to servant render");
         }
         else {
            viewDoc = sessionData.model;
            time2 = Calendar.getInstance().getTime().getTime();
            XSLT xslt = new XSLT(this);
            xslt.setXML(viewDoc);
            xslt.setTarget(out);
            xslt.setStylesheetParameter("baseActionURL", sessionData.runtimeData.getBaseActionURL());
           if (sessionData.isAdminUser) {
               xslt.setStylesheetParameter("ignorePermissions", "true");
            }
            if (sessionData.customMessage !=null) {
               xslt.setStylesheetParameter("customMessage", sessionData.customMessage);
            }
            xslt.setStylesheetParameter("mode", sessionData.mode);
            xslt.setStylesheetParameter("page", String.valueOf(sessionData.currentPage));
            if (sessionData.highlightedGroupID != null) {
               xslt.setStylesheetParameter("highlightedGroupID", sessionData.highlightedGroupID);
            }
            if (sessionData.rootViewGroupID != null) {
               xslt.setStylesheetParameter("rootViewGroupID", sessionData.rootViewGroupID);
            }
            if (sessionData.feedback != null) {
               xslt.setStylesheetParameter("feedback", sessionData.feedback);
               sessionData.feedback = null;
            }
            if (sessionData.servantMode) {
               xslt.setStylesheetParameter("grpServantMode", "true");
            }
            if (!sessionData.allowFinish) {
              xslt.setStylesheetParameter("blockFinishActions", "true");
            }
            if (sessionData.blockEntitySelect) {
              xslt.setStylesheetParameter("blockEntitySelect", "true");
            }
            try {
               //Utility.logMessage("DEBUG", this.getClass().getName()
               //        + ".renderXML(): grpView=" + runtimeData.getParameter("grpView"));
               xslt.setXSL(sslLocation, "main", sessionData.runtimeData.getBrowserInfo());
               xslt.transform();
            }
            catch (PortalException pe){
               LogService.instance().log(LogService.ERROR, pe);
               if (pe.getRecordedException()!=null){
                LogService.instance().log(LogService.ERROR, pe.getRecordedException());
               }
            }
            catch (Exception e) {
               LogService.instance().log(LogService.ERROR, e);
            }
            //Utility.printDoc(viewDoc, "viewXMl ready:\n");

            Utility.logMessage("DEBUG","CGroupsManager::renderXML(): Servant services complete");
            //Utility.printDoc(viewDoc, "CGroupsManager::renderXML(): Final document state:");
         }
      } catch (Exception e) {
         LogService.instance().log(LogService.ERROR, e);
      }
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Finished with Groups Management");
      //Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): =-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_ XXXXXXXXXXXXXX _=-+_=-+_=-+_=-+_=-+_=-+_=-+_=-+_");
      long time3 = Calendar.getInstance().getTime().getTime();
      Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML() timer: "
            + String.valueOf((time3 - time1)) + " ms total, xsl took " + String.valueOf((
            time3 - time2)) + " ms for view " + runtimeData.getParameter("grpView"));
      Utility.logMessage("DEBUG", this.getClass().getName() + ".renderXML() time since setRD: "
            + String.valueOf((time3 - sessionData.startRD)));
      return;
   }

   /**
    * Passes ChannelRuntimeData to the channel.
    * This function is called prior to the renderXML() call.
    * @param rd channel runtime data
    * @see ChannelRuntimeData
    * @param uid
    */
   public void setRuntimeData (ChannelRuntimeData rd, String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::setRuntimeData(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      sessionData.runtimeData = rd;
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      sessionData.startRD = Calendar.getInstance().getTime().getTime();
      if(sessionData.servantChannel == null){
        if (hasValue(runtimeData.getParameter("grpCommand"))) {
            GroupsManagerCommandFactory cf = GroupsManagerCommandFactory.instance();
            String theCommand = runtimeData.getParameter("grpCommand");
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): COMMAND PROCESS About to get the'"
                  + theCommand + "' command");
            IGroupsManagerCommand c = cf.get(theCommand);
            Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): Got the '"
                  + theCommand + "' command = " + (c != null));
            if (c != null) {
               Utility.logMessage("DEBUG", this.getClass().getName() + "::renderXML(): setup parms and about to execute command");
               try{
                c.execute(sessionData);
               }
               catch(Exception e){
                  LogService.instance().log(LogService.ERROR,e);
                  sessionData.feedback = "Error executing command "+theCommand+": "+e.getMessage();
               }
            }
         }
         if (hasValue(runtimeData.getParameter("grpPageForward"))){
            sessionData.currentPage += Integer.parseInt(runtimeData.getParameter("grpPageForward"));
         }
         if (hasValue(runtimeData.getParameter("grpPageBack"))){
            sessionData.currentPage -= Integer.parseInt((String)runtimeData.getParameter("grpPageBack"));
         }
      }

      if (sessionData.servantChannel != null) {
         try {
            Utility.logMessage("DEBUG", this.getClass().getName() + ".setRuntimeData(): Setting Servant runtimedata");
            ((IChannel)sessionData.servantChannel).setRuntimeData(sessionData.runtimeData);
            if (sessionData.servantChannel.isFinished()) {
               sessionData.servantChannel = null;
               // flushing runtimedata for case where GroupsManager is master and servant, to prevent dirtiness
               Enumeration rd2 = runtimeData.keys();
               while (rd2.hasMoreElements()) {
                  runtimeData.remove(rd2.nextElement());
               }
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): removed servant");
            }
            else {
               Utility.logMessage("DEBUG", this.getClass().getName() +
                     ".setRuntimeData(): servant Not Finished");
            }
         } catch (Exception e) {
            Utility.logMessage("ERROR", this.getClass().getName() + ".setRuntimeDat(): Problem setting servant runtimedata "
                  + e);
         }
      }
   }

   /**
    * Passes ChannelStaticData to the channel.
    * This is done during channel instantiation time.
    * see org.jasig.portal.StaticData
    * @param sd channel static data
    * @see ChannelStaticData
    * @param uid
    */
   public void setStaticData (ChannelStaticData sd, String uid) {
      try{
         CGroupsManagerSessionData sessionData = getSessionData(uid);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): this = " + this);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): session Data = " + sessionData);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): sd = " + sd);
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): uid = " + uid);
         sessionData.staticData = sd;
         IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
         IGroupMember currUser = AuthorizationService.instance().getGroupMember(sessionData.staticData.getAuthorizationPrincipal());
         sessionData.isAdminUser = (admin.deepContains(currUser));
         sessionData.model = GroupsManagerXML.getGroupsManagerXml(sessionData);
         sessionData.user = sessionData.staticData.getPerson();
         Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData Person ID = "
               + sessionData.user.getID());
         Iterator i = sessionData.staticData.entrySet().iterator();
         while (i.hasNext()) {
            Map.Entry m = (Map.Entry)i.next();
            Utility.logMessage("DEBUG", this.getClass().getName() + "::setStaticData(): staticData "
                  + m.getKey() + " = " + m.getValue());
         }
      } catch (Exception e) {
         LogService.instance().log(LogService.ERROR, e);
      }
   }

   /**
    * put your documentation comment here
    * @return String
    */
   public String getOwnerName () {
      return  "Groups Manager";
   }

   /**
    * put your documentation comment here
    * @return String[]
    */
   public String[] getActivityTokens () {
      init();
      return  (String[])activities.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @return String
    */
   public String getOwnerToken () {
      return  OWNER;
   }

   /**
    * put your documentation comment here
    * @param token
    * @return String
    */
   public String getActivityName (String token) {
      return  (String)activities.get(token);
   }

   /**
    * put your documentation comment here
    * @return String[]
    */
   public String[] getTargetTokens () {
      init();
      return  (String[])targets.keySet().toArray(new String[0]);
   }

   /**
    * put your documentation comment here
    * @param token
    * @return String
    */
   public String getTargetName (String token) {
      String r = (String) targets.get(token);
      if (r ==null){
        try{
          r= EntityNameFinderService.instance().getNameFinder(IEntityGroup.class).getName(token);
        }
        catch (Exception e){
          LogService.instance().log(LogService.ERROR,e);
        }
      }
      return  r;
   }

   /**
    * put your documentation comment here
    * @param o
    * @return boolean
    */
   protected boolean hasValue (Object o) {
      boolean rval = false;
      if (o != null && !o.toString().trim().equals("")) {
         rval = true;
      }
      return  rval;
   }

   /**
    * put your documentation comment here
    * @param o
    * @param test
    * @return boolean
    */
   protected boolean hasValue (Object o, String test) {
      boolean rval = false;
      if (hasValue(o)) {
         if (String.valueOf(o).equals(test)) {
            rval = true;
         }
      }
      return  rval;
   }

   /**
    * put your documentation comment here
    * @param uid
    * @return ChannelCacheKey
    */
   public ChannelCacheKey generateKey (String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::generateKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      ChannelCacheKey cck;
      Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: uid parm = " + uid);
      if (sessionData.servantChannel == null) {
         cck = new ChannelCacheKey();
         cck.setKey(staticData.getChannelPublishId()
            + "-"+staticData.getChannelSubscribeId()
            + "-" + String.valueOf(staticData.getPerson().getID()));
         //   + "-" + Calendar.getInstance().getTime().getTime()));
         cck.setKeyValidity(vKey(uid));
         Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: [NO SERVANT] key = " + cck.getKey());
      }
      else {
         cck = ((ICacheable)sessionData.servantChannel).generateKey();
         Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: [SERVANT] key = " + cck.getKey());
      }
      Utility.logMessage("DEBUG", "CGroupsManager.generateKey():: ChannelCacheKey.getKeyValidity = " + cck.getKeyValidity());
      return  cck;
   }

   /**
    * put your documentation comment here
    * @param uid
    * @return String
    */
   private String vKey (String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::vKey(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      String vkey = sessionData.currentPage+" - "+sessionData.feedback+" - "+sessionData.highlightedGroupID+" - "
        +sessionData.mode+" - "+sessionData.rootViewGroupID;
      Utility.logMessage("DEBUG", this.getClass().getName() + ".vKey() : vKey returns = " + vkey);
      return  vkey;
   }

   /**
    * put your documentation comment here
    * @param validity
    * @param uid
    * @return boolean
    */
   public boolean isCacheValid (Object validity, String uid) {
      Utility.logMessage("DEBUG", this.getClass().getName() + "::isCacheValid(): this = " + this);
      CGroupsManagerSessionData sessionData = getSessionData(uid);
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      boolean valid = false;
      if (sessionData.servantChannel == null) {
         if (validity != null) {
            if (validity.equals(vKey(uid)) && runtimeData.get("grpCommand") == null) {
               valid = true;
            }
         }
         long time3 = Calendar.getInstance().getTime().getTime();
         Utility.logMessage("DEBUG", this.getClass().getName() + ".isCacheValid() time since setRD: "
               + String.valueOf((time3 - sessionData.startRD)) + ", valid=" + valid);
      }
      else {
         valid = ((ICacheable)sessionData.servantChannel).isCacheValid(validity);
      }
      return valid;
   }

   /**
    * Returns the session data for a thread
    * @param uid
    * @return CGroupsManagerSessionData
    */
   public synchronized CGroupsManagerSessionData getSessionData (String uid) {
      CGroupsManagerSessionData sd = (CGroupsManagerSessionData) sessionsMap.get(uid);
      if (sd == null) {
         sd =  new CGroupsManagerSessionData();
         sd.uid = uid;
         sd.permissible = this;
         sessionsMap.put(uid, sd);
      }
      Utility.logMessage("DEBUG", this.getClass().getName() + "::getSessionData(): sd = " + sd);
      return sd;
   }
}



