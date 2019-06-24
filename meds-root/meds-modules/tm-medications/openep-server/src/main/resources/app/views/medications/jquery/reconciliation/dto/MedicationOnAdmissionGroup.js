Class.define('app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup', 'app.views.medications.reconciliation.dto.MedicationGroup', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup({
        groupEnum: jsonObject.groupEnum,
        groupName: jsonObject.groupName,
        lastUpdateTime: !!jsonObject.lastUpdateTime ? new Date(jsonObject.lastUpdateTime) : null,
        groupElements: tm.jquery.Utils.isArray(jsonObject.groupElements) ?
            jsonObject.groupElements.map(app.views.medications.reconciliation.dto.SourceMedication.fromJson) :
            []
      });
    }
  },

  lastUpdateTime: null,

  /**
   * @return {Date|null}
   */
  getLastUpdateTime: function()
  {
    return this.lastUpdateTime;
  },

  /**
   * @return {boolean} true if this group represents the last discharge medications.
   */
  isLastDischargeMedicationsGroup: function()
  {
    return this.groupEnum === app.views.medications.TherapyEnums.therapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS
  },

  /**
   * @return {boolean} true, if this group represents the therapies prescribed during the last hospitalization.
   */
  isLastHospitalizationTherapiesGroup: function()
  {
    return this.groupEnum === app.views.medications.TherapyEnums.therapySourceGroupEnum.LAST_HOSPITALIZATION;
  },

  /**
   * Applies the specified sources of information to all group members's therapies that have no source of information set.
   * @param {Array<app.views.medications.common.dto.InformationSource>|null} values
   */
  applyDefaultInformationSource: function(values)
  {
    this.getGroupElements()
        .map(
            function extractTherapy(groupElement)
            {
              return groupElement.getTherapy();
            })
        .filter(
            function isSourcesEmpty(therapy)
            {
              return !!therapy && therapy.getInformationSources().length === 0;
            })
        .forEach(
            function setDefault(therapy)
            {
              therapy.setInformationSources(values);
            });
  }
});