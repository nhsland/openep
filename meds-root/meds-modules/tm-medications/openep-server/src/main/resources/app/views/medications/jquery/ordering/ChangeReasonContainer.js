Class.define('app.views.medications.ordering.ChangeReasonContainer', 'tm.jquery.Container', {
  cls: "change-reason-pane",
  scrollable: "visible",
  view: null,

  _commentField: null,
  _reasonTypeField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Populates the UI components with values of the provided therapy change reason. When {@lin #getTherapyChangeReason} is
   * called, a new instance will be created.
   * @param {app.views.medications.common.dto.TherapyChangeReason|null} therapyChangeReason
   */
  setTherapyChangeReason: function(therapyChangeReason)
  {
    this._reasonTypeField.setSelections(
        !!therapyChangeReason && !!therapyChangeReason.getReason() ?
            [therapyChangeReason.getReason()] :
            [],
        true);
    this._commentField.setValue(!!therapyChangeReason ? therapyChangeReason.getComment() : null, true);
  },

  /**
   * @return {app.views.medications.common.dto.TherapyChangeReason|null} a new instance of the change reason, as defined
   * in by component.
   */
  getTherapyChangeReason: function()
  {
    return this._reasonTypeField.hasSelections() ?
        new app.views.medications.common.dto.TherapyChangeReason({
          changeReason: this._reasonTypeField.getSelections()[0],
          comment: this._commentField.getValue()
        }) :
        null;
  },

  /**
   * Configures and attaches server side based validation of the change reason. In case the API determines the changes
   * to the therapy are significant, the user input of the type and comment are mandatory. They also become mandatory
   * if either of the values is set/selected.
   *
   * Due to how the server side validation was implemented in {@link tm.jquery.Form}, the validation is achieved by creating
   * a proxy AppView object that allows us to change how {@link tm.jquery.Form} executes the Ajax value submit request, which
   * in turn enables us to use an API method with a completely signature. The downside is that only one such validator
   * can be used on the form instance passed to this method! This should be resolved once we upgrade to the new form
   * validation.
   * @param {app.views.medications.common.dto.Therapy} originalTherapy
   * @param {app.views.medications.common.dto.Therapy} modifiedTherapy
   * @param {tm.jquery.Form} form
   */
  attachRemoteFormValidation: function(originalTherapy, modifiedTherapy, form)
  {
    var remoteValidationFields = this._buildRemoteValidationFormFields();
    var self = this;

    remoteValidationFields.forEach(function(field)
    {
      form.addFormField(field);
    });

    // The code from {@link tm.jquery.Form#_sendRequest} looks incomplete - requires a separate success handler
    // when remote validation is added, but calls onValidationError if onSubmitError is set.
    form.setOnSubmitSuccess(function()
    {
      this.onValidationSuccess();
    });
    form.setOnSubmitError(function()
    {
      this.onValidationError();
    });
    form.setSubmitRequestUrl('remote'); // _sendRequest checks for a value before calling AppView#sendRequest.
    form.setView({
      sendRequest: function(url, method, dataType, params, objectKey, successFn, failureFn)
      {
        self.getView()
            .getRestApi()
            .loadHasTherapyChanged(originalTherapy, modifiedTherapy, 'REQUIRES_CHANGE_REASON')
            .then(
                function onSuccess(significantChange)
                {
                  successFn({
                    formValidationResults: {
                      fieldValidationResultsList:
                          self._buildRemoteValidationResult(remoteValidationFields, significantChange)
                    }
                  });
                },
                function onFailure()
                {
                  failureFn();
                }
            )
      }
    });
  },

  /**
   * Sets focus to the comment field.
   */
  requestFocus: function()
  {
    this._commentField.getInputElement().focus();
  },

  /**
   * Clears the values of the input fields, effectively resetting the component state.
   */
  clear: function()
  {
    this._reasonTypeField.setSelections([], true);
    this._commentField.setValue(null);
  },

  _buildGui: function()
  {
    var view = this.getView();
    var editChangeReasonTypeEnum = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 5));

    var commentComponent = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('change.commentary'),
      contentComponent: new tm.jquery.TextField({
        cls: "comment-field",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%")
    });
    this._commentField = commentComponent.getContentComponent();

    var reasonComponent = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('change.reason'),
      scrollable: "visible",
      contentComponent: new tm.jquery.SelectBox({
        cls: "indication-combo",
        placeholder: view.getDictionary("select"),
        options: this._buildReasonOptions(this.getView().getChangeReasonTypeHolder().getMap()[editChangeReasonTypeEnum]),
        liveSearch: false,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
        dropdownWidth: "stretch",
        allowSingleDeselect: true,
        defaultValueCompareToFunction: function(value1, value2)
        {
          return (tm.jquery.Utils.isEmpty(value1) ? null : value1.code)
              === (tm.jquery.Utils.isEmpty(value2) ? null : value2.code);
        },
        defaultTextProvider: function(selectBox, index, option)
        {
          return option.getValue().name;
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%")
    });
    this._reasonTypeField = reasonComponent.getContentComponent();

    this.add(reasonComponent);
    this.add(commentComponent);
  },

  _buildReasonOptions: function(values)
  {
    values = tm.jquery.Utils.isEmpty(values) ? [] : values;

    return values.map(function(element)
    {
      return tm.jquery.SelectBox.createOption(element, null);
    });
  },

  /**
   * Generates validation results for the given form fields. If the therapy change is significant, all fields must
   * have a value set. If not, a field is presumed valid if it either has a value set or it doesn't have a value set
   * and no other field has a value either (this ensures only the field with the missing value will be invalid).
   *
   * @param {Array<tm.jquery.FormField>} formFields
   * @param {boolean} significantChange true, when the change was significant which should make the result valid only
   * if the field has a value set. When false, the field value is not required.
   * @return {Array<{fieldName: *, fieldValidationResults: *[]}>} an JSON object representing the result of remote field
   * validation, as defined and required by {@link tm.jquery.Form}.
   * @private
   */
  _buildRemoteValidationResult: function(formFields, significantChange)
  {
    return formFields.map(
        function generateResultJson(field)
        {
          return {
            fieldName: field.getName(),
            fieldValidationResults: [
              {
                field: field.getName(),
                validator: 'requiredByChangeValidationOf' + field.getName(),
                valid: !!field.getValue() || !significantChange && !field.getValue() && formFields.every(hasNoValueSet),
                message: this.getView().getDictionary("field.value.is.required")
              }
            ]
          };
        },
        this
    );

    /**
     * @param {tm.jquery.FormField} formField
     * @return {boolean} true if the field has a value set, otherwise false.
     */
    function hasNoValueSet(formField)
    {
      return !formField.getValue();
    }
  },

  /**
   * @return {Array<tm.jquery.FormField>} an array of form fields used to enable conditionally validating the presence
   * of the change reason type and comment input when the API defines the change to the therapy as significant.
   * @private
   */
  _buildRemoteValidationFormFields: function()
  {
    return [
      new tm.jquery.FormField({
        name: 'remoteReasonSelectBox',
        component: this._reasonTypeField,
        required: false,
        validation: {
          type: 'remote'
        },
        componentValueImplementationFn: function(component)
        {
          return component.hasSelections() ? component.getSelections()[0] : null;
        },
        getComponentValidationMarkElement: function(component)
        {
          return component.getButtonElement();
        }
      }),
      new tm.jquery.FormField({
        name: 'remoteReasonComment',
        component: this._commentField,
        required: false,
        componentValueImplementationFn: function(component)
        {
          return component.getValue();
        },
        validation: {
          type: 'remote'
        }
      })
    ]
  },

  getChangeReasonValidations: function(remote)
  {
    var self = this;
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      name: "reasonSelectBox", label: null, component: this._reasonTypeField, required: remote === true,
      validation: {
        type: !!remote ? 'remote' : 'local'
      },
      componentValueImplementationFn: function(component)
      {
        return component.hasSelections() ? component.getSelections() : null;
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getButtonElement();
      }
    }));
    formFields.push(new tm.jquery.FormField({
      name: "reasonComment",
      component: self._commentField,
      required: remote === true,
      componentValueImplementationFn: function(component)
      {
        return component.getValue();
      },
      validation: {
        type: !!remote ? 'remote' : 'local'
      }
    }));
    return formFields;
  }
});

