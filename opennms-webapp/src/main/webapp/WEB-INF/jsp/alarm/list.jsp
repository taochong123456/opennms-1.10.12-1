<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java" contentType="text/html" session="true" %>
  
<%@page import="org.opennms.core.utils.InetAddressUtils" %>
<%@page import="org.opennms.web.WebSecurityUtils" %>
<%@page import="org.opennms.netmgt.model.OnmsAlarm" %>
<%@page import="org.opennms.web.alarm.AcknowledgeType" %>
<%@page import="org.opennms.web.alarm.SortStyle" %>
<%@page import="org.opennms.web.alarm.filter.*" %>
<%@page import="org.opennms.web.api.Util" %>
<%@page import="org.opennms.web.controller.alarm.AlarmSeverityChangeController" %>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@page import="org.opennms.web.XssRequestWrapper" %>
<%@page import="org.opennms.web.tags.filters.AlarmFilterCallback" %>
<%@page import="org.opennms.web.tags.filters.FilterCallback" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--
  This page is written to be the display (view) portion of the AlarmQueryServlet
  at the /alarm/list.htm URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) alarms: the list of {@link OnmsAlarm} instances to display
  2) parms: an org.opennms.web.alarm.AlarmQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
    urlBase = (String) request.getAttribute("relativeRequestPath");

    XssRequestWrapper req = new XssRequestWrapper(request);

    //required attributes
    OnmsAlarm[] alarms = new OnmsAlarm[]{};
    long alarmCount = 100l;
    NormalizedQueryParameters parms = new NormalizedQueryParameters();
    FilterCallback callback = (AlarmFilterCallback) req.getAttribute("callback");

    if( alarms == null || parms == null ) {
        throw new ServletException( "Missing either the alarms or parms request attribute." );
    }

    // Make 'action' the opposite of the current acknowledgement state
    String action = AcknowledgeType.ACKNOWLEDGED.getShortName();
   

    // optional bookmark

    pageContext.setAttribute("addPositiveFilter", "<i class=\"fa fa-plus-square-o\"></i>");
    pageContext.setAttribute("addNegativeFilter", "<i class=\"fa fa-minus-square-o\"></i>");
    pageContext.setAttribute("addBeforeFilter", "<i class=\"fa fa-toggle-right\"></i>");
    pageContext.setAttribute("addAfterFilter", "<i class=\"fa fa-toggle-left\"></i>");
%>
<c:set var="baseHref" value="<%=Util.calculateUrlBase(request)%>"/>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Alarm List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}alarm/index.htm' title='Alarms System Page'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<div id="severityLegendModal" class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
        <jsp:include page="/alarm/severity.jsp" flush="false" />
    </div>
  </div>
</div>

      <!-- menu -->
      <div class="row">
      <div class="col-md-12">
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, new ArrayList<Filter>())%>" title="Remove all search constraints" >View all alarms</a>
      <a class="btn btn-default" href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a>
      <c:choose>
        <c:when test="${param.display == 'long'}">
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, "short")%>" title="Summary List of Alarms">Short Listing</a>
        </c:when>
        <c:otherwise>
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, "long")%>" title="Detailed List of Alarms">Long Listing</a>
        </c:otherwise>
      </c:choose>
      <a class="btn btn-default" onclick="$('#severityLegendModal').modal()">Severity Legend</a>
      
      <% if( true) { %>
        <% if ( alarmCount > 0 ) { %>
            <!-- hidden form for acknowledging the result set -->
            <form style="display:inline" method="post" action="<%= Util.calculateUrlBase(req, "alarm/acknowledgeByFilter") %>" name="acknowledge_by_filter_form">
              <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
              <input type="hidden" name="actionCode" value="<%=action%>" />
              <%=Util.makeHiddenTags(req)%>
            </form>
            <% if(true) { %>
              <a class="btn btn-default" href="javascript:void()" onclick="if (confirm('Are you sure you want to acknowledge all alarms in the current search including those not shown on your screen?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Acknowledge all alarms that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
            <% } else { %>
              <a class="btn btn-default" href="#javascript:void()" onclick="if (confirm('Are you sure you want to unacknowledge all alarms in the current search including those not shown on your screen)?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Unacknowledge all alarms that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>
            <% } %>
        <% } %>
      <% } %>
      </div>
      </div>
      <!-- end menu -->

<div class="hidden">
  <jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />
</div>


<div class="row">
  <br/>
</div>

<div class="row">
  <div class="col-sm-6 col-md-3">
  <div class="input-group">
    <span class="input-group-addon">
      <c:choose>
      <c:when test="${favorite == null}">
      <a onclick="createFavorite()">
        <!-- Star outline -->
        <i class="fa fa-lg fa-star-o"></i>
      </a>
      </c:when>
      <c:otherwise>
     
      </c:otherwise>
      </c:choose>
    </span>
    <!-- Use background-color:white to make it look less disabled -->
    <input type="text" class="form-control" style="background-color:white;" readonly placeholder="Unsaved filter" value="<c:out value="${favorite.name}"/>"/>
    <div class="input-group-btn">
      <div class="dropdown">
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
          <span class="caret"></span>
        </button>
       
      </div>
    </div>
  </div>
  </div>


</div>

<div class="row">
  <br/>
</div>


            <% if( alarmCount > 0 ) { %>
              <% String baseUrl = this.makeLink(callback, parms); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=alarmCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
              </jsp:include>
            <% } %>

      <% if( true ) { %>
          <form class="form-inline" action="<%= Util.calculateUrlBase(request, "alarm/acknowledge") %>" method="post" name="alarm_action_form">
          <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
          <input type="hidden" name="actionCode" value="<%=action%>" />
          <%=Util.makeHiddenTags(req)%>
      <% } %>

      <table class="table table-condensed severity">
				<thead>
					<tr>
                                             <% if(true) { %>
						<% if ( true) { %>
						<th width="1%">Ack</th>
						<% } else if ( true) { %>
						<th width="1%">UnAck</th>
						<% } else if ( true ) { %>
						<th width="1%">Ack?</th>
						<% } %>
                    <% } else { %>
                        <th width="1%">&nbsp;</th>
                    <% } %>



			<th width="7%">
              <%=this.makeSortLink(callback, parms, SortStyle.ID,        SortStyle.REVERSE_ID,        "id",        "ID" )%>
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.SEVERITY,  SortStyle.REVERSE_SEVERITY,  "severity",  "Severity")%>
            </th>
			<th width="19%">
              <%=this.makeSortLink(callback, parms, SortStyle.NODE,      SortStyle.REVERSE_NODE,      "node",      "Node")%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.INTERFACE, SortStyle.REVERSE_INTERFACE, "interface", "Interface")%>
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.SERVICE,   SortStyle.REVERSE_SERVICE,   "service",   "Service" )%>
              </c:if>
            </th>
			<th width="3%">
              <%=this.makeSortLink(callback, parms, SortStyle.COUNT,  SortStyle.REVERSE_COUNT,  "count",  "Count")%>
            </th>
			<th width="13%">
              <%=this.makeSortLink(callback, parms, SortStyle.LASTEVENTTIME,  SortStyle.REVERSE_LASTEVENTTIME,  "lasteventtime",  "Last Event Time" )%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.FIRSTEVENTTIME,  SortStyle.REVERSE_FIRSTEVENTTIME,  "firsteventtime",  "First Event Time" )%>
              <br />
            
              </c:if>
            </th>
			<th width="56%">Log Msg</th>
		</tr>
	</thead>

      <% for( int i=0; i < alarms.length; i++ ) { 
      	pageContext.setAttribute("alarm", alarms[i]);
      %> 

        <tr class="severity-<%=alarms[i].getSeverity().getLabel()%>">
              <td class="divider" valign="middle" rowspan="1">
                <nobr>
                  <input type="checkbox" name="alarm" disabled="disabled" /> 
                </nobr>
               </td>

          
          <td class="divider bright" valign="middle" rowspan="1">
            
            <a style="vertical-align:middle" href="<%= Util.calculateUrlBase(request, "alarm/detail.htm?id=" + alarms[i].getId()) %>"><%=alarms[i].getId()%></a>
            <c:if test="<%= (alarms[i].getStickyMemo() != null && alarms[i].getStickyMemo().getId() != null) && (alarms[i].getReductionKeyMemo() != null && alarms[i].getReductionKeyMemo().getId() != null) %>">
                <br />
            </c:if>
            <c:if test="<%= alarms[i].getStickyMemo() != null && alarms[i].getStickyMemo().getId() != null%>">
                <img style="vertical-align:middle" src="images/AlarmMemos/StickyMemo.png" width="20" height="20" title="<%=alarms[i].getStickyMemo().getBody() %>"/>
            </c:if>
            <c:if test="<%= alarms[i].getReductionKeyMemo() != null && alarms[i].getReductionKeyMemo().getId() != null%>">
                <img style="vertical-align:middle" src="images/AlarmMemos/JournalMemo.png" width="20" height="20" title="<%=alarms[i].getReductionKeyMemo().getBody() %>"/>
            </c:if>

          <c:if test="${param.display == 'long'}">
            <% if(alarms[i].getUei() != null) { %>
              <% Filter exactUEIFilter = new ExactUEIFilter(alarms[i].getUei()); %>
                <br />UEI
              <% if( !parms.getFilters().contains( exactUEIFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, exactUEIFilter, true)%>" class="filterLink" title="Show only events with this UEI">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeExactUEIFilter(alarms[i].getUei()), true)%>" class="filterLink" title="Do not show events for this UEI">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
            <% Filter severityFilter = new SeverityFilter(alarms[i].getSeverity()); %>      
            <% if( !parms.getFilters().contains( severityFilter )) { %>
		<br />Sev.
              <nobr>
                <a href="<%=this.makeLink(callback, parms, severityFilter, true)%>" class="filterLink" title="Show only alarms with this severity">${addPositiveFilter}</a>
                <a href="<%=this.makeLink(callback, parms, new NegativeSeverityFilter(alarms[i].getSeverity()), true)%>" class="filterLink" title="Do not show alarms with this severity">${addNegativeFilter}</a>

              </nobr>
            <% } %>
          </c:if>
          </td>
          <td class="divider">
	    <% if(alarms[i].getNodeId() != null && alarms[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(alarms[i].getNodeId(), getServletContext()); %>             
              <% String[] labels = this.getNodeLabels( alarms[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=alarms[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.getFilters().contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, nodeFilter, true)%>" class="filterLink" title="Show only alarms on this node">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeNodeFilter(alarms[i].getNodeId(), getServletContext()), true)%>" class="filterLink" title="Do not show alarms for this node">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <c:if test="${param.display == 'long'}">
		<br />
            <% if(alarms[i].getIpAddr() != null ) { %>
              <% Filter intfFilter = null; %>
              <% if( alarms[i].getNodeId() != null ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(alarms[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=InetAddressUtils.str(alarms[i].getIpAddr())%>"/>
                </c:url>
                <a href="<c:out value="${interfaceLink}"/>" title="More info on this interface"><%=InetAddressUtils.str(alarms[i].getIpAddr())%></a>
              <% } else { %>
                <%=InetAddressUtils.str(alarms[i].getIpAddr())%>
              <% } %>
              <% if( !parms.getFilters().contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, intfFilter, true)%>" class="filterLink" title="Show only alarms on this IP address">${addPositiveFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <br />
            <% if(alarms[i].getServiceType() != null && !"".equals(alarms[i].getServiceType().getName())) { %>
              <% Filter serviceFilter = null; %>
              <% if( alarms[i].getNodeId() != null && alarms[i].getIpAddr() != null ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(alarms[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=InetAddressUtils.str(alarms[i].getIpAddr())%>"/>
                  <c:param name="service" value="<%=String.valueOf(alarms[i].getServiceType().getId())%>"/>
                </c:url>
                <a href="<c:out value="${serviceLink}"/>" title="More info on this service"><c:out value="<%=alarms[i].getServiceType().getName()%>"/></a>
              <% } else { %>
                <c:out value="<%=alarms[i].getServiceType().getName()%>"/>
              <% } %>
              <% if( !parms.getFilters().contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, serviceFilter, true)%>" class="filterLink" title="Show only alarms with this service type">${addPositiveFilter}</a>
                </nobr>
              <% } %>                            
            <% } %>
            </c:if>
          </td>          
          <td class="divider" valign="middle" rowspan="1" >
	    <% if(alarms[i].getId() > 0 ) { %>           
                <nobr>
                  <a href="event/list.htm?sortby=id&amp;acktype=unack&amp;filter=alarm%3d<%=alarms[i].getId()%>"><%=alarms[i].getCounter()%></a>
                </nobr>
            <% } else { %>
            <%=alarms[i].getCounter()%>
            <% } %>
          </td>
          <td class="divider">
            <nobr>
              <% if(alarms[i].getLastEvent() != null) { %><span title="Event <%= alarms[i].getLastEvent().getId()%>"><a href="event/detail.htm?id=<%= alarms[i].getLastEvent().getId()%>"><% } %>
                <fmt:formatDate value="${alarm.lastEventTime}" type="BOTH" />
              <% if(alarms[i].getLastEvent() != null) { %></a></span><% } %>
              <a href="<%=this.makeLink(callback, parms, new AfterLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <c:if test="${param.display == 'long'}">
          <br />
            <nobr>
              <fmt:formatDate value="${alarm.firstEventTime}" type="BOTH" />
              <a href="<%=this.makeLink(callback, parms, new AfterFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <br />
			<nobr><%=alarms[i].getAckUser()%></nobr>          
            <nobr>
              <a href="<%=this.makeLink(callback, parms, new AcknowledgedByFilter(alarms[i].getAckUser()), true)%>"  class="filterLink" title="Only show alarms ack by this user">${addPositiveFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new NegativeAcknowledgedByFilter(alarms[i].getAckUser()), true)%>" class="filterLink" title="Only show alarms ack by other users">${addNegativeFilter}</a>
            </nobr>
          </c:if>
          </td>
          <td class="divider"><%=WebSecurityUtils.sanitizeString(alarms[i].getLogMsg(), true)%></td>
        </tr> 
      <% } /*end for*/%>

      </table>
			<hr />
			 <p><%=alarms.length%> alarms &nbsp;
      <% if( true ) { %>
          <input class="btn btn-default" TYPE="reset" />
          <input class="btn btn-default" TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
          <select class="form-control" name="alarmAction">
       
          <option value="clear">Clear Alarms</option>
          <option value="escalate">Escalate Alarms</option>
          </select>
          <input class="btn btn-default" type="button" value="Go" onClick="submitForm(document.alarm_action_form.alarmAction.value)" />
      <% } %>
        </p>
      </form>


        <% if( alarmCount > 0 ) { %>
          <% String baseUrl = this.makeLink(callback, parms); %>
          <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
            <jsp:param name="count"    value="<%=alarmCount%>" />
            <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
            <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
            <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
          </jsp:include>
        <% } %>


<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


<%!
    String urlBase;

    protected String makeSortLink(FilterCallback callback, NormalizedQueryParameters parms, SortStyle style, SortStyle revStyle, String sortString, String title) {
         return "";
    }

    public String makeLink( FilterCallback callback, NormalizedQueryParameters params) {
        return "";
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, String display) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setDisplay(display);
        return makeLink(callback, newParms);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, SortStyle sortStyle) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setSortStyleShortName(sortStyle.getShortName());
        return makeLink(callback, newParms);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, AcknowledgeType ackType) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        return makeLink(callback, newParms);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, List<Filter> filters) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms); // clone;
        newParms.setFilters(filters);
        return this.makeLink(callback, newParms);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, Filter filter, boolean add) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        List<Filter> newList = new ArrayList<Filter>( parms.getFilters());
        if( add ) {
            newList.add( filter );
        } else {
            newList.remove( filter );
        }
        newParms.setFilters(newList);
        return this.makeLink(callback, newParms);
    }

    public String[] getNodeLabels( String nodeLabel ) {
        String[] labels = null;

        if( nodeLabel.length() > 32 ) {
            String shortLabel = nodeLabel.substring( 0, 31 ) + "...";                        
            labels = new String[] { shortLabel, nodeLabel };
        }
        else {
            labels = new String[] { nodeLabel, nodeLabel };
        }

        return( labels );
    }

%>
