<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!--
 | This file determines the presentation of the main navigation systems of the portal.
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
    xmlns:upAuth="xalan://org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="xalan://org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    extension-element-prefixes="portal portlet" 
    exclude-result-prefixes="xalan portal portlet upAuth upGroup" 
    version="1.0">

    <xalan:component prefix="portal" elements="url param">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortalUrlXalanElements" />
    </xalan:component>
    <xalan:component prefix="portlet" elements="url param">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.url.xml.PortletUrlXalanElements" />
    </xalan:component>
<!-- ============================================= -->
  
  
  <!-- ========== TEMPLATE: BODY COLUMNS ========== -->
  <!-- ============================================ -->
  <!--
   | This template renders the columns of the page body.
  -->
  <xsl:template name="columns">
  	<div id="portalPageBodyColumns">
    	<xsl:attribute name="class"> <!-- Write appropriate FSS class based on number of columns to produce column layout. -->
        <xsl:choose>
          <xsl:when test="$COLUMNS=1">
          	fl-col-flex
          </xsl:when>
          <xsl:otherwise>
            fl-col-flex<xsl:value-of select="$COLUMNS" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:for-each select="column">
        <xsl:variable name="POSITION"> <!-- Determine column place in the layout and add appropriate class. -->
          <xsl:choose>
            <xsl:when test="position()=1 and position()=last()">single</xsl:when>
            <xsl:when test="position()=1">left</xsl:when>
            <xsl:when test="position()=last()">right</xsl:when>
            <xsl:when test="$COLUMNS=3 and position()=2">middle</xsl:when>
            <xsl:otherwise>column<xsl:value-of select="$COLUMNS" /></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="COLUMN_WIDTH">
        	<xsl:choose>
            <xsl:when test="@width = '100%'"></xsl:when>
            <xsl:otherwise><xsl:value-of select="floor(substring-before(@width,'%'))" /></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <div id="column_{@ID}" class="portal-page-column {$POSITION} fl-col-flex{$COLUMN_WIDTH}"> <!-- Unique column_ID needed for drag and drop. -->
          <div id="inner-column_{@ID}" class="portal-page-column-inner"> <!-- Column inner div for additional presentation/formatting options.  -->
            <xsl:apply-templates select="channel"/> <!-- Render the column's portlets.  -->
          </div>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>
  <!-- ============================================ -->
  
  
  <!-- ========== TEMPLATE: SIDEBAR ========== -->
  <!-- =========================================== -->
  <!--
   | This template renders the left navigation column of the page body.
  -->
  <xsl:template name="sidebar">
  	<div id="portalSidebar">
    	<xsl:variable name="FSS_SIDEBAR_LOCATION_CLASS">
      	<xsl:call-template name="sidebar.location" /> <!-- Template located in page.xsl. -->
      </xsl:variable>
    	<xsl:attribute name="class">
      	<xsl:choose>
          <xsl:when test="$AUTHENTICATED='true'">
            <xsl:choose>
              <xsl:when test="$PORTAL_VIEW='focused'">
              	fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/> <!-- when focused -->
              </xsl:when>
              <xsl:otherwise>
              	fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/> <!-- when dashboard -->
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            fl-col-fixed fl-force-<xsl:value-of select="$FSS_SIDEBAR_LOCATION_CLASS"/> <!-- when logged out -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <div id="portalSidebarInner"> <!-- Inner div for additional presentation/formatting options. -->
      	
        <xsl:choose>
          <xsl:when test="$AUTHENTICATED='true'">
          
            <xsl:choose>
              <xsl:when test="$PORTAL_VIEW='focused'">
              
                <!-- Sidebar when a portlet is focused. -->
                <xsl:if test="$USE_SIDEBAR_FOCUSED='true'">
                  <!-- ****** CONTENT SIDEBAR BLOCK ****** -->
                  <xsl:call-template name="content.sidebar.focused.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
                  <!-- ****** CONTENT SIDEBAR FOCUSED BLOCK ****** -->
                </xsl:if>
                
              </xsl:when>
              <xsl:otherwise>
                
                <!-- Sidebar when in dashboard. -->
                <xsl:if test="$USE_SIDEBAR='true'">
                  <!-- ****** SIDEBAR BLOCK ****** -->
                  <xsl:call-template name="content.sidebar.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
                  <!-- ****** SIDEBAR BLOCK ****** -->
                </xsl:if>
                
              </xsl:otherwise>
            </xsl:choose>
            
          </xsl:when>
          <xsl:otherwise>
            
            <!-- Sidebar when logged out. -->
            <xsl:if test="$USE_SIDEBAR_GUEST='true'">
              <!-- ****** CONTENT SIDEBAR GUEST BLOCK ****** -->
              <xsl:call-template name="content.sidebar.guest.block"/> <!-- Calls a template of customizable content from universality.xsl. -->
              <!-- ****** CONTENT SIDEBAR GUEST BLOCK ****** -->
            </xsl:if>
            
          </xsl:otherwise>
        </xsl:choose>
      
      </div>
    </div>
  </xsl:template>
  <!-- ============================================ -->
  
		
</xsl:stylesheet>
