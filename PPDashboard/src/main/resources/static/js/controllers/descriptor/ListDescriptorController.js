var ListDescriptorController = function($scope,DataService,$stateParams){
	
	DataService.get(dashboardServerURL,'descriptor',true).then(function(descriptors){
		$scope.descriptors = descriptors;
		$scope.currentDescriptor = DataService.find('descriptor','stringId',$stateParams.descriptorId);
		console.log($scope.currentDescriptor)
	})
}