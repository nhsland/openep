Class.define('app.views.medications.common.patient.ViewBasedReferenceData', 'app.views.medications.common.patient.AbstractReferenceData', {
  view: null,

  /**
   * The purpose of this class is to provide {@link tm.views.medications.TherapyView} based reference data for dose and
   * rate calculations.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   *
   * @return {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @Override
   * @return {number|undefined|null}
   */
  getWeight: function()
  {
    return this.getView().getReferenceWeight();
  },

  /**
   * @override
   * @return {number|undefined|null}
   */
  getHeight: function()
  {
    return this.getView().getPatientHeightInCm();
  },

  /**
   * @override
   * @return {Date|undefined|null}
   */
  getDateOfBirth: function()
  {
    return !!this.getView().getPatientData() ? this.getView().getPatientData().getBirthDate() : undefined;
  },

  /**
   * @return {Date|null} the date when the reference weight was set or updated.
   */
  getWeightDate: function()
  {
    return this.getView().getReferenceWeightDate();
  }
});