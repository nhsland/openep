Class.define('app.views.medications.ordering.dosing.DosingPatternPane', 'tm.jquery.Container', {
  cls: "dosing-pattern-pane",
    /** @type app.views.common.AppView */
  view: null,
  /** @type function */
  getFrequencyKeyFunction: null,
  /** @type function */
  getFrequencyTypeFunction: null,
  /** @type function */
  patternChangedEvent: null,

  /** @type number|undefined */
  _eventFireTimer: undefined,
  /** @type number|undefined */
  _hideTooltipTimer: null,
  /** @type Array<tm.jquery.TimePicker> */
  _timeFields: null,
  /** @type tm.jquery.Form */
  validationForm: null,
  /** @type app.views.medications.common.DosingPatternValidator */
  _dosingPatternValidator: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var layout = tm.jquery.HFlexboxLayout.create('flex-start', "center", 0);
    layout.setFlexFlow(tm.jquery.flexbox.FlexFlow.create("row", "wrap"));
    this.setLayout(layout);

    this._dosingPatternValidator = new app.views.medications.common.DosingPatternValidator({
      view: this.view
    });

    this._timeFields = [];

    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
      },
      onValidationError: function()
      {
        setTimeout(function()
        {
          self.getTooltip().show();
        }, 0);
      }
    });
  },

  /** private methods */
  _presentPattern: function(administrationTimes)
  {
    administrationTimes = tm.jquery.Utils.isEmpty(administrationTimes) ? [] : administrationTimes;

    $(this.getDom()).removeClass("form-field-validationError");
    this.setTooltip(null);
    administrationTimes.length > 0 ? this.show() : this.hide();
    this.removeAll();
    this._timeFields.length = 0;

    var self = this;
    var frequencyType = this.getFrequencyTypeFunction();
    var dosingFrequencyTypeEnum = app.views.medications.TherapyEnums.dosingFrequencyTypeEnum;

    if (frequencyType === dosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      var asapButton = new tm.jquery.Button({
        cls: 'asap-btn',
        height: 22,
        margin: '0 5 0 5',
        text: this.view.getDictionary("asap"),
        handler: function()
        {
          if (self._timeFields.length > 0)
          {
            self._timeFields[0].setTime(
                app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForAsap(frequencyType));
          }
        }
      });
      var asapButtonWrapper = new tm.jquery.Container();
      asapButtonWrapper.add(asapButton);
      this.add(asapButtonWrapper);
    }

    administrationTimes.forEach(
        function(hourMinute, index)
        {
          var time = app.views.medications.MedicationTimingUtils.hourMinuteToDate(hourMinute);
          var enabled = index === 0 || frequencyType === dosingFrequencyTypeEnum.DAILY_COUNT;

          var timeField = this._buildNewTimePicker(time, enabled, frequencyType);
          this._timeFields.push(timeField);

          var timeFieldContainer = new tm.jquery.Container({
            cls: "time-field-container",
            layout: tm.jquery.VFlexboxLayout.create("center", "center")
          });
          if (enabled)
          {
            var arrowContainer = new tm.jquery.Container({
              cls: "arrow-container",
              width: 0,
              height: 0,
              alignSelf: "flex-start"
            });
            timeFieldContainer.add(arrowContainer);
          }
          timeFieldContainer.add(timeField);
          this.add(timeFieldContainer);
        },
        this);

    if (this.isRendered())
    {
      this.repaint();
    }
  },

  /**
   * @param {Date} time
   * @param {boolean} enabled
   * @param {string} frequencyType of {@link app.views.medications.TherapyEnums.dosingFrequencyTypeEnum}
   * @return {tm.jquery.TimePicker}
   * @private
   */
  _buildNewTimePicker: function(time, enabled, frequencyType)
  {
    var self = this;
    var timeField = new tm.jquery.TimePicker({
      cls: "time-field",
      time: time,
      showType: "focus",
      enabled: enabled,
      width: 47,
      initialValue: time,
      nowButton: {
        text: this.view.getDictionary("asap")
      },
      currentTimeProvider: function()
      {
        return app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForAsap(frequencyType);
      }
    });
    timeField.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        function onDosePatternTimeFieldChange(component)
        {
          if (frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
          {
            if (component.isEnabled() && frequencyType)
            {
              self._recalculateTimesForBetweenDoses();
            }
          }
          self._fireSingleChangeEvent();
        });
    timeField.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED,
        function onDosePatternTimeFieldFocusGained()
        {
          clearTimeout(self._hideTooltipTimer);
          if (self.getTooltip() && !self.getTooltip().isShowed())
          {
            self.getTooltip().hideOnDocumentClickHandler = false;
            self.getTooltip().show();
          }
        });
    timeField.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST,
        function onDosePatternTimeFieldFocusLost()
        {
          clearTimeout(self._hideTooltipTimer);
          self._hideTooltipTimer = setTimeout(
              function()
              {
                if (self.getTooltip()) self.getTooltip().hide();
              },
              100);
        });
    return timeField;
  },

  _recalculateTimesForBetweenDoses: function()
  {
    var frequencyKey = this.getFrequencyKeyFunction();
    var betweenDoses = frequencyKey.substring(0, frequencyKey.length - 1);
    if (this._timeFields.length > 0)
    {
      var firstTime = this._timeFields[0].getTime();
      if (firstTime)
      {
        for (var i = 1; i < this._timeFields.length; i++)
        {
          this._timeFields[i].setTime(new Date(firstTime.getTime() + i * betweenDoses * 60 * 60 * 1000), true);
        }
      }
    }
  },

  _fireSingleChangeEvent: function()
  {
    var self = this;
    clearTimeout(this._eventFireTimer);
    this._eventFireTimer = setTimeout(
        function()
        {
          self.patternChangedEvent();
          self._validate();
        },
        100);
  },

  _validate: function()
  {
    this._validationForm.addFormField(this.getDosingPatternPaneValidation());
    this._validationForm.submit();
  },

  /**
   * @private
   * @returns {Array<Date>} times
   */
  _getTimes: function()
  {
    return this._timeFields.map(function mapTimeFieldsToValue(timeField)
    {
      return timeField.getTime()
    });
  },

  /**
   * @return {Array<{hour: number, minute: number}>}
   */
  getDosingPattern: function()
  {
    var hourMinutes = [];

    this._timeFields.forEach(function(timeField)
    {
      var value = timeField.getPlugin() != null ? timeField.getTime() : timeField.initialValue;
      if (!tm.jquery.Utils.isEmpty(value))
      {
        hourMinutes.push({
          hour: value.getHours(),
          minute: value.getMinutes()
        });
      }
    });
    return hourMinutes;
  },

  /**
   * @return {tm.jquery.FormField}
   */
  getDosingPatternPaneValidation: function()
  {
    return this._dosingPatternValidator.getDosingPatternValidation(this, this._getTimes());
  },

  /**
   * @param {Number} duration in minutes
   * @returns {tm.jquery.FormField}
   */
  getPatternDurationValidation: function(duration)
  {
    return this._dosingPatternValidator.getPatternDurationValidation(this, this._getTimes(), duration);
  },

  /**
   * @param {Array<{hour: number, minute: number}>} pattern
   */
  setDosingPattern: function(pattern)
  {
    this._presentPattern(pattern);
  },

  refreshDosingPattern: function()
  {
    var administrationTiming = this.view.getAdministrationTiming();
    var frequencyKey = this.getFrequencyKeyFunction();
    var frequencyType = this.getFrequencyTypeFunction();
    var pattern = app.views.medications.MedicationTimingUtils.getFrequencyTimingPattern(
        administrationTiming,
        frequencyKey,
        frequencyType);
    this._presentPattern(pattern);
  },

  clear: function()
  {
    this._presentPattern([]);
    this.hide();
  },

  /**
   * @override
   * @param {tm.jquery.Tooltip|null} tooltip
   */
  setTooltip: function(tooltip)
  {
    if (tooltip)
    {
      tooltip.setTrigger('manual');
      tooltip.hideOnDocumentClickHandler = false;
    }
    this.callSuper(tooltip);
  }
});