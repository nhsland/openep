Class.define('app.views.medications.common.dto.FormularyMedication', 'tm.jquery.Object', {
  id: null,
  name: null,
  formulary: true,
  supplyUnit: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {number}
   */
  getId: function()
  {
    return this.id;
  },

  /**
   * @return {null}
   */
  getName: function()
  {
    return this.name;
  },

  /**
   * @return {boolean}
   */
  isFormulary: function()
  {
    return this.formulary === true
  },

  /**
   * @return {string|null} optional supply unit, when available.
   */
  getSupplyUnit: function()
  {
    return this.supplyUnit;
  }
});