Class.define('app.views.medications.common.dto.TherapyActionHistory', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.actionPerformedTime = new Date(jsonObject.actionPerformedTime);
      config.actionTakesEffectTime = !tm.jquery.Utils.isEmpty(jsonObject.actionTakesEffectTime) ?
          new Date(jsonObject.actionTakesEffectTime):
          null;
      config.changeReason = !tm.jquery.Utils.isEmpty(jsonObject.changeReason) ?
          app.views.medications.common.dto.TherapyChangeReason.fromJson(jsonObject.changeReason):
          null;
      config.changes = jsonObject.changes.map(app.views.medications.common.dto.TherapyChange.fromJson);

      return new app.views.medications.common.dto.TherapyActionHistory(config);
    }
  },
  actionPerformedTime: null, /* Date */
  actionTakesEffectTime: null, /* Date|null */
  performer: null, /* String */
  therapyActionHistoryType: null, /* String */
  changeReason: null, /* app.views.medications.common.dto.TherapyChangeReason */
  changes: null, /* Array(app.views.medications.common.dto.TherapyChange) */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },
  
  /**
   * @returns {Date}
   */
  getActionPerformedTime: function()
  {
    return this.actionPerformedTime;
  },
  
  /**
   * @param {Date} actionPerformedTime
   */
  setActionPerformedTime: function(actionPerformedTime)
  {
    this.actionPerformedTime = actionPerformedTime;
  },
  
  /**
   * @returns {Date|null}
   */
  getActionTakesEffectTime: function()
  {
    return this.actionTakesEffectTime;
  },
  
  /**
   * @param {Date|null} actionTakesEffectTime
   */
  setActionTakesEffectTime: function(actionTakesEffectTime)
  {
    this.actionTakesEffectTime = actionTakesEffectTime;
  },
  
  /**
   * @returns {String}
   */
  getPerformer: function()
  {
    return this.performer;
  },
  
  /**
   * @param {String} performer
   */
  setPerformer: function(performer)
  {
    this.performer = performer;
  },
  
  /**
   * @returns {String}
   */
  getTherapyActionHistoryType: function()
  {
    return this.therapyActionHistoryType;
  },
  
  /**
   * @param {String} therapyActionHistoryType
   */
  setTherapyActionHistoryType: function(therapyActionHistoryType)
  {
    this.therapyActionHistoryType = therapyActionHistoryType;
  },
  
  /**
   * @returns {app.views.medications.common.dto.TherapyChangeReason|null}
   */
  getChangeReason: function()
  {
    return this.changeReason;
  },
  
  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason|null} changeReason
   */
  setChangeReason: function(changeReason)
  {
    this.changeReason = changeReason;
  },
  
  /**
   * @returns {Array}
   */
  getChanges: function()
  {
    return this.changes;
  },
  
  /**
   * @param {Array} changes
   */
  setChanges: function(changes)
  {
    this.changes = changes;
  }
});