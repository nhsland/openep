(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .directive('tmMedsDocumentPresenter', tmMedsDocumentPresenter);

  function tmMedsDocumentPresenter()
  {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        document: '=',
        readOnly: '='
      },
      templateUrl: '../ui/app/views/medications/jquery/documentation/common/document/TmMedsDocumentPresenter.template.html',
      controller: 'tm.angular.medications.documentation.common.document.DocumentPresenterController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document',
        _readOnly: '=readOnly'
      }
    };
  }
})();