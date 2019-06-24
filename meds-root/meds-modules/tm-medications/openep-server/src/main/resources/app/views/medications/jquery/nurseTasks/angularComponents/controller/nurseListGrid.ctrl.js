var NurseListGridCtrl = function($scope, $rootScope, $timeout, $q, $http, AdministrationTasksService,
                                 ActiveInitData, ActiveUpdateData, SaveFilterToContext, GetTranslation, viewProxy,
                                 NurseTaskService)
{
  /*************************************************************************************************************************/

  $scope.showNurseTaskGrid = function()
  {
    $scope.clearDataImpl();
    $scope.refreshNurseTaskGridGrid();
  };

  $scope.refreshNurseTaskGridGrid = function()
  {
    viewProxy.showLoaderMask();
    delete $scope.maskData.nurseTaskList['data']; // clears the no data message in the grid
    $rootScope.$emit('setAplicationTypeFilterOptions', []);

    AdministrationTasksService.getAdministrationTasks(
        $scope.isCareProviderFilterEnabled() ? ActiveInitData.careProviderIds : null,
        $scope.isCareProviderFilterEnabled() ? null : ActiveUpdateData.patientIds)
        .then(
            function(data)
            {
              var filterItemsObj = {},
                  filterItems = [];
              data.forEach(function(task)
              {
                if (angular.isObject(task) && angular.isObject(task.therapyDayDto) &&
                    angular.isObject(task.therapyDayDto.therapy) && angular.isArray(task.therapyDayDto.therapy.routes))
                {
                  task.therapyDayDto.therapy.routes.forEach(function markAllRoutes(route){
                    filterItemsObj[route.name.toUpperCase()] = 0;
                  });
                }
              });

              for (var key in filterItemsObj)
              {
                if (filterItemsObj.hasOwnProperty(key))
                {
                  filterItems.push(key);
                }
              }

          $rootScope.$emit('setAplicationTypeFilterOptions', filterItems);
          $scope.maskData.nurseTaskList.data = data;
          $scope.applyApplicationTypeFilter(ActiveInitData.applicationTypes);
        },
        function(data)
        {
          viewProxy.displayRequestErrorNotice(data);
        })
        .finally(function hideLoaderMask()
        {
          viewProxy.hideLoaderMask();
        });
  };

  /**
   * @param {Object} updateData
   */
  $scope.updateDataImpl = function(updateData)
  {
    if (!$scope.isCareProviderFilterEnabled())
    {
      ActiveUpdateData.patientIds = angular.isArray(updateData.patientIds) ? updateData.patientIds : [];
    }
    $scope.showNurseTaskGrid();
  };

  $scope.refreshDataImpl = function()
  {
    $scope.refreshNurseTaskGridGrid();
  };

  $scope.clearDataImpl = function()
  {
    $scope.maskData.nurseTaskList.origData = [];
    $scope.maskData.nurseTaskList.data = [];
  };

  $scope.isCareProviderFilterEnabled = function()
  {
    return ActiveInitData.careProviderFilterEnabled === true;
  };

  $scope.$on('refreshData', function()
  {
    $scope.refreshDataImpl();
  });

  $scope.$on('clearData', function()
  {
    $rootScope.$emit('clearData');
    $scope.clearDataImpl();
  });

  $scope.$on('updateData', function(event, updateData)
  {
    $scope.updateDataImpl(updateData);
  });

  $scope.init = function()
  {
    $scope.maskData = {
      nurseTaskList: {
        filter: [],
        origData: []
      }
    };

    $scope.applyApplicationTypeFilter = function(filterValue)
    {
      $scope.maskData.nurseTaskList.filter = filterValue;

      if ($scope.maskData.nurseTaskList.origData.length === 0)
      {
        $scope.maskData.nurseTaskList.origData = angular.copy($scope.maskData.nurseTaskList.data);
      }

      if ($scope.maskData.nurseTaskList.filter.length === 0)
      {
        $scope.maskData.nurseTaskList.data = $scope.maskData.nurseTaskList.origData;
      }
      else
      {
        var filteredTasks = [];
        $scope.maskData.nurseTaskList.origData.forEach(function(task)
        {
          var matchedRoute = false; // a therapy may contain multiple routes and we should only it to the new list once
          if (task.therapyDayDto && task.therapyDayDto.therapy && angular.isArray(task.therapyDayDto.therapy.routes))
          {
            task.therapyDayDto.therapy.routes.forEach(function checkRoute(route)
            {
              if ($scope.maskData.nurseTaskList.filter.contains(route.name.toUpperCase()))
              {
                matchedRoute = true;
              }
            });
          }
          if (matchedRoute)
          {
            filteredTasks.push(task);
          }
        });
        $scope.maskData.nurseTaskList.data = filteredTasks;
      }

    };

    //directives callbacks
    $scope.confirm = function(selectedIds, zeroSelectedCareproviders)
    {
      //ce ni izbran noben potem dobimo nazaj VSE idje
      //vendar jih ne shranimo v context - saj bi v tem primeru pri naslednjem odprtju imel vse izbrane
      if (zeroSelectedCareproviders === true)
      {
        SaveFilterToContext([], ActiveInitData.applicationTypes);
      }
      else
      {
        SaveFilterToContext(selectedIds, ActiveInitData.applicationTypes);
      }
      ActiveInitData.careProviderIds = selectedIds;
      $scope.clearDataImpl();
      $scope.refreshDataImpl();
    };

    $scope.cancel = function()
    {
    };

    $scope.careproviderSelectorDataProvider = NurseTaskService.careproviderSelectorDataProvider;

    $scope.getContextCareproviders = function()
    {
      return ActiveInitData.careProviderIds;
    };

  };
  $scope.init();

};
NurseListGridCtrl.$inject = ['$scope', '$rootScope', '$timeout', '$q', '$http', 'AdministrationTasksService',
  'ActiveInitData', 'ActiveUpdateData', 'SaveFilterToContext', 'GetTranslation', 'tm.angularjs.common.tmcBridge.ViewProxy',
  'NurseTaskService'];