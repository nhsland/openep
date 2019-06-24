/**
 * Represents a small dialog by which the user can decide how he wishes to suspend a therapy that was identified during
 * the admission of the patient (medications on admission). The reason for the suspend action can also be entered and might
 * also be mandatory - depending on the server configuration.
 */
Class.define('app.views.medications.reconciliation.SuspendAdmissionTherapyEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    /**
     * Factory method to simplify creation of a new dialog.
     * @param {app.views.common.AppView} view
     * @param {boolean} prescribeEnabled enables or disables the 'prescribe and suspend' option
     * @param {function} resultCallback
     * @return {app.views.common.dialog.AppDialog}
     */
    asDialog: function(view, prescribeEnabled, resultCallback)
    {
      var content = new app.views.medications.reconciliation.SuspendAdmissionTherapyEntryContainer({
        view: view
      });

      var footer = new app.views.medications.reconciliation.SuspendAdmissionTherapyDialogFooter({
        untilDischargeButtonText: view.getDictionary('suspend.until.discharge'),
        prescribeButtonText: view.getDictionary('prescribe.and.suspend'),
        view: view
      });

      var suspendDialog = view.getAppFactory().createDefaultDialog(
          view.getDictionary("warning"),
          null,
          view.getAppFactory().createContentAndFooterButtonsContainer(content, footer),
          footer,
          content.defaultWidth,
          content.defaultHeight
      );

      var createCallbackWrapper =
          app.views.medications.reconciliation.SuspendAdmissionTherapyDialogFooter.createProcessDataCallbackWrapper;

      footer.getPrescribeButton().setHandler(
          function onPrescribeAndSuspendButtonClick()
          {
            footer.setEnabledForAllButtons(false);
            content.processResultData(createCallbackWrapper(footer, suspendDialog, resultCallback), true);
          }
      );
      footer.getPrescribeButton().setHidden(!prescribeEnabled);

      footer.getUntilDischargeButton().setHandler(
          function onSuspendUntilDischargeButtonClick()
          {
            footer.setEnabledForAllButtons(false);
            content.processResultData(createCallbackWrapper(footer, suspendDialog, resultCallback), false);
          }
      );

      return suspendDialog;
    }
  },
  padding: 10,
  cls: 'suspend-admission-dialog',
  /** @type number */
  defaultWidth: 460,
  /** @type number */
  defaultHeight: 205,
  /** @type app.views.common.AppView */
  view: null,
  /** @type tm.jquery.TextField|null */
  _suspendReasonField: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  /**
   * @param {function(app.views.common.AppResultData)} resultDataCallback
   * @param {boolean} prescribe which is set based on the requested action triggered by one of the footer buttons
   */
  processResultData: function(resultDataCallback, prescribe)
  {
    var self = this;
    var validationForm = this._createValidationForm();

    validationForm.onSubmit = function()
    {
      resultDataCallback(
          new app.views.common.AppResultData({
            success: !this.hasFormErrors(),
            value: {
              prescribe: prescribe === true,
              reason: self._suspendReasonField.getValue()
            }
          })
      );
    };
    validationForm.submit();
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var titleContainer = new app.views.medications.common.IconDescriptionContainer({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.view,
      iconImage: "warningYellow_status_48.png",
      description: this.view.getDictionary("additional.information.needed")
    });

    this.add(titleContainer);

    this._suspendReasonField = new tm.jquery.TextField();
    var suspendReasonContainer = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.view.getDictionary("reason"),
      contentComponent: this._suspendReasonField
    });
    this.add(suspendReasonContainer);
  },

  /**
   * @return {app.views.common.AppForm}
   * @private
   */
  _createValidationForm: function()
  {
    var validationForm = this.view.getAppFactory().createForm({
      name: 'suspend-reason-form'
    });

    if (this.view.isSuspendReasonMandatory())
    {
      validationForm.addFormField(
          new tm.jquery.form.FormField({
            name: 'suspend-reason-field',
            component: this._suspendReasonField,
            validation: new tm.jquery.form.FormFieldsValidation({
              type: "locale",
              validators: [
                app.views.medications.MedicationUtils.buildDefaultMinimumLengthStringValidator(this.view),
                new tm.jquery.RequiredValidator({errorMessage: this.view.getDictionary("field.value.is.required")})
              ],
              markers: {
                error: [
                  new tm.jquery.form.FormFieldValidationMarker()
                ]
              }
            })
          }));
    }

    return validationForm;
  }
});


