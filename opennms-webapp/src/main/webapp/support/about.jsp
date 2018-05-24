<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.core.db.DataSourceFactory,
		org.opennms.core.resource.Vault,
		org.opennms.core.utils.DBUtils,
		java.sql.Connection
	"
%>

<%@page import="org.opennms.core.resource.Vault"%>
<%@page import="org.opennms.web.BeanUtils"%>
<%@page import="org.opennms.netmgt.config.SyslogdConfigFactory"%>
<%@page import="org.opennms.netmgt.config.TrapdConfigFactory"%>

<%
    boolean role = true;
    
    final DBUtils d = new DBUtils();
    String dbName;
    String dbVersion;
    try {
      Connection conn = DataSourceFactory.getInstance().getConnection();
      d.watch(conn);
      dbName = conn.getMetaData().getDatabaseProductName();
      dbVersion = conn.getMetaData().getDatabaseProductVersion();
   	} catch (Exception e) {
   	  dbName = "Unknown";
      dbVersion = "Unknown";
   	} finally {
   	  d.cleanUp();
   	}
%> 


<%
   String trapPort = "Unknown";
   try {
       TrapdConfigFactory.init();
       trapPort = String.valueOf(TrapdConfigFactory.getInstance().getSnmpTrapPort());
   } catch (Throwable e) {
       // if factory can't be initialized, status is already 'Unknown'
   }

   String syslogPort = "Unknown";
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="About" />
  <jsp:param name="headTitle" value="About" />
  <jsp:param name="breadcrumb" value="<a href='support/index.htm'>Support</a>" />
  <jsp:param name="breadcrumb" value="About" />
</jsp:include>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">OpenNMS Web Console</h3>
    </div>
	<table class="table table-condensed">
		<tr>
			<th>Version:</th>
			<td><%=Vault.getProperty("version.display")%></td>
		</tr>

		<tr>
			<th>Server Time:</th>
			<td><%=new java.util.Date()%></td>
		</tr>
		<tr>
			<th>Client Time:</th>
			<td><script type="text/javascript"> document.write( new Date().toString()) </script></td>
		</tr>
		<tr>
			<th>Java Version:</th>
			<td><%=System.getProperty( "java.version" )%> (<%=System.getProperty( "java.vendor" )%>)</td>
		</tr>
		<tr>
			<th>Java Runtime:</th>
			<td><%=System.getProperty( "java.runtime.name" )%> (<%=System.getProperty( "java.runtime.version" )%>)</td>
		</tr>
		<tr>
			<th>Java Specification:</th>
			<td><%=System.getProperty( "java.specification.name" )%> (<%=System.getProperty( "java.specification.vendor" )%>,
				<%=System.getProperty( "java.specification.version" )%>)</td>
		</tr>
		<tr>
			<th>Java Virtual Machine:</th>
			<td><%=System.getProperty( "java.vm.name" )%> (<%=System.getProperty( "java.vm.vendor" )%>,
				<%=System.getProperty( "java.vm.version" )%>)</td>
			<%-- java.vm.info doesn't appear to be part of the standard Java system properties--%>
			<%-- <%=System.getProperty( "java.vm.info" )%> --%>
		</tr>
		<tr>
			<th>Java Virtual Machine Specification:</th>
			<td><%=System.getProperty( "java.vm.specification.name" )%> (<%=System.getProperty( "java.vm.specification.vendor" )%>,
				<%=System.getProperty( "java.vm.specification.version" )%>)</td>
		</tr>
		<tr>
			<th>Operating System:</th>
			<td><%=System.getProperty( "os.name" )%> <%=System.getProperty( "os.version" )%>
				(<%=System.getProperty( "os.arch" )%>)</td>
		</tr>
		<tr>
			<th>Servlet Container:</th>
			<td><%=application.getServerInfo()%> (Servlet Spec <%=application.getMajorVersion()%>.<%=application.getMinorVersion()%>)</td>
		</tr>

		<tr>
			<th>Database Type:</th>
			<td><%=dbName%></td>
		</tr>
		<tr>
			<th>Database Version:</th>
			<td><%=dbVersion%></td>
		</tr>
		
		 <tr>
          <th>OpenNMS Version:</th>
          <td><%=Vault.getProperty("version.display")%></td>
        </tr>
        <tr>
          <th>Home Directory:</th>
          <td><%=Vault.getProperty("opennms.home")%></td>
        </tr>
        <tr>
          <th>RRD store by group enabled?</th>
          <td><%=(Boolean.valueOf(Vault.getProperty("org.opennms.rrd.storeByGroup")) ? "True" : "False")%></td>
        </tr>
        <tr>
          <th>RRD store by foreign source enabled?</th>
          <td><%=(Boolean.valueOf(Vault.getProperty("org.opennms.rrd.storeByForeignSource")) ? "True" : "False")%></td>
        </tr>
        <tr>
          <th>Reports directory:</th>
          <td><%=Vault.getProperty("opennms.report.dir")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.host") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTP port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.port") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.port")%></td>
        </tr>
         <tr>
          <th>Jetty HTTPS host:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-host") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.https-host")%></td>
        </tr>
        <tr>
          <th>Jetty HTTPS port:</th>
          <td><%=Vault.getProperty("org.opennms.netmgt.jetty.https-port") == null ? "<i>Unspecified</i>" : Vault.getProperty("org.opennms.netmgt.jetty.https-port")%></td>
        </tr>
        <tr>
          <th>SNMP trap port:</th>
          <td><%=trapPort%></td>
        </tr>
        <tr>
          <th>Syslog port:</th>
          <td><%=syslogPort%></td>
        </tr>
	</table>
</div>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">OSI Certified Open Source Software</h3>
  </div>
  <div class="panel-body">

<a target="_new" href="http://www.opensource.org/"><img src="images/osi-certified.png" style="float: left;" alt="OSI Certified"/></a>
<p>This software is OSI Certified Open Source Software.<br/>
  OSI Certified is a certification mark of the
  <a href="http://www.opensource.org/">Open Source Initiative</a>.
</p>
<div style="clear:both;"></div>
</div>
</div>

<div class="panel panel-default">
	<div class="panel-heading">
		<h3 class="panel-title">Supporting Applications and Frameworks</h3>
	</div>
	<div class="panel-body">
		<div class="col-md-3">
			<p align="center">
				Powered By Jetty<br />
				<a href="http://www.eclipse.org/jetty/" target="_new">
					<img src="images/powered_by_jetty.png" style="border:0;" alt="eclipse.org/jetty/">
				</a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				Support for Tobi Oetiker's RRDTool<br />
				<a href="http://www.rrdtool.org/" target="_new">
					<img src="images/rrdtool-logo-dark.png" style="border:0;padding:1em;" alt="www.rrdtool.org">
				</a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				PostgreSQL Powered<br /> <a href="http://www.postgresql.org/"
					target="_new"><img src="images/pg-power_95x51_4.gif" style="border:0;padding:1em;" alt="www.postgresql.org"></a>
			</p>
		</div>

		<div class="col-md-3">
			<p align="center">
				Powered by Hibernate ORM<br />
				<a href="http://hibernate.org/" target="_new">
					<img src="images/hibernate_logo.gif" style="border:0;" alt="hibernate.org">
				</a>
			</p>
		</div>
	</div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
