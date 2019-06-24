var NurseTaskListRowDir = function ($filter)
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/nurseTasks/angularComponents/directive/nurseTaskList/nurseTaskListRow.template.html',
    controller: ['$scope', 'OpenPatientClickHandler', function ($scope, OpenPatientClickHandler)
    {
      $scope.callTimeHourFormatFilter = function (dateTime)
      {
        return $filter('tmTimeHourFormatFilter')(dateTime);
      };

      $scope.openPatientTimelineView = function (patientId)
      {
        OpenPatientClickHandler.call(this, patientId, 'TIMELINE');
      };

      /**
       * @return {string} value of the therapy's planned dose time in a safe manner, in case any of the objects
       * would be missing.
       */
      $scope.getPlannedDoseType = function()
      {
        return !!this.task.therapyDayDto && !!this.task.therapyDayDto.therapy ?
            this.task.therapyDayDto.therapy.doseType :
            undefined;
      };
    }],
    replace: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};