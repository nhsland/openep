Class.define('app.views.medications.ordering.dosing.DoseContainer', 'tm.jquery.Container', {
  cls: "dose-pane",
  /** configs */
  view: null,
  pack: 'start',
  medicationData: null,
  doseNumerator: null,
  doseDenominator: null,
  denominatorAlwaysVolume: false,
  hideDenominator: false,
  hideUnit: false,
  numeratorChangeEvent: null, //optional
  volumeChangedEvent: null, //optional
  focusLostEvent: null, //optional
  numeratorFocusLostEvent: null, //optional
  denominatorFocusLostEvent: null, //optional
  dosageCalculationFocusLostEvent: null, //optional
  verticalLayout: false,
  /* the following properties allow fine grained control over dosage calculation, but keep in mind they will be ignored
  * if the {@link #orderingBehaviour} disables the functionality completly */
  addDosageCalculationPane: false,
  showDosageCalculation: false,
  addDosageCalcBtn: false,
  showDoseUnitCombos: null,
  showRounding: false,
  doseRangeEnabled: false,
  orderingBehaviour: null,
  referenceData: null,
  /** privates */
  strengthNumeratorUnit: null,
  strengthDenominatorUnit: null,
  /** privates: components */
  numeratorField: null,
  numeratorUnitLabel: null,
  fractionLine: null,
  dosageCalculationFractionLine: null,
  denominatorField: null,
  denominatorUnitLabel: null,
  dosageCalculationField: null,
  dosageCalculationUnitCombo: null,
  dosageCalculationUnitLabel: null,
  numeratorRoundingTooltipAllowed: false,
  denominatorRoundingTooltipAllowed: false,
  _numeratorRoundingTooltipTimer: null,
  _denominatorRoundingTooltipTimer: null,
  dosageRoundingValidationForm: null,
  dosageCalculationBtn: null,
  /** statics */
  LABEL_PADDING: "5 0 0 5",

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

    if (!this.getOrderingBehaviour().isReferenceDataBasedDoseCalculationAvailable())
    {
      this.addDosageCalculationPane = false;
      this.showDosageCalculation = false;
      this.addDosageCalcBtn = false;
    }

    if (this.verticalLayout)
    {
      this.LABEL_PADDING = 0;
    }
    this._buildComponents();
    if (this.verticalLayout)
    {
      this.setLayout(tm.jquery.VFlexboxLayout.create("center", "stretch", 0));
      this._buildVerticalGui();
    }
    else
    {
      this.setLayout(tm.jquery.HFlexboxLayout.create(this.pack, "center"));
      this._buildGui();
    }
    if (this.medicationData)
    {
      this.setMedicationData(this.medicationData);
      this.setDoseNumerator(this.doseNumerator, true);
      if (this.doseDenominator)
      {
        this.setDoseDenominator(this.doseDenominator);
      }
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.dosageRoundingValidationForm = new tm.jquery.Form({
      showTooltips: false,
      onValidationSuccess: function()
      {
        if (!self.numeratorField.isHidden())
        {
          self.numeratorField.setTooltip(null);
        }
        if (!self.denominatorField.isHidden())
        {
          self.denominatorField.setTooltip(null);
        }
      },
      onValidationError: function()
      {
        if (!self.numeratorField.isHidden())
        {
          self._attachDoseNotRoundedTooltip(self.numeratorField);
        }
        if (!self.denominatorField.isHidden())
        {
          self._attachDoseNotRoundedTooltip(self.denominatorField);
        }
        return false;
      }
    });

    this.numeratorField = this.isDoseRangeEnabled() ?
        new app.views.medications.common.RangeField({
          cls: 'numerator-field',
          formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2},
          width: 110
        }) :
        app.views.medications.MedicationUtils.createNumberField('n2', 68, 'numerator-field');

    this.numeratorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._numeratorFieldChangedAction();
      self._applyAfterFocusBorders(component);
    });
    this.numeratorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (!self.denominatorField.isHidden())
      {
        self.denominatorField.focus();
      }
      else if (!self.dosageCalculationField.isHidden())
      {
        self.dosageCalculationField.focus();
      }
      else
      {
        self._setupDosageRoundingValidation();
        self.dosageRoundingValidationForm.submit();
      }
      if (self.focusLostEvent && self.denominatorField.isHidden() && self.dosageCalculationField.isHidden())
      {
        self.focusLostEvent();
      }
      if (self.numeratorFocusLostEvent)
      {
        self.numeratorFocusLostEvent(self);
      }
    });

    this.numeratorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowNumeratorRoundingTooltip();
      if (self.showRounding === true && self._isDoseRoundingPossible())
      {
        var definingIngredient = self.medicationData.getDefiningIngredient();
        if (definingIngredient && definingIngredient.getNumerator())
        {
          var roundedDosages = self._calculateDosageRounding(self.numeratorField.getValue(),
              definingIngredient.getNumerator());
        }

        if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
        {
          if (self.numeratorRoundingTooltipAllowed)
          {
            self._showNumeratorRoundingTooltip(roundedDosages.roundedDoseUp, roundedDosages.roundedDoseDown);
          }
        }
      }
    });

    this.numeratorUnitLabel = new tm.jquery.Container({cls: 'TextData numerator-unit-label', padding: this.LABEL_PADDING});

    this.fractionLine = this._getFractionLine();
    this.dosageCalculationFractionLine = this._getFractionLine();

    this.denominatorField = this.isDoseRangeEnabled() ?
        new app.views.medications.common.RangeField({
          cls: 'denominator-field',
          formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2},
          width: 110
        }) : app.views.medications.MedicationUtils.createNumberField('n2', 68, 'denominator-field');
    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._denominatorFieldChangedAction();
      self._applyAfterFocusBorders(component);

    });
    this.denominatorField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (self.focusLostEvent)
      {
        self.focusLostEvent();
      }
      if (!self.dosageCalculationField.isHidden())
      {
        self.dosageCalculationField.focus();
      }
      else
      {
        self._setupDosageRoundingValidation();
        self.dosageRoundingValidationForm.submit();
      }
      if (self.denominatorFocusLostEvent)
      {
        self.denominatorFocusLostEvent(self);
      }

    });

    this.denominatorField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowDenominatorRoundingTooltip();
      if (self.showRounding === true && self._isDoseRoundingPossible())
      {
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.getDenominator())
        {
          roundedDosages = self._calculateDosageRounding(self.denominatorField.getValue(),
              definingIngredient.getDenominator());
        }
        // noinspection JSObjectNullOrUndefined
        if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
        {
          if (self.denominatorRoundingTooltipAllowed)
          {
            self._showDenominatorRoundingTooltip(roundedDosages.roundedDoseUp, roundedDosages.roundedDoseDown);
          }
        }
      }
    });

    this.denominatorUnitLabel = new tm.jquery.Container({
      cls: 'TextData denominator-unit-label',
      padding: this.LABEL_PADDING
    });

    this.dosageCalculationBtn = new tm.jquery.Button({
      cls: "dosage-calculation-icon dosage-calculation-button",
      alignSelf: "center",
      handler: function()
      {
        self.showDosageCalculationFields();
        self._applyAfterFocusBorders(self.dosageCalculationField);
        self._showDosageCalcUnitComboOrLabel();
        self.setDosageCalculationFieldValue();
        self.requestFocusToDosageCalculation();
        self.dosageCalculationBtn.hide();
      },
      hidden: true
    });
    this.dosageCalculationField = app.views.medications.MedicationUtils.createNumberField('n2', 68, 'dosage-calculation-field');

    this.dosageCalculationField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (self.focusLostEvent)
      {
        self.focusLostEvent();
      }
      if (self.dosageCalculationFocusLostEvent)
      {
        self.dosageCalculationFocusLostEvent(self);
      }
      self._setupDosageRoundingValidation();
      self.dosageRoundingValidationForm.submit();
    });

    this.dosageCalculationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (!tm.jquery.Utils.isEmpty(self.dosageCalculationUnitCombo) && !self.dosageCalculationUnitCombo.isHidden())
      {
        if (self.dosageCalculationUnitCombo.getSelections().length > 0)
        {
          self._dosageCalculationFieldChangedAction(
              self.dosageCalculationUnitCombo.getSelections()[0].doseUnit,
              self.dosageCalculationUnitCombo.getSelections()[0].patientUnit
          );
        }
      }
      else if (!tm.jquery.Utils.isEmpty(self.dosageCalculationUnitLabel) && !self.dosageCalculationUnitLabel.isHidden())
      {
        self._dosageCalculationFieldChangedAction(
            self.dosageCalculationUnitLabel.getData().doseUnit,
            self.dosageCalculationUnitLabel.getData().patientUnit
        );

      }
      self._applyAfterFocusBorders(component);
    });

    this.dosageCalculationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
    {
      self._allowNumeratorOrDenominatorRoundingTooltip();
    });

    this.dosageCalculationUnitCombo = new tm.jquery.SelectBox({
      cls: "dosage-calculation-unit-combo",
      width: 140,
      allowSingleDeselect: false,
      multiple: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      },
      hidden: true
    });
    this.dosageCalculationUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (component.getSelections().length > 0)
      {
        self._calculateDosageCalculationFromFormula(component.getSelections()[0]);
      }
    });
    this.dosageCalculationUnitLabel = new tm.jquery.Container({
      cls: "TextData dosage-calculation-unit-label",
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(this.numeratorField);
    this.add(this.numeratorUnitLabel);
    this.add(this.fractionLine);
    this.add(this.denominatorField);
    this.add(this.denominatorUnitLabel);
    if (this.addDosageCalcBtn === true)
    {
      this.add(this.dosageCalculationBtn);
    }
    if (this.addDosageCalculationPane)
    {
      this.add(this.dosageCalculationFractionLine);
      this.add(this.dosageCalculationField);
      this.add(this.dosageCalculationUnitCombo);
      this.add(this.dosageCalculationUnitLabel);
    }
  },
  _buildVerticalGui: function()
  {
    var numeratorContainer = new tm.jquery.Container({
      cls: "numerator-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5)
    });
    this.numeratorField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    numeratorContainer.add(this.numeratorField);
    numeratorContainer.add(this.numeratorUnitLabel);
    numeratorContainer.add(this._createVerticalSpacer());
    this.add(numeratorContainer);
    var denominatorContainer = new tm.jquery.Container({
      cls: "denominator-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5)
    });
    this.denominatorField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "100%"));
    denominatorContainer.add(this.denominatorField);
    denominatorContainer.add(this.denominatorUnitLabel);
    denominatorContainer.add(this._createVerticalSpacer());
    this.add(denominatorContainer);
  },
  _createVerticalSpacer: function()
  {
    return new tm.jquery.Spacer({type: 'horizontal', size: 2});
  },
  _getFractionLine: function()
  {
    return new tm.jquery.Container({cls: 'TextData dose-pane-fraction-line', html: '/', padding: this.LABEL_PADDING});
  },

  _calculateDosageRounding: function(fieldValue, basicStrength)
  {
    var roundingFactor = this.medicationData.roundingFactor;
    if (!tm.jquery.Utils.isEmpty(fieldValue) && tm.jquery.Utils.isNumeric(fieldValue) &&
        !tm.jquery.Utils.isEmpty(roundingFactor))
    {
      var rounding = basicStrength * roundingFactor;
      var roundedDoseUp = Math.round((Math.ceil(fieldValue / rounding) * rounding) * 10000000000) / 10000000000;
      var roundedDoseDown = Math.round((Math.floor(fieldValue / rounding) * rounding) * 10000000000) / 10000000000;
      var isDoseRounded = false;
      if (fieldValue === roundedDoseUp || fieldValue === roundedDoseDown)
      {
        isDoseRounded = true;
      }
      return {roundedDoseUp: roundedDoseUp, roundedDoseDown: roundedDoseDown, isDoseRounded: isDoseRounded};
    }
    return null;
  },

  /**
   *
   * @param {tm.jquery.Component} selectedField
   * @private
   */
  _attachDoseNotRoundedTooltip: function(selectedField)
  {
    if (!tm.jquery.ClientUserAgent.isTablet())
    {
      selectedField.setTooltip(this._getDoseNotRoundedHintTooltip());
    }
  },

  _getDoseNotRoundedHintTooltip: function()
  {
    return new app.views.medications.MedicationUtils.createTooltip(
        this.view.getDictionary("strength.dose.not.matched"),
        "bottom",
        this.view);
  },

  _validateNumeratorAndDenominator: function()
  {
    if (this.medicationData)
    {
      this._setupDosageRoundingValidation();
      this.dosageRoundingValidationForm.submit();
    }
  },

  _numeratorFieldChangedAction: function()
  {
    var self = this;
    if (this.medicationData)
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var numerator = this.numeratorField.getValue();

      if (tm.jquery.Utils.isNumeric(numerator) && this.showRounding && this._isDoseRoundingPossible())
      {
        this._validateNumeratorAndDenominator();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.getNumerator())
        {
          roundedDosages = this._calculateDosageRounding(numerator, definingIngredient.getNumerator());
        }

        // noinspection JSObjectNullOrUndefined
        if (!tm.jquery.Utils.isEmpty(roundedDosages) && !roundedDosages.isDoseRounded)
        {
          var roundedDoseUp = roundedDosages.roundedDoseUp;
          var roundedDoseDown = roundedDosages.roundedDoseDown;

          if (self.numeratorRoundingTooltipAllowed === true)
          {
            self._showNumeratorRoundingTooltip(roundedDoseUp, roundedDoseDown);
          }
        }
      }

      self.calculateAndSetDoseDenominator(true);

      if (this.volumeChangedEvent)
      {
        this.volumeChangedEvent();
      }

      if (!this.dosageCalculationField.isHidden())
      {
        this.setDosageCalculationFieldValue();
      }
      if (this.numeratorChangeEvent)
      {
        this.numeratorChangeEvent();
      }
    }
  },

  setDosageCalculationFieldValue: function()
  {
    if (!this.dosageCalculationUnitCombo.isHidden())
    {
      if (this.dosageCalculationUnitCombo.getSelections().length > 0)
      {
        this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitCombo.getSelections()[0]);
      }
      else if (this.dosageCalculationUnitCombo.getOptions().length > 0)
      {
        this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitCombo.getOptions()[0].value);
      }
    }
    else if (!this.dosageCalculationUnitLabel.isHidden())
    {
      this._calculateDosageCalculationFromFormula(this.dosageCalculationUnitLabel.getData());
    }
  },

  _denominatorFieldChangedAction: function()
  {
    var self = this;
    if (this.medicationData)
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var denominator = this.denominatorField.getValue();
      var isDenominatorRange = denominator instanceof app.views.medications.common.dto.Range;

      if (tm.jquery.Utils.isNumeric(denominator) && this.showRounding && this._isDoseRoundingPossible() &&
          !tm.jquery.Utils.isEmpty(definingIngredient.getDenominator()))
      {
        this._setupDosageRoundingValidation();
        this.dosageRoundingValidationForm.submit();
        var roundedDosages = null;
        if (definingIngredient && definingIngredient.getDenominator())
        {
          roundedDosages = this._calculateDosageRounding(this.denominatorField.getValue(),
              definingIngredient.getDenominator());
        }

        // noinspection JSObjectNullOrUndefined
        if (!tm.jquery.Utils.isEmpty(roundedDosages) && !roundedDosages.isDoseRounded)
        {
          var roundedDoseUp = roundedDosages.roundedDoseUp;
          var roundedDoseDown = roundedDosages.roundedDoseDown;
          if (self.denominatorRoundingTooltipAllowed)
          {
            self._showDenominatorRoundingTooltip(roundedDoseUp, roundedDoseDown);
          }
        }
      }

      if (!this.numeratorField.isHidden())
      {
        if (definingIngredient)
        {
          if (!tm.jquery.Utils.isEmpty(definingIngredient.getNumerator()) &&
              !tm.jquery.Utils.isEmpty(definingIngredient.getDenominator()))
          {
            if (isDenominatorRange)
            {
              var numeratorRange = app.views.medications.common.dto.Range.createStrict(
                Math.round((definingIngredient.getNumerator() / definingIngredient.getDenominator() *
                    denominator.getMin()) * 10000000000) / 10000000000,
                Math.round((definingIngredient.getNumerator() / definingIngredient.getDenominator() *
                    denominator.getMax()) * 10000000000) / 10000000000
              );
              this.numeratorField.setValue(numeratorRange);
            }
            else if (tm.jquery.Utils.isNumeric(denominator))
            {
              var numerator = Math.round(
                      (definingIngredient.getNumerator() / definingIngredient.getDenominator() * denominator) *
                      10000000000) / 10000000000;
              this.numeratorField.setValue(numerator);
            }
            else if (!tm.jquery.Utils.isNumeric(denominator))
            {
              this._clearNumerator();
            }
          }
        }
        else if (this.medicationData.getMedication() && !tm.jquery.Utils.isNumeric(denominator) && !isDenominatorRange)
        {
          this._clearNumerator()
        }
      }
      else if (this.volumeChangedEvent)
      {
        this.volumeChangedEvent();
      }
    }
  },

  _showNumeratorRoundingTooltip: function(roundedDoseUp, roundedDoseDown)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();
    this._attachDoseNotRoundedTooltip(this.denominatorField);

    var tooltipButtonsContainer = new tm.jquery.Container({
      cls: "rounding-tooltip-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 5)
    });
    var roundingLabel = new tm.jquery.Container({
      cls: "PopupHeading1 rounding-tooltip-heading",
      html: this.view.getDictionary("rounding"),
      alignSelf: "center"
    });
    tooltipButtonsContainer.add(roundingLabel);
    var roundUpButton = new tm.jquery.Button({
      cls: "btn-flat button-align-left",
      iconCls: 'icon-round-up-arrow',
      alignSelf: 'stretch',
      text: roundedDoseUp + " " + self.numeratorUnitLabel.getHtml(),
      handler: function()
      {
        self.numeratorField.setValue(roundedDoseUp);
        self.numeratorField.focus();
      }
    });
    tooltipButtonsContainer.add(roundUpButton);

    if (roundedDoseDown > 0)
    {
      var roundDownButton = new tm.jquery.Button({
        cls: "btn-flat button-align-left",
        iconCls: 'icon-round-down-arrow',
        alignSelf: 'stretch',
        text: roundedDoseDown + " " + self.numeratorUnitLabel.getHtml(),
        handler: function()
        {
          self.numeratorField.setValue(roundedDoseDown);
          self.numeratorField.focus();
        }
      });
      tooltipButtonsContainer.add(roundDownButton);
    }

    var numeratorRoundingTooltip = appFactory.createDefaultPopoverTooltip(null, null, tooltipButtonsContainer);
    numeratorRoundingTooltip.setPlacement("bottom");
    numeratorRoundingTooltip.setTrigger("manual");

    numeratorRoundingTooltip.onHide = function()
    {
      // can't figure out why onHide executes as soon as we set the tooltip, even before it's shown for the first time
      // so double check if the field has lost focus before replacing the tooltip
      if (!$(self.numeratorField.getDom()).is(":focus") && numeratorRoundingTooltip)
      {
        numeratorRoundingTooltip = null;
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var calculatedDosages = null;
        if (definingIngredient && definingIngredient.getNumerator())
        {
          calculatedDosages = self._calculateDosageRounding(
              self.numeratorField.getValue(),
              definingIngredient.getNumerator()
          );
        }
        self.numeratorField.setTooltip(null);
        // noinspection JSObjectNullOrUndefined
        if (!tm.jquery.Utils.isEmpty(calculatedDosages) && calculatedDosages.isDoseRounded === false)
        {
          self._attachDoseNotRoundedTooltip(self.numeratorField);
        }
      }
    };
    this.numeratorField.setTooltip(numeratorRoundingTooltip);

    setTimeout(function()
    {
      if (self.numeratorRoundingTooltipAllowed &&
          numeratorRoundingTooltip && self.numeratorField.getTooltip() === numeratorRoundingTooltip)
      {
        numeratorRoundingTooltip.show();
      }
    }, 0);
  },

  _showDenominatorRoundingTooltip: function(roundedDoseUp, roundedDoseDown)
  {
    var self = this;
    var appFactory = self.view.getAppFactory();
    this._attachDoseNotRoundedTooltip(this.numeratorField);

    var tooltipButtonsContainer = new tm.jquery.Container({
      cls: "rounding-tooltip-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 5)
    });
    var roundingLabel = new tm.jquery.Container({
      cls: "PopupHeading1 rounding-tooltip-heading",
      html: this.view.getDictionary("rounding"),
      alignSelf: "center"
    });
    tooltipButtonsContainer.add(roundingLabel);
    var roundUpButton = new tm.jquery.Button({
      cls: "btn-flat button-align-left",
      iconCls: 'icon-round-up-arrow',
      alignSelf: 'stretch',
      text: roundedDoseUp + " " + self.denominatorUnitLabel.getHtml(),
      handler: function()
      {
        self.denominatorField.setValue(roundedDoseUp);
        self.denominatorField.focus();
      }
    });
    tooltipButtonsContainer.add(roundUpButton);

    if (roundedDoseDown > 0)
    {
      var roundDownButton = new tm.jquery.Button({
        cls: "btn-flat button-align-left",
        iconCls: 'icon-round-down-arrow',
        alignSelf: 'stretch',
        text: roundedDoseDown + " " + self.denominatorUnitLabel.getHtml(),
        handler: function()
        {
          self.denominatorField.setValue(roundedDoseDown);
          self.denominatorField.focus();
        }
      });
      tooltipButtonsContainer.add(roundDownButton);
    }

    var denominatorRoundingTooltip = appFactory.createDefaultPopoverTooltip(null, null, tooltipButtonsContainer);
    denominatorRoundingTooltip.setPlacement("bottom");
    denominatorRoundingTooltip.setTrigger("manual");

    denominatorRoundingTooltip.onHide = function()
    {
      // can't figure out why onHide executes as soon as we set the tooltip, even before it's shown for the first time
      // so double check if the field has lost focus before replacing the tooltip
      if (!$(self.denominatorField.getDom()).is(":focus") && denominatorRoundingTooltip)
      {
        denominatorRoundingTooltip = null;
        var definingIngredient = self.medicationData.getDefiningIngredient();
        var calculatedDosages = null;
        if (definingIngredient && definingIngredient.getDenominator())
        {
          calculatedDosages = self._calculateDosageRounding(self.denominatorField.getValue(),
              definingIngredient.getDenominator());
        }
        self.denominatorField.setTooltip(null);
        // noinspection JSObjectNullOrUndefined
        if (!tm.jquery.Utils.isEmpty(calculatedDosages) && calculatedDosages.isDoseRounded === false)
        {
          self._attachDoseNotRoundedTooltip(self.denominatorField);
        }
      }
    };
    this.denominatorField.setTooltip(denominatorRoundingTooltip);

    setTimeout(function()
    {
      if (self.denominatorRoundingTooltipAllowed &&
          denominatorRoundingTooltip && self.denominatorField.getTooltip() === denominatorRoundingTooltip)
      {
        denominatorRoundingTooltip.show();
      }
    }, 0);
  },

  /**
   * @param {String} selectedDoseUnit
   * @param {app.views.medications.TherapyEnums.knownUnitType.KG|app.views.medications.TherapyEnums.knownUnitType.M2|*} selectedPatientUnit
   * @private
   */
  _dosageCalculationFieldChangedAction: function(selectedDoseUnit, selectedPatientUnit)
  {
    var view = this.getView();
    if (this.medicationData)
    {
      var doseWithUnit = this._getDoseWithUnit();
      var dosageCalculationFieldValue = this.dosageCalculationField.getValue();

      if (tm.jquery.Utils.isNumeric(dosageCalculationFieldValue))
      {
        var dosageCalculationInDefiningUnit = view.getUnitsHolder().convertToUnit(
            dosageCalculationFieldValue, selectedDoseUnit, doseWithUnit.doseUnit);
        var numerator = null;
        if (view.getUnitsHolder().isUnitInMassGroup(selectedPatientUnit))
        {
          // Presumed patient mass unit is KG
          if (!!this.getReferenceData().getWeight())
          {
            numerator = Math.round((dosageCalculationInDefiningUnit * this.getReferenceData().getWeight()) * 10000000000) /
                10000000000;
            this.numeratorField.setValue(numerator);
          }
        }
        else if (view.getUnitsHolder().isUnitInSurfaceGroup(selectedPatientUnit))
        {
          // Presumed patient surface unit is M2
          if (!!this.getReferenceData().getBodySurfaceArea())
          {
            numerator = Math.round((dosageCalculationInDefiningUnit * this.getReferenceData().getBodySurfaceArea()) *
                10000000000) / 10000000000;
            this.numeratorField.setValue(numerator);
          }
        }
      }
      else
      {
        if (!this.numeratorField.isHidden())
        {
          if (this.numeratorField.getValue() != null)
          {
            this.numeratorField.setValue(null);
          }
        }
      }
    }
  },

  _setDosageCalculationComboUnits: function(doseUnit)
  {
    var view = this.getView();
    this.dosageCalculationUnitCombo.removeAllOptions();
    var patientHeight = this.getReferenceData().getHeight();
    var dosageCalculationUnits =
        app.views.medications.MedicationUtils.getDosageCalculationUnitOptions(view, doseUnit, patientHeight);
    if (dosageCalculationUnits.length === 1)
    {
      this.setDosageCalculationUnitLabel(dosageCalculationUnits[0]);
    }
    var selectedId = null;
    var selectedOption = null;
    var setOptionSelected = null;
    var selectBoxOptions = dosageCalculationUnits.map(function(option)
    {
      // Presumed patient mass unit is KG
      selectedId = option.doseUnit === doseUnit && view.getUnitsHolder().isUnitInMassGroup(option.patientUnit) ?
          option.id : selectedId;
      setOptionSelected = option.doseUnit === doseUnit && view.getUnitsHolder().isUnitInMassGroup(option.patientUnit);

      var currentOption = tm.jquery.SelectBox.createOption(option, option.displayUnit, null, null, setOptionSelected);
      if (setOptionSelected)
      {
        selectedOption = currentOption.value;
      }
      return currentOption;
    });
    this.dosageCalculationUnitCombo.addOptions(selectBoxOptions);

    if (!tm.jquery.Utils.isEmpty(selectedOption))
    {
      this.dosageCalculationUnitCombo.setSelections([selectedOption]);
    }
  },

  _calculateDosageCalculationFromFormula: function(selectedDoseUnit)
  {
    var view = this.getView();
    if (!tm.jquery.Utils.isEmpty(selectedDoseUnit))
    {
      var patientUnit = selectedDoseUnit.patientUnit;
      var doseUnit = selectedDoseUnit.doseUnit;
      var doseWithUnit = this._getDoseWithUnit();
      if (!!this.getReferenceData().getWeight() && !!doseWithUnit && !!doseWithUnit.doseUnit &&
          !!doseWithUnit.dose)
      {
        var doseInUnit = view.getUnitsHolder().convertToUnit(
            doseWithUnit.dose, doseWithUnit.doseUnit, doseUnit);
        if (view.getUnitsHolder().isUnitInMassGroup(patientUnit))
        {
          // Presumed patient mass unit is KG
          this.dosageCalculationField.setValue(
              Math.round((doseInUnit / this.getReferenceData().getWeight()) * 10000000000) / 10000000000, true);
        }
        else if (view.getUnitsHolder().isUnitInSurfaceGroup(patientUnit) && !!this.getReferenceData().getBodySurfaceArea())
        {
          // Presumed patient surface unit is M2
          this.dosageCalculationField.setValue(
              Math.round((doseInUnit / this.getReferenceData().getBodySurfaceArea()) * 10000000000) / 10000000000, true);
        }
      }
    }
  },

  /**
   * Returns the dose value as set in {@link #numeratorField} and the unit for the dose from {@link #medicationData}.
   * @typedef {Object} DoseWithUnit
   * @property {app.views.medications.common.dto.Range|number|null} dose The dose value.
   * @property {string|null} doseUnit The doses' unit.
   * @return {DoseWithUnit}
   * @private
   */
  _getDoseWithUnit: function()
  {
    var dose = this.numeratorField.getValue();
    var doseUnit = !tm.jquery.Utils.isEmpty(this.medicationData) ?
        this.medicationData.getStrengthNumeratorUnit() :
        null;

    return {
      dose: dose,
      doseUnit: doseUnit
    };
  },

  _isDoseRoundingPossible: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.medicationData))
    {
      var definingIngredient = this.medicationData.getDefiningIngredient();
      var roundingFactor = this.medicationData.getRoundingFactor();
      if (!tm.jquery.Utils.isEmpty(definingIngredient))
      {
        var strengthNumerator = definingIngredient.getNumerator();
        if (!tm.jquery.Utils.isEmpty(strengthNumerator) && !tm.jquery.Utils.isEmpty(roundingFactor))
        {
          return true;
        }
      }
      return false;
    }
    return false;
  },

  _allowNumeratorOrDenominatorRoundingTooltip: function()
  {
    if (this.denominatorField.isHidden())
    {
      this._allowNumeratorRoundingTooltip();
    }
    else
    {
      this._allowDenominatorRoundingTooltip();
    }
  },

  _allowNumeratorRoundingTooltip: function()
  {
    this.denominatorRoundingTooltipAllowed = false;
    this.numeratorRoundingTooltipAllowed = true;
  },

  _allowDenominatorRoundingTooltip: function()
  {
    this.denominatorRoundingTooltipAllowed = true;
    this.numeratorRoundingTooltipAllowed = false;
  },

  _prepareFields: function()
  {
    var utils = app.views.medications.MedicationUtils;
    this.numeratorField.show();
    this.numeratorUnitLabel.show();
    this.fractionLine.show();
    this.denominatorField.show();
    this.denominatorUnitLabel.show();
    this._handleDosageCalculationVisibility();

    this.numeratorUnitLabel.setHtml(utils.getFormattedUnit(this.strengthNumeratorUnit, this.getView()));

    if (this.strengthDenominatorUnit)
    {
      this.denominatorUnitLabel.setHtml(utils.getFormattedUnit(this.strengthDenominatorUnit, this.getView()));
    }
    else
    {
      this.fractionLine.hide();
      this.denominatorField.hide();
      this.denominatorUnitLabel.hide();
    }

    if (this.hideDenominator)
    {
      this.fractionLine.hide();
      this.denominatorField.hide();
      this.denominatorUnitLabel.hide();
    }
    if (this.hideUnit)
    {
      this.numeratorUnitLabel.hide();
    }
  },

  _applyAfterFocusBorders: function(lastChangedCell)
  {
    var denominatorFieldCls = tm.jquery.Utils.isEmpty(this.denominatorField.getCls()) ? "" : this.denominatorField.getCls().replace(" softer-border", "");
    var numeratorFieldCls = tm.jquery.Utils.isEmpty(this.numeratorField.getCls()) ? "" : this.numeratorField.getCls().replace(" softer-border", "");
    var dosageCalculationFieldCls = tm.jquery.Utils.isEmpty(this.dosageCalculationField.getCls()) ? "" : this.dosageCalculationField.getCls().replace(" softer-border", "");
    if (!this.dosageCalculationField.isHidden())
    {
      this.denominatorField.setCls(denominatorFieldCls + " softer-border");
      this.numeratorField.setCls(numeratorFieldCls + " softer-border");
      this.dosageCalculationField.setCls(dosageCalculationFieldCls + " softer-border");
      lastChangedCell.setCls(lastChangedCell.getCls().replace(" softer-border", ""));
    }
    else
    {
      this.denominatorField.setCls(denominatorFieldCls);
      this.numeratorField.setCls(numeratorFieldCls);
    }
  },

  _showDosageCalcUnitComboOrLabel: function()
  {
    if (this.dosageCalculationUnitCombo.getOptions().length === 1)
    {
      this.dosageCalculationUnitCombo.hide();
      this.dosageCalculationUnitLabel.show();
    }
    else
    {
      this.dosageCalculationUnitCombo.show();
      this.dosageCalculationUnitLabel.hide();
    }
  },

  _hideDosageCalculationFields: function()
  {
    this.dosageCalculationFractionLine.hide();
    this.dosageCalculationField.hide();
    this.dosageCalculationUnitCombo.hide();
    this.dosageCalculationUnitLabel.hide();
  },

  _setupDosageRoundingValidation: function()
  {
    var self = this;
    this.dosageRoundingValidationForm.reset();
    if (!this.numeratorField.isHidden())
    {
      this.dosageRoundingValidationForm.addFormField(new tm.jquery.FormField({
        component: self.numeratorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var definingIngredient = self.medicationData.getDefiningIngredient();
          var roundedDosages = null;
          if (definingIngredient && definingIngredient.getNumerator())
          {
            roundedDosages = self._calculateDosageRounding(self.numeratorField.getValue(),
                definingIngredient.getNumerator());
          }
          // noinspection JSObjectNullOrUndefined
          if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
          {
            return null;
          }
          return true;
        }
      }));
    }
    if (!this.denominatorField.isHidden())
    {
      this.dosageRoundingValidationForm.addFormField(new tm.jquery.FormField({
        component: self.denominatorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var definingIngredient = self.medicationData.getDefiningIngredient();
          var roundedDosages = null;
          if (definingIngredient && definingIngredient.getDenominator())
          {
            roundedDosages = self._calculateDosageRounding(self.denominatorField.getValue(),
                definingIngredient.getDenominator());
          }
          // noinspection JSObjectNullOrUndefined
          if (!tm.jquery.Utils.isEmpty(roundedDosages) && roundedDosages.isDoseRounded === false)
          {
            return null;
          }
          return true;
        }
      }));
    }
  },

  _clearNumerator: function(preventEvent)
  {
    if (this.numeratorField.getValue() != null)
    {
      this.numeratorField.setValue(null, preventEvent === true);
    }
  },

  _clearDenominator: function(preventEvent)
  {
    if (this.denominatorField.getValue() != null)
    {
      this.denominatorField.setValue(null, preventEvent === true);
    }
  },

  _clearDosageCalculation: function(preventEvent)
  {
    if (this.dosageCalculationField.getValue() != null)
    {
      this.dosageCalculationField.setValue(null, preventEvent === true);
    }
  },

  /**
   * @private
   */
  _handleDosageCalculationVisibility: function()
  {
    if (this.isDosageCalculationPossible())
    {
      this._setDosageCalculationComboUnits(this.medicationData.getStrengthNumeratorUnit());
      if (this.addDosageCalcBtn === true)
      {
        this.dosageCalculationBtn.show();
      }
      if (this.showDosageCalculation === true)
      {
        this.showDosageCalculationFields();
        this._showDosageCalcUnitComboOrLabel();
      }
      else
      {
        this._hideDosageCalculationFields();
        this._applyAfterFocusBorders(this.numeratorField);
      }
    }
    else
    {
      this.dosageCalculationBtn.hide();
      this._hideDosageCalculationFields();
      this._applyAfterFocusBorders(this.numeratorField);
    }
  },

  /** public methods */

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this.clear();
    if (medicationData)
    {
      this.strengthDenominatorUnit = this.medicationData.getStrengthDenominatorUnit();
      this.strengthNumeratorUnit = this.medicationData.getStrengthNumeratorUnit();
      this._prepareFields();
    }
  },

  //use this for uncoded medications only (universal forms)
  setUnits: function(numeratorUnit, denominatorUnit)
  {
    this.strengthNumeratorUnit = numeratorUnit;
    this.strengthDenominatorUnit = denominatorUnit;
    this._prepareFields();
  },

  clear: function(keepUnits)
  {
    this._clearNumerator(true);
    this._clearDenominator(true);
    this._clearDosageCalculation(true);
    this.denominatorRoundingTooltipAllowed = false;
    this.numeratorRoundingTooltipAllowed = false;
    this.dosageRoundingValidationForm.reset();
    if (keepUnits !== true)
    {
      this.fractionLine.isRendered() ? this.fractionLine.show() : this.fractionLine.setHidden(false);
      this.numeratorUnitLabel.isRendered() ? this.numeratorUnitLabel.show() : this.numeratorUnitLabel.setHidden(false);
      this.denominatorUnitLabel.isRendered() ? this.denominatorUnitLabel.show() : this.denominatorUnitLabel.setHidden(false);
    }
    this._handleDosageCalculationVisibility();
  },

  /**
   * @return {{quantity: number|undefined, quantityDenominator: string|undefined,
   * doseRange: {minNumerator: number|null, maxNumerator: number|null, minDenominator: number|null,
   * maxDenominator: number|null}}|undefined}
   */
  getDose: function()
  {
    var numeratorValue = !this.numeratorField.isHidden() ? this.numeratorField.getValue() : null;
    var denominatorValue = null;
    if (this.hideDenominator && this.medicationData.getStrengthDenominatorUnit() || !this.denominatorField.isHidden())
    {
      denominatorValue = this.denominatorField.getValue();
    }

    if (numeratorValue instanceof app.views.medications.common.dto.Range ||
        denominatorValue instanceof app.views.medications.common.dto.Range)
    {
      return {
        doseRange: {
          minNumerator: numeratorValue ? numeratorValue.getMin() : null,
          maxNumerator: numeratorValue ? numeratorValue.getMax() : null,
          minDenominator: denominatorValue ? denominatorValue.getMin() : null,
          maxDenominator: denominatorValue ? denominatorValue.getMax() : null
        }
      }
    }

    return {
      quantity: numeratorValue,
      quantityDenominator: denominatorValue
    };
  },

  /**
   * @returns {Object}
   */
  getEmptyDose: function()
  {
    return {
      quantity: null,
      quantityDenominator: null
    };
  },

  getDoseWithUnits: function()
  {
    var dose = this.getDose();
    var doseUnits = this.getDoseUnits();

    dose.quantityUnit = doseUnits.quantityUnit;
    dose.denominatorUnit = doseUnits.denominatorUnit;

    return dose;
  },

  /**
   * @returns {Object}
   */
  getEmptyDoseWithUnits: function()
  {
    var doseUnits = this.getDoseUnits();

    return {
      quantity: null,
      quantityUnit: doseUnits.quantityUnit,
      quantityDenominator: null,
      denominatorUnit: doseUnits.denominatorUnit
    };
  },

  getDoseUnits: function()
  {
    return {
      quantityUnit: !this.numeratorUnitLabel.isHidden() && this.strengthNumeratorUnit ? this.strengthNumeratorUnit : null,
      denominatorUnit: !this.denominatorUnitLabel.isHidden() && this.strengthDenominatorUnit ?
          this.strengthDenominatorUnit :
          null
    }
  },

  getDosageCalculation: function()
  {
    return !this.dosageCalculationField.isHidden() ? this.dosageCalculationField.getValue() : null;
  },

  setDoseDenominator: function(denominator, preventEvent)
  {
    this.denominatorField.setValue(denominator, preventEvent);

    if (preventEvent)
    {
      this._allowNumeratorOrDenominatorRoundingTooltip();
      if (this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(denominator))
      {
        this._validateNumeratorAndDenominator();
      }
    }
  },

  setVolume: function(volume, preventEvent)
  {
    if (this.denominatorField.isHidden() &&
        this.getView().getUnitsHolder().isUnitInLiquidGroup(this.strengthNumeratorUnit))
    {
      this.setDoseNumerator(volume, preventEvent);
    }
    else if (!this.denominatorField.isHidden())
    {
      this.setDoseDenominator(volume, preventEvent);
    }
  },

  setDoseNumerator: function(doseNumerator, preventEvent)
  {
    this.numeratorField.setValue(doseNumerator, preventEvent);

    if (preventEvent)
    {
      this._allowNumeratorOrDenominatorRoundingTooltip();

      if (this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(doseNumerator))
      {
        this._validateNumeratorAndDenominator();
      }
    }
  },

  /**
   * Calculates the dose denominator based on the numerator value and defining ingredient, if set.
   * @param {Boolean} preventEvent
   */
  calculateAndSetDoseDenominator: function(preventEvent)
  {
    var numerator = this.numeratorField.getValue();
    var isNumeratorRange = numerator instanceof app.views.medications.common.dto.Range;
    var definingIngredient = this.medicationData.getDefiningIngredient();

    if (definingIngredient)
    {
      if (!tm.jquery.Utils.isEmpty(definingIngredient.getNumerator()) &&
          !tm.jquery.Utils.isEmpty(definingIngredient.getDenominator()))
      {
        if (isNumeratorRange)
        {
          var denominatorRange = app.views.medications.common.dto.Range.createStrict(
            Math.round((definingIngredient.getDenominator() /
                definingIngredient.getNumerator() * numerator.getMin()) *
                10000000000) / 10000000000,
            Math.round((definingIngredient.getDenominator() /
                definingIngredient.getNumerator() * numerator.getMax()) *
                10000000000) / 10000000000
          );
          this.denominatorField.setValue(denominatorRange, preventEvent);
        }
        else if (tm.jquery.Utils.isNumeric(numerator))
        {
          var denominator = Math.round(
                  (definingIngredient.getDenominator() / definingIngredient.getNumerator() * numerator) *
                  10000000000) / 10000000000;
          this.denominatorField.setValue(denominator, preventEvent);
        }
        else if (!tm.jquery.Utils.isNumeric(numerator))
        {
          this._clearDenominator(preventEvent);
          this._clearDosageCalculation(true);
        }
      }
    }
    else if (this.medicationData.getMedication() && !tm.jquery.Utils.isNumeric(numerator) && !isNumeratorRange)
    {
      this._clearDenominator(preventEvent);
      this._clearDosageCalculation(true);
    }

    if (preventEvent && this.showRounding && this._isDoseRoundingPossible() && tm.jquery.Utils.isNumeric(numerator))
    {
      this._validateNumeratorAndDenominator();
    }
  },

  getDosePaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!this.numeratorField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.numeratorField,
        validation: {
          type: "local",
          validators: [
            new tm.jquery.Validator({
              errorMessage: this.view.getDictionary('value.must.be.numeric.not.zero'),
              isValid: function(value)
              {
                if ((self.isDoseRangeEnabled() && value instanceof app.views.medications.common.dto.Range) ||
                    (tm.jquery.Utils.isNumeric(value) && value > 0))
                {
                  return true;
                }
                // strings are received as null when using tm.jquery.NumberField, numeric values are evaluated above
                return tm.jquery.Utils.isEmpty(value) && !self.orderingBehaviour.isDoseRequired();
              }
            })
          ]
        }
      }));
    }

    if (!this.denominatorField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.denominatorField,
        required: this.orderingBehaviour.isDoseRequired(),
        componentValueImplementationFn: function()
        {
          var value = self.denominatorField.getValue();
          if (!value || value <= 0)
          {
            return null;
          }
          return value;
        }
      }));
    }
    return formFields;
  },

  requestFocusToDose: function()
  {
    if (!this.numeratorField.isHidden())
    {
      this._allowNumeratorRoundingTooltip();
      this.numeratorField.focus();
    }
    else if (!this.denominatorField.isHidden())
    {
      this._allowDenominatorRoundingTooltip();
      this.denominatorField.focus();
    }
  },

  requestFocusToNumerator: function()
  {
    this._allowNumeratorRoundingTooltip();
    this.numeratorField.focus();
    $(this.numeratorField.getDom()).select();
  },

  requestFocusToDosageCalculation: function()
  {
    this._allowNumeratorOrDenominatorRoundingTooltip();
    this.dosageCalculationField.focus();
    $(this.dosageCalculationField.getDom()).select();
  },

  requestFocusToDenominator: function()
  {
    this._allowDenominatorRoundingTooltip();
    this.denominatorField.focus();
    $(this.denominatorField.getDom()).select();
  },

  setPaneEditable: function(editable)
  {
    this.numeratorField.setEnabled(editable);
    this.denominatorField.setEnabled(editable);
    this.dosageCalculationField.setEnabled(editable);
  },

  markAsLateDose: function(late)
  {
    var numeratorCls = this.numeratorField.getCls().replace(" late-dose", "");
    var denominatorCls = this.denominatorField.getCls().replace(" late-dose", "");
    if (late)
    {
      numeratorCls += " late-dose";
      denominatorCls += " late-dose";
    }
    this.numeratorField.setCls(numeratorCls);
    this.denominatorField.setCls(denominatorCls);
  },

  setSharpBorders: function(sharpBorders)
  {
    var numeratorCls = this.numeratorField.getCls().replace(" sharp-borders", "");
    var denominatorCls = this.denominatorField.getCls().replace(" sharp-borders", "");
    if (sharpBorders)
    {
      numeratorCls += " sharp-borders";
      denominatorCls += " sharp-borders";
    }
    this.numeratorField.setCls(numeratorCls);
    this.denominatorField.setCls(denominatorCls);
  },

  setDosageCalculationUnitLabel: function(doseUnit)
  {
    this.dosageCalculationUnitCombo.hide();
    this.dosageCalculationUnitLabel.show();
    if (!tm.jquery.Utils.isEmpty(doseUnit))
    {
      this.dosageCalculationUnitLabel.setData(doseUnit);
      this.dosageCalculationUnitLabel.setHtml(tm.jquery.Utils.escapeHtml(doseUnit.displayUnit));
      this._calculateDosageCalculationFromFormula(doseUnit);
    }
    else
    {
      this.dosageCalculationUnitLabel.setData(null);
      this.dosageCalculationUnitLabel.setHtml("");
    }
  },

  /**
   * @return {boolean}
   */
  isDosageCalculationHidden: function()
  {
    return this.dosageCalculationField.isHidden();
  },

  /**
   * Is dose calculation supported by view property, ordering behaviour and strength numerator unit?
   * @return {boolean}
   */
  isDosageCalculationPossible: function()
  {
    if (this.getOrderingBehaviour().isReferenceDataBasedDoseCalculationAvailable() &&
        this.getOrderingBehaviour().isDoseCalculationsAvailable())
    {
      var utils = app.views.medications.MedicationUtils;
      if (!tm.jquery.Utils.isEmpty(this.medicationData))
      {
        return utils.isUnitSupportedForDosageCalculation(this.medicationData.getStrengthNumeratorUnit());
      }
    }
    return false;
  },

  showDosageCalculationFields: function()
  {
    this.dosageCalculationFractionLine.show();
    this.dosageCalculationField.show();
  },

  suppressDosageCalculationTooltips: function()
  {
    if (this._isDoseRoundingPossible())
    {
      this.numeratorRoundingTooltipAllowed = false;
      this.denominatorRoundingTooltipAllowed = false;
      this._setupDosageRoundingValidation();
      this.dosageRoundingValidationForm.submit();

    }
  },

  /**
   * @returns {boolean}
   */
  isDoseRangeEnabled: function()
  {
    return this.doseRangeEnabled === true;
  },

  /**
   * @returns {app.views.medications.common.RangeField|tm.jquery.NumberField}
   */
  getNumeratorField: function()
  {
    return this.numeratorField;
  },

  /**
   * @returns {app.views.medications.common.RangeField|tm.jquery.NumberField}
   */
  getDenominatorField: function()
  {
    return this.denominatorField;
  },

  /**
   * Returns dosage calculation formula display provider, if dosage calculation field is being used.
   * @returns {app.views.medications.ordering.calculationDisplay.DoseCalculationFormula}
   */
  getCalculationFormula: function()
  {
    var dosageCalculationUnit = null;
    if (!this.dosageCalculationUnitCombo.isHidden())
    {
      if (this.dosageCalculationUnitCombo.getSelections().length > 0)
      {
        dosageCalculationUnit = this.dosageCalculationUnitCombo.getSelections()[0];
      }
      else
      {
        dosageCalculationUnit = this.dosageCalculationUnitCombo.getOptions()[0].value;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.dosageCalculationUnitLabel) && !this.dosageCalculationUnitLabel.isHidden())
    {
      dosageCalculationUnit = this.dosageCalculationUnitLabel.getData();
    }


    return new app.views.medications.ordering.calculationDisplay.DoseCalculationFormula({
      view: this.getView(),
      referenceData: this.getReferenceData(),
      displayOrderId: 1,
      dosageCalculation: !this.dosageCalculationField.isHidden() ? this.dosageCalculationField.getValue(): null,
      dosageCalculationUnit: dosageCalculationUnit,
      doseWithUnits: this.getDoseWithUnits()
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
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  }
});