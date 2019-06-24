Class.define('app.views.medications.common.dto.MedicationDocument', 'tm.jquery.Object', {
  documentReference: null,
  externalSystem: null,
  type: app.views.medications.TherapyEnums.medicationDocumentType.PDF,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {string|null}
   */
  getDocumentReference: function()
  {
    return this.documentReference;
  },

  /**
   * @return {string|null}
   */
  getExternalSystem: function()
  {
    return this.externalSystem;
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.medicationDocumentType}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @return {boolean} true, if the document is hosted on an external URL.
   */
  isExternalLink: function()
  {
    return this.type === app.views.medications.TherapyEnums.medicationDocumentType.URL;
  }
});