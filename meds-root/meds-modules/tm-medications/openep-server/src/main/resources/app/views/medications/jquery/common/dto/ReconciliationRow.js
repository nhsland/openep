Class.define('app.views.medications.common.dto.ReconciliationRow', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;
      var config = jQuery.extend(true, {}, jsonObject);

      config.therapyOnAdmission = !tm.jquery.Utils.isEmpty(jsonObject.therapyOnAdmission) ?
          app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapyOnAdmission) :
          null;
      config.therapyOnDischarge = !tm.jquery.Utils.isEmpty(jsonObject.therapyOnDischarge) ?
          app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapyOnDischarge) :
          null;
      config.changeReasonDto = !tm.jquery.Utils.isEmpty(jsonObject.changeReasonDto) ?
          app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReasonDto) :
          null;
      config.changes = tm.jquery.Utils.isArray(jsonObject.changes) ?
          jsonObject.changes.map(app.views.medications.common.dto.TherapyChange.fromJson) :
          config.changes;

      return new app.views.medications.common.dto.ReconciliationRow(config);
    }
  },
  groupEnum: null,
  therapyOnAdmission: null,
  therapyOnDischarge: null,
  changeReasonDto: null,
  statusEnum: null,
  changes: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.changes = tm.jquery.Utils.isArray(this.changes) ? this.changes : [];
  },

  getGroupEnum: function()
  {
    return this.groupEnum
  },

  /**
   * @return {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy}
   */
  getTherapyOnAdmission: function()
  {
    return this.therapyOnAdmission;
  },

  /**
   * @return {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy}
   */
  getTherapyOnDischarge: function()
  {
    return this.therapyOnDischarge;
  },

  /**
   * @return {app.views.medications.common.dto.TherapyChangeReason|null}
   */
  getChangeReason: function()
  {
    return this.changeReasonDto;
  },

  /**
   * @return {String}
   */
  getStatusEnum: function()
  {
    return this.statusEnum;
  },

  /**
   * @return {Array<app.views.medications.common.dto.TherapyChange>}
   */
  getChanges: function()
  {
    return this.changes;
  },

  /**
   * True, if the admission therapy state is present.
   * @return {boolean}
   */
  isTherapyOnAdmissionPresent: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTherapyOnAdmission());
  },

  /**
   * True, if the discharge therapy state is present.
   * @return {boolean}
   */
  isTherapyOnDischargePresent: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTherapyOnDischarge());
  }
});