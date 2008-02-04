/* Copyright 2001, 2005, 2007 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.LocalConnectionContext;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.cache.CacheFactory;
import org.jasig.portal.utils.cache.CacheFactoryLocator;
import org.jasig.portal.utils.uri.BlockedUriException;
import org.jasig.portal.utils.uri.IUriScrutinizer;
import org.jasig.portal.utils.uri.PrefixUriScrutinizer;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;

/**
 * <p>A channel which transforms XML for rendering in the portal.</p>
 *
 * <p>Static channel parameters to be supplied:
 * <ol>
 *  <li> "xmlUri" - a URI representing the source XML document</li>
 *  <li> "sslUri" - a URI representing the corresponding .ssl (stylesheet list) file</li>
 *  <li> "xslTitle" - a title representing the stylesheet (optional)
 *                  <i>If no title parameter is specified, a default
 *                  stylesheet will be chosen according to the media</i>
 *  </li>
 *  <li> "xslUri" - a URI representing the stylesheet to use
 *                  <i>If <code>xslUri</code> is supplied, <code>sslUri</code>
 *                  and <code>xslTitle</code> will be ignored.</i>
 *  </li>
 *  <li> "cacheTimeout" - the amount of time (in seconds) that the contents of the
 *                  channel should be cached (optional).  If this parameter is left
 *                  out, a default timeout value will be used.
 *  </li>
 *  <li> "cacheScope" - the desired scope for caching channel responses.  Defaults to
 *   "instance" indicating that cache is per-channel-instance-per-user.  Cache will
 *   not match across users.  This is typically desirable to prevent user A from
 *   seeing user B's data.  Alternately, this parameter may be set to "system"
 *   indicating that caching should occur across the entire portal
 *   such that two different users' instances of like-configured CGenericXSLT channels will share cache.
 *   </li>
 *  <li> "upc_localConnContext" - The class name of the ILocalConnectionContext
 *                  implementation.
 *                  <i>Use when local data needs to be sent with the
 *                  request for the URL.</i>
 *  </li>
 *  <li> "upc_allow_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String the allowable xmlUri prefixes.
 *                  <i>Defaults to "http:// https://"</i>
 *  </li>
 *  <li> "upc_deny_xmlUri_prefixes" - Optional parameter specifying as a whitespace
 *                  delimited String URI prefixes that should block a URI
 *                  as xmlUri even if it matched one of the allow prefixes.
 *                  <i>Defaults to ""</i>
 *  </li>
 *  <li> "restrict_xmlUri_inStaticData" - Optional parameter specifying whether
 *                  the xmlUri should be restricted according to the allow and
 *                  deny prefix rules above as presented in ChannelStaticData
 *                  or just as presented in ChannelRuntimeData.  "true" means
 *                  both ChannelStaticData and ChannelRuntimeData will be restricted.
 *                  Any other value or the parameter not being present means
 *                  only ChannelRuntimeData will be restricted.  It is important
 *                  to set this value to true when using subscribe-time
 *                  channel parameter configuration of the xmlUri.
 *  </li>
 *  <li>"cacheGlobalMode" - should content be cached globally
 *          		<i>May be <code>false</code> (normally don't globally cache), or
 *          		<code>true</code> (cache everything).</i>
 *  </li>
 * </ol>
 * </p>
 * <p>The xmlUri and xslTitle static parameters above can be overridden by including
 * parameters of the same name (<code>xmlUri</code> and/or <code>xslTitle</code>)
 * in the HttpRequest string.  Prior to uPortal 2.5.1 sslUri and xslUri could also
 * be overridden -- these features have been removed to improve the security of
 * CGenericXSLT instances. </p>
 * <p>
 * Additionally, as of uPortal 2.5.1, the xmlUri must match an allowed URI prefix.
 * By default http:// and https:// URIs are allowed.  If you are using the
 * empty document or another XML file from the classpath or from the filesystem,
 * you will need to allow a prefix to or the full path of that resource.
 * </p>
 * <p>This channel can be used for all XML formats including RSS.
 * Any other parameters passed to this channel via HttpRequest will get
 * passed in turn to the XSLT stylesheet as stylesheet parameters. They can be
 * read in the stylesheet as follows:
 * <code>&lt;xsl:param name="yourParamName"&gt;aDefaultValue&lt;/xsl:param&gt;</code></p>
 * <p>CGenericXSLT is also useful for channels that have no dynamic data.  In these types
 * of channels, all the markup comes from the XSLT stylesheets.  An empty XML document
 * can be used and is included with CGenericXSLT.  Just set the xml parameter to this
 * empty document and allow the path to the empty document.</p>
 * @author Steve Toth, stoth@interactivebusiness.com
 * @author Ken Weiner, kweiner@unicon.net
 * @author Peter Kharchenko pkharchenko@unicon.net
 * @version $Revision$
 */

public class CGenericXSLT
    extends BaseChannel
    implements IChannel, ICacheable {

	/**
	 * Added based on GCCC patch developed by Rutgers.
	 */
	private static final Map<Serializable, Document> contentCache;
	static {
        contentCache = CacheFactoryLocator.getCacheFactory().getCache(CacheFactory.CONTENT_CACHE);
	}
	private boolean cacheGlobalMode = PropertiesManager.getPropertyAsBoolean("org.jasig.portal.channels.CGenericXSLT.cache_global_mode", false);
	private long cacheContentLoaded;	// last time the content was loaded (instance shared by ALL users)

	private String xmlUri;
	private String sslUri;
	private String xslTitle;
	private String xslUri;
	private final Map params = new HashMap();
	private long cacheTimeout;
	private LocalConnectionContext localConnContext = null;

    /**
     * Either ChannelCacheKey.INSTANCE_KEY_SCOPE or ChannelCacheKey.SYSTEM_KEY_SCOPE.
     * Indicates cache key scope that should be used in fulfilling the ICacheable API.
     * Populated from 'cache-scope' channel parameter.
     */
    private int channelCacheScope = ChannelCacheKey.INSTANCE_KEY_SCOPE;

	private IUriScrutinizer uriScrutinizer;

	public CGenericXSLT()
	{

	}

	public void setStaticData (ChannelStaticData sd) throws ResourceMissingException
	{

		String allowXmlUriPrefixesParam =
			sd.getParameter("upc_allow_xmlUri_prefixes");
		String denyXmlUriPrefixesParam =
			sd.getParameter("upc_deny_xmlUri_prefixes");

		IUriScrutinizer temp =
			PrefixUriScrutinizer.instanceFromParameters(allowXmlUriPrefixesParam, denyXmlUriPrefixesParam);

		if (temp == null) {
			throw new IllegalArgumentException("CGenericXSLT channel requires a non-null IUriScrutinizer");
		}
		uriScrutinizer = temp;

		xmlUri = sslUri = xslTitle = xslUri = null;
		cacheTimeout = PropertiesManager.getPropertyAsLong("org.jasig.portal.channels.CGenericXSLT.default_cache_timeout");


		// determine whether we should restrict what URIs we accept as the xmlUri from
		// ChannelStaticData
		String scrutinizeXmlUriAsStaticDataString = sd.getParameter("restrict_xmlUri_inStaticData");
		boolean scrutinizeXmlUriAsStaticData = "true".equals(scrutinizeXmlUriAsStaticDataString);

		String xmlUriParam = sd.getParameter("xmlUri");
		if (scrutinizeXmlUriAsStaticData) {
			// apply configured xmlUri restrictions
			setXmlUri(xmlUriParam);
		} else {
			// set the field directly to avoid applying xmlUri restrictions
			xmlUri = xmlUriParam;
		}

		sslUri = sd.getParameter("sslUri");
		xslTitle = sd.getParameter("xslTitle");
		xslUri = sd.getParameter("xslUri");

		String cacheTimeoutText = sd.getParameter("cacheTimeout");

		if (cacheTimeoutText != null)
			cacheTimeout = Long.parseLong(cacheTimeoutText);

        String cacheScopeText = sd.getParameter(ICacheable.CHANNEL_CACHE_KEY_SCOPE_PARAM_NAME);

        if (cacheScopeText != null) {
            String trimmedCacheScopeText = cacheScopeText.trim();
            if (trimmedCacheScopeText.length() > 0) {
                if (trimmedCacheScopeText.equals(ICacheable.CHANNEL_CACHE_KEY_SYSTEM_SCOPE)) {
                    this.channelCacheScope = ChannelCacheKey.SYSTEM_KEY_SCOPE;
                } else if (trimmedCacheScopeText.equals(ICacheable.CHANNEL_CACHE_KEY_SYSTEM_SCOPE)) {
                    this.channelCacheScope = ChannelCacheKey.INSTANCE_KEY_SCOPE;
                } else {
                    log.warn("CGnericXSLT ChannelStaticData parameter [" + ICacheable.CHANNEL_CACHE_KEY_SYSTEM_SCOPE + "] had unrecognized value [" + trimmedCacheScopeText + "]; 'failing shut' with instance scoped caching.");
                    this.channelCacheScope = ChannelCacheKey.INSTANCE_KEY_SCOPE;
                }
            }
        }

        String gcm = sd.getParameter("cacheGlobalMode");
        	if (gcm != null) {
        		gcm = gcm.trim().toLowerCase();
        	if ("true".equals(gcm)) {
        		cacheGlobalMode = true;
        	} else if ("false".equals(gcm)) {
        		cacheGlobalMode = false;
        	} else {
        		if (log.isWarnEnabled()) {
        			log.warn("Invalid channel [" + sd.getChannelPublishId() + ":"
        				+ xmlUri + "] parameter cacheGlobalMode value ["
        				+ cacheGlobalMode + "]. Valid values are true|false. Defaulting to ["
        				+ cacheGlobalMode + "].");
        		}
        	}
        }

        String connContext = sd.getParameter ("upc_localConnContext");
		if (connContext != null && !connContext.trim().equals(""))
		{
			try
			{
				localConnContext = (LocalConnectionContext) Class.forName(connContext).newInstance();
				localConnContext.init(sd);
			}
			catch (Exception e)
			{
				log.error( "CGenericXSLT: Cannot initialize ILocalConnectionContext: "+connContext, e);
			}
		}
		params.putAll(sd);
	}

	public void setRuntimeData (ChannelRuntimeData rd)
	{
		// because of the portal rendering model, there is no reason to synchronize on state
		runtimeData=rd;
		String xmlUri = rd.getParameter("xmlUri");

		if (xmlUri != null)
			setXmlUri(xmlUri);

		// prior to uPortal 2.5.1 sslUri was configurable via ChannelRuntimeProperties
		// this feature has been removed to improve security of CGenericXSLT instances.

		String s = rd.getParameter("xslTitle");

		if (s != null)
			xslTitle = s;

		// grab the parameters and stuff them all into the state object
		Enumeration enum1 = rd.getParameterNames();
		while (enum1.hasMoreElements()) {
			String n = (String)enum1.nextElement();
			if (rd.getParameter(n) != null) {
				params.put(n,rd.getParameter(n));
			}
		}
	}

	public void renderXML(ContentHandler out) throws PortalException
	{
		if (log.isDebugEnabled())
			log.debug(this);

		// OK, pass everything we got cached in params...
		if (params != null) {
			Iterator it = params.keySet().iterator();
			while (it.hasNext()) {
				String n = (String) it.next();
				if (params.get((Object) n) != null) {
					runtimeData.put(n, params.get((Object) n));
				}
			}
		}

		// Optimized; cache retrieval of content between users
		Document xmlDoc = null;

		// global cache (i.e., share content between users) only if
		// caching is turned on
		boolean cacheit = !(cacheGlobalMode == false || cacheTimeout < 0 || localConnContext != null);

		if (cacheit) {
			xmlDoc = (Document) getCacheContent(xmlUri);
			if (log.isInfoEnabled()) {
				log.info("Global cache hit [" + (xmlDoc != null) + "] for channel: " + xmlUri);
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("Global cache not enabled for channel: " + xmlUri);
			}
		}

		// xmlDoc is *not* available from globalCache, need to retrieve...
		if (xmlDoc == null) {

			InputStream inputStream = null;
			try {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setNamespaceAware(true);
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				DTDResolver dtdResolver = new DTDResolver();
				docBuilder.setEntityResolver(dtdResolver);

				URL url;
				if (localConnContext != null)
					url = ResourceLoader.getResourceAsURL(this.getClass(), localConnContext.getDescriptor(xmlUri, runtimeData));
				else
					url = ResourceLoader.getResourceAsURL(this.getClass(), xmlUri);

				URLConnection urlConnect = url.openConnection();

				if (localConnContext != null) {
					try {
						localConnContext.sendLocalData(urlConnect, runtimeData);
					} catch (Exception e) {
						log.error( "CGenericXSLT: Unable to send data through " + runtimeData.getParameter("upc_localConnContext"), e);
					}
				}
				inputStream = urlConnect.getInputStream();
				xmlDoc = docBuilder.parse(inputStream);
			}
			catch (IOException ioe) {
				throw new ResourceMissingException (xmlUri, "", ioe);
			} catch (Exception e) {
				throw new GeneralRenderingException("Problem parsing " + xmlUri , e);
			} finally {
				try {
					if (inputStream != null) inputStream.close();
				} catch (IOException ioe) {
					throw new PortalException("CGenericXSLT:renderXML():: could not close InputStream", ioe);
				}
			}

			// Add the retrieved document to the globalCache...
			if (cacheit) {
				if (log.isInfoEnabled()) {
					log.info("Global cache store for channel: " + xmlUri);
				}
				setCacheContent(xmlUri, xmlDoc);
			}

		}

		runtimeData.put("baseActionURL", runtimeData.getBaseActionURL());
		runtimeData.put("isRenderingAsRoot", String.valueOf(runtimeData.isRenderingAsRoot()));

		// add version parameters (used in footer channel)
        VersionsManager versionsManager = VersionsManager.getInstance();
        Version[] versions = versionsManager.getVersions();

        for (Version version : versions) {
            String paramName = "version-" + version.getFname();
            runtimeData.setParameter(paramName, version.dottedTriple());
        }

        Version uPortalVersion = versionsManager.getVersion(IPermission.PORTAL_FRAMEWORK);

        // The "uP_productAndVersion" parameter is deprecated
        // instead use the "version-UP_FRAMEWORK" and other version parameters
        // generated immediately previously.
        runtimeData.put("uP_productAndVersion", "uPortal " + uPortalVersion.dottedTriple());

		// OK, pass everything we got cached in params...
		if (params != null)
		{
			Iterator it = params.keySet().iterator();
			while (it.hasNext()) {
				String n = (String)it.next();
				if (params.get((Object)n) != null) {
					runtimeData.put(n,params.get((Object)n));
				}
			}
		}

		XSLT xslt = XSLT.getTransformer(this);
		xslt.setXML(xmlDoc);
		if (xslUri != null)
			xslt.setXSL(xslUri);
		else
			xslt.setXSL(sslUri, xslTitle, runtimeData.getBrowserInfo());
		xslt.setTarget(out);
		xslt.setStylesheetParameters(runtimeData);
		xslt.transform();
	}

	public ChannelCacheKey generateKey()
	{
		ChannelCacheKey k = new ChannelCacheKey();
		k.setKey(getKey());
		k.setKeyScope(this.channelCacheScope);
		k.setKeyValidity(new Long(System.currentTimeMillis()));
		return k;
	}

	public boolean isCacheValid(Object validity)
	{
		if (!(validity instanceof Long))
			return false;

		if (System.currentTimeMillis() - ((Long)validity).longValue() > cacheTimeout*1000) {

			if (log.isInfoEnabled()) {
				log.info("Framework cache timeout: " + xmlUri);
			}
			return false;

		} else if (getCacheContentLoaded() > ((Long)validity).longValue()) {
			if (log.isInfoEnabled()) {
				log.info("Global cache timeout:"
						+ " >cacheContentLoaded: " + getCacheContentLoaded()
						+ " >validity: " + validity
						+ " >currentTime: " + System.currentTimeMillis()
						+ " >url: " + xmlUri);
			}
			return false;
		}

		return true;

	}

	private String getKey()
	{
		// Maybe not the best way to generate a key, but it seems to work.
		// If you know a better way, please change it!
		StringBuffer sbKey = new StringBuffer(1024);
		sbKey.append("xmluri:").append(xmlUri).append(", ");
		sbKey.append("sslUri:").append(sslUri).append(", ");

		// xslUri may either be specified as a parameter to this channel or we will
		// get it by looking in the stylesheet list file
		String xslUriForKey = xslUri;
		try {
			if (xslUriForKey == null) {
				String s = ResourceLoader.getResourceAsURLString(CGenericXSLT.class, sslUri);
				xslUriForKey = XSLT.getStylesheetURI(s, runtimeData.getBrowserInfo());
			}
		} catch (Exception e) {
			log.error(e,e);
			xslUriForKey = "Not attainable: " + e;
		}

		sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));
		sbKey.append("xslUri:").append(xslUriForKey).append(", ");
		sbKey.append("cacheTimeout:").append(cacheTimeout).append(", ");
		sbKey.append("isRenderingAsRoot:").append(runtimeData.isRenderingAsRoot()).append(", ");

		// If a local connection context is configured, include its descriptor in the key
		if (localConnContext != null)
			sbKey.append("descriptor:").append(localConnContext.getDescriptor(xmlUri, runtimeData)).append(", ");

		sbKey.append("params:").append(params.toString());
		return sbKey.toString();
	}

	/**
	 * Set the URI or resource-relative-path of the XML this CGenericXSLT should
	 * render.
	 * @param xmlUriArg URI or local resource path to the XML this channel should render.
	 * @throws IllegalArgumentException if xmlUriArg specifies a missing resource
	 * or if the URI has bad syntax
	 * @throws BlockedUriException if the xmlUriArg is blocked for policy reasons
	 */
	private void setXmlUri(String xmlUriArg) {
		URL url = null;
		try {
			url = ResourceLoader.getResourceAsURL(this.getClass(), xmlUriArg);
		} catch (ResourceMissingException e) {
			IllegalArgumentException iae = new IllegalArgumentException("Resource [" + xmlUriArg + "] missing.");
			iae.initCause(e);
			throw iae;
		}

		String urlString = url.toExternalForm();
		try {
			uriScrutinizer.scrutinize(new URI(urlString));
		}catch (URISyntaxException e1) {
			IllegalArgumentException iae2 = new IllegalArgumentException("xmlUri [" + xmlUriArg + "] resolved to a URI with bad syntax.");
			iae2.initCause(e1);
			throw iae2;
		}

		xmlUri = xmlUriArg;
	}

	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append("xmlUri = "+xmlUri+"\n");
		str.append("xslUri = "+xslUri+"\n");
		str.append("sslUri = "+sslUri+"\n");
		str.append("xslTitle = "+xslTitle+"\n");
		if (params != null) {
			str.append("params = "+params.toString()+"\n");
		}
		return str.toString();
	}

	/**
	 * Storing cache data is straigt-forward. We rely on the cache implementation
	 * to remove data as it ages AND only allow a maximum amount of data into
	 * the structure.
	 *
	 * @param key the URL key String
	 * @param data our cache data as a String or Document Object
	 */
	public synchronized void setCacheContent(String key, Document data) {
		contentCache.put(key, data);
		// we substract 2 seconds to the cache content loaded flag on purpose
		// this is due to the framework running in a different thread and
		// setting the validity of the content to a different time that
		// what we have. This ensures us that we will function properly
		cacheContentLoaded = System.currentTimeMillis() - 2000;
	}

	public synchronized Document getCacheContent(String key) {
		return (contentCache.get(key));
	}

	public synchronized long getCacheContentLoaded() {
		return (cacheContentLoaded);
	}

}
