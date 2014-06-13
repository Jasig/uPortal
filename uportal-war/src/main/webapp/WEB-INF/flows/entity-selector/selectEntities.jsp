<%--

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

--%>

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
    | selectEntities.jsp.
    | Description: 
    ================================================-->
    
    <!--
    | Taglibs, definitions, etc.
    ================================================-->
    <%@ include file="/WEB-INF/jsp/include.jsp" %>
    <portlet:actionURL var="submitUrl">
    <portlet:param name="execution" value="${flowExecutionKey}" /></portlet:actionURL>
    <c:set var="n"><portlet:namespace/></c:set>
    <c:set var="selectionMode">${selectMultiple}</c:set>

    <style>
        .portlet .portlet-content { zoom:1; margin:5px 0 0 0; overflow:visible; }
        .portlet .portlet-msg { padding:0.25em 0.25em 0.25em 2.25em; }

        .portlet .titlebar { position:relative; margin-bottom:1.5em; }

        .portlet .titlebar ul { margin:0; padding:0; overflow:hidden; zoom:1; }
        .portlet .titlebar li { list-style-type:none; float:left; display:inline; padding:0 5px 0 5px; }

        .portlet .titlebar .breadcrumb-link { margin-bottom:1em; font-size:80%; color:#ccc; }
        .portlet .titlebar .breadcrumb-link .separator { font-size:90%;}
        .portlet .titlebar .breadcrumb-link a { color:#bbb !important; font-weight:normal; text-decoration:none !important;}
        .portlet .titlebar .breadcrumb-link a:hover,
        .portlet .titlebar .breadcrumb-link a:focus { color:#999 !important; text-decoration:underline !important; }

        .portlet .selection-basket a { color:#248222; padding:0 0 0 20px; text-decoration:none; background:transparent url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/tick.png') 0 50% no-repeat;}
        .portlet .selection-basket a:hover,
        .portlet .selection-basket a:focus { background-color:#FFFFCC; color:#9F0000; background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/delete.png'); }

        .portlet .breadcrumb-link .title { color:#333;}
        .portlet .breadcrumb-link .title,
        .portlet .breadcrumb-link .breadcrumbs { color:#CCC; font-size:77%; display:inline; }
        .portlet .breadcrumb-link .breadcrumbs a { text-decoration:none; }
        .portlet .breadcrumb-link .breadcrumbs .last { text-decoration:none; color:#AAA; cursor:default; }

        .portlet .entity-browser .titlebar { background-color:#F8F8F8; }
        .portlet .entity-browser .titlebar:hover,
        .portlet .entity-browser .titlebar:focus { background-color:#FFC; cursor:pointer; }
        .portlet .entity-browser .titlebar .title { font-size:120%; }
        .portlet .entity-browser .titlebar .select { background:transparent url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/add.png') 0 50% no-repeat; }
        .portlet .entity-browser .titlebar .select span { visibility:hidden; }
        .portlet .entity-browser .titlebar h4 { display:inline; }

        .portlet .entity-browser .selected { background-color:#D1F0E0;}
        .portlet .entity-browser .selected:hover,
        .portlet .entity-browser .selected:focus { background-color:#C8F0DD; }
        .portlet .entity-browser .selected .title { color:#248222;}
        .portlet .entity-browser .selected .select { background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/delete.png'); cursor:pointer; }


        .portlet .entity-browser .group .member-list a { background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/folder.png'); }
        .portlet .entity-browser .person .member-list a { background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/vcard.png'); }
        .portlet .entity-browser .category .member-list a { background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/tag_orange.png'); }

        .portlet .entity-browser .member-list { margin-left:0.5em; padding:0; list-style:none;}
        .portlet .entity-browser .member-list li { padding:0; margin:0; list-style:none; }
        .portlet .entity-browser .member-list a { background-position:0 50%; background-repeat:no-repeat;display: block; padding:1px 0 1px 20px;}
        .portlet .entity-browser .member-list a:hover,
        .portlet .entity-browser .member-list a:focus { background-color:#FFC; color:#336699; }

        .portlet .portlet-search form { margin-left:20px; }

        .portlet .search-dropdown { background-color:#FFF; -moz-box-shadow:0px 0px 5px 0px #999; -webkit-box-shadow:0 0 5px 0 #999; box-shadow:0px 0px 5px 0px #999; display:none; position:absolute; top:0; left:0; border:1px solid #AAA; margin-left:20px; padding:0px; min-width:200px; min-height:100px;}
        .portlet .search-dropdown .search-close { background-color:#F8F8F8; padding:5px 7px; text-align:right; border-bottom:1px solid #CCC; }
        .portlet .search-dropdown .search-close a { font-size:77%; }

        .portlet .search-dropdown .search-list { max-height:250px; overflow:auto; }
        .portlet .search-dropdown .search-list,
        .portlet .search-dropdown .search-list li { padding: 0; margin: 0; list-style: none;display: block; float:none; padding: 2px 5px; }
        .portlet .search-dropdown .search-list li:hover,
        .portlet .search-dropdown .search-list li:focus { background-color:#FFC; }
        .portlet .search-dropdown .search-list a { display:block; padding:0 0 0 20px; background:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/add.png') 0% 50% no-repeat; text-decoration:none; }
        .portlet .search-dropdown .search-list a:hover,
        .portlet .search-dropdown .search-list a:focus { color:#000; }
        .portlet .search-dropdown .search-list span { margin-left:15px;  }

        .portlet .search-dropdown .search-list .selected { background-color:#D1F0E0; }
        .portlet .search-dropdown .search-list .selected:hover,
        .portlet .search-dropdown .search-list .selected:focus { background-color:#C8F0DD; }
        .portlet .search-dropdown .search-list .selected a { color:#248222; background-image:url('/ResourceServingWebapp/rs/famfamfam/silk/1.3/delete.png'); font-weight:bold; }

        .portlet .search-dropdown .search-loader span { visibility: hidden; }
        .portlet .search-dropdown .portlet-msg.info { display: none; margin: 5px 7px; }
        .portlet .search-dropdown .search-loader { background:#EFEFEF url('../images/loading.gif') 50% 50% no-repeat; position:absolute; top:0; left:0; right:0; bottom:0; }

        .portlet.search-portlet .hidden { display:none; }

        .portlet .view-single-select .titlebar { background-color:#F8F8F8; }

        .portlet .view-single-select .portlet-selection .title.selections { background:transparent; }
        .portlet .view-single-select .selection-basket { color:#AAA; font-size:120%; }
        .portlet .view-single-select .selection-basket a { background:transparent; cursor:default; }
        .portlet .view-single-select .selection-basket a:hover,
        .portlet .view-single-select .selection-basket a:focus { color:#248222; }

        .portlet .view-single-select .entity-browser .titlebar { background-color:#F8F8F8; }
        .portlet .view-single-select .entity-browser .titlebar:hover,
        .portlet .view-single-select .entity-browser .titlebar:focus { background-color:#FFC; }

        .portlet .view-single-select .entity-browser .selected { background-color:#D1F0E0; }
        .portlet .view-single-select .entity-browser .selected:hover,
        .portlet .view-single-select .entity-browser .selected:focus { background-color:#C8F0DD; }

        .portlet .view-multi-select .breadcrumb-link { padding:0 0; }

        .portlet .view-multi-select .entity-browser {position:relative; margin-right: 24px; }
        .portlet .view-multi-select .entity-browser .titlebar { border:1px solid #CCC; padding:8px 8px 6px 8px; margin-bottom:0; position:relative; }
        .portlet .view-multi-select .entity-browser .content { padding:8px 8px 12px 8px; border:1px solid #CCC; }

        .portlet .view-multi-select .portlet-selection { margin-top:20px; border:1px solid #CCC; }
        .portlet .view-multi-select .portlet-selection .titlebar { background-color:#F8F8F8; padding:8px 8px 6px 8px; border-bottom:2px solid #CCC; margin-bottom:0; position:relative; }
        .portlet .view-multi-select .portlet-selection .content { padding:0px 0px 0px 0px; }
        .portlet .view-multi-select .portlet-selection .title.selections { background: transparent; padding-left: 0; }
        .portlet .view-multi-select .portlet-selection .selection-basket { border-bottom:1px solid #AAA; padding:8px; }
        .portlet .view-multi-select .portlet-selection .selection-basket a { display:block; padding:1px 0 1px 20px; }
        .portlet .view-multi-select .portlet-selection .selection-basket ul { padding:0; margin:0; list-style:none; height:200px; overflow:auto; }
        .portlet .view-multi-select .portlet-selection .selection-basket li { padding:0; margin:0; list-style:none;}

        .portlet .view-multi-select .buttons { margin:10px 0; padding-right:8px; border-top:none; text-align:right; }

        .portlet .view-multi-select .portlet-search { position:absolute; top:36px; right:8px; }
    </style>
    <!--
    | PORTLET DEVELOPMENT STANDARDS AND GUIDELINES
    | For the standards and guidelines that govern the user interface of this portlet
    | including HTML, CSS, JavaScript, accessibilty, naming conventions, 3rd Party libraries
    | (like jQuery and the Fluid Skinning System) and more, refer to:
    | http://www.ja-sig.org/wiki/x/cQ
    -->
    
    <!--
    | Portlet.
    ================================================-->
    <div class="fl-widget portlet grp-mgr view-selectgroups" role="section">
        <!--titlebar-->
        <div class="fl-widget-titlebar titlebar portlet-titlebar" role="sectionhead">
            <h2 class="title" role="heading"><spring:message code="${pageTitleCode}" text="${pageTitleText}"/></h2>
            <h3 class="subtitle"><spring:message code="${pageSubtitleCode}" arguments="${pageSubtitleArgument}" text="${pageSubtitleText}"/></h3>
        </div>
        <!--content-->
        <div id="${n}chooseGroupsBody" class="fl-widget-content content portlet-content container-fluid" role="main">
            <c:choose>
                <c:when test="${selectionMode}">
                    <!--
                    | View: Multi Select.
                    ================================================-->
                    <div class="view-multi-select">
                        <div class="columns-2 row-fluid row">
                            <div class="fl-container-flex60 span8 col-md-8">
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser row">
                                    <!--breadcrumb-->
                                    <div id="${n}entityBreadcrumb" class="link-breadcrumb col-md-12">
                                        <h5 class="title"><spring:message code="groups"/>:</h5>
                                        <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                    </div>
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix col-md-12">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>

                                    </div>
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content row">
                                        <!--includes-->
                                        <p class="col-md-12"><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <!--members-->
                                        <c:forEach items="${selectTypes}" var="type">
                                        <c:choose>
                                            <c:when test="${type == 'group'}">
                                                <div class="group col-md-12">
                                                    <h6 class="title"><spring:message code="groups"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'person'}">
                                                <div class="person col-md-12">
                                                    <h6 class="title"><spring:message code="people"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.people"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'category'}">
                                                <div class="category col-md-12">
                                                    <h6 class="title"><spring:message code="categories"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subcategories"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'portlet'}">
                                                <div class="portlet col-md-12">
                                                    <h6 class="title"><spring:message code="portlets"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.portlets"/></p>
                                                </div>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                    </div><!--end: content-->

                                    <!--search-->
                                    <div id="${n}portletSearch" class="portlet-search">
                                        <form id="${n}searchForm" class="form-inline" role="form">
                                            <input type="text" class="form-control" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                            <input type="submit" class="button btn" value="<spring:message code="go"/>" />
                                        </form>
                                        <div id="${n}searchDropDown" class="search-dropdown">
                                            <div id="${n}closeDropDown" class="search-close"><a href="javascript:;">Close</a></div>
                                            <div id="${n}searchResultsNoMembers" class="portlet-msg info" role="alert"><p><spring:message code="no.members"/></p></div>
                                            <ul id="${n}searchResults" class="search-list">
                                                <li class="group">
                                                    <a href="javascript:;" title="&nbsp;"><span>&nbsp;</span></a>
                                                </li>
                                            </ul>
                                            <div id="${n}searchLoader" class="search-loader"><span>&nbsp;</span></div>
                                        </div>
                                    </div>

                                </div><!--end: entity-->
                            </div>
                            <div class="fl-container-flex40 span4 col-md-4">
                                <!--selection-->
                                <div class="portlet-selection">
                                    <!--titlebar-->
                                    <div class="titlebar">
                                        <h4 class="title selections"><spring:message code="your.selections"/></h4>
                                    </div>
                                    <!--content-->
                                    <div class="content">
                                        <form action="${ submitUrl }" method="post">
                                            <div id="${n}selectionBasket" class="selection-basket">
                                                <ul>
                                                    <c:forEach items="${groups}" var="group">
                                                        <li>
                                                            <a key="${group.entityType}:${group.id}" href="javascript:;"><c:out value="${group.name}"/></a>
                                                            <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </div>
                                            
                                            <div id="${n}buttonPanel" class="buttons">
                                                <c:if test="${ showBackButton }">
                                                    <input class="button btn" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                                </c:if>
                                                <input id="${n}buttonPrimary" class="button btn btn-primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                                <c:if test="${ showCancelButton }">
                                                    <input class="button btn btn-link" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                                                </c:if>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div><!--end:view-multi-select-->
                </c:when>
                <c:otherwise>
                    <!--
                    | View: Single Select.
                    ================================================-->
                    <div class="view-single-select">
                        <form action="${submitUrl}" method="post">
                            <!--titlebar-->
                            <div class="titlebar">
                                <!--selection-->
                                <div class="portlet-selection">
                                    <h4 class="title selections"><spring:message code="your.selection"/>:</h4>
                                    <div id="${n}selectionBasket" class="selection-basket">
                                        <c:choose>
                                            <c:when test="${fn:length(groups) == 0}">
                                                <span class="selection" title="<spring:message code="nothing.selected"/>"><spring:message code="nothing.selected"/></span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${groups}" var="group">
                                                    <a key="${group.entityType}:${group.id}" href="javascript:;" class="selection"><c:out value="${group.name}"/></a>
                                                    <input type="hidden" name="groups" value="${group.entityType}:${group.id}"/>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                            <!--content-->
                            <div class="content">
                                <!--breadcrumb-->
                                <div id="${n}entityBreadcrumb" class="link-breadcrumb">
                                    <h5 class="title"><spring:message code="groups"/>:</h5>
                                    <div id="${n}entityBreadcrumbs" class="breadcrumbs"></div>
                                </div>
                                <!-- entity -->
                                <div id="${n}entityBrowser" class="entity-browser">
                                    <!--titlebar-->
                                    <div id="${n}entityBrowserTitlebar" class="titlebar ui-helper-clearfix">
                                        <h4 class="title" id="${n}currentEntityName"></h4>
                                        <a class="select" id="${n}selectEntityLink" href="javascript:;" title="<spring:message code="select"/>"><span><spring:message code="select"/></span></a>
                                    </div>
                                    <!--content-->
                                    <div id="${n}entityBrowserContent" class="content">
                                        <p><span id="${n}browsingInclude" class="current"></span> <spring:message code="includes"/>:</p>
                                        <c:forEach items="${selectTypes}" var="type">
                                        <c:choose>
                                            <c:when test="${type == 'group'}">
                                                <div class="group">
                                                    <h6 class="title"><spring:message code="groups"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subgroups"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'person'}">
                                                <div class="person">
                                                    <h6 class="title"><spring:message code="people"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.people"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'category'}">
                                                <div class="category">
                                                    <h6 class="title"><spring:message code="categories"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.member.subcategories"/></p>
                                                </div>
                                            </c:when>
                                            <c:when test="${type == 'portlet'}">
                                                <div class="portlet">
                                                    <h6 class="title"><spring:message code="portlets"/></h6>
                                                    <ul class="member-list"></ul>
                                                    <p class="no-members" style="display:none"><spring:message code="no.direct.member.portlets"/></p>
                                                </div>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                    </div><!--end: content-->
                                </div><!--end: entity-->
                                
                                <!--buttons-->
                                <div id="${n}buttonPanel" class="buttons">
                                    <c:if test="${showBackButton}">
                                        <input class="button btn" type="submit" value="<spring:message code="${ backButtonCode }" text="${ backButtonText }"/>" name="_eventId_back"/>
                                    </c:if>
                                    <input id="${n}buttonPrimary" class="button btn btn-primary" type="submit" value="<spring:message code="${ saveButtonCode }" text="${ saveButtonText }"/>" name="_eventId_save"/>
                                    <c:if test="${showCancelButton}">
                                        <input class="button btn btn-link" type="submit" value="<spring:message code="${ cancelButtonCode }" text="${ cancelButtonText }"/>" name="_eventId_cancel"/>
                                    </c:if>
                                </div><!--end: buttons-->
                            </div><!--end: content-->
                        </form>
                        
                        <!--search-->
                        <div id="${n}portletSearch" class="portlet-search">
                            <form id="${n}searchForm">
                                <input type="text" name="searchterm" value="<spring:message code="enter.name"/>"/>
                                <input type="submit" class="button btn" value="<spring:message code="go"/>" />
                            </form>
                            <div id="${n}searchDropDown" class="search-dropdown">
                                <div id="${n}closeDropDown" class="search-close"><a href="javascript:;">Close</a></div>
                                <div id="${n}searchResultsNoMembers" class="portlet-msg info" role="alert"><p><spring:message code="no.members"/></p></div>
                                <ul id="${n}searchResults" class="search-list">
                                    <li class="group">
                                        <a href="javascript:;" title="&nbsp;"><span>&nbsp;</span></a>
                                    </li>
                                </ul>
                                <div id="${n}searchLoader" class="search-loader"><span>&nbsp;</span></div>
                            </div>
                        </div>
                    </div><!--end:view-single-select-->
                </c:otherwise>
            </c:choose>
        </div><!--end:portlet-content-->
    </div><!--end:portlet-->
    <script type="text/javascript">
        up.jQuery(function() {
            var $ = up.jQuery;
            
            $(document).ready(function(){
                up.entityselection("#${n}chooseGroupsBody", {
                    entityRegistry: {
                        options: { entitiesUrl: "<c:url value="/api/entities"/>" }
                    },
                    entityTypes: [<c:forEach items="${selectTypes}" var="type" varStatus="status">'<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${type}</spring:escapeBody>'${status.last ? '' : ','}</c:forEach>],
                    selected: [<c:forEach items="${groups}" var="group" varStatus="status">'<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${group.entityType}:${group.id}</spring:escapeBody>'${ status.last ? '' : ',' }</c:forEach>],
                    initialFocusedEntity: '${rootEntity.entityType}:${rootEntity.id}',
                    selectMultiple: ${selectionMode},
                    requireSelection: ${ not empty requireSelection ? requireSelection : true },
                    selectors: {
                        selectionBasket: "#${n}selectionBasket",
                        breadcrumbs: "#${n}entityBreadcrumbs",
                        currentEntityName: "#${n}currentEntityName",
                        selectEntityLink: "#${n}selectEntityLink",
                        entityBrowserContent: "#${n}entityBrowserContent",
                        entityBrowserTitlebar: "#${n}entityBrowserTitlebar",
                        browsingInclude: "#${n}browsingInclude",
                        closeSearch: "#${n}closeDropDown",
                        searchForm: "#${n}searchForm",
                        searchDropDown: "#${n}searchDropDown",
                        searchResults: "#${n}searchResults",
                        searchResultsNoMembers: "#${n}searchResultsNoMembers",
                        searchLoader: "#${n}searchLoader",
                        buttonPanel: "#${n}buttonPanel",
                        buttonPrimary: "#${n}buttonPrimary"
                    },
                    messages: {
                        selectButtonMessage: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        deselectButtonMessage: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="deselect"/></spring:escapeBody>',
                        removeCrumb: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        removeSelection: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="remove"/></spring:escapeBody>',
                        addSelection: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="select"/></spring:escapeBody>',
                        selected: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="selected"/></spring:escapeBody>',
                        nothingSelected: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="nothing.selected"/></spring:escapeBody>',
                        searchValue: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><spring:message code="please.enter.name"/></spring:escapeBody>'
                    }
                });
            });
        });
    </script>
