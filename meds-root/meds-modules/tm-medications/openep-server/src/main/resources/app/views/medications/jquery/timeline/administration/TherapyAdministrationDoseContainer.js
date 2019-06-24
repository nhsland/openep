Class.define('app.views.medications.timeline.administration.TherapyAdministrationDoseContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_VOLUME_CHANGE: new tm.jquery.event.EventType({
      name: 'TherapyAdministrationVolumeChange', delegateName: null
    }),
    EVENT_TYPE_MEDICATION_DOSE_RECALCULATED: new tm.jquery.event.EventType({
      name: 'TherapyAdministrationDoseRecalculated', delegateName: null
    }),
    EVENT_TYPE_DOSE_FOCUS_LOST: new tm.jquery.event.EventType({
      name: 'TherapyAdministrationDoseFocusLost', delegateName: null
    })
  },
  cls: 'therapy-administration-dose-container',
  scrollable: 'visible',
  view: null,
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  administrationType: null,
  administration: null,
  administrations: null,
  medicationData: null,
  prescribedMedicationData: null,
  stopFlow: null,
  therapyDoseTypeEnum: null,

  _rateContainer: null,
  _rateCommentContainer: null,
  _ratePane: null,
  _rateLabel: null,
  _dosePane: null,
  _doseLabel: null,
  _doseContainer: null,
  _doseCommentContainer: null,
  _volumeContainer: null,
  _volumeField: null,
  _volumeCommentContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.registerEventTypes('app.views.medications.timeline.administration.TherapyAdministrationDoseContainer', [
      {
        eventType:
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_DOSE_FOCUS_LOST
      },
      {
        eventType:
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_VOLUME_CHANGE
      },
      {
        eventType:
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_MEDICATION_DOSE_RECALCULATED}
    ]);
    this._buildGui();
  },
  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy|null}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {String} therapyDoseTypeEnum of type {@link app.views.medications.TherapyEnums.therapyDoseTypeEnum}
   */
  setMedicationData: function(medicationData, therapyDoseTypeEnum)
  {
    var enums = app.views.medications.TherapyEnums;

    var complexTherapyWithSingleIngredient =
        this.therapy.isOrderTypeComplex() && this.therapy.getIngredientsList().length === 1;

    var administrationDose;
    if (this.administration)
    {
      administrationDose = this.administration.isAdministrationAdministered() ?
          this.administration.getAdministeredDose() :
          this.administration.getPlannedDose();
    }

    if (therapyDoseTypeEnum === enums.therapyDoseTypeEnum.QUANTITY)
    {
      this._presentMedicationDataForQuantity(medicationData, administrationDose, complexTherapyWithSingleIngredient);
    }
    else if (therapyDoseTypeEnum === enums.therapyDoseTypeEnum.VOLUME_SUM)
    {
      this._presentMedicationDataForVolumeSum(medicationData, administrationDose);
    }
    else if (therapyDoseTypeEnum === enums.therapyDoseTypeEnum.RATE)
    {
      this._presentMedicationDataForRate(medicationData, administrationDose);
    }
    else if (therapyDoseTypeEnum === enums.therapyDoseTypeEnum.RATE_QUANTITY)
    {
      this._presentMedicationDataForRateQuantity(medicationData, administrationDose, complexTherapyWithSingleIngredient);
    }
    else if (therapyDoseTypeEnum === enums.therapyDoseTypeEnum.RATE_VOLUME_SUM)
    {
      this._presentMedicationDataForRateVolumeSum(medicationData, administrationDose);
    }
    if (!this._doseContainer.isHidden())
    {
      this._setDosePaneAlignment();
    }
    if (!this._rateContainer.isHidden())
    {
      this._setRateContainerAlignment();
    }
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  buildTherapyDose: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var therapyDose = new app.views.medications.common.dto.TherapyDose();
    var rateValues = null;
    var doseValues = null;

    if (this.stopFlow)
    {
      var therapy = this.getTherapy();

      if (therapy.isOrderTypeOxygen())
      {
        therapyDose.setNumerator(0);
        therapyDose.setNumeratorUnit(therapy.getFlowRateUnit() ? therapy.getFlowRateUnit()
            : this._ratePane.getFlowRateUnit());
        therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.RATE);
      }
      else
      {
        var isCodedMedication = !tm.jquery.Utils.isEmpty(this.medicationId); //uncoded medication are from universal form
        var doseElement = this._getDoseElementFromTherapy();
        therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.RATE);
        therapyDose.setNumerator(0);
        therapyDose.setNumeratorUnit(doseElement.rateUnit ? doseElement.rateUnit : this._ratePane.getInfusionRate().rateUnit);
        therapyDose.setDenominator(isCodedMedication && !tm.jquery.Utils.isEmpty(doseElement.rateFormula) ? 0 : null);
        therapyDose.setDenominatorUnit(isCodedMedication ? doseElement.rateFormulaUnit : null);
      }
    }
    else if (!this._rateContainer.isHidden() && !this._doseContainer.isHidden()) //TherapyDoseTypeEnum RATE_QUANTITY
    {
      rateValues = this._ratePane.getInfusionRate();
      doseValues = this._dosePane.getDoseWithUnits();

      therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.RATE_QUANTITY);
      therapyDose.setNumerator(rateValues.rate);
      therapyDose.setNumeratorUnit(rateValues.rateUnit);
      therapyDose.setDenominator(!tm.jquery.Utils.isEmpty(rateValues.rateFormula) ? rateValues.rateFormula : null);
      therapyDose.setDenominatorUnit(!tm.jquery.Utils.isEmpty(rateValues.rateFormulaUnit) ?
          rateValues.rateFormulaUnit : null);

      if (tm.jquery.Utils.isEmpty(doseValues.quantity))
      {
        therapyDose.setSecondaryNumerator(doseValues.quantityDenominator);
      }
      else
      {
        therapyDose.setSecondaryNumerator(doseValues.quantity);
        therapyDose.setSecondaryDenominator(doseValues.quantityDenominator);
      }
      therapyDose.setSecondaryNumeratorUnit(doseValues.quantityUnit);
      therapyDose.setSecondaryDenominatorUnit(doseValues.denominatorUnit);
    }
    else if (!this._volumeContainer.isHidden() && !this._rateContainer.isHidden())  //TherapyDoseTypeEnum RATE_VOLUME_SUM
    {
      rateValues = this._ratePane.getInfusionRate();
      therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.RATE_VOLUME_SUM);
      therapyDose.setNumerator(rateValues.rate);
      therapyDose.setNumeratorUnit(rateValues.rateUnit);
      therapyDose.setDenominator(!tm.jquery.Utils.isEmpty(rateValues.rateFormula) ? rateValues.rateFormula : null);
      therapyDose.setDenominatorUnit(!tm.jquery.Utils.isEmpty(rateValues.rateFormulaUnit) ? rateValues.rateFormulaUnit : null);

      therapyDose.setSecondaryNumerator(this._volumeField.getValue());
      therapyDose.setSecondaryNumeratorUnit('ml');
      therapyDose.setSecondaryDenominator(null);
      therapyDose.setSecondaryDenominatorUnit(null);
    }
    else if (!this._doseContainer.isHidden()) // TherapyDoseTypeEnum QUANTITY
    {
      doseValues = this._dosePane.getDoseWithUnits();
      if (tm.jquery.Utils.isEmpty(doseValues.quantity))
      {
        therapyDose.setNumerator(doseValues.quantityDenominator);
      }
      else
      {
        therapyDose.setNumerator(doseValues.quantity);
        therapyDose.setDenominator(doseValues.quantityDenominator);
      }
      therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.QUANTITY);
      therapyDose.setNumeratorUnit(doseValues.quantityUnit);
      therapyDose.setDenominatorUnit(doseValues.denominatorUnit);
    }
    else if (!this._rateContainer.isHidden()) //TherapyDoseTypeEnum RATE
    {
      rateValues = this._ratePane.getInfusionRate();
      therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.RATE);
      therapyDose.setNumerator(rateValues.rate);
      therapyDose.setNumeratorUnit(rateValues.rateUnit);
      therapyDose.setDenominator(!tm.jquery.Utils.isEmpty(rateValues.rateFormula) ? rateValues.rateFormula : null);
      therapyDose.setDenominatorUnit(!tm.jquery.Utils.isEmpty(rateValues.rateFormulaUnit) ? rateValues.rateFormulaUnit : null);
    }
    else if (!this._volumeContainer.isHidden()) //TherapyDoseTypeEnum VOLUME_SUM
    {
      therapyDose.setTherapyDoseTypeEnum(this.therapyDoseTypeEnum || enums.therapyDoseTypeEnum.VOLUME_SUM);
      therapyDose.setNumerator(this._volumeField.getValue());
      therapyDose.setNumeratorUnit('ml');
      therapyDose.setDenominator(null);
      therapyDose.setDenominatorUnit(null);
    }

    return therapyDose;
  },

  applyAdministrationDoseFieldsFocus: function()
  {
    if (!this._doseContainer.isHidden())
    {
      this._dosePane.requestFocusToNumerator();
    }
    else if (!this._rateContainer.isHidden())
    {
      this._ratePane.requestFocus();
    }
    else if (!this._volumeContainer.isHidden())
    {
      this._volumeField.focus();
    }
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getValidations: function()
  {
    var self = this;
    var validations = [];
    if (!this._doseContainer.isHidden())
    {
      this._dosePane.getDosePaneValidations().forEach(
          function(validator)
          {
            validations.push(validator);
          });
    }
    if (!this._rateContainer.isHidden())
    {
      this._ratePane.getInfusionRatePaneValidations().forEach(
          function(validator)
          {
            validations.push(validator);
          });
    }
    if (!this._volumeContainer.isHidden())
    {
      validations.push(new tm.jquery.FormField({
        component: this._volumeField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self._volumeField.getValue();
          if (tm.jquery.Utils.isEmpty(value) || value <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
    return validations;
  },

  /**
   * @returns {Object}
   */
  getDose: function()
  {
    return this._dosePane.isHidden() ? this._dosePane.getEmptyDose() : this._dosePane.getDose();
  },

  getRatePane: function()
  {
    return this._ratePane;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setPrescribedMedicationData: function(medicationData)
  {
    this.prescribedMedicationData = medicationData;
  },

  /**
   * @returns {Boolean}
   */
  isDoseInRange: function()
  {
    if (this._isDoseTypeDoseRange())
    {
      var prescribedDoseRange = this.getTherapy().getDoseElement().doseRange;
      var administrationDose = this._dosePane.getDose();
      return prescribedDoseRange.minNumerator <= administrationDose.quantity &&
          prescribedDoseRange.maxNumerator >= administrationDose.quantity;
    }
    return true;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _setDoseFromTherapy: function(medicationData)
  {
    var doseNumerator = this.getTherapy().getTherapyDose().doseNumerator;
    var doseDenominator = this.getTherapy().getTherapyDose().doseDenominator;

    if (this._hasMedicationChanged(this.prescribedMedicationData, medicationData))
    {
      this._recalculateAndSetDose(
          medicationData,
          doseNumerator,
          this.prescribedMedicationData.getStrengthNumeratorUnit());
    }
    else
    {
      if (!tm.jquery.Utils.isEmpty(doseNumerator))
      {
        this._dosePane.setDoseNumerator(doseNumerator, true);
      }
      if (!tm.jquery.Utils.isEmpty(doseDenominator))
      {
        this._dosePane.setDoseDenominator(doseDenominator, true);
      }
      else
      {
        this._dosePane.calculateAndSetDoseDenominator(true);
      }
    }
  },

  _setVolumeFromTherapy: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.therapy.getVolumeSum()))
    {
      this._volumeField.setValue(this.therapy.getVolumeSum());
    }
  },

  /**
   * Sets rate from dose element on therapy. If possible, rate values are recalculated, otherwise, all available data is set
   * from therapy.
   * @private
   */
  _setRateFromTherapy: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.therapy.getDoseElement()))
    {
      if (!tm.jquery.Utils.isEmpty(this.therapy.getDoseElement().rate))
      {
        this._ratePane.setRate(this.therapy.getDoseElement().rate, true);
      }
      if (!tm.jquery.Utils.isEmpty(this.therapy.getDoseElement().rateFormulaUnit))
      {
        this._ratePane.setFormulaUnitToLabel(this.therapy.getDoseElement().rateFormulaUnit, true);
      }
      if (!this.getView().isDoseCalculationsEnabled())
      {
        if (!tm.jquery.Utils.isEmpty(this.getTherapy().getDoseElement().duration))
        {
          this._ratePane.setDurationInMinutes(this.getTherapy().getDoseElement().duration, true);
        }
      }
      else
      {
        this._ratePane.recalculateFromRate(true);
      }
    }
  },

  _setRateFromLastAdministrationWithRate: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var rateIsSet = false;
    if (this.administrations)
    {
      var lastAdministrationWithRate = null;
      this.administrations.forEach(function(administration)
      {
        if (((administration.getAdministrationType() === enums.administrationTypeEnum.START ||
                administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION) &&
                administration.isAdministrationCompleted()) &&
            (!tm.jquery.Utils.isEmpty(administration.getAdministeredDose()) &&
                administration.getAdministeredDose().getNumerator() !== 0))
        {
          if (tm.jquery.Utils.isEmpty(lastAdministrationWithRate) ||
              administration.isScheduledAfter(lastAdministrationWithRate))
          {
            lastAdministrationWithRate = administration;
          }
        }
      });
      if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate))
      {
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.getAdministeredDose().getNumerator()))
        {
          rateIsSet = true;
          this._ratePane.setRate(lastAdministrationWithRate.getAdministeredDose().getNumerator());
        }
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.getAdministeredDose().getDenominatorUnit()))
        {
          this._ratePane.setFormulaUnitToLabel(lastAdministrationWithRate.getAdministeredDose().getDenominatorUnit());
        }
      }
    }
    if (!rateIsSet && this.lastPositiveInfusionRate)
    {
      this._ratePane.setRate(this.lastPositiveInfusionRate);
      if (this.getTherapy() && !this.getTherapy().isOrderTypeOxygen())
      {
        this._ratePane.setFormulaUnitToLabel(this._getDoseElementFromTherapy().rateFormulaUnit);
      }
    }
  },

  _buildGui: function()
  {
    this._buildDoseContainer();
    this._buildRateContainer();
    this._buildVolumeContainer();

    this.add(this._doseContainer);
    this.add(this._rateContainer);
    this.add(this._volumeContainer);
  },

  _buildDoseContainer: function()
  {
    var self = this;
    var view = this.getView();

    this._doseContainer = new tm.jquery.Container({
      cls: "dose-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-end", 0),
      hidden: true
    });
    this._doseLabel = new tm.jquery.Component({
      cls: 'TextLabel dose-label',
      html: view.getDictionary('dose'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this._dosePane = new app.views.medications.ordering.dosing.DoseContainer({
      view: view,
      verticalLayout: true,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      denominatorAlwaysVolume: self.therapy.isOrderTypeComplex(),
      referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()}),
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        doseCalculationsAvailable: view.isDoseCalculationsEnabled()
      }),
      volumeChangedEvent: function()
      {
        self._onVolumeChange();

        if (!self._rateContainer.isHidden())
        {
          self._ratePane.calculateInfusionValues();
        }
      },
      numeratorChangeEvent: function()
      {
        if (!self._rateContainer.isHidden())
        {
          self._ratePane.recalculateFromRate();
        }
      },
      numeratorFocusLostEvent: function(dosePane)
      {
        if (!dosePane.denominatorField.isHidden())
        {
          dosePane.requestFocusToDenominator();
        }
        else if (!self._rateContainer.isHidden())
        {
          self._ratePane.requestFocus();
        }
        else if (!self._volumeContainer.isHidden())
        {
          self._volumeField.focus();
        }
        else
        {
          self._fireDoseFocusLostEvent();
        }
      },
      denominatorFocusLostEvent: function()
      {
        if (!self._rateContainer.isHidden())
        {
          self._ratePane.requestFocus();
        }
        else if (!self._volumeContainer.isHidden())
        {
          self._volumeField.focus();
        }
        else
        {
          self._fireDoseFocusLostEvent();
        }
      }
    });

    var doseContentContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      alignSelf: "stretch"
    });
    this._doseCommentContainer = this._createDoctorsCommentContainer();
    doseContentContainer.add(this._doseLabel);
    doseContentContainer.add(this._dosePane);
    this._doseContainer.add(doseContentContainer);
    this._doseContainer.add(this._doseCommentContainer);
  },

  _buildRateContainer: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var isOxygenTherapy = this.getTherapy() && this.getTherapy().isOrderTypeOxygen();

    this._rateContainer = new tm.jquery.Container({
      cls: 'rate-container',
      scrollable: "visible",
      hidden: true,
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-end", 0)
    });

    this._rateLabel = new tm.jquery.Component({
      cls: 'TextLabel rate-label',
      html: view.getDictionary(isOxygenTherapy ? 'rate' : 'infusion.rate'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._ratePane = new app.views.medications.ordering.InfusionRatePane({
      view: view,
      cls: "infusion-rate-pane",
      verticalLayout: true,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      allowZeroRate: this.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION ||
      (this.administration && this.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION) ||
      (this.getTherapy().isContinuousInfusion()),
      recalculateOnRateChange: !isOxygenTherapy,
      referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()}),
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        doseCalculationsAvailable: view.isDoseCalculationsEnabled()
      }),
      getInfusionRateTypeBolusFunction: function()
      {
        return self.therapy.isRateTypeBolus();
      },
      getInfusionIngredientsFunction: function()
      {
        if (self.therapy.isOrderTypeComplex())
        {
          var ingredientList = [];

          if (!self._doseContainer.isHidden() && self.getTherapy().getIngredientsList().length === 1)
          {
            var medication = self.therapy.getIngredientsList()[0].medication;
            var dose = self._dosePane.getDoseWithUnits();
            if (tm.jquery.Utils.isEmpty(dose.quantityDenominator) &&
                self.getView().getUnitsHolder().isUnitInLiquidGroup(dose.quantityUnit))
            {
              ingredientList.push({
                medication: medication,
                quantity: dose.quantity,
                quantityUnit: dose.quantityUnit,
                quantityDenominator: null
              });
            }
            else
            {
              ingredientList.push({
                medication: medication,
                quantity: dose.quantity,
                quantityUnit: dose.quantityUnit,
                quantityDenominator: dose.quantityDenominator,
                quantityDenominatorUnit: dose.denominatorUnit
              });
            }
          }
          else
          {
            self.therapy.getIngredientsList().forEach(function(ingredient)
            {
              ingredientList.push({
                medication: ingredient.medication,
                quantity: ingredient.quantity,
                quantityUnit: ingredient.quantityUnit,
                quantityDenominator: ingredient.quantityDenominator,
                quantityDenominatorUnit: ingredient.quantityDenominatorUnit
              });
            });
          }
          return ingredientList;
        }
        return null;
      },
      getContinuousInfusionFunction: function()
      {
        if (self.therapy.isOrderTypeComplex())
        {
          return self.therapy.isContinuousInfusion();
        }
        return false;
      },
      getVolumeSumFunction: function()
      {
        if (!tm.jquery.Utils.isEmpty(self._volumeContainer) && !self._volumeContainer.isHidden())
        {
          return self._volumeField.getValue();
        }
        return self.therapy.getVolumeSum();
      },
      formulaVisibleFunction: function()
      {
        return !isOxygenTherapy && self.getTherapy().hasNonDiluentIngredient();
      }
    });

    var rateContentContainer = new tm.jquery.Container({
      cls: "rate-content-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", self.therapy.isOrderTypeOxygen() ? "center" : "flex-start", 0),
      alignSelf: "stretch"
    });
    this._rateCommentContainer = this._createDoctorsCommentContainer();
    rateContentContainer.add(this._rateLabel);
    rateContentContainer.add(this._ratePane);
    this._rateContainer.add(rateContentContainer);
    this._rateContainer.add(this._rateCommentContainer);
  },

  _buildVolumeContainer: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    this._volumeContainer = new tm.jquery.Container({
      cls: 'volume-container',
      layout: tm.jquery.VFlexboxLayout.create("center", "flex-end", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      hidden: true
    });

    var volumeLabel = new tm.jquery.Container({
      cls: 'TextLabel volume-label',
      html: view.getDictionary('volume.total'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this._volumeField = app.views.medications.MedicationUtils.createNumberField('n2', 68);
    this._volumeField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));
    this._volumeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (!self._rateContainer.isHidden())
      {
        self._ratePane.calculateInfusionValues();
      }
      if (self.therapyDoseTypeEnum === enums.therapyDoseTypeEnum.VOLUME_SUM)
      {
        self._onVolumeChange();
      }
    });
    var mlLabel = new tm.jquery.Container({
      cls: 'TextData ml-label',
      html: 'mL',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    var volumeContentContainer = new tm.jquery.Container({
      cls: "volume-sum-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      alignSelf: "stretch"
    });
    this._volumeCommentContainer = this._createDoctorsCommentContainer();
    volumeContentContainer.add(volumeLabel);
    volumeContentContainer.add(this._volumeField);
    volumeContentContainer.add(mlLabel);
    this._volumeContainer.add(volumeContentContainer);
    this._volumeContainer.add(this._volumeCommentContainer);
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _createDoctorsCommentContainer: function()
  {
    return new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("center", "center"),
      cls: "doctors-comment TextData",
      hidden: true
    })
  },

  /**
   * Called on volume changed, fire event that calls {@link _handleMedicationIngredientRule} on
   * {@link app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer}
   * @private
   */
  _onVolumeChange: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_VOLUME_CHANGE,
      eventData: {
        medicationIngredientRule: app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE
      }
    }), null);
  },

  /**
   * @returns {Boolean}
   * @private
   */
  _isDoseTypeDoseRange: function()
  {
    return this.getTherapy() && this.getTherapy().getDoseElement() && this.getTherapy().getDoseElement().doseRange;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} administrationDose
   * @param {Boolean} complexTherapyWithSingleIngredient
   * @private
   */
  _presentMedicationDataForQuantity: function(medicationData, administrationDose, complexTherapyWithSingleIngredient)
  {
    var view = this.getView();
    if (!medicationData.isDoseFormDescriptive())
    {
      this._dosePane.setMedicationData(medicationData);
      this._doseContainer.show();
      if (this.administration && !tm.jquery.Utils.isEmpty(administrationDose) && administrationDose.numerator)
      {
        var numeratorIsVolume =
            tm.jquery.Utils.isEmpty(administrationDose.denominator) &&
            view.getUnitsHolder().isUnitInLiquidGroup(administrationDose.numeratorUnit);
        if (complexTherapyWithSingleIngredient && numeratorIsVolume)
        {
          this._dosePane.setVolume(administrationDose.numerator);
        }
        else
        {
          if (this._hasMedicationChanged(this.prescribedMedicationData, medicationData))
          {
            this._recalculateAndSetDose(medicationData, administrationDose.numerator, administrationDose.numeratorUnit);
          }
          else
          {
            this._dosePane.setDoseNumerator(administrationDose.numerator, true);
            if (administrationDose.denominator)
            {
              this._dosePane.setDoseDenominator(administrationDose.denominator, true);
            }
          }
        }
      }
      else
      {
        this._setDoseFromTherapy(medicationData);
      }
    }
    if (this.administration && this.administration.getDoctorsComment())
    {
      this._doseCommentContainer.setHtml(tm.jquery.Utils.escapeHtml(this.administration.getDoctorsComment()));
      this.isRendered() ? this._doseCommentContainer.show() : this._doseCommentContainer.setHidden(false);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} administrationDose
   * @private
   */
  _presentMedicationDataForVolumeSum: function(medicationData, administrationDose)
  {
    this._dosePane.setMedicationData(medicationData);
    this._volumeContainer.show();
    if (this.administration && administrationDose.numerator)
    {
      this._volumeField.setValue(administrationDose.numerator);
    }
    else
    {
      this._setVolumeFromTherapy();
    }
    if (this.administration && this.administration.getDoctorsComment())
    {
      this._volumeCommentContainer.setHtml(tm.jquery.Utils.escapeHtml(this.administration.getDoctorsComment()));
      this.isRendered() ? this._volumeCommentContainer.show() : this._volumeCommentContainer.setHidden(false);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} administrationDose
   * @private
   */
  _presentMedicationDataForRate: function(medicationData, administrationDose)
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.getTherapy().isOrderTypeOxygen())
    {
      if (this.administration && administrationDose)
      {
        this._ratePane.setRate(administrationDose.numerator, true);
        this._ratePane.setRateUnit(administrationDose.numeratorUnit);
      }
      else
      {
        this._ratePane.setRate(this.getTherapy().getFlowRate(), true);
        this._ratePane.setRateUnit(this.getTherapy().getFlowRateUnit());
      }
      this._ratePane.setFormulaVisible(true);
      this._ratePane.setDurationVisible(false);
    }
    else
    {
      this._ratePane.setFirstMedicationData(medicationData, true);
      this._ratePane.setDurationVisible(false);
      if (this.administration && administrationDose)
      {
        this._ratePane.setFormulaUnitToLabel(administrationDose.denominatorUnit, true);
        this._ratePane.setRate(administrationDose.numerator, true);
      }
      this._ratePane.recalculateFromRate(true);
      this._ratePane.setFormulaVisible();
    }

    this._rateContainer.show();

    if (!this.administration)
    {
      this._setRateFromLastAdministrationWithRate();
    }
    if (this.administration &&
        this.administrationType === enums.administrationTypeEnum.START &&
        this.therapy.isContinuousInfusion())
    {
      if (this.isInfusionBagEnabled())
      {
        this.getBagContainer().show();
      }
      if (this.isInfusionBagEnabled() && this.getAdministration().getInfusionBag())
      {
        this.setBagFieldValue(this.getAdministration().getInfusionBag().quantity);
      }
    }
    if (this.administration && this.administration.getDoctorsComment())
    {
      this._rateCommentContainer.setHtml(tm.jquery.Utils.escapeHtml(this.administration.getDoctorsComment()));
      this.isRendered() ? this._rateCommentContainer.show() : this._rateCommentContainer.setHidden(false);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} administrationDose
   * @param {Boolean} complexTherapyWithSingleIngredient
   * @private
   */
  _presentMedicationDataForRateQuantity: function(medicationData, administrationDose, complexTherapyWithSingleIngredient)
  {
    this._dosePane.setMedicationData(medicationData);
    if (tm.jquery.Utils.isEmpty(medicationData))
    {
      var onlyInfusionIngredient = this.therapy.getIngredientsList()[0];
      if (onlyInfusionIngredient.quantityUnit)
      {
        this._dosePane.setUnits(onlyInfusionIngredient.quantityUnit, onlyInfusionIngredient.quantityDenominatorUnit);
      }
      else
      {
        this._dosePane.setUnits(onlyInfusionIngredient.quantityDenominatorUnit, null);
      }
    }
    this._doseContainer.show();

    this._ratePane.setFirstMedicationData(medicationData);
    this._rateContainer.show();

    if (this.administration && administrationDose)
    {
      if (this.administration && administrationDose.secondaryNumerator)
      {
        var secondaryNumeratorIsVolume =
            tm.jquery.Utils.isEmpty(administrationDose.secondaryDenominator) &&
            view.getUnitsHolder().isUnitInLiquidGroup(administrationDose.secondaryNumeratorUnit);
        if (complexTherapyWithSingleIngredient && secondaryNumeratorIsVolume)
        {
          this._dosePane.setVolume(administrationDose.secondaryNumerator);
        }
        else
        {
          this._dosePane.setDoseNumerator(administrationDose.secondaryNumerator, true);
          if (administrationDose.secondaryDenominator)
          {
            this._dosePane.setDoseDenominator(administrationDose.secondaryDenominator, true);
          }
        }
      }
      this._ratePane.setFormulaUnitToLabel(administrationDose.denominatorUnit);
      this._ratePane.setRate(administrationDose.numerator);
    }
    else
    {
      this._setDoseFromTherapy(medicationData);
      this._setRateFromTherapy();
    }
    this._ratePane.setFormulaVisible();
    this._ratePane.setDurationVisible(!this.therapy.variable);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Object} administrationDose
   * @private
   */
  _presentMedicationDataForRateVolumeSum: function(medicationData, administrationDose)
  {
    this._ratePane.setFirstMedicationData(medicationData);
    this._ratePane.setFormulaVisible();
    this._rateContainer.show();
    this._volumeContainer.show();
    if (this.administration && administrationDose)
    {
      if (administrationDose.secondaryNumerator)
      {
        this._volumeField.setValue(administrationDose.secondaryNumerator);
      }
      this._ratePane.setFormulaUnitToLabel(administrationDose.denominatorUnit);
      this._ratePane.setRate(administrationDose.numerator);
    }
    else
    {
      this._setVolumeFromTherapy();
      this._setRateFromTherapy();
    }
    if (this.administration &&
        this.administrationType === app.views.medications.TherapyEnums.administrationTypeEnum.START &&
        this.therapy.isContinuousInfusion())
    {
      if (this.isInfusionBagEnabled())
      {
        this.getBagContainer().show();
      }
      if (this.isInfusionBagEnabled() && this.administration.getInfusionBag())
      {
        this.setBagFieldValue(this.administration.getInfusionBag().quantity);
      }
    }
  },

  /**
   * If only one of two fields on {@link #_dosePane} is visible, the {@link #_doseLabel}  should be aligned to center.
   * @private
   */
  _setDosePaneAlignment: function()
  {
    if (this._dosePane.getNumeratorField().isHidden() || this._dosePane.getDenominatorField().isHidden())
    {
      this._doseLabel.setAlignSelf("center");
    }
    else
    {
      this._doseLabel.setAlignSelf("flex-start");
    }
  },

  /**
   * If only rate field on {@link #_ratePane} is visible, the {@link #_rateLabel}  should be aligned to center.
   * @private
   */
  _setRateContainerAlignment: function()
  {
    if (this._ratePane.getDurationField().isHidden() && this._ratePane.getFormulaField().isHidden())
    {
      this._rateLabel.setAlignSelf("center");
    }
    else
    {
      this._rateLabel.setAlignSelf("flex-start");
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Number} numerator
   * @param {String} numeratorUnit
   * @private
   */
  _recalculateAndSetDose: function(medicationData, numerator, numeratorUnit)
  {
    if (!!this.prescribedMedicationData && !medicationData.hasMatchingNumeratorUnit(this.prescribedMedicationData))
    {
      var convertedNumerator = this.getView().getUnitsHolder().convertToUnit(
          numerator,
          numeratorUnit,
          medicationData.getStrengthNumeratorUnit());
      this._dosePane.setDoseNumerator(convertedNumerator, true);
    }
    else
    {
      this._dosePane.setDoseNumerator(numerator, true);
    }
    this._dosePane.calculateAndSetDoseDenominator(true);
    this._onDoseRecalculated(medicationData);
  },

  /**
   * @returns {Object|null}
   * @private
   */
  _getDoseElementFromTherapy: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTherapy().getDoseElement()) ? this.getTherapy().getDoseElement() :
        this.getTherapy().getTimedDoseElements()[0].doseElement;
  },

  /**
   * Asserts if medicationData has changed by comparing medication id-s
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} previousMedicationData
   * @param {app.views.medications.common.dto.MedicationData} newMedicationData
   * @returns {boolean}
   * @private
   */
  _hasMedicationChanged: function(previousMedicationData, newMedicationData)
  {
    if (!tm.jquery.Utils.isArray(previousMedicationData))
    {
      return previousMedicationData && newMedicationData &&
          previousMedicationData.getMedication().getId() !== newMedicationData.getMedication().getId();
    }
    return false;
  },

  /**
   * Fires event when the administration dose has been recalculated due to medication change.
   * @private
   */
  _onDoseRecalculated: function(medicationData)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_MEDICATION_DOSE_RECALCULATED,
      eventData: {
        medicationData: medicationData
      }
    }), null);
  },

  /**
   * Called if no other field on any component that describes the administration dose is an appropriate candidate for focus.
   * The parent component should decide, which field should be in focus next.
   * @private
   */
  _fireDoseFocusLostEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_DOSE_FOCUS_LOST
    }), null);
  }
});