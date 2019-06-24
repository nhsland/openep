Class.define('app.views.medications.ordering.calculationDisplay.BodySurfaceCalculationFormula', 'app.views.medications.ordering.calculationDisplay.CalculationFormula', {
  referenceData: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.referenceData)
    {
      throw Error('referenceData is undefined.');
    }
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  },

  /**
   * @Override
   * @returns {app.views.medications.common.VerticallyTitledComponent|null}
   */
  getCalculationFormulaDisplay: function()
  {
    var utils = app.views.medications.MedicationUtils;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var bodySurfaceArea = this.getReferenceData().getBodySurfaceArea();
    if (bodySurfaceArea)
    {
      var contentHtml =
          utils.doubleToString(bodySurfaceArea, "n3") + " " +
          view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.M2).getDisplayName() +
          " = " + "&radic;" + '<span class ="TextDataOverLine">' +
          utils.doubleToString(this.getReferenceData().getWeight(), "n3") + " " +
          view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.KG).getDisplayName() + " * " +
          utils.doubleToString(this.getReferenceData().getHeight(), "n3") + " cm</span>" + " &divide; 60";
      return new app.views.medications.common.VerticallyTitledComponent({
        titleText: view.getDictionary("body.surface") + ": ",
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