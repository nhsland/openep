Class.define('app.views.medications.ordering.ComplexTherapyContainer', 'tm.jquery.Container', {
  cls: "complex-therapy-container",
  scrollable: 'vertical',
  margin: '10 0 0 0',

  /** configs */
  /** @type tm.views.medications.TherapyView */
  view: null,
  editMode: false,
  copyMode: false,
  changeCardEvent: null, //optional
  /** @type function(app.views.medications.ordering.SaveOrderToTemplateEventData)|null */
  saveOrderToTemplateEventCallback: null,
  getTherapyStartNotBeforeDateFunction: null, //optional
  /** @type function(app.views.medications.ordering.ConfirmOrderEventData) */
  confirmOrderEventCallback: null,
  saveDateTimePaneEvent: null,
  changeReasonAvailable: false,
  getBasketTherapiesFunction: null, //optional
  refreshBasketFunction: null, //optional
  additionalMedicationSearchFilter: null,
  preventUnlicensedMedicationSelection: false,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  availableInformationSources: null,
  /** privates */
  _originalTherapy: null,
  validationForm: null,
  timedDoseElements: null,
  recurringContinuousInfusion: null,
  medicationData: null,
  medicationPanes: null,
  valueSettingInProgress: null,
  linkName: null,
  readyConditionTask: null,
  showHeparinPane: null,
  linkedTherapy: null, /* Therapy.js */
  medicationRuleUtils: null,
  /** privates: components */
  medicationPanesContainer: null,
  heparinPane: null,
  volumeSumTopSpacer: null,
  volumeSumPane: null,
  routesPane: null,
  infusionRatePane: null,
  infusionRateTypePane: null,
  rateLabelVSpacer: null,
  variableRateContainer: null,
  varioButton: null,
  dosingFrequencyLabel: null,
  dosingFrequencyPane: null,
  /** @type app.views.medications.ordering.TherapyIntervalPane */
  therapyIntervalPane: null,
  commentIndicationPane: null,
  _changeReasonContainer: null,
  calculatedDosagePane: null,
  addToBasketButton: null,
  templatesButton: null,
  simpleTherapyButton: null,
  saveToTemplateButton: null,
  therapyNextAdministrationLabelPane: null,
  linkIcon: null,
  linkTherapyButton: null,
  unlinkTherapyButton: null,
  administrationPreviewTimeline: null,

  contentExtensions: null,

  orderingBehaviour: null,
  referenceData: null,

  _previewRefreshTimer: undefined, /* set to undefined to prevent code inspection errors from clearTimeout */
  _overridenCriticalWarnings: null,
  _extensionsPlaceholder: null,
  /** @type app.views.medications.ordering.InformationSourceContainer|null */
  _informationSourceContainer: null,
  _showInformationSourceButton: null,
  _therapySupplyContainer: null,
  _antimicrobialTherapyStartContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    if (!this.referenceData)
    {
      throw Error('referenceData is not defined.');
    }

    this.timedDoseElements = [];
    this.recurringContinuousInfusion = false;
    this.medicationPanes = [];
    this.showHeparinPane = this.view.getMedicationsShowHeparinPane();

    if (!this.getOrderingBehaviour().isHeparinAvailable())
    {
      this.showHeparinPane = false;
    }

    if (tm.jquery.Utils.isEmpty(this.contentExtensions))
    {
      this.contentExtensions = [];
    }

    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({view: this.view, referenceData: this.getReferenceData()}));

    this.availableInformationSources = tm.jquery.Utils.isArray(this.availableInformationSources) ?
        this.availableInformationSources :
        [];

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();


    this.medicationPanesContainer = new tm.jquery.Container({
      cls: "medication-panes-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });

    if (this.showHeparinPane === true)
    {
      this.heparinPane = new app.views.medications.ordering.HeparinPane({view: view});
      this.heparinPane.hide();
    }
    else
    {
      this.volumeSumTopSpacer = this._createVerticalSpacer(23);
      this.volumeSumTopSpacer.hide();
    }

    this.volumeSumPane = new app.views.medications.ordering.VolumeSumPane({
      view: view,
      width: 504,
      margin: '0 10 0 0',
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      orderingBehaviour: this.getOrderingBehaviour(),
      adjustVolumesEvent: function()
      {
        self._adjustVolumes();
      }
    });

    this.routesPane = new app.views.medications.ordering.RoutesPane({
      view: view,
      height: 30,
      selectionRequired: this.orderingBehaviour.isRouteOfAdministrationRequired(),
      discretionaryRoutesDisabled: false,
      /**
       *
       * @param {Array<app.views.medications.common.dto.MedicationRoute>|null} selectedRoutes
       */
      changeEvent: function(selectedRoutes)
      {
        if (self._isSingleRouteSelected(selectedRoutes))
        {
          for (var j = 0; j < self.medicationPanes.length; j++)
          {
            var medicationData = self.medicationPanes[j].getMedicationData();
            if (!!medicationData && medicationData.hasMatchingRouteWithMaxDose(selectedRoutes[0]))
            {
              self._handleMaxDoseChange(self.medicationPanes[j], medicationData);
            }
            else
            {
              self.medicationPanes[j].clearMaxDose();
              self.medicationPanes[j].setMaxDoseContainerVisibility(false);
            }
          }
        }
        else
        {
          self.medicationPanes.forEach(
              /**
               * @param {app.views.medications.ordering.ComplexTherapyMedicationPane} medicationPane
               */
              function clearMaxDose(medicationPane)
              {
                medicationPane.setMaxDoseContainerVisibility(false);
                medicationPane.clearMaxDose();
              })
        }
      }
    });

    this.infusionRateTypePane = new app.views.medications.ordering.InfusionRateTypePane({
      view: view,
      orderingBehaviour: this.getOrderingBehaviour(),
      preventRateTypeChange: this.isEditMode() && !this.isCopyMode(),
      continuousInfusionChangedFunction: function(continuousInfusion, clearValues)
      {
        self._setMedicationsEditable();
        self.variableRateContainer.hide();
        self.timedDoseElements.removeAll();
        self.recurringContinuousInfusion = false;
        self._continuousInfusionChanged(continuousInfusion, true);
        // since we trigger continuousInfusionChangedFunction either when being selected or deselected
        // on top of the related bolusChangedFunction / speedChangedFunction, only recalculate when
        // we actually select/deselect continues infusion and let the other handlers handle other scenarios.
        if (clearValues)
        {
          self._calculateDosing();
        }
        self._handleTherapyLinkButtonDisplay();
      },
      adjustableRateChangeFunction: function(adjustableRate)
      {
        self._setRateContainersVisible(!adjustableRate);
        self.infusionRatePane.clearInfusionValues();
        // reset the possible warnings. If set to false, dose should be entered first before anything needs to be recalc.
        if (adjustableRate)
        {
          self._calculateDosing();
        }
        self._removeVariableRate();
      },
      bolusChangedFunction: function()
      {
        if (self.getOrderingBehaviour().isStartEndTimeAvailable())
        {
          self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        }
        self._handleSpeedOrBolusChanged(false);
        self._calculateDosing();
      },
      speedChangedFunction: function(isSpeed)
      {
        if (self.getOrderingBehaviour().isStartEndTimeAvailable())
        {
          self.administrationPreviewTimeline.setData(self._buildTherapy(), []);
        }
        if (isSpeed)
        {
          self._handleSpeedOrBolusChanged(true);
          self.infusionRatePane.setDurationVisible(true);
        }
        else
        {
          self._handleSpeedOrBolusChanged(false);
        }
        self._calculateDosing();
      }
    });
    this.rateLabelContainer = new tm.jquery.Container({
      width: 160,
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "start")
    });
    var clearRatesContextMenu = appFactory.createContextMenu();
    var clearRatesMenuItem = new tm.jquery.MenuItem({
          cls: "clear-rates-menu-item",
          text: view.getDictionary('empty.form'),
          handler: function()
          {
            if (!self.infusionRatePane.isHidden())
            {
              self.infusionRatePane.clearInfusionValues();
            }
            else
            {
              self.therapyIntervalPane.setStartHourEnabled(true);
              self.dosingFrequencyPane.showAllFields();
              self.infusionRatePane.show();
              self.infusionRatePane.clear();
              self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
              self.variableRateContainer.hide();
              self.timedDoseElements.removeAll();
              self.recurringContinuousInfusion = false;
              self._setMedicationsEditable();
              self.infusionRatePane.requestFocus();
              self._calculateDosing();
            }
            self.varioButton.show();
            self.rateLabelContainer.show();
          }
        }
    );
    clearRatesContextMenu.addMenuItem(clearRatesMenuItem);
    this.rateLabelContainer.setContextMenu(clearRatesContextMenu);
    this.rateLabelContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      component.getContextMenu().show(elementEvent.pageX, elementEvent.pageY);
    });
    this.rateLabelVSpacer = this._createVerticalSpacer(2);
    this.infusionRatePane = new app.views.medications.ordering.InfusionRatePane({
      view: view,
      scrollable: "visible",
      cls: "infusion-rate-pane",
      width: 587,
      referenceData: this.getReferenceData(),
      orderingBehaviour: this.getOrderingBehaviour(),
      getInfusionRateTypeBolusFunction: function()
      {
        return self.infusionRateTypePane.isBolus();
      },
      setInfusionRateTypeFunction: function(rate)
      {
        self._infusionRateTypePaneFunction(rate);
      },
      getInfusionIngredientsFunction: function()
      {
        return self._getComplexTherapyIngredients();
      },
      getContinuousInfusionFunction: function()
      {
        return self.infusionRateTypePane.isContinuousInfusion();
      },
      getVolumeSumFunction: function()
      {
        return self.volumeSumPane.getVolumeSum();
      },
      durationChangeEvent: function(duration)
      {
        self._handleDurationChange(duration);
      },
      rateFormulaChangeEvent: function()
      {
        self._calculateDosing();
      },
      singleIngredientVolumeCalculatedEvent: function(volume)
      {
        self.medicationPanes[0].setVolume(volume);
        self._handleVarioEnabling();
      },
      formulaVisibleFunction: function()
      {
        /**
         * Formula should not be displayed if all ingredients are diluents, or if therapy only has one ingredient,
         * and this ingredient is from universal prescription form
         */
        return self._areAnyIngredientsNotADiluent() &&
            (!self._isUniversalSingleIngredient() || !self.infusionRateTypePane.isContinuousInfusion());
      }
    });
    this.variableRateContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      margin: '5 0 0 10'
    });

    this.varioButton = new tm.jquery.Button({
      cls: "vario-button",
      text: view.getDictionary('variable'),
      type: 'link',
      handler: function()
      {
        self._openVariableRateEditPane();
      }
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.dosing.DosingFrequencyPane({
      view: view,
      orderingBehaviour: this.getOrderingBehaviour(),
      editMode: this.isEditMode(),
      width: 678,
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
        self._calculateDosing();
      },
      setMaxDailyFrequencyFieldsVisibleFunction: function(setVisible)
      {
        if (self.therapyIntervalPane)
        {
          self.therapyIntervalPane.setMaxDailyFrequencyFieldVisible(setVisible);
        }
      }
    });
    if (!this.getOrderingBehaviour().isDaysButtonAvailable())
    {
      this.dosingFrequencyPane.hideDaysButton();
    }
    this.therapyIntervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: view,
      width: 678,
      orderingBehaviour: this.getOrderingBehaviour(),
      hidden: this.getOrderingBehaviour().isSupplyAvailable(),
      getFrequencyDataFunction: function()
      {
        return {
          frequencyKey: self.dosingFrequencyPane.getFrequencyKey(),
          frequencyType: self.dosingFrequencyPane.getFrequencyType(),
          frequencyMode: self.dosingFrequencyPane.getFrequencyMode()
        }
      },
      getDosingPatternFunction: function()
      {
        return self.timedDoseElements.length > 0 ?
            [self.timedDoseElements[0].doseTime] :
            self.dosingFrequencyPane.getDosingPattern();
      },
      getDurationFunction: function()
      {
        var infusionRate = self.infusionRatePane.getInfusionRate();
        if (infusionRate && infusionRate.duration)
        {
          return infusionRate.duration;
        }
        return null;
      },
      onMaxDailyFrequencyFieldFocusLost: function()
      {
        self._handleParacetamolRuleChange();
      }
    });
    this.therapyIntervalPane.on(app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
        function(component, componentEvent)
        {
          self._updateNextAdministrationTime(componentEvent.eventData.start);
          self._antimicrobialTherapyStartContainer.setTimeOfTherapyStart(componentEvent.eventData.start)
        });
    if (!this.getOrderingBehaviour().isStartEndTimeAvailable() && this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._therapySupplyContainer = new app.views.medications.ordering.supply.TherapySupplyContainer({
        view: view,
        required: this.getOrderingBehaviour().isSupplyRequired()
      });
    }

    this.therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: !this.getOrderingBehaviour().isStartEndTimeAvailable(),
      view: view
    });

    if (this.getOrderingBehaviour().isInformationSourceAvailable())
    {
      this._informationSourceContainer = new app.views.medications.ordering.InformationSourceContainer({
        view: this.getView(),
        availableInformationSources: this.availableInformationSources,
        required: this.getOrderingBehaviour().isInformationSourceRequired(),
        hidden: !this.getOrderingBehaviour().isInformationSourceRequired()
      });
    }

    this._antimicrobialTherapyStartContainer = new app.views.medications.ordering.PastTherapyStartContainer({
      view: view,
      hidden: true,
      therapyIntervalPane: this.therapyIntervalPane,
      titleText: this.getView().getDictionary('antimicrobial.therapy.start')
    });
    this.commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      view: view,
      orderingBehaviour: this.getOrderingBehaviour(),
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this._changeReasonContainer = new app.views.medications.ordering.ChangeReasonContainer({
      padding: "5 20 0 0",
      view: view,
      hidden: !this.isChangeReasonAvailable()
    });
    this.calculatedDosagePane = new app.views.medications.ordering.calculationDisplay.CalculatedDosagePane({
      view: view,
      referenceData: this.getReferenceData()
    });

    this._extensionsPlaceholder = new tm.jquery.Container({
      cls: "extensions-container",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    if (!this.isEditMode())
    {
      this.addToBasketButton = new tm.jquery.Button({
        cls: "add-to-basket-button",
        text: view.getDictionary("add"),
        handler: function()
        {
          self.addToBasketButton.setEnabled(false);
          self.validateAndConfirmOrder();
        }
      });

      this.templatesButton = new tm.jquery.Button({
        cls: "templates-button",
        text: view.getDictionary('empty.form'),
        type: "link",
        //flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
            self.refreshBasketFunction();
          }
        }
      });
      this.simpleTherapyButton = new tm.jquery.Button({
        cls: "simple-therapy-button",
        text: view.getDictionary('simple'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent(self.medicationData);
            self.refreshBasketFunction();
          }
        }
      });

      if (this.getOrderingBehaviour().isAddToTemplateAvailable() && view.getTherapyAuthority().isManageAnyTemplatesAllowed())
      {
        this.saveToTemplateButton = new tm.jquery.Button({
          cls: "save-to-template-button",
          text: view.getDictionary('add.to.order.set'),
          type: "link",
          handler: this._addToTemplate.bind(this)
        });
      }

      this.linkTherapyButton = new tm.jquery.Button({
        cls: "link-therapy-button",
        text: view.getDictionary("link.medication"),
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self._openLinkTherapyDialog();
        }
      });
      this.linkIcon = new tm.jquery.Container({
        margin: '8 -18 0 0',
        cls: 'ordering-basket-container-link'
      });
      this.unlinkTherapyButton = new tm.jquery.Button({
        cls: "unlink-therapy-button",
        text: view.getDictionary("unlink.medication"),
        style: "width: 150px, text-align:left;",
        type: "link",
        handler: function()
        {
          self._unlinkTherapy();
          self._handleTherapyLinkButtonDisplay();
          self.refreshBasketFunction();
        }
      });

      if (!!this._informationSourceContainer && !this.getOrderingBehaviour().isInformationSourceRequired())
      {
          this._showInformationSourceButton = new tm.jquery.Button({
            text: view.getDictionary('show.source'),
            type: "link",
            handler: function()
            {
              self._applyInformationSourceVisibility(true);
            }
          });
      }
    }

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._confirm();
      },
      onValidationError: function()
      {
        self.confirmOrderEventCallback(new app.views.medications.ordering.ConfirmOrderEventData({validationPassed: false}));

        if (self.addToBasketButton)
        {
          self.addToBasketButton.setEnabled(true);
        }
      },
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    this._setRateContainersVisible(false);
    this._registerCommonCalculationFormulaProviders();
  },

  _buildGui: function()
  {
    this.add(this.medicationPanesContainer);

    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '0 0 0 20',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });
    this.add(mainContainer);
    var heparinRowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")});
    if (this.showHeparinPane === true)
    {
      mainContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel', 'Heparin'));
      heparinRowContainer.add(this.heparinPane);
      this.heparinPane.show();
    }
    else
    {
      mainContainer.add(this.volumeSumTopSpacer);
    }
    heparinRowContainer.add(this.volumeSumPane);
    mainContainer.add(heparinRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('route')));
    var routesRowPane = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5),
      scrollable: 'visible'
    });
    routesRowPane.add(this.routesPane);
    routesRowPane.add(this.infusionRateTypePane);
    mainContainer.add(routesRowPane);
    mainContainer.add(this._createVerticalSpacer(2));

    this.rateLabelContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('infusion.rate')));
    this.rateLabelContainer.add(new tm.jquery.Container({cls: 'menu-icon', width: 16, height: 16, margin: '4 0 0 5'}));
    mainContainer.add(this.rateLabelContainer);
    mainContainer.add(this.rateLabelVSpacer);
    var infusionRateContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      width: 678,
      scrollable: "visible"
    });
    infusionRateContainer.add(this.infusionRatePane);
    infusionRateContainer.add(this.varioButton);
    infusionRateContainer.add(this.variableRateContainer);

    mainContainer.add(infusionRateContainer);
    mainContainer.add(this._createVerticalSpacer(2));

    this.dosingFrequencyLabel = app.views.medications.MedicationUtils.crateLabel('TextLabel', this.view.getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyLabel);
    mainContainer.add(this.dosingFrequencyPane);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(this.therapyIntervalPane);
    if (!!this._therapySupplyContainer)
    {
      mainContainer.add(this._therapySupplyContainer);
    }
    mainContainer.add(this.therapyNextAdministrationLabelPane);

    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this.administrationPreviewTimeline = new app.views.medications.ordering.timeline.AdministrationPreviewTimeline({
        view: this.getView(),
        autoDraw: !this.isEditMode()
      });
      mainContainer.add(this.administrationPreviewTimeline);
    }

    mainContainer.add(this._createVerticalSpacer(2));

    mainContainer.add(this._antimicrobialTherapyStartContainer);
    mainContainer.add(this.commentIndicationPane);
    mainContainer.add(this._changeReasonContainer);

    if (!!this._informationSourceContainer)
    {
      mainContainer.add(this._informationSourceContainer);
    }

    this.add(this._extensionsPlaceholder);
    this.add(new app.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component calculation-details",
      titleText: this.view.getDictionary('calculated.dosing'),
      contentComponent: this.calculatedDosagePane,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }));

    if (!this.isEditMode())
    {
      this.add(new tm.jquery.Container({style: 'border-top: 1px solid #d6d6d6'}));
      this.add(this._createVerticalSpacer(7));

      var navigationContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 20),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        margin: '0 25 5 20'
      });

      this.linkTherapyButton.hide();
      this.unlinkTherapyButton.hide();
      this.linkIcon.hide();
      navigationContainer.add(this.templatesButton);
      navigationContainer.add(new tm.jquery.Spacer({type: 'horizontal', size: 50}));
      navigationContainer.add(this.simpleTherapyButton);
      if (!!this.saveToTemplateButton)
      {
        navigationContainer.add(this.saveToTemplateButton);
      }
      navigationContainer.add(this.linkTherapyButton);
      navigationContainer.add(this.linkIcon);
      navigationContainer.add(this.unlinkTherapyButton);
      if (!!this._showInformationSourceButton)
      {
        navigationContainer.add(this._showInformationSourceButton);
      }
      var addToBasketContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-start")
      });
      addToBasketContainer.add(this.addToBasketButton);
      navigationContainer.add(addToBasketContainer);
      this.add(navigationContainer);
    }

    this._applyChangeReasonContainerVisibility();
    this._rebuildExtensions();
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _setRateContainersVisible: function(isVisible)
  {
    if (isVisible)
    {
      this.infusionRatePane.show();
      this.rateLabelContainer.show();
      this.rateLabelVSpacer.hide();
      this.varioButton.show();
    }
    else
    {
      this.infusionRatePane.hide();
      this.rateLabelContainer.hide();
      this.rateLabelVSpacer.show();
      this.varioButton.hide();
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData|null} medicationData
   * @param {boolean} addSpacer
   * @param {boolean} medicationEditable
   * @param {boolean} sameGenericOnly
   * @param {boolean} addRemoveEnabled
   * @param {boolean} isMainMedication
   * @returns {app.views.medications.ordering.ComplexTherapyMedicationPane}
   * @private
   */
  _addMedicationPane: function(medicationData, addSpacer, medicationEditable, sameGenericOnly, addRemoveEnabled,
                               isMainMedication)
  {
    var self = this;

    var medicationPane = new app.views.medications.ordering.ComplexTherapyMedicationPane({
      view: this.view,
      addSpacer: addSpacer,
      medicationEditable: medicationEditable,
      medicationEditableSameGenericOnly: sameGenericOnly,
      addRemoveEnabled: addRemoveEnabled,
      medicationData: medicationData,
      titratedDoseSupported: self.medicationPanes.length === 0,
      additionalMedicationSearchFilter: self.additionalMedicationSearchFilter,
      preventUnlicensedMedicationSelection: self.preventUnlicensedMedicationSelection,
      preventTitrationChange: this.isEditMode() && !this.isCopyMode(),
      orderingBehaviour: this.getOrderingBehaviour(),
      referenceData: this.getReferenceData(),
      addElementEvent: function(medicationPane)
      {
        self.medicationPanes[0].hideAllTooltips(); // visible tooltips get broken when repainting the parent
        self._addMedicationPane(null, true, true, false, true, false);
        self._handleFirstMedicationPaneTitratedDoseModeSupport();
        self._handleFirstMedicationPaneDoseVisibility();
        self.medicationPanesContainer.repaint(); // repaint after values for visibility are set, because of the repaint yield
        self._showHideVolumeSum();
        if (!!self._therapySupplyContainer)
        {
          self._therapySupplyContainer.clear(true); // switch back to initial state with no controlled drug supply
        }
        self._executeOnValidContentExtensions(function(extension)
        {
          extension.onMedicationsCountChange(self.medicationPanes.length);
        });

        setTimeout(function()
        {
          self._focusToNextMedicationPane(medicationPane);
        }, 500);
      },
      removeElementEvent: function()
      {
        self._unregisterMedicationPaneCalculationFormulaDisplayProviders(this);
        if (self.medicationPanes.length > 1)
        {
          self.medicationPanes[0].hideAllTooltips(); // visible tooltips get broken when repainting the parent
          self._removeMedicationPane();
          self._handleFirstMedicationPaneTitratedDoseModeSupport();
          self._handleFirstMedicationPaneDoseVisibility();
          self.medicationPanesContainer.repaint(); // repaint after values for visibility are set, because of the repaint yield
          self._showHideVolumeSum();
          self.infusionRatePane.setFormulaVisible();
          self._calculateVolumeSum();
          self._handleParacetamolRuleChange();

          if (!self.infusionRatePane.isHidden())
          {
            self.infusionRatePane.calculateInfusionValues();
          }
          if (!!self.saveToTemplateButton)
          {
            if (self._existsUniversalMedication())
            {
              self.saveToTemplateButton.hide();
            }
            else
            {
              self.saveToTemplateButton.show();
            }
          }
          // reconfigure the supply container by the main medication again (controlled drug state & unit)
          if (!!self._therapySupplyContainer && self.medicationPanes.length === 1)
          {
            self._therapySupplyContainer.setOptionsByMedication(self.medicationPanes[0].getMedicationData());
          }
          self._executeOnValidContentExtensions(function(extension)
          {
            extension.onMedicationsCountChange(self.medicationPanes.length);
          });
          self._setInfusionTypePaneAvailability();
        }
        else
        {
          if (!self.isEditMode() && self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
            self.refreshBasketFunction();
          }
        }
      },
      volumeChangedEvent: function()
      {
        self._calculateVolumeSum();
        self._handleVarioEnabling();
        if (!self.infusionRatePane.isHidden())
        {
          self.infusionRatePane.calculateInfusionValues();
        }
      },
      numeratorChangeEvent: function()
      {
        self._calculateDosing(self);
        self.infusionRatePane.recalculateFromRate();
      },
      focusLostEvent: function(medicationPane)
      {
        self._focusToNextMedicationPane(medicationPane);
      },
      medicationChangedEvent: function(medicationData)
      {
        self.infusionRatePane.setFormulaVisible();
        self.infusionRatePane.setFormula(null, true);
        if (isMainMedication)
        {
          self._setMainMedicationData(medicationData);
        }
        if (medicationData.getMedication().isMedicationUniversal())
        {
          medicationPane.hideMedicationInfo(true);
          medicationPane.setMedication(medicationData.getMedication());

          if (!!self.saveToTemplateButton)
          {
            self.saveToTemplateButton.hide();
          }
        }
        else
        {
          medicationPane.hideMedicationInfo(false);
        }
        self._handleFirstMedicationPaneDoseVisibility();
        self._setInfusionTypePaneAvailability();
      }
    });

    if (this.medicationPanes.length === 0)
    {
      medicationPane.on(
          app.views.medications.ordering.ComplexTherapyMedicationPane.EVENT_TYPE_TITRATION_CHANGE,
          function(component, componentEvent)
          {
            var eventData = componentEvent.eventData;
            self._onTitrationDosingSelectionChanged(eventData && eventData.selected);
          });
    }

    var lastMedicationPaneIndex = this.medicationPanes.length - 1;
    if (lastMedicationPaneIndex >= 0)
    {
      this.medicationPanes[lastMedicationPaneIndex].setAddRemoveButtonsVisible(false);
    }
    if (medicationData)
    {
      if (medicationData.getMedication().isMedicationUniversal())
      {
        if (!!self.saveToTemplateButton)
        {
          self.saveToTemplateButton.hide();
        }
        medicationPane.hideMedicationInfo(true);
        var selectedRoutes = this.routesPane.getSelectedRoutes();
        if (this._isSingleRouteSelected(selectedRoutes) && medicationData.hasMatchingRouteWithMaxDose(selectedRoutes[0]))
        {
          medicationPane.setMaxDoseContainerVisibility(true);
          medicationPane.setMaxDoseValuesAndSelectedRoute(medicationData,
              this._getSelectedRouteFromMedication(medicationData));
        }
      }
      else
      {
        medicationPane.hideMedicationInfo(false);
      }
    }
    this.medicationPanes.push(medicationPane);
    this.medicationPanesContainer.add(medicationPane);
    this.calculatedDosagePane.registerCalculationFormulaProvider(medicationPane);
    this.calculatedDosagePane.registerCalculationFormulaProvider(medicationPane.getMaxDoseContainer());
    return medicationPane;
  },

  _focusToNextMedicationPane: function(medicationPane)
  {
    var index = this.medicationPanes.indexOf(medicationPane);
    var nextMedicationPane = this.medicationPanes[index + 1];
    if (nextMedicationPane)
    {
      nextMedicationPane.focusToMedicationField();
    }
    else
    {
      this.routesPane.requestFocus();
    }
  },

  _removeMedicationPane: function()
  {
    var lastMedicationPane = this.medicationPanes[this.medicationPanes.length - 1];
    this.medicationPanesContainer.remove(lastMedicationPane);
    this.medicationPanes.pop();   //removes last medicationPane
    var lastMedicationPaneIndex = this.medicationPanes.length - 1;
    this.medicationPanes[lastMedicationPaneIndex].setAddRemoveButtonsVisible(true);
  },

  _unregisterMedicationPaneCalculationFormulaDisplayProviders: function(medicationPane)
  {
    this.calculatedDosagePane.unregisterCalculationFormulaProvider(medicationPane);
    this.calculatedDosagePane.unregisterCalculationFormulaProvider(medicationPane.getMaxDoseContainer());
  },

  _registerCommonCalculationFormulaProviders: function()
  {
    this.calculatedDosagePane.registerCalculationFormulaProvider(this);
    this.calculatedDosagePane.registerCalculationFormulaProvider(this.infusionRatePane);
  },

  _openVariableRateEditPane: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var volumeSum = null;
    if (!this.volumeSumPane.isHidden())
    {
      volumeSum = this.volumeSumPane.getVolumeSum();
    }
    else if (this.medicationPanes.length === 1)
    {
      volumeSum = this.medicationPanes[0].getVolume();
    }

    var variableDosePane = new app.views.medications.ordering.ComplexVariableRateDataEntryContainer({
      view: self.view,
      startProcessOnEnter: true,
      medicationData: this.medicationData,
      timedDoseElements: this.timedDoseElements,
      recurringContinuousInfusion: this.recurringContinuousInfusion,
      infusionIngredients: this._getComplexTherapyIngredients(),
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      volumeSum: volumeSum,
      showFormula: this.getOrderingBehaviour().isDoseCalculationsAvailable() && this._areAnyIngredientsNotADiluent(),
      referenceData: this.getReferenceData()
    });

    var variableDoseEditDialog = appFactory.createDataEntryDialog(
        this.view.getDictionary('variable.dose'),
        null,
        variableDosePane,
        function(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.infusionRatePane.hide();
            self.dosingFrequencyPane.showDaysOnly();
            self.timedDoseElements = resultData.timedDoseElements;
            self.recurringContinuousInfusion = resultData.recurring;
            self._showVariableRateDisplayValue();
            if (self.recurringContinuousInfusion)
            {
              var nextAdministrationTimestamp =
                  app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(self.timedDoseElements);
              self.therapyIntervalPane.setStart(nextAdministrationTimestamp, true);
              self.therapyIntervalPane.setStartHourEnabled(true);
              self._updateNextAdministrationTime(nextAdministrationTimestamp);
            }
            else if (self.infusionRateTypePane.isContinuousInfusion())
            {
              self._setAndDisableStartHourForVario();
            }
            else
            {
              self._handleDosingFrequencyChange();
            }
            self._setMedicationsEditable();
            self._calculateDosing();
          }
        },
        550,
        330
    );
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(this.view.getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var removeVarioButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function()
    {
      self._removeVariableRate();
      self.dosingFrequencyPane.showAllFields();
      self.dosingFrequencyLabel.show();
      self._continuousInfusionChanged(self.infusionRateTypePane.isContinuousInfusion(), true);
      self._setRateContainersVisible(true);
      self._setMedicationsEditable();
      self._calculateDosing();
      self.infusionRatePane.requestFocus();
      removeVarioButtonHandler();
    });

    variableDoseEditDialog.getBody().footer.setRightButtons([removeVarioButton, variableDoseEditDialog.getBody().footer.confirmButton]);
    variableDoseEditDialog.show();
  },

  _removeVariableRate: function()
  {
    if (tm.jquery.Utils.isArray(this.timedDoseElements) && this.timedDoseElements.length > 0)
    {
      this.therapyIntervalPane.setStartHourEnabled(true);
      this.infusionRatePane.clear();
      this.variableRateContainer.hide();
      this.timedDoseElements.removeAll();
      this.recurringContinuousInfusion = false;
      this.refreshAdministrationPreview();
    }
  },

  _showVariableRateDisplayValue: function()
  {
    var utils = app.views.medications.MedicationUtils;
    this.variableRateContainer.removeAll();
    var diluentOnly = !this._areAnyIngredientsNotADiluent();
    for (var i = 0; i < this.timedDoseElements.length; i++)
    {
      var rowContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10)});
      var timedDoseElement = this.timedDoseElements[i];
      var doseTime = timedDoseElement.doseTime;
      var doseElement = timedDoseElement.doseElement;

      var startTimeDisplayValue = app.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute) + ' - ';
      rowContainer.add(utils.crateLabel('TextLabel', startTimeDisplayValue, '1 0 0 0'));

      if (doseElement.duration)
      {
        var endTime = CurrentTime.get();
        endTime.setHours(doseTime.hour);
        endTime.setMinutes(doseTime.minute + doseElement.duration);
        var endTimeDisplayValue = app.views.medications.MedicationTimingUtils.hourMinuteToString(endTime.getHours(), endTime.getMinutes()) + '  ';
        rowContainer.add(utils.crateLabel('TextLabel', endTimeDisplayValue, '1 0 0 0'));
      }
      else
      {
        rowContainer.add(utils.crateLabel('TextLabel', '...', '1 20 0 0'));
      }
      var doseDisplayValue = utils.getFormattedDecimalNumber(utils.doubleToString(doseElement.rate, 'n2')) + ' ' +
          utils.getFormattedUnit(doseElement.rateUnit, this.getView());
      if (!diluentOnly && doseElement.rateFormula)
      {
        doseDisplayValue += ' (' +
            utils.getFormattedDecimalNumber(utils.doubleToString(doseElement.rateFormula, 'n2')) + ' ' +
            doseElement.rateFormulaUnit + ')';
      }

      rowContainer.add(utils.crateLabel('TextData', doseDisplayValue, 0));
      this.variableRateContainer.add(rowContainer);
    }

    this.variableRateContainer.show();
    this.variableRateContainer.repaint();
  },

  _setMedicationsEditable: function()
  {
    var vario = this.timedDoseElements.length > 0;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    var enableAddingAndRemoving = !vario && (!this.isEditMode() || this.isCopyMode() || continuousInfusion);
    var doseEditable = !vario;

    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var isLastRow = i === this.medicationPanes.length - 1;
      var showAddRemoveButtons = enableAddingAndRemoving && isLastRow;
      var medicationEditable = !vario && (this.isEditMode() || i > 0);
      var medicationEditableSameGenericOnly = !vario && this.isEditMode() && !this.isCopyMode() &&
          !this.medicationPanes[i].getMedicationData().getMedication().isMedicationUniversal();
      if (continuousInfusion)
      {
        medicationEditableSameGenericOnly = medicationEditableSameGenericOnly && i === 0;
      }
      this.medicationPanes[i].setPaneEditable(
          showAddRemoveButtons, medicationEditable, medicationEditableSameGenericOnly, doseEditable);
    }
  },

  _setAndDisableStartHourForVario: function()
  {
    var viewMode = this.view.getViewMode();
    if (viewMode !== 'ORDERING_PAST' && viewMode !== 'EDIT_PAST')
    {
      var firstTimedDoseElement = this.timedDoseElements[0];
      var start = CurrentTime.get();
      start.setHours(firstTimedDoseElement.doseTime.hour);
      start.setMinutes(firstTimedDoseElement.doseTime.minute);
      this.therapyIntervalPane.setStart(start, true);
      this.therapyIntervalPane.setStartHourEnabled(false);
      this._updateNextAdministrationTime(start);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @returns {Object}
   */
  _getSelectedRouteFromMedication: function(medicationData)
  {
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    if (medicationData.getRoutes())
    {
      return medicationData.getRoutes().filter(function(route)
      {
        if (!tm.jquery.Utils.isEmpty(route.getMaxDose()))
        {
          return route.getId() === selectedRoutes[0].getId();
        }
      })[0];
    }
  },

  /**
   * @param {boolean} selected
   * @private
   */
  _onTitrationDosingSelectionChanged: function(selected)
  {
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(!selected);
    this.infusionRateTypePane.setTitratedDoseMode(selected, true);
    if (!this.infusionRateTypePane.isContinuousInfusion())
    {
      this._continuousInfusionChanged(false, false);
    }
    else
    {
      this._setRateContainersVisible(!selected);

      if (selected)
      {
        this.infusionRatePane.clear(true);
      }
      else
      {
        this.infusionRatePane.onInfusionRateTypeChanged(true);
      }
    }
    if (selected)
    {
      this._removeVariableRate();
    }
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();

    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      this._addValidations(this.medicationPanes[i].getMedicationPaneValidations());
    }

    this._addValidations(this.routesPane.getRoutesPaneValidations());
    this._addValidations(this.commentIndicationPane.getIndicationValidations());
    if (!this.infusionRatePane.isHidden())
    {
      this._addValidations(this.infusionRatePane.getInfusionRatePaneValidations());
    }
    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations(this._isWhenNeededSelected()));
      if (this.infusionRateTypePane.isSpeed())
      {
        this.validationForm.addFormField(this.dosingFrequencyPane.getDosingPatternPaneDurationValidation(
            this.infusionRatePane.getDuration(app.views.medications.TherapyEnums.knownUnitType.MIN)));
      }
    }
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(this._getMinimumStartTime()));
    }
    if (!!this._therapySupplyContainer)
    {
      this._addValidations(this._therapySupplyContainer.getFormValidations());
    }
    if (!this._antimicrobialTherapyStartContainer.isHidden())
    {
      this._addValidations(this._antimicrobialTherapyStartContainer.getValidators())
    }

    if (!this._changeReasonContainer.isHidden())
    {
      this._changeReasonContainer.attachRemoteFormValidation(
          this.getOriginalTherapy(),
          this._buildTherapy(),
          this.validationForm);
    }

    if (this._isIndicationRequired())
    {
      this._addValidations(this.commentIndicationPane.getIndicationFieldValidation());
    }
    if (this._isCommentRequired())
    {
      this._addValidations(this.commentIndicationPane.getCommentFieldValidation());
    }

    if (!!this._informationSourceContainer)
    {
      this._addValidations(this._informationSourceContainer.getFormValidations());
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      self._addValidations(extension.getFormValidations());
    });
  },

  _addValidations: function(validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
  },

  _showHideVolumeSum: function()
  {
    if (this.medicationPanes.length > 1)
    {
      if (!this.showHeparinPane)
      {
        this.isRendered() ? this.volumeSumTopSpacer.show() : this.volumeSumTopSpacer.setHidden(false);
      }
      this.isRendered() ? this.volumeSumPane.show() : this.volumeSumPane.setHidden(false) ;
    }
    else
    {
      this.isRendered() ? this.volumeSumPane.hide() : this.volumeSumPane.setHidden(true);
      if (!this.showHeparinPane)
      {
        this.isRendered() ? this.volumeSumTopSpacer.hide() : this.volumeSumTopSpacer.setHidden(true);
      }
    }
  },

  _handleDosingFrequencyChange: function()
  {
    this._calculateStartAndEnd(this._buildTherapy());
  },

  /*
   * Triggers the start and end time calculation with the help of the server API. When editing or copying an existing
   * therapy, the minimum therapy end date and time is intentionally unlimited, due to the behaviour of the minimum date and
   * time control algorithm, which enforces the minimum value in such a way that the date and time is automatically set to
   * the minimum, when we attempt to set an older time. This, in turn, can cause an unintentional extension of a prescribed
   * therapy, which is dangerous.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _calculateStartAndEnd: function(therapy)
  {
    var self = this;
    // only copy the end date when editing
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.getOriginalTherapy() : null;
    this.therapyIntervalPane.calculateStart(therapy, tm.jquery.Utils.isEmpty(oldTherapy), oldTherapy, function()
    {
      self.therapyIntervalPane.calculateEnd();
    });
  },

  _adjustVolumes: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var medsVolumeSum = this._getTherapyVolumeSumOfTypes([enums.medicationTypeEnum.MEDICATION]);
    var diluentVolumeSum = this._getTherapyVolumeSumOfTypes([enums.medicationTypeEnum.DILUENT]);
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var diluentData = this.medicationPanes[i].getMedicationData();
      if (diluentData && diluentData.getMedication().isDiluent())
      {
        var diluentVolume = this.medicationPanes[i].getVolume();
        var diluentRatio = diluentVolume / diluentVolumeSum;
        var newVolume = diluentVolume - medsVolumeSum * diluentRatio;
        this.medicationPanes[i].setVolume(newVolume);
      }
    }
    this._calculateVolumeSum();
  },

  _getTherapyVolumeSumOfTypes: function(types)
  {
    var volumeSum = 0;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var medicationData = this.medicationPanes[i].getMedicationData();
      if (medicationData)
      {
        var medicationType = medicationData.medication.medicationType;
        if ($.inArray(medicationType, types) > -1)
        {
          var volume = this.medicationPanes[i].getVolume();
          if (volume)
          {
            volumeSum += volume;
          }
        }
      }
    }
    return volumeSum;
  },

  /**
   * Attempts to calculate the total sum for mixtures and show it. We ignore calculated sums of 0 and rather clear the field
   * value intentionally, as such sums don't make sense. One such use case is when the mixture contains medications with
   * units other than volume units (e.g. mg), or if {@link app.views.medications.ordering.OrderingBehaviour#isDoseRequired}
   * is set to false and the user leaves the doses empty (editing such an order triggers volume sum recalculation).
   * @private
   */
  _calculateVolumeSum: function()
  {
    var volumeSum = 0;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var volume = this.medicationPanes[i].getVolume();
      if (volume)
      {
        volumeSum += volume;
      }
    }

    this.volumeSumPane.setVolumeSum(volumeSum || null);
    this._handleVarioEnabling();
  },

  _handleSpeedOrBolusChanged: function(rateContainersVisible)
  {
    this._handleFirstMedicationPaneDoseVisibility();
    this.infusionRatePane.clearFieldValues();
    this.dosingFrequencyLabel.show();
    this.dosingFrequencyPane.show();
    this._setRateContainersVisible(rateContainersVisible);
    //handle vario
    this.variableRateContainer.hide();
    this.timedDoseElements.removeAll();
    this._setMedicationsEditable();
    this.dosingFrequencyPane.showAllFields();
    this.therapyIntervalPane.setStartHourEnabled(true);
    this._handleVarioEnabling();
  },

  /**
   * @param {Boolean} continuousInfusion
   * @param {Boolean} [calculateStart=false]
   * @private
   */
  _continuousInfusionChanged: function(continuousInfusion, calculateStart)
  {
    this.infusionRatePane.onInfusionRateTypeChanged(continuousInfusion);
    this.infusionRatePane.setAllowZeroRate(continuousInfusion);
    this.infusionRatePane.setFormulaVisible();
    this._handleFirstMedicationPaneTitratedDoseModeSupport();
    if (continuousInfusion)
    {
      this._setRateContainersVisible(true);
      this.dosingFrequencyLabel.hide();
      this.dosingFrequencyPane.clear();
      this.dosingFrequencyPane.hide();
      this.therapyIntervalPane.setRestrictedStartHourSelection(false);
      this.therapyIntervalPane.hideWhenNeededDoctorsOrder();
    }
    else
    {
      this._setRateContainersVisible(false);
      this.dosingFrequencyPane.showAllFields();
      this.dosingFrequencyLabel.show();
      this.dosingFrequencyPane.show();
      if (!this.isEditMode() || this.isCopyMode())
      {
        this.therapyIntervalPane.setRestrictedStartHourSelection(true);
      }
      this.therapyIntervalPane.showWhenNeededDoctorsOrder();
    }
    this.therapyIntervalPane.setMaxDailyFrequencyFieldVisible(!continuousInfusion);
    this.therapyIntervalPane.setStartHourEnabled(true); // reset in case variable dose was set

    var therapy = this._buildTherapy();
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.getOriginalTherapy() : null;
    if (calculateStart)
    {
      this.therapyIntervalPane.calculateStart(therapy, true, oldTherapy, null);
    }

    this._handleFirstMedicationPaneDoseVisibility();
    this._handleVarioEnabling();
  },

  _handleFirstMedicationPaneDoseVisibility: function()
  {
    if (this.infusionRateTypePane.isContinuousInfusion() && this.medicationPanes.length === 1)
    {
      this.medicationPanes[0].setDoseVisible(false);
    }
    else if (this.medicationPanes.length > 0)
    {
      this.medicationPanes[0].setDoseVisible(tm.jquery.Utils.isEmpty(this.medicationPanes[0].getTitrationDoseType()));
    }
  },

  _handleFirstMedicationPaneTitratedDoseModeSupport: function()
  {
    if (this.medicationPanes.length > 0)
    {
      this.medicationPanes[0].setTitratedDoseSupported(
          !this.infusionRateTypePane.isContinuousInfusion() &&
          this.medicationPanes.length === 1 &&
          this.getOrderingBehaviour().isTitratedDoseModeAvailable());
    }
  },

  _handleVarioEnabling: function()
  {
    var volumeSumSet = !this.volumeSumPane.isHidden() && this.volumeSumPane.getVolumeSum() > 0 ||
        this.medicationPanes.length === 1 && this.medicationPanes[0].getVolume() > 0;
    var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
    this.varioButton.setEnabled(volumeSumSet || continuousInfusion);
  },

  /**
   * @private
   * @returns {boolean} true, if an ingredient exists that is not a diluting agent, otherwise false.
   */
  _areAnyIngredientsNotADiluent: function()
  {
    return this._getMedicationPanesWithSelectedMedication()
        .some(
            function isInfusionIngredientNotADiluent(pane)
            {
              var infusionIngredient = pane.getInfusionIngredient();
              return infusionIngredient && !infusionIngredient.medication.isDiluent();
            });
  },

  _isUniversalSingleIngredient: function()
  {
    return this.medicationPanes.length === 1 &&
        this.medicationPanes[0].getMedicationData().getMedication().isMedicationUniversal();
  },

  /**
   * @returns {boolean} true, if any medication is a high alert medication or antibiotic, otherwise false.
   * @private
   */
  _areAnyMedicationsHighAlertOrAntibiotic: function()
  {
    return this._getMedicationPanesWithSelectedMedication()
        .some(
            function isHighAlertOrAntibiotic(medicationPane)
            {
              return medicationPane.getMedicationData().isHighAlertMedication() ||
                  medicationPane.getMedicationData().isAntibiotic();
            });
  },

  /**
   * Returns an array of medication panes with selected medications. Useful when we want to skip a possible medication pane
   * with no medication selected.
   * @returns {Array<app.views.medications.ordering.ComplexTherapyMedicationPane>}
   * @private
   */
  _getMedicationPanesWithSelectedMedication: function()
  {
    return this.medicationPanes
        .filter(function(medicationPane)
        {
          return !!medicationPane.getMedicationData();
        })
  },

  _calculateDosing: function()
  {
    if (!this.getOrderingBehaviour().isReferenceDataBasedDoseCalculationAvailable())
    {
      return;
    }

    var view = this.getView();
    var ingredients = this._getComplexTherapyIngredients();
    if (ingredients.length === 1)
    {
      var quantity;
      var quantityUnit;
      var weightInKg = this.getReferenceData().getWeight();
      var heightInCm = this.getReferenceData().getHeight();

      if (this.infusionRateTypePane.isContinuousInfusion() && !this._isUniversalSingleIngredient())
      {
        quantityUnit = ingredients[0].quantityUnit;
        var rateFormulaUnit = this.infusionRatePane.getRateFormulaUnit();
        var formulaPatientUnit = rateFormulaUnit.patientUnit;
        var infusionRateFormulaInMgPerHour = this.infusionRatePane.getInfusionRateFormulaPerHour(quantityUnit);

        var calculatedInfusionRateWithPatientUnitFormula;

        if (view.getUnitsHolder().isUnitInMassGroup(formulaPatientUnit))
        {
          // Presumed patient mass unit is KG
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour * weightInKg;
        }
        else if (view.getUnitsHolder().isUnitInSurfaceGroup(formulaPatientUnit))
        {
          // Presumed patient surface unit is M2
          var calculatedBodySurfaceArea = this.getReferenceData().getBodySurfaceArea();
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour * calculatedBodySurfaceArea;
        }
        else
        {
          calculatedInfusionRateWithPatientUnitFormula = infusionRateFormulaInMgPerHour;
        }

        quantity = !tm.jquery.Utils.isEmpty(infusionRateFormulaInMgPerHour) ?
            calculatedInfusionRateWithPatientUnitFormula :
            null;
        this.calculatedDosagePane.calculate(quantity, quantityUnit, null, weightInKg, heightInCm, true);
      }
      else
      {
        quantity = ingredients[0].quantity;
        quantityUnit = ingredients[0].quantityUnit;
        var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
        this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
        this.medicationPanes[0].overdosePane.calculateOverdose(quantity);
      }
    }
    else
    {
      this.calculatedDosagePane.clear();
    }

    if (this.medicationPanes.length >= 1)
    {
      var selectedRoutes = this.routesPane.getSelectedRoutes();
      if(this._isSingleRouteSelected(selectedRoutes))
      {
        for (var j = 0; j < this.medicationPanes.length; j++)
        {
          var medicationData = this.medicationPanes[j].getMedicationData();
          if (!!medicationData && medicationData.hasMatchingRouteWithMaxDose(selectedRoutes[0]))
          {
            this._handleMaxDoseChange(this.medicationPanes[j], medicationData);
          }
        }
      }
    }

    if (ingredients.length >= 1)
    {
      this._handleParacetamolRuleChange();
    }
  },

  /**
   * @param {app.views.medications.ordering.ComplexTherapyMedicationPane} medicationPane
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _handleMaxDoseChange: function(medicationPane, medicationData)
  {
    var ingredient = medicationPane.getInfusionIngredient();
    var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
    var quantity = !tm.jquery.Utils.isEmpty(ingredient) ? ingredient.quantity : null;
    var variable = false;

    if (this.timedDoseElements.length > 0)
    {
      variable = true;
      quantity = app.views.medications.MedicationUtils.calculateVariablePerDay(this.timedDoseElements);
    }
    medicationPane.setMaxDoseContainerVisibility(true);
    medicationPane.setMaxDoseValuesAndSelectedRoute(medicationData,
        this._getSelectedRouteFromMedication(medicationData));
    medicationPane.calculateMaxDosePercentage(quantity, timesPerDay, this.dosingFrequencyPane.getTimesPerWeek(), variable);
  },

  _handleParacetamolRuleChange: function()
  {
    var self = this;
    var medicationIngredientRuleEnum = app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE;

    var medicationDataList = [];
    var containsParacetamol = false;
    var paracetamolMedicationPane = null;

    for (var j = 0; j < this.medicationPanes.length; j++)
    {
      var currentPane = this.medicationPanes[j];
      var paracetamolRuleSet =
          self.medicationRuleUtils.isMedicationRuleSet(currentPane.getMedicationData(), medicationIngredientRuleEnum);
      containsParacetamol = containsParacetamol || paracetamolRuleSet;

      medicationDataList.push(currentPane.getMedicationData());

      if (!paracetamolMedicationPane && paracetamolRuleSet)
      {
        paracetamolMedicationPane = currentPane;
      }
    }

    if (containsParacetamol)
    {
      self.medicationRuleUtils.getParacetamolRuleForTherapy(
          this._buildTherapy(),
          medicationDataList).then(
          function validationSuccessHandler(medicationRuleResult)
          {
            if (self.isRendered())
            {
              paracetamolMedicationPane.setCalculatedParacetamolLimit(medicationRuleResult);
            }
          });
    }
    else
    {
      for (var i = 0; i < this.medicationPanes.length; i++)
      {
        this.medicationPanes[i].paracetamolLimitContainer.hideAll();
      }
    }
  },

  _openLinkTherapyDialog: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_GET_LINK_THERAPY_CANDIDATES_HUB;
    viewHubNotifier.actionStarted(hubAction);
    this.view.showLoaderMask();

    var params = {
      patientId: this.view.getPatientId(),
      referenceWeight: this.getReferenceData().getWeight(),
      patientHeight: this.getReferenceData().getHeight()
    };

    var getTherapiesForLinkUrl = this.view.getViewModuleUrl() +
        tm.views.medications.TherapyView.SERVLET_PATH_GET_LINK_THERAPY_CANDIDATES;
    this.view.loadGetViewData(getTherapiesForLinkUrl, params, null,
        function(linkTherapyCandidates)
        {
          self.view.hideLoaderMask();
          var actTherapies = [];
          linkTherapyCandidates.forEach(function(therapy)
          {
            actTherapies.push(new app.views.medications.common.TherapyJsonConverter.convert(therapy));
          });
          actTherapies = actTherapies.concat(self.getBasketTherapiesFunction());
          var dialog = appFactory.createDataEntryDialog(
              self.view.getDictionary('link'),
              null,
              new app.views.medications.ordering.LinkTherapyPane({
                view: self.view,
                orderedTherapies: actTherapies
              }),
              function(resultData)
              {
                if (resultData)
                {
                  var linkTherapy = resultData.selectedTherapy;
                  if (!linkTherapy.linkName)
                  {
                    linkTherapy.linkName = self.view.getPatientNextLinkName();
                  }
                  self.linkName = app.views.medications.MedicationUtils.getNextLinkName(linkTherapy.linkName);

                  if (linkTherapy.end)
                  {
                    if (self.therapyIntervalPane.getEnd() && self.therapyIntervalPane.getStart())
                    {
                      var diff = linkTherapy.end - self.therapyIntervalPane.getStart();
                      self.therapyIntervalPane.setEnd(
                          new Date(self.therapyIntervalPane.getEnd().getTime() + diff));
                    }
                    self.therapyIntervalPane.setStart(new Date(linkTherapy.end));
                    self._handleTherapyLinkButtonDisplay();
                    self.refreshBasketFunction();
                    self.linkedTherapy = linkTherapy;
                  }
                }
              },
              750,
              480
          );

          dialog.show();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  _unlinkTherapy: function()
  {
    var isSecondLinkedTherapy = this.linkName && this.linkName.length === 2 && this.linkName.charAt(1) === '2';
    if (isSecondLinkedTherapy) //clear linkName on first therapy
    {
      var previousLinkName = this.linkName.charAt(0) + '1';
      var basketTherapies = this.getBasketTherapiesFunction();
      for (var i = 0; i < basketTherapies.length; i++)
      {
        if (basketTherapies[i].linkName === previousLinkName)
        {
          basketTherapies[i].linkName = null;
          break;
        }
      }
    }
    this.linkName = null;
    this.linkedTherapy = null;
  },

  _infusionRateTypePaneFunction: function(rate, preventEvent)
  {
    if (!tm.jquery.Utils.isEmpty(rate))
    {
      if (rate === app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS)
      {
        this.infusionRateTypePane.markAsBolus(preventEvent);
      }
      else
      {
        if (!this.infusionRateTypePane.isContinuousInfusion())
        {
          this.infusionRateTypePane.markAsSpeed(preventEvent);
        }
        this.infusionRatePane.setRate(rate, preventEvent);
      }
    }
  },

  _getComplexTherapyIngredients: function()
  {
    var ingredients = [];
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      var infusionIngredient = this.medicationPanes[i].getInfusionIngredient();
      if (infusionIngredient)
      {
        ingredients.push(infusionIngredient);
      }
    }
    return ingredients;
  },

  _confirm: function()
  {
    var confirmEventData = new app.views.medications.ordering.ConfirmOrderEventData({
      therapyOrder: this._buildTherapyOrder()
    });

    var confirmSuccess = this.confirmOrderEventCallback(confirmEventData);
    if (confirmSuccess === false)
    {
      this.addToBasketButton.setEnabled(true);
    }
    else if (!!confirmEventData.getTherapyOrder().getTherapy().getLinkName())
    {
      this.view.setPatientLastLinkNamePrefix(confirmEventData.getTherapyOrder().getTherapy().getLinkName().substring(0, 1));
    }

    if (this.view.getViewMode() === 'ORDERING_PAST')
    {
      this.view.setPresetDate(confirmEventData.getTherapyOrder().getTherapy().getStart());
    }
  },

  /**
   * @returns {string}
   * @private
   */
  _getDoseType: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var multipleIngredients = this.medicationPanes.length > 1;

    if (this.infusionRateTypePane.isContinuousInfusion())
    {
      return enums.therapyDoseTypeEnum.RATE;
    }
    else if (this.infusionRateTypePane.isSpeed())
    {
      return multipleIngredients ? enums.therapyDoseTypeEnum.RATE_VOLUME_SUM : enums.therapyDoseTypeEnum.RATE_QUANTITY;
    }
    else
    {
      return multipleIngredients ? enums.therapyDoseTypeEnum.VOLUME_SUM : enums.therapyDoseTypeEnum.QUANTITY;
    }
  },

  /**
   * Based on the current state of the form. Includes a new instance of a therapy.
   * @return {app.views.medications.ordering.TherapyOrder}
   * @private
   */
  _buildTherapyOrder: function()
  {
    return new app.views.medications.ordering.TherapyOrder()
        .setTherapy(this._buildTherapy())
        .setTherapyChangeReason(
            !this._changeReasonContainer.isHidden() ?
                this._changeReasonContainer.getTherapyChangeReason() :
                null)
        .setLinkedTherapy(this.getLinkedTherapy())
        .setRecordAdministration(this.therapyIntervalPane.isRecordAdministration());
  },

  /**
   * Based on the current state of the order form.
   * @return {app.views.medications.common.dto.Therapy}
   * @private
   */
  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;

    var variableRate = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var maxDosePercentage = null;
    for (var j = 0; j < this.medicationPanes.length; j++)
    {
      if (this.medicationPanes[j].isShowMaxDose())
      {
        maxDosePercentage += this.medicationPanes[j].getMaxDosePercentage();
      }
    }
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    // Unlike variable doses, the variable rate isn't determined by the number of doses, and the main frequency pane
    // is in a limited functionally mode, without the ability to determine the frequency pattern. To keep data logic
    // persistent, we have to set the frequency manually - to 1 daily count.
    var dosingFrequency = !this.dosingFrequencyPane.isHidden() ?
        (variableRate ?
            app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
                enums.dosingFrequencyTypeEnum.DAILY_COUNT,
                1) :
            this.dosingFrequencyPane.getFrequency()) :
        null;

    var therapy = {                                                            // [ComplexTherapyDto.java]
      medicationOrderFormType: enums.medicationOrderFormType.COMPLEX,
      variable: variableRate,
      ingredientsList: this._getComplexTherapyIngredients(),
      routes: selectedRoutes,
      continuousInfusion: this.infusionRateTypePane.isContinuousInfusion(),
      adjustToFluidBalance: this.infusionRateTypePane.isAdjustToFluidBalance(),
      volumeSum: !this.volumeSumPane.isHidden() ? this.volumeSumPane.getVolumeSum() : null,
      volumeSumUnit: !this.volumeSumPane.isHidden() ? "ml" : null,
      additionalInstruction: this.showHeparinPane === true ? this.heparinPane.getHeparinValue() : null,
      baselineInfusion: this.infusionRateTypePane.isBaselineInfusion(),
      dosingFrequency: dosingFrequency,
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      doseType: this._getDoseType(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: !this.therapyIntervalPane.isHidden() ? this.therapyIntervalPane.getStart() : null,
      end: !this.therapyIntervalPane.isHidden() ? this.therapyIntervalPane.getEnd() : null,
      whenNeeded: !!this._therapySupplyContainer ?
          this._therapySupplyContainer.getWhenNeeded() :
          this.therapyIntervalPane.getWhenNeeded(),
      startCriterion: !this.therapyIntervalPane.isHidden() ? this.therapyIntervalPane.getStartCriterion() : null,
      reviewReminderDays: this.dosingFrequencyPane.getReviewReminderDays(),
      reviewReminderComment: this.dosingFrequencyPane.getReviewReminderComment(),
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentIndicationPane.getComment(),
      clinicalIndication: this.commentIndicationPane.getIndication(),
      dispenseDetails: !!this._therapySupplyContainer ? this._therapySupplyContainer.getDispenseDetails() : null,
      criticalWarnings: this._overridenCriticalWarnings ? this._overridenCriticalWarnings : [],
      linkName: this.linkName,
      maxDosePercentage: maxDosePercentage,
      admissionId: !this.isCopyMode() && !tm.jquery.Utils.isEmpty(this.getOriginalTherapy()) ?
          this.getOriginalTherapy().getAdmissionId() :
          null,
      titration: this._getTitrationType(),
      informationSources: !!this._informationSourceContainer ?
          this._informationSourceContainer.getSelections() :
          (!this.isCopyMode() && !!this.getOriginalTherapy() ?
              this.getOriginalTherapy().getInformationSources().slice(0) :
              [])
    };

    therapy.pastTherapyStart = this._antimicrobialTherapyStartContainer.getDateTime();

    if (variableRate)                  // [VariableComplexTherapyDto.java]
    {
      therapy.timedDoseElements = this.timedDoseElements.slice();
      therapy.recurringContinuousInfusion = this.recurringContinuousInfusion;
    }
    else                                // [ConstantComplexTherapyDto.java]
    {
      var infusionRate = this.infusionRatePane.getInfusionRate();
      if (infusionRate.duration)
      {
        infusionRate.duration = Math.round(infusionRate.duration);
      }
      if (infusionRate === app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS)
      {
        therapy.rateString = infusionRate;
      }
      else
      {
        therapy.doseElement = infusionRate;
      }
      var dosingPattern = this.dosingFrequencyPane.getDosingPattern();
      var frequencyType = this.dosingFrequencyPane.getFrequencyType();
      if (frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        therapy.doseTimes = dosingPattern.length > 0 ? [dosingPattern[0]] : [];
      }
      else
      {
        therapy.doseTimes = dosingPattern;
      }
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.attachTherapyConfig(therapy);
    });

    return new app.views.medications.common.dto.Therapy(therapy);
  },

  /**
   * @returns {string|null}
   * @private
   */
  _getTitrationType: function()
  {
    if (this.infusionRateTypePane.isContinuousInfusion())
    {
      return this.infusionRateTypePane.isTitratedRate() ? this.medicationData.getTitration() : null;
    }

    return this.medicationPanes[0] ? this.medicationPanes[0].getTitrationDoseType() : null;
  },

  /**
   * Returns the minimum value of the therapy start time. Should be used both for adjusting the start time if required
   * and validation. Normally there should be no limit when ordering, but when we edit an existing therapy this time should
   * be the current time, unless we're prescribing with the development trick that enable us to adjust the start into the
   * past.
   * @return {null}
   * @private
   */
  _getMinimumStartTime: function()
  {
    return this.isEditMode() && !this.isCopyMode() && !this.getOrderingBehaviour().isPastMode() ?
        (tm.jquery.Utils.isFunction(this.getTherapyStartNotBeforeDateFunction) ?
            this.getTherapyStartNotBeforeDateFunction() :
            moment(CurrentTime.get()).startOf('minute')) :
        null;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>|null} selectedRoutes
   * @returns {boolean}
   * @private
   */
  _isSingleRouteSelected: function(selectedRoutes)
  {
    return tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1;
  },

  clear: function()
  {
    this._originalTherapy = null;
    this.linkName = null;
    this.linkedTherapy = null;
    if (this.linkTherapyButton)
    {
      this.linkTherapyButton.hide();
      this.unlinkTherapyButton.hide();
      this.linkIcon.hide();
    }
    this.medicationPanesContainer.removeAll(true);
    this.medicationPanes.removeAll();
    if (this.showHeparinPane === true) this.heparinPane.clear();
    this.volumeSumPane.clear();
    this.routesPane.clear();
    this.infusionRateTypePane.clear(true);
    this.infusionRatePane.clear(true);
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    // reset the restricted start mode as it was when constructed, since the component doesn't hold the state any more
    this.therapyIntervalPane.setRestrictedStartHourSelection(true);
    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.clear();
    }
    this._antimicrobialTherapyStartContainer.clear();
    this._antimicrobialTherapyStartContainer.hide();
    this.commentIndicationPane.clear();
    this._setRateContainersVisible(false);
    this.dosingFrequencyPane.show();
    this.dosingFrequencyLabel.show();
    this.variableRateContainer.hide();
    this.timedDoseElements.removeAll();
    this.recurringContinuousInfusion = false;
    this._setMedicationsEditable();
    this.infusionRatePane.requestFocus();
    if (!!this._informationSourceContainer)
    {
      this._applyInformationSourceVisibility(false);
      this._informationSourceContainer.clear();
    }
    if (this.refreshBasketFunction)
    {
      this.refreshBasketFunction();
    }
    this._changeReasonContainer.clear();
    this.calculatedDosagePane.clear();
    if (!!this.saveToTemplateButton)
    {
      this.saveToTemplateButton.show();
    }
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.clear();
    });
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _setMainMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    var routes = !tm.jquery.Utils.isEmpty(medicationData.routes) ? medicationData.routes : this.view.getRoutes();
    this.routesPane.setRoutes(routes, medicationData.defaultRoute);
    this.infusionRatePane.setFirstMedicationData(medicationData, true);
    this.commentIndicationPane.setMedicationData(medicationData);
    this.infusionRateTypePane.setTitratedRateSupported(!tm.jquery.Utils.isEmpty(medicationData.getTitration()));
    this.dosingFrequencyPane.setMedicationData(medicationData);
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setMedicationData(medicationData);
    });
  },

  _handleTherapyLinkButtonDisplay: function()
  {
    if (this.linkTherapyButton)
    {
      var continuousInfusion = this.infusionRateTypePane.isContinuousInfusion();
      if (continuousInfusion)
      {
        if (!tm.jquery.Utils.isEmpty(this.linkName))
        {
          this.linkTherapyButton.hide();
          this.linkIcon.setHtml(this.linkName);
          this.linkIcon.show();

          var basketTherapies = this.getBasketTherapiesFunction();
          var otherTherapiesLinkedToTherapy =
              app.views.medications.MedicationUtils.areOtherTherapiesLinkedToTherapy(this.linkName, basketTherapies);
          if (!otherTherapiesLinkedToTherapy)
          {
            this.unlinkTherapyButton.show();
          }
        }
        else
        {
          this.linkTherapyButton.show();
          this.unlinkTherapyButton.hide();
          this.linkIcon.setHtml("");
          this.linkIcon.hide();
        }
      }
      else
      {
        this.linkTherapyButton.hide();
        this.unlinkTherapyButton.hide();
        this.linkIcon.setHtml("");
        this.linkIcon.hide();
        this._unlinkTherapy();
        this.refreshBasketFunction();
      }
    }
  },

  _rebuildExtensions: function()
  {
    this._extensionsPlaceholder.removeAll();
    this.contentExtensions.forEach(function(extension)
    {
      this._extensionsPlaceholder.add(extension);
    }, this);

    if (this._extensionsPlaceholder.isRendered()) this._extensionsPlaceholder.repaint();
  },

  _applyChangeReasonContainerVisibility: function()
  {
    if (this.isChangeReasonAvailable())
    {
      this.isRendered() ? this._changeReasonContainer.show() : this._changeReasonContainer.setHidden(false);
    }
    else
    {
      this.isRendered() ? this._changeReasonContainer.hide() : this._changeReasonContainer.setHidden(true);
    }
  },

  /**
   * Applies the visibility of the source of information input field and the corresponding button that toggles it's
   * visibility, when the source of information input is not required. Does nothing if the source of information
   * field is either not available or the input is required. If the field should be hidden, it's selection is also
   * cleared to prevent accidental application of a hidden value.
   * @param {boolean} visible
   * @private
   */
  _applyInformationSourceVisibility: function(visible)
  {
    if (!this._informationSourceContainer || this.getOrderingBehaviour().isInformationSourceRequired())
    {
      return;
    }

    if (visible === true)
    {
      this._informationSourceContainer.isRendered() ?
          this._informationSourceContainer.show() :
          this._informationSourceContainer.setHidden(false);
      this._showInformationSourceButton.isRendered() ?
          this._showInformationSourceButton.hide() :
          this._showInformationSourceButton.setHidden(true);
    }
    else
    {
      this._informationSourceContainer.isRendered() ?
          this._informationSourceContainer.hide() :
          this._informationSourceContainer.setHidden(true);
      this._showInformationSourceButton.isRendered() ?
          this._showInformationSourceButton.show() :
          this._showInformationSourceButton.setHidden(false);
    }
  },

  _executeOnValidContentExtensions: function(callback)
  {
    if (tm.jquery.Utils.isEmpty(callback)) return;

    var contentExtensions = tm.jquery.Utils.isEmpty(this.contentExtensions) ? [] : this.contentExtensions;

    contentExtensions.forEach(function(extension)
    {
      if (extension instanceof app.views.medications.ordering.PrescriptionContentExtensionContainer)
      {
        callback(extension);
      }
    }, this);
  },

  _existsUniversalMedication: function()
  {
    var universalMedicationFound = false;
    for (var i = 0; i < this.medicationPanes.length; i++)
    {
      if (this.medicationPanes[i].getMedicationData() &&
          this.medicationPanes[i].getMedicationData().getMedication().isMedicationUniversal())
      {
        universalMedicationFound = true;
        break;
      }
    }
    return universalMedicationFound;
  },

  /**
   * @private
   */
  _setInfusionTypePaneAvailability: function()
  {
    var view = this.getView();
    if (!this.isEditMode() || this.isCopyMode())
    {
      var medicationWithVolumeUnitAvailable = false;
      for (var i = 0; i < this.medicationPanes.length; i++)
      {
        var medicationData = this.medicationPanes[i].getMedicationData();
        if (medicationData && (view.getUnitsHolder().isUnitInLiquidGroup(medicationData.getStrengthNumeratorUnit()) ||
            view.getUnitsHolder().isUnitInLiquidGroup(medicationData.getStrengthDenominatorUnit())))
        {
          medicationWithVolumeUnitAvailable = true;
          break;
        }
      }

      if (!medicationWithVolumeUnitAvailable)
      {
        this.infusionRateTypePane.clearSelection();
      }

      this.infusionRateTypePane.setPreventRateTypeChange(
          !medicationWithVolumeUnitAvailable ||
          (this._isUniversalSingleIngredient() &&
              this.infusionRateTypePane.isContinuousInfusion() &&
              (this.isEditMode() || this.isCopyMode())));

      this.infusionRateTypePane.setRateGroupTooltip(
          !medicationWithVolumeUnitAvailable ?
              this.getView().getDictionary("ingredient.with.volume.required") :
              null);
    }
  },

  /**
   * @param {Date} nextAdministrationTime
   * @private
   */
  _updateNextAdministrationTime: function(nextAdministrationTime)
  {
    this.therapyNextAdministrationLabelPane.setNextAdministration(nextAdministrationTime);
    var durationGiven = this.infusionRateTypePane.isSpeed() && !this.infusionRatePane.isHidden() ?
        !tm.jquery.Utils.isEmpty(this.infusionRatePane.getInfusionRate().duration) :
        true;

    if (durationGiven)
    {
      this.refreshAdministrationPreview();
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _setMedicationData: function(medicationData)
  {
    this._setMainMedicationData(medicationData);
    var medicationEditable = this.isEditMode() && !medicationData.getMedication().isMedicationUniversal();
    var sameGenericOnly = this.isEditMode() && !medicationData.getMedication().isMedicationUniversal();
    this._addMedicationPane(medicationData, false, medicationEditable, sameGenericOnly, true, true);

    this._showHideVolumeSum();
    this.infusionRatePane.setFormulaVisible();
    this.validationForm.reset();
    this._handleVarioEnabling();
    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }

    if (medicationData.isAntibiotic() && this.getOrderingBehaviour().isPastDaysOfTherapyVisible())
    {
      this._antimicrobialTherapyStartContainer.show();
    }

    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setOptionsByMedication(medicationData);
    }
  },

  /**
   * Indications are required for certain prescription types, unless the rules are turned off via ordering behavior.
   * @returns {boolean}
   * @private
   */
  _isIndicationRequired: function()
  {
    return !this.orderingBehaviour.isIndicationAlwaysOptional() &&
        (this._areAnyMedicationsHighAlertOrAntibiotic() || this._isWhenNeededSelected());
  },

  /**
   * When formulary medication lists are configured, prescribing medications not classified as formulary (which includes
   * universal prescriptions by definition) is discouraged and a reason for such a selection must be entered into the comment
   * field.
   * @returns {boolean}
   * @private
   */
  _isCommentRequired: function()
  {
    return !this.orderingBehaviour.isCommentAlwaysOptional() &&
        (this.view.isFormularyFilterEnabled() ? this._isNonFormularyMedicationSelected() : false);
  },

  /**
   * True if one of the selected medications is non formulary.
   * @return {boolean}
   * @private
   */
  _isNonFormularyMedicationSelected: function()
  {
    return this._getMedicationPanesWithSelectedMedication().some(
            function isNonFormulary(medicationPane)
            {
              return !medicationPane.getMedicationData().isFormulary();
            });
  },

  /**
   * Returns true if "when needed" (PRN) is selected. When needed is selected on either {@link #therapyIntervalPane} or
   * {@link #_therapySupplyContainer}, depending on which of the two is available in a given context.
   * @returns {boolean}
   * @private
   */
  _isWhenNeededSelected: function()
  {
    var therapyIntervalWhenNeeded = !!this.therapyIntervalPane &&
        !this.therapyIntervalPane.isHidden() &&
        this.therapyIntervalPane.getWhenNeeded();
    var therapySupplyWhenNeeded = !!this._therapySupplyContainer &&
        !this._therapySupplyContainer.isHidden() &&
        this._therapySupplyContainer.getWhenNeeded();
    return therapyIntervalWhenNeeded || therapySupplyWhenNeeded;
  },

  /**
   * Event handler for the {@link #saveToTemplateButton} click.
   * @private
   */
  _addToTemplate: function()
  {
    if (!this.saveOrderToTemplateEventCallback)
    {
      return;
    }

    this._setupValidation();

    this.saveOrderToTemplateEventCallback(
        new app.views.medications.ordering.SaveOrderToTemplateEventData({
          therapyOrder: this._buildTherapyOrder(),
          validationPassed: !this.validationForm.hasFormErrors()
        }));
  },

  validateAndConfirmOrder: function()
  {
    if (this.isEditMode() && !this.isCopyMode())
    {
      this.therapyIntervalPane.adjustStartTimeToOrderTime(this._getMinimumStartTime(), true);
    }
    this._setupValidation();
    this.validationForm.submit();
  },

  /**
   * Handles infusion duration change. If no duration is provided, the {@link #administrationPreviewTimeline} will be
   * cleared.
   * @param {number|null} duration
   * @private
   */
  _handleDurationChange: function(duration)
  {
    this.therapyIntervalPane.calculateEnd();
    if (!!duration)
    {
      this.refreshAdministrationPreview();
    }
    else
    {
      this.administrationPreviewTimeline.clear();
    }
  },

  /**
   * This method should not execute any Ajax requests and change the UI values when the request returns,
   * since it has to be safe to execute the render right after it!
   *
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>} medicationData
   */
  setMedicationDataFromTherapy: function(therapy, medicationData)
  {
    this._setMedicationData(medicationData[0]);
    // reconfigure the supply container for multiple ingredients (fallback unit, no control drug details)
    if (medicationData.length > 1 && !!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.clear(true);
    }
    for (var i = 1; i < therapy.getIngredientsList().length; i++)
    {
      var infusionIngredient = therapy.getIngredientsList()[i];

      // passing the infusionIngredient as the execution context to prevent accessing a mutable value
      // from outside the closure
      var ingredientMedicationData =
          app.views.medications.MedicationUtils.findInArray(
              medicationData,
              function(currentElement)
              {
                return currentElement.getMedication().getId() === this.medication.getId();
              },
              infusionIngredient);

      if (!ingredientMedicationData && !infusionIngredient.medication.isMedicationUniversal())
      {
        continue;
      } // skip if not found (anymore?)

      var medicationEditable = !this.isEditMode() && !infusionIngredient.medication.isMedicationUniversal();
      var sameGenericOnly = this.isEditMode() && !therapy.continuousInfusion &&
          !infusionIngredient.medication.isMedicationUniversal();
      var medicationPane = this._addMedicationPane(
          ingredientMedicationData,
          true,
          medicationEditable,
          sameGenericOnly,
          true,
          false);

      if (!infusionIngredient.medication.isMedicationUniversal())
      {
        medicationPane.setDose(infusionIngredient.quantity, infusionIngredient.quantityDenominator, true);
      }
      else
      {
        medicationPane.setUniversalMedicationAndDose(
            app.views.medications.MedicationUtils.getMedicationDataFromComplexTherapy(this.getView(), therapy, i),
            infusionIngredient.quantity,
            infusionIngredient.quantityDenominator,
            true);
      }
    }
    this._setInfusionTypePaneAvailability();

    if (this.medicationPanesContainer.isRendered())
    {
      this.medicationPanesContainer.repaint();
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationDataBySearch: function(medicationData)
  {
    var self = this;
    this._setMedicationData(medicationData);
    this._setInfusionTypePaneAvailability();
    if (this.medicationPanesContainer.isRendered())
    {
      this.medicationPanesContainer.repaint();
      setTimeout(function()
      {
        self.medicationPanes[0].requestFocusToDose();
      }, 0);
    }
  },

  /**
   * This method can load data via an Ajax call and thus has to be called only when you are certain the
   * whole container was already rendered/repainted. Otherwise changes will not be correctly propagated to the DOM!
   *
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [therapyModifiedInThePast=false]
   * @param {Boolean} [isPastMode=false]
   */
  setComplexTherapy: function(therapy, therapyModifiedInThePast, isPastMode)
  {
    this._originalTherapy = therapy;

    var editTimestamp = CurrentTime.get();
    editTimestamp.setSeconds(0);
    editTimestamp.setMilliseconds(0);

    var therapyHasAlreadyStarted = new Date(therapy.getStart()) < editTimestamp;

    var doseElement = therapy.getDoseElement();
    var timedDoseElements = therapy.getTimedDoseElements();
    var variable = therapy.isVariable();
    var setTherapyStart = !tm.jquery.Utils.isEmpty(therapy.getStart());

    if (this.isCopyMode() || this.isEditMode())
    {
      setTherapyStart = !therapyHasAlreadyStarted || isPastMode === true;
      if (therapy.isContinuousInfusion() &&
          therapy.isVariable() &&
          !therapy.isRecurringContinuousInfusion())
      {
        if (this.isCopyMode())
        {
          timedDoseElements = therapy.getRescheduledVariableRateTiming();
        }
        else if (therapyHasAlreadyStarted)
        {
          variable = false;
          if (tm.jquery.Utils.isArray(therapy.getTimedDoseElements()) &&
              therapy.getTimedDoseElements().length > 0)
          {
            doseElement = therapy.getTimedDoseElements()[therapy.getTimedDoseElements().length - 1].doseElement;
          }
          timedDoseElements = [];
        }
      }
    }

    var firstMedicationPane = this.medicationPanes[0];
    firstMedicationPane.setDose(
        therapy.getIngredientsList()[0].quantity,
        therapy.getIngredientsList()[0].quantityDenominator,
        true);

    if (therapy.isTitrationDoseType() && !therapy.isContinuousInfusion())
    {
      firstMedicationPane.setTitrationDoseType(therapy.getTitration(), true);
      this.infusionRateTypePane.setTitratedDoseMode(true, true);
      this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(false);
    }

    this._calculateVolumeSum();
    this._showHideVolumeSum();

    if (this.showHeparinPane === true)
    {
      this.heparinPane.setHeparinValue(therapy.getAdditionalInstruction());
    }

    this._handleFirstMedicationPaneDoseVisibility();
    this.infusionRatePane.setFormulaVisible();

    this.routesPane.setSelectedRoute(therapy.getRoutes());

    if (therapy.isContinuousInfusion())
    {
      this.infusionRateTypePane.markAsContinuousInfusion(
          therapy.isBaselineInfusion(),
          therapy.isTitrationDoseType(),
          therapy.isAdjustToFluidBalance(),
          false,
          true);
      this._continuousInfusionChanged(true, false);
      this._setInfusionTypePaneAvailability();
    }

    if (therapy.isAdjustToFluidBalance() || therapy.isTitrationDoseType())
    {
      this._setRateContainersVisible(false);
    }

    this.dosingFrequencyPane.setDosingFrequencyAndPattern(therapy.getDosingFrequency(), therapy.getDoseTimes(), true);
    this.dosingFrequencyPane.setDaysOfWeek(therapy.getDaysOfWeek());
    this.dosingFrequencyPane.setDaysFrequency(therapy.getDosingDaysFrequency());
    this.dosingFrequencyPane.setApplicationPrecondition(therapy.getApplicationPrecondition());
    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setWhenNeeded(therapy.getWhenNeeded());
    }
    this.therapyIntervalPane.setWhenNeeded(therapy.getWhenNeeded());
    this.therapyIntervalPane.setStartCriterion(therapy.getStartCriterion());
    this.therapyIntervalPane.setMaxDailyFrequency(therapy.getMaxDailyFrequency());
    this.commentIndicationPane.setComment(therapy.getComment());
    this.commentIndicationPane.setIndication(therapy.getClinicalIndication());
    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setDispenseDetails(therapy.getDispenseDetails());
    }
    if (!this.isCopyMode())
    {
      this._antimicrobialTherapyStartContainer.setPastTherapyStart(therapy.getPastTherapyStart());
      this.dosingFrequencyPane.setReviewReminder(therapy.getReviewReminderDays(), therapy.getReviewReminderComment());
    }
    this.linkName = therapy.getLinkName();
    this._handleTherapyLinkButtonDisplay();

    if (variable)
    {
      if (!therapy.continuousInfusion)
      {
        this.infusionRateTypePane.markAsSpeed();
        this._setRateContainersVisible(true);
      }
      this.infusionRatePane.setInfusionRate(null);
      this.infusionRatePane.hide();
      this.dosingFrequencyPane.showDaysOnly();
      this.timedDoseElements.push.apply(this.timedDoseElements, timedDoseElements);
      this.recurringContinuousInfusion = therapy.recurringContinuousInfusion;
      this._showVariableRateDisplayValue();
      if (this.recurringContinuousInfusion)
      {
        var nextAdministrationTimestamp =
            app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(this.timedDoseElements);
        this.therapyIntervalPane.setStart(nextAdministrationTimestamp);
        this.therapyIntervalPane.setStartHourEnabled(true);
      }
      else if (therapy.isContinuousInfusion())
      {
        this._setAndDisableStartHourForVario();
      }
      else
      {
        this._handleDosingFrequencyChange();
      }
    }
    else
    {
      if (doseElement && doseElement.rate)
      {
        this._setRateContainersVisible(true);
      }
      if (doseElement && doseElement.rateFormulaUnit)
      {
        this.infusionRatePane.setFormulaUnit(doseElement.rateFormulaUnit, true);
      }
      var rate = doseElement && tm.jquery.Utils.isNumeric(doseElement.rate) ? doseElement.rate : therapy.getRateString();
      this._infusionRateTypePaneFunction(rate, true);
      this.infusionRatePane.setDurationVisible(!therapy.isContinuousInfusion());
      if (this.getOrderingBehaviour().isDoseCalculationsAvailable())
      {
        this.infusionRatePane.recalculateFromRate(true);
      }
      else if (!!doseElement && !!doseElement.duration)
      {
        this.infusionRatePane.setDurationInMinutes(doseElement.duration, true)
      }
    }

    // Set the start if declared (usually meaning the therapy hasn't started yet) and we're not copying,
    // otherwise calculate a new start and possibly end (which should always happen when copying a therapy).
    if (setTherapyStart && !this.isCopyMode())
    {
      this.therapyIntervalPane.setStartOptionsFromPattern();

      // we need to manually trigger the interval change event and in turn preview timeline refresh, since relying on
      // DatePicker's change event is unreliable as there is none if the plugin ins't initialized.
      var therapyStart = tm.jquery.Utils.isDate(therapy.getStart()) ? new Date(therapy.getStart().getTime()) : null;
      this.therapyIntervalPane.setStart(therapyStart, true);
      this.therapyIntervalPane.setEnd(
          tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null, true);
      this._updateNextAdministrationTime(therapyStart);
    }
    else
    {
      // Set the end regardless (unless we're copying), since it might get recalculated (1ex) or cleared
      // (no end date support). If it's not set, the therapyIntervalPane's calculateEnd,
      // called by _calculateStartAndEnd, will clear it, which is not what we want when editing a prescribed therapy.
      if (!this.isCopyMode())
      {
        this.therapyIntervalPane.setEnd(
            tm.jquery.Utils.isDate(therapy.getEnd()) ? new Date(therapy.getEnd().getTime()) : null);
      }
      this._calculateStartAndEnd(this._buildTherapy());
    }

    this._setMedicationsEditable();
    this._calculateDosing();

    if (!tm.jquery.Utils.isEmpty(therapy.getMaxDosePercentage()))
    {
      for (var j = 0; j < this.medicationPanes.length; j++)
      {
        if (this.medicationPanes[j].isShowMaxDose())
        {
          this.medicationPanes[j].setMaxDosePercentage(therapy.getMaxDosePercentage());
          break;
        }
      }
    }

    if (this.isEditMode() && !this.isCopyMode() && (therapy.isStarted() || therapyModifiedInThePast))
    {
      this.therapyNextAdministrationLabelPane.setOldTherapyId(
          therapy.getCompositionUid(),
          therapy.getEhrOrderName(),
          therapy.isContinuousInfusion());
    }
    this._overridenCriticalWarnings = therapy.getCriticalWarnings();

    if (!!this._informationSourceContainer)
    {
      this._informationSourceContainer.setSelections(therapy.getInformationSources());
      this._applyInformationSourceVisibility(therapy.getInformationSources().length > 0);
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setValues(therapy);
    });

    setTimeout(function()
    {
      firstMedicationPane.requestFocusToDose();
    }, 0);
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   *         app.views.medications.common.therapy.AbstractTherapyContainerData} therapyOrder
   */
  setComplexTherapyFromOrder: function(therapyOrder)
  {
    this.setComplexTherapy(therapyOrder.getTherapy(), false, false);
    this._changeReasonContainer.setTherapyChangeReason(therapyOrder.getTherapyChangeReason());
    this.therapyIntervalPane.setRecordAdministration(therapyOrder.isRecordAdministration());
  },

  /**
   * Sets the availability of the change reason input fields and ensures the correct availability of the input fields.
   * @param {boolean} available
   */
  setChangeReasonAvailable: function(available)
  {
    this.changeReasonAvailable = available === true;
    this._applyChangeReasonContainerVisibility();
  },

  /**
   * @see app.views.medications.ordering.InformationSourceContainer#setDefaultSelections
   * @param {Array<app.views.medications.common.dto.InformationSource>} sources
   * @param {boolean} [overrideCurrentSelection=false]
   */
  setDefaultInformationSources: function(sources, overrideCurrentSelection)
  {
    if (!this._informationSourceContainer)
    {
      return;
    }

    this._informationSourceContainer.setDefaultSelections(sources, overrideCurrentSelection === true);
  },

  setContentExtensions: function(extensions)
  {
    if (!tm.jquery.Utils.isEmpty(extensions))
    {
      extensions = tm.jquery.Utils.isArray(extensions) ? extensions : [extensions];
      this.contentExtensions = extensions;

      this._rebuildExtensions();
    }
  },

  isChangeReasonAvailable: function()
  {
    return this.changeReasonAvailable === true;
  },

  refreshAdministrationPreview: function()
  {
    if (!this.administrationPreviewTimeline)
    {
      return;
    }

    var self = this;
    /* add a small delay so we don't call it too often*/
    clearTimeout(this._previewRefreshTimer);

    this._previewRefreshTimer = setTimeout(function()
        {
          if (self.isRendered())
          {
            self.administrationPreviewTimeline.refreshData(
                self.therapyIntervalPane.getStart(),
                self._buildTherapy());
          }
        }
        , 150);
  },

  /**
   * @returns {app.views.medications.ordering.calculationDisplay.BodySurfaceCalculationFormula}
   */
  getCalculationFormula: function()
  {
    return new app.views.medications.ordering.calculationDisplay.BodySurfaceCalculationFormula({
      view: this.getView(),
      referenceData: this.getReferenceData(),
      displayOrderId: 0
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
   * @returns {app.views.medications.common.dto.Therapy|null}
   */
  getLinkedTherapy: function()
  {
    return this.linkedTherapy;
  },

  /**
   * @returns {boolean}
   */
  isEditMode: function ()
  {
    return this.editMode === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function()
  {
    return this.copyMode === true;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @protected
   * @return {app.views.medications.common.dto.Therapy|null} the instance of the original therapy in case of the edit or
   * copy operation, otherwise null. Do not change this instance, it might still be used by the owner!
   */
  getOriginalTherapy: function()
  {
    return this._originalTherapy;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  }
});
