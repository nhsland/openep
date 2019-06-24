/**
 * Created by matejp on 26.8.2015.
 */

var ResupplyTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistResupplyTasks',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

ResupplyTasksResource.$inject = ['$resource'];

var DispenseTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistDispenseMedicationTasks',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

DispenseTasksResource.$inject = ['$resource'];

var ReviewTasksResource = function ($resource)
{
  return $resource('medications/getPharmacistReviewTasks',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};

ReviewTasksResource.$inject = ['$resource'];

var PerfusionSyringeTasksResource = function($resource)
{
  return $resource('medications/findPerfusionSyringePreparationRequests',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};
PerfusionSyringeTasksResource.$inject = ['$resource'];

var FinishedPerfusionSyringeTasksResource = function($resource)
{
  return $resource('medications/findFinishedPerfusionSyringePreparationRequests',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};
FinishedPerfusionSyringeTasksResource.$inject = ['$resource'];
