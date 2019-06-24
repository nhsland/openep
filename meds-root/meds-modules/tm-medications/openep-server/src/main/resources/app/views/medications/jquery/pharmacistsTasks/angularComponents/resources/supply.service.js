var TasksService = function($q, ResupplyTasksResource, DispenseTasksResource, ReviewTasksResource,
                            PerfusionSyringeTasksResource, FinishedPerfusionSyringeTasksResource)
{
  this.getSupplyTasks = function(careProviderIds, patientIds, closedTasksOnly, includeUnverifiedDispenseTasks)
  {
    var deferred = $q.defer();
    ResupplyTasksResource.get({
          careProviderIds: stringifyArrayOrNull(careProviderIds),
          patientIds: stringifyArrayOrNull(patientIds),
          closedTasksOnly: closedTasksOnly,
          includeUnverifiedDispenseTasks: includeUnverifiedDispenseTasks,
          taskTypes: JSON.stringify(['SUPPLY_REMINDER', 'SUPPLY_REVIEW'])
        }, //todo supply implement filter at frontend
        function(response)
        {
          deferred.resolve(response);
        }, function(response)
        {
          deferred.reject(response);
        });
    return deferred.promise;
  };

  this.getDispenseTasks = function(careProviderIds, patientIds, closedTasksOnly, includeUnverifiedDispenseTasks)
  {
    var deferred = $q.defer();
    DispenseTasksResource.get({
          careProviderIds: stringifyArrayOrNull(careProviderIds),
          patientIds: stringifyArrayOrNull(patientIds),
          closedTasksOnly: closedTasksOnly,
          includeUnverifiedDispenseTasks: includeUnverifiedDispenseTasks
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

  this.getReviewTasks = function(careProviderIds, patientIds)
  {
    var deferred = $q.defer();
    ReviewTasksResource.get({
          careProviderIds: stringifyArrayOrNull(careProviderIds),
          patientIds: stringifyArrayOrNull(patientIds)
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

  this.getPerfusionSyringeTasks = function(careProviderIds, patientIds, taskTypes)
  {
    var deferred = $q.defer();
    PerfusionSyringeTasksResource.get({
          careProviderIds: stringifyArrayOrNull(careProviderIds),
          patientIds: stringifyArrayOrNull(patientIds),
          taskTypes: JSON.stringify(taskTypes)
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

  this.getFinishedPerfusionSyringeTasks = function(careProviderIds, patientIds, date)
  {
    var deferred = $q.defer();
    FinishedPerfusionSyringeTasksResource.get({
          careProviderIds: stringifyArrayOrNull(careProviderIds),
          patientIds: stringifyArrayOrNull(patientIds),
          date: JSON.stringify(date)
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

  /**
   * @param {Array<*>} array
   * @returns {String|null}
   */
  function stringifyArrayOrNull(array)
  {
    return angular.isArray(array) ? JSON.stringify(array) : null;
  }
};
TasksService.$inject = ['$q', 'ResupplyTasksResource', 'DispenseTasksResource', 'ReviewTasksResource',
  'PerfusionSyringeTasksResource', 'FinishedPerfusionSyringeTasksResource'];