Class.define('app.views.medications.reconciliation.dto.MedicationGroup', 'tm.jquery.Object', {

  groupName: null, /* string */
  groupEnum: null, /* string */
  groupElements: null, /* array of therapy DTOs */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.groupElements = this.getConfigValue("groupElements", []);
  },

  getName: function()
  {
    return this.groupName;
  },
  getGroupEnum: function()
  {
    return this.groupEnum;
  },

  /**
   * @return {Array<app.views.medications.reconciliation.dto.SourceMedication>}
   */
  getGroupElements: function()
  {
    return this.groupElements;
  }
});