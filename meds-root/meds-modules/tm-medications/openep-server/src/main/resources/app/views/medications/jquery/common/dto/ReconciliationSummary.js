Class.define('app.views.medications.common.dto.ReconciliationSummary', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject){
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.rows = tm.jquery.Utils.isArray(config.rows) ?
          config.rows.map(app.views.medications.common.dto.ReconciliationRow.fromJson) :
          [];
      config.admissionLastUpdateTime = !!jsonObject.admissionLastUpdateTime ?
          new Date(jsonObject.admissionLastUpdateTime) :
          null;
      config.dischargeLastUpdateTime = !!jsonObject.dischargeLastUpdateTime ?
          new Date(jsonObject.dischargeLastUpdateTime) :
          null;
      config.reconciliationStarted = !!jsonObject.reconciliationStarted ? new Date(jsonObject.reconciliationStarted) : null;

      return new app.views.medications.common.dto.ReconciliationSummary(config);
    }
  },

  /** @type Array<app.views.medications.common.dto.ReconciliationRow> */
  rows: null,
  /** @type boolean */
  admissionReviewed: false,
  /** @type boolean */
  dischargeReviewed: false,
  /** @type Date|null */
  admissionLastUpdateTime: null,
  /** @type Date|null */
  dischargeLastUpdateTime: null,
  /** @type Date|null */
  reconciliationStarted: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.rows = tm.jquery.Utils.isArray(this.rows) ? this.rows : [];
  },

  /**
   * @return {Array<app.views.medications.common.dto.ReconciliationRow>}
   */
  getRows: function()
  {
    return this.rows;
  },

  /**
   * True if row data is present, otherwise false.
   * @returns {boolean}
   */
  isEmpty: function()
  {
    return this.rows.isEmpty();
  },

  /**
   * Returns true if the admission medication list was already reviewed.
   * @return {boolean}
   */
  isMedicationsOnAdmissionReviewed: function()
  {
    return this.admissionReviewed === true;
  },

  /**
   * Returns true if the discharge medication list was already reviewed.
   * @return {boolean}
   */
  isMedicationsOnDischargeReviewed: function()
  {
    return this.dischargeReviewed === true;
  },


  /**
   * Returns true if at least one row contains a the state of a therapy at admission.
   * @returns {boolean}
   */
  hasMedicationOnAdmission: function()
  {
    return this.rows
        .some(function hasAdmissionTherapy(row)
        {
          return row.isTherapyOnAdmissionPresent();
        });
  },

  /**
   * Returns true if at least one row contains the state of a therapy at discharge.
   * @returns {boolean}
   */
  hasMedicationOnDischarge: function()
  {
    return this.rows
        .some(function hasDischargeTherapy(row)
        {
          return row.isTherapyOnDischargePresent();
        });
  },

  /**
   * @return {null|Date}
   */
  getAdmissionLastUpdateTime: function()
  {
    return this.admissionLastUpdateTime;
  },

  /**
   * @return {null|Date}
   */
  getDischargeLastUpdateTime: function()
  {
    return this.dischargeLastUpdateTime;
  },

  /**
   * The date and time when the last reconciliation process was started, or null if it hasn't been started yet.
   * @return {Date|null}
   */
  getReconciliationStartedTime: function()
  {
    return this.reconciliationStarted;
  }
});
