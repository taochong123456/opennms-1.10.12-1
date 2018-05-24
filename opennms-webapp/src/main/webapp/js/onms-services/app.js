/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-services', [
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

.controller('ServicesCtrl', ['$scope', '$http', '$q', 'growl', 'uibDateParser', function($scope, $http, $q, growl, uibDateParser) {
  $scope.serviceList = [];
  $scope.count;
  $scope.totalCount;
  $scope.init = function(serviceName) {
    $http.get('rest/services/'+serviceName)
      .success(function(services) {
    	  if(services.count == 1){
    		  $scope.serviceList = [services.service];
    	  }else{
        	  $scope.serviceList = services.service; 
    	  }
    	  $scope.count = services.count;
    	  $scope.totalCount = services.totalCount;
      })
      .error(function(msg) {
        growl.error(msg);
      });
   
  };
  
  $scope.deleteService = function(serviceId) {
	   $http.delete('rest/services/'+serviceId)
	      .success(function() {
	      })
	      .error(function(msg) {
	        growl.error(msg);
	      });
   };

}]);
