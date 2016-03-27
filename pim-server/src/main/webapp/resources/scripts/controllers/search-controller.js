(function() {
angular.module('pimControllers')
	.controller('SearchCtrl', ['$scope', '$http', 'workbenchService', function($scope, $http, workbenchService) {

		$scope.pageSize = 10;
		$scope.results = $scope.allResults = null;
		
		$(document).click(function (e) {
			if (!$(e.target).parents('.top-search-form').length) { 
				$scope.results = $scope.allResults = null;
				$scope.$digest();
			}
		});
		
		$scope.search = $.debounce(function() {
			$scope.loading = true;
			$scope.results = $scope.allResults = null;
			$http.get('/search', {params : {query : $scope.searchText}}).then(function(response) {
				$scope.allResults = response.data;
				$scope.page = 0;
				$scope.nextPage();
			}).finally(function() {
				$scope.loading = false;	
			});
		}, 500);
		
		$scope.nextPage = function() {
			var page = $scope.page ? $scope.page + 1 : 1,
				end = Math.min($scope.allResults.length, page * $scope.pageSize);

			$scope.page = page;
			$scope.results = $scope.allResults.slice(0, end)
		};
		
		$scope.hasNextPage = function() {
			return $scope.page * $scope.pageSize < $scope.allResults.length;
		}
		
		$scope.navigateToResource = function(selectedResource) {
			workbenchService.getDataPromise()
				.then(function(data) {
					return workbenchService.findResource(selectedResource.id);
				}).then(function(resource) {
					if (!resource) { // selected resource not found -> force reload data from server
						return workbenchService.populateAndExpandTree(null, selectedResource.id);
					} else {
						workbenchService.resourcesTree.select(workbenchService.findTreeNode(selectedResource.id));
					}
				}).then(function() {
					return workbenchService.navigateToResource(selectedResource.id);
				})
			$scope.showResults = false;
		}
	}])
})();