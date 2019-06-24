Class.define('app.views.medications.timeline.titration.dto.TherapyForTitration', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);

      if (jsonObject.therapy)
      {
        config.therapy = app.views.medications.common.TherapyJsonConverter.convert(jsonObject.therapy);
      }

      if (jsonObject.administrations)
      {
        config.administrations = jsonObject.administrations.map(function(item)
        {
          return new app.views.medications.timeline.titration.dto.QuantityWithTime(item);
        });
      }

      return new app.views.medications.timeline.titration.dto.TherapyForTitration(config);
    }
  },

  therapy: null,
  administrations: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.administrations = this.getConfigValue("administrations", []);
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Array<app.views.medications.timeline.titration.dto.QuantityWithTime>}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },
  /**
   * @returns {String}
   */
  getUnit: function()
  {
    return this.doseUnit;
  },

  /**
   * @returns {Number}
   */
  getInfusionFormulaAtIntervalStart: function()
  {
    return this.infusionFormulaAtIntervalStart;
  }
});