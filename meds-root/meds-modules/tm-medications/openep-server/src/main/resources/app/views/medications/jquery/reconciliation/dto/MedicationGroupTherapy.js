Class.define('app.views.medications.reconciliation.dto.MedicationGroupTherapy', 'app.views.medications.ordering.AbstractTherapyOrder', {
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type string */
  sourceGroupEnum: null,
  /** @type string|null */
  sourceId: null,
  /** @type string|null */
  status: null,
  /** @type app.views.medications.common.dto.TherapyChangeReason|null */
  changeReasonDto: null,
  /** @type Array<string> */
  validationIssues: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.validationIssues = tm.jquery.Utils.isArray(this.validationIssues) ? this.validationIssues : [];
  },

  /**
   * @override
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.reconciliation.dto.MedicationGroupTherapy}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.reconciliation.dto.MedicationGroupTherapy({
      therapy: this.therapy.clone(true),
      sourceGroupEnum: this.sourceGroupEnum,
      sourceId: this.sourceId,
      status: this.status,
      changeReasonDto: this.changeReasonDto ? this.changeReasonDto.clone() : null,
      validationIssues: this.validationIssues.slice()
    })
  },

  getTherapy: function()
  {
    return this.therapy;
  },

  getSourceGroupEnum: function()
  {
    return this.sourceGroupEnum;
  },

  getSourceId: function()
  {
    return this.sourceId;
  },

  getStatus: function()
  {
    return this.status;
  },

  setTherapy: function(therapy)
  {
    this.therapy = therapy;
  },

  setSourceGroupEnum: function(sourceGroup)
  {
    this.sourceGroupEnum = sourceGroup;
  },

  /**
   * @param {string} sourceId
   */
  setSourceId: function(sourceId)
  {
    this.sourceId = sourceId;
  },

  /**
   * @param {string} status
   */
  setStatus: function(status)
  {
    this.status = status;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason} therapyChangeReason
   */
  setTherapyChangeReason: function(therapyChangeReason)
  {
    this.changeReasonDto = therapyChangeReason;
  },

  /**
   * @returns {Array<String>}
   */
  getValidationIssues: function()
  {
    return this.validationIssues;
  }
});
