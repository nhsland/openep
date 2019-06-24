Class.define('app.views.medications.common.dto.MedicationProperty', 'tm.jquery.Object', {

  type: null, /* String */
  name: null, /* String */
  value: null, /* String || Boolean */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
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
   * @param {String || Boolean} value
   */
  setValue: function(value)
  {
    this.value = value;
  },

  /**
   * @returns {String || Boolean}
   */
  getValue: function()
  {
    return this.value;
  }
});