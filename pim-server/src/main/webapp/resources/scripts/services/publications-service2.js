angular.module('pimServices', []).
	factory('Folder', function($resource) {
		return $resource('/publications/folders/:name', {name : '@_name'}, { 
			update : {
				method : 'PUT' // this method issues a PUT request
		    }
		});
	}).
	factory('Publication', function($resource) {
		return $resource('/publications/folders/:folder/files/:id', {folder : '@_folderId', id : '@_id'}, { 
			update : {
				method : 'PUT' // this method issues a PUT request
		    }
		});
	}).
	service('publicationService', ['$state', '$http', '$q', 'Folder', 'Publication', function($state, $http, $q, Folder, Publication) {
		var me = this;
//		me.folders = [
//   		    { name : 'To_See', title : 'To See' },
//   		    { name : 'PIM', title : 'PIM', items : [
//   		        { id : 1, title : 'Stuff I’ve Seen: A System for Personal Information Retrieval and Re-Use', year : 2003 },
//   		        {
//   		        	id : 2,
//   		        	title : 'iMecho: An Associative Memory Based Desktop Search System', 
//   		        	year : 2009,
//   		        	creationDate : '03.02.2014 15:37',
//   		        	size : 893,
//   		        	authors : [
//   		        	    { name : 'W Wu', href : '#'},
//   		        	    { name : 'Jidong Chen' }
//   				    ],
//   		        	related : [
//   		        	    { title : 'Building a semantic representation for personal information', href : '#' },
//   		        	    { title : 'Integrating memory context into personal information re-finding', href : '#' }
//   		        	]
//   		        },
//   		        { id : 3, title : 'Desktop Gateway. Semantic Desktop Integration with Cloud Services', year : 2013 },
//   		        { id : 4, title : 'OurSpaces – Design and Deployment of a Semantic Virtual Research Environment', year : 2012 },
//   		        { id : 5, title : 'IRIS. Integrate. Relate. Infer. Share', year : 2005 },
//   		        { id : 6, title : 'Interlinking Personal Semantic Data on the Semantic Desktop and the Web of Data', year : 2012 },
//   		        { 
//   		        	id : 7, 
//   		        	title : 'Semantic desktop 2.0: The Gnowsis experience', year : 2004,
//   		        	creationDate : '21.02.2014 21:37',
//   		        	size : 1793,
//   		        	authors : [
//   		        	    { name : 'Leo Sauermann', href : '#'},
//   		        	    { name : 'Gunnar Aastrand Grimnes' }
//   				    ],
//   				    relatedResources: [
//   				        { id : 11 },
//   				        { id : 10 }
//   				    ],
//   		        	relatedPublications : [
//   		        	    { title : 'IRIS: Integrate, Relate. Infer. Share', href : '#' },
//   		        	    { title : 'Overview and Outlook on the Semantic Desktop', href : '#' }
////		   		        	    { title : 'The NEPOMUK project-on the way to the social semantic desktop', href : '#' }
//   		        	],
//   		        	tags : [
//   		        	     { name : 'PKM', user : true },
//   		        	     { name : 'Semantic Desktop', user : false },
//   		        	     { name : 'Memex', user : false },
//   		        	     { name : 'Personal Information Management', user : false },
//   		        	     { name : 'RDF', user : false }
//   		        	]
//   		        },
//   		        { id : 8, title : 'Feldspar. A System for Finding Information by Association', year : 2008 },
//   		        { id : 9, title : 'iMemex. A Unified Approach to Personal Information Management', year : 2006 },
//   		        { id : 10, title : 'MyLifeBits. Fulfilling the Memex Vision', year : 2002 },
//   		        { id : 11, title : 'Haystack’s User Experience for Interacting with Semistructured Information', year : 2003 },
//   		        { id : 12, title : 'DeepaMehta — A Semantic Desktop', year : 2005 },
//   		        { id : 13, title : 'SemEx. A Platform for Personal Information Management and Integration', year : 2005 },
//   		        { id : 14, title : 'Building a Semantic Representation for Personal Information (LiFiDeA)', year : 2010 },
//   		        { id : 15, title : 'The NEPOMUK Project - On the way to the Social Semantic Desktop', year : 2007 },
//   		        { id : 16, title : 'Personal Knowledge Management with the Social Semantic Desktop'},
//   		        { id : 17, title : 'Why Personal Information Management (PIM) Technologies Are Not Widespread', year : 2009 },
//   		        { id : 18, title : 'Understanding the Evolution of Users’ Personal Information Management Practices', year : 2007 }
//   		    ]},
//   		    { name : 'Knowledge_Management', title : 'Knowledge Management'},
//   		    { name : 'Obzory', title : 'Обзоры'}
//   		];
		
		me.getFolders = function(success) {
			if (!me.folders) {
				me.folders = Folder.query();
			}
			me.folders.$promise.then(success);
		};
		
		me.getItems = function(folder, success) {
			if (folder.items) {
				return folder.items;
			}
			folder.items = Publication.query({folder : folder.id});
			folder.items.$promise.then(success);
		};
		
		me.findFolder = function(folderId, success) {
			me.getFolders(function(folders) {
				folder = $.grep(folders, function(folder) {
					return folder.id == folderId;
				})[0];
				success(folder);
			});
		};
		
		me.findItem = function(itemId, folderId, success) {
			me.findFolder(folderId, function(folder) {
				me.getItems(folder, function(items) {
					var foundedItem = $.grep(items, function(item) {
						return item.id == itemId;
					})[0];
					success(foundedItem);
				});
			});
		};
		
		me.selectedItem = null;
		me.selectedFolder = null;
		
		me.navigateToItem = function(itemId) {
			me.findItem(itemId, function(item) {
				$state.go('publications.folder.item', {folderId : item.folderId, itemId : itemId});
			});
		};
		
		me.navigateToFolder = function(folderId) {
			$state.go('publications.folder', {folderId : folderId});
		};
		
		me.getSelectedItem = function() {
			return me.selectedItem;
		};
		
		me.getSelectedFolder = function() {
			return me.selectedFolder;
		};
		
		me.stateChanged = function(success) {
			var selectedItemDefer = $q.defer();
			me.selectedItem = selectedItemDefer.promise;
			me.findItem($state.params.itemId, $state.params.folderId, function(item) {
				selectedItemDefer.resolve(item);
			});
			
			var selectedFolderDefer = $q.defer();
			me.selectedFolder = selectedFolderDefer.promise;
			me.findFolder($state.params.folderId, function(folder) {
				selectedFolderDefer.resolve(folder);
			});
		};
	}]);