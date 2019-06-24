Class.define('app.views.medications.warnings.dto.ParacetamolRuleResult', 'tm.jquery.Object', {
  rule: null,
  quantityOk: false,
  betweenDosesTimeOk: false,
  adultRulePercentage: null,
  underageRulePercentage: null,
  medications: null,
  lastTaskTimestamp: null,
  lastTaskAdministered: false,
  errorMessage: null,

  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      config.lastTaskTimestamp = config.lastTaskTimestamp ? new Date(config.lastTaskTimestamp) : null;

      return new app.views.medications.warnings.dto.ParacetamolRuleResult(config);
    }
  },

  /**
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medications = tm.jquery.Utils.isArray(this.medications ) ? this.medications : [];
  },

  /**
   * @returns {String|null}
   */
  getRule: function()
  {
    return this.rule;
  },
  /**
   * @returns {boolean}
   */
  isQuantityOk: function()
  {
    return this.quantityOk === true;
  },
  /**
   * @returns {boolean}
   */
  isBetweenDosesTimeOk: function()
  {
    return this.betweenDosesTimeOk === true;
  },
  /**
   * @returns {Number}
   */
  getAdultRulePercentage: function()
  {
    return this.adultRulePercentage;
  },
  /**
   * @returns {Number}
   */
  getUnderageRulePercentage: function()
  {
    return this.underageRulePercentage;
  },

  /**
   * @returns {Array<{name: String, id: String}>}
   */
  getMedications: function()
  {
    return this.medications;
  },
  /**
   * @returns {Date|null}
   */
  getLastTaskTimestamp: function()
  {
    return this.lastTaskTimestamp;
  },
  /**
   * @returns {boolean}
   */
  isLastTaskAdministered: function()
  {
    return this.lastTaskAdministered === true;
  },
  /**
   * @returns {String}
   */
  getErrorMessage: function()
  {
    return this.errorMessage;
  },

  /**
   * Returns the highest value of the rule percentage - either {@link getUnderageRulePercentage} or
   * {@link getAdultRulePercentage}.
   */
  getHighestRulePercentage: function()
  {
    return this.getUnderageRulePercentage() >= this.getAdultRulePercentage() ?
        this.getUnderageRulePercentage() :
        this.getAdultRulePercentage();

  }
});