Class.define('app.views.medications.mentalHealth.MentalHealthTherapyContainerData', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  therapy: null,

  _id: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._id = tm.jquery.Utils.createGUID();
  },

  /**
   * @return {string|null|undefined} of type {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   * @override to point to value to {@link app.views.medications.mentalHealth.dto.MentalHealthTherapy#getTherapyStatusEnum)
   */
  getTherapyStatus: function()
  {
    return this.getTherapy().getTherapyStatusEnum();
  },

  /***
   * @return {string} unique ID used to track elements when they are removed from the basket.
   */
  getId: function()
  {
    // noinspection JSValidateTypes
    return this._id;
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.mentalHealthGroupEnum}
   */
  getGroup: function()
  {
    return this.getTherapy().getMentalHealthGroup();
  },

  /**
   * @return {Boolean}
   */
  isTherapyActive: function()
  {
    return !!this.getTherapy() && this.getTherapy().isActive();
  }
});
