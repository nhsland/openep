(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.externalPrescriptions')
      .controller('tm.angular.medications.documentation.externalPrescriptions.PrescriptionsDialogController',
          PrescriptionsDialogController);

  /**
   * @constructor
   */
  function PrescriptionsDialogController($uibModalInstance, documents, documentService)
  {
    var vm = this;
    vm.documents = documents;
    vm.getDocuments = getDocuments;
    vm.close = close;
    vm.moreDocumentsExist = moreDocumentsExist;
    vm.showList = showList;
    vm.setActiveDocument = setActiveDocument;
    vm.getActiveDocument = getActiveDocument;
    vm.isActiveDocument = isActiveDocument;
    vm.clearActiveDocument = clearActiveDocument;

    function getDocuments()
    {
      return vm.documents;
    }

    function close()
    {
      $uibModalInstance.close({
        success: true
      });
    }

    /*  Private properties */
    var _activeDocument = null;

    /**
     * Defines if the complete list of documents should be shown.
     * @returns {boolean}
     */
    function showList()
    {
      return _activeDocument === null;
    }

    /***
     * Always returns false in case of external prescriptions.
     * @returns {boolean}
     */
    function moreDocumentsExist()
    {
      return false;
    }

    /**
     * Sets the active document which should be shown in the single document view.
     * @param {TherapyDocument} document
     */
    function setActiveDocument(document)
    {
      _activeDocument = document;
    }

    /**
     * Returns the currently active document, that should be shown in the single document view.
     * @returns {TherapyDocument}
     */
    function getActiveDocument()
    {
      return _activeDocument;
    }

    /**
     * Returns true if the passed document is also the currently active document show in the single document
     * view.
     * @param {TherapyDocument} document
     * @returns {boolean}
     */
    function isActiveDocument(document)
    {
      return _activeDocument === document;
    }

    /**
     * Clears the currently active document, which in effect should switch from single dcoument view to
     * multi document view.
     */
    function clearActiveDocument()
    {
      _activeDocument = null;
    }
  }

  PrescriptionsDialogController.$inject = ['$uibModalInstance', 'documents', 'tm.angular.medications.documentation.data.DocumentService'];
})();