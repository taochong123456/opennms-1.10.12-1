<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Node" />
	<jsp:param name="headTitle" value="${model.label}" />
	<jsp:param name="headTitle" value="ID ${model.id}" />
	<jsp:param name="headTitle" value="Node" />
	<jsp:param name="breadcrumb"
		value="<a href='element/index.jsp'>Search</a>" />
	<jsp:param name="breadcrumb" value="Node" />
	<jsp:param name="enableExtJS" value="false" />
</jsp:include>

<div class="onms">
	<h2>Node: test (ID: 1)</h2>
	
	<div id="linkbar">
		<ul class="o-menu">
			<c:url var="eventLink" value="event/list">
				<c:param name="filter" value="node=${model.id}" />
			</c:url>
			<li class="o-menuitem"><a href="<c:out value="${eventLink}"/>">View
					Events</a></li>

			<c:url var="alarmLink" value="alarm/list.htm">
				<c:param name="filter" value="node=${model.id}" />
			</c:url>
			<li class="o-menuitem"><a href="<c:out value="${alarmLink}"/>">View
					Alarms</a></li>

			<c:url var="outageLink" value="outage/list.htm">
				<c:param name="filter" value="node=${model.id}" />
			</c:url>
			<li class="o-menuitem"><a href="<c:out value="${outageLink}"/>">View
					Outages</a></li>

			<c:url var="assetLink" value="asset/modify.jsp">
				<c:param name="node" value="${model.id}" />
			</c:url>
			<li class="o-menuitem"><a href="<c:out value="${assetLink}"/>">Asset
					Info</a></li>

			<c:if test="${! empty model.statusSite}">
				<c:url var="siteLink" value="siteStatusView.htm">
					<c:param name="statusSite" value="${model.statusSite}" />
				</c:url>
				<li class="o-menuitem"><a href="<c:out value="${siteLink}"/>">Site
						Status</a></li>
			</c:if>

			<c:forEach items="${model.links}" var="link">
				<li class="o-menuitem"><a href="<c:out value="${link.url}"/>">${link.text}</a>
				</li>
			</c:forEach>

			<c:if test="${! empty model.resources}">
				<c:url var="resourceGraphsUrl" value="graph/chooseresource.htm">
					<c:param name="parentResourceType" value="node" />
					<c:param name="parentResource" value="${model.id}" />
					<c:param name="reports" value="all" />
				</c:url>
				<li class="o-menuitem"><a
					href="<c:out value="${resourceGraphsUrl}"/>">Resource Graphs</a></li>
			</c:if>

			<c:if test="${model.admin}">
				<c:url var="rescanLink" value="element/rescan.jsp">
					<c:param name="node" value="${model.id}" />
				</c:url>
				<li class="o-menuitem"><a href="<c:out value="${rescanLink}"/>">Rescan</a>
				</li>

				<c:url var="adminLink" value="admin/nodemanagement/index.jsp">
					<c:param name="node" value="${model.id}" />
				</c:url>
				<li class="o-menuitem"><a href="<c:out value="${adminLink}"/>">Admin</a>
				</li>

				<c:if test="${! empty model.snmpPrimaryIntf}">
					<c:url var="updateSnmpLink" value="admin/updateSnmp.jsp">
						<c:param name="node" value="${model.id}" />
						<c:param name="ipaddr" value="${model.snmpPrimaryIntf.ipAddress}" />
					</c:url>
					<li class="o-menuitem"><a
						href="<c:out value="${updateSnmpLink}"/>">Update SNMP</a></li>
				</c:if>

				<c:url var="createOutage" value="admin/sched-outages/editoutage.jsp">
					<c:param name="newName" value="${model.label}" />
					<c:param name="addNew" value="true" />
					<c:param name="nodeID" value="${model.id}" />
				</c:url>
				<li class="o-menuitem"><a
					href="<c:out value="${createOutage}"/>">Schedule Outage</a></li>
			</c:if>
		</ul>
	</div>
</div>

<div class="TwoColLeft">
	<!-- Asset box, if info available -->
	<c:if
		test="${! empty model.asset && (! empty model.asset.description || ! empty model.asset.comments)}">
		<h3 class="o-box">Asset Information</h3>
		<table class="o-box">
			<tr>
				<th>Description</th>
				<td>${model.asset.description}</td>
			</tr>

			<tr>
				<th>Comments</th>
				<td>${model.asset.comments}</td>
			</tr>
		</table>
	</c:if>

	<h3 class="o-box">Attributes</h3>
	<table class="o-box">
		<c:forEach items="${cprops}" var="cprop">
			<tr>
				<th>${cprop.key}</th>
				<td>${cprop.value}</td>
			</tr>
		</c:forEach>
	</table>
	<jsp:include page="/includes/applicationAvailability-box.jsp"
		flush="false" />
</div>

<div class="TwoColRight">
   <jsp:include page="/includes/metric.jsp" flush="false"></jsp:include>
	<jsp:include page="/includes/nodeCategory-box.htm?node=232"
		flush="false" />
		
	<c:url var="eventListUrl" value="event/list">
		<c:param name="filter" value="node=232" />
	</c:url>
	<jsp:include page="/includes/eventlist.jsp" flush="false">
		<jsp:param name="node" value="232" />
		<jsp:param name="throttle" value="5" />
		<jsp:param name="header" value="<a href='11'>Recent Events</a>" />
		<jsp:param name="moreUrl" value="11" />
	</jsp:include>

	<!-- Recent outages box -->
	<jsp:include page="/outage/nodeOutages-box.htm" flush="false">
		<jsp:param name="node" value="232" />
	</jsp:include>
	<%-- <!-- general info box -->
  <h3 class="o-box">General (Status: ${model.status})</h3>
  
  <!-- Category box -->
 
  
  <!-- notification box -->
  <jsp:include page="/includes/notification-box.jsp" flush="false" >
    <jsp:param name="node" value="${model.id}" />
  </jsp:include>
  
  <!-- events list  box -->
  <c:url var="eventListUrl" value="event/list">
    <c:param name="filter" value="node=${model.id}"/>
  </c:url>
  <jsp:include page="/includes/eventlist.jsp" flush="false" >
    <jsp:param name="node" value="${model.id}" />
    <jsp:param name="throttle" value="5" />
    <jsp:param name="header" value="<a href='${eventListUrl}'>Recent Events</a>" />
    <jsp:param name="moreUrl" value="${eventListUrl}" />
  </jsp:include>
  
  <!-- Recent outages box -->
  <jsp:include page="/outage/nodeOutages-box.htm" flush="false"> 
    <jsp:param name="node" value="${model.id}" />
  </jsp:include> --%>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />
