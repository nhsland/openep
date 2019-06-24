Class.define('app.views.medications.ordering.dosing.DosingFrequencyPane', 'tm.jquery.Container', {
  cls: "dosing-frequency-pane",

  /** configs */
  view: null,
  frequencyChangeEvent: null, //optional
  setMaxDailyFrequencyFieldsVisibleFunction: null, //optional
  withSingleFrequencies: true,
  editMode: false,
  allowMoreThan24hFrequency: true,
  /** privates*/
  mode: null,  // 'HOURS', 'COUNT'
  daysOfWeek: null,
  daysInterval: null,
  selectedInterval: null,
  shownInterval: null,
  applicationPrecondition: null,
  orderingBehaviour: null,
  /** privates: components */
  cardContainer: null,
  buttonsContainer: null,
  labelsContainer: null,
  button1xExButtonGroup: null,
  button1xEx: null,
  timeOfDayButtonGroup: null,
  buttonMorning: null,
  buttonNoon: null,
  buttonEvening: null,
  hoursCard: null,
  hoursButtonGroup: null,
  button6h: null,
  button8h: null,
  button12h: null,
  button24h: null,
  hoursField: null,
  countCard: null,
  countButtonGroup: null,
  button1x: null,
  button2x: null,
  button3x: null,
  button4x: null,
  countField: null,
  modeButtonGroup: null,
  countModeButton: null,
  hoursModeButton: null,
  daysButton: null,
  daysLabel: null,
  preconditionButton: null,
  preconditionLabel: null,
  dosingPatternPane: null,
  onModeChangeEvent: null, //optional
  /** @type tm.jquery.Form */
  _frequencyFieldValidationForm: null,
  _reviewReminderLabel: null,
  _reviewReminderDays: null,
  _reviewReminderComment: null,

  BUTTON_WIDTH: 40,

  _triggerChangeEventConditionTask: null,
  /** @type number | undefined */
  _customFrequencyInputKeyUpThrottleTimer: undefined,

  statics: {
    /**
     * Simple dosing frequency object builder, until we implement the correspoding jsClass.
     * @param {string} type one of {@link app.views.medications.TherapyEnums.dosingFrequencyTypeEnum}.
     * @param {number|undefined} [value=undefined]
     * @return {{type: *, value: *}}
     */
    buildDosingFrequency: function(type, value)
    {
      return {
        type: type,
        value: tm.jquery.Utils.isNumeric(value) ? value : null
      }
    }
  },

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }
    this.daysOfWeek = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));
    this._buildComponents();
    this._buildGui();
    if (!this.editMode)
    {
      this._setModeFromContext();
    }
  },

  _buildComponents: function()
  {
    var view = this.getView();
    var self = this;
    this.dosingPatternPane = new app.views.medications.ordering.dosing.DosingPatternPane({
      view: view,
      hidden: true,
      scrollable: "visible",
      getFrequencyKeyFunction: function()
      {
        return self.getFrequencyKey();
      },
      getFrequencyTypeFunction: function()
      {
        return self.getFrequencyType();
      },
      patternChangedEvent: function()
      {
        if (self.frequencyChangeEvent)
        {
          self.frequencyChangeEvent();
        }
      }
    });
    this.button1xEx = new tm.jquery.ToggleButton({cls: "button1xEx", text: view.getDictionary('stat.dose')});

    this.button1xExButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.button1xEx]
    });

    this.button1xExButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.button1xExButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.button1xExButtonGroup);
        self._triggerChangeEvent(self.button1xExButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
        self.hideDaysButton();
        self.clearDaysOfWeek();
      }
      else
      {
        if (self.getOrderingBehaviour().isDaysButtonAvailable() && self.daysButton.isHidden())
        {
          self.showDaysButton();
        }
      }
    });

    this.buttonMorning = new tm.jquery.Button({
      cls: 'morning-button', data: 'MORNING', text: view.getDictionary('in.morning.short')
    });
    this.buttonNoon = new tm.jquery.Button({
      cls: 'noon-button', data: 'NOON', text: view.getDictionary('at.noon.short')
    });
    this.buttonEvening = new tm.jquery.Button({
      cls: 'evening-button', data: 'EVENING', text: view.getDictionary('in.evening.short')
    });

    this.timeOfDayButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: [this.buttonMorning, this.buttonNoon, this.buttonEvening]
    });
    this.timeOfDayButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      clearTimeout(self._customFrequencyInputKeyUpThrottleTimer);
      if (self.timeOfDayButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.timeOfDayButtonGroup);
        self._triggerChangeEvent(self.timeOfDayButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.cardContainer = new tm.jquery.CardContainer({
      cls: 'frequency-card-container',
      width: this.withSingleFrequencies ? 220 : 185,
      height: 30,
      animation: 'slide-vertical'
    });

    var hourUnit = view.getDictionary("hour.unit");
    this.button6h = new tm.jquery.Button({cls: "button6h", data: 6, text: '6' + hourUnit, width: this.BUTTON_WIDTH});
    this.button8h = new tm.jquery.Button({cls: "button8h", data: 8, text: '8' + hourUnit, width: this.BUTTON_WIDTH});
    this.button12h = new tm.jquery.Button({cls: "button12h", data: 12, text: '12' + hourUnit, width: this.BUTTON_WIDTH});
    this.button24h = new tm.jquery.Button({cls: "button24h", data: 24, text: '24' + hourUnit, width: this.BUTTON_WIDTH});

    this.hoursButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      buttons: this.withSingleFrequencies ?
          [this.button6h, this.button8h, this.button12h, this.button24h] :
          [this.button6h, this.button8h, this.button12h]
    });
    this.hoursButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      clearTimeout(self._customFrequencyInputKeyUpThrottleTimer);
      if (self.hoursButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.hoursButtonGroup);
        self._triggerChangeEvent(self.hoursButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.hoursField = new tm.jquery.TextField({cls: "hours-field", width: 45});
    this.hoursField.on(tm.jquery.ComponentEvent.EVENT_TYPE_KEY_UP, this._onCustomFrequencyInputKeyUp.bind(this));
    this.hoursField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.button1x = new tm.jquery.Button({cls: "button1x", data: 1, text: '1X', width: this.BUTTON_WIDTH});
    this.button2x = new tm.jquery.Button({cls: "button2x", data: 2, text: '2X', width: this.BUTTON_WIDTH});
    this.button3x = new tm.jquery.Button({cls: "button3x", data: 3, text: '3X', width: this.BUTTON_WIDTH});
    this.button4x = new tm.jquery.Button({cls: "button4x", data: 4, text: '4X', width: this.BUTTON_WIDTH});

    this.countButtonGroup = new tm.jquery.ButtonGroup({
      type: 'radio',
      height: 30,
      buttons: this.withSingleFrequencies ?
          [this.button1x, this.button2x, this.button3x, this.button4x] :
          [this.button2x, this.button3x, this.button4x]
    });
    this.countButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      clearTimeout(self._customFrequencyInputKeyUpThrottleTimer);
      if (self.countButtonGroup.getSelection().length > 0)
      {
        self._clearOther(self.countButtonGroup);
        self._triggerChangeEvent(self.countButtonGroup.getSelection()[0]);
        self._frequencyFieldValidationForm.reset();
      }
    });

    this.countField = new tm.jquery.TextField({cls: "count-field", width: 45});
    this.countField.on(tm.jquery.ComponentEvent.EVENT_TYPE_KEY_UP, this._onCustomFrequencyInputKeyUp.bind(this));
    this.countField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self.countModeButton.focus();
    });

    this.countModeButton = new tm.jquery.Button({cls: "count-mode-button", text: 'X', pressed: true});
    this.hoursModeButton = new tm.jquery.Button({cls: "hours-mode-button", text: view.getDictionary("hour.unit")});

    this.modeButtonGroup = new tm.jquery.ButtonGroup({
      cls: 'mode-button-group',
      type: 'radio',
      buttons: [this.countModeButton, this.hoursModeButton]
    });

    this.modeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._modeButtonGroupChanged();
      if (self.onModeChangeEvent)
      {
        self.onModeChangeEvent();
      }
      self._triggerChangeEvent(self.modeButtonGroup.getSelection()[0]);
    });

    this.daysButton = new tm.jquery.Button({
      cls: 'show-days-icon days-button',
      handler: function()
      {
        self._openDaysPane();
      }
    });
    this.daysLabel = new tm.jquery.Container({cls: 'TextData days-label'});
    this.preconditionButton = new tm.jquery.Button({
      cls: 'conditions-icon preconditions-button',
      handler: function()
      {
        self._openApplicationPreconditionsPane();
      }
    });
    this.preconditionLabel = new tm.jquery.Container({cls: 'TextData'});
    this._reviewReminderLabel = new tm.jquery.Container({cls: 'TextData'});
    this._frequencyFieldValidationForm = new tm.jquery.Form();
  },

  _buildGui: function()
  {
    this.buttonsContainer = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.add(this.buttonsContainer);
    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.button1xExButtonGroup);
      this.buttonsContainer.add(this.timeOfDayButtonGroup);
    }

    this.countCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.countCard.add(this.countButtonGroup);
    this.countCard.add(this.countField);
    this.cardContainer.add(this.countCard);

    this.hoursCard = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)});
    this.hoursCard.add(this.hoursButtonGroup);
    this.hoursCard.add(this.hoursField);
    this.cardContainer.add(this.hoursCard);

    this.buttonsContainer.add(this.cardContainer);
    this.buttonsContainer.add(this.modeButtonGroup);

    if (this.withSingleFrequencies)
    {
      this.buttonsContainer.add(this.daysButton);
      this.buttonsContainer.add(this.preconditionButton);
      if (this.getOrderingBehaviour().isDosingTimePatternAvailable())
      {
        this.add(this.dosingPatternPane);
      }
      this.labelsContainer = new tm.jquery.Container({
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 1)
      });
      this.add(this.labelsContainer);
      this.labelsContainer.add(this.daysLabel);
      this.labelsContainer.add(this.preconditionLabel);
      this.labelsContainer.add(this._reviewReminderLabel);
    }
  },

  /**
   * Throttled KeyUp event handler for the hours and count field. Triggers input value validation and the redraw of the
   * dosing pattern pane. We previously used the change event on both fields, but due to the event reacting when
   * focus was lost, we had issues when the user wanted to click a button
   * @private
   */
  _onCustomFrequencyInputKeyUp: function(input)
  {
    var self = this;

    clearTimeout(this._customFrequencyInputKeyUpThrottleTimer);
    this._customFrequencyInputKeyUpThrottleTimer = setTimeout(
        function customFrequencyInputKeyUpTimerExecution()
        {
          self._validateCustomFrequencyInput(input);
        },
        350);
  },

  _triggerChangeEvent: function(sender, preventPatternRefresh, preventFrequencyChangeEvent)
  {
    var appFactory = this.getView().getAppFactory();
    var self = this;
    if (this.getOrderingBehaviour().isDosingTimePatternAvailable() &&
        this.withSingleFrequencies && !this.dosingPatternPane.isHidden())
    {

      if (!tm.jquery.Utils.isEmpty(this._triggerChangeEventConditionTask))
      {
        this._triggerChangeEventConditionTask.abort();
      }

      this._triggerChangeEventConditionTask = appFactory.createConditionTask(
          function()
          {
            self._triggerChangeEventImpl(sender, preventPatternRefresh, preventFrequencyChangeEvent);
            self._triggerChangeEventConditionTask = null;
          },
          function()
          {
            return self.dosingPatternPane.isRendered() && $(self.dosingPatternPane.getDom()).is(':visible');
          },
          50, 100);
    }
    else
    {
      self._triggerChangeEventImpl(sender, preventPatternRefresh, preventFrequencyChangeEvent);
    }
  },

  _triggerChangeEventImpl: function(sender, preventPatternRefresh, preventFrequencyChangeEvent)
  {
    if (this.dosingPatternPane)
    {
      if ([this.countModeButton, this.hoursModeButton].indexOf(sender) < 0)
      {
        var $dosingPattern = $(this.dosingPatternPane.getDom());

        if (!preventPatternRefresh)
        {
          $dosingPattern.css("visibility", "hidden");
          this.dosingPatternPane.refreshDosingPattern();
        }
        if (sender)
        {
          var $sender = $(sender.getDom());
          var $dom = $(this.getDom());

          $dosingPattern.css("visibility", "hidden");
          $dosingPattern.css("margin-left", ""); // reset to get the proper size

          var paneWidth = $dosingPattern.outerWidth();
          var domLeftOffset = $dom.offset().left;
          var senderLeftOffset = $sender.offset().left;
          var senderOuterWidth = $sender.outerWidth();

          var freeRightSpace = $dom.width() - senderLeftOffset + domLeftOffset - (senderOuterWidth / 2);
          // we need to check and see how much space there's left to the right - since we want to move the
          // pane so that it's centered under the sender
          paneWidth = paneWidth / 2 > freeRightSpace ? 2 * freeRightSpace : paneWidth;
          var marginLeft = senderLeftOffset - domLeftOffset - (paneWidth / 2) + (senderOuterWidth / 2);

          if (marginLeft > 0) $dosingPattern.css("margin-left", marginLeft + "px");
        }

        $dosingPattern.css("visibility", "");
      }
      else
      {
        this.dosingPatternPane.refreshDosingPattern();
      }
    }
    if (this.frequencyChangeEvent && !preventFrequencyChangeEvent)
    {
      this.frequencyChangeEvent();
    }
  },

  _modeButtonGroupChanged: function()
  {
    clearTimeout(self._customFrequencyInputKeyUpThrottleTimer);
    var selectedButton = this.modeButtonGroup.getSelection()[0];
    if (selectedButton === this.countModeButton)
    {
      this.cardContainer.setActiveItem(this.countCard);
      if (this.hoursButtonGroup.getSelection().length > 0)
      {
        this.hoursButtonGroup.clearSelection();
      }
      if (this.hoursField.getValue())
      {
        this.hoursField.setValue(null)
      }
      if (this.setMaxDailyFrequencyFieldsVisibleFunction)
      {
        this.setMaxDailyFrequencyFieldsVisibleFunction(false);
      }
    }
    else
    {
      this.cardContainer.setActiveItem(this.hoursCard);
      if (this.countButtonGroup.getSelection().length > 0)
      {
        this.countButtonGroup.clearSelection();
      }
      if (this.countField.getValue())
      {
        this.countField.setValue(null)
      }
      if (this.setMaxDailyFrequencyFieldsVisibleFunction)
      {
        this.setMaxDailyFrequencyFieldsVisibleFunction(true);
      }
    }
    this._frequencyFieldValidationForm.reset();
    this._setContext();
  },

  _setContext: function()
  {
    var view = this.getView();
    var context = view.getContext();
    if (!context)
    {
      view.setContext({});
    }
    view.getContext().dosingFrequencyMode = this.getFrequencyMode();
  },

  _openApplicationPreconditionsPane: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var preconditionsPane = new app.views.medications.ordering.ApplicationPreconditionPane({
      view: view,
      orderingBehaviour: this.getOrderingBehaviour(),
      applicationPrecondition: this.applicationPrecondition,
      reminderDays: this._reviewReminderDays,
      reminderComment: this._reviewReminderComment
    });

    var conditionsDialog = appFactory.createDataEntryDialog(
        view.getDictionary("therapy.application.preconditions"),
        null,
        preconditionsPane,
        function(resultData)
        {
          if (resultData)
          {
            var resultDataValue = resultData.getValue();
            self.applicationPrecondition = resultDataValue.selection;
            self._reviewReminderDays = resultDataValue.reminderDays || null;
            self._reviewReminderComment = resultDataValue.reminderComment || null;
            self._setApplicationPreconditionLabel();
            self._setReviewReminderLabel();
          }
        },
        340,
        340
    );
    conditionsDialog.show();
  },

  _openDaysPane: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var daysPane = new app.views.medications.ordering.TherapyDaysContainer({
      view: self.view,
      startProcessOnEnter: true,
      padding: 5,
      daysInterval: this.daysInterval,
      shownInterval: this.shownInterval,
      selectedInterval: this.selectedInterval,
      daysOfWeek: this.daysOfWeek
    });

    var daysDialog = appFactory.createDataEntryDialog(
        view.getDictionary("chosen.days"),
        null,
        daysPane,
        function(resultData)
        {
          if (resultData)
          {
            if (resultData.value.type === 'ALL_DAYS')
            {
              self.daysOfWeek.removeAll();
              self.daysInterval = null;
            }
            else if (resultData.value.type === 'DAYS_OF_WEEK')
            {
              self.daysOfWeek = resultData.value.daysOfWeek;
              self.daysInterval = null;
            }
            else if (resultData.value.type === 'DAYS_INTERVAL')
            {
              self.daysOfWeek.removeAll();
              self.daysInterval = resultData.value.daysInterval;
              self.selectedInterval = resultData.value.selectedInterval;
              self.shownInterval = resultData.value.shownInterval;
            }
            self._setDaysOfWeekLabel();
            self._triggerChangeEvent(null, true);
          }
        },
        400,
        250
    );
    daysDialog.show();
  },

  _setDaysOfWeekLabel: function()
  {
    var view = this.getView();

    if (this.daysInterval)
    {
      this.daysLabel.setHtml(
          view.getDictionary("chosen.days") + ": " +
          (this.daysInterval === 7 ?
              view.getDictionary('weekly') :
              tm.jquery.Utils.formatMessage(view.getDictionary('every.n.days'), this.daysInterval)));
    }
    else if (this.daysOfWeek.length > 0)
    {
      var daysDisplay = view.getDictionary("chosen.days") + ": ";
      for (var i = 0; i < this.daysOfWeek.length; i++)
      {
        daysDisplay += app.views.medications.MedicationTimingUtils.getDayOfWeekDisplay(view, this.daysOfWeek[i], true);
        if (i < this.daysOfWeek.length - 1)
        {
          daysDisplay += ', ';
        }
      }
      this.daysLabel.setHtml(daysDisplay);
      this.daysLabel.setTooltip(app.views.medications.MedicationUtils.createTooltip(daysDisplay, null, view));
    }
    else
    {
      this.daysLabel.setHtml("");
    }
  },

  _setApplicationPreconditionLabel: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.applicationPrecondition))
    {
      var view = this.getView();
      this.preconditionLabel.setHtml(view.getDictionary("medication.start.criterion") + ": " +
          view.getDictionary('MedicationAdditionalInstructionEnum.' + this.applicationPrecondition));
    }
    else
    {
      this.preconditionLabel.setHtml("");
    }
  },

  _setReviewReminderLabel: function()
  {
    var view = this.getView();
    var reminderString = "";
    if (!tm.jquery.Utils.isEmpty(this._reviewReminderDays))
    {
      reminderString = tm.jquery.Utils.formatMessage(view.getDictionary("review.therapy.days"), this._reviewReminderDays);
      if (!tm.jquery.Utils.isEmpty(this._reviewReminderComment))
      {
        reminderString += ", " + view.getDictionary("commentary").toLowerCase() + ": " + this._reviewReminderComment;
      }
    }
    this._reviewReminderLabel.setHtml(reminderString)
  },

  _clearOther: function(component)
  {
    if (this.button1xExButtonGroup !== component)
    {
      this.button1xExButtonGroup.clearSelection();
    }
    if (this.timeOfDayButtonGroup !== component)
    {
      this.timeOfDayButtonGroup.clearSelection();
    }
    if (this.countButtonGroup !== component)
    {
      this.countButtonGroup.clearSelection();
    }
    if (this.hoursButtonGroup !== component)
    {
      this.hoursButtonGroup.clearSelection();
    }
    if (this.hoursField !== component)
    {
      this.hoursField.setValue(null, true);
    }
    if (this.countField !== component)
    {
      this.countField.setValue(null, true);
    }
  },

  _setModeFromContext: function()
  {
    var context = this.getView().getContext();
    if (context && this.getFrequencyMode() !== context.dosingFrequencyMode)
    {
      if (context.dosingFrequencyMode === "HOURS")
      {
        this.modeButtonGroup.setSelection([this.hoursModeButton], true);
      }
      else
      {
        this.modeButtonGroup.setSelection([this.countModeButton], true);
      }
      this._modeButtonGroupChanged();
    }
  },

  /**
   * Validates the input of the given field (custom count or hour input) and, if valid, triggers the redraw of the dosing
   * pattern representation.
   * @param {tm.jquery.TextField|tm.jquery.Field} inputField
   * @private
   */
  _validateCustomFrequencyInput: function(inputField)
  {
    var self = this;

    this._frequencyFieldValidationForm.reset();
    this._frequencyFieldValidationForm.addFormField(this._createCustomFrequencyFormValidation(inputField));
    this._frequencyFieldValidationForm.onValidationSuccess = function onFrequencyFieldValidationSuccess()
    {
      self._clearOther(inputField);
      self._triggerChangeEvent(inputField);
    };
    this._frequencyFieldValidationForm.onValidationError = function onFrequencyFieldValidationError()
    {
      // clear the values and inform any listeners of the change (so that they may also clear their values)
      self._clearOther(inputField);
      self.dosingPatternPane.clear();
      self.frequencyChangeEvent();
    };

    this._frequencyFieldValidationForm.submit();
  },

  /**
   * Creates a new instance of the FormField validation for the given custom frequency value input - either the hour
   * or count field. The validator can be safely attached to {@link #countField} and {@link #hoursField} at the same
   * time as neither of the field values are required.
   * @param {tm.jquery.Field|tm.jquery.TextField} inputField
   * @return {tm.jquery.FormField}
   * @private
   */
  _createCustomFrequencyFormValidation: function(inputField)
  {
    if (inputField !== this.hoursField && inputField !== this.countField)
    {
      throw new Error('Custom frequency validator only supports the count or hour field!');
    }

    var view = this.getView();
    var self = this;
    var hourFieldValidator = inputField === this.hoursField;

    return new tm.jquery.FormField({
      component: inputField,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: hourFieldValidator ?
                view.getDictionary('invalid.dosing.frequency.hour.format') :
                view.getDictionary('invalid.dosing.frequency.number.format'),
            /**
             * Checks if value exists, is numeric and positive.
             * If frequency mode is by hours, we allow frequency by half an hour.
             * If frequency mode is count, the number must be whole.
             * @param {Number|null} value
             * @returns {boolean}
             */
            isValid: function(value)
            {
              return !value || (tm.jquery.Utils.isNumeric(value) &&
                  value > 0 &&
                  (hourFieldValidator ? value % 0.5 === 0 : value % 1 === 0));
            }
          }),
          new tm.jquery.Validator({
            errorMessage: view.getDictionary('invalid.dosing.frequency.number.range'),
            isValid: function(value)
            {
              if (!value || !tm.jquery.Utils.isNumeric(value))
              {
                return true;
              }
              var preventValueGreaterThan24 = !hourFieldValidator || !self.allowMoreThan24hFrequency;
              return preventValueGreaterThan24 ? value <= 24 : true;
            }
          })
        ]
      }
    });
  },

  /**
   * Automatically sets review reminder for medications with review reminder property
   * @private
   */
  _setAutomaticReviewReminder: function()
  {
    this.setReviewReminder(2, this.getView().getDictionary("review.therapy"));
  },

  /** public methods */
  clear: function()
  {
    this.showAllFields();
    this.clearDaysOfWeek();
    this.button1xExButtonGroup.show();
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
    this.button1xExButtonGroup.clearSelection(true);
    this.timeOfDayButtonGroup.clearSelection(true);
    if (!this.editMode)
    {
      this._setModeFromContext();
    }
    this.hoursButtonGroup.clearSelection(true);
    this.countButtonGroup.clearSelection(true);
    this.hoursField.setValue(null, true);
    this.countField.setValue(null, true);
    this.applicationPrecondition = null;
    this.preconditionLabel.setHtml("");
    this._reviewReminderLabel.setHtml("");
    this.dosingPatternPane.clear();
    this._reviewReminderDays = null;
    this._reviewReminderComment = null;
  },

  clearDaysOfWeek: function()
  {
    this.daysOfWeek.removeAll();
    this.daysInterval = null;
    this.selectedInterval = null;
    this.shownInterval = null;
    this._setDaysOfWeekLabel();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {{type: string, value: number|null}|null}
   */
  getFrequency: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (this.button1xExButtonGroup.getSelection().length > 0)
    {
      return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
          enums.dosingFrequencyTypeEnum.ONCE_THEN_EX);
    }
    if (this.timeOfDayButtonGroup.getSelection().length > 0)
    {
      var timeOfDayButton = this.timeOfDayButtonGroup.getSelection()[0];
      return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(timeOfDayButton.data);
    }
    if (this.getFrequencyMode() === 'COUNT')
    {
      if (this.countButtonGroup.getSelection().length > 0)
      {
        var countButton = this.countButtonGroup.getSelection()[0];
        return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
            enums.dosingFrequencyTypeEnum.DAILY_COUNT,
            countButton.data);
      }
      var countFieldValue = this.countField.getValue();
      if (countFieldValue && tm.jquery.Utils.isNumeric(countFieldValue))
      {
        return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
            enums.dosingFrequencyTypeEnum.DAILY_COUNT,
            countFieldValue);
      }
      return null;
    }
    if (this.getFrequencyMode() === 'HOURS')
    {
      if (this.hoursButtonGroup.getSelection().length > 0)
      {
        var hoursButton = this.hoursButtonGroup.getSelection()[0];
        return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
            enums.dosingFrequencyTypeEnum.BETWEEN_DOSES,
            hoursButton.data);
      }
      var hoursFieldValue = this.hoursField.getValue();
      if (hoursFieldValue && tm.jquery.Utils.isNumeric(hoursFieldValue))
      {
        return app.views.medications.ordering.dosing.DosingFrequencyPane.buildDosingFrequency(
            enums.dosingFrequencyTypeEnum.BETWEEN_DOSES,
            hoursFieldValue);
      }
    }
    return null;
  },

  getFrequencyValue: function()
  {
    var frequency = this.getFrequency();
    return !tm.jquery.Utils.isEmpty(frequency) ? frequency.value : null;
  },

  getFrequencyKey: function()
  {
    var dosingFrequency = this.getFrequency();
    return app.views.medications.MedicationTimingUtils.getFrequencyKey(dosingFrequency);
  },

  getFrequencyTimesPerDay: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var dosingFrequency = this.getFrequency();
    if (dosingFrequency)
    {
      if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        return 24 / dosingFrequency.value;
      }
      if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.DAILY_COUNT)
      {
        return dosingFrequency.value;
      }
      return 1;
    }
    return null;
  },

  getDaysOfWeek: function()
  {
    return this.daysOfWeek;
  },

  getFrequencyType: function()
  {
    var frequency = this.getFrequency();
    if (frequency)
    {
      return frequency.type;
    }
    else if (this.daysOfWeek.length > 0)
    {
      return "DAYS_ONLY";
    }
    return null;
  },

  /**
   * Returns the active frequency mode (hours or count), which may be irrelevant when 1ex, morning, evening or the
   * noon button is selected or if we're only showing the day limitation buttons.
   * @return {String} 'HOURS' or 'COUNT'
   */
  getFrequencyMode: function()
  {
    return this.modeButtonGroup.getSelection()[0] === this.countModeButton ? 'COUNT' : 'HOURS';
  },

  setDaysOfWeek: function(daysOfWeek)
  {
    this.daysOfWeek = daysOfWeek ? daysOfWeek : [];
    this._setDaysOfWeekLabel();
  },

  getDaysFrequency: function()
  {
    return this.daysInterval;
  },

  getTimesPerWeek: function()
  {
    var timesPerWeek = null;
    if (this.getDaysFrequency() !== null)
    {
      //timesPerWeek = Math.ceil(7 / this.getDaysFrequency());
      timesPerWeek = 7 / this.getDaysFrequency();
    }
    else if (this.getDaysOfWeek() !== null)
    {
      timesPerWeek = this.getDaysOfWeek().size();
    }

    return timesPerWeek;
  },

  setDaysFrequency: function(daysInterval)
  {
    this.daysInterval = daysInterval;
    this._setDaysOfWeekLabel();
  },

  getApplicationPrecondition: function()
  {
    return this.applicationPrecondition;
  },

  setApplicationPrecondition: function(applicationPrecondition)
  {
    this.applicationPrecondition = applicationPrecondition;
    this._setApplicationPreconditionLabel();
  },

  getDosingPattern: function()
  {
    return this.dosingPatternPane.getDosingPattern();
  },

  setFrequency: function(frequency, preventEvent) //returns selected component
  {
    var oldFrequency = this.getFrequency();

    if (oldFrequency && frequency &&
        (app.views.medications.MedicationTimingUtils.getFrequencyKey(oldFrequency) ===
            app.views.medications.MedicationTimingUtils.getFrequencyKey(frequency)))
    {
      // do nothing
      return;
    }

    var enums = app.views.medications.TherapyEnums;
    var selectedComponent = null;
    if (frequency)
    {
      if (frequency.type === enums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        this.button1xExButtonGroup.setSelection([this.button1xExButtonGroup.getButtons()[0]], true);
        selectedComponent = this.button1xExButtonGroup.getButtons()[0];
      }
      else if ([enums.dosingFrequencyTypeEnum.MORNING, enums.dosingFrequencyTypeEnum.NOON,
        enums.dosingFrequencyTypeEnum.EVENING].indexOf(frequency.type) > -1)
      {
        var timeOfDayButtons = this.timeOfDayButtonGroup.getButtons();
        for (var k = 0; k < timeOfDayButtons.length; k++)
        {
          if (timeOfDayButtons[k].data === frequency.type)
          {
            this.timeOfDayButtonGroup.setSelection([timeOfDayButtons[k]], true);
            selectedComponent = timeOfDayButtons[k];
          }
        }
      }
      else if (frequency.type === enums.dosingFrequencyTypeEnum.DAILY_COUNT)
      {
        this.modeButtonGroup.setSelection([this.countModeButton], true);
        this._modeButtonGroupChanged();
        var countButtons = this.countButtonGroup.getButtons();
        var countValueInButtons = false;
        for (var i = 0; i < countButtons.length; i++)
        {
          if (countButtons[i].data === frequency.value)
          {
            countValueInButtons = true;
            this.countButtonGroup.setSelection([countButtons[i]], true);
            this.countField.setValue(null, true);
            selectedComponent = countButtons[i];
          }
        }
        if (!countValueInButtons)
        {
          this.countButtonGroup.clearSelection();
          this.countField.setValue(frequency.value, true);
          selectedComponent = this.countField;
        }
      }
      else if (frequency.type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        this.modeButtonGroup.setSelection([this.hoursModeButton], true);
        this._modeButtonGroupChanged();
        var hoursButtons = this.hoursButtonGroup.getButtons();
        var hoursValueInButtons = false;
        for (var j = 0; j < hoursButtons.length; j++)
        {
          if (hoursButtons[j].data === frequency.value)
          {
            hoursValueInButtons = true;
            this.hoursButtonGroup.setSelection([hoursButtons[j]], true);
            this.hoursField.setValue(null, true);
            selectedComponent = hoursButtons[j];
          }
        }
        if (!hoursValueInButtons)
        {
          this.hoursButtonGroup.clearSelection();
          this.hoursField.setValue(frequency.value, true);
          selectedComponent = this.hoursField;
        }
      }
      if (frequency.type === enums.dosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        this.hideDaysButton();
        this.clearDaysOfWeek();
      }
      else
      {
        this.showDaysButton();
      }
    }
    else
    {
      this.clear();
    }
    if (!preventEvent)
    {
      this._triggerChangeEvent(selectedComponent);
    }
    return selectedComponent;
  },

  /**
   * @param {Array<{hour: number, minute: number}>} pattern
   * @param {{type: string, value: null|number}} frequency
   */
  setDosingPattern: function(pattern, frequency)
  {
    var enums = app.views.medications.TherapyEnums;

    if (this.dosingPatternPane)
    {
      var frequencyKey = app.views.medications.MedicationTimingUtils.getFrequencyKey(frequency);
      if (frequency && frequency.type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var hoursPattern =
            app.views.medications.MedicationTimingUtils.getPatternForFrequencyBetweenHours(
                pattern[0],
                frequencyKey ?
                    frequencyKey :
                    this.getFrequencyKey());
        this.dosingPatternPane.setDosingPattern(hoursPattern);
      }
      else
      {
        this.dosingPatternPane.setDosingPattern(pattern);
      }
    }
  },

  /**
   * @param {{type: string, value: number}} frequency
   * @param {Array<{hour: number, minute: number}>} pattern
   * @param {Boolean} [preventEvent=false]
   */
  setDosingFrequencyAndPattern: function(frequency, pattern, preventEvent)
  {
    var selectedComponent = this.setFrequency(frequency, true);
    if (pattern && pattern.length > 0)
    {
      this.setDosingPattern(pattern, frequency);
    }
    // trigger change event so the position sets correctly, optionally prevent other events
    this._triggerChangeEvent(selectedComponent, preventEvent, preventEvent);
  },

  /**
   * Returns the frequency input validators. Prescriptions that will be administered when needed do not require the
   * frequency set, regardless of the ordering behavior configuration, as it's not possible to predict the needed frequency
   * in advance.
   * @param {Boolean} [whenNeeded=false]
   * @returns {Array<tm.jquery.FormField>}
   */
  getDosingFrequencyPaneValidations: function(whenNeeded)
  {
    var enums = app.views.medications.TherapyEnums;
    var formFields = [];
    var self = this;

    if ((whenNeeded || !this.orderingBehaviour.isDoseFrequencyRequired()) && !this.getFrequency())
    {
      return formFields;
    }

    if (!this.cardContainer.isHidden())
    {
      formFields.push(this._createCustomFrequencyFormValidation(this.countField));
      formFields.push(this._createCustomFrequencyFormValidation(this.hoursField));
      formFields.push(
          new tm.jquery.FormField({
            component: this, //validate values of multiple fields on this pane
            required: true,
            componentValueImplementationFn: function()
            {
              return self.getFrequency();
            },
            validation: {
              type: "local",
              validators: [
                new tm.jquery.Validator({
                  errorMessage: tm.jquery.Utils.isEmpty(self.daysInterval) ||
                  tm.jquery.Utils.isEmpty(self.getFrequencyValue()) ?
                      "" :
                      tm.jquery.Utils.formatMessage(self.view.getDictionary('invalid.dosing.frequency.days.hours'),
                          self.daysInterval, self.getFrequencyValue()),
                  isValid: function()
                  {
                    var frequency = self.getFrequency();
                    if (tm.jquery.Utils.isEmpty(frequency) || tm.jquery.Utils.isEmpty(frequency.value) ||
                        frequency.type !== enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
                    {
                      return true;
                    }
                    var positiveNumber = tm.jquery.Utils.isNumeric(frequency.value) && frequency.value > 0;
                    return positiveNumber && tm.jquery.Utils.isEmpty(self.daysInterval);
                  }
                })
              ]
            }
          }));
    }
    if (this.withSingleFrequencies && this.dosingPatternPane && !this.dosingPatternPane.isHidden())
    {
      formFields.push(this.dosingPatternPane.getDosingPatternPaneValidation());
    }
    return formFields;
  },

  /**
   * @param {Number} duration in minutes
   * @returns {tm.jquery.FormField}
   */
  getDosingPatternPaneDurationValidation: function(duration)
  {
    return this.dosingPatternPane.getPatternDurationValidation(duration);
  },

  requestFocus: function()
  {
    this.button1xExButtonGroup.focus();
  },

  hideDaysButton: function()
  {
    this.daysButton.isRendered() ? this.daysButton.hide() : this.daysButton.setHidden(true);
  },

  showDaysButton: function()
  {
    this.daysButton.isRendered() ? this.daysButton.show() : this.daysButton.setHidden(false);
  },

  showDaysOnly: function(preventClear)
  {
    if (this.getOrderingBehaviour().isDaysButtonAvailable() && this.daysButton.isHidden())
    {
      this.showDaysButton();
    }
    if (!preventClear)
    {
      this.button1xExButtonGroup.clearSelection(true);
      this.timeOfDayButtonGroup.clearSelection(true);
      this.modeButtonGroup.setSelection([this.countModeButton], true);
      this.hoursButtonGroup.clearSelection(true);
      this.countButtonGroup.clearSelection(true);
      this.hoursField.setValue(null, true);
      this.countField.setValue(null, true);
    }
    this.button1xExButtonGroup.hide();
    this.timeOfDayButtonGroup.hide();
    this.cardContainer.hide();
    this.modeButtonGroup.hide();
    this.dosingPatternPane.hide();
  },

  showAllFields: function()
  {
    this.button1xExButtonGroup.show();
    if (this.getOrderingBehaviour().isDaysButtonAvailable() && this.daysButton.isHidden()
        && this.button1xExButtonGroup.getSelection().length === 0)
    {
      this.showDaysButton();
    }
    this.timeOfDayButtonGroup.show();
    this.cardContainer.show();
    this.modeButtonGroup.show();
  },

  setMedicationData: function(medicationData)
  {
    if (medicationData.isReviewReminder())
    {
      this._setAutomaticReviewReminder();
    }
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @param {Number} days
   * @param {String} comment
   */
  setReviewReminder: function(days, comment)
  {
    this._reviewReminderDays = days;
    this._reviewReminderComment = comment;
    this._setReviewReminderLabel();
  },

  /**
   * @returns {Number|null}
   */
  getReviewReminderDays: function()
  {
    return this._reviewReminderDays;
  },

  /**
   * @returns {String|null}
   */
  getReviewReminderComment: function()
  {
    return this._reviewReminderComment;
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    if (!tm.jquery.Utils.isEmpty(this._triggerChangeEventConditionTask))
    {
      this._triggerChangeEventConditionTask.abort();
      this._triggerChangeEventConditionTask = null;
    }
    clearTimeout(this._customFrequencyInputKeyUpThrottleTimer);
  }
});

