Class.define('app.views.medications.common.overview.AbstractSubViewContainer', 'tm.jquery.Container', {
  /**
   * Implement in the concrete class if you want to link the west toolbar's event to the given sub view.
   * @abstract
   * @param {app.views.medications.common.overview.header.WestToolbarContainer} toolbar
   */
  attachOverviewHeaderWestToolbarEvents: function(toolbar)
  {

  },

  /**
   * Implement in the concrete class if you want to link the east toolbar's event to the given sub view.
   * @abstract
   * @param toolbar
   */
  attachOverviewHeaderEastToolbarEvents: function(toolbar)
  {

  },

  /**
   * Implement in the concrete class if you want to link the central toolbar's event to the given sub view.
   * @abstract
   * @param toolbar
   */
  attachOverviewHeaderCenterToolbarEvents: function(toolbar)
  {

  }
});