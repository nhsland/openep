Class.define('app.views.medications.common.dto.Medication', 'tm.jquery.Object', {

  id: null, /* number */
  name: null, /* String */
  genericName: null, /* String */
  medicationType: null, /* MedicationTypeEnum */
  surcharge: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },
  /**
   * @param {number} id
   */
  setId: function(id)
  {
    this.id = id;
  },
  /**
   * @returns {number | null}
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
  },
  /**
   * @param {String} genericName
   */
  setGenericName: function(genericName)
  {
    this.genericName = genericName;
  },
  /**
   * @returns {String}
   */
  getGenericName: function()
  {
    return this.genericName;
  },
  /**
   * @param {String} medicationType
   */
  setMedicationType: function(medicationType)
  {
    this.medicationType = medicationType;
  },
  /**
   * @returns {String}
   */
  getMedicationType: function()
  {
    return this.medicationType;
  },
  /**
   * @returns {boolean}
   */
  isMedicationUniversal: function()
  {
    return tm.jquery.Utils.isEmpty(this.id)
  },
  /**
   *
   * @returns {boolean}
   */
  isDiluent: function()
  {
    return this.medicationType === app.views.medications.TherapyEnums.medicationTypeEnum.DILUENT;
  },

  /**
   * @returns {boolean}
   */
  isOxygen: function()
  {
    return this.medicationType === app.views.medications.TherapyEnums.medicationTypeEnum.MEDICINAL_GAS;
  },

  /**
   * @returns {boolean}
   */
  isMedication: function()
  {
    return this.medicationType === app.views.medications.TherapyEnums.medicationTypeEnum.MEDICATION;
  },

  /**
   * @returns {String}
   */
  getDisplayName: function()
  {
    if (this.getGenericName())
    {
      return this.getGenericName() + ' (' + this.getName() + ')';
    }
    return this.getName();
  },

  /**
   * @returns {String}
   */
  getFormattedDisplayName: function()
  {
    if (tm.jquery.Utils.isEmpty(this.getGenericName()) || this.isDiluent())
    {
      return "<span class='TextDataBold'>" + tm.jquery.Utils.escapeHtml(this.getName()) + "</span>";
    }
    return "<span class='TextDataBold'>" + tm.jquery.Utils.escapeHtml(this.getGenericName()) + "</span>"
        + " " + "<span class='TextData'>" + "(" + tm.jquery.Utils.escapeHtml(this.getName()) + ")" + "</span>";
  },

  /**
   * @return {string|null}
   */
  getSurcharge: function()
  {
    return this.surcharge;
  }
});