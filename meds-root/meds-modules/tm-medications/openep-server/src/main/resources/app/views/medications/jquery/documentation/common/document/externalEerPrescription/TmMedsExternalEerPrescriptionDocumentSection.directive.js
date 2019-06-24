(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsExternalEerPrescriptionDocumentSection', tmMedsExternalEerPrescriptionDocumentSection);

  function tmMedsExternalEerPrescriptionDocumentSection()
  {
    return {
      restrict: 'E',
      scope: {
        sectionTitle: '@',
        sectionTherapies: '=',
        prescriptionDate: '='
        
      },
      controller: 'tm.angular.medications.documentation.common.document.ExternalEerPrescriptionDocumentSectionController',
      controllerAs: 'vm',
      bindToController: {
        _sectionTitle: '@sectionTitle',
        _sectionTherapies: '=sectionTherapies',
        _prescriptionDate: '=prescriptionDate'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/externalEerPrescription/TmMedsExternalEerPrescriptionDocumentSection.template.html'
    };
  }
})();