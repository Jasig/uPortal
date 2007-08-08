/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This command removes the association of a member element to an IEntityGroup
 * It then gathers all of the xml nodes for the parent group and removes the child
 * node of the removed member.
 * @author Don Fracapane
 * @version $Revision$
 */
public class RemoveMember extends GroupsManagerCommand {

   /** Creates new RemoveMember */
   public RemoveMember () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception {
      //ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData = sessionData.runtimeData;
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String cmdIds = getCommandArg(runtimeData);
      Object parentGroup = null;
      IGroupMember childGm = null;
      String hasMbrs = "duh";
      Utility.logMessage("DEBUG", "RemoveMember::execute(): About to get parent and child keys");
      String parentID = Utility.parseStringDelimitedBy("parent.", cmdIds, "|");
      String childID = Utility.parseStringDelimitedBy("child.", cmdIds, "|");
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Uid of parent element = "
            + parentID + " child element = " + childID);
      Element parentElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME,
            parentID);
      if (parentElem == null) {
         Utility.logMessage("ERROR", "RemoveMember::execute(): Unable to retrieve parent element!");
         return;
      }
      Utility.logMessage("DEBUG", "RemoveMember::execute(): About to get child element = "
            + childID);
      Element childElem = GroupsManagerXML.getElementById(model, childID);
      if (childElem == null) {
         Utility.logMessage("ERROR", "RemoveMember::execute(): Unable to retrieve Child element!");
         return;
      }
      // The child will always be an IGroupMember
      childGm = GroupsManagerXML.retrieveGroupMemberForElementId(model, childID);
      parentGroup = sessionData.lockedGroup;
      removeChildFromGroup(parentGroup, childGm);
      hasMbrs = String.valueOf(((IEntityGroup)parentGroup).hasMembers());
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Got the parent group ");
      Utility.logMessage("DEBUG", "RemoveMember::execute(): about to remove child elements");
      // remove property elements for child gm and clear the Entity Properties cache.
      GroupsManagerXML.removePropertyElements (model, childGm, true);

      // Removes EntityProperites for the child GroupMember from the Entity Property cache.
      GroupsManagerXML.clearPropertiesCache (childGm);
      
      // remove this member from all parent group elements.
      Iterator parentNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME,
            parentElem.getAttribute("key"));
      Node parentNode;
      NodeList childNodes;
      Node childNode;
      while (parentNodes.hasNext()) {
         parentNode = (Node)parentNodes.next();
         childNodes = parentNode.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
            childNode = childNodes.item(i);
            if (((Element)childNode).getAttribute("key").equals(childElem.getAttribute("key"))) {
               parentNode.removeChild(childNode);
               ((Element)parentNode).setAttribute("hasMembers", hasMbrs);
            }
         }
      }
      Utility.logMessage("DEBUG", "RemoveMember::execute(): Completed");
   }

   /**
    * This section removes the selected member from an IEntityGroup.
    * @param parentGroup
    * @param childGm
    * @exception Exception
    */
   private void removeChildFromGroup (Object parentGroup, IGroupMember childGm) throws Exception {
      Utility.logMessage("DEBUG", "RemoveMember::removeChildrenFromGroup(): about to remove child");
      if (parentGroup != null && childGm != null) {
         try {
            ((IEntityGroup)parentGroup).removeMember(childGm);
            ((ILockableEntityGroup)parentGroup).updateMembersAndRenewLock();
         } catch (GroupsException ge) {
            String aMsg = "Unable to remove child from parent/n" + ge;
            Utility.logMessage("ERROR", aMsg, ge);
         }
      }
      else {
         String suspect = (parentGroup==null ? "Parent" : "Child");
         throw  new Exception(suspect + " group member was not found");
      }
   }
}
