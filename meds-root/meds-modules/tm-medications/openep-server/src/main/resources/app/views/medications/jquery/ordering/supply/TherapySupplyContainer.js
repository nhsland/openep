Class.define('app.views.medications.ordering.supply.TherapySupplyContainer', 'tm.jquery.Container', {
  cls: 'therapy-supply-container',
  scrollable: 'visible',
  view: null,

  required: true,
  whenNeededSupported: true,

  _whenNeededButton: null,
  _durationField: null,
  _durationUnitSelectBox: null,
  _quantityField: null,
  _controlledDrugSupply: null,
  _medicationData: null, /** used by controlled drug supply dialog, when available, otherwise empty */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._controlledDrugSupply = [];
    this._buildGui();
  },

  /**
   * @return {boolean} true, if the quantity or duration input is required, otherwise false. Determines the behaviour of the
   * form validations returned by {@link #getFormValidations}. Does not apply to controlled drug supply, which is always
   * required.
   */
  isRequired: function()
  {
    return this.required === true;
  },

  /**
   * @returns {boolean}
   */
  isWhenNeededSupported: function()
  {
    return this.whenNeededSupported === true;
  },

  /**
   * Sets available options based on provided medication data. Should be called before calling {@link #setDispenseDetails}.
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setOptionsByMedication: function(medicationData)
  {
    this._quantityField.setUnit(medicationData.getSupplyUnit());
    this._applyQuantityFieldVisibility(!medicationData.isControlledDrug());
    this._medicationData = medicationData;
  },

  /**
   * @return {app.views.medications.common.dto.DispenseDetails}
   */
  getDispenseDetails: function()
  {
    var durationUnitSelectionId = this._durationUnitSelectBox.getSelections()[0].id;
    var durationValue = parseInt(this._durationField.getValue());

    if (durationValue)
    {
      if (durationUnitSelectionId === 'months')
      {
        durationValue = durationValue * 30;
      }
      else if (durationUnitSelectionId === 'weeks')
      {
        durationValue = durationValue * 7;
      }
    }

    var quantityValue = parseInt(this._quantityField.getValue());

    return !!durationValue || !!quantityValue || this._controlledDrugSupply.length > 0 ?
        new app.views.medications.common.dto.DispenseDetails({
          daysDuration: !!durationValue ? durationValue : null,
          quantity: !!quantityValue ? quantityValue : null,
          unit: this._quantityField.getUnit(),
          controlledDrugSupply: tm.jquery.Utils.isArray(this._controlledDrugSupply) ?
              this._controlledDrugSupply.slice(0) :
              []
        }) :
        null
  },

  /**
   * Sets the dispense details. If no details are provided the values are cleared, apart from the quantity unit which is
   * presumably set by the previous call to {@link #setOptionsByMedication}. If the details are present, the quantity unit is
   * set regardless of what was configured by the medication. This ensures the unit matches the entered quantity regardless
   * of any changes made on the medication after the therapy was saved.
   * @param {app.views.medications.common.dto.DispenseDetails|null|undefined} details
   */
  setDispenseDetails: function(details)
  {
    this._durationField.setValue(!!details ? details.getDaysDuration() : null, true);
    this._quantityField.setValue(!!details ? details.getQuantity() : null, true);
    this._controlledDrugSupply = !!details ? details.controlledDrugSupply.slice(0) : [];
    if (!!details)
    {
      this._quantityField.setUnit(details.getUnit());
    }
  },

  /**
   * @return {boolean}
   */
  getWhenNeeded: function()
  {
    return this.isWhenNeededSupported() ? this._whenNeededButton.isPressed() : false;
  },

  /**
   * @param {boolean} [whenNeeded=false]
   */
  setWhenNeeded: function(whenNeeded)
  {
    if (this.isWhenNeededSupported())
    {
      this._whenNeededButton.setPressed(whenNeeded === true);
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {Array<tm.jquery.FormField>} representing the validation rules. If {@link #isRequired} is true, and the
   * medication is not a controlled drug, the duration or quantity has to be set. If the medication is a controlled drug,
   * the controlled drug supply quantity must be defined (with the specific dialog) regardless of the {@link #isRequired}
   * value and the duration is always optional. Either way, if the duration or quantity field has a value it has to be a
   * integer larger than 0.
   */
  getFormValidations: function()
  {
    var self = this;
    var formFields = [];

    formFields.push(new tm.jquery.FormField({
      component: this._durationField,
      required: false,
      validation: {
        type: 'local',
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary('value.must.be.numeric.not.zero'),
            isValid: function(value)
            {
              return self._controlledDrugDetailsButton.isHidden() ?
                  self._isDurationAndQuantityInputValid() :
                  (!value || !!value && app.views.medications.MedicationUtils.isStringPositiveInteger(value));
            }
          })
        ]
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: this._quantityField.getInputField(),
      required: false,
      validation: {
        type: 'local',
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary('value.must.be.numeric.not.zero'),
            isValid: function()
            {
              return self._isDurationAndQuantityInputValid();
            }
          })
        ]
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: this._controlledDrugDetailsButton,
      required: true,
      validation: {
        type: 'local',
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary("field.value.is.required"),
            /**
             * @param {Array} value
             * @return {boolean}
             */
            isValid: function(value)
            {
              return self._controlledDrugDetailsButton.isHidden() || tm.jquery.Utils.isArray(value) && value.length > 0;
            }
          })
        ]
      },
      componentValueImplementationFn: function()
      {
        return self._controlledDrugSupply;
      }
    }));

    return formFields;
  },

  /**
   * @param {boolean} [keepDuration=false]
   */
  clear: function(keepDuration)
  {
    this._applyQuantityFieldVisibility(true);
    if (this.isWhenNeededSupported())
    {
      this._whenNeededButton.setPressed(false, true);
    }
    if (keepDuration !== true)
    {
      this._durationField.setValue(null, true);
      var durationOptions = this._durationUnitSelectBox.getOptions();
      this._durationUnitSelectBox.setSelections(durationOptions.length > 0 ? [durationOptions[0].value] : null, true);
    }
    this._quantityField.setValue(null, true);
    this._quantityField.setUnit(null);
    this._controlledDrugSupply = [];
    this._medicationData = null; // only required for controlled drugs
  },

  _buildGui: function()
  {
    var view = this.getView();

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));

    /** Using a TextField instead of {@link tm.jquery.NumberField} because the {@link tm.jquery.NumberField#getValue) returns
     * invalid input (text) as a null value, which doesn't work well when the input is optional, since we can't warn the user
     * of a invalid input. */
    this._durationField = new tm.jquery.TextField({
      width: 70,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var durationUnitOptions = this._createDurationUnitOptions();
    this._durationUnitSelectBox = new tm.jquery.SelectBox({
      cls: 'duration-unit',
      liveSearch: false,
      options: durationUnitOptions,
      selections: durationUnitOptions.length > 0 ? [durationUnitOptions[0]] : [],
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: 'stretch',
      allowSingleDeselect: false,
      defaultValueCompareToFunction: this._defaultSelectBoxValueCompareToFunction,
      defaultTextProvider: this._defaultSelectBoxTextProvider
    });

    var durationWrapper = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('medication.supply.duration'),
      scrollable: 'visible',
      contentComponent: new tm.jquery.Container({
        scrollable: 'visible',
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')

      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    durationWrapper.getContentComponent().add(this._durationField);
    durationWrapper.getContentComponent().add(this._durationUnitSelectBox);

    this._quantityField = new app.views.medications.ordering.supply.SupplyQuantityComponent({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._controlledDrugDetailsButton = new tm.jquery.Button({
      cls: 'controlled-drug-details-button',
      hidden: true,
      type: 'link',
      alignSelf: 'center',
      text: this.getView().getDictionary('controlled.drug.details'),
      handler: this._onControlledDrugDetailsButtonClick.bind(this)
    });

    if (this.isWhenNeededSupported())
    {
      var flexEndWrapper = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create('flex-end', 'center', 0),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
      });
      this._whenNeededButton = new tm.jquery.ToggleButton({
        cls: 'when-needed-button',
        text: this.view.getDictionary('when.needed.short')
      });
      this._whenNeededButton.setTooltip(
          app.views.medications.MedicationUtils.createTooltip(
              view.getDictionary('when.needed'),
              'left',
              view));
      flexEndWrapper.add(this._whenNeededButton);
    }

    this.add(durationWrapper);
    this.add(this._quantityField);
    this.add(this._controlledDrugDetailsButton);
    if (this.isWhenNeededSupported())
    {
      this.add(flexEndWrapper);
    }
  },

  _defaultSelectBoxValueCompareToFunction: function(value1, value2)
  {
    return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
        === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
  },

  _defaultSelectBoxTextProvider: function(selectBox, index, option)
  {
    return option.getValue().title;
  },

  /**
   * @return {Array<tm.jquery.SelectBox.createOption>}
   * @private
   */
  _createDurationUnitOptions: function()
  {
    var view = this.getView();
    var durationUnits = [];

    durationUnits.push({
      id: 'days',
      title: view.getDictionary('days')
    });
    durationUnits.push({
      id: 'weeks',
      title: view.getDictionary('weeks')
    });
    durationUnits.push({
      id: 'months',
      title: view.getDictionary('month.plural.lc')
    });

    return durationUnits.map(function(item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
  },

  /**
   * @return {boolean} true if the duration or quantity input is a positive integer larger than 0, or if neither of the
   * fields has a value set and {@link #isRequired} is false, otherwise false.
   * @private
   */
  _isDurationAndQuantityInputValid: function()
  {
    var durationVal = this._durationField.getValue();
    var quantityVal = this._quantityField.getValue();

    return !durationVal && !quantityVal && !this.isRequired() ||
        !!durationVal && app.views.medications.MedicationUtils.isStringPositiveInteger(durationVal) ||
        !!quantityVal && app.views.medications.MedicationUtils.isStringPositiveInteger(quantityVal);
  },

  /**
   * Click event handler for the controlled drug supply details button. Ensures the required VMP medications are loaded
   * if the configuration allows for it (the medication provided to {@link #setOptionsByMedication} was a controlled drug
   * prescribed on the VTM level).
   * @private
   */
  _onControlledDrugDetailsButtonClick: function()
  {
    var self = this;
    this._loadControlledDrugMedicationOptions()
        .then(
            function(medications)
            {
              self._showControlledDrugSupplyDialog(medications)
            }
        )
  },

  /**
   * Returns a list of available medication for which controlled drug supply can be defined. When prescribing from a
   * medication on the VTM level, it's VMP level medications are loaded from the API server, otherwise a single option
   * of the prescribing medication is created.
   * @return {tm.jquery.Promise}
   * @private
   */
  _loadControlledDrugMedicationOptions: function()
  {
    if (!!this._medicationData.isVtm())
    {
      return this.getView()
          .getRestApi()
          .loadVmpMedications(this._medicationData.getVtmId().toString());
    }
    else
    {
      var deferred = tm.jquery.Deferred.create();
      deferred.resolve([
        new app.views.medications.common.dto.FormularyMedication({
          id: this._medicationData.getMedication().getId(),
          name: this._medicationData.getMedication().getDisplayName(),
          formulary: this._medicationData.isFormulary()
        })
      ]);
      return deferred.promise();
    }
  },

  /**
   * @param {Array<app.views.medications.common.dto.FormularyMedication>|undefined|null} medications containing the
   * list of selectable medication for which supply can be defined.
   * @private
   */
  _showControlledDrugSupplyDialog: function(medications)
  {

    var appFactory = this.getView().getAppFactory();
    var self = this;
    var contentContainer = new app.views.medications.ordering.supply.ControlledDrugDetailsDataEntryContainer({
      view: this.getView(),
      supply: this._controlledDrugSupply,
      orderMedicationSupplyUnit: this._medicationData.getSupplyUnit(),
      medicationOptions: medications
    });

    var dialog = appFactory.createDataEntryDialog(
        this.getView().getDictionary('controlled.drug.details'),
        null,
        contentContainer,
        function onResultCallback(result)
        {
          if (!!result && result.isSuccess())
          {
            self._controlledDrugSupply = tm.jquery.Utils.isArray(result.value) ? result.value : [];
          }
        },
        750,
        315);

    dialog.addTestAttribute('controlled-drug-details-dialog');
    dialog.setHideOnEscape(true);
    dialog.show();
  },

  /**
   * Applies the quantity input and unit visibility. When hidden, the control drug details button is shown.
   * @param {boolean} visible
   * @private
   */
  _applyQuantityFieldVisibility: function(visible)
  {
    if (!visible)
    {
      this._quantityField.isRendered() ?
          this._quantityField.hide() :
          this._quantityField.setHidden(true);
      this._controlledDrugDetailsButton.isRendered() ?
          this._controlledDrugDetailsButton.show() :
          this._controlledDrugDetailsButton.setHidden(false);
    }
    else
    {
      this._controlledDrugDetailsButton.isRendered() ?
          this._controlledDrugDetailsButton.hide() :
          this._controlledDrugDetailsButton.setHidden(true);
      this._quantityField.isRendered() ?
          this._quantityField.show() :
          this._quantityField.setHidden(false);

    }
  }
});