Class.define('app.views.medications.ordering.dto.TherapyTemplateElement', 'app.views.medications.ordering.AbstractTherapyOrder', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.ordering.dto.TherapyTemplateElement({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        templateStatus: jsonObject.templateStatus,
        recordAdministration: jsonObject.recordAdministration === true,
        validationIssues: tm.jquery.Utils.isArray(jsonObject.validationIssues) ? jsonObject.validationIssues.slice(0) : []
      });
    },
    /**
     * Creates a therapy template element with a cloned instance of the therapy, cleared of any properties which should not
     * be saved as part of an order set.
     * @param {app.views.medications.common.dto.Therapy} therapy
     * @returns {app.views.medications.ordering.dto.TherapyTemplateElement}
     */
    fromExistingTherapy: function(therapy)
    {
      return new app.views.medications.ordering.dto.TherapyTemplateElement({
        therapy: therapy.clone(true )
            .setLinkName(null)
            .setCompositionUid(null)
            .setCriticalWarnings(null)
            .setInformationSources([]),
        templateStatus: therapy.isCompleted() ?
            app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE :
            app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE
      });
    },
    /**
     * Similar to {@link fromExistingTherapy}, but supports setting additional properties not found on an a therapy instance.
     * @param {app.views.medications.ordering.AbstractTherapyOrder|
     * app.views.medications.common.therapy.AbstractTherapyContainerData} order
     * @returns {app.views.medications.ordering.dto.TherapyTemplateElement}
     */
    fromTherapyOrder: function(order)
    {
      return app.views.medications.ordering.dto.TherapyTemplateElement
          .fromExistingTherapy(order.getTherapy())
          .setRecordAdministration(order.isRecordAdministration());
    }
  },
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type string from {@link app.views.medications.TherapyEnums.therapyTemplateStatus} */
  templateStatus: app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE,
  /** @type Array<string> */
  validationIssues: null,
  /** @type boolean */
  recordAdministration: false,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.validationIssues = tm.jquery.Utils.isArray(this.validationIssues) ? this.validationIssues : [];
  },

  /**
   * @override
   * @param {boolean} [startEndTimeAvailable=true]
   * @return {app.views.medications.ordering.dto.TherapyTemplateElement}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    var therapyClone = this.therapy.clone(true);
    if (startEndTimeAvailable !== false)
    {
      therapyClone.rescheduleTherapyTimings(false);
    }
    else
    {
      therapyClone.setStart(null);
      therapyClone.setEnd(null);
    }

    return new app.views.medications.ordering.dto.TherapyTemplateElement({
      therapy: therapyClone,
      recordAdministration: this.recordAdministration,
      templateStatus: this.templateStatus,
      validationIssues: this.validationIssues.slice()
    });
  },

  setTherapy: function(value)
  {
    this.therapy = value;
  },

  /**
   * @returns {String} {app.views.medications.TherapyEnums.therapyTemplateStatus}
   */
  getTemplateStatus: function()
  {
    return this.templateStatus;
  },

  /**
   * @param {String} templateStatus {app.views.medications.TherapyEnums.therapyTemplateStatus}
   * @return {app.views.medications.ordering.dto.TherapyTemplateElement}
   */
  setTemplateStatus: function(templateStatus)
  {
    this.templateStatus = templateStatus;
    return this;
  },

  /**
   * Based upon {@link app.views.medications.ordering.dto.TherapyTemplateElement#templateStatus}.
   * @return {boolean}
   */
  isIncomplete: function()
  {
    return this.templateStatus === app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE
  },

  /**
   * @param {boolean} value
   * @return {app.views.medications.ordering.dto.TherapyTemplateElement}
   */
  setRecordAdministration: function(value)
  {
    this.recordAdministration = value;
    return this;
  },

  /**
   * @override
   * @returns {Array<String>} of {@link app.views.medications.TherapyEnums.validationIssueEnum}
   */
  getValidationIssues: function()
  {
    return this.validationIssues;
  }
});
