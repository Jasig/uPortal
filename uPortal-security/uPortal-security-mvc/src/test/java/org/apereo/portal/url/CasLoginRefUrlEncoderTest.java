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
package org.apereo.portal.url;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** Some Testing on CasLoginRefUrlEncoder class. */
public class CasLoginRefUrlEncoderTest {

    CasLoginRefUrlEncoder encoder;

    @Before
    public void setUp() throws UnsupportedEncodingException {
        encoder = new CasLoginRefUrlEncoder();
        encoder.setCasLoginUrl(
                "https://cas:80/cas/login?service=https://testhost:8080/uPortal/Login");
        UrlMultiServerNameCustomizer urlCustomizer = new UrlMultiServerNameCustomizer();
        urlCustomizer.setAllServerNames(Sets.newHashSet("myhost:8080", "theirhost:8443"));
        List<IAuthUrlCustomizer> customizers = new ArrayList<>();
        customizers.add(urlCustomizer);
        UrlAuthCustomizerRegistry registry = new UrlAuthCustomizerRegistry();
        registry.setRegistry(customizers);
        encoder.setUrlCustomizer(registry);
    }

    @Test
    public void testSimpleDestination() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setCharacterEncoding("UTF-8");
        request.setServerName("myhost");
        request.setServerPort(8080);
        request.addHeader("Host", "myhost:8080");
        assertEquals(
                "https://cas:80/cas/login?service=https://myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname",
                encoder.encodeLoginAndRefUrl(request));
    }

    @Test
    public void testWithParam() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setQueryString("pP_announcementId=1834");
        request.setCharacterEncoding("UTF-8");
        assertEquals(
                "https://cas:80/cas/login?service=https://myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname%253FpP_announcementId%253D1834",
                encoder.encodeLoginAndRefUrl(request));
    }

    @Test
    public void testWithTwoParams() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://myhost:8080/uPortal/p/fname");
        request.setQueryString("pP_action=displayFullAnnouncement&pP_announcementId=1834");
        request.setCharacterEncoding("UTF-8");
        assertEquals(
                "https://cas:80/cas/login?service=https://myhost:8080/uPortal/Login%3FrefUrl%3Dhttp%3A%2F%2Fmyhost%3A8080%2FuPortal%2Fp%2Ffname%253FpP_action%253DdisplayFullAnnouncement%2526pP_announcementId%253D1834",
                encoder.encodeLoginAndRefUrl(request));
    }
}
