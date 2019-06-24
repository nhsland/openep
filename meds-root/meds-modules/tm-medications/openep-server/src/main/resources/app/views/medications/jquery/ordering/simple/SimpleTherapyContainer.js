Class.define('app.views.medications.ordering.SimpleTherapyContainer', 'tm.jquery.Container', {
  cls: "simple-therapy-container",
  scrollable: 'vertical',

  /** configs */
  /** @type tm.views.medications.TherapyView */
  view: null,
  editMode: false,
  copyMode: false,
  getTherapyStartNotBeforeDateFunction: null, //optional
  /** @type function(app.views.medications.ordering.ConfirmOrderEventData) */
  confirmOrderEventCallback: null,
  saveDateTimePaneEvent: null,
  changeCardEvent: null,  //optional
  /** @type function(app.views.medications.ordering.SaveOrderToTemplateEventData)|null */
  saveOrderToTemplateEventCallback: null,
  changeReasonAvailable: false,
  maxDosePercentage: null,
  showParacetamolContainer: null,
  /** privates */
  _originalTherapy: null,
  validationForm: null,
  medicationData: null,
  timedDoseElements: null,
  medicationRuleUtils: null,
  repeatProtocolUntilCanceled: false,
  protocolEndDate: null,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  availableInformationSources: null,
  /** privates: components */
  medicationInfo: null,
  removeButton: null,
  medicationField: null,
  highRiskIconsContainer: null,
  routesPane: null,
  dosePane: null,
  variableDoseContainer: null,
  varioButton: null,
  dosingFrequencyTitle: null,
  dosingFrequencyPane: null,
  /** @type app.views.medications.ordering.TherapyIntervalPane */
  therapyIntervalPane: null,
  commentIndicationPane: null,
  _maxDoseContainer: null,
  paracetamolLimitContainer: null,
  overdosePane: null,
  _changeReasonContainer: null,
  calculatedDosagePane: null,
  warningsContainer: null,
  addToBasketButton: null,
  therapyNextAdministrationLabelPane: null,

  templatesButton: null,
  complexTherapyButton: null,
  /** @type tm.jquery.Button|null */
  saveToTemplateButton: null,
  administrationPreviewTimeline: null,

  orderingBehaviour: null,
  referenceData: null,
  contentExtensions: null,
  variableDoseDialogFactory: null,

  _previewRefreshTimer: null,
  _overridenCriticalWarnings: null,
  _extensionsPlaceholder: null,
  _toggleTitrationButton: null,
  _releaseDetailsContainer: null,
  _testRenderCoordinator: null,
  /** @type app.views.medications.ordering.InformationSourceContainer|null */
  _informationSourceContainer: null,
  _showInformationSourceButton: null,
  _therapySupplyContainer: null,
  _targetInrContainer: null,
  _antimicrobialTherapyStartContainer: null,

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

    this.timedDoseElements = [];

    if (tm.jquery.Utils.isEmpty(this.contentExtensions))
    {
      this.contentExtensions = [];
    }

    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({view: this.getView(), referenceData: this.getReferenceData()}));

    this.availableInformationSources = tm.jquery.Utils.isArray(this.availableInformationSources) ?
        this.availableInformationSources :
        [];

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-therapy-container-coordinator',
      view: this.getView(),
      component: this
    });

    if (!this.variableDoseDialogFactory)
    {
      this.variableDoseDialogFactory = new app.views.medications.ordering.dosing.DefaultVariableDoseDialogFactory();
    }

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    this._registerFormulaDisplayProviders();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.medicationInfo = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor medication-info',
      width: 25,
      height: 30,
      hidden: true
    });
    this.medicationInfo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._showMedicationInfoPopup();
    });

    this.removeButton = new tm.jquery.Container({cls: 'remove-icon clear-button', width: 30, height: 30});
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (self.changeCardEvent)
      {
        self.changeCardEvent('TEMPLATES');
      }
    });

    this.medicationField = new app.views.medications.common.MedicationSearchField({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      enabled: false,
      dropdownWidth: "stretch",
      dropdownAppendTo: this.getView().getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      searchResultFormatter: this.getOrderingBehaviour().getMedicationSearchResultFormatter()
    });

    this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      var medication = component.getSelectionMedication();
      if (medication && (!self.medicationData || self.medicationData.getMedication().getId() !== medication.getId()))
      {
        self.getView().getRestApi().loadMedicationData(medication.getId()).then(function setData(medicationData)
        {
          self._setMedicationData(medicationData, false);
        });
      }
    });

    this.highRiskIconsContainer = new app.views.medications.ordering.HighRiskMedicationIconsContainer({
      view: this.getView(),
      layout: tm.jquery.HFlexboxLayout.create("center", "center", 0)
    });

    this.routesPane = new app.views.medications.ordering.RoutesPane({
      view: this.getView(),
      selectionRequired: this.orderingBehaviour.isRouteOfAdministrationRequired(),
      discretionaryRoutesDisabled: false,
      changeEvent: function(selectedRoutes)
      {
        if (self._isSingleRouteWithMaxDoseSelected())
        {
          self._maxDoseContainer.setMaxDoseValuesAndNumeratorUnit(self.medicationData, selectedRoutes[0]);
          self._handleMaxDoseChange();
        }

        self.setMaxDoseContainerShowed(self._isSingleRouteWithMaxDoseSelected());
      }
    });

    this._releaseDetailsContainer = new app.views.medications.ordering.simple.ReleaseDetailsContainer({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this._releaseDetailsContainer.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onReleaseDetailsValueChange.bind(this));

    this.dosePane = new app.views.medications.ordering.dosing.DoseContainer({
      cls: "dose-pane with-large-input",
      margin: '0 0 0 5',
      view: this.getView(),
      orderingBehaviour: this.getOrderingBehaviour(),
      referenceData: this.getReferenceData(),
      addDosageCalculationPane: true,
      addDosageCalcBtn: true,
      showRounding: true,
      doseRangeEnabled: this.getView().isDoseRangeEnabled(),
      numeratorChangeEvent: function()
      {
        self._calculateDosing();
      },
      focusLostEvent: function()
      {
        if (self.routesPane.getSelectedRoutes().length === 0)
        {
          self.routesPane.requestFocus();
        }
        else
        {
          self.dosingFrequencyPane.requestFocus();
        }
      }
    });
    this.variableDoseContainer = new tm.jquery.Container({
      cls: 'variable-dose-display-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    });

    this.descriptiveDoseField = new tm.jquery.TextField({
      testAttribute: 'descriptive-dose-input',
      cls: "descriptive-dose-field",
      placeholder: this.getView().getDictionary('single.dose'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      hidden: true
    });

    this.varioButton = new tm.jquery.Button({
      testAttribute: 'variable-dose-button',
      cls: "vario-button",
      text: this.getView().getDictionary('variable'),
      type: 'link',
      handler: function()
      {
        self._openVariableDoseEditPane(
            app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(self.timedDoseElements));
      }
    });

    this._maxDoseContainer = new app.views.medications.ordering.MaxDoseContainer({
      alignSelf: "center",
      view: this.getView(),
      percentage: this.getMaxDosePercentage()
    });

    this.paracetamolLimitContainer = new app.views.medications.ordering.ParacetamolLimitContainer({view: this.getView()});

    this.overdosePane = new app.views.medications.ordering.OverdoseContainer({
      view: this.getView(),
      alignSelf: "center",
      padding: "5 0 0 0",
      hidden: true
    });

    this.dosingFrequencyPane = new app.views.medications.ordering.dosing.DosingFrequencyPane({
      view: this.getView(),
      width: 678,
      orderingBehaviour: this.getOrderingBehaviour(),
      editMode: this.isEditMode(),
      frequencyChangeEvent: function()
      {
        self._handleDosingFrequencyChange();
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
      view: this.getView(),
      width: 678,
      orderingBehaviour: this.getOrderingBehaviour(),
      hidden: this.getOrderingBehaviour().isSupplyAvailable(),
      copyMode: this.isCopyMode(),
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
        if (app.views.medications.MedicationUtils.isTherapyWithVariableDose(self.timedDoseElements) ||
            (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(self.timedDoseElements) &&
                !app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(self.timedDoseElements)))
        {
          var dosingPattern = [];
          self.timedDoseElements.forEach(function(element)
          {
            dosingPattern.push(element.doseTime);
          });
          return dosingPattern;
        }
        else
        {
          return self.dosingFrequencyPane.getDosingPattern();
        }
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
          self._antimicrobialTherapyStartContainer.setTimeOfTherapyStart(componentEvent.eventData.start);
        });

    if (!this.getOrderingBehaviour().isStartEndTimeAvailable() && this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._therapySupplyContainer = new app.views.medications.ordering.supply.TherapySupplyContainer({
        view: this.getView(),
        required: this.getOrderingBehaviour().isSupplyRequired()
      });
    }

    this.therapyNextAdministrationLabelPane = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: !this.getOrderingBehaviour().isStartEndTimeAvailable(),
      view: this.getView()
    });

    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this.administrationPreviewTimeline = new app.views.medications.ordering.timeline.AdministrationPreviewTimeline({
        view: this.getView(),
        autoDraw: !this.isEditMode()
      });
    }

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
      view: this.getView(),
      hidden: true,
      therapyIntervalPane: this.therapyIntervalPane,
      titleText: this.getView().getDictionary('antimicrobial.therapy.start')
    });

    this.commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      view: this.getView(),
      orderingBehaviour: this.getOrderingBehaviour(),
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePaneEvent();
      }
    });
    this._changeReasonContainer = new app.views.medications.ordering.ChangeReasonContainer({
      padding: "5 20 0 0",
      view: this.getView(),
      hidden: !this.isChangeReasonAvailable()
    });
    this.calculatedDosagePane = new app.views.medications.ordering.calculationDisplay.CalculatedDosagePane({
      view: this.getView(),
      referenceData: this.getReferenceData()
    });
    if (!this.isEditMode())
    {
      this.addToBasketButton = new tm.jquery.Button({
        cls: 'add-to-basket-button',
        text: this.getView().getDictionary("add"),
        handler: function()
        {
          self.addToBasketButton.setEnabled(false);
          self.validateAndConfirmOrder();
        }
      });

      this.templatesButton = new tm.jquery.Button({
        cls: "templates-button",
        text: this.getView().getDictionary('empty.form'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent('TEMPLATES');
          }
        }
      });
      this.complexTherapyButton = new tm.jquery.Button({
        cls: "complex-therapy-button",
        text: this.getView().getDictionary('expanded1'),
        type: "link",
        handler: function()
        {
          if (self.changeCardEvent)
          {
            self.changeCardEvent(self.medicationData);
          }
        }
      });

      if (this.getOrderingBehaviour().isAddToTemplateAvailable() &&
          this.getView().getTherapyAuthority().isManageAnyTemplatesAllowed())
      {
        this.saveToTemplateButton = new tm.jquery.Button({
          cls: "save-to-template-button",
          text: this.getView().getDictionary('add.to.order.set'),
          type: "link",
          handler: this._addToTemplate.bind(this)
        });
      }

      if (!!this._informationSourceContainer && !this.getOrderingBehaviour().isInformationSourceRequired())
      {
        this._showInformationSourceButton = new tm.jquery.Button({
          text: this.getView().getDictionary('show.source'),
          type: "link",
          handler: function()
          {
            self._applyInformationSourceVisibility(true);
          }
        });
      }
    }

    this._extensionsPlaceholder = new tm.jquery.Container({
      cls: "extensions-container",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

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
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });

    this._toggleTitrationButton = new tm.jquery.ToggleButton({
      cls: 'toggle-titration-button',
      iconCls: 'icon-titration-dosage-24',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      alignSelf: "center",
      tooltip: appFactory.createDefaultHintTooltip(this.getView().getDictionary("dose.titration"), "bottom"),
      enabled: !this.isEditMode() || this.isCopyMode(),
      handler: function(component)
      {
        component.isPressed() ? self._markAsTitrationDosing() : self._unmarkAsTitrationDosing();
      }
    });

    this._setTitrationButtonVisibility();

    this._targetInrContainer = new app.views.medications.common.VerticallyTitledComponent({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      cls: 'vertically-titled-component target-inr-container',
      titleText: this.getView().getDictionary('target.inr'),
      contentComponent: new tm.jquery.NumberField({
        width: 68
      }),
      hidden: true
    });
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      margin: '10 0 0 20'
    });
    this.add(mainContainer);
    var medicationContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: 'medication-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    medicationContainer.add(this.medicationField);
    medicationContainer.add(this.highRiskIconsContainer);
    medicationContainer.add(this.medicationInfo);
    mainContainer.add(medicationContainer);
    mainContainer.add(this._createVerticalSpacer(7));

    var doseRowContainer = new tm.jquery.Container({
      cls: 'dose-row-container',
      layout: new tm.jquery.HFlexboxLayout({align: 'right'}),
      width: 678,
      scrollable: "visible"
    });
    if (!this.isEditMode())
    {
      doseRowContainer.add(this.removeButton);
    }
    doseRowContainer.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
    if (this.getOrderingBehaviour().isVariableDoseAvailable())
    {
      doseRowContainer.add(this.varioButton);
    }
    doseRowContainer.add(this.overdosePane);
    doseRowContainer.add(this.paracetamolLimitContainer);
    doseRowContainer.add(this._maxDoseContainer);
    doseRowContainer.add(this.dosePane);
    doseRowContainer.add(this.variableDoseContainer);
    doseRowContainer.add(this.descriptiveDoseField);
    doseRowContainer.add(this._toggleTitrationButton);

    mainContainer.add(doseRowContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(this._buildRoutesReleaseInrContainer());
    mainContainer.add(this._createVerticalSpacer(2));
    this.dosingFrequencyTitle = app.views.medications.MedicationUtils.crateLabel(
        'TextLabel',
        this.getView().getDictionary('dosing.interval'));
    mainContainer.add(this.dosingFrequencyTitle);
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
      mainContainer.add(this.administrationPreviewTimeline);
    }
    mainContainer.add(this._antimicrobialTherapyStartContainer);
    mainContainer.add(this._createVerticalSpacer(2));
    mainContainer.add(this.commentIndicationPane);
    mainContainer.add(this._changeReasonContainer);

    if (!!this._informationSourceContainer)
    {
      mainContainer.add(this._informationSourceContainer);
    }

    this.add(this._extensionsPlaceholder);
    this.add(new app.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component calculation-details",
      titleText: this.getView().getDictionary('calculated.dosing'),
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

      navigationContainer.add(this.templatesButton);
      navigationContainer.add(new tm.jquery.Spacer({type: 'horizontal', size: 50}));
      navigationContainer.add(this.complexTherapyButton);
      if (!!this.saveToTemplateButton)
      {
        navigationContainer.add(this.saveToTemplateButton);
      }
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

  /**
   * Constructs the horizontal wrapping container housing the routes selection component and release details component.
   * @return {tm.jquery.Container}
   * @private
   */
  _buildRoutesAndReleaseDetailsRow: function()
  {
    var routeAndReleaseRowContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    routeAndReleaseRowContainer.add(
        new app.views.medications.common.VerticallyTitledComponent({
          scrollable: 'visible',
          titleText: this.getView().getDictionary('route'),
          contentComponent: this.routesPane,
          flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto")
        }));
    routeAndReleaseRowContainer.add(this._releaseDetailsContainer);

    return routeAndReleaseRowContainer;
  },

  /**
   * Add a wrapper container to allow routes and release container to stretch as far as possible (to allow showing multiple
   * routes buttons) and push {@link #_targetInrContainer} container to the right side of the order form.
   * @private
   * @returns {tm.jquery.Container}
   */
  _buildRoutesReleaseInrContainer: function()
  {
    var wrapperContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start'),
      scrollable: "visible"
    });

    wrapperContainer.add(this._buildRoutesAndReleaseDetailsRow());
    wrapperContainer.add(this._targetInrContainer);

    return wrapperContainer;
  },

  /**
   * Change event handler for {@link #_releaseDetailsContainer}. Should trigger the refresh of preview timeline since
   * we draw duration lines in case the release duration is specified.
   * @private
   */
  _onReleaseDetailsValueChange: function()
  {
    this.refreshAdministrationPreview();
  },

  /**
   * register dose calculation formula providers for common components
   * @private
   */
  _registerFormulaDisplayProviders: function()
  {
    this.calculatedDosagePane.registerCalculationFormulaProvider(this.dosePane);
    this.calculatedDosagePane.registerCalculationFormulaProvider(this);
    this.calculatedDosagePane.registerCalculationFormulaProvider(this._maxDoseContainer)
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this._addValidations(this.routesPane.getRoutesPaneValidations());
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this._addValidations(this.therapyIntervalPane.getTherapyIntervalPaneValidations(this._getMinimumStartTime()));
    }
    if (!!this._therapySupplyContainer)
    {
      this._addValidations(this._therapySupplyContainer.getFormValidations());
    }
    this._addValidations(this.commentIndicationPane.getIndicationValidations());
    if (!this.dosePane.isHidden())
    {
      this._addValidations(this.dosePane.getDosePaneValidations());
    }
    else if (!this.descriptiveDoseField.isHidden())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: self.descriptiveDoseField,
        required: this.orderingBehaviour.isDoseRequired(),
        validation: {
          type: "local",
          validators: [app.views.medications.MedicationUtils.buildDefaultMinimumLengthStringValidator(this.getView())]
        }
      }))
    }

    if (!this.dosingFrequencyPane.isHidden())
    {
      this._addValidations(this.dosingFrequencyPane.getDosingFrequencyPaneValidations(this._isWhenNeededSelected()));
    }

    if (!this._targetInrContainer.isHidden())
    {
      this.validationForm.addFormField(new tm.jquery.FormField({
        component: this._targetInrContainer.getContentComponent(),
        validation: {
          type: 'local',
          validators: [
            new tm.jquery.Validator({
              errorMessage: this.getView().getDictionary('value.must.be.numeric.not.zero'),
              isValid: function(value)
              {
                if (!tm.jquery.Utils.isEmpty(value))
                {
                  return tm.jquery.Utils.isNumeric(value) && value > 0;
                }

                return !self.orderingBehaviour.isTargetedInrRequired();
              }
            })
          ]
        }
      }));
    }

    if (!this._changeReasonContainer.isHidden())
    {
      this._changeReasonContainer.attachRemoteFormValidation(
          this._originalTherapy,
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

    if (!this._antimicrobialTherapyStartContainer.isHidden())
    {
      this._addValidations(this._antimicrobialTherapyStartContainer.getValidators())
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      self._addValidations(extension.getFormValidations());
    })
  },

  _addValidations: function(validation)
  {
    if (tm.jquery.Utils.isEmpty(validation)) return;

    for (var i = 0; i < validation.length; i++)
    {
      this.validationForm.addFormField(validation[i]);
    }
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

  _openVariableDoseEditPane: function(variableDays)
  {
    this._updateProtocolProperties();

    var self = this;

    this.getVariableDoseDialogFactory()
        .create(
            this,
            variableDays,
            this.timedDoseElements.slice(0),
            this.repeatProtocolUntilCanceled,
            (!tm.jquery.Utils.isEmpty(this.protocolEndDate) && this.therapyIntervalPane.getEnd() > this.protocolEndDate) ?
                this.therapyIntervalPane.getEnd() :
                this.protocolEndDate,
            function(resultData)
            {
              if (resultData)
              {
                self.dosePane.hide();
                self.dosePane.clear(true);
                self.timedDoseElements = resultData.value.timedDoseElements;
                self._showVariableDoseDisplayValue();
                self.dosingFrequencyPane.setFrequency(resultData.value.frequency, false);
                self._handleDosingFrequencyChange();
                self._adjustDosingFrequencyPaneFields();
                self._adjustTherapyIntervalToVariableDose(resultData.value.endDate, resultData.value.untilCanceled);
              }
            })
        .show();
  },

  /**
   * Only use for therapies with existing protocol. Will recalculate the protocol end date and repetition if the current
   * dosing is set to descriptive variable days doses.
   * It protocol exists and therapy end is not defined, presume last protocol day is being repeated until therapy is canceled.
   * If protocol exists and therapy end is after the last protocol dose time, presume last day of protocol is being repeated
   * until therapy end.
   * @private
   */
  _updateProtocolProperties: function()
  {
    if (!app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.timedDoseElements))
    {
      return;
    }

    this.repeatProtocolUntilCanceled = tm.jquery.Utils.isEmpty(this.therapyIntervalPane.getEnd());
    if (!this.protocolEndDate && !tm.jquery.Utils.isEmpty(this.therapyIntervalPane.getEnd()))
    {
      var lastElement = this.timedDoseElements[this.timedDoseElements.length - 1];
      var lastElementDate = tm.jquery.Utils.isDate(lastElement.date) ?
          new Date(lastElement.date.getFullYear(),
              lastElement.date.getMonth(),
              lastElement.date.getDate(),
              lastElement.doseTime.hour,
              lastElement.doseTime.minute) :
          null;
      this.protocolEndDate = this.therapyIntervalPane.getEnd() > lastElementDate ?
          new Date(this.therapyIntervalPane.getEnd().getTime()) : null;
    }
  },

  _createVerticalSpacer: function(size)
  {
    return new tm.jquery.Spacer({type: 'vertical', size: size});
  },

  _adjustDosingFrequencyPaneFields: function()
  {
    if (this.timedDoseElements.length > 0)
    {
      this.dosingFrequencyPane.showDaysOnly(true);
      // remove days of week if protocol is selected
      if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.timedDoseElements))
      {
        this.dosingFrequencyPane.clearDaysOfWeek();
        this.dosingFrequencyPane.hideDaysButton();
      }
    }
    else
    {
      this.dosingFrequencyPane.showAllFields();
    }
  },

  _showVariableDoseDisplayValue: function()
  {
    this.variableDoseContainer.removeAll(true);
    if (this.timedDoseElements.length > 0)
    {
      // protocols are too large to display, or missing timing information
      if (!app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.timedDoseElements) &&
          app.views.medications.MedicationUtils.isTherapyWithVariableDose(this.timedDoseElements))
      {
        this._showHoursVariableDoseDisplayValue();
      }
    }

    this.variableDoseContainer.show();
    this.variableDoseContainer.repaint();
  },

  _showHoursVariableDoseDisplayValue: function()
  {
    var utils = app.views.medications.MedicationUtils;
    this.timedDoseElements.forEach(
        function(timedDoseElement)
        {
          // if there's no dose time defined there's really nothing to show, so skip
          if (!timedDoseElement.doseTime)
          {
            return;
          }

          var rowContainer = new tm.jquery.Container({
            cls: 'variable-dose-row-container',
            layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
          });

          var doseTime = timedDoseElement.doseTime;
          var timeDisplayValue = app.views.medications.MedicationTimingUtils.hourMinuteToString(
              doseTime.hour, doseTime.minute) + '  ';

          rowContainer.add(new tm.jquery.Component({
            cls: 'TextLabel',
            html: timeDisplayValue
          }));

          // support empty doses when dose input not required
          var doseDisplayValue = '';
          if (!tm.jquery.Utils.isEmpty(timedDoseElement.doseElement.quantity))
          {
            doseDisplayValue = utils.getFormattedDecimalNumber(
                utils.doubleToString(timedDoseElement.doseElement.quantity, 'n2')) + ' ' +
                utils.getFormattedUnit(this.medicationData.getStrengthNumeratorUnit(), this.getView());
          }

          if (this.medicationData.getStrengthDenominatorUnit() &&
              !tm.jquery.Utils.isEmpty(timedDoseElement.doseElement.quantityDenominator))
          {
            doseDisplayValue += ' / ' +
                utils.getFormattedDecimalNumber(
                    utils.doubleToString(timedDoseElement.doseElement.quantityDenominator, 'n2')) + ' ' +
                utils.getFormattedUnit(this.medicationData.getStrengthDenominatorUnit(), this.getView());
          }

          rowContainer.add(new tm.jquery.Component({
            cls: 'TextData',
            html: doseDisplayValue
          }));

          this.variableDoseContainer.add(rowContainer);

        },
        this);
  },

  _adjustTherapyIntervalToVariableDose: function(endDate, untilCanceled)
  {
    if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.timedDoseElements))
    {
      // keep in mind not all variable dose day prescriptions contain structured timing information
      var therapyInterval =
          app.views.medications.MedicationTimingUtils.getVariableDaysTherapyInterval(this.timedDoseElements);

      if (!this.isEditMode() && tm.jquery.Utils.isDate(therapyInterval.start))
      {
        this.therapyIntervalPane.setStart(therapyInterval.start);
      }
      var therapyEnd = null;
      if (endDate)
      {
        therapyEnd = endDate;
        if (tm.jquery.Utils.isDate(therapyInterval.end))
        {
          therapyEnd.setHours(therapyInterval.end.getHours());
          therapyEnd.setMinutes(therapyInterval.end.getMinutes());
        }
        this.repeatProtocolUntilCanceled = false;
        this.protocolEndDate = endDate;

      }
      else if (untilCanceled)
      {
        this.repeatProtocolUntilCanceled = true;
        this.protocolEndDate = null;
        therapyEnd = null;
      }
      else
      {
        therapyEnd = tm.jquery.Utils.isDate(therapyInterval.end) ? therapyInterval.end : null;
        this.repeatProtocolUntilCanceled = false;
      }
      this.therapyIntervalPane.setEnd(therapyEnd);
      this.therapyIntervalPane.setMinEnd(therapyEnd);
      this.therapyIntervalPane.setTherapyEndEnabled(false);
    }
    else
    {
      var nextAdministrationTimestamp =
          app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForVario(this.timedDoseElements);
      if (!!nextAdministrationTimestamp)
      {
        this.therapyIntervalPane.setStart(nextAdministrationTimestamp);
      }
    }
  },

  /**
   * Should be used every time a parameter that could affect max dose is changed. Calculates the new max dose percentage,
   * if single route with max dose defined is selected. Otherwise, clears the max dose container.
   * @private
   */
  _handleMaxDoseChange: function()
  {
    if (this._isSingleRouteWithMaxDoseSelected())
    {
      var quantity = null;
      var variable = tm.jquery.Utils.isArray(this.timedDoseElements) && this.timedDoseElements.length > 0;
      if (variable)
      {
        quantity = app.views.medications.MedicationUtils.calculateVariablePerDay(this.timedDoseElements);
      }
      else
      {
        var dose = this.dosePane.isHidden() ? this.dosePane.getEmptyDose() : this.dosePane.getDose();
        quantity = dose.doseRange ? dose.doseRange : dose.quantity
      }

      this._maxDoseContainer.calculatePercentage(
          quantity, this.dosingFrequencyPane.getFrequencyTimesPerDay(), this.dosingFrequencyPane.getTimesPerWeek(), variable);
    }
    else
    {
      this._maxDoseContainer.clear();
    }
  },

  _handleParacetamolRuleChange: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var self = this;

    var medicationData = this.medicationData;
    if (self.medicationRuleUtils.isMedicationRuleSet(medicationData, enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE))
    {
      var medicationId = !tm.jquery.Utils.isEmpty(medicationData)
          ? medicationData.medication.id
          : null;

      self.medicationRuleUtils.getParacetamolRuleForTherapy(
          this._buildTherapy(),
          [medicationData]).then(
          function validationSuccessHandler(medicationRuleResult)
          {
            var medicationConsistent = self.medicationData && self.medicationData.medication && medicationId
                && medicationId === self.medicationData.medication.id;

            if (self.isRendered() && medicationConsistent)
            {
              self.paracetamolLimitContainer.setCalculatedParacetamolLimit(medicationRuleResult);
              if (self.paracetamolLimitContainer.hasContent())
              {
                self.paracetamolLimitContainer.show();
              }
            }
          });
    }
    else
    {
      this.paracetamolLimitContainer.clear();
    }
  },

  _calculateDosing: function()
  {
    if (this.medicationData)
    {
      var dose = this.dosePane.isHidden() ? this.dosePane.getEmptyDose() : this.dosePane.getDose();
      var quantity = dose.doseRange ? dose.doseRange : dose.quantity;
      var quantityUnit = this.medicationData.getStrengthNumeratorUnit();
      var timesPerDay = this.dosingFrequencyPane.getFrequencyTimesPerDay();
      var weightInKg = this.getReferenceData().getWeight();
      var heightInCm = this.getReferenceData().getHeight();

      if (this.getOrderingBehaviour().isReferenceDataBasedDoseCalculationAvailable())
      {
        this.calculatedDosagePane.calculate(quantity, quantityUnit, timesPerDay, weightInKg, heightInCm, false);
      }

      this.overdosePane.calculateOverdose(quantity);

      this._handleMaxDoseChange();
      this._handleParacetamolRuleChange();
    }
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
    if (this.getView().getViewMode() === 'ORDERING_PAST')
    {
      this.getView().setPresetDate(confirmEventData.getTherapyOrder().getTherapy().getStart());
    }
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
        .setRecordAdministration(this.therapyIntervalPane.isRecordAdministration());
  },

  /**
   * Based on the current state of the form.
   * @return {app.views.medications.common.dto.Therapy}
   * @private
   */
  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var variableDose = this.timedDoseElements.length > 0;
    var maxDailyFrequency = this.therapyIntervalPane.getMaxDailyFrequency();
    var selectedRoutes = this.routesPane.getSelectedRoutes();
    var quantityUnits = this._getStrengthUnits();

    var therapy = {                                                              // [SimpleTherapyDto.java]
      medicationOrderFormType: enums.medicationOrderFormType.SIMPLE,
      variable: variableDose,
      medication: this.medicationData.medication,
      doseForm: this.medicationData.getDoseForm(),
      routes: selectedRoutes,
      quantityUnit: quantityUnits.quantityUnit,
      quantityDenominatorUnit: quantityUnits.quantityDenominatorUnit,
      dosingFrequency: this.dosingFrequencyPane.getFrequency(),
      dosingDaysFrequency: this.dosingFrequencyPane.getDaysFrequency(),
      daysOfWeek: this.dosingFrequencyPane.getDaysOfWeek(),
      maxDailyFrequency: !tm.jquery.Utils.isEmpty(maxDailyFrequency) ? maxDailyFrequency : null,
      start: this.therapyIntervalPane.getStart(),
      end: this.therapyIntervalPane.getEnd(),
      reviewReminderDays: this.dosingFrequencyPane.getReviewReminderDays(),
      reviewReminderComment: this.dosingFrequencyPane.getReviewReminderComment(),
      whenNeeded: !!this._therapySupplyContainer ?
          this._therapySupplyContainer.getWhenNeeded() :
          this.therapyIntervalPane.getWhenNeeded(),
      startCriterion: !this.therapyIntervalPane.isHidden() ? this.therapyIntervalPane.getStartCriterion() : null,
      applicationPrecondition: this.dosingFrequencyPane.getApplicationPrecondition(),
      comment: this.commentIndicationPane.getComment(),
      clinicalIndication: this.commentIndicationPane.getIndication(),
      dispenseDetails: !!this._therapySupplyContainer ? this._therapySupplyContainer.getDispenseDetails() : null,
      criticalWarnings: this._overridenCriticalWarnings ? this._overridenCriticalWarnings : [],
      maxDosePercentage: this._isSingleRouteWithMaxDoseSelected() ? this._maxDoseContainer.getPercentage() : null,
      admissionId: !this.isCopyMode() && !!this.getOriginalTherapy() ? this.getOriginalTherapy().getAdmissionId() : null,
      titration: this._toggleTitrationButton.isPressed() ? this.medicationData.getTitration() : null,
      releaseDetails: this._releaseDetailsContainer.getSelection(),
      informationSources: !!this._informationSourceContainer ?
          this._informationSourceContainer.getSelections() :
          (!this.isCopyMode() && !!this.getOriginalTherapy() ?
              this.getOriginalTherapy().getInformationSources().slice(0) :
              []),
      targetInr: !this._targetInrContainer.isHidden() ? this._targetInrContainer.getContentComponent().getValue() : null,
      pastTherapyStart: !this._antimicrobialTherapyStartContainer.isHidden() ?
          this._antimicrobialTherapyStartContainer.getDateTime() :
          null
    };

    if (variableDose)          // [VariableSimpleTherapyDto.java]
    {
      therapy.timedDoseElements = this.timedDoseElements;
    }
    else                    // [ConstantSimpleTherapyDto.java]
    {
      if (!this.dosePane.isHidden())
      {
        therapy.doseElement = this.dosePane.isHidden() ? this.dosePane.getEmptyDose() : this.dosePane.getDose();
      }
      else
      {
        therapy.doseElement = {doseDescription: this.descriptiveDoseField.getValue()};
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

  _getStrengthUnits: function()
  {
    return {
      quantityUnit: this.medicationData.getStrengthNumeratorUnit(),
      quantityDenominatorUnit: this.medicationData.getStrengthDenominatorUnit()
    }
  },

  _handleDosingFrequencyChange: function()
  {
    this._calculateStartAndEnd(this._buildTherapy());
    this._calculateDosing();
  },

  /**
  * Triggers the start and end time calculation with the help of the server API. When editing or copying an existing therapy,
  * the minimum therapy end date and time is intentionally unlimited, due to the behaviour of the minimum date & time control
  * algorithm, which enforces the minimum value in such a way that the date and time is automatically set to the minimum,
  * when we attempt to set an older time. This, in turn, can cause an unintentional extension of a prescribed therapy,
  * which is dangerous.
  * @param {app.views.medications.common.dto.Therapy} therapy
  * @private
  */
  _calculateStartAndEnd: function(therapy)
  {
    if (!this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      return;
    }

    var self = this;
    // only
    var oldTherapy = this.isEditMode() && !this.isCopyMode() ? this.getOriginalTherapy() : null;
    this.therapyIntervalPane.calculateStart(therapy, tm.jquery.Utils.isEmpty(oldTherapy), oldTherapy, function()
    {
      self.therapyIntervalPane.calculateEnd();
    });
  },

  /**
   * Either shows the correct combination of dosing components (either the dose pane with/without the variable dose button
   * or just the descriptive dose field), based on medication data and timedDoseElements, or hides all of the said components
   * along with clearing the variable/protocol data.
   * Keep in mind this method can also be called when switching the current medication data for similar medication.
   *
   * @param {boolean} show True if you want the correct dose input component to be visible.
   * @private
   */
  _showDoseComponents: function(show)
  {
    var isDescriptiveDose = this.medicationData && this.medicationData.isDoseFormDescriptive();
    var isVariableDose = this.timedDoseElements.length > 0;

    if (show)
    {
      if (isDescriptiveDose || isVariableDose)
      {
        this.dosePane.hide();
        if (isDescriptiveDose)
        {
          this.varioButton.hide();
          this.descriptiveDoseField.show();
        }
        else
        {
          this.varioButton.show();
          this.descriptiveDoseField.hide();
        }
      }
      else
      {
        this.dosePane.show();
        this.varioButton.show();
        this.descriptiveDoseField.hide();
      }
    }
    else
    {
      if (isVariableDose)
      {
        this.removeVariableDosage();
      }

      this.dosePane.hide();
      this.varioButton.hide();
      this.descriptiveDoseField.hide();
    }
  },

  _clear: function()
  {
    this._originalTherapy = null;
    this.timedDoseElements = [];
    this.routesPane.setSelectedRoute(null);
    this.dosePane.clear();
    this._maxDoseContainer.clear();
    this.setMaxDoseContainerShowed(false);
    this.variableDoseContainer.hide();
    this.dosingFrequencyPane.clear();
    this.therapyIntervalPane.clear();
    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.clear();
    }
    this._antimicrobialTherapyStartContainer.clear();
    this._antimicrobialTherapyStartContainer.hide();
    this.commentIndicationPane.clear();
    this._adjustDosingFrequencyPaneFields();
    if (!!this._informationSourceContainer)
    {
      this._applyInformationSourceVisibility(false);
      this._informationSourceContainer.clear();
    }
    this._executeOnValidContentExtensions(function(extension)
    {
      extension.clear();
    });
    this._changeReasonContainer.clear();
    this._toggleTitrationButton.setPressed(false);
    this.highRiskIconsContainer.clear();
    this.repeatProtocolUntilCanceled = false;
    this.protocolEndDate = null;
    this.therapyIntervalPane.setMinEnd(null);
    this.paracetamolLimitContainer.clear();
    this.routesPane.clear();
    this.calculatedDosagePane.clear();
    this._releaseDetailsContainer.clear();
    this._targetInrContainer.isRendered() ? this._targetInrContainer.hide() : this._targetInrContainer.setHidden(true);
    this._targetInrContainer.getContentComponent().setValue(null);
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

  _showMedicationInfoPopup: function()
  {
    var appFactory = this.getView().getAppFactory();
    var medicationInfoContent = new app.views.medications.common.MedicationDetailsContainer({
      view: this.getView(),
      medicationData: [this.medicationData],
      selectedRoute: this.routesPane.getSelectedRoutes()
    });

    var medicationInfoPopup = appFactory.createDefaultPopoverTooltip(
        this.getView().getDictionary("medication"),
        null,
        medicationInfoContent
    );

    this.medicationInfo.setTooltip(medicationInfoPopup);

    setTimeout(function()
    {
      medicationInfoPopup.show();
    }, 10);
  },

  /**
   * Configures the visibility of the titration based dosing input components. If the {@link #medicationData} is not set,
   * or the medication does not support titrated dosing, or the ordering behaviour prohibits such dosing, we hide both the
   * titration mode button, otherwise we show it.
   * @private
   */
  _setTitrationButtonVisibility: function()
  {
    var isVisible = this.medicationData && !!this.medicationData.getTitration() &&
        this.getOrderingBehaviour().isTitratedDoseModeAvailable();

    if (isVisible)
    {
      this._toggleTitrationButton.setPressed(false, true);
      this.isRendered() ? this._toggleTitrationButton.show() : this._toggleTitrationButton.setHidden(false);
    }
    else
    {
      this.isRendered() ? this._toggleTitrationButton.hide() : this._toggleTitrationButton.setHidden(true);
    }
  },

  _markAsTitrationDosing: function()
  {
    this._showDoseComponents(false);
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(false);
  },

  _unmarkAsTitrationDosing: function()
  {
    this._showDoseComponents(true);
    this.therapyIntervalPane.setByDoctorsOrderButtonEnabled(true);
  },

  /**
   * Sets the active medication data. Call {@link setMedicationDataBySearch} when you want to populate values based on the
   * selected medication, which includes additional preset logic.
   * @private
   */
  _setMedicationData: function(medicationData, clear)
  {
    var self = this;
    if (clear)
    {
      this._clear();
    }

    this.medicationData = medicationData;
    this.medicationField.setSelection(medicationData.getMedication(), true);
    var routes = !tm.jquery.Utils.isEmpty(medicationData.getRoutes()) ?
        medicationData.getRoutes() :
        this.getView().getRoutes();
    this.routesPane.setRoutes(routes, medicationData.defaultRoute);
    this.dosePane.setMedicationData(medicationData);
    this.commentIndicationPane.setMedicationData(medicationData);
    this.dosingFrequencyPane.setMedicationData(medicationData);
    this.validationForm.reset();
    this.descriptiveDoseField.setValue(null);
    this.overdosePane.setMedicationDataValues(medicationData);
    if (this.overdosePane.isTabletOrCapsule())
    {
      this.overdosePane.show();
    }
    else
    {
      this.overdosePane.hide();
    }

    this._releaseDetailsContainer.setOptionsByMedication(medicationData);

    if (this.addToBasketButton)
    {
      this.addToBasketButton.setEnabled(true);
    }
    if (!medicationData.getMedication().isMedicationUniversal())
    {
      if (!!this.saveToTemplateButton)
      {
        this.saveToTemplateButton.show();
      }
      this.medicationInfo.show();
    }
    else
    {
      if (!!this.saveToTemplateButton)
      {
        this.saveToTemplateButton.hide();
      }
      this.medicationInfo.hide();
    }

    this._showDoseComponents(true);

    setTimeout(function()
    {
      if (!self.dosePane.isHidden())
      {
        self.dosePane.requestFocusToNumerator();
      }
      else if (!self.descriptiveDoseField.isHidden())
      {
        self.descriptiveDoseField.focus();
      }
    }, 0);

    if (medicationData.isAntibiotic() &&
        this.getOrderingBehaviour().isPastDaysOfTherapyVisible())
    {
      this._antimicrobialTherapyStartContainer.show();
    }

    this._maxDoseContainer.setPercentage(null);
    var selectedRoutes = this.routesPane.getSelectedRoutes();
    if (selectedRoutes.length === 1 && !tm.jquery.Utils.isEmpty(selectedRoutes[0].getMaxDose()))
    {
      this.setMaxDoseContainerShowed(true);
      this._maxDoseContainer.setMaxDoseValuesAndNumeratorUnit(medicationData, selectedRoutes[0]);
    }

    this.showHideParacetamolContainer(
        this.medicationRuleUtils.isMedicationRuleSet(
            medicationData, app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE));

    this.highRiskIconsContainer.presentHighAlertIcons(medicationData);

    this._setTitrationButtonVisibility();

    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setOptionsByMedication(medicationData);
    }

    // toggle the visibility of the target inr field based on selected medication's titration type regardless of
    // the ability or intent to set the dosing as titrated. The field is mandatory for all medications which
    // help control inr levels.
    if (medicationData.isInrBasedTitrationSupported())
    {
      this._targetInrContainer.isRendered() ? this._targetInrContainer.show() : this._targetInrContainer.setHidden(false);
    }
    else
    {
      this._targetInrContainer.isRendered() ? this._targetInrContainer.hide() : this._targetInrContainer.setHidden(true);
    }

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setMedicationData(medicationData);
    });
  },

  validateAndConfirmOrder: function()
  {
    if (this.isEditMode() && !this.isCopyMode())
    {
      this.therapyIntervalPane.adjustStartTimeToOrderTime(
          tm.jquery.Utils.isFunction(this.getTherapyStartNotBeforeDateFunction) ?
              this.getTherapyStartNotBeforeDateFunction() :
              null,
          true);
    }
    this._setupValidation();
    this.validationForm.submit();
  },

  /**
   * Sets the active medication data. Call {@link setSimpleTherapy} or {@link setSimpleTherapyFromOrder} when you want to
   * populate values based on an existing therapy, which will also include setting the active medication data from that
   * therapy. In case of INR based medication, the data will preset the titration mode and handles the visibility of the INR
   * input field.
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationDataBySearch: function(medicationData)
  {
    this._setMedicationData(medicationData, true);

    if (this.getOrderingBehaviour().isTitratedDoseModeAvailable() && medicationData.isInrBasedTitrationSupported())
    {
      this._toggleTitrationButton.setPressed(true, true);
      this._markAsTitrationDosing();
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {boolean} [setTherapyStart=false]
   * @param {boolean} [therapyModifiedInThePast=false]
   */
  setSimpleTherapy: function(therapy, medicationData, setTherapyStart, therapyModifiedInThePast)
  {
    // don't set values before setMedicationData clears everything.
    if (medicationData)
    {
      this._setMedicationData(medicationData, true);
      if (therapy.isVariable())
      {
        // when copying protocol therapy, refresh timed dose elements timing
        if (this.isCopyMode() && therapy.isVariable() && this.getOrderingBehaviour().isStartEndTimeAvailable() &&
            app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements()))
        {
          therapy.rescheduleTherapyTimings(false);
        }
        this.timedDoseElements = therapy.getTimedDoseElements().slice(0);
        this.dosePane.hide();
        this._showVariableDoseDisplayValue();
      }
      else if (medicationData.isDoseFormDescriptive())
      {
        this.descriptiveDoseField.setValue(therapy.getDoseElement().doseDescription);
      }
      else if (therapy.getDoseElement())
      {
        var numerator = therapy.getDoseElement().doseRange ?
            app.views.medications.common.dto.Range.createStrict(
                therapy.getDoseElement().doseRange.minNumerator,
                therapy.getDoseElement().doseRange.maxNumerator) :
            therapy.getDoseElement().quantity;
        this.dosePane.setDoseNumerator(numerator, true);
        this.dosePane.calculateAndSetDoseDenominator(true);
      }

      if (this.getOrderingBehaviour().isTitratedDoseModeAvailable() && therapy.isTitrationDoseType())
      {
        this._toggleTitrationButton.setPressed(true, true);
        this._markAsTitrationDosing();
      }

      this.routesPane.setSelectedRoute(therapy.getRoutes());
    }

    this._originalTherapy = therapy;
    this.dosingFrequencyPane.setDosingFrequencyAndPattern(therapy.getDosingFrequency(), therapy.getDoseTimes(), true);
    this.dosingFrequencyPane.setDaysOfWeek(therapy.getDaysOfWeek());
    this.dosingFrequencyPane.setDaysFrequency(therapy.getDosingDaysFrequency());
    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setWhenNeeded(therapy.getWhenNeeded());
    }
    this.therapyIntervalPane.setWhenNeeded(therapy.getWhenNeeded());
    this.therapyIntervalPane.setStartCriterion(therapy.getStartCriterion());
    this.dosingFrequencyPane.setApplicationPrecondition(therapy.getApplicationPrecondition());
    this._adjustDosingFrequencyPaneFields();
    this.therapyIntervalPane.setMaxDailyFrequency(therapy.getMaxDailyFrequency());
    this.commentIndicationPane.setComment(therapy.getComment());
    this.commentIndicationPane.setIndication(therapy.getClinicalIndication());

    if (!!this._therapySupplyContainer)
    {
      this._therapySupplyContainer.setDispenseDetails(therapy.getDispenseDetails());
    }
    if (therapy.getMaxDosePercentage())
    {
      this._maxDoseContainer.setPercentage(therapy.getMaxDosePercentage());
    }
    if (!this.isCopyMode())
    {
      this._antimicrobialTherapyStartContainer.setPastTherapyStart(therapy.getPastTherapyStart());
      this.dosingFrequencyPane.setReviewReminder(therapy.getReviewReminderDays(), therapy.getReviewReminderComment());
    }

    // Set the start if declared (usually meaning the therapy hasn't started yet) and we're not copying,
    // unless we are copying a protocol, which has recalculated timed dose elements, with start and end set
    // accordingly. Otherwise, calculate a new start and possibly end (which should always happen when copying a therapy).
    if ((!!setTherapyStart && !this.isCopyMode()) ||
        (this.isCopyMode() && therapy.isVariable() &&
            app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements())))
    {
      this.therapyIntervalPane.setStartOptionsFromPattern();
      // keep in mind if the date picker isn't initialized it doesn't fire change events
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

    if (this.isEditMode())
    {
      if (!therapy.getMedication().isMedicationUniversal())
      {
        this.medicationField.setLimitBySimilar(therapy.getMedication());
      }

      if (!this.isCopyMode() && (therapy.isStarted() || !!therapyModifiedInThePast))
      {
        this.therapyNextAdministrationLabelPane.setOldTherapyId(
            therapy.getCompositionUid(), therapy.getEhrOrderName(), false);
      }
    }

    this._overridenCriticalWarnings = therapy.getCriticalWarnings();
    this._releaseDetailsContainer.setSelection(therapy.getReleaseDetails());

    if (!!this._informationSourceContainer)
    {
      this._informationSourceContainer.setSelections(therapy.getInformationSources());
      this._applyInformationSourceVisibility(therapy.getInformationSources().length > 0);
    }

    if (!!therapy.getTargetInr())
    {
      this._targetInrContainer.getContentComponent().setValue(therapy.getTargetInr());
    }

    this._calculateDosing();

    this._executeOnValidContentExtensions(function(extension)
    {
      extension.setValues(therapy);
    });
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   *         app.views.medications.common.therapy.AbstractTherapyContainerData} therapyOrder
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setSimpleTherapyFromOrder: function(therapyOrder, medicationData)
  {
    this.setSimpleTherapy(
        therapyOrder.getTherapy(),
        medicationData,
        !tm.jquery.Utils.isEmpty(therapyOrder.getTherapy().getStart()),
        false);
    this._changeReasonContainer.setTherapyChangeReason(therapyOrder.getTherapyChangeReason());
    this.therapyIntervalPane.setRecordAdministration(therapyOrder.isRecordAdministration());
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
   * Should be called once the next administration time (therapy start) changes. Updates both the label and triggers the
   * administration preview timeline reload.
   * @param {Date} nextAdministrationTime
   * @private
   */
  _updateNextAdministrationTime: function(nextAdministrationTime)
  {
    this.therapyNextAdministrationLabelPane.setNextAdministration(nextAdministrationTime);
    this.refreshAdministrationPreview(nextAdministrationTime);
  },

  /**
   * Indications are required for certain prescription types, unless the rules are turned off via ordering behavior.
   * @returns {boolean}
   * @private
   */
  _isIndicationRequired: function()
  {
    return !this.orderingBehaviour.isIndicationAlwaysOptional() &&
        (this.medicationData.isHighAlertMedication() || this.medicationData.isAntibiotic() || this._isWhenNeededSelected());
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
        (this.view.isFormularyFilterEnabled() ? !this.medicationData.isFormulary() : false);
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
    }, 150);
  },

  /**
   * Removes the variable dosage on the current prescribing therapy, if set.
   */
  removeVariableDosage: function()
  {
    this.dosePane.isRendered() ? this.dosePane.show() : this.dosePane.setHidden(false);
    this.variableDoseContainer.isRendered() ? this.variableDoseContainer.hide() : this.variableDoseContainer.setHidden(true);
    this.timedDoseElements.removeAll();
    this.dosingFrequencyPane.setFrequency(null, true);
    this._adjustDosingFrequencyPaneFields();
    if (this.dosePane.isRendered())
    {
      this.dosePane.requestFocusToNumerator();
    }
    this._handleDosingFrequencyChange();
    this.repeatProtocolUntilCanceled = false;
    this.protocolEndDate = null;
    this.therapyIntervalPane.setMinEnd(null);
    this.therapyIntervalPane.setTherapyEndEnabled(true);
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

  setMaxDoseContainerShowed: function(value)
  {
    value === true ? this._maxDoseContainer.show() : this._maxDoseContainer.hide();
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

  showHideParacetamolContainer: function(show)
  {
    this.showParacetamolContainer = show;
    if (this.paracetamolLimitContainer.isRendered())
    {
      show === true ? this.paracetamolLimitContainer.show() : this.paracetamolLimitContainer.hide();
    }
    else
    {
      this.paracetamolLimitContainer.setHidden(show === false);
    }
  },

  isChangeReasonAvailable: function()
  {
    return this.changeReasonAvailable === true;
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

  /**
   * @returns {boolean}
   */
  isEditMode: function()
  {
    return this.editMode === true;
  },

  /**
   * @returns {boolean}
   */
  _isSingleRouteWithMaxDoseSelected: function()
  {
    var selectedRoutes = this.routesPane.getSelectedRoutes();

    return tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1 &&
        !tm.jquery.Utils.isEmpty(selectedRoutes[0].getMaxDose());
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function()
  {
    return this.copyMode === true;
  },

  /**
   * @return {boolean} true, if the dosing calculation is possible, which is based on values of {@link #getOrderingBehaviour}
   * and {@link #getMedicationData}.
   */
  isDosageCalculationPossible: function()
  {
    return this.dosePane.isDosageCalculationPossible();
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   *
   * @returns {Number|null}
   */
  getMaxDosePercentage: function()
  {
    return this.maxDosePercentage;
  },

  /**
   * @returns {app.views.medications.ordering.MaxDoseContainer}
   */
  getMaxDoseContainer: function()
  {
    return this._maxDoseContainer;
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
   * @return {{type: *, value: *}|null} the currently selected dosing frequency, if any.
   * {@see app.views.medications.ordering.dosing.DosingFrequencyPane#getFrequency}
   */
  getDosingFrequency: function()
  {
    return this.dosingFrequencyPane.getFrequency();
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
   * @protected
   * @return {app.views.medications.common.dto.Therapy|null} the instance of the original therapy in case of the edit or
   * copy operation, otherwise null. Do not change this instance, it might still be used by the owner!
   */
  getOriginalTherapy: function()
  {
    return this._originalTherapy;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @return {app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory}
   */
  getVariableDoseDialogFactory: function()
  {
    return this.variableDoseDialogFactory;
  }
});

