<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Alarm List" />
	<jsp:param name="headTitle" value="List" />
	<jsp:param name="useStyle" value="true" />
	<jsp:param name="headTitle" value="Alarms" />
	<jsp:param name="breadcrumb" value="<a href='${baseHref}alarm/index.htm' title='Alarms System Page'>Alarms</a>" />
	<jsp:param name="breadcrumb" value="List" />
	<jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="lib/angular-growl-v2/build/angular-growl.css" />' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular/angular.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/angular-growl-v2/build/angular-growl.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="lib/bootbox/bootbox.js"></script>' />
	<jsp:param name="script" value='<script type="text/javascript" src="js/onms-monitor/app.js"></script>' />
</jsp:include>

<div class="row">
	<div class="col-md-12 text-right">
		<a class="btn btn-default" href="element/nodeList.htm"
			title="More advanced searching and sorting options">list</a>
	</div>
</div>

<body class="gray-bg">
	<div class="wrapper wrapper-content  animated fadeInRight"
		ng-app="onms-monitor" ng-controller="MonitorCtrl" ng-init="init()">
		<div ng-repeat="monitorCategory in monitorlist.monitorRow">
			<div class="row">
				<div class="col-sm-3" ng-repeat="group in monitorCategory.group">
					<div class="ibox">
						<div class="ibox-content">
							<h3>{{group.label}}</h3>
							<ul class="sortable-list connectList agile-list"
								ng-repeat="item in group.server">
								<li class="warning-element">
									<div class="agile-detail">
										<img src="images/server/{{item.icon}}" /> <a href="{{item.name}}"
											style="padding-left: 20px; text-decoration: underline" ng-if="item.count!=0">{{item.label}}</a>
											<span ng-if="item.count==0">{{item.label}}</span> 
                                         
										<span class="badge pull-right "
											style="background-color: green;" ng-if="item.count!=0">{{item.count}}</span>
									</div>
								</li>
							</ul>
						</div>
					</div>
				</div>
			</div>

		</div>

	</div>
</body>

