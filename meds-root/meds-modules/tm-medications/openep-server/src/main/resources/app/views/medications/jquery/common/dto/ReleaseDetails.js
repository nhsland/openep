Class.define('app.views.medications.common.dto.ReleaseDetails', 'tm.jquery.Object', {
  type: app.views.medications.TherapyEnums.releaseType.MODIFIED_RELEASE,
  hours: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {String} {@see app.views.medications.TherapyEnums.releaseType}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @returns {Number|null}
   */
  getHours: function()
  {
    return this.hours;
  },

  /**
   * @return {boolean} true, if the type is modified release, otherwise it's presumed it's gastro resistant.
   */
  isModifiedRelease: function()
  {
    return this.type === app.views.medications.TherapyEnums.releaseType.MODIFIED_RELEASE;
  }
});