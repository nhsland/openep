Class.define('app.views.medications.ordering.dto.TherapyTemplatePrecondition', 'tm.jquery.Object', {
  precondition: null,
  minValue: null,
  maxValue: null,
  exactValue: null,

  Constructor: function(config)
  {
    this.callSuper(config)
  },

  /**
   * @returns {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum|null}
   */
  getPrecondition: function()
  {
    return this.precondition;
  },

  /**
   * @param {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum|null} precondition
   */
  setPrecondition: function(precondition)
  {
    this.precondition = precondition;
  },

  /**
   * @returns {Number|null}
   */
  getMinValue: function()
  {
    return this.minValue;
  },

  /**
   * @param {Number|null} minValue
   */
  setMinValue: function(minValue)
  {
    this.minValue = minValue;
  },

  /**
   * @returns {Number|null}
   */
  getMaxValue: function()
  {
    return this.maxValue;
  },

  /**
   * @param {Number|null} maxValue
   */
  setMaxValue: function(maxValue)
  {
    this.maxValue = maxValue;
  },

  /**
   * @returns {Number|null}
   */
  getExactValue: function()
  {
    return this.exactValue;
  },

  /**
   * @param {Number|null} exactValue
   */
  setExactValue: function(exactValue)
  {
    this.exactValue = exactValue;
  }
});