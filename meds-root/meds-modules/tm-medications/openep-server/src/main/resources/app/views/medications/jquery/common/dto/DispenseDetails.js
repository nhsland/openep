Class.define('app.views.medications.common.dto.DispenseDetails', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.controlledDrugSupply = tm.jquery.Utils.isArray(jsonObject.controlledDrugSupply) ?
          jsonObject.controlledDrugSupply.map(function(controlledDrugSupplyJson)
          {
            return new app.views.medications.common.dto.ControlledDrugSupply(controlledDrugSupplyJson);
          }) :
          [];
      config.dispenseSource = !!jsonObject.dispenseSource ?
          new app.views.medications.common.dto.DispenseSource(jsonObject.dispenseSource) :
          null;

      return new app.views.medications.common.dto.DispenseDetails(config);
    }
  },

  daysDuration: null,
  quantity: null,
  unit: null,
  dispenseSource: null,
  controlledDrugSupply: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.controlledDrugSupply = tm.jquery.Utils.isArray(this.controlledDrugSupply) ? this.controlledDrugSupply : [];
  },

  /**
   * @return {number|null}
   */
  getDaysDuration: function()
  {
    return this.daysDuration;
  },

  /**
   * @return {number|null}
   */
  getQuantity: function()
  {
    return this.quantity;
  },

  /**
   * @return {string|null}
   */
  getUnit: function()
  {
    return this.unit;
  },

  /**
   * @return {app.views.medications.common.dto.DispenseSource|null}
   */
  getDispenseSource: function()
  {
    return this.dispenseSource;
  },

  /**
   * @return {Array<Object>}
   */
  getControlledDrugSupply: function()
  {
    return this.controlledDrugSupply;
  },

  /**
   * @param {app.views.medications.common.dto.DispenseSource|null} value
   */
  setDispenseSource: function(value)
  {
    this.dispenseSource = value;
  }
});