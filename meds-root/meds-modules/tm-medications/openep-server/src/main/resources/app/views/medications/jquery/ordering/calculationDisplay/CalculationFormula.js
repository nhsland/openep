Class.define('app.views.medications.ordering.calculationDisplay.CalculationFormula', 'tm.jquery.Object', {
  view: null,
  displayOrderId: 0,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Override to provide displayable calculation formula
   */
  getCalculationFormulaDisplay: function()
  {

  },

  /**
   * Returns display order id. Objects with lower order id will be displayed first.
   * @returns {number}
   */
  getDisplayOrderId: function()
  {
    return this.displayOrderId;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});