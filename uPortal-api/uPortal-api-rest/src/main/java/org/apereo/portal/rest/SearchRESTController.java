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
package org.apereo.portal.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlets.lookup.PersonLookupHelperImpl;
import org.apereo.portal.rest.utils.PortletRegistryUtil;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * REST Search endpoint that aggregates search of various types.
 *
 * <p>JSON results example (whitespace added for clarity):
 *
 * <pre><code>
 * {
 *   "people": [
 *     {
 *       "telephoneNumber": [
 *         "(555) 555-5555"
 *       ],
 *       "mail": [
 *         "amy.administrator@university.edu"
 *       ],
 *       "displayName": [
 *         "Amy Administrator"
 *       ],
 *       "givenName": [
 *         "Amy"
 *       ],
 *       "sn": [
 *         "Administrator"
 *       ],
 *       "title": [
 *         "Portal Administrator"
 *       ],
 *       "department": [
 *         "IT Services"
 *       ],
 *       "username": [
 *         "admin"
 *       ]
 *     },
 *     {
 *       "telephoneNumber": [
 *         "(555) 555-5555"
 *       ],
 *       "mail": [
 *         "samuel.staff@example.org"
 *       ],
 *       "displayName": [
 *         "Samuel Staff"
 *       ],
 *       "givenName": [
 *         "Samuel"
 *       ],
 *       "sn": [
 *         "Staff"
 *       ],
 *       "title": [
 *         "Database Administrator"
 *       ],
 *       "department": [
 *         "IT Services"
 *       ],
 *       "username": [
 *         "staff"
 *       ]
 *     }
 *   ],
 *   "portlets": [
 *     {
 *       "description": "Campus News",
 *       "fname": "campus-news",
 *       "name": "Campus News",
 *       "title": "Campus News",
 *       "url": "/uPortal/f/news-fav-collection/p/campus-news.u33l1n8/max/render.uP"
 *     },
 *     {
 *       "description": "Top stories from The Chronicle of Higher Education: Wired Campus Edition",
 *       "fname": "chronicle-wired",
 *       "name": "The Chronicle: Wired Campus",
 *       "title": "The Chronicle: Wired Campus",
 *       "url": "/uPortal/f/news-fav-collection/p/chronicle-wired.u33l1n9/max/render.uP"
 *     },
 *     {
 *       "description": "Calendar of campus, academic, and personal events.",
 *       "fname": "calendar",
 *       "name": "Calendar",
 *       "title": "Calendar",
 *       "url": "/uPortal/f/welcome/p/calendar.u17l1n13/max/render.uP"
 *     }
 *   ]
 * }
 * </code></pre>
 *
 * @since 5.0
 */
@Controller
@RequestMapping("/v5-0/portal/search")
public final class SearchRESTController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRESTController.class);

    @Autowired private ObjectMapper jsonMapper;

    @Autowired private IPersonManager personManager;

    private PersonLookupHelperImpl lookupHelper;

    @Autowired(required = true)
    public void setPersonLookupHelper(PersonLookupHelperImpl lookupHelper) {
        this.lookupHelper = lookupHelper;
    }

    @Resource(name = "directoryQueryAttributes")
    private List<String> directoryQueryAttributes;

    @Autowired private IPortletDefinitionRegistry portletDefinitionRegistry;

    @Autowired private PortletRegistryUtil portletRegistryUtil;

    @RequestMapping(method = RequestMethod.GET)
    public void search(
            @RequestParam("q") String query,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        List<Object> matchingPeople = getMatchingPeople(query, request);

        List<Object> matchingPortlets = getMatchingPortlets(query, request);

        if (matchingPeople.isEmpty() && matchingPortlets.isEmpty()) {
            logger.debug("Nothing found for query string: {}", query);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            Map<String, List<Object>> results = new TreeMap<>();
            results.put("people", matchingPeople);
            results.put("portlets", matchingPortlets);
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            jsonMapper.writeValue(response.getOutputStream(), results);
        }
    }

    private List<Object> getMatchingPortlets(String query, HttpServletRequest request) {
        List<Object> results = new ArrayList<>();

        final List<IPortletDefinition> portlets =
                portletDefinitionRegistry.getAllPortletDefinitions();
        for (IPortletDefinition portlet : portlets) {
            if (portletRegistryUtil.matches(query, portlet)) {
                /* requester permissions checked in buildPortletUrl() */
                String url = portletRegistryUtil.buildPortletUrl(request, portlet);
                if (url != null) {
                    results.add(getPortletAttrs(portlet, url));
                }
            }
        }
        return results;
    }

    private Map<String, String> getPortletAttrs(IPortletDefinition portlet, String url) {
        Map<String, String> attrs = new TreeMap<>();
        attrs.put("name", portlet.getName());
        attrs.put("fname", portlet.getFName());
        attrs.put("title", portlet.getTitle());
        attrs.put("description", portlet.getDescription());
        attrs.put("url", url);
        return attrs;
    }

    private List<Object> getMatchingPeople(String query, HttpServletRequest request) {
        List<Object> results = new ArrayList<>();

        final IPerson user = personManager.getPerson(request);

        Map<String, Object> queryPplAttrMap = new HashMap<>();
        for (String attr : directoryQueryAttributes) {
            queryPplAttrMap.put(attr, query);
        }

        List<IPersonAttributes> people = lookupHelper.searchForPeople(user, queryPplAttrMap);
        if (people != null) {
            for (IPersonAttributes p : people) {
                results.add(p.getAttributes());
            }
        }
        return results;
    }
}
