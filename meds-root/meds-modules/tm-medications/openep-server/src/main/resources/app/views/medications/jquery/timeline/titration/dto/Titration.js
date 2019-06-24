Class.define('app.views.medications.timeline.titration.dto.Titration', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      if (jsonObject.therapies)
      {
        config.therapies = jsonObject.therapies.map(function(therapyForTitration)
        {
          return app.views.medications.timeline.titration.dto.TherapyForTitration.fromJson(therapyForTitration);
        });
      }
      if (jsonObject.results)
      {
        config.results = jsonObject.results.map(function(resultsForTitration)
        {
          return new app.views.medications.timeline.titration.dto.QuantityWithTime(resultsForTitration);
        });
      }
      if (jsonObject.medicationData)
      {
        config.medicationData = app.views.medications.common.dto.MedicationData.fromJson(jsonObject.medicationData);
      }

      return new app.views.medications.timeline.titration.dto.Titration(config);
    }
  },

  name: null,
  range: null,
  results: null,
  therapies: null,
  titrationType: null,
  unit: null,
  medicationData: null,
  normalRangeMax: null,
  normalRangeMin: null,

  startInterval: null,
  endInterval: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.therapies = this.getConfigValue("therapies", []);
    this.results = this.getConfigValue("results", []);
  },

  /**
   * @returns {Array<app.views.medications.timeline.titration.dto.TherapyForTitration>}
   */
  getTherapies: function()
  {
    return this.therapies;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {Array<app.views.medications.timeline.titration.dto.QuantityWithTime>}
   */
  getResults: function()
  {
    return this.results;
  },

  /**
   * @returns {String}
   */
  getTitrationType: function()
  {
    return this.titrationType;
  },

  /**
   * @returns {String}
   */
  getUnit: function()
  {
    return this.unit;
  },

  /**
   * @param {Date} value
   */
  setStartInterval: function(value)
  {
    this.startInterval = value;
  },

  /**
   * @returns {Date}
   */
  getStartInterval: function()
  {
    return this.startInterval;
  },

  /**
   * @param {Date} value
   */
  setEndInterval: function(value)
  {
    this.endInterval = value;
  },

  /**
   * @returns {Date}
   */
  getEndInterval: function()
  {
    return this.endInterval;
  },

  /**
   * @returns {Number}
   */
  getNormalRangeMax: function()
  {
    return this.normalRangeMax;
  },

  /**
   * @returns {Number}
   */
  getNormalRangeMin: function()
  {
    return this.normalRangeMin;
  }
});