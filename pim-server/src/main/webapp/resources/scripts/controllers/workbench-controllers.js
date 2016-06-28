(function() {

angular.module('pimControllers', ['kendo.directives', 'ngResource', 'ngSanitize'])
	.controller('WorkbenchCtrl', ['$scope', '$rootScope', 'workbenchService', '$q', '$location', '$stateParams', function($scope, $rootScope, workbenchService, $q, $location, $stateParams) {
		workbenchService.clearCache();
		
		$('#publication-panel').height($(window).height() - $('.pim-top-menu').outerHeight() - $('.pim-header').outerHeight() - 2);

		$scope.$on("$destroy", function() {
			kendo.destroy($('.pim-container')[0]);
		});
	}])
	.controller('ResourcesTreeCtrl', ['$scope', '$rootScope', '$state', 'workbenchService', '$q', '$location', '$stateParams', function($scope, $rootScope, $state, workbenchService, $q, $location, $stateParams) {
		var getItem = function(el) {
			return $scope.resourcesTree.dataItem(el);
		}

		var currentResourceId = workbenchService.getCurrentResourceId();

		$scope.treeDatasource = workbenchService.treeDatasource;

	    $scope.$on('kendoWidgetCreated', function(event, widget){
	        if (widget === $scope.resourcesTree) {
        		$rootScope.resourcesTree = $scope.resourcesTree;
        		workbenchService.populateAndExpandTree($scope.resourcesTree);
	        }
	    });

		$scope.expand = function(e) {
			var item = getItem(e.node);
			!item._loaded && item.load();
			if (item.hasNarrowerResources && !item.narrowerResources.length) {
				workbenchService.findResource(item.id).then(workbenchService.getNarrowerResources).then(function(narrowerResources) {
					e.sender.append(narrowerResources, $(e.node));
				});
			}
		}

		$scope.treeItemSelected = function(e) {
			var resourceId = getItem(e.node).id;
			if ($scope.mode == 'workbench') {
				workbenchService.navigateToResource(resourceId);
			}
			$rootScope.logViewAction(resourceId);
		};

		$rootScope.onSelectResourceContextMenu = function(e) {
			var $menuNode = $(e.item);
			var action = $menuNode.data('action');
			var treeDataItem = getItem(e.target);
			var sourceResourceId = $(e.target).data('id') || treeDataItem ? treeDataItem.id : null;
			switch (action) {
			case 'create-mindmap':
				workbenchService.createMindMap(sourceResourceId);
				break;
			case 'add':
				workbenchService.newResource($menuNode.data('resourceType'), sourceResourceId).then(function(resource) {
					var newTreeNode = $scope.resourcesTree.append(resource, workbenchService.findTreeNode(sourceResourceId));
					$scope.resourcesTree.select(newTreeNode);
				});
				break;
			case 'delete':
				workbenchService.removeResource(sourceResourceId);
				break;
			default:
				break;
			}
		}

		$rootScope.logViewAction = function(resourceId) {
			workbenchService.logViewAction(resourceId).then(function(activatedResources) {
				$rootScope.activatedResources = activatedResources;
			});
		}

		workbenchService.getActivatedResources().then(function(activatedResources) {
			$rootScope.activatedResources = activatedResources;
		});

		$scope.onResourceDrop = function(e) {
			if (e.dropPosition == 'over') {
				var sourceResource = getItem(e.sourceNode),
					newParent = getItem(e.destinationNode);
				workbenchService.changeParent(sourceResource, newParent);
			}
		}

	}])
	.controller('WorkbenchResourceCtrl', ['$scope', '$rootScope', '$state', '$stateParams', '$filter', '$http', 'workbenchService', function($scope, $rootScope, $state, $stateParams, $filter, $http, workbenchService) {
		// FIXME dont use $rootScope

		$rootScope.workbenchService = workbenchService;
		$rootScope.inlineEditorTools = ['bold', 'italic', 'undeline'];
		$rootScope.recommendations = null;
		$rootScope.relatedResources = null;

	    $scope.$on('kendoWidgetCreated', function(event, widget){
	        if (widget === $scope.tagsSelect) {
	        	$rootScope.tagSelect = $scope.tagsSelect;
        		widget.input.keyup(function(e) {
        		    // only append on enter
        		    if (kendo.keys.ENTER == e.keyCode) {
        		    	workbenchService.addTag($rootScope.detailedResource.id, widget.input.val()).then(function(tag) {
        		    		// updates multi-select data source
//        		    		widget.search('');

        		    		$rootScope.detailedResource.tags.push(tag);
            		        widget.value($.map($rootScope.detailedResource.tags, function(tag) { return tag.id }));
            		        widget.trigger('change');
        		    	});
        		    }
        		});
	        }
	    });
		$rootScope.tagSelectOptions = {
			dataTextField: "title",
            dataValueField: "id",
            dataSource: {
                serverFiltering: true,
                transport: {
                    read: {
                    	dataType: "json",
                        url: "/workbench/tags",
                        data: function() {
                        	return $rootScope.tagsSelect ? {
                        		query: $rootScope.tagsSelect.input.val()
                        	} : {};
                        }
                    }
                }
            }
		}

		var prepareScope = function(resource) {
			$http.get('/workbench/resources/' + resource.id).then(function(response) {
				$rootScope.detailedResource = response.data;
			});
			workbenchService.getRecommendations(resource).then(function(recommendations) {
				$rootScope.recommendations = recommendations;
			});
			workbenchService.getRelated(resource).then(function(relatedResources) {
				$rootScope.relatedResources = relatedResources;
			});
			$rootScope.size = $filter('number')(resource.publicationFile ? resource.publicationFile.size : resource.size);
		}

		$rootScope.viewDetails = function(resource) {
			prepareScope(resource);
			$rootScope.logViewAction(resource.id);
		};

		$rootScope.updateResource = function(property) {
			workbenchService.updateResource($rootScope.detailedResource, property);
		};

		workbenchService.getDataPromise()
			.then(
				workbenchService.stateChanged
			).then(function() {
				$rootScope.narrowerResources = !workbenchService.selectedResource ? [] : workbenchService.selectedResource.narrowerResources;
				prepareScope(workbenchService.selectedResource);
			});

	}])
})();