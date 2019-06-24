Class.define('app.views.medications.common.overview.NumberDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'number-data-entry-container',

  defaultHeight: 150,
  defaultWidth: 170,
  view: null,
  startProcessOnEnter: true,

  _numberField: null,
  _validationForm: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._configureForm();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function onRenderNumberDataEntryContainer(self)
    {
      self._numberField.focus();
    })
  },

  processResultData: function(resultDataCallback)
  {
    var numberField = this._numberField;

    this._validationForm.onSubmit = function()
    {
      if (this.hasFormErrors())
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
        return;
      }

      resultDataCallback(new app.views.common.AppResultData({success: true, value: numberField.getValue()}));
    };
    this._validationForm.submit();
  },

  /**
   * @returns {number}
   */
  getDefaultHeight: function()
  {
    return this.defaultHeight;
  },

  /**
   * @returns {number}
   */
  getDefaultWidth: function()
  {
    return this.defaultWidth;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    this._numberField = new tm.jquery.NumberField({
      width: 70,
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0}
    });

    this.add(this._numberField);
  },

  /**
   * Configures the validation form. The value must be an integer larger than 0.
   * @private
   */
  _configureForm: function()
  {
    this._validationForm = this.getView().getAppFactory().createForm({name: "number-data-entry-container-form"});
    this._validationForm.addFormField(
        new tm.jquery.form.FormField({
          name: 'number-of-pages-field',
          component: this._numberField,
          validation: new tm.jquery.form.FormFieldsValidation({
            type: "locale",
            validators: [
              new tm.jquery.Validator({
                errorMessage: this.getView().getDictionary('value.must.be.numeric.not.zero'),
                isValid: function(value)
                {
                  return !!value && value > 0;
                }
              })
            ],
            markers: {
              error: [
                new tm.jquery.form.FormFieldValidationMarker(),
                new tm.jquery.form.TooltipValidationMarker({trigger: 'focus'})
              ]
            }
          })
        }));
  }
});