Class.define('app.views.medications.ordering.InfusionRatePane', 'tm.jquery.Container', {
  cls: "infusion-rate-pane",
  scrollable: "visible",

  /** configs */
  view: null,
  setInfusionRateTypeFunction: null, //optional
  getInfusionRateTypeBolusFunction: null, //optional
  getInfusionIngredientsFunction: null,
  getContinuousInfusionFunction: null,
  getVolumeSumFunction: null,
  formulaVisibleFunction: null,
  singleIngredientVolumeCalculatedEvent: null, //optional
  durationChangeEvent: null, //optional
  rateFormulaChangeEvent: null, //optional
  changeEvent: null, //optional
  verticalLayout: false,
  formulaFieldFocusLostEvent: null, //optional
  rateFieldFocusLostEvent: null, //optional
  allowZeroRate: false, //optional
  recalculateOnRateChange: true,
  rateUnit: 'ml/h',
  referenceData: null,
  orderingBehaviour: null,

  /** privates */
  firstMedicationData: null,
  /** privates: components */
  durationField: null,
  durationSpacer: null,
  durationUnitLabel: null,
  durationUnitSpacer: null,
  rateField: null,
  rateUnitLabel: null,
  formulaField: null,
  formulaUnitPane: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.referenceData)
    {
      throw Error('referenceData is not defined.');
    }

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    this._buildComponents();
    if (this.verticalLayout)
    {
      this.setLayout(tm.jquery.VFlexboxLayout.create("center", "stretch", 0));
      this._buildVerticalGui();
    }
    else
    {
      this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
      this._buildGui();
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.durationField = app.views.medications.MedicationUtils.createNumberField('n2', 68, "duration-field");
    this.durationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._calculateInfusionValues('DURATION');
      self._fireDurationChangeEvent(component.getValue());
    });

    this.durationSpacer = this._createSpacer();
    this.durationUnitLabel = new app.views.medications.ordering.ValueLabel({
      cls: 'TextData pointer-cursor duration-unit-label',
      width: "25",
      value: app.views.medications.TherapyEnums.knownUnitType.H,
      displayProvider: function(value)
      {
        return self.getView().getUnitsHolder().findKnownUnitByName(value).getDisplayName();
      }
    });
    this.durationUnitLabel.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._toggleDurationUnits();
    });
    this.durationUnitSpacer = this._createSpacer();
    this.rateField = app.views.medications.MedicationUtils.createNumberField('n2', 68, "rate-field");

    if (this.isRecalculateOnRateChange())
    {
      this.rateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        self._calculateInfusionValues('RATE');
        self._fireRateChangeEvent()
      });
    }

    this.rateField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.requestFocusToFormula();
      if (self.rateFieldFocusLostEvent)
      {
        self.rateFieldFocusLostEvent();
      }
    });

    this.rateUnitLabel = new tm.jquery.Container({
      cls: 'TextData rate-unit-label',
      html: app.views.medications.MedicationUtils.getFormattedUnit(this.rateUnit, this.getView())
    });

    this.formulaField = app.views.medications.MedicationUtils.createNumberField('n3', 68, "formula-field");
    this.formulaField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._calculateInfusionValues('FORMULA');
      self._fireRateFormulaChangeEvent();
    });

    if (this.formulaFieldFocusLostEvent)
    {
      this.formulaField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
      {
        if (!self.formulaUnitPane.isHidden())
        {
          self.formulaUnitPane.requestFocus();
        }
        if (self.formulaFieldFocusLostEvent)
        {
          self.formulaFieldFocusLostEvent();
        }
      });
    }

    this.formulaUnitPane = new app.views.medications.ordering.InfusionRateFormulaUnitPane({
      view: this.view,
      referenceData: this.getReferenceData(),
      formulaUnitChangeEvent: function()
      {
        self._calculateFormulaFromRate();
      }
    });
  },

  _buildGui: function()
  {
    this.add(this.durationField);
    this.add(this.durationSpacer);
    this.add(this.durationUnitLabel);
    this.add(this.durationUnitSpacer);
    this.add(this.rateField);
    this.add(this._createSpacer());
    this.add(this.rateUnitLabel);
    this.add(this._createSpacer());
    this.add(this.formulaField);
    this.add(this._createSpacer());
    this.add(this.formulaUnitPane);
  },

  _buildVerticalGui: function()
  {
    var durationContainer = new tm.jquery.Container({
      cls: "duration-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center")
    });
    this.durationField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    durationContainer.add(this.durationField);
    durationContainer.add(this._createSpacer());
    durationContainer.add(this.durationUnitLabel);
    durationContainer.add(this._createVerticalSpacer());
    this.add(durationContainer);
    var rateFieldContainer = new tm.jquery.Container({
      cls: "rate-field-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center")
    });
    this.rateField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    rateFieldContainer.add(this.rateField);
    rateFieldContainer.add(this._createSpacer());
    rateFieldContainer.add(this.rateUnitLabel);
    rateFieldContainer.add(this._createVerticalSpacer());
    this.add(rateFieldContainer);
    var formulaFieldContainer = new tm.jquery.Container({
      cls: "formula-field-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center"),
      scrollable: 'visible'
    });
    this.formulaField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    formulaFieldContainer.add(this.formulaField);
    formulaFieldContainer.add(this._createSpacer());
    formulaFieldContainer.add(this.formulaUnitPane);
    formulaFieldContainer.add(this._createVerticalSpacer());
    this.add(formulaFieldContainer);
  },

  _createSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 5});
  },

  _createVerticalSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 2});
  },

  _showDurationFields: function()
  {
    this.durationField.show();
    this.durationSpacer.show();
    this.durationUnitLabel.show();
    this.durationUnitSpacer.show();
  },

  _hideDurationFields: function()
  {
    this.durationField.hide();
    this.durationSpacer.hide();
    this.durationUnitLabel.hide();
    this.durationUnitSpacer.hide();
  },

  /**
   *
   * @param {String} changeType 'RATE', 'FORMULA'
   * @param {Boolean} preventEvent
   * @private
   */
  _handleFormulaOrRateChange: function(changeType, preventEvent)
  {
    if (changeType === 'RATE')
    {
      if (tm.jquery.Utils.isNumeric(this.rateField.getValue()))
      {
        this._calculateFormulaFromRate(preventEvent);
      }
      else
      {
        this._calculateRateFromFormula(preventEvent);
      }
    }
    else if (changeType === 'FORMULA')
    {
      if (tm.jquery.Utils.isNumeric(this.formulaField.getValue()))
      {
        this._calculateRateFromFormula(preventEvent);
      }
      else
      {
        this._calculateFormulaFromRate(preventEvent);
      }
    }
  },

  /**
   * If calculations are enabled via view property, recalculates infusion values depending on provided change type.
   * @param {String} changeType  'VOLUME', 'DURATION', 'RATE', 'FORMULA'
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _calculateInfusionValues: function(changeType, preventEvent)
  {
    if (!this.getOrderingBehaviour().isDoseCalculationsAvailable())
    {
      return;
    }

    if (this.getContinuousInfusionFunction())
    {
      this._calculateValuesForContinuousTypeInfusion(changeType, preventEvent);
    }
    else
    {
      if (this.durationField.isHidden()) // presume variable rate
      {
        this._calculateValuesForVariableRateTypeInfusion(changeType, preventEvent);
      }
      else
      {
        this._calculateValuesForRateTypeInfusion(changeType, preventEvent);
      }
    }

  },

  /**
   * Ignore duration for variable rate type infusions.
   * Recalculates formula from rate or rate from formula, since volume is always given and never changes.
   * @param {String} changeType  'VOLUME', 'DURATION', 'RATE', 'FORMULA'
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _calculateValuesForVariableRateTypeInfusion: function(changeType, preventEvent)
  {
    this._handleFormulaOrRateChange(changeType, preventEvent);
  },

  /**
   * Calculates third value from the two given (volume, duration, rate).
   * Only recalculates volume for infusions with single ingredient.
   * Recalculates formula from rate and vice-versa.
   * If three values are given (volume, duration, rate), recalculate:
   * - If volume changes, recalculate rate.
   * - If duration changes, recalculate rate.
   * - If rate changes, recalculate duration.
   * @param {String} changeType  'VOLUME', 'DURATION', 'RATE', 'FORMULA'
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _calculateValuesForRateTypeInfusion: function(changeType, preventEvent)
  {
    var enums = app.views.medications.TherapyEnums;
    var dataForCalculation = this._getDataForCalculation();

    var volume = dataForCalculation.volume;
    var infusionIngredients = this.getInfusionIngredientsFunction();
    var duration = this.getDuration(enums.knownUnitType.H);
    var rate = this.rateField.getValue();
    var formula = this.formulaField.getValue();
    if (duration && volume && !tm.jquery.Utils.isNumeric(rate))
    {
      this._calculateRateFromDurationAndVolume(duration, volume, preventEvent);
      this._handleFormulaOrRateChange('RATE', preventEvent);
    }
    else if (!duration && volume && tm.jquery.Utils.isNumeric(rate))
    {
      this._calculateDurationFromRateAndVolume(rate, volume, preventEvent);
      if (!formula)
      {
        this._handleFormulaOrRateChange('RATE', preventEvent);
      }
    }
    else if (duration && !volume && tm.jquery.Utils.isNumeric(rate) && infusionIngredients.length === 1)
    {
      this._calculateVolumeFromDurationAndRate(duration, rate);
      if (!formula)
      {
        this._handleFormulaOrRateChange('RATE', preventEvent);
      }
    }
    else if (duration && !volume && formula && infusionIngredients.length === 1)
    {
      this._calculateVolumeFromDurationAndFormula(duration, formula);
      this._handleFormulaOrRateChange('FORMULA', preventEvent);
    }
    else if (formula && volume && !duration && !tm.jquery.Utils.isNumeric(rate))
    {
      this._handleFormulaOrRateChange('FORMULA', preventEvent);
      this._calculateDurationFromRateAndVolume(this.rateField.getValue(), volume, preventEvent);
    }
    else if (duration && volume && tm.jquery.Utils.isNumeric(rate))
    {
      if (changeType === 'VOLUME')
      {
        this._calculateRateFromDurationAndVolume(duration, volume, preventEvent);
        this._handleFormulaOrRateChange('RATE', preventEvent);
      }
      else if (changeType === 'DURATION')
      {
        this._calculateRateFromDurationAndVolume(duration, volume, preventEvent);
        this._handleFormulaOrRateChange('RATE', preventEvent);
      }
      else if (changeType === 'RATE')
      {
        this._calculateDurationFromRateAndVolume(rate, volume, preventEvent);
        this._handleFormulaOrRateChange('RATE', preventEvent);
      }
      else if (changeType === 'FORMULA')
      {
        this._handleFormulaOrRateChange('FORMULA', preventEvent);
        this._calculateDurationFromRateAndVolume(this.rateField.getValue(), volume, preventEvent);
      }
    }
  },

  /**
   * Duration is hidden (ignored) for continuous infusion.
   * Recalculates rate from formula or formula from rate. If volume changes, recalculate formula from existing rate.
   * @param {String} changeType  'VOLUME', 'DURATION', 'RATE', 'FORMULA'
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _calculateValuesForContinuousTypeInfusion: function(changeType, preventEvent)
  {
    var rate = this.rateField.getValue();

    if (changeType === 'RATE' || changeType === 'FORMULA')
    {
      this._handleFormulaOrRateChange(changeType, preventEvent);
    }
    else if (changeType === 'VOLUME' && tm.jquery.Utils.isNumeric(rate))
    {
      this._handleFormulaOrRateChange('RATE', preventEvent);
    }
  },

  _clearDuration: function()
  {
    this.durationField.setValue(null, true);
    this.durationUnitLabel.setValue(app.views.medications.TherapyEnums.knownUnitType.H, true);
  },

  _toggleDurationUnits: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var durationValue = this.durationField.getValue();
    var isMinutes = this.durationUnitLabel.getValue() === enums.knownUnitType.MIN;
    var newUnits = isMinutes ? enums.knownUnitType.H : enums.knownUnitType.MIN;
    this._setDuration(durationValue, this.durationUnitLabel.getValue(), newUnits, true);
  },

  /**
   * @param {Number} durationValue
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} oldUnits
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} newUnits
   * @param {Boolean} preventEvent
   * @private
   */
  _setDuration: function(durationValue, oldUnits, newUnits, preventEvent)
  {
    var newDurationValue = this.getView().getUnitsHolder().convertKnownUnits(durationValue, oldUnits, newUnits);
    this.durationUnitLabel.setValue(newUnits, true);
    this.durationField.setValue(newDurationValue, true);
    this._fireDurationChangeEvent(newDurationValue, preventEvent);
  },

  /**
   * @param {Number} duration
   * @param {Number} volume
   * @param {Boolean} preventEvent
   * @private
   */
  _calculateRateFromDurationAndVolume: function(duration, volume, preventEvent)
  {
    var rate = Number(volume) / Number(duration);
    this.rateField.setValue(rate, true);
    this._fireRateChangeEvent(preventEvent);
  },

  /**
   * @param {Number} rate
   * @param {Number} volume
   * @param {Boolean} preventEvent
   * @private
   */
  _calculateDurationFromRateAndVolume: function(rate, volume, preventEvent)
  {
    var duration = Number(volume) / Number(rate);
    this._setDuration(
        duration,
        app.views.medications.TherapyEnums.knownUnitType.H,
        this.durationUnitLabel.getValue(),
        preventEvent);
  },

  /**
   * Will only work for single ingredient infusions.
   * @param {Number|String} duration
   * @param {Number|String} rate
   * @private
   */
  _calculateVolumeFromDurationAndRate: function(duration, rate) //for single ingredient only
  {
    var volume = Number(duration) * Number(rate);
    this._fireSingleIngredientVolumeCalculatedEvent(volume);
  },

  /**
   * Will only work for single ingredient infusions.
   * @param {Number|String} duration
   * @param {Number|String} formula
   * @private
   */
  _calculateVolumeFromDurationAndFormula: function(duration, formula)    //for single ingredient only
  {
    if (this.singleIngredientVolumeCalculatedEvent)
    {
      var dataForCalculation = this._getDataForCalculationFromSingleMedicationStrength();

      if (formula &&
          dataForCalculation.mass &&
          dataForCalculation.massUnit &&
          dataForCalculation.volume)
      {
        var calculatedRate = this._calculateRateFromFormulaWithData(
            dataForCalculation.mass,
            dataForCalculation.massUnit,
            dataForCalculation.volume,
            formula);

        if (calculatedRate)
        {
          var volume = Number(duration) * Number(calculatedRate);
          this._fireSingleIngredientVolumeCalculatedEvent(volume);
        }
      }
    }
  },

  _getDataForCalculation: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var infusionIngredients = this.getInfusionIngredientsFunction();

    if (!tm.jquery.Utils.isEmpty(infusionIngredients) && infusionIngredients.length === 1)
    {
      var continuousInfusion = this.getContinuousInfusionFunction();
      if (continuousInfusion)
      {
        return this._getDataForCalculationFromSingleMedicationStrength();
      }
      else
      {
        var volumeInMl;
        if (!tm.jquery.Utils.isEmpty(infusionIngredients[0].quantityDenominator))
        {
          volumeInMl = view.getUnitsHolder().convertToKnownUnit(
              infusionIngredients[0].quantityDenominator,
              infusionIngredients[0].quantityDenominatorUnit,
              enums.knownUnitType.ML
          )
        }
        else
        {
          volumeInMl = view.getUnitsHolder().convertToKnownUnit(
              infusionIngredients[0].quantity,
              infusionIngredients[0].quantityUnit,
              enums.knownUnitType.ML
          )
        }
        return {
          mass: infusionIngredients[0].quantity,
          massUnit: infusionIngredients[0].quantityUnit,
          volume: volumeInMl
        };
      }
    }
    else
    {
      var volumeSum = this.getVolumeSumFunction();
      var orderVolumeSum = 0;
      var ratio = 1;
      for (var i = 0; i < infusionIngredients.length; i++)
      {
        if (!tm.jquery.Utils.isEmpty(infusionIngredients[i].quantityDenominator))
        {
          orderVolumeSum += this.getView().getUnitsHolder().convertToKnownUnit(
              infusionIngredients[i].quantityDenominator,
              infusionIngredients[i].quantityDenominatorUnit,
              enums.knownUnitType.ML);
        }
        else if (view.getUnitsHolder().isUnitInLiquidGroup(infusionIngredients[i].quantityUnit))
        {
          orderVolumeSum += this.getView().getUnitsHolder().convertToKnownUnit(
              infusionIngredients[i].quantity,
              infusionIngredients[i].quantityUnit,
              enums.knownUnitType.ML);
        }
      }

      var therapyMeds = infusionIngredients.filter(function(ingredient)
      {
        return ingredient.medication.isMedication();
      });

      if (therapyMeds.length === 1 && volumeSum)
      {
        if (orderVolumeSum !== 0)
        {
          ratio = volumeSum / orderVolumeSum;
        }
        return {
          mass: therapyMeds[0].quantity * ratio,
          massUnit: therapyMeds[0].quantityUnit,
          volume: volumeSum
        };
      }
      else
      {
        return {
          mass: null,
          massUnit: null,
          volume: volumeSum
        };
      }
    }
  },

  _getDataForCalculationFromSingleMedicationStrength: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    if (this.getFirstMedicationData())
    {
      var definingIngredient = this.getFirstMedicationData().getDefiningIngredient();
      var isLiquidUnit = this.getView().getUnitsHolder().isUnitInLiquidGroup(definingIngredient.getDenominatorUnit());
      if (definingIngredient && isLiquidUnit)
      {
        var volumeInMl;
        if (!tm.jquery.Utils.isEmpty(definingIngredient.getDenominator()))
        {
          volumeInMl = view.getUnitsHolder().convertToKnownUnit(
              definingIngredient.getDenominator(),
              definingIngredient.getDenominatorUnit(),
              enums.knownUnitType.ML
          )
        }
        else
        {
          volumeInMl = view.getUnitsHolder().convertToKnownUnit(
              definingIngredient.getNumerator(),
              definingIngredient.getNumeratorUnit(),
              enums.knownUnitType.ML
          )
        }
        return {
          mass: definingIngredient.getNumerator(),
          massUnit: definingIngredient.getNumeratorUnit(),
          volume: volumeInMl
        };
      }
    }
    return {
      mass: null,
      massUnit: null,
      volume: null
    };
  },

  _calculateRateFromFormula: function(preventEvent)
  {
    var dataForCalculation = this._getDataForCalculation();
    var formula = this.formulaField.getValue();
    if (this.isAllowZeroRate() && formula === 0)
    {
      this.rateField.setValue(0, true);
      this._fireRateChangeEvent(preventEvent);
    }
    else if (formula && dataForCalculation.mass && dataForCalculation.massUnit && dataForCalculation.volume)
    {
      var calculatedRate = this._calculateRateFromFormulaWithData(
          dataForCalculation.mass,
          dataForCalculation.massUnit,
          dataForCalculation.volume,
          formula);

      if (!tm.jquery.Utils.isEmpty(calculatedRate))
      {
        this.rateField.setValue(calculatedRate, true);
        this._fireRateChangeEvent(preventEvent);
      }
    }
  },

  _calculateRateFromFormulaWithData: function(mass, massUnit, volume, formula)  //mass unit = mg
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var formulaUnit = this.formulaUnitPane.getRateFormulaUnit(); // (ug/kg/min)
    var formulaMassUnit = formulaUnit.massUnit;
    var formulaPatientUnit = formulaUnit.patientUnit;
    var formulaTimeUnit = formulaUnit.timeUnit;

    var formulaWithPatientUnit;
    if (view.getUnitsHolder().isUnitInMassGroup(formulaPatientUnit))
    {
      // Presumed patient mass unit is KG
      formulaWithPatientUnit = !!this.getReferenceData().getWeight() ?
          formula * this.getReferenceData().getWeight() : // ug/min
          null;
    }
    else if (view.getUnitsHolder().isUnitInSurfaceGroup(formulaPatientUnit))
    {
      // Presumed patient surface unit is M2
      formulaWithPatientUnit = !!this.getReferenceData().getBodySurfaceArea() ?
          formula * this.getReferenceData().getBodySurfaceArea() :
          null;
    }
    else
    {
      formulaWithPatientUnit = formula;
    }
    var formulaInMassUnit = view.getUnitsHolder().convertToUnit(formulaWithPatientUnit, formulaMassUnit, massUnit); // mg/min
    if (formulaInMassUnit)
    {
      var formulaInMl = formulaInMassUnit * volume / mass; // ml/min
      var timeRatio = view.getUnitsHolder().convertKnownUnits(1, formulaTimeUnit, enums.knownUnitType.H);
      return formulaInMl / timeRatio;  // ml/h
    }
    return null;
  },

  _calculateFormulaFromRateWithData: function(mass, massUnit, volume)  //mass unit = mg
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var rate = this.rateField.getValue(); // ml/h
    var formulaUnit = this.formulaUnitPane.getRateFormulaUnit(); // (ug/kg/min)
    if (formulaUnit)
    {
      var formulaMassUnit = formulaUnit.massUnit;
      var formulaPatientUnit = formulaUnit.patientUnit;
      var formulaTimeUnit = formulaUnit.timeUnit;

      var patientUnitWithFormula;
      if (view.getUnitsHolder().isUnitInMassGroup(formulaPatientUnit))
      {
        // Presumed patient mass unit is KG
        patientUnitWithFormula = !!this.getReferenceData().getWeight() ?
            rate / this.getReferenceData().getWeight() : // ml/kg/h
            null;
      }
      else if (view.getUnitsHolder().isUnitInSurfaceGroup(formulaPatientUnit))
      {
        // Presumed patient surface unit is M2
        patientUnitWithFormula = !!this.getReferenceData().getBodySurfaceArea() ?
            rate / this.getReferenceData().getBodySurfaceArea() :
            null;
      }
      else
      {
        patientUnitWithFormula = rate;
      }
      var rateInMassUnit = patientUnitWithFormula * mass / volume; // mg/kg/h
      var rateInFormulaMassUnit = view.getUnitsHolder().convertToUnit(rateInMassUnit, massUnit, formulaMassUnit); // ug/kg/h
      if (rateInFormulaMassUnit)
      {
        var timeRatio = view.getUnitsHolder().convertKnownUnits(1, enums.knownUnitType.H, formulaTimeUnit);
        return rateInFormulaMassUnit / timeRatio;  // ug/kg/min
      }
    }
    return null;
  },

  _clearFieldValues: function()
  {
    this._clearDuration();
    this.rateField.setValue(null, true);
    this.formulaField.setValue(null, true);
  },

  /**
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _fireRateFormulaChangeEvent: function(preventEvent)
  {
    if (!preventEvent && this.rateFormulaChangeEvent)
    {
      this.rateFormulaChangeEvent();
    }
  },

  /**
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _fireRateChangeEvent: function(preventEvent)
  {
    if (!preventEvent && this.changeEvent)
    {
      this.changeEvent();
    }
  },

  /**
   * @param {Number|null} volume
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _fireSingleIngredientVolumeCalculatedEvent: function(volume, preventEvent)
  {
    var view = this.getView();
    var infusionIngredients =
        tm.jquery.Utils.isFunction(this.getInfusionIngredientsFunction) ?
            this.getInfusionIngredientsFunction() :
            [];
    if (!preventEvent && infusionIngredients.length === 1)
    {
      var volumeInIngredientUnit;
      if (!tm.jquery.Utils.isEmpty(infusionIngredients[0].quantityDenominatorUnit))
      {
        volumeInIngredientUnit = view.getUnitsHolder().convertFromKnownUnit(
            volume,
            app.views.medications.TherapyEnums.knownUnitType.ML,
            infusionIngredients[0].quantityDenominatorUnit
        )
      }
      else
      {
        volumeInIngredientUnit = view.getUnitsHolder().convertFromKnownUnit(
            volume,
            app.views.medications.TherapyEnums.knownUnitType.ML,
            infusionIngredients[0].quantityUnit
        )
      }
      this.singleIngredientVolumeCalculatedEvent(volumeInIngredientUnit);
    }
  },

  /**
   * @param {number|null} duration
   * @param {Boolean} [preventEvent=false]
   * @private
   */
  _fireDurationChangeEvent: function(duration, preventEvent)
  {
    if (!preventEvent && this.durationChangeEvent)
    {
      this.durationChangeEvent(duration);
    }
  },

  /** public methods */

  /**
   * @param {Boolean} [preventChangeEvent=false]
   */
  _calculateFormulaFromRate: function(preventChangeEvent)
  {
    var rate = this.rateField.getValue();
    if (this.isAllowZeroRate() && rate === 0)
    {
      this.formulaField.setValue(0, true);
      this._fireRateFormulaChangeEvent(preventChangeEvent);
    }
    else
    {
      var dataForCalculation = this._getDataForCalculation();
      if (dataForCalculation.mass && dataForCalculation.massUnit && dataForCalculation.volume)
      {
        var calculatedFormula = this._calculateFormulaFromRateWithData(
            dataForCalculation.mass,
            dataForCalculation.massUnit,
            dataForCalculation.volume);

        if (!tm.jquery.Utils.isEmpty(calculatedFormula))
        {
          this.formulaField.setValue(calculatedFormula, true);
          this._fireRateFormulaChangeEvent(preventChangeEvent);
        }
      }
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {boolean} [preventEvent=false]
   */
  setFirstMedicationData: function(medicationData, preventEvent)
  {
    this.firstMedicationData = medicationData;
    this.formulaUnitPane.setMedicationData(medicationData, this.getContinuousInfusionFunction(), preventEvent);
  },

  getInfusionRate: function()
  {
    if (this.getInfusionRateTypeBolusFunction && this.getInfusionRateTypeBolusFunction())
    {
      return app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS;
    }
    return {
      duration: this.getDuration(app.views.medications.TherapyEnums.knownUnitType.MIN),
      rate: this.rateField.getValue(),
      rateUnit: this.rateUnit,
      rateFormula: this.formulaField.getValue(),
      rateFormulaUnit: this.formulaUnitPane.getRateFormulaUnit() && !tm.jquery.Utils.isEmpty(this.formulaField.getValue()) ?
          this.formulaUnitPane.getRateFormulaUnit().displayUnit : null
    }
  },

  /**
   * @param {Number|String|null} rate
   * @param [preventEvent=false]
   */
  setInfusionRate: function(rate, preventEvent)
  {
    if (tm.jquery.Utils.isNumeric(rate) &&
        rate !== app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS)
    {
      this.rateField.setValue(rate, true);
      this._fireRateChangeEvent(preventEvent);
    }

    if (!preventEvent && this.setInfusionRateTypeFunction)
    {
      this.setInfusionRateTypeFunction(rate);
    }
  },

  /**
   * @param {Number|null} rate
   * @param [preventEvent=false]
   */
  setRate: function(rate, preventEvent)
  {
    this.rateField.setValue(rate, preventEvent);
  },

  /**
   * @param {String} rateUnit
   */
  setRateUnit: function(rateUnit)
  {
    this.rateUnit = rateUnit;
    this.rateUnitLabel.setHtml(app.views.medications.MedicationUtils.getFormattedUnit(rateUnit, this.getView()));
  },

  /**
   * Recalculates provided duration to selected duration unit. Presumes duration in minutes. Useful when setting duration
   * directly from the therapy object, since duration on the therapy is always stored in minutes.
   * @param {Number} duration
   * @param {Boolean} preventEvent
   */
  setDurationInMinutes: function(duration, preventEvent)
  {
    this._setDuration(duration,
        app.views.medications.TherapyEnums.knownUnitType.MIN,
        this.durationUnitLabel.getValue(),
        preventEvent)
  },

  /**
   * @param {Boolean} visible
   */
  setDurationVisible: function(visible)
  {
    if (visible)
    {
      this._showDurationFields();
    }
    else
    {
      this._hideDurationFields();
    }
  },

  getInfusionRateFormulaPerHour: function(quantityUnit)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var rateFormula = this.formulaField.getValue();
    var rateFormulaUnit = this.formulaUnitPane.getRateFormulaUnit();
    if (rateFormula && quantityUnit)
    {
      var formulaInMassUnit = view.getUnitsHolder().convertToUnit(rateFormula, rateFormulaUnit.massUnit, quantityUnit);
      if (formulaInMassUnit)
      {
        var timeRatio = view.getUnitsHolder().convertKnownUnits(1, rateFormulaUnit.timeUnit, enums.knownUnitType.H);
        return formulaInMassUnit / timeRatio;
      }
    }
    return null;
  },

  clear: function()
  {
    this.setDurationVisible(true);
    this._clearFieldValues();
    if (this.infusionRateTypePane)
    {
      this.infusionRateTypePane.clear(true);
    }
  },

  calculateInfusionValues: function()
  {
    this._calculateInfusionValues('VOLUME');
  },

  /**
   * @param {Boolean} [preventEvent=false]
   */
  clearInfusionValues: function(preventEvent)
  {
    this._clearFieldValues();
    this._fireSingleIngredientVolumeCalculatedEvent(null, preventEvent);
  },

  clearFieldValues: function()
  {
    this._clearFieldValues(true);
  },

  getInfusionRatePaneValidations: function()
  {
    var self = this;
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      component: self,
      required: true,
      componentValueImplementationFn: function()
      {
        var infusionRate = self.getInfusionRate();
        if (infusionRate === app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS)
        {
          return true;
        }
        else if (infusionRate.rate && (self.durationField.isHidden() || infusionRate.duration))
        {
          return true;
        }
        else if (self.isAllowZeroRate() && infusionRate.rate === 0)
        {
          return true;
        }
        return null;
      }
    }));
    return formFields;
  },

  requestFocus: function()
  {
    if (!this.durationField.isHidden())
    {
      this.durationField.focus();
    }
    else
    {
      this.rateField.focus();
    }
  },

  requestFocusToRate: function()
  {
    this.rateField.focus();
  },

  requestFocusToFormula: function()
  {
    this.formulaField.focus();
  },

  /**
   * @param {Number|null} formula
   * @param [preventEvent=false]
   */
  setFormula: function(formula, preventEvent)
  {
    this.formulaField.setValue(formula, true);
    this._fireRateFormulaChangeEvent(preventEvent);
  },

  /**
   * @param {String|null} formulaUnitDisplay
   * @param {Boolean} [preventEvent=false]
   */
  setFormulaUnitToLabel: function(formulaUnitDisplay, preventEvent)
  {
    this.formulaUnitPane.setFormulaUnitToLabel(formulaUnitDisplay);
    this._calculateFormulaFromRate(preventEvent);
  },

  /**
   * @param {String|null} formulaUnitDisplay
   * @param {Boolean} [preventEvent=false]
   */
  setFormulaUnit: function(formulaUnitDisplay, preventEvent)
  {
    this.formulaUnitPane.setFormulaUnit(formulaUnitDisplay, preventEvent);
  },

  setFormulaVisible: function()
  {
    if (this.formulaVisibleFunction() && this.getOrderingBehaviour().isDoseCalculationsAvailable())
    {
      this.isRendered() ? this.formulaField.show() : this.formulaField.setHidden(false);
      this.isRendered() ? this.formulaUnitPane.show() : this.formulaUnitPane.setHidden(false);
    }
    else
    {
      this.isRendered() ? this.formulaField.hide() : this.formulaField.setHidden(true);
      this.isRendered() ? this.formulaUnitPane.hide() : this.formulaUnitPane.setHidden(true);
    }
  },

  /**
   * Recalculates other values (duration, volume, formulaRate) based on the rate.
   * @param {Boolean} [preventEvent=false]
   */
  recalculateFromRate: function(preventEvent)
  {
    this._calculateInfusionValues('RATE', preventEvent);
  },

  /**
   * @param {Boolean} isContinuousInfusion
   */
  onInfusionRateTypeChanged: function(isContinuousInfusion)
  {
    this.clearInfusionValues();
    this.setDurationVisible(!isContinuousInfusion);
    if (this.getFirstMedicationData())
    {
      this.formulaUnitPane.setMedicationData(this.getFirstMedicationData(), isContinuousInfusion, true);
    }
  },

  /**
   * @returns {boolean}
   */
  isRecalculateOnRateChange: function()
  {
    return this.recalculateOnRateChange === true;
  },

  /**
   * @returns {boolean}
   */
  isAllowZeroRate: function()
  {
    return this.allowZeroRate === true;
  },

  /**
   * @param {boolean} value
   */
  setAllowZeroRate: function(value)
  {
    this.allowZeroRate = value;
  },

  /**
   * @returns {tm.jquery.NumberField}
   */
  getRateField: function()
  {
    return this.rateField;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getFirstMedicationData: function()
  {
    return this.firstMedicationData;
  },

  getRateFormulaUnit: function()
  {
    return this.formulaUnitPane.getRateFormulaUnit();
  },

  /**
   * Returns the duration in specific units.
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} units
   * @returns {Number|null}
   */
  getDuration: function(units)
  {
    return !this.durationField.isHidden() && this.durationField.getValue() ?
        this.getView().getUnitsHolder().convertKnownUnits(
            this.durationField.getValue(),
            this.durationUnitLabel.getValue(),
            units) :
        null;
  },

  /**
   * @returns {tm.jquery.NumberField}
   */
  getDurationField: function()
  {
    return this.durationField;
  },

  /**
   * @returns {tm.jquery.NumberField}
   */
  getFormulaField: function()
  {
    return this.formulaField;
  },

  /**
   * Creates displayable formula calculations for infusion
   * @returns {app.views.medications.ordering.calculationDisplay.RateCalculationFormula}
   */
  getCalculationFormula: function()
  {
    return new app.views.medications.ordering.calculationDisplay.RateCalculationFormula({
      view: this.getView(),
      displayOrderId: 3,
      referenceData: this.getReferenceData(),
      rateFormulaUnit: this.getRateFormulaUnit(),
      dataForCalculation: this._getDataForCalculation(),
      duration: !this.durationField.isHidden() ? this.durationField.getValue() : null,
      rate: this.rateField.getValue(),
      formula: this.formulaField.getValue(),
      durationUnit: this.durationUnitLabel.getValue(),
      continuousInfusion: this.getContinuousInfusionFunction()
    });
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  }
});
