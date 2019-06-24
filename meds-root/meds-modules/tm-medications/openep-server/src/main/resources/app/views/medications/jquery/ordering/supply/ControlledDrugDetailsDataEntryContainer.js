Class.define('app.views.medications.ordering.supply.ControlledDrugDetailsDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'controlled-drug-details-entry-container',
  scrollable: 'vertical',

  view: null,
  supply: null,
  orderMedicationSupplyUnit: null,
  medicationOptions: null,

  _rowsContainer: null,
  _addRowButton: null,
  _removeRowButton: null,

  /**
   * Returns a new instance of the contents of the dialog by which the user can enter or edit controlled drug supply. When
   * editing an existing supply information, the {@link supply} should be set. A new instance of the supply list will
   * be returned when the {@link #processResultData} is called.
   * @param {Object} config
   * @param {app.views.common.AppView} config.view
   * @param {Array<app.views.medications.common.dto.ControlledDrugSupply>} config.supply of existing supply data.
   * @param {string|null} config.orderMedicationSupplyUnit the optional supply unit used by the medication being ordered for
   * which this supply is being defined.
   * @param {Array<app.views.medications.common.dto.FormularyMedication>} config.medicationOptions containing the selectable
   * medication for which supply can be defined. Either a list of possible VMP level medication, if the prescribing
   * medication is on the VTM level, or the medication itself when no change can be made (prescribed on either the VMP or
   * AMP level)
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.supply = tm.jquery.Utils.isArray(this.supply) ? this.supply : [];
    this._buildGUI();
  },

  /**
   * @return {Array<app.views.medications.common.dto.FormularyMedication>} the list of medication from which the user
   * can pick when defining the supply. If the list is empty, the medication cannot be changed and is defined either
   * by the existing {@link #supply} or {@link #medicationData}.
   */
  getMedicationOptions: function()
  {
    return this.medicationOptions;
  },

  /**
   * @return {string|null} the optional supply unit used by the medication being ordered for which this supply is being
   * defined. When a preexisting supply from {@link #supply} exists, it's unit value will be used unless the user changes
   * the selected medication.
   */
  getOrderMedicationSupplyUnit: function()
  {
    return this.orderMedicationSupplyUnit;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
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

    form.onSubmit = function()
    {
      if (this.hasFormErrors())
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
        return;
      }

      var supply =
          rows.map(
              /** @param {app.views.medications.ordering.supply.ControlledDrugDetailsSupplyRowContainer} row */
              function createSupply(row)
              {
                return new app.views.medications.common.dto.ControlledDrugSupply({
                  quantity: row.getQuantity(),
                  unit: row.getUnit(),
                  medication: row.getMedication()
                });
              });

      resultDataCallback(new app.views.common.AppResultData({success: true, value: supply}));
    };
    form.submit();
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this._rowsContainer = new app.views.medications.common.RowBasedDataContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      rowData: this.supply,
      rowFactory: this._createRowContainer.bind(this)
    });

    this.add(this._rowsContainer);
  },

  /**
   * Row factory method for {@link #_rowsContainer}. When no supply data for the row is defined and exactly one
   * medication can be selected, a default is created based on that medication. In case the medication doesn't have a
   * supply unit defined, the one from the medication being ordered and for which this supply is being created is used.
   * @param {app.views.medications.common.dto.ControlledDrugSupply|undefined} [supply=undefined]
   * @return {app.views.medications.ordering.supply.ControlledDrugDetailsSupplyRowContainer}
   * @private
   */
  _createRowContainer: function(supply)
  {
    if (!supply && this.medicationOptions.length === 1)
    {
      supply = new app.views.medications.common.dto.ControlledDrugSupply({
        unit: !!this.medicationOptions[0].getSupplyUnit() ?
            this.medicationOptions[0].getSupplyUnit() :
            this.getOrderMedicationSupplyUnit(),
        medication: {
          id: this.medicationOptions[0].getId(),
          name: this.medicationOptions[0].getName()
        }
      });
    }

    return new app.views.medications.ordering.supply.ControlledDrugDetailsSupplyRowContainer({
      view: this.getView(),
      supply: supply,
      medicationOptions: this.getMedicationOptions()
    });
  },

  _createValidationForm: function()
  {
    var form = this.getView().getAppFactory().createForm({name: "controlled-drug-supply-form"});

    this._rowsContainer
        .getRows()
        .forEach(
            /** @param {app.views.medications.ordering.supply.ControlledDrugDetailsSupplyRowContainer} row */
            function attachValidation(row)
            {
              row.attachFormValidation(form);
            });

    return form;
  }
});