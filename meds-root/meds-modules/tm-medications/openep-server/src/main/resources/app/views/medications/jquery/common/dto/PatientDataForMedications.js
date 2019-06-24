Class.define('app.views.medications.common.dto.PatientDataForMedications', 'tm.jquery.Object', {
  statics: {
    /**
     * @param {Object} jsonObject
     * @return {app.views.medications.common.dto.PatientDataForMedications}
     */
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.birthDate = !!jsonObject.birthDate ? new Date(jsonObject.birthDate) : null;

      return new app.views.medications.common.dto.PatientDataForMedications(config);
    }
  },
  /** @type string|null */
  patientName: null,
  /** @type Date|null */
  birthDate: null,
  /** @type number|null */
  weightInKg: null,
  /** @type number|null */
  heightInCm: null,
  /** @type string|null */
  gender: null,
  /** @type Array<{id: string, name: string}> */
  diseases: null,
  /** @type Array<{id: string, name: string}>|undefined */
  allergies: null,
  /** @type string of {@link app.views.medications.TherapyEnums.allergiesStatus} */
  allergiesStatus: app.views.medications.TherapyEnums.allergiesStatus.NOT_CHECKED,
  /**
   * @type null|{outpatient: boolean, centralCaseId: string, centralCaseEffective: {startMillis: number,
   * endMillis: number}|null, episodeId: string, careProvider: Object}
   */
  centralCaseDto: null,
  /** @type boolean */
  witnessingRequired: false,

  /**
   * Introduced by our server side serializers for time intervals as the value used when the upper limit of an interval
   * is not defined.
   * @type number
   */
  _intervalPositiveInfinityMillis: 32503680000000,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.allergies = tm.jquery.Utils.isArray(this.allergies) ? this.allergies : [];
    this.diseases = tm.jquery.Utils.isArray(this.diseases) ? this.diseases : [];
  },

  /**
   * @return {String|null}
   */
  getPatientName: function()
  {
    return this.patientName;
  },

  /**
   * @return {Date|null}
   */
  getBirthDate: function()
  {
    return this.birthDate;
  },

  /**
   * @return {Number|null}
   */
  getWeightInKg: function()
  {
    return this.weightInKg;
  },

  /**
   * @return {Number|null}
   */
  getHeightInCm: function()
  {
    return this.heightInCm;
  },

  /**
   * @return {string|null}
   */
  getGender: function()
  {
    return this.gender;
  },

  /**
   * @return {Array<{id: string, name: string}>}
   */
  getDiseases: function()
  {
    return this.diseases;
  },

  /**
   * @return {Array<{id: string, name: string}>|undefined}
   */
  getAllergies: function()
  {
    return this.hasAllergiesInformation() &&
    this.getAllergiesStatus() === app.views.medications.TherapyEnums.allergiesStatus.PRESENT ?
        this.allergies :
        undefined;
  },

  /**
   * @return {String} {@link app.views.medications.TherapyEnums.allergiesStatus}
   */
  getAllergiesStatus: function()
  {
    return this.allergiesStatus;
  },

  /**
   * return {Boolean}
   */
  hasAllergiesInformation: function()
  {
    return !!this.getAllergiesStatus();
  },

  /**
   * @return {Date|null}
   */
  getHospitalizationStart: function()
  {
    if (!!this.centralCaseDto &&
        this.centralCaseDto.outpatient === false &&
        !!this.centralCaseDto.centralCaseEffective &&
        !!this.centralCaseDto.centralCaseEffective.startMillis)
    {
      return new Date(this.centralCaseDto.centralCaseEffective.startMillis);
    }
    return null;
  },

  /**
   * @return {{outpatient: boolean, centralCaseId: string, centralCaseEffective: null|{startMillis: number,
   * endMillis: number}, episodeId: string, careProvider: Object}|null}
   */
  getCentralCaseDto: function()
  {
    return this.centralCaseDto;
  },

  /**
   * @param {Date} date
   */
  isDischargedAfter: function(date)
  {
    if (!tm.jquery.Utils.isDate(date) ||
        !this.centralCaseDto ||
        !this.centralCaseDto.centralCaseEffective ||
        !this.centralCaseDto.centralCaseEffective.endMillis ||
        this.centralCaseDto.centralCaseEffective.endMillis === this._intervalPositiveInfinityMillis)
    {
      return false
    }

    return this.centralCaseDto.centralCaseEffective.endMillis > date.getTime();
  },

  /**
   * @return {boolean}
   */
  isWitnessingRequired: function()
  {
    return this.witnessingRequired === true;
  }
});