Class.define('app.views.medications.mentalHealth.dto.MentalHealthMedication', 'tm.jquery.Object', {
  statics: {
    /**
     * @param {object} jsonObject
     * @return {app.views.medications.mentalHealth.dto.MentalHealthMedication|null}
     */
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;
      var config = jQuery.extend(true, {}, jsonObject);
      config.route = config.route ?
          new app.views.medications.common.dto.MedicationRoute(config.route) :
          null;
      return new app.views.medications.mentalHealth.dto.MentalHealthMedication(config);
    }
  },
  id: null,
  name: null,
  genericName: null,
  route: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {number|null}
   */
  getId: function()
  {
    return this.id;
  },

  /**
   * @return {string|null}
   */
  getName: function()
  {
    return this.name;
  },

  /**
   * @return {string|null}
   */
  getGenericName: function()
  {
    return this.genericName;
  },

  /**
   * @return {app.views.medications.common.dto.MedicationRoute|null}
   */
  getRoute: function()
  {
    return this.route;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationRoute|null} route
   */
  setRoute: function(route)
  {
    this.route = route;
  }
});
