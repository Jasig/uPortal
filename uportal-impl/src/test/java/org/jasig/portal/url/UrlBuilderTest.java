/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UrlBuilderTest {

    @Test
    public void testInvalidEncoding() {
        try {
            new UrlBuilder("NOT VALID");
            Assert.fail("Encoding 'NOT VALID' should throw an exception");
        }
        catch (RuntimeException re) {
            //expected
        }
    }

    @Test
    public void testEmptyBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        final String url = builder.toString();
        Assert.assertEquals("/", url);
    }

    @Test
    public void testEmptyProtocolHostBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8", "http", "www.example.com");
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com", url);
    }

    @Test
    public void testEmptyProtocolHostPortBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8", "http", "www.example.com", 8080);
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com:8080", url);
    }

    @Test
    public void testParameterEmptyBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2");
        builder.addParameter("p2", (List<String>)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        final String url = builder.toString();
        Assert.assertEquals("/?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testParameterProtocolHostPortBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8", "http", "www.example.com", 8080);
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.setParameter("p0");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com:8080?p1=v1&p1=&p1=v2&p0=&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testParametersBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        
        final Map<String, List<String>> p0 = new LinkedHashMap<String, List<String>>();
        p0.put("notSeen", Arrays.asList("b", "c"));
        
        builder.setParameters(p0);
        builder.addParameters(p0);
        
        final Map<String, List<String>> p1 = new LinkedHashMap<String, List<String>>();
        p1.put("a", Arrays.asList("b", "c"));
        p1.put("b", Arrays.asList(null, "d"));
        p1.put("c", null);
        
        builder.setParameters(p1);
        
        builder.addParameters("uP_", p1);
        
        final String url = builder.toString();
        Assert.assertEquals("/?a=b&a=c&b=&b=d&c=&uP_a=b&uP_a=c&uP_b=&uP_b=d&uP_c=", url);
    }

    @Test
    public void testPathEmptyBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        
        builder.addPath("portal");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        final String url = builder.toString();
        Assert.assertEquals("/portal/home/normal/render.uP", url);
    }

    @Test
    public void testParameterPathEmptyBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        builder.setPath();
        builder.addPath("portal");
        builder.setPath("foo", "bar");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        final String url = builder.toString();
        Assert.assertEquals("/foo/bar/home/normal/render.uP?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testCloneParameterPathEmptyBuilder() {
        final UrlBuilder builder = new UrlBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        builder.setPath();
        builder.addPath("portal");
        builder.setPath("foo", "bar");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        
        final UrlBuilder builder2 = (UrlBuilder)builder.clone();
        Assert.assertEquals(builder, builder2);
        Assert.assertEquals(builder.hashCode(), builder2.hashCode());
        
        builder2.setPath();
        Assert.assertNotSame(builder, builder2);
        Assert.assertNotSame(builder.hashCode(), builder2.hashCode());
        
        final String url = builder.toString();
        Assert.assertEquals("/foo/bar/home/normal/render.uP?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
        
        final String url2 = builder2.toString();
        Assert.assertEquals("/?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url2);
    }
}
