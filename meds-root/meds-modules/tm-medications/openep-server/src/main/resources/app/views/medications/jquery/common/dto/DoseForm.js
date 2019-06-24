Class.define('app.views.medications.common.dto.DoseForm', 'tm.jquery.Object', {
  name: null, /* String */
  code: null, /* String */
  description: null, /* String */
  id: null, /* Long */
  doseFormType: null, /* String DoseFormType */
  medicationOrderFormType: null, /* String MedicationOrderFormType */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {String} doseFormType
   */
  setDoseFormType: function(doseFormType)
  {
    this.doseFormType = doseFormType;
  },

  /**
   * @returns {String}
   */
  getDoseFormType: function()
  {
    return this.doseFormType;
  },

  /**
   * @param {String} medicationOrderFormType
   */
  setMedicationOrderFormType: function(medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  },

  /**
   * @returns {String}
   */
  getMedicationOrderFormType: function()
  {
    return this.medicationOrderFormType;
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
   * @param {String} description
   */
  setDescription: function(description)
  {
    this.description = description;
  },

  /**
   * @returns {String}
   */
  getDescription: function()
  {
    return this.description;
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
  }
});