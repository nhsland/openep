package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.model.TimingDaily;
import com.marand.thinkmed.medications.ehr.model.TimingNonDaily;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class VariableComplexTherapyToEhrConverter extends ComplexTherapyToEhrConverter<VariableComplexTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof VariableComplexTherapyDto;
  }

  @Override
  protected DvText extractAdministrationMethod(final VariableComplexTherapyDto therapy)
  {
    if (therapy.isContinuousInfusion())
    {
      if (therapy.isRecurringContinuousInfusion())
      {
        return MedicationDeliveryMethodEnum.RECURRING_CONTINUOUS_INFUSION.getDvCodedText();
      }
      return MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.getDvCodedText();
    }
    return null;
  }

  @Override
  protected List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final VariableComplexTherapyDto therapy)
  {
    final List<Dosage> dosages = buildDosages(therapy);

    final TimingNonDaily timingNonDaily = getTherapyToEhrUtils().buildTimingNonDaily(
        therapy.getDosingDaysFrequency(),
        therapy.getDaysOfWeek(),
        null,
        null);

    final TherapeuticDirection therapeuticDirection = getTherapyToEhrUtils().buildTherapeuticDirection(
        dosages,
        timingNonDaily,
        therapy.getDosingFrequency());

    return Collections.singletonList(therapeuticDirection);
  }

  private List<Dosage> buildDosages(final VariableComplexTherapyDto therapy)
  {
    return therapy.getTimedDoseElements().stream()
        .map(t -> buildDosage(therapy, t, therapy.getTimedDoseElements().indexOf(t) == 0))
        .collect(Collectors.toList());
  }

  private Dosage buildDosage(
      final VariableComplexTherapyDto therapy,
      final TimedComplexDoseElementDto timedComplexDoseElement,
      final boolean isFirstDoseElement)
  {
    final List<HourMinuteDto> administrationTimes =
        timedComplexDoseElement.getDoseTime() != null ?
        Collections.singletonList(timedComplexDoseElement.getDoseTime()) :
        Collections.emptyList();
    final TimingDaily timingDaily = getTherapyToEhrUtils().buildTimingDaily(
        therapy.getDosingFrequency(),
        administrationTimes,
        therapy.getWhenNeeded());

    final ComplexDoseElementDto doseElement = timedComplexDoseElement.getDoseElement();

    final boolean setDoseAmount = isFirstDoseElement && !therapy.isContinuousInfusion();
    return getTherapyToEhrUtils().buildDosage(therapy, doseElement, null, timingDaily, setDoseAmount);
  }
}
