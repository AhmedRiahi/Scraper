var HomeController = function($scope,$http){

    $http.get(dashboardServerURL+'jobExecutionHistory/inError').then(function(response){
    	$scope.executionHistory = response.data
    })

    $http.get(dashboardServerURL+'jobExecutionHistory/active').then(function(response){
        	$scope.activeExecutionHistory = response.data
        })


	$scope.sendCleanRequest = function(){
		$http.get(dashboardServerURL+'dashboard/clean')
	}
}