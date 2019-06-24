var ResupplyFormDir = function()
{
  return {
    restrict: 'E',
    replace: true,
    scope: {
      dataItem: '=item',
      formPosition: '=formPosition',
      showResupplyForm: '=showResupplyForm',
      formMode: '=formMode'
    },
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/resupplyForm/resupplyForm.template.html',
    controller: ['$scope', '$rootScope', '$http', '$timeout', 'TasksOperationService', 'TaskTypeEnum',
      'tm.angularjs.common.tmcBridge.ViewProxy',
      function($scope, $rootScope, $http, $timeout, TasksOperationService, TaskTypeEnum, viewProxy)
      {
        $scope.init = function()
        {
          $scope.data = {};
          $scope.maskData = {};
        };
        $scope.init();

        $scope.$watch('showResupplyForm', function(newValue)
        {
          if (newValue === true)
          {
            $scope.data = {
              stockType: $scope.dataItem.supplyTypeEnum,
              days: parseInt($scope.dataItem.supplyInDays),
              comment: ''
            };
          }
        });

        $scope.submitResuppyAction = function(resupplyForm)
        {
          if (!resupplyForm.$valid)
          {
            return;
          }

          var selfTaskId = $scope.dataItem.id;
          if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REMINDER)
          {
            var operation = $scope.formMode === 2 ? 'editSupplyReminderTask' : 'confirmSupplyReminderTask';
            TasksOperationService.supplyTaskOperation(operation, $scope.dataItem.id,
                $scope.dataItem.therapyDayDto.therapy.compositionUid, $scope.data.stockType, $scope.data.days,
                $scope.data.comment).then(
                function()
                {
                  $scope.showResupplyForm = false;
                  if (operation === 'editSupplyReminderTask')
                  {
                    $scope.dataItem.supplyTypeEnum = $scope.data.stockType;
                    $scope.dataItem.supplyInDays = $scope.data.days;
                  }
                  else
                  {
                    $scope.$emit('refreshData');
                  }
                },
                function(response)
                {
                  viewProxy.displayRequestErrorNotice(response);
                });
          }
          else if ($scope.dataItem.taskType === TaskTypeEnum.SUPPLY_REVIEW)
          {
            TasksOperationService.confirmSupplyReviewTask($scope.dataItem.patientDisplayDto.id,
                $scope.dataItem.id, true, $scope.data.stockType, $scope.data.days, $scope.data.comment,
                $scope.dataItem.therapyDayDto.therapy.compositionUid).then(
                function()
                {
                  //TODO: what to do here?
                  $scope.showResupplyForm = false;
                  $rootScope.$emit('removeSupplyTask', selfTaskId);
                },
                function(response)
                {
                  viewProxy.displayRequestErrorNotice(response);
                });
          }
        };
        $scope.closeResupplyForm = function()
        {
          $scope.showResupplyForm = false;
          $scope.data = {
            stockType: $scope.dataItem.supplyTypeEnum,
            days: parseInt($scope.dataItem.supplyInDays),
            comment: ''
          };
        };
        $rootScope.$on("documentClicked", function(event, target, escKey)
        {
          if (escKey)
          {
            $timeout(function resupplyFormSafeScopeApply()
            {
              $scope.closeResupplyForm();
            });
          }
        });
      }]
  };
};
