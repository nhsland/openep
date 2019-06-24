(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.documentHeader')
      .controller('tm.angular.medications.documentation.common.documentHeader.DocumentHeaderController', DocumentHeaderController);

  function DocumentHeaderController()
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