Class.define('app.views.medications.common.overview.TherapyRowGroupData', 'tm.jquery.Object', {
  /** @type string */
  key: '',
  /** @type string|null - when not set the key is used */
  sortOrder: null,
  /** @type Array<Object> */
  elements: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.elements = tm.jquery.Utils.isArray(this.elements) ? this.elements : [];
  },

  /**
   * @return {string|null}
   */
  getKey: function()
  {
    return this.key;
  },

  /**
   * Escaped HTML version of of {@link app.views.medications.common.overview.TherapyRowGroupData#getKey}.
   * @return {string}
   */
  getKeyDisplayValue: function()
  {
    return tm.jquery.Utils.escapeHtml(this.key);
  },

  /**
   * @return {string}
   */
  getSortOrderValue: function()
  {
    return this.sortOrder == null ? this.getKey().toLowerCase() : this.sortOrder;
  },

  /**
   * @return {Array<*>}
   */
  getElements: function()
  {
    return this.elements;
  },

  /**
   * @param {object} therapyRowDataObject TherapyRowData, TherapyFlowRow and similar DTOs.
   */
  addTherapyRowData: function(therapyRowDataObject)
  {
    this.getElements().push(therapyRowDataObject);
  }
});
