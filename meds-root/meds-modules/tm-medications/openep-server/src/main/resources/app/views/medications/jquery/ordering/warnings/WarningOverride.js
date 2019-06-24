Class.define('app.views.medications.ordering.warnings.WarningOverride', 'tm.jquery.Container', {

  warning: null,
  overrideReason: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {app.views.medications.warnings.dto.MedicationsWarning|null}
   */
  getWarning: function()
  {
    return this.warning;
  },

  /**
   * @param {app.views.medications.warnings.dto.MedicationsWarning} value
   */
  setWarning: function(value)
  {
    this.warning = value;
  },

  /**
   * @returns {String|null}
   */
  getOverrideReason: function()
  {
    return this.overrideReason;
  },

  /**
   * @param {String|null} value
   */
  setOverrideReason: function(value)
  {
    this.overrideReason = value;
  }
});