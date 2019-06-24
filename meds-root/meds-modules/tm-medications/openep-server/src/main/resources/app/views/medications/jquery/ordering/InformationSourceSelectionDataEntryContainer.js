/**
 * Represents the contents of a simple dialog intended to enter the information source of a therapy. Selecting at least
 * one source is required.
 */
Class.define('app.views.medications.ordering.InformationSourceSelectionDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'information-source-entry-container',
  scrollable: 'visible',
  /** @type app.views.common.AppView */
  view: null,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  availableInformationSources: null,

  /** @type app.views.medications.ordering.InformationSourceContainer */
  _informationSourceContainer: null,

  Constructor: function()
  {
    this.callSuper();

    if (!tm.jquery.Utils.isArray(this.availableInformationSources))
    {
      throw new Error('Missing availableInformationSources config value.');
    }

    this._buildGui();
  },

  /**
   * @param {function(app.views.common.AppResultData)} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var form = this._createValidationForm();
    var selections = this._informationSourceContainer.getSelections();

    form.setOnValidationSuccess(function()
    {
      resultDataCallback(new app.views.common.AppResultData({
        success: true,
        value: selections
      }));
    });
    form.setOnValidationError(function()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    });

    form.submit();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this._informationSourceContainer = new app.views.medications.ordering.InformationSourceContainer({
      view: this.view,
      availableInformationSources: this.availableInformationSources,
      required: true
    });

    this.add(new tm.jquery.Component({
      cls: 'TextData',
      html: this.view.getDictionary('select.one.or.more.sources')
    }));
    this.add(this._informationSourceContainer);
  },

  /**
   * @return {tm.jquery.Form}
   * @private
   */
  _createValidationForm: function()
  {
    var form =  new tm.jquery.Form({
      requiredFieldValidatorErrorMessage: this.view.getDictionary("field.value.is.required")
    });

    this.view
            .getAppFactory()
            .createForm({name: "information-source-entry-container-form"});

    this._informationSourceContainer
        .getFormValidations()
        .forEach(
            function attachFieldValidationToForm(field)
            {
              form.addFormField(field);
            }
        );

    return form;
  }
});
