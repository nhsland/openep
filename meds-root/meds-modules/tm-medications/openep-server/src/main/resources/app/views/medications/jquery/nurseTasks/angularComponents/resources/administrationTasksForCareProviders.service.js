var AdministrationTasksService = function($q, $timeout, AdministrationTasksResource)
{
  this.getAdministrationTasks = function(careProviderIds, patientIds)
  {
    var deferred = $q.defer();
    AdministrationTasksResource.get({
          careProviderIds: angular.isArray(careProviderIds) ? JSON.stringify(careProviderIds) : null,
          patientIds: angular.isArray(patientIds) ? JSON.stringify(patientIds) : null
        },
        function(response)
        {
          deferred.resolve(response);
        }, function(response)
        {
          deferred.reject(response);
        });
    return deferred.promise;
  };
};
AdministrationTasksService.$inject = ['$q', '$timeout', 'AdministrationTasksResource'];