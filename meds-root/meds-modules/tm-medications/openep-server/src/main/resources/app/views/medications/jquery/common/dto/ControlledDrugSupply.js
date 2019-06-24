Class.define('app.views.medications.common.dto.ControlledDrugSupply', 'tm.jquery.Object', {
  quantity: null,
  unit: null,
  medication: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {number}
   */
  getQuantity: function()
  {
    return this.quantity;
  },

  /**
   * @param {number} value
   */
  setQuantity: function(value)
  {
    this.quantity = value;
  },

  /**
   * @return {string|null}
   */
  getUnit: function()
  {
    return this.unit;
  },

  /**
   * @param {string} value
   */
  setUnit: function(value)
  {
    this.unit = value;
  },

  /**
   * @return {{id: number, name: string}}
   */
  getMedication: function()
  {
    return this.medication;
  },

  /**
   * @param {{id: number, name: string}} namedIdentity
   */
  setMedication: function(namedIdentity)
  {
    this.medication = namedIdentity;
  }
});