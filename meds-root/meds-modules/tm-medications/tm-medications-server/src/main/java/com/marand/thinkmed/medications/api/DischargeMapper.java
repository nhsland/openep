package com.marand.thinkmed.medications.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;

import com.google.common.collect.Lists;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.medications.api.external.dto.AdditionalInstructionsDto;
import com.marand.thinkmed.medications.api.external.dto.AdditionalInstructionsItemDto;
import com.marand.thinkmed.medications.api.external.dto.CommentDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeDetailsDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeDurationDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeListDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeListItemDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeMedicationDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeQuantitiesDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeQuantityDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeQuantityItemDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryItemDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryStatusEnum;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryStatusReasonDto;
import com.marand.thinkmed.medications.api.external.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.api.external.dto.DoseDto;
import com.marand.thinkmed.medications.api.external.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.external.dto.DoseItemDto;
import com.marand.thinkmed.medications.api.external.dto.DoseItemQuantityDto;
import com.marand.thinkmed.medications.api.external.dto.DoseItemTimingDto;
import com.marand.thinkmed.medications.api.external.dto.IndicationDto;
import com.marand.thinkmed.medications.api.external.dto.KeyValueDto;
import com.marand.thinkmed.medications.api.external.dto.MedicationDto;
import com.marand.thinkmed.medications.api.external.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.external.dto.ReleaseCharacteristicsDto;
import com.marand.thinkmed.medications.api.external.dto.RouteDto;
import com.marand.thinkmed.medications.api.external.dto.RoutesDto;
import com.marand.thinkmed.medications.api.external.dto.TimingDirectionsDto;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class DischargeMapper
{
  private MedicationsValueHolder medicationsValueHolder;
  private final NumberFormat numberToWordsFormatter;

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  public DischargeMapper()
  {
    numberToWordsFormatter = new RuleBasedNumberFormat(
        DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale(),
        RuleBasedNumberFormat.SPELLOUT);
  }

  public DischargeListDto mapDischargeList(final @NonNull List<MedicationOnDischargeDto> medicationsOnDischarge)
  {
    final DischargeListDto dischargeList = new DischargeListDto();

    dischargeList.setItems(
        medicationsOnDischarge.stream()
            .map(this::mapDischargeListItem)
            .collect(Collectors.toList()));

    return dischargeList;
  }

  public DischargeSummaryDto mapDischargeSummary(final @NonNull ReconciliationSummaryDto reconciliationSummaryDto)
  {
    final DischargeSummaryDto summary = new DischargeSummaryDto();

    summary.setAdmissionLastUpdateTime(reconciliationSummaryDto.getAdmissionLastUpdateTime());
    summary.setDischargeLastUpdateTime(reconciliationSummaryDto.getDischargeLastUpdateTime());
    summary.setItems(
        reconciliationSummaryDto.getRows().stream()
            .map(this::mapDischargeSummaryItem)
            .collect(Collectors.toList()));

    return summary;
  }

  private DischargeListItemDto mapDischargeListItem(final MedicationOnDischargeDto medicationOnDischarge)
  {
    final DischargeListItemDto item = new DischargeListItemDto();
    final TherapyDto therapy = medicationOnDischarge.getTherapy();

    item.setId(therapy.getTherapyId());
    item.setPrescription(mapPrescription(therapy));
    item.setDischarge(mapDischarge(medicationOnDischarge.getTherapy()));

    return item;
  }

  private DischargeSummaryItemDto mapDischargeSummaryItem(final ReconciliationRowDto row)
  {
    final DischargeSummaryItemDto item = new DischargeSummaryItemDto();
    final TherapyDto therapy =
        row.getTherapyOnDischarge() != null ? row.getTherapyOnDischarge() : row.getTherapyOnAdmission();

    item.setId(therapy.getTherapyId());
    item.setPrescription(mapPrescription(therapy));
    item.setDischarge(mapDischarge(therapy));
    item.setStatus(mapSummaryStatus(row.getGroupEnum()));
    item.setStatusReason(mapStatusReason(row));
    return item;
  }

  PrescriptionDto mapPrescription(final TherapyDto therapy)
  {
    final PrescriptionDto prescription = new PrescriptionDto();
    prescription.setDisplay(therapy.getTherapyDescription());
    prescription.setMedication(mapMedication(therapy));
    prescription.setDose(mapDose(therapy));
    prescription.setDoseForm(mapDoseForm(therapy));
    prescription.setRoutes(mapRoutes(therapy));
    prescription.setReleaseCharacteristics(mapReleaseCharacteristics(therapy));
    prescription.setTimingDirections(mapTimingDirections(therapy));
    prescription.setComment(mapComment(therapy));
    prescription.setIndication(mapIndication(therapy));
    prescription.setAdditionalInstructions(mapAdditionalInstructions(therapy));
    return prescription;
  }

  private MedicationDto mapMedication(final TherapyDto therapy)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      return mapMedication(((SimpleTherapyDto)therapy).getMedication());
    }
    if (therapy instanceof ComplexTherapyDto)
    {
      //noinspection IfMayBeConditional
      if (therapy.getMedications().size() == 1)
      {
        return mapMedication(therapy.getMedications().get(0));
      }
      else
      {
        return mapMedication(therapy.getMedications());
      }
    }
    return null;
  }

  private MedicationDto mapMedication(final com.marand.thinkmed.medications.api.internal.dto.MedicationDto medication)
  {
    return new MedicationDto(String.valueOf(medication.getId()), medication.getDisplayName(), isControlledDrug(medication));
  }

  private boolean isControlledDrug(final com.marand.thinkmed.medications.api.internal.dto.MedicationDto medication)
  {
    if (medication.getId() != null)
    {
      final MedicationDataDto medicationDataDto = medicationsValueHolder.getMedications().get(medication.getId());
      if (medicationDataDto != null)
      {
        return medicationDataDto.isControlledDrug();
      }
    }
    return false;
  }

  @SuppressWarnings("Convert2MethodRef")
  private MedicationDto mapMedication(final List<com.marand.thinkmed.medications.api.internal.dto.MedicationDto> medications)
  {
    final String medicationName = medications.stream()
        .map(m -> m.getDisplayName())
        .collect(Collectors.joining(" - "));

    final boolean controlledDrug = medications.stream()
        .anyMatch(m -> medicationsValueHolder.getMedications().get(m.getId()).isControlledDrug());

    return new MedicationDto(null, medicationName, controlledDrug);
  }

  private DoseDto mapDose(final TherapyDto therapy)
  {
    final DoseDto dose = new DoseDto();

    if (therapy instanceof ConstantSimpleTherapyDto)
    {
      dose.getItems().add(mapConstantSimpleTherapyDose((ConstantSimpleTherapyDto)therapy));
    }
    else if (therapy instanceof VariableSimpleTherapyDto)
    {
      dose.setItems(mapVariableSimpleTherapyDose((VariableSimpleTherapyDto)therapy));
    }
    else if (therapy instanceof ConstantComplexTherapyDto)
    {
      dose.getItems().add(mapConstantComplexTherapyDose((ConstantComplexTherapyDto)therapy));
    }
    else if (therapy instanceof VariableComplexTherapyDto)
    {
      dose.setItems(getVariableComplexTherapyDose((VariableComplexTherapyDto)therapy));
    }
    else if (therapy instanceof OxygenTherapyDto)
    {
      dose.getItems().add(mapOxygenTherapyDose((OxygenTherapyDto)therapy));
    }
    return dose;
  }

  private DoseItemDto mapConstantSimpleTherapyDose(final ConstantSimpleTherapyDto therapy)
  {
    return new DoseItemDto(therapy.getQuantityDisplay());
  }

  private List<DoseItemDto> mapVariableSimpleTherapyDose(final VariableSimpleTherapyDto variableSimpleTherapy)
  {
    return variableSimpleTherapy.getTimedDoseElements().stream()
        .map(this::mapVariableSimpleTherapyDoseItem)
        .collect(Collectors.toList());
  }

  private DoseItemDto mapVariableSimpleTherapyDoseItem(final TimedSimpleDoseElementDto doseElement)
  {
    return new DoseItemDto(
        doseElement.getQuantityDisplay() + " - " + doseElement.getTimeDisplay(),
        new DoseItemQuantityDto(doseElement.getQuantityDisplay()),
        new DoseItemTimingDto(
            doseElement.getTimeDisplay(),
            doseElement.getDoseTime() != null ? doseElement.getDoseTime().toLocalTime() : null,
            doseElement.getTimingDescription()));
  }

  private DoseItemDto mapConstantComplexTherapyDose(final ConstantComplexTherapyDto constantComplexTherapy)
  {
    if (constantComplexTherapy.isWithRate())
    {
      return new DoseItemDto(constantComplexTherapy.getSpeedDisplay());
    }
    else if (constantComplexTherapy.getVolumeSumDisplay() != null)
    {
      return new DoseItemDto(constantComplexTherapy.getVolumeSumDisplay());
    }
    else
    {
      return new DoseItemDto(constantComplexTherapy.getIngredientsList().get(0).getQuantityDisplay());
    }
  }

  private List<DoseItemDto> getVariableComplexTherapyDose(final VariableComplexTherapyDto variableComplexTherapyDto)
  {
    return variableComplexTherapyDto.getTimedDoseElements().stream()
        .map(e -> new DoseItemDto(e.getIntervalDisplay() + " - " + e.getSpeedDisplay()))
        .collect(Collectors.toList());
  }

  private DoseItemDto mapOxygenTherapyDose(final OxygenTherapyDto oxygenTherapyDto)
  {
    return new DoseItemDto(oxygenTherapyDto.getSpeedDisplay());
  }

  private DoseFormDto mapDoseForm(final TherapyDto therapy)
  {
    if (therapy instanceof SimpleTherapyDto && ((SimpleTherapyDto)therapy).getDoseForm() != null)
    {
      return new DoseFormDto(((SimpleTherapyDto)therapy).getDoseForm().getName());
    }
    return null;
  }

  private RoutesDto mapRoutes(final TherapyDto therapy)
  {
    final RoutesDto routes = new RoutesDto();

    routes.setItems(
        therapy.getRoutes().stream()
            .map(r -> new RouteDto(r.getName()))
            .collect(Collectors.toList()));

    routes.setDisplay(
        therapy.getRoutes().stream()
            .map(NamedIdentityDto::getName)
            .collect(Collectors.joining(", ")));

    return routes;
  }

  private ReleaseCharacteristicsDto mapReleaseCharacteristics(final TherapyDto therapy)
  {
    if (therapy.getReleaseDetails() != null)
    {
      return new ReleaseCharacteristicsDto(therapy.getReleaseDetails().getDisplay());
    }
    return null;
  }

  private TimingDirectionsDto mapTimingDirections(final TherapyDto therapy)
  {
    final String timingDirectionsDisplay = Lists.newArrayList(
        therapy.getFrequencyDisplay(),
        therapy.getDaysFrequencyDisplay(),
        therapy.getDaysOfWeekDisplay(),
        therapy.getWhenNeededDisplay(),
        therapy.getStartCriterionDisplay())
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" - "));

    return new TimingDirectionsDto(timingDirectionsDisplay);
  }

  private CommentDto mapComment(final TherapyDto therapy)
  {
    if (therapy.getComment() != null)
    {
      return new CommentDto(therapy.getComment());
    }
    return null;
  }

  private IndicationDto mapIndication(final TherapyDto therapy)
  {
    if (therapy.getClinicalIndication() != null)
    {
      return new IndicationDto(therapy.getClinicalIndication().getName());
    }
    return null;
  }

  private AdditionalInstructionsDto mapAdditionalInstructions(final TherapyDto therapy)
  {
    final String applicationPreconditionDisplay = therapy.getApplicationPreconditionDisplay();
    if (applicationPreconditionDisplay != null)
    {
      final AdditionalInstructionsDto additionalInstructions = new AdditionalInstructionsDto();
      additionalInstructions.setDisplay(applicationPreconditionDisplay);
      additionalInstructions.getItems().add(new AdditionalInstructionsItemDto(applicationPreconditionDisplay));
      return additionalInstructions;
    }
    return null;
  }

  DischargeDetailsDto mapDischarge(final TherapyDto therapy)
  {
    final DischargeDetailsDto dischargeDetails = new DischargeDetailsDto();

    final DispenseDetailsDto dispenseDetails = therapy.getDispenseDetails();
    if (dispenseDetails != null)
    {
      if (dispenseDetails.getDaysDuration() != null)
      {
        dischargeDetails.setDuration(new DischargeDurationDto(
            dispenseDetails.getDaysDuration(),
            Dictionary.getEntry("days")));
      }

      if (dispenseDetails.getQuantity() != null && dispenseDetails.getUnit() != null)
      {
        dischargeDetails.setQuantities(mapDischargeQuantities(dispenseDetails));
      }
      else if (!dispenseDetails.getControlledDrugSupply().isEmpty())
      {
        dischargeDetails.setQuantities(mapControlledDrugDischargeQuantities(dispenseDetails));
      }

      if (dispenseDetails.getDispenseSource() != null)
      {
        dischargeDetails.setDispenseSource(new DispenseSourceDto(dispenseDetails.getDispenseSource().getName()));
      }
    }

    final String targetInr = mapTargetInr(therapy);
    if (targetInr != null)
    {
      dischargeDetails.getAdditionalData().add(new KeyValueDto("targetInr", targetInr));
    }

    return dischargeDetails;
  }

  private DischargeQuantitiesDto mapDischargeQuantities(final DispenseDetailsDto dispenseDetails)
  {
    final DischargeQuantitiesDto quantities = new DischargeQuantitiesDto();
    final DischargeQuantityItemDto item = new DischargeQuantityItemDto();
    final Integer quantity = dispenseDetails.getQuantity();
    final String display = String.join(" ", String.valueOf(quantity), dispenseDetails.getUnit());
    item.setQuantity(new DischargeQuantityDto(quantity, dispenseDetails.getUnit(), display, null));
    item.setDisplay(display);
    quantities.getItems().add(item);
    return quantities;
  }

  private DischargeQuantitiesDto mapControlledDrugDischargeQuantities(final DispenseDetailsDto dispenseDetails)
  {
    final DischargeQuantitiesDto quantities = new DischargeQuantitiesDto();
    quantities.getItems().addAll(
        dispenseDetails.getControlledDrugSupply().stream()
            .map(this::mapControlledDrugSupply)
            .collect(Collectors.toList()));
    return quantities;
  }

  private DischargeQuantityItemDto mapControlledDrugSupply(final ControlledDrugSupplyDto dispenseQuantity)
  {
    final DischargeQuantityItemDto item = new DischargeQuantityItemDto();
    item.setMedication(new DischargeMedicationDto(dispenseQuantity.getMedication().getName()));
    item.setQuantity(mapDischargeQuantity(dispenseQuantity));
    item.setDisplay(item.getMedication().getDisplay() + " - " + item.getQuantity().getDisplay());
    return item;
  }

  private DischargeQuantityDto mapDischargeQuantity(final ControlledDrugSupplyDto dispenseQuantity)
  {
    final Integer value = dispenseQuantity.getQuantity();
    final String unit = dispenseQuantity.getUnit();
    final String display = value + " " + unit;
    final String textValue = numberToWordsFormatter.format(value);
    return new DischargeQuantityDto(value, unit, display, textValue);
  }

  private String mapTargetInr(final TherapyDto therapy)
  {
    if (therapy instanceof SimpleTherapyDto)
    {
      final SimpleTherapyDto simpleTherapyDto = (SimpleTherapyDto)therapy;
      if (simpleTherapyDto.getTargetInr() != null)
      {
        return String.valueOf(simpleTherapyDto.getTargetInr());
      }
    }
    return null;
  }

  private DischargeSummaryStatusEnum mapSummaryStatus(final ReconciliationRowGroupEnum reconciliationRowGroupEnum)
  {
    if (reconciliationRowGroupEnum == ReconciliationRowGroupEnum.ONLY_ON_ADMISSION)
    {
      return DischargeSummaryStatusEnum.STOPPED;
    }
    if (reconciliationRowGroupEnum == ReconciliationRowGroupEnum.ONLY_ON_DISCHARGE)
    {
      return DischargeSummaryStatusEnum.NEW;
    }
    if (reconciliationRowGroupEnum == ReconciliationRowGroupEnum.CHANGED)
    {
      return DischargeSummaryStatusEnum.CHANGED;
    }
    if (reconciliationRowGroupEnum == ReconciliationRowGroupEnum.NOT_CHANGED)
    {
      return DischargeSummaryStatusEnum.UNCHANGED;
    }
    return null;
  }

  private DischargeSummaryStatusReasonDto mapStatusReason(final ReconciliationRowDto row)
  {
    final String changeReason = Opt.resolve(() -> row.getChangeReasonDto().getChangeReason().getName()).orElse(null);
    final String changeReasonComment = Opt.resolve(() -> row.getChangeReasonDto().getComment()).orElse(null);
    if (changeReason != null)
    {
      final String display = changeReasonComment != null
                             ? String.join(" - ", changeReason, changeReasonComment)
                             : changeReason;
      return new DischargeSummaryStatusReasonDto(display);
    }
    return null;
  }
}