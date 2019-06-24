/**
 * A simple data entry container, allowing the user to enter a reason for administration cancellation or deletion. Includes
 * a static helper to present the container within a dialog with the correct title, depending on the selected action.
 * */
Class.define('app.views.medications.timeline.DeleteAdministrationContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    asDialog: function(view, administration, resultCallback)
    {
      var content = new app.views.medications.timeline.DeleteAdministrationContainer({
        view: view,
        administration: administration
      });

      var administrationTimeDisplay = view.getDisplayableValue(administration.getAdministrationTimestamp(), "short.time");
      var title = administration.isAdministrationPlanned() ?
          view.getDictionary("cancel.administration.at") + " " + administrationTimeDisplay :
          view.getDictionary("delete.administration.at") + " " + administrationTimeDisplay;

      return view.getAppFactory().createDataEntryDialog(
          title,
          null,
          content,
          resultCallback,
          530,
          130
      )

    }
  },
  /** @type String */
  cls: 'delete-container',
  /** @type app.views.common.AppView */
  view: null,
  /** @type app.views.medications.timeline.administration.dto.Administration */
  administration: null,
  /** @type tm.jquery.TextField */
  _deleteCommentField: null,

  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self._deleteCommentField.focus();
      }, 300);
    });
  },

  /**
   * @param {function} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var validationForm = this._createValidationForm();

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
          deleteComment: self._deleteCommentField.getValue()
        }
      }));
    };

    validationForm.submit();
  },

  /** @private */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center'));
    this._deleteCommentField = new tm.jquery.TextField({width: 370});
    this.add(new tm.jquery.Container({
      cls: 'TextData',
      html: this.administration.isAdministrationPlanned() ?
          this.view.getDictionary("cancellation.reason") :
          this.view.getDictionary("delete.reason")
    }));
    this.add(this._deleteCommentField);
  },

  /**
   * @return app.views.common.AppForm
   * @private
   */
  _createValidationForm: function()
  {
    var deleteCommentField = this._deleteCommentField;

    var validationForm = this.view.getAppFactory().createForm({
      name: 'delete-administration-form'
    });

    validationForm.addFormField(new tm.jquery.form.FormField({
      name: 'delete-comment-field',
      component: deleteCommentField,
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
    return validationForm;
  }
});
