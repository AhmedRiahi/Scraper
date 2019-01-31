var SchemasController = function($scope,$http,$state,$stateParams,DataService){

	var self = this;

	if($state.current.name == 'schemas.create'){
		$scope.currentSchema = {
			name : '',
		};
		$scope.currentSchema.properties = [];
		$scope.currentProperty  =null;
		DataService.get(worldServerURL,'schema',true).then(function(schemas){
			$scope.schemas = schemas;
		});
	}else{
		$scope.currentSchema = null;
		$scope.currentIndividual = null;
		$scope.schemaIndividuals = []

		DataService.get(worldServerURL,'schema',true).then(function(schemas){
			$scope.schemas = schemas;
			var schema = DataService.find('schema','name',$stateParams.schemaName);
			if(schema != undefined){
				self.selectSchema(schema)
			}
			
		});
	}

	


	$scope.addSchema = function(){
		console.log($scope.currentSchema)
		$http.post(worldServerURL+'schema/addSchema',$scope.currentSchema).then(function(response){
			console.log(response)
		})
	}

	$scope.deleteSchema = function(){
		$http.post(worldServerURL+'schema/deleteSchema/'+$scope.currentSchema.stringId).then(function(response){
			console.log(response);
			$scope.currentIndividual = undefined
			$scope.currentProperty = undefined
			$scope.currentSchema = null;

		})
	}

	this.selectSchema = function(schema){
		console.log(schema)
		$scope.currentIndividual = undefined
		$scope.currentProperty = undefined
		$scope.currentSchema = schema;
		$http.get(worldServerURL+'world/getSchemaIndividuals/'+schema.name).then(function(response){
			$scope.schemaIndividuals = response.data
		})
	}

	$scope.selectProperty = function(property){
		$scope.currentProperty = property;
	}

	$scope.addProperty = function(){
		$scope.currentSchema.properties.push({name:"undefined"})
	}

	$scope.removeProperty = function(){
		var index = $scope.currentSchema.properties.indexOf($scope.currentProperty)
		$scope.currentSchema.properties.splice(index,1)
		$scope.currentProperty = undefined
	}

	$scope.createIndividual = function(){
		$scope.currentIndividual = {}
		$scope.currentIndividual.schemaName = $scope.currentSchema.name;
		$scope.currentIndividual.properties = [];
		$($scope.currentSchema.properties).each(function(i,property){
			console.log(property)
			var individualProperty = {
				name : property.name,
				value : ""
			};
			$scope.currentIndividual.properties.push(individualProperty)
			if(property.propertyType.class=='reference'){
				$http.get(worldServerURL+'world/getAllSchemaIndividuals/'+property.propertyType.value).then(function(response){
					individualProperty.individuals = response.data
				});
			}
		})
		
	}

	$scope.saveIndividual = function(){
		console.log($scope.currentIndividual)
		$http.post(worldServerURL+'world/addIndividual',$scope.currentIndividual).then(function(response){
			console.log(response)
		})
	}


	$scope.deleteIndividual = function(individual){
		console.log("deleteing idnividual");
		console.log(individual)
		$http.delete(worldServerURL+'world/deleteIndividual/'+individual.schemaName+'/'+individual.stringId)
	}


}