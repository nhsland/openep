Class.define('app.views.medications.timeline.administration.TherapyAdministrationDataEntryContainer', 'app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer', {
  /** privates */
  medicationId: null,
  prescribedMedicationData: null,
  medicationProducts: null,
  _medicationDataForAllIngredients: null,
  _nextAdministrationDisplayValue: null,
  _administrationWarnings: null,
  /** @type string {@link app.views.medications.TherapyEnums.administrationResultEnum} */
  administrationResultEnum: app.views.medications.TherapyEnums.administrationResultEnum.GIVEN,
  administrationNotAdministeredReasonEnum: null,
  selfAdministrationTypeEnum: null,
  reasonMap: null,
  stopFlow: null,
  lastPositiveInfusionRate: null,
  therapyDoseTypeEnum: null, //optional
  preselectedProductMedicationData: null,
  barcode: null,
  currentStartingDevice: null,
  /** privates: components */
  therapyDescriptionContainer: null,
  medicationField: null,
  administrationDateTimeCard: null,
  ratePane: null,
  _routesContainer: null, // present only if therapy dto contains more then one rotue (discretionary routes)
  _routesPane: null,
  _oxygenRouteRowContainer: null,
  _oxygenRouteContainer: null,
  medicationData: null,
  setMedicationField: null,
  currentAdministration: null,

  administrationResultMenu: null,
  administrationResultMenuContainer: null,
  administrationResultButtonGroup: null,
  administrationResultButtonsContainer: null,
  administrationGivenContainer: null,
  administrationDeferContainer: null,
  administrationSelfAdminContainer: null,
  administrationNotGivenContainer: null,
  selfAdminButtonGroup: null,
  notGivenButtonGroup: null,
  medicationContainer: null,
  resetButton: null,
  _medicationInfoButton: null,
  _titrationDataIcon: null,
  _medicationBarcodeContainer: null,
  _administrationDoseContainer: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    var view = this.getView();
    this.medicationProducts = !tm.jquery.Utils.isArray(this.medicationProducts) ? [] : this.medicationProducts;
    this.medicationId = this.therapy.getMedicationId();
    this.careProfessionals = [];

    this.reasonMap = view.getChangeReasonTypeHolder().getMap();
    this._buildComponents();
    this._buildGui();
    this._setAdministrationValues();
    this._setMedicationDataValues();

  },

  _buildComponents: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var isOxygenTherapy = this.getTherapy() && this.getTherapy().isOrderTypeOxygen();

    this.administrationDateTimeCard = new tm.jquery.Container({layout: new tm.jquery.HFlexboxLayout({gap: 5})});

    var routesLabelContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    routesLabelContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('route'), 0));

    var routePaneContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-end', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });

    this._routesPane = new app.views.medications.ordering.RoutesPane({
      view: view,
      height: 30,
      maxRouteButtons: 3,
      maxRouteCharLength: 40,
      discretionaryRoutesDisabled: true,
      changeEvent: function()
      {
        self._configureWitnessRequirement();
      }
    });
    routePaneContainer.add(this._routesPane);

    this._routesContainer = new tm.jquery.Container({
      cls: "dose-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
      height: 48,
      scrollable: "visible",
      hidden: true
    });

    this._routesContainer.add(routesLabelContainer);
    this._routesContainer.add(routePaneContainer);

    this._administrationDoseContainer = new app.views.medications.timeline.administration.TherapyAdministrationDoseContainer({
      view: view,
      administration: this.getAdministration(),
      administrations: this.getAdministrations(),
      therapy: this.getTherapy(),
      medicationData: this.getMedicationData(),
      prescribedMedicationData: this.prescribedMedicationData,
      stopFlow: this.stopFlow,
      therapyDoseTypeEnum: this.therapyDoseTypeEnum
    });

    this._administrationDoseContainer.on(
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_VOLUME_CHANGE,
        function(component, componentEvent)
        {
          self._handleMedicationIngredientRule(componentEvent.eventData.medicationIngredientRule)
        }
    );

    this._administrationDoseContainer.on(
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_MEDICATION_DOSE_RECALCULATED,
        function(component, componentEvent)
        {
          self._handleMedicationIdentifierStatus(componentEvent.eventData.medicationData)
        }
    );

    this._administrationDoseContainer.on(
        app.views.medications.timeline.administration.TherapyAdministrationDoseContainer.EVENT_TYPE_DOSE_FOCUS_LOST,
        function()
        {
          self.getAdministrationDateField().focus();
        }
    );

    this._oxygenRouteRowContainer = new tm.jquery.Container({
      cls: 'oxygen-route-row-container',
      scrollable: "visible",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      hidden: true
    });

    var oxygenRouteLabel = new tm.jquery.Component({
      cls: 'TextLabel route-label',
      html: view.getDictionary('device'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this._oxygenRouteContainer = new app.views.medications.ordering.oxygen.OxygenRouteContainer({
      view: view,
      allowDeviceDeselect: false,
      cls: "oxygen-route-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._oxygenRouteRowContainer.add(oxygenRouteLabel);
    this._oxygenRouteRowContainer.add(this._oxygenRouteContainer);

    this.administrationGivenContainer = new tm.jquery.Container({
      cls: 'administration-given-container'
    });
    this.administrationDeferContainer = new tm.jquery.Container({
      cls: 'administration-defer-container'
    });

    this.administrationSelfAdminContainer = new tm.jquery.Container({
      cls: 'administration-self-admin-container with-top-border',
      height: 73
    });

    var selfAdminLabel = app.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('level.self.admin'));
    var selfAdminChartedByNurseButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('charted.by.nurse'),
      testAttribute: 'self-admin-charted-by-nurse',
      handler: function()
      {
        self.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.CHARTED_BY_NURSE;
      }
    });
    var selfAdminAutoChartedButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('automatically.charted'),
      testAttribute: 'self-admin-auto-charted',
      handler: function()
      {
        self.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.AUTOMATICALLY_CHARTED;
      }
    });

    this.selfAdminButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-bubble",
      buttons: [selfAdminChartedByNurseButton, selfAdminAutoChartedButton],
      orientation: 'horizontal',
      type: 'radio'
    });
    this.administrationSelfAdminContainer.add(selfAdminLabel);
    this.administrationSelfAdminContainer.add(this.selfAdminButtonGroup);

    this.administrationNotGivenContainer = new tm.jquery.Container({
      cls: 'administration-not-given-container with-top-border'
    });
    var notGivenLabel = app.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('reason'));

    var patientRefusedButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('patient.refused'),
      testAttribute: 'not-given-patient-refused',
      data: 11
    });
    var nilByMouthButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('nil.by.mouth'),
      testAttribute: 'not-given-nil-by-mouth',
      data: 12
    });
    var drugUnavailableButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('drug.unavailable'),
      testAttribute: 'not-given-medicine-unavailable',
      data: 13
    });
    var patientUnavailableButton = new tm.jquery.Button({
      cls: "btn-bubble",
      text: view.getDictionary('patient.unavailable'),
      testAttribute: 'not-given-patient-unavailable',
      data: 14
    });
    var clinicalReasonsNotGivenButton = new tm.jquery.Button({
      cls: "btn-bubble",
      id: "clinical-reason",
      text: view.getDictionary('clinical.reason'),
      testAttribute: 'not-given-clinical-reason',
      data: 15
    });

    this.notGivenButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-bubble",
      buttons: [patientUnavailableButton, nilByMouthButton, patientRefusedButton, drugUnavailableButton,
        clinicalReasonsNotGivenButton],
      orientation: 'horizontal',
      type: 'radio'
    });

    this.notGivenButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.notGivenButtonGroup.getSelection().length > 0)
      {
        self._handleNotAdministeredReasonChange(self.notGivenButtonGroup.getSelection()[0].getData());
      }
    });

    this.administrationNotGivenContainer.add(notGivenLabel);
    this.administrationNotGivenContainer.add(this.notGivenButtonGroup);

    this.administrationResultMenu = new tm.jquery.SimpleCardContainer({
      cls: "borderless",
      animation: "fade",
      prerendering: true,
      optimized: true,
      activeIndex: 0
    });
    this.administrationResultMenu.add(this.administrationGivenContainer);
    this.administrationResultMenu.add(this.administrationDeferContainer);
    this.administrationResultMenu.add(this.administrationSelfAdminContainer);
    this.administrationResultMenu.add(this.administrationNotGivenContainer);

    var givenButton = new tm.jquery.Button({
      pressed: true,
      cls: "btn-ios",
      text: view.getDictionary('given'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      testAttribute: 'administration-mark-given',
      handler: function()
      {
        self._applyAdministrationGivenState(enums.administrationResultEnum.GIVEN, self.administrationGivenContainer);
      }
    });
    var deferButton = new tm.jquery.Button({
      cls: "btn-ios",
      text: view.getDictionary('defer'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      testAttribute: 'administration-mark-deferred',
      handler: function()
      {
        self._applyAdministrationSkippedState(enums.administrationResultEnum.DEFER, self.administrationDeferContainer);
      }
    });
    var selfAdminButton = new tm.jquery.Button({
      cls: "btn-ios",
      text: view.getDictionary('self.admin'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      testAttribute: 'administration-mark-self-admission',
      handler: function()
      {
        self._applyAdministrationGivenState(
            enums.administrationResultEnum.SELF_ADMINISTERED,
            self.administrationSelfAdminContainer);
        self.selfAdminButtonGroup.setSelection([]);
      }
    });
    var notGivenButton = new tm.jquery.Button({
      cls: "btn-ios",
      text: view.getDictionary('not.given'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      testAttribute: 'administration-mark-not-given',
      handler: function()
      {
        self._applyAdministrationSkippedState(enums.administrationResultEnum.NOT_GIVEN, self.administrationNotGivenContainer);
        self.notGivenButtonGroup.setSelection([], true);
        self._handleNotAdministeredReasonChange(null);
      }
    });

    var isTitratedTherapy = this.getTherapy() && this.getTherapy().isTitrationDoseType();
    var addSelfAdminBtn = view.isAutoAdministrationChartingEnabled() && !isOxygenTherapy && !isTitratedTherapy;

    this.administrationResultButtonGroup = new tm.jquery.ButtonGroup({
      cls: 'btn-group-ios',
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      buttons: addSelfAdminBtn ? [givenButton, deferButton, selfAdminButton, notGivenButton] :
          [givenButton, deferButton, notGivenButton],
      orientation: 'horizontal',
      type: 'radio'
    });

    this.administrationResultButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._configureWitnessRequirement();
    });

    this.administrationResultButtonsContainer = new tm.jquery.Container({
      cls: 'admin-result-btns-container',
      layout: tm.jquery.HFlexboxLayout.create('center', 'center'),
      height: 48
    });
    this.administrationResultButtonsContainer.add(this.administrationResultButtonGroup);

    this.administrationResultMenuContainer = new tm.jquery.Container({
      cls: 'admin-status-container',
      layout: tm.jquery.VFlexboxLayout.create("start", "start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      scrollable: 'visible'
    });
    this.administrationResultMenuContainer.add(this.administrationResultMenu);
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
   * @param {Array} map
   * @param {Number} code
   * @returns {Object|null}
   * @private
   */
  _getReasonFromMap: function(map, code)
  {
    var reason = map.find(function(r)
    {
      return Number(r.code) === code;
    });
    return reason || null;
  },

  /**
   * Returns the first route from therapy, if only one route is available. Otherwise it returns the selected route from
   * {@link _routesPane}. We anticipate at least one route on the therapy, if more then one route is available, the
   * {@link _routesContainer} should be visible. Keep in mind that this function will return null if no route is selected.
   * @returns {app.views.medications.common.dto.MedicationRoute | null}
   * @private
   */
  _getSelectedRoute: function()
  {
    if (this.getTherapy().getRoutes().length === 1)
    {
      return this.getTherapy().getRoutes()[0];
    }
    if (!this._routesContainer.isHidden())
    {
      return this._routesPane.getSelectedRoutes().length > 0 ? this._routesPane.getSelectedRoutes()[0] : null;
    }
    return null;
  },

  _setAdministrationValues: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var therapy = this.therapy;
    var administration = this.administration;

    if (administration)
    {
      if (this.editMode)
      {
        var notAdministeredReason = administration.getNotAdministeredReason();
        var reasonCode = !tm.jquery.Utils.isEmpty(notAdministeredReason) ? Number(notAdministeredReason.code) : null;
        this.administrationResultEnum = administration.getAdministrationResult();
        if (administration.getAdministrationResult() === enums.administrationResultEnum.GIVEN)
        {
          this.administrationResultButtonGroup.setSelection([this.administrationResultButtonGroup.getButtons()[0]], false);
          this.administrationResultMenu.setActiveItem(this.administrationGivenContainer);
        }
        if (administration.getAdministrationResult() === enums.administrationResultEnum.DEFER)
        {
          this.administrationResultButtonGroup.setSelection([this.administrationResultButtonGroup.getButtons()[1]], false);
          this.administrationResultMenu.setActiveItem(this.administrationDeferContainer);
          this.administrationNotAdministeredReasonEnum = notAdministeredReason;
        }
        if (administration.getAdministrationResult() === enums.administrationResultEnum.SELF_ADMINISTERED)
        {
          this.administrationResultButtonGroup.setSelection([this.administrationResultButtonGroup.getButtons()[2]], false);
          this.administrationResultMenu.setActiveItem(this.administrationSelfAdminContainer);
          if (administration.getSelfAdministrationType() === enums.selfAdministrationTypeEnum.CHARTED_BY_NURSE)
          {
            this.selfAdminButtonGroup.setSelection([this.selfAdminButtonGroup.getButtons()[0]], false);
            this.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.CHARTED_BY_NURSE;
          }
          else if (administration.getSelfAdministrationType() === enums.selfAdministrationTypeEnum.AUTOMATICALLY_CHARTED)
          {
            this.selfAdminButtonGroup.setSelection([this.selfAdminButtonGroup.getButtons()[1]], false);
            this.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.AUTOMATICALLY_CHARTED;
          }
        }
        if (administration.getAdministrationResult() === enums.administrationResultEnum.NOT_GIVEN)
        {
          this.administrationResultButtonGroup.setSelection([this.administrationResultButtonGroup.getButtons()[3]], false);
          this.administrationResultMenu.setActiveItem(this.administrationNotGivenContainer);
          this.notGivenButtonGroup.setSelection([this._findNotGivenButtonByReasonCode(reasonCode)], true);
          this._handleNotAdministeredReasonChange(reasonCode);
          this.administrationNotAdministeredReasonEnum = notAdministeredReason;
        }
        this.setAdministrationDocumentedTimeLabel(this.administrationResultEnum === enums.administrationResultEnum.GIVEN ||
            this.administrationResultEnum === enums.administrationResultEnum.SELF_ADMINISTERED);
      }
      else
      {
        this.administrationResultEnum = enums.administrationResultEnum.GIVEN;
      }
    }

    var selfAdministeringActionEnum = !tm.jquery.Utils.isEmpty(therapy) ? therapy.selfAdministeringActionEnum : null;
    if (!tm.jquery.Utils.isEmpty(selfAdministeringActionEnum) &&
        selfAdministeringActionEnum !== enums.selfAdministeringActionEnum.STOP_SELF_ADMINISTERING &&
        !tm.jquery.Utils.isEmpty(administration) &&
        !this.editMode)
    {
      this.administrationResultEnum = enums.administrationResultEnum.SELF_ADMINISTERED;
      this.administrationResultButtonGroup.setSelection([this.administrationResultButtonGroup.getButtons()[2]], false);
      this.administrationResultMenu.setActiveItem(this.administrationSelfAdminContainer);

      if (selfAdministeringActionEnum === enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        this.selfAdminButtonGroup.setSelection([this.selfAdminButtonGroup.getButtons()[0]], false);
        this.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.CHARTED_BY_NURSE;
      }
      else if (selfAdministeringActionEnum === enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        this.selfAdminButtonGroup.setSelection([this.selfAdminButtonGroup.getButtons()[1]], false);
        this.selfAdministrationTypeEnum = enums.selfAdministrationTypeEnum.AUTOMATICALLY_CHARTED;
      }
    }
  },

  /**
   * @param {number} reasonCode
   * @returns {tm.jquery.Button|null}
   * @private
   */
  _findNotGivenButtonByReasonCode: function(reasonCode)
  {
    return this.notGivenButtonGroup.getButtons().find(function(button)
    {
      return button.getData() === reasonCode;
    })
  },

  /**
   * Resets validation form and sets internal variable state according to selected reason. Takes null as an argument to
   * enable clearing the selection.
   * @param {number|null} reasonCode
   * @private
   */
  _handleNotAdministeredReasonChange: function(reasonCode)
  {
    this.getValidationForm().reset();
    this.administrationNotAdministeredReasonEnum = this._getReasonFromMap(
        this.reasonMap.ADMINISTRATION_NOT_GIVEN,
        reasonCode);
  },

  _setMedicationDataValues: function()
  {
    if (this.getTherapy().isOrderTypeComplex() &&
        tm.jquery.Utils.isArray(this.getMedicationData()) && this.getMedicationData().length > 0)
    {
      this._findFirstMedicationAndValidateData(this.getMedicationData(), this.getTherapy().getMainMedication().getId());
      this._applyTitrationDataIconVisibility();
    }
    else if (this.getTherapy().getMainMedication() && !this.getTherapy().getMainMedication().isMedicationUniversal())
    {
      this._setPrescribedMedicationData(this.getMedicationData());
      var medicationData =
          this.preselectedProductMedicationData ? this.preselectedProductMedicationData : this.getMedicationData();
      this.applyMedicationInfoButtonTooltip(medicationData);

      this._validateAndPresentData(medicationData, !tm.jquery.Utils.isEmpty(this.preselectedProductMedicationData));
      this._applyTitrationDataIconVisibility();
    }
    else // presume uncoded medications (universal forms)
    {
      this._validateAndPresentData(this.getMedicationData(), false);
    }
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var medicationSubstitutionPossible = this.medicationProducts.length > 0;
    
    this.add(this.buildTherapyDescriptionAndInfoContainer());

    this.add(new tm.jquery.Spacer({type: 'vertical', size: 4}));

    if(!tm.jquery.Utils.isEmpty(this.administration) && !tm.jquery.Utils.isEmpty(this.administration.getTaskId()) &&
        this.administrationType !== enums.administrationTypeEnum.STOP)
    {
      this.add(this.administrationResultButtonsContainer);
    }

    this.add(this.administrationResultMenu);

    if (!this.therapy.getMainMedication().isMedicationUniversal() &&
        (view.getTherapyAuthority().isMedicationIdentifierScanningAllowed() || medicationSubstitutionPossible ))
    {
      var medicationContainerLayout = tm.jquery.HFlexboxLayout.create(
          view.getTherapyAuthority().isMedicationIdentifierScanningAllowed() && medicationSubstitutionPossible
              ? "flex-start" : "center",
          "center", 5);
      this.medicationContainer = new tm.jquery.Container({
        cls: 'medication-container',
        layout: medicationContainerLayout,
        height:
            view.getTherapyAuthority().isMedicationIdentifierScanningAllowed() && medicationSubstitutionPossible ? 72 : 48,
        scrollable: 'visible'
      });
      var medicationLabel = new tm.jquery.Component({
        cls: 'TextLabel medication-label',
        html: view.getDictionary('medication'),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });

      var medicationFieldsContainer = new tm.jquery.Container({
        scrollable: 'visible',
        cls: 'medication-fields-container',
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-end", 0),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      if (view.getTherapyAuthority().isMedicationIdentifierScanningAllowed())
      {
        this._medicationBarcodeContainer = new app.views.medications.timeline.administration.MedicationBarcodeContainer({
          view: view,
          therapy: this.getTherapy(),
          barcode: this.barcode,
          medicationProducts: this.medicationProducts
        });
        this._medicationBarcodeContainer.on(
            app.views.medications.timeline.administration.MedicationBarcodeContainer.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
            function(component, componentEvent)
            {
              var eventData = componentEvent.getEventData();
              self._medicationIdFromBarcodeChanged(eventData.medicationIdFound, eventData.medicationId);
            });
        medicationFieldsContainer.add(this._medicationBarcodeContainer);
      }

      if (medicationSubstitutionPossible)
      {
        this.medicationField =
            app.views.medications.MedicationUtils.createMedicationTypeaheadField(
                view,
                null,
                true,
                "medication-field");

        this.medicationField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));
        this.medicationField.setSource(this.medicationProducts);
        this.medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
        {
          if (self._medicationBarcodeContainer)
          {
            self._medicationBarcodeContainer.clear();
          }
          var selection = component.getSelection();
          if (selection && self.medicationId !== selection.getId())
          {
            self._loadMedicationData(selection.getId());
          }
        });
        medicationFieldsContainer.add(this.medicationField);
      }
      this.medicationContainer.add(medicationLabel);
      this.medicationContainer.add(medicationFieldsContainer);

      this.add(this.medicationContainer);
    }
    this.add(this._routesContainer);
    this.add(this._oxygenRouteRowContainer);
    this.add(this._administrationDoseContainer);
    if (view.isInfusionBagEnabled())
    {
      this.add(this.getBagContainer())
    }
    this.add(this.getAdministrationTimeContainer());
    if (!!this.getWitnessContainer())
    {
      this.add(this.getWitnessContainer());
      this._configureWitnessRequirement();
    }
    if (this.isRequestSupplyEnabled())
    {
      this.add(this.getRequestSupplyContainer());
    }
    this.add(this.getCommentContainer());
    this.add(this.getWarningContainer());
  },

  _showTitrationData: function()
  {
    var dialogBuilder = new app.views.medications.timeline.titration.TitrationDialogBuilder({
      view: this.getView(),
      therapy: this.getLatestTherapyVersion() ? this.getLatestTherapyVersion() : this.getTherapy(),
      administration: this.administration
    });

    dialogBuilder
        .setTitrationType(this.prescribedMedicationData.getTitration())
        .showDialog()
        .then(function doNothing()
        {
        });
  },

  /**
   * @param {Boolean} medicationWithIdFound
   * @param {Number} medicationId
   * @private
   */
  _medicationIdFromBarcodeChanged: function(medicationWithIdFound, medicationId)
  {
    if (medicationWithIdFound)
    {
      this._loadMedicationData(medicationId, true);
    }
    else if (this.medicationField)
    {
      this.medicationField.setSelection(null, null, true);
    }
  },

  _confirmTherapyAdministration: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    if (this.administration === null)
    {
      this.administration = new app.views.medications.timeline.administration.dto.Administration();
      this.administration.setAdditionalAdministration(true);
      this.administration.setAdministrationType(this.administrationType);
    }
    this.administration.setAdministeredDose(new app.views.medications.common.dto.TherapyDose());
    this.administration.setAdministrationResult(this.administrationResultEnum);
    if (!!this.getWitnessContainer())
    {
      this.administration.setWitness(this.getWitnessContainer().getAuthenticatedWitness());
    }

    if (this.administrationResultEnum === enums.administrationResultEnum.DEFER ||
        this.administrationResultEnum === enums.administrationResultEnum.NOT_GIVEN)
    {
      this.administration.setNotAdministeredReason(this.administrationNotAdministeredReasonEnum);
    }
    if (this.administrationResultEnum === enums.administrationResultEnum.SELF_ADMINISTERED)
    {
      this.administration.setSelfAdministrationType(this.selfAdministrationTypeEnum);
    }

    this.administration.setComment(this.getAdministrationComment());

    var selectedTimestamp = this._getSelectedTimestamp();
    var therapyDose = this._administrationDoseContainer.buildTherapyDose();

    if (this.medicationField)
    {
      var selectedMedication = this.medicationField.getSelection();
      var therapyMedicationId = this.getTherapy().getMedicationId();
      if (selectedMedication.getId() !== therapyMedicationId)
      {
        this.administration.setSubstituteMedication(selectedMedication);
      }
    }

    this.administration.setRoute(this._getSelectedRoute());

    if (this.isInfusionBagEnabled() && !this.getBagContainer().isHidden())
    {
      if (this.getBagFieldValue())
      {
        this.administration.setInfusionBag({quantity: this.getBagFieldValue(), unit: "mL"});
      }
    }

    if (this.createNewTask)
    {
      this.administration.setPlannedTime(selectedTimestamp);
      this.administration.setPlannedDose(therapyDose);

      view.getRestApi().createAdministrationTask(
          this.therapy,
          this.administration,
          this.isSupplyRequested(),
          true)
          .then(
              function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              }
          );
    }
    else
    {
      this.administration.setAdministrationTime(selectedTimestamp);
      this.administration.setAdministeredDose(therapyDose);
      if (this.getTherapy().isOrderTypeOxygen())
      {
        if (this.administrationType === enums.administrationTypeEnum.ADJUST_INFUSION)
        {
          this.administration.setAdjustAdministrationSubtype(enums.adjustAdministrationSubtype.OXYGEN);
          this.administration.setAdministrationResult(enums.administrationResultEnum.GIVEN);
          this.administration.setStartingDevice(this._getPresetStartingDevice());
        }
        else if (this.administrationType === enums.administrationTypeEnum.START)
        {
          this.administration.setStartAdministrationSubtype(enums.startAdministrationSubtype.OXYGEN);
          this.administration.setStartingDevice(this._oxygenRouteContainer.getStartingDevice());
        }
      }

      view.getRestApi().confirmAdministrationTask(
          this.therapy,
          this.administration,
          this.editMode,
          this.isSupplyRequested(),
          true)
          .then(
              function onSuccess()
              {
                self.resultCallback(new app.views.common.AppResultData({success: true}));
              },
              function onFailure()
              {
                self.resultCallback(new app.views.common.AppResultData({success: false}));
              }
          );
    }
  },

  /**
   *
   * @param {String} message
   * @param {number | null} width
   * @param {number | null} height
   * @private
   */
  _openConfirmationWarningDialog: function(message, width, height)
  {
    var self = this;
    var utils = app.views.medications.MedicationUtils;
    utils.openConfirmationWithWarningDialog(this.getView(),
        message,
        width,
        height).then(function(confirm)
        {
          if (confirm)
          {
            self._confirmTherapyAdministration();
          }
          else if (self.resultCallback)
          {
            var resultData = new app.views.common.AppResultData({success: false});
            self.resultCallback(resultData);
          }
        },
        function()
        {
          if (self.resultCallback)
          {
            var resultData = new app.views.common.AppResultData({success: false});
            self.resultCallback(resultData);
          }
        });
  },

  /**
   * @returns {boolean}
   * @Private
   */
  _assertInsufficientQuantityForBolus: function()
  {
    var view = this.getView();
    var dose = this._administrationDoseContainer.getDose();

    if (dose.quantityDenominator)
    {
      if (this.getCurrentBagQuantity() < dose.quantityDenominator)
      {
        return true;
      }
    }
    else if (dose.quantity &&
        view.getUnitsHolder().isUnitInLiquidGroup(dose.quantityUnit))
    {
      if (this.getCurrentBagQuantity() < dose.quantity)
      {
        return true;
      }
    }
    return false;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationData>} medicationDataArray
   * @param {Number} firstMedicationId
   * @private
   */
  _findFirstMedicationAndValidateData: function(medicationDataArray, firstMedicationId)
  {
    this._medicationDataForAllIngredients = medicationDataArray;
    this.applyMedicationInfoButtonTooltip(medicationDataArray);

    var firstMedicationData = medicationDataArray.find(
        function isMedicationDataWithMatchingMedicationId(medicationData)
        {
          return !!medicationData.getMedication() && medicationData.getMedication().getId() === firstMedicationId;
        });

    if (!tm.jquery.Utils.isEmpty(firstMedicationData))
    {
      this._setPrescribedMedicationData(firstMedicationData);
      this._validateAndPresentData(firstMedicationData, false);
    }
    else
    {
      this._createAndPresentUniversalMedicationData();
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _setPrescribedMedicationData: function(medicationData)
  {
    this.prescribedMedicationData = medicationData;
    this._administrationDoseContainer.setPrescribedMedicationData(medicationData);

  },

  _loadMedicationData: function (medicationId, preventEvent)
  {
    var self = this;
    if (medicationId)
    {
      this.medicationId = medicationId;
      this.getView().getRestApi().loadMedicationData(medicationId).then(function onDataLoad(medicationData)
      {
        self.applyMedicationInfoButtonTooltip(medicationData);
        self._validateAndPresentData(medicationData, true, preventEvent);
      });
    }
    else //uncoded medications (universal forms)
    {
      this._createAndPresentUniversalMedicationData();
    }
  },

  _applyTitrationDataIconVisibility: function()
  {
    if (this.prescribedMedicationData &&
        !tm.jquery.Utils.isEmpty(this.prescribedMedicationData.getTitration()))
    {
      this.isRendered() ? this._titrationDataIcon.show() : this._titrationDataIcon.setHidden(false);
    }
  },

  _createAndPresentUniversalMedicationData: function()
  {
    var medicationData = null;
    if (!tm.jquery.Utils.isEmpty(this.therapy))
    {
      if (this.therapy.isOrderTypeComplex())
      {
        medicationData =
            app.views.medications.MedicationUtils.getMedicationDataFromComplexTherapy(this.getView(), this.therapy);
      }
      else
      {
        medicationData = app.views.medications.MedicationUtils.getMedicationDataFromSimpleTherapy(this.therapy);
      }
    }
    this._validateAndPresentData(medicationData, false);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Boolean} setMedicationField
   * @param {Boolean} [preventMedicationFieldEvent=false]
   * @private
   */
  _validateAndPresentData: function(medicationData, setMedicationField, preventMedicationFieldEvent)
  {
    var self = this;
    this._presentMedicationData(medicationData, setMedicationField, preventMedicationFieldEvent);
    if (!!this.prescribedMedicationData && !medicationData.hasMatchingNumeratorUnit(this.prescribedMedicationData))
    {
      setTimeout(function ensureCorrectDialogStack()
      {
        self._warnNumeratorUnitsNotMatched(medicationData);
      }, 0);
    }
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>} medicationDataRoutes
   * @param {app.views.medications.common.dto.MedicationRoute} preselectedRoute
   * @private
   */
  _setRoutes: function(medicationDataRoutes, preselectedRoute)
  {
    var filteredRoutes = medicationDataRoutes.filter(function(route)
    {
      return this.getTherapy().getRoutes().some(function(therapyRoute)
      {
        return therapyRoute.getId() === route.getId();
      });
    }, this);

    if (filteredRoutes.length > 1)
    {
      this._routesPane.setRoutes(filteredRoutes, preselectedRoute);
    }
    this._applyRoutesContainerVisibility(filteredRoutes.length > 1);
  },

  /**
   * @param {boolean} visible
   * @private
   */
  _applyRoutesContainerVisibility: function(visible)
  {
    if (!!visible)
    {
      this._routesContainer.isRendered() ? this._routesContainer.show() : this._routesContainer.setHidden(false)
    }
    else
    {
      this._routesContainer.isRendered() ? this._routesContainer.hide() : this._routesContainer.setHidden(true)
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {Boolean} setMedicationField
   * @param {Boolean} [preventMedicationFieldEvent=false]
   * @private
   */
  _presentMedicationData: function(medicationData, setMedicationField, preventMedicationFieldEvent)
  {
    var enums = app.views.medications.TherapyEnums;

    if (!this.createNewTask && medicationData.getRoutes())
    {
      this._setRoutes(medicationData.getRoutes(),
          this.administration && this.administration.getRoute() ?
              this.administration.getRoute() :
              null);
    }

    if (this.getView().isAdministrationWitnessingEnabled() && !this.createNewTask)
    {
      this._showWitnessContainer();
      this._configureWitnessRequirement();
    }

    if (!tm.jquery.Utils.isEmpty(this.currentAdministration))
    {
      this.administration = this.currentAdministration;
    }
    this.setMedicationData(medicationData);
    this.setMedicationField = setMedicationField;
    var therapyDoseTypeEnum = this.administration && this.administration.getDoseType() || this.therapyDoseTypeEnum;
    if (!therapyDoseTypeEnum && this._isDoseTypeDoseRange())
    {
      therapyDoseTypeEnum = enums.therapyDoseTypeEnum.QUANTITY;
    }
    this.therapyDoseTypeEnum = therapyDoseTypeEnum;

    if (medicationData && setMedicationField && this.medicationField)
    {
      this.medicationField.setSelection(medicationData.medication, null, preventMedicationFieldEvent);
    }

    if (!therapyDoseTypeEnum || this.stopFlow)
    {
      this._hideAllDoseRateFields();
    }
    else
    {
      this._administrationDoseContainer.show();
      this._administrationDoseContainer.setMedicationData(medicationData, therapyDoseTypeEnum);
      if (this.getTherapy().isOrderTypeOxygen() && this.administrationType === enums.administrationTypeEnum.START)
      {
        this._showOxygenRouteRowContainer();
        var startingDevice = this.administration && this.administration.getStartingDevice() ?
            this.administration.getStartingDevice() :
            this._getPresetStartingDevice();
        this._oxygenRouteContainer.setStartingDevice(startingDevice);
      }
    }

    this.currentAdministration = this.administration;
    if (this.administration)
    {
      if ((this.administrationResultEnum === enums.administrationResultEnum.DEFER ||
              this.administrationResultEnum === enums.administrationResultEnum.NOT_GIVEN))
      {
        this._applyRoutesContainerVisibility(false);
        this._hideAllDoseRateFields();
        this._hideWitnessContainer();
      }
    }

    if (this.isRendered())
    {
      this._applyAdministrationAmountFieldFocus(preventMedicationFieldEvent);
    }
  },

  /**
   * Checks if witness container is available, and if any requirements for witnessing are met. Sets witness as mandatory
   * accordingly. Relies on correctly set {@link #administrationType}, {@link #administrationResultEnum} and administration
   * route, so these properties should be set or changed before calling this configuration.
   * @private
   */
  _configureWitnessRequirement: function()
  {
    if (this.isWitnessingContainerAvailable())
    {
      var enums = app.views.medications.TherapyEnums;
      var isAdministrationWithOptionalWitness =
          this.getAdministrationType() === enums.administrationTypeEnum.STOP ||
          this.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION ||
          this._isAdministrationTypeBolus();

      var isAdministrationResultWithOptionalWitness =
          this.administrationResultEnum === enums.administrationResultEnum.SELF_ADMINISTERED;

      if (isAdministrationWithOptionalWitness || isAdministrationResultWithOptionalWitness)
      {
        this.getWitnessContainer().setMandatory(false);
        return;
      }

      this.getWitnessContainer().setMandatory(this.isWitnessingRequired() || this._isWitnessingRequiredByRoute());
    }
  },

  _applyAdministrationAmountFieldFocus: function(preventMedicationFieldEvent)
  {
    var self = this;
    setTimeout(function()
    {
      if (self.getView().getTherapyAuthority().isMedicationIdentifierScanningAllowed() &&
          self._medicationBarcodeContainer &&
          !preventMedicationFieldEvent &&
          !self.barcode)
      {
        self._medicationBarcodeContainer.requestFocusToBarcodeField();
      }
      else
      {
        self._administrationDoseContainer.applyAdministrationDoseFieldsFocus();
      }
    }, 0);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _handleMedicationIdentifierStatus: function(medicationData)
  {
    if (this.getView().getTherapyAuthority().isMedicationIdentifierScanningAllowed()
        && this._medicationBarcodeContainer && this._medicationBarcodeContainer.getBarcode())
    {
      if (this.prescribedMedicationData.getDefiningIngredient().strengthNumerator !==
          medicationData.getDefiningIngredient().strengthNumerator ||
          this.prescribedMedicationData.getDefiningIngredient().strengthDenominator !==
          medicationData.getDefiningIngredient().strengthDenominator)
      {
        this._medicationBarcodeContainer.hideBarcodeStatusIcons();
        this._medicationBarcodeContainer.showBarcodeDifferentMassImg();
      }
    }
  },

  /**
   * Shows the witness container, if present.
   * @private
   */
  _showWitnessContainer: function()
  {
    if (!this.getWitnessContainer())
    {
      return;
    }
    this.getWitnessContainer().show();
  },

  /**
   * Hides the witnessing container, if present.
   * @private
   */
  _hideWitnessContainer: function()
  {
    if (!this.getWitnessContainer())
    {
      return;
    }
    this.getWitnessContainer().hide();
  },

  _hideAllDoseRateFields: function()
  {
    this._administrationDoseContainer.hide();

    if (this.isInfusionBagEnabled())
    {
      this.getBagContainer().hide();
    }
    if (!tm.jquery.Utils.isEmpty(this.medicationContainer))
    {
      this.medicationContainer.hide();
    }

  },

  _hideOxygenRouteRowContainer: function()
  {
    this._oxygenRouteRowContainer.hide();
  },

  _showOxygenRouteRowContainer: function()
  {
    this._oxygenRouteRowContainer.show();
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   * @private
   */
  _getPresetStartingDevice: function()
  {
    return !!this.currentStartingDevice ?
        this.currentStartingDevice :
        this.getTherapy().getStartingDevice();
  },

  /**
   * @return {boolean}
   * @private
   */
  _isAdministrationTypeBolus: function()
  {
    return this.getTherapy().isRateTypeBolus() ||
        this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
  },

  /**
   * @Override
   */
  setupValidation: function()
  {
    this.callSuper();

    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    if (!this._routesContainer.isHidden() && this.therapy.getRoutes().length > 1 &&
        this.administrationResultMenu.getActiveIndex() === 0) // active index 0 -> given
    {
      this._addValidations(this._routesPane.getRoutesPaneValidations());
    }
    if (this.medicationField)
    {
      this.getValidationForm().addFormField(new tm.jquery.FormField({
        component: self.medicationField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self.medicationField.getSelection();
        }
      }));
    }
    if (!this._administrationDoseContainer.isHidden())
    {
      this._addValidations(this._administrationDoseContainer.getValidations());
    }
    if (this.administrationResultEnum === enums.administrationResultEnum.SELF_ADMINISTERED)
    {
      this.getValidationForm().addFormField(new tm.jquery.FormField({
        component: self.administrationResultMenu,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.selfAdminButtonGroup.getSelection();
          if (tm.jquery.Utils.isEmpty(value) || value.length <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
    else if (this.administrationResultEnum === enums.administrationResultEnum.NOT_GIVEN)
    {
      this.getValidationForm().addFormField(new tm.jquery.FormField({
        component: self.administrationResultMenu,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.notGivenButtonGroup.getSelection();
          if (tm.jquery.Utils.isEmpty(value) || value.length <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }

    /*Require comment if administration is deferred or not given with clinical reason*/
    if (this.administrationResultEnum === enums.administrationResultEnum.DEFER ||
        (this.administrationResultEnum === enums.administrationResultEnum.NOT_GIVEN &&
            this.notGivenButtonGroup.getSelection().length > 0 &&
            this.notGivenButtonGroup.getSelection()[0].id === 'clinical-reason'))
    {
      this.getValidationForm().addFormField(new tm.jquery.FormField({
        component: self.getCommentField(),
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self.getAdministrationComment();
          if (tm.jquery.Utils.isEmpty(value) || value.length <= 0)
          {
            return null;
          }
          return true;
        }
      }));
    }
    if (!this._oxygenRouteRowContainer.isHidden())
    {
      this.getValidationForm().addFormField(new tm.jquery.FormField({
        component: self._oxygenRouteContainer,
        required: true,
        componentValueImplementationFn: function(component)
        {
          return component.getStartingDevice();
        }
      }));
    }

    if (this.getTherapy().isOrderTypeOxygen())
    {
      var oxygenFlowValidator = new app.views.medications.ordering.oxygen.OxygenFlowRateValidator({
        view: view
      });
      this._addValidations(oxygenFlowValidator.getAsFormFieldValidators(
          this._administrationDoseContainer.getRatePane().getRateField()));
    }
  },


  _addValidations: function (validation)
  {
    for (var i = 0; i < validation.length; i++)
    {
      this.getValidationForm().addFormField(validation[i]);
    }
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
   * @private
   */
  _warnNumeratorUnitsNotMatched: function(medicationData)
  {
    var numeratorUnit = medicationData.getStrengthNumeratorUnit();
    var prescribedNumeratorUnit = this.prescribedMedicationData.getStrengthNumeratorUnit();
    var message = this.getView().getDictionary('units.dont.match') + ': ' + prescribedNumeratorUnit + ', ' + numeratorUnit;
    this.getView().getAppNotifier().warning(message, app.views.common.AppNotifierDisplayType.HTML, 320, 134);
  },

  /**
   * Witnessing for IV therapies is required if enabled by server property and the selected (or only available) route is
   * of type {@link app.views.medications.TherapyEnums.medicationRouteTypeEnum.IV}.
   * @returns {Boolean}
   * @private
   */
  _isWitnessingRequiredByRoute: function()
  {
    return this.getView().isAdministrationWitnessingIvRequired() &&
        !!this._getSelectedRoute() &&
        this._getSelectedRoute().getType() === app.views.medications.TherapyEnums.medicationRouteTypeEnum.IV;

  },

  /**
   * Configures the UI state for administration actions that result in medication being given.
   * @param {String} administrationResult of type {@link app.views.medications.TherapyEnums.administrationResultEnum}
   * @param {tm.jquery.Container} activeAdministrationMenuItem
   * @private
   */
  _applyAdministrationGivenState: function(administrationResult, activeAdministrationMenuItem)
  {
    this.getValidationForm().reset();
    this.setRequestSupplyCheckBox(false);
    this.administrationResultEnum = administrationResult;
    if (!tm.jquery.Utils.isEmpty(this.medicationContainer))
    {
      this.medicationContainer.show();
    }
    this._presentMedicationData(this.getMedicationData(), this.setMedicationField);
    this.administrationResultMenu.setActiveItem(activeAdministrationMenuItem);
    this.administrationNotAdministeredReasonEnum = null;
    this.setAdministrationDocumentedTimeLabel(true);
  },

  /**
   * Configures te UI state for administration actions that result in medication not being given.
   * @param {String} administrationResult of type {@link app.views.medications.TherapyEnums.administrationResultEnum}
   * @param {tm.jquery.Container} activeAdministrationMenuItem
   * @private
   */
  _applyAdministrationSkippedState: function(administrationResult, activeAdministrationMenuItem)
  {
    this.getValidationForm().reset();
    this.setRequestSupplyCheckBox(false);
    this.administrationResultEnum = administrationResult;
    this._applyRoutesContainerVisibility(false);
    this._hideAllDoseRateFields();
    this._hideOxygenRouteRowContainer();
    this._hideWitnessContainer();
    this.administrationResultMenu.setActiveItem(activeAdministrationMenuItem);
    this.administrationNotAdministeredReasonEnum = null;
    this.setAdministrationDocumentedTimeLabel(false);
  },

  /** public methods */
  getInfusionIngredient: function()
  {
    var dose = this._administrationDoseContainer.getDose();
    if (this.getMedicationData())
    {
      return {
        medication: this.getMedicationData().getMedication(),
        quantity: dose.quantity,
        quantityUnit: this.getMedicationData().getStrengthNumeratorUnit(),
        quantityDenominator: dose.quantityDenominator,
        quantityDenominatorUnit: 'ml',
        doseForm: this.getMedicationData().getDoseForm()
      }
    }
    return null;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} value
   */
  setMedicationData: function(value)
  {
    this.medicationData = value;
  },

  /**
   * Perform checks that do not prevent administration, but open warning dialog.
   * @Override
   */
  onValidationSuccess: function()
  {
    var view = this.getView();
    var additionalAdministrationWarnings = [];

    if (this._medicationBarcodeContainer && !this._medicationBarcodeContainer.isScannedCodeCorrect())
    {
      if (!this._medicationBarcodeContainer.isScannedCodeCorrect())
      {
        additionalAdministrationWarnings.push(
            view.getDictionary("therapy.administration.scanned.medication.different.warning"));
      }
    }
    if (this.therapy.getWhenNeeded())
    {
      if (this.getPlannedDoseTimeValidator().isPrnTimeTooSoon(
              this._getSelectedTimestamp().getTime(),
              this.therapy.getDosingFrequency()))
      {
        additionalAdministrationWarnings.push(view.getDictionary("therapy.administration.prn.warning"));
      }
    }
    else if (this.therapy.isContinuousInfusion() || this.therapy.isOrderTypeOxygen())
    {
      if (this.getPlannedDoseTimeValidator()
              .isRateAdministrationChangesTooClose(this._getSelectedTimestamp(), this.administration))
      {
        additionalAdministrationWarnings.push(view.getDictionary("therapy.administration.near.other.warning"));
      }
      if (view.isInfusionBagEnabled() && this._assertInsufficientQuantityForBolus())
      {
        var bagQuantity =
            this.getCurrentBagQuantity() > 0 ? this.getCurrentBagQuantity() + " mL" : " " + view.getDictionary("bag.empty");

        additionalAdministrationWarnings.push(
            tm.jquery.Utils.formatMessage(view.getDictionary("not.enough.for.bolus"), bagQuantity));
      }
    }
    if (!this._administrationDoseContainer.isDoseInRange())
    {
      additionalAdministrationWarnings.push(view.getDictionary('dose.is.out.of.range'));
    }
    if (additionalAdministrationWarnings.length > 0)
    {
      var confirmationDialogHeight = 80 + (additionalAdministrationWarnings.length * 80);
      this._openConfirmationWarningDialog(
          additionalAdministrationWarnings.join('<br><br>'),
          300,
          confirmationDialogHeight);
    }
    else
    {
      this._confirmTherapyAdministration();
    }
  }
});