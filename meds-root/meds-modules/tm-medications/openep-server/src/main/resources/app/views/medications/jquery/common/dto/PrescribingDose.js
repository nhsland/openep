Class.define('app.views.medications.common.dto.PrescribingDose', 'tm.jquery.Object', {
  numerator: null,
  numeratorUnit: null,
  denominator: null,
  denominatorUnit: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Number|null} numerator
   */
  setNumerator: function(numerator)
  {
    this.numerator = numerator;
  },

  /**
   * @returns {Number|null}
   */
  getNumerator: function()
  {
    return this.numerator;
  },

  /**
   * @param {String} numeratorUnit
   */
  setNumeratorUnit: function(numeratorUnit)
  {
    this.numeratorUnit = numeratorUnit
  },

  /**
   * @returns {String}
   */
  getNumeratorUnit: function()
  {
    return this.numeratorUnit;
  },

  /**
   * @param {Number|null} denominator
   */
  setDenominator: function(denominator)
  {
    this.denominator = denominator;
  },

  /**
   * @returns {Number|null}
   */
  getDenominator: function()
  {
    return this.denominator;
  },

  /**
   * @param {String} denominatorUnit
   */
  setDenominatorUnit: function(denominatorUnit)
  {
    this.denominatorUnit = denominatorUnit;
  },

  /**
   * @returns {String|null}
   */
  getDenominatorUnit: function()
  {
    return this.denominatorUnit;
  },


  /**
   * @returns {String}
   */
  getDisplaySting: function()
  {
    var strengthString = "";
    if (this.getNumerator())
    {
      strengthString += Globalize.formatNumber(this.getNumerator()) +
          this.getNumeratorUnit();
    }
    if (this.getDenominator())
    {
      strengthString += '/' + Globalize.formatNumber(this.getDenominator()) +
          this.getDenominatorUnit();
    }
    return strengthString;
  }
});
