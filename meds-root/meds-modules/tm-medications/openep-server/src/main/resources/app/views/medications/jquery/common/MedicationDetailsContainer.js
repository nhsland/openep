Class.define('app.views.medications.common.MedicationDetailsContainer', 'tm.jquery.Container', {
  cls: "medication-details-container",
  scrollable: "vertical",
  /** config */
  view: this.view,
  medicationData: null,
  selectedRoute: null, /* optional: set if route selection possible */

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    var self = this;
    this.medicationData.forEach(function(medicationData)
    {
      if (!medicationData.getMedication().isMedicationUniversal())
      {
        var infoContainer = new app.views.medications.therapy.MedicationDetailsCardPane({
          view: self.view,
          medicationData: medicationData,
          selectedRoute: self.selectedRoute ? self.selectedRoute : medicationData.defaultRoute
        });
        self.add(infoContainer);
      }
    });
  }
});