Class.define('app.views.medications.common.dto.TherapyAuditTrail', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.currentTherapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.currentTherapy);
      config.originalTherapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.originalTherapy);
      for (var i = 0; i < jsonObject.actionHistoryList.length; i++)
      {
        jsonObject.actionHistoryList[i] = app.views.medications.common.dto.TherapyActionHistory.fromJson(jsonObject.actionHistoryList[i]);
      }
      config.actionHistoryList = jsonObject.actionHistoryList;

      return new app.views.medications.common.dto.TherapyAuditTrail(config);
    }
  },
  currentTherapy: null, /* app.views.medications.common.dto.Therapy */
  currentTherapyStatus: null,
  originalTherapy: null, /* app.views.medications.common.dto.Therapy */
  actionHistoryList: null, /* Array */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getCurrentTherapy: function()
  {
    return this.currentTherapy;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} currentTherapy
   */
  setCurrentTherapy: function(currentTherapy)
  {
    this.currentTherapy = currentTherapy;
  },

  /**
   * @returns {null|string} from {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   */
  getCurrentTherapyStatus: function()
  {
    return this.currentTherapyStatus;
  },
  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getOriginalTherapy: function()
  {
    return this.originalTherapy;
  },
  
  /**
   * @param {app.views.medications.common.dto.Therapy} originalTherapy
   */
  setOriginalTherapy: function(originalTherapy)
  {
    this.originalTherapy = originalTherapy;
  },

  /**
   * @returns {Array}
   */
  getActionHistoryList: function()
  {
    return this.actionHistoryList;
  },

  /**
   * @param {Array} actionHistoryList
   */
  setActionHistoryList: function(actionHistoryList)
  {
    this.actionHistoryList = actionHistoryList
  }

});
