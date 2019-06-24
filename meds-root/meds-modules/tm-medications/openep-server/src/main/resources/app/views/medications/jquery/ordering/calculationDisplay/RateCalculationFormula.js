Class.define('app.views.medications.ordering.calculationDisplay.RateCalculationFormula', 'app.views.medications.ordering.calculationDisplay.CalculationFormula', {
  duration: null,
  durationUnit: null,
  rate: null,
  formula: null,
  rateFormulaUnit: null,
  dataForCalculation: null,
  continuousInfusion: false,
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
   * @returns {Array<app.views.medications.common.VerticallyTitledComponent>}
   */
  getCalculationFormulaDisplay: function()
  {
    var calculations = [];
    var view = this.getView();
    var utils = app.views.medications.MedicationUtils;
    var enums = app.views.medications.TherapyEnums;
    var formulaUnit = this._getRateFormulaUnit();
    var patientUnit = formulaUnit.patientUnit;
    var timeUnit = view.getUnitsHolder().findKnownUnitByName(formulaUnit.timeUnit);
    var dataForCalculation = this._getDataForCalculation();
    var duration = this._getDuration();
    var rate = this._getRate();
    var formula = this._getFormula();
    if (formula && dataForCalculation.mass)
    {
      var durationDisplay = duration ? duration : "1";
      var contentHtml = utils.doubleToString(formula, "n3") + " " + formulaUnit.displayUnit + " = ";

      var massWithRate = dataForCalculation.mass;
      if (this._isContinuousInfusion() && rate)
      {
        massWithRate *= rate;
        if (dataForCalculation.volume)
        {
          massWithRate *= dataForCalculation.volume;
        }
      }
      contentHtml += utils.doubleToString(massWithRate, "n3") + " " + dataForCalculation.massUnit;

      if (view.getUnitsHolder().isUnitInMassGroup(patientUnit))
      {
        contentHtml += " &divide; " + this.getReferenceData().getWeight() + " " +
            view.getUnitsHolder().findKnownUnitByName(patientUnit).getDisplayName();
      }
      else if (view.getUnitsHolder().isUnitInSurfaceGroup(patientUnit))
      {
        var bodySurfaceArea = this.getReferenceData().getBodySurfaceArea();
        contentHtml += " &divide; " + utils.doubleToString(bodySurfaceArea, "n3") + " " + patientUnit;
      }
      contentHtml += " &divide; " + durationDisplay + " " + timeUnit.getDisplayName();
      calculations.push(this._createVerticallyTitledComponent(view.getDictionary("formula") + ": ", contentHtml));
    }
    if (duration && rate && dataForCalculation.volume)
    {
      var rateCalculationHtml = utils.doubleToString(rate, "n3") + " " +
          view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.ML).getDisplayName() + "/" +
          view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.H).getDisplayName() + " = " +
          dataForCalculation.volume + " " +
          view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.ML).getDisplayName() + " &divide; " +
          duration + " " + view.getUnitsHolder().findKnownUnitByName(this._getDurationUnit());

      calculations.push(this._createVerticallyTitledComponent(view.getDictionary("infusion.rate")+ ": ", rateCalculationHtml));
    }
    return calculations;
  },

  /**
   *
   * @param {String} title
   * @param {String} content
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _createVerticallyTitledComponent: function(title, content)
  {
    return new app.views.medications.common.VerticallyTitledComponent({
      titleText: title,
      scrollable: 'visible',
      contentComponent: new tm.jquery.Container({
        scrollable: 'visible',
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
        html: content
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });
  },

  /**
   * @returns {Number|null}
   * @private
   */
  _getDuration: function()
  {
    return this.duration;
  },

  /**
   * @returns {String|null}
   * @private
   */
  _getDurationUnit: function()
  {
    return this.durationUnit;
  },

  /**
   * @returns {Number|null}
   * @private
   */
  _getRate: function()
  {
    return this.rate
  },

  /**
   * @returns {Number|null}
   * @private
   */
  _getFormula: function()
  {
    return this.formula;
  },

  /**
   * @returns {Object|null}
   * @private
   */
  _getRateFormulaUnit: function()
  {
    return this.rateFormulaUnit
  },

  /**
   * @returns {Object|null}
   * @private
   */
  _getDataForCalculation: function()
  {
    return this.dataForCalculation;
  },

  /**
   * @returns {Boolean}
   * @private
   */
  _isContinuousInfusion: function()
  {
    return this.continuousInfusion === true;
  }
});