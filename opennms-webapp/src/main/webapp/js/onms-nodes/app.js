/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-nodes', [
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(3000);
  growlProvider.globalPosition('bottom-center');
}])

.config(['$uibTooltipProvider', function($uibTooltipProvider) {
  $uibTooltipProvider.setTriggers({
    'mouseenter': 'mouseleave'
  });
  $uibTooltipProvider.options({
    'placement': 'right',
    'trigger': 'mouseenter'
  });
}])

.controller('NodesCtrl', ['$scope', '$http', '$q', 'growl', 'uibDateParser', function($scope, $http, $q, growl, uibDateParser) {

  $scope.blackList = [ 'id', 'lastModifiedDate', 'lastModifiedBy', 'lastCapsdPoll', 'createTime' ];
  $scope.infoKeys = [ 'sysObjectId', 'sysName', 'sysLocation', 'sysContact', 'sysDescription' ];
  $scope.dateKeys = [ 'dateInstalled', 'leaseExpires', 'maintContractExpiration' ];

  $scope.dateFormat = 'yyyy-MM-dd';

  $scope.nodelist = [];
  $scope.count;
  $scope.totalCount;
  $scope.init = function() {
    $http.get('rest/nodes/')
      .success(function(nodes) {
    	  if(nodes.count == 1){
    		  $scope.nodelist = [nodes.node];
    	  }else{
        	  $scope.nodelist = nodes.node; 
    	  }
    	  $scope.count = nodes.count;
    	  $scope.totalCount = nodes.totalCount;
      })
      .error(function(msg) {
        growl.error(msg);
      });
   
  };


  $scope.reset = function() {
    $scope.asset = angular.copy($scope.master);
    $scope.assetForm.$setPristine();
  };
  
  $scope.deleteNode = function(nodeId) {
	   $http.delete('rest/nodes/'+nodeId)
	      .success(function() {
			  //FIX ME why slices not take effect
			  var nodeArray = new Array();
			  for(var index in $scope.nodelist){
				  var node = $scope.nodelist[index];
				  if(node.id != nodeId){
				     nodeArray.push(node);
				  }
				
	    	   }
			$scope.nodelist = nodeArray;
		  
	      })
	      .error(function(msg) {
	        growl.error(msg);
	      });
   };

  $scope.save = function() {
    var target = {};
    for (var k in $scope.asset) {
      if ($scope.infoKeys.indexOf(k) == -1 && $scope.blackList.indexOf(k) == -1 && $scope.asset[k] != '' && $scope.asset[k] != null) {
        target[k] = $scope.dateKeys.indexOf(k) == -1 ? $scope.asset[k] : uibDateParser.filter($scope.asset[k], $scope.dateFormat);
      }
    }
    
    console.log('Assets to save: ' + angular.toJson(target));
    $http({
      method: 'PUT',
      url: 'rest/nodes/' + $scope.nodeId + '/assetRecord',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      data: $.param(target)
    }).success(function() {
      growl.success('The asset record has been successfully updated.');
      $scope.checkRequisition(target);
    }).error(function(msg) {
      growl.error('Cannot update the asset record: ' + msg);
    });
  };

 

}]);
