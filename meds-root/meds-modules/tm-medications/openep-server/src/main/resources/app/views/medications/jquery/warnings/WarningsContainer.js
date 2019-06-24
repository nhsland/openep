Class.define('app.views.medications.warnings.WarningsContainer', 'app.views.medications.warnings.BaseWarningsContainer', {
  warnings: null,

  /**
   * Returns an instance of a container that displays the given therapy warnings as a readonly list (no override).
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.setMedicationWarnings(this.getWarnings());
    this.handleLowSeverityWarningsBtnVisibility();
    this.hideLoadingMask();
    this.getList().on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, this._onListRender.bind(this));
  },

  /**
   * @protected
   * @return {app.views.medications.warnings.dto.MedicationsWarnings}
   */
  getWarnings: function()
  {
    return this.warnings;
  },

  /**
   * @override
   * @param {app.views.medications.warnings.dto.MedicationsWarning} item
   * @return {app.views.medications.warnings.WarningsContainerRow}
   */
  buildListRow: function(item)
  {
    return new app.views.medications.warnings.WarningsContainerRow({
      view: this.getView(),
      warning: item,
      overrideAvailable: false
    });
  },

  /**
   * Event handler for the internal list's render event - acts as an auto loading mechanism for {@link tm.jquery.List}.
   * @private
   */
  _onListRender: function()
  {
    this.refreshWarningsList();
  }
});