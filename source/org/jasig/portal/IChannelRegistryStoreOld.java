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

import org.jasig.portal.security.IPerson;
import org.w3c.dom.*;
import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface defining how the portal retrieves it's channels and categories.
 * Methods are also provided to allow for publishing and unpublishing content.
 * The intent is that this task can be performed based on channel, category, and role.
 * @author John Laker
 * @version $Revision$
 * @deprecated Use {@link IChannelRegistryStore} instead
 */
public interface IChannelRegistryStoreOld {

  /**
   * Get next available channel ID
   * @return channel id
   * @exception PortalException
   */
  public int getNextId () throws PortalException;

  /**
   * Gets the channel registry as an XML document
   * @return the channel registry XML
   * @throws java.lang.Exception
   */
  public Document getChannelRegistryXML () throws Exception;

  /**
   * put your documentation comment here
   * @param registryXML
   */
  public void setRegistryXML (String registryXML) throws Exception;

  /**
   * Returns a string of XML which describes the channel types.
   * @return channelTypes, the list of publishable channel types
   * @throws java.lang.Exception
   */
  public Document getChannelTypesXML () throws Exception;

  /**
   * Removes a channel from the channel registry.
   * @param chanID, the ID of the channel to remove.
   * @exception Exception
   */
  public void removeChannel (String chanID) throws Exception;

  /**
   * Publishes a channel.
   * @param id the identifier for the channel
   * @param publisher the user who is publishing this channel
   * @param chanXML XML that describes the channel
   * @param catID an array of category IDs
   * @exception Exception
   */
  public void addChannel (int id, IPerson publisher, Document chanXML, String catID[]) throws Exception;

  /**
   * Approves a channel.
   * @param chanId
   * @param approver
   * @param approveDate
   * @exception Exception
   */
  public void approveChannel(int chanId, IPerson approver, Date approveDate) throws Exception;

  /**
   * Get a prepared statement for Channels
   */
  public RDBMServices.PreparedStatement getChannelPstmt(Connection con) throws SQLException;

  /**
   * Get a prepared statement for channel parameters (if needed)
   */
  public RDBMServices.PreparedStatement getChannelParmPstmt(Connection con) throws SQLException;

  /**
   * Get a channel definition
   */
  public ChannelDefinition getChannel(int channelPublishId);

  /**
   * Invalidate a channel definition
   */
  public void flushChannelEntry(int chanId);

  /**
   * Get a channel definition (from the store if necessary)
   */
  public ChannelDefinition getChannel(int channelPublishId, boolean cacheChannel, RDBMServices.PreparedStatement pstmtChannel, RDBMServices.PreparedStatement pstmtChannelParm) throws java.sql.SQLException;

  /**
   * Get an XML representation of a channel
   */
  public Element getChannelXML(int chanId, Document doc, String idTag);
}







