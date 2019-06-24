Class.define('app.views.medications.ordering.calculationDisplay.CalculationDetailsContainer',  'app.views.common.containers.AppDataEntryContainer', {
  view: null,
  cls: 'calculations-info-container',

  /**configs*/
  calculationFormulaProviders: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isArray(this.calculationFormulaProviders))
    {
      this.calculationFormulaProviders = [];
    }
    this._buildGui();
  },

  _buildGui: function()
  {
    var calculationsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this.setFlex(tm.jquery.flexbox.item.Flex.create(0, 0, "auto"));
    this._sortDisplayOrder();

    this.calculationFormulaProviders.forEach(function(calculationFormulaProvider)
    {
      var calculationFormulaDisplay = calculationFormulaProvider.getCalculationFormula().getCalculationFormulaDisplay();
      if (!tm.jquery.Utils.isEmpty(calculationFormulaDisplay))
      {
        calculationFormulaDisplay = tm.jquery.Utils.isArray(calculationFormulaDisplay) ?
            calculationFormulaDisplay :
            [calculationFormulaDisplay];

        calculationFormulaDisplay.forEach(function(formulaDisplay)
        {
          calculationsContainer.add(formulaDisplay);
        });
      }
    });
    this.add(calculationsContainer);
  },

  /**
   * @private
   */
  _sortDisplayOrder: function()
  {
    this.calculationFormulaProviders.sort(function(a, b)
    {
      return parseInt(a.getCalculationFormula().getDisplayOrderId(), 10) -
          parseInt(b.getCalculationFormula().getDisplayOrderId(), 10);
    })
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});