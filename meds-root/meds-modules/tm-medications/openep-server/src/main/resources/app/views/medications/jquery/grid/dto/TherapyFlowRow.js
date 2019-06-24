Class.define('app.views.medications.grid.dto.TherapyFlowRow', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);
      if (!!jsonObject.therapyFlowDayMap)
      {
        for (var flowDay in jsonObject.therapyFlowDayMap)
        {
          if (jsonObject.therapyFlowDayMap.hasOwnProperty(flowDay))
          {
            config.therapyFlowDayMap[flowDay] =
                app.views.medications.grid.dto.TherapyDay.fromJson(jsonObject.therapyFlowDayMap[flowDay])
          }
        }
      }
      return new app.views.medications.grid.dto.TherapyFlowRow(config);
    }
  },
  /** @type string|null */
  atcGroupName: null,
  /** @type string */
  atcGroupCode: '',
  /** @type Array<String>|null */
  routes: null,
  /** @type string|null */
  customGroup: null,
  /** @type number|null */
  customGroupSortOrder: null,
  /** @ype Object<number, app.views.medications.grid.dto.TherapyDay>|null */
  therapyFlowDayMap: null,
  /** @type string of {@link app.views.medications.TherapyEnums.prescriptionGroupEnum} */
  prescriptionGroup: app.views.medications.TherapyEnums.prescriptionGroupEnum.REGULAR,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {String|null}
   */
  getAtcGroupName: function()
  {
    return this.atcGroupName;
  },

  /**
   * @param {String|null} atcGroupName
   */
  setAtcGroupName: function(atcGroupName)
  {
    this.atcGroupName = atcGroupName
  },

  /**
   * @returns {String|null}
   */
  getAtcGroupCode: function()
  {
    return this.atcGroupCode;
  },

  /**
   * @param {String|null} atcGroupCode
   */
  setAtcGroupCode: function(atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  },

  /**
   * @returns {Array<String>|null}
   */
  getRoutes: function()
  {
    return this.routes;
  },

  /**
   * @param {Array<String>|null} routes
   */
  setRoutes: function(routes)
  {
    this.routes = routes
  },

  /**
   * @returns {string|null}
   */
  getCustomGroup: function()
  {
    return this.customGroup
  },

  /**
   * @param {string|null} customGroup
   */
  setCustomGroup: function(customGroup)
  {
    this.customGroup = customGroup;
  },

  /**
   * @returns {Object<number, app.views.medications.grid.dto.TherapyDay>|null}
   */
  getTherapyFlowDayMap: function()
  {
    return this.therapyFlowDayMap;
  },

  /**
   * @param {Object<number, app.views.medications.grid.dto.TherapyDay>|null} therapyFlowDayMap
   */
  setTherapyFlowDayMap: function(therapyFlowDayMap)
  {
    this.therapyFlowDayMap = therapyFlowDayMap;
  },

  /**
   * @returns {number|null}
   */
  getCustomGroupSortOrder: function()
  {
    return this.customGroupSortOrder;
  },

  /**
   * @param {number|null} customGroupSortOrder
   */
  setCustomGroupSortOrder: function(customGroupSortOrder)
  {
    this.customGroupSortOrder = customGroupSortOrder;
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.prescriptionGroupEnum}
   */
  getPrescriptionGroup: function()
  {
    return this.prescriptionGroup;
  }
});