Class.define('app.views.medications.common.overview.header.AbstractToolbarContainer', 'tm.jquery.Container', {
  /**
   * Should be implemented in the concrete class, if the toolbar supports attaching it's button events to the subview. The
   * actual implementation should call the appropriate {@link app.views.medications.common.overview.AbstractSubViewContainer}
   * visitor method, allowing for the sub view to hold the actual implementation of linking logic.
   * @abstract
   * @param {app.views.medications.common.overview.AbstractSubViewContainer} subView
   */
  attachEventsToSubView: function(subView)
  {

  }
});