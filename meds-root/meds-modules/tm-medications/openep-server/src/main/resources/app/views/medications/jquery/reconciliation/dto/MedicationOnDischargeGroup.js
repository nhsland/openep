Class.define('app.views.medications.reconciliation.dto.MedicationOnDischargeGroup', 'app.views.medications.reconciliation.dto.MedicationGroup', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.MedicationOnDischargeGroup({
        groupEnum: jsonObject.groupEnum,
        groupName: jsonObject.groupName,
        groupElements: tm.jquery.Utils.isArray(jsonObject.groupElements) ?
            jsonObject.groupElements.map(app.views.medications.reconciliation.dto.DischargeSourceMedication.fromJson) :
            []
      });
    }
  },

  /***
   * @return {boolean}
   */
  isAbortedTherapiesGroup: function()
  {
    return this.getGroupEnum() === app.views.medications.TherapyEnums.therapySourceGroupEnum.STOPPED_ADMISSION_MEDICATION;
  },

  /**
   * @return {boolean}
   */
  isCancelTherapySupported: function()
  {
    return this.getGroupEnum() === app.views.medications.TherapyEnums.therapySourceGroupEnum.MEDICATION_ON_ADMISSION;
  }
});