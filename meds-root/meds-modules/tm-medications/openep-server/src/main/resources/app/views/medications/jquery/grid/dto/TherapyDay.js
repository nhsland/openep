Class.define('app.views.medications.grid.dto.TherapyDay', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  statics: {
    fromJson: function(jsonObject){
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.therapy = app.views.medications.common.TherapyJsonConverter.convert(config.therapy);
      config.medicationProperties = config.medicationProperties.map(
          function(property)
          {
            return new app.views.medications.common.dto.MedicationProperty(property);
          });

      return new app.views.medications.grid.dto.TherapyDay(config);
    }
  },
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type String of {@link #app.views.medications.TherapyEnums.therapyStatusEnum} */
  therapyStatus: null,
  /** @type Date */
  originalTherapyStart: null,
  /** @type Array<app.views.medications.common.dto.MedicationProperty> */
  medicationProperties: null,
  /** @type Boolean */
  containsNonFormularyMedications: false,
  /** @type Boolean */
  therapyEndsBeforeNextRounds: false,
  /** @type String */
  statusReason: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medicationProperties = this.getConfigValue("medicationProperties", []);
  },

  getMedicationProperties: function()
  {
    return this.medicationProperties;
  },

  hasNonFormularyMedications: function()
  {
    return this.containsNonFormularyMedications === true;
  },

  setTherapyStatus: function(therapyStatus)
  {
    this.therapyStatus = therapyStatus;
  },

  setDoctorReviewNeeded: function(doctorReviewNeeded)
  {
    this.doctorReviewNeeded = doctorReviewNeeded;
  },

  setTherapyEndsBeforeNextRounds: function(therapyEndsBeforeNextRounds)
  {
    this.therapyEndsBeforeNextRounds = therapyEndsBeforeNextRounds;
  },

  setStatusReason: function(statusReason)
  {
    this.statusReason = statusReason;
  }
});