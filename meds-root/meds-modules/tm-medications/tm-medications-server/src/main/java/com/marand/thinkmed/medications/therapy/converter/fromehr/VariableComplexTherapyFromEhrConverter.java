package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Component
public class VariableComplexTherapyFromEhrConverter extends ComplexTherapyFromEhrConverter<VariableComplexTherapyDto>
{
  @Override
  public boolean isFor(final MedicationOrder medicationOrder)
  {
    return !MedicationsEhrUtils.isSimpleTherapy(medicationOrder) && MedicationsEhrUtils.isVariableTherapy(medicationOrder);
  }

  @Override
  protected VariableComplexTherapyDto createTherapyDto(final MedicationOrder medicationOrder)
  {
    return new VariableComplexTherapyDto();
  }

  @Override
  public VariableComplexTherapyDto mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final VariableComplexTherapyDto therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setTimedDoseElements(extractDoseElements(medicationOrder));
    therapy.setRecurringContinuousInfusion(extractRecurringContinuousInfusion(medicationOrder));
    return therapy;
  }

  private List<TimedComplexDoseElementDto> extractDoseElements(final MedicationOrder medicationOrder)
  {
    final List<TimedComplexDoseElementDto> timedDoseElements = new ArrayList<>();
    for (final TherapeuticDirection therapeuticDirection : medicationOrder.getStructuredDoseAndTimingDirections())
    {
      for (final Dosage dosage : therapeuticDirection.getDosage())
      {
        final TimedComplexDoseElementDto timedDoseElement = new TimedComplexDoseElementDto();
        timedDoseElement.setDoseElement(getTherapyFromEhrUtils().extractComplexDoseElement(dosage));
        timedDoseElement.setDoseTime(extractDoseTime(dosage));
        timedDoseElements.add(timedDoseElement);
      }
    }

    return timedDoseElements;
  }

  private boolean extractRecurringContinuousInfusion(final MedicationOrder medicationOrder)
  {
    return MedicationDeliveryMethodEnum.RECURRING_CONTINUOUS_INFUSION.matches(medicationOrder.getAdministrationMethod());
  }

  private HourMinuteDto extractDoseTime(final Dosage dosage)
  {
    return Opt.resolve(() -> EhrValueUtils.getTime(dosage.getTiming().getSpecificTime().get(0))).orElse(null);
  }
}
