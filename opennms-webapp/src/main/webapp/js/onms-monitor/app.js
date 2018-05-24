/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-monitor', [
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

.controller('MonitorCtrl', ['$scope', '$http', '$q', 'growl', 'uibDateParser', function($scope, $http, $q, growl, uibDateParser) {
  $scope.monitorlist = [];
  $scope.init = function() {
    $http.get('rest/monitorCategory/list')
      .success(function(monitorCategory) {
    	  $scope.monitorlist = monitorCategory;
      })
      .error(function(msg) {
        growl.error(msg);
      });
   
  };

}]);
