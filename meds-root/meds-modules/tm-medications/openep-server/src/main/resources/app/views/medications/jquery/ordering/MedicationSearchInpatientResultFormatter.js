/**
 * Adds inpatient specific additional information to the medication title when displaying medication search results.
 * @see app.views.medications.common.MedicationSearchResultFormatter
 * @see app.views.medications.common.MedicationSearchField
 */
Class.define('app.views.medications.ordering.MedicationSearchInpatientResultFormatter', 'app.views.medications.common.MedicationSearchResultFormatter', {
  /**
   * @override
   * @param {{key: string, title: string, data: object}} node
   * @return {string} HTML for the single node title
   */
  createTitle: function(node)
  {
    var title = node.title;

    if (node.data && !tm.jquery.Utils.isEmpty(node.data.inpatientAdditionalInfo))
    {
      title += '<span class="additional-information">' + '&nbsp-&nbsp;' + node.data.inpatientAdditionalInfo + '</span>';
    }

    return title;
  }
});
