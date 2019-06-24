Class.define('app.views.medications.ordering.dosing.DefaultVariableDoseDialogFactory', 'app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory', {
  switchModeButtonTextDictionaryKey: 'protocol',

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
   * @return {string} the dictionary key used to retrieve the translated text for the mode switch button.
   * @protected
   */
  getSwitchModeButtonTextDictionaryKey: function()
  {
    return this.switchModeButtonTextDictionaryKey;
  },

  /**
   * @override implements the creation of a dialog that permits entering a variable dose or dose protocol.
   *
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
    this._ensureDefined(orderForm, 'orderForm');
    this._ensureDefined(timedDoseElements, 'timedDoseElements');

    variableDays = variableDays === true;
    untilCanceled = untilCanceled === true;
    endDate = endDate || null;

    // clone medication data before passing it to variable dose pane (TMC-13807)
    var appFactory = orderForm.getView().getAppFactory();
    var self = this;

    var dialogDetails = variableDays ?
        this.buildVariableDaysDialogDetails(
            orderForm,
            app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(timedDoseElements) ?
                timedDoseElements :
                null,
            untilCanceled,
            endDate) :
        this.buildVariableDoseDialogDetails(orderForm, timedDoseElements);

    var variableDoseEditDialog = appFactory.createDataEntryDialog(
        orderForm.getView().getDictionary('variable.dose'),
        null,
        dialogDetails.content,
        resultCallback,
        dialogDetails.width,
        dialogDetails.height
    );
    variableDoseEditDialog.addTestAttribute(variableDays ? 'variable-days-edit-dialog' : 'variable-dose-edit-dialog');
    var removeVarioButton = variableDoseEditDialog.getBody().footer.cancelButton;
    removeVarioButton.setText(orderForm.getView().getDictionary('remove.vario'));
    removeVarioButton.setType("link");
    var cancelButtonHandler = removeVarioButton.getHandler();
    removeVarioButton.setHandler(function()
    {
      orderForm.removeVariableDosage();
      cancelButtonHandler();
    });

    if (variableDays)
    {
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton]);
    }
    else
    {
      var switchToVariableDaysButton = new tm.jquery.Button({
        testAttribute: 'switch-to-protocol-button',
        text: orderForm.getView().getDictionary(this.getSwitchModeButtonTextDictionaryKey()),
        type: "link",
        handler: function()
        {
          variableDoseEditDialog.hide();
          self
              .create(orderForm, true, timedDoseElements, untilCanceled, endDate, resultCallback)
              .show();
        }
      });
      variableDoseEditDialog.getBody().footer.setLeftButtons([removeVarioButton, switchToVariableDaysButton]);
    }
    variableDoseEditDialog.getBody().footer.setRightButtons([variableDoseEditDialog.getBody().footer.confirmButton]);

    return variableDoseEditDialog;
  },

  /**
   * @param {app.views.medications.ordering.SimpleTherapyContainer} orderForm
   * @param {Array<Object>} timedDoseElements
   * @param {boolean} untilCanceled
   * @param {Date|undefined|null} endDate
   * @return {{content: app.views.medications.ordering.VariableDoseDaysDataEntryContainer, height: number, width: number}}
   * @protected
   */
  buildVariableDaysDialogDetails: function(orderForm, timedDoseElements, untilCanceled, endDate)
  {
      return {
        content: new app.views.medications.ordering.VariableDoseDaysDataEntryContainer({
          orderingBehaviour: orderForm.getOrderingBehaviour(),
          referenceData: orderForm.getReferenceData(),
          view: orderForm.getView(),
          startProcessOnEnter: true,
          padding: 10,
          height: 180,
          // clone medication data before passing it to variable dose pane (see TMC-13807)
          medicationData: orderForm.getMedicationData().clone(),
          timedDoseElements: timedDoseElements,
          frequency: orderForm.getDosingFrequency(),
          editMode: orderForm.isEditMode() && !orderForm.isCopyMode(),
          untilCanceled: untilCanceled,
          selectedDate: endDate
        }),
        height: 850,
        width: 950
      }
  },

  /**
   * @param {app.views.medications.ordering.SimpleTherapyContainer} orderForm
   * @param {Array<Object>} timedDoseElements
   * @return {{content: app.views.medications.ordering.VariableDoseDataEntryContainer, height: number, width: number}}
   * @protected
   */
  buildVariableDoseDialogDetails: function(orderForm, timedDoseElements)
  {
    return {
      content: new app.views.medications.ordering.VariableDoseDataEntryContainer({
        view: orderForm.getView(),
        orderingBehaviour: orderForm.getOrderingBehaviour(),
        referenceData: orderForm.getReferenceData(),
        startProcessOnEnter: true,
        padding: 10,
        // clone medication data before passing it to variable dose pane (see TMC-13807)
        medicationData: orderForm.getMedicationData().clone(),
        timedDoseElements: timedDoseElements,
        frequency: orderForm.getDosingFrequency(),
        addDosageCalculationPane: orderForm.isDosageCalculationPossible()
      }),
      height: 500,
      width: 650
    };
  },

  _ensureDefined: function(value, name)
  {
    if (!value)
    {
      throw new Error(name + ' not defined');
    }
  }
});