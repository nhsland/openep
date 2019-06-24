package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.MedicationDeliveryMethodEnum;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
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
public class ConstantComplexTherapyToEhrConverter extends ComplexTherapyToEhrConverter<ConstantComplexTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof ConstantComplexTherapyDto;
  }

  @Override
  protected DvText extractAdministrationMethod(final ConstantComplexTherapyDto therapy)
  {
    if (therapy.isContinuousInfusion())
    {
      return MedicationDeliveryMethodEnum.CONTINUOUS_INFUSION.getDvCodedText();
    }
    if (MedicationDeliveryMethodEnum.BOLUS.name().equals(therapy.getRateString()))
    {
      return MedicationDeliveryMethodEnum.BOLUS.getDvCodedText();
    }
    return null;
  }

  @Override
  protected List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final ConstantComplexTherapyDto therapy)
  {
    final TimingDaily timingDaily = getTherapyToEhrUtils().buildTimingDaily(
        therapy.getDosingFrequency(),
        therapy.getDoseTimes(),
        therapy.getWhenNeeded());

    final Dosage dosage = getTherapyToEhrUtils().buildDosage(
        therapy,
        therapy.getDoseElement(),
        therapy.getRateString(),
        timingDaily,
        true);

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
  protected List<DvText> extractDosageJustification(final ConstantComplexTherapyDto therapy)
  {
    final List<DvText> dosageJustifications = super.extractDosageJustification(therapy);
    if (therapy.getTitration() != null)
    {
      dosageJustifications.add(therapy.getTitration().getDosageJustificationEnum().getDvCodedText());
    }
    return dosageJustifications;
  }
}
