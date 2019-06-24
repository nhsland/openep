Class.define('app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'therapy-administration-container',
  scrollable: 'vertical',
  /** configs */
  view: null,
  dialog: null,
  therapy: null,
  administrations: null,
  administration: null,
  patientId: null,
  createNewTask: false,
  administrationType: null,
  editMode: null,
  therapyReviewedUntil: null,
  editableIfNotInFuture: null,
  latestTherapyVersion: null,
  infusionActive: true,
  witnessingRequired: false,

  /** components */
  _bagContainer: null,
  _requestSupplyContainer: null,
  _commentContainer: null,
  _commentField: null,
  _administrationTimeContainer: null,
  _witnessContainer: null,
  _administrationDateField: null,
  _administrationTimeField: null,
  _bagField: null,
  _resetButton: null,
  _warningContainer: null,
  _requestSupplyCheckBox: null,
  _validationForm: null,

  /** privates */
  _administrationWarningsProvider: null,
  _currentBagQuantity: null,
  _plannedDoseTimeValidator: null,
  _ingredientRuleRequestTimeout: undefined,
  _medicationRuleUtils: null,
  _renderConditionTask: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch", 0));
    this._medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({
          view: view,
          referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: view})
        }));
    this._buildCommonComponents();
    this._setAdministrationTime();
    this._setAdministrationComment();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._assertAdministrationWarnings();
            self._applyAdministrationAmountFieldFocus();
          },
          function()
          {
            return self.isRendered(true) &&
                !tm.jquery.Utils.isEmpty(self.getAdministrationDateField().getPlugin()) &&
                !tm.jquery.Utils.isEmpty(self.getAdministrationTimeField().getPlugin());
          },
          50, 100
      );
    });
  },

  /**
   * Builder for common components. Components must be added to GUI depending on usage.
   * @private
   */
  _buildCommonComponents: function()
  {
    this._buildResetButton();
    this._buildAdministrationTimeContainer();
    this._buildPlannedDoseTimeValidator();
    this._buildWarningsProvider();
    this._buildWarningsContainer();
    if (this.getView().isAdministrationWitnessingEnabled())
    {
      this._buildWitnessContainer();
    }
    if (this.isInfusionBagEnabled())
    {
      this._buildBagContainer();
    }
    if (this.isRequestSupplyEnabled())
    {
      this._buildRequestSupplyContainer();
    }
    this._buildCommentContainer();
    this._buildValidationForm();
  },

  /**
   * @private
   */
  _buildAdministrationTimeContainer: function()
  {
    var view = this.getView();
    var self = this;

    this._administrationDateField = new tm.jquery.DatePicker({
      showType: "focus",
      width: 100,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this._administrationTimeField = new tm.jquery.TimePicker({
      showType: "focus",
      width: 50,
      nowButton: {
        text: view.getDictionary("asap")
      },
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this._administrationDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.getAdministrationDateField().setDate(CurrentTime.get(), true);
          self.getAdministrationTimeField().setTime(CurrentTime.get());
        });
    this._administrationTimeField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.getAdministrationDateField().setDate(CurrentTime.get(), true);
          self.getAdministrationTimeField().setTime(CurrentTime.get());
        });

    this._administrationDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self._getSelectedTimestamp())
      {
        self._assertAdministrationWarnings();
      }
    });

    this._administrationTimeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self._getSelectedTimestamp())
      {
        self._assertAdministrationWarnings();
      }
    });
    this._administrationTimeContainer = new tm.jquery.Container({
      cls: 'administration-time-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 48
    });
    this._administrationTimeLabel = new tm.jquery.Container({
      cls: 'TextLabel administration-time-label',
      html: this.getView().getDictionary('administration.time'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this._administrationTimeContainer.add(this._administrationTimeLabel);
    this._administrationTimeContainer.add(this._administrationDateField);
    this._administrationTimeContainer.add(this._administrationTimeField);
  },

  /**
   * @private
   */
  _buildAdditionalInformationRow: function()
  {
    var self = this;

    var additionalInformationRow = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });

    this._medicationInfoButton = new tm.jquery.Container({
      cls: 'medication-info-button',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      width: 25,
      height: 30,
      hidden: true
    });
    additionalInformationRow.add(this._medicationInfoButton);

    this._titrationDataIcon = new tm.jquery.Container({
      cls: "show-titration-data-icon",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      width: 24,
      height: 24,
      hidden: true
    });
    this._titrationDataIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      self._showTitrationData();
    });

    additionalInformationRow.add(this._titrationDataIcon);

    return additionalInformationRow;
  },

  /**
   * @private
   */
  buildTherapyDescriptionAndInfoContainer: function()
  {
    var view = this.getView();
    var administration = this.getAdministration();
    var therapy = this.getTherapy();
    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      cls: 'TherapyDescription'
    });
    therapyDescriptionContainer.add(new tm.jquery.Component({
      html: therapy.getFormattedTherapyDisplay()
    }));
    if (administration && !tm.jquery.Utils.isEmpty(administration.plannedTime))
    {
      therapyDescriptionContainer.add(new tm.jquery.Container({
        html: app.views.medications.MedicationTimingUtils.getFormattedAdministrationPlannedTime(view, administration)
      }));
    }
    var maxDosePercentageInfo =
        app.views.medications.MedicationUtils.createMaxDosePercentageInfoHtml(view, therapy.getMaxDosePercentage());
    if (!tm.jquery.Utils.isEmpty(maxDosePercentageInfo))
    {
      therapyDescriptionContainer.add(new tm.jquery.Component({
        html: maxDosePercentageInfo
      }));
    }
    therapyDescriptionContainer.add(this._buildAndSetHighRiskIconsContainer());

    var therapyDescriptionAndInfoContainer = new tm.jquery.Container({
      cls: "therapy-description-and-info borderless",
      layout: tm.jquery.HFlexboxLayout.create("center", "flex-start")
    });

    therapyDescriptionAndInfoContainer.add(therapyDescriptionContainer);
    therapyDescriptionAndInfoContainer.add(this._buildAdditionalInformationRow());
    return therapyDescriptionAndInfoContainer;
  },

  /**
   * @returns {app.views.medications.ordering.HighRiskMedicationIconsContainer}
   * @private
   */
  _buildAndSetHighRiskIconsContainer: function()
  {
    var highAlertIconsContainer = new app.views.medications.ordering.HighRiskMedicationIconsContainer({
      view: this.getView(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    highAlertIconsContainer.presentHighAlertIconsForMultipleMedicationData(this.getMedicationData());
    return highAlertIconsContainer;
  },

  /**
   * @private
   */
  _buildBagContainer: function()
  {
    var view = this.getView();
    this._bagContainer = new tm.jquery.Container({
      cls: 'bag-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 48,
      hidden: true
    });

    var bagLabel = new tm.jquery.Container({
      cls: 'TextLabel volume-label',
      html: view.getDictionary('bag.syringe.volume'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._bagField = app.views.medications.MedicationUtils.createNumberField('n2', 68);
    this._bagField.setFlex(tm.jquery.flexbox.item.Flex.create(1, 1, "auto"));

    var bagMlLabel = new tm.jquery.Container({
      cls: 'TextData ml-label',
      html: 'mL',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._bagContainer.add(bagLabel);
    this._bagContainer.add(this._bagField);
    this._bagContainer.add(bagMlLabel);
  },

  /**
   * @private
   */
  _buildWitnessContainer: function()
  {
    this._witnessContainer = new app.views.medications.timeline.administration.WitnessContainer({
      view: this.getView(),
      height: 48,
      careProfessionals: this.careProfessionals,
      mandatory: this.isWitnessingRequired(),
      hidden: true
    });
  },

  /**
   * @private
   */
  _assertAdministrationWarnings: function()
  {
    this._assertAdministrationAllowed();
    this._loadInfusionBagQuantity();
    this._handleMedicationIngredientRule(
        app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
  },

  /**
   * Check if administration is allowed and set warnings. If the date or time are missing, do nothing. If we are
   * already displaying warnings from the previous assertion, the user should correct them anyway, otherwise the
   * form validation will prompt him to enter the required missing data.
   * @private
   */
  _assertAdministrationAllowed: function()
  {
    var selectedTimestamp = this._getSelectedTimestamp();

    if (selectedTimestamp)
    {
      var warnings = this._administrationWarningsProvider.getRestrictiveAdministrationWarnings(
          selectedTimestamp,
          !this.createNewTask,
          !this.createNewTask,
          !this.createNewTask,
          true
      );

      this.getWarningContainer().setRestrictiveWarnings(warnings);
      this._setAdministrationWarnings(warnings);
      this._handleConfirmButtonEnabled();
    }
  },

  /**
   * @param warnings
   * @private
   */
  _setAdministrationWarnings: function(warnings)
  {
    this._administrationWarnings = warnings;
  },

  /**
   * @returns {Date|undefined}
   * @private
   */
  _getSelectedTimestamp: function()
  {
    var administrationDate = this.getAdministrationDateField().getDate();
    var administrationTime = this.getAdministrationTimeField().getTime();
    return tm.jquery.Utils.isDate(administrationDate) && tm.jquery.Utils.isDate(administrationTime) ?
        new Date(
            administrationDate.getFullYear(),
            administrationDate.getMonth(),
            administrationDate.getDate(),
            administrationTime.getHours(),
            administrationTime.getMinutes(),
            0, 0) :
        undefined;
  },

  /**
   * @private
   */
  _loadInfusionBagQuantity: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var therapy = this.getTherapy();

    if (therapy.isContinuousInfusion() && this.administrationType === enums.administrationTypeEnum.BOLUS)
    {
      this.getDialog().getConfirmButton().setEnabled(false);
      this.getWarningContainer().showLoadingIcon(true);
      var selectedTimestamp = this._getSelectedTimestamp();

      view.getRestApi().loadInfusionBagQuantity(
          selectedTimestamp,
          therapy.getTherapyId(),
          true).then(
          function onSuccess(data)
          {
            if (self.isRendered())
            {
              self._currentBagQuantity = data;
              self.getWarningContainer().showLoadingIcon(false);
              self._handleConfirmButtonEnabled();
            }
          }
      );
    }
  },

  /**
   * @private
   */
  _buildWarningsProvider: function()
  {
    this._administrationWarningsProvider = new app.views.medications.timeline.administration.AdministrationWarningsProvider({
      view: this.getView(),
      plannedDoseTimeValidator: this.getPlannedDoseTimeValidator(),
      administration: this.administration,
      administrations: this.administrations,
      administrationType: this.administrationType,
      therapy: this.getTherapy(),
      infusionActive: this.infusionActive,
      therapyReviewedUntil: this.therapyReviewedUntil
    });
  },

  /**
   * @private
   */
  _buildWarningsContainer: function()
  {
    this._warningContainer = new app.views.medications.timeline.administration.WarningContainer({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
  },

  /**
   * @private
   */
  _buildPlannedDoseTimeValidator: function()
  {
    this._plannedDoseTimeValidator = new app.views.medications.timeline.administration.PlannedDoseTimeValidator({
      administrations: this.getAdministrations()
    });
  },

  /**
   * @private
   */
  _createYesLabel: function()
  {
    return new tm.jquery.Container({
      cls: 'YesTextLabel',
      html: this.getView().getDictionary("yes"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
  },

  /**
   * @private
   */
  _buildRequestSupplyContainer: function()
  {
    this._requestSupplyContainer = new tm.jquery.Container({
      cls: 'request-supply-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      height: 48
    });

    this._requestSupplyCheckBox = new tm.jquery.CheckBox({enabled: true, nowrap: true});
    var requestSupplyLabel = new tm.jquery.Container({
      cls: 'TextLabel request-supply-label',
      html: this.getView().getDictionary("nurse.resupply.request.button"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this._requestSupplyContainer.add(requestSupplyLabel);
    this._requestSupplyContainer.add(this._requestSupplyCheckBox);
    this._requestSupplyContainer.add(this._createYesLabel());
  },

  /**
   * @private
   */
  _buildCommentContainer: function()
  {
    var view = this.getView();
    var commentLabelContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 5),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")
    });
    this._commentContainer = new tm.jquery.Container({
      cls: 'comment-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });

    this._commentField = new tm.jquery.TextArea({
      width: 438,
      cls: 'comment-field',
      rows: 3,
      placeholder: view.getDictionary('commentary') + "..."
    });
    commentLabelContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel', view.getDictionary('commentary')));
    this._commentContainer.add(commentLabelContainer);
    this._commentContainer.add(this._commentField);
  },

  /**
   * @private
   */
  _buildValidationForm: function()
  {
    var self = this;
    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self.onValidationSuccess();
      },
      onValidationError: function()
      {
        self.resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });
  },

  /**
   * @private
   */
  _buildResetButton: function()
  {
    var view = this.getView();
    var self = this;
    this._resetButton = new tm.jquery.Button({
      cls: "btn-reset",
      type: "link",
      text: view.getDictionary('revert'),
      handler: function()
      {
        self._setAdministrationTime();
        self._presentMedicationData(self.getMedicationData(), self.setMedicationField);
        self._assertAdministrationWarnings();
      }
    });
  },

  /**
   * Override if needed. Use to set focus to desired field.
   * @private
   */
  _applyAdministrationAmountFieldFocus: function()
  {

  },

  /**
   * @private
   */
  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
  },

  /**
   * @private
   */
  _setAdministrationTime: function()
  {

    var administration = this.getAdministration();
    var now = CurrentTime.get();
    var administrationTime = now;

    if (!!administration)
    {
      if (this.isEditMode())
      {
        administrationTime = administration.getAdministrationTime()
      }
      else if (!this.getView().isPresetPastAdministrationTimeToNow())
      {
        var plannedTime = new Date(administration.getPlannedTime());
        administrationTime = plannedTime < now ? plannedTime : now;
      }
    }
    this.getAdministrationDateField().setDate(administrationTime, true);
    this.getAdministrationTimeField().setTime(administrationTime, true);
  },

  /**
   * @param {Object|null} medicationIngredientRule
   * @param {app.views.medications.common.dto.MedicationData} [medicationData = null]
   * @private
   */
  _handleMedicationIngredientRule: function(medicationIngredientRule, medicationData)
  {
    var self = this;
    var view = this.getView();
    var type = this.administrationType;

    var showRuleWarning = type === app.views.medications.TherapyEnums.administrationTypeEnum.START
        || type === app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION
        || !type;

    if (showRuleWarning)
    {
      this.getDialog().getConfirmButton().setEnabled(false);
      clearTimeout(self._ingredientRuleRequestTimeout);
      this._ingredientRuleRequestTimeout = setTimeout(function()
      {
        if (self.isRendered())
        {
          var paracetamolDailyDoseRule = app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE;

          var medicationWithIngredient = self.getMedicationRuleUtils().extractMedicationWithMedicationRule(
              self._medicationDataForAllIngredients,
              paracetamolDailyDoseRule);

          medicationData = !tm.jquery.Utils.isEmpty(medicationWithIngredient)
              ? medicationWithIngredient
              : tm.jquery.Utils.isEmpty(medicationData) ? self.getMedicationData() : medicationData;

          if (!tm.jquery.Utils.isEmpty(medicationData)
              && medicationIngredientRule === paracetamolDailyDoseRule
              && self.getMedicationRuleUtils().isMedicationRuleSet(medicationData, paracetamolDailyDoseRule))
          {
            self.getWarningContainer().showLoadingIcon(true);

            var therapyDose = self._administrationDoseContainer.buildTherapyDose();
            var therapyDto = self.therapy;

            var administrationId = self.administration && self.administration.getAdministrationId() ?
                self.administration.getAdministrationId() :
                null;

            var taskId = self.administration && !tm.jquery.Utils.isEmpty(self.administration.getTaskId()) ?
                self.administration.getTaskId() :
                null;

            self.getMedicationRuleUtils().getParacetamolRuleForAdministration(
                medicationData,
                therapyDto,
                self.getAdministrationDateField().getDate(),
                self.getAdministrationTimeField().getTime(),
                view.getPatientId(),
                therapyDose,
                administrationId,
                taskId).then(
                function validationSuccessHandler(medicationRuleResult)
                {
                  if (self.isRendered())
                  {
                    self.getWarningContainer().showLoadingIcon(false);
                    var ruleWarning =
                        self._administrationWarningsProvider.getMedicationIngredientRuleHtml(medicationRuleResult);
                    self.getWarningContainer().handleIngredientWarning(ruleWarning);
                    self._handleConfirmButtonEnabled();
                  }
                });
          }
          else
          {
            self._handleConfirmButtonEnabled();
          }
        }
      }, 150);
    }
  },

  /**
   * @private
   */
  _handleConfirmButtonEnabled: function()
  {
    this.getDialog().getConfirmButton().setEnabled(
        this.getAdministrationWarnings() ?
            !this.getAdministrationWarnings().hasRestrictiveWarnings() :
            true);
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   * @returns {app.views.common.tooltip.AppPopoverTooltip}
   * @private
   */
  _createMedicationDetailsTooltip: function(medicationData)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var medicationDetailsContainer = new app.views.medications.common.MedicationDetailsContainer({
      view: view,
      medicationData: medicationData
    });

    return appFactory.createDefaultPopoverTooltip(
        view.getDictionary("medication"),
        null,
        medicationDetailsContainer
    );
  },

  /**
   * @private
   */
  _setAdministrationComment: function()
  {
    if (!!this.getAdministration() && this.getAdministration().getComment())
    {
      this._commentField.setValue(this.getAdministration().getComment());
    }
  },

  /** Public */

  /**
   * When the administration is either deferred or not given, there is no administration time, but only documented time.
   * Sets administration time and date field label to reflect that.
   * @param {boolean} administrationIsBeingAdministered
   */
  setAdministrationDocumentedTimeLabel: function(administrationIsBeingAdministered)
  {
    this._administrationTimeLabel.setHtml(administrationIsBeingAdministered ?
        this.view.getDictionary('administration.time') :
        this.view.getDictionary('time.documented'));
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   */
  applyMedicationInfoButtonTooltip: function(medicationData)
  {
    medicationData = tm.jquery.Utils.isArray(medicationData) ? medicationData : [medicationData];

    this._medicationInfoButton.setTooltip(this._createMedicationDetailsTooltip(medicationData));
    this.isRendered() ? this._medicationInfoButton.show() : this._medicationInfoButton.setHidden(false);
  },
  /**
   * @returns {boolean}
   */
  isInfusionBagEnabled: function()
  {
    return this.getView().isInfusionBagEnabled() === true;
  },

  /**
   * @returns {boolean}
   */
  isRequestSupplyEnabled: function()
  {
    var view = this.getView();
    return view.getMedicationsSupplyPresent() === true && view.getTherapyAuthority().isCreateResupplyRequestAllowed();
  },

  /**
   * @returns {Number|null}
   */
  getCurrentBagQuantity: function()
  {
    return this._currentBagQuantity;
  },

  /**
   * @returns {Number|null}
   */
  getBagFieldValue: function()
  {
    return this.isInfusionBagEnabled() ? this._bagField.getValue() : null;
  },

  /**
   * @param {Number} value
   */
  setBagFieldValue: function(value)
  {
    if (this.isInfusionBagEnabled())
    {
      this._bagField.setValue(value);
    }
  },

  /**
   * @returns {null|tm.jquery.Container}
   */
  getBagContainer: function()
  {
    return this._bagContainer;
  },

  /**
   * @returns {String|null}
   */
  getAdministrationComment: function()
  {
    return this._commentField.getValue() ? this._commentField.getValue() : null;
  },

  getCommentField: function()
  {
    return this._commentField;
  },

  /**
   * @returns {app.views.medications.timeline.administration.AdministrationWarnings|null}
   */
  getAdministrationWarnings: function()
  {
    return this._administrationWarnings;
  },

  /**
   * @param {tm.jquery.Dialog} dialog
   */
  setDialog: function(dialog)
  {
    this.dialog = dialog;
  },

  /**
   * @returns {tm.jquery.Button}
   */
  getResetButton: function()
  {
    return this._resetButton;
  },

  /**
   * @returns {app.views.medications.timeline.administration.WarningContainer}
   */
  getWarningContainer: function()
  {
    return this._warningContainer;
  },

  /**
   * @returns {app.views.medications.MedicationRuleUtils}
   */
  getMedicationRuleUtils: function()
  {
    return this._medicationRuleUtils;
  },

  /**
   * @returns {tm.jquery.DatePicker}
   */
  getAdministrationDateField: function()
  {
    return this._administrationDateField;
  },

  /**
   * @returns {tm.jquery.TimePicker}
   */
  getAdministrationTimeField: function()
  {
    return this._administrationTimeField;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  getAdministrationTimeContainer: function()
  {
    return this._administrationTimeContainer;
  },

  /**
   * @returns {app.views.medications.timeline.administration.PlannedDoseTimeValidator}
   */
  getPlannedDoseTimeValidator: function()
  {
    return this._plannedDoseTimeValidator;
  },

  /**
   * @returns {app.views.medications.timeline.administration.WitnessContainer|null}
   */
  getWitnessContainer: function()
  {
    return this._witnessContainer;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  getRequestSupplyContainer: function()
  {
    return this._requestSupplyContainer;
  },

  /**
   * @returns {tm.jquery.Container}
   */
  getCommentContainer: function()
  {
    return this._commentContainer;
  },

  /**
   * @param {Boolean} checked
   */
  setRequestSupplyCheckBox: function(checked)
  {
    if (this.isRequestSupplyEnabled())
    {
      this._requestSupplyCheckBox.setChecked(checked, false);
    }
  },

  /**
   * @returns {Boolean}
   */
  isSupplyRequested: function()
  {
    return this.isRequestSupplyEnabled() ? this._requestSupplyCheckBox.isChecked() : false
  },

  /**
   * Returns the latest therapy version for this administration task, if set. Used to load titration data correctly.
   * @returns {app.views.medications.common.dto.Therapy|null}
   */
  getLatestTherapyVersion: function()
  {
    return this.latestTherapyVersion;
  },

  /**
   * @returns {boolean}
   */
  isEditMode: function()
  {
    return this.editMode === true;
  },

  /**
   * @returns {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {Array<Object>|null}
   */
  getAdministrations: function()
  {
    return this.administrations;
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
   * @returns {app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {boolean} true, if the administration witnessing is mandatory, otherwise false. The value is ignored when
   * witnessing is not enabled by server configuration.
   */
  isWitnessingRequired: function()
  {
    return this.witnessingRequired === true;
  },

  /**
   * @returns {boolean}
   */
  isWitnessingContainerAvailable: function()
  {
    return !!this.getWitnessContainer() && !this.getWitnessContainer().isHidden();
  },
  /**
   * @return {tm.jquery.Form}
   */
  getValidationForm: function()
  {
    return this._validationForm;
  },

  /**
   * @returns {tm.jquery.Dialog}
   */
  getDialog: function()
  {
    return this.dialog;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    this._abortRenderConditionTask();
  },

  /**
   * Override for additional validation rules.
   */
  setupValidation: function()
  {
    this.getValidationForm().reset();
    this.getValidationForm().addFormField(new tm.jquery.FormField({
      component: this.getAdministrationDateField().getField(),
      required: true
    }));
    this.getValidationForm().addFormField(new tm.jquery.FormField({
      component: this.getAdministrationTimeField().getField(),
      required: true
    }));
    if (this.isWitnessingContainerAvailable())
    {
      this.getValidationForm().addFormField(this.getWitnessContainer().getFormValidations())
    }
  },

  /**
   * @param resultDataCallback
   */
  processResultData: function (resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.setupValidation();
    this._validationForm.submit();
  },

  onValidationSuccess: function()
  {
    this.resultCallback(new app.views.common.AppResultData({success: true}));
  },

  /**
   * @param actionCallback
   */
  onActionCallback: function(actionCallback)
  {
    if (this.isRendered())
    {
      if (actionCallback.action === tm.views.medications.TherapyView.VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS)
      {
        if (actionCallback.successful)
        {
          this.getWitnessContainer().setAuthenticatedWitness(actionCallback.actionData);
        }
        else
        {
          this.getWitnessContainer().setAuthenticatedWitness(null);
        }
      }
    }
  }
});