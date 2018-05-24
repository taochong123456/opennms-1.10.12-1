<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"%>


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Asset List" />
	<jsp:param name="location" value="asset" />
	<jsp:param name="headTitle" value="Assets" />
	<jsp:param name="breadcrumb"
		value="<a href='${baseHref}asset/index.jsp' title='Alarms System Page'>Assets</a>" />
	<jsp:param name="breadcrumb" value="List" />
</jsp:include>
<!-- menu -->
<div id="linkbar">
	<ul>
		<li><a href="asset/modify.jsp?node=232" title="Remove all search constraints">Add
				Asset</a></li>
		<li><a href="alarm/advsearch.jsp"
			title="More advanced searching and sorting options">Advanced
				Search</a></li>
		<li><a href="javascript:void()"
			onclick="javascript:window.open('','', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=158')"
			title="Open a window explaining the alarm severities">Severity
				Legend</a></li>
	</ul>
</div>
<jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />
<jsp:include page="/includes/key.jsp" flush="false" />
<table>
	<thead>
		<tr>

			<th width="1%">select</th>
			<th width="7%"><fmt:message key="application.name" /></th>
			<th width="19%"><fmt:message key="application.type" /></th>
			<th width="3%"><fmt:message key="application.desc" /></th>
		</tr>
	</thead>
	<c:forEach items="${pageList}" var="resourceValue">
		<tr>
			<td class="divider" valign="middle" rowspan="1"><nobr>
					<input type="checkbox" name="alarm" disabled="true" />
				</nobr></td>
			<td class="divider" valign="middle" rowspan="1"><nobr>
					   <a href="application/resource.htm?eid=${entityType}:${resourceValue.id}">${resourceValue.fqdn}</a></nobr> </td>
			<td class="divider" valign="middle" rowspan="1"><nobr>${resourceValue.platformType.name}
				</nobr></td>
			<td class="divider bright" valign="middle" rowspan="1"><nobr>${resourceValue.description}
				</nobr></td>
		</tr>
	</c:forEach>

</table>
<hr />
<p>hi alarms &nbsp;</p>

<jsp:include page="/includes/resultsIndex.jsp" flush="false">
	<jsp:param name="count" value="1" />
	<jsp:param name="baseurl" value="1" />
	<jsp:param name="limit" value="1" />
	<jsp:param name="multiple" value="1" />
</jsp:include>

<jsp:include page="/includes/footer.jsp" flush="false" />
