var NavbarController = function($scope,$http,$state,$location){

	$scope.user = undefined;

	$http.get(dashboardServerURL+'dashboard/user').then(function(response){
		$scope.user = response.data
	})

	$scope.isActive = function(menuLocation){
		return $location.path().indexOf(menuLocation) != -1;
	}

	$scope.logout = function(){
		$http.defaults.headers.common['Authorization'] = ''
		$http.get(dashboardServerURL+'logout')
		$state.go('login')
	}
}