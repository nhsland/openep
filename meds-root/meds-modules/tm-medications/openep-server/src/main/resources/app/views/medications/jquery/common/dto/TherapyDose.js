Class.define('app.views.medications.common.dto.TherapyDose', 'tm.jquery.Object', {

  therapyDoseTypeEnum: null,
  numerator: null,
  numeratorUnit: null,
  denominator: null,
  denominatorUnit: null,
  secondaryNumerator: null,
  secondaryNumeratorUnit: null,
  secondaryDenominator: null,
  secondaryDenominatorUnit: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {String} value
   */
  setTherapyDoseTypeEnum: function(value)
  {
    this.therapyDoseTypeEnum = value;
  },
  /**
   * @param {Number} value
   */
  setNumerator: function(value)
  {
    this.numerator = value;
  },
  /**
   * @param {String} value
   */
  setNumeratorUnit: function(value)
  {
    this.numeratorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setDenominator: function(value)
  {
    this.denominator = value;
  },
  /**
   * @param {String} value
   */
  setDenominatorUnit: function(value)
  {
    this.denominatorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setSecondaryNumerator: function(value)
  {
    this.secondaryNumerator = value;
  },
  /**
   * @param {String} value
   */
  setSecondaryNumeratorUnit: function(value)
  {
    this.secondaryNumeratorUnit = value;
  },
  /**
   * @param {Number} value
   */
  setSecondaryDenominator: function(value)
  {
    this.secondaryDenominator = value;
  },
  /**
   * @param {String} value
   */
  setSecondaryDenominatorUnit: function(value)
  {
    this.secondaryDenominatorUnit = value;
  },

  /**
   * @returns {String}
   */
  getTherapyDoseTypeEnum: function()
  {
    return this.therapyDoseTypeEnum;
  },
  /**
   * @returns {Number}
   */
  getNumerator: function()
  {
    return this.numerator;
  },
  /**
   * @returns {String}
   */
  getNumeratorUnit: function()
  {
    return this.numeratorUnit;
  },
  /**
   * @returns {Number}
   */
  getDenominator: function()
  {
    return this.denominator;
  },
  /**
   * @returns {String}
   */
  getDenominatorUnit: function()
  {
    return this.denominatorUnit;
  },
  /**
   * @returns {Number}
   */
  getSecondaryNumerator: function()
  {
    return this.secondaryNumerator;
  },
  /**
   * @returns {String}
   */
  getSecondaryNumeratorUnit: function()
  {
    return this.secondaryNumeratorUnit;
  },
  /**
   * @returns {Number}
   */
  getSecondaryDenominator: function()
  {
    return this.secondaryDenominator;
  },
  /**
   * @returns {String}
   */
  getSecondaryDenominatorUnit: function()
  {
    return this.secondaryDenominatorUnit;
  }
});