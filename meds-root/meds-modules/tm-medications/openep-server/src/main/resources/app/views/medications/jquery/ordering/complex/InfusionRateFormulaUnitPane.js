Class.define('app.views.medications.ordering.InfusionRateFormulaUnitPane', 'tm.jquery.Container', {
  cls: "infusion-rate-formula-unit-pane",
  scrollable: "visible",

  /** configs */
  view: null,
  formulaUnitChangeEvent: null,
  referenceData: null,
  /** privates */
  defaultValue: null,
  dropdownAppendTo: false,
  /** privates: components */
  formulaUnitCombo: null,
  formulaUnitLabel: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.referenceData)
    {
      throw Error('referenceData is not defined.');
    }

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.formulaUnitCombo = new tm.jquery.SelectBox({
      cls: "formula-unit-combo",
      width: 151,
      appendTo: this.getDropdownAppendTo(),
      allowSingleDeselect: false,
      multiple: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      }
    });
    this.formulaUnitCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.formulaUnitChangeEvent();
    });

    this.formulaUnitLabel = new tm.jquery.Container({
      cls: 'TextData formula-unit-label',
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(this.formulaUnitCombo);
    this.add(this.formulaUnitLabel);
  },

  _getFormulaUnitDisplayValue: function(massUnit, patientUnit, timeUnit)
  {
    var view = this.getView();
    var formulaUnitDisplay = massUnit + "/";
    if (patientUnit)
    {
      formulaUnitDisplay += view.getUnitsHolder().findKnownUnitByName(patientUnit).getDisplayName() + "/";
    }
    return formulaUnitDisplay + view.getUnitsHolder().findKnownUnitByName(timeUnit).getDisplayName();
  },

  /**
   *
   * @param {Number} id
   * @param {String} massUnit
   * @param {String} patientUnit
   * @param {String} timeUnit
   * @param {Boolean} enabled
   * @returns {tm.jquery.selectbox.Option}
   * @private
   */
  _createFormulaUnitOption: function(id, massUnit, patientUnit, timeUnit, enabled)
  {
    var unitDisplayValue = this._getFormulaUnitDisplayValue(massUnit, patientUnit, timeUnit);
    return tm.jquery.SelectBox.createOption({
      id: id,
      displayUnit: unitDisplayValue,
      massUnit: massUnit,
      patientUnit: patientUnit,
      timeUnit: timeUnit
    }, unitDisplayValue, null, null, null, enabled);
  },

  _setGramFormulaComboOptions: function(continuousInfusion, preventEvent)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var unitsHolder = view.getUnitsHolder();

    var mgUnit = unitsHolder.findKnownUnitByName(enums.knownUnitType.MG).getDisplayName();
    var microgramUnit = unitsHolder.findKnownUnitByName(enums.knownUnitType.MICRO_G).getDisplayName();
    var nanogramUnit = unitsHolder.findKnownUnitByName(enums.knownUnitType.NANO_G).getDisplayName();

    var patientWeightUnit = enums.knownUnitType.KG;
    var patientSurfaceUnit = enums.knownUnitType.M2;
    var weightPresent = !!this.getReferenceData().getWeight();
    var surfacePresent = !!this.getReferenceData().getBodySurfaceArea();

    var hourUnit = enums.knownUnitType.H;
    var minuteUnit = enums.knownUnitType.MIN;
    var dayUnit = enums.knownUnitType.D;

    var previousSelectedOptions = this.formulaUnitCombo.getSelections();
    this.formulaUnitCombo.removeAllOptions();

    this.formulaUnitCombo.addOption(this._createFormulaUnitOption(0, mgUnit, patientWeightUnit, hourUnit, weightPresent));
    this.formulaUnitCombo.addOption(this._createFormulaUnitOption(1, mgUnit, null, hourUnit, true));
    this.formulaUnitCombo.addOption(this._createFormulaUnitOption(2, mgUnit, patientWeightUnit, minuteUnit, weightPresent));
    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(this._createFormulaUnitOption(3, mgUnit, patientWeightUnit, dayUnit, weightPresent));
      this.formulaUnitCombo.addOption(
          this._createFormulaUnitOption(4, mgUnit, patientSurfaceUnit, dayUnit, surfacePresent));
    }
    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(5, nanogramUnit, patientWeightUnit, minuteUnit, weightPresent));
    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(6, nanogramUnit, patientWeightUnit, hourUnit, weightPresent));
    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(7, microgramUnit, patientWeightUnit, minuteUnit, weightPresent));
    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(8, microgramUnit, patientWeightUnit, hourUnit, weightPresent));

    this._setDefaultSelection(previousSelectedOptions, preventEvent);
  },

  _setFormulaComboOptions: function(unit, continuousInfusion, preventEvent)
  {
    var enums = app.views.medications.TherapyEnums;

    var patientWeightUnit = enums.knownUnitType.KG;
    var weightPresent = !!this.getReferenceData().getWeight();
    var surfacePresent = !!this.getReferenceData().getBodySurfaceArea();

    var previousSelectedOptions = this.formulaUnitCombo.getSelections();
    this.formulaUnitCombo.removeAllOptions();
    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(0, unit, patientWeightUnit, enums.knownUnitType.H, weightPresent));
    this.formulaUnitCombo.addOption(this._createFormulaUnitOption(1, unit, null, enums.knownUnitType.H, true));

    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(
          this._createFormulaUnitOption(2, unit, patientWeightUnit, enums.knownUnitType.D, weightPresent));
    }

    this.formulaUnitCombo.addOption(
        this._createFormulaUnitOption(3, unit, patientWeightUnit, enums.knownUnitType.MIN, weightPresent));

    if (continuousInfusion === true)
    {
      this.formulaUnitCombo.addOption(
          this._createFormulaUnitOption(4, unit, enums.knownUnitType.M2, enums.knownUnitType.D, surfacePresent));
    }

    this._setDefaultSelection(previousSelectedOptions, preventEvent);
  },

  _findFirstEnabledFormulaUnitOptionValue: function()
  {
    var options = this.formulaUnitCombo.getOptions();
    for (var idx = 0; idx < options.length; idx++) {
      if (options[idx].isEnabled())
      {
        return options[idx].getValue();
      }
    }
    return undefined;
  },

  /**
   * Preselects the value of {@link #formulaUnitCombo}. Finds the first viable default fallback value {@link #defaultValue},
   * then determines if the previously selected option is still available and if so, selects it. Otherwise the
   * {@link #defaultValue} is used, if available. When all else fails, the current selection is cleared.
   * @param previousSelectedOptions
   * @param preventEvent
   * @private
   */
  _setDefaultSelection: function(previousSelectedOptions, preventEvent)
  {
    this.defaultValue = this._findFirstEnabledFormulaUnitOptionValue();

    var preselect = null;
    if (previousSelectedOptions.length > 0)
    {
      var previousSelection = previousSelectedOptions[0];
      for (var i = 0; i < this.formulaUnitCombo.getOptions().length; i++)
      {
        var option = this.formulaUnitCombo.getOptions()[i];
        if (option.getValue().id === previousSelection.id && option.isEnabled())
        {
          preselect = option.getValue();
          break;
        }
      }
    }

    this.formulaUnitCombo.setSelections(
        !!preselect ?
            [preselect] :
            (!!this.defaultValue ? [this.defaultValue] : []),
        preventEvent
    );
  },

  /**
   * @param {String} formulaUnitDisplay
   * @returns {tm.jquery.selectbox.Option}
   * @private
   */
  _getFormulaUnitComboOptionByDisplayValue: function(formulaUnitDisplay)
  {
    var comboOptions = this.formulaUnitCombo.getOptions();
    for (var i = 0; i < comboOptions.length; i++)
    {
      if (!tm.jquery.Utils.isEmpty(comboOptions[i].value) && comboOptions[i].value.displayUnit === formulaUnitDisplay)
      {
        return comboOptions[i];
      }
    }
    return null;
  },

  /**
   * @returns {tm.jquery.selectbox.Option|undefined}
   * @private
   */
  _getFirstSelectableOption: function()
  {
    return this.formulaUnitCombo.getOptions().find(
        /**
         * @param {tm.jquery.selectbox.Option} option
         * @returns {Boolean}
         */
        function findFirstSelectable(option)
        {
          return option.isEnabled()
        });
  },

  /** public methods */

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Boolean} continuousInfusion
   * @param {Boolean} preventEvent
   */
  setMedicationData: function(medicationData, continuousInfusion, preventEvent)
  {
    var numeratorUnit = null;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      numeratorUnit = medicationData.getStrengthNumeratorUnit();
    }

    if (!tm.jquery.Utils.isEmpty(numeratorUnit))
    {
      if (this.getView().getUnitsHolder().isUnitInMassGroup(numeratorUnit))
      {
        this._setGramFormulaComboOptions(continuousInfusion, preventEvent);
      }
      else
      {
        this._setFormulaComboOptions(numeratorUnit, continuousInfusion, preventEvent);
      }
    }
    else
    {
      this._setGramFormulaComboOptions(continuousInfusion, preventEvent);
    }
  },

  getRateFormulaUnit: function()
  {
    if (!this.formulaUnitLabel.isHidden())
    {
      var formulaUnitComboOption = this._getFormulaUnitComboOptionByDisplayValue(this.formulaUnitLabel.getHtml());
      return formulaUnitComboOption ? formulaUnitComboOption.value : null;
    }
    else
    {
      return this.formulaUnitCombo.getSelections()[0];
    }
  },

  /**
   * @param {String} formulaUnitDisplay
   * @param {Boolean} preventEvent
   */
  setFormulaUnit: function(formulaUnitDisplay, preventEvent)
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    appFactory.createConditionTask(
        /**
         * Wait for combo to be ready, then set formula unit by {@link #formulaUnitDisplay}. If formulaUnit does not exist
         * in the combo (can happen if displayName of the unit changes) or if selectbox option for
         * {@link #formulaUnitDisplay} is not enabled, select first enabled option.
         */
        function()
        {
          var formulaUnitComboOption = self._getFormulaUnitComboOptionByDisplayValue(formulaUnitDisplay);
          if (!formulaUnitComboOption || (formulaUnitComboOption && !formulaUnitComboOption.isEnabled()))
          {
            formulaUnitComboOption = self._getFirstSelectableOption();
          }
          self.formulaUnitCombo.setSelections(!!formulaUnitComboOption ? [formulaUnitComboOption.value] : [], preventEvent);
        },
        function()
        {
          return self.formulaUnitCombo.getSelections().length > 0;
        },
        50, 1000
    );
  },

  setFormulaUnitToLabel: function(formulaUnitDisplay)
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    if (formulaUnitDisplay)
    {
      setFormulaAndShowLabel(formulaUnitDisplay);
    }
    else
    {
      appFactory.createConditionTask(
          function()
          {
            setFormulaAndShowLabel(self.formulaUnitCombo.getSelections()[0].displayUnit);
          },
          function()
          {
            return self.formulaUnitCombo.getSelections().length > 0;
          },
          50, 1000
      );
    }

    function setFormulaAndShowLabel(formula)
    {
      self.formulaUnitCombo.hide();
      self.formulaUnitLabel.setHtml(tm.jquery.Utils.escapeHtml(formula));
      self.formulaUnitLabel.show();
    }
  },

  getDefaultValue: function()
  {
    if (!this.defaultValue)
    {
      this._setGramFormulaComboOptions();
    }
    return this.defaultValue;
  },

  /**
   * @see tm.jquery.SelectBox#appendTo
   * @returns {Boolean|Element|String}
   */
  getDropdownAppendTo: function()
  {
    return this.dropdownAppendTo;
  },

  requestFocus: function()
  {
    if(!this.formulaUnitCombo.isHidden())
    {
      this.formulaUnitCombo.focus();
    }
  },

  /**
   * @returns {tm.views.medications.TherapyView}
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
  }
});
