Class.define('app.views.medications.common.overview.TherapyRowCustomGroupData', 'app.views.medications.common.overview.TherapyRowGroupData', {
  sortOrder: '000000',

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Escaped HTML version of of {@link app.views.medications.common.overview.TherapyRowGroupData#getKey}. The hidden
   * element is used by our grid component.
   * @return {string}
   * @override
   */
  getKeyDisplayValue: function()
  {
    return '<span style="display:none;">' + tm.jquery.Utils.escapeHtml(this.getSortOrderValue()) + '</span>' +
        tm.jquery.Utils.escapeHtml(this.getKey());
  }
});