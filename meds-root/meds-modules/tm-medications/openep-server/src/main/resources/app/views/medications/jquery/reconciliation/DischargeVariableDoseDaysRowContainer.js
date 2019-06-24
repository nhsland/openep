Class.define('app.views.medications.reconciliation.DischargeVariableDoseDaysRowContainer', 'tm.jquery.Container', {
  cls: 'discharge-variable-dose-days-row-container',
  scrollable: 'visible',
  view: null,

  timeDoseElement: null,
  medicationData: null,
  orderingBehaviour: null,
  referenceData: null,

  _dosePane: null,
  _timingDescriptionField: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.medicationData)
    {
      throw new Error('medicationData is not defined');
    }
    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined');
    }
    if (!this.referenceData)
    {
      throw new Error('referenceData is not defined');
    }
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
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @return {{quantity: number|undefined|null, quantityDenominator: string|undefined|null}}
   */
  getDose: function()
  {
    return this._dosePane.getDose();
  },

  /**
   * @return {string|null}
   */
  getTimingDescription: function()
  {
    return this._timingDescriptionField.getValue();
  },

  /**
   * Attaches the validation rules to the given form.
   * @param {tm.jquery.Form} form
   */
  attachFormValidation: function(form)
  {
    this._dosePane
        .getDosePaneValidations()
        .forEach(function attach(formField)
        {
          form.addFormField(formField)
        });
    form.addFormField(
        new tm.jquery.FormField({
          component: this._timingDescriptionField,
          required: true
        }));
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));

    this._dosePane = new app.views.medications.ordering.dosing.DoseContainer({
      cls: "dose-pane",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      view: this.getView(),
      medicationData: this.getMedicationData(),
      doseNumerator: !!this.timeDoseElement && !!this.timeDoseElement.doseElement ?
          this.timeDoseElement.doseElement.quantity :
          null,
      orderingBehaviour: this.getOrderingBehaviour(),
      referenceData: this.getReferenceData(),
      addDosageCalculationPane: false,
      showDoseUnitCombos: false,
      showDosageCalculation: false,
      showRounding: true,
      hideDenominator: true,
      /** this is wrong on many levels but the current DosePane's implementation of JS based focusing breaks the browser's
       * ability to focus on the next tabIndex field... */
      focusLostEvent: this._onDosePaneFocusLost.bind(this)
    });

    this._timingDescriptionField = new tm.jquery.TextField({
      tabIndex: 0,
      width: 'auto',
      value: !!this.timeDoseElement && !!this.timeDoseElement.timingDescription ?
          this.timeDoseElement.timingDescription :
          null,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var doseFieldTitleWrapComponent = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.getView().getDictionary('dose'),
      scrollable: 'visible',
      contentComponent: this._dosePane,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var timingDescriptionTitleWrapComponent = new app.views.medications.common.VerticallyTitledComponent({
      cls: 'vertically-titled-component timing-description-component',
      titleText: this.getView().getDictionary('timing.description'),
      scrollable: 'visible',
      contentComponent: this._timingDescriptionField,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    this.add(doseFieldTitleWrapComponent);
    this.add(timingDescriptionTitleWrapComponent);
  },

  /**
   * Handles {@link app.views.medications.ordering.dosing.DoseContainer#focusLostEvent} which forces us to set the focus on the next
   * field in a messed up way, but the current DosePane's implementation of JS based focusing breaks the browser's
   * ability to focus on the next field with the tabIndex attribute set to 0 or more.
   * @private
   */
  _onDosePaneFocusLost: function()
  {
    this._timingDescriptionField.focus();
  }
});