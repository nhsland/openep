(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.singleDocumentColumn')
      .directive('tmMedsSingleDocumentColumn', function()
      {
        return {
          restrict: 'E',
          scope: false,
          replace: true,
          templateUrl: '../ui/app/views/medications/jquery/documentation/singleDocumentColumn/TmMedsSingleDocumentColumn.template.html'
        };
      });
})();
