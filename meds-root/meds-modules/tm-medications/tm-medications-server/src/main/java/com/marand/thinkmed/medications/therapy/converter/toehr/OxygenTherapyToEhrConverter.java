package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.FlowRateMode;
import com.marand.thinkmed.medications.api.internal.dto.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicalDevice;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.model.TimingDaily;
import com.marand.thinkmed.medications.ehr.model.TimingNonDaily;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class OxygenTherapyToEhrConverter extends TherapyToEhrConverter<OxygenTherapyDto>
{
  @Override
  public boolean isFor(final TherapyDto therapy)
  {
    return therapy instanceof OxygenTherapyDto;
  }

  @Override
  protected DvText extractMedicationItem(final OxygenTherapyDto therapy)
  {
    return getTherapyToEhrUtils().extractMedication(therapy.getMedication());
  }

  @Override
  protected Medication extractPreparationDetails(final OxygenTherapyDto therapy)
  {
    final Medication medication = new Medication();
    medication.setComponentName(extractMedicationItem(therapy));
    return medication;
  }

  @Override
  protected List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final OxygenTherapyDto therapy)
  {
    final TimingDaily timingDaily = getTherapyToEhrUtils().buildTimingDaily(
        therapy.getDosingFrequency(),
        null,
        null);

    final Dosage dosage = getTherapyToEhrUtils().buildDosage(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        therapy.getFlowRate(),
        therapy.getFlowRateUnit(),
        null,
        null,
        null,
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
  protected List<DvText> extractMoreAdditionalInstructions(final OxygenTherapyDto therapy)
  {
    final List<DvText> additionalInstructions = new ArrayList<>();
    if (therapy.getFlowRateMode() == FlowRateMode.HIGH_FLOW)
    {
      additionalInstructions.add(FlowRateMode.HIGH_FLOW.getAdditionalInstructionEnum().getDvCodedText());
    }
    if (therapy.isHumidification())
    {
      additionalInstructions.add(MedicationAdditionalInstructionEnum.HUMIDIFICATION.getDvCodedText());
    }
    return additionalInstructions;
  }

  @Override
  protected MedicalDevice extractAdministrationDevice(final OxygenTherapyDto therapy)
  {
    return getTherapyToEhrUtils().buildMedicalDeviceForOxygen(therapy.getStartingDevice());
  }

  @Override
  protected DvCodedText extractPrescriptionType(final OxygenTherapyDto therapy)
  {
    return MedicationOrderFormType.OXYGEN.getDvCodedText();
  }

  @Override
  protected AdditionalDetails extractAdditionalDetails(final OxygenTherapyDto therapy)
  {
    final AdditionalDetails additionalDetails = super.extractAdditionalDetails(therapy);
    if (therapy.getMinTargetSaturation() != null)
    {
      additionalDetails.setMinTargetSaturation(DataValueUtils.getPercentProportion(therapy.getMinTargetSaturation().floatValue() * 100));
    }
    if (therapy.getMaxTargetSaturation() != null)
    {
      additionalDetails.setMaxTargetSaturation(DataValueUtils.getPercentProportion(therapy.getMaxTargetSaturation().floatValue() * 100));
    }
    return additionalDetails;
  }
}
