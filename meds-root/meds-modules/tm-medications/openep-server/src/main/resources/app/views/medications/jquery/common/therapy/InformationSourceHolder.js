Class.define('app.views.medications.common.therapy.InformationSourceHolder', 'tm.jquery.Object', {
  view: null,

  _knownInformationSources: undefined,

  /**
   * This jsClass represents a simple coded list holder for source of information of therapies. Ideally it should
   * be used as a singleton as it will automatically attempt to load the data from the backend API during construction.
   * Since the retrieval is async, you may check the state of initialization by calling {@link #isLoaded}, which will return
   * false until the data is successfully loaded. Does not handle network failure at this time.
   * @param {Object|undefined} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._loadKnownInformationSources();
  },

  /**
   * @return {boolean} true, if the sources were loaded successfully.
   */
  isLoaded: function()
  {
    return tm.jquery.Utils.isArray(this._knownInformationSources);
  },

  /**
   * @param {function(source: app.views.medications.common.dto.InformationSource):boolean} predicate by which to match
   * the desired sources. {@see app.views.medications.common.therapy.InformationSourceFilterBuilder.js}
   * @returns {Array<app.views.medications.common.dto.InformationSource>} the list of available sources, matched by the
   * passed predicate, or an empty array if no sources match.
   */
  getSources: function(predicate)
  {
    return this._knownInformationSources.filter(predicate);
  },

  /**
   * @return {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Loads the array of sources from the API and caches it internally as {@link #_knownInformationSources}.
   * @private
   */
  _loadKnownInformationSources: function()
  {
    var self = this;

    this.getView()
        .getRestApi()
        .loadInformationSources(true)
        .then(function onLoad(sources)
        {
          self._knownInformationSources = sources;
        });
  }
});