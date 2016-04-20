(function() {
angular.module('pimControllers')
	.controller('MailCtrl', ['$scope', '$http', '$state', 'workbenchService', function($scope, $http, $state, workbenchService) {
		
		function handleOauthAuthentication(response) {
			if (response.data.authorizationUrl) {
				window.location = response.data.authorizationUrl;
				return true;
			}
			return false;
		}

		function getUserConfigs() {
			$http.get('/mail/userConfigs').then(function(response) {
				handleOauthAuthentication(response);

				var data = response.data;
				$scope.userConfigs = data.userConfigs;
				$scope.allFolders = !data.allFolders ? [] : $.each(data.allFolders, function(ind, folder) {
					folder.$checked = $.grep(data.userConfigs.synchronizedEmailFolders, function(syncFolder) {
						return syncFolder.name == folder.name;
					}).length > 0;
				});
			});
		}

		
		$scope.callback = $state.current.data && $state.current.data.callback;

		$scope.checkState = {all : false};
		$scope.userConfigs = {};

		getUserConfigs();
		
		$scope.saveCredentials = function() {
			$scope.userConfigs.synchronizedEmailFolders = !$scope.allFolders ? [] : $.grep($scope.allFolders, function(folder) { return folder.$checked });
			$http.post('/mail/userConfigs', $scope.userConfigs).then(function(response) {
				var hasRedirect = handleOauthAuthentication(response);
				if (!hasRedirect) {
					getUserConfigs();
				}
			});
		}

		$scope.checkAll = function() {
			if ($scope.checkState.all) {
				$.each($scope.allFolders, function(ind, folder) {folder.$checked = true});
			} else {
				$.each($scope.allFolders, function(ind, folder) {folder.$checked = false});
			}
		}
	}])
})();