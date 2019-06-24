Class.define('app.views.medications.common.dto.AdditionalWarnings', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      config.warnings.forEach(function(warning)
      {
        warning.therapy = app.views.medications.common.TherapyJsonConverter.convert(warning.therapy);
      });
      return new app.views.medications.common.dto.AdditionalWarnings(config);
    }
  },
  taskIds: null,


  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Array<string>}
   */
  getTaskIds: function()
  {
    return this.taskIds;
  },

  /**
   * @returns {Array<{therapy: app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy,
   * warnings: [{additionalWarningsType: string, warning: Object}]}>}
   */
  getWarnings: function()
  {
    return this.warnings;
  },

  /**
   * @returns {boolean} true, if warnings are present, otherwise false.
   */
  hasWarnings: function()
  {
    return this.warnings.length > 0;
  },

  /**
   * @returns {boolean} true, if there are user tasks associated with the warnings, otherwise false.
   */
  hasTaskIds: function()
  {
    return this.taskIds.length > 0;
  },

  /**
   * @returns {boolean} true, if the user is required to review the present warnings and either override them or
   * cancel a problematic active prescription.
   */
  isUserReviewRequired: function()
  {
     return this.hasTaskIds() && this.hasWarnings();
  }
});