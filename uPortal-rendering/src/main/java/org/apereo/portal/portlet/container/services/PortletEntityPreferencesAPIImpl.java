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
package org.apereo.portal.portlet.container.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.portlet.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Preference;
import org.apache.pluto.container.om.portlet.Preferences;
import org.apereo.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/** Preferences impl that manipulates the portlet entity level preference data for the REST API */
public class PortletEntityPreferencesAPIImpl
        extends AbstractPortletPreferencesImpl<IPortletEntity> {
    private final HttpServletRequest portletRequest;
    private final IPortletEntityId portletEntityId;

    private final IPortletEntityRegistry portletEntityRegistry;
    private final IPortletDefinitionRegistry portletDefinitionRegistry;
    private final TransactionOperations transactionOperations;

    public PortletEntityPreferencesAPIImpl(
            HttpServletRequest portletRequestContext,
            IPortletEntityRegistry portletEntityRegistry,
            IPortletDefinitionRegistry portletDefinitionRegistry,
            TransactionOperations transactionOperations,
            IPortletEntityId portletEntityId) {
        super(false);

        this.portletRequest = portletRequestContext;
        this.portletEntityRegistry = portletEntityRegistry;
        this.portletDefinitionRegistry = portletDefinitionRegistry;
        this.transactionOperations = transactionOperations;
        this.portletEntityId = portletEntityId;
    }

    @Override
    protected IPortletEntity getInitializationContext() {
        final HttpServletRequest containerRequest = this.portletRequest;
        return this.portletEntityRegistry.getPortletEntity(containerRequest, portletEntityId);
    }

    @Override
    protected Object getLogDescription() {
        return this.getInitializationContext();
    }

    @Override
    protected void loadTargetPortletPreferences(
            IPortletEntity portletEntity,
            Map<String, IPortletPreference> targetPortletPreferences) {
        final List<IPortletPreference> entityPreferences = portletEntity.getPortletPreferences();
        for (final IPortletPreference preference : entityPreferences) {
            targetPortletPreferences.put(preference.getName(), preference);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected void loadBasePortletPreferences(
            IPortletEntity portletEntity, Map<String, IPortletPreference> basePortletPreferences) {
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();

        // Add descriptor prefs to base Map
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final PortletDefinition portletDescriptor =
                this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        final Preferences descriptorPreferences = portletDescriptor.getPortletPreferences();
        for (final Preference preference : descriptorPreferences.getPortletPreferences()) {
            final IPortletPreference preferenceWrapper = new PortletPreferenceImpl(preference);
            basePortletPreferences.put(preferenceWrapper.getName(), preferenceWrapper);
        }

        // Add definition prefs to base Map
        final List<IPortletPreference> definitionPreferences =
                portletDefinition.getPortletPreferences();
        for (final IPortletPreference preference : definitionPreferences) {
            basePortletPreferences.put(preference.getName(), preference);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    protected boolean storeInternal() throws IOException, ValidatorException {
        final HttpServletRequest containerRequest = portletRequest;

        final IPortletEntity portletEntity =
                this.portletEntityRegistry.getPortletEntity(containerRequest, portletEntityId);
        final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
        final Lock portletEntityLock =
                this.portletEntityRegistry.getPortletEntityLock(containerRequest, portletEntityId);

        // Do a tryLock first so that we can warn about concurrent preference modification if it
        // fails
        boolean locked = portletEntityLock.tryLock();
        try {
            if (!locked) {
                logger.warn(
                        "Concurrent portlet preferences modification by: "
                                + portletEntity
                                + " "
                                + "This has the potential for changes to preferences to be lost. "
                                + "This portlet should be modified to synchronize its preference modifications appropriately",
                        new Throwable());

                portletEntityLock.lock();
                locked = true;
            }

            return this.transactionOperations.execute(
                    new TransactionCallback<Boolean>() {
                        @Override
                        public Boolean doInTransaction(TransactionStatus status) {
                            // Refresh the entity to avoid optimistic locking errors
                            final IPortletEntity portletEntity =
                                    portletEntityRegistry.getPortletEntity(
                                            containerRequest, portletEntityId);

                            final Map<String, IPortletPreference> targetPortletPreferences =
                                    getTargetPortletPreferences();
                            final Collection<IPortletPreference> values =
                                    targetPortletPreferences.values();
                            final boolean modified =
                                    portletEntity.setPortletPreferences(
                                            new ArrayList<IPortletPreference>(values));
                            if (!modified) {
                                // Nothing actually changed, skip the store
                                return Boolean.FALSE;
                            }

                            portletEntityRegistry.storePortletEntity(
                                    containerRequest, portletEntity);

                            return Boolean.TRUE;
                        }
                    });
        } finally {
            // check if locked, needed due to slightly more complex logic around the tryLock and
            // logging
            if (locked) {
                portletEntityLock.unlock();
            }
        }
    }
}
