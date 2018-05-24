<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Distributed Poller Status" />
	<jsp:param name="headTitle" value="Distributed Poller Status" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb"
		value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Agent List" />
</jsp:include>

<h3>
	<fmt:message key="agent.list" />
</h3>

<table>
	<tr>
		<th><fmt:message key="plugin.name" /></th>
		<th><fmt:message key="plugin.version" /></th>
		<th><fmt:message key="plugin.creationTime" /></th>
		<th><fmt:message key="plugin.modifiedTime" /></th>
		<th><fmt:message key="plugin.disabled" /></th>

	</tr>
	<c:forEach items="${plugins}" var="plugin">
		<tr>
			<td class="divider">${plugin.name}</td>
			<td class="divider">${plugin.version}</td>
			<td class="divider">${plugin.creationTime}</td>
			<td class="divider">${plugin.modifiedTime}</td>
			<c:if test="${plugin.disabled}">
			  <td class="divider"><img src="../../opennms/images/enaable.png" alt="enable"/></td>
			</c:if>
			<c:if test="${plugin.disabled==false}">
			  <td class="divider"><img src="/images/logo.png" alt="enable"/></td>
			</c:if>
		</tr>
	</c:forEach>

</table>


<jsp:include page="/includes/footer.jsp" flush="false" />
