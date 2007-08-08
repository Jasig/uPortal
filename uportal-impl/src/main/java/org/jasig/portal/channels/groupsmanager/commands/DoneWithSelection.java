/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Iterator;
import java.util.Vector;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** A select cycle could be started in Servant mode or it could be started by
 *  the AddMembers command. The AddMembers command sets the id of the parent
 *  group (ie. the group to which child  members will be added). Control is then
 *  passed to a selection view where the child group members will be selected
 *  for addition. When the selection has been completed by the user, the
 *  DoneWithSelection command will be invoked where the selected members are
 *  added to a collection of IAuthorizationPrincipal. If a parent group had
 *  been set the children groupmembers are actually added to the parent group.
 *  If in Servant mode, the collection is simply returned to the master channel.
 *  Alternatively, the CancelSelection command have been invoked by the user to
 *  cancel the selection process and reset the mode and view control parameters.
 * @author Don Fracapane
 * @version $Revision$
 */
public class DoneWithSelection extends GroupsManagerCommand {

   /** Creates new AddMember */
   public DoneWithSelection () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelStaticData staticData = sessionData.staticData;
      //ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Start");
      String parentId = null;
      boolean hasParentId = hasParentId(staticData);
      Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Parent ID is set: "
            + hasParentId);
      IGroupMember[] princResults = null;
      Document model = getXmlDoc(sessionData);
      Element rootElem = model.getDocumentElement();
      NodeList nGroupList = rootElem.getElementsByTagName(GROUP_TAGNAME);
      NodeList nEntityList = rootElem.getElementsByTagName(ENTITY_TAGNAME);
      Vector gmCollection = new Vector();
      Element parentElem = null;
      Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Starting group process");
      addGroupMemberToCollection(gmCollection, nGroupList);
      Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Starting entity process");
      addGroupMemberToCollection(gmCollection, nEntityList);
      // check if selections were made
      if (gmCollection.size() <1) {
         sessionData.feedback = sessionData.feedback + "\n No groups or people were selected! ";
         return;
      }

      /** Presence of parentID means the process is not in servant mode. That is,
       * the master channel is the Groups Manager channel and AddMembers had
       * been selected
       */
      if (hasParentId) {
         parentId = getParentId(staticData);
         Utility.logMessage("DEBUG", "DoneWithSelection::execute(): Parent ID is set to: "
               + parentId);
         parentElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, parentId);
         if (parentElem == null) {
            Utility.logMessage("ERROR", "DoneWithSelection::execute: Error parent element not found");
            return;
         }
         addChildrenToGroup(gmCollection, sessionData, parentElem, model);
         clearSelected(sessionData);
         sessionData.mode = sessionData.returnToMode;;
         sessionData.highlightedGroupID = parentId;
         sessionData.rootViewGroupID=null;
         // Parent was locked so no other thread or process could have changed it, but
         // child members could have changed.
         GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(sessionData.getUnrestrictedData(), parentElem);
         sessionData.staticData.remove("groupParentId");
      }
      else {
         princResults = (IGroupMember[])gmCollection.toArray(new IGroupMember[0]);
         if (princResults.length > 0) {
            staticData.put("princResults", princResults);
            staticData.setParameter("groupManagerFinished", "true");
         }
      }
   }

   /**
    * This method processes the xml document looking for selected groupmembers.
    * It then creates an instance of IGroupMember for each selected
    * member and passes the collection back.
    * @param gmCollection
    * @param nList
    * @throws Exception
    */
   private void addGroupMemberToCollection (Vector gmCollection, NodeList nList)
         throws Exception {
      boolean addit;
      for (int i = 0; i < nList.getLength(); i++) {
         Element elem = (org.w3c.dom.Element)nList.item(i);
         if (Utility.areEqual(elem.getAttribute("selected"), "true")) {
            addit = true;
            Iterator gmItr = gmCollection.iterator();
            while (gmItr.hasNext()) {
               IGroupMember ggmm = (IGroupMember)gmItr.next();
               if ((ggmm.getKey().equals(elem.getAttribute("key")))
                        && (ggmm.getType().equals(elem.getAttribute("type")))){
                       addit = false;
                       break;
               }
            }
            if (addit) {
               IGroupMember gm = Utility.retrieveGroupMemberForKeyAndType(elem.getAttribute("key"),elem.getAttribute("type"));
               gmCollection.add(gm);
               Utility.logMessage("DEBUG", "DoneWithSelection::addGroupMemberToCollection(): " +
                     "- adding group member" + elem.getAttribute("key"));
            }
         }
      }
   }

   /**
    * This section adds the selected members to an IEntityGroup.
    * @param gmCollection
    * @param sessionData
    * @param parentElem
    * @param model
    * @throws Exception
    */
   private void addChildrenToGroup (Vector gmCollection, CGroupsManagerSessionData sessionData,
      Element parentElem, Document model) throws Exception {
      //ChannelRuntimeData runtimeData = sessionData.runtimeData;
      Element parent;
      ILockableEntityGroup parentGroup = null;
      IGroupMember childGm = null;
      Element childElem;
      parentGroup = sessionData.lockedGroup;
      Iterator gmItr = gmCollection.iterator();
      while (gmItr.hasNext()) {
         childGm = (IGroupMember) gmItr.next();
         Utility.logMessage("DEBUG", "DoneWithSelection::execute: About to add child");
         // add to parent group
         parentGroup.addMember(childGm);
         // remove property elements for child gm and clear the Entity Properties cache.
         GroupsManagerXML.removePropertyElements (model, childGm, true);
         // update parent group
         parentGroup.updateMembersAndRenewLock();
         // get parent element(s) and add element for child group member
         Iterator parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME,
               parentElem.getAttribute("key"));
         while (parentNodes.hasNext()) {
            parent = (Element)parentNodes.next();

            childElem = GroupsManagerXML.getGroupMemberXml(childGm, false, null, sessionData.getUnrestrictedData());
            parent.appendChild(childElem);
            parent.setAttribute("hasMembers", "true");
         }
      }
   }
}
