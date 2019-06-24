Class.define('app.views.medications.pharmacists.dto.PharmacistMedicationReview', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      config.relatedTherapies = tm.jquery.Utils.isArray(config.relatedTherapies) ? config.relatedTherapies : [];
      config.relatedTherapies = config.relatedTherapies
          .map(app.views.medications.pharmacists.dto.PharmacistReviewTherapy.fromJson);

      config.createTimestamp = tm.jquery.Utils.isEmpty(config.createTimestamp) ? null : new Date(config.createTimestamp);
      config.reminderDate = tm.jquery.Utils.isEmpty(config.reminderDate) ? null : new Date(config.reminderDate);
      config.drugRelatedProblem = tm.jquery.Utils.isEmpty(config.drugRelatedProblem) ? null :
          app.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.drugRelatedProblem);
      config.pharmacokineticIssue = tm.jquery.Utils.isEmpty(config.pharmacokineticIssue) ? null :
          app.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.pharmacokineticIssue);
      config.patientRelatedProblem = tm.jquery.Utils.isEmpty(config.patientRelatedProblem) ? null :
          app.views.medications.pharmacists.dto.TherapyProblemDescription.fromJson(config.patientRelatedProblem);

      return new app.views.medications.pharmacists.dto.PharmacistMedicationReview(config);
    }
  },
  compositionUid: null, /* string */
  composer: null, /* NamedIdentity */
  createTimestamp: null, /* Date */
  relatedTherapies: null, /* array */
  noProblem: null, /* boolean */
  overallRecommendation: null, /* string */
  referBackToPrescriber: null, /* boolean */
  pharmacistReviewStatus: null, /* string enum */
  reminderDate: null,
  reminderNote: null, /* string */
  medicationSupplyTypeEnum: null, /* string enum */
  daysSupply: null, /* Integer */
  mostRecentReview: null, /* boolean */

  drugRelatedProblem: null, /* app.views.medications.pharmacists.dto.TherapyProblemDescription */
  pharmacokineticIssue: null, /* app.views.medications.pharmacists.dto.TherapyProblemDescription */
  patientRelatedProblem: null, /* app.views.medications.pharmacists.dto.TherapyProblemDescription */

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.relatedTherapies = this.getConfigValue("relatedTherapies", []);
    this.pharmacistReviewStatus = this.getConfigValue("pharmacistReviewStatus",
        app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.DRAFT);
  },

  /* getters and setters */
  getCompositionUid: function ()
  {
    return this.compositionUid;
  },
  setCompositionUid: function (value)
  {
    this.compositionUid = value;
  },
  getComposer: function ()
  {
    return this.composer;
  },
  setComposer: function (value)
  {
    this.composer = value;
  },
  getCreateTimestamp: function ()
  {
    return this.createTimestamp;
  },
  setCreateTimestamp: function (value)
  {
    this.createTimestamp = value;
  },
  /**
   * @return {app.views.medications.pharmacists.dto.PharmacistReviewTherapy[]}
   */
  getRelatedTherapies: function ()
  {
    return this.relatedTherapies;
  },
  getNoProblem: function ()
  {
    return this.noProblem;
  },
  setNoProblem: function (value)
  {
    this.noProblem = value;
  },
  getOverallRecommendation: function ()
  {
    return this.overallRecommendation;
  },
  setOverallRecommendation: function (value)
  {
    this.overallRecommendation = value;
  },
  getReferBackToPrescriber: function ()
  {
    return this.referBackToPrescriber;
  },
  setReferBackToPrescriber: function (value)
  {
    this.referBackToPrescriber = value;
  },
  getDrugRelatedProblem: function ()
  {
    return this.drugRelatedProblem;
  },
  setDrugRelatedProblem: function (value)
  {
    this.drugRelatedProblem = value;
  },
  getPharmacokineticIssue: function ()
  {
    return this.pharmacokineticIssue;
  },
  setPharmacokineticIssue: function (value)
  {
    this.pharmacokineticIssue = value;
  },
  getPatientRelatedProblem: function ()
  {
    return this.patientRelatedProblem;
  },
  setPatientRelatedProblem: function (value)
  {
    this.patientRelatedProblem = value;
  },
  isDraft: function()
  {
    return this.pharmacistReviewStatus === app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.DRAFT;
  },
  markReviewed: function()
  {
    this.setPharmacistReviewStatus(app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.REVIEWED);
  },
  isReviewed: function()
  {
    return this.pharmacistReviewStatus === app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.REVIEWED;
  },
  markAuthorized: function ()
  {
    this.setPharmacistReviewStatus(app.views.medications.TherapyEnums.pharmacistReviewStatusEnum.FINAL);
  },
  getPharmacistReviewStatus: function ()
  {
    return this.pharmacistReviewStatus;
  },
  setPharmacistReviewStatus: function (value)
  {
    this.pharmacistReviewStatus = value;
  },
  getReminderDate: function()
  {
    return this.reminderDate;
  },
  setReminderDate: function(value)
  {
    this.reminderDate = value;
  },
  getReminderNote: function()
  {
    return this.reminderNote;
  },
  setReminderNote: function(value)
  {
    this.reminderNote = value;
  },
  getMedicationSupplyType: function()
  {
      return this.medicationSupplyTypeEnum;
  },
  setMedicationSupplyType: function(value)
  {
    this.medicationSupplyTypeEnum = value;
  },
  getDaysSupply: function()
  {
    return this.daysSupply;
  },
  setDaysSupply: function(value)
  {
    this.daysSupply = value;
  },
  isMostRecentReview: function()
  {
    return this.mostRecentReview;
  },
  setMostRecentReview: function(value)
  {
    this.mostRecentReview = value;
  }
});