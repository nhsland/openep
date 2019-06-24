Class.define('app.views.medications.reconciliation.dto.MedicationOnDischarge', 'app.views.medications.reconciliation.dto.MedicationGroupTherapy', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.MedicationOnDischarge({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        status: jsonObject.status,
        sourceGroupEnum: jsonObject.sourceGroupEnum,
        sourceId: jsonObject.sourceId,
        changeReasonDto: app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReasonDto)
      });
    }
  },
  /** @type string of {@link app.views.medications.TherapyEnums.medicationOnDischargeStatus} */
  status: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @override
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.reconciliation.dto.MedicationOnDischarge}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.reconciliation.dto.MedicationOnDischarge({
      therapy: this.therapy.clone(true),
      status: this.status,
      sourceGroupEnum: this.sourceGroupEnum,
      sourceId: this.sourceId,
      changeReasonDto: this.changeReasonDto ? this.changeReasonDto.clone() : null,
      validationIssues: this.validationIssues.slice()
    })
  },

  /* status helpers*/
  /**
   * @return {boolean}
   */
  isAborted: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnDischargeStatus.NOT_PRESCRIBED;
  },

  /**
   * @return {boolean}
   */
  isPrescribed: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnDischargeStatus.PRESCRIBED;
  },

  /**
   * @return {boolean}
   */
  isEditedAndPrescribed: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnDischargeStatus.EDITED_AND_PRESCRIBED;
  }
});
