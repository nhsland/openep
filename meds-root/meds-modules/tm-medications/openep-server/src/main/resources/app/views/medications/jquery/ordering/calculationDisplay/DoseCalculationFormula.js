Class.define('app.views.medications.ordering.calculationDisplay.DoseCalculationFormula', 'app.views.medications.ordering.calculationDisplay.CalculationFormula', {
  dosageCalculation: null,
  dosageCalculationUnit: null,
  doseWithUnits: null,
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
    var contentHtml = "";
    var doseWithUnits = this._getDoseWithUnits();
    if (doseWithUnits && this.dosageCalculation)
    {
      var dosageCalculationUnitDisplay = this._getDosageCalculationUnit().displayUnit;
      var dosageCalculationPatientUnit =
          view.getUnitsHolder().findKnownUnitByName(this._getDosageCalculationUnit().patientUnit);
      contentHtml += utils.doubleToString(this.dosageCalculation, "n3") + " " + dosageCalculationUnitDisplay;
      if (doseWithUnits.quantity)
      {
        contentHtml += " = " + utils.doubleToString(doseWithUnits.quantity, "n3") + " " + doseWithUnits.quantityUnit;
      }
      if (view.getUnitsHolder().isUnitInMassGroup(dosageCalculationPatientUnit))
      {
        contentHtml += " &divide; " + utils.doubleToString(this.getReferenceData().getWeight(), "n3");
      }
      else
      {
        var bodySurfaceArea = this.getReferenceData().getBodySurfaceArea();
        contentHtml += " &divide; " + utils.doubleToString(bodySurfaceArea, "n3");
      }
      contentHtml += " " + dosageCalculationPatientUnit.getDisplayName() + " &divide; " + view.getDictionary("dose.short");

      return new app.views.medications.common.VerticallyTitledComponent({
        titleText: view.getDictionary("dosage.calculation") + ": ",
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
  },

  /**
   * @returns {Object|null}
   * @private
   */
  _getDoseWithUnits: function()
  {
    return this.doseWithUnits;
  },

  /**
   * @returns {Object|null}
   * @private
   */
  _getDosageCalculationUnit: function()
  {
    return this.dosageCalculationUnit;
  }
});