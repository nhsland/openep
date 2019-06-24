package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.model.TimingDaily;
import com.marand.thinkmed.medications.ehr.model.TimingNonDaily;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class VariableSimpleTherapyToEhrConverter extends SimpleTherapyToEhrConverter<VariableSimpleTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof VariableSimpleTherapyDto;
  }

  @Override
  protected List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final VariableSimpleTherapyDto therapy)
  {
    final boolean variableDaysTherapy = therapy.getTimedDoseElements().stream()
        .anyMatch(t -> t.getDate() != null);

    if (variableDaysTherapy)
    {
      return buildTherapeuticDirectionsForVariableDaysTherapy(therapy);
    }

    final boolean dischargeProtocol = therapy.getTimedDoseElements().stream()
        .anyMatch(t -> t.getTimingDescription() != null);

    if (dischargeProtocol)
    {
      return buildTherapeuticDirectionsForDischargeProtocol(therapy);
    }

    return Collections.singletonList(buildTherapeuticDirection(therapy, therapy.getTimedDoseElements(), null, null));
  }

  private List<TherapeuticDirection> buildTherapeuticDirectionsForVariableDaysTherapy(final VariableSimpleTherapyDto therapy)
  {
    //group elements with same date together, if not variable on multiple days, date is null
    final Map<DateTime, List<TimedSimpleDoseElementDto>> daysMap = new LinkedHashMap<>();
    therapy.getTimedDoseElements()
        .forEach(t -> daysMap.computeIfAbsent(t.getDate(), k -> new ArrayList<>()).add(t));

    return daysMap.entrySet().stream()
        .map(e -> buildTherapeuticDirection(therapy, e.getValue(), e.getKey(), null))
        .collect(Collectors.toList());
  }

  private List<TherapeuticDirection> buildTherapeuticDirectionsForDischargeProtocol(final VariableSimpleTherapyDto therapy)
  {
    return therapy.getTimedDoseElements().stream()
        .map(t -> buildTherapeuticDirection(therapy, Collections.singletonList(t), null, t.getTimingDescription()))
        .collect(Collectors.toList());
  }

  private TherapeuticDirection buildTherapeuticDirection(
      final VariableSimpleTherapyDto therapy,
      final List<TimedSimpleDoseElementDto> timedSimpleDoseElement,
      final DateTime specificDate,
      final String timingDescription)
  {
    final List<Dosage> doses = timedSimpleDoseElement.stream()
        .map(t -> buildDosage(t, therapy))
        .collect(Collectors.toList());

    final TimingNonDaily timingNonDaily = getTherapyToEhrUtils().buildTimingNonDaily(
        therapy.getDosingDaysFrequency(),
        therapy.getDaysOfWeek(),
        specificDate,
        timingDescription);

    return getTherapyToEhrUtils().buildTherapeuticDirection(
        doses,
        timingNonDaily,
        therapy.getDosingFrequency());
  }

  private Dosage buildDosage(final TimedSimpleDoseElementDto timedSimpleDoseElement, final VariableSimpleTherapyDto therapy)
  {
    final List<HourMinuteDto> administrationTimes =
        timedSimpleDoseElement.getDoseTime() != null
        ? Collections.singletonList(timedSimpleDoseElement.getDoseTime())
        : Collections.emptyList();
    final TimingDaily timingDaily = getTherapyToEhrUtils().buildTimingDaily(
        therapy.getDosingFrequency(),
        administrationTimes,
        therapy.getWhenNeeded());

    final SimpleDoseElementDto doseElement = timedSimpleDoseElement.getDoseElement();

    return getTherapyToEhrUtils().buildDosage(
        doseElement.getQuantity(),
        therapy.getQuantityUnit(),
        doseElement.getQuantityDenominator(),
        therapy.getQuantityDenominatorUnit(),
        null,
        null,
        doseElement.getDoseRange(),
        null,
        null,
        null,
        null,
        doseElement.getDoseDescription(),
        timingDaily);
  }
}
