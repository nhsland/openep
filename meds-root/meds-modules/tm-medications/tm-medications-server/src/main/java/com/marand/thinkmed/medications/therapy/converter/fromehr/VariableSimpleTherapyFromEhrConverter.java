package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
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

@Component
public class VariableSimpleTherapyFromEhrConverter extends SimpleTherapyFromEhrConverter<VariableSimpleTherapyDto>
{
  @Override
  public boolean isFor(final MedicationOrder medicationOrder)
  {
    return MedicationsEhrUtils.isSimpleTherapy(medicationOrder) && MedicationsEhrUtils.isVariableTherapy(medicationOrder);
  }

  @Override
  protected VariableSimpleTherapyDto createTherapyDto(final MedicationOrder medicationOrder)
  {
    return new VariableSimpleTherapyDto();
  }

  @Override
  public VariableSimpleTherapyDto mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final VariableSimpleTherapyDto therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setTimedDoseElements(extractTimedSimpleDoseElements(medicationOrder));
    return therapy;
  }

  private List<TimedSimpleDoseElementDto> extractTimedSimpleDoseElements(final MedicationOrder medicationOrder)
  {
    final List<TimedSimpleDoseElementDto> timedDoseElement = new ArrayList<>();
    for (final TherapeuticDirection therapeuticDirection : medicationOrder.getStructuredDoseAndTimingDirections())
    {
      for (final Dosage dosage : therapeuticDirection.getDosage())
      {
        timedDoseElement.add(extractTimedSimpleDoseElement(therapeuticDirection, dosage));
      }
    }
    return timedDoseElement;
  }

  private TimedSimpleDoseElementDto extractTimedSimpleDoseElement(
      final TherapeuticDirection therapeuticDirection,
      final Dosage dosage)
  {
    final TimedSimpleDoseElementDto timedDoseElement = new TimedSimpleDoseElementDto();
    timedDoseElement.setDoseElement(getTherapyFromEhrUtils().extractSimpleDoseElement(dosage));
    timedDoseElement.setDoseTime(extractDoseTime(dosage));
    timedDoseElement.setDate(extractDate(therapeuticDirection));
    timedDoseElement.setTimingDescription(extractTimingDescription(therapeuticDirection));
    return timedDoseElement;
  }

  private DateTime extractDate(final TherapeuticDirection therapeuticDirection)
  {
    return Opt.resolve(() -> EhrValueUtils.getDate(therapeuticDirection.getDirectionRepetition().getSpecificDate().get(0)))
        .orElse(null);
  }

  private HourMinuteDto extractDoseTime(final Dosage dosage)
  {
    return Opt.resolve(() -> EhrValueUtils.getTime(dosage.getTiming().getSpecificTime().get(0))).orElse(null);
  }

  private String extractTimingDescription(final TherapeuticDirection therapeuticDirection)
  {
    return Opt.resolve(() -> therapeuticDirection.getDirectionRepetition().getTimingDescription().getValue()).orElse(null);
  }
}
