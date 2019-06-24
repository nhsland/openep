Class.define('app.views.medications.reconciliation.dto.SourceMedication', 'app.views.medications.ordering.AbstractTherapyOrder', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.SourceMedication({
            therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
            sourceId: jsonObject.sourceId,
            validationIssues: tm.jquery.Utils.isArray(jsonObject.validationIssues) ?
                jsonObject.validationIssues.slice(0) :
                []
          });
    }
  },
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type string|null */
  sourceId: null,
  /** @type Array<string> */
  validationIssues: null,
  /** @type boolean */
  readOnly: false,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.validationIssues = tm.jquery.Utils.isArray(this.validationIssues) ? this.validationIssues : [];
  },

  /**
   * @override
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.reconciliation.dto.SourceMedication}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.reconciliation.dto.SourceMedication({
      therapy: this.therapy.clone(true),
      sourceId: this.sourceId,
      validationIssues: this.validationIssues.slice(),
      readOnly: this.readOnly
    })
  },

  getTherapy: function()
  {
    return this.therapy;
  },

  getId: function()
  {
    return this.sourceId;
  },

  /**
   * @override
   * @returns {Array<String>} of {@link app.views.medications.TherapyEnums.validationIssueEnum}
   */
  getValidationIssues: function()
  {
    return this.validationIssues;
  },

  /**
   * @return {boolean} true, when actions on the given source medication are prohibited, otherwise false.
   */
  isReadOnly: function()
  {
    return this.readOnly === true;
  }
});
