<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!--
 | This file determines the presentation of the portal preferences interface.
 | The file is imported by the base stylesheet universality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to universality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<!-- ============================================= -->
<!-- ========== STYLESHEET DELCARATION =========== -->
<!-- ============================================= -->
<!-- 
 | RED
 | This statement defines this document as XSL and declares the Xalan extension
 | elements used for URL generation and permissions checks.
 |
 | If a change is made to this section it MUST be copied to all other XSL files
 | used by the theme
-->
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xalan="http://xml.apache.org/xalan" 
  xmlns:dlm="http://www.uportal.org/layout/dlm"
  xmlns:portal="http://www.jasig.org/uportal/XSL/portal"
  xmlns:portlet="http://www.jasig.org/uportal/XSL/portlet"
  xmlns:layout="http://www.jasig.org/uportal/XSL/layout"
  xmlns:upAuth="xalan://org.jasig.portal.security.xslt.XalanAuthorizationHelper"
  xmlns:upGroup="xalan://org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
  extension-element-prefixes="portal portlet layout" 
  exclude-result-prefixes="xalan portal portlet layout upAuth upGroup" 
  version="1.0">
  
  <xalan:component prefix="portal" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortalUrlXalanElements" />
  </xalan:component>
  <xalan:component prefix="portlet" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortletUrlXalanElements" />
  </xalan:component>
  <xalan:component prefix="layout" elements="url param">
    <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.LayoutUrlXalanElements" />
  </xalan:component>
<!-- ============================================= -->
  
 <xsl:template name="preferences">
  
  <xsl:choose>
   
   <xsl:when test="//focused[@in-user-layout='no'] and upGroup:isChannelDeepMemberOf(//focused/channel/@fname, 'local.1')">
    <div id="ajaxMenus" style="display:none;">
     <!-- Add Channel Menu -->
     <div id="focusedContentAddingDialog" title="{$TOKEN[@name='AJAX_ADD_FOCUSED_PORTLET_DIALOG_TITLE']}">
      <form>
       <fieldset>
        <legend><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_TO_LAYOUT']"/></legend>
        <xsl:for-each select="/layout/navigation/tab">
         <input name="targetTab" id="targetTab{@ID}" value="{@ID}" type="radio"/> <label for="targetTab{@ID}" class="portlet-form-field-label"><xsl:value-of select="@name"/></label><br/>
        </xsl:for-each>
       </fieldset>
       <p>
        <input name="channelId" type="hidden" value="{//focused/channel/@chanID}"/>
        <input type="submit" value="{$TOKEN[@name='AJAX_ADD_FOCUSED_PORTLET_SUBMIT_BUTTON']}" class="portlet-form-button"/>&#160;
       </p>
      </form>
     </div>
    </div>
    <script type="text/javascript">
     
     up.jQuery(document).ready(function(){
       up.jQuery.uportal.UportalLayoutManager(
       {
          portalUrl: 'TODO_UP-2045 was BASE_ACTION_URL',
          mediaPath: '<xsl:value-of select="$MEDIA_PATH"/>',
          currentSkin: '<xsl:value-of select="$SKIN"/>',
          isFocusMode: true
       });
     });
     
    </script>
   </xsl:when>
   
   <xsl:when test="not(//focused)">
  
    <div id="dojoMenus" style="display:none;">
     <!-- Add Channel Menu -->
     <div id="contentAddingDialog" title="{$TOKEN[@name='AJAX_ADD_PORTLET_DIALOG_TITLE']}">
        <div id="channelAddingTabs">
         <ul>
          <li><a href="#channel-tab-1"><span><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_BROWSE']"/></span></a></li>
          <li><a href="#channel-tab-2"><span><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_SEARCH']"/></span></a></li>
         </ul>
         <div id="channel-tab-1">
          <h4 id="channelLoading"><xsl:value-of select="$TOKEN[@name='AJAX_LOADING_PORTLET_LIST']"/></h4>
         <table cellspacing="0" cellpadding="0" border="0">
          <tr>
           <td class="portlet-section-subheader"><label for="categorySelectMenu"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_CATEGORY_COLUMN']"/></label></td>
           <td class="portlet-section-subheader"><label for="channelSelectMenu"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_CATEGORY_PORTLET']"/></label></td>
          </tr>
          <tr>
           <td><select id="categorySelectMenu" size="14" style="width: 150px; background: url({$SKIN_PATH}/images/loading.gif) no-repeat center center"></select></td>
           <td><select id="channelSelectMenu" size="14" style="width: 300px; background: url({$SKIN_PATH}/images/loading.gif) no-repeat center center"></select></td>
          </tr>
         </table>
         </div>
         <div id="channel-tab-2">
         <p>
          <label class="portlet-form-field-label" for="addChannelSearchTerm"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_SEARCH_LABEL']"/></label>&#160;
          <input id="addChannelSearchTerm" type="text"/>
         </p>
         <h3><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_SEARCH_RESULTS_TITLE']"/></h3>
         <ul id="addChannelSearchResults" style="list-style-type: none; list-style-image: none; padding: 0px; margin-left: 5px; max-height: 160px; overflow: auto"></ul>
         </div>
        </div>
      <h3 class="portal-section-header"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_DETAILS_TITLE']"/></h3>
      <h4 id="channelTitle" class="portal-section-subheader"></h4>
      <p id="channelDescription"></p>
      <p style="padding-top: 10px;">
       <input id="addChannelId" type="hidden"/>
       <button id="addChannelLink" class="portlet-form-button"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_ADD_BUTTON']"/></button>&#160;
       <button id="previewChannelLink" class="portlet-form-button"><xsl:value-of select="$TOKEN[@name='AJAX_ADD_PORTLET_USE_BUTTON']"/></button>&#160;
      </p>
     </div>
     
     <div id="pageLayoutDialog" title="{$TOKEN[@name='AJAX_EDIT_PAGE_DIALOG_TITLE']}">
      <form>
      <p>
       <label for="pageName" class="portlet-form-label"><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_PAGE_NAME']"/></label><br/>
       <input id="pageName" name="pageName" type="text" size="20" value="{/layout/navigation/tab[@activeTab='true']/@name}"/>
      </p>
      <hr/>
      <fieldset>
       <legend><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_PAGE_LAYOUT']"/></legend>
       <table id="changeColumns" style="width: 100%">
        <tr>
         <td><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_COLUMN_TITLE_1']"/></td>
         <td><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_COLUMN_TITLE_2']"/></td>
         <td><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_COLUMN_TITLE_3']"/></td>
         <td><xsl:value-of select="$TOKEN[@name='AJAX_EDIT_PAGE_COLUMN_TITLE_4']"/></td>
        </tr>
        <tr>
         <td>
          <input type="radio" name="layoutChoice" value="100" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_100']}"/>
          <img src="{$SKIN_PATH}/images/layout_100.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_100']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_100']}"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="50-50" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_50_50']}"/>
          <img src="{$SKIN_PATH}/images/layout_50-50.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_50_50']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_50_50']}"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="33-34-33" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_33_34_33']}"/>
          <img src="{$SKIN_PATH}/images/layout_33-34-33.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_33_34_33']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_33_34_33']}"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-25-25-25" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_25_25_25']}"/>
          <img src="{$SKIN_PATH}/images/layout_25-25-25-25.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_25_25_25']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_25_25_25']}"/>
         </td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="40-60" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_40_60']}"/>
          <img src="{$SKIN_PATH}/images/layout_40-60.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_40_60']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_40_60']}"/>
         </td>
         <td>
          <input type="radio" name="layoutChoice" value="25-50-25" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_50_25']}"/>
          <img src="{$SKIN_PATH}/images/layout_25-50-25.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_50_25']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_25_50_25']}"/>
         </td>
         <td></td>
        </tr>
        <tr>
         <td></td>
         <td>
          <input type="radio" name="layoutChoice" value="60-40" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_60_40']}"/>
          <img src="{$SKIN_PATH}/images/layout_60-40.gif" alt="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_60_40']}" title="{$TOKEN[@name='AJAX_EDIT_PAGE_LAYOUT_OPTION_60_40']}"/>
         </td>
         <td></td>
         <td></td>
        </tr>
       </table>
       </fieldset>
       <input type="submit" value="{$TOKEN[@name='AJAX_EDIT_PAGE_SUBMIT_BUTTON']}" class="portlet-form-button"/>
      </form>
     </div>	
  
     <div id="skinChoosingDialog" title="{$TOKEN[@name='AJAX_SELECT_SKIN_DIALOG_TITLE']}">
      <form>
       <h4 id="skinLoading"><xsl:value-of select="$TOKEN[@name='AJAX_SELECT_SKIN_LOADING_MESSAGE']"/></h4>
       <p class="portlet-form-label">
        <xsl:value-of select="$TOKEN[@name='AJAX_SELECT_SKIN_TITLE']"/>
       </p>
       <div id="skinList"></div>
       <p>
        <input type="submit" value="{$TOKEN[@name='AJAX_SELECT_SKIN_SUBMIT_BUTTON']}" class="portlet-form-button"/>&#160;
       </p>
      </form>
     </div>
     
     <div id="portalDropWarning" style="display:none;">
      <p><xsl:value-of select="$TOKEN[@name='AJAX_PORTAL_DROP_WARNING_MESSAGE']"/></p>
     </div>
     
    </div>
    <script type="text/javascript">
       up.jQuery(document).ready(function(){
          up.jQuery.uportal.UportalLayoutManager(
            {
              portalUrl: 'TODO_UP-2045 was BASE_ACTION_URL',
              mediaPath: '<xsl:value-of select="$MEDIA_PATH"/>',
              currentSkin: '<xsl:value-of select="$SKIN"/>',
              messages: { 
                  confirmRemoveTab: '<xsl:value-of select="$TOKEN[@name='AJAX_REMOVE_TAB_CONFIRMATION_MESSAGE']"/>', 
                  confirmRemovePortlet: '<xsl:value-of select="$TOKEN[@name='AJAX_REMOVE_PORTLET_CONFIRMATION_MESSAGE']"/>' 
              }
            }
          );
       });
    </script>
      
   </xsl:when>
  </xsl:choose>
 </xsl:template>
 
</xsl:stylesheet>
