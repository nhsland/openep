Class.define('app.views.medications.ordering.MaxDoseContainer', 'tm.jquery.Container', {
  cls: "max-dose-container",
  /** configs */
  view: null,
  percentage: null,
  maxValue: null,
  period: null,
  numeratorUnit: null,

  quantity: null,
  timesPerDay: null,
  timesPerWeek: null,
  variable: null,

  maxDoseLabel: null,
  maxDoseImage: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this.maxDoseLabel = new tm.jquery.Label({
      text: tm.jquery.Utils.isEmpty(this.percentage) ? null : this.percentage + '%',
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      hidden: tm.jquery.Utils.isEmpty(this.percentage)
    });

    this.maxDoseImage = new tm.jquery.Image({
      cls: app.views.medications.warnings.WarningsHelpers.getImageClsForMaximumDosePercentage(this.percentage),
      cursor: "pointer",
      width: 16,
      height: 16,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
      hidden: tm.jquery.Utils.isEmpty(this.percentage)
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
    this.add(this.maxDoseImage);
    this.add(this.maxDoseLabel);
  },

  _resetDisplayData: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.percentage))
    {
      this.maxDoseLabel.isRendered() ? this.maxDoseLabel.show() : this.maxDoseLabel.setHidden(false);
      this.maxDoseImage.isRendered() ? this.maxDoseImage.show() : this.maxDoseImage.setHidden(false);

      this.maxDoseLabel.setText(this.percentage + '%');
      this.maxDoseImage.setCls(
          app.views.medications.warnings.WarningsHelpers.getImageClsForMaximumDosePercentage(this.percentage));
      var maximumRecommendedDoseWarning = tm.jquery.Utils.formatMessage(
          this.getView().getDictionary("percentage.of.maximum.recommended.dose"), [
            this.getPercentage(),
            this.getView()
                .getDictionary(this.getPeriod() === app.views.medications.TherapyEnums.maxDosePeriod.DAY ?
                    "maxDosePeriod.DAY" :
                    "maxDosePeriod.WEEK")
                .toLowerCase()
          ]);

      this.maxDoseImage.setTooltip(this._createPopup(maximumRecommendedDoseWarning));
    }
    else
    {
      this.maxDoseLabel.isRendered() ? this.maxDoseLabel.hide() : this.maxDoseLabel.setHidden(true);
      this.maxDoseImage.isRendered() ? this.maxDoseImage.hide() : this.maxDoseImage.setHidden(true);
    }
  },

  _createPopup: function(text)
  {
    var defaultPopoverTooltip = this.view.getAppFactory().createDefaultPopoverTooltip(null, null, new tm.jquery.Label({
      text: text,
      alignSelf: "center",
      padding: '5 10 5 10'
    }));
    defaultPopoverTooltip.setPlacement("bottom");
    return defaultPopoverTooltip;
  },

  /** public methods */

  /**
   * Getters & Setters
   */

  /**
   * @param {Number|null} value
   */
  setTimesPerWeek: function(value)
  {
    this.timesPerWeek = value;
  },

  /**
   * @param {Number|null} value
   */
  setTimesPerDay: function(value)
  {
    this.timesPerDay = value;
  },

  /**
   * @param {Number|null} value
   */
  setQuantity: function(value)
  {
    this.quantity = value;
  },

  /**
   * @param {Number|null} value
   */
  setPercentage: function(value)
  {
    this.percentage = value;
    this._resetDisplayData();
  },

  /**
   * @returns {Number|null}
   */
  getPercentage: function ()
  {
    return this.percentage;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} route
   */
  setMaxDoseValuesAndNumeratorUnit: function(medicationData, route)
  {
    var numeratorUnit = medicationData.getStrengthNumeratorUnit();
    var maxValue = route.getMaxDose().dose;
    var maxDoseUnit = route.getMaxDose().unit;

    if (!tm.jquery.Utils.isEmpty(numeratorUnit) && !tm.jquery.Utils.isEmpty(maxDoseUnit))
    {
      maxValue = this.getView().getUnitsHolder().convertToUnit(maxValue, maxDoseUnit, numeratorUnit);
    }
    this.numeratorUnit = numeratorUnit;
    this.maxValue = maxValue;
    this.period = route.getMaxDose().period;

    if (!tm.jquery.Utils.isEmpty(this.quantity))
    {
      this.calculatePercentage(this.quantity, this.timesPerDay, this.timesPerWeek, this.variable);
    }
  },

  clear: function()
  {
    this.setPercentage(null);
    this.setQuantity(null);
    this.setTimesPerDay(null);
    this.setTimesPerWeek(null);
  },

  /**
   * @param {number|{minNumerator: {number}, maxNumerator: {number}, minDenominator: {number}, maxDenominator: {number}}} quantity
   * @param {number} timesPerDay
   * @param {number} timesPerWeek
   * @param {boolean} variable
   * @returns {number|null}
   */
  calculatePercentage: function(quantity, timesPerDay, timesPerWeek, variable)
  {
    var self = this;
    this.quantity = quantity;
    this.timesPerDay = timesPerDay;
    this.timesPerWeek = timesPerWeek;
    this.variable = variable;

    if (!tm.jquery.Utils.isEmpty(this.timesPerDay) && !tm.jquery.Utils.isEmpty(this.quantity))
    {
      if (this.variable)
      {
        this.timesPerDay = 1;
      }

      if (this.period === app.views.medications.TherapyEnums.maxDosePeriod.DAY)
      {
        this.percentage = Math.ceil(this.timesPerDay * this.quantity / this.maxValue * 100);
      }
      else if (this.period === app.views.medications.TherapyEnums.maxDosePeriod.WEEK)
      {
        if (this.timesPerWeek === 0)
        {
          this.setTimesPerWeek(7);
        }
        this.percentage = Math.ceil((this.timesPerWeek * this.quantity * this.timesPerDay) / this.maxValue * 100);
      }
      this._resetDisplayData();
      return this.percentage;
    }
    else
    {
      self.setPercentage(null);
      return null;
    }
  },

  /**
   * Returns dosage calculation formula display provider, if dosage calculation field is being used.
   * @returns {app.views.medications.ordering.calculationDisplay.MaxDoseCalculationFormula}
   */
  getCalculationFormula: function()
  {
    return new app.views.medications.ordering.calculationDisplay.MaxDoseCalculationFormula({
      view: this.getView(),
      displayOrderId: 2,
      period: this.period,
      percentage: this.percentage,
      quantity: this.quantity,
      timesPerDay: this.timesPerDay,
      timesPerWeek: this.timesPerWeek,
      numeratorUnit: this.numeratorUnit,
      maxValue: this.maxValue
    });
  },

  /**
   * @returns {app.views.medications.TherapyEnums.maxDosePeriod}
   */
  getPeriod: function()
  {
    return this.period;
  },

  /**
   * @returns {Number|null}
   */
  getTimesPerDay: function()
  {
    return this.timesPerDay;
  },

  /**
   * @returns {Number|null}
   */
  getTimesPerWeek: function()
  {
    return this.timesPerWeek;
  },

  /**
   * @returns {Number|null}
   */
  getQuantity: function()
  {
    return this.quantity;
  },

  /**
   * @returns {String|null}
   */
  getNumeratorUnit: function()
  {
    return this.numeratorUnit;
  },

  /**
   * @returns {Number|null}
   */
  getMaxValue: function()
  {
    return this.maxValue;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});
