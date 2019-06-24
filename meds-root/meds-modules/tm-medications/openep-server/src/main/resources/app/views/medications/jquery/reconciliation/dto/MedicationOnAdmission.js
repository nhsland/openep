Class.define('app.views.medications.reconciliation.dto.MedicationOnAdmission', 'app.views.medications.reconciliation.dto.MedicationGroupTherapy', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.MedicationOnAdmission({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        status: jsonObject.status,
        sourceGroupEnum: jsonObject.sourceGroupEnum,
        sourceId: jsonObject.sourceId,
        changeReasonDto: app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReasonDto),
        validationIssues: jsonObject.validationIssues
      });
    }
  },
  /** @type string of {@link app.views.medications.TherapyEnums.medicationOnAdmissionStatus} */
  status: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @override
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.reconciliation.dto.MedicationOnAdmission}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.reconciliation.dto.MedicationOnAdmission({
      therapy: this.therapy.clone(true),
      status: this.status,
      sourceGroupEnum: this.sourceGroupEnum,
      sourceId: this.sourceId,
      changeReasonDto: this.changeReasonDto ? this.changeReasonDto.clone() : null,
      validationIssues: this.validationIssues.slice()
    })
  },

  /**
   * @param {string} status of  {@link app.views.medications.TherapyEnums.medicationOnAdmissionStatus}
   */
  setStatus: function(status)
  {
    this.status = status;
  },

  /**
   * @return {boolean}
   */
  isReadOnly: function()
  {
    return !this.isPending();
  },

  /* status helpers*/
  /**
   * @return {boolean}
   */
  isPending: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnAdmissionStatus.PENDING;
  },

  /**
   * @return {boolean}
   */
  isAborted: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnAdmissionStatus.ABORTED;
  },

  /**
   * @return {boolean}
   */
  isPrescribed: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnAdmissionStatus.PRESCRIBED;
  },

  /**
   * @return {boolean}
   */
  isSuspended: function()
  {
    return this.status === app.views.medications.TherapyEnums.medicationOnAdmissionStatus.SUSPENDED;
  }
});
