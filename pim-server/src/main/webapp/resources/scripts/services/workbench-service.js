(function() {

function parseResponse(response) {
	var data = response.data || response;
	var dataArr = data instanceof Array ? data : [data];
	$.each(dataArr, function () {
		for (var key in this) {
			// FIXME enumerate all date fields in special field
			if (this.hasOwnProperty(key) && key.indexOf('date') >= 0) {
				try {
					this[key] = new Date(this[key]); // FIXME timezones on server and client may be different
				} catch (e) {}
			}
		}
		if (this.narrowerResources) {
			parseResponse(this.narrowerResources);
		}
	});
	return data;
}

angular.module('pimServices', [])
	.factory('Resource', function($resource) {
		return $resource('/workbench/resources/:name', {name : '@name'}, {
			get : {
				method:'GET', interceptor: {response: parseResponse}
			},
			query : {
				method:'GET', interceptor: {response: parseResponse}, isArray : true
			},
		});
	})
	.service('workbenchService', ['$state', '$http', '$q', 'Resource', '$location', '$stateParams', function($state, $http, $q, Resource, $location, $stateParams) {
		var me = this;

		function makeResolvedPromise(ret) {
			var defered = $q.defer();
			defered.resolve(ret);
			return defered.promise;
		}

		// actual values, populated when corresponding promise would be resolved
		me.rootResources = null;
		me.selectedResource = null;
		me.activatedResources = null;

		/**
		 * @return promise for resources
		 */
		me.getRootResources = function() {
			if (me.broaderResourcesPromise) {
				return me.broaderResourcesPromise.then(function(response) {
					if (!me.rootResources) {
						me.rootResources = parseResponse(response.data.data);
					}
					return me.rootResources;
				});
			}

			if (!me.rootResourcesPromise) {
				me.rootResourcesPromise = Resource.query().$promise;
			}
			return me.rootResourcesPromise.then(function(resources) {
				if (!me.rootResources) {
					me.rootResources = resources;
				}
				return me.rootResources;
			});
		};

		me.getBroaderResources = function(resourceId) {
			if (me.rootResourcesPromise) {
				return me.rootResourcesPromise;
			}

			if (!me.broaderResourcesPromise) {
				me.broaderResourcesPromise = $http.get('/workbench/broaderResources', {params : {id : resourceId}});
			}
			return me.broaderResourcesPromise.then(function(response) {
				if (!me.rootResources) {
					me.rootResources = parseResponse(response.data.data);
				}
				return response.data;
			});
		}

		me.clearPromises = function() {
			me.broaderResourcesPromise = null;
			me.dataPromise = null;
		}

		/**
		 * @return promise for narrowerResources
		 */
		me.getNarrowerResources = function(resource) {
			if (!resource.narrowerResourcesPromise) {
				resource.narrowerResourcesPromise = $http.get('/workbench/narrowerResources', {params : {uri : resource.uri}});
			}
			return resource.narrowerResourcesPromise.then(function(response) {
				if (!resource.narrowerResources) {
					resource.narrowerResources = parseResponse(response);
				}
				return resource.narrowerResources;
			});
		};

		me.grepResource = function(resources, resourceId) {
			if (!resources) {
				return null;
			}
			for (var i = 0; i < resources.length; i++) {
				if (resources[i].id == resourceId) {
					return resources[i];
				}
				var ret = me.grepResource(resources[i].narrowerResources, resourceId);
				if (ret) {
					return ret;
				}
			}
			return null;
		}

		/**
		 * @return promise for resource
		 */
		me.findResource = function(resourceId) {
			return me.getRootResources().then(function(resources) {
				return me.grepResource(resources, resourceId);
			});
		};

		/**
		 * @return promise for narrowerResource
		 */
		me.findNarrowerResource = function(narrowerResourceId, resourceId) {
			return me.findResource(resourceId).then(me.getNarrowerResources).then(function(narrowerResources) {
				var foundedNarrowerResource = $.grep(narrowerResources, function(narrowerResource) {
					return narrowerResource.id == narrowerResourceId;
				})[0];

				return foundedNarrowerResource;
			});
		};

		me.getDataPromise = function(resourceToLoad) {
			if (resourceToLoad && me.getCurrentResourceId() != resourceToLoad) {
				me.dataPromise = me.rootResourcesPromise = me.broaderResourcesPromise = me.rootResources = null;
			}

			if (!me.dataPromise) {
				var resourceId = resourceToLoad || me.getCurrentResourceId();
				me.dataPromise = resourceId ? me.getBroaderResources(resourceId) : me.getRootResources();
			}
			return me.dataPromise;
		}

		me.getCurrentResourceId = function() {
			if ($stateParams.resourceId) {
				return $stateParams.resourceId;
			}
			// substring after last "/"
			var tail = /[^\/]*$/g.exec($location.url());
			return isNaN(tail) ? null : tail;
		}

		me.showSuccess = function(text) {
			me.showAlert($('#pim-success'), text);
		};

		me.showError = function(text) {
			me.showAlert($('#pim-failure'), text);
		};

		me.showAlert = function($el, text) {
			$el.find('.alert-text').html(text);
			$el.alert();
			$el.fadeTo(2000, 500).slideUp(500);
		};

		me.updateResource = function(resource, property) {
			return $http.put('/workbench/resources/update', {"uri" : resource.uri, "property" : property, "value" : resource[property]}).then(
				function() {
					me.showSuccess('Изменения сохранены');
				},
				function(e) {
					me.showError('Ошибка при сохранении данных: ' + e.message);
					console.error(e);
				});
		};

		me.navigateToResource = function(resourceId) {
			return $state.go('workbench.resource', {resourceId : resourceId});
		};

		me.stateChanged = function() {
			if ($state.params.resourceId) {
				return me.findResource($state.params.resourceId).then(function(resource) {
					me.selectedResource = resource;
				}).then(function() {
					return me.getNarrowerResources(me.selectedResource);
				});
			}
		};

		me.newResource = function(resourceType, broaderResourceId) {
			return $http.post('/workbench/resources', {'resourceType' : resourceType, 'broaderResource' : broaderResourceId}).then(function(response) {
				if (broaderResourceId) {
					me.findResource(broaderResourceId).then(me.getNarrowerResources).then(function(narrowerResources) {
						narrowerResources.push(response.data);
					});
				} else {
					me.getRootResources().then(function(rootResources) {
						rootResources.push(response.data);
					});
				}
				return response.data;
			});
		}

		me.changeParent = function(sourceResource, newParent) {
			return $http.post('/workbench/changeParent', {'sourceResource' : sourceResource.id, 'newParent' : newParent.id});
		}

		me.removeResource = function(resourceId) {
			return $http.delete('/workbench/resources/' + resourceId);
		}

		me.getActivatedResources = function() {
			if (me.activatedResources) {
				return makeResolvedPromise(me.activatedResources);
			}
			if (me.activatedResourcesPromise) {
				return me.activatedResourcesPromise;
			} else {
				me.activatedResourcesPromise = $http.get('/workbench/activatedResources');
				return me.activatedResourcesPromise.then(function(response) {
					me.activatedResources = parseResponse(response.data);
					return me.activatedResources;
				});
			}
		}

		me.logViewAction = function(resourceId) {
			return $http.get('/workbench/logAction', {params : {id : resourceId, actionType : 'VIEW'}}).then(function(response) {
				me.activatedResources = parseResponse(response.data);
				return me.activatedResources;
			});
		}

		me.getRecommendations = function(resource) {
			if (resource.recommendations) {
				return $q.when(resource.recommendations);
			} else {
				return $http.get('/workbench/recommendations', {params : {id : resource.id}}).then(function(response) {
					resource.recommendations = response.data;
					return response.data;
				});
			}
		}

		me.treeDatasource = new kendo.data.HierarchicalDataSource({
			data : [],
			schema : {
				model : {
					children : 'narrowerResources',
					hasChildren : 'hasNarrowerResources'
				}
			},
		});

		me.populateAndExpandTree = function(tree, selectedResourceId) {
			me.resourcesTree = tree || me.resourcesTree;
			selectedResourceId = selectedResourceId || me.getCurrentResourceId();

			return me.getDataPromise(selectedResourceId).then(function(response) {
				var data = response.data || response;
				me.treeDatasource.data(data);
				if (response.path) {
					$.each(response.path, function() {
						me.treeDatasource.get(this).load();
					});
					me.resourcesTree.expandPath(response.path);
					me.resourcesTree.select(me.findTreeNode(selectedResourceId));
				}
			});
		}

		me.findTreeNode = function(resourceId) {
			return !resourceId ? null : me.resourcesTree.findByUid(me.treeDatasource.get(resourceId).uid);
		}

		me.addTag = function(resourceId, tagName) {
			return $http.post('/workbench/tags', {resourceId : resourceId, tagName : tagName}).then(function(response) {
				return response.data;
			});
		}

		me.createMindMap = function(resourceId) {
			return $http.put('/mindmaps', resourceId).then(function(response) {
				return $state.go('mindmap', {mapId : response.data});
			});
		}
	}]);
})();