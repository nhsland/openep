Class.define('app.views.medications.ordering.VolumeSumPane', 'tm.jquery.Container', {
  cls: "volume-sum-pane",

  /** configs */
  view: null,
  adjustVolumesEvent: null,
  orderingBehaviour: null,
  /** privates */
  /** privates: components */
  adjustButton: null,
  volumeField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 5));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    if (this.getOrderingBehaviour().isDoseCalculationsAvailable())
    {
      this.adjustButton = new tm.jquery.Button({
        cls: 'update-icon adjust-volume-button',
        handler: function()
        {
          self.adjustVolumesEvent();
        }
      });
    }
    this.volumeField = app.views.medications.MedicationUtils.createNumberField('n2', 68, "volume-field");
    this.volumeField.setEnabled(false);
  },

  _buildGui: function()
  {
    var view = this.getView();
    if (!!this.adjustButton)
    {
      this.add(this.adjustButton);
    }
    this.add(this.volumeField);
    var unit = view.getUnitsHolder().findKnownUnitByName(app.views.medications.TherapyEnums.knownUnitType.ML);

    this.add(app.views.medications.MedicationUtils.crateLabel(
        'TextData',
        app.views.medications.MedicationUtils.getFormattedUnit(unit.getDisplayName(), view))
    );
  },

  /** public methods */

  getVolumeSum: function()
  {
    return this.volumeField.getValue();
  },

  setVolumeSum: function(volume)
  {
    this.volumeField.setValue(volume);
  },

  clear: function()
  {
    this.volumeField.setValue(null);
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  getView: function()
  {
    return this.view;
  }
});
