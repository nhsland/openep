Class.define('app.views.medications.grid.dto.TherapyReloadAfterAction', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);
      config.therapyStart = tm.jquery.Utils.isEmpty(jsonObject.therapyStart) ? null : new Date(jsonObject.therapyStart);
      config.therapyEnd = tm.jquery.Utils.isEmpty(jsonObject.therapyEnd) ? null : new Date(jsonObject.therapyEnd);

      return new app.views.medications.grid.dto.TherapyReloadAfterAction(config);
    }
  },
  /** @type String */
  ehrCompositionId: null,
  /** @type String */
  ehrOrderName: null,
  /** @type String of {@link #app.views.medications.TherapyEnums.therapyStatusEnum} */
  therapyStatus: null,
  /** @type Boolean */
  doctorReviewNeeded: null,
  /** @type Boolean */
  therapyEndsBeforeNextRounds: null,
  /** @type Date */
  therapyStart: null,
  /** @type Date */
  therapyEnd: null,
  /** @type String */
  statusReason: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getEhrCompositionId: function()
  {
    return this.ehrCompositionId;
  },

  getEhrOrderName: function()
  {
    return this.ehrOrderName;
  },

  getTherapyStatus: function()
  {
    return this.therapyStatus;
  },

  getDoctorReviewNeeded: function()
  {
    return this.doctorReviewNeeded;
  },

  getTherapyEndsBeforeNextRounds: function()
  {
    return this.therapyEndsBeforeNextRounds;
  },

  getTherapyStart: function()
  {
    return this.therapyStart;
  },

  getTherapyEnd: function()
  {
    return this.therapyEnd;
  },

  getStatusReason: function()
  {
    return this.statusReason;
  }
});