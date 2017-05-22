/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.groups;

import java.util.Iterator;
import org.apereo.portal.EntityIdentifier;

/**
 * Defines an api for discovering an entry point into the groups system, represented by an <code>
 * IGroupMember</code>. This is analogous to getting an <code>InitialContext</code> in JNDI.
 * Subsequent requests for navigating or maintaining groups go thru the <code>IGroupMember</code>.
 *
 * @deprecated Use instead {@link ICompositeGroupService} or {@link IIndividualGroupService}
 */
@Deprecated
public interface IGroupService {

    /**
     * Returns a pre-existing <code>IEntityGroup</code> or null if the <code>IGroupMember</code>
     * does not exist.
     */
    IEntityGroup findGroup(String key) throws GroupsException;

    /**
     * Returns an <code>IEntity</code> representing a portal entity. This does not guarantee that
     * the entity actually exists.
     */
    IEntity getEntity(String key, Class type) throws GroupsException;

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a portal entity. If the
     * parm <code>type</code> is the group type, the <code>IGroupMember</code> is an <code>
     * IEntityGroup</code> else it is an <code>IEntity</code>.
     */
    IGroupMember getGroupMember(String key, Class type) throws GroupsException;

    /**
     * Returns an <code>IGroupMember</code> representing either a group or a portal entity, based on
     * the <code>EntityIdentifier</code>, which refers to the UNDERLYING entity for the <code>
     * IGroupMember</code>.
     */
    IGroupMember getGroupMember(EntityIdentifier underlyingEntityIdentifier)
            throws GroupsException;

    /** Returns an <code>IEntityGroupStore</code>. */
    IEntityGroupStore getGroupStore() throws GroupsException;

    /** Removes the <code>IEntityGroup</code> from the store. */
    void deleteGroup(IEntityGroup group) throws GroupsException;

    /**
     * Returns the member groups for the <code>IEntityGroup</code>
     *
     * @param eg IEntityGroup
     */
    Iterator findMemberGroups(IEntityGroup eg) throws GroupsException;

    /**
     * Find EntityIdentifiers for groups whose name matches the query string according to the
     * specified method and matches the provided leaf type
     */
    EntityIdentifier[] searchForGroups(String query, int method, Class leaftype)
            throws GroupsException;

    /**
     * Find EntityIdentifiers for entities whose name matches the query string according to the
     * specified method and is of the specified type
     */
    EntityIdentifier[] searchForEntities(String query, int method, Class type)
            throws GroupsException;

}
