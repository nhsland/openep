Class.define('app.views.medications.common.dto.PrescriptionPackage', 'tm.jquery.Object', {
  prescriptionPackageId: null,
  compositionUid: null,
  prescriptionTherapies: null,
  composer: null,
  lastUpdateTimestamp: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.prescriptionTherapies = tm.jquery.Utils.isArray(this.prescriptionTherapies) ? this.prescriptionTherapies : [];
  },

  /**
   * @param {Array<app.views.medications.common.dto.PrescriptionTherapy>} prescriptionTherapies
   */
  setPrescriptionTherapies: function(prescriptionTherapies)
  {
    this.prescriptionTherapies = tm.jquery.Utils.isArray(prescriptionTherapies) ? prescriptionTherapies : [];
  },

  /**
   * @return {string|null}
   */
  getPrescriptionPackageId: function()
  {
    return this.prescriptionPackageId;
  },

  /**
   * @return {string|null}
   */
  getCompositionUid: function()
  {
    return this.compositionUid;
  },

  /**
   * @return {Array<app.views.medications.common.dto.PrescriptionTherapy>}
   */
  getPrescriptionTherapies: function()
  {
    return this.prescriptionTherapies;
  },

  /**
   * @return {{name: string, id: string}|null}
   */
  getComposer: function()
  {
    return this.composer;
  },

  /**
   * @return {Date|null}
   */
  getLastUpdateTimestamp: function()
  {
    return this.lastUpdateTimestamp;
  }
});