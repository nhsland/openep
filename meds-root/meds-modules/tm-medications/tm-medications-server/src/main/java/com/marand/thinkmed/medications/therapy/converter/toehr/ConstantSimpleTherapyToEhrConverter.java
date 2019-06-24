package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
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
public class ConstantSimpleTherapyToEhrConverter extends SimpleTherapyToEhrConverter<ConstantSimpleTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof ConstantSimpleTherapyDto;
  }

  @Override
  protected List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final ConstantSimpleTherapyDto therapy)
  {
    final TimingDaily timingDaily = getTherapyToEhrUtils().buildTimingDaily(
        therapy.getDosingFrequency(),
        therapy.getDoseTimes(),
        therapy.getWhenNeeded());

    final SimpleDoseElementDto doseElement = therapy.getDoseElement();

    final Dosage dosage = getTherapyToEhrUtils().buildDosage(
        doseElement != null ? doseElement.getQuantity() : null,
        therapy.getQuantityUnit(),
         doseElement != null ? doseElement.getQuantityDenominator() : null,
        therapy.getQuantityDenominatorUnit(),
        null,
        null,
        doseElement != null ? doseElement.getDoseRange() : null,
        null,
        null,
        null,
        null,
        doseElement != null ? doseElement.getDoseDescription() : null,
        timingDaily);

    final TimingNonDaily timingNonDaily = getTherapyToEhrUtils().buildTimingNonDaily(
        therapy.getDosingDaysFrequency(),
        therapy.getDaysOfWeek(),
        null,
        null);

    final TherapeuticDirection therapeuticDirection = getTherapyToEhrUtils().buildTherapeuticDirection(
        Collections.singletonList(dosage),
        timingNonDaily,
        therapy.getDosingFrequency());

    return Collections.singletonList(therapeuticDirection);
  }

  @Override
  protected List<DvText> extractDosageJustification(final ConstantSimpleTherapyDto therapy)
  {
    final List<DvText> dosageJustifications = super.extractDosageJustification(therapy);
    if (therapy.getTitration() != null)
    {
      dosageJustifications.add(therapy.getTitration().getDosageJustificationEnum().getDvCodedText());
    }
    return dosageJustifications;
  }
}
