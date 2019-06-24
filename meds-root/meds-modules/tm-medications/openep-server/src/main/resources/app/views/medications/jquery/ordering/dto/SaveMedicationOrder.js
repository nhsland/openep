Class.define('app.views.medications.ordering.dto.SaveMedicationOrder', 'app.views.medications.ordering.AbstractTherapyOrder', {
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type string of {@link app.views.medications.TherapyEnums.medicationOrderActionEnum} */
  actionEnum: null,
  /** @type string|null */
  sourceId: null,
  /** @type string|null */
  linkCompositionUid: null,
  /** @type app.views.medications.common.dto.TherapyChangeReason|null */
  changeReasonDto: null,
  /** @type string of {@link app.views.medications.TherapyEnums.therapyStatusEnum.NORMAL} used by presentation logic */
  therapyStatus: app.views.medications.TherapyEnums.therapyStatusEnum.NORMAL,
  /** @type boolean */
  readOnly: false,
  /** @type Array<string> */
  validationIssues: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._applyTherapyStatus();
    this.validationIssues = tm.jquery.Utils.isArray(this.validationIssues) ? this.validationIssues : [];
  },

  /**
   * @override
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.ordering.dto.SaveMedicationOrder}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.ordering.dto.SaveMedicationOrder({
      therapy: this.therapy.clone(true),
      actionEnum: this.actionEnum,
      sourceId: this.sourceId,
      linkCompositionUid: this.linkCompositionUid,
      changeReasonDto: this.changeReasonDto ? this.changeReasonDto.clone() : null,
      therapyStatus: this.therapyStatus,
      readOnly: this.readOnly,
      validationIssues: this.validationIssues.slice()
    })
  },

  /**
   * @param {string} enumValue of {@link app.views.medications.TherapyEnums.medicationOrderActionEnum}
   */
  setActionEnum: function(enumValue)
  {
    this.actionEnum = enumValue;
    this._applyTherapyStatus();
  },

  /**
   * @param {string} value
   */
  setSourceId: function(value)
  {
    this.sourceId = value;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason|null} value
   */
  setTherapyChangeReason: function(value)
  {
    this.changeReasonDto = value;
  },

  /**
   * @param {boolean} value
   */
  setReadOnly: function(value)
  {
    this.readOnly = value;
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.medicationOrderActionEnum}
   */
  getActionEnum: function()
  {
    return this.actionEnum;
  },

  /**
   * @return {string}
   */
  getSourceId: function()
  {
    return this.sourceId;
  },

  /**
   * @return {boolean}
   */
  isReadOnly: function()
  {
    return this.readOnly;
  },

  /**
   * @returns {string|null}
   */
  getLinkCompositionUid: function()
  {
    return this.linkCompositionUid;
  },
  /**
   * @param {string} linkCompositionUid
   */
  setLinkCompositionUid: function(linkCompositionUid)
  {
    this.linkCompositionUid = linkCompositionUid;
  },

  /**
   * @override
   * @returns {Array<String>}
   */
  getValidationIssues: function()
  {
    return this.validationIssues;
  },

  /**
   * @override
   * @return {boolean}
   */
  isRecordAdministration: function()
  {
    return this.actionEnum === app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER;
  },

  _applyTherapyStatus: function()
  {
    var medicationOrderEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var therapyStatusEnum = app.views.medications.TherapyEnums.therapyStatusEnum;
    var actionEnum = this.getActionEnum();

    switch (actionEnum)
    {
      case medicationOrderEnum.ABORT:
        this.therapyStatus = therapyStatusEnum.ABORTED;
        break;
      case medicationOrderEnum.SUSPEND:
        this.therapyStatus = therapyStatusEnum.SUSPENDED;
        break;
      case medicationOrderEnum.SUSPEND_ADMISSION:
        this.therapyStatus = therapyStatusEnum.SUSPENDED;
        break;
      default:
        this.therapyStatus = therapyStatusEnum.NORMAL;
        break;
    }
  }
});
