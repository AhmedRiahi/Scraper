var SubscriptionController = function($scope,$uibModal,$http){


	$scope.schema = {}
	$scope.individual = {}
	$scope.schemaIndividuals = {}
	$scope.subscriptions = null;

	$http.get(subscriptionServerURL+'subscription/all').then(function(response){
		$scope.subscriptions = response.data;
		console.log($scope.subscriptions)
	})


	$http.get(worldServerURL+'schema/getAll').then(function(response){
		$scope.schemas = response.data;
		console.log($scope.schemas)
	});

	$scope.selectSubscriptionSchema = function(){
		console.log($scope.schema)
		$($scope.schema.allProperties).each(function(i,property){
			if(property.propertyType.class=='reference'){
				$scope.getSchemaIndividuals(property.propertyType.value)
			}
		})
	}

	$scope.getSchemaIndividuals = function(schemaName){
		$http.get(worldServerURL+'world/getSchemaIndividuals/'+schemaName).then(function(response){
			$scope.schemaIndividuals[schemaName] = response.data
			console.log($scope.schemaIndividuals)
		})
	}

	$scope.createSubscription = function(){
		var schemaSubscription = {};
		schemaSubscription.schema = $scope.schema;
		schemaSubscription.subscriptionFilter = $scope.individual;
		console.log(schemaSubscription)
		$http.post(subscriptionServerURL+'subscription/create',schemaSubscription).then(function(response){
			console.log(response)
		})
	}
}