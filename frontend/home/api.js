angular.module('myApp', [])
    .controller('myCtrl', ['$scope', '$http', function($scope, $http) {
        $scope.myFunc = function() {
            $http.get('http://localhost:8080/?query=data').then(function(response){
                $scope.data = response;
            });
        };
    }]);