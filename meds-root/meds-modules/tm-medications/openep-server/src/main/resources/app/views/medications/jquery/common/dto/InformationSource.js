Class.define('app.views.medications.common.dto.InformationSource', 'tm.jquery.Object', {
  id: null,
  name: null,
  informationSourceType: null,
  informationSourceGroup: null,

  /**
   * @param {Object|undefined} config
   * @constructor
   */
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
   * @return {string|null} of {@link app.views.medications.TherapyEnums.informationSourceTypeEnum}
   */
  getInformationSourceType: function()
  {
    return this.informationSourceType;
  },

  /**
   * @return {string|null} of {@link app.views.medications.TherapyEnums.informationSourceGroupEnum}
   */
  getInformationSourceGroup: function()
  {
    return this.informationSourceGroup;
  }
});