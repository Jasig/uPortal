/**
 * Copyright � 2002 The JA-SIG Collaborative.  All Nrights reserved.
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

package org.jasig.portal.channels.UserPreferences;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.IUserLayoutManager;
import org.jasig.portal.UserLayoutManager;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.UserProfile;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.StructureAttributesIncorporationFilter;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.utils.SmartCache;
import org.jasig.portal.services.LogService;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.StylesheetSet;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.utils.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.traversal.NodeIterator;

/**
 * This user preferences component is for use with layouts based
 * on the tab-column structure.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class TabColumnPrefsState extends BaseState
{
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;
  private static final String sslLocation = "/org/jasig/portal/channels/CUserPreferences/tab-column/tab-column.ssl";
  private Document userLayout;
  private PortalControlStructures pcs;
  private UserPreferences userPrefs;
  private UserProfile editedUserProfile;
  private static IUserLayoutStore ulStore = UserLayoutStoreFactory.getUserLayoutStoreImpl();
  private StylesheetSet set;

  private String action = "none";
  private String activeTab = "none";
  private String elementID = "none";

  // These can be overridden in a sub-class.
  protected static String BLANK_TAB_NAME = "My Tab"; // The tab will take on this name if left blank by the user
  protected static String SKIN_LIST_FILE = "media/org/jasig/portal/layout/tab-column/nested-tables/skinList.xml";

  // Here are all the possible error messages for this channel. Maybe these should be moved to
  // a properties file or static parameters.  Actually, the error handling written so far isn't
  // very good and should be improved.  For example, there needs to be a way to let a user know that
  // he/she couldn't remove a tab because it was set as unremovable.
  private String errorMessage = "Nothing is wrong!";
  private static final String errorMessageSetActiveTab = "Problem trying to set the active tab";
  private static final String errorMessageRenameTab = "Problem trying to rename tab";
  private static final String errorMessageMoveTab = "Problem trying to move the tab";
  private static final String errorMessageAddTab = "Problem trying to add a new tab";
  private static final String errorMessageDeleteTab = "Problem trying to delete tab";
  private static final String errorMessageLockTab = "Problem trying to lock tab";
  private static final String errorMessageUnlockTab = "Problem trying to unlock tab";
  private static final String errorMessageChangeColumnWidths = "Problem changing column widths";
  private static final String errorMessageMoveColumn = "Problem trying to move column";
  private static final String errorMessageNewColumn = "Problem trying to add a new column";
  private static final String errorMessageDeleteColumn = "Problem trying to delete column";
  private static final String errorMessageNewChannel = "Problem trying to add a new channel";
  private static final String errorMessageModChannelParams = "Problem trying to modify channel parameters";
  private static final String errorMessageMoveChannel = "Problem trying to move channel";
  private static final String errorMessageDeleteChannel = "Problem trying to delete channel";

  public TabColumnPrefsState() throws PortalException
  {
    super();
    this.internalState = new DefaultState(this);

    // initialize stylesheet set
    set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation));
  }

  public TabColumnPrefsState(CUserPreferences context) throws PortalException
  {
    super(context);
    this.internalState = new DefaultState(this);
    // initialize stylesheet set
    set = new StylesheetSet(ResourceLoader.getResourceAsURLString(this.getClass(), sslLocation));
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException
  {
    this.staticData = sd;
    this.internalState.setStaticData(sd);
  }

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
  {
    this.runtimeData = rd;

    // See if a top-level action has been given
    String action=rd.getParameter("action");
    if (action != null) {
      if (action.equals("newChannel")) {
        if (!(internalState instanceof NewChannelState)) {
          internalState = new NewChannelState(this);
          internalState.setStaticData(staticData);
        }
      } else if (action.equals("manageSkins")) {
        if (!(internalState instanceof SelectSkinsState)) {
          internalState = new SelectSkinsState(this);
          internalState.setStaticData(staticData);
        }
      } else if (action.equals("managePreferences")) {
        internalState = new DefaultState(this);
        internalState.setStaticData(staticData);
      }
    }
    internalState.setRuntimeData(rd);

    try
    {
      // The profile the user is currently viewing or modifying...
      editedUserProfile = context.getEditedUserProfile();
      userLayout = getUserLayout();
      userPrefs = context.getUserPreferencesFromStore(editedUserProfile);
    }
    catch (Exception e)
    {
      throw new GeneralRenderingException(e.getMessage());
    }
  }
  
  public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException  {
    this.pcs = pcs;
  }

  public void renderXML(ContentHandler out) throws PortalException
  {
    if (this.internalState != null)
      this.internalState.renderXML(out);
    else
      LogService.instance().log(LogService.ERROR, "TabColumnPrefsState::renderXML() : No internal state!");
  }

  // Helper methods...

  private final Document getUserLayout() throws Exception
  {
    IUserLayoutManager ulm = context.getUserLayoutManager();
    // If the we are editing the current profile, get a copy of the current user layout,
    // otherwise get it from the database or other persistant storage
    Document userLayout = null;
    if (modifyingCurrentProfile())
      userLayout = ulm.getUserLayoutCopy();
    else
      userLayout = ulStore.getUserLayout(ulm.getPerson(), context.getCurrentUserPreferences().getProfile());

    return userLayout;
  }

  private final String getActiveTab()
  {
    String activeTab = "none";

    try
    {
      // Get the profile associated with the layout currently being modified
      UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
      activeTab = userPrefsFromStore.getStructureStylesheetUserPreferences().getParameterValue("activeTab");
    }
    catch (Exception e)
    {
      LogService.instance().log(LogService.ERROR, "TabColumnPrefsState::getAcctiveTab : Unable to retrieve active tab.");
    }

    return activeTab;
  }

  private final void setActiveTab(String activeTab) throws Exception
  {
    // Must get from store because the one in memory is comtaminated with stylesheet params
    // that shouldn't get persisted
    //UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    //StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    ssup.putParameterValue("activeTab", activeTab);

    // Persist structure stylesheet user preferences
    int profileId = editedUserProfile.getProfileId();
    ulStore.setStructureStylesheetUserPreferences(staticData.getPerson(), profileId, ssup);
  }

  private final void renameTab(String tabId, String tabName) throws Exception
  {
    Element tab = userLayout.getElementById(tabId);

    if (tab.getAttribute("immutable").equals("false")) {
      // Ensure that tabs have a name or else you can't select them!
      if (tabName != null && tabName.trim().length() == 0)
        tabName = BLANK_TAB_NAME;
      tab.setAttribute("name", tabName);
    } else {
      throw new Exception("Attempt to rename immutable tab " + tabId + "has failed");
    }

    saveLayout(false);
  }

  private final void moveTab(String sourceTabId, String method, String destinationTabId) throws Exception
  {
    Element sourceTab = userLayout.getElementById(sourceTabId);
    Element destinationTab = userLayout.getElementById(destinationTabId);
    Element layout = userLayout.getDocumentElement();
    Node siblingTab = method.equals("insertBefore") ? destinationTab : null;
    UserLayoutManager.moveNode(sourceTab, layout, siblingTab);

    saveLayout(false);
  }

  /**
   * Adds a new tab to the layout.
   * @param tabName the name of the tab
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationTabId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void addTab(String tabName, String method, String destinationTabId) throws Exception
  {
    // Ensure that tabs have a name or else you can't select them!
    if (tabName != null && tabName.trim().length() == 0)
      tabName = BLANK_TAB_NAME;

    Element newTab = createFolder(tabName);
    Element destinationTab = userLayout.getElementById(destinationTabId);

    Node parent = null;
    if ( destinationTab != null )
      parent = destinationTab.getParentNode();
    else
      parent = userLayout.getDocumentElement();

    Element siblingTab = method.equals("insertBefore") ? destinationTab : null;
    UserLayoutManager.moveNode(newTab, parent, siblingTab);

    saveLayout(false);
  }

  /**
   * Adds a new column into the layout.  Before the column is added,
   * a check is done to see whether the destination element is a tab.  If it is,
   * a new column is inserted first.
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationElementId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void addColumn(String method, String destinationElementId) throws Exception
  {
    Element newColumn = createFolder("");
    Element destinationFolder = userLayout.getElementById(destinationElementId);

    // Insert a column if the destination element is a tab
    if (isTab(destinationFolder))
    {
      Element aColumn = createFolder("");
      UserLayoutManager.moveNode(aColumn, destinationFolder, null);
      destinationFolder = aColumn;
    }

    Node parent = destinationFolder.getParentNode();
    Node siblingFolder = method.equals("insertBefore") ? destinationFolder : null;
    UserLayoutManager.moveNode(newColumn, parent, siblingFolder);

    saveLayout(false);
  }

  private final void changeColumnWidths(HashMap columnWidths) throws Exception
  {
    // Must get from store because the one in memory is comtaminated with stylesheet params
    // that shouldn't get persisted
    //UserPreferences userPrefsFromStore = context.getUserPreferencesFromStore(context.getCurrentUserPreferences().getProfile());
    //StructureStylesheetUserPreferences ssup = userPrefsFromStore.getStructureStylesheetUserPreferences();
    StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
    java.util.Set sColWidths = columnWidths.keySet();
    java.util.Iterator iterator = sColWidths.iterator();
    while(iterator.hasNext())
    {
      String folderId = (String)iterator.next();
      String newWidth = (String)columnWidths.get(folderId);

      // Only accept widths that are either percentages or integers (fixed widths)
      boolean widthIsValid = true;
      try
      {
        Integer.parseInt(newWidth.endsWith("%") ? newWidth.substring(0, newWidth.indexOf("%")) : newWidth);
      }
      catch (java.lang.NumberFormatException nfe)
      {
        widthIsValid = false;
      }

      if (widthIsValid)
        ssup.setFolderAttributeValue(folderId, "width", newWidth);
      else
        LogService.instance().log(LogService.DEBUG, "User id " + staticData.getPerson().getID() + " entered invalid column width: " + newWidth);

    }

    // Persist structure stylesheet user preferences
    saveUserPreferences();
  }

  /**
   * Moves a column from one position in the layout to another.  Before the move is performed,
   * a check is done to see whether the source and/or destination elements are tabs.  If either
   * is a tab, a new column is inserted between it and the channels that it contains before the
   * move is carried out.
   * @param sourceId the column to move (may actually be a tab)
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationId the column to insert the new column before or append after (may actually be a tab)
   * @throws Exception
   */
  private final void moveColumn(String sourceId, String method, String destinationId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();

    Element source = userLayout.getElementById(sourceId);
    Element destination = userLayout.getElementById(destinationId);
    Element sourceColumn = source;
    Element destinationColumn = destination;

    // If source is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the source
    if (isTab(source))
    {
      sourceColumn = createFolder("");
      NodeList channels = source.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = UserLayoutManager.moveNode(channel, sourceColumn, null);
        // Not done yet: Need to deal with case when move isn't successful!!!
      }

      source.appendChild(sourceColumn);
    }

    // If destination is a tab, create a column, move the tab's children channels to this column,
    // and use this new column as the destination
    if (isTab(destination))
    {
      destinationColumn = createFolder("");
      NodeList channels = destination.getElementsByTagName("channel");
      int numChannels = channels.getLength();
      for (int nodeIndex = 0; nodeIndex < numChannels; nodeIndex++)
      {
        Node channel = channels.item(0); // The index is 0 because after each move, the channel positions move up a notch
        boolean moveSuccessful = UserLayoutManager.moveNode(channel, destinationColumn, null);
        // Not done yet: Need to deal with case when move isn't successful!!!
      }

      destination.appendChild(destinationColumn);
    }

    // Move the source column before the destination column or at the end
    Node targetTab = destinationColumn.getParentNode();
    Node siblingColumn = method.equals("insertBefore") ? destinationColumn : null;
    UserLayoutManager.moveNode(sourceColumn, targetTab, siblingColumn);

    saveLayout(false);
  }

  /**
   * Moves a channel from one position in the layout to another.
   * @param sourceChannelSubscribeId the channel to move
   * @param method either <code>insertBefore</code> or <code>appendAfter</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void moveChannel(String sourceChannelSubscribeId, String method, String destinationElementId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();

    Element sourceChannel = userLayout.getElementById(sourceChannelSubscribeId);
    Element destinationElement = userLayout.getElementById(destinationElementId);

    // The destination element might be an empty tab or a column
    if (isTab(destinationElement))
    {
      // Create a new column in this tab and move the source channel there
      Element newColumn = createFolder("");
      Node destinationTab = userLayout.getElementById(destinationElementId);
      UserLayoutManager.moveNode(newColumn, destinationTab, null);
      UserLayoutManager.moveNode(sourceChannel, newColumn, null);
    }
    else if (isColumn(destinationElement))
    {
      // Move the source channel into the destination column
      UserLayoutManager.moveNode(sourceChannel, destinationElement, null);
    }
    else
    {
      // Move the source channel before the destination channel or at the end
      Node targetColumn = destinationElement.getParentNode();
      Node siblingChannel = method.equals("insertBefore") ? destinationElement : null;
      UserLayoutManager.moveNode(sourceChannel, targetColumn, siblingChannel);
    }

    saveLayout(false);
  }

  /**
   * Adds a channel to the layout.
   * @param newChannel the channel to add
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void addChannel(Element newChannel, String position, String destinationElementId) throws Exception
  {
    Element layout = userLayout.getDocumentElement();
    newChannel = (Element)(userLayout.importNode(newChannel, true));
    String instanceId = ulStore.generateNewChannelSubscribeId(staticData.getPerson());
    newChannel.setAttribute("ID", instanceId);
    // The following line is Xerces-specific
    ((org.apache.xerces.dom.DocumentImpl)userLayout).putIdentifier(instanceId, newChannel);

    Element destinationElement = userLayout.getElementById(destinationElementId);

    // The destination element might be an empty tab or a column
    if (isTab(destinationElement))
    {
      // Create a new column in this tab and move the source channel there
      Element newColumn = createFolder("");
      Node destinationTab = userLayout.getElementById(destinationElementId);
      UserLayoutManager.moveNode(newColumn, destinationTab, null);
      UserLayoutManager.moveNode(newChannel, newColumn, null);
    }
    else if (isColumn(destinationElement))
    {
      // Move the source channel into the destination column
      UserLayoutManager.moveNode(newChannel, destinationElement, null);
    }
    else
    {
      // Move the source channel before the destination channel or at the end
      Node targetColumn = destinationElement.getParentNode();
      Node siblingChannel = position.equals("before") ? destinationElement : null;
      UserLayoutManager.moveNode(newChannel, targetColumn, siblingChannel);
    }
    
    saveLayout(true);
  }

  /**
   * Adds a channel to the layout.
   * @param selectedChannelSubscribeId the channel to add
   * @param position either <code>before</code> or <code>after</code>
   * @param destinationElementId the ID of the channel to insert the new channel before or append after
   * @throws Exception
   */
  private final void addChannel(String selectedChannelSubscribeId, String position, String destinationElementId) throws Exception
  {
    Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    Element newChannel = channelRegistry.getElementById(selectedChannelSubscribeId);
    addChannel(newChannel, position, destinationElementId);
  }

  /**
   * Removes a channel element from the layout
   * @param channelSubscribeId the ID attribute of the channel to remove
   */
  private final void deleteChannel(String channelSubscribeId) throws Exception
  {
    pcs.getChannelManager().removeChannel(channelSubscribeId);
    deleteElement(channelSubscribeId);
  }  
  
  /**
   * Removes a tab or column element from the layout.  To remove
   * a channel element, call deleteChannel().
   * @param elementId the ID attribute of the element to remove
   */
  private final void deleteElement(String elementId) throws Exception
  {
    Element element = userLayout.getElementById(elementId);
    // for some reason I am getting a null here for any newly added element
    // I remember some other people mentioning this problem
    boolean deleteSuccessful = UserLayoutManager.deleteNode(element);
    if (deleteSuccessful)
      saveLayout(false);
    else
      throw new Exception("Element " + elementId + " cannot be removed because it is either unremovable or it or one of its parent elements is immutable.");
  }

  private final void updateTabLock(String elementId, boolean locked) throws Exception
  {
    Element element = userLayout.getElementById(elementId);
    if(locked)
    {
      element.setAttribute("unremovable", "true");
      element.setAttribute("immutable", "true");
    }
    else
    {
      element.setAttribute("unremovable", "false");
      element.setAttribute("immutable", "false");
    }
    saveLayout(false);
  }
  /**
   * A folder is a tab if its parent element is the layout element
   * @param folder the folder in question
   * @return <code>true</code> if the folder is a tab, otherwise <code>false</code>
   */
  private final boolean isTab (Element folder)
  {
    return folder.getParentNode().getNodeName().equals("layout");
  }

  /**
   * A folder is a column if its parent is a tab element
   * @param folder the folder in question
   * @return <code>true</code> if the folder is a column, otherwise <code>false</code>
   */
  private final boolean isColumn (Element folder)
  {
    return isTab((Element)folder.getParentNode());
  }

  /**
   * Creates a folder element with default attributes.  This method can be used
   * to create a tab or a column.  For tabs, pass the tab name.  For column,
   * pass an empty String since column names aren't meaningful
   * @param name the tab name for tabs and an empty string for columns
   * @return the newly created tab or column
   */
  private final Element createFolder (String name) throws Exception
  {
    String ID = String.valueOf(ulStore.generateNewFolderId(staticData.getPerson()));
    Element layout = userLayout.getDocumentElement();
    Document doc = layout.getOwnerDocument();
    Element folder = doc.createElement("folder");
    folder.setAttribute("name", name);
    folder.setAttribute("ID", ID);
    folder.setAttribute("type", "regular");
    folder.setAttribute("hidden", "false");
    folder.setAttribute("unremovable", "false");
    folder.setAttribute("immutable", "false");

    // This is Xerces-dependent, but it is the only way to get things to work at the moment
    // Without this line, it is not possible to access this new folder with the getElementById() method
    ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(ID, folder);

    return folder;
  }

  /**
   * Finds any parameters in a channel that are determined to be overridable
   * by a user.
   * @param selectedChannelSubscribeId an identifier to find the channel within the channel registry
   * @return a list of <parameter> elements whose override attribute is set to true
   * @throws org.jasig.portal.PortalException
   */
  private final List getOverridableChannelParams(String selectedChannelSubscribeId) throws PortalException {
    Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
    Element channel = (Element)channelRegistry.getElementById(selectedChannelSubscribeId);
    List overridableParams = null;

    if (channel != null) {
      overridableParams = new ArrayList();

      NodeList params = channel.getElementsByTagName("parameter");
      for (int i = 0; i < params.getLength(); i++) {
        Element param = (Element)params.item(i);
        String override = param.getAttribute("override");
        if (override != null && override.equals("yes"))
          overridableParams.add(param);
      }
    } else {
      throw new PortalException("Channel " + selectedChannelSubscribeId + " is missing from the channel registry");
    }
    return overridableParams;
  }


  private void saveLayout (boolean channelsAdded) throws PortalException
  {
    // Persist user layout
    // Needs to check if we're modifying the current layout!
    IUserLayoutManager ulm = context.getUserLayoutManager();
    ulm.setNewUserLayoutAndUserPreferences(userLayout, null, channelsAdded);
  }

  private void saveUserPreferences () throws PortalException
  {
    IUserLayoutManager ulm = context.getUserLayoutManager();
    if (modifyingCurrentProfile()) {
      ulm.setNewUserLayoutAndUserPreferences(null, userPrefs, false);
    } else {
      try {
        ulStore.putUserPreferences(staticData.getPerson(), userPrefs);
      } catch (Exception e) {
        throw new PortalException(e.getMessage(), e);
      }
    }
  }

  private boolean modifyingCurrentProfile () throws PortalException
  {
    // If the we are editing the current profile, return true, otherwise false
    return context.getUserLayoutManager().getCurrentProfile().equals(editedUserProfile);
  }

  /**
   * A sub-state of TabColumnPrefsState for visualizing the user layout
   * in tab-column form.
   */
  protected class DefaultState extends BaseState
  {
    protected TabColumnPrefsState context;

    public DefaultState(TabColumnPrefsState context)
    {
      this.context = context;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException
    {
      runtimeData = rd;

      // If the user hasn't clicked on a tab, get persisted active tab
      if (activeTab.equals("none"))
        activeTab = getActiveTab();

      action = runtimeData.getParameter("action");

      if (action != null)
      {
        // Select tab
        if (action.equals("selectTab"))
          activeTab = runtimeData.getParameter("activeTab");
        // Set active tab
        else if (action.equals("setActiveTab"))
        {
          try
          {
            setActiveTab(activeTab);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageSetActiveTab;
          }
        }
        // Rename tab
        else if (action.equals("renameTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");
            String tabName = runtimeData.getParameter("tabName");

            renameTab(tabId, tabName);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageRenameTab;
          }
        }
        // Move tab
        else if (action.equals("moveTab"))
        {
          try
          {
            String methodAndID = runtimeData.getParameter("method_ID");

            if (methodAndID != null)
            {
              String sourceTabId = runtimeData.getParameter("elementID");
              int indexOf_ = methodAndID.indexOf("_");
              String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
              String destinationTabId = methodAndID.substring(indexOf_ + 1);

              moveTab(sourceTabId, method, destinationTabId);
            }
            else
              action = "selectTab";
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveTab;
          }
        }
        // Add tab
        else if (action.equals("addTab"))
        {
          try
          {
            String tabName = runtimeData.getParameter("tabName");
            String methodAndID = runtimeData.getParameter("method_ID");

            if (methodAndID != null)
            {
              int indexOf_ = methodAndID.indexOf("_");
              String method = methodAndID.substring(0, indexOf_); // insertBefore or appendAfter
              String destinationTabId = methodAndID.substring(indexOf_ + 1);

              addTab(tabName, method, destinationTabId);
            }
            else
              action = "newTab";
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageAddTab;
          }
        }
        // Delete tab
        else if (action.equals("deleteTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            deleteElement(tabId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteTab;
          }
        }
        // Lock tab
        else if (action.equals("lockTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            updateTabLock(tabId, true);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageLockTab;
          }
        }
        // Unlock tab
        else if (action.equals("unlockTab"))
        {
          try
          {
            String tabId = runtimeData.getParameter("elementID");

            updateTabLock(tabId, false);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageUnlockTab;
          }
        }
        // Select column
        else if (action.equals("selectColumn"))
          elementID = runtimeData.getParameter("elementID");
        // Change column width(s)
        else if (action.equals("columnWidth"))
        {
          try
          {
            HashMap columnWidths = new HashMap();
            Enumeration eParams = runtimeData.getParameterNames();
            while (eParams.hasMoreElements())
            {
              String param = (String)eParams.nextElement();
              String prefix = "columnWidth_";

              if (param.startsWith(prefix))
              {
                String folderId = param.substring(prefix.length());
                String newWidth = runtimeData.getParameter(prefix + folderId);
                columnWidths.put(folderId, newWidth);
              }
            }

            changeColumnWidths(columnWidths);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageChangeColumnWidths;
          }
        }
        // Move column
        else if (action.equals("moveColumn"))
        {
          String activeTabParam = runtimeData.getParameter("activeTab");
          if (activeTabParam != null)
            activeTab = activeTabParam;
        }
        // Move column here
        else if (action.equals("moveColumnHere"))
        {
          try
          {
            // Get the source column if this is a one-step move, otherwise we already
            // have it stored as elementID
            String sourceId = runtimeData.getParameter("sourceID");
            if (sourceId != null)
              elementID = sourceId;

            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveColumn(elementID, method, destinationId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveColumn;
          }
        }
        // New column
        else if (action.equals("newColumn"))
        {
          try
          {
            String method = runtimeData.getParameter("method");
            elementID = runtimeData.getParameter("elementID");
            String destinationColumnId = elementID;

            addColumn(method, destinationColumnId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageNewColumn;
          }
        }
        // Add column
        else if (action.equals("addColumn"))
        {
          // Currently not implemented...
          // We need to assign widths to columns.
          // The action addColumn isn't in the stylesheet yet.
        }
        // Delete column
        else if (action.equals("deleteColumn"))
        {
          try
          {
            String columnId = runtimeData.getParameter("elementID");

            deleteElement(columnId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteColumn;
          }
        }
        // Select channel
        else if (action.equals("selectChannel"))
        {
          elementID = runtimeData.getParameter("elementID");

          // Modify channel parameters
          String subAction = runtimeData.getParameter("subAction");
          if (subAction != null && subAction.equals("modifyChannelParams"))
          {
            Element layoutChannel = userLayout.getElementById(elementID);
            String channelPublishId = "chan" + layoutChannel.getAttribute("chanID");
            Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
            Element channel = (Element)channelRegistry.getElementById(channelPublishId);
            List overridableChanParams = getOverridableChannelParams(channelPublishId);
            context.internalState = new ParametersState(context, this, overridableChanParams, channel);
            context.internalState.setStaticData(staticData);
          }
        }
        // Move channel
        else if (action.equals("moveChannel"))
        {
          String activeTabParam = runtimeData.getParameter("activeTab");
          if (activeTabParam != null)
            activeTab = activeTabParam;
        }
        // Move channel here
        else if (action.equals("moveChannelHere"))
        {
          try
          {
            // Get the source channel if this is a one-step move, otherwise we already
            // have it stored as elementID
            String sourceId = runtimeData.getParameter("sourceID");
            if (sourceId != null)
              elementID = sourceId;

            String method = runtimeData.getParameter("method");
            String destinationId = runtimeData.getParameter("elementID");

            moveChannel(elementID, method, destinationId);

            // Clear out elementId so the channel doesn't stay highlighted
            elementID = null;
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageMoveChannel;
          }
        }
        // Delete channel
        else if (action.equals("deleteChannel"))
        {
          try
          {
            String channelSubscribeId = runtimeData.getParameter("elementID");

            deleteChannel(channelSubscribeId);
          }
          catch (Exception e)
          {
            LogService.instance().log(LogService.ERROR, e);
            action = "error";
            errorMessage = errorMessageDeleteChannel;
          }
        }
        // Cancel
        else if (action.equals("cancel"))
        {
          elementID = "none";
        }
      }
      else
        action = "none";
    }

    public void renderXML (ContentHandler out) throws PortalException {
      try {
        // Set up chain: userLayout --> Structure Attributes Incorp. Filter --> out
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if (tFactory instanceof SAXTransformerFactory) {
          SAXTransformerFactory saxTFactory = (SAXTransformerFactory)tFactory;

          // Empty transformer to do the initial dom2sax transition
          Transformer emptytr = tFactory.newTransformer();

          // Stylesheet transformer
          String xslURI = set.getStylesheetURI("default", runtimeData.getBrowserInfo());
          TransformerHandler th = saxTFactory.newTransformerHandler(XSLT.getTemplates(ResourceLoader.getResourceAsURLString(this.getClass(), xslURI)));
          th.setResult(new SAXResult(out));
          Transformer sstr = th.getTransformer();

          // Set the parameters
          sstr.setParameter("baseActionURL", runtimeData.getBaseActionURL());
          sstr.setParameter("activeTab", activeTab);
          sstr.setParameter("action", action);
          sstr.setParameter("elementID", elementID != null ? elementID : "none");
          sstr.setParameter("errorMessage", errorMessage);

          StructureStylesheetUserPreferences ssup = userPrefs.getStructureStylesheetUserPreferences();
          StructureAttributesIncorporationFilter saif = new StructureAttributesIncorporationFilter(th, ssup);

          // Incorporate channel registry document into userLayout if user is in the subscribe process
          if (action.equals("newChannel")) {
            Node channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson()).getDocumentElement();
            userLayout.getDocumentElement().appendChild(userLayout.importNode(channelRegistry, true));
          }

          //if (action.equals("moveChannelHere"))
          //System.out.println(org.jasig.portal.utils.XML.serializeNode(userLayout));

          // Begin SAX chain
          emptytr.transform(new DOMSource(userLayout), new SAXResult(saif));

        } else {
          LogService.instance().log(LogService.ERROR, "TablColumnPrefsState::renderXML() : Unable to obtain SAX Transformer Factory ! Check your TRAX configuration.");
        }
      } catch (Exception e) {
        LogService.instance().log(LogService.ERROR, e);
        throw new GeneralRenderingException(e.getMessage());
      }
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for selecting skins
   */
  protected class SelectSkinsState extends BaseState
  {
    protected TabColumnPrefsState context;

    public SelectSkinsState(TabColumnPrefsState context) {
        this.context = context;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
        runtimeData = rd;
        String action = runtimeData.getParameter("action");
        if (action != null) {
            if (runtimeData.getParameter("submitSave")!=null) {
                // save
                String skinName = runtimeData.getParameter("skinName");
                userPrefs.getThemeStylesheetUserPreferences().putParameterValue("skin",skinName);
                // save user preferences ?
                saveUserPreferences();
                // reset state
                BaseState df=new DefaultState(context);
                df.setStaticData(staticData);
                context.setState(df);
            } else if (runtimeData.getParameter("submitCancel")!=null) {
                // return to the default state
                BaseState df=new DefaultState(context);
                df.setStaticData(staticData);
                context.setState(df);
            }
        }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
      InputStream xmlStream = PortalSessionManager.getResourceAsStream(SKIN_LIST_FILE);
      String currentSkin = userPrefs.getThemeStylesheetUserPreferences().getParameterValue("skin");

      XSLT xslt = new XSLT (this);
      xslt.setXML(xmlStream);
      xslt.setXSL(sslLocation, "skinList", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if(currentSkin!=null)
        xslt.setStylesheetParameter("currentSkin", currentSkin);
      xslt.transform();
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for choosing a new channel (formerly subscribe)
   */
  protected class NewChannelState extends BaseState
  {
    protected TabColumnPrefsState context;
    private String position = "none";
    private String catID = "top";

    public NewChannelState(TabColumnPrefsState context) {
      this.context = context;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
      runtimeData = rd;
      String action = runtimeData.getParameter("action");
      if (action != null) {
        if (action.equals("cancel")) {
          returnToDefaultState();
        } else {
          // User clicked "?"
          if (runtimeData.getParameter("channelMoreInfo") != null) {
            // Implement channel preview here!
            String selectedChannel = runtimeData.getParameter("selectedChannel");
            /*
            if (selectedChannel != null) {
              Element channelE = ChannelRegistryManager.getChannel(selectedChannel);
              String javaClass = channelE.getAttribute("class");
              org.jasig.portal.IChannel channel = null;
              try {
                System.out.println("Instantiating class: " + javaClass);
                channel = (org.jasig.portal.IChannel)Class.forName(javaClass).newInstance();
              } catch (Exception e) {
                LogService.instance().log(LogService.ERROR, "Not able to instantiate channel: " + javaClass);
                e.printStackTrace();
              }
              NodeIterator ni = null;
              try {
                ni = XPathAPI.selectNodeIterator(channelE, "parameter");
              } catch (javax.xml.transform.TransformerException te) {
                te.printStackTrace();
              }
              ChannelStaticData sdata = new ChannelStaticData();
              for (Node n = ni.nextNode(); n != null; n = ni.nextNode()) {
                Element parameter = (Element)n;
                System.out.println(parameter.getAttribute("name") + "=" + parameter.getAttribute("value"));
                sdata.setParameter(parameter.getAttribute("name"), parameter.getAttribute("value"));
              }
              channel.setStaticData(sdata);
              ChannelRuntimeData rdata = new ChannelRuntimeData();
              rdata.setBaseActionURL(rd.getBaseActionURL());
              rdata.setRenderingAsRoot(true);
              rdata.setBrowserInfo(rd.getBrowserInfo());
              channel.setRuntimeData(rdata);

              java.io.StringWriter sw = new java.io.StringWriter();
              org.apache.xml.serialize.OutputFormat outputFormat = new org.apache.xml.serialize.OutputFormat();
              outputFormat.setIndenting(true);
              org.apache.xml.serialize.XMLSerializer ser = new org.apache.xml.serialize.XMLSerializer(sw, outputFormat);

              try {
                channel.renderXML(ser.asContentHandler());
              } catch (java.io.IOException ioe) {
                 ioe.printStackTrace();
              }
              System.out.println("Channel output:");
              System.out.println(sw.toString());

            }*/
          } else if (runtimeData.getParameter("addChannel") != null) {
            // User clicked "Add"
            String selectedChannel = runtimeData.getParameter("selectedChannel");
            if (selectedChannel != null) {
              try {
                // Determine whether channel has overridable parameters
                List overridableChanParams = getOverridableChannelParams(selectedChannel);
                if (overridableChanParams.isEmpty()) {
                  addChannel(selectedChannel, position, elementID);
                  returnToDefaultState();
                } else { // present user with screen to specify subscribe-time params
                  Document channelRegistry = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());
                  Element channel = (Element)channelRegistry.getElementById(selectedChannel);
                  context.internalState = new ParametersState(context, this, overridableChanParams, channel, position, elementID);
                  context.internalState.setStaticData(staticData);
                }
              } catch (Exception e) {
                errorMessage = errorMessageNewChannel;
              }
            }
          } else {
            // Collect the position and element ID the first time
            String passedPosition = runtimeData.getParameter("position");
            String passedElementID = runtimeData.getParameter("elementID");
            if (passedPosition != null)
              position = passedPosition;
            if (passedElementID != null)
              elementID = passedElementID;

            // User clicked "Go"
            String selectedCategory = runtimeData.getParameter("selectedCategory");
            if (selectedCategory != null  && selectedCategory.trim().length() > 0)
              catID = selectedCategory;
          }
        }
      }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
      Document doc = ChannelRegistryManager.getChannelRegistry(staticData.getPerson());

      XSLT xslt = new XSLT(this);
      xslt.setXML(doc);
      xslt.setXSL(sslLocation, "newChannel", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      xslt.setStylesheetParameter("elementID", elementID);
      xslt.setStylesheetParameter("position", position);
      xslt.setStylesheetParameter("catID", catID);
      xslt.setStylesheetParameter("errorMessage", errorMessage);
      xslt.transform();
    }

    private void returnToDefaultState() throws PortalException {
      // Reset global variables
      elementID = "none";
      position = "none";
      action = "none";

      BaseState defaultState = new DefaultState(context);
      defaultState.setStaticData(staticData);
      context.setState(defaultState);
    }
  }

  /**
   * A sub-state of TabColumnPrefsState for setting channel parameters
   */
  protected class ParametersState extends BaseState
  {
    protected TabColumnPrefsState context;
    protected BaseState previousState;
    private List overridableChanParams;
    private Element registryChannel;
    private String position;
    private String destinationElementId;

    private boolean error = false;

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, Element registryChannel) {
      this.context = context;
      this.previousState = previousState;
      this.overridableChanParams = overridableChanParams;
      this.registryChannel = registryChannel;
    }

    public ParametersState(TabColumnPrefsState context, BaseState previousState, List overridableChanParams, Element registryChannel, String position, String destinationElementId) {
      this(context, previousState, overridableChanParams, registryChannel);
      this.position = position;
      this.destinationElementId = destinationElementId;
    }

    public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
      runtimeData = rd;
      String action = runtimeData.getParameter("uPTCUP_action");
      if (action != null) {
        if (action.equals("back")) {
          context.setState(previousState);
        } else if (action.equals("finished")) {
          applyChanges(); // Add or modify the channel
          returnToDefaultState();
        } else if (action.equals("cancel")) {
          returnToDefaultState();
        }
      }
    }

    public void renderXML (ContentHandler out) throws PortalException
    {
      XSLT xslt = new XSLT (this);
      xslt.setXML(getParametersDoc());
      xslt.setXSL(sslLocation, "parameters", runtimeData.getBrowserInfo());
      xslt.setTarget(out);
      xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());
      if (error)
        xslt.setStylesheetParameter("errorMessage", errorMessage);
      xslt.transform();
    }

    private void returnToDefaultState() throws PortalException {
      // Reset global variables
        elementID = "none";
        position = "none";
        action = "none";

      BaseState defaultState = new DefaultState(context);
      defaultState.setStaticData(staticData);
      context.setState(defaultState);
    }

    private void applyChanges() {
      // Finally, add the channel to the layout or modify it if it is already there
      try {
        if (previousState instanceof NewChannelState) {
          processParams(registryChannel);
          context.addChannel(registryChannel, position, destinationElementId);
        }
        else if (previousState instanceof DefaultState) {
          Element layoutChannel = userLayout.getElementById(elementID);
          processParams(layoutChannel);
          context.saveLayout(false);
        }

      } catch (Exception e) {
        error = true;
        errorMessage = errorMessageModChannelParams;
      }
    }

    private void processParams(Element channel) {
      // Process params
      Iterator iter = overridableChanParams.iterator();
      while (iter.hasNext()) {
        Element parameterE = (Element)iter.next();
        String paramName = parameterE.getAttribute("name");
        String paramValue = runtimeData.getParameter(paramName);

        // Find param within channel and update it
        NodeList params = channel.getElementsByTagName("parameter");
        for (int i = 0; i < params.getLength(); i++) {
          Element paramE = (Element)params.item(i);
          if (paramE.getAttribute("name").equals(paramName)) {
            paramE.setAttribute("value", paramValue);
            break;
          }
        }
      }
    }

    private Document getParametersDoc() throws PortalException {
      Document doc = DocumentFactory.getNewDocument();

      // Top-level element
      Element userPrefParamsE = doc.createElement("userPrefParams");

      if (previousState instanceof NewChannelState)
        userPrefParamsE.appendChild(doc.importNode(registryChannel, true));
      else if (previousState instanceof DefaultState) {
        Element layoutChannel = userLayout.getElementById(elementID);
        userPrefParamsE.appendChild(doc.importNode(layoutChannel, true));
      }

      // CPD
      Document cpd = ChannelRegistryManager.getCPD(registryChannel.getAttribute("typeID"));
      if (cpd != null)
        userPrefParamsE.appendChild(doc.importNode(cpd.getDocumentElement(), true));

      doc.appendChild(userPrefParamsE);
      return doc;
    }
  }

}
