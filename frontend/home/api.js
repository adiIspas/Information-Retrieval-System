angular.module('kingRetrieval', [])
    .controller('inputQueryController', ['$scope', '$http', function($scope, $http) {
        $scope.getResults = function() {
            var query = $scope.query;

            if(query !== undefined && query !== "") {
                $http.get('http://localhost:8080/?query=' + query).then(function (response) {
                    $scope.results = response;
                });
            }
        };
    }]);