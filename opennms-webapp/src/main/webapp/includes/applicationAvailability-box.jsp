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

<%-- 
  This page is included by other JSPs to create a box containing a tree of 
  service level availability information for the interfaces and services of
  a given node.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div id="availability-box">

<h3 class="o-box">Availability</h3>
<table class="o-box">
	    <tr class="CellStatus">
	         <td class="Critical  nobright">Availability (last 24 hours)</td>
	         <td colspan="2" class="Critical  bright">1233</td>
	    </tr>
		<c:url var="interfaceLink" value="element/interface.jsp">
			<c:param name="node" value="1" />
			<c:param name="intf" value="127.0.0.1" />
		</c:url>
		
	    <tr class="CellStatus">
            <td class="Critical nobright" rowspan="4"><a href="">127.0.0.1</a></td>
            <td class="Critical  nobright">Overall</td>
            <td class="Critical  bright">1</td>
         </tr>
                       
          <c:url var="serviceLink" value="element/service.jsp">
            <c:param name="node" value="1"/>
            <c:param name="intf" value="127.0.0.1"/>
            <c:param name="service" value="1"/>
          </c:url>
          
        <c:forEach items="${serverValues}" var="serverValue">
         <tr class="CellStatus">
           <td>
           <a href="<c:out value=""/>">${serverValue.resourceName}</a>
           </td>
           <td class="Indeterminate" colspan="2">${serverValue.availability}</td>
         </tr>
        </c:forEach>

</table>   

</div>

