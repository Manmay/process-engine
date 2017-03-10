app.controller("RegistrationReviewCtrl", function($scope, $routeParams, $http, $location){
	
	$scope.task;
	
	$scope.init = function(){
		console.log($routeParams.taskId);
		$http({
			url: '/api/tasks/' + $routeParams.taskId + '?userId=' + $routeParams['userId'],
			method: "GET",
			headers: {
				'Accept': 'application/json'
			}
		}).success(function(data, status){
			console.log('success');
			$scope.task = data;
		}).error(function(error){
			console.log("error");
		});
	};
	
	$scope.claim = function(){
		$http({
			url: '/api/tasks/' + $routeParams.taskId + '/claim?userId=' + $routeParams['userId'],
			method: "GET"
		}).success(function(data, status){
			console.log("success!!!");
			$scope.task.assignee = $routeParams['userId'];
		}).error(function(error){
			console.log("error");
		});
	};
	
	$scope.submit = function(){
		$http({
			url: '/api/processes/registration/data?taskId=' + $routeParams.taskId,
			method: "POST",
			headers: {
				'Content-Type': 'application/json'
			},
			data: $scope.task.data
		}).success(function(data, status){
			console.log("success!!!");
			$location.path("/tasks");
		}).error(function(error){
			console.log("error");
		});
	};
	
	
	
});

app.controller("RegistrationVerifyCtrl", function($scope, $routeParams, $http, $location){
	
	$scope.task;
	
	$scope.init = function(){
		console.log($routeParams.taskId);
		$http({
			url: '/api/processes/registration/data?taskId=' + $routeParams.taskId,
			method: "GET",
			headers: {
				'Accept': 'application/json'
			}
		}).success(function(data, status){
			console.log('success');
			$scope.task = data;
		}).error(function(error){
			console.log("error");
		});
	};
	
	$scope.submit = function(){
		$http({
			url: '/api/processes/registration/data?taskId=' + $routeParams.taskId,
			method: "POST",
			headers: {
				'Content-Type': 'application/json'
			},
			data: $scope.data
		}).success(function(data, status){
			console.log("success!!!");
			$location.path("/tasks");
		}).error(function(error){
			console.log("error");
		});
	};
	
});