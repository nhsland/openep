Class.define('app.views.medications.ordering.ComplexTherapyMedicationPane', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_TITRATION_CHANGE: new tm.jquery.event.EventType({
      name: 'complexTherapyMedicationPaneTitrationChange', delegateName: null
    })
  },
  cls: "complex-therapy-medication-pane",
  scrollable: 'visible',
  /** configs */
  view: null,
  typeaheadAdvancedMode: false,
  medicationData: null, //optional  MedicationDataDto.java
  addSpacer: false,
  medicationEditable: false,
  /* can be overridden by {@link #orderingBehaviour}, will override medication data if disabled */
  titratedDoseSupported: false,
  addRemoveEnabled: true,
  addElementEvent: null,
  removeElementEvent: null,
  volumeChangedEvent: null,
  numeratorChangeEvent: null,
  medicationChangedEvent: null,
  focusLostEvent: null, //optional
  showMaxDose: false,
  _maxDoseContainer: null,
  paracetamolLimitContainer: null,
  overdosePane: null,
  selectedRoute: null,
  additionalMedicationSearchFilter: null,
  preventUnlicensedMedicationSelection: false,
  preventTitrationChange: false,
  orderingBehaviour: null,
  referenceData: null,
  /** privates */
  medicationEditableSameGenericOnly: false,
  /** privates: components */
  medicationInfo: null,
  addButton: null,
  removeButton: null,
  doseContainer: null,
  buttonsContainer: null,
  medicationField: null,
  medicationTypeLabel: null,
  dosePane: null,
  universalOrderButton: null,
  universalOrderingPopupMenu: null,
  highRiskIconsContainer: null,

  _toggleTitrationButton: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.ordering.ComplexTherapyMedicationPane', [
      {eventType: app.views.medications.ordering.ComplexTherapyMedicationPane.EVENT_TYPE_TITRATION_CHANGE}
    ]);

    if (!this.referenceData)
    {
      throw Error('referenceData is not defined.');
    }

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    if (!this.getOrderingBehaviour().isTitratedDoseModeAvailable())
    {
      this.titratedDoseSupported = false;
    }

    this._buildComponents();
    this._buildGui();

    if (this.medicationData)
    {
      this._setMedicationType(this.medicationData.medication.medicationType);
      this.highRiskIconsContainer.presentHighAlertIcons(this.medicationData);
    }
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.medicationInfo = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor medication-info',
      width: 20,
      height: 30,
      hidden: true
    });
    this.medicationInfo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._showMedicationInfoPopup();
    });

    this.addButton = new tm.jquery.Container({cls: 'add-icon add-button', width: 30, height: 30, cursor: "pointer"});
    this.addButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self.addElementEvent(self);
    });
    this.removeButton = new tm.jquery.Container({
      cls: 'remove-icon remove-button',
      width: 30,
      height: 30,
      cursor: "pointer"
    });
    this.removeButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self.removeElementEvent();
    });

    this.medicationField = new app.views.medications.common.MedicationSearchField({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      dropdownWidth: "stretch",
      enabled: this.isMedicationEditable(),
      placeholder: this.view.getDictionary("enter.three.chars.to.search.medication") + "...",
      selection: this.medicationData ? this.medicationData.medication : null,
      additionalFilter: this.additionalMedicationSearchFilter,
      limitSimilarMedication: this.isMedicationEditableSameGenericOnly() && self.getMedicationData() ?
          this.getMedicationData().getMedication() :
          null,
      dropdownAppendTo: this.view.getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      searchResultFormatter: this.getOrderingBehaviour().getMedicationSearchResultFormatter()
    });

    this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      var medication = component.getSelectionMedication();
      if (medication)
      {
        self._readMedicationData(medication.getId());
      }
      else
      {
        self.setDoseVisible(false);
      }
    });

    this.highRiskIconsContainer = new app.views.medications.ordering.HighRiskMedicationIconsContainer({
      view: this.view,
      layout: tm.jquery.HFlexboxLayout.create("center", "center", 0)
    });

    if (this.getOrderingBehaviour().isUniversalOrderFormAvailable() && this.isMedicationEditable())
    {
      this.universalOrderingPopupMenu = appFactory.createPopupMenu();
      this.universalOrderingPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "open-universal-ordering",
        text: this.view.getDictionary("universal.form"),
        handler: function()
        {
          self._openUniversalMedicationDataDialog();
        },
        iconCls: 'icon-add-universal'
      }));

      this.universalOrderButton = new tm.jquery.Image({
        cls: 'icon-show-universal-ordering',
        width: 46,
        height: 34,
        cursor: 'pointer'
      });
      this.universalOrderButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        if (component.isEnabled())
        {
          self.universalOrderingPopupMenu.show(elementEvent);
        }
      });
    }

    this._maxDoseContainer = new app.views.medications.ordering.MaxDoseContainer({
      view: this.view,
      alignSelf: "center",
      percentage: this.maxDosePercentage,
      hidden: !this.isShowMaxDose()
    });

    this.paracetamolLimitContainer = new app.views.medications.ordering.ParacetamolLimitContainer({view: this.view});

    this.overdosePane = new app.views.medications.ordering.OverdoseContainer({
      view: this.view,
      alignSelf: "center",
      padding: "5 0 0 0",
      hidden: true
    });

    if (this.medicationData)
    {
      this._setUpOverdosePane();
    }

    this.medicationTypeLabel = new tm.jquery.Container({
      cls: 'TextData medication-type-label',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      padding: '5 0 0 40'
    });

    this.dosePane = new app.views.medications.ordering.dosing.DoseContainer({
      cls: "dose-pane with-large-input",
      margin: '0 0 0 5',
      view: this.view,
      pack: 'end',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      denominatorAlwaysVolume: true,
      addDosageCalculationPane: true,
      medicationData: this.medicationData,
      addDosageCalcBtn: true,
      showRounding: true,
      orderingBehaviour: this.getOrderingBehaviour(),
      referenceData: this.getReferenceData(),
      volumeChangedEvent: function()
      {
        self.volumeChangedEvent();
        self._calculateOverdose()
      },
      numeratorChangeEvent: function()
      {
        self.numeratorChangeEvent();
        self._calculateOverdose();
        //self.volumeChangedEvent();
      },
      focusLostEvent: function()
      {
        if (self.focusLostEvent)
        {
          self.focusLostEvent(self);
        }
      }
    });


    if (this.medicationData)
    {
      this._calculateOverdose();
    }
    else
    {
      self.setDoseVisible(false);
    }

    this._toggleTitrationButton = new tm.jquery.ToggleButton({
      cls: 'toggle-titration-button',
      iconCls: 'icon-titration-dosage-24',
      alignSelf: "center",
      enabled: !this.isPreventTitrationChange(),
      tooltip: appFactory.createDefaultHintTooltip(this.view.getDictionary("dose.titration"), "bottom"),
      handler: function(component)
      {
        component.isPressed() ? self._markAsTitrationDosing() : self._unmarkAsTitrationDosing();
      }
    });
    this._setTitrationButtonVisibility();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      height: 80,
      scrollable: 'visible',
      padding: '0 0 0 20'
    });

    if (this.addSpacer)
    {
      this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    }

    var rowsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      margin: '0 15 0 0',
      height: 80,
      scrollable: 'visible'
    });

    var searchContainer = new tm.jquery.Container({
      layout: new tm.jquery.HFlexboxLayout(),
      height: 30,
      scrollable: 'visible'
    });
    searchContainer.add(this.medicationField);
    searchContainer.add(this.highRiskIconsContainer);
    searchContainer.add(this.medicationInfo);
    if (!!this.universalOrderButton)
    {
      searchContainer.add(this.universalOrderButton);
    }
    rowsContainer.add(searchContainer);
    rowsContainer.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));

    this.doseContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "flex-start"),
      scrollable: "visible"
    });

    var addRemoveBtnsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "60px")
    });
    addRemoveBtnsContainer.add(this.addButton);
    addRemoveBtnsContainer.add(this.removeButton);
    this.doseContainer.add(addRemoveBtnsContainer);
    this.doseContainer.add(this.medicationTypeLabel);
    this.doseContainer.add(this._maxDoseContainer);
    this.doseContainer.add(this.paracetamolLimitContainer);
    this.doseContainer.add(this.overdosePane);
    this.doseContainer.add(this.dosePane);
    this.doseContainer.add(this._toggleTitrationButton);

    rowsContainer.add(this.doseContainer);
    mainContainer.add(rowsContainer);
    this.setAddRemoveButtonsVisible(this.addRemoveEnabled);

    this.add(mainContainer);
    this.add(new tm.jquery.Spacer({type: 'vertical', size: 7}));
    this.add(new tm.jquery.Container({style: 'border-bottom: 1px solid #d6d6d6'}));
  },

  _setMedicationType: function(medicationType)
  {
    this.medicationTypeLabel.setHtml(this.view.getDictionary('MedicationTypeEnum.' + medicationType));
  },

  /**
   * @param {Boolean} value
   */
  setMaxDoseContainerVisibility: function(value)
  {
    this.showMaxDose = value;
    value ? this._maxDoseContainer.show() : this._maxDoseContainer.hide();
  },

  /**
   * @returns {boolean}
   */
  isShowMaxDose: function()
  {
    return this.showMaxDose === true;
  },

  _setUpOverdosePane: function()
  {
    var self = this;
    this.overdosePane.setMedicationDataValues(self.medicationData);
    if (this.overdosePane.isTabletOrCapsule())
    {
      this.overdosePane.show();
    }
  },

  _calculateOverdose: function()
  {
    var dose = this.dosePane.isHidden() ? this.dosePane.getEmptyDose() : this.dosePane.getDose();
    this.overdosePane.calculateOverdose(dose.quantity);
  },

  /**
   * @param {string} medicationId
   * @param {function|null} [callback=null]
   * @private
   */
  _readMedicationData: function(medicationId, callback)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    view.getRestApi().loadMedicationData(medicationId).then(function onDataLoad(medicationData)
    {
      if (medicationData)
      {
        if (self.preventUnlicensedMedicationSelection && medicationData.isUnlicensedMedication())
        {
          self.medicationField.abortUnlicensedMedicationSelection();
        }
        else
        {
          self.medicationData = medicationData;
          self.dosePane.show();
          self.dosePane.setMedicationData(medicationData);
          self._setUpOverdosePane();
          self._setMedicationType(medicationData.medication.medicationType);
          self.clearMaxDose();
          self.setMaxDoseContainerVisibility(!tm.jquery.Utils.isEmpty(medicationData.defaultRoute) &&
              !tm.jquery.Utils.isEmpty(medicationData.defaultRoute.getMaxDose()));
          self.requestFocusToDose();
          self.hideMedicationInfo(false);
          self._setTitrationButtonVisibility();
          if (callback)
          {
            callback();
          }
          self.medicationChangedEvent(medicationData);
          self.highRiskIconsContainer.presentHighAlertIcons(medicationData);
        }
      }
      else
      {
        var message = view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            view.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 320, 150).show();
      }
    });
  },

  /**
   * @param {app.views.medications.common.dto.Medication} medication
   */
  setMedication: function(medication)
  {
    this.medicationField.setSelection(medication, true);
  },

  _openUniversalMedicationDataDialog: function()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    var universalMedicationDataContainer = new app.views.medications.ordering.UniversalMedicationDataContainer({
      view: this.view,
      cls: 'universal-medication-data-container'
    });
    var universalMedicationDataDialog = appFactory.createDataEntryDialog(
        self.view.getDictionary("universal.form"),
        null,
        universalMedicationDataContainer,
        function(resultData)
        {
          if (resultData)
          {
            var medicationData = resultData.value;
            self.medicationData = medicationData;
            self.dosePane.show();
            self.dosePane.setMedicationData(medicationData);
            self._setMedicationType(medicationData.medication.medicationType);
            self.requestFocusToDose();
            self.medicationChangedEvent(medicationData);
            self.medicationField.setEnabled(false);
          }
        },
        468,
        300
    );
    if (this.view.getDoseForms().length === 0)
    {
      this.view.loadDoseForms();
    }
    universalMedicationDataDialog.show();
  },

  /** public methods */
  getVolume: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var dose = this.dosePane.isHidden() ? this.dosePane.getEmptyDoseWithUnits() : this.dosePane.getDoseWithUnits();
    if (dose.quantityDenominator && view.getUnitsHolder().isUnitInLiquidGroup(dose.denominatorUnit))
    {
      return view.getUnitsHolder().convertToKnownUnit(
          dose.quantityDenominator,
          dose.denominatorUnit,
          enums.knownUnitType.ML);
    }
    else if (this.getView().getUnitsHolder().isUnitInLiquidGroup(dose.quantityUnit))
    {
      return view.getUnitsHolder().convertToKnownUnit(
          dose.quantity,
          dose.quantityUnit,
          enums.knownUnitType.ML);
    }
    return 0;
  },

  setVolume: function(volume, preventEvent)
  {
    this.dosePane.setVolume(volume, preventEvent);
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  setUniversalMedicationAndDose: function(medicationData, numerator, volume, preventEvents)
  {
    var self = this;
    if (medicationData)
    {
      self.medicationData = medicationData;
      self.setDoseVisible(true);
      self.dosePane.setMedicationData(medicationData);
      self._setMedicationType(medicationData.medication.medicationType);
      self.setAddRemoveButtonsVisible(false);
      self.requestFocusToDose();
      self.medicationChangedEvent(medicationData);
      self.dosePane.setDoseNumerator(numerator, preventEvents);
      self.dosePane.setDoseDenominator(volume, preventEvents);
    }
  },

  setDose: function(numerator, volume, preventEvent)
  {
    this.dosePane.setDoseNumerator(numerator, preventEvent);
    this.dosePane.setDoseDenominator(volume, preventEvent);
  },

  getInfusionIngredient: function()
  {
    var dose = this.dosePane.isHidden() ? this.dosePane.getEmptyDose() : this.dosePane.getDose();
    if (this.medicationData)
    {
      var doseUnits = this.dosePane.getDoseUnits();
      return {
        medication: this.medicationData.getMedication(),
        quantity: dose.quantity,
        quantityUnit: doseUnits.quantityUnit,
        quantityDenominator: dose.quantityDenominator,
        quantityDenominatorUnit: doseUnits.denominatorUnit,
        doseForm: this.medicationData.getDoseForm()
      }
    }
    return null;
  },

  requestFocusToDose: function()
  {
    this.dosePane.requestFocusToDose();
  },

  focusToMedicationField: function()
  {
    this.medicationField.focus();
  },

  getMedicationPaneValidations: function()
  {
    var self = this;
    var formFields = [];
    if (!this.dosePane.isHidden())
    {
      formFields = formFields.concat(this.dosePane.getDosePaneValidations());
    }
    formFields.push(new tm.jquery.FormField({
      component: self.medicationField,
      required: true
    }));
    return formFields;
  },

  setDoseVisible: function(visible)
  {
    if (visible)
    {
      this.isRendered() ? this.dosePane.show() : this.dosePane.setHidden(false);
    }
    else
    {
      this.isRendered() ? this.dosePane.hide() : this.dosePane.setHidden(true);
      this.dosePane.clear(true);
    }
  },

  /**
   * @param {boolean} showAddRemoveButtons
   * @param {boolean} medicationEditable
   * @param {boolean} medicationEditableSameGenericOnly
   * @param {boolean} doseEditable
   */
  setPaneEditable: function(showAddRemoveButtons, medicationEditable, medicationEditableSameGenericOnly, doseEditable)
  {
    this.setAddRemoveButtonsVisible(showAddRemoveButtons);
    this.medicationField.setEnabled(medicationEditable);
    this.dosePane.setPaneEditable(doseEditable);

    if (medicationEditableSameGenericOnly && this.getMedicationData())
    {
      this.medicationField.setLimitBySimilar(this.getMedicationData().getMedication());
      this.setMedicationEditableSameGenericOnly(true);
    }
    else
    {
      this.medicationField.setLimitBySimilar(null);
      this.setMedicationEditableSameGenericOnly(false);
    }
    this.setMedicationEditable(medicationEditable);
  },

  _getSelectedRoute: function()
  {
    return tm.jquery.Utils.isEmpty(this.selectedRoute) ? [] : [this.selectedRoute];
  },

  /**
   * Shows the universal medication order button. Does nothing if the functionality is disabled by
   * {@link #orderingBehaviour}.
   * @private
   */
  _showUniversalMedicationEditButton: function()
  {
    if (!this.universalOrderButton)
    {
      return;
    }

    this.universalOrderButton.show();
    this.medicationInfo.hide();
    this.universalOrderingPopupMenu.getMenuItems()[0].setText(this.view.getDictionary("edit"));
  },

  _setTitrationButtonVisibility: function()
  {
    var isVisible = this.isTitratedDoseSupported() && this.isTitrationSupportedByMedication();

    if (isVisible)
    {
      this.isRendered() ? this._toggleTitrationButton.show() : this._toggleTitrationButton.setHidden(false);
    }
    else
    {
      if (this._toggleTitrationButton.isPressed())
      {
        this._toggleTitrationButton.setPressed(false);
      }
      this.isRendered() ? this._toggleTitrationButton.hide() : this._toggleTitrationButton.setHidden(true);
    }
  },

  /**
   * @param {boolean} [preventEvent=false]
   * @private
   */
  _markAsTitrationDosing: function(preventEvent)
  {
    this.setDoseVisible(false);
    if (!preventEvent)
    {
      this._signalTitrationChangedEvent();
    }
  },

  /**
   * @param {boolean} [preventEvent=false]
   * @private
   */
  _unmarkAsTitrationDosing: function(preventEvent)
  {
    this.setDoseVisible(true);
    if (!preventEvent)
    {
      this._signalTitrationChangedEvent();
    }
  },

  _signalTitrationChangedEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.ComplexTherapyMedicationPane.EVENT_TYPE_TITRATION_CHANGE,
      eventData: {selected: this._toggleTitrationButton.isPressed()}
    }), null);
  },

  _showMedicationInfoPopup: function()
  {
    var appFactory = this.getView().getAppFactory();

    var medicationInfoContent = new app.views.medications.common.MedicationDetailsContainer({
      view: this.getView(),
      medicationData: [this.medicationData],
      selectedRoute: this._getSelectedRoute()
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

  setAddRemoveButtonsVisible: function(visible)
  {
    if (visible)
    {
      this.addButton.isRendered() ? this.addButton.show(): this.addButton.setHidden(false);
      this.removeButton.isRendered() ? this.removeButton.show() : this.removeButton.setHidden(false);
    }
    else
    {
      this.addButton.isRendered() ? this.addButton.hide(): this.addButton.setHidden(true);
      this.removeButton.isRendered() ? this.removeButton.hide() : this.removeButton.setHidden(true);
    }
  },

  setMaxDoseValuesAndSelectedRoute: function(medicationData, route)
  {
    this._maxDoseContainer.setMaxDoseValuesAndNumeratorUnit(medicationData, route);
    this.selectedRoute = route;
  },

  setMaxDosePercentage: function(percentage)
  {
    this._maxDoseContainer.setPercentage(percentage);
  },

  calculateMaxDosePercentage: function(quantity, timesPerDay, timesPerWeek, variable)
  {
    return this._maxDoseContainer.calculatePercentage(quantity, timesPerDay, timesPerWeek, variable);
  },

  /**
   * @param {app.views.medications.warnings.dto.ParacetamolRuleResult} calculatedParacetamolRule
   */
  setCalculatedParacetamolLimit: function(calculatedParacetamolRule)
  {
    this.paracetamolLimitContainer.setCalculatedParacetamolLimit(calculatedParacetamolRule);
    if (this.paracetamolLimitContainer.hasContent())
    {
      this.paracetamolLimitContainer.show();
    }
  },

  getMaxDosePercentage: function()
  {
    return this._maxDoseContainer.getPercentage();
  },

  clearMaxDose: function()
  {
    this._maxDoseContainer.clear();
  },

  /**
   * Hide or show the medication info icon and show the universal order form button in it's place. Keep in mind that
   * if the universal order form functionality is disabled by {@link #orderingBehaviour}, the only acceptable result is
   * to show the medication info icon.
   * @param {boolean} hide
   */
  hideMedicationInfo: function(hide)
  {
    if (hide)
    {
      this._showUniversalMedicationEditButton();
    }
    else
    {
      if (!!this.universalOrderButton)
      {
        this.universalOrderButton.hide();
      }
      this.medicationInfo.show();
    }
  },

  /**
   * Manually closes any currently open tooltips of this instance, which is sometimes desired when changing the state
   * of the component programmatically or before repainting the parent container, which will break the tooltip state.
   */
  hideAllTooltips: function()
  {
    if (this.medicationInfo && this.medicationInfo.getTooltip())
    {
      this.medicationInfo.getTooltip().hide();
    }
  },

  /**
   * If titration is not supported, it will override the value from medication data.
   * @returns {boolean}
   */
  isTitratedDoseSupported: function()
  {
    return this.titratedDoseSupported === true;
  },

  /**
   * If not supported, titrated dose will not be possible regardless if the medication supports it. The value
   * will be ignored if {@link #orderingBehaviour} disables the functionality.
   * @param {boolean} value
   */
  setTitratedDoseSupported: function(value)
  {
    this.titratedDoseSupported = value && this.getOrderingBehaviour().isTitratedDoseModeAvailable();
    this._setTitrationButtonVisibility();
  },

  /**
   * Is titration possible? Based on the active medication data.
   * @returns {boolean}
   */
  isTitrationSupportedByMedication: function()
  {
    return this.medicationData && !tm.jquery.Utils.isEmpty(this.medicationData.getTitration());
  },

  /**
   * @returns {boolean}
   */
  isMedicationEditable: function()
  {
    return this.medicationEditable === true;
  },

  /**
   * @param {boolean} value
   */
  setMedicationEditable: function(value)
  {
    this.medicationEditable = value;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
  },

  /**
    * @param value
   */
  setMedicationEditableSameGenericOnly: function(value)
  {
    this.medicationEditableSameGenericOnly = value;
  },

  /**
   * @returns {boolean}
   */
  isMedicationEditableSameGenericOnly: function()
  {
    return this.medicationEditableSameGenericOnly === true;
  },

  /**
   * @returns {string|null}
   */
  getTitrationDoseType: function()
  {
    return this._toggleTitrationButton.isPressed() ? this.medicationData.getTitration() : null;
  },

  /**
   * @param {string} type
   * @param {boolean} preventEvent
   */
  setTitrationDoseType: function(type, preventEvent)
  {
    var titrationActive = !tm.jquery.Utils.isEmpty(type);
    if (this._toggleTitrationButton.isPressed() !== titrationActive)
    {
      this._toggleTitrationButton.setPressed(titrationActive, preventEvent);
      titrationActive ? this._markAsTitrationDosing(preventEvent) : this._unmarkAsTitrationDosing(preventEvent);
    }
  },

  /**
   * @returns {boolean}
   */
  isPreventTitrationChange: function()
  {
    return this.preventTitrationChange === true;
  },

  /**
   * @returns {app.views.medications.ordering.MaxDoseContainer}
   */
  getMaxDoseContainer: function()
  {
    return this._maxDoseContainer;
  },

  /**
   * @returns {*|tm.jquery.Object}
   */
  getCalculationFormula: function()
  {
    return this.dosePane.getCalculationFormula();
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

