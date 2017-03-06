var app = angular.module("app", ["ngRoute"]);

app.config(function($routeProvider) {
    $routeProvider
    	.when("/home", {
	        templateUrl : "template/home.html"
	    })
	    .when("/inbox", {
	        templateUrl : "template/inbox.html",
	        controller: "InboxCtrl"
	    })
	    .when("/processes/:processName", {
	    	templateUrl: "template/task.html",
	    	controller: "TaskCtrl"
	    })
	    .otherwise("/home");
});

app.controller('TaskCtrl', function($scope, $routeParams, $http) {
	console.log('[START] TaskCtrl');
	
	$scope.processName;
	$scope.taskId;
	$scope.formData;

	$scope.init = function(){
		$scope.processName = $routeParams.processName;
		$scope.taskId = $routeParams['taskId'];
		$scope.getData();
	};

	$scope.getData = function(){
		$http({
			url: '/processes/' + $scope.processName + "/data" + ($scope.taskId == undefined ? ('') : ('?taskId=' + $scope.taskId)),
			method: "GET",
			headers: {
				'Accept': 'application/json'
			}
		}).success(function(data, status){
			console.log('success');
			$scope.formData = data;
			$scope.getForm();
		}).error(function(error){
			console.log("error");
		});
	};
	
	$scope.getForm = function(){
		$scope.templateUrl = 'processes/' + $scope.processName + "/form" + ($scope.taskId == undefined ? ('') : ('?taskId=' + $scope.taskId));
	};
	
	$scope.submitForm = function(){
		$http({
			url: "/processes/" + $scope.processName + "/form" + ($scope.taskId == undefined ? ('') : ('?taskId=' + $scope.taskId)),
			method: "POST",
			headers: {
				'Content-Type': 'application/json'
			},
			data: $scope.formData
		}).success(function(data, status){
			console.log("success");
		}).error(function(error){
			console.log("error");
		});
	};

});


app.controller('InboxCtrl', function($scope, $http) {
	
	 $scope.init = function(){
		 $http({
				url: '/api/tasks',
				method: "GET",
				headers: {
					'Accept': 'application/json'
				}
			}).success(function(data, status){
				console.log('success');
				$scope.taskList = data;
			}).error(function(error){
				console.log("error");
			});
	 };	 
	 
});

