Class.define('app.views.medications.common.dto.MedicationRoute', 'tm.jquery.Object', {
  id: null,
  name: null,
  type: null,
  unlicensedRoute: null,
  maxDose: null,
  discretionary: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Number} id
   */
  setId: function(id)
  {
    this.id = id;
  },

  /**
   * @returns {Number}
   */
  getId: function()
  {
    return this.id;
  },

  /**
   * @param {String} type
   */
  setType: function(type)
  {
    this.type = type;
  },

  /**
   * @returns {String}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @param {String} name
   */
  setName: function(name)
  {
    this.name = name;
  },

  /**
   * @returns {String}
   */
  getName: function()
  {
    return this.name;
  },

  /**
   * @param {Boolean} unlicensedRoute
   */
  setUnlicensedRoute: function(unlicensedRoute)
  {
    this.unlicensedRoute = unlicensedRoute;
  },

  /**
   * @returns {Boolean}
   */
  isUnlicensedRoute: function()
  {
    return this.unlicensedRoute === true;
  },

  /**
   * @param {{dose: number, unit: string, period: string}} maxDose
   */
  setMaxDose: function(maxDose)
  {
    this.maxDose = maxDose;
  },

  /**
   * @returns {{dose: number, unit: string, period: string}}
   */
  getMaxDose: function()
  {
    return this.maxDose;
  },

  /**
   * @param {Boolean} discretionary
   */
  setDiscretionary: function(discretionary)
  {
    this.discretionary = discretionary;
  },

  /**
   * @returns {Boolean}
   */
  isDiscretionary: function()
  {
    return this.discretionary === true;
  }
});
