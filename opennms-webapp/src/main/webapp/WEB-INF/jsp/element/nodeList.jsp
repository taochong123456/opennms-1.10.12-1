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
	<jsp:param name="script" value='<script type="text/javascript" src="js/onms-nodes/app.js"></script>' />
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
	<div class="col-md-6">
		<a class="btn btn-default" href="asset/addAsset.jsp"
			title="More advanced searching and sorting options">Add Asset</a> 
	</div>
	<div class="col-md-6 text-right">
			<a class="btn btn-default" href="element/nodeCategory.jsp"
				title="More advanced searching and sorting options">Category</a> 
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

<div  ng-app="onms-nodes" ng-controller="NodesCtrl" ng-init="init()">
    <div growl></div>
	<table class="table table-condensed severity">
		<thead>
			<tr>
				<th width="20%">name</th>
				<th width="20%">status</th>
				<th width="20%">IP</th>
				<th width="20%">MAC</th>
				<th width="20%">Operate</th>
			</tr>
		</thead>
		
		 <tr  class="severity-Normal"  ng-repeat="node in nodelist">
            <td class="divider" valign="middle" rowspan="1"> <nobr>
                  <a href="element/node.jsp?node={{node.id}}" class="filterLink" >{{ node.label }}</a>
                </nobr>
                、          </td>
            <td class="divider" valign="middle" rowspan="1">
               <img style="vertical-align:middle" src="{{node.status}}" >
            </td>
            <td class="divider" valign="middle" rowspan="1">{{ node.ip}}</td>
            <td class="divider" valign="middle" rowspan="1">{{ node.mac}}</td>
            <td class="divider" valign="middle" rowspan="1">
              <button type="button" class="btn btn-default" ng-click="deleteNode(node.id)" id="reset-asset">Delete&nbsp;&nbsp;&nbsp;
              </button>
               
            </td>
          </tr>
		
		
	</table>
</div>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />
