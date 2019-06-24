/**
 * Common base for grid view therapy container toolbars. Registers and fires all events, required by actions on the
 * toolbars. It should also have all public methods, or at least serve as an interface.
 */
Class.define('app.views.medications.grid.toolbar.BaseGridTherapyContainerToolbar', 'app.views.medications.common.therapy.TherapyContainerToolbar', {
  Constructor: function()
  {
    this.callSuper();
    this.registerEventTypes('app.views.medications.grid.toolbar.BaseGridTherapyContainerToolbar', [
      {eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_EDIT_THERAPY},
      {eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_STOP_THERAPY},
      {eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_CONFIRM_THERAPY},
      {eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_SHOW_RELATED_PHARMACIST_REVIEW},
      {eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_TASKS_CHANGED}
    ]);
  },

  /**
   * Override if functionality is needed on subclass.
   * @protected
   */
  disableActionButtons: function()
  {

  },

  /**
   * @protected
   */
  fireEditTherapyEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_EDIT_THERAPY,
      eventData: {
        therapyContainer: this.getTherapyContainer()
      }
    }), null);
  },

  /**
   * @protected
   */
  fireConfirmTherapyEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_CONFIRM_THERAPY,
      eventData: {
        therapyContainer: this.getTherapyContainer()
      }
    }), null);
  },

  /**
   * @protected
   */
  fireShowRelatedPharmacistReviewEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_SHOW_RELATED_PHARMACIST_REVIEW,
      eventData: {
        therapyContainer: this.getTherapyContainer()
      }
    }), null);
  },

  /**
   * @protected
   */
  fireStopTherapyEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_STOP_THERAPY,
      eventData: {
        therapyContainer: this.getTherapyContainer()
      }
    }), null);
  },
  /**
   * @protected
   */
  fireTasksChangedEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_TASKS_CHANGED
    }), null);
  }
});