(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.indexColumn')
      .directive('tmMedsIndexColumnListItem', tmMedsIndexColumnListItem);

  function tmMedsIndexColumnListItem()
  {
    return {
      restrict: 'E',
      scope: {
        document: '='
      },
      replace: true,
      templateUrl: '../ui/app/views/medications/jquery/documentation/indexColumn/TmMedsIndexColumnListItem.template.html',
      controller: 'tm.angular.medications.documentation.indexColumn.ListItemController',
      controllerAs: 'vm',
      bindToController: {
        _document: '=document'
      }
    };
  }
})();