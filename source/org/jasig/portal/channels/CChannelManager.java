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

package org.jasig.portal.channels;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalException;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.utils.XML;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.GroupsException;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * <p>Manages channels, replaces CPublisher</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CChannelManager extends BaseChannel {
  protected static final String sslLocation = "CChannelManager/CChannelManager.ssl";
  protected static final Document emptyDoc = DocumentFactory.getNewDocument();
  protected short state;
  protected static final short DEFAULT_STATE = 0;
  protected static final short CHANNEL_TYPE_STATE = 1;
  protected static final short GENERAL_SETTINGS_STATE = 2;
  protected static final short CUSTOM_SETTINGS_STATE = 3;
  protected static final short CHANNEL_DEF_STATE = 4;
  protected static final short CHANNEL_CONTROLS_STATE = 5;
  protected static final short CHANNEL_CATEGORIES_STATE = 6;
  protected static final short CHANNEL_GROUPS_STATE = 7;
  protected static final short CHANNEL_REVIEW_STATE = 8;
  protected static final short MODIFY_CHANNEL_STATE = 9;
  protected String action;
  protected String stepID;
  protected Document channelManagerDoc;
  protected ChannelDefinition channelDef;
  protected CategorySettings categorySettings = new CategorySettings();
  protected GroupSettings groupSettings = new GroupSettings();
  protected ModifyChannelSettings modChanSettings = new ModifyChannelSettings();
  protected IPerson person;

  // Called after publishing so that you won't see any previous settings
  // on the next publish attempt
  protected void resetSettings() {
    channelDef = new ChannelDefinition();
    categorySettings = new CategorySettings();
    groupSettings = new GroupSettings();
    modChanSettings = new ModifyChannelSettings();
  }

  public void setStaticData (ChannelStaticData sd) throws PortalException {
    staticData = sd;
    person = sd.getPerson();
  }

  public void setRuntimeData (ChannelRuntimeData rd) throws PortalException {
    runtimeData = rd;
    action = runtimeData.getParameter("uPCM_action");

    // Capture information that the user entered on previous screen
    doCapture(); // Keep after "action = " because action might change inside doCapture()

    // Prepare the appropriate XML documents for the destination screen
    doAction();
  }

  public void renderXML (ContentHandler out) throws PortalException {
    XSLT xslt = new XSLT(this);
    xslt.setXML(channelManagerDoc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.setStylesheetParameter("baseActionURL", runtimeData.getBaseActionURL());

    String action = null;
    switch (state) {
      case DEFAULT_STATE:
        action = "none";
        break;
      case CHANNEL_TYPE_STATE:
        action = "selectChannelType";
        break;
      case GENERAL_SETTINGS_STATE:
        action = "selectGeneralSettings";
        break;
      case CUSTOM_SETTINGS_STATE:
        action = "customSettings";
        break;
      case CHANNEL_DEF_STATE:
        action = "channelDef";
        xslt.setStylesheetParameter("stepID", fixStepID(stepID));
        break;
      case CHANNEL_CONTROLS_STATE:
        action = "selectControls";
        break;
      case CHANNEL_CATEGORIES_STATE:
        action = "selectCategories";
        break;
      case CHANNEL_GROUPS_STATE:
        action = "selectGroups";
        break;
      case CHANNEL_REVIEW_STATE:
        action = "reviewChannel";
        break;
      case MODIFY_CHANNEL_STATE:
        action = "selectModifyChannel";
        break;
      default:
        action = "none";
        break;
    }

    xslt.setStylesheetParameter("action", action);
    xslt.transform();
  }

  private String fixStepID (String stepID) {
    if (stepID == null) {
      stepID = "1";
    } else {
      try {
        Integer.parseInt(stepID);
      }
      catch (java.lang.NumberFormatException nfe) {
        stepID = "1";
      }
    }
    return stepID;
  }

  protected void doCapture() {
    stepID = runtimeData.getParameter("uPCM_step");
    String capture = runtimeData.getParameter("uPCM_capture");
    if (capture != null) {
      // Channel types
      if (capture.equals("selectChannelType")) {
        String typeID = runtimeData.getParameter("ID");
        if (typeID != null) {
          if (!typeID.equals(channelDef.getTypeID())) {
            channelDef.setTypeID(typeID);
            channelDef.resetChannelControls();
            channelDef.removeParameters();
          }
        }
        else
          action = "selectChannelType";
      // General Settings (name and timeout)
      } else if (capture.equals("selectGeneralSettings")) {
        String name = runtimeData.getParameter("name");
        String description = runtimeData.getParameter("description");
        String title = runtimeData.getParameter("title");
        String timeout = runtimeData.getParameter("timeout");
        String javaClass = runtimeData.getParameter("class");
        if (name != null)
          channelDef.setName(name.trim());
        if (description != null)
          channelDef.setDescription(description.trim());
        if (title != null)
          channelDef.setTitle(title.trim());
        if (timeout != null)
          channelDef.setTimeout(timeout.trim());
        if (javaClass != null)
          channelDef.setJavaClass(javaClass.trim());
      // Custom parameters
      } else if (capture.equals("customSettings")) {
        String subAction = runtimeData.getParameter("uPCM_subAction");
        if (subAction != null) {
          String name = runtimeData.getParameter("name");
          if (name != null)
            name = name.trim();
          if (subAction.equals("addParameter")) {
            String value = runtimeData.getParameter("value");
            if (value != null)
              value = value.trim();
            String override = runtimeData.getParameter("override");
            channelDef.addParameter(name, value, (override != null ? "yes" : "no"));
          }
          else if (subAction.equals("deleteParameter")) {
            channelDef.removeParameter(name);
          }
        }
      // CPD parameters
      } else if (capture.equals("channelDef")) {
        Iterator iter = ((java.util.Hashtable)runtimeData).keySet().iterator();
        while (iter.hasNext()) {
          String name = (String)iter.next();

          // Ignore parameters whose name starts with "uPCM_"
          if (name.startsWith("uPCM_"))
            continue;

          String value = runtimeData.getParameter(name);
          String override = runtimeData.getParameter("uPCM_" + name + "_sub");
          channelDef.addParameter(name, value, (override != null ? "yes" : "no"));
        }
      // Channel controls
      } else if (capture.equals("selectControls")) {
        String editable = runtimeData.getParameter("editable");
        channelDef.setEditable(editable != null ? "true" : "false");
        String hasHelp = runtimeData.getParameter("hasHelp");
        channelDef.setHasHelp(hasHelp != null ? "true" : "false");
        String hasAbout = runtimeData.getParameter("hasAbout");
        channelDef.setHasAbout(hasAbout != null ? "true" : "false");
      // Categories
      } else if (capture.equals("selectCategories")) {
        String selectedCategory = runtimeData.getParameter("selectedCategory");
        if (selectedCategory != null && selectedCategory.trim().length() > 0) {
          if (runtimeData.getParameter("uPCM_browse") != null)
            categorySettings.setBrowsingCategory(selectedCategory);
          else // runtimeData.getParameter("uPCM_select") != null
            categorySettings.addSelectedCategory(selectedCategory);
        }
        String removeCategory = runtimeData.getParameter("removeCategory");
        if (removeCategory != null)
          categorySettings.removeCategory(removeCategory);
      // Groups
      } else if (capture.equals("selectGroups")) {
        String selectedGroup = runtimeData.getParameter("selectedGroup");
        if (selectedGroup != null && selectedGroup.trim().length() > 0) {
          if (runtimeData.getParameter("uPCM_browse") != null)
            groupSettings.setBrowsingGroup(selectedGroup);
          else // runtimeData.getParameter("uPCM_select") != null
            groupSettings.addSelectedGroup(selectedGroup);
        }
        String removeGroup = runtimeData.getParameter("removeGroup");
        if (removeGroup != null)
          groupSettings.removeGroup(removeGroup);
      }
    }
  }

  protected void doAction () throws PortalException {
    if (action != null) {
      if (action.equals("selectChannelType")) {
        state = CHANNEL_TYPE_STATE;
        Workflow workflow = new Workflow();

        // Add channel types and channel def
        WorkflowSection chanTypeSection = new WorkflowSection("selectChannelType");
        WorkflowStep step = new WorkflowStep("1", "Channel Type");
        step.addDataElement(ChannelRegistryManager.getChannelTypes().getDocumentElement());
        step.addDataElement(channelDef.toXML());
        chanTypeSection.addStep(step);
        workflow.setChannelTypesSection(chanTypeSection);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectGeneralSettings")) {

        state = GENERAL_SETTINGS_STATE;
        Workflow workflow = new Workflow();

        // Add General Settings section
        WorkflowSection gsSection = new WorkflowSection("selectGeneralSettings");
        workflow.setGeneralSettingsSection(gsSection);
        WorkflowStep step = new WorkflowStep("1", "General Settings");
        step.addDataElement(channelDef.toXML());
        gsSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("channelParams") || action.equals("customSettings") || action.equals("channelDef")) {

        Workflow workflow = new Workflow();

        // Add CPD document if channel is "generic", otherwise custom settings
        if (channelDef.getTypeID().equals("-1")) {
          state = CUSTOM_SETTINGS_STATE;
          WorkflowSection csSection = new WorkflowSection("customSettings");
          WorkflowStep step = new WorkflowStep("1", "Channel Parameters");
          step.addDataElement(channelDef.toXML());
          csSection.addStep(step);
          workflow.setChannelParamsSection(csSection);
        } else {
          state = CHANNEL_DEF_STATE;
          CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
          cpdSection.addToStep(channelDef.toXML(), fixStepID(stepID));
          workflow.setChannelParamsSection(cpdSection);
        }

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectControls")) {

        state = CHANNEL_CONTROLS_STATE;
        Workflow workflow = new Workflow();

        // Add controlsSection
        WorkflowSection controlsSection = new WorkflowSection("selectControls");
        if (channelDef.getEditable() == null) // if one is null, they are all null
          channelDef.resetChannelControls();
        WorkflowStep step = new WorkflowStep("1", "Channel Controls");
        step.addDataElement(channelDef.toXML());
        controlsSection.addStep(step);
        workflow.setControlsSection(controlsSection);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectCategories")) {

        state = CHANNEL_CATEGORIES_STATE;
        Workflow workflow = new Workflow();

        // Add channel registry
        WorkflowSection catSection = new WorkflowSection("selectCategories");
        workflow.setCategoriesSection(catSection);
        WorkflowStep step = new WorkflowStep("1", "Categories");
        step.addDataElement(ChannelRegistryManager.getChannelRegistry().getDocumentElement());
        // Add user settings with previously chosen categories
        step.addDataElement(categorySettings.toXML());
        catSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("selectGroups")) {

        state = CHANNEL_GROUPS_STATE;
        Workflow workflow = new Workflow();

        // Add groups document with all IPerson members of "Everyone"
        WorkflowSection groupSection = new WorkflowSection("selectGroups");
        workflow.setGroupsSection(groupSection);
        WorkflowStep step = new WorkflowStep("1", "Groups");
        step.addDataElement(groupSettings.getGroups().getDocumentElement());
        // Add user settings with previously chosen groups
        step.addDataElement(groupSettings.toXML());
        groupSection.addStep(step);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("reviewChannel")) {

        state = CHANNEL_REVIEW_STATE;
        Workflow workflow = new Workflow();

        // Channel types
        WorkflowSection ctSection = new WorkflowSection("selectChannelType");
        WorkflowStep ctStep = new WorkflowStep("1", "Channel Type");
        ctStep.addDataElement(ChannelRegistryManager.getChannelTypes().getDocumentElement());
        ctSection.addStep(ctStep);
        workflow.setChannelTypesSection(ctSection);

        // Selected categories
        WorkflowSection regSection = new WorkflowSection("selectCategories");
        WorkflowStep regStep = new WorkflowStep("1", "Categories");
        regStep.addDataElement(ChannelRegistryManager.getChannelRegistry().getDocumentElement());
        regStep.addDataElement(categorySettings.toXML());
        regSection.addStep(regStep);
        workflow.setCategoriesSection(regSection);

        // Selected groups
        WorkflowSection groupsSection = new WorkflowSection("selectGroups");
        WorkflowStep groupsStep = new WorkflowStep("1", "Groups");
        groupsStep.addDataElement(groupSettings.getGroups().getDocumentElement());
        groupsStep.addDataElement(groupSettings.toXML());
        groupsSection.addStep(groupsStep);
        workflow.setGroupsSection(groupsSection);

        // Review (with channel definition)
        WorkflowSection reviewSection = new WorkflowSection("reviewChannel");
        WorkflowStep step = new WorkflowStep("1", "Review");
        step.addDataElement(channelDef.toXML());
        reviewSection.addStep(step);
        workflow.setReviewSection(reviewSection);

        channelManagerDoc = workflow.toXML();

      } else if (action.equals("finished")) {

        state = DEFAULT_STATE; // we need to add a confirmation and channel preview screen
        String[] catIDs = (String[])(categorySettings.getSelectedCategories()).toArray(new String[0]);
        IEntityGroup[] groups = groupSettings.getSelectedGroupsAsIEntityGroups();
        try {
          Element channelE = channelDef.toXML();
          ChannelRegistryManager.publishChannel(channelE, catIDs, groups, person);
          resetSettings();
        } catch (Exception e) {
          // Need to revisit this and handle the error!
          throw new PortalException(e);
        }

      } else if (action.equals("selectModifyChannel")) {
        state = MODIFY_CHANNEL_STATE;
        channelManagerDoc = getChannelManagerDoc(modChanSettings);
      } else if (action.equals("changePage")) {
        String newPage = runtimeData.getParameter("newPage");
        if (newPage != null) {
          modChanSettings.setCurrentPage(newPage);
          channelManagerDoc = getChannelManagerDoc(modChanSettings);
        }
      } else if (action.equals("changeRecordsPerPage")) {
        String recordsPerPage = runtimeData.getParameter("recordsPerPage");
        if (recordsPerPage != null) {
          // Figure out what page we should be on based on the change in records per page.
          try {
            int oldPage = Integer.parseInt(modChanSettings.getCurrentPage());
            int oldRecordsPerPage = Integer.parseInt(modChanSettings.getRecordsPerPage());
            int recsPerPage = Integer.parseInt(recordsPerPage);
            if (recsPerPage > 0 && recsPerPage != oldRecordsPerPage) {            
              // Thanks to jweight@campuspipeline.com for the following formula:
              String newPage = String.valueOf(((((oldPage-1)*oldRecordsPerPage)+1)/(recsPerPage)+1));
              modChanSettings.setCurrentPage(newPage);
              modChanSettings.setRecordsPerPage(recordsPerPage);
              channelManagerDoc = getChannelManagerDoc(modChanSettings);
            }
          } catch (NumberFormatException nfe) {
            // do nothing here, just leave the current page as is.
          }
        }
      } else if (action.equals("filterByCategory")) {
        String filterByID = runtimeData.getParameter("newCategory");
        if (filterByID != null) {
          // User may be beyond the last page of this filtered set so put them back on page 1.
          modChanSettings.setCurrentPage("1");                            
          modChanSettings.setFilterByID(filterByID);
          channelManagerDoc = getChannelManagerDoc(modChanSettings);
        }
      } else if (action.equals("editChannelSettings")) {
        String str_channelPublishId = runtimeData.getParameter("channelID");
        // Set the channel definition
        channelDef.setChannelDefinition(ChannelRegistryManager.getChannel(str_channelPublishId));

        // Set the groups
        int int_channelPublishId = Integer.parseInt(str_channelPublishId.startsWith("chan") ? str_channelPublishId.substring(4) : str_channelPublishId);
        IPermissionManager pm = AuthorizationService.instance().newPermissionManager("UP_FRAMEWORK");        
        IAuthorizationPrincipal[] principals = pm.getAuthorizedPrincipals("SUBSCRIBE", "CHAN_ID." + int_channelPublishId);

        for (int i = 0; i < principals.length; i++) {
          IGroupMember gm = AuthorizationService.instance().getGroupMember(principals[i]);
          if (gm instanceof IEntityGroup) {
            groupSettings.addSelectedGroup((IEntityGroup)gm);
          }
        }

        // Set the categories
        Set categories = new TreeSet();
        org.w3c.dom.NodeList nl = ChannelRegistryManager.getCategories(str_channelPublishId);
        for (int i = 0; i < nl.getLength(); i++) {
          Element category = (Element)nl.item(i);
          categories.add(category.getAttribute("ID"));
        }
        categorySettings.setSelectedCategories(categories);

        action = "reviewChannel";
        doAction();
      } else if (action.equals("removePublishedChannel")) {
        String channelPublishId = runtimeData.getParameter("channelID");
        if (channelPublishId != null) {
          try {
            ChannelRegistryManager.removeChannel(channelPublishId);
          } catch (Exception e) {
            throw new GeneralRenderingException(e.getMessage());
          }
        }
        channelManagerDoc = getChannelManagerDoc(modChanSettings);
      }
    }

    if (action == null || action.equals("cancel")) {
      state = DEFAULT_STATE;
      channelManagerDoc = emptyDoc;
      channelDef = new ChannelDefinition();
      categorySettings = new CategorySettings();
      groupSettings = new GroupSettings();
    }
  }

  protected Document getChannelManagerDoc(ModifyChannelSettings modChanSettings) throws PortalException {
    Document channelManagerDoc = DocumentFactory.getNewDocument();

    // Add the top level <manageChannels> to the document
    Element channelManager = channelManagerDoc.createElement("manageChannels");
    channelManagerDoc.appendChild(channelManager);

    // Get the channel registry
    Document channelRegistryDoc = ChannelRegistryManager.getChannelRegistry();

    // Set the registry ID attribute to "-1"
    Element registry = channelRegistryDoc.getDocumentElement();
    registry.setAttribute("ID", "-1");

    // Add the <registry> to <manageChannels>
    Element channelRegistry = (Element)channelManagerDoc.importNode(channelRegistryDoc.getDocumentElement(), true);
    channelManager.appendChild(channelRegistry);

    // Add a <userSettings> fragment to <manageChannels>
    appendModifyChannelSettings(channelManager, modChanSettings);

    return channelManagerDoc;
  }

  protected static void appendModifyChannelSettings(Element channelManager, ModifyChannelSettings modChanSettings) {
    Document doc = channelManager.getOwnerDocument();
    Element userSettingsE = doc.createElement("userSettings");
    Element modifyView = doc.createElement("modifyView");
    userSettingsE.appendChild(modifyView);
    Element recordsPerPageE = doc.createElement("recordsPerPage");
    recordsPerPageE.appendChild(doc.createTextNode(modChanSettings.getRecordsPerPage()));
    modifyView.appendChild(recordsPerPageE);
    Element currentPageE = doc.createElement("currentPage");
    currentPageE.appendChild(doc.createTextNode(modChanSettings.getCurrentPage()));
    modifyView.appendChild(currentPageE);
    Element filterByIDE = doc.createElement("filterByID");
    filterByIDE.appendChild(doc.createTextNode(modChanSettings.getFilterByID()));
    modifyView.appendChild(filterByIDE);
    channelManager.appendChild(userSettingsE);
  }

  /**
   * Keeps track of page settings for MODIFY_CHANNEL_STATE
   */
  protected class ModifyChannelSettings {
    private String recordsPerPage;
    private String currentPage;
    private String filterByID;

    protected ModifyChannelSettings() {
      recordsPerPage = "15";
      currentPage = "1";
      filterByID = "-1";
    }

    // Accessor methods
    protected String getRecordsPerPage() { return recordsPerPage; }
    protected String getCurrentPage() { return currentPage; }
    protected String getFilterByID() { return filterByID; }

    protected void setRecordsPerPage(String recordsPerPage) { this.recordsPerPage = recordsPerPage; }
    protected void setCurrentPage(String currentPage) { this.currentPage = currentPage; }
    protected void setFilterByID(String filterByID) { this.filterByID = filterByID; }
  }


  /**
   * <p>This Workflow class represents the collection of workflow sections and can
   * produce an XML version of itself for passing to the XSLT stylesheets. When a
   * particular section is not explicitly set, a minimal XML fragment will still
   * be included so that the channel can render the workflow sections at the top.
   * The channel parameters section is included every time except when the channel
   * type is not known.</p>
   */
  protected class Workflow {
    protected WorkflowSection channelTypesSection;
    protected WorkflowSection generalSettingsSection;
    protected WorkflowSection channelParamsSection;
    protected WorkflowSection controlsSection;
    protected WorkflowSection categoriesSection;
    protected WorkflowSection groupsSection;
    protected WorkflowSection reviewSection;

    protected void setChannelTypesSection(WorkflowSection channelTypesSection) { this.channelTypesSection = channelTypesSection; }
    protected void setGeneralSettingsSection(WorkflowSection generalSettingsSection) { this.generalSettingsSection = generalSettingsSection; }
    protected void setChannelParamsSection(WorkflowSection channelParamsSection) { this.channelParamsSection = channelParamsSection; }
    protected void setControlsSection(WorkflowSection controlsSection) { this.controlsSection = controlsSection; }
    protected void setCategoriesSection(WorkflowSection categoriesSection) { this.categoriesSection = categoriesSection; }
    protected void setGroupsSection(WorkflowSection groupsSection) { this.groupsSection = groupsSection; }
    protected void setReviewSection(WorkflowSection reviewSection) { this.reviewSection = reviewSection; }

    protected Document toXML() throws PortalException {
      Document doc = DocumentFactory.getNewDocument();

      // Add the top level <manageChannels> to the document
      Element channelManagerE = doc.createElement("manageChannels");
      doc.appendChild(channelManagerE);

      // Add all the sections
      addSection(channelTypesSection, "selectChannelType", "Channel Type", channelManagerE);
      addSection(generalSettingsSection, "selectGeneralSettings", "General Settings", channelManagerE);
      addChannelParamsSection(channelManagerE);
      addSection(controlsSection, "selectControls", "Channel Controls", channelManagerE);
      addSection(categoriesSection, "selectCategories", "Categories", channelManagerE);
      addSection(groupsSection, "selectGroups", "Groups", channelManagerE);
      addSection(reviewSection, "reviewChannel", "Review", channelManagerE);

      return doc;
    }

    private void addSection (WorkflowSection section, String sectionElementName, String stepTitle, Element e) {
      // For sections that haven't been set, add an empty one with one step
      if (section == null) {
        section = new WorkflowSection(sectionElementName);
        section.addStep(new WorkflowStep("1", stepTitle));
      }
      e.appendChild(section.toXML(e.getOwnerDocument()));
    }

    private void addChannelParamsSection(Element e) throws PortalException {
      if (channelParamsSection == null) {
        // Add CPD document if channel is "generic", otherwise custom settings
        String channelTypeID = channelDef.getTypeID();
        if (channelTypeID != null) {
          if (channelTypeID.equals("-1")) {
            WorkflowSection csSection = new WorkflowSection("customSettings");
            WorkflowStep step = new WorkflowStep("1", "Channel Parameters");
            csSection.addStep(step);
            setChannelParamsSection(csSection);
          } else {
            CPDWorkflowSection cpdSection = new CPDWorkflowSection(channelDef.getTypeID());
            setChannelParamsSection(cpdSection);
          }
        } else {
          return;
        }
      }
      e.appendChild(channelParamsSection.toXML(e.getOwnerDocument()));
    }
  }

  protected class WorkflowSection {
    protected String name;
    protected List steps;

    protected WorkflowSection() {
    }

    protected WorkflowSection(String name) {
      this.name = name;
      steps = new ArrayList();
    }

    protected void setName(String name) { this.name = name; }
    protected void addStep(WorkflowStep step) {
      steps.add(step);
    }

    protected Element toXML(Document doc) {
      // Add this step's workflow element
      Element sectionE = doc.createElement(name);

      // Add the <params> element
      Element paramsE = doc.createElement("params");
      sectionE.appendChild(paramsE);

      // Add all the <step> elements
      Iterator iter = steps.iterator();
      while (iter.hasNext()) {
        WorkflowStep step = (WorkflowStep)iter.next();
        paramsE.appendChild(step.toXML(doc));
      }

      return sectionE;
    }
  }

  protected class CPDWorkflowSection extends WorkflowSection {
    protected Document cpdDoc;

    protected CPDWorkflowSection (String chanTypeID) throws PortalException {
      super();
      cpdDoc = ChannelRegistryManager.getCPD(chanTypeID);
    }

    protected void addToStep(Element element, String stepID) {
      for (Node n1 = cpdDoc.getDocumentElement().getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
        if (n1.getNodeType() == Node.ELEMENT_NODE && n1.getNodeName().equals("params")) {
          for (Node n2 = n1.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            if (n2.getNodeType() == Node.ELEMENT_NODE && n2.getNodeName().equals("step")) {
              for (Node n3 = n2.getFirstChild(); n3 != null; n3 = n3.getNextSibling()) {
                if (n3.getNodeType() == Node.ELEMENT_NODE && n3.getNodeName().equals("ID")) {
                  for (Node n4 = n3.getFirstChild(); n4 != null; n4 = n4.getNextSibling()) {
                    if (n4.getNodeType() == Node.TEXT_NODE) {
                      String ID = n4.getNodeValue();
                      if (ID.equals(stepID)) {
                        n2.appendChild(cpdDoc.importNode(element, true));
                        break;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    protected Element toXML(Document doc) {
      return (Element)doc.importNode(cpdDoc.getDocumentElement(), true);
    }
  }

  protected class WorkflowStep {
    protected String ID;
    protected String name;
    protected List dataElements;

    protected WorkflowStep(String ID, String name) {
      this.ID = ID;
      this.name = name;
      this.dataElements = new ArrayList();
    }

    // Accessor methods
    protected String getID() { return ID; }
    protected String getName() { return name; }

    protected void setID(String ID) { this.ID = ID; }
    protected void setName(String name) { this.name = name; }
    protected void addDataElement(Element dataElement) { this.dataElements.add(dataElement); }

    protected Element toXML(Document doc) {
      Element stepE = doc.createElement("step");
      Element IDE = doc.createElement("ID");
      IDE.appendChild(doc.createTextNode(ID));
      stepE.appendChild(IDE);
      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(name));
      stepE.appendChild(nameE);

      Iterator iter = dataElements.iterator();
      while (iter.hasNext()) {
        stepE.appendChild(doc.importNode((Element)iter.next(), true));
      }

      return stepE;
    }
  }

  protected class ChannelDefinition {
    protected String ID;
    protected String typeID;
    protected String name;
    protected String description;
    protected String title;
    protected String timeout;
    protected String fname;
    protected String javaClass;
    protected String editable;
    protected String hasHelp;
    protected String hasAbout;
    protected Map parameters;

    protected class Parameter {
      protected String name;
      protected String value;
      protected String override; // "yes" or "no"

      protected Parameter(String name, String value, String override) {
        this.name = name;
        this.value = value;
        this.override = override;
      }

      protected String getName() { return name; }
      protected String getValue() { return value; }
      protected String getOverride() { return override; }
      protected void setName(String name) { this.name = name; }
      protected void setValue(String value) { this.value = value; }
      protected void setOverride(String override) { this.override = override; }
    }

    protected ChannelDefinition() {
      parameters = new HashMap();
    }

    protected String getTypeID() { return typeID; }
    protected String getEditable() { return editable; }
    protected String getHasHelp() { return hasHelp; }
    protected String getHasAbout() { return hasAbout; }

    protected void setTypeID(String typeID) { this.typeID = typeID; }
    protected void setName(String name) { this.name = name; }
    protected void setDescription(String description) { this.description = description; }
    protected void setTitle(String title) { this.title = title; }
    protected void setTimeout(String timeout) { this.timeout = timeout; }
    protected void setJavaClass(String javaClass) { this.javaClass = javaClass; }
    protected void setEditable(String editable) { this.editable = editable; }
    protected void setHasHelp(String hasHelp) { this.hasHelp = hasHelp; }
    protected void setHasAbout(String hasAbout) { this.hasAbout = hasAbout; }

    private void setAttribute(Element e, String attName, String attVal) {
      // Only set the attribute if it has a non-null value
      if (attVal != null)
        e.setAttribute(attName, attVal);
    }

    protected void addParameter(String name, String value, String modType) {
      parameters.put(name, new Parameter(name, value, modType));
    }

    protected void removeParameter(String name) {
      parameters.remove(name);
    }

    protected void removeParameters() {
      parameters = new HashMap();
    }

    protected void resetChannelControls() {
      try {
        // Look inside CPD for controls.
        Document cpdDoc = ChannelRegistryManager.getCPD(typeID);
        if (cpdDoc != null) {
          for (Node n1 = cpdDoc.getDocumentElement().getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if (n1.getNodeType() == Node.ELEMENT_NODE && n1.getNodeName().equals("controls")) {
              for (Node n2 = n1.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
                if (n2.getNodeType() == Node.ELEMENT_NODE && n2.getNodeName().equals("control")) {
                  Element control = (Element)n2;
                  String type = control.getAttribute("type");
                  if (type != null) {
                    if (type.equals("edit")) {
                      String editAtt = control.getAttribute("include");
                      editable = editAtt != null && editAtt.equals("yes") ? "true" : "false";
                    } else if (type.equals("about")) {
                      String aboutAtt = control.getAttribute("include");
                      hasAbout = aboutAtt != null && aboutAtt.equals("yes") ? "true" : "false";
                    } else if (type.equals("help")) {
                      String helpAtt = control.getAttribute("include");
                      hasHelp = helpAtt != null && helpAtt.equals("yes") ? "true" : "false";
                    }
                  }
                }
              }
            }
          }
        }
      } catch (PortalException pe) {
        // Unable to open CPDDoc, just leave values uninitialized
      }
    }

    protected void setChannelDefinition(Element channelE) throws PortalException {
      ID = channelE.getAttribute("ID");
      typeID = channelE.getAttribute("typeID");
      name = channelE.getAttribute("name");
      description = channelE.getAttribute("description");
      title = channelE.getAttribute("title");
      timeout = channelE.getAttribute("timeout");
      fname = channelE.getAttribute("fname");
      javaClass = channelE.getAttribute("class");
      editable = channelE.getAttribute("editable");
      hasHelp = channelE.getAttribute("hasHelp");
      hasAbout = channelE.getAttribute("hasAbout");

      for (Node n = channelE.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals("parameter")) {
          Element parameterE = (Element)n;
          String name = parameterE.getAttribute("name");
          String value = parameterE.getAttribute("value");
          String override = parameterE.getAttribute("override");
          parameters.put(name, new Parameter(name, value, override));
        }
      }
    }

    protected Element toXML() {
      Element channelE = emptyDoc.createElement("channel");
      setAttribute(channelE, "ID", ID);
      setAttribute(channelE, "typeID", typeID);
      setAttribute(channelE, "name", name);
      setAttribute(channelE, "description", description);
      setAttribute(channelE, "title", title);
      setAttribute(channelE, "fname", fname);
      setAttribute(channelE, "class", javaClass);
      setAttribute(channelE, "timeout", timeout);
      setAttribute(channelE, "editable", editable);
      setAttribute(channelE, "hasAbout", hasAbout);
      setAttribute(channelE, "hasHelp", hasHelp);

      Iterator iter = parameters.keySet().iterator();
      while (iter.hasNext()) {
        String name = (String)iter.next();
        Parameter param = (Parameter)parameters.get(name);
        Element parameterE = emptyDoc.createElement("parameter");
        parameterE.setAttribute("name", param.getName());
        parameterE.setAttribute("value", param.getValue());
        parameterE.setAttribute("override", param.getOverride());
        channelE.appendChild(parameterE);
      }
      return channelE;
    }
  }

  protected class CategorySettings {
    protected String browsingCategory;
    protected Set selectedCategories;

    protected CategorySettings() {
      browsingCategory = "top";
      selectedCategories = new TreeSet();
    }

    protected Set getSelectedCategories() { return selectedCategories; }
    protected void setBrowsingCategory(String browsingCategory) { this.browsingCategory = browsingCategory; }
    protected void addSelectedCategory(String category) { selectedCategories.add(category); }
    protected void setSelectedCategories(Set selectedCategories) { this.selectedCategories = selectedCategories; }
    protected void removeCategory(String category) { selectedCategories.remove(category); }

    protected Element toXML() {
      Element userSettingsE = emptyDoc.createElement("userSettings");
      Element browsingCategoryE = emptyDoc.createElement("browsingCategory");
      browsingCategoryE.appendChild(emptyDoc.createTextNode(browsingCategory));
      userSettingsE.appendChild(browsingCategoryE);

      // Add selected categories if there are any
      if (selectedCategories.size() > 0) {
        Element selectedCategoriesE = emptyDoc.createElement("selectedCategories");
        Iterator iter = selectedCategories.iterator();
        while (iter.hasNext()) {
          Element selectedCategoryE = emptyDoc.createElement("selectedCategory");
          selectedCategoryE.appendChild(emptyDoc.createTextNode((String)iter.next()));
          selectedCategoriesE.appendChild(selectedCategoryE);
        }
        userSettingsE.appendChild(selectedCategoriesE);
      }

      return userSettingsE;
    }
  }

  protected class GroupSettings {
    protected String browsingGroup;
    protected Set selectedGroups; // a set of Strings, each String is a group ID
    protected final String groupIDPrefix = "g"; // XML IDs have to start with a letter

    protected GroupSettings() {
      browsingGroup = "top";
      selectedGroups = new TreeSet();
    }

    protected void setBrowsingGroup(String browsingGroup) { this.browsingGroup = browsingGroup; }
    protected Set getSelectedGroups() { return selectedGroups; }
    protected IEntityGroup[] getSelectedGroupsAsIEntityGroups() throws GroupsException {
      IEntityGroup[] selectedEntityGroups = new IEntityGroup[selectedGroups.size()];
      Iterator iter = selectedGroups.iterator();
      for (int i = 0; iter.hasNext(); i++) {
        String key = (String)iter.next();
        IEntityGroup group = GroupService.findGroup(key.substring(groupIDPrefix.length()));
        if (group != null)
          selectedEntityGroups[i] = group;
        else
          throw new GroupsException("Unable to find group '" + key + "'");
      }
      return selectedEntityGroups;
    }
    protected void setSelectedGroups(Set selectedGroups) { this.selectedGroups = selectedGroups; }
    protected void addSelectedGroup(String group) { selectedGroups.add(group); }
    protected void addSelectedGroup(IEntityGroup group) { selectedGroups.add(groupIDPrefix + group.getKey()); }
    protected void removeGroup(String group) { selectedGroups.remove(group); }

    protected Element toXML() {
      Element userSettingsE = emptyDoc.createElement("userSettings");
      Element browsingGroupE = emptyDoc.createElement("browsingGroup");
      browsingGroupE.appendChild(emptyDoc.createTextNode(browsingGroup));
      userSettingsE.appendChild(browsingGroupE);

      // Add selected groups if there are any
      if (selectedGroups.size() > 0) {
        Element selectedGroupsE = emptyDoc.createElement("selectedGroups");
        Iterator iter = selectedGroups.iterator();
        while (iter.hasNext()) {
          Element selectedGroupE = emptyDoc.createElement("selectedGroup");
          selectedGroupE.appendChild(emptyDoc.createTextNode((String)iter.next()));
          selectedGroupsE.appendChild(selectedGroupE);
        }
        userSettingsE.appendChild(selectedGroupsE);
      }

      return userSettingsE;
    }

    // This method needs some caching!
    protected Document getGroups() throws GroupsException {
      Document groupsDoc = DocumentFactory.getNewDocument();
      Element groupsE = groupsDoc.createElement("groups");

      IEntityGroup everyoneGroup = GroupService.getDistinguishedGroup(GroupService.EVERYONE);

      // Create a top-level group representing everyone
      Element everyoneGroupE = groupsDoc.createElement("group");
      everyoneGroupE.setAttribute("ID", groupIDPrefix + everyoneGroup.getKey());
      everyoneGroupE.setAttribute("name", everyoneGroup.getName());
      everyoneGroupE.setAttribute("description", everyoneGroup.getDescription());
      groupsE.appendChild(everyoneGroupE);

      processGroupsRecursively(everyoneGroup, everyoneGroupE);
      groupsDoc.appendChild(groupsE);
      return groupsDoc;
    }

    protected void processGroupsRecursively(IEntityGroup group, Element parentGroup) throws GroupsException {
      Iterator iter = group.getMembers();
      while (iter.hasNext()) {
        IGroupMember member = (IGroupMember)iter.next();
        if (member.isGroup()) {
          IEntityGroup memberGroup = (IEntityGroup)member;
          String key = memberGroup.getKey();
          String name = memberGroup.getName();
          String description = memberGroup.getDescription();

          // Create group element and append it to its parent
          Document groupsDoc = parentGroup.getOwnerDocument();
          Element groupE = groupsDoc.createElement("group");
          groupE.setAttribute("ID", groupIDPrefix + key);
          groupE.setAttribute("name", name);
          groupE.setAttribute("description", description);
          parentGroup.appendChild(groupE);
          processGroupsRecursively(memberGroup, groupE);
        }
      }
    }
  }

}


