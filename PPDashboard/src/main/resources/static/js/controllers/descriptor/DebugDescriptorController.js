var DebugDescriptorController = function($scope,$http,$stateParams,DataService,ngToast,$sce){
	
	var self = this;
	$scope.dwdp = null;
	$scope.crawledContentURL = null;
	$scope.currentListener = null;
	$scope.currentSignature = null;
	$scope.currentSignatureTaggedItem = null;
	$scope.matchedItems = null;


	var lastSelectedSignature = undefined;

	$http.get(dashboardServerURL+'jobExecutionHistory/'+$stateParams.executionId).then(function(response){
		$scope.executionHistory = response.data
		$scope.dwdp = response.data.dwdp;
		$scope.contentListeners = $scope.executionHistory.descriptorJob.descriptor.contentListeners;
		if($scope.dwdp != undefined && $scope.dwdp.crawledContent != undefined){
			$scope.crawledContentURL = dashboardServerURL+'descriptor/crawledContent/'+$scope.dwdp.crawledContent.stringId+'?output=embed';
		}
		
	});


	$scope.selectContentListener = function(listener){
		$scope.currentListener = listener;
		for(var i=0; i<listener.signatures.length;i++){
			var signature = listener.signatures[i];
			if(signature.signatureType=='CSS_SELECTOR'){
				$scope.currentSignature = signature;
				$scope.currentSignature.taggedItems = self.tagSignature(signature.value);
				var elems = self.selectIFrameElement(signature.value)
				if(lastSelectedSignature != undefined){
					var oldSelector = lastSelectedSignature.value.substring(2,lastSelectedSignature.value.length)
					var oldElemes = iframe.contentWindow.document.body.querySelectorAll(oldSelector);
					$(oldElemes).each(function(index,elem){
						$(elem).css('background-color','white')
					})
				}
				$(elems).each(function(index,elem){
					$(elem).css('background-color','green')
				})
				lastSelectedSignature = signature
			}
		}
	}

	this.selectIFrameElement = function(selector){
		var iframe = document.querySelectorAll('iframe')[0];
		var selector = selector.substring(2,selector.length)
		var elems = iframe.contentWindow.document.querySelectorAll(selector);
		iframe.contentWindow.scrollTo(0,elems[0].offsetTop)
		return elems;
	}

	this.tagSignature = function(signature){
		var signatureItems = signature.split('>');
		var cleanSignatureItems = new Array();
		var taggedItems = new Array()
		for(var i=0; i < signatureItems.length; i++){
			var signatureItem = signatureItems[i].trim();
			if(signatureItem != '' ){
				cleanSignatureItems.push(signatureItem);
			}
		}
		for(var i=0; i < cleanSignatureItems.length; i++){
			var signatureItem = cleanSignatureItems[i]
			var taggedItem = {};
			taggedItem.element = signatureItem.split('.')[0];
			taggedItem.value = signatureItem;
			if(i > 0){
				taggedItem.fullPath = taggedItems[i-1].fullPath+' > '+taggedItem.value;
			}else{
				taggedItem.fullPath = '> '+taggedItem.value;
			}

			taggedItems.push(taggedItem);	
		}
		return taggedItems;
	}

	$scope.selectSignatureTaggedItem = function(taggedItem){
		$scope.matchedItems = 0;
		$scope.currentSignatureTaggedItem = taggedItem;
		console.log(taggedItem.fullPath)
		$scope.matchedItems = self.selectIFrameElement(taggedItem.fullPath).length;
	}

	$scope.applyNewSignatureValue = function(){
		var newValue = "> ";
		for(var i=0 ; i< $scope.currentSignature.taggedItems.length -1 ; i++){
			newValue += $scope.currentSignature.taggedItems[i].value + ' > '
		}
		newValue += $scope.currentSignature.taggedItems[$scope.currentSignature.taggedItems.length-1].value
		$scope.currentSignature.taggedItems = self.tagSignature(newValue);
		$scope.currentSignature.value = newValue
	}
}