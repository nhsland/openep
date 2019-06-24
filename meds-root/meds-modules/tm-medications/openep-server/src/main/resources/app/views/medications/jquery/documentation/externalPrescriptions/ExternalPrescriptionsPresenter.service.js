(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.externalPrescriptions')
      .service('tm.angular.medications.documentation.externalPrescriptions.ExternalPrescriptionsPresenter',
          ExternalPrescriptionsPresenter);

  /**
   * @constructor
   */
  function ExternalPrescriptionsPresenter(modalService, TherapyDocument)
  {
    this.showExternalPrescriptions = showExternalPrescriptions;
    this.handleViewActionCallback = handleViewActionCallback;

    /**
     * @param {Object} eventData
     */
    function handleViewActionCallback(eventData)
    {
      if (!eventData || !eventData.therapyDocuments) return;

      var therapyDocuments = eventData.therapyDocuments.map(_jsonToTherapyDocumentMapper);
      showExternalPrescriptions(therapyDocuments);
    }
    
    /***
      * @param {Array|TherapyDocument} documents
     */
    function showExternalPrescriptions(documents)
    {
      var modalInstance = modalService.openModal({
        templateUrl: '../ui/app/views/medications/jquery/documentation/externalPrescriptions/TmMedsExternalPrescriptionsDialog.template.html',
        backdrop: true,
        controllerAs: 'vm',
        controller: 'tm.angular.medications.documentation.externalPrescriptions.PrescriptionsDialogController',
        resolve: {
          documents: function getDocuments()
          {
            return documents;
          }
        }
      });

      modalInstance.result.then(function()
      {
        console.log('Modal closed.');
      });
    }

    function _jsonToTherapyDocumentMapper(jsonObject)
    {
      return TherapyDocument.fromJsonObject(jsonObject);
    }
  }

  ExternalPrescriptionsPresenter.$inject = ['tm.angularjs.common.modal.ModalService', 'tm.angular.medications.documentation.data.models.TherapyDocument'];
})();