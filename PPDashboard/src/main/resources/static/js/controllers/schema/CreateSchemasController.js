var CreateSchemasController = function($scope,$http,$stateParams,DataService){
	
	$scope.currentSchema = {
		name : '',
	};
	$scope.currentSchema.properties = [];
	$scope.currentProperty  =null;
}