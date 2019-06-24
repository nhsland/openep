(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.indexColumn')
      .controller('tm.angular.medications.documentation.indexColumn.ListItemController', ListItemController);

  /**
   * @constructor
   */
  function ListItemController()
  {
    var vm = this;
    vm.getDocument = getDocument;

    /**
     * @returns {TherapyDocument}
     */
    function getDocument()
    {
      return vm._document;
    }
  }
})();