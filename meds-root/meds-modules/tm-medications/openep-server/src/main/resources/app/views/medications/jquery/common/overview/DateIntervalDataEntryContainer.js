Class.define('app.views.medications.common.overview.DateIntervalDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'date-interval-data-entry-container',

  defaultHeight: 150,
  defaultWidth: 240,
  view: null,
  startProcessOnEnter: true,

  defaultDuration: 7, /* in days */
  maximumDuration: null, /* optional, in days */

  _startDateField: null,
  _endDateField: null,
  _validationForm: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._configureForm();
  },

  /**
   * @return {number} of days for the default duration of the interval. The value does not get updated with user
   * interaction!
   */
  getDefaultDuration: function()
  {
    return this.defaultDuration;
  },

  /**
   * @return {number|null} representing the maximum duration in days, that can be selected.
   */
  getMaximumDuration: function()
  {
    return this.maximumDuration;
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this._validationForm;
    var failResultData = new app.views.common.AppResultData({success: false, value: null});

    form.setOnValidationSuccess(function()
    {
      var successResultData = new app.views.common.AppResultData({
        success: true,
        value: {
          startDate: self.getStartDateField().getDate(),
          endDate: self.getEndDateField().getDate()
        }
      });
      resultDataCallback(successResultData);
    });
    form.setOnValidationError(function()
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  },

  /**
   * @returns {number}
   */
  getDefaultHeight: function()
  {
    return this.defaultHeight;
  },

  /**
   * @returns {number}
   */
  getDefaultWidth: function()
  {
    return this.defaultWidth;
  },

  /**
   * @returns {tm.jquery.DatePicker}
   */
  getStartDateField: function()
  {
    return this._startDateField;
  },

  /**
   * @returns {tm.jquery.DatePicker}
   */
  getEndDateField: function()
  {
    return this._endDateField;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    var startDate = CurrentTime.get();
    startDate.setDate(startDate.getDate() - this.getDefaultDuration());

    this._startDateField = new tm.jquery.DatePicker({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "100px"),
      viewMode: "overlay",
      showType: "focus",
      date: startDate,
      maxDate: moment(CurrentTime.get()).subtract(1, 'days').toDate()
    });

    var spacerElement = new tm.jquery.Component({
      cls: 'TextData field-divider',
      html: '-'
    });

    this._endDateField = new tm.jquery.DatePicker({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "100px"),
      viewMode: "overlay",
      showType: "focus",
      date: CurrentTime.get(),
      maxDate: CurrentTime.get()
    });

    if (!!this.getMaximumDuration())
    {
      this._startDateField.on(
          tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
          this._adjustStartDateIfMaximumDurationExceeded.bind(this));
      this._endDateField.on(
          tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
          this._adjustStartDateIfMaximumDurationExceeded.bind(this));
    }

    this.add(this._startDateField);
    this.add(spacerElement);
    this.add(this._endDateField);
  },

  _configureForm: function()
  {
    var self = this;
    var view = this.getView();

    var form = new tm.jquery.Form({
      view: view,
      showTooltips: false,
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    form.addFormField(new tm.jquery.FormField({
      label: null, component: this.getStartDateField(), required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: view.getDictionary('starting.date.should.not.come.before.end.date'),
            isValid: function(value)
            {
              var endDate = self.getEndDateField().getDate();
              return value <= endDate;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate() ? component.getDate() : null;
      }
    }));
    form.addFormField(new tm.jquery.FormField({
      label: null, component: this.getEndDateField(), required: true,
      validation: {
        type: "local"
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate() ? component.getDate() : null;
      }
    }));

    this._validationForm = form;
  },

  /**
   * Change event handler for both time fields. Calculates the selected duration in days and adjusts the start date if it
   * exceeds the value of {@link #getMaximumDuration}.
   * @private
   */
  _adjustStartDateIfMaximumDurationExceeded: function()
  {
    if (!this._startDateField.getDate() || !this._endDateField.getDate())
    {
      return;
    }

    if (moment(this._endDateField.getDate()).diff(this._startDateField.getDate(), 'days') > this.getMaximumDuration())
    {
      this._startDateField.setDate(
          moment(this._endDateField.getDate()).subtract(this.getMaximumDuration(), 'days').toDate(),
          true);
    }
  }
});