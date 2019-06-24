Class.define('app.views.medications.common.dto.TherapyViewPatient', 'tm.jquery.Object', {
  statics: {
    /**
     * @param {*} jsonObject
     * @param {string} patientId for which this data was loaded, so the caller can ensure it's still valid for the active
     * patient.
     * @return {*}
     */
    fromJson: function(jsonObject, patientId)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.referenceWeightDate = !!jsonObject.referenceWeightDate ? new Date(jsonObject.referenceWeightDate) : null;
      config.patientData = app.views.medications.common.dto.PatientDataForMedications.fromJson(config.patientData);

      return new app.views.medications.common.dto.TherapyViewPatient(config).markPatientId(patientId);
    }
  },
  patientData: null,
  roundsInterval: null,
  administrationTiming: null,
  customGroups: null,
  referenceWeight: null,
  referenceWeightDate: null,
  lastLinkName: null,
  recentHospitalization: false,

  _patientId: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.customGroups = tm.jquery.Utils.isArray(this.customGroups) ? this.customGroups : [];
  },

  /**
   * @return {app.views.medications.common.dto.PatientDataForMedications|null}
   */
  getPatientData: function()
  {
    return this.patientData;
  },

  /**
   * @return {{startHour: Number, startMinute: Number, endHour: Number, endMinute: Number}}
   */
  getRoundsInterval: function()
  {
    return this.roundsInterval;
  },
  /**
   * @return {{timestampsList: []}}
   */
  getAdministrationTiming: function()
  {
    return this.administrationTiming;
  },

  /**
   * @return {Array<*>}
   */
  getCustomGroups: function()
  {
    return this.customGroups;
  },

  /**
   * @return {Number|null}
   */
  getReferenceWeight: function()
  {
    return this.referenceWeight;
  },

  /**
   * @param {Number} value
   */
  setReferenceWeight: function(value)
  {
    this.referenceWeight = value;
    this.referenceWeightDate = CurrentTime.get();
  },

  /**
   * @return {Date|null}
   */
  getReferenceWeightDate: function()
  {
    return this.referenceWeightDate;
  },

  /**
   * @return {String}
   */
  getLastLinkName: function()
  {
    return this.lastLinkName;
  },

  /**
   * @param {String} value
   */
  setLastLinkName: function(value)
  {
    this.lastLinkName = value;
  },

  /**
   * @return {string}
   */
  getNextLinkName: function()
  {
    if (!this.lastLinkName || this.lastLinkName === 'Z')
    {
      return 'A1';
    }

    return String.fromCharCode(this.lastLinkName.charCodeAt(0) + 1) + 1;
  },

  /**
   * @return {Boolean}
   */
  isRecentHospitalization: function()
  {
    return this.recentHospitalization === true;
  },

  /**
   * @param {string|null} value the patient ID for which this data was loaded. See {@link #isForPatient}.
   * @return {app.views.medications.common.dto.TherapyViewPatient}
   */
  markPatientId: function(value)
  {
    this._patientId = value;
    return this;
  },

  /**
   * @param {string} patientId
   * @return {boolean} true, if the data is intended for the passed patient id, otherwise false. When false, the
   * object should be discarded and a new one requested from the API.
   */
  isForPatient: function(patientId)
  {
    return this._patientId === patientId;
  }
});