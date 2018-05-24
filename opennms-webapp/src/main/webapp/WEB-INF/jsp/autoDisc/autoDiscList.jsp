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

<%@page language="java"
	contentType="text/html"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Distributed Poller Status" />
  <jsp:param name="headTitle" value="Distributed Poller Status" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Auto-Discovery" />
</jsp:include>
<h3>auto-discovery</h3>
<div id="linkbar">
  <ul class="o-menu">
    <c:url var="addToInventory" value="autoDiscProcess/process.htm">
        <c:param name="platformIdStr" value="${platformIdStr}"/>
        <c:param name="serverIdStr" value="${serverIdStr}"/>
      </c:url>
      <li class="o-menuitem">
        <a href="<c:out value="${addToInventory}"/>">Add to Inventory</a>
      </li>
    </ul>
</div>
<table>
  <tr>
    <th>Resource Name</th>
    <th>Status</th>
    <th>Changes</th>
  </tr>
   <c:forEach items="${resources}" var="resource">
       <tr>
         <td><c:out value="${resource.name}"/></td>
         <td><c:out value="${resource.queueStatusStr}"/></td>
       </tr>   
       
            <c:forEach items="${resource.AIServerValues}" var="server">
              <c:if test="${server.queueStatus != 0 && !server.ignored}">
                <tr>
                  <td><c:out value="${server.name}"/></td>
                  <td><c:out value="${server.queueStatusStr}"/></td>
                </tr>
              </c:if>
            </c:forEach>
        </c:forEach>
</table>
<jsp:include page="/includes/footer.jsp" flush="false"/>
