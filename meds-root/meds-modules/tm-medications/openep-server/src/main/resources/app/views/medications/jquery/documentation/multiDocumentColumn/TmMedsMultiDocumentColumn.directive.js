(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.multiDocumentColumn')
      .directive('tmMedsMultiDocumentColumn', tmMedsMultiDocumentColumn);

  function tmMedsMultiDocumentColumn()
  {
    return {
      restrict: 'E',
      scope: false,
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/multiDocumentColumn/TmMedsMultiDocumentColumn.template.html'
    };
  }
})();