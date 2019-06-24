Class.define('app.views.medications.common.dto.OxygenStartingDevice', 'tm.jquery.Object', {
  route: null,
  routeType: null,
  /**
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {string|null}
   */
  getRoute: function()
  {
    return this.route;
  },
  /**
   * @returns {number|null}
   */
  getRouteType: function()
  {
    return this.routeType;
  },

  /**
   * @param {string} value of type app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute
   */
  setRoute: function(value)
  {
    this.route = value;
  },
  /**
   * @param {number|null} value
   */
  setRouteType: function(value)
  {
    this.routeType = value;
  },

  /**
   * @param {app.views.common.AppView} appView
   * @returns {String}
   */
  getDisplayText: function(appView)
  {
    if (appView && this.getRoute())
    {
      var routeText = appView.getDictionary('MedicalDeviceEnum.' + this.getRoute());
      if (this.getRouteType())
      {
        routeText += ' ';
        routeText += this.getRouteType();
      }
      return routeText;
    }
    return '';
  },

  /**
   * @returns {boolean}
   */
  isVenturiMask: function()
  {
    return this.getRoute() === app.views.medications.TherapyEnums.oxygenDeliveryClusterRoute.VENTURI_MASK
  }
});
