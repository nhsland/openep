Class.define('app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents', 'tm.jquery.Object', {
  /**
   * All the events, fired by grid view therapy container toolbars.
   */
  statics: {
    EVENT_TYPE_CONFIRM_THERAPY: new tm.jquery.event.EventType({
      name: 'confirmTherapy', delegateName: null
    }),
    EVENT_TYPE_EDIT_THERAPY: new tm.jquery.event.EventType({
      name: 'editTherapy', delegateName: null
    }),
    EVENT_TYPE_STOP_THERAPY: new tm.jquery.event.EventType({
      name: 'stopTherapy', delegateName: null
    }),
    EVENT_TYPE_SHOW_RELATED_PHARMACIST_REVIEW: new tm.jquery.event.EventType({
      name: 'showRelatedPharmacistReview', delegateName: null
    }),
    EVENT_TYPE_TASKS_CHANGED: new tm.jquery.event.EventType({
      name: 'tasksChanged', delegateName: null
    })
  }
});