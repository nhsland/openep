(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsExternalEerPrescriptionDocument', tmMedsExternalEerPrescriptionDocument);

  function tmMedsExternalEerPrescriptionDocument()
  {
    return {
      restrict: 'E',
      scope: {
        document: '='
      },
      controller: 'tm.angular.medications.documentation.common.document.ExternalEerPrescriptionDocumentController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocument.template.html'
    };
  }
})();