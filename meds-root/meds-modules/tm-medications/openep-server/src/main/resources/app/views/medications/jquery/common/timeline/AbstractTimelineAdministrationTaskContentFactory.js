// noinspection JSUnusedLocalSymbols
Class.define('app.views.medications.common.timeline.AbstractTimelineAdministrationTaskContentFactory', 'tm.jquery.Object', {
  /**
   * An abstraction intended to be used as an interface.
   * {@see app.views.medications.common.timeline.TimelineContentBuilder}
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Should return the container representing an individual administration task.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Array<*>} administrations
   * @returns {tm.jquery.Container|tm.jquery.Component}
   */
  createContentContainer: function(therapy, administration, administrations)
  {
    throw new Error('not implemented');
  }
});
