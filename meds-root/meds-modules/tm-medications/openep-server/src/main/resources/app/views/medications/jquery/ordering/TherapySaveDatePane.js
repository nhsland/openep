Class.define('app.views.medications.ordering.TherapySaveDatePane', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_SAVE_DATE_CHANGE: new tm.jquery.event.EventType({
      name: 'therapySaveDateChange', delegateName: null
    })
  },
  cls: 'therapy-save-date-pane',

  /** configs */
  saveDateTime: null,
  /** privates */
  /** privates: components */
  saveDateField: null,
  saveTimeField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5));
    this._buildComponents();
    this._buildGui();
    this.registerEventTypes('app.views.medications.ordering.TherapySaveDatePane', [
      {eventType: app.views.medications.ordering.TherapySaveDatePane.EVENT_TYPE_SAVE_DATE_CHANGE}
    ]);
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var saveDateTime = this.saveDateTime ? this.saveDateTime : CurrentTime.get();
    this.saveDateField = new tm.jquery.DatePicker({
      cls: "save-date-field",
      date: saveDateTime,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.saveTimeField = new tm.jquery.TimePicker({
      cls: "save-time-field",
      time: saveDateTime,
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });

    this.saveDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._fireTherapySaveDateChange();
    });
    this.saveTimeField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._fireTherapySaveDateChange();
    })
  },

  _buildGui: function()
  {
    this.add(this.saveDateField);
    this.add(this.saveTimeField);
  },

  /**
   * @private
   */
  _fireTherapySaveDateChange: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.TherapySaveDatePane.EVENT_TYPE_SAVE_DATE_CHANGE,
      eventData: {
        therapyStartDate: this.getSaveDateTime()
      }
    }), null);
  },

  /** public methods */
  getSaveDateTime: function()
  {
    var saveDate = this.saveDateField.getDate();
    var saveTime = this.saveTimeField.getTime();
    return new Date(
        saveDate.getFullYear(),
        saveDate.getMonth(),
        saveDate.getDate(),
        saveTime.getHours(),
        saveTime.getMinutes(),
        0,
        0);
  }
});

