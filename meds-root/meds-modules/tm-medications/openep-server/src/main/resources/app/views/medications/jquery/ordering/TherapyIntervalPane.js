Class.define('app.views.medications.ordering.TherapyIntervalPane', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_INTERVAL_CHANGE: new tm.jquery.event.EventType({
      name: 'therapyIntervalPaneIntervalChange', delegateName: null})
  },
  cls: "therapy-interval-pane",
  scrollable: 'visible',

  /** configs */
  /** @type tm.views.medications.TherapyView */
  view: null,
  getDurationFunction: null,  //optional
  getFrequencyDataFunction: null, //optional
  getDosingPatternFunction: null, //optional
  onMaxDailyFrequencyFieldFocusLost: null,
  copyMode: false,
  restrictedStartHourSelection: true,
  presetCurrentTime: false,
  byDoctorsOrderSupported: true,
  whenNeededSupported: true,
  maxDailyFrequencyAvailable: true,
  orderingBehaviour: null,
  /** privates */
  fixedEndDate: null,
  /** privates: components */
  startDateField: null,
  startHourField: null,
  startHourCombo: null,
  _therapyDurationFieldsContainer: null,
  _maxFrequencyContainer: null,
  durationUnitOfMeasureSelectBox: null,
  endAmountField: null,
  endAmountFieldUnitId: null, /* caches the last selected selectbox value for calculations when switching to date */
  endDateTimeField: null,
  endDateTimeText: null,
  whenNeededButton: null,
  byDoctorsOrderButton: null,
  maxDailyFrequencyField: null,

  _endDateLabelRefreshTimer: undefined, /* set as undefined to prevent inspection warning on clearTimeout */
  /** @type tm.jquery.CheckBox|null */
  _recordAdministrationCheckBox: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.ordering.TherapyIntervalPane', [
      { eventType: app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE }
    ]);

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    this._buildStartEndTimeComponents();
    this._buildOtherComponents();
    this._buildGui();
  },

  /** private methods */
  _buildStartEndTimeComponents: function()
  {
    var self = this;
    var view = this.getView();
    this.startDateField = new tm.jquery.DatePicker({
      cls: "start-date-field",
      showType: "focus",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "150px"),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      },
      date: this.isPresetCurrentTime() ? CurrentTime.get() : null
    });
    this.startDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });
    this.startDateField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(CurrentTime.get());
          self.startHourField.setTime(self.startHourField.formatTime(CurrentTime.get()));
        });
    this.startHourField = new tm.jquery.TimePicker({
      cls: "start-hour-field",
      showType: "focus",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "90px"),
      nowButton: {
        text: view.getDictionary("asap")
      },
      hidden: this.isRestrictedStartHourSelection(),
      time: this.isPresetCurrentTime() && ! this.isRestrictedStartHourSelection() ? CurrentTime.get() : null,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.startHourField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.calculateEnd();
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });
    this.startHourField.getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.startDateField.setDate(CurrentTime.get());
          self.startHourField.setTime(self.startHourField.formatTime(CurrentTime.get()));
        });
    this.startHourCombo = new tm.jquery.SelectBox({
      cls: "start-hour-combo",
      allowSingleDeselect: false,
      multiple: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "90px"),
      dropdownWidth: "stretch",
      placeholder: " ",
      hidden: !this.isRestrictedStartHourSelection(),
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1.hour === value2.hour && value1.minute === value2.minute;
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        var value = option.getValue();
        return app.views.medications.MedicationTimingUtils.hourMinuteToString(value.hour, value.minute)
      }
    });

    this.startHourCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._onEndAmountValueChanged();
      self._signalChangedEvent();
      self._calculateAndSetMinEndDateTime(true);
    });

    var durationUnitOptions = this.getDurationInputTypes().map(function (item)
    {
      return tm.jquery.SelectBox.createOption(item, null);
    });
    this.durationUnitOfMeasureSelectBox = new tm.jquery.SelectBox({
      liveSearch: false,
      options: durationUnitOptions,
      selections: durationUnitOptions.length > 0 ? [durationUnitOptions[0]] : [],
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: "stretch",
      allowSingleDeselect: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return option.getValue().title;
      },
      hidden: false
    });
    this.durationUnitOfMeasureSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        function(component, componentEvent){
          var selections = componentEvent.eventData.selections;
          self._onDurationUnitOfMeasureValueChanged(selections.length > 0 ? selections[0] : null);
    });

    this.endAmountField = new tm.jquery.TextField({ // using TextField instead of NumberField due to KEY_UP event support
      cls: "end-days-field",
      width: 40,
      hidden: true
    });

    var endAmountFieldEventDebouncedTask = view.getAppFactory().createDebouncedTask(
        "endAmountFieldEventDebouncedTask", function()
        {
          self._onEndAmountValueChanged();
          self._signalChangedEvent();
        }, 200);

    this.endAmountField.on(tm.jquery.ComponentEvent.EVENT_TYPE_KEY_UP, function()
    {
      endAmountFieldEventDebouncedTask.run();
    });

    this.endAmountField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      if (self.isWhenNeededSupported())
      {
        self.whenNeededButton.focus();
      }
    });

    this.endDateTimeField = new tm.jquery.DateTimePicker({
      showType: "focus",
      hidden: true
    });

    this.endDateTimeField.getDatePicker().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._onEndDateTimeDatePickerChange();
    });

    this.endDateTimeField.getTimePicker().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (self.getStart() && self.endDateTimeField.getDatePicker().getDate())
      {
        self._signalChangedEvent();
      }
    });

    this.endDateTimeField.getDatePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateTimeField.setDate(CurrentTime.get());
        });
    this.endDateTimeField.getTimePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        function()
        {
          self.endDateTimeField.setDate(CurrentTime.get());
        });
    this.endDateTimeField.getDatePicker().setStyle("min-width: 95px;");

    this.endDateTimeText = new tm.jquery.Label({
      cls: "TextData",
      html: null,
      hidden: true
    });

    if (this.getOrderingBehaviour().isPastMode())
    {
      this.showDurationFieldsForValueType();
    }
  },

  _buildOtherComponents: function()
  {
    var self = this;
    var view = this.getView();

    if (this.isWhenNeededSupported())
    {
      this.whenNeededButton = new tm.jquery.ToggleButton({
        cls: "when-needed-button",
        text: view.getDictionary("when.needed.short"),
        alignSelf: "center"
      });
      this.whenNeededButton.setTooltip(app.views.medications.MedicationUtils.createTooltip(
          view.getDictionary("when.needed"),
          "left",
          view));
      this.whenNeededButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, this._onWhenNeededChange.bind(this));
    }

    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton = new tm.jquery.ToggleButton({
        cls: "by-doctors-orders",
        width: 40,
        text: view.getDictionary("by.doctor.orders.short"),
        data: app.views.medications.TherapyEnums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS,
        alignSelf: "center"
      });
      this.byDoctorsOrderButton.setTooltip(app.views.medications.MedicationUtils.createTooltip(
          view.getDictionary("by.doctor.orders"),
          "left",
          view));
      this.byDoctorsOrderButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        if (self.byDoctorsOrderButton.isPressed() && self.isWhenNeededSupported())
        {
          self.whenNeededButton.setPressed(false);
          self.setMaxDailyFrequencyFieldVisible(false);
        }
      });
    }

    if (this.isMaxDailyFrequencyAvailable())
    {
      this.maxDailyFrequencyField = new tm.jquery.TextField({
        cls: "max-daily-frequency-field",
        width: 32,
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary('dosing.max.24h'), null, view)
      });

      this.maxDailyFrequencyField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function()
      {
        self.onMaxDailyFrequencyFieldFocusLost();
      });
    }
  },

  _buildGui: function()
  {
    // align items to the top in case the reminder/doctor's order/prn/supply group has to wrap into two rows,
    // which usually happens when setting the therapy's end as a fixed date
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0));

    //start
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      var dateTimeContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
        scrollable: "visible"
      });

      dateTimeContainer.add(this.startDateField);
      dateTimeContainer.add(this.startHourField);
      dateTimeContainer.add(this.startHourCombo);

      var startTimeContainer = new app.views.medications.common.VerticallyTitledComponent({
        cls: 'therapy-start-container',
        titleText: this.getView().getDictionary('start'),
        contentComponent: dateTimeContainer,
        scrollable: "visible"
      });

      this.add(startTimeContainer);

      var therapyDurationContentContainer = new tm.jquery.Container({
        cls: "therapy-duration-container",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
      });

      therapyDurationContentContainer.add(this.endAmountField);
      therapyDurationContentContainer.add(this.endDateTimeField);
      therapyDurationContentContainer.add(this.durationUnitOfMeasureSelectBox); // value type selectbox
      therapyDurationContentContainer.add(this.endDateTimeText);

      this._therapyDurationFieldsContainer = new app.views.medications.common.VerticallyTitledComponent({
        cls: "therapy-duration-fields-container",
        titleText: this.getView().getDictionary('therapy.duration'),
        contentComponent: therapyDurationContentContainer,
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      this.add(this._therapyDurationFieldsContainer);

      // the assumption here is that if there's no start and end time available we can't auto administer either
      if (this.orderingBehaviour.isRecordAdministrationAvailable())
      {
        this._recordAdministrationCheckBox = new tm.jquery.CheckBox({
          cls: 'record-administration-checkbox',
          labelText: this.view.getDictionary('record.administration'),
          labelCls: 'TextData',
          checked: false,
          hidden: true /** only be visible for stat doses */
        });
        this.add(this._recordAdministrationCheckBox);
      }
    }

    var rightContainer = new tm.jquery.Container({
      cls: 'right-buttons-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 0, tm.jquery.flexbox.FlexFlow.create("row", "wrap")),
      scrollable: "visible",
      // allow it to grow to maximum size so that the row wrap works and that the buttons are always on the far right
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      alignSelf: 'flex-end' // in case we only display buttons, without title labels, which are shorter
    });

    if (this.isWhenNeededSupported())
    {
      rightContainer.add(this.whenNeededButton);
    }
    if (this.isByDoctorsOrderAvailable())
    {
      rightContainer.add(this.byDoctorsOrderButton);
    }

    if (this.isMaxDailyFrequencyAvailable())
    {
      this._maxFrequencyContainer = new app.views.medications.common.VerticallyTitledComponent({
        cls: "max-frequency-container",
        titleText: "Max",
        hidden: true, /* don't show until the when need button is pressed */
        contentComponent: this.maxDailyFrequencyField,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });

      rightContainer.add(this._maxFrequencyContainer);
    }
    this.add(rightContainer);
  },

  /**
   * @param {Boolean} [autoSelect=false] true if the first duration type option should be selected automatically, otherwise
   * false.
   * @private
   */
  _resetTherapyDurationInputFields: function(autoSelect)
  {
    this.isRendered() ? this.endDateTimeField.hide() : this.endDateTimeField.setHidden(true);
    this.isRendered() ? this.endAmountField.hide() : this.endAmountField.setHidden(true);
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    this.isRendered() ? this.endDateTimeText.hide() : this.endDateTimeText.setHidden(true);
    if (this._therapyDurationFieldsContainer)
    {
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
    if (autoSelect)
    {
      var firstOption = this.durationUnitOfMeasureSelectBox.getOptions()[0];
      if (!tm.jquery.Utils.isEmpty(firstOption))
      {
        this.durationUnitOfMeasureSelectBox.setSelections([firstOption.value], true);
      }
    }
  },

  _onDurationUnitOfMeasureValueChanged: function(selection)
  {
    var self = this;

    if (!tm.jquery.Utils.isEmpty(selection)
        && selection instanceof app.views.medications.ordering.TherapyIntervalPaneDurationUnitType)
    {
      if (tm.jquery.Utils.isEmpty(this.endAmountFieldUnitId)) /* if there was no value date was prev. selected */
      {
        this.endAmountField.setValue(null);
        this.endDateTimeText.setHtml(null);
      }
      this.endAmountFieldUnitId = selection.getId();
      this.showDurationFieldsForValueType();
      this._onEndAmountValueChanged();
    }
    else if (selection.id === app.views.medications.TherapyEnums.therapyIntervalPaneSelectionIds.UNTIL_CANCELED)
    {
      self.endAmountField.setValue(null);
      self.endDateTimeText.setHtml(null);
      self.endDateTimeField.getDatePicker().setDate(null, true);
      self.endDateTimeField.getTimePicker().setTime(null, true);
      self._signalChangedEvent();

      self._resetTherapyDurationInputFields(true);
    }
    else // date picker
    {
      var fixedAmountValue = this._getDeLocalizedEndAmountValue();
      if (fixedAmountValue && tm.jquery.Utils.isNumeric(fixedAmountValue) && fixedAmountValue > 0)
      {
        var fixedAmountType = this._findDurationValueTypOptionById(this.endAmountFieldUnitId);
        var startDate = this.startDateField.getDate();

        if (startDate && !tm.jquery.Utils.isEmpty(fixedAmountType))
        {
          var endDate = new Date(startDate.valueOf());
          var startTime = this.getStart();
          if (startTime)
          {
            endDate.setTime(startTime);
          }

          this.endDateTimeField.setDate(fixedAmountType.appendToInputValue(endDate, fixedAmountValue));
        }
      }
      else if (this.fixedEndDate)
      {
        this.endDateTimeField.setDate(this.fixedEndDate);
      }
      else
      {
        this.endDateTimeField.setDate(null);
        this.endAmountField.setValue(null);
        this.endDateTimeText.setHtml(null);
      }
      this.endAmountFieldUnitId = null; /* reset whatever was cached as last value */

      this._isStatDose() && this._therapyDurationFieldsContainer ?
          this._therapyDurationFieldsContainer.hide() :
          this.showDurationFieldsForDateType();
    }
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isStatDose: function()
  {
    var dosingFrequencyTypeEnum = app.views.medications.TherapyEnums.dosingFrequencyTypeEnum;

    return !!this.getFrequencyDataFunction &&
        this.getFrequencyDataFunction().frequencyType === dosingFrequencyTypeEnum.ONCE_THEN_EX;
  },

  _onEndAmountValueChanged: function()
  {
    var self = this;
    var view = this.getView();
    clearTimeout(this._endDateLabelRefreshTimer);
    this._endDateLabelRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        var displayValue = self.getEnd();
        displayValue = tm.jquery.Utils.isEmpty(displayValue) ?
            null : ("(" + view.getDisplayableValue(displayValue, "short.date.time") + ")");
        self.endDateTimeText.setHtml(displayValue);
      }
    }, 350);
  },

  _findDurationValueTypOptionById: function(id)
  {
    if (tm.jquery.Utils.isEmpty(id)) return null;

    var options = this.durationUnitOfMeasureSelectBox.getOptions();
    for (var idx = 0; idx < options.length; idx++)
    {
      var option = options[idx].value;

      if (option instanceof app.views.medications.ordering.TherapyIntervalPaneDurationUnitType && option.getId() === id)
      {
        return option;
      }
    }
    return null;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.Therapy|null} oldTherapy
   * @returns {boolean}
   * @private
   */
  _frequencyOrPatternChanged: function(therapy, oldTherapy)
  {
    var frequencyChanged = tm.jquery.Utils.isEmpty(oldTherapy) ||
        tm.jquery.Utils.isEmpty(oldTherapy.getDosingFrequency()) ||
        oldTherapy.getDosingFrequency().type !== therapy.getDosingFrequency().type ||
        oldTherapy.getDosingFrequency().value !== therapy.getDosingFrequency().value ||
        oldTherapy.getDosingDaysFrequency() !== therapy.getDosingDaysFrequency();

    /** Check preliminary #doseTimes state, if length has changed, pattern has inherently also changed */
    var doseTimesLengthChanged = tm.jquery.Utils.isEmpty(oldTherapy) ||
        !!oldTherapy.getDoseTimes() !== !!therapy.getDoseTimes() ||
        (!!therapy.getDoseTimes() &&
            !!oldTherapy.getDoseTimes() &&
            therapy.getDoseTimes().length !== oldTherapy.getDoseTimes().length);

    if (frequencyChanged || doseTimesLengthChanged)
    {
      return true;
    }
    else if (!therapy.isVariable())
    {
      for (var i = 0; i < therapy.getDoseTimes().length; i++)
      {
        if (therapy.getDoseTimes()[i].hour !== oldTherapy.getDoseTimes()[i].hour ||
            therapy.getDoseTimes()[i].minute !== oldTherapy.getDoseTimes()[i].minute)
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * Triggers the INTERVAL_CHANGE event.
   * @private
   */
  _signalChangedEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
      eventData: { start: this.getStart() }
    }), null);
  },

  _handleStartHourInputRestrictionChange: function()
  {
    if (!this.isRestrictedStartHourSelection())
    {
      this.isRendered() ? this.startHourCombo.hide() : this.startHourCombo.setHidden(true);
      this.isRendered() ? this.startHourField.show() : this.startHourField.setHidden(false);

      if (this._therapyDurationFieldsContainer && this._therapyDurationFieldsContainer.isHidden()) //previous frequency was 1ex
      {
        this.setEnd(null);
      }
    }
    else
    {
      this.isRendered() ? this.startHourField.hide() : this.startHourField.setHidden(true);
      this.isRendered() ? this.startHourCombo.show() : this.startHourCombo.setHidden(false);
    }
  },

  /**
   * Returns the endAmountField value with the possible occurrences of decimal symbols replaced to the
   * english format, which works with JS's parseFloat and jQuery's isNumeric method for further use
   * and input checks.
   * @returns {null|String}
   * @private
   */
  _getDeLocalizedEndAmountValue: function()
  {
    var fieldAmount = this.endAmountField.getValue();

    if (!fieldAmount) return null;

    var decimalSymbol = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");

    if (decimalSymbol !== ".") // sl
    {
      fieldAmount = fieldAmount.replaceAll(decimalSymbol, '.');
    }
    return fieldAmount;
  },

  /**
   * Sets minimal therapy end date and time
   * @param {Boolean} limitByStart
   * @private
   */
  _calculateAndSetMinEndDateTime: function(limitByStart)
  {
    if (limitByStart)
    {
      if (!this.endDateTimeField.isHidden() && this.getStart())
      {
        if (this.endDateTimeField.getDatePicker().getDate() &&
            this.startDateField.getDate() > this.endDateTimeField.getDatePicker().getDate())
        {
          this.endDateTimeField.getDatePicker().setDate(this.startDateField.getDate(), true);
        }
        this.endDateTimeField.getDatePicker().setMinDate(this.getStart());

        if (this.endDateTimeField.getDatePicker().getDate() &&
            this.endDateTimeField.getDatePicker().getDate().getTime() === this.startDateField.getDate().getTime())
        {
          this.endDateTimeField.getTimePicker().setMinTime(this.getStart(), true);
          if (this.endDateTimeField.getTimePicker().getTime() &&
              this.endDateTimeField.getTimePicker().getTime() < this.getStart())
          {
            this.endDateTimeField.getTimePicker().setTime(this.getStart(), true);
          }
        }
        else
        {
          this.endDateTimeField.getTimePicker().setMinTime(null, true);
        }
      }
    }
    else
    {
      this.endDateTimeField.getDatePicker().setMinDate(null);
      this.endDateTimeField.getTimePicker().setMinTime(null, true);
    }
  },

  _onEndDateTimeDatePickerChange: function()
  {
    if (this.getStart())
    {
      this._calculateAndSetMinEndDateTime(true);
      if (this.endDateTimeField.getTimePicker().getTime())
      {
        this._signalChangedEvent();
      }
    }
  },

  /**
   * Handler for {@link #whenNeededButton}. If a dosing pattern is present, restricts therapy start selection to pattern
   * times. Otherwise, any time is selectable, and current time is preset.
   * If {@link #whenNeededButton} is pressed, depresses {@link #byDoctorsOrderButton}, as the combination of both is
   * unavailable.
   * Ensures correct {@link #maxDailyFrequencyField} visibility and signals change event.
   * @private
   */
  _onWhenNeededChange: function()
  {
    if (this.getWhenNeeded())
    {
      if (this.isByDoctorsOrderAvailable())
      {
        this.byDoctorsOrderButton.setPressed(false);
      }
      if (!!this.getDosingPatternFunction && this.getDosingPatternFunction().length === 0)
      {
        this.setRestrictedStartHourSelection(false);
        this.startDateField.setDate(CurrentTime.get());
        this.startHourField.setTime(CurrentTime.get());
      }
    }
    else
    {
      this.setRestrictedStartHourSelection(true);
    }
    this.setMaxDailyFrequencyFieldVisible(this.getWhenNeeded());
    this._signalChangedEvent();
  },

  /**
   * If when needed is selected and no pattern is present, the therapy start is selectable via {@link #startHourField}.
   * If the pattern is present, the therapy start should be selected from the pattern presented on {@link #startHourCombo}.
   * @private
   */
  _configureStartHourRestrictionForWhenNeededDoses: function()
  {
    if (!!this.getDosingPatternFunction && this.getWhenNeeded())
    {
      if (this.getDosingPatternFunction().length > 0)
      {
        this.setRestrictedStartHourSelection(true);
      }
      else
      {
        this.setRestrictedStartHourSelection(false);
      }
    }
  },

  /**
   * Clears the checkbox and can be called safely regardless of the conditional presence or render state.
   * @param {boolean} visible
   * @private
   */
  _applyRecordAdministrationCheckBoxVisibility: function(visible)
  {
    if (!this._recordAdministrationCheckBox)
    {
      return;
    }

    if (visible)
    {
      this._recordAdministrationCheckBox.isRendered() ?
          this._recordAdministrationCheckBox.show() :
          this._recordAdministrationCheckBox.setHidden(false);
    }
    else
    {
      this._recordAdministrationCheckBox.isRendered() ?
          this._recordAdministrationCheckBox.hide() :
          this._recordAdministrationCheckBox.setHidden(true);
      this._recordAdministrationCheckBox.setChecked(false, true);
    }
  },

  clear: function()
  {
    var presetDate = this.getView().getPresetDate();
    this.startDateField.setDate(presetDate);
    this.startHourField.setTime(presetDate);
    this.endAmountField.setValue(null);
    this.endDateTimeText.setHtml(null);
    this.endAmountFieldUnitId = null;
    if (this.isWhenNeededSupported())
    {
      this.whenNeededButton.setPressed(false);
    }
    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton.setPressed(false);
      this.byDoctorsOrderButton.setEnabled(true);
    }
    this.setMaxDailyFrequencyFieldVisible(false);
    this.setEnd(null);
    this.startHourCombo.removeAllOptions();
    this.showWhenNeededDoctorsOrder();
    this.setStartHourEnabled(true);
    this.setTherapyEndEnabled(true);
    this._applyRecordAdministrationCheckBoxVisibility(false);
  },

  /**
   * @return {boolean}
   */
  isRecordAdministration: function()
  {
    return !!this._recordAdministrationCheckBox &&
        !this._recordAdministrationCheckBox.isHidden() &&
        this._recordAdministrationCheckBox.isChecked();
  },

  /**
   * Applies the maximum frequency field visibility, if the given functionality is available. The requested
   * visibility is also determined by the dosing frequency (requires 'hours') and selecting the 'when needed' condition.
   * Otherwise the field value is reset and the field will remain hidden.
   * @param show
   */
  setMaxDailyFrequencyFieldVisible: function(show)
  {
    if (!this.isMaxDailyFrequencyAvailable() || !this.isWhenNeededSupported())
    {
      return;
    }

    var setVisible = show && this.whenNeededButton.isPressed() && this.getFrequencyDataFunction().frequencyMode === 'HOURS';
    if (setVisible)
    {
      this._maxFrequencyContainer.show();
    }
    else
    {
      this.maxDailyFrequencyField.setValue(null);
      this._maxFrequencyContainer.hide();
    }
  },

  getStart: function()
  {
    if (!this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      return null;
    }

    var date = this.startDateField.getDate();
    var time = null;
    if (!this.startHourField.isHidden())
    {
      time = this.startHourField.getTime();
    }
    else
    {
      var hourMinute = this.startHourCombo.getSelections()[0];
      time = app.views.medications.MedicationTimingUtils.hourMinuteToDate(hourMinute);
    }
    if (date && time)
    {
      return new Date(date.getFullYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), 0, 0);
    }
    return null;
  },

  setStart: function(start, suppressChangeEvent)
  {
    var startDate = new Date(start);
    this.startDateField.setDate(start ? startDate : null, suppressChangeEvent === true);
    if (!this.startHourField.isHidden())
    {
      this.startHourField.setTime(start ? startDate : null, suppressChangeEvent === true);
    }
    else
    {
      var selections = start ? [{hour: startDate.getHours(), minute: startDate.getMinutes()}] : [];
      this.startHourCombo.setSelections(selections, suppressChangeEvent === true);
    }
  },

  /**
   * If the {@link #startHourField} is visible, adjusts the start time to 1 minute in the future, if the desired start is
   * older than the order time. If no order time is provided, the current time is used. This adjustment is
   * required when editing an existing order right after it was created, because we're operating with time
   * on a minute precision and want to prevent starting a therapy before it was actually created.
   */
  adjustStartTimeToOrderTime: function(orderTime, preventEvent)
  {
    orderTime = tm.jquery.Utils.isDate(orderTime) ? orderTime : CurrentTime.get();
    if (this.getOrderingBehaviour().isStartEndTimeAvailable() && !this.startHourField.isHidden() &&
        this.getStart() < orderTime)
    {
      this.setStart(moment(orderTime).add(1, 'minutes'), preventEvent);
    }
  },

  getEnd: function()
  {
    if (this.fixedEndDate)
    {
      return this.fixedEndDate;
    }

    if (!this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      return null;
    }

    //end defined as duration in days
    if (!this.endAmountField.isHidden())
    {
      var fixedAmountValue = this._getDeLocalizedEndAmountValue();
      if (fixedAmountValue && tm.jquery.Utils.isNumeric(fixedAmountValue) && fixedAmountValue > 0)
      {
        var fixedAmountType = this.durationUnitOfMeasureSelectBox.getSelections()[0];
        var end = this.getStart();

        if (end && !tm.jquery.Utils.isEmpty(fixedAmountType))
        {
          return fixedAmountType.appendToInputValue(end, fixedAmountValue);
        }
      }
      return null;
    }

    //end defined as date
    var endDate = this.endDateTimeField.getDate();
    if (!tm.jquery.Utils.isEmpty(endDate))
    {
      return new Date(endDate.valueOf());
    }
    return null;
  },

  /**
   * @param {Date|null|undefined} end
   * @param {Boolean} [suppressChangeEvent=false]
   */
  setEnd: function(end, suppressChangeEvent)
  {
    this.fixedEndDate = end ? new Date(end.getTime()) : null;
    this.endDateTimeField.setDate(null, suppressChangeEvent);
    this.endAmountField.setValue(null, suppressChangeEvent);
    this.endAmountFieldUnitId = null;

    if (this.fixedEndDate)
    {
      this._resetTherapyDurationInputFields(false);

      this.endDateTimeField.setDate(this.fixedEndDate, suppressChangeEvent);
      this.durationUnitOfMeasureSelectBox.setSelections(
          [{id: app.views.medications.TherapyEnums.therapyIntervalPaneSelectionIds.DATE}],
          suppressChangeEvent);
      if (suppressChangeEvent)
      {
        this._onDurationUnitOfMeasureValueChanged(this.durationUnitOfMeasureSelectBox.getSelections()[0]);
      }
      this.fixedEndDate = null;
    }
    else if (this.getOrderingBehaviour().isPastMode())
    {
      this.showDurationFieldsForDateType();
    }
    else
    {
      this._resetTherapyDurationInputFields(true);
    }

    var statDose = this._isStatDose();
    if (statDose && this._therapyDurationFieldsContainer)
    {
      this.isRendered() ? this._therapyDurationFieldsContainer.hide() : this._therapyDurationFieldsContainer.setHidden(true);
    }
    this._applyRecordAdministrationCheckBoxVisibility(statDose);
  },

  /**
   * @returns {number|null}
   */
  getMaxDailyFrequency: function()
  {
    if (this.isMaxDailyFrequencyAvailable() && tm.jquery.Utils.isNumeric(this.maxDailyFrequencyField.getValue()))
    {
      return this.maxDailyFrequencyField.getValue();
    }
    return null;
  },

  setMaxDailyFrequency: function(maxDailyFrequency)
  {
    if (this.isMaxDailyFrequencyAvailable())
    {
      this.maxDailyFrequencyField.setValue(maxDailyFrequency);
    }
  },

  setStartHourEnabled: function(enabled)
  {
    this.startHourField.setEnabled(enabled);
  },

  setStartOptionsFromPattern: function()
  {
    var self = this;
    if (this.getDosingPatternFunction)
    {
      this.startHourCombo.removeAllOptions();
      var dosingPattern = this.getDosingPatternFunction();

      if (dosingPattern && dosingPattern.length > 0)
      {
        var uniquePatterns = {};
        dosingPattern.forEach(function(item)
        {
          var key = (!tm.jquery.Utils.isEmpty(item.hour) ? (item.hour).toString() : "")
          + (!tm.jquery.Utils.isEmpty(item.minute) ? (item.minute).toString() : "");

          if (!uniquePatterns.hasOwnProperty(key))
          {
            uniquePatterns[key] = true;
            self.startHourCombo.addOption(tm.jquery.SelectBox.createOption(item));
          }
        });
      }
      this.startHourCombo.setSelections([], true);
    }
  },

  /**
   * Only viable for {@link app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX}.
   * @param {Boolean} value
   */
  setRecordAdministration: function(value)
  {
    if (!this._recordAdministrationCheckBox)
    {
      return;
    }

    if (value && this._recordAdministrationCheckBox.isHidden())
    {
      throw new Error('recordAdministration can only be set true for stat doses.');
    }

    this._recordAdministrationCheckBox.setChecked(value, true);
  },

  /**
   * Calling this method will recalculate and set {@link app.views.medications.common.dto.Therapy#start} - which usually
   * means to a new date either now or in the future.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [limitEndDate=true] Should the new start limit the therapy end date.
   * @param {app.views.medications.common.dto.Therapy} [oldTherapy=null] Should be set when editing an existing prescription.
   * @param {function} [callback=null]
   */
  calculateStart: function(therapy, limitEndDate, oldTherapy, callback)
  {
    limitEndDate = tm.jquery.Utils.isBoolean(limitEndDate) ? limitEndDate : true;

    var self = this;
    therapy.setEnd(null);
    if (this.getDosingPatternFunction)
    {
      this.setStartOptionsFromPattern();
      this._configureStartHourRestrictionForWhenNeededDoses();
      var patternMode = !this.startHourCombo.isHidden();
      if (patternMode)
      {
        if (!therapy.getDosingFrequency() && !therapy.isNormalVariableInfusion())
        {
          this.setStart(null);
        }
        else
        {
          if (therapy.isNormalVariableInfusion())
          {
            therapy.setTimedDoseElements([therapy.getTimedDoseElements()[0]]);
          }
          var newPrescriptionOrFrequencyChanged = this._frequencyOrPatternChanged(therapy, oldTherapy);
          if (!newPrescriptionOrFrequencyChanged)
          {
            therapy.setStart(oldTherapy.getStart());
          }
          else
          {
            var now = CurrentTime.get();
            therapy.setStart(new Date(
                now.getFullYear(),
                now.getMonth(),
                now.getDate(),
                now.getHours(),
                now.getMinutes(),
                0,
                0));
          }

          var moreThan24h =
              therapy.getDosingFrequency() &&
              therapy.getDosingFrequency().type === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES &&
              therapy.getDosingFrequency().value > 24;
          var daysInvolved = !tm.jquery.Utils.isEmpty(therapy.getDosingDaysFrequency()) || moreThan24h ||
              therapy.getDaysOfWeek().length > 0;

          if (!tm.jquery.Utils.isEmpty(oldTherapy) && newPrescriptionOrFrequencyChanged && daysInvolved)
          {
            self.setStart(null);
          }
          else
          {
            // When editing an existing prescription, the old therapy's uid is required to correctly determine the next safe
            // administration time(s) in cases when a previously scheduled (future) administration is given prematurely, by
            // looking at the actual recorded administrations.
            var nextAdministrationCalculationTherapy = tm.jquery.Utils.isEmpty(oldTherapy) ?
                therapy :
                therapy.clone(true)
                    .setCompositionUid(oldTherapy.getCompositionUid());

            this.view
                .getRestApi()
                .loadNextTherapyAdministrationTime(
                    nextAdministrationCalculationTherapy,
                    newPrescriptionOrFrequencyChanged,
                    true)
                .then(
                    function therapyIntervalPaneApplyNextAdministrationTime(nextTime)
                    {
                      // set the date first, but suppress the change event, since it's unreliable as it checks
                      // if the new value is the same as the old before it fires a change event
                      self.startDateField.setDate(tm.jquery.Utils.isDate(nextTime) ? nextTime : null, true);
                      self.startHourCombo.setSelections(
                          tm.jquery.Utils.isDate(nextTime) ?
                              [{hour: nextTime.getHours(), minute: nextTime.getMinutes()}] :
                              [],
                          true);
                      self._onEndAmountValueChanged();
                      self._calculateAndSetMinEndDateTime(limitEndDate);
                      self._signalChangedEvent();

                      if (callback)
                      {
                        callback();
                      }
                    });
          }
        }
      }
      else
      {
        // set the date first, but suppress the change event, since it's unreliable as it checks
        // if the new value is the same as the old before it fires a change event
        var startTime = app.views.medications.MedicationTimingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);
        this.startDateField.setDate(startTime, true);
        this.startHourField.setTime(startTime, true); // suppress the change event
        this._onEndAmountValueChanged();
        this._calculateAndSetMinEndDateTime(limitEndDate);
        if (callback)
        {
          callback();
        }
        self._signalChangedEvent();
      }
    }
  },

  calculateEnd: function()
  {
    var start = this.getStart();
    if (start)
    {
      if (this._isStatDose())
      {
        if (this.getDurationFunction)
        {
          var duration = this.getDurationFunction();
          if (duration)
          {
            start.setMinutes(Number(start.getMinutes()) + Number(duration));
            this.setEnd(start);
          }
          else
          {
            this.setEnd(start);
          }
        }
        else
        {
          this.setEnd(start);
        }
        if (this._therapyDurationFieldsContainer)
        {
          this._therapyDurationFieldsContainer.hide();
        }
      }
      else
      {
        if (this._therapyDurationFieldsContainer && this._therapyDurationFieldsContainer.isHidden()) //previous frequency was 1ex
        {
          this.setEnd(null);
        }
      }
    }
  },

  getWhenNeeded: function()
  {
    return this.isWhenNeededSupported() && !this.whenNeededButton.isHidden() ? this.whenNeededButton.isPressed() : false;
  },

  setWhenNeeded: function(whenNeeded)
  {
    if (this.isMaxDailyFrequencyAvailable())
    {
      this.setMaxDailyFrequencyFieldVisible(whenNeeded);
    }
    if (this.isWhenNeededSupported())
    {
      this.whenNeededButton.setPressed(whenNeeded);
    }
  },

  /**
   * @param {boolean} value
   */
  setByDoctorsOrderButtonEnabled: function(value)
  {
    if (this.isByDoctorsOrderAvailable())
    {
      this.byDoctorsOrderButton.setPressed(false);
      this.byDoctorsOrderButton.setEnabled(value);
    }
  },

  getStartCriterion: function()
  {
    if (this.isByDoctorsOrderAvailable() && !this.byDoctorsOrderButton.isHidden() && this.byDoctorsOrderButton.isPressed())
    {
      return this.byDoctorsOrderButton.data;
    }
    return null;
  },

  setStartCriterion: function(startCriterion)
  {
    if (this.isByDoctorsOrderAvailable() && startCriterion === this.byDoctorsOrderButton.data)
    {
      this.byDoctorsOrderButton.setPressed(true);
    }
  },

  getTherapyIntervalPaneValidations: function(startNotBefore)
  {
    var self = this;
    var formFields = [];
    var startTime = self.getStart();
    var endTime = self.getEnd();

    formFields.push(new tm.jquery.FormField({
      component: self,
      required: true,
      componentValueImplementationFn: function()
      {
        if (!startTime)
        {
          return null;
        }
        if (self.getOrderingBehaviour().isPastMode() && !endTime)
        {
          return null;
        }
        if (endTime && startTime > endTime)
        {
          return null;
        }
        if (self.endDateTimeField.getDatePicker().getDate() && !self.endDateTimeField.getTimePicker().getTime() ||
            !self.endDateTimeField.getDatePicker().getDate() && self.endDateTimeField.getTimePicker().getTime())
        {
          return null;
        }
        if (startNotBefore)
        {
          if (startTime < startNotBefore && !this.isCopyMode())
          {
            return null;
          }
        }
        return true;
      }
    }));
    formFields.push(new tm.jquery.FormField({
      component: self.endAmountField,
      required: false,
      componentValueImplementationFn: function()
      {
        return self._getDeLocalizedEndAmountValue();
      },
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary("field.value.is.invalid"),
            isValid: function (value)
            {
              return value ? tm.jquery.Utils.isNumeric(value) : true;
            }
          })
        ]
      }
    }));
    if (this.isMaxDailyFrequencyAvailable() && !this._maxFrequencyContainer.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.maxDailyFrequencyField,
        required: false,
        validation: {
          type: "local",
          validators: [
            new tm.jquery.Validator({
              errorMessage: this.getView().getDictionary("field.value.is.invalid"),
              isValid: function (value)
              {
                return !!value ? tm.jquery.Utils.isNumeric(value) && value > 0 : true;
              }
            })
          ]
        }
      }));
    }
    return formFields;
  },

  getDurationInputTypes: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var options = [];

    options.push(
        new tm.jquery.Object({
          id: enums.therapyIntervalPaneSelectionIds.UNTIL_CANCELED,
          title: view.getDictionary("until.cancellation.lc").toLowerCase()
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.HOURS,
          title: view.getDictionary("hours.accusative").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setMinutes(endDate.getMinutes() + parseFloat(inputAmount) * 60);
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.DAYS,
          title: view.getDictionary("days").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + parseInt(inputAmount));
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.WEEKS,
          title: view.getDictionary("weeks").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + (parseInt(inputAmount) * 7));
            return endDate;
          }
        }),
        new app.views.medications.ordering.TherapyIntervalPaneDurationUnitType({
          id: enums.therapyIntervalPaneSelectionIds.MONTHS,
          title: view.getDictionary("month.plural.lc").toLowerCase(),
          appendToInputValue: function(toDate, inputAmount)
          {
            var endDate = new Date(toDate.valueOf());
            endDate.setDate(endDate.getDate() + (parseInt(inputAmount) * 30));
            return endDate;
          }
        }),
        new tm.jquery.Object({
          id: enums.therapyIntervalPaneSelectionIds.DATE,
          title: view.getDictionary("select.date").toLowerCase()
        })
    );
    return options;
  },

  /**
   * @param {boolean} value
   */
  setRestrictedStartHourSelection: function(value)
  {
    this.restrictedStartHourSelection = value;
    this._handleStartHourInputRestrictionChange();
  },

  /**
   * @returns {boolean}
   */
  isRestrictedStartHourSelection: function()
  {
    return this.restrictedStartHourSelection === true;
  },

  requestFocus: function()
  {
    this.startDateField.focus();
  },

  showDurationFieldsForValueType: function()
  {
    this.isRendered() ? this.endDateTimeField.hide() : this.endDateTimeField.setHidden(true);
    this.isRendered() ? this.endAmountField.show() : this.endAmountField.setHidden(false);
    // make sure durationUnitOfMeasureSelectBox it's displayed - costs nothing
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    this.isRendered() ? this.endDateTimeText.show() : this.endDateTimeText.setHidden(false);
    if (this._therapyDurationFieldsContainer)
    {
      // make sure it's displayed - costs nothing
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
  },

  setMinEnd: function(therapyEnd)
  {
    this.endDateTimeField.getDatePicker().setMinDate(therapyEnd);
  },

  /**
   * Limits the earliest selectable time on the time and date picker for start therapy to the provided time. This will only
   * limit the {@link startDateField} and {@link startHourField}, but will not limit the {@link startHourCombo}.
   * @param therapyStart
   */
  limitMinStartDateTimePickers: function(therapyStart)
  {
    this.startDateField.setMinDate(therapyStart);
    this.startHourField.setMinTime(therapyStart);
  },

  showDurationFieldsForDateType: function()
  {
    this.isRendered() ? this.endAmountField.hide() : this.endAmountField.setHidden(true);
    this.isRendered() ? this.endDateTimeText.hide() : this.endDateTimeText.setHidden(true);
    this.isRendered() ? this.endDateTimeField.show() : this.endDateTimeField.setHidden(false);
    this._calculateAndSetMinEndDateTime(true);
    // make sure durationUnitOfMeasureSelectBox it's displayed - costs nothing
    this.isRendered() ? this.durationUnitOfMeasureSelectBox.show() : this.durationUnitOfMeasureSelectBox.setHidden(false);
    if (this._therapyDurationFieldsContainer)
    {
      // make sure it's displayed - costs nothing
      this.isRendered() ?
          this._therapyDurationFieldsContainer.show() :
          this._therapyDurationFieldsContainer.setHidden(false);
    }
  },

  hideWhenNeededDoctorsOrder: function()
  {
    if (this.isWhenNeededSupported())
    {
      this.isRendered() ? this.whenNeededButton.hide() : this.whenNeededButton.setHidden(false);
    }
    if (this.isByDoctorsOrderAvailable())
    {
      this.isRendered() ?  this.byDoctorsOrderButton.hide() : this.byDoctorsOrderButton.setHidden(true);
    }
  },

  showWhenNeededDoctorsOrder: function()
  {
    if (this.isWhenNeededSupported() && this.whenNeededButton.isRendered())
    {
      this.whenNeededButton.show();
    }
    if (this.isByDoctorsOrderAvailable() && this.byDoctorsOrderButton.isRendered())
    {
      this.byDoctorsOrderButton.show();
    }
  },

  /**
   * @returns {boolean}
   */
  isPresetCurrentTime: function()
  {
    return this.presetCurrentTime === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function ()
  {
    return this.copyMode === true;
  },

  /**
   * @returns {boolean}
   */
  isByDoctorsOrderAvailable: function()
  {
    return this.isByDoctorsOrderSupported() && this.getOrderingBehaviour().isDoctorsOrderAvailable();
  },

  /**
   * @returns {boolean}
   */
  isByDoctorsOrderSupported: function()
  {
    return this.byDoctorsOrderSupported === true;
  },

  /**
   * @returns {boolean}
   */
  isWhenNeededSupported: function()
  {
    return this.whenNeededSupported === true;
  },

  /**
   * @returns {boolean}
   */
  isMaxDailyFrequencyAvailable: function()
  {
    return this.maxDailyFrequencyAvailable === true;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  setTherapyEndEnabled: function(available)
  {
    this.endDateTimeField.getDatePicker().setEnabled(available);
    this.endDateTimeField.getTimePicker().setEnabled(available);
    this.durationUnitOfMeasureSelectBox.setEnabled(available);
  },

  /**
   * @Override to include all the time and date picker plugins into the deep render check.
   */
  isRendered: function(deep)
  {
    var rendered = this.callSuper(deep);
    if (deep === true)
    {
      rendered = rendered && !!this.startDateField.getPlugin() && !!this.startHourField.getPlugin() &&
          !!this.endDateTimeField.getDatePicker().getPlugin() && !!this.endDateTimeField.getTimePicker().getPlugin();
    }
    return rendered;
  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  }
});
Class.define('app.views.medications.ordering.TherapyIntervalPaneDurationUnitType', 'tm.jquery.Object', {
  id: null,
  title: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Clones the given date to ensure further changes don't get applied to the original value.
   * @param {Date} toDate
   * @return {Date}
   */
  appendToInputValue: function(toDate)
  {
    // implement the conversion function, make a copy of toDate first
    return new Date(toDate.valueOf());
  },

  getTitle: function()
  {
    return this.title;
  },
  getId: function()
  {
    return this.id;
  }
});

