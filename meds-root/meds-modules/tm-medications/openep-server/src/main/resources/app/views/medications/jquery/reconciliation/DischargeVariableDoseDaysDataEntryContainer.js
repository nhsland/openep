Class.define('app.views.medications.reconciliation.DischargeVariableDoseDaysDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'discharge-variable-days-entry-container',
  scrollable: 'vertical',
  view: null,

  medicationData: null,
  orderingBehaviour: null,
  referenceData: null,
  timedDoseElements: null,

  _rowsContainer: null,
  _addRowButton: null,
  _removeRowButton: null,

  /**
   * The discharge medication's version of the variable dose days (protocol) defining dialog content.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper();
    this.timedDoseElements =
        app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(this.timedDoseElements) ?
            this.timedDoseElements :
            [];
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
    this._buildGUI();
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
   * @return {app.views.common.AppView}
   */
  getView: function ()
  {
    return this.view;
  },

  /**
   * @param {function(app.views.common.AppResultData)} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var form = this._createValidationForm();
    var rows = this._rowsContainer.getRows();

    form.setOnValidationSuccess(function()
    {
      var timedDoseElements =
          rows.map(
              /** @param {app.views.medications.reconciliation.DischargeVariableDoseDaysRowContainer} row */
              function createTimedElement(row)
              {
                return {
                  doseElement: {
                    quantity: row.getDose().quantity,
                    quantityDenominator: row.getDose().quantityDenominator
                  },
                  timingDescription: row.getTimingDescription()
                }
              });

      resultDataCallback(new app.views.common.AppResultData({
        success: true,
        value: {timedDoseElements: timedDoseElements}
      }));
    });
    form.setOnValidationError(function()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    });

    form.submit();
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this._rowsContainer = new app.views.medications.common.RowBasedDataContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      initialRowCount: 2,
      minimumRowCount: 2,
      rowData: this.timedDoseElements,
      rowFactory: this._createRowContainer.bind(this)
    });

    this.add(this._rowsContainer);
  },

  /**
   * Row factory method for {@link #_rowsContainer}.
   * @param {Object} [timeDoseElement=undefined]
   * @return {app.views.medications.reconciliation.DischargeVariableDoseDaysRowContainer}
   * @private
   */
  _createRowContainer: function(timeDoseElement)
  {
    return new app.views.medications.reconciliation.DischargeVariableDoseDaysRowContainer({
      view: this.getView(),
      timeDoseElement: timeDoseElement,
      medicationData: this.getMedicationData(),
      orderingBehaviour: this.getOrderingBehaviour(),
      referenceData: this.getReferenceData()
    });
  },

  /**
   * @return {tm.jquery.Form} a new instance of the validation form with attached {@link tm.jquery.FormField} for each
   * row instance.
   * @private
   */
  _createValidationForm: function()
  {
    var form = new tm.jquery.Form({
      view: this.getView(),
      requiredFieldValidatorErrorMessage: this.getView().getDictionary("field.value.is.required")
    });

    this._rowsContainer
        .getRows()
        .forEach(
            /** @param {app.views.medications.reconciliation.DischargeVariableDoseDaysRowContainer} row */
            function attachValidation(row)
            {
              row.attachFormValidation(form);
            });

    return form;
  }
});