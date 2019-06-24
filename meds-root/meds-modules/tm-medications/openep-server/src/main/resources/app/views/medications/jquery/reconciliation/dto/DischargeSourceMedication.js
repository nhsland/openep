Class.define('app.views.medications.reconciliation.dto.DischargeSourceMedication', 'app.views.medications.reconciliation.dto.SourceMedication', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.DischargeSourceMedication({
        therapy: app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy),
        sourceId: jsonObject.sourceId,
        status: jsonObject.status,
        changeReason: app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReason),
        validationIssues: tm.jquery.Utils.isArray(jsonObject.validationIssues) ?
            jsonObject.validationIssues.slice(0) :
            []
      });
    }
  },
  status: null, /* app.views.medications.TherapyEnums.therapyStatusEnum  */
  changeReason: null, /* app.views.medications.common.dto.TherapyChangeReason */

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @return {string|null} status of {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   */
  getStatus: function()
  {
    return this.status;
  },

  /**
   * @override {@link app.views.medications.ordering.AbstractTherapyOrder#getTherapyStatus}
   * @return {string|null}
   */
  getTherapyStatus: function()
  {
    return this.status;
  },

  /**
   * @override {@link app.views.medications.ordering.AbstractTherapyOrder#getTherapyChangeReason} and link to
   * a different property name.
   * @return {app.views.medications.common.dto.TherapyChangeReason|null}
   */
  getTherapyChangeReason: function()
  {
    return this.changeReason;
  },

  /**
   * @param {string|null} status of {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   */
  setStatus: function(status)
  {
    this.status = status;
  }
});