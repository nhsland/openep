Class.define('app.views.medications.timeline.titration.RateApplicationRowContainer', 'app.views.medications.timeline.titration.BaseApplicationRowContainer', {
  scrollable: 'visible',
  cls: "rate-application-row-container",

  lastPositiveInfusionRate: null,
  activeContinuousInfusion: true,

  _ratePane: null,
  _bagContainerWrapper: null,
  _bagField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @override
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   */
  buildApplicationOptionsColumn: function()
  {
    var self = this;
    var view = this.getView();

    var applicationOptionsColumn = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("application"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.CheckBox({
        labelText: view.getDictionary("mark.this.application.as.already.given"),
        cls: "mark-given-checkbox",
        labelCls: "TextData",
        checked: this.isAdjustInfusion() || this.isStopFlow(),
        labelAlign: "right",
        enabled: !(this.isAdjustInfusion() || this.isStopFlow()),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        nowrap: true
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    applicationOptionsColumn.getContentComponent().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (view.isInfusionBagEnabled())
      {
        self.applyInfusionBagContainerVisibility(component.checked === true);
      }
      self.assertAdministrationChange();
    });
    this._markAsGivenCheckBox = applicationOptionsColumn.getContentComponent();

    return applicationOptionsColumn;
  },

  /**
   * @override
   * @returns {tm.jquery.Container}
   */
  buildApplicationDosingRow: function()
  {
    var self = this;
    var view = this.getView();
    var applicationDosingRow = new tm.jquery.Container({
      scrollable: 'visible',
      hidden: this.isStopFlow(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var ratePaneColumn = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("infusion.rate"),
      cls: 'rate-pane-container',
      scrollable: 'visible',
      contentComponent: new app.views.medications.ordering.InfusionRatePane({
        view: this.view,
        cls: "infusion-rate-pane",
        medicationData: this.getMedicationData(),
        verticalLayout: false,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()}),
        orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
          doseCalculationsAvailable: view.isDoseCalculationsEnabled()
        }),
        getInfusionIngredientsFunction: function()
        {
          var ingredientList = [];

          self.getTherapyForTitration().getTherapy().getIngredientsList().forEach(function(ingredient)
          {
            ingredientList.push({
              medication: ingredient.medication,
              quantity: ingredient.quantity,
              quantityUnit: ingredient.quantityUnit,
              quantityDenominator: ingredient.quantityDenominator
            });
          });
          return ingredientList;
        },
        getContinuousInfusionFunction: function()
        {
          if (self.getTherapyForTitration().getTherapy().isOrderTypeComplex())
          {
            return self.getTherapyForTitration().getTherapy().isContinuousInfusion();
          }
          return false;
        },
        formulaVisibleFunction: function()
        {
          return true;
        },
        getVolumeSumFunction: function()
        {
          return self.getTherapyForTitration().getTherapy().getVolumeSum();
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    applicationDosingRow.add(ratePaneColumn);

    ratePaneColumn.getContentComponent().setDurationVisible(false);
    ratePaneColumn.getContentComponent().setFirstMedicationData(this.medicationData);
    ratePaneColumn.getContentComponent().setFormulaVisible(true);

    this._ratePane = ratePaneColumn.getContentComponent();

    if (this.isAdjustInfusion())
    {
      this._setRateFromLastAdministrationWithRate();
    }
    else if (this.getAdministration() && !!this.getAdministration().getPlannedDose())
    {
      this._setRateFromPlannedDose();
    }
    return applicationDosingRow;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  buildInfusionBagRow: function()
  {
    var view = this.getView();

    var infusionBagRow = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 48,
      hidden: true
    });

    var bagLabel = new tm.jquery.Container({
      cls: 'TextLabel volume-label',
      html: view.getDictionary('bag.syringe.volume'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var bagContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });

    var bagField = app.views.medications.MedicationUtils.createNumberField('n2', 68);

    var bagMlLabel = new tm.jquery.Container({
      cls: 'TextData ml-label',
      html: 'mL',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "center")
    });

    bagContainer.add(bagField);
    bagContainer.add(bagMlLabel);
    infusionBagRow.add(bagLabel);
    infusionBagRow.add(bagContainer);

    this._bagContainerWrapper = infusionBagRow;
    this._bagField = bagField;

    return infusionBagRow;
  },

  _setRateFromLastAdministrationWithRate: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var rateIsSet = false;

    if (this.getAllAdministrations())
    {
      var lastAdministrationWithRate = null;
      this.getAllAdministrations().forEach(function(administration)
      {
        if (((administration.getAdministrationType() === enums.administrationTypeEnum.START ||
            administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION) &&
            administration.isAdministrationCompleted()) &&
            (!tm.jquery.Utils.isEmpty(administration.getAdministeredDose()) &&
                administration.getAdministeredDose().getNumerator() !== 0))
        {
          if (lastAdministrationWithRate === null ||
              administration.isScheduledAfter(lastAdministrationWithRate))
          {
            lastAdministrationWithRate = administration;
          }
        }
      });
      if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate))
      {
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.administeredDose.numerator))
        {
          rateIsSet = true;
          this._ratePane.setRate(lastAdministrationWithRate.administeredDose.numerator);
        }
        if (!tm.jquery.Utils.isEmpty(lastAdministrationWithRate.administeredDose.denominatorUnit))
        {
          this._ratePane.setFormulaUnitToLabel(lastAdministrationWithRate.administeredDose.denominatorUnit);
        }
      }
    }
    if (!rateIsSet && this.getLastPositiveInfusionRate())
    {
      this._ratePane.setRate(this.getLastPositiveInfusionRate());
    }
  },

  /**
   * Sets {@link #_ratePane} values from planned dose. Useful when editing an administration. Presumes that
   * {@link #administration} exists and that it has {@link #plannedDose} defined.
   * @private
   */
  _setRateFromPlannedDose: function()
  {
    if (this.getAdministration().getPlannedDose().getDenominatorUnit())
    {
      this._ratePane.setFormulaUnitToLabel(this.getAdministration().getPlannedDose().getDenominatorUnit(), true);
    }
    if (this.getAdministration().getPlannedDose().getNumerator())
    {
      this._ratePane.setRate(this.getAdministration().getPlannedDose().numerator, true);
    }
    this._ratePane.recalculateFromRate(true);
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormFields: function()
  {
    var formFields = [];
    var self = this;
    formFields = formFields.concat(this._ratePane.getInfusionRatePaneValidations());

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !self.hasAdministrationTimeRelatedWarnings();
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getTimePicker().getField().getInputElement();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._markAsGivenCheckBox,
      required: false,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function(value)
            {
              var hasRestrictiveWarnings = self.getAdministrationWarnings() ?
                  self.getAdministrationWarnings().hasRestrictiveWarnings() : false;
              return value === true && !hasRestrictiveWarnings || value === false;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.isChecked();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !self.hasAdministrationTimeRelatedWarnings();
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getDatePicker().getField().getInputElement();
      }
    }));

    return formFields;
  },

  /**
   * Show or hide the infusion bag container.
   * @param {Boolean} visible
   */
  applyInfusionBagContainerVisibility: function(visible)
  {
    if (visible)
    {
      this._bagContainerWrapper.show();
      this._bagContainerWrapper.focus();
    }
    else
    {
      this._bagContainerWrapper.hide();
    }
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  getTherapyDose: function()
  {
    var doseEnums = app.views.medications.TherapyEnums.therapyDoseTypeEnum;
    var rateValues = this._ratePane.getInfusionRate();
    return new app.views.medications.common.dto.TherapyDose({
      therapyDoseTypeEnum: doseEnums.RATE,
      numerator: this.isStopFlow() ? 0 : rateValues.rate,
      denominator: this.isStopFlow() ? 0 : rateValues.rateFormula,
      numeratorUnit: rateValues.rateUnit,
      denominatorUnit: this.isStopFlow() ? this.getTherapyForTitration().getUnit() : rateValues.rateFormulaUnit
    });
  },

  /**
   * @returns {Number|null}
   */
  getLastPositiveInfusionRate: function()
  {
    return this.lastPositiveInfusionRate
  }
});
