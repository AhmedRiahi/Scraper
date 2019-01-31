var EntityWS = function($http){
	var self = this;

	self.getAll = function(url,entityName){
		return $http.get(url+entityName+'/getAll');
	}

	self.get = function(url,entityName){
		return $http.get(url+entityName+'/get');
	}

	return self;
}