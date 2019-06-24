Class.define('app.views.medications.common.dto.MedicationIngredient', 'tm.jquery.Object', {
  ingredientId: null, /* Long */
  ingredientName: null, /* String */
  strengthNumerator: null, /* Double */
  strengthNumeratorUnit: null, /* String */
  strengthDenominator: null, /* Double */
  strengthDenominatorUnit: null, /* String */
  descriptive: null, /* Boolean */
  main: null, /* Boolean */
  ingredientRule: null, /* MedicationRuleEnum */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Number} ingredientId
   */
  setIngredientId: function(ingredientId)
  {
    this.ingredientId = ingredientId;
  },

  /**
   * @returns {Number}
   */
  getIngredientId: function()
  {
    return this.ingredientId;
  },

  /**
   * @param {String} ingredientName
   */
  setIngredientName: function(ingredientName)
  {
    this.ingredientName = ingredientName;
  },

  /**
   * @returns {String}
   */
  getIngredientName: function()
  {
    return this.ingredientName;
  },

  /**
   * @param {Number} strengthNumerator
   */
  setStrengthNumerator: function(strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  },

  /**
   * @returns {Number}
   */
  getStrengthNumerator: function()
  {
    return this.strengthNumerator;
  },

  /**
   * @param {String|null} strengthNumeratorUnit
   */
  setStrengthNumeratorUnit: function(strengthNumeratorUnit)
  {
    this.strengthNumeratorUnit = strengthNumeratorUnit;
  },

  /**
   * @returns {String|null}
   */
  getStrengthNumeratorUnit: function()
  {
    return this.strengthNumeratorUnit;
  },

  /**
   * @param {Number} strengthDenominator
   */
  setStrengthDenominator: function(strengthDenominator)
  {
    this.strengthDenominator = strengthDenominator;
  },

  /**
   * @returns {Number}
   */
  getStrengthDenominator: function()
  {
    return this.strengthDenominator;
  },

  /**
   * @param {String|null} strengthDenominatorUnit
   */
  setStrengthDenominatorUnit: function(strengthDenominatorUnit)
  {
    this.strengthDenominatorUnit = strengthDenominatorUnit;
  },

  /**
   * @returns {String|null}
   */
  getStrengthDenominatorUnit: function()
  {
    return this.strengthDenominatorUnit;
  },

  /**
   * @param {String} ingredientRule
   */
  setIngredientRule: function(ingredientRule)
  {
    this.ingredientRule = ingredientRule;
  },

  /**
   * @returns {String}
   */
  getIngredientRule: function()
  {
    return this.ingredientRule;
  },

  /**
   * @returns {Boolean}
   */
  isDescriptive: function()
  {
    return this.descriptive === true;
  },

  /**
   * @returns {Boolean}
   */
  isMain: function()
  {
    return this.main === true;
  },

  /**
   * @param {Boolean} main
   */
  setMain: function(main)
  {
    this.main = main;
  },

  /**
   * @returns {string}
   */
  getDisplayString: function()
  {
    var strengthString = "";
    if (this.getStrengthNumerator())
    {
      strengthString += Globalize.formatNumber(this.getStrengthNumerator()) +
          this.getStrengthNumeratorUnit();
    }
    if (this.getStrengthDenominator())
    {
      strengthString += '/' + Globalize.formatNumber(this.getStrengthDenominator()) +
          this.getStrengthDenominatorUnit();
    }
    return strengthString;
  }
});