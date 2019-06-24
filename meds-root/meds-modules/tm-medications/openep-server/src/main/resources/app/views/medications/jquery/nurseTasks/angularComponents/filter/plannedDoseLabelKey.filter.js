/**
 * @return {function(string): string}
 * @constructor creates a new filter that can be used to determine the correct label associated with a task's plannedDose
 * property value, based on tasks's therapy. It can be either a dose or a rate.
 */
function PlannedDoseLabelKeyFilter()
{
  return function(doseType)
  {
    return ['RATE', 'RATE_QUANTITY', 'RATE_VOLUME_SUM'].contains(doseType) ? 'planned.rate' : 'dose.planned';
  };
}
PlannedDoseLabelKeyFilter.$inject = [];