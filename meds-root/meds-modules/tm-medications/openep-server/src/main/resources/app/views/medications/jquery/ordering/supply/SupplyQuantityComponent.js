Class.define('app.views.medications.ordering.supply.SupplyQuantityComponent', 'tm.jquery.Component', {
  componentCls: 'supply-quantity-component',
  view: null,
  unit: null,
  quantity: null,

  _quantityField: null,
  _quantityUnitLabel: null,
  _fallbackQuantityUnit: null,

  /**
   * Creates a new instance of the supply quantity input component. DOM creation based on {@link tm.jquery.DateTimePicker}.
   * Allows wrapping the VerticallyTitledComponent and setting additional class names without having to worry about
   * overriding the defaults for the VerticallyTitledComponent.
   * @constructor
   */
  Constructor: function()
  {
    this.callSuper();
    /** used when the medication has no supply unit defined */
    this._fallbackQuantityUnit = this.getView().getDictionary('packages').toLowerCase();

    this._quantityField = new tm.jquery.TextField({
      value: this.quantity,
      width: 70,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._quantityUnitLabel = new tm.jquery.Label({
      cls: 'quantity-unit',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      text: this.unit || this._fallbackQuantityUnit
    });
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @param {string|null|undefined} unit
   */
  setUnit: function(unit)
  {
    this._quantityUnitLabel.setText(unit || this._fallbackQuantityUnit);
  },

  /**
   * @return {string}
   */
  getUnit: function()
  {
    return this._quantityUnitLabel.getText();
  },

  /**
   * @return {string|null}
   */
  getValue: function()
  {
    return this._quantityField.getValue();
  },

  /**
   * @param {string|null} value
   * @param {boolean} [preventEvent=false]
   */
  setValue: function(value, preventEvent)
  {
    this._quantityField.setValue(value, preventEvent);
  },

  /**
   * @return {tm.jquery.TextField}
   */
  getInputField: function()
  {
    return this._quantityField;
  },

  /**
   * @Override
   */
  createDom: function()
  {
    var $div = $('<div/>');
    var renderToElement = $div.get(0);
    
    var container = new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      renderToElement: renderToElement,
      titleText: this.getView().getDictionary('medication.supply.quantity'),
      contentComponent: new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        scrollable: 'visible'
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    container.getContentComponent().add(this._quantityField);
    container.getContentComponent().add(this._quantityUnitLabel);
    container.setCls(this.getCls() ?
        [container.getCls(), this.getComponentCls(), this.getCls()].join(' ') :
        [container.getCls(), this.getComponentCls()].join(' '));

    container.doRender();

    return container.dom;
  }
});