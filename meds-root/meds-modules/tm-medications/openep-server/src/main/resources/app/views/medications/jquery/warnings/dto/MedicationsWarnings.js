Class.define('app.views.medications.warnings.dto.MedicationsWarnings', 'tm.jquery.Object', {

  _highSeverityWarnings: null,
  _lowSeverityWarnings: null,
  warnings: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.warnings = tm.jquery.Utils.isArray(this.warnings) ? this.warnings : [];
    this._highSeverityWarnings = tm.jquery.Utils.isArray(this._highSeverityWarnings) ?
        this._highSeverityWarnings :
        [];
    this._lowSeverityWarnings = tm.jquery.Utils.isArray(this._lowSeverityWarnings) ?
        this._lowSeverityWarnings :
        [];
    this._classifyWarnings();
  },

  _classifyWarnings: function()
  {
    var warningsSeverityEnums = app.views.medications.TherapyEnums.warningSeverityEnum;
    this.warnings.forEach(function classifyWarnings(warning)
    {
      if (warning.getSeverity() === warningsSeverityEnums.OTHER || tm.jquery.Utils.isEmpty(warning.getSeverity()))
      {
        this._lowSeverityWarnings.push(warning);
      }
      else
      {
        this._highSeverityWarnings.push(warning);
      }
    }, this)
  },

  /**
   * @returns {Array<app.views.medications.warnings.dto.MedicationsWarning>}
   */
  getHighSeverityWarnings: function()
  {
    return this._highSeverityWarnings;
  },

  /**
   * @returns {Array<app.views.medications.warnings.dto.MedicationsWarning>}
   */
  getLowSeverityWarnings: function()
  {
    return this._lowSeverityWarnings;
  },

  /**
   * @param {Array<app.views.medications.warnings.dto.MedicationsWarning>} warnings
   */
  setLowSeverityWarnings: function(warnings)
  {
    this._lowSeverityWarnings = warnings;
  },

  /**
   * @param {Array<app.views.medications.warnings.dto.MedicationsWarning>} warnings
   */
  setHighSeverityWarnings: function(warnings)
  {
    this._highSeverityWarnings = warnings;
  },

  /**
   * @returns {Array<app.views.medications.warnings.dto.MedicationsWarning>}
   */
  getAllWarnings: function()
  {
    return this.getHighSeverityWarnings().concat(this.getLowSeverityWarnings());
  },

  /**
   * @returns {Number}
   */
  getHighSeverityWarningsCount: function()
  {
    return this.getHighSeverityWarnings().length;
  },

  /**
   * @returns {Number}
   */
  getLowSeverityWarningsCount: function()
  {
    return this.getLowSeverityWarnings().length;
  }
});
