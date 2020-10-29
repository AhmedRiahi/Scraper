var ViewPortfolioController = function($scope,$http,$stateParams,DataService,ngToast){


	DataService.get(dashboardServerURL,'descriptor',true).then(function(descriptors){
		$scope.descriptors = descriptors;
	})

	DataService.get(dashboardServerURL,'portfolio',true).then(function(portfolios){
		if($stateParams.portfolioId != undefined){
			var portfolio = DataService.find('portfolio','stringId',$stateParams.portfolioId);
			if(portfolio == undefined){
				portfolio = portfolios[0]
			}
			$scope.currentPortfolio = portfolio;
			$http.get(dashboardServerURL+'jobExecutionHistory/portfolio/'+$scope.currentPortfolio.stringId).then(function(response){
				$scope.executionHistory = response.data
			})
		}else{
			$scope.currentPortfolio = {
				name : 'Untitled (click to edit)'
			}
		}
		if($scope.currentPortfolio.descriptors == undefined) $scope.currentPortfolio.descriptors = []
		if($scope.currentPortfolio.joins == undefined) $scope.currentPortfolio.joins = []
		if($scope.currentPortfolio.jobs == undefined) $scope.currentPortfolio.jobs = []
	});

	$scope.descriptorAlreadyAllocated = function(descriptor){
		var found = false
		angular.forEach($scope.currentPortfolio.descriptors,function(tmpDescriptor){
			if(descriptor.stringId == tmpDescriptor.stringId){
				found =  true;
			}
		});
		return found;
	}

	$scope.allocateDescriptor = function(){
		if($scope.descriptorAlreadyAllocated($scope.selectedDescriptor)){
			alert("Descriptor already allocated")
			return;
		}
		$scope.currentPortfolio.descriptors.push($scope.selectedDescriptor);
	}

	$scope.unallocateDescriptor = function(){
		var index = $scope.currentPortfolio.descriptors.indexOf($scope.selectedAllocatedDescriptor);
		$scope.currentPortfolio.descriptors.splice(index,1);
	}

	$scope.addJoin = function(){
		var join = {
			name : 'Untitled'
		}
		$scope.currentPortfolio.joins.push(join)
		$scope.selectedJoin = join;
	}

	$scope.addJoinProperties = function(){
		if($scope.selectedJoin.joinProperties == null) $scope.selectedJoin.joinProperties = new Array();
		$scope.selectedJoin.joinProperties.push({sourceContentListenerModel:'',targetContentListenerModel:''})
	}

	$scope.removeJoin = function(){
		var index = $scope.currentPortfolio.joins.indexOf($scope.selectedJoin);
		$scope.currentPortfolio.joins.splice(index,1);
		$scope.selectedJoin = null;
	}

	$scope.addJob = function(){
		var job = {
			name : 'Untitled'
		}
		$scope.currentPortfolio.jobs.push(job)
		$scope.selectedJob = job;
	}

	$scope.removeJob = function(){
		var index = $scope.currentPortfolio.jobs.indexOf($scope.selectedJob);
		$scope.currentPortfolio.jobs.splice(index,1);
		$scope.selectedJob = null;
	}

	$scope.submitPortfolio = function(){
		$http.post(dashboardServerURL+'portfolio',$scope.currentPortfolio).then(function(response){
			console.log(response.data);
			ngToast.create('Portfolio Saved.');
		},function(response){
			ngToast.danger('Portfolio Not Saved! Please try again.');
		})
	}

	$scope.deletePortfolio = function(){
		$http.delete(dashboardServerURL+'portfolio/'+$scope.currentPortfolio.stringId).then(function(response){
			ngToast.create('Portfolio Deleted.');
		},function(response){
			ngToast.danger('Portfolio Not Deleted! Please try again.');
		})
	}


	$scope.selectExecutionHistory = function(exec){
		$scope.selectedExecution = exec;
		$http.get(dashboardServerURL+'dwdp/'+exec.dwdpStringId+'/individuals').then(function(response){
			$scope.selectedExecution.individuals = response.data;
		})
	}

	$scope.refreshExecutionHistory = function(){
		$http.get(dashboardServerURL+'jobExecutionHistory/portfolio/'+$scope.currentPortfolio.stringId).then(function(response){
			$scope.executionHistory = response.data
		})
	}

	$scope.runJob = function(job){
		$http.get(dashboardServerURL+'portfolio/launchJob/'+$scope.currentPortfolio.stringId+'/'+job.name).then(function(response){
			ngToast.create('Job Launched.');
		},function(response){
			ngToast.danger('Job Not Launched!');
		})
	}

	$scope.addHttpBodyParam = function(){
		if($scope.selectedJob.crawlingParams == null)$scope.selectedJob.crawlingParams = {}
		if($scope.selectedJob.crawlingParams.httpParams == null)$scope.selectedJob.crawlingParams.httpParams = new Array();
		$scope.selectedJob.crawlingParams.httpParams.push({key:'key',value:'value'});
	}

	$scope.testURLScript = function(){
		alert(eval($scope.selectedJob.crawlingParams.urlGeneratorScript));
	}
}
