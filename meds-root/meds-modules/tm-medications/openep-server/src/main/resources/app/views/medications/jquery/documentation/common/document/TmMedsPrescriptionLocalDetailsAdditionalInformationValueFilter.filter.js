(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.document')
      .filter('tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter',
          tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter);

  /**
   * Returns a filter for the display value of the additional information label - based on doNotSwitch, maxDoseExceeded
   * and illnessConditionType values from the PrescriptionLocalDetails.
   */
  function tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter(viewProxy)
  {
    return function(localDetails)
    {
      var additionalInformationArray = [];
      if (localDetails.getIllnessConditionType())
      {
        additionalInformationArray.push(viewProxy.getDictionary("IllnessConditionType."
            + localDetails.getIllnessConditionType()));
      }

      if (localDetails.isDoNotSwitch())
      {
        additionalInformationArray.push(viewProxy.getDictionary('do.not.switch'));
      }

      if (localDetails.isMaxDoseExceeded())
      {
        additionalInformationArray.push(viewProxy.getDictionary('exceed.maximum.dose'));
      }

      return additionalInformationArray.join(' - ');
    }
  }
  tmMedsPrescriptionLocalDetailsAdditionalInformationValueFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];

})();