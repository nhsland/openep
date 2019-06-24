Class.define('app.views.medications.timeline.RescheduleTasksContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'reschedule-tasks-container',
  scrollable: 'vertical',
  /** configs */
  view: null,
  administration: null,
  administrations: null,
  therapy: null,
  therapyReviewedUntil: null,
  infusionActive: null,

  /** privates: components */
  _moveAllRadioButtonGroup: null,
  _warningContainer: null,
  _plannedDoseTimeValidator: null,
  _enableDialogConfirmationFunction: null,
  _administrationWarningsProvider: null,
  _administrationDateTimeField: null,
  _validationForm: null,
  _resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._plannedDoseTimeValidator = new app.views.medications.timeline.administration.PlannedDoseTimeValidator({
      administrations: this.getAdministrations()
    });

    this._administrationWarningsProvider = new app.views.medications.timeline.administration.AdministrationWarningsProvider({
      view: this.view,
      plannedDoseTimeValidator: this._plannedDoseTimeValidator,
      administration: this.getAdministration(),
      administrations: this.getAdministrations(),
      administrationType: this.getAdministrationType(),
      therapy: this.getTherapy(),
      infusionActive: this.isInfusionActive(),
      therapyReviewedUntil: this.getTherapyReviewedUntil()
    });

    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));

    this._administrationDateTimeField = new tm.jquery.DateTimePicker({
      date: this.getAdministration() ? new Date(this.getAdministration().plannedTime) : CurrentTime.get(),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });

    this._administrationDateTimeField.getDatePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        this._onAdministrationDateTimeFieldCurrentTimeKeystroke.bind(this));
    this._administrationDateTimeField.getTimePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        this._onAdministrationDateTimeFieldCurrentTimeKeystroke.bind(this));
    this._administrationDateTimeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onRescheduleOptionsChange.bind(this));

    this._moveAllRadioButtonGroup = new tm.jquery.RadioButtonGroup({
      onChange: this._onRescheduleOptionsChange.bind(this)
    });
    this._moveAllRadioButtonGroup.add(new tm.jquery.RadioButton({
      labelText: this.view.getDictionary('single.f'),
      moveSingle: true,
      labelAlign: "right",
      checked: true
    }));
    this._moveAllRadioButtonGroup.add(new tm.jquery.RadioButton({
      labelText: this.view.getDictionary('all.f'),
      moveSingle: false,
      labelAlign: "right"
    }));

    this._warningContainer = new app.views.medications.timeline.administration.WarningContainer({
      view: this.view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: 'vertical'
    });

    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self.onValidationSuccess();
      },
      onValidationError: function()
      {
        self._resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    var mainContainer = new tm.jquery.Container({
      layout: new tm.jquery.VFlexboxLayout(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    if (this.isDosingFrequencyBetweenDoses())
    {
      mainContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel',
          this.view.getDictionary('move'), 0));
      mainContainer.add(this.view.getAppFactory().createHRadioButtonGroupContainer(this._moveAllRadioButtonGroup));
    }

    mainContainer.add(app.views.medications.MedicationUtils.crateLabel('TextLabel',
        this.view.getDictionary('reschedule.to'), 0));
    mainContainer.add(this._administrationDateTimeField);

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function yieldToPaint()
      {
        self._administrationDateTimeField.getDatePicker().getInputElement().focus();
      }, 0);
    });

    this.add(mainContainer);
    this.add(this._warningContainer);
  },

  /**
   * onKey event handler for the date and time picker.
   * @private
   */
  _onAdministrationDateTimeFieldCurrentTimeKeystroke: function()
  {
    this._administrationDateTimeField.setDate(CurrentTime.get());
  },

  /**
   * Common {@link tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE} handler for input change which should trigger checking
   * for any restrictive administration warnings (such as jump warnings). When moving all administrations tasks from
   * a selected point, we disable future administration jump warnings.
   * @private
   */
  _onRescheduleOptionsChange: function()
  {
    if (this.isDosingFrequencyBetweenDoses())
    {
      var activeMoveTypeRadioButton = this._moveAllRadioButtonGroup.getActiveRadioButton() || {};
      this._plannedDoseTimeValidator.setIgnoreFutureAdministrations(activeMoveTypeRadioButton.moveSingle === false);
    }
    if (this._administrationDateTimeField.getDate())
    {
      this._checkAllowedTherapyAdministration();
    }
  },

  _rescheduleTasks: function()
  {
    var self = this;
    this.getView().getRestApi().rescheduleAdministrationTasks(this.getAdministration().getTaskId(),
        this._administrationDateTimeField.getDate(),
        this.getTherapy().getTherapyId(),
        this._moveAllRadioButtonGroup.getActiveRadioButton().moveSingle,
        true)
        .then(
            function()
            {
              var resultData = new app.views.common.AppResultData({success: true});
              self._resultCallback(resultData);
            },
            function()
            {
              var resultData = new app.views.common.AppResultData({success: false});
              self._resultCallback(resultData);
            });
  },

  /**
   * Check if selected time is between previous and next planned administration
   * @private
   */
  _checkAllowedTherapyAdministration: function()
  {
    var warnings = this._administrationWarningsProvider.getRestrictiveAdministrationWarnings(
        this._administrationDateTimeField.getDate(),
        true,
        false,
        false,
        false);
    this._warningContainer.setRestrictiveWarnings(warnings);
    if (this._enableDialogConfirmationFunction)
    {
      this._enableDialogConfirmationFunction(!warnings.hasRestrictiveWarnings());
    }
  },

  /**
   * Checks if selected timestamp is more than 10% off planned timestamp
   * @private
   */
  _checkWarnSelectedTimeDifference: function()
  {
    var self = this;
    var utils = app.views.medications.MedicationUtils;
    if (this._plannedDoseTimeValidator.isTimeDifferenceTooBig(
            this._administrationDateTimeField.getDate(),
            new Date(this.getAdministration().plannedTime),
            0.1,
            this.getTherapy().getDosingFrequency().value))
    {
      utils.openConfirmationWithWarningDialog(this.getView(),
          tm.jquery.Utils.formatMessage(
              this.view.getDictionary("selected.timestamp.difference.warning"), this.getTherapy().getDosingFrequency().value),
          300,
          150).then(function(confirm)
          {
            if (confirm)
            {
              self._rescheduleTasks();
            }
            else if (self._resultCallback)
            {
              var resultData = new app.views.common.AppResultData({success: false});
              self._resultCallback(resultData);
            }
          },
          function()
          {
            if (self._resultCallback)
            {
              var resultData = new app.views.common.AppResultData({success: false});
              self._resultCallback(resultData);
            }
          })
    }
    else
    {
      this._rescheduleTasks();
    }
  },

  _setupValidation: function()
  {
    this._validationForm.reset();
    this._validationForm.addFormField(
        new tm.jquery.FormField({
          component: this._administrationDateTimeField.getDatePicker(),
          required: true,
          componentValueImplementationFn: function(component)
          {
            return component.getDate();
          },
          validation: this._createDateTimePickerValidators()
        }));
    this._validationForm.addFormField(
        new tm.jquery.FormField({
          component: this._administrationDateTimeField.getTimePicker(),
          required: true,
          componentValueImplementationFn: function(component)
          {
            return component.getTime();
          },
          validation: this._createDateTimePickerValidators()
        }));
  },

  /**
   * @return {{type: string, validators: *[]}} validators definition intended to be used on both the date and time picker.
   * @private
   */
  _createDateTimePickerValidators: function()
  {
    return {
      type: "local",
      validators: [
        new tm.jquery.Validator({
          errorMessage: this.getView().getDictionary("field.value.is.invalid"),
          isValid: function(value)
          {
            return tm.jquery.Utils.isDate(value);
          }
        })
      ]
    }
  },

  /**
   * @param {function} enableFunction
   */
  setEnableDialogConfirmationFunction: function(enableFunction)
  {
    this._enableDialogConfirmationFunction = enableFunction;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Boolean}
   */
  isDosingFrequencyBetweenDoses: function()
  {
    var enums = app.views.medications.TherapyEnums;

    return this.getTherapy().getDosingFrequency() &&
        this.getTherapy().getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES;
  },

  /**
   * @Override
   * @param resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    this._resultCallback = resultDataCallback;
    this._setupValidation();
    this._validationForm.submit();
  },

  /**
   * Executed after processResultData triggers the form submit, if the input is valid.
   */
  onValidationSuccess: function()
  {
    if (this.isDosingFrequencyBetweenDoses() && this._moveAllRadioButtonGroup.getActiveRadioButton().moveSingle)
    {
      this._checkWarnSelectedTimeDifference();
    }
    else
    {
      this._rescheduleTasks();
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
   * @return {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @return {Object|null}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @returns {null|app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @return {boolean}
   */
  isInfusionActive: function()
  {
    return this.infusionActive === true;
  },

  /**
   * @return {null|Date}
   */
  getTherapyReviewedUntil: function()
  {
    return this.therapyReviewedUntil;
  }
});