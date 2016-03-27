angular.module('pimApp', ['ui.router', 'pimControllers', 'pimServices'])
.config([
	'$stateProvider', '$urlRouterProvider', '$locationProvider',
    function ($stateProvider, $urlRouterProvider, $locationProvider) {
//		$locationProvider.html5Mode(true);

		$stateProvider
			.state('index', {
				url: '',
				templateUrl: '/partials/index.html',
				controller: 'WorkbenchCtrl'
			})
			.state('workbench', {
				url: '/workbench',
				templateUrl: '/partials/workbench/index.html',
				controller: 'WorkbenchCtrl'
			})
			.state('workbench.resource', {
				url: '/{resourceId}',
				views: {
					'resource@workbench': {
						templateUrl: '/partials/workbench/resource.html',
						controller: 'WorkbenchResourceCtrl'
					},
					'details-bottom@workbench': {
						templateUrl: '/partials/workbench/details-bottom.html',
					},
					'details-right@workbench': {
						templateUrl: '/partials/workbench/details-right.html',
					}
				}
			})
			.state('mindmap', {
				url: '/mindmaps/{mapId}',
				templateUrl: '/partials/mindmap/index.html',
				controller: 'MindMapCtrl'
			})
			.state('mail', {
				url: '/mail',
				templateUrl: '/partials/mail/index.html',
				controller: 'MailCtrl'
			})
			.state('oauthCallback', {
				url: '/mail/oauthCallback',
				templateUrl: '/partials/mail/index.html',
				controller: 'MailCtrl',
				data: { callback: true }
			})
	}
])
.filter('joinBy', function () {
    return function (input,delimiter) {
        return (input || []).join(delimiter || ',');
    };
});
