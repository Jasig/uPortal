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

package org.jasig.portal.pags.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.Validate;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.jpa.OpenEntityManager;
import org.jasig.portal.pags.dao.IPersonAttributesGroupDefinitionDao;
import org.jasig.portal.pags.om.IPersonAttributesGroupDefinition;
import org.jasig.portal.pags.om.IPersonAttributesGroupStoreDefinition;
import org.springframework.stereotype.Repository;

import com.google.common.base.Function;

/**
 * @author Shawn Connolly, sconnolly@unicon.net
 */
@Repository("personAttributesGroupDefinitionDao")
public class JpaPersonAttributesGroupDefinitionDao extends BasePortalJpaDao implements IPersonAttributesGroupDefinitionDao {
    private CriteriaQuery<PersonAttributesGroupDefinitionImpl> findAllDefinitions;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.findAllDefinitions = this.createCriteriaQuery(new Function<CriteriaBuilder, CriteriaQuery<PersonAttributesGroupDefinitionImpl>>() {
            @Override
            public CriteriaQuery<PersonAttributesGroupDefinitionImpl> apply(CriteriaBuilder cb) {
                final CriteriaQuery<PersonAttributesGroupDefinitionImpl> criteriaQuery = cb.createQuery(PersonAttributesGroupDefinitionImpl.class);
                criteriaQuery.from(PersonAttributesGroupDefinitionImpl.class);
                return criteriaQuery;
            }
        });
    }

    @PortalTransactional
    @Override
    public IPersonAttributesGroupDefinition updatePersonAttributesGroupDefinition(IPersonAttributesGroupDefinition personAttributesGroupDefinition) {
        Validate.notNull(personAttributesGroupDefinition, "personAttributesGroupDefinition can not be null");
        
        final IPersonAttributesGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(personAttributesGroupDefinition)) {
            persistentDefinition = personAttributesGroupDefinition;
        } else {
            persistentDefinition = entityManager.merge(personAttributesGroupDefinition);
        }
        this.getEntityManager().persist(persistentDefinition);
        return persistentDefinition;
    }

    @PortalTransactional
    @Override
    public void deletePersonAttributesGroupDefinition(IPersonAttributesGroupDefinition definition) {
        Validate.notNull(definition, "definition can not be null");

        final IPersonAttributesGroupDefinition persistentDefinition;
        final EntityManager entityManager = this.getEntityManager();
        if (entityManager.contains(definition)) {
            persistentDefinition = definition;
        } else {
            persistentDefinition = entityManager.merge(definition);
        }
        entityManager.remove(persistentDefinition);
    }

    @OpenEntityManager(unitName = PERSISTENCE_UNIT_NAME)
    @Override
    public List<IPersonAttributesGroupDefinition> getPersonAttributesGroupDefinitionByName(String name) {
        CriteriaBuilder criteriaBuilder = this.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<PersonAttributesGroupDefinitionImpl> criteriaQuery = 
                criteriaBuilder.createQuery(PersonAttributesGroupDefinitionImpl.class);
        Root<PersonAttributesGroupDefinitionImpl> root = criteriaQuery.from(PersonAttributesGroupDefinitionImpl.class);
        ParameterExpression<String> nameParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("name"), nameParameter));
        TypedQuery<PersonAttributesGroupDefinitionImpl> query = this.getEntityManager().createQuery(criteriaQuery);
        query.setParameter(nameParameter, name);
        List<IPersonAttributesGroupDefinition> groups = new ArrayList<IPersonAttributesGroupDefinition>();
        for (IPersonAttributesGroupDefinition group: query.getResultList()) {
            groups.add(group);
        }
        return groups;
    }

    @Override
    public List<IPersonAttributesGroupDefinition> getPersonAttributesGroupDefinitions() {
        final TypedQuery<PersonAttributesGroupDefinitionImpl> query = this.createCachedQuery(this.findAllDefinitions);
        List<IPersonAttributesGroupDefinition> groups = new ArrayList<IPersonAttributesGroupDefinition>();
        for (IPersonAttributesGroupDefinition group: query.getResultList()) {
            groups.add(group);
        }
        return groups;
    }
    
    @PortalTransactional
    @Override
    public IPersonAttributesGroupDefinition createPersonAttributesGroupDefinition(IPersonAttributesGroupStoreDefinition store, String name, String description) {
        final IPersonAttributesGroupDefinition personAttributesGroupDefinition = new PersonAttributesGroupDefinitionImpl(store, name, description);
        this.getEntityManager().persist(personAttributesGroupDefinition);
        return personAttributesGroupDefinition;
    }

}
