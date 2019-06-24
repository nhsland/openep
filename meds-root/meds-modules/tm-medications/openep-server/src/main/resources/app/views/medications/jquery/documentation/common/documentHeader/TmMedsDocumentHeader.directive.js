(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.documentHeader')
      .directive('tmMedsDocumentHeader', tmMedsDocumentHeader);

  function tmMedsDocumentHeader()
  {
    return {
      restrict: 'E',
      scope: {
        document: '='
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/documentHeader/TmMedsDocumentHeader.template.html',
      controller: 'tm.angular.medications.documentation.common.documentHeader.DocumentHeaderController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document'
      }
    };
  }
})();