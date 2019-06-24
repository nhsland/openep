Class.define('app.views.medications.ordering.calculationDisplay.MaxDoseCalculationFormula', 'app.views.medications.ordering.calculationDisplay.CalculationFormula', {

  period: null,
  percentage: null,
  quantity: null,
  timesPerDay: null,
  timesPerWeek: null,
  numeratorUnit: null,
  maxValue: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @Override
   * @returns {app.views.medications.common.VerticallyTitledComponent|null}
   */
  getCalculationFormulaDisplay: function()
  {
    var utils = app.views.medications.MedicationUtils;
    var contentHtml = "";
    var period = this.period;

    if (this.percentage)
    {
      var quantityDisplay = utils.doubleToString(this.quantity, "n3");
      contentHtml += this.percentage + "% = ";
      if (period === app.views.medications.TherapyEnums.maxDosePeriod.DAY)
      {
        contentHtml += this.timesPerDay + " * " + quantityDisplay + " " + this.numeratorUnit;
      }
      else if (period === app.views.medications.TherapyEnums.maxDosePeriod.WEEK)
      {
        contentHtml += this.timesPerWeek + " * " + quantityDisplay + this.numeratorUnit + " * "
            + this.timesPerDay;
      }
      contentHtml += " / " + this.maxValue + " * 100";
      return new app.views.medications.common.VerticallyTitledComponent({
        titleText: this.getView().getDictionary("max.dose.percentage") + ": ",
        scrollable: 'visible',
        contentComponent: new tm.jquery.Container({
          scrollable: 'visible',
          layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
          html: contentHtml
        }),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
      });
    }
    return null;
  }
});