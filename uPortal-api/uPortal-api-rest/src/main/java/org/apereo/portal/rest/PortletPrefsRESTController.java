package org.apereo.portal.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.portlet.container.services.PortletPreferencesFactory;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.security.AuthorizationPrincipalHelper;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PortletPrefsRESTController provides REST targets for preference operations for soffits and
 * webcomponents.
 */
@Controller
public class PortletPrefsRESTController {

    protected final Log logger = LogFactory.getLog(getClass());
    private ObjectMapper mapper;

    private IPersonManager personManager;

    private final boolean DEFINITION_MODE = true;
    private final boolean ENTITY_MODE = false;

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    private IPortletDefinitionDao portletDao;

    @Autowired
    public void setPortletDao(IPortletDefinitionDao portletDao) {
        this.portletDao = portletDao;
    }

    private IPortletEntityRegistry portletEntityRegistry;

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    private PortletPreferencesFactory portletPreferencesFactory;

    @Autowired
    @Qualifier("portletPreferencesFactory")
    public void setPortletPreferencesFactoryAPI(
            PortletPreferencesFactory portletPreferencesFactory) {
        this.portletPreferencesFactory = portletPreferencesFactory;
    }

    public PortletPrefsRESTController() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Get uPortal Entity information for portlet/soffit/webcomponent. This method provides a REST
     * interface for obtaining uPortal portlet/soffit/webcomponent metadata
     *
     * <p>The path for this method is /prefs/getentity/fname. The fname is a string representing the
     * fname of the portlet/soffit/webcomponent to find the entity information for
     */
    /*non-javadoc
     * I used this primarily for testing, could be useful given that it returns the windowstate, etc
     * up to you if you want to keep it or modify it
     * */
    @RequestMapping(value = "/prefs/getentity/{fname}", method = RequestMethod.GET)
    public ResponseEntity getEntity(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to get entity for") @PathVariable(value = "fname")
                    String fname) {
        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{\"" + portletDefinition.getFName() + "\": {");
            stringBuilder.append(" \"layoutNodeID\": \"" + entity.getLayoutNodeId() + "\",");
            stringBuilder.append(" \"userID\": \"" + entity.getUserId() + "\",");
            stringBuilder.append(" \"entityID\": \"" + entity.getPortletEntityId() + "\",");
            stringBuilder.append(" \"windowStates\": [");
            boolean comma = false;
            for (Map.Entry<Long, WindowState> entry : entity.getWindowStates().entrySet()) {
                stringBuilder.append(" { \"stylesheetID\": \"");
                stringBuilder.append(entry.getKey());
                stringBuilder.append("\", \"windowState\": \"");
                stringBuilder.append(entry.getValue().toString());
                stringBuilder.append("\"},");
                comma = true;
            }
            if (comma) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            stringBuilder.append(" ] } }");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stringBuilder.toString());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get uPortal Preferecnces for portlet/soffit/webcomponent. This method provides a REST
     * interface for uPortal for obtaining the entity preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/getprefs/fname. The fname is a string representing the
     * fname of the portlet/soffit/webcomponent to return the composite entity preferences. These
     * are the per-user preferences for the given portlet/soffit/webcomponent including any
     * non-overwritten definition and descriptor preferences for the given
     * portlet/soffit/webcomponent. Basically every preference for the soffit with the current
     * user's preferences set. Will include extra preferences for soffits including the url, so
     * please set any of these to read-only
     */
    // returns the entity composite preferences for the portlet in question
    @RequestMapping(value = "/prefs/getprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getCompositePrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to find preferences for")
                    @PathVariable(value = "fname")
                    String fname) {

        try {

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());
            boolean config = ENTITY_MODE;
            PortletPreferences prefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, config);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(prefs.getMap()));

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {

            logger.error("threw error trying to retrieve composite preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get uPortal Preferecnces for portlet/soffit/webcomponent. This method provides a REST
     * interface for uPortal for obtaining only the entity preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/getprefs/fname. The fname is a string representing the
     * fname of the portlet/soffit/webcomponent to return the composite entity preferences. These
     * are the per-user preferences for the given portlet/soffit/webcomponent. Includes only the
     * user set preferences, does not include any defaults set in the definition
     */
    /*maybe get rid of this one? don't know if there is any specific use for it given that the preferences needed might be set in
     * any of the entity, definition, or descriptor preferences. But is useful if you only need the user set preferences and are
     * not relying on a default in the definition.
     */
    @RequestMapping(value = "/prefs/getentityonlyprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getEntityOnlyPrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to find preferences for")
                    @PathVariable(value = "fname")
                    String fname) {
        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            PortletPreferences defprefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, DEFINITION_MODE);
            PortletPreferences entprefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, ENTITY_MODE);
            Map<String, String[]> eprefs = getEntityOnly(entprefs.getMap(), defprefs.getMap());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(eprefs));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // get the entity preferences by comparing the entity with the defintion and returning the
    // entity ones that are different
    private Map<String, String[]> getEntityOnly(
            Map<String, String[]> entity, Map<String, String[]> definition) {
        Map<String, String[]> result = new HashMap<>();
        for (Map.Entry<String, String[]> entry : entity.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (!definition.containsKey(key)) {
                // the entity has something the definition does not
                result.put(key, value);
            } else {
                // they both have it so compare the two
                String[] defvalues = definition.get(key);
                if (value == null) {
                    if (defvalues != null) {
                        result.put(key, value);
                    } // otherwise they're both null so don't add
                } else {
                    if (!Arrays.equals(value, defvalues)) {
                        result.put(key, value);
                    } // otherwise they're the same so don't add
                }
            }
        }
        return result;
    }

    /**
     * Get uPortal definition preferecnces for portlet/soffit/webcomponent. This method provides a
     * REST interface for uPortal for obtaining the definition preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/getdefinitionprefs/fname. The fname is a string
     * representing the fname of the portlet/soffit/webcomponent to return the composite definition
     * preferences. These are the default preferences for the given portlet/soffit/webcomponent
     * including any non-overwritten descriptor preferences for the given
     * portlet/soffit/webcomponent. Will include extra preferences for soffits including the url, so
     * please set any of these to read-only when you define them. You must be logged in as a portal
     * administrator or have configuration rights for the given portlet/soffit/webcomponent in
     * uPortal to access these. Will return a 403 or 404 otherwise
     */
    @RequestMapping(value = "/prefs/getdefinitionprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getDefinitionPrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to find preferences for")
                    @PathVariable(value = "fname")
                    String fname) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if (person.isGuest()) {
            logger.warn("Guest user tried to access getDefinitionPrefs. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: guest cannot use this action.", HttpStatus.UNAUTHORIZED);
        } else if (!person.getSecurityContext().isAuthenticated()) {
            logger.warn(
                    "Person: {"
                            + person.getUserName()
                            + "} tried to access getDefinitionPrefs while not authenticated. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: must be logged in to use this action.", HttpStatus.UNAUTHORIZED);
        }

        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }

            if (!canConfigure(person, portletDefinition.getPortletDefinitionId())) {
                logger.warn(
                        "Person: {"
                                + person.getUserName()
                                + "} tried to access getDefinitionPrefs for portlet: {"
                                + fname
                                + "} without configuration permissions. Possible hacking attempt.");
                return new ResponseEntity<>(
                        "ERROR: user is not authorized to perform this action",
                        HttpStatus.UNAUTHORIZED);
            }

            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            PortletPreferences prefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, DEFINITION_MODE);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(prefs.getMap()));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get uPortal definition preferecnces for portlet/soffit/webcomponent. This method provides a
     * REST interface for uPortal for obtaining the definition preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/getdefinitiononlyprefs/fname. The fname is a string
     * representing the fname of the portlet/soffit/webcomponent to return the definition
     * preferences. These are the default preferences for the given portlet/soffit/webcomponent.
     * Will include extra preferences for soffits -including the url- so please set any of these to
     * read-only when you define them. You must be logged in as a portal administrator or have
     * configuration rights for the given portlet/soffit/webcomponent in uPortal to access these.
     * Will return a 403 or 404 otherwise
     */
    @RequestMapping(value = "/prefs/getdefinitiononlyprefs/{fname}", method = RequestMethod.GET)
    public ResponseEntity getDefinitionOnlyPrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to find preferences for")
                    @PathVariable(value = "fname")
                    String fname) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if (person.isGuest()) {
            logger.warn("Guest user tried to access getDefinitionPrefs. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: guest cannot use this action.", HttpStatus.UNAUTHORIZED);
        } else if (!person.getSecurityContext().isAuthenticated()) {
            logger.warn(
                    "Person: {"
                            + person.getUserName()
                            + "} tried to access getDefinitionPrefs while not authenticated. Possible hacking attempt.");
            return new ResponseEntity<>(
                    "ERROR: must be logged in to use this action.", HttpStatus.UNAUTHORIZED);
        }

        try {
            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }

            if (!canConfigure(person, portletDefinition.getPortletDefinitionId())) {
                logger.warn(
                        "Person: {"
                                + person.getUserName()
                                + "} tried to access getDefinitionPrefs for portlet: {"
                                + fname
                                + "} without configuration permissions. Possible hacking attempt.");
                return new ResponseEntity<>(
                        "ERROR: user is not authorized to perform this action",
                        HttpStatus.UNAUTHORIZED);
            }

            List<IPortletPreference> prefs = portletDefinition.getPortletPreferences();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(prefMap(prefs)));
        } catch (Exception e) {

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean canConfigure(IPerson user, IPortletDefinitionId portletDefinitionId) {
        final IAuthorizationPrincipal principal =
                AuthorizationPrincipalHelper.principalFromUser(user);
        return principal.canConfigure(portletDefinitionId.toString());
    }

    private Map<String, String[]> prefMap(List<IPortletPreference> prefs) {
        Map<String, String[]> map = new HashMap<>();
        for (IPortletPreference p : prefs) {
            map.put(p.getName(), p.getValues());
        }
        return map;
    }

    /**
     * Set uPortal definition preferecnces for portlet/soffit/webcomponent. This method provides a
     * REST interface for uPortal for obtaining the definition preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/putdefinitionprefs/fname. The fname is a string
     * representing the fname of the portlet/soffit/webcomponent to set definition preferences for.
     * These are the default preferences for the given portlet/soffit/webcomponent. This will set
     * the key:value(s) pairs provided into the defintion preferences overwriting any current
     * definition preferences with the same key, adding a new key:value(s) pair if the key does not
     * exits. You must be logged in as a portal administrator or have configuration rights for the
     * given portlet/soffit/webcomponent in uPortal to access these. Will return a 403 or 404
     * otherwise
     */
    @RequestMapping(value = "/prefs/putdefinitionprefs", method = RequestMethod.PUT)
    public ResponseEntity putDefinitionPrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to store preferences for")
                    @RequestParam(value = "fname", required = true)
                    String fname,
            @ApiParam(
                            value =
                                    "the entity preferences to be stored in a single json object ex. "
                                            + "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. "
                                            + "Values can be strings, booleans, numbers, null, or a jsonarray of values "
                                            + "(objects can be stored as a value as serialized strings). "
                                            + "This will overwrite any currently saved preferences for the portlet"
                                            + " with the same key as the pairs put in.")
                    @RequestBody
                    String body) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        final IPerson person = personManager.getPerson(request);
        if (person.isGuest()) {
            return new ResponseEntity<>(
                    "ERROR: guests may not use this action.", HttpStatus.FORBIDDEN);
        } else if (!person.getSecurityContext().isAuthenticated()) {
            return new ResponseEntity<>(
                    "ERROR: must be logged in to use this action.", HttpStatus.UNAUTHORIZED);
        }

        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            if (!canConfigure(person, entity.getPortletDefinitionId())) {
                return new ResponseEntity<>(
                        "ERROR: user is not authorized to perform this action",
                        HttpStatus.FORBIDDEN);
            }

            PortletPreferences prefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, DEFINITION_MODE);

            boolean stored = storePrefs(prefs, map);
            return ResponseEntity.ok().body(stored);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {

            logger.error("threw error trying to parse preferences", e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {

            logger.error("threw error trying to store preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Set uPortal entity preferecnces for portlet/soffit/webcomponent. This method provides a REST
     * interface for uPortal for obtaining the entity preferences for the
     * portlet/soffit/webcomponent
     *
     * <p>The path for this method is /prefs/putprefs/fname. The fname is a string representing the
     * fname of the portlet/soffit/webcomponent to set entity preferences for. These are the user
     * preferences for the given portlet/soffit/webcomponent. This will set the key:value(s) pairs
     * provided into the entity preferences overwriting any current entity preferences with the same
     * key, adding a new key:value(s) pair if the key does not exits.
     */
    @RequestMapping(value = "/prefs/putprefs", method = RequestMethod.PUT)
    public ResponseEntity putEntityPrefs(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(value = "The portlet fname to store preferences for")
                    @RequestParam(value = "fname", required = true)
                    String fname,
            @ApiParam(
                            value =
                                    "the entity preferences to be stored in a single json object ex. "
                                            + "{\"key1\":\"value1\",\"key2\":\"value2\", ...}. "
                                            + "Values can be strings, booleans, numbers, null, or a jsonarray of values "
                                            + "(objects can be stored as a value as serialized strings). "
                                            + "This will overwrite any currently saved preferences for the portlet"
                                            + " with the same key as the key passed in.")
                    @RequestBody
                    String body) {
        try {
            JsonNode map = mapper.readTree(body);

            IPortletDefinition portletDefinition = portletDao.getPortletDefinitionByFname(fname);
            if (portletDefinition == null) {
                return new ResponseEntity<>("ERROR: Portlet not found", HttpStatus.NOT_FOUND);
            }
            IPortletEntity entity =
                    portletEntityRegistry.getOrCreateDefaultPortletEntity(
                            request, portletDefinition.getPortletDefinitionId());

            PortletPreferences prefs =
                    portletPreferencesFactory.createAPIPortletPreferences(
                            request, entity, false, ENTITY_MODE);

            boolean stored = storePrefs(prefs, map);
            return ResponseEntity.ok().body(stored);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No portlet definition found for id '")) {
                String error = "ERROR: User not authorized to access portlet '" + fname + "'.";
                logger.info(error);
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
            }

            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (IOException e) {

            logger.error("threw error trying to parse user preferences", e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (PreferenceException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ReadOnlyException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {

            logger.error("threw error trying to store preferences", e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // process the incoming map to ensure validity then store
    private boolean storePrefs(PortletPreferences prefs, JsonNode map) throws Exception {
        if (prefs == null) {
            return false;
        }

        try {
            Iterator nodes = map.fields();
            Map<String, String[]> tempMap = new HashMap<>();
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
                String key = entry.getKey();
                JsonNode node = entry.getValue();
                if (node.isValueNode()) {
                    String[] temp = {node.asText()};
                    tempMap.put(key, temp);
                } else if (node.isArray()) {
                    List<String> array = new ArrayList<String>();
                    for (JsonNode innerNode : node) {
                        if (innerNode.isValueNode()) {
                            array.add(innerNode.asText());
                        } else {
                            throw new PreferenceException(
                                    "ERROR: preference arrays must only contain strings, numbers, booleans, or null");
                        }
                    }
                    tempMap.put(key, array.toArray(new String[array.size()]));
                } else {
                    throw new PreferenceException(
                            "ERROR: preferences must be strings, numbers, booleans, null, or arrays of strings, numbers, booleans, or nulls");
                }
            }
            if (tempMap.size() == 0) {
                throw new PreferenceException(
                        "ERROR: invalid json. json must be in key:value pairs.");
            }
            for (Map.Entry<String, String[]> entry : tempMap.entrySet()) {
                prefs.setValues(entry.getKey(), entry.getValue());
            }
            prefs.store();
            return true;

        } catch (Exception e) {
            throw e;
        }
    }

    private class PreferenceException extends Exception {

        public PreferenceException() {
            super();
        }

        public PreferenceException(String message) {
            super(message);
        }

        public PreferenceException(String message, Exception cause) {
            super(message, cause);
        }
    }
}
