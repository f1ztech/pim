(function() {
angular.module('pimControllers')
	.controller('MindMapCtrl', ['$scope', '$http', '$stateParams', '$location', 'workbenchService', function($scope, $http, $stateParams, $location, workbenchService) {
		window.onerror = alert;
		var container = jQuery('#mindmap-container');

		$http.get('/mindmaps/' + $stateParams.mapId).then(function(response) {
			var idea = MAPJS.content(response.data),
				imageInsertController = new MAPJS.ImageInsertController("http://localhost:4999?u="),
				mapModel = new MAPJS.MapModel(MAPJS.DOMRender.layoutCalculator, []);
			container.domMapWidget(console, mapModel, false, imageInsertController);
			jQuery('body').mapToolbarWidget(mapModel);
//			jQuery('body').attachmentEditorWidget(mapModel);
			$("[data-mm-action='export-image']").click(function () {
				MAPJS.pngExport(idea).then(function (url) {
					window.open(url, '_blank');
				});
			});
			mapModel.setIdea(idea);
			jQuery('#linkEditWidget').linkEditWidget(mapModel);
			window.mapModel = mapModel;
			jQuery('.arrow').click(function () {
				jQuery(this).toggleClass('active');
			});
			imageInsertController.addEventListener('imageInsertError', function (reason) {
				console.log('image insert error', reason);
			});
			container.on('drop', function (e) {
				var dataTransfer = e.originalEvent.dataTransfer;
				e.stopPropagation();
				e.preventDefault();
				if (dataTransfer && dataTransfer.files && dataTransfer.files.length > 0) {
					var fileInfo = dataTransfer.files[0];
					if (/\.mup$/.test(fileInfo.name)) {
						var oFReader = new FileReader();
						oFReader.onload = function (oFREvent) {
							mapModel.setIdea(MAPJS.content(JSON.parse(oFREvent.target.result)));
						};
						oFReader.readAsText(fileInfo, 'UTF-8');
					}
				}
			});
		});

		var panelHeight = $(window).height() - $('.pim-top-menu').outerHeight() - $('.pim-header').outerHeight() - 2
		$('#publication-panel').height(panelHeight);
		$('#mindmap-container').height(panelHeight);
		$('[data-toggle="tooltip"]').tooltip()

		workbenchService.clearPromises();

		$scope.$on("$destroy", function() {
			kendo.destroy($('.pim-container')[0]);
//			$scope.treeWidget && $scope.treeWidget.destroy();
		});
	}])
})();