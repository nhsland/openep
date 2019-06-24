var AplicationTypeDir = function()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/nurseTasks/angularComponents/directive/aplicationType/aplicationType.template.html',
    controllerAs: 'aplicationType',
    scope: {
      clickCallback: '=clickCallback',
      getContextFilters: '=selected'
    },
    controller: ['$scope', '$rootScope', 'ActiveInitData', 'SaveFilterToContext',
      'tm.angularjs.common.tmcBridge.ViewProxy', function($scope, $rootScope, activeInitData, saveFilterToContext, viewProxy)
      {
        $rootScope.$on('closeDialogs', function()
        {
          $scope.opened = false;
        });
        $rootScope.$on('setAplicationTypeFilterOptions', function(event, filterItems)
        {
          $scope.data.options = [];

          for (var i = 0; i < filterItems.length; i++)
          {
            var contextIndex = activeInitData.applicationTypes.indexOf(filterItems[i]);
            $scope.data.options.push({
              key: filterItems[i],
              selected: contextIndex > -1
            });
          }
          _updateFilterTitle();
        });

        $scope.getSelectedFilter = function()
        {
          var selected = [];
          for (var i = 0; i < $scope.data.options.length; i++)
          {
            if ($scope.data.options[i].selected)
            {
              selected.push($scope.data.options[i].key);
            }
          }
          return selected;
        };
        $scope.filterClick = function(option)
        {
          option.selected = !option.selected;
          var selectedItems = $scope.getSelectedFilter();
          $scope.clickCallback(selectedItems);

          _updateFilterTitle();
          activeInitData.applicationTypes = selectedItems;
          saveFilterToContext(activeInitData.careProviderIds, activeInitData.applicationTypes);
        };

        $scope.showFilter = function()
        {
          $scope.opened = true;
        };
        $scope.hideFilter = function()
        {
          $scope.opened = false;
        };

        var init = function()
        {
          $scope.data = {
            options: []
          };
          _updateFilterTitle();
        }();

        function _updateFilterTitle()
        {
          $scope.data.title = _generateFilterTitle();
        }

        /**
         * @returns {String}
         * @private
         */
        function _generateFilterTitle()
        {
          var selected = $scope.getSelectedFilter();
          if (selected.length > 0)
          {
            if (selected.length <= 3)
            {
              return viewProxy.getDictionary('route.short') + ": " + selected.join(', ');
            }
            return viewProxy.getDictionary('route.short') + ": " + viewProxy.getDictionary('filtered');
          }
          return viewProxy.getDictionary('all.routes.short');
        }
      }],
    replace: true,
    link: function(scope, element)
    {
      scope.element = element;
      scope.opened = false;
    }
  };
};
