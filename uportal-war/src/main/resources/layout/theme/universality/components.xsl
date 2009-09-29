<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!--
 | This file determines the presentation of UI components of the portal.
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
	
  
  <!-- ========== TEMPLATE: PORTAL PIPE ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders a pipe ( | ), generally used to separate links.
  -->
  <xsl:template name="portal.pipe">
		<span class="portal-pipe">|</span> 
  </xsl:template>
  <!-- =========================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR ========== -->
  <!-- =============================================== -->
  <!--
   | This template renders the portal page bar.
  -->
  <xsl:template name="portal.page.bar">  
    <div id="portalPageBar">
      <xsl:choose>
        <xsl:when test="//focused">
          <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
          <xsl:call-template name="portal.page.bar.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
          <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
        </xsl:when>
        <xsl:otherwise>
          <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
          <xsl:call-template name="portal.page.bar.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
          <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
        </xsl:otherwise>
      </xsl:choose>
      <div id="portalPageBarLinks">
      	<xsl:choose>
          <xsl:when test="//focused">
            <!-- ****** PORTAL PAGE BAR LINKS FOCUSED BLOCK ****** -->
            <xsl:call-template name="portal.page.bar.links.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** PORTAL PAGE BAR LINKS FOCUSED BLOCK ****** -->
          </xsl:when>
          <xsl:otherwise>
            <!-- ****** PORTAL PAGE BAR LINKS BLOCK ****** -->
            <xsl:call-template name="portal.page.bar.links.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** PORTAL PAGE BAR LINKS BLOCK ****** -->
          </xsl:otherwise>
        </xsl:choose>
        <!-- ????? THIS CHANNEL IS OBSOLETE WITH THE HEADER BLOCK IMPLEMENTATION ?????
        <xsl:copy-of select="channel[@name='Header']"/> -->
      </div>
    </div>
  </xsl:template>
  <!-- =============================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR TITLE ========== -->
  <!-- ===================================================== -->
  <!--
   | This template renders the portal page bar title.
  -->
  <xsl:template name="portal.page.bar.title">
  	<xsl:choose>
      <xsl:when test="//focused">
        <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
        <xsl:call-template name="portal.page.bar.title.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** PORTAL PAGE BAR TITLE FOCUSED BLOCK ****** -->
      </xsl:when>
      <xsl:otherwise>
        <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
        <xsl:call-template name="portal.page.bar.title.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
        <!-- ****** PORTAL PAGE BAR TITLE BLOCK ****** -->
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>
  <!-- ===================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK HOME ========== -->
  <!-- ========================================================== -->
  <!--
   | This template renders the home link into the portal page bar title.
  -->
  <xsl:template name="portal.page.bar.link.home">
  	<span id="portalPageBarHome">
      <a href="{$HOME_ACTION_URL}">
        <xsl:attribute name="title">
          <xsl:choose>
            <xsl:when test="//focused">
              <xsl:value-of select="$TOKEN[@name='BACK_HOME_LONG_LABEL']"/> <!-- Use the Back to Home label for focused view -->
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$TOKEN[@name='HOME_LONG_LABEL']"/> <!-- Otherwise, just Home label -->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <span>
          <xsl:choose>
            <xsl:when test="//focused">
              <xsl:value-of select="$TOKEN[@name='BACK_HOME_LABEL']"/> <!-- Use the Back to Home label for focused view -->
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$TOKEN[@name='HOME_LABEL']"/> <!-- Otherwise, just Home label -->
            </xsl:otherwise>
          </xsl:choose>
        </span>
      </a>
      <xsl:call-template name="portal.pipe" />
    </span>
  </xsl:template>
  <!-- ========================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK ADMIN ========== -->
  <!-- ========================================================== -->
  <!--
   | This template renders the admin menu into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.admin">
  	<xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin')">
    	<span id="portalPageBarAdmin">
    	  <xsl:variable name="portletAdminUrl">
    	    <portlet:url fname="portlet-admin" state="MAXIMIZED"/>
    	  </xsl:variable>
    	  <a href="{$portletAdminUrl}" title="{$TOKEN[@name='CHANNEL_MANAGER_LONG_LABEL']}">
          <span><xsl:value-of select="$TOKEN[@name='CHANNEL_MANAGER_LABEL']"/></span>
        </a>
        <xsl:call-template name="portal.pipe" />
      </span>
    </xsl:if>
  </xsl:template>
  <!-- ========================================================== -->
  

  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK CUSTOMIZE ========== -->
  <!-- ============================================================== -->
  <!--
   | This template renders the customize link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.customize">
    <xsl:if test="$AUTHENTICATED='true'">
    	<span id="portalPageBarCustom">
    	  <xsl:variable name="portalPrefsUrl">
    	    <portlet:url fname="portal_userpreferences_dlm" state="MAXIMIZED"/>
    	  </xsl:variable>
    	  <a href="{$portalPrefsUrl}" title="{$TOKEN[@name='TURN_ON_PREFERENCES_LONG_LABEL']}">
          <span><xsl:value-of select="$TOKEN[@name='TURN_ON_PREFERENCES_LABEL']"/></span>
        </a>
        <xsl:call-template name="portal.pipe" />
      </span>
    </xsl:if>
  </xsl:template>
  <!-- ============================================================== -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK SITEMAP ========== -->
  <!-- ============================================================ -->
  <!--
   | This template renders the sitemap link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.sitemap">
    <xsl:if test="$AUTHENTICATED='true'">
    	<span id="portalPageBarSitemap">
    	  <xsl:variable name="layoutSitemapUrl">
    	    <portlet:url fname="layout-sitemap" state="MAXIMIZED"/>
    	  </xsl:variable>
    	  <a href="{$layoutSitemapUrl}" title="{$TOKEN[@name='SITEMAP_LONG_LABEL']}">
          <span><xsl:value-of select="$TOKEN[@name='SITEMAP_LABEL']"/></span>
        </a>
        <xsl:call-template name="portal.pipe" />
      </span>
    </xsl:if>
  </xsl:template>
  <!-- ============================================================ -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK HELP ========== -->
  <!-- ========================================================= -->
  <!--
   | This template renders the help link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.help">
  	<span id="portalPageBarHelp">
      <a href="{$HELP_URL}" title="{$TOKEN[@name='HELP_LONG_LABEL']}" target="_blank">
        <span><xsl:value-of select="$TOKEN[@name='HELP_LABEL']"/></span>
      </a>
      <xsl:call-template name="portal.pipe" />
    </span>
  </xsl:template>
  <!-- ========================================================= -->
  
  
  <!-- ========== TEMPLATE: PORTAL PAGE BAR LINK LOGOUT ========== -->
  <!-- ========================================================= -->
  <!--
   | This template renders the logout link into the portal page bar.
  -->
  <xsl:template name="portal.page.bar.link.logout">
    <xsl:if test="$AUTHENTICATED='true'">
    	<span id="portalPageBarLogout">
        <a href="Logout" title="{$TOKEN[@name='LOGOFF_LONG_LABEL']}">
          <span><xsl:value-of select="$TOKEN[@name='LOGOFF_LABEL']"/></span>
        </a>
        <xsl:call-template name="portal.pipe" />
      </span>
    </xsl:if>
  </xsl:template>
  <!-- ========================================================= -->
  
  
  <!-- ========== TEMPLATE: LOGO ========== -->
  <!-- ==================================== -->
  <!--
   | This template renders the portal logo.
  -->
  <xsl:template name="logo">  
    <div id="portalLogo">
      <a href="{$HOME_ACTION_URL}" title="{$TOKEN[@name='LOGO_TITLE']}">
        <xsl:choose>
          <xsl:when test="//focused">
            <!-- ****** LOGO FOCUSED BLOCK ****** -->
            <xsl:call-template name="logo.focused.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** LOGO FOCUSED BLOCK ****** -->
          </xsl:when>
          <xsl:otherwise>
            <!-- ****** LOGO BLOCK ****** -->
            <xsl:call-template name="logo.block"/> <!-- Calls a template of institution custom content from universality.xsl. -->
            <!-- ****** LOGO BLOCK ****** -->
          </xsl:otherwise>
        </xsl:choose>
      </a>
    </div>
  </xsl:template>
  <!-- ==================================== -->
  
  
  <!-- ========== TEMPLATE: LOGIN ========== -->
  <!-- ===================================== -->
  <!--
   | This template renders the login form.
  -->
  <xsl:template name="login">  
    <xsl:if test="$AUTHENTICATED='false'">
      <div id="portalLogin">
        <div id="portalLoginInner">
          <div id="portalLoginHeader">
            <div id="portalLoginHeaderInner">
              <h3><xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_HEADER']"/></h3>
            </div>
          </div>
          <div id="portalLoginBody">
            <div id="portalLoginBodyInner">
              <form method="post" action="/uP3/j_acegi_security_check">
                <fieldset>
                  <legend><xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_LEGEND']"/></legend>
                  <ol>
                    <li><label class="portal-login-username"><span><xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_USERNAME_LABEL']"/></span><input type="text" name="j_username" title="{$TOKEN[@name='WELCOME_LOGIN_USERNAME_FIELD_TITLE']}"/></label></li>
                    <li><label class="portal-login-password"><span><xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_PASSWORD_LABEL']"/></span><input type="password" name="j_password" title="{$TOKEN[@name='WELCOME_LOGIN_PASSWORD_FIELD_TITLE']}"/></label></li>
                  </ol>
                </fieldset>
                <div class="portal-login-controls-submit">
                	<input type="submit" value="{$TOKEN[@name='WELCOME_LOGIN_BUTTON_LABEL']}"/>
                </div>
              </form>
              <div id="portalLoginHelp">
              	<a href="{$LOGIN_HELP_URL}" title="{$TOKEN[@name='WELCOME_LOGIN_HELP_LONG_LABEL']}">
                	<xsl:value-of select="$TOKEN[@name='WELCOME_LOGIN_HELP_LABEL']"/>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ===================================== -->
  
  
  <!-- ========== TEMPLATE: LOGIN CHANNEL ========== -->
  <!-- ============================================= -->
  <!--
   | This template renders the CLogin channel.
  -->
  <xsl:template name="login.channel">
    <xsl:copy-of select="//channel[@name='Login']"/>
  </xsl:template>
  <!-- ============================================= -->
  
  
  <!-- ========== TEMPLATE: CAS LOGIN ========== -->
  <!-- ========================================= -->
  <!--
   | This template renders links for CAS login.
  -->
  <xsl:template name="cas.login">  
    <xsl:if test="$AUTHENTICATED='false'">
      <div id="portalLogin">
      	<div id="portalLoginInner">
          <div id="portalCASLogin">
            <a id="portalCASLoginLink" href="{$CAS_LOGIN_URL}" title="{$TOKEN[@name='CAS_LOGIN_LONG_LABEL']}">
            	<span><xsl:value-of select="$TOKEN[@name='CAS_LOGIN_LABEL']"/></span>
            </a>
            <a id="portalCASLoginNewLink" href="{$CAS_NEW_USER_URL}" title="{$TOKEN[@name='CAS_NEW_USER_LONG_LABEL']}">
            	<span><xsl:value-of select="$TOKEN[@name='CAS_NEW_USER_LABEL']"/></span>
            </a>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ========================================= -->
  
  
  <!-- ========== TEMPLATE: WELCOME ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders links for CAS login.
  -->
  <xsl:template name="welcome">  
    <xsl:if test="$AUTHENTICATED='true'"> <!-- Welcome only displays if the user is logged in. -->
      <div id="portalWelcome">
        <div id="portalWelcomeInner">
          <p><xsl:value-of select="$TOKEN[@name='WELCOME_PRE']"/><xsl:value-of select="$USER_NAME"/><xsl:value-of select="$TOKEN[@name='WELCOME_POST']"/>
          <span class="logout-label"><a href="Logout" title="{$TOKEN[@name='LOGOFF_LONG_LABEL']}"><xsl:value-of select="$TOKEN[@name='LOGOFF_LABEL']"/></a></span>
          </p>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ======================================= -->
  
  
  <!-- ========== TEMPLATE: ADMINISTRATION LINKS ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the administration links navigation component, a prominent, short list of links to administrative functions.
  -->
  <xsl:template name="administration.links">
  	<xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin')">
      <div id="portalAdminLinks" class="fl-widget">
        <div class="fl-widget-inner">
        	<div class="fl-widget-titlebar">
          	<h2><xsl:value-of select="$TOKEN[@name='ADMIN_LINKS_LABEL']"/></h2>
          </div>
          <div class="fl-widget-content">
            <ul class="fl-listmenu">
              <xsl:if test="upAuth:canRender($USER_ID, 'portlet-admin')">
                <li id="portalAdminLinksPortletAdmin">
                  <xsl:variable name="portletAdminUrl">
                    <portlet:url fname="portlet-admin" state="MAXIMIZED"/>
                  </xsl:variable>
                  <a href="{$portletAdminUrl}" title="{$TOKEN[@name='CHANNEL_MANAGER_LONG_LABEL']}">
                    <span><xsl:value-of select="$TOKEN[@name='CHANNEL_MANAGER_LABEL']"/></span>
                  </a>
                </li>
              </xsl:if>
              <li id="portalAdminLinksCustomize">
                <xsl:variable name="portalPrefsUrl">
                  <portlet:url fname="portal_userpreferences_dlm" state="MAXIMIZED"/>
                </xsl:variable>
                <a href="{$portalPrefsUrl}" id="portalPageBarCustom" title="{$TOKEN[@name='TURN_ON_PREFERENCES_LONG_LABEL']}">
                  <span><xsl:value-of select="$TOKEN[@name='TURN_ON_PREFERENCES_LABEL']"/></span>
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ========================================== -->
  
  <!-- ========== TEMPLATE: QUICKLINKS ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the quicklinks navigation component, a prominent, short list of links to high priority portlets regardless of the portlet's placement within the layout.
  -->
  <xsl:template name="quicklinks">
  	<xsl:if test="count(/layout/navigation/descendant::tabChannel[@quicklink > 0]) > 0"> <!-- Write out markup only if one or more quicklinks exist. -->
      <div id="portalQuicklinks" class="fl-widget">
      	<div class="fl-widget-inner">
        	<div class="fl-widget-titlebar">
        		<h2><xsl:value-of select="$TOKEN[@name='QUICKLINKS_LABEL']"/></h2>
          </div>
        	<div class="fl-widget-content">
            <ul class="fl-listmenu">  <!-- Navigation list. -->
              <xsl:apply-templates select="/layout/navigation/descendant::tabChannel[@quicklink > 0]" mode="quicklink"> <!-- Selects from the XML only those portlets with the matching quicklink parameter. -->
                <xsl:sort select="@quicklink" order="ascending" /> <!-- Sorts the subsequent array in ascending order by the value of the quicklink parameter. -->
              </xsl:apply-templates>
            </ul>
      		</div>
        </div>
      </div>
    </xsl:if>
  </xsl:template>
  <!-- ========================================== -->
	
  
  <!-- ========== TEMPLATE: QUICKLINKS LIST ========== -->
  <!-- =============================================== -->
  <!-- 
   | This template renders quicklinks navigation list.
  -->
  <xsl:template match="tabChannel" mode="quicklink">
    <xsl:variable name="qLinkID" select="@qID" /> <!-- Pull the unique ID from the portlet parameter in the XML if one exists. -->
    <xsl:variable name="POSITION"> <!-- Determine the position of the navigation option within the whole navigation list and add css hooks for the first and last positions. -->
      <xsl:choose>
        <xsl:when test="position()=1 and position()=last()">single</xsl:when>
        <xsl:when test="position()=1">first</xsl:when>
        <xsl:when test="position()=last()">last</xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <li id="{$qLinkID}" class="{$POSITION}"> <!-- Each subnavigation menu item.  The unique ID can be used in the CSS to give each menu item a unique icon, color, or presentation. -->
      <xsl:variable name="subNavUrl">
        <portlet:url layoutId="{@ID}" state="MAXIMIZED"/>
      </xsl:variable>
      <a href="{$subNavUrl}" title="{@name}">  <!-- Navigation item link. -->
        <span>
          UP:CHANNEL_TITLE-{<xsl:value-of select="@ID" />}
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- =============================================== -->
	
  
  <!-- ========== TEMPLATE: WEB SEARCH ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the web search component, a search form that runs a search query on the selected search engine.
   | The list of search engines may be modified in the script file "search.js", which is located in webpages/media/skins/javascript.
  -->
  <xsl:template name="web.search">
    <div id="webSearchContainer" class="fl-widget">
      <div class="fl-widget-inner">
      	<div class="fl-widget-titlebar">
      		<h2><label for="webSearchInput"><xsl:value-of select="$TOKEN[@name='WEB_SEARCH_TITLE']"/></label></h2>
        </div>
        <div class="fl-widget-content">
					<script language="JavaScript" type="text/javascript">
            var skinPath='<xsl:value-of select="$SKIN_PATH"/>/<xsl:value-of select="$SKIN"/>/';
          </script>
          <script type="text/javascript" language="JavaScript" src="{$SCRIPT_PATH}/uportal/cookies.min.js">
            // Included JS file
          </script>
          <script type="text/javascript" language="JavaScript" src="{$SCRIPT_PATH}/uportal/search.min.js">
            // Included JS file
          </script>
          <noscript>
            <form target="_parent" method="get" action="http://www.google.com/search" id="webSearchForm">
              <input id="webSearchInput" value="" name="q" type="text" />
              <input id="webSearchSubmit" type="submit" name="submit" value="Google Search" />
            </form>
          </noscript>
        </div>
      </div>
    </div>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: BREADCRUMB ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the page breadcrumb.
  -->
  <xsl:template name="breadcrumb">
      <div id="portalPageBodyBreadcrumb">
      <xsl:variable name="basePortalUrl">
        <portal:url/>
      </xsl:variable>
      <a href="{$basePortalUrl}" title="{$TOKEN[@name='HOME_LONG_LABEL']}"><xsl:value-of select="$TOKEN[@name='HOME_LABEL']"/></a>
      <span class="breadcrumb-separator">&gt;</span>
      <xsl:for-each select="/layout/navigation/tab">
        <xsl:if test="@activeTab='true'">
          <xsl:variable name="tabUrl">
            <layout:url layoutId="{@ID}"/>
          </xsl:variable>
          <a href="{$tabUrl}">
            <xsl:attribute name="title">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:value-of select="@name"/>
          </a>
        </xsl:if>
      </xsl:for-each>
      <span class="breadcrumb-separator">&gt;</span>
    </div>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: PAGE TITLE ========== -->
  <!-- ========================================== -->
  <!--
   | This template renders the page title.
  -->
  <xsl:template name="page.title">
  	<a name="startContent" title="Reference anchor: the starting point of the page content"> <!-- Skip navigation target. -->
      <h1 id="portalPageBodyTitle">
        <xsl:choose>
          <xsl:when test="//focused"> <!-- When focused, include the focused portlet title -->
            UP:CHANNEL_TITLE-{<xsl:value-of select="//focused/channel/@ID" />}
          </xsl:when>
          <xsl:otherwise> <!-- Otherwise, just the current tab name -->
            <xsl:value-of select="/layout/navigation/tab[@activeTab='true']/@name"/>
          </xsl:otherwise>
        </xsl:choose>
      </h1>
    </a>
  </xsl:template>
  <!-- ========================================== -->
  
  
  <!-- ========== TEMPLATE: BACK TO HOME ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders Back To Home link form the focused view.
  -->
  <xsl:template name="back.to.home">
  	<xsl:if test="//focused">
      <a href="{$HOME_ACTION_URL}" id="portalBackToHome" title="{$TOKEN[@name='BACK_HOME_LONG_LABEL']}">
        <span><xsl:value-of select="$TOKEN[@name='BACK_HOME_LABEL']"/></span>
      </a>
  	  <xsl:if test="//focused[@in-user-layout='no'] and $USE_AJAX='true' and $AUTHENTICATED">
  	    <a href="javascript:;" id="focusedContentDialogLink" title="{$TOKEN[@name='PREFERENCES_LINK_ADD_FOCUSED_CONTENT_LONG_LABEL']}">
  	      <span><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINK_ADD_FOCUSED_CONTENT_LABEL']"/></span>
  	    </a>
 	    </xsl:if>
  	</xsl:if>
  </xsl:template>
  <!-- ============================================ -->
  
  
  <!-- ========== TEMPLATE: CUSTOMIZE LINKS ========== -->
  <!-- =============================================== -->
  <!--
   | This template renders customization links.
  -->
  <xsl:template name="customize.links">
      <xsl:if test="$AUTHENTICATED='true' and $USE_AJAX='true' and $AUTHENTICATED"> <!-- Currently, AJAX must be enabled for these links to function. -->
        <div id="portalCustomizationLinks" class="fl-widget">
        	<div class="fl-widget-inner">
          	<div class="fl-widget-titlebar">
            	<h2><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINKS_TITLE']"/></h2>
            </div>
            <div class="fl-widget-content">
              <ul class="fl-listmenu">
                <xsl:if test="count(//navigation/tab) > 0">
                  <li id="portalCustomizationLinksAddContent">
                    <a id="contentDialogLink" href="javascript:;" title="{$TOKEN[@name='PREFERENCES_LINK_ADD_CONTENT_LONG_LABEL']}">
                      <span><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINK_ADD_CONTENT_LABEL']"/></span>
                    </a>
                    <xsl:call-template name="portal.pipe"/>
                  </li>
                  <li id="portalCustomizationLinksChangeLayout">
                    <a id="layoutDialogLink" href="javascript:;" title="{$TOKEN[@name='PREFERENCES_LINK_LAYOUT_LONG_LABEL']}">
                      <span><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINK_LAYOUT_LABEL']"/></span>
                    </a>
                    <xsl:call-template name="portal.pipe"/>
                  </li>
                </xsl:if>
                <li id="portalCustomizationLinksChooseSkin">
                  <a id="skinDialogLink" href="javascript:;" title="{$TOKEN[@name='PREFERENCES_LINK_SKINS_LONG_LABEL']}">
                    <span><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINK_SKINS_LABEL']"/></span>
                  </a>
                  <xsl:call-template name="portal.pipe"/>
                </li>
                <li id="portalCustomizationLinksAddTab">
                  <a id="addTabLink" href="javascript:;" title="{$TOKEN[@name='PREFERENCES_LINK_ADD_TAB_LONG_LABEL']}">
                    <span><xsl:value-of select="$TOKEN[@name='PREFERENCES_LINK_ADD_TAB_LABEL']"/></span>
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </xsl:if>
  </xsl:template>
  <!-- =============================================== -->
  
		
</xsl:stylesheet>
