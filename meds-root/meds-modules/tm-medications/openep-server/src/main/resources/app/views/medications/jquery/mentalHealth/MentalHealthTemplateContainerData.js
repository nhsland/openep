Class.define('app.views.medications.mentalHealth.MentalHealthTemplateContainerData', 'app.views.medications.mentalHealth.MentalHealthTherapyContainerData', {
  /**
   * The purpose of this class is to adopt {@link app.views.medications.mentalHealth.MentalHealthTherapyContainerData}
   * to instances of {@link app.views.medications.mentalHealth.dto.MentalHealthTemplate}, which have less properties
   * than instances of {@link app.views.medications.mentalHealth.dto.MentalHealthTherapy}, while enabling us to treat
   * all instances as {@link app.views.medications.mentalHealth.MentalHealthTherapyContainerData} when communicating with
   * the ordering dialog components.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.mentalHealthGroupEnum}
   */
  getGroup: function()
  {
    return app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES;
  },

  /**
   * @return {Boolean}
   */
  isTherapyActive: function()
  {
    return true;
  },

  /**
   * @override as a template element has no status.
   */
  getTherapyStatus: function()
  {
    return undefined;
  }
});
