(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.indexColumn')
      .directive('tmMedsIndexColumnList', tmMedsIndexColumnList);

  function tmMedsIndexColumnList()
  {
    return {
      restrict: 'E',
      scope: false,
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/indexColumn/TmMedsIndexColumnList.template.html'
    };
  }
})();