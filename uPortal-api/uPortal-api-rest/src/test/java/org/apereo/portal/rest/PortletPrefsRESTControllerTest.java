package org.apereo.portal.rest;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.WindowState;
import org.apereo.portal.portlet.container.services.PortletPreferencesFactory;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

public class PortletPrefsRESTControllerTest {

    @InjectMocks private PortletPrefsRESTController portletPrefsRESTController;

    @Mock private IPersonManager personManager;

    @Mock private IPortletDefinitionDao portletDao;

    @Mock private IPortletEntityRegistry portletEntityRegistry;

    @Mock private PortletPreferencesFactory portletPreferencesFactory;

    private MockHttpServletRequest req;

    private MockHttpServletResponse res;

    private String non_existant_name = "non-existent-portlet";
    private String not_authorized_name = "not-authorized-portlet";
    private String valid_name = "bob-portlet";
    private IPortletDefinition valid_portletDefinitionMock = mock(PortletDefinitionImpl.class);
    private IPortletDefinitionId valid_portletDefIDMock = mock(IPortletDefinitionId.class);
    private IPortletEntity valid_entityMock = mock(IPortletEntity.class);
    private ObjectMapper mapper = new ObjectMapper();
    private IPerson valid_person = mock(IPerson.class, RETURNS_DEEP_STUBS);

    @Before
    public void setUp() throws Exception {

        portletPrefsRESTController = new PortletPrefsRESTController();
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        req.setSession(session);
        MockitoAnnotations.initMocks(this);

        // non-existant mocks
        Mockito.when(portletDao.getPortletDefinitionByFname(non_existant_name)).thenReturn(null);

        // not-authorized to subscribe mocks
        IPortletDefinitionId not_authorized_portletDefIDMock = mock(IPortletDefinitionId.class);
        IPortletDefinition not_authorized_portletDefinitionMock = mock(PortletDefinitionImpl.class);
        Mockito.when(portletDao.getPortletDefinitionByFname(not_authorized_name))
                .thenReturn(not_authorized_portletDefinitionMock);
        Mockito.when(not_authorized_portletDefinitionMock.getPortletDefinitionId())
                .thenReturn(not_authorized_portletDefIDMock);
        Mockito.when(
                        portletEntityRegistry.getOrCreateDefaultPortletEntity(
                                req, not_authorized_portletDefIDMock))
                .thenThrow(
                        new IllegalArgumentException("No portlet definition found for id '200'."));

        // authorized to subscribe common methods
        Mockito.when(portletDao.getPortletDefinitionByFname(valid_name))
                .thenReturn(valid_portletDefinitionMock);
        Mockito.when(valid_portletDefinitionMock.getPortletDefinitionId())
                .thenReturn(valid_portletDefIDMock);
        Mockito.when(
                        portletEntityRegistry.getOrCreateDefaultPortletEntity(
                                req, valid_portletDefIDMock))
                .thenReturn(valid_entityMock);

        Mockito.when(personManager.getPerson(req)).thenReturn(valid_person);
        Mockito.when(valid_person.isGuest()).thenReturn(false);
        Mockito.when(valid_person.getSecurityContext().isAuthenticated()).thenReturn(true);
    }

    @Test // test that it returns the correct json when the fname is present in the database
    public void getEntity() {

        String valid_layoutid = "bobLayoutNode";
        String valid_entityid = "bobEntityID";
        int userid = 2;

        // test specific mocks
        IPortletEntityId entityIDMock = mock(IPortletEntityId.class);
        Map<Long, WindowState> windowStateMock = new HashMap<>();
        windowStateMock.put(new Long(90), new WindowState("bob-state"));
        // test specific mock methods
        Mockito.when(valid_portletDefinitionMock.getFName()).thenReturn(valid_name);
        Mockito.when(valid_entityMock.getLayoutNodeId()).thenReturn(valid_layoutid);
        Mockito.when(valid_entityMock.getUserId()).thenReturn(userid);
        Mockito.when(valid_entityMock.getPortletEntityId()).thenReturn(entityIDMock);
        Mockito.when(entityIDMock.toString()).thenReturn(valid_entityid);
        Mockito.when(valid_entityMock.getWindowStates()).thenReturn(windowStateMock);

        // test specific json result
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"" + valid_name + "\": {");
        stringBuilder.append(" \"layoutNodeID\": \"" + valid_layoutid + "\",");
        stringBuilder.append(" \"userID\": \"" + userid + "\",");
        stringBuilder.append(" \"entityID\": \"" + valid_entityid + "\",");
        stringBuilder.append(" \"windowStates\": [");
        boolean comma = false;
        for (Map.Entry<Long, WindowState> entry : windowStateMock.entrySet()) {
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

        ResponseEntity response = portletPrefsRESTController.getEntity(req, res, valid_name);
        Assert.assertEquals(200, response.getStatusCodeValue());

        Assert.assertEquals(stringBuilder.toString(), (String) response.getBody());
    }

    @Test // test that it returns a 400 when the portlet fname does not exits
    public void getEntityNonExistent() {
        ResponseEntity response = portletPrefsRESTController.getEntity(req, res, non_existant_name);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getEntityNotAuthorized() {

        ResponseEntity response =
                portletPrefsRESTController.getEntity(req, res, not_authorized_name);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + not_authorized_name + "'.",
                (String) response.getBody());
    }

    @Test
    public void getCompositePrefsNonExistent() {

        ResponseEntity response =
                portletPrefsRESTController.getCompositePrefs(req, res, non_existant_name);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getCompositePrefsNotAuthorized() {

        ResponseEntity response =
                portletPrefsRESTController.getCompositePrefs(req, res, not_authorized_name);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + not_authorized_name + "'.",
                (String) response.getBody());
    }

    @Test
    public void getCompositePrefs() {

        PortletPreferences prefs = mock(PortletPreferences.class);
        Map<String, String[]> map = new HashMap<>();
        String[] pref1 = {"bob1", "bob2", "bob3"};
        String[] pref2 = {"true"};
        map.put("pref1", pref1);
        map.put("pref2", pref2);
        JsonNode jsonmap = mapper.valueToTree(map);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);
        Mockito.when(prefs.getMap()).thenReturn(map);

        ResponseEntity response =
                portletPrefsRESTController.getCompositePrefs(req, res, valid_name);
        Assert.assertEquals(200, response.getStatusCodeValue());
        JsonNode body = null;
        try {
            body = mapper.readTree((String) response.getBody());
        } catch (IOException e) {
            fail("failed to parse the json from the api: " + e.getMessage());
        }
        Assert.assertTrue(
                "Json is not equal, \nExpected: "
                        + jsonmap.toString()
                        + "\nActual: "
                        + body.toString(),
                jsonmap.equals(body));
    }

    @Test
    public void getEntityOnlyPrefsNonExistant() {
        ResponseEntity response =
                portletPrefsRESTController.getEntityOnlyPrefs(req, res, non_existant_name);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getEntityOnlyPrefsNotAuthorized() {

        ResponseEntity response =
                portletPrefsRESTController.getEntityOnlyPrefs(req, res, not_authorized_name);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + not_authorized_name + "'.",
                (String) response.getBody());
    }

    @Test
    public void getEntityOnlyPrefs() {
        PortletPreferences defPrefs = mock(PortletPreferences.class);
        PortletPreferences entPrefs = mock(PortletPreferences.class);

        Map<String, String[]> defMap = new HashMap<>();
        String[] pref1 = {"bob1", "bob2", "bob3"};
        String[] pref2 = {"true"};
        defMap.put("pref1", pref1);
        defMap.put("pref2", pref2);

        Map<String, String[]> entMap = new HashMap<>();
        entMap.put("pref1", pref1);
        entMap.put("pref2", pref2);
        String[] newPref2 = {"false"};
        entMap.put("pref2", newPref2);

        Map<String, String[]> entOnlyMap = new HashMap<>();
        entOnlyMap.put("pref2", newPref2);

        JsonNode jsonmap = mapper.valueToTree(entOnlyMap);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, true))
                .thenReturn(defPrefs);
        Mockito.when(defPrefs.getMap()).thenReturn(defMap);
        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(entPrefs);
        Mockito.when(entPrefs.getMap()).thenReturn(entMap);

        ResponseEntity response =
                portletPrefsRESTController.getEntityOnlyPrefs(req, res, valid_name);
        Assert.assertEquals(200, response.getStatusCodeValue());
        JsonNode body = null;
        try {
            body = mapper.readTree((String) response.getBody());
        } catch (IOException e) {
            fail("failed to parse the json from the api: " + e.getMessage());
        }
        Assert.assertTrue(
                "Json is not equal, \nExpected: "
                        + jsonmap.toString()
                        + "\nActual: "
                        + body.toString(),
                jsonmap.equals(body));
    }

    @Test
    public void getDefinitionPrefsNoSession() {
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setSession(null);

        ResponseEntity response =
                portletPrefsRESTController.getDefinitionPrefs(req2, res, valid_name);
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getDefinitionPrefsGuest() {
        IPerson person = mock(IPerson.class);
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        req2.setSession(session);
        Mockito.when(personManager.getPerson(req2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(true);

        ResponseEntity response =
                portletPrefsRESTController.getDefinitionPrefs(req2, res, valid_name);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: guest cannot use this action.", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNotAuthenticated() {
        IPerson person = mock(IPerson.class, RETURNS_DEEP_STUBS);
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "false");
        req2.setSession(session);
        Mockito.when(personManager.getPerson(req2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(false);
        Mockito.when(person.getSecurityContext().isAuthenticated()).thenReturn(false);

        ResponseEntity response =
                portletPrefsRESTController.getDefinitionPrefs(req2, res, valid_name);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: must be logged in to use this action.", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNonExistant() {

        ResponseEntity response =
                portletPrefsRESTController.getDefinitionPrefs(req, res, non_existant_name);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void getDefinitionPrefsSessionNonConfig() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void getDefinitionPrefs() {
        // can't progamatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putDefinitionPrefsNoSession() {
        String body = "{\"pref1\":\"value1\"}";
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setSession(null);

        ResponseEntity response =
                portletPrefsRESTController.putDefinitionPrefs(req2, res, valid_name, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void putDefinitionPrefsGuest() {
        String body = "{\"pref1\":\"value1\"}";
        IPerson person = mock(IPerson.class);
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "true");
        req2.setSession(session);
        Mockito.when(personManager.getPerson(req2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(true);

        ResponseEntity response =
                portletPrefsRESTController.putDefinitionPrefs(req2, res, valid_name, body);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: guests may not use this action.", (String) response.getBody());
    }

    @Test
    public void putDefinitionPrefsSessionNotAuthenticated() {
        String body = "{\"pref1\":\"value1\"}";
        IPerson person = mock(IPerson.class, RETURNS_DEEP_STUBS);
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session", "false");
        req2.setSession(session);
        Mockito.when(personManager.getPerson(req2)).thenReturn(person);
        Mockito.when(person.isGuest()).thenReturn(false);
        Mockito.when(person.getSecurityContext().isAuthenticated()).thenReturn(false);

        ResponseEntity response =
                portletPrefsRESTController.putDefinitionPrefs(req2, res, valid_name, body);
        Assert.assertEquals(401, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: must be logged in to use this action.", (String) response.getBody());
    }

    @Test
    public void putDefinitionPrefsSessionNonExistant() {
        String body = "{\"pref1\":\"value1\"}";
        ResponseEntity response =
                portletPrefsRESTController.putDefinitionPrefs(req, res, non_existant_name, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void putDefinitionInvalidJson() throws Exception {
        String body = "{\"pref1\":\"bob\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, true))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putDefinitionPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putDefinitionPrefsSessionNonConfig() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putDefinitionPrefs() {
        // can't programatically test this one without setting up a full stack since it calls the
        // static method:
        //      AuthorizationPrincipalHelper.principalFromUser(user)
        // to insure that only portal administrators or others with configuration authority can
        // configure.
        // *Could easily be implemented by using PowerMock
        // has been thouroughly tested through manual testing though
    }

    @Test
    public void putEntityPrefsNonExistant() {
        String body = "{\"pref1\":\"bob\"}";
        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, non_existant_name, body);
        Assert.assertEquals(404, response.getStatusCodeValue());
        Assert.assertEquals("ERROR: Portlet not found", (String) response.getBody());
    }

    @Test
    public void putEntityPrefsNotAuthorized() {
        String body = "{\"pref1\":\"bob\"}";
        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, not_authorized_name, body);
        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: User not authorized to access portlet '" + not_authorized_name + "'.",
                (String) response.getBody());
    }

    @Test
    public void putEntityPrefsInvalidJson() throws Exception {
        String body = "{\"pref1\":\"bob\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsObjectPref() throws Exception {
        String body = "{\"pref1\":{\"name\":\"bob\"}}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preferences must be strings, numbers, booleans, null, or arrays of strings, numbers, booleans, or nulls",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsMultipleArray() throws Exception {
        String body = "{\"pref1\":[\"bob\",[\"name\",\"bob\"]]}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preference arrays must only contain strings, numbers, booleans, or null",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsObjectArray() throws Exception {
        String body = "{\"pref1\":[{\"bob\":[\"name\",\"bob\"]}]}";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: preference arrays must only contain strings, numbers, booleans, or null",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefsReadOnly() throws Exception {
        String body = "{\"pref1\":\"bob\"}";
        String[] values = {"bob"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        Mockito.doThrow(new ReadOnlyException("Preference '" + "pref1" + "' is read only"))
                .when(prefs)
                .setValues("pref1", values);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals("Preference 'pref1' is read only", (String) response.getBody());
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test // regression test. found it returned true even though it stored nothing. make sure it
    // rejects non-key-value-pairs
    public void putEntityPrefsSingleValue() throws Exception {
        String body = "\"string\"";
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertEquals(
                "ERROR: invalid json. json must be in key:value pairs.",
                (String) response.getBody());
        Mockito.verify(prefs, times(0)).setValues(anyString(), any());
        Mockito.verify(prefs, times(0)).store();
    }

    @Test
    public void putEntityPrefs() throws Exception {
        String body = "{\"pref1\":\"bob\"}";
        String[] values = {"bob"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(true, (boolean) response.getBody());
    }

    @Test
    public void putEntityPrefsArray() throws Exception {
        String body = "{\"pref1\":[\"bob\",\"joe\"]}";
        String[] values = {"bob", "joe"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(true, (boolean) response.getBody());
    }

    @Test
    public void putEntityPrefsAllTypes() throws Exception {
        String body = "{\"pref1\":[\"bob\",true,false,null,1,50.9]}";
        String[] values = {"bob", "true", "false", "null", "1", "50.9"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values);
        Mockito.verify(prefs, times(1)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(true, (boolean) response.getBody());
    }

    @Test
    public void putEntityPrefsMultiplePrefs() throws Exception {
        String body = "{\"pref1\":[\"bob\",\"joe\"],\"pref2\":true}";
        String[] values1 = {"bob", "joe"};
        String[] values2 = {"true"};
        PortletPreferences prefs = mock(PortletPreferences.class);

        Mockito.when(
                        portletPreferencesFactory.createAPIPortletPreferences(
                                req, valid_entityMock, false, false))
                .thenReturn(prefs);

        ResponseEntity response =
                portletPrefsRESTController.putEntityPrefs(req, res, valid_name, body);
        Mockito.verify(prefs, times(1)).setValues("pref1", values1);
        Mockito.verify(prefs, times(1)).setValues("pref2", values2);
        Mockito.verify(prefs, times(2)).setValues(anyString(), any());
        Mockito.verify(prefs, times(1)).store();
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(true, (boolean) response.getBody());
    }
}
