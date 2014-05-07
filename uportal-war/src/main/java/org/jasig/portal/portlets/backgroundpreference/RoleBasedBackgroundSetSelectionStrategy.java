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

package org.jasig.portal.portlets.backgroundpreference;

import java.util.Arrays;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.context.PortletWebRequest;

/**
 * Determines set of images to display by checking to see if user is in a mobile user group as determined by the
 * portal's security roles.
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Component
public class RoleBasedBackgroundSetSelectionStrategy implements BackgroundSetSelectionStrategy {

    private enum PreferenceNames {

        DEFAULT("default"),

        MOBILE("mobile");

        /**
         * <b>Must match</a> the name of the corresponding PAGS group perfectly.
         */
        private static final String MOBILE_DEVICE_ROLE_NAME = "Mobile Device Access";

        public static PreferenceNames getInstance(PortletRequest req) {
            return req.isUserInRole(MOBILE_DEVICE_ROLE_NAME) ? MOBILE : DEFAULT;
        }
        private final String prefix;

        private PreferenceNames(String prefix) {
            this.prefix = prefix;
        }

        public String getImageSetPreferenceName() {
            return prefix + "BackgroundImages";
        }

        public String getImageThumbnailSetPreferenceName() {
            return prefix + "BackgroundThumbnailImages";
        }

        public String getSelectedBackgroundImagePreferenceName() {
            return prefix + "SelectedBackgroundImage";
        }

        public String getBackgroundContainerSelectorPreferenceName() {
            return prefix + "BackgroundContainerSelector";
        }
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private IPortalSpELService portalSpELService;

    @Autowired
    public void setPortalSpELService(IPortalSpELService portalSpELService) {
        this.portalSpELService = portalSpELService;
    }

    /**
     * Contructor of the object RoleBasedBackgroundSetSelectionStrategy.java.
     */
    public RoleBasedBackgroundSetSelectionStrategy() {
        super();
    }

    /**
     * Contructor of the object RoleBasedBackgroundSetSelectionStrategy.java.
     */
    public RoleBasedBackgroundSetSelectionStrategy(IPortalSpELService portalSpELService) {
        super();
        this.portalSpELService = portalSpELService;
    }

    @Override
    public String[] getImageSet(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValues(names.getImageSetPreferenceName(), null);
    }

    @Override
    public String[] getImageThumbnailSet(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValues(names.getImageThumbnailSetPreferenceName(), null);
    }

    @Override
    public String getSelectedImage(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValue(names.getSelectedBackgroundImagePreferenceName(), null);
    }

    @Override
    public String getBackgroundContainerSelector(PortletRequest req) {
        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        return prefs.getValue(names.getBackgroundContainerSelectorPreferenceName(), null);
    }

    @Override
    public void setSelectedImage(ActionRequest req, String backgroundImage) {

        PreferenceNames names = PreferenceNames.getInstance(req);
        PortletPreferences prefs = req.getPreferences();
        final PortletWebRequest webRequest = new PortletWebRequest(req);

        if (StringUtils.isNotBlank(backgroundImage)) {

            // We are trying to choose a background;  first verify the requested image is actually in the set...
            String[] images = setPrefContextpath(prefs.getValues(names.getImageSetPreferenceName(), EMPTY_STRING_ARRAY), webRequest);
            if (Arrays.asList(images).contains(backgroundImage)) {
                try {
                    prefs.setValue(names.getSelectedBackgroundImagePreferenceName(), backgroundImage);
                    prefs.store();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to store the user's choice of background image", e);
                }
            }

        } else {

            // We trying to clear a previous selection
            try {
                prefs.reset(names.getSelectedBackgroundImagePreferenceName());
                prefs.store();
            } catch (Exception e) {
                throw new RuntimeException("Failed to reset the user's choice of background image", e);
            }

        }

    }

    protected String[] setPrefContextpath(String[] values, WebRequest request) {
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++)
                values[i] = this.setPrefContextpath(values[i], request);
        }

        return values;
    }

    protected String setPrefContextpath(String value, WebRequest request) {
        if (value != null) {
            return this.portalSpELService.parseString(value, request);
        }

        return value;
    }

}
