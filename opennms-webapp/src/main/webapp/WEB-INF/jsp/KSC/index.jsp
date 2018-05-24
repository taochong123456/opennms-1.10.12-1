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
  session="true"
  import="
  org.opennms.web.api.Util,
  org.opennms.web.XssRequestWrapper,
  org.opennms.web.controller.ksc.CustomViewController
  "
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%
    final HttpServletRequest req = new XssRequestWrapper(request);
    final String match = req.getParameter("match");
    pageContext.setAttribute("match", match);
    final String baseHref = Util.calculateUrlBase(request);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="KSC Reports" />
</jsp:include>

<!-- A script for validating Node ID Selection Form before submittal -->
<script type="text/javascript" >
  function validateDomain()
  {
      var isChecked = false
      for( i = 0; i < document.choose_domain.domain.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_domain.domain[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the domain that you would like to report on.");
      }
      return isChecked;
  }
</script>

<div class="TwoColLeft">
 
  <h3 class="o-box">Customized Reports</h3>

  <div class="boxWrapper">
  <p>Choose the custom report title to view or modify from the list below. There are ${fn:length(reports)} custom reports to select from.</p>
	<script type="text/javascript">
		var customData = {total:"${fn:length(reports)}", records:[
											<c:set var="first" value="true"/>
											<c:forEach var="report" items="${reports}">
											  <c:if test="${match == null || match == '' || fn:containsIgnoreCase(report.value,match)}">
											    <c:choose>
											      <c:when test="${first == true}">
												<c:set var="first" value="false"/>
											        {id:"${report.key}", value:"${report.value}", type:"custom"}
											      </c:when>
											      <c:otherwise>
											        ,{id:"${report.key}", value:"${report.value}", type:"custom"}
											      </c:otherwise>
											    </c:choose>
											  </c:if>
											</c:forEach>
				                             ]};
		
		
	</script>
    <opennms:kscCustomReportList id="kscReportList" dataObject="customData"></opennms:kscCustomReportList>
    <!-- For IE Only -->
    <div name="opennms-kscCustomReportList" id="kscReportList-ie" dataObject="customData"></div>
  </div>

<h3 class="o-box">Node SNMP Interface Reports</h3>
<div class="boxWrapper">
      <p>Select node for desired performance report</p>
      <c:set var="totalNodeResources" value="${fn:length(nodeResources)}"/>
      <script type="text/javascript">
      	var nodeData = {total:"${totalNodeResources}", records:[
												<c:set var="first" value="true"/>
												<c:forEach var="resource" items="${nodeResources}">
												  <c:if test="${match == null || match == '' || fn:containsIgnoreCase(resource.label,match)}">
												    <c:choose>
												      <c:when test="${first == true}">
													<c:set var="first" value="false"/>
													{id:"${resource.name}", value:"${resource.label}", type:"node"}
												      </c:when>
												      <c:otherwise>
													,{id:"${resource.name}", value:"${resource.label}", type:"node"}
												      </c:otherwise>
												    </c:choose>
												  </c:if>
												</c:forEach>
      	                                  	]};
      </script>
      <div id="snmp-reports"></div>
      <opennms:nodeSnmpReportList id="nodeSnmpList" dataObject="nodeData"></opennms:nodeSnmpReportList>
      <div name="opennms-nodeSnmpReportList" id="nodeSnmpList-ie" dataObject="nodeData"></div>
</div>

<h3 class="o-box">Domain SNMP Interface Reports</h3>
<div class="boxWrapper">
      <c:choose>
        <c:when test="${empty domainResources}">
          <p>No data has been collected by domain</p>
        </c:when>

        <c:otherwise>
          <p>Select domain for desired performance report</p>
          <script type="text/javascript">
          		var domainData = {total:"${fn:length(domainResources)}", records:[
														<c:set var="first" value="true"/>
														<c:forEach var="resource" items="${domainResources}">
												  		  <c:if test="${match == null || match == '' || fn:containsIgnoreCase(resource.label,match)}">}">
														    <c:choose>
														      <c:when test="${first == true}">
															<c:set var="first" value="false"/>
															{id:"${resource.name}", value:"${resource.label}", type:"domain"}
														      </c:when>
														      <c:otherwise>
															,{id:"${resource.name}", value:"${resource.label}", type:"domain"}
														      </c:otherwise>
														    </c:choose>
														  </c:if>
														</c:forEach>	
          		                                		]}
          </script>
          <form method="get" name="choose_domain" action="<%= Util.calculateUrlBase(request, "KSC/customView.htm") %>" onsubmit="return validateDomain();">
            <input type="hidden" name="<%=CustomViewController.Parameters.type%>" value="domain">

                  <select style="width: 100%;" name="<%=CustomViewController.Parameters.domain%>" size="10">
                    <c:forEach var="resource" items="${domainResources}">


                      <c:choose>
                        <c:when test="${match == null || match == ''}">
                          <option value="${resource.name}">${resource.label}</option>
                        </c:when>
                        <c:otherwise>
                          <c:if test="${fn:containsIgnoreCase(resource.label,match)}">
                            <option value="${resource.name}">${resource.label}</option>
                          </c:if>
                        </c:otherwise>
                      </c:choose>



                    </c:forEach>
                  </select>

                  <input type="submit" value="Submit" alt="Initiates Generation of Domain Report"/>
          </form>
        </c:otherwise>
      </c:choose>
  </div>

</div>

<div class="TwoColRight">
  <h3 class="o-box">Descriptions</h3>

  <div class="boxWrapper">
    <p>
      <b>Customized Reports</b>
      <c:choose>
        <c:when test="${kscReadOnly == false }">
          allows users to create, view, and edit customized reports containing
          any number of prefabricated reports from any available graphable
          resource.
        </c:when>
        <c:otherwise>
          allows users to view customized reports containing any number of
          prefabricated reports from any available graphable resource.
        </c:otherwise>
      </c:choose>
    </p>

    <p>
      <b>Node and Domain Reports</b>
      <c:choose>
        <c:when test="${kscReadOnly == false }">
          allows users to view automatically generated reports for any node or
          domain.  These reports can be further edited and saved just like other
          customized reports.  These reports list only the SNMP interfaces on
          the selected node or domain, but they can be customized to include
          any graphable resource.
        </c:when>
        <c:otherwise>
          allows users to view automatically generated reports for any node or
          domain.  
        </c:otherwise>
      </c:choose>

    </p>
  </div>
</div>
<script type="text/javascript">
function doReload() {
    if (confirm("Are you sure you want to do this?")) {
        document.location = "<%=Util.calculateUrlBase(request, "KSC/index.htm?reloadConfig=true")%>";
    }
}
</script>
<input type="button" onclick="doReload()" value="Request a Reload of KSC Reports Configuration"/>

<jsp:include page="/includes/footer.jsp" flush="false"/>
