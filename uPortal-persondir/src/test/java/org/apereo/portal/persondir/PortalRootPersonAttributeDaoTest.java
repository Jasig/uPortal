/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.persondir;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.junit.Assert;
import org.junit.Test;

public class PortalRootPersonAttributeDaoTest extends PortalRootPersonAttributeDao {

    private static final String USERNAME = "nebuchadnezzar";
    private static final String FIRST_NAME = "Nebuchadnezzar";
    private static final String LAST_NAME = "II";

    private final IUsernameAttributeProvider usernameAttributeProvider =
            new SimpleUsernameAttributeProvider();
    private final PortalRootPersonAttributeDao portalRootPersonAttributeDao =
            new PortalRootPersonAttributeDao();

    public PortalRootPersonAttributeDaoTest() {
        portalRootPersonAttributeDao.setUsernameAttributeProvider(usernameAttributeProvider);
    }

    @Test
    public void applyOverridesIfPresentTest() {

        // Setup
        final Map<String, List<Object>> attributes =
                Collections.singletonMap(
                        CUSTOMARY_FIRST_NAME_ATTRIBUTE, Collections.singletonList(FIRST_NAME));
        final IPersonAttributes person = new NamedPersonImpl(USERNAME, attributes);

        // First without overrides
        portalRootPersonAttributeDao.setUserAttributeOverride(
                "somebody.else",
                Collections.singletonMap(CUSTOMARY_FIRST_NAME_ATTRIBUTE, "something"));
        final IPersonAttributes rsltWithout =
                portalRootPersonAttributeDao.applyOverridesIfPresent(person);
        Assert.assertEquals(
                FIRST_NAME, rsltWithout.getAttributeValue(CUSTOMARY_FIRST_NAME_ATTRIBUTE));

        // Then with overrides
        final String overriddenFirstName = "Nebby";
        portalRootPersonAttributeDao.setUserAttributeOverride(
                USERNAME,
                Collections.singletonMap(CUSTOMARY_FIRST_NAME_ATTRIBUTE, overriddenFirstName));
        final IPersonAttributes rsltWith =
                portalRootPersonAttributeDao.applyOverridesIfPresent(person);
        Assert.assertEquals(
                overriddenFirstName, rsltWith.getAttributeValue(CUSTOMARY_FIRST_NAME_ATTRIBUTE));
    }

    @Test
    public void selectUsernameIfAbsentTest() {

        // Setup
        final Map<String, List<Object>> attributes =
                Collections.singletonMap(
                        CUSTOMARY_FIRST_NAME_ATTRIBUTE, Collections.singletonList(FIRST_NAME));
        final IPersonAttributes person = new NamedPersonImpl(USERNAME, attributes);

        // Test
        final IPersonAttributes rslt =
                portalRootPersonAttributeDao.selectUsernameIfAbsent(person, USERNAME);
        Assert.assertEquals(
                USERNAME, rslt.getAttributeValue(usernameAttributeProvider.getUsernameAttribute()));
    }

    @Test
    public void selectDisplayNameIfAbsent() {

        // Setup
        final Map<String, List<Object>> attributes = new HashMap<>();
        attributes.put(CUSTOMARY_FIRST_NAME_ATTRIBUTE, Collections.singletonList(FIRST_NAME));
        attributes.put(CUSTOMARY_LAST_NAME_ATTRIBUTE, Collections.singletonList(LAST_NAME));
        final IPersonAttributes person = new NamedPersonImpl(USERNAME, attributes);

        // Test
        final IPersonAttributes rslt =
                portalRootPersonAttributeDao.selectDisplayNameIfAbsent(person);
        Assert.assertEquals(
                FIRST_NAME + " " + LAST_NAME,
                rslt.getAttributeValue(ILocalAccountPerson.ATTR_DISPLAY_NAME));
    }
}
