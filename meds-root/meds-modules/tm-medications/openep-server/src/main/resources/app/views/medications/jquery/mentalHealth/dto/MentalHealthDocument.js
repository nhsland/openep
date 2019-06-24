Class.define('app.views.medications.mentalHealth.dto.MentalHealthDocument', 'tm.jquery.Object', {
  patientId: null,
  mentalHealthDocumentType: null,
  maxDosePercentage: null,
  mentalHealthMedicationDtoList: null,
  mentalHealthTemplateDtoList: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.mentalHealthMedicationDtoList = tm.jquery.Utils.isArray(this.mentalHealthMedicationDtoList) ?
        this.mentalHealthMedicationDtoList :
        [];
    this.mentalHealthTemplateDtoList = tm.jquery.Utils.isArray(this.mentalHealthTemplateDtoList) ?
        this.mentalHealthTemplateDtoList :
        [];
  }
});