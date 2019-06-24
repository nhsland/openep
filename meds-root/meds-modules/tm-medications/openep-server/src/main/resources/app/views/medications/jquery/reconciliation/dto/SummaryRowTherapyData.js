Class.define('app.views.medications.reconciliation.dto.SummaryRowTherapyData', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type app.views.medications.common.dto.TherapyChangeReason */
  changeReasonDto: null,
  /** @type String app.views.medications.TherapyEnums.therapyStatusEnum */
  therapyStatus: null,
  /** @type {Array<app.views.medications.common.dto.TherapyChange>} */
  changes: null,
  /** @type Boolean marks the therapy as changed */
  modifiedFromLastReview: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.changes = this.getConfigValue("changes", []);

    if (this.changes.length > 0)
    {
      this.modifiedFromLastReview = true;
    }
  },

  setTherapyStatus: function(value)
  {
    this.therapyStatus = value;
  },

  setModifiedFromLastReview: function(value)
  {
    this.modifiedFromLastReview = value;
  },

  isModifiedFromLastReview: function()
  {
    return this.modifiedFromLastReview === true;
  }
});