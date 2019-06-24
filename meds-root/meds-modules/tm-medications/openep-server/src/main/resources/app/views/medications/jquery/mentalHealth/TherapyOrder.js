Class.define('app.views.medications.mentalHealth.TherapyOrder', 'app.views.medications.ordering.AbstractTherapyOrder', {
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type string|null the ID of the {@link app.views.medications.mentalHealth.MentalHealthTherapyContainerData} */
  sourceId: null,
  /** @type string|null of {@link app.views.medications.TherapyEnums.mentalHealthGroupEnum} */
  group: null,

  /**
   * @override
   * @param {boolean} [startEndTimeAvailable=true]
   * @return {app.views.medications.mentalHealth.TherapyOrder}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.mentalHealth.TherapyOrder({
      therapy: this.therapy.clone(true),
      sourceId: this.sourceId,
      group: this.group
    });
  },

  /**
   * @return {string|app.views.medications.TherapyEnums.mentalHealthGroupEnum|null}
   */
  getGroup: function()
  {
    return this.group;
  },

  /**
   * @return {string|null} the ID of the {@link app.views.medications.mentalHealth.MentalHealthTherapyContainerData} used
   * to create the is order, if any.
   */
  getSourceId: function()
  {
    return this.sourceId;
  },

  /**
   * @param {string|app.views.medications.TherapyEnums.mentalHealthGroupEnum} value
   * @return {app.views.medications.mentalHealth.TherapyOrder}
   */
  setGroup: function(value)
  {
    this.group = value;
    return this;
  },

  /**
   * @return {{medication: app.views.medications.mentalHealth.dto.MentalHealthMedication|undefined,
   * template: app.views.medications.mentalHealth.dto.MentalHealthTemplate|undefined}}
   */
  getOrderDetails: function()
  {
    var mentalHealthTherapyGroups = [
      app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ABORTED,
      app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ACTIVE,
      app.views.medications.TherapyEnums.mentalHealthGroupEnum.NEW_MEDICATION];

    var containsMedication = mentalHealthTherapyGroups.indexOf(this.getGroup()) > -1;

    return {
      medication: containsMedication ? this.getTherapy().getMentalHealthMedication() : undefined,
      template: containsMedication ? undefined : this.getTherapy()
    }
  }
});
