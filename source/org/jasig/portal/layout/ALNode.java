/**
 * Copyright � 2002 The JA-SIG Collaborative.  All rights reserved.
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


package org.jasig.portal.layout;



import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.w3c.dom.Element;


/**
 * IALNode summary sentence goes here.
 * <p>
 * Company: Instructional Media &amp; Magic
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */

public abstract class ALNode {

     protected String parentNodeId;
     protected String nextNodeId;
     protected String previousNodeId;
     protected IALNodeDescription nodeDescription;
     protected int priority = 0;

     public ALNode() {}

     public ALNode ( IALNodeDescription nd ) {
       nodeDescription = nd;
     }

     public String getId() {
        return nodeDescription.getId();
     }

     public String getFragmentId() {
        return nodeDescription.getFragmentId();
     }

     public String getFragmentNodeId() {
        return nodeDescription.getFragmentNodeId();
     }

     /**
     * Gets the node type
     * @return a node type
     */
     public abstract int getNodeType();

     public void setNodeDescription ( IALNodeDescription nd ) {
       nodeDescription = nd;
     }

     public IALNodeDescription getNodeDescription() {
       return nodeDescription;
     }

     public void setParentNodeId ( String parentNodeId ) {
      this.parentNodeId = parentNodeId;
     }

     public String getParentNodeId() {
       return parentNodeId;
     }

     public void setNextNodeId ( String nextNodeId ) {
      this.nextNodeId = nextNodeId;
     }

     public String getNextNodeId() {
       return nextNodeId;
     }

     public void setPreviousNodeId ( String previousNodeId ) {
      this.previousNodeId = previousNodeId;
     }

     public String getPreviousNodeId() {
       return previousNodeId;
     }

    /**
     * Sets the priority for this node.
     * @param priority a <code>int</code> priority value
     */
     public void setPriority ( int priority ) {
       this.priority = priority;
     }

     /**
     * Gets the priority value for this node.
     */
     public int getPriority() {
       return priority;
     }

     /**
     * Gets a restriction by the type.
     * @param restrictionName a <code>String</code>  name of the restriction
     * @return a IUserLayoutRestriction
     */
     public IUserLayoutRestriction getRestriction( String restrictionName ) {
      if ( nodeDescription != null )
       return nodeDescription.getRestriction(restrictionName);
       return null;
     }

     /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>String</code> restriction path
     * @return a IUserLayoutRestriction
     */
     public Vector getRestrictionsByPath( String restrictionPath ) {
      if ( nodeDescription != null )
        return nodeDescription.getRestrictionsByPath(restrictionPath);
        return new Vector();
     }

     /**
     * Add all of common node attributes to the <code>Element</code>.
     * @param node an <code>Element</code> value
     */
    protected void addNodeAttributes(Element node) {
        nodeDescription.addNodeAttributes(node);
        node.setAttribute("priority",priority+"");
    }


    /**
     * A factory method to create a <code>IALNodeDescription</code> instance,
     * based on the information provided in the user layout <code>Element</code>.
     *
     * @param xmlNode a user layout DTD folder/channel <code>Element</code> value
     * @return an <code>IALNodeDescription</code> value
     * @exception PortalException if the xml passed is somehow invalid.
     */
    public static IALNodeDescription createUserLayoutNodeDescription(Element xmlNode) throws PortalException {
        // is this a folder or a channel ?
        String nodeName=xmlNode.getNodeName();
        if(nodeName.equals("channel")) {
            return new ALChannelDescription(xmlNode);
        } else if(nodeName.equals("folder")) {
            return new ALFolderDescription(xmlNode);
        } else {
            throw new PortalException("Given XML element is neither folder nor channel");
        }
    }

    public static ALNode createALNode(IUserLayoutNodeDescription nodeDescription) throws PortalException {
        if(nodeDescription instanceof IALFolderDescription) {
            // should be a folder
            return new ALFolder(new ALFolderDescription((IALFolderDescription)nodeDescription));
        } else if(nodeDescription instanceof IALChannelDescription) {
            return new ALChannel(new ALChannelDescription((IALChannelDescription)nodeDescription));
        } else {
            throw new PortalException("ALNode::createALNode() : The node description supplied is neither a folder nor a channel! Can't make the ALNode");
        }
    }
    
    public String toString() {
  	  return "{"+parentNodeId+","+nextNodeId+","+previousNodeId
  	  + ","+nodeDescription+ ","+priority+"}";
  	}

  }
