var ViewDescriptorController = function($scope,$http,$stateParams,DataService){
	console.log($stateParams)
	var self = this;
	$scope.currentListener = null;
	
	$scope.currentDescriptor = undefined;
	$scope.descriptorSchemas = []
	$scope.publishedIndividuals = undefined

	DataService.get(dashboardServerURL,'descriptor',true).then(function(descriptors){
		var descriptor = DataService.find('descriptor','stringId',$stateParams.descriptorId);
		if(descriptor == undefined){
			$scope.currentDescriptor = descriptors[0]
		}else{
			$scope.currentDescriptor = descriptor;
		}
		console.log($scope.currentDescriptor);
		self.getDescriptorIndividuals();
		self.getDescriptorExecutionHistory();
	})

	this.getDescriptorIndividuals = function(){
		var processedSchemas = []
		$http.get(dashboardServerURL+'dashboard/getPublishedIndividuals/'+$scope.currentDescriptor.stringId).then(function(response){
			$scope.publishedIndividuals = response.data;
			$scope.descriptorSchemas = []
			$($scope.publishedIndividuals).each(function(i,indiv){
				if( processedSchemas[indiv.schemaName] == undefined){
					processedSchemas[indiv.schemaName] = true
					DataService.get(worldServerURL,'schema',true).then(function(schemas){
						var schema = DataService.find('schema','name',indiv.schemaName)
						$scope.descriptorSchemas.push(schema)
						console.log($scope.descriptorSchemas)
					});
				}
			})
		})
	}

	$scope.refreshDescriptorData = function(){
		self.getDescriptorIndividuals();
		self.getDescriptorExecutionHistory();

	}

	this.getDescriptorExecutionHistory = function(){
		$http.get(dashboardServerURL+'descriptor/executionHistory/'+$scope.currentDescriptor.stringId).then(function(response){
			$scope.executionsHistory = response.data;
		})
	}

	
	$scope.createDescriptor = function(){
		$scope.currentDescriptor = {}
		$scope.currentDescriptor.name = "Untitled"
		$scope.descriptors.push($scope.currentDescriptor)
	}

	$scope.removeDescriptorIndividuals = function(){
		$http.delete(dashboardServerURL+'dashboard/deleteDescriptorIndividuals/'+$scope.currentDescriptor.stringId);
	}


	$scope.processDescriptor = function(descriptorId){
		$http.get(dashboardServerURL+'descriptor/processDescriptor/'+descriptorId).then(function(response){
			console.log(response)
		})
	}

	$scope.flagAsChecked = function(){
		$http.get(dashboardServerURL+'descriptor/flagAsChecked/'+$scope.currentDescriptor.stringId)
	}


	
}