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

package org.jasig.portal.security.provider.cas;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.provider.ChainingSecurityContext;
import org.jasig.portal.spring.locator.ApplicationContextLocator;
import org.jasig.services.persondir.support.IAdditionalDescriptors;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * Implementation of the {@link org.jasig.portal.security.provider.cas.ICasSecurityContext} that reads the Assertion
 * from the ThreadLocal. The Assertion stored in a ThreadLocal is an artifact of the Jasig CAS Client for Java 3.x
 * library.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.2
 */
public class CasAssertionSecurityContext extends ChainingSecurityContext implements ICasSecurityContext {
    private static final String DEFAULT_PORTAL_SECURITY_PROPERTY_FILE = "properties/security.properties";
    private static final String SESSION_ADDITIONAL_DESCRIPTORS_BEAN = "sessionScopeAdditionalDescriptors";
    private static final String PROPERTY_COPY_ASSERT_ATTRS_TO_USER_ATTRS = "org.jasig.portal.security.provider.cas.CasAssertionSecurityContext.copy-assertion-attributes-to-user-attributes";

    private Assertion assertion;
    private boolean copyAssertionAttributesToUserAttributes = true;

    public CasAssertionSecurityContext() {
        readConfiguration();
    }

    public int getAuthType() {
        return CAS_AUTHTYPE;
    }

    /**
     * Exposes a template post-authentication method for subclasses to implement their custom logic in.
     * <p>
     * NOTE: This is called BEFORE super.authenticate();
     *
     * @param assertion the Assertion that was retrieved from the ThreadLocal.  CANNOT be NULL.
     */
    protected void postAuthenticate(final Assertion assertion) {
        copyAssertionAttributesToUserAttributes(assertion);
    }

    @Override
    public final void authenticate() throws PortalSecurityException {
        if (log.isTraceEnabled()) {
            log.trace("Authenticating user via CAS.");
        }

        this.isauth = false;
        this.assertion = AssertionHolder.getAssertion();

        if (this.assertion != null) {
            this.myPrincipal.setUID(assertion.getPrincipal().getName());
            this.isauth = true;
            log.debug("CASContext authenticated [" + this.myPrincipal.getUID() + "] using assertion [" + this.assertion + "]");
            postAuthenticate(assertion);
        }

        this.myAdditionalDescriptor = null; //no additional descriptor from CAS
        super.authenticate();
        if (log.isTraceEnabled()) {
            log.trace("Finished CAS Authentication");
        }
    }

    public final String getCasServiceToken(final String target) throws CasProxyTicketAcquisitionException {
        if (log.isTraceEnabled()) {
            log.trace("Attempting to retrieve proxy ticket for target [" + target + "] by using CAS Assertion [" + assertion + "]");
        }

        if (this.assertion == null) {
            if (log.isDebugEnabled()) {
                log.debug("No Assertion found for user.  Returning null Proxy Ticket.");
            }
            return null;
        }

        final String proxyTicket = this.assertion.getPrincipal().getProxyTicketFor(target);

        if (proxyTicket == null) {
            log.error("Failed to retrieve proxy ticket for assertion [" + assertion + "].  Is the PGT still valid?");
            throw new CasProxyTicketAcquisitionException(target, assertion.getPrincipal());
        }

        if (log.isTraceEnabled()) {
            log.trace("Returning from Proxy Ticket Request with ticket [" + proxyTicket + "]");
        }

        return proxyTicket;
    }

    public String toString() {
        final StringBuilder builder =  new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append(" assertion:");
        builder.append(this.assertion);
        return builder.toString();
    }

    /**
     * If enabled, convert CAS assertion person attributes into uPortal user attributes.
     *
     * @param assertion the Assertion that was retrieved from the ThreadLocal.  CANNOT be NULL.
     */
    protected void copyAssertionAttributesToUserAttributes(Assertion assertion) {
        if (!copyAssertionAttributesToUserAttributes) {
            return;
        }

        // skip this if there are no attributes or if the attribute set is empty.
        if (assertion.getPrincipal().getAttributes() == null || assertion.getPrincipal().getAttributes().isEmpty()) {
            return;
        }

        Map<String, List<Object>> attributes = new HashMap<>();
        // loop over the set of person attributes from CAS...
        for (Map.Entry<String, Object> attrEntry : assertion.getPrincipal().getAttributes().entrySet()) {
            log.debug("Adding attribute '{}' from Assertion with value '{}'; runtime type of value is {}",
                    attrEntry.getKey(), attrEntry.getValue(), attrEntry.getValue().getClass().getName());

            // convert each attribute to a list, if necessary...
            List<Object> valueList = null;
            if (attrEntry.getValue() instanceof List) {
                valueList = (List)attrEntry.getValue();
            } else {
                valueList = Arrays.asList(attrEntry.getValue());
            }

            // add the attribute...
            attributes.put(attrEntry.getKey(), valueList);
        }

        // get the attribute descriptor from Spring...
        ApplicationContext applicationContext = ApplicationContextLocator.getApplicationContext();
        IAdditionalDescriptors additionalDescriptors = (IAdditionalDescriptors) applicationContext.getBean(SESSION_ADDITIONAL_DESCRIPTORS_BEAN);

        // add the new properties...
        additionalDescriptors.addAttributes(attributes);
    }


    /**
     * Read the config attributes specific to this security context.
     */
    private void readConfiguration() {
        final Resource resource = new ClassPathResource(DEFAULT_PORTAL_SECURITY_PROPERTY_FILE, getClass().getClassLoader());
        final Properties securityProperties = new Properties();

        InputStream inputStream = null;
        try {
            inputStream = resource.getInputStream();
            securityProperties.load(inputStream);
            String propertyVal = securityProperties.getProperty(PROPERTY_COPY_ASSERT_ATTRS_TO_USER_ATTRS, "false");
            copyAssertionAttributesToUserAttributes = "true".equalsIgnoreCase(propertyVal);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
