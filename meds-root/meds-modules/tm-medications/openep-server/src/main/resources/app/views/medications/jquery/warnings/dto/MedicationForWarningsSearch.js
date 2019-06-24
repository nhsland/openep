Class.define('app.views.medications.warnings.dto.MedicationForWarningsSearch', 'tm.jquery.Object', {
  id: null,
  code: null,
  name: null,
  prospective: false,
  onlyOnce: false,
  product: false,
  routeId: null,
  frequency: null,
  frequencyUnit: null,
  effective: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Number|null}
   */
  getId: function()
  {
    return this.id;
  },
  /**
   * @returns {String|null}
   */
  getCode: function()
  {
    return this.code;
  },
  /**
   * @returns {String|null}
   */
  getName: function()
  {
    return this.name;
  },
  /***
   * @returns {boolean}
   */
  isOnlyOnce: function()
  {
    return this.onlyOnce === true;
  },
  /***
   * @returns {boolean}
   */
  isProduct: function()
  {
    return this.product === true;
  },
  /***
   * @returns {boolean}
   */
  isProspective: function()
  {
    return this.prospective === true;
  },
  /**
   * @returns {String|null}
   */
  getRouteId: function()
  {
    return this.routeId;
  },
  /**
   * @returns {Number|null}
   */
  getFrequency: function()
  {
    return this.frequency;
  },
  /**
   * @returns {String|null}
   */
  getFrequencyUnit: function()
  {
    return this.frequencyUnit;
  },
  /**
   * @returns {{endMillis: Number, startMillis: Number}|null}
   */
  getEffective: function()
  {
    return this.effective;
  }
});