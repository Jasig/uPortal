/* Copyright � 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups;

import org.jasig.portal.*;
import org.jasig.portal.security.*;
import org.jasig.portal.services.GroupService;
import java.util.*;

/**
 * @author Dan Ellentuck
 * @version $Revision$
 * @see IGroupMember
 * @see IGroupMemberFactory
 */
public abstract class GroupMemberImpl implements IGroupMember
{
/*
 * The <code>EntityIdentifier</code> that uniquely identifies the entity,
 * e.g., the <code>IPerson</code>, <code>ChannelDefinition</code>, etc.,
 * that underlies the <code>IGroupMember</code>.
 */
    private EntityIdentifier underlyingEntityIdentifier;
    private static java.lang.Class defaultEntityType;

    // Our home service.
    protected IGroupService groupService;

/*
 * The Set of keys to groups that contain this <code>IGroupMember</code>.
 * the groups themselves are cached by the service.
 */
    private Set groupKeys;
    private boolean groupKeysInitialized;
/**
 * GroupMemberImpl constructor
 */
public GroupMemberImpl(String key, Class type) throws GroupsException
{
    this(new EntityIdentifier(key, type));
}
/**
 * GroupMemberImpl constructor
 */
public GroupMemberImpl(EntityIdentifier newEntityIdentifier) throws GroupsException
{
    super();
    if ( isKnownEntityType(newEntityIdentifier.getType()) )
        { underlyingEntityIdentifier = newEntityIdentifier; }
    else
        { throw new GroupsException("Unknown entity type: " + newEntityIdentifier.getType()); }
}
/**
 * Adds the key of the <code>IEntityGroup</code> to our groups <code>Collection</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public synchronized void addGroup(IEntityGroup eg)
{
    getGroupKeys().add(eg.getEntityIdentifier().getKey());
}
/**
 * @return boolean
 */
private boolean areGroupKeysInitialized() {
    return groupKeysInitialized;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IGroupMember gm) throws GroupsException
{
    return false;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean deepContains(IGroupMember gm) throws GroupsException
{
    return false;
}
/**
 * Returns an <code>Iterator</code> over the <code>Set</code> of this
 * <code>IGroupMember's</code> recursively-retrieved parent groups.
 *
 * @return java.util.Iterator
 */
public java.util.Iterator getAllContainingGroups() throws GroupsException
{
    return primGetAllContainingGroups(new HashSet()).iterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getAllMembers() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return java.lang.String
 */
protected String getCacheKey() {
    return getEntityIdentifier().getKey();
//  return getKey() + new Boolean(isGroup()).hashCode();
}
/**
 * Returns the composite group service.
 */
protected ICompositeGroupService getCompositeGroupService() throws GroupsException
{
    return GroupService.getCompositeGroupService();
}
/**
 * Returns an <code>Iterator</code> over this <code>IGroupMember's</code> parent groups.
 * Synchronize the collection of keys with adds and removes.
 * @return java.util.Iterator
 */
public java.util.Iterator getContainingGroups() throws GroupsException
{
    Iterator i;
    Collection groupsColl;

    synchronized ( this )
    {
        if ( ! areGroupKeysInitialized() )
            { initializeGroupKeys(); }
        groupsColl = new ArrayList(getGroupKeys().size());
        i = getGroupKeys().iterator();
    }  // end synchronized

    while ( i.hasNext() )
    {
        String groupKey = (String) i.next();
        groupsColl.add(getCompositeGroupService().findGroup(groupKey));
    }

    return groupsColl.iterator();
}
/**
 * @return java.lang.Class
 */
private java.lang.Class getDefaultEntityType()
{
    if (defaultEntityType == null)
    {
        Class cls = (new Object()).getClass();
        defaultEntityType = cls;
    }
    return defaultEntityType;
}
/**
 * @return java.util.Iterator
 */
private java.util.Iterator getEmptyIterator()
{
    return java.util.Collections.EMPTY_LIST.iterator();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getEntities() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return org.jasig.portal.groups.IEntityStore
 */
protected IEntityStore getEntityFactory() throws GroupsException {
    return RDBMEntityStore.singleton();
}
/**
 * @return org.jasig.portal.groups.IEntityGroupStore
 */
protected IEntityGroupStore getEntityGroupFactory() throws GroupsException {
    return GroupService.getGroupService().getGroupStore();
}
/**
 * @return java.util.Set
 */
private java.util.Set getGroupKeys() {
    if ( this.groupKeys == null )
        this.groupKeys = new HashSet(10);
    return groupKeys;
}
/**
 * @return org.jasig.portal.groups.IGroupService
 */
protected IGroupService getGroupService() throws GroupsException
{
    if (groupService == null)
        { groupService = GroupService.getGroupService(); }
    return groupService;
}
/**
 * @return java.lang.String
 */
public java.lang.String getKey() {
    return getUnderlyingEntityIdentifier().getKey();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public IEntityGroup getMemberGroupNamed(String name) throws GroupsException
{
    return null;
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return java.util.Iterator
 */
public java.util.Iterator getMembers() throws GroupsException
{
    return getEmptyIterator();
}
/**
 * @return java.lang.Class
 */
public java.lang.Class getType() {
    return getUnderlyingEntityIdentifier().getType();
}
/**
 * @return EntityIdentifier
 */
public EntityIdentifier getUnderlyingEntityIdentifier() {
    return underlyingEntityIdentifier;
}
/*
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
    return getKey().hashCode();
}
/**
 * Default implementation, overridden on EntityGroupImpl.
 * @return boolean
 */
public boolean hasMembers() throws GroupsException
{
    return false;
}
/**
 * Cache the keys for <code>IEntityGroups</code> that contain this <code>IGroupMember</code>.
 * @return void
 */
private void initializeContainingGroupKeys() throws GroupsException
{
    for ( Iterator it = getCompositeGroupService().findContainingGroups(this); it.hasNext(); )
        {  addGroup((IEntityGroup) it.next()); }
}
/**
 * Cache <code>IEntityGroup</code> keys for groups that contain this IGroupMember.
 */
private void initializeGroupKeys() throws GroupsException
{
    this.groupKeys = null;
    initializeContainingGroupKeys();
    setGroupKeysInitialized(true);
}
/**
 * Answers if this <code>IGroupMember</code> is, recursively, a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException {

    if ( gm.isEntity() )
        return false;
    if ( this.isMemberOf(gm) )
        return true;

    boolean isMember = false;
    Iterator it = gm.getMembers();
    while ( it.hasNext() && !isMember )
    {
        IGroupMember potentialParent = (IGroupMember) it.next();
        isMember = this.isDeepMemberOf(potentialParent);
    }

    return isMember;
}
/**
 * @return boolean
 */
public boolean isEntity()
{
    return false;
}
/**
 * @return boolean
 */
public boolean isGroup()
{
    return false;
}
/**
 * @return boolean.
 */
protected boolean isKnownEntityType(Class anEntityType) throws GroupsException
{
    return ( org.jasig.portal.EntityTypes.getEntityTypeID(anEntityType) != null );
}
/**
 * Answers if this <code>IGroupMember</code> is a member of <code>IGroupMember</code> gm.
 * @return boolean
 * @param gm org.jasig.portal.groups.IGroupMember
 */
public boolean isMemberOf(IGroupMember gm) throws GroupsException
{
    if ( gm.isEntity() )
        { return false; }
    if ( ! areGroupKeysInitialized() )
        { initializeGroupKeys(); }

    Object cacheKey = gm.getKey();
    return getGroupKeys().contains(cacheKey);
}
/**
 * Returns the <code>Set</code> of groups in our member <code>Collection</code> and,
 * recursively, in the <code>Collections</code> of our members.
 * @param s java.lang.Set - A Set that groups are added to.
 * @return java.util.Set
 */
protected java.util.Set primGetAllContainingGroups(Set s) throws GroupsException
{
    Iterator i = getContainingGroups();
    while ( i.hasNext() )
    {
        GroupMemberImpl gmi = (GroupMemberImpl) i.next();
        s.add(gmi);
        gmi.primGetAllContainingGroups(s);
    }
    return s;
}
/**
 * Remove the <code>IEntityGroup</code> key from our group keys <code>Set</code>.
 * @return void
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public synchronized void removeGroup(IEntityGroup eg)
{
    getGroupKeys().remove(eg.getEntityIdentifier().getKey());
}
/**
 * @param newGroupsInitialized boolean
 */
protected void setGroupKeysInitialized(boolean newGroupKeysInitialized) {
    groupKeysInitialized = newGroupKeysInitialized;
}
}
