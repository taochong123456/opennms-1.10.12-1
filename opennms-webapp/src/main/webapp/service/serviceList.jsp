<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Alarm List" />
	<jsp:param name="headTitle" value="List" />
	<jsp:param name="headTitle" value="Alarms" />
	<jsp:param name="breadcrumb" value="List" />
	<jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="lib/angular-growl-v2/build/angular-growl.css" />' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular/angular.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular-growl-v2/build/angular-growl.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/bootbox/bootbox.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="js/onms-services/app.js"></script>' />
</jsp:include>

<div id="severityLegendModal" class="modal fade" tabindex="-1">
	<div class="modal-dialog">
		<div class="modal-content">
			<jsp:include page="/alarm/severity.jsp" flush="false" />
		</div>
	</div>
</div>


<!-- end menu -->

<div class="hidden">
	<jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />
</div>

<div class="row">
	<br />
</div>

 <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
        <jsp:param name="count"    value="100" />
        <jsp:param name="baseurl"  value="1"  />
        <jsp:param name="limit"    value="20" />
        <jsp:param name="multiple" value="10" />
</jsp:include>

<div  ng-app="onms-services" ng-controller="ServicesCtrl" ng-init="init('${param.serviceName}')">
    <div growl></div>
	<table class="table table-condensed severity">
		<thead>
			<tr>
				<th width="20%">IP</th>
				<th width="20%">status</th>
				<th width="20%">Operate</th>
			</tr>
		</thead>
		
		 <tr  class="severity-Normal"  ng-repeat="service in serviceList">
            <td class="divider" valign="middle" rowspan="1">
			      <a href="element/service.jsp?node={{service.nodeId}}&intf={{service.ipAddr}}&service={{service.serviceType.id}}" class="filterLink" >{{service.ipAddr}}</a>
            </td>
            <td class="divider" valign="middle" rowspan="1">
                <img style="vertical-align:middle" src="images/down.png" ng-if="service.isDown">
                <img style="vertical-align:middle" src="images/up.png" ng-if="!service.isDown">
            </td>
            <td class="divider" valign="middle" rowspan="1">
              <button type="button" class="btn btn-default" ng-click="deleteNode(service.id)" id="reset-asset">Delete&nbsp;&nbsp;&nbsp;
              </button>
               
            </td>
          </tr>
		
		
	</table>
</div>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
