Class.define('app.views.medications.common.therapy.ChangeReasonTypeHolder', 'tm.jquery.Object', {
  view: null,

  _knownChangeReasonMap: null,

  /**
   * This jsClass represents a simple coded list holder for therapy change reason map. Ideally it should
   * be used as a singleton service as it will automatically attempt to load the data from the backend API once constructed.
   * You may check the state of initialization by calling {@link getMap}, which will return
   * null until the data was successfully loaded.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._loadKnownChangeReasons();
  },

  /**
   * @return {null|Object.<string, Array<{{id: string, name: string>>}
   */
  getMap: function()
  {
    return this._knownChangeReasonMap;
  },

  /**
   * @return {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Loads the data from the API and caches it internally for further use. While the data is loading,
   * {@link getMap} will be returning null.
   * @private
   */
  _loadKnownChangeReasons: function()
  {
    var self = this;
    this.getView()
        .getRestApi()
        .loadTherapyChangeReasonTypeMap(true)
        .then(
            function(changeReasons)
            {
              self._knownChangeReasonMap = changeReasons;
            }
        )
  }
});