Class.define('app.views.medications.common.dto.DispenseSource', 'tm.jquery.Object', {
  statics: {
    /**
     * Utility method for {@link Array#find} and similar functions to quickly locate the default source.
     * @param {app.views.medications.common.dto.DispenseSource} source
     * @return {boolean}
     */
    matchDefault: function(source)
    {
      return source.isDefaultSource();
    }
  },
  defaultSource: false,
  id: null,
  name: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {string|null}
   */
  getName: function()
  {
    return this.name;
  },

  /**
   * @return {Number|null}
   */
  getId: function()
  {
    return this.id;
  },

  /**
   * @return {boolean}
   */
  isDefaultSource: function()
  {
    return this.defaultSource;
  }
});