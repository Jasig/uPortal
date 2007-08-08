/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import java.util.Set;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;

/**
 * An aggregated-layout specific extension of the user layout interface
 *
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1 $Revision$ $Date$
 */
public interface IAggregatedLayout extends IUserLayout {

    // the tag names constants
    public static final String LAYOUT = "layout";
    public static final String FRAGMENT = "fragment";
    public static final String FOLDER = "folder";
    public static final String CHANNEL = "channel";
    public static final String PARAMETER = "parameter";
    public static final String RESTRICTION = "restriction";
    // The names for marking nodes
    public static final String ADD_TARGET = "add_target";
    public static final String MOVE_TARGET = "move_target";


    /**
     * Returns a list of fragment Ids existing in the layout.
     *
     * @return a <code>Set</code> of <code>String</code> fragment Ids.
     * @exception PortalException if an error occurs
     */
    public Set getFragmentIds() throws PortalException;

    /**
     * Returns an fragment Id for a given node.
     * Returns null if the node is not part of any fragments.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>String</code> fragment Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentId(String nodeId) throws PortalException;

    /**
     * Returns an fragment root Id for a given fragment.
     *
     * @param fragmentId a <code>String</code> value
     * @return a <code>String</code> fragment root Id
     * @exception PortalException if an error occurs
     */
    public String getFragmentRootId(String fragmentId) throws PortalException;
    
    /**
     * Returns the node by a given node ID.
     *
     * @param nodeId a <code>String</code> value
     * @return a <code>ALNode</code> instance
     */
    public ALNode getLayoutNode(String nodeId);
    
    /**
     * Returns the folder by a given folder ID.
     *
     * @param folderId a <code>String</code> value
     * @return a <code>ALFolder</code> instance
     */
    public ALFolder getLayoutFolder(String folderId);
    
    /**
     * Gets the tree depth for a given node
     * @param nodeId a <code>String</code> node ID
     * @return a depth value
     * @exception PortalException if an error occurs
     */
    public int getDepth(String nodeId) throws PortalException;
   
}
