Class.define('app.views.medications.ordering.PrescriptionContentExtensionContainer', 'tm.jquery.Container', {
  view: null,
  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * Override with required logic of setting component values from an existing therapy (on edit).
   * @param {app.views.medications.common.dto.Therapy} therapy
   */
  setValues: function(therapy)
  {

  },

  /**
   * Override to attach values on a therapy constructed by the order form.
   * @param {Object} config
   */
  attachTherapyConfig: function (config)
  {

  },

  /**
   * Override if required - will be executed when the primary medication changes. Keep in mind that
   * a render event can happen on the parent right after this call, so if you are loading any data
   * via a yielded Ajax, make sure you yield via a setTimeout or create a conditional task before
   * trying to change any UI values in the DOM. Otherwise bad things can happen (values won't be visible in the DOM).
   * @param medicationData
   */
  setMedicationData: function(medicationData) {

  },

  /**
   * Override with required component validations.
   * @return {Array<tm.jquery.FormField>}
   */
  getFormValidations: function()
  {
    return [];
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /* implement the response to the change of number of medications that build the therapy */
  onMedicationsCountChange: function(numberOfMedications)
  {
  },

  /**
   * Override and implement the required content clearing.
   */
  clear: function()
  {

  }
});