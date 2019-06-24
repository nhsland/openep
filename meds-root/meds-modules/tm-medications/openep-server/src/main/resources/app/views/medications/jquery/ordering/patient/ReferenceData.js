Class.define('app.views.medications.ordering.patient.ReferenceData', 'app.views.medications.common.patient.AbstractReferenceData', {
  view: null,
  weight: null,
  height: null,
  dateOfBirth: null,

  /**
   * The purpose of this class is to provide static values for dosage and rate calculations.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  }
});