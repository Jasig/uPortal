<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (c) 2004 The JA-SIG Collaborative.  All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed by the JA-SIG Collaborative
   (http://www.jasig.org/)."

THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

Author: 
Jon Allen, jfa@immagic.com
Version $Revision$
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="no"/>
    <xsl:param name="baseActionURL" select="'render.userLayoutRootNode.uP'"/>
    <xsl:param name="locale" select="'en_US'"/>
    <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CContentSubscriber'"/>
    <xsl:param name="channel-state" select="'browse'"/>
    <xsl:param name="isFragment" select="'false'"/>
    <!--~-->
    <!-- parameters for content search -->
    <!--~-->
    <xsl:param name="search-fragment" select="'true'"/>
    <xsl:param name="search-category" select="'true'"/>
    <xsl:param name="search-channel" select="'true'"/>
    <xsl:param name="search-query"/>
    <!--~-->
    <!-- end of xsl parameter declarations -->
    <!--~-->
    <xsl:template match="/">
        <table cellspacing="0" cellpadding="5" width="100%" border="0">
            <tr class="uportal-background-content">
                <td align="left" valign="top">
                    <!-- test -->
                    <!-- <textarea name="test" rows="3" cols="50" wrap="no">
                        <xsl:copy-of select="/"/>
                    </textarea> -->
                    <!-- test -->
                    <xsl:call-template name="tabLine"/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!-- match registry and recursively build the tree -->
    <!--~-->
    <xsl:template match="registry">
        <xsl:apply-templates select="category">
            <xsl:sort select="@name"/>
        </xsl:apply-templates>            
        <xsl:apply-templates select="fragments">
            <xsl:sort select="@name"/>
        </xsl:apply-templates>            
    </xsl:template>
    <!--~-->
    <!-- tab line table template - draws the browse/search buttons. -->
    <!--~-->
    <xsl:template name="tabLine">
        <xsl:choose>
            <xsl:when test="$channel-state='search'">
                <!--~-->
                <!-- Begin - Search front tab. -->
                <!--~-->
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                    <tr>
                        <td>
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td colspan="4">
                                        <img height="1" width="1" src="$mediaPath/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med" nowrap="nowrap">
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                        <a href="{$baseActionURL}?channel-state=browse">
                                            <span class="uportal-text-small">Browse</span>
                                        </a>
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="10" class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td>
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light" nowrap="nowrap">
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                        <span class="uportal-text-small">Search</span>
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="11" class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="100%" valign="bottom">
                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td width="100%" class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <xsl:call-template name="searchTable"/>
                <!--~-->
                <!-- End - Search front tab. -->
                <!--~-->
            </xsl:when>
            <xsl:otherwise>
                <!--~-->
                <!-- Begin - Browse front tab. -->
                <!--~-->
                <table cellpadding="0" cellspacing="0" border="0" width="100%">
                    <tr>
                        <td>
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light" nowrap="nowrap">
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                        <span class="uportal-text-small">Browse</span>
                                        <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="10" class="uportal-background-light">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td>
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="4">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="3">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td colspan="2">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med" nowrap="nowrap">
                                        <a href="#">
                                            <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                            <a href="{$baseActionURL}?channel-state=search">
                                                <span class="uportal-text-small">Search</span>
                                            </a>
                                            <img border="0" height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                        </a>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-content">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-semidark">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                    <td class="uportal-background-shadow">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="11" class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                        <td width="100%" valign="bottom">
                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <td width="100%" class="uportal-background-med">
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <!--~-->
                <!-- End - Browse front tab. -->
                <!--~-->
                <xsl:call-template name="frame"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--~-->
    <!-- End - tab line table template. -->
    <!--~-->
    <!--~-->
    <!-- Begin - search content table template. -->
    <!--~-->
    <xsl:template name="searchTable">
        <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td width="100%">
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-dark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-dark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-dark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-dark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-dark">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <table class="uportal-background-content" cellpadding="2" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                                <span class="uportal-channel-table-header">Search Content <img src="{$mediaPath}/transparent.gif" width="16" height="1" alt="" title=""/>
                                </span>
                            </td>
                            <td width="100%" align="right" valign="bottom" nowrap="nowrap" class="uportal-text-small">
                                <img src="{$mediaPath}/transparent.gif" width="1" height="1" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                    <tr>
                                        <td>
                                            <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                    <form name="search-form" action="{$baseActionURL}" method="post">
                        <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                            <tr class="uportal-channel-text">
                                <td>
                                    <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt=""/>
                                </td>
                                <td align="left" valign="top">
                                    <span class="uportal-label">Search for</span>:<br/>
                                    <table border="0" cellpadding="0" cellspacing="0">
                                        <tr>
                                            <td>
                                                <input name="search-category" type="checkbox" value="true">
                                                    <xsl:if test="$search-category='true'">
                                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                                    </xsl:if>
                                                </input>
                                            </td>
                                            <td>
                                                <span class="uportal-text-small">Categories</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <input name="search-channel" type="checkbox" value="true">
                                                    <xsl:if test="$search-channel='true'">
                                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                                    </xsl:if>
                                                </input>
                                            </td>
                                            <td>
                                                <span class="uportal-text-small">Channels</span>
                                            </td>
                                        </tr>
                                        <xsl:if test="$isFragment='false'">
                                        <tr>
                                            <td>
                                                <input name="search-fragment" type="checkbox" value="true">
                                                    <xsl:if test="$search-fragment='true'">
                                                        <xsl:attribute name="checked">checked</xsl:attribute>
                                                    </xsl:if>
                                                </input>
                                            </td>
                                            <td class="uportal-text-small">
                                                <span>Fragments</span>
                                            </td>
                                        </tr>
                                        </xsl:if>
                                    </table>
                                </td>
                                <td>
                                    <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                </td>
                                <!-- <td align="left" valign="top">
                <span class="uportal-label">whos</span>
                <br/>
                <table border="0" cellpadding="0" cellspacing="0">
                  <tr>
                    <td>
                      <input name="channels2" type="checkbox" value="channels" checked="checked"/>
                    </td>
                    <td>
                      <span class="uportal-text-small">Name</span>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <input name="fragments2" type="checkbox" value="fragments" checked="checked"/>
                    </td>
                    <td class="uportal-text-small">
                      <span>Description</span>
                    </td>
                  </tr>
                </table>
              </td> -->
                                <!-- <td>
                <img height="1" width="5" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td> -->
                                <!-- <td align="center" valign="top">
                <br/>
                <select name="select5" class="uportal-input-text">
                  <option value="contains" selected="selected">contains</option>
                  <option value="is">is</option>
                  <option value="startsWith">starts with</option>
                  <option value="endsWith">ends with</option>
                </select>
              </td> -->
                                <!-- <td>
                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
              </td> -->
                                <td align="center" valign="top">
                                    <br/>
                                    <input name="search-query" type="text" class="uportal-input-text" value="{$search-query}" size="40"/>
                                </td>
                                <td>
                                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                </td>
                                <td width="100%" align="left" valign="top">
                                    <br/>
                                    <input type="hidden" name="uPcCS_action" value="search"/> 
                                    <input type="hidden" name="channel-state" value="search"/>
                                    <input name="Submit" type="submit" class="uportal-button" value="Search"/>
                                </td>
                            </tr>
                            <tr class="uportal-background-content" valign="top" align="left">
                                <td colspan="10">
                                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                        <tr>
                                            <td>
                                                <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
  <tr class="uportal-background-content" valign="top" align="left" width="100%">
                                <td colspan="10">
                                    <xsl:apply-templates/>
                                </td>
                            </tr>
                        </table>
                    </form>
                </td>
                <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-content">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <td class="uportal-background-med">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td>
                    <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
                <td class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
            <tr>
                <td colspan="7" class="uportal-background-shadow">
                    <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!-- End - search content table template. -->
    <!--~-->
    <!--~-->
    <!-- Begin browse content framework - draws the content subscriber frame.  includes the form and cancel button. -->
    <!--~-->
    <xsl:template name="frame">
        <table cellspacing="0" cellpadding="0" width="100%" border="0">
            <tr class="uportal-background-content">
                <td align="left" valign="top">
                    <!--~-->
                    <!-- Begin Outline Table Template.  Draws the padded border around the channel content.  Includes the "cancel" button. -->
                    <!--~-->
                    <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                        <tr>
                            <td class="uportal-background-med">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td width="100%">
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-shadow">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td class="uportal-background-med">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-dark">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-dark">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-dark">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-dark">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td class="uportal-background-med">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-dark">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <table class="uportal-background-content" cellpadding="2" cellspacing="0" border="0" width="100%">
                                    <tr>
                                        <td class="uportal-background-content" align="left" valign="bottom" nowrap="nowrap">
                                            <span class="uportal-channel-table-header">Select Category<img src="{$mediaPath}/transparent.gif" width="16" height="1" alt="" title=""/>
                                            </span>
                                        </td>
                                        <td width="100%" align="right" valign="bottom" nowrap="nowrap" class="uportal-text-small">
                                            <strong>
                                                <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID=all&amp;channel-state={$channel-state}">Expand</a>/<a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID=all&amp;channel-state={$channel-state}">Condense</a> All Categories</strong>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                                <tr>
                                                    <td height="2">
                                                        <img height="2" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                </table>
                                <xsl:apply-templates/>
                                <!-- <xsl:apply-templates select="category"/>
                                <xsl:apply-templates select="channel"/>
                                <xsl:apply-templates select="fragments"/> -->
                            </td>
                            <td class="uportal-background-content">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-shadow">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td class="uportal-background-med">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-content">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-content">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-content">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-shadow">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td class="uportal-background-med">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td>
                                <img height="10" width="10" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                            <td class="uportal-background-shadow">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="7" class="uportal-background-shadow">
                                <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
    <!--~-->
    <!-- end of table set for browsed framework -->
    <!--~-->
    <!--~-->
    <!-- begin table for content item: chooses between expanded and contracted -->
    <!--~-->
    <xsl:template match="category">
        <xsl:choose>
            <xsl:when test="($channel-state='browse' and @view='expanded') or ($channel-state='search' and @search-view='expanded')">
                <xsl:if test="$isFragment='false' or @name!='Fragments'">
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <strong>
                                <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={@ID}&amp;channel-state={$channel-state}">
                                    <img src="{$mediaPath}/expanded.gif" width="16" height="16" border="0" alt="" title=""/>
                                </a>
                            </strong>
                        </td>
                        <xsl:variable name="depth">
                            <xsl:choose>
                                <xsl:when test="@name='Fragments' ">1</xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="count(ancestor::*)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <td class="uportal-navigation-category">
                            <img src="{$mediaPath}/transparent.gif" width="{$depth*16+($depth - 1)*3}" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <strong>
                                <a class="uportal-navigation-category" href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={@ID}&amp;channel-state={$channel-state}">
                                    <img src="{$mediaPath}/folder_open.gif" width="16" height="16" border="0" alt="" title=""/>
                                    <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                    <xsl:value-of select="@name"/>
                                </a>
                            </strong>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </xsl:if>
                <xsl:apply-templates select="category">
                    <xsl:sort select="@name"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="channel">
                    <xsl:sort select="@title"/>
                </xsl:apply-templates>
                <xsl:apply-templates select="fragment">
                    <xsl:sort select="name"/>
                </xsl:apply-templates>
                <!-- <xsl:apply-templates select="category"/>
                <xsl:apply-templates select="channel"/> -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$isFragment='false' or @name!='Fragments'">
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <strong>
                                <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={@ID}&amp;channel-state={$channel-state}">
                                    <img src="{$mediaPath}/collapsed.gif" width="16" height="16" border="0" alt="" title=""/>
                                </a>
                            </strong>
                        </td>
                        <xsl:variable name="depth">
                            <xsl:choose>
                                <xsl:when test="@name='Fragments' ">1</xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="count(ancestor::*)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="{$depth*16+($depth - 1)*3}" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a class="uportal-navigation-category" href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={@ID}&amp;channel-state={$channel-state}">
                                <strong>
                                    <img src="{$mediaPath}/folder_closed.gif" width="16" height="16" border="0" alt="" title=""/>
                                    <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                    <xsl:value-of select="@name"/>
                                </strong>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--~-->
    <!-- end of table for closed category name -->
    <!--~-->
    <!--~-->
    <!-- begin table for closed content item: contains the content name and divider line -->
    <!--~-->
    <xsl:template match="channel">
        <xsl:choose>
            <xsl:when test="($channel-state='browse' and @view='expanded') or ($channel-state='search' and @search-view='expanded')">
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <xsl:variable name="indentWidth">
                            <xsl:value-of select="((count(ancestor::*)*16)+((count(ancestor::*)-1)*3))"/>
                        </xsl:variable>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a class="uportal-navigation-channel" href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_channelID={@ID}&amp;uPcCS_categoryID={../@ID}&amp;channel-state={$channel-state}">
                                <img src="{$mediaPath}/channel_icon.gif" width="16" height="16" border="0" alt="" title=""/>
                                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                <xsl:value-of select="@title"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="uportal-background-content">
                            <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                        </td>
                        <td class="uportal-background-content">
                            <!-- Form not needed until language selector is reintroduced
                <form name="subscribe_channel_form" action="{$baseActionURL}" method="post">
                -->
                            <table cellpadding="5" cellspacing="0" border="0" width="100%">
                                <tr class="uportal-channel-text" valign="top">
                                    <td nowrap="nowrap" align="right">Type:</td>
                                    <td width="100%">Individual Channel</td>
                                </tr>
                                <tr class="uportal-channel-text" valign="top">
                                    <td nowrap="nowrap" align="right">Description:</td>
                                    <td width="100%">
                                        <xsl:value-of select="@description"/>
                                    </td>
                                </tr>
                                <!-- Language Selector temporarily removed until i18n is more inclusive
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td>Settings:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="0" cellpadding="2">
                      <tr>
                        <td class="uportal-label">Language:</td>
                      </tr>
                      <tr>
                        <td>
                          <select name="select">
                            <option value="defualtlang" selected="selected">Use my defualt language</option>
                            <option value="EnglishC">English (Canadian)</option>
                            <option value="EnglishUK">English (United Kingdom)</option>
                            <option value="EnglishUS">English (United States)</option>
                            <option value="French">French</option>
                            <option value="German">German</option>
                            <option value="Japanese">Japanese</option>
                            <option value="Russian">Russian</option>
                            <option value="Spanish">Spanish</option>
                            <option value="Swedish">Swedish</option>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                Language Selector temporarily removed until i18n is more inclusive  -->
                                <tr class="uportal-channel-text" valign="top">
                                    <td nowrap="nowrap" align="right">Actions:</td>
                                    <td width="100%">
                                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                            <!-- Preview of Channel Held until Later Release
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="#" target="_blank">
                            <img src="{$mediaPath}/preview.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td>
                          <a href="#" target="_blank">Preview this channel in a new window</a>
                        </td>
                      </tr>  Preview of Channel Held until Later Release -->
                                            <tr align="left" class="uportal-channel-text">
                                                <td valign="top">
                                                    <a href="{$baseActionURL}?uP_root=root&amp;channelPublishID={@chanID}&amp;uP_request_add_targets=channel&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Channel&amp;uP_sparam=targetRestriction&amp;targetRestriction=channel">
                                                        <img src="{$mediaPath}/channel_subscribe.gif" width="16" height="16" border="0" alt="" title=""/>
                                                    </a>
                                                </td>
                                                <td width="100%" valign="bottom">
                                                    <a href="{$baseActionURL}?uP_root=root&amp;channelPublishID={@chanID}&amp;uP_request_add_targets=channel&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Channel&amp;uP_sparam=targetRestriction&amp;targetRestriction=channel">
                                                        <img height="1" width="3" border="0" src="{$mediaPath}/transparent.gif" alt="" title=""/>Subscribe to this channel</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <!-- Form not needed until language selector is reintroduced
          </form>
          -->
                </table>
                <table class="uportal-background-content" cellpadding="3" cellspacing="0" border="0" width="100%">
                    <tr>
                        <td>
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:when>
            <xsl:otherwise>
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <xsl:variable name="indentWidth">
                            <xsl:value-of select="((count(ancestor::*)*16)+((count(ancestor::*)-1)*3))"/>
                        </xsl:variable>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="{$indentWidth}" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a class="uportal-navigation-channel" href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_channelID={@ID}&amp;uPcCS_categoryID={../@ID}&amp;channel-state={$channel-state}">
                                <img src="{$mediaPath}/channel_icon.gif" width="16" height="16" border="0" alt="" title=""/>
                                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                <xsl:value-of select="@title"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--~-->
    <!-- end of table for content item -->
    <!--~-->
    <!--~-->
    <!-- begin table for fragment title -->
    <!--~-->
    <xsl:template match="fragments">
        <xsl:apply-templates/>
        <!-- <xsl:choose>
            <xsl:when test="category/@view='expanded'">
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                                                                       <xsl:if test="$channel-state='search' and @search-selected='true'">
                        <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td class="uportal-navigation-category">
                            <img src="{$mediaPath}/expanded.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <td class="uportal-navigation-category">
                            <img src="{$mediaPath}/transparent.gif" width="16" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom" class="uportal-navigation-category">
                            <img src="{$mediaPath}/folder_open.gif" width="16" height="16" border="0" alt="" title=""/>
                            <a href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_categoryID={category/@ID}&amp;channel-state={$channel-state}">
                                <strong> Fragments </strong>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <xsl:apply-templates select="category/fragment"/>
            </xsl:when>
            <xsl:otherwise>
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr class="uportal-channel-text" valign="top" align="left">
                        <td class="uportal-navigation-category">
                            <img src="{$mediaPath}/collapsed.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <td class="uportal-navigation-category">
                            <img src="{$mediaPath}/transparent.gif" width="16" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom" class="uportal-navigation-category">
                            <img src="{$mediaPath}/folder_closed.gif" width="16" height="16" border="0" alt="" title=""/>
                            <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                            <a href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_categoryID={category/@ID}&amp;channel-state={$channel-state}">
                                <strong> Fragments </strong>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose> -->
    </xsl:template>
    <!--~-->
    <!-- end table for fragment title -->
    <!--~-->
    <!--~-->
    <!-- begin table for fragment list -->
    <!--~-->
    <xsl:template match="fragment">
        <xsl:if test="$isFragment='false'">
        <xsl:choose>
            <xsl:when test="($channel-state='browse' and @view='expanded') or ($channel-state='search' and @search-view='expanded')">
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-highlight">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="35" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a class="uportal-navigation-channel" href="{$baseActionURL}?uPcCS_action=condense&amp;uPcCS_fragmentID={@ID}&amp;uPcCS_categoryID={../@ID}&amp;channel-state={$channel-state}">
                                <img src="{$mediaPath}/channel_icon.gif" width="16" height="16" border="0" alt="" title=""/>
                                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                <xsl:value-of select="@title"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="uportal-background-content"/>
                        <td class="uportal-background-content" width="100%">
                            <table cellpadding="5" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <table cellpadding="5" cellspacing="0" border="0" width="100%">
                                            <tr class="uportal-channel-text" valign="top">
                                                <td nowrap="nowrap" align="right">Type:</td>
                                                <td width="100%">Fragment</td>
                                            </tr>
                                            <tr class="uportal-channel-text" valign="top">
                                                <td nowrap="nowrap" align="right">Description:</td>
                                                <td width="100%">
                                                    <xsl:value-of select="./description"/>
                                                </td>
                                            </tr>
                                            <!-- Language Selector temporarily removed until i18n is more inclusive 
                <tr class="uportal-channel-text" valign="top" align="left">
                  <td>Settings:</td>
                  <td width="100%">
                    <table width="100%" border="0" cellspacing="0" cellpadding="2">
                      <tr>
                        <td class="uportal-label">Language:</td>
                      </tr>
                      <tr>
                        <td>
                          <select name="select">
                            <option value="defualtlang" selected="selected">Use my defualt language</option>
                            <option value="EnglishC">English (Canadian)</option>
                            <option value="EnglishUK">English (United Kingdom)</option>
                            <option value="EnglishUS">English (United States)</option>
                            <option value="French">French</option>
                            <option value="German">German</option>
                            <option value="Japanese">Japanese</option>
                            <option value="Russian">Russian</option>
                            <option value="Spanish">Spanish</option>
                            <option value="Swedish">Swedish</option>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                Language Selector temporarily removed until i18n is more inclusive -->
                                            <tr class="uportal-channel-text" valign="top">
                                                <td nowrap="nowrap" align="right">Actions:</td>
                                                <td width="100%">
                                                    <table width="100%" border="0" cellspacing="3" cellpadding="3">
                                                        <!-- Preview of Channel Held until Later Release
                      <tr align="left" valign="top" class="uportal-channel-text">
                        <td>
                          <a href="#" target="_blank">
                            <img src="{$mediaPath}/preview.gif" width="16" height="16" border="0" alt="" title=""/>
                          </a>
                        </td>
                        <td>
                          <a href="#" target="_blank">Preview this fragment in a new window</a>
                        </td>
                      </tr>  Preview of Channel Held until Later Release -->
                                                        <tr align="left" valign="top" class="uportal-channel-text">
                                                            <td valign="top">
                                                                <a href="{$baseActionURL}?uP_root=root&amp;fragmentRootID={rootNodeID}&amp;fragmentPublishID={@ID}&amp;uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Tab&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab">
                                                                    <img src="{$mediaPath}/channel_subscribe.gif" width="16" height="16" border="0" alt="" title=""/>
                                                                </a>
                                                            </td>
                                                            <td width="100%" valign="bottom">
                                                                <a href="{$baseActionURL}?uP_root=root&amp;fragmentRootID={rootNodeID}&amp;fragmentPublishID={@ID}&amp;uP_request_add_targets=folder&amp;uP_sparam=mode&amp;mode=preferences&amp;uP_sparam=targetAction&amp;targetAction=New Tab&amp;uP_sparam=targetRestriction&amp;targetRestriction=tab">
                                                                    <img height="1" width="3" border="0" src="{$mediaPath}/transparent.gif" alt="" title=""/> Subscribe to this fragment</a>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <table class="uportal-background-content" cellpadding="3" cellspacing="0" border="0" width="100%">
                    <tr>
                        <td>
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:when>
            <xsl:otherwise>
                <table cellpadding="2" cellspacing="0" border="0" width="100%" class="uportal-background-content">
                    <tr valign="top" align="left">
                        <xsl:if test="$channel-state='search' and @search-selected='true'">
                            <xsl:attribute name="class">uportal-background-selected</xsl:attribute>
                        </xsl:if>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="16" height="16" border="0" alt="" title=""/>
                        </td>
                        <td>
                            <img src="{$mediaPath}/transparent.gif" width="35" height="1" border="0" alt="" title=""/>
                        </td>
                        <td width="100%" valign="bottom">
                            <a class="uportal-navigation-channel" href="{$baseActionURL}?uPcCS_action=expand&amp;uPcCS_fragmentID={@ID}&amp;uPcCS_categoryID={../@ID}&amp;channel-state={$channel-state}">
                                <img src="{$mediaPath}/fragment_icon.gif" width="16" height="16" border="0" alt="" title=""/>
                                <img src="{$mediaPath}/transparent.gif" width="3" height="1" border="0" alt="" title=""/>
                                <xsl:value-of select="./name"/>
                            </a>
                        </td>
                    </tr>
                    <tr class="uportal-background-content" valign="top" align="left">
                        <td colspan="5">
                            <table class="uportal-background-light" cellpadding="0" cellspacing="0" border="0" width="100%">
                                <tr>
                                    <td>
                                        <img height="1" width="1" src="{$mediaPath}/transparent.gif" alt="" title=""/>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </xsl:otherwise>
        </xsl:choose>
        </xsl:if>
    </xsl:template>
    <!--~-->
    <!-- end table for fragment list -->
    <!--~-->
</xsl:stylesheet>
