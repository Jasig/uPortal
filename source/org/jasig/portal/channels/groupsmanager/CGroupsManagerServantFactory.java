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

package org.jasig.portal.channels.groupsmanager;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Factory that produces a Groups Manager <code>IServant</code> for
 * one of several groups-management related tasks.  Each specific servant
 * is available from one of the methods documented below.
 * 
 * Groups Manager servants can relieve uPortal channels from having to 
 * implement complicated custom GUIs for managing group memberships or 
 * selecting groups and entities.
 * 
 * @see org.jasig.portal.channels.CChannelManager
 *  as an example of using Groups Manager servants
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class CGroupsManagerServantFactory implements GroupsManagerConstants{
    private static CGroupsManagerServantFactory _instance;
    private static int UID = 0;
    private HashMap servantClasses = new HashMap();
    private static CGroupsManager _groupsManager = new CGroupsManager();

    /** Creates new CGroupsManagerServantFactory */
    protected CGroupsManagerServantFactory() {
    }

    protected static CGroupsManagerServant getGroupsServant(){
      return new CGroupsManagerServant(_groupsManager, getNextUid());
    }

    /**
    * Returns a servant that is used to select IGroupMembers. The 
    * list of selected IGroupMembers is available via the getResults() method
    *
    * @return IServant
    * @param staticData
    *   the master channel's staticData
    * @param message 
    *   a custom message to present the user to explain what they should select
    * @throws PortalException
    */
    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message) throws PortalException{
      return getGroupsServantforSelection(staticData,message,null);
    }

    /**
    * Returns a servant that is used to select IGroupMembers. The 
    * list of selected IGroupMembers is available via the getResults() method
    *
    * @return IServant
    * @param staticData
    *   the master channel's staticData
    * @param message 
    *   a custom message to present the user to explain what they should select
    * @param type 
    *   the distinguished group name representing the desired root group for 
    *   selection, e.g. GroupService.EVERYONE or GroupService.ALL_CATEGORIES
    * @param allowFinish
    *   whether or not the user can "finish" selecting. if false, the servant 
    *   method "isFinished()" will always return false; the master must have
    *   some other mechanism for allowing the user to change screens.
    * @param allowEntitySelect 
    *   if false, only groups can be selected
    * @param members
    *   an IGroupMember[] of pre-selected members.
    * @throws PortalException
    */
    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message, String type, boolean allowFinish, boolean allowEntitySelect, IGroupMember[] members) throws PortalException{
      long time1 = System.currentTimeMillis();
      CGroupsManagerServant servant;

      try {
        servant = getGroupsServant();
        ChannelStaticData slaveSD =  cloneStaticData(staticData);
        Utility.logMessage("DEBUG", "CGroupsManagerFactory::getGroupsServantforSelection: slaveSD before setting servant static data: " + slaveSD);
        servant.setStaticData(slaveSD);

        servant.getSessionData().mode = "select";
        servant.getSessionData().allowFinish = allowFinish;
        if(!allowEntitySelect){
           servant.getSessionData().blockEntitySelect = true;
        }
        if (message != null){
          servant.getSessionData().customMessage = message;
        }
        String typeKey = null;
        if (type!=null && !type.equals("")){
          try{
            typeKey = GroupService.getDistinguishedGroup(type).getKey();
          }
          catch(Exception e){
            ;
          }
        }
        if (typeKey!=null){
          servant.getSessionData().rootViewGroupID = Utility.translateKeytoID(typeKey,servant.getSessionData().model);
        }
        servant.getSessionData().highlightedGroupID = servant.getSessionData().rootViewGroupID;
        if (members!=null && members.length>0){
          Document viewDoc = servant.getSessionData().model;
            Element rootElem = viewDoc.getDocumentElement();
            try{
                for (int mm = 0; mm< members.length;mm++){
                  IGroupMember mem = members[mm];
                  Element memelem = GroupsManagerXML.getGroupMemberXml(mem,false,null,viewDoc);
                  memelem.setAttribute("selected","true");
                  rootElem.appendChild(memelem);
                }
            }
            catch (Exception e){
              LogService.log(LogService.ERROR,e);
            }
        }
      }
      catch (Exception e){
          LogService.log(LogService.ERROR,e);
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
      long time2 = System.currentTimeMillis();
      Utility.logMessage("INFO", "CGroupsManagerFactory took  "
         + String.valueOf((time2 - time1)) + " ms to instantiate selection servant");
      return (IServant) servant;
    }

    /**
    * Returns a servant that is used to select IEntityGroups that the supplied 
    * GroupMember belongs to.  Existing memberships are reflected as pre-
    * selected groups.
    *
    * @return IServant
    * @param staticData
    *   the master channel's staticData
    * @param message 
    *   a custom message to present the user to explain what they should select
    * @param member
    *   The IGroupMember whose parent groups are to be selected
    * @param allowFinish
    *   whether or not the user can "finish" selecting. if false, the servant 
    *   method "isFinished()" will always return false; the master must have
    *   some other mechanism for allowing the user to change screens.
    * @throws PortalException
    */
    public static IServant getGroupsServantforGroupMemberships(ChannelStaticData staticData, String message, IGroupMember member, boolean allowFinish) throws PortalException{
      CGroupsManagerServant servant = null;
      String typeKey = null;

        try{
          typeKey = GroupService.getRootGroup(member.getType()).getKey();
        }
        catch(Exception e){
          ;
        }

      try {
        servant = getGroupsServant();
        ChannelStaticData slaveSD = cloneStaticData(staticData);
        servant.setStaticData(slaveSD);
        if (typeKey!=null){
          servant.getSessionData().rootViewGroupID = Utility.translateKeytoID(typeKey,servant.getSessionData().model);
        }
        servant.getSessionData().highlightedGroupID = servant.getSessionData().rootViewGroupID;
        servant.getSessionData().mode = "select";
        servant.getSessionData().allowFinish = allowFinish;
        servant.getSessionData().blockEntitySelect = true;
        if (message != null){
          servant.getSessionData().customMessage = message;
        }
        Document viewDoc = servant.getSessionData().model;
        Element rootElem = viewDoc.getDocumentElement();
        try{
          Iterator parents = member.getContainingGroups();
          IEntityGroup parent;
          while (parents.hasNext()){
             parent = (IEntityGroup) parents.next();
             Element parentElem = GroupsManagerXML.getGroupMemberXml(parent,false,null,viewDoc);
             parentElem.setAttribute("selected","true");
             rootElem.appendChild(parentElem);
          }
        }
        catch (Exception e){
          LogService.log(LogService.ERROR,e);
        }
      }
      catch (Exception e){
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
      return (IServant) servant;
    }

    /**
    * Returns a servant that is used to select IGroupMembers. The 
    * list of selected IGroupMembers is available via the getResults() method
    *
    * @return IServant
    * @param staticData
    *   the master channel's staticData
    * @param message 
    *   a custom message to present the user to explain what they should select
    * @param type 
    *   the distinguished group name representing the desired root group for 
    *   selection, e.g. GroupService.EVERYONE or GroupService.ALL_CATEGORIES
    * @throws PortalException
    */
    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message, String type) throws PortalException{
        return getGroupsServantforSelection(staticData, message, type, true,true);
    }
    
    /**
    * Returns a servant that is used to select IGroupMembers. The 
    * list of selected IGroupMembers is available via the getResults() method
    *
    * @return IServant
    * @param staticData
    *   the master channel's staticData
    * @param message 
    *   a custom message to present the user to explain what they should select
    * @param type 
    *   the distinguished group name representing the desired root group for 
    *   selection, e.g. GroupService.EVERYONE or GroupService.ALL_CATEGORIES
    * @param allowFinish
    *   whether or not the user can "finish" selecting. if false, the servant 
    *   method "isFinished()" will always return false; the master must have
    *   some other mechanism for allowing the user to change screens.
    * @param allowEntitySelect 
    *   if false, only groups can be selected
    * @throws PortalException
    */
    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message, String type, boolean allowFinish, boolean allowEntitySelect) throws PortalException{
      return getGroupsServantforSelection(staticData, message, type, allowFinish, allowEntitySelect, new IGroupMember[0]);
    }
    
    /**
    * Returns a servant with the group corresponding to the provided key selected
    * and locked for editing.  Only add/remove member functions are enabled - 
    * group name, description and permissions are not editable.
    *
    * @return IServant
    * @param staticData
    * @param groupKey 
    *   the group to be managed
    * @throws PortalException
    */
    public static IServant getGroupsServantforAddRemove(ChannelStaticData staticData, String groupKey) throws PortalException{
        long time1 = Calendar.getInstance().getTime().getTime();
      CGroupsManagerServant servant;
      try {
        ILockableEntityGroup lockedGroup = GroupService.findLockableGroup(groupKey,staticData.getAuthorizationPrincipal().getPrincipalString());
        lockedGroup.getClass();
        servant = getGroupsServant();
        ChannelStaticData slaveSD = cloneStaticData(staticData);

        ((IChannel)servant).setStaticData(slaveSD);
        servant.getSessionData().mode = MEMBERS_ONLY_MODE;
        servant.getSessionData().lockedGroup = lockedGroup;
        servant.getSessionData().highlightedGroupID = Utility.translateKeytoID(groupKey,servant.getSessionData().model);
        servant.getSessionData().defaultRootViewGroupID = Utility.translateKeytoID(groupKey,servant.getSessionData().model);
      }
      catch (Exception e){
        LogService.log(LogService.ERROR,e);
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
        long time2 = Calendar.getInstance().getTime().getTime();
                Utility.logMessage("INFO", "CGroupsManagerFactory took  "
                        + String.valueOf((time2 - time1)) + " ms to instantiate add/remove servant");
        return (IServant) servant;
    }

    protected static ChannelStaticData cloneStaticData(ChannelStaticData sd){
        ChannelStaticData rsd = (ChannelStaticData) sd.clone();
        Enumeration srd = rsd.keys();
        while (srd.hasMoreElements()) {
            rsd.remove(srd.nextElement());
        }
        return rsd;
    }

    protected static synchronized CGroupsManagerServantFactory instance(){
        if(_instance==null){
            _instance = new CGroupsManagerServantFactory();
        }
        return _instance;
    }

   /**
    * Returns the next sequential identifier which is used to uniquely
    * identify an element. This identifier is held in the Element "id" attribute.
    * "0" is reserved for the Group containing the Initial Contexts for the user.
    * @return String
    */
   public static synchronized String getNextUid () {
      // max size of int = (2 to the 32 minus 1) = 2147483647
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNextUid(): Start");
      if (UID > 2147483600) {
         // the value 0 is reserved for the group holding the initial group contexts
         UID = 0;
      }
      String newUid = Calendar.getInstance().getTime().getTime() + "grpsservant" + ++UID;
      return  newUid;
   }


}
