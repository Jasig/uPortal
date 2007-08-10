<?xml version='1.0' encoding='utf-8' ?>
<!--
Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

Author: Justin Tilton, jet@immagic.com
$Revision$
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" indent="no"/>
    <xsl:param name="baseActionURL">default</xsl:param>
    <xsl:param name="unauthenticated">true</xsl:param>
    <xsl:param name="locale">en_US</xsl:param>
    <xsl:param name="mediaPath" select="'media/org/jasig/portal/channels/CLogin'"/>
    <!-- ~ -->
    <!-- ~ Match on root element then check if the user is NOT authenticated-->
    <!-- ~ -->
    <xsl:template match="/">
        <xsl:if test="$unauthenticated='true'">
	        <xsl:apply-templates/>
        </xsl:if>
        <xsl:if test="$unauthenticated='false'">
        		Welcome <xsl:value-of select="//login-status/full-name"/>&#160;&#160;
        </xsl:if>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user is not authenticated insert login form-->
    <!-- ~ -->
    <xsl:template match="login-status">
        <form action="Login" method="post" style="padding:0;margin:0 0 0 5px;vertical-align:middle;">
           <input type="hidden" name="action" value="login"/>
           Username: <input style="vertical-align:middle;" type="text" name="userName" size="15" value="{failure/@attemptedUserName}"/>&#160;
           Password: <input style="vertical-align:middle;" type="password" name="password" size="15"/>&#160;
           <input type="submit" style="vertical-align:middle;" value="Login" name="Login" class="portlet-form-button"/>
           
           <xsl:apply-templates/>
        </form>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login fails present error message box-->
    <!-- ~ -->
    <xsl:template match="failure">
        <xsl:call-template name="message">
            <xsl:with-param name="messageString" select="'The user name/password combination entered is not recognized. Please try again.'"/>
        </xsl:call-template>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ If user login encounters an error present error message box-->
    <!-- ~ -->
    <xsl:template match="error">
        <xsl:call-template name="message">
            <xsl:with-param name="messageString" select="'An error occured during authentication. The portal is unable to log you on at this time. Try again later.'"/>
        </xsl:call-template>
    </xsl:template>
    <!-- ~ -->
    <!-- ~ error message box-->
    <!-- ~ -->
    <xsl:template name="message">
        <xsl:param name="messageString"/>
        <div style="font-weight: bold; padding-top: .2cm;"><xsl:value-of select="$messageString"/></div>
    </xsl:template>
</xsl:stylesheet>
