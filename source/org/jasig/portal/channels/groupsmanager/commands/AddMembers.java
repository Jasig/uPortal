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

package  org.jasig.portal.channels.groupsmanager.commands;

import  java.util.*;
import  org.jasig.portal.*;
import  org.jasig.portal.channels.groupsmanager.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.services.*;
import  org.w3c.dom.Element;
import  org.w3c.dom.Document;

/** This command sets the id of the parent group (ie. the group to which child
 *  members will be added). Control is then passed to a selection view where
 *  the child group members will be selected for addition. When the selection
 *  has been completed by the user, the DoneWithSelection command will be
 *  invoked where the selected children group members are actually processed.
 *  Alternatively, the CancelSelection command is invoked to cancel the
 *  selection process and reset the mode and view control parameters.
 * @author Don Fracapane
 * @version $Revision$
 */
public class AddMembers extends GroupsManagerCommand {

   /**
    * put your documentation comment here
    */
   public AddMembers () {
   }

   /**
    * This is the public method
    * @throws Exception
    * @param sessionData
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;
      Document model = getXmlDoc(sessionData);
      Utility.logMessage("DEBUG", "AddMembers::execute(): Start");
      String parentAddElemId = getCommandArg(runtimeData);

      IGroupMember pg = GroupsManagerXML.retrieveGroupMemberForElementId(model, parentAddElemId);
      sessionData.rootViewGroupID = Utility.translateKeytoID(GroupService.getRootGroup(pg.getLeafType()).getKey(),getXmlDoc(sessionData));
      // Parent was locked so no other thread or process could have changed it, but
      // child members could have changed.
      Element parentElem = GroupsManagerXML.getElementById(model, parentAddElemId);
      GroupsManagerXML.refreshAllNodesRecursivelyIfRequired(model, parentElem);
      sessionData.returnToMode = sessionData.mode;
      sessionData.mode=SELECT_MODE;
      sessionData.highlightedGroupID = sessionData.rootViewGroupID;
      Utility.logMessage("DEBUG", "AddMembers::execute(): Uid of parent element = " +
            parentAddElemId);
      staticData.setParameter("groupParentId", parentAddElemId);
   }
}



