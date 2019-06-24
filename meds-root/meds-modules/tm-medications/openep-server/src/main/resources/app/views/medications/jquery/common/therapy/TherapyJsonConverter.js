Class.define('app.views.medications.common.TherapyJsonConverter', 'tm.jquery.Object', {
  statics: {
    /**
     * @param jsonObject
     * @returns {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy}
     */
    convert: function(jsonObject)
    {
      if (jsonObject.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.OXYGEN)
      {
        return app.views.medications.common.dto.OxygenTherapy.fromJson(jsonObject);
      }

      return app.views.medications.common.dto.Therapy.fromJson(jsonObject);
    }
  }
});