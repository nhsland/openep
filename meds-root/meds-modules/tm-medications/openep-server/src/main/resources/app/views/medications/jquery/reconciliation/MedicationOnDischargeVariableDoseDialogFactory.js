// noinspection JSUnusedLocalSymbols
Class.define('app.views.medications.reconciliation.MedicationOnDischargeVariableDoseDialogFactory', 'app.views.medications.ordering.dosing.DefaultVariableDoseDialogFactory', {
  switchModeButtonTextDictionaryKey: 'discharge.protocol',
  /**
   * The default implementation of the variable dose factory used by our simple order form, permitting the user to either
   * enter a variable dose or a complex protocol for the dosing (which varies by days).
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper();
  },

  /**
   * @override and changed the default protocol dialog content with the discharge protocol content.
   * @param {app.views.medications.ordering.SimpleTherapyContainer} orderForm
   * @param {Array<Object>} timedDoseElements
   * @param {boolean} untilCanceled
   * @param {Date|undefined|null} endDate
   * @return {{content: app.views.medications.ordering.VariableDoseDaysDataEntryContainer, height: number, width: number}}
   */
  buildVariableDaysDialogDetails: function(orderForm, timedDoseElements, untilCanceled, endDate)
  {
    return {
      content: new app.views.medications.reconciliation.DischargeVariableDoseDaysDataEntryContainer({
        orderingBehaviour: orderForm.getOrderingBehaviour(),
        referenceData: orderForm.getReferenceData(),
        view: orderForm.getView(),
        startProcessOnEnter: true,
        height: 180,
        // clone medication data before passing it to variable dose pane (see TMC-13807)
        medicationData: orderForm.getMedicationData().clone(),
        timedDoseElements: timedDoseElements
      }),
      height: 500,
      width: 650
    }
  }
});