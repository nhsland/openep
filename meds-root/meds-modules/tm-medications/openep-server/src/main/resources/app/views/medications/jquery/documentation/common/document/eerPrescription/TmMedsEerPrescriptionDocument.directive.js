(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsEerPrescriptionDocument', tmMedsEerPrescriptionDocument);

  function tmMedsEerPrescriptionDocument()
  {
    return {
      restrict: 'E',
      scope: {
        document: '=',
        readOnly: '='
      },
      controller: 'tm.angular.medications.documentation.common.document.EerPrescriptionDocumentController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document',
        _readOnly: '=readOnly'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/eerPrescription/TmMedsEerPrescriptionDocument.template.html'
    };
  }
})();