Class.define('app.views.medications.reconciliation.DispenseSourcePrescriptionContentExtensionContainer', 'app.views.medications.ordering.PrescriptionContentExtensionContainer', {
  cls: "dispense-source-prescription-extension",
  scrollable: 'visible',
  dispenseSources: null,

  _dispenseSourceField: null,
  _defaultSource: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!tm.jquery.Utils.isArray(this.dispenseSources))
    {
      throw new Error('dispenseSources not defined');
    }

    this._defaultSource = this.getDispenseSources().find(app.views.medications.common.dto.DispenseSource.matchDefault);

    this._buildGui();
  },

  /**
   * @return {Array<app.views.medications.common.dto.DispenseSource>}
   */
  getDispenseSources: function()
  {
    return this.dispenseSources;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @override Applies the existing dispense source to the selection field, if dispense details exist. Otherwise
   * presumes {@link #clear} was called first and the default is already set.
   */
  setValues: function(therapy)
  {
    if (!!therapy.getDispenseDetails() && !!therapy.getDispenseDetails().getDispenseSource())
    {
      this._dispenseSourceField.setSelections([therapy.getDispenseDetails().getDispenseSource()]);
    }
  },

  /**
   * @param {Object} config
   * @override Applies the selected dispense source to the new therapy configuration object.
   */
  attachTherapyConfig: function (config)
  {
    if (!config.dispenseDetails)
    {
      config.dispenseDetails = new app.views.medications.common.dto.DispenseDetails();
    }

    config.dispenseDetails.setDispenseSource(this._dispenseSourceField.getSelections()[0]);
  },

  /**
   * @override
   * @return {Array<tm.jquery.FormField>}
   */
  getFormValidations: function()
  {
    return [
      new tm.jquery.FormField({
        component: this._dispenseSourceField,
        required: true,
        validation: {
          type: "local"
        },
        componentValueImplementationFn: function(component)
        {
          return component.hasSelections() ? component.getSelections() : null;
        },
        getComponentValidationMarkElement: function(component)
        {
          return component.getButtonElement();
        }
      })
    ];
  },

  /**
   * @override
   */
  clear: function()
  {
    this._dispenseSourceField.setSelections(!!this._defaultSource ? [this._defaultSource] : []);
  },

  _buildGui: function()
  {
    var view = this.getView();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch"));

    this._dispenseSourceField = new tm.jquery.SelectBox({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cls: "left-margin",
      liveSearch: false,
      options: this.getDispenseSources().map(
          function createOption(source)
          {
            return tm.jquery.SelectBox.createOption(source);
          }),
      selections: this._defaultSource ? [this._defaultSource] : [],
      width: 'auto',
      dropdownWidth: "stretch",
      allowSingleDeselect: false,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.getId())
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.getId());
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return option.getValue().getName();
      }
    });

    var sourceTitleWrapper = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('dispensing.from'),
      scrollable: "visible",
      contentComponent: this._dispenseSourceField,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(sourceTitleWrapper);
  }
});