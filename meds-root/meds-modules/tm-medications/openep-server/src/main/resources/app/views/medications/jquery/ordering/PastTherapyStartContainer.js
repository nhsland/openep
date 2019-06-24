Class.define('app.views.medications.ordering.PastTherapyStartContainer', 'tm.jquery.Container', {
  cls: 'past-therapy-start-container',
  view: null,
  therapyIntervalPane: null,
  titleText: null,
  _dateTimePicker: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @param {Date|null} dateTime
   */
  setPastTherapyStart: function(dateTime)
  {
    this._dateTimePicker.setDate(dateTime);
  },

  /**
   * Only set time if date is selected and time is not.
   * @param {Date} therapyStart
   */
  setTimeOfTherapyStart: function(therapyStart)
  {
    if (!tm.jquery.Utils.isEmpty(this._dateTimePicker.getDatePicker().getDate()) &&
        tm.jquery.Utils.isEmpty(this._dateTimePicker.getTimePicker().getTime()))
    {
      this._dateTimePicker.getTimePicker().setTime(therapyStart);
    }
  },

  clear: function()
  {
    this._dateTimePicker.setDate(null);
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getValidators: function()
  {
    var self = this;
    var formFields = [];

    if (tm.jquery.Utils.isDate(this._dateTimePicker.getDatePicker().getDate()))
    {
      formFields.push(new tm.jquery.FormField({
        component: this._dateTimePicker.getTimePicker(),
        componentValueImplementationFn: function()
        {
          return self._dateTimePicker.getTimePicker().getTime();
        },
        required: true,
        validation: {
          type: 'local',
          validators: [getValidator()]
        }
      }));
    }

    if (tm.jquery.Utils.isDate(this._dateTimePicker.getTimePicker().getTime()))
    {
      formFields.push(new tm.jquery.FormField({
        component: this._dateTimePicker.getDatePicker(),
        componentValueImplementationFn: function()
        {
          return self._dateTimePicker.getDatePicker().getDate();
        },
        required: true,
        validation: {
          type: 'local',
          validators: [getValidator()]
        }
      }));
    }
    return formFields;

    /**
     * @returns {tm.jquery.Validator}
     */
    function getValidator()
    {
      return new tm.jquery.Validator({
        errorMessage: self.getView().getDictionary('date.and.time.must.be.defined'),
        isValid: function(value)
        {
          return tm.jquery.Utils.isDate(value);
        }
      })
    }
  },

  /**
   * @returns {Date|null}
   */
  getDateTime: function()
  {
    return this._dateTimePicker.getDate();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    var self = this;
    this._dateTimePicker = new tm.jquery.DateTimePicker({
      showType: 'focus'
    });
    this._dateTimePicker.getDatePicker().on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      if (!!self.therapyIntervalPane)
      {
        self._dateTimePicker.getTimePicker().setTime(self.therapyIntervalPane.getStart());
      }
    });

    var titledDateTimeContainer = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.titleText,
      contentComponent: this._dateTimePicker
    });
    this.add(titledDateTimeContainer);
  }
});