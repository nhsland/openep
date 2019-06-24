Class.define('app.views.medications.common.TherapyStatusChangeReasonDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    /**
     * Factory method to simplify creation of a new dialog.
     * @param {app.views.common.AppView} view
     * @param {string} selectedTherapyStatus of type {@link #app.views.medications.TherapyEnums.therapyStatusEnum}
     * @param {function} resultCallback
     * @returns {app.views.common.dialog.AppDialog}
     */
    asDialog: function(view, selectedTherapyStatus, resultCallback)
    {
      var content = new app.views.medications.common.TherapyStatusChangeReasonDataEntryContainer({
        view: view,
        selectedTherapyStatus: selectedTherapyStatus
      });

      return view.getAppFactory().createDataEntryDialog(
          view.getDictionary('warning'),
          null,
          content,
          resultCallback,
          460,
          205
      )
    }
  },
  /** @type number */
  padding: 10,
  /** @type app.views.common.AppView */
  view: null,
  /** @type String {@link #app.views.medications.TherapyEnums.therapyStatusEnum} */
  selectedTherapyStatus: null,
  /** @type tm.jquery.TextField */
  _therapyStatusChangeReasonField: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @param {function} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var therapyStatusChangeReasonField = this._therapyStatusChangeReasonField;

    var validationForm = this.view.getAppFactory().createForm({
      name: 'suspend-reason-form'
    });

    validationForm.addFormField(new tm.jquery.form.FormField({
      name: 'suspend-reason-field',
      component: therapyStatusChangeReasonField,
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

    validationForm.onSubmit = function()
    {
      if (this.hasFormErrors())
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
        return;
      }

      resultDataCallback(new app.views.common.AppResultData({
        success: true,
        value: {
          therapyChangeReason: therapyStatusChangeReasonField.getValue()
        }
      }));
    };
    validationForm.submit();
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    this.add(new app.views.medications.common.IconDescriptionContainer({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.view,
      iconImage: "warningYellow_status_48.png",
      description: this._getTherapyStatusChangeReasonDescription()
    }));

    this._therapyStatusChangeReasonField = new tm.jquery.TextField();
    var suspendReasonContainer = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.view.getDictionary("reason"),
      contentComponent: this._therapyStatusChangeReasonField
    });

    this.add(suspendReasonContainer);
  },

  /**
   * Returns a description according to the selected therapy status.
   * @returns {String}
   * @private
   */
  _getTherapyStatusChangeReasonDescription: function()
  {
    if (this.selectedTherapyStatus === app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED)
    {
      return this.view.getDictionary('reason.for.suspending.required');
    }
    else if (this.selectedTherapyStatus === app.views.medications.TherapyEnums.therapyStatusEnum.ABORTED)
    {
      return this.view.getDictionary('reason.for.stopping.required');
    }
  }
});