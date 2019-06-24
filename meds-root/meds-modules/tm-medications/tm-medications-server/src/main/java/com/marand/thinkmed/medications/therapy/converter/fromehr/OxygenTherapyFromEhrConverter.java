package com.marand.thinkmed.medications.therapy.converter.fromehr;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.api.internal.dto.FlowRateMode;
import com.marand.thinkmed.medications.api.internal.dto.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Component
public class OxygenTherapyFromEhrConverter extends TherapyFromEhrConverter<OxygenTherapyDto>
{
  @Override
  public boolean isFor(final MedicationOrder medicationOrder)
  {
    return MedicationsEhrUtils.isOxygen(medicationOrder);
  }

  @Override
  protected OxygenTherapyDto createTherapyDto(final MedicationOrder medicationOrder)
  {
    return new OxygenTherapyDto();
  }

  @Override
  public OxygenTherapyDto mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final OxygenTherapyDto therapy = super.mapTherapyFromEhr(medicationOrder, compositionUid, createdTimestamp);
    therapy.setMedication(getTherapyFromEhrUtils().buildMedication(medicationOrder.getMedicationItem()));
    therapy.setFlowRate(extractFlowRate(medicationOrder));
    therapy.setFlowRateUnit(extractFlowRateUnit(medicationOrder));
    therapy.setFlowRateMode(extractFlowRateMode(medicationOrder));
    therapy.setHumidification(extractHumidification(medicationOrder));
    therapy.setStartingDevice(extractStartingDevice(medicationOrder));
    therapy.setMinTargetSaturation(extractMinTargetSaturation(medicationOrder));
    therapy.setMaxTargetSaturation(extractMaxTargetSaturation(medicationOrder));
    return therapy;
  }

  @Override
  protected TherapyDoseTypeEnum extractDoseType(final MedicationOrder medicationOrder)
  {
    return TherapyDoseTypeEnum.RATE;
  }

  private Double extractFlowRate(final MedicationOrder medicationOrder)
  {
    final Opt<Dosage> dosage = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0));
    if (dosage.isPresent())
    {
      return getTherapyFromEhrUtils().extractRate(dosage.get());
    }
    return null;
  }

  private String extractFlowRateUnit(final MedicationOrder medicationOrder)
  {
    final Opt<Dosage> dosage = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0));
    if (dosage.isPresent())
    {
      return getTherapyFromEhrUtils().extractRateUnit(dosage.get());
    }
    return null;
  }

  private FlowRateMode extractFlowRateMode(final MedicationOrder medicationOrder)
  {
    final boolean highFlowRate = medicationOrder.getAdditionalInstruction().stream()
        .anyMatch(MedicationAdditionalInstructionEnum.HIGH_FLOW::matches);
    return highFlowRate ? FlowRateMode.HIGH_FLOW : FlowRateMode.LOW_FLOW;
  }

  private boolean extractHumidification(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getAdditionalInstruction().stream()
        .anyMatch(MedicationAdditionalInstructionEnum.HUMIDIFICATION::matches);
  }

  private OxygenStartingDevice extractStartingDevice(final MedicationOrder medicationOrder)
  {
    return getTherapyFromEhrUtils().buildOxygenDevice(medicationOrder.getAdministrationDevice());
  }

  private Double extractMinTargetSaturation(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(
        () -> (double)medicationOrder.getAdditionalDetails().getMinTargetSaturation().getNumerator() / 100).orElse(null);
  }

  private Double extractMaxTargetSaturation(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(
        () -> (double)medicationOrder.getAdditionalDetails().getMaxTargetSaturation().getNumerator() / 100).orElse(null);
  }
}
