/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import java.util.Collection;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.IUserLayoutManager;

/**
 * An aggregated-layout specific extension of the user layout manager interface.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version 1.1 $Revision$ $Date$
 */
public interface IAggregatedUserLayoutManager extends IUserLayoutManager {

    public static final String NEW_FRAGMENT = "new_fragment";

    // The priority coefficient for changing priority values through an user interface
    public static final int PRIORITY_COEFF = 100;


    /**
     * Sets a layout manager to auto-commit mode that allows to update the database immediately
     * @param autoCommit a boolean value
     */
    public void setAutoCommit (boolean  autoCommit);

    /**
     * Saves the current fragment if the layout is a fragment
     * @exception PortalException if an error occurs
     */
    public void saveFragment() throws PortalException;
    
    /**
     * Saves the current fragment if the layout is a fragment
     * @exception PortalException if an error occurs
     */
    public void saveFragment(UserPreferences userPrefs) throws PortalException;

	/**
		 * Deletes the current fragment if the layout is a fragment
		 * @exception PortalException if an error occurs
		 */
	public void deleteFragment() throws PortalException;
	
     /**
     * Loads the fragment as an user layout given by fragmentId
     * @param fragmentId a fragment ID
     * @exception PortalException if an error occurs
     */
    public void loadFragment( String fragmentId ) throws PortalException;
    
	/**
	* Saves the fragment in the store
	* @param fragment a <code>ILayoutFragment</code> instance
	* @exception PortalException if an error occurs
	*/
    public void saveFragment ( ILayoutFragment fragment ) throws PortalException;

	/**
		* Saves the fragment in the store
		* @param fragment a <code>ILayoutFragment</code> instance
		* @exception PortalException if an error occurs
		*/
	public void saveFragment ( ILayoutFragment fragment, UserPreferences userPrefs ) throws PortalException;
   
	
	/**
		 * Removes the fragment
		 * @param fragmentId a fragment ID
		 * @exception PortalException if an error occurs
		 */
	public void deleteFragment ( String fragmentId ) throws PortalException;
	
	/**
			 * Returns the fragment Ids of the owner associated with the current layout
			 * @return <code>Collection</code> a set of the fragment IDs
			 * @exception PortalException if an error occurs
			 */
	public Collection getFragments() throws PortalException;
	
	/**
				 * Returns the list of Ids of the fragments that the user can subscribe to
				 * @return <code>Collection</code> a set of the fragment IDs
				 * @exception PortalException if an error occurs
				 */
	public Collection getSubscribableFragments() throws PortalException;

	
	/**
				 * Returns the fragment
				 * @param fragmentId a fragment ID
				 * @return <code>ILayoutFragment</code> a fragment
				 * @exception PortalException if an error occurs
				 */
	public ILayoutFragment getFragment ( String fragmentId ) throws PortalException;
	
	/**
		* Returns true if any fragment is currently loaded into the layout manager, 
		* false - otherwise
		* @return a boolean value
		* @exception PortalException if an error occurs
		*/
	public boolean isFragmentLoaded() throws PortalException;

    /**
     * Returns the description of the node currently being added to the layout
     *
     * @return node an <code>IALNodeDescription</code> object
     * @exception PortalException if an error occurs
     */
    public IALNodeDescription getNodeBeingAdded() throws PortalException;


    /**
     * Returns the description of the node currently being moved in the layout
     *
     * @return node an <code>IALNodeDescription</code> object
     * @exception PortalException if an error occurs
     */
    public IALNodeDescription getNodeBeingMoved() throws PortalException;
    
	/**
	  * Creates a new fragment and loads it as an user layout
	  * @param fragmentName a fragment name
	  * @param fragmentDesc a fragment description
	  * @param fragmentRootName a fragment root node name
	  * @return a new generated fragment ID
	  * @exception PortalException if an error occurs
	  */
	public String createFragment( String fragmentName, String fragmentDesc, String fragmentRootName ) throws PortalException;
	
	/**
			* Returns the user group keys which the fragment is published to
			* @param fragmentId a <code>String</code> value
			* @return a <code>Colection</code> object containing the group keys
			* @exception PortalException if an error occurs
			*/
	public Collection getPublishGroups (String fragmentId ) throws PortalException;
	
	/**
	  * Persists the user groups which the fragment is published to
	  * @param groups an array of <code>IGroupMember</code> objects
	  * @param fragmentId a <code>String</code> value
	  * @exception PortalException if an error occurs
	  */
	public void setPublishGroups ( IGroupMember[] groups, String fragmentId ) throws PortalException;
	
    /**
     * Signal manager to persist user layout to a database
     * @param userPrefs <code>UserPreferences</code> that are needed for fragment param saving.
     * @exception PortalException if an error occurs
     */
	public void saveUserLayout(UserPreferences userPrefs) throws PortalException;


}
