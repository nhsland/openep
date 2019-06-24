Class.define('app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory', 'tm.jquery.Object', {
  /**
   * Interface like jsClass that defines a factory used to create the dialog by which the user may enter a variable dose for
   * a prescription being made with the simple order form. Should be used as a base class and nothing else.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper();
  },

  /**
   * @param {app.views.medications.ordering.SimpleTherapyContainer} orderForm
   * @param {boolean} variableDays
   * @param {Array<Object>} timedDoseElements
   * @param {boolean} untilCanceled
   * @param {Date|undefined|null} endDate
   * @param {function(resultData: object)} resultCallback
   * @return {app.views.common.dialog.AppDialog}
   */
  create: function(orderForm, variableDays, timedDoseElements, untilCanceled, endDate, resultCallback)
  {

  }
});