package com.marand.thinkmed.medications.therapy.converter.toehr;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.OutpatientPrescriptionType;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.DispenseDirections;
import com.marand.thinkmed.medications.ehr.model.MaximumDose;
import com.marand.thinkmed.medications.ehr.model.MedicalDevice;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationAuthorisationSlovenia;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.MedicationSafety;
import com.marand.thinkmed.medications.ehr.model.MedicationSupplyAmount;
import com.marand.thinkmed.medications.ehr.model.OrderDetails;
import com.marand.thinkmed.medications.ehr.model.SafetyOverride;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public abstract class TherapyToEhrConverter<T extends TherapyDto>
{
  private TherapyToEhrUtils therapyToEhrUtils;

  @Autowired
  public void setTherapyToEhrUtils(final TherapyToEhrUtils therapyToEhrUtils)
  {
    this.therapyToEhrUtils = therapyToEhrUtils;
  }

  protected TherapyToEhrUtils getTherapyToEhrUtils()
  {
    return therapyToEhrUtils;
  }

  @SuppressWarnings("unchecked")
  public MedicationOrder mapTherapyToEhr(final TherapyDto therapy)
  {
    final MedicationOrder medicationOrder = new MedicationOrder();
    medicationOrder.setRoute(extractRoutes(therapy));
    medicationOrder.setOrderDetails(extractOrderDetails(therapy));
    medicationOrder.setComment(EhrValueUtils.getText(therapy.getComment()));
    medicationOrder.setClinicalIndication(extractClinicalIndication(therapy));
    medicationOrder.setAdditionalInstruction(extractAdditionalInstructions((T)therapy));
    medicationOrder.setMedicationItem(extractMedicationItem((T)therapy));
    medicationOrder.setStructuredDoseAndTimingDirections(extractStructuredDoseAndTimingDirections((T)therapy));
    medicationOrder.setAdministrationMethod(extractAdministrationMethod((T)therapy));
    medicationOrder.setAdministrationDevice(extractAdministrationDevice((T)therapy));
    medicationOrder.setOverallDirectionsDescription(EhrValueUtils.getText(therapy.getTherapyDescription()));
    medicationOrder.setParsableDirections(EhrValueUtils.getParsableHtml(therapy.getFormattedTherapyDisplay()));
    medicationOrder.setAdditionalDetails(extractAdditionalDetails((T)therapy));
    medicationOrder.setMedicationSafety(extractMedicationSafety((T)therapy));
    medicationOrder.setDosageJustification(extractDosageJustification((T)therapy));
    medicationOrder.setDispenseDirections(extractDispenseDirections((T)therapy));
    medicationOrder.setAuthorisationDirection(extractAuthorisationDirection((T)therapy));
    medicationOrder.setPreparationDetails(extractPreparationDetails((T)therapy));

    return medicationOrder;
  }

  public abstract boolean isFor(final TherapyDto therapy);

  protected abstract DvText extractMedicationItem(final T therapy);

  protected abstract Medication extractPreparationDetails(final T therapy);

  protected abstract List<TherapeuticDirection> extractStructuredDoseAndTimingDirections(final T therapy);

  protected abstract DvCodedText extractPrescriptionType(final T therapy);

  protected DvText extractAdministrationMethod(final T therapy)
  {
    return null;
  }

  protected MedicalDevice extractAdministrationDevice(final T therapy)
  {
    return null;
  }

  protected List<DvText> extractMoreAdditionalInstructions(final T therapy)
  {
    return new ArrayList<>();
  }

  protected List<DvText> extractDosageJustification(final T therapy)
  {
    return new ArrayList<>();
  }

  private List<DvText> extractAdditionalInstructions(final T therapy)
  {
    final List<DvText> additionalInstructions = new ArrayList<>();
    final String applicationPrecondition = therapy.getApplicationPrecondition();
    if (applicationPrecondition != null)
    {
      additionalInstructions.add(MedicationAdditionalInstructionEnum.valueOf(applicationPrecondition).getDvCodedText());
    }
    additionalInstructions.addAll(extractMoreAdditionalInstructions(therapy));
    return additionalInstructions;
  }

  private DvText extractClinicalIndication(final TherapyDto therapy)
  {
    final IndicationDto indication = therapy.getClinicalIndication();
    if (indication != null)
    {
      if (indication.getId() != null)
      {
        return DataValueUtils.getLocalCodedText(indication.getId(), indication.getName());
      }
      return EhrValueUtils.getText(indication.getName());
    }
    return null;
  }

  private OrderDetails extractOrderDetails(final TherapyDto therapyDto)
  {
    final OrderDetails orderDetails = new OrderDetails();
    orderDetails.setOrderStartDateTime(DataValueUtils.getDateTime(therapyDto.getStart()));
    orderDetails.setOrderStopDateTime(DataValueUtils.getDateTime(therapyDto.getEnd()));
    return orderDetails;
  }

  private List<DvCodedText> extractRoutes(final TherapyDto therapy)
  {
    return therapy.getRoutes().stream()
        .map(r -> DataValueUtils.getLocalCodedText(String.valueOf(r.getId()), r.getName()))
        .collect(Collectors.toList());
  }

  protected AdditionalDetails extractAdditionalDetails(final T therapy)
  {
    final AdditionalDetails additionalDetails = new AdditionalDetails();

    additionalDetails.setPrescriptionType(extractPrescriptionType(therapy));

    if (therapy.getMaxDosePercentage() != null)
    {
      additionalDetails.setMaxDosePercentage(DataValueUtils.getPercentProportion(therapy.getMaxDosePercentage()));
    }

    if (therapy.getStartCriterion() != null)
    {
      if (MedicationStartCriterionEnum.valueOf(therapy.getStartCriterion()) == MedicationStartCriterionEnum.BY_DOCTOR_ORDERS)
      {
        additionalDetails.setDoctorsOrder(DataValueUtils.getBoolean(true));
      }
    }

    if (therapy.getSelfAdministeringActionEnum() != null)
    {
      additionalDetails.setSelfAdministrationType(therapy.getSelfAdministeringActionEnum().getDvCodedText());
      additionalDetails.setSelfAdministrationStart(DataValueUtils.getDateTime(therapy.getSelfAdministeringLastChange()));
    }

    final ReleaseDetailsDto releaseDetails = therapy.getReleaseDetails();
    if (releaseDetails != null)
    {
      if (releaseDetails.getType() != null)
      {
        additionalDetails.setReleaseDetailsType(releaseDetails.getType().getDvCodedText());
      }
      if (releaseDetails.getHours() != null)
      {
        additionalDetails.setReleaseDetailsInterval(
            DataValueUtils.getDuration(0, 0, 0, releaseDetails.getHours(), 0, 0));
      }
    }

    additionalDetails.setInformationSource(
        therapy.getInformationSources().stream()
            .map(i -> DataValueUtils.getLocalCodedText(String.valueOf(i.getId()), i.getName()))
            .collect(Collectors.toList()));

    if (therapy.getPastTherapyStart() != null)
    {
      additionalDetails.setPastTherapyStart(ConversionUtils.toDvDateTime(therapy.getPastTherapyStart()));
    }

    return additionalDetails;
  }

  private MedicationSafety extractMedicationSafety(final T therapy)
  {
    final MedicationSafety medicationSafety = new MedicationSafety();

    if (therapy.getMaxDailyFrequency() != null)
    {
      final MaximumDose maximumDose = new MaximumDose();
      maximumDose.setMaximumAmount(DataValueUtils.getQuantity(therapy.getMaxDailyFrequency(), "1"));
      maximumDose.setMaximumAmountUnit(DataValueUtils.getText("doses"));
      maximumDose.setAllowedPeriod(DataValueUtils.getDuration(0, 0, 1, 0, 0, 0));
      medicationSafety.setMaximumDose(maximumDose);
    }

    medicationSafety.setExceptionalSafetyOverride(DataValueUtils.getBoolean(!therapy.getCriticalWarnings().isEmpty()));

    medicationSafety.setSafetyOverrides(
        therapy.getCriticalWarnings().stream()
            .map(w -> {
              final SafetyOverride safetyOverride = new SafetyOverride();
              safetyOverride.setOverridenSafetyAdvice(DataValueUtils.getText(w));
              return safetyOverride;
            })
            .collect(Collectors.toList()));
    return medicationSafety;
  }

  private DispenseDirections extractDispenseDirections(final T therapy)
  {
    final DispenseDetailsDto dispenseDetails = therapy.getDispenseDetails();
    if (dispenseDetails != null)
    {
      final DispenseDirections dispenseDirections = new DispenseDirections();

      dispenseDirections.setDispenseAmount(extractMedicationSupplyAmount(dispenseDetails));

      if (dispenseDetails.getControlledDrugSupply() != null)
      {
        final List<Medication> medications = dispenseDetails.getControlledDrugSupply().stream()
            .map(this::extractMedication)
            .collect(Collectors.toList());
        dispenseDirections.setDispenseDetails(medications);
      }

      final NamedIdDto dispenseSource = dispenseDetails.getDispenseSource();
      if (dispenseSource != null)
      {
        dispenseDirections.setDispenseInstructions(DataValueUtils.getLocalCodedText(
            String.valueOf(dispenseSource.getId()),
            dispenseSource.getName()));
      }
      return dispenseDirections;
    }
    return null;
  }

  private MedicationSupplyAmount extractMedicationSupplyAmount(final DispenseDetailsDto dispenseDetails)
  {
    final MedicationSupplyAmount medicationSupplyAmount = new MedicationSupplyAmount();
    if (dispenseDetails.getQuantity() != null)
    {
      medicationSupplyAmount.setAmount(DataValueUtils.getQuantity(dispenseDetails.getQuantity(), "1"));
    }

    if (dispenseDetails.getUnit() != null)
    {
      medicationSupplyAmount.setUnits(DataValueUtils.getText(dispenseDetails.getUnit()));
    }

    if (dispenseDetails.getDaysDuration() != null)
    {
      medicationSupplyAmount.setDurationOfSupply(
          DataValueUtils.getDuration(
              0,
              0,
              dispenseDetails.getDaysDuration(),
              0,
              0,
              0));
    }
    return medicationSupplyAmount;
  }

  private Medication extractMedication(final ControlledDrugSupplyDto controlledDrugSupply)
  {
    final Medication medication = new Medication();
    medication.setComponentName(DataValueUtils.getLocalCodedText(
        String.valueOf(controlledDrugSupply.getMedication().getId()),
        controlledDrugSupply.getMedication().getName()));
    medication.setAmountValue(DataValueUtils.getQuantity(controlledDrugSupply.getQuantity(), "1"));
    medication.setAmountUnit(DataValueUtils.getText(controlledDrugSupply.getUnit()));
    return medication;
  }

  private MedicationAuthorisationSlovenia extractAuthorisationDirection(final T therapy)
  {
    if (therapy.getPrescriptionLocalDetails() instanceof EERPrescriptionLocalDetailsDto)
    {
      final EERPrescriptionLocalDetailsDto eerDetails = (EERPrescriptionLocalDetailsDto)therapy.getPrescriptionLocalDetails();
      final MedicationAuthorisationSlovenia authorisation = new MedicationAuthorisationSlovenia();
      if (eerDetails.getPrescriptionDocumentType() != null)
      {
        authorisation.setPrescriptionDocumentType(eerDetails.getPrescriptionDocumentType().getDvCodedText());
      }
      authorisation.setAdditionalInstructionsForPharmacist(EhrValueUtils.getText(eerDetails.getInstructionsToPharmacist()));

      final Integer prescriptionRepetition = eerDetails.getPrescriptionRepetition();
      final boolean renewable = prescriptionRepetition != null && prescriptionRepetition > 0;
      authorisation.setRenewable(DataValueUtils.getBoolean(renewable));
      if (renewable)
      {
        authorisation.setMaximumNumberOfDispenses(EhrValueUtils.getCount((long)prescriptionRepetition));
      }
      authorisation.setDoNotSwitch(DataValueUtils.getBoolean(eerDetails.isDoNotSwitch()));
      authorisation.setUrgent(DataValueUtils.getBoolean(eerDetails.isUrgent()));
      authorisation.setTypeOfPrescription(
          eerDetails.isMagistralPreparation() ?
          OutpatientPrescriptionType.MAGISTRAL.getDvCodedText() :
          OutpatientPrescriptionType.BRAND_NAME.getDvCodedText());
      authorisation.setInteractions(DataValueUtils.getBoolean(true));
      authorisation.setMaximumDoseExceeded(DataValueUtils.getBoolean(eerDetails.isMaxDoseExceeded()));
      if (eerDetails.getIllnessConditionType() != null)
      {
        authorisation.setIllnessConditionType(eerDetails.getIllnessConditionType().getDvCodedText());
      }
      if (eerDetails.getRemainingDispenses() != null)
      {
        authorisation.setNumberOfRemainingDispenses(EhrValueUtils.getCount((long)eerDetails.getRemainingDispenses()));
      }
      if (eerDetails.getPayer() != null)
      {
        authorisation.setPayer(eerDetails.getPayer().getDvCodedText());
      }

      return authorisation;
    }
    return null;
  }
}
