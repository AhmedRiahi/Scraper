var PPDashboard = angular.module('PPDashboard',['ngResource','ui.router','ui.bootstrap','ngToast','xeditable']);

// Dashboard views configuration
PPDashboard.config(function($stateProvider,$urlRouterProvider){

	$urlRouterProvider.otherwise('home');

	$stateProvider.state('admin',{
		url 		: '/admin',
		templateUrl	: 'views/admin/index.html'

	}).state('home',{
		url 		: '/home',
		templateUrl	: 'views/home/index.html',
		controller 	: HomeController

	});

	$stateProvider.state('portfolio',{
		abstract 	: true,
		url 		: '/portfolio',
		templateUrl : 'views/portfolio/index.html'
	}).state('portfolio.view',{
		url 		: '/view/:portfolioId',
		views:{
			'sidebar':{
				templateUrl:'views/portfolio/sidebar.html',
				controller 	: ListPortfolioController
			},
			'content':{
				templateUrl	: 'views/portfolio/view.html',
				controller 	: ViewPortfolioController
			}
		}
	}).state('portfolio.create',{
		url 		: '/create',
		views:{
			'sidebar':{
				templateUrl:'views/portfolio/sidebar.html',
				controller 	: ListPortfolioController
			},
			'content':{
				templateUrl	: 'views/portfolio/view.html',
				controller 	: ViewPortfolioController
			}
		}
	});


	$stateProvider.state('descriptor',{
		abstract 	: true,
		url 		: '/descriptor',
		templateUrl : 'views/descriptor/index.html'
	}).state('descriptor.edit',{
		url 		: '/edit/:descriptorId',
		views:{
			'sidebar':{
				templateUrl:'views/descriptor/sidebar.html',
				controller 	: ListDescriptorController
			},
			'content':{
				templateUrl	: 'views/descriptor/edit.html',
				controller 	: EditDescriptorController
			}
		}
	}).state('descriptor.create',{
		url 		: '/create',
		views:{
			'sidebar':{
				templateUrl:'views/descriptor/sidebar.html',
				controller 	: ListDescriptorController
			},
			'content':{
				templateUrl	: 'views/descriptor/edit.html',
				controller 	: EditDescriptorController
			}
		}
	})
	$stateProvider.state('debug',{
		url 		: '/debug/:executionId',
		templateUrl	: 'views/descriptor/debug.html',
		controller 	: DebugDescriptorController

	});



	$stateProvider.state('schemas',{
		abstract 	: true,
		url 		: '/schemas',
		templateUrl : 'views/schema/index.html'
	}).state('schemas.view',{
		url 		: '/view/:schemaName',
		views:{
			'sidebar':{
				templateUrl:'views/schema/sidebar.html',
				controller 	: SchemasController
			},
			'content':{
				templateUrl	: 'views/schema/view.html',
				controller 	: SchemasController
			}
		}
	}).state('schemas.create',{
		url 		: '/create',
		views:{
			'sidebar':{
				templateUrl:'views/schema/sidebar.html',
				controller 	: SchemasController
			},
			'content':{
				templateUrl	: 'views/schema/view.html',
				controller 	: SchemasController
			}
		}
	});


	$stateProvider.state('subscription',{
		url 		: '/subscription',
		templateUrl	: 'views/subscription/index.html',
		controller 	: SubscriptionController
	}).state('login',{
		url 		: '/login',
		templateUrl	: 'views/login/index.html'
	});


	$stateProvider.state('tools',{
		url 	: '/tools',
		templateUrl	: 'views/tools/index.html',
	})

});


// ************** Controllers Management **************

PPDashboard.factory('EntityWS',EntityWS)
PPDashboard.factory('DataService',DataService)

PPDashboard.controller('NavbarController',NavbarController);
PPDashboard.controller('ViewDescriptorController',ViewDescriptorController);
PPDashboard.controller('EditDescriptorController',EditDescriptorController);
PPDashboard.controller('SchemasController',SchemasController);
PPDashboard.controller('CreateSchemasController',CreateSchemasController);
PPDashboard.controller('SubscriptionController',SubscriptionController);



// ************** Services Management **************
var serverHost = window.location.hostname;
var dashboardServerURL 	= 'http://'+serverHost+':8002/';
var worldServerURL 	= 'http://'+serverHost+':9001/';
var subscriptionServerURL 	= 'http://'+serverHost+':9002/';
var Services 	= angular.module('Services',['ngResource']);


PPDashboard.filter('with', function($filter) {
  	return function(items, filter) {
  		if(filter == undefined || filter.trim() == "") return items;
  		var result = [];
       	for(var i=0; i < Object.keys(items).length; i++){
       		var element = Object.keys(items)[i];
       		if(element.toLowerCase().indexOf(filter.toLowerCase()) != -1){
       			result.push(items[element])
       		}
	    }
        return result;
    };
});


PPDashboard.run(function(editableOptions) {
  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});


PPDashboard.filter('joinable', [ function() {
    return function (object) {
        var array = [];
        angular.forEach(object, function (item) {
            if (item.joinable == true)
                array.push(item);
        });
        return array;
    };
}]);


PPDashboard.directive('prettyprint', ['$compile', function($compile) {
    return {
        restrict: 'C',
        require:"^ngController",
        link: function postLink(scope, element, attrs,parentController) {
        	var parser = new DOMParser();
			var xmlDoc = parser.parseFromString(attrs['content'],"text/xml");
			console.log(xmlDoc)
			var text = "";
			var toBeProcessedNodes = new Array();
			toBeProcessedNodes.push(xmlDoc);
			toBeProcessedNodes.push('finish');
			var level = 0;
			var nodeIndex=0;
			parentController.nodes = new Array()
			while(toBeProcessedNodes.length > 0){
				var currentNode = toBeProcessedNodes.shift();
				if(currentNode == 'finish'){
					level += 1;
					continue;
				}
				console.log(currentNode)
				console.log(currentNode.nodeType)
				switch(currentNode.nodeType){
					case 9 :
					case 1 :
						var margin = level*4;
						text += "</br><span style='margin-left:"+margin+"px' class='xml-span' ng-click='selectXmlDOM("+nodeIndex+")'>&lt;"+currentNode.nodeName+"&gt;</span>";
						for(var i=0; i< currentNode.childNodes.length; i++){
							toBeProcessedNodes.push(currentNode.childNodes[i]);
							toBeProcessedNodes.push('finish');
						}
						parentController.nodes.push(currentNode)
						nodeIndex++;
						break;
					case 3 :
						var textValue = currentNode.nodeValue.trim();
						textValue.replace('\\n','');
						if(textValue != ''){
							text += "<span>"+textValue+"</span>";
						}
				}
			}
			scope.selectXmlDOM = function(index){
				parentController.selectXmlDOM(index)
			}
			element.html(text);
			$compile(element.contents())(scope);
        }
    };
}]);
