var ListPortfolioController = function($scope,DataService,$stateParams){
	
	DataService.get(dashboardServerURL,'portfolio',true).then(function(portfolios){
		$scope.portfolios = portfolios;
		$scope.currentPortfolio = DataService.find('portfolio','stringId',$stateParams.portfolioId);
		console.log($scope.currentPortfolio)
	})
}