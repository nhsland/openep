/**
 * Represents the confirmation event data, issued by all three order forms in conjunction with the
 * {@link app.views.medications.ordering.OrderingContainer} as the user confirms the input of the current therapy order.
 */
Class.define('app.views.medications.ordering.ConfirmOrderEventData', 'tm.jquery.Object', {
  /** @type boolean */
  validationPassed: true,
  /** @type app.views.medications.ordering.AbstractTherapyOrder */
  therapyOrder: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {boolean}
   */
  isValidationPassed: function()
  {
    return this.validationPassed === true;
  },

  /**
   *
   * @return {app.views.medications.ordering.AbstractTherapyOrder}
   */
  getTherapyOrder: function()
  {
    return this.therapyOrder;
  }
});