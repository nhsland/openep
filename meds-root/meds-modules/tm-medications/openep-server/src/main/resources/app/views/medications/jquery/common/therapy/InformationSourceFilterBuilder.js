Class.define('app.views.medications.common.therapy.InformationSourceFilterBuilder', 'tm.jquery.Object', {
  sourceGroup: undefined,
  _sourceType: undefined,

  /**
   * Constructs a new instance of a builder intended to simplified creation of the the predicate function, which we can use
   * to retrieve matched {@link app.views.medications.common.dto.InformationSource} by calling
   * {@link app.views.medications.common.therapy.InformationSourceHolder#getSources}.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {string} group from {@link app.views.medications.TherapyEnums.informationSourceGroupEnum}
   * @return {app.views.medications.common.therapy.InformationSourceFilterBuilder}
   */
  setSourceGroup: function(group)
  {
    this.sourceGroup = group;
    return this;
  },

  /**
   * @returns {string} from {@link app.views.medications.TherapyEnums.informationSourceGroupEnum}
   */
  getSourceGroup: function()
  {
    return this.sourceGroup;
  },

  /**
   * @param {string|null|undefined} type from {@link app.views.medications.TherapyEnums.informationSourceTypeEnum}
   * @return {app.views.medications.common.therapy.InformationSourceFilterBuilder}
   */
  setSourceType: function(type)
  {
    this._sourceType = type;
    return this;
  },

  /**
   * @return {function(source: app.views.medications.common.dto.InformationSource):boolean} which always matches the value
   * of the source group, provided by {@link #setSourceGroup}, and optionally matches the value of the source type,
   * provided by {@link #setSourceType}, if a value was set.
   */
  build: function()
  {
    var sourceGroup = this.getSourceGroup();
    var sourceType = this._sourceType;

    if (!sourceGroup)
    {
      throw new Error('sourceGroup not defined');
    }

    /**
     * @param {app.views.medications.common.dto.InformationSource} source
     * @returns {boolean}
     */
    return function(source)
    {
      // group must match so we don't return everything for an unmatched context
      return source.getInformationSourceGroup() === sourceGroup &&
          (!sourceType || source.getInformationSourceType() === sourceType);
    }
  }
});
