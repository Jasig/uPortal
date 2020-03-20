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
package org.apereo.portal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.ResourceMissingException;
import org.apereo.portal.properties.PropertiesManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This utility provides methods for accessing resources. The methods generally use the classpath to
 * find the resource if the requested URL isn't already specified as a fully-qualified URL string.
 *
 * <p>The methods of this class sort of replace the old UtiltiesBean.fixURI() method.
 *
 * @since 2.0
 * @deprecated use CachingResourceLoader
 */
@Deprecated
public class ResourceLoader {

    private static final Log log = LogFactory.getLog(ResourceLoader.class);

    private static DocumentBuilderFactory validatingDocumentBuilderFactory;

    private static DocumentBuilderFactory nonValidatingDocumentBuilderFactory;

    private static Map<Tuple<Class<?>, String>, URL> resourceUrlCache;
    private static Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache;

    static {
        validatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        nonValidatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();

        validatingDocumentBuilderFactory.setValidating(true);
        nonValidatingDocumentBuilderFactory.setValidating(false);

        validatingDocumentBuilderFactory.setNamespaceAware(true);
        nonValidatingDocumentBuilderFactory.setNamespaceAware(true);

        try {
            String handler =
                    PropertiesManager.getProperty(
                            "org.apereo.portal.utils.ResourceLoader.HttpsHandler");
            if ((System.getProperty("java.protocol.handler.pkgs") != null)
                    && !(System.getProperty("java.protocol.handler.pkgs").equals(""))) {
                handler = handler + "|" + System.getProperty("java.protocol.handler.pkgs");
            }
            System.setProperty("java.protocol.handler.pkgs", handler);
        } catch (Exception e) {
            log.error("Unable to set HTTPS Protocol handler", e);
        }
    }

    /** @param resourceUrlCache the resourceUrlCache to set */
    public void setResourceUrlCache(Map<Tuple<Class<?>, String>, URL> resourceUrlCache) {
        ResourceLoader.resourceUrlCache = resourceUrlCache;
    }

    /** @param resourceUrlNotFoundCache the resourceUrlNotFoundCache to set */
    public void setResourceUrlNotFoundCache(
            Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache) {
        ResourceLoader.resourceUrlNotFoundCache = resourceUrlNotFoundCache;
    }

    /**
     * Finds a resource with a given name. This is a convenience method for accessing a resource
     * from a channel or from the uPortal framework. If a well-formed URL is passed in, this method
     * will use that URL unchanged to find the resource. If the URL is not well-formed, this method
     * will look for the desired resource relative to the classpath. If the resource name starts
     * with "/", it is unchanged. Otherwise, the package name of the requesting class is prepended
     * to the resource name.
     *
     * @param requestingClass the java.lang.Class object of the class that is attempting to load the
     *     resource
     * @param resource a String describing the full or partial URL of the resource to load
     * @return a URL identifying the requested resource
     */
    private static URL getResourceAsURL(Class<?> requestingClass, String resource)
            throws ResourceMissingException {
        final Tuple<Class<?>, String> cacheKey = new Tuple<>(requestingClass, resource);

        // Look for a cached URL
        final Map<Tuple<Class<?>, String>, URL> resourceUrlCache = ResourceLoader.resourceUrlCache;
        URL resourceURL = resourceUrlCache != null ? resourceUrlCache.get(cacheKey) : null;
        if (resourceURL != null) {
            return resourceURL;
        }

        // Look for a failed lookup
        final Map<Tuple<Class<?>, String>, ResourceMissingException> resourceUrlNotFoundCache =
                ResourceLoader.resourceUrlNotFoundCache;
        ResourceMissingException exception =
                resourceUrlNotFoundCache != null ? resourceUrlNotFoundCache.get(cacheKey) : null;
        if (exception != null) {
            throw new ResourceMissingException(exception);
        }

        try {
            resourceURL = new URL(resource);
        } catch (MalformedURLException murle) {
            // URL is invalid, now try to load from classpath
            resourceURL = requestingClass.getResource(resource);

            if (resourceURL == null) {
                String resourceRelativeToClasspath;
                if (resource.startsWith("/")) resourceRelativeToClasspath = resource;
                else
                    resourceRelativeToClasspath =
                            '/'
                                    + requestingClass.getPackage().getName().replace('.', '/')
                                    + '/'
                                    + resource;
                exception =
                        new ResourceMissingException(
                                resource,
                                resourceRelativeToClasspath,
                                "Resource not found in classpath: " + resourceRelativeToClasspath);
                if (resourceUrlNotFoundCache != null) {
                    resourceUrlNotFoundCache.put(cacheKey, exception);
                }
                throw new ResourceMissingException(exception);
            }
        }

        if (resourceUrlCache != null) {
            resourceUrlCache.put(cacheKey, resourceURL);
        }
        return resourceURL;
    }

    /**
     * Returns the requested resource as a stream.
     *
     * @param requestingClass the java.lang.Class object of the class that is attempting to load the
     *     resource
     * @param resource a String describing the full or partial URL of the resource to load
     * @return the requested resource as a stream
     */
    public static InputStream getResourceAsStream(Class<?> requestingClass, String resource)
            throws ResourceMissingException, IOException {
        return getResourceAsURL(requestingClass, resource).openStream();
    }

    /**
     * Get the contents of a URL as an XML Document
     *
     * @param requestingClass the java.lang.Class object of the class that is attempting to load the
     *     resource
     * @param resource a String describing the full or partial URL of the resource whose contents to
     *     load
     * @param validate boolean. True if the document builder factory should validate, false
     *     otherwise.
     * @return the actual contents of the resource as an XML Document
     */
    public static Document getResourceAsDocument(
            Class<?> requestingClass, String resource, boolean validate)
            throws ResourceMissingException, IOException, ParserConfigurationException,
                    SAXException {
        Document document;
        InputStream inputStream = null;

        try {

            DocumentBuilderFactory factoryToUse;

            if (validate) {
                factoryToUse = ResourceLoader.validatingDocumentBuilderFactory;
            } else {
                factoryToUse = ResourceLoader.nonValidatingDocumentBuilderFactory;
            }
            inputStream = getResourceAsStream(requestingClass, resource);
            DocumentBuilder db = factoryToUse.newDocumentBuilder();

            db.setEntityResolver(new DTDResolver());
            db.setErrorHandler(
                    new SAXErrorHandler("ResourceLoader.getResourceAsDocument(" + resource + ")"));
            document = db.parse(inputStream);
        } finally {
            if (inputStream != null) inputStream.close();
        }
        return document;
    }
}
