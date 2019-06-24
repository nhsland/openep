var NurseTaskListDir = function ()
{
  var resizeTimer, nurseTaskListElement, headerElement, headerRowElement;
  var resizeTimerMillis = 500; // update the transition length in CSS if you change it, has to be smaller!
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/nurseTasks/angularComponents/directive/nurseTaskList/nurseTaskList.template.html',
    controller: ['$scope', '$timeout', function ($scope, $timeout)
    {
      nurseTaskListElement = angular.element(document.querySelector('.nurse-task-list'));
      // set the initial height of the list to match the parent, since the parent doesn't use flex
      nurseTaskListElement.height(angular.element(document.querySelector('.v-nurse-task-list-view')).height() - 45);

      function resizeHeaderRow(){
        var firstRowWidth = nurseTaskListElement.find(".content .row").first().width();
        var headerRowWidth = headerRowElement.width();
        if (firstRowWidth && firstRowWidth != headerRowWidth)
        {
          var diff = headerRowWidth - firstRowWidth;
          diff = isNaN(diff) || diff < 0 ? 0 : diff;
          headerElement.css('padding-right', diff + 'px');
        }
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      };

      if (nurseTaskListElement.length > 0) {
        headerElement = nurseTaskListElement.find('.header');
        headerRowElement = headerElement.find(".row").first();
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      }

      $scope.$on('$destroy', function(){
        $timeout.cancel(resizeTimer);
        headerRowElement = null;
        headerElement = null;
        nurseTaskListElement = null;
        resizeTimer = null;
      });
    }],
    replace: true,
    scope: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};