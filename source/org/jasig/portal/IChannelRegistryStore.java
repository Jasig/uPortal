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

package org.jasig.portal;

import java.util.Date;

import org.jasig.portal.security.IPerson;

/**
 * Interface defining how the portal reads and writes its channel types,
 * definitions, and categories.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public interface IChannelRegistryStore {

  /**
   * Creates a new channel type.
   * @return channelType, the new channel type
   * @throws java.lang.Exception
   */
  public ChannelType newChannelType() throws Exception;

  /**
   * Get the channel type associated with a particular identifier.
   * @param channelTypeId, the channel type identifier
   * @return channelType, the channel type
   * @throws java.lang.Exception
   */
  public ChannelType getChannelType(int channelTypeId) throws Exception;

  /**
   * Returns an array of ChannelTypes.
   * @return channelTypes, the list of publishable channel types
   * @throws java.lang.Exception
   */
  public ChannelType[] getChannelTypes() throws Exception;

  /**
   * Persists a channel type.
   * @param chanType a channel type
   * @throws java.lang.Exception
   */
  public void saveChannelType(ChannelType chanType) throws Exception;

  /**
   * Deletes a channel type.  The deletion will only succeed if no existing
   * channels reference the channel type.
   * @param chanType a channel type
   * @throws java.lang.Exception
   */
  public void deleteChannelType(ChannelType chanType) throws Exception;

  /**
   * Create a new ChannelDefinition object.
   * @return channelDefinition, the new channel definition
   * @throws java.lang.Exception
   */
  public ChannelDefinition newChannelDefinition() throws Exception;

  /**
   * Get a channel definition.
   * @param channelPublishId a channel publish ID
   * @return channelDefinition, a definition of the channel or <code>null</code>
   *   if no matching channel definition can be found
   * @throws java.lang.Exception
   */
  public ChannelDefinition getChannelDefinition(int channelPublishId) throws Exception;

  /**
   * Get a channel definition.  If there is more than one channel definition
   * with the given functional name, then the first one will be returned.
   * @param channelFunctionalName a channel functional name
   * @return channelDefinition, a definition of the channel or <code>null</code>
   *   if no matching channel definition can be found
   * @throws java.lang.Exception
   */
  public ChannelDefinition getChannelDefinition(String channelFunctionalName) throws Exception;

  /**
   * Get all channel definitions including ones that haven't been approved.
   * @return channelDefs, the channel definitions
   * @throws java.lang.Exception
   */
  public ChannelDefinition[] getChannelDefinitions() throws Exception;

  /**
   * Persists a channel definition.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void saveChannelDefinition(ChannelDefinition channelDef) throws Exception;

  /**
   * Permanently deletes a channel definition from the store.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void deleteChannelDefinition(ChannelDefinition channelDef) throws Exception;

  /**
   * Sets a channel definition as "approved".  This effectively makes a
   * channel definition available in the channel registry, making the channel
   * available for subscription.
   * @param channelDef the channel definition
   * @param approver the user that approves this channel definition
   * @param approveDate the date when the channel definition should be approved (can be future dated)
   * @throws java.lang.Exception
   */
  public void approveChannelDefinition(ChannelDefinition channelDef, IPerson approver, Date approveDate) throws Exception;


  /**
   * Sets a channel definition as "unapproved".  This effectively removes a
   * channel definition from the channel registry, making the channel
   * unavailable for subscription.
   * @param channelDef the channel definition
   * @throws java.lang.Exception
   */
  public void disapproveChannelDefinition(ChannelDefinition channelDef) throws Exception;

  /**
   * Creates a new channel category.
   * @param category, the channel category to create
   * @return channelCategory the new channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory newChannelCategory() throws Exception;

  /**
   * Gets an existing channel category.
   * @param channelCategoryId the id of the category to get
   * @return channelCategory the channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory getChannelCategory(String channelCategoryId) throws Exception;

  /**
   * Gets top level channel category
   * @return channelCategories the new channel category
   * @throws java.lang.Exception
   */
  public ChannelCategory getTopLevelChannelCategory() throws Exception;

  /**
   * Gets all child channel categories for a parent category.
   * @return channelCategories the children categories
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getChildCategories(ChannelCategory parent) throws Exception;

  /**
   * Gets all child channel definitions for a parent category.
   * @return channelDefinitions the children channel definitions
   * @throws java.lang.Exception
   */
  public ChannelDefinition[] getChildChannels(ChannelCategory parent) throws Exception;

  /**
   * Gets the immediate parent categories of this category.
   * @return parents, the parent categories.
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getParentCategories(ChannelCategory child) throws Exception;

  /**
   * Gets the immediate parent categories of this channel definition.
   * @return parents, the parent categories.
   * @throws java.lang.Exception
   */
  public ChannelCategory[] getParentCategories(ChannelDefinition child) throws Exception;

  /**
   * Persists a channel category.
   * @param channelCategory, the channel category to persist
   * @throws java.lang.Exception
   */
  public void saveChannelCategory(ChannelCategory category) throws Exception;

  /**
   * Deletes a channel category.
   * @param channelCategory, the channel category to delete
   * @throws java.lang.Exception
   */
  public void deleteChannelCategory(ChannelCategory category) throws Exception;

  /**
   * Makes one category a child of another.
   * @param child, the source category
   * @param parent, the destination category
   * @throws java.lang.Exception
   */
  public void addCategoryToCategory(ChannelCategory source, ChannelCategory destination) throws Exception;

  /**
   * Makes one category a child of another.
   * @param child, the category to remove
   * @param parent, the category to remove from
   * @throws java.lang.Exception
   */
  public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent) throws Exception;

  /**
   * Associates a channel definition with a category.
   * @param channelDef, the channel definition
   * @param category, the channel category to which to associate the channel definition
   * @throws java.lang.Exception
   */
  public void addChannelToCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception;

  /**
   * Disassociates a channel definition from a category.
   * @param channelDef, the channel definition
   * @param category, the channel category from which to disassociate the channel definition
   * @throws java.lang.Exception
   */
  public void removeChannelFromCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception;

}







