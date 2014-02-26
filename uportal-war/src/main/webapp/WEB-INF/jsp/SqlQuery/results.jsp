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

<%@ include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="n"><portlet:namespace/></c:set>

<style>
#${n}resultBrowser .dataTables_filter, #${n}resultBrowser .first.paginate_button, #${n}resultBrowser .last.paginate_button{
    display: none;
}
#${n}resultBrowser .dataTables-inline, #${n}resultBrowser .column-filter-widget, #${n}resultBrowser .column-filter-widget {
    display: inline-block;
}
#${n}resultBrowser .dataTables_wrapper {
    width: 100%;
}
#${n}resultBrowser .dataTables_paginate .paginate_button {
    margin: 2px;
    cursor: pointer;
    *cursor: hand;
}
#${n}resultBrowser .dataTables_paginate .paginate_active {
    margin: 2px;
    color:#000;
}
#${n}resultBrowser .dataTables-right {
    float:right;
}
</style>

<!-- Portlet -->
<div class="fl-widget portlet" role="section">
  
  <!-- Portlet Body -->
  <div id="${n}resultBrowser" class="fl-widget-content portlet-body" role="main">
  
        <table id="${n}sqlResults" style="width:100%;">
            <thead>
                <tr style="text-transform:capitalize">
                  <!-- Dynamically create number of columns -->
                    <c:forEach items="${ results[0] }" var="cell" varStatus="status">
                        <th>${ fn:escapeXml(cell.key) }</th>
                    </c:forEach>
                </tr>
            </thead>
        </table>
    
  </div>

</div>

<script type="text/javascript">
 up.jQuery(function() {
    var $ = up.jQuery;
    // create data set from model
    var results = [<c:forEach items="${ results }" var="row" varStatus="status">{<c:forEach items="${ row }" var="cell" varStatus="cellStatus">'column${ cellStatus.index }': '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${ cell.value }</spring:escapeBody>'${ cellStatus.last ? '' : ','}</c:forEach>}${ status.last ? '' : ','}</c:forEach>];

    var resultList_configuration = {
        column: {
            <c:forEach items="${ results[0] }" var="row" varStatus="status">
                column${ status.index } : ${ status.index }${ status.last ? "" : ","}
            </c:forEach>
        },
        main: {
            table : null,
            pageSize: 10
        }
    };
    var initializeTable = function() {
        resultList_configuration.main.table = $("#${n}sqlResults").dataTable({
            iDisplayLength: resultList_configuration.main.pageSize,
            aLengthMenu: [5, 10, 20, 50],
            sPaginationType: 'full_numbers',
            bDeferRender: false,
            bProcessing: true,
            oLanguage: {
                sLengthMenu: '<spring:message code="datatables.length-menu.message" htmlEscape="false" javaScriptEscape="true"/>',
                oPaginate: {
                    sPrevious: '<spring:message code="datatables.paginate.previous" htmlEscape="false" javaScriptEscape="true"/>',
                    sNext: '<spring:message code="datatables.paginate.next" htmlEscape="false" javaScriptEscape="true"/>'
                }
            },
            // use results right from the model instead of pulling from server via ajax
            aaData: results,
            // dynamically create columns
            aoColumns: [
                <c:forEach items="${ results[0] }" var="row" varStatus="status">
                { mData: 'column${ status.index }', sType: 'string' }${ status.last ? "" : ","}
                </c:forEach>
            ],
            fnInitComplete: function (oSettings) {
                resultList_configuration.main.table.fnDraw();
            },
            fnInfoCallback: function( oSettings, iStart, iEnd, iMax, iTotal, sPre ) {
                var infoMessage = '<spring:message code="datatables.info.message" htmlEscape="false" javaScriptEscape="true"/>';
                var iCurrentPage = Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength) + 1;
                infoMessage = infoMessage.replace(/_START_/g, iStart).
                                      replace(/_END_/g, iEnd).
                                      replace(/_TOTAL_/g, iTotal).
                                      replace(/_CURRENT_PAGE_/g, iCurrentPage);
                return infoMessage;
            },
            // Add links to the proper columns after we get the data
            fnRowCallback: function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
            },
            // Setting the top and bottom controls
            sDom: 'r<"row alert alert-info view-filter"<"dataTables-inline"W><"dataTables-inline dataTables-right"l><"dataTables-inline dataTables-right"i><"dataTables-inline dataTables-right"p>><"row"<"span12"t>>>',
            // Filtering
            oColumnFilterWidgets: { }
        });
    };
    initializeTable();
 });
</script>
