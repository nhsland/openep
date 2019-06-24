(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsMentalHealthDocument', tmMedsMentalHealthDocument);

  function tmMedsMentalHealthDocument()
  {
    return {
      restrict: 'E',
      scope: {
        document: '='
      },
      controller: 'tm.angular.medications.documentation.common.document.MentalHealthDocumentController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/mentalHealth/TmMedsMentalHealthDocument.template.html'
    };
  }
})();