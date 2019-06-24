Class.define('app.views.medications.common.dto.MedicationUnitType', 'tm.jquery.Object', {
  code: null,
  displayName: null,
  factor: null,
  group: null,
  id: null,
  name: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {String} code
   */
  setCode: function(code)
  {
    this.code = code;
  },

  /**
   * @returns {String}
   */
  getCode: function()
  {
    return this.code;
  },

  /**
   * @param {String} displayName
   */
  setDisplayName: function(displayName)
  {
    this.displayName = displayName;
  },

  /**
   * @returns {String}
   */
  getDisplayName: function()
  {
    return this.displayName;
  },

  /**
   * @param {Number} factor
   */
  setFactor: function(factor)
  {
    this.factor = factor;
  },

  /**
   * @returns {Number}
   */
  getFactor: function()
  {
    return this.factor;
  },

  /**
   * @param {app.views.medications.TherapyEnums.unitGroup} group
   */
  setGroup: function(group)
  {
    this.group = group;
  },

  /**
   * @returns {app.views.medications.TherapyEnums.unitGroup}
   */
  getGroup: function()
  {
    return this.group;
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
  }
});
