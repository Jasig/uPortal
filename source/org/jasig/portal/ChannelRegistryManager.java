/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal;

import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.StatsRecorder;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.XML;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IUpdatingPermissionManager;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Text;
import java.util.Set;
import java.util.Date;
import java.util.Iterator;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xpath.XPathAPI;
import org.apache.xerces.dom.DocumentImpl;

/**
 * Manages the channel registry which is a listing of published channels
 * that one can subscribe to (add to their layout).
 * Also currently manages the channel types data and CPD documents.
 * (maybe these should be managed by another class  -Ken)
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelRegistryManager {
  protected static final IChannelRegistryStore crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();

  // Cache timeout properties
  protected static final int registryCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_registry_cache_timeout");
  protected static final int chanTypesCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.channel_types_cache_timeout");
  protected static final int cpdCacheTimeout = PropertiesManager.getPropertyAsInt("org.jasig.portal.ChannelRegistryManager.cpd_cache_timeout");

  // Caches
  protected static final SmartCache channelRegistryCache = new SmartCache(registryCacheTimeout);
  protected static final SmartCache channelTypesCache = new SmartCache(chanTypesCacheTimeout);
  protected static final SmartCache cpdCache = new SmartCache(cpdCacheTimeout);

  // Cache keys
  private static final String CHANNEL_REGISTRY_CACHE_KEY = "channelRegistryCacheKey";
  private static final String CHANNEL_TYPES_CACHE_KEY = "channelTypesCacheKey";
  private static final String CPD_CACHE_KEY = "cpdCacheKey";

  // Permission constants
  private static final String FRAMEWORK_OWNER = "UP_FRAMEWORK";
  private static final String SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
  private static final String GRANT_PERMISSION_TYPE = "GRANT";

  /**
   * Returns a copy of the channel registry as a Document.
   * This document is not filtered according to a user's channel permissions.
   * For a filtered list, see  <code>getChannelRegistry(IPerson person)</code>
   * @return a copy of the channel registry as a Document
   */
  public static Document getChannelRegistry() throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    if (channelRegistry == null) {
      // Channel registry has expired, so get it and cache it
      try {
        channelRegistry = getChannelRegistryXML();
      } catch (Exception e) {
        throw new PortalException(e);
      }

      if (channelRegistry != null) {
        channelRegistryCache.put(CHANNEL_REGISTRY_CACHE_KEY, channelRegistry);
        LogService.instance().log(LogService.INFO, "Caching channel registry.");
      }
    }

    // Clone the original registry document so that it doesn't get modified
    return XML.cloneDocument((DocumentImpl)channelRegistry);
  }

  /**
   * Returns the channel registry as a Document.  This document is filtered
   * according to a user's channel permissions.
   * @return the filtered channel registry as a Document
   */
  public static Document getChannelRegistry(IPerson person) throws PortalException {
    Document channelRegistry = getChannelRegistry();

    // Filter the channel registry according to permissions
    EntityIdentifier ei = person.getEntityIdentifier();
    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());

    // Cycle through all the channels, looking for restricted channels
    NodeList nl = channelRegistry.getElementsByTagName("channel");
    for (int i = (nl.getLength()-1); i >=0; i--) {
      Element channel = (Element)nl.item(i);
      String channelPublishId = channel.getAttribute("chanID");
      channelPublishId = channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId;

      // Take out channels which user doesn't have access to
      if (!ap.canSubscribe(Integer.parseInt(channelPublishId)))
        channel.getParentNode().removeChild(channel);
    }

    return channelRegistry;
  }

  /**
   * Returns an XML document which describes the channel registry.
   * See uPortal's <code>channelRegistry.dtd</code>
   * @return doc the channel registry document
   * @throws java.lang.Exception
   */
  public static Document getChannelRegistryXML() throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element registry = doc.createElement("registry");
    doc.appendChild(registry);

    IEntityGroup channelCategoriesGroup = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
    processGroupsRecursively(channelCategoriesGroup, registry);

    return doc;
  }

  private static void processGroupsRecursively(IEntityGroup group, Element parentGroup) throws Exception {
    Date now = new Date();
    Document registryDoc = parentGroup.getOwnerDocument();
    Iterator iter = group.getMembers();
    while (iter.hasNext()) {
      IGroupMember member = (IGroupMember)iter.next();
      if (member.isGroup()) {
        IEntityGroup memberGroup = (IEntityGroup)member;
        String key = memberGroup.getKey();
        String name = memberGroup.getName();
        String description = memberGroup.getDescription();

        // Create category element and append it to its parent
        Element categoryE = registryDoc.createElement("category");
        categoryE.setAttribute("ID", "cat" + key);
        categoryE.setAttribute("name", name);
        categoryE.setAttribute("description", description);
        parentGroup.appendChild(categoryE);
        processGroupsRecursively(memberGroup, categoryE);
      } else {
        IEntity channelDefMember = (IEntity)member;
        int channelPublishId = Integer.parseInt(channelDefMember.getKey());
        ChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
        if (channelDef != null) {
          // Make sure channel is approved
          Date approvalDate = channelDef.getApprovalDate();
          if (approvalDate != null && approvalDate.before(now)) {
            Element channelDefE = channelDef.getDocument(registryDoc, "chan" + channelPublishId);
            channelDefE = (Element)registryDoc.importNode(channelDefE, true);
            parentGroup.appendChild(channelDefE);
          }
        }
      }
    }
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param channelPublishId the channel publish id
   * @return the channel element matching specified channel publish id
   * @throws PortalException
   */
  public static Element getChannel (String channelPublishId) throws PortalException {
    Document channelRegistry = getChannelRegistry();
    Element channelE = null;
    try {
      // This is unfortunately dependent on Xalan 2.  Is there a way to use a standard interface?
      channelE = (Element)XPathAPI.selectSingleNode(channelRegistry, "(//channel[@ID = '" + channelPublishId + "'])[1]");
    } catch (javax.xml.transform.TransformerException te) {
      throw new GeneralRenderingException("Not able to find channel " + channelPublishId + " within channel registry: " + te.getMessageAndLocation());
    }
    return channelE;
  }

  /**
   * Create XML representing this channel definition.
   * I don't think this method really belongs in the
   * ChannelRegistryManager since this XML fragment is
   * related more to a channel instance, but we'll hold
   * it here for now and find a better place for it later :)
   * @param subscribeId, the channel subscibe ID, formerly called instance ID
   * @param channelDef a channel definition
   * @return channelXML, the XML representing this channel definition
   */
  public static Element getChannelXML(String subscribeId, ChannelDefinition channelDef) {
    Document doc = DocumentFactory.getNewDocument();
    Element channelE = doc.createElement("channel");
    channelE.setAttribute("ID", subscribeId);
    channelE.setAttribute("chanID", String.valueOf(channelDef.getId()));
    channelE.setAttribute("timeout", String.valueOf(channelDef.getTimeout()));
    channelE.setAttribute("name", channelDef.getName());
    channelE.setAttribute("title", channelDef.getTitle());
    channelE.setAttribute("fname", channelDef.getFName());
    channelE.setAttribute("class", channelDef.getJavaClass());
    channelE.setAttribute("typeID", String.valueOf(channelDef.getTypeId()));
    channelE.setAttribute("editable", channelDef.isEditable() ? "true" : "false");
    channelE.setAttribute("hasHelp", channelDef.hasHelp() ? "true" : "false");
    channelE.setAttribute("hasAbout", channelDef.hasAbout() ? "true" : "false");

    // Add any parameters
    ChannelParameter[] parameters = channelDef.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      ChannelParameter cp = parameters[i];
      Element parameterE = doc.createElement("parameter");
      parameterE.setAttribute("name", cp.getName());
      parameterE.setAttribute("value", cp.getValue());
      if (cp.getOverride()) {
        parameterE.setAttribute("override", "yes");
      }
      channelE.appendChild(parameterE);
    }

    return channelE;
  }

  /**
   * Update a channel definition with data from a channel XML
   * element.  I don't think this method really belongs in the
   * ChannelRegistryManager since this XML fragment contains
   * a channel subscribe ID, but we'll hold it here for now
   * and find a better place for it later :)
   * Note that this method does not set the ID, publisher ID,
   * approver ID, pubish date, or approval date.
   * @param channelE, an XML element representing a channel definition
   * @param channelDef, the channel definition to update
   */
  public static void setChannelXML(Element channelE, ChannelDefinition channelDef) {
    channelDef.setFName(channelE.getAttribute("fname"));
    channelDef.setName(channelE.getAttribute("name"));
    channelDef.setDescription(channelE.getAttribute("description"));
    channelDef.setTitle(channelE.getAttribute("title"));
    channelDef.setJavaClass(channelE.getAttribute("class"));

    String timeout = channelE.getAttribute("timeout");
    if (timeout != null && timeout.trim().length() != 0) {
      channelDef.setTimeout(Integer.parseInt(timeout));
    }

    channelDef.setTypeId(Integer.parseInt(channelE.getAttribute("typeID")));
    String chanEditable = channelE.getAttribute("editable");
    String chanHasHelp = channelE.getAttribute("hasHelp");
    String chanHasAbout = channelE.getAttribute("hasAbout");
    channelDef.setEditable(chanEditable != null && chanEditable.equals("true") ? true : false);
    channelDef.setHasHelp(chanHasHelp != null && chanHasHelp.equals("true") ? true : false);
    channelDef.setHasAbout(chanHasAbout != null && chanHasAbout.equals("true") ? true : false);

    // Now set the channel parameters
    NodeList channelChildren = channelE.getChildNodes();
    if (channelChildren != null) {
      for (int i = 0; i < channelChildren.getLength(); i++) {
        if (channelChildren.item(i).getNodeName().equals("parameter")) {
          Element parameterE = (Element)channelChildren.item(i);
          NamedNodeMap parameterAtts = parameterE.getAttributes();
          String paramName = null;
          String paramValue = null;
          String paramOverride = "NULL";

          for (int j = 0; j < parameterAtts.getLength(); j++) {
            Node parameterAtt = parameterAtts.item(j);
            String parameterAttName = parameterAtt.getNodeName();
            String parameterAttValue = parameterAtt.getNodeValue();

            if (parameterAttName.equals("name")) {
              paramName = parameterAttValue;
            } else if (parameterAttName.equals("value")) {
              paramValue = parameterAttValue;
            } else if (parameterAttName.equals("override") && parameterAttValue.equals("yes")) {
              paramOverride = "Y";
            }
          }

          if (paramName == null && paramValue == null) {
            throw new RuntimeException("Invalid parameter node");
          }

          channelDef.addParameter(paramName, paramValue, paramOverride);
        }
      }
    }
  }

  /**
   * Create XML representing the channel types.
   * It will look something like this:
   * <p><code>
   *
   *<channelTypes>
   *  <channelType ID="0">
   *    <class>org.jasig.portal.channels.CImage</class>
   *    <name>Image</name>
   *    <description>Simple channel to display an image with optional
   *        caption and subcaption</description>
   *    <cpd-uri>webpages/media/org/jasig/portal/channels/CImage/CImage.cpd</cpd-uri>
   *  </channelType>
   *  <channelType ID="1">
   *    <class>org.jasig.portal.channels.CWebProxy</class>
   *    <name>Web Proxy</name>
   *    <description>Incorporate a dynamic HTML or XML application</description>
   *    <cpd-uri>webpages/media/org/jasig/portal/channels/CWebProxy/CWebProxy.cpd</cpd-uri>
   *  </channelType>
   *</channelTypes>
   *
   * </code></p>
   * @return channelTypesXML, the XML representing the channel types
   * @throws java.lang.Exception
   */
  public static Document getChannelTypesXML() throws Exception {
    Document doc = DocumentFactory.getNewDocument();
    Element channelTypesE = doc.createElement("channelTypes");

    ChannelType[] channelTypes = crs.getChannelTypes();
    for (int i = 0; i < channelTypes.length; i++) {
      int channelTypeId = channelTypes[i].getId();
      String javaClass = channelTypes[i].getJavaClass();
      String name = channelTypes[i].getName();
      String descr = channelTypes[i].getDescription();
      String cpdUri = channelTypes[i].getCpdUri();

      // <channelType>
      Element channelTypeE = doc.createElement("channelType");
      channelTypeE.setAttribute("ID", String.valueOf(channelTypeId));

      // <class>
      Element classE = doc.createElement("class");
      classE.appendChild(doc.createTextNode(javaClass));
      channelTypeE.appendChild(classE);

      // <name>
      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(name));
      channelTypeE.appendChild(nameE);

      // <description>
      Element descriptionE = doc.createElement("description");
      descriptionE.appendChild(doc.createTextNode(descr));
      channelTypeE.appendChild(descriptionE);

      // <cpd-uri>
      Element cpdUriE = doc.createElement("cpd-uri");
      cpdUriE.appendChild(doc.createTextNode(cpdUri));
      channelTypeE.appendChild(cpdUriE);

      channelTypesE.appendChild(channelTypeE);
    }
    doc.appendChild(channelTypesE);

    return doc;
  }

  /**
   * Looks in channel registry for a channel element matching the
   * given channel ID.
   * @param channelPublishId the channel publish ID
   * @return the channel element matching chanID
   * @throws org.jasig.portal.PortalException
   */
  public static NodeList getCategories(String channelPublishId) throws PortalException {
    Document channelRegistry = (Document)channelRegistryCache.get(CHANNEL_REGISTRY_CACHE_KEY);
    NodeList categories = null;
    try {
      // This is unfortunately dependent on Xalan 2.  Is there a way to use a standard interface?
      categories = (NodeList)XPathAPI.selectNodeList(channelRegistry, "//category[channel/@ID = '" + channelPublishId + "']");
    } catch (javax.xml.transform.TransformerException te) {
      throw new GeneralRenderingException("Not able to find channel " + channelPublishId + " within channel registry: " + te.getMessageAndLocation());
    }
    return categories;
  }

  /**
   * Publishes a channel.
   * @param channel the channel XML fragment
   * @param categoryIDs a list of categories that the channel belongs to
   * @param groupMembers a list of groups and/or people that are permitted to subscribe to and view the channel
   * @param publisher the user ID of the channel publisher
   * @throws java.lang.Exception
   */
  public static void publishChannel (Element channel, String[] categoryIDs, IGroupMember[] groupMembers, IPerson publisher) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);

    ChannelDefinition channelDef = null;

    // Use current channel ID if modifying previously published channel, otherwise get a new ID
    boolean newChannel = true;
    int ID = 0;
    String channelPublishId = channel.getAttribute("ID");
    if (channelPublishId != null && channelPublishId.trim().length() > 0) {
      newChannel = false;
      ID = Integer.parseInt(channelPublishId.startsWith("chan") ? channelPublishId.substring(4) : channelPublishId);
      channelDef = crs.getChannelDefinition(ID);
      LogService.instance().log(LogService.DEBUG, "Attempting to modify channel " + ID + "...");
    }
    else {
      channelDef = crs.newChannelDefinition();
      ID = channelDef.getId();
      LogService.instance().log(LogService.DEBUG, "Attempting to publish new channel " + ID + "...");
    }

    // Add channel
    setChannelXML(channel, channelDef);
    channelDef.setPublisherId(publisher.getID());
    channelDef.setApproverId(-1);
    crs.saveChannelDefinition(channelDef);

    // Delete existing category memberships for this channel
    String channelDefEntityKey = String.valueOf(channelDef.getId());
    IEntity channelDefEntity = GroupService.getEntity(channelDefEntityKey, ChannelDefinition.class);
    IEntityGroup topLevelCategory = GroupService.getDistinguishedGroup(GroupService.CHANNEL_CATEGORIES);
    Iterator iter = topLevelCategory.getAllMembers();
    while (iter.hasNext()) {
      IGroupMember groupMember = (IGroupMember)iter.next();
      if (groupMember.isGroup()) {
        IEntityGroup group = (IEntityGroup)groupMember;
        group.removeMember(channelDefEntity);
        group.updateMembers();
      }
    }

    // For each category ID, add channel to category
    for (int i = 0; i < categoryIDs.length; i++) {
      categoryIDs[i] = categoryIDs[i].startsWith("cat") ? categoryIDs[i].substring(3) : categoryIDs[i];

      /*
// de 11/21/02:
      String catKey = GroupService.parseLocalKey(categoryIDs[i]);
      int iCatID = Integer.parseInt(catKey);
//    int iCatID = Integer.parseInt(categoryIDs[i]);
  */
      String iCatID = categoryIDs[i];
      ChannelCategory category = crs.getChannelCategory(iCatID);
      crs.addChannelToCategory(channelDef, category);
    }

    // Set groups
    AuthorizationService authService = AuthorizationService.instance();
    String target = "CHAN_ID." + ID;
    IUpdatingPermissionManager upm = authService.newUpdatingPermissionManager(FRAMEWORK_OWNER);
    IPermission[] permissions = new IPermission[groupMembers.length];
    for (int i = 0; i < groupMembers.length; i++) {
      IAuthorizationPrincipal authPrincipal = authService.newPrincipal(groupMembers[i]);
      permissions[i] = upm.newPermission(authPrincipal);
      permissions[i].setType(GRANT_PERMISSION_TYPE);
      permissions[i].setActivity(SUBSCRIBER_ACTIVITY);
      permissions[i].setTarget(target);
    }

    // If modifying the channel, remove the existing permissions before adding the new ones
    if (!newChannel) {
      IPermission[] oldPermissions = upm.getPermissions(SUBSCRIBER_ACTIVITY, target);
      upm.removePermissions(oldPermissions);
    }
    upm.addPermissions(permissions);

    // Approve channel - this can be removed when there is a mechanism to approve channels
    crs.approveChannelDefinition(channelDef, publisher, new Date(System.currentTimeMillis()));

    LogService.instance().log(LogService.INFO, "Channel " + ID + " has been " + (newChannel ? "published" : "modified") + ".");

    // Record that a channel has been published or modified
    if (newChannel)
      StatsRecorder.recordChannelDefinitionPublished(publisher, channelDef);
    else
      StatsRecorder.recordChannelDefinitionModified(publisher, channelDef);
  }

  /**
   * Removes a channel from the channel registry.
   * @param channel ID, the channel ID
   * @param person, the person removing the channel
   * @throws java.lang.Exception
   */
  public static void removeChannel (String channelID, IPerson person) throws Exception {
    // Reset the channel registry cache
    channelRegistryCache.remove(CHANNEL_REGISTRY_CACHE_KEY);
    // Remove the channel
    String sChannelPublishId = channelID.startsWith("chan") ? channelID.substring(4) : channelID;
    int channelPublishId = Integer.parseInt(sChannelPublishId);
    ChannelDefinition channelDef = crs.getChannelDefinition(channelPublishId);
    crs.disapproveChannelDefinition(channelDef);

    // Record that a channel has been deleted
    StatsRecorder.recordChannelDefinitionRemoved(person, channelDef);
  }

  /**
   * Returns the publishable channel types as a Document.
   * @return a list of channel types as a Document
   */
  public static Document getChannelTypes() throws PortalException {
    Document channelTypes = (Document)channelTypesCache.get(CHANNEL_TYPES_CACHE_KEY);
    if (channelTypes == null)
    {
      // Channel types doc has expired, so get it and cache it
      try {
        channelTypes = getChannelTypesXML();
      } catch (Exception e) {
        throw new PortalException(e);
      }

      if (channelTypes != null)
      {
        channelTypesCache.put(CHANNEL_TYPES_CACHE_KEY, channelTypes);
        LogService.instance().log(LogService.INFO, "Caching channel types.");
      }
    }

    // Clone the original channel types document so that it doesn't get modified
    return XML.cloneDocument((DocumentImpl)channelTypes);
  }

  /**
   * Returns a CPD (channel publishing document) as a Document
   * @param chanTypeID the channel type ID, "-1" if channel type is "custom"
   * @return the CPD document matching the chanTypeID, <code>null</code> if "custom" channel
   * @throws org.jasig.portal.PortalException
   */
  public static Document getCPD(String chanTypeID) throws PortalException {
    //  There are no CPD docs for custom channels (chanTypeID = -1)
    if (chanTypeID == null || chanTypeID.equals("-1"))
      return null;

    Document cpd = (Document)cpdCache.get(CPD_CACHE_KEY + chanTypeID);
    if (cpd == null) {
      // CPD doc has expired, so get it and cache it
      Element channelTypes = getChannelTypes().getDocumentElement();

      // Look for channel type element matching the channel type ID
      Element chanType = null;

      for (Node n = channelTypes.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("channelType")) {
          chanType = (Element)n;
          if (chanTypeID.equals(chanType.getAttribute("ID")))
            break;
        }
      }

      // Find the cpd-uri within this element
      String cpdUri = null;
      for (Node n = chanType.getLastChild(); n != null; n = n.getPreviousSibling()) {
        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("cpd-uri")) {
          // Found the <cpd-uri> element, now get its value
          for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
            if (m instanceof Text)
              cpdUri = m.getNodeValue();
          }
          break;
        }
      }

      if (cpdUri != null) {
        try {
          cpd = ResourceLoader.getResourceAsDocument(ChannelRegistryManager.class, cpdUri);
        } catch (java.io.IOException ioe) {
          throw new ResourceMissingException(cpdUri, "Channel publishing document", ioe.getMessage());
        } catch (org.xml.sax.SAXException se) {
          throw new PortalException("Unable to parse CPD file: " + cpdUri, se);
        } catch (ParserConfigurationException pce) {
          throw new PortalException("Unable to parse CPD file: " + cpdUri, pce);
        }
      }

      if (cpd != null) {
        cpdCache.put(CPD_CACHE_KEY + chanTypeID, cpd);
        LogService.instance().log(LogService.INFO, "Caching CPD for channel type " + chanTypeID);
      }
    }

    // Clone the original CPD document so that it doesn't get modified
    return XML.cloneDocument((DocumentImpl)cpd);
  }
}






