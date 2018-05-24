/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-assets', [
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

.controller('NodeAssetsCtrl', ['$scope', '$http', '$q', 'growl', 'uibDateParser','$window', function($scope, $http, $q, growl, uibDateParser,$window) {

  $scope.blackList = [ 'id', 'lastModifiedDate', 'lastModifiedBy', 'lastCapsdPoll', 'createTime' ];
  $scope.infoKeys = [ 'sysObjectId', 'sysName', 'sysLocation', 'sysContact', 'sysDescription' ];
  $scope.dateKeys = [ 'dateInstalled', 'leaseExpires', 'maintContractExpiration' ];

  $scope.dateFormat = 'yyyy-MM-dd';

  $scope.config = {};
  $scope.master = {};
  $scope.asset = {};
  $scope.suggestions = {};
  $scope.nodeId;
  $scope.nodeLabel;
  $scope.foreignSource;
  $scope.foreignId;

  $scope.init = function(nodeId) {
    $scope.nodeId = nodeId;
    $http.get('js/onms-assets/config.json')
      .success(function(config) {
        $scope.config = config;
        if(typeof($scope.nodeId)!='undefined'){
        	 $http.get('rest/nodes/' + $scope.nodeId)
             .success(function(node) {
               $scope.nodeLabel = node.label;
               $scope.foreignSource = node.foreignSource;
               $scope.foreignId = node.foreignId;
               angular.forEach($scope.dateKeys, function(key) {
                 node.assetRecord[key] = uibDateParser.parse(node.assetRecord[key], $scope.dateFormat);
               });
               $scope.master = angular.copy(node.assetRecord);
               $scope.asset = angular.copy(node.assetRecord);
               angular.forEach($scope.infoKeys, function(k) {
                 $scope.asset[k] = node[k];
               });
             })
             .error(function(msg) {
               growl.error(msg);
             });
        	
        }
        
      })
      .error(function(msg) {
        growl.error(msg);
      });
   
  };


  $scope.reset = function() {
    $scope.asset = angular.copy($scope.master);
    $scope.assetForm.$setPristine();
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
      $window.location.href = "element/nodeList.htm";
      $scope.checkRequisition(target);
    }).error(function(msg) {
      growl.error('Cannot update the asset record: ' + msg);
    });
  };

 

}]);
