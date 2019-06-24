Class.define('app.views.medications.ordering.calculationDisplay.CalculatedDosagePane', 'tm.jquery.Container', {
  cls: "calculated-dosage-pane",
  /** configs */
  view: null,
  getDetailedCalculationsFunction: null,
  referenceData: null,
  /** privates */
  /** privates: components */
  calculationDetailsBtn: null,
  calculationsHtml: null,
  _calculationFormulaProviders: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.referenceData)
    {
      throw Error('referenceData is undefined.');
    }
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center"));
    this._calculationFormulaProviders = [];
    this._buildGui();
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /** public methods */
  calculate: function(dosage, dosageUnit, timesPerDay, weightInKg, heightInCm, perHour)
  {
    var appFactory = this.view.getAppFactory();
    var utils = app.views.medications.MedicationUtils;

    var displayItemsList = [];
    if (dosage && dosageUnit && weightInKg)
    {
      var perUnit = perHour ? this.view.getDictionary("hour.accusative") : this.view.getDictionary("dose");

      var dosagePerWeight = this._dosagePerWeight(dosage, weightInKg);
      displayItemsList.push(
          this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerWeight) + ' ' +
              dosageUnit + '/kg/' + perUnit));

      var dosagePerWeightPerDay = null;
      if (perHour)
      {
        dosagePerWeightPerDay = this._dosagePerWeight(dosage, weightInKg, 24);
      }
      else if (timesPerDay)
      {
        dosagePerWeightPerDay = this._dosagePerWeightPerDay(dosage, timesPerDay, weightInKg);
      }
      if (dosagePerWeightPerDay)
      {
        displayItemsList.push(
            this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerWeightPerDay) + ' ' +
                dosageUnit + '/kg/' + this.view.getDictionary("day.lc")));
      }

      if (heightInCm)
      {
        var bodySurfaceArea = this.getReferenceData().getBodySurfaceArea();

        var dosagePerSurface = this._dosagePerSurface(dosage, bodySurfaceArea);
        displayItemsList.push(
            this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerSurface) + ' ' +
                dosageUnit + '/m2/' + perUnit));

        var dosagePerSurfacePerDay = null;
        if (perHour)
        {
          dosagePerSurfacePerDay = this._dosagePerSurface(dosage, bodySurfaceArea, 24);
        }
        else if (timesPerDay)
        {
          dosagePerSurfacePerDay = this._dosagePerSurfacePerDay(dosage, timesPerDay, bodySurfaceArea);
        }
        if (dosagePerSurfacePerDay)
        {
          displayItemsList.push(
              this._getCalculationDisplayItem(utils.getFormattedDecimalNumber(dosagePerSurfacePerDay) + ' ' +
                  dosageUnit + '/m2/' + this.view.getDictionary("day.lc")));
        }
      }
    }
    this.calculationsHtml.setHtml(appFactory.createInlineItemListHtml({block: true, items: displayItemsList}));
  },

  /**
   * @param {tm.jquery.Component|*} calculationFormulaProvider
   */
  registerCalculationFormulaProvider: function(calculationFormulaProvider)
  {
    this._calculationFormulaProviders.push(calculationFormulaProvider);
  },

  /**
   * @param {tm.jquery.Component|*} calculationFormulaProvider
   */
  unregisterCalculationFormulaProvider: function(calculationFormulaProvider)
  {
    var displayProviderIndex = this._calculationFormulaProviders.indexOf(calculationFormulaProvider);
    if (displayProviderIndex > -1)
    {
      this._calculationFormulaProviders.splice(displayProviderIndex, 1);
    }
  },

  clear: function()
  {
    this.calculationsHtml.setHtml("");
  },

  _buildGui: function()
  {
    var self = this;
    this.calculationsHtml = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "5px")
    });
    this.calculationDetailsBtn = new tm.jquery.Button({
      type: 'link',
      cls: 'more-calculations-btn',
      text: this.getView().getDictionary("details"),
      handler: function()
      {
        self._openCalculationDetailsPopup();
      }
    });
    this.add(this.calculationsHtml);
    this.add(this.calculationDetailsBtn);
  },
  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} weightInKg
   * @param {Number} [dosageMultiplier=1]
   * @returns {String}
   * @private
   */
  _dosagePerWeight: function(dosage, weightInKg, dosageMultiplier)
  {
    var dm = tm.jquery.Utils.isNumeric(dosageMultiplier) ? dosageMultiplier : 1;
    
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return app.views.medications.MedicationUtils.doubleToString(dosage.minNumerator * dm / weightInKg, 'n2') +
          ' - ' + app.views.medications.MedicationUtils.doubleToString(dosage.maxNumerator * dm / weightInKg, 'n2');
    }

    return app.views.medications.MedicationUtils.doubleToString(dosage * dm / weightInKg, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} timesPerDay
   * @param {Number} weightInKg
   * @returns {String}
   * @private
   */
  _dosagePerWeightPerDay: function(dosage, timesPerDay, weightInKg)
  {
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return app.views.medications.MedicationUtils.doubleToString((dosage.minNumerator / weightInKg) * timesPerDay, 'n2') +
          ' - ' + app.views.medications.MedicationUtils.doubleToString((dosage.maxNumerator / weightInKg) * timesPerDay, 'n2');
    }

    return app.views.medications.MedicationUtils.doubleToString((dosage / weightInKg) * timesPerDay, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} bodySurfaceArea
   * @param {Number} [dosageMultiplier=1]
   * @returns {String}
   * @private
   */
  _dosagePerSurface: function(dosage, bodySurfaceArea, dosageMultiplier)
  {
    var dm = tm.jquery.Utils.isNumeric(dosageMultiplier) ? dosageMultiplier : 1;

    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return app.views.medications.MedicationUtils.doubleToString(dosage.minNumerator * dm / bodySurfaceArea, 'n2') +
          ' - ' + app.views.medications.MedicationUtils.doubleToString(dosage.maxNumerator * dm / bodySurfaceArea, 'n2');
    }

    return app.views.medications.MedicationUtils.doubleToString(dosage * dm/ bodySurfaceArea, 'n2');
  },

  /**
   * @param {Number|{minNumerator: Number, maxNumerator: Number}} dosage
   * @param {Number} timesPerDay
   * @param {Number} bodySurfaceArea
   * @returns {String}
   * @private
   */
  _dosagePerSurfacePerDay: function(dosage, timesPerDay, bodySurfaceArea)
  {
    if (dosage && (dosage.maxNumerator || dosage.minNumerator))
    {
      return app.views.medications.MedicationUtils.doubleToString((dosage.minNumerator / bodySurfaceArea) * timesPerDay, 'n2') +
          ' - ' +
          app.views.medications.MedicationUtils.doubleToString((dosage.maxNumerator / bodySurfaceArea) * timesPerDay, 'n2');
    }

    return app.views.medications.MedicationUtils.doubleToString((dosage / bodySurfaceArea) * timesPerDay, 'n2');
  },

  _getCalculationDisplayItem: function(text)
  {
    return {text: "<span class='TextData lowercase'>" + text + "</span>"};
  },

  _openCalculationDetailsPopup: function()
  {
    var calculationDetailsContainer = new app.views.medications.ordering.calculationDisplay.CalculationDetailsContainer({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      calculationFormulaProviders: this._calculationFormulaProviders
    });
    var calculationDetailsPopup = this.getView().getAppFactory().createDefaultPopoverTooltip(
        this.getView().getDictionary("details"),
        null,
        calculationDetailsContainer
    );
    calculationDetailsPopup.setPlacement("auto");
    calculationDetailsPopup.setTrigger("manual");
    this.calculationDetailsBtn.setTooltip(calculationDetailsPopup);
    setTimeout(function()
    {
      calculationDetailsPopup.show();
    }, 0);
  }
});
