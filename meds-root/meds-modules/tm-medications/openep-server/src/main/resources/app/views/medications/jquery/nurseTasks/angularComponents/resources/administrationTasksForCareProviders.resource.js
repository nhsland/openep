var AdministrationTasksResource = function($resource)
{
  return $resource('medications/getAdministrationTasks',
      {},
      {
        get: {method: 'GET', isArray: true}
      }
  );
};
AdministrationTasksResource.$inject = ['$resource'];
