Class.define('app.views.medications.ordering.supply.ControlledDrugDetailsSupplyRowContainer', 'tm.jquery.Container', {
  cls: 'controlled-drug-details-supply-row-container',
  scrollable: 'visible',
  view: null,

  supply: null,
  medicationOptions: null,

  _medicationField: null,
  _quantityField: null,

  /**
   * Creates a new instance of a single controlled drug supply row, enabling the user to optionally select the desired
   * medication and set it's supply quantity. When only one medication is passed, selection is disabled and the
   * medication is preselected. When editing existing supply information, the {@link supply} should be set and the
   * values will be pre-filled from it.
   * @param {Object} config
   * @param {app.views.common.AppView} config.view
   * @param {app.views.medications.common.dto.ControlledDrugSupply} config.supply of existing supply data to be pre-filled.
   * @param {Array<app.views.medications.common.dto.FormularyMedication>} config.medicationOptions containing the selectable
   * medication for which supply can be defined. If only one exists, the medication selection component is disabled and
   * the medication will be pre-selected.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medicationOptions = tm.jquery.Utils.isArray(this.medicationOptions) ? this.medicationOptions : [];
    this._buildGui();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.dto.MedicationData}
   */
  getMedication: function()
  {
    var selection = this._medicationField.getSelection();
    return !!selection ? selection.getData() : null;
  },

  /**
   * @return {string|null} the quantity used for this supply definition.
   */
  getQuantity: function()
  {
    return this._quantityField.getValue();
  },

  /**
   * @return {string} the unit used by this for this supply definition.
   */
  getUnit: function()
  {
    return this._quantityField.getUnit();
  },

  /**
   * @return {Array<app.views.medications.common.dto.FormularyMedication>} the list of medications from which the user
   * can pick when defining the supply. If the list only contains one medication, it will be preselected and selection
   * disabled.
   */
  getMedicationOptions: function()
  {
    return this.medicationOptions;
  },

  /**
   * Attaches the validation rules to the given form. The medication selection and quantity are mandatory. Since the
   * validation error tooltips don't seem to work with the {@link tm.jquery.TypeaheadField}, only a
   * {@link tm.jquery.form.FormFieldValidationMarker} is added for that field.
   * @param {tm.jquery.form.Form} form
   */
  attachFormValidation: function(form)
  {
    form.addFormField(
        new tm.jquery.form.FormField({
          name: this.getId() + '-medication-field',
          component: this._medicationField,
          validation: new tm.jquery.form.FormFieldsValidation({
            type: "locale",
            validators: [
              new tm.jquery.RequiredValidator({errorMessage: this.getView().getDictionary("field.value.is.required")})
            ],
            markers: {
              error: [
                new tm.jquery.form.FormFieldValidationMarker()
              ]
            }
          }),
          getValue: function()
          {
            return this.getComponent().getSelection();
          }
        }));
    form.addFormField(
        new tm.jquery.form.FormField({
          name: this.getId() + '-quantity-field',
          component: this._quantityField.getInputField(),
          validation: new tm.jquery.form.FormFieldsValidation({
            type: "locale",
            validators: [
              new tm.jquery.Validator({
                errorMessage: this.getView().getDictionary('value.must.be.numeric.not.zero'),
                isValid: function(value)
                {
                  return !!value && app.views.medications.MedicationUtils.isStringPositiveInteger(value);
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
  },

  _buildGui: function()
  {
    var self = this;
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));

    var preselectionNode =
        this.supply && this.supply.getMedication() ?
            new tm.jquery.tree.Node({
              key: this.supply.getMedication().id,
              title: this.supply.getMedication().name,
              data: this.supply.getMedication()
            }) :
            null;

    var medicationSelectable = this.getMedicationOptions().length > 1;

    this._medicationField = new app.views.medications.common.MedicationSearchField({
      view: this.getView(),
      width: 'auto',
      mode: "advanced",
      minLength: 3,
      dropdownHorizontalAlignment: "auto",
      dropdownVerticalAlignment: "auto",
      dropdownAppendTo: this.getView().getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      dropdownWidth: 'stretch',
      selection: preselectionNode,
      forceToggleButton: medicationSelectable,
      enabled: medicationSelectable,
      clearable: medicationSelectable
    });
    this._medicationField.setDataLoader(this._medicationFieldDataLoader.bind(this));
    this._medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, this._onMedicationFieldSelectionChange.bind(this));

    var medicationFieldWrapper = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.getView().getDictionary('medication'),
      scrollable: 'visible',
      contentComponent: this._medicationField,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    this._quantityField = new app.views.medications.ordering.supply.SupplyQuantityComponent({
      view: this.getView(),
      quantity: !!this.supply ? this.supply.getQuantity() : null,
      unit: !!this.supply ? this.supply.getUnit() : undefined,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this.add(medicationFieldWrapper);
    this.add(this._quantityField);
  },

  /**
   * Based on {@see https://stackoverflow.com/questions/44604794/searching-for-words-in-string}. Similar compare to
   * our server side API when we search in the whole catalog.
   * @param {string} name
   * @param {Array<string>} terms
   * @return {boolean} true if the term words are found inside the passed name, otherwise false. Uses a case insensitive
   * search.
   * @private
   */
  _isSearchTermWordsMatch: function(name, terms)
  {
    for (var i = 0, len = terms.length; i < len; i++) {
      var regex = new RegExp(terms[i], 'i');
      if (regex.test(name) === false) {
        return false;
      }
    }
    return true;
  },

  /**
   * Event handler for {@link #_medicationField}'s selection. Set the supply units from the selected medication, if
   * a a selection was made and it's unit is defined, otherwise sets the supply unit to undefined, which will revert
   * the unit to the predefined global default.
   * @private
   */
  _onMedicationFieldSelectionChange: function()
  {
    this._quantityField.setUnit(
        this._medicationField.getSelection() && this._medicationField.getSelection().getData().getSupplyUnit() ?
            this._medicationField.getSelection().getData().getSupplyUnit() :
            undefined);
  },

  /**
   * Local implementation of the {@link app.views.medications.common.MedicationSearchField#dataLoader}. The component's
   * internal data loader is intended to search across the complete medication catalog, optionally filtered by the route of
   * application. In our case, we have a predefined list of medications VMP medications, and the API returns a simpler
   * DTO, which has to be transformed to the correct data structure.
   * @param {app.views.medications.common.MedicationSearchField} component
   * @param {{searchQuery: string}} requestParams
   * @param {function} processCallback
   */
  _medicationFieldDataLoader: function(component, requestParams, processCallback)
  {
    var onlyFormularyResults = component.isLimitByFormulary();
    var searchWords = requestParams.searchQuery.split(' ');

    processCallback(
        this.getMedicationOptions()
            .filter(
                function byName(medication)
                {
                  return this._isSearchTermWordsMatch(medication.getName(), searchWords) &&
                      (!onlyFormularyResults || onlyFormularyResults && medication.isFormulary());
                },
                this)
            .map(
                function toTreeData(medication)
                {
                  return {
                    key: medication.getId(),
                    title: medication.getName(),
                    tooltip: null,
                    expanded: false,
                    folder: false,
                    hideCheckbox: false,
                    lazy: false,
                    selected: false,
                    unselectable: false,
                    extraClasses: null,
                    children: [],
                    data: medication
                  }
                }),
        requestParams
    );
  }
});