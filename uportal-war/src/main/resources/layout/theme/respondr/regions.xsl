<?xml version="1.0" encoding="utf-8"?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

<!--
 | This file defines areas or _Regions_ of the page in which non-tab/column
 | portlets may be placed.  Regions with portlets present must display them
 | properly;  regions without portlets must "disappear" gracefully.  ALL of the
 | essential page structure markup (related to regions) MUST be provided by the
 | regions themselves, not by rendered portlets.
 |
 | The file is imported by the base stylesheet respondr.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to respondr.xsl for the list of parameters and imported XSL files.
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
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    xmlns:url="https://source.jasig.org/schemas/uportal/layout/portal-url"
    xsi:schemaLocation="
            https://source.jasig.org/schemas/uportal/layout/portal-url https://source.jasig.org/schemas/uportal/layout/portal-url-4.0.xsd"
    exclude-result-prefixes="url upAuth upGroup upMsg dlm xsi"
    version="1.0">

  <!-- ========== TEMPLATE: GREETING ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders portlets in the top-right greeting area.
  -->
  <xsl:template name="region.greeting">
    <div id="region-greeting" class="portal-user">
      <xsl:for-each select="//region[@name='greeting']/channel">
        <xsl:call-template name="regions.portlet.decorator" />
      </xsl:for-each>
    </div>
  </xsl:template>

  <!-- ========== TEMPLATE: LOGO ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders portlets in the top-left logo area.
  -->
  <xsl:template name="region.logo">
    <div id="region-logo" class="col-sm-8">
      <xsl:for-each select="//region[@name='logo']/channel">
        <xsl:call-template name="regions.portlet.decorator" />
      </xsl:for-each>
    </div>
  </xsl:template>

  <!-- ========== TEMPLATE: SEARCH ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders portlets in the top-right search area.
  -->
  <xsl:template name="region.search">
    <div id="region-search" class="col-sm-4">
      <xsl:for-each select="//region[@name='search']/channel">
        <xsl:call-template name="regions.portlet.decorator" />
      </xsl:for-each>
    </div>
  </xsl:template>

  <!-- ========== TEMPLATE: EMERGENCY ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders portlets in the top-right search area.
  -->
  <xsl:template name="region.emergency">
    <div id="region-emergency" class="container">
      <div class="row">
        <div class="col-sm-12">
          <xsl:for-each select="//region[@name='emergency']/channel">
            <xsl:call-template name="regions.portlet.decorator" />
          </xsl:for-each>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- ========== TEMPLATE: CUSTOMIZE ========== -->
  <!-- ======================================= -->
  <!--
   | This template renders portlets in the top-left logo area.
  -->
  <xsl:template name="region.customize">
    <div id="region-customize" class="container">
        <div id="customizeOptionsWrapper">
            <div id="customizeOptions" class="collapse">
                <div class="container">
                    <div class="alert alert-info" style="margin-top: 1.5em;">Place customization components here</div>
                </div>
            </div>
            <button type="button" class="btn btn-default" data-toggle="collapse" data-target="#customizeOptions">CUSTOMIZE <i class="fa"></i></button>
        </div>
<!-- 
        <div id="customizeOptionsWrapper">
                <div id="customizeOptions" class="in" style="height: auto;">
                        <div class="container">
                                <div class="alert alert-info" style="margin-top: 1.5em;">Place customization components here</div>
                        </div>
                </div>
                <button type="button" class="btn btn-default optionsButton collapsed" data-toggle="collapse" data-target="#customizeOptions">CUSTOMIZE <i class="fa"></i></button>
        </div> -->
      <!-- <xsl:for-each select="//region[@name='customizeDrawer']/channel">
        <xsl:call-template name="regions.portlet.decorator" />
      </xsl:for-each> -->
    </div>
  </xsl:template>


  <!-- ========== TEMPLATE: REGIONS PORTLET DECORATOR ========== -->
  <!-- ======================================= -->
  <!--
   | This template decorates a portlet that appears in a region (in lieu of chrome).
  -->
  <xsl:template name="regions.portlet.decorator">
    <section id="portlet_{@ID}" class="up-portlet-wrapper {@fname}">
      <xsl:copy-of select="."/> <!-- Write in the contents of the portlet. -->
    </section>
  </xsl:template>

</xsl:stylesheet>
