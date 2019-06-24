(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsEerPrescriptionDocumentSection', tmMedsEerPrescriptionDocumentSection);
  
  function tmMedsEerPrescriptionDocumentSection()
  {
    return {
      restrict: 'E',
      scope: {
        sectionTherapies: '=',
        sectionTitle: '@',
        cancelTherapyClickHandler: '&',
        removeTherapyClickHandler: '&',
        readOnly: '='
      },
      controller: 'tm.angular.medications.documentation.common.document.EerPrescriptionDocumentSectionController',
      controllerAs: 'vm',
      bindToController: {
        _readOnly: '=readOnly',
        _sectionTherapies: '=sectionTherapies',
        _sectionTitle: '@sectionTitle',
        cancelTherapyClickHandler: '&',
        removeTherapyClickHandler: '&'
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/eerPrescription/TmMedsEerPrescriptionDocumentSection.template.html'
    };
  }
})();