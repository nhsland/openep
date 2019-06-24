Class.define('app.views.medications.grid.dto.TherapyFlow', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);
      if (tm.jquery.Utils.isArray(jsonObject.therapyRows))
      {
        config.therapyRows = jsonObject.therapyRows.map(app.views.medications.grid.dto.TherapyFlowRow.fromJson)
      }
      return new app.views.medications.grid.dto.TherapyFlow(config);
    }
  },
  therapyRows: null,
  referenceWeightsDayMap: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Array<app.views.medications.grid.dto.TherapyFlowRow>|null}
   */
  getTherapyRows: function()
  {
    return this.therapyRows;
  },

  /**
   * @param {Array<app.views.medications.grid.dto.TherapyFlowRow>|null} therapyRows
   */
  setTherapyRows: function(therapyRows)
  {
    this.therapyRows = therapyRows
  },

  /**
   * @returns {Object<number, number>|null}
   */
  getReferenceWeightsDayMap: function()
  {
    return this.referenceWeightsDayMap;
  },

  /**
   * @param  {Object<number, number>|null} referenceWeightsDayMap
   */
  setReferenceWeightsDayMap: function(referenceWeightsDayMap)
  {
    this.referenceWeightsDayMap = referenceWeightsDayMap;
  }
});