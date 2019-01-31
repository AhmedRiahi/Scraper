var EditDescriptorController = function($scope,$http,$state,$stateParams,DataService,ngToast){
	
	$scope.currentDescriptor = {}
	console.log($state)
	DataService.get(dashboardServerURL,'descriptor',true).then(function(descriptors){
		$scope.descriptors = descriptors;
		$scope.currentDescriptor = DataService.find('descriptor','stringId',$stateParams.descriptorId);
		if($scope.currentDescriptor == undefined && $state.current.name != 'descriptor.create'){
			$scope.currentDescriptor = descriptors[0]
		}
	})

	DataService.get(worldServerURL,'schema',true).then(function(schemas){
		$scope.schemas = schemas;
	});

	$scope.schemaIndividuals = {};


	$scope.parseDescriptor = function(){
		var stringId = undefined;
		if($scope.currentDescriptor != undefined && $scope.currentDescriptor.stringId != undefined){
			stringId = $scope.currentDescriptor.stringId;
		}
		if($scope.currentDescriptor.type == 'JSON' || $scope.currentDescriptor.type == 'HTML'){
			$scope.currentDescriptor = JSON.parse($scope.descriptor);
		}else{
			if($scope.currentDescriptor.type == 'XML'){
				$scope.currentDescriptor = XML.parse($scope.descriptor);
			}
		}
		
		if(stringId != undefined){
			$scope.currentDescriptor.stringId = stringId
		}
		if($scope.currentDescriptor.semanticRelations == undefined){
			$scope.currentDescriptor.semanticRelations = []
		}

		if($scope.currentDescriptor.structureRelations == undefined){
			$scope.currentDescriptor.structureRelations = []
		}

		if($scope.currentDescriptor.descriptorSemanticMappings == undefined){
			$scope.currentDescriptor.descriptorSemanticMappings = []
		}
		console.log($scope.currentDescriptor)
	}

	$scope.deleteDescriptor = function(descriptor){
		$http.delete(dashboardServerURL+'descriptor/delete/'+descriptor.stringId).then(function(response){
			console.log(response)
		})
	}


	$scope.selectListener = function(listener){
		console.log(listener)
		if(listener.semanticProperties){
			$scope.getSchemaIndividuals(listener.semanticProperties.semanticName)
		}
		
		$scope.currentListener = listener;
		$($scope.currentDescriptor.semanticRelations).each(function(i,sr){
			if(sr.target.name == listener.name){
				$scope.currentListener.semanticReference = true
				if(sr.source.semanticProperties){
					var sourceSchema = $scope.schemas[sr.source.semanticProperties.semanticName]
					$scope.currentListener.sourceSchema = sourceSchema;
				}
			}
		})
		console.log($scope.currentListener);
	}

	$scope.isSchemaReferenceProperty = function(listener){
		for(var i=0;i<listener.sourceSchema.allProperties.length;i++){
			if(listener.sourceSchema.allProperties[i].name == $scope.currentDSM.clSemanticProperties[listener.name]){
				return listener.sourceSchema.allProperties[i].propertyType.class == 'reference'
			}
		}
	}

	$scope.getSchemaIndividuals = function(schemaName){
		$http.get(worldServerURL+'world/getSchemaIndividuals/'+schemaName).then(function(response){
			$scope.schemaIndividuals[schemaName] = response.data
			console.log($scope.schemaIndividuals)
		})
	}


	$scope.submitDescriptor = function(){
		console.log($scope.currentDescriptor)
		$scope.descriptor = JSON.stringify($scope.currentDescriptor)
		$http.post(dashboardServerURL+'/descriptor/add',$scope.currentDescriptor).then(function(response){
			ngToast.create('Descriptor Saved.');
		},function(response){
			ngToast.danger('Descriptor Not Saved! Please try again.');
		});
	}

	$scope.addListener = function(){
		var listener = {
			name : "Undefined"
		}
		$scope.currentDescriptor.contentListeners.push(listener)
	}

	$scope.deleteListener = function(){
		
		//Remove Listener semantic relations
		for(var i=0 ; i < $scope.currentDescriptor.semanticRelations.length ; i++){
			if($scope.currentDescriptor.semanticRelations[i].source.name == $scope.currentListener.name
				|| $scope.currentDescriptor.semanticRelations[i].target.name == $scope.currentListener.name){
				$scope.removeSemanticRelation($scope.currentDescriptor.semanticRelations[i])
			}	
		}

		//Remove Listener structure relations
		for(var i=0 ; i < $scope.currentDescriptor.structureRelations.length ; i++){
			if($scope.currentDescriptor.structureRelations[i].source.name == $scope.currentListener.name
				|| $scope.currentDescriptor.structureRelations[i].target.name == $scope.currentListener.name){
				$scope.removeStructureRelation($scope.currentDescriptor.structureRelations[i])
			}
		}

		var index = $scope.currentDescriptor.contentListeners.indexOf($scope.currentListener);
		$scope.currentDescriptor.contentListeners.splice(index,1);
		$scope.currentListener = undefined;
	}

	$scope.addSemanticRelation = function(){
		var semanticRelation = {
			'source' : {
				'name' : ''
			},

			'target' : {
				'name' : ''
			},
			'cardinalityType' : 'ONE_TO_ONE',
			'type' : 'compositionRelation'
		}
		
		$scope.currentDescriptor.semanticRelations.push(semanticRelation);
	}

	$scope.removeSemanticRelation = function(semanticRelation){
		var index = $scope.currentDescriptor.semanticRelations.indexOf(semanticRelation)
		$scope.currentDescriptor.semanticRelations.splice(index,1)
	}

	$scope.addStructureRelation = function(){
		var structureRelation = {
			'source' : {
				'name' : ''
			},

			'target' : {
				'name' : ''
			},
			'cardinalityType' : ''
		}
		$scope.currentDescriptor.structureRelations.push(structureRelation);
	}

	$scope.removeStructureRelation = function(structureRelation){
		var index = $scope.currentDescriptor.structureRelations.indexOf(structureRelation)
		$scope.currentDescriptor.structureRelations.splice(index,1);
	}

	$scope.addSignature = function(){
		var signature = {
			signatureType : "",
			value :"",
			type : "domSignatureModel"
		}
		if($scope.currentListener.signatures == undefined){
			$scope.currentListener.signatures = []
		}
		$scope.currentListener.signatures.push(signature)
	}

	$scope.testScript = function(){
		$http.post(dashboardServerURL+'descriptor/testScript',[$scope.currentListener.preProcessScript,$scope.scriptInput]).then(function(response){
			console.log(response.data)
			$scope.scriptResult = response.data.value
		})
	}

	$scope.selectDescriptor = function(descriptor){
		$scope.descriptorSchemas = []
		$scope.currentDescriptor = descriptor;
		var downloadedSchemas = []
		$http.get(serverURL+'dashboard/getPublishedIndividuals/'+descriptor.stringId).then(function(response){
			$scope.publishedIndividuals = response.data;
			$($scope.publishedIndividuals).each(function(i,indiv){
				if( downloadedSchemas[indiv.schemaName] == undefined){
					downloadedSchemas[indiv.schemaName] = {}
					$http.get(worldServerURL+'world/getSchema/'+indiv.schemaName).then(function(response){
						$scope.descriptorSchemas.push(response.data)
						console.log($scope.descriptorSchemas)
					});
				}
			})
		})
	}

	$scope.selectDSM = function(dsm){
		$scope.currentDSM = dsm;
	}

	$scope.addDSM = function(){
		var dsm = {
			name : 'Untitled',
			clSemanticProperties : {}
		}
		$scope.currentDescriptor.contentListeners.forEach(function(listener){
			dsm.clSemanticProperties[listener.name] = '';
		})
		$scope.currentDescriptor.descriptorSemanticMappings.push(dsm)
	}

	$scope.deleteDSM = function(){
		var index = $scope.currentDescriptor.descriptorSemanticMappings.indexOf($scope.currentDSM)
		$scope.currentDescriptor.descriptorSemanticMappings.splice(index,1);
		$scope.currentDSM = undefined
	}

	$scope.selectDSMListener = function(listener){
		console.log(listener)
		
		$scope.currentDSM.currentListener = listener;
		$($scope.currentDescriptor.semanticRelations).each(function(i,sr){
			if(sr.target.name == listener.name){
				$scope.currentDSM.currentListener.semanticReference = true
				var sourceSchema = $scope.schemas[$scope.currentDSM.clSemanticProperties[sr.source.name]]
				$scope.currentDSM.currentListener.sourceSchema = sourceSchema;
			}
		})
		if(listener.semanticReference){
			$scope.getSchemaIndividuals($scope.currentDSM.clSemanticProperties[listener.name]);
		}

		console.log($scope.currentDSM.currentListener);
	}


	this.selectXmlDOM = function(nodeIndex){
		$scope.currentNode = this.nodes[nodeIndex];
	}

	$scope.addXmlDescriptorListener = function(){
		var listener = {
			name : "Undefined"
		}
		$scope.currentDescriptor.contentListeners.push(listener);
		$scope.currentListener = listener;
	}
}