/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalException;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/** <p>A simple channel which renders an image along with an optional
 * caption and subcaption.</p>
 * <p>Channel parameters:</p>
 *   <table>
 *     <tr><th>Name</th><th>Description</th><th>Example</th><th>Required</th></tr>
 *     <tr><td>img-uri</td><td>The URI of the image to display</td><td>http://webcam.its.hawaii.edu/uhmwebcam/image01.jpg</td><td>yes</td></tr>
 *     <tr><td>img-width</td><td>The width of the image to display</td><td>320</td><td>no</td></tr>
 *     <tr><td>img-height</td><td>The height of the image to display</td><td>240</td><td>no</td></tr>
 *     <tr><td>img-border</td><td>The border of the image to display</td><td>0</td><td>no</td></tr>
 *     <tr><td>img-link</td><td>A URI to be used as an href for the image</td><td>http://www.hawaii.edu/visitor/#webcams</td><td>no</td></tr>
 *     <tr><td>caption</td><td>A caption of the image to display</td><td>Almost Live Shot of Hamilton Library Front Entrance</td><td>no</td></tr>
 *     <tr><td>subcaption</td><td>The subcaption of the image to display</td><td>Updated Once per Minute During Daylight Hours</td><td>no</td></tr>
 *     <tr><td>alt-text</td><td>Text to include as the 'alt' attribute of the img tag</td><td>Almost live shot of Hamilton library front enterance</td><td>no, but highly recommended in support of non-visual browsers</td></tr>
 *   </table>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CImage extends BaseChannel implements ICacheable, IChannel
{
    public static final String ALT_TEXT_CHANNEL_PARAM_NAME = "alt-text";


  private static final String sslLocation = "CImage/CImage.ssl";

  /**
   * Output channel content to the portal
   * @param out a sax content handler
   * @throws org.jasig.portal.PortalException
   */
  public void renderXML (ContentHandler out) throws PortalException
  {
    // Get the static data
    String sImageUri = staticData.getParameter ("img-uri");
    String sImageWidth = staticData.getParameter ("img-width");
    String sImageHeight = staticData.getParameter ("img-height");
    String sImageBorder = staticData.getParameter ("img-border");
    String sImageLink = staticData.getParameter ("img-link");
    String sCaption = staticData.getParameter ("caption");
    String sSubCaption = staticData.getParameter ("subcaption");
    String sAltText = staticData.getParameter(ALT_TEXT_CHANNEL_PARAM_NAME);

    Document doc = null;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException pce) {
      log.error("Error getting a Document", pce);
      throw new GeneralRenderingException(pce);
    }

    // Create XML doc
    Element contentE = doc.createElement("content");

    // Add image tag src, width, height, border, and link
    Element imageE = doc.createElement("image");
    imageE.setAttribute("src", sImageUri);
    if (exists(sImageWidth))
      imageE.setAttribute("width", sImageWidth);
    if (exists(sImageWidth))
      imageE.setAttribute("height", sImageHeight);
    if (exists(sImageWidth))
      imageE.setAttribute("border", sImageBorder);
    if (exists(sImageWidth))
      imageE.setAttribute("link", sImageLink);
    if (exists(sAltText)) {
        imageE.setAttribute("alt-text", sAltText);
    }
    contentE.appendChild(imageE);

    // Add a caption if it is specified
    if (exists(sCaption)) {
      Element captionE = doc.createElement("caption");
      captionE.appendChild(doc.createTextNode(sCaption));
      contentE.appendChild(captionE);
    }

    // Add a subcaption if it is specified
    if (exists(sSubCaption)) {
      Element subcaptionE = doc.createElement("subcaption");
      subcaptionE.appendChild(doc.createTextNode(sSubCaption));
      contentE.appendChild(subcaptionE);
    }

    doc.appendChild(contentE);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
    xslt.transform();
  }

  private static boolean exists (String s)
  {
    return (s != null && s.length () > 0);
  }

  // ICachable methods...

  public ChannelCacheKey generateKey() {
    ChannelCacheKey key = new ChannelCacheKey();
    key.setKey(getKey());
    key.setKeyScope(ChannelCacheKey.SYSTEM_KEY_SCOPE);
    key.setKeyValidity(null);
    return key;
  }

  public boolean isCacheValid(Object validity) {
    return true;
  }

  private String getKey() {
    StringBuffer sbKey = new StringBuffer(1024);
    sbKey.append("org.jasig.portal.channels.CImage").append(": ");
    sbKey.append("xslUri:");
    try {
      String sslUrl = ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation);
      sbKey.append(XSLT.getStylesheetURI(sslUrl, runtimeData.getBrowserInfo())).append(", ");
    } catch (PortalException pe) {
      sbKey.append("Not available, ");
    }
    sbKey.append("staticData:").append(staticData.toString());
    sbKey.append("locales:").append(LocaleManager.stringValueOf(runtimeData.getLocales()));

    return sbKey.toString();
  }
}
