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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.portlet.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

/**
 * Preferences impl that manipulates the portlet entity level preference data for Guests used only
 * for the REST API
 */
public class GuestPortletEntityPreferencesAPIImpl
        extends AbstractPortletPreferencesImpl<IPortletEntity> {
    protected static final String PORTLET_PREFERENCES_MAP_ATTRIBUTE =
            GuestPortletEntityPreferencesAPIImpl.class.getName() + ".PORTLET_PREFERENCES_MAP";

    private final HttpServletRequest portletRequest;
    private final IPortletEntityId portletEntityId;

    private final IPortletEntityRegistry portletEntityRegistry;
    private final IPortletDefinitionRegistry portletDefinitionRegistry;

    public GuestPortletEntityPreferencesAPIImpl(
            HttpServletRequest portletRequest,
            IPortletEntityRegistry portletEntityRegistry,
            IPortletDefinitionRegistry portletDefinitionRegistry,
            IPortletEntityId portletEntityId,
            boolean render) {
        super(render);

        this.portletRequest = portletRequest;
        this.portletEntityRegistry = portletEntityRegistry;
        this.portletDefinitionRegistry = portletDefinitionRegistry;
        this.portletEntityId = portletEntityId;
    }

    @Override
    protected IPortletEntity getInitializationContext() {
        return this.portletEntityRegistry.getPortletEntity(portletRequest, portletEntityId);
    }

    @Override
    protected Object getLogDescription() {
        return this.getInitializationContext();
    }

    @Override
    protected void loadTargetPortletPreferences(
            IPortletEntity portletEntity,
            Map<String, IPortletPreference> targetPortletPreferences) {
        final Map<String, IPortletPreference> sessionPreferences =
                this.getSessionPreferences(portletEntityId, portletRequest);
        if (sessionPreferences != null) {
            targetPortletPreferences.putAll(sessionPreferences);
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

        // Add entity prefs to base Map
        final List<IPortletPreference> entityPreferences = portletEntity.getPortletPreferences();
        for (final IPortletPreference preference : entityPreferences) {
            basePortletPreferences.put(preference.getName(), preference);
        }
    }

    @Override
    protected boolean storeInternal() throws IOException, ValidatorException {
        final Map<String, IPortletPreference> targetPortletPreferences =
                this.getTargetPortletPreferences();
        if (targetPortletPreferences.isEmpty()) {
            return false;
        }

        this.storeSessionPreferences(portletEntityId, portletRequest, targetPortletPreferences);

        return true;
    }

    /**
     * Gets the session-stored map of IPortletPreferences for the specified request and
     * IPortletEntityId.
     *
     * @return Map of IPortletPreferences for the entity and session, may be null if no preferences
     *     have been set.
     */
    @SuppressWarnings({"unchecked", "Duplicates"})
    protected Map<String, IPortletPreference> getSessionPreferences(
            IPortletEntityId portletEntityId, HttpServletRequest httpServletRequest) {
        final HttpSession session = httpServletRequest.getSession();

        final Map<IPortletEntityId, Map<String, IPortletPreference>> portletPreferences;

        // Sync on the session to ensure the Map isn't in the process of being created
        synchronized (session) {
            portletPreferences =
                    (Map<IPortletEntityId, Map<String, IPortletPreference>>)
                            session.getAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE);
        }

        if (portletPreferences == null) {
            return null;
        }

        return portletPreferences.get(portletEntityId);
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    protected void storeSessionPreferences(
            IPortletEntityId portletEntityId,
            HttpServletRequest httpServletRequest,
            Map<String, IPortletPreference> preferences) {
        final HttpSession session = httpServletRequest.getSession();

        Map<IPortletEntityId, Map<String, IPortletPreference>> portletPreferences;

        // Sync on the session to ensure other threads aren't creating the Map at the same time
        synchronized (session) {
            portletPreferences =
                    (Map<IPortletEntityId, Map<String, IPortletPreference>>)
                            session.getAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE);
            if (portletPreferences == null) {
                portletPreferences =
                        new ConcurrentHashMap<IPortletEntityId, Map<String, IPortletPreference>>();
                session.setAttribute(PORTLET_PREFERENCES_MAP_ATTRIBUTE, portletPreferences);
            }
        }

        portletPreferences.put(portletEntityId, preferences);
    }
}
