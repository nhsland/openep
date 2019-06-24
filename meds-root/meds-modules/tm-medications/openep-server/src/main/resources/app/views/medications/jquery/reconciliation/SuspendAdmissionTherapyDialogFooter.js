/**
 * A dialog footer container implementation that houses two confirmation buttons - one for the 'prescribe and suspend'
 * operation and the other one for the 'suspend until discharge' operation.
 */
Class.define('app.views.medications.reconciliation.SuspendAdmissionTherapyDialogFooter', 'app.views.common.containers.AppFooterButtonsContainer', {
  statics: {
    /**
     * Factory method for the wrapper function that handles enabling and disabling of the footer buttons while processing
     * the dialog results.
     * @see app.views.common.AppFactory#createDataEntryDialog
     * @param {app.views.medications.reconciliation.SuspendAdmissionTherapyDialogFooter} footer
     * @param {tm.jquery.Dialog} dialog
     * @param {function(app.views.common.AppResultData)} resultCallback
     * @return {function(app.views.common.AppResultData)}
     */
    createProcessDataCallbackWrapper: function(footer, dialog, resultCallback)
    {
      return function(resultData)
      {
        if (resultData instanceof app.views.common.AppResultData)
        {
          if (resultData.isSuccess())
          {
            resultCallback(resultData);
            footer.setEnabledForAllButtons(true);
            dialog.hide();
          }
          else
          {
            setTimeout(function()
            {
              footer.setEnabledForAllButtons(true);
            }, 500);
          }
        }
      };
    }
  },
  /** @ype string */
  untilDischargeButtonText: null,
  /** @ype string */
  prescribeButtonText: null,
  /** @type tm.jquery.Button */
  _untilDischargeButton: null,
  /** @type tm.jquery.Button */
  _prescribeButton: null,

  Constructor: function()
  {
    this.callSuper();

    this._untilDischargeButton = new tm.jquery.Button({
      text: this.untilDischargeButtonText,
      testAttribute: 'suspend-until-discharge-button'
    });
    this._prescribeButton = new tm.jquery.Button({
      text: this.prescribeButtonText,
      testAttribute: 'prescribe-and-suspend-button'
    });

    this.setRightButtons([this._untilDischargeButton, this._prescribeButton]);
  },

  /**
   * @return {tm.jquery.Button}
   */
  getUntilDischargeButton: function()
  {
    return this._untilDischargeButton;
  },

  /**
   * @return {tm.jquery.Button}
   */

  getPrescribeButton: function()
  {
    return this._prescribeButton;
  },

  /***
   * @param {boolean} enabled
   */
  setEnabledForAllButtons: function(enabled)
  {
    this.getRightButtons()
        .forEach(function setEnabledToButton(button)
        {
          button.setEnabled(enabled);
        });
  }
});