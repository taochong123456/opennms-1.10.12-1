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

<%@page language="java" contentType="text/html" session="true"%>
<%@page import="java.util.Collection"%>
<%@page import="org.opennms.web.navigate.PageNavEntry"%>
<%@page
	import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.opennms.core.soa.ServiceRegistry"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.opennms.netmgt.config.NotifdConfigFactory"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Admin" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="location" value="admin" />
	<jsp:param name="breadcrumb" value="Admin" />
</jsp:include>

<script type="text/javascript">
	function addInterfacePost() {
		document.addInterface.action = "admin/newInterface.jsp?action=new";
		document.addInterface.submit();
	}

	function deletePost() {
		document.deleteNodes.submit();
	}

	function submitPost() {
		document.getNodes.submit();
	}

	function manageRanges() {
		document.manageRanges.submit();
	}

	function snmpManagePost() {
		document.snmpManage.submit();
	}

	function manageSnmp() {
		document.manageSnmp.submit();
	}

	function snmpConfigPost() {
		document.snmpConfig.action = "admin/snmpConfig?action=default";
		document.snmpConfig.submit();
	}

	function networkConnection() {
		document.networkConnection.submit();
	}

	function dns() {
		document.dns.submit();
	}

	function communication() {
		document.communication.submit();
	}
</script>

<form method="post" name="getNodes" action="admin/getNodes">
	<input type="hidden" />
</form>

<form method="post" name="addInterface">
	<input type="hidden" />
</form>

<form method="post" name="deleteNodes" action="admin/deleteNodes">
	<input type="hidden" />
</form>

<form method="post" name="snmpManage" action="admin/snmpGetNodes">
	<input type="hidden" />
</form>

<form method="post" name="snmpConfig" action="admin/snmpConfig">
	<input type="hidden" />
</form>

<div class="row">
	<div class="col-md-6">
		<!-- panel -->

		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">配置</h3>
			</div>
			<div class="panel-body">
				<ul class="list-unstyled">
					<li><a href="admin/categories.htm">设备分类管理</a></li>
					<li><a href="admin/discovery/edit-config.jsp">自动发现配置</a></li>
					<li><a href="javascript:snmpConfigPost()">snmp配置</a></li>
					<li><a href="javascript:addInterfacePost()">添加节点</a></li>
					<li><a href="javascript:deletePost()">删除节点</a></li>
				</ul>
			</div>
			<!-- panel-body -->
		</div>
		<!-- panel -->

		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">事件管理</h3>
			</div>
			<div class="panel-body">
				<ul class="list-unstyled">
					<li><a href="admin/notification/index.jsp">事件通知配置</a></li>
				</ul>
			</div>
			<!-- panel-body -->
			<div class="panel-footer text-right">
				<form role="form" method="post" name="notificationStatus"
					action="admin/updateNotificationStatus">
					<%
						String status = "Unknown";
						try {
							NotifdConfigFactory.init();
							status = NotifdConfigFactory.getPrettyStatus();
						} catch (Throwable e) { /*if factory can't be initialized, status is already 'Unknown'*/
						}
					%>
					<label class="control-label">通知状态:<%
						if (status.equals("Unknown")) {
					%>
						Unknown<%
						}
					%></label> &nbsp; <label for="on" class="radio-inline"><input
						style="margin-top: 0px;" type="radio" name="status" id="on"
						value="on" <%=("On".equals(status) ? "checked" : "")%> />启用</label> <label
						for="off" class="radio-inline"><input
						style="margin-top: 0px;" type="radio" name="status" id="off"
						value="off" <%=("Off".equals(status) ? "checked" : "")%> />禁用</label>
					&nbsp;
					<button type="submit" class="btn btn-default">更新</button>
				</form>
			</div>
			<!-- panel-footer -->
		</div>
		<!-- panel -->

		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">配置管理</h3>
			</div>
			<div class="panel-body">
				<ul class="list-unstyled">
					<li><a href="javascript:snmpManagePost()">snmp配置</a></li>
					<li><a href="admin/thresholds/index.htm">阈值管理</a></li>
				</ul>
			</div>
			<!-- panel-body -->
		</div>
		<!-- panel -->

	</div>
	<!-- column -->

	<div class="col-md-6">
		<div class="panel panel-default">
			<div class="panel-heading">
				<h3 class="panel-title">系统</h3>
			</div>
			<div class="panel-body">
				<ul class="list-unstyled">
					<li><a href="admin/sysconfig.jsp">系统信息</a></li>
					<li><a href="admin/support/systemReport.htm">生成系统报告</a></li>
					<li><a href="account/selfService/index.jsp">修改密码</a></li>
				</ul>

			</div>
			<!-- panel-body -->
		</div>
		<!-- panel -->
	</div>
	
	
</div>
<!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!/**
	 * Loads all in OSGI installed PageNavEntries with the properties
	 * <ul>
	 * <li>Page=admin</li>
	 * <li><b>AND</li>
	 * <li>Category=<category></li>
	 * </ul>
	 *
	 */
	protected String getAdminPageNavEntries(final String category) {
		// create query string
		String queryString = "(Page=admin)";
		if (category != null && !category.isEmpty()) {
			queryString = "(&(Page=admin)(Category=" + category + "))";
		}

		String retVal = "";
		WebApplicationContext webappContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(getServletContext());
		ServiceRegistry registry = webappContext.getBean(ServiceRegistry.class);
		Collection<PageNavEntry> navEntries = registry.findProviders(
				PageNavEntry.class, queryString);
		for (PageNavEntry navEntry : navEntries) {
			retVal += "<li><a href=\"" + navEntry.getUrl() + "\" >"
					+ navEntry.getName() + "</a></li>";
		}
		return retVal;
	}%>
