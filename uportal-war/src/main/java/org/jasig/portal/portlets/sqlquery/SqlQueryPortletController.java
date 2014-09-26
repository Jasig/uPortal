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

package org.jasig.portal.portlets.sqlquery;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.sql.DataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.jasig.portal.jpa.BasePortalJpaDao;
import org.jasig.portal.portlet.IPortletSpELService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This portlet executes a (configurable) SQL query against a (configurable) DataSource accessed via the Spring
 * application context, translates the ResultSet into a collection of row Map objects, and feeds that object to
 * a (configurable) JSP page.
 *
 * The SQL Query can substitute attributes from the request, user attributes, or spring beans by using Spring
 * Expression Language (SpEL) patterns against the request object, user attributes, or a bean (@MyBeanName).
 * It's recommended to provide a default value using the Elvis operator (?:) in case the attribute is undefined
 * which results in no value. Some example queries using SpEL are:
 * <pre>
 * select * from EB_CONTACT_TABLE where pidm = ${userInfo['pidm']?:0} and standard_priority<>0 order by standard_priority
 * select * from UP_USER where  user_name='${userInfo['user.login.id']?:''}'
 * select '${@PortalDb.class.toString()}' as className from up_user where user_name='admin';
 * </pre>
 * 
 * This portlet is useful for exposing results of a simple DB query as a single page for users.
 *
 * @author Andrew Petro
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public class SqlQueryPortletController extends AbstractController {
    /**
     * The name of the cache to use for sql results.  Defaults to DEFAULT_CACHE_NAME.  User should set to empty
     * string to disable query results caching.  See portlet.xml.
     */
    public static final String PREF_CACHE_NAME = "cacheName";
    public static final String DEFAULT_CACHE_NAME = "org.jasig.portal.portlets.sqlquery.SqlQueryPortletController.queryResults";

    /**
     * True (default) to have sql results cached per-user if caching is enabled.
     */
    public static final String PREF_CACHE_PER_USER = "perUserCache";

    /**
     * The bean name of the DataSource against which this channel will
     * execute the SQL query is specified as a channel parameter named 
     * "dataSource".  This parameter is optional, defaulting to the uPortal 
     * DataSource (PortalDb).
     */
    public static final String DATASOURCE_BEAN_NAME_PARAM_NAME = "dataSource";

    /**
     * The SQL query this channel will execute is specified as a channel
     * parameter named "sql".  This parameter is required.
     */
    public static final String SQL_QUERY_PARAM_NAME = "sql";

    public static final String VIEW_PARAM_NAME = "view";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private IPortletSpELService portletSpELService;

    @Autowired
    public void setPortletSpELService(IPortletSpELService portletSpELService) {
            this.portletSpELService = portletSpELService;
        }

    public String getPrefCacheName() {
        return PREF_CACHE_NAME;
    }

    @Override
	public ModelAndView handleRenderRequest(RenderRequest request,
			RenderResponse response) throws Exception {
		
		// find the configured SQL statement
		PortletPreferences preferences = request.getPreferences();
		String sqlQuery = preferences.getValue(SQL_QUERY_PARAM_NAME, null);
		String dsName = preferences.getValue(DATASOURCE_BEAN_NAME_PARAM_NAME, BasePortalJpaDao.PERSISTENCE_UNIT_NAME);
		String viewName = preferences.getValue(VIEW_PARAM_NAME, "jsp/SqlQuery/results");

        List<Map<String, Object>> results = null;
        Cache cache = getCache(request);
        String cacheKey = null;
        if (cache != null) {
            cacheKey = createCacheKey(request, sqlQuery);
            Element cachedElement = cache.get(cacheKey);
            if (cachedElement != null) {
                log.debug("Cache hit. Returning item from cache {} for user {}, query {}", cache.getName(),
                        request.getRemoteUser(), sqlQuery);
                results = (List<Map<String, Object>>) cachedElement.getObjectValue();
            }
        }

        if (results == null) {
            // generate a JDBC template for the requested data source
            DataSource ds = (DataSource) getApplicationContext().getBean(dsName);
            JdbcTemplate template = new JdbcTemplate(ds);

            // Allow substituting attributes from the request and userInfo objects using the SPEL ${} notation..
            String spelSqlQuery = evaluateSpelExpression(sqlQuery, request);

            // Execute the SQL query and build a results object.  This result will consist of one
            // rowname -> rowvalue map for each row in the result set
            results = template.query(spelSqlQuery, new ColumnMapRowMapper());
            log.debug("found {} results for query {}", results.size(), spelSqlQuery);

            if (cache != null) {
                log.debug("Adding SQL results to cache {} for user {}, query {}", cache.getName(),
                        request.getRemoteUser(), sqlQuery);
                Element cachedElement = new Element(cacheKey, results);
                cache.put(cachedElement);

            }
        }
		
		// build the model

		ModelAndView modelandview = new ModelAndView(viewName);
		modelandview.addObject("results", results);
		return modelandview;
	}

    /**
     * Substitute any SpEL expressions with values from the PortletRequest and other attributes added to the
     * SpEL context.
     * @param value SQL Query String with optional SpEL expressions in it
     * @param request Portlet request
     * @return SQL Query string with SpEL substitutions
     */
    protected String evaluateSpelExpression(String value, PortletRequest request) {
        if (StringUtils.isNotBlank(value)) {
            String result = portletSpELService.parseString(value, request);
            return result;
        }
        throw new IllegalArgumentException("SQL Query expression required");
    }

    /**
     * Obtain the cache configured for this portlet instance.
     * @param req Portlet request
     * @return Cache configured for this portlet instance.
     */
    private Cache getCache(PortletRequest req) {
        String cacheName = req.getPreferences().getValue(PREF_CACHE_NAME, DEFAULT_CACHE_NAME);
        if (StringUtils.isNotBlank(cacheName)) {
            log.debug("Looking up cache '{}'", cacheName);
            Cache cache = CacheManager.getInstance().getCache(cacheName);
            if (cache == null) {
                throw new RuntimeException("Unable to find cache named " + cacheName + ". Check portlet preference value "
                        + PREF_CACHE_NAME + " and configuration in ehcache.xml");
            }
            return cache;
        } else {
            log.debug("Portlet preference {} set to empty string; disabling caching for this portlet instance",
                    PREF_CACHE_NAME);
            return null;
        }
    }

    /**
     * Create a cache key that includes the bean name and if configured the logged in userId.  This allows multiple
     * portlet instances to share the same cache (if desired but not recommended).
     * @param req Portlet request
     * @param rawSqlQuery SQL Query from portlet preferences (without SpEL substitution)
     * @return Generated Cache key
     */
    private String createCacheKey(PortletRequest req, String rawSqlQuery) {
        PortletPreferences prefs = req.getPreferences();
        boolean perUser = Boolean.valueOf(prefs.getValue(PREF_CACHE_PER_USER, "true"));
        String key = (perUser ? req.getRemoteUser() + "-": "") + rawSqlQuery + "-SqlQueryPortlet";
        log.debug("Using cache key {}", key);
        return key;
    }
}
