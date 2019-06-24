Class.define('app.views.medications.common.dto.PrescriptionTherapy', 'tm.jquery.Object', {
  prescriptionTherapyId: null,
  prescriptionStatus: null,
  therapy: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   *  @param {app.views.medications.common.dto.Therapy} value
   */
  setTherapy: function(value)
  {
    return this.therapy = value;
  },

  /**
   * @return {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  }
});