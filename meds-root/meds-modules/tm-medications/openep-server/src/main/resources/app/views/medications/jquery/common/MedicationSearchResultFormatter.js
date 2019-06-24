/**
 * The default implementation of a GOF builder in charge of creating search result presentation of matched medication, shown
 * by {@link app.views.medications.common.MedicationSearchField}. Created with the purpose to support displaying additional
 * information based on the search context (outpatient ordering, inpatient ordering, etc.). Acts as both the interface to
 * implemented in case of new variation and as the default logic, since it's not optional for the search field component.
 */
Class.define('app.views.medications.common.MedicationSearchResultFormatter', 'tm.jquery.Object', {
  /**
   * @param {{key: string, title: string, data: object}} node
   * @return {string} HTML for the single node title
   */
  createTitle: function(node)
  {
    return node.title;
  }
});
