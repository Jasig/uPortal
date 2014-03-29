/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups.pags.entity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.EntityTestingGroupImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupTestGroupDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.security.provider.RestrictedPerson;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.jasig.portal.spring.locator.PersonAttributeDaoLocator;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Person Attributes Group Store uses attributes stored in the IPerson object to determine
 * group membership.  It can use attributes from any data source supported by the PersonDirectory
 * service.
 * 
 * @author Shawn Connolly, sconnolly@unicon.net
 * @since 4.1
 */
public class EntityPersonAttributesGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {
    private final Logger logger = LoggerFactory.getLogger(EntityPersonAttributesGroupStore.class);
    private static final Class<IPerson> IPERSON_CLASS = IPerson.class;
    private static final EntityIdentifier[] EMPTY_SEARCH_RESULTS = new EntityIdentifier[0];
    private IPersonAttributesGroupDefinitionDao personAttributesGroupDefinitionDao;
    private final ApplicationContext applicationContext;
    
    public EntityPersonAttributesGroupStore() {
         super();
         this.applicationContext = ApplicationContextLocator.getApplicationContext();
         this.personAttributesGroupDefinitionDao = applicationContext.getBean("personAttributesGroupDefinitionDao", IPersonAttributesGroupDefinitionDao.class);
    }

    public boolean contains(IEntityGroup group, IGroupMember member) {
        GroupDefinition groupDef = convertEntityToGroupDef(group);
        if (member.isGroup()) 
        {
           String key = ((IEntityGroup)member).getLocalKey();
           return groupDef.hasMember(key);
        } 
        else 
        {
           if (member.getEntityType() != IPERSON_CLASS) 
               { return false; }
           IPerson person = null;
           try {
               IPersonAttributeDao pa = PersonAttributeDaoLocator.getPersonAttributeDao();
               final IPersonAttributes personAttributes = pa.getPerson(member.getKey());

               RestrictedPerson rp = PersonFactory.createRestrictedPerson();
               if (personAttributes != null) {
                   rp.setAttributes(personAttributes.getAttributes());
               }
               
               person = rp;
           }
           catch (Exception ex) { 
               logger.error("Exception acquiring attributes for member " + member + " while checking if group " + group + " contains this member.", ex);
               return false;
           }
           return testRecursively(groupDef, person, member);
        }
    }

    private GroupDefinition convertEntityToGroupDef(IEntityGroup group) {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(group.getName());
        IPersonAttributesGroupDefinition pagsGroup = groups.get(0);
        return initGroupDef(pagsGroup);
    }
    
    private IEntityGroup convertPagsGroupToEntity(IPersonAttributesGroupDefinition group) {
        IEntityGroup entityGroup = new EntityTestingGroupImpl(group.getName(), IPERSON_CLASS);
        entityGroup.setName(group.getName());
        entityGroup.setDescription(group.getDescription());
        return entityGroup;
    }

    public void delete(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method delete() not supported.");
    }

    public IEntityGroup find(String name) throws GroupsException {
        List<IPersonAttributesGroupDefinition> groups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        IPersonAttributesGroupDefinition pagsGroup = groups.get(0);
        GroupDefinition groupDef = initGroupDef(pagsGroup);
        IEntityGroup group = new EntityTestingGroupImpl(groupDef.getKey(), IPERSON_CLASS);
        group.setName(groupDef.getName());
        group.setDescription(groupDef.getDescription());
        return group;
    }

    private boolean testRecursively(GroupDefinition groupDef, IPerson person, IGroupMember member)
        throws GroupsException {
            if ( ! groupDef.contains(person) )
                { return false;}
            else
            {
                IEntityGroup group = find(groupDef.getName());
                IEntityGroup parentGroup = null;
                Set<IEntityGroup> allParents = primGetAllContainingGroups(group, new HashSet<IEntityGroup>());
                boolean testPassed = true;
                for (Iterator<IEntityGroup> i=allParents.iterator(); i.hasNext() && testPassed;)
                {
                    parentGroup = i.next();
                    GroupDefinition parentGroupDef = (GroupDefinition) convertEntityToGroupDef(parentGroup);
                    testPassed = parentGroupDef.test(person);
                }
                
                if (!testPassed && logger.isWarnEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("PAGS group=").append(group.getKey());
                    sb.append(" contained person=").append(member.getKey());
                    sb.append(", but the person failed to be contained in ");
                    sb.append("ancesters of this group");
                    sb.append((parentGroup != null ? " (parentGroup="+parentGroup.getKey()+")" : ""));
                    sb.append(". This may indicate a ");
                    sb.append("misconfigured PAGS group ");
                    sb.append("store. Please check PAGSGroupStoreConfig.xml.");
                    logger.warn(sb.toString());
                }
                return testPassed;
            }
        }
    private java.util.Set<IEntityGroup> primGetAllContainingGroups(IEntityGroup group, Set<IEntityGroup> s)
            throws GroupsException
            {
                Iterator<IEntityGroup> i = findContainingGroups(group);
                while ( i.hasNext() )
                {
                    IEntityGroup parentGroup = i.next();
                    s.add(parentGroup);
                    primGetAllContainingGroups(parentGroup, s);
                }
                return s;
            }

    public Iterator<IEntityGroup> findContainingGroups(IGroupMember member) 
    throws GroupsException 
    {
        return (member.isEntity()) 
          ? findContainingGroupsForEntity((IEntity)member)
          : findContainingGroupsForGroup((IEntityGroup)member);
    }
    
    private Iterator<IEntityGroup> findContainingGroupsForGroup(IEntityGroup group)
    {
         List<IEntityGroup> parents = getContainingGroups(group.getName(), new ArrayList<IEntityGroup>());
         return (parents !=null)
            ? parents.iterator()
            : Collections.EMPTY_LIST.iterator();
    }
    private Iterator<IEntityGroup> findContainingGroupsForEntity(IEntity member)
    throws GroupsException {
        List<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
         List<IEntityGroup> results = new ArrayList<IEntityGroup>();
         for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
             IEntityGroup group = convertPagsGroupToEntity(pagsGroup);
             if ( contains(group, member))
                  { results.add(group); }
         }
         return results.iterator();
    }

    public Iterator<IEntityGroup> findEntitiesForGroup(IEntityGroup group) throws GroupsException {
        return Collections.EMPTY_LIST.iterator();
    }

    public ILockableEntityGroup findLockable(String key) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method findLockable() not supported");
    }

    public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException {

        List<String> keys = new ArrayList<String>();
        GroupDefinition groupDef = convertEntityToGroupDef(group);
        if (groupDef != null)
        {
             for (Iterator<String> i = groupDef.members.iterator(); i.hasNext(); ) 
                  { keys.add(i.next()); }
        }
        return keys.toArray(new String[]{});
    }

    public Iterator<IEntityGroup> findMemberGroups(IEntityGroup group) throws GroupsException {
        List<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(group.getName());
        IPersonAttributesGroupDefinition pagsGroup = pagsGroups.get(0);
        List<IEntityGroup> results = new ArrayList<IEntityGroup>();
        for (IPersonAttributesGroupDefinition member : pagsGroup.getMembers()) {
            results.add(convertPagsGroupToEntity(member));
        }
        return results.iterator();
    }

    public IEntityGroup newInstance(Class entityType) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method newInstance() not supported");
    }

    public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
        if ( leaftype != IPERSON_CLASS )
             { return EMPTY_SEARCH_RESULTS; }
        List<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitions();
        List<EntityIdentifier> results = new ArrayList<EntityIdentifier>();
        switch (method) {
            case IS:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().equalsIgnoreCase(query)) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case STARTS_WITH:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().startsWith(query.toUpperCase())) {
                        results.add(g.getEntityIdentifier());
                    }
                }
                break;
            case ENDS_WITH:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().endsWith(query.toUpperCase())) {
                        results.add(g.getEntityIdentifier());
                  }
                }
                break;
            case CONTAINS:
                for (IPersonAttributesGroupDefinition pagsGroup : pagsGroups) {
                    IEntityGroup g = convertPagsGroupToEntity(pagsGroup);
                    if (g.getName().toUpperCase().indexOf(query.toUpperCase()) != -1) {
                        results.add(g.getEntityIdentifier());
                  }
                }
                break;
        }
        return results.toArray(new EntityIdentifier[]{});
    }

    public void update(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method update() not supported.");
    }

    public void updateMembers(IEntityGroup group) throws GroupsException {
        throw new UnsupportedOperationException("EntityPersonAttributesGroupStore: Method updateMembers() not supported.");
    }
    
    public static class GroupDefinition {
        private String key;
        private String name;
        private String description;
        private List<String> members;
        private List<TestGroup> testGroups;
        
        public GroupDefinition() {
            members = new Vector<String>();
            testGroups = new Vector<TestGroup>();
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        public String getKey() {
            return key;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }
        public void addMember(String key) {
            members.add(key);
        }
        public boolean hasMember(String key) {
            return members.contains(key);
        }
        public void addTestGroup(TestGroup testGroup) {
            testGroups.add(testGroup);
        }
        public boolean contains(IPerson person) {
            return ( testGroups.isEmpty() ) ? false : test(person);
        }
        public boolean test(IPerson person) {
            if (testGroups.isEmpty())
                 return true;
            for (Iterator<TestGroup> i = testGroups.iterator(); i.hasNext(); ) {
                TestGroup testGroup = i.next();
                if (testGroup.test(person)) {
                    return true;
                }
            }
            return false;
        }
        public String toString() {
             return "GroupDefinition " + key + " (" + name + ")";
        }
    }
    
    public static class TestGroup {
        private List<IPersonTester> tests;
        
        public TestGroup() {
            tests = new Vector<IPersonTester>();
        }
        
        public void addTest(IPersonTester test) {
            tests.add(test);
        }
        
        public boolean test(IPerson person) {
            for (Iterator<IPersonTester> i = tests.iterator(); i.hasNext(); ) {
                IPersonTester tester = i.next();
                if ((tester == null) || (!tester.test(person))) {
                    return false;
                }
            }
            return true;
        }
    }

    public IEntity newInstance(String key, Class type) throws GroupsException {
        if (EntityTypes.getEntityTypeID(type) == null) {
            throw new GroupsException("Invalid entity type: "+type.getName());
        }
        return new EntityImpl(key, type);
    }

    public IEntity newInstance(String key) throws GroupsException {
        return new EntityImpl(key, null);
    }

    public EntityIdentifier[] searchForEntities(String query, int method, Class type) throws GroupsException {
        return EMPTY_SEARCH_RESULTS;
    }

    private GroupDefinition initGroupDef(IPersonAttributesGroupDefinition group) {
        GroupDefinition groupDef = new GroupDefinition();
        groupDef.setKey(group.getName());
        groupDef.setName(group.getName());
        groupDef.setDescription(group.getDescription());
        addMemberKeys(groupDef, group.getMembers());
        List<IPersonAttributesGroupTestGroupDefinition> testGroups = group.getTestGroups();
        for(IPersonAttributesGroupTestGroupDefinition testGroup : testGroups) {
            TestGroup tg = new TestGroup();
            List<IPersonAttributesGroupTestDefinition> tests = testGroup.getTests();
            for(IPersonAttributesGroupTestDefinition test : tests) {
                IPersonTester testerInst = initializeTester(test.getTesterClassName(), test.getAttributeName(), test.getTestValue());
                tg.addTest(testerInst);
            }
            groupDef.addTestGroup(tg);
        }
        return groupDef;
     }
    private void addMemberKeys(GroupDefinition groupDef, List<IPersonAttributesGroupDefinition> members) {
        for(IPersonAttributesGroupDefinition member: members) {
            groupDef.addMember(member.getName());
        }
    }
    private IPersonTester initializeTester(String tester, String attribute, String value) {
          try {
             Class<?> testerClass = Class.forName(tester);
             Constructor<?> c = testerClass.getConstructor(new Class[]{String.class, String.class});
             Object o = c.newInstance(new Object[]{attribute, value});
             return (IPersonTester)o;
          } catch (Exception e) {
             logger.error("Error in initializing tester class: {}", tester, e);
             return null;
          }
       }
    private List<IEntityGroup> getContainingGroups(String name, List<IEntityGroup> groups) throws GroupsException
    {
        List<IPersonAttributesGroupDefinition> pagsGroups = personAttributesGroupDefinitionDao.getPersonAttributesGroupDefinitionByName(name);
        IPersonAttributesGroupDefinition pagsGroup = pagsGroups.get(0);
        List<IPersonAttributesGroupDefinition> pagsParentGroups = pagsGroup.getParents();
        for (IPersonAttributesGroupDefinition parent : pagsParentGroups) {
            if (!groups.contains(parent)) {
                groups.add(convertPagsGroupToEntity(parent));
                getContainingGroups(parent.getName(), groups);
            } else {
                throw new RuntimeException("Recursive grouping detected! for " + name + " and parent " + parent.getName());
            }
        }
        return groups;
    }
}