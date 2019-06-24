var SyringesListDir = function ()
{
  var resizeTimer, syringeListElement, headerElement, headerRowElement;
  var resizeTimerMillis = 500; // update the transition length in CSS if you change it, has to be smaller!
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/syringesList/syringesList.template.html',
    controller: ['$scope', '$timeout', 'OpenPatientClickHandler', function ($scope, $timeout, OpenPatientClickHandler)
    {
      syringeListElement = angular.element(document.querySelector('.perfusion-syringes-list'));
      // set the initial height of the list to match the parent, since the parent doesn't use flex
      syringeListElement.height(angular.element(document.querySelector('.v-pharmacist-tasks-view')).height() - 45);

      function resizeHeaderRow(){
        var firstRowWidth = syringeListElement.find(".content .row").first().width();
        var headerRowWidth = headerRowElement.width();
        if (firstRowWidth && firstRowWidth != headerRowWidth)
        {
          var diff = headerRowWidth - firstRowWidth;
          diff = isNaN(diff) || diff < 0 ? 0 : diff;
          headerElement.css('padding-right', diff + 'px');
        }
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      };

      if (syringeListElement.length > 0) {
        headerElement = syringeListElement.find('.header');
        headerRowElement = headerElement.find(".row").first();
        resizeTimer = $timeout(resizeHeaderRow, resizeTimerMillis);
      }

      function openPatient(id) {
        OpenPatientClickHandler(id, 'TIMELINE');
      };

      $scope.openPatient = openPatient;
      $scope.$on('$destroy', function(){
        $timeout.cancel(resizeTimer);
        headerRowElement = null;
        headerElement = null;
        syringeListElement = null;
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