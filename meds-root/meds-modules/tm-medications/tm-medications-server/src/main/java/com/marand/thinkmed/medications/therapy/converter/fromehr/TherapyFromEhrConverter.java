package com.marand.thinkmed.medications.therapy.converter.fromehr;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.util.ConversionUtils;
import com.marand.thinkmed.medications.MedicationStartCriterionEnum;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationAdditionalInstructionEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionSystemEnum;
import com.marand.thinkmed.medications.api.internal.dto.eer.IllnessConditionType;
import com.marand.thinkmed.medications.api.internal.dto.eer.OutpatientPrescriptionDocumentType;
import com.marand.thinkmed.medications.api.internal.dto.eer.OutpatientPrescriptionType;
import com.marand.thinkmed.medications.api.internal.dto.eer.Payer;
import com.marand.thinkmed.medications.ehr.model.DayOfWeek;
import com.marand.thinkmed.medications.ehr.model.DispenseDirections;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationAuthorisationSlovenia;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.MedicationSupplyAmount;
import com.marand.thinkmed.medications.ehr.model.TherapeuticDirection;
import com.marand.thinkmed.medications.ehr.model.TimingDaily;
import com.marand.thinkmed.medications.ehr.model.TimingNonDaily;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.valueholder.MedicationRoutesValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public abstract class TherapyFromEhrConverter<T extends TherapyDto>
{
  private TherapyFromEhrUtils therapyFromEhrUtils;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private MedicationRoutesValueHolder medicationRoutesValueHolder;

  @Autowired
  public void setTherapyFromEhrUtils(final TherapyFromEhrUtils therapyFromEhrUtils)
  {
    this.therapyFromEhrUtils = therapyFromEhrUtils;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setMedicationRoutesValueHolder(final MedicationRoutesValueHolder medicationRoutesValueHolder)
  {
    this.medicationRoutesValueHolder = medicationRoutesValueHolder;
  }

  protected TherapyFromEhrUtils getTherapyFromEhrUtils()
  {
    return therapyFromEhrUtils;
  }

  public MedicationsValueHolderProvider getMedicationsValueHolderProvider()
  {
    return medicationsValueHolderProvider;
  }

  public T mapTherapyFromEhr(
      final MedicationOrder medicationOrder,
      final String compositionUid,
      final DateTime createdTimestamp)
  {
    final T therapy = createTherapyDto(medicationOrder);

    therapy.setCompositionUid(compositionUid);
    therapy.setEhrOrderName("Medication order");
    therapy.setCreatedTimestamp(createdTimestamp);
    therapy.setRoutes(extractRoutes(medicationOrder));
    therapy.setDosingFrequency(extractDosingFrequency(medicationOrder));
    therapy.setDaysOfWeek(extractDaysOfWeek(medicationOrder));
    therapy.setDosingDaysFrequency(extractDosingDaysFrequency(medicationOrder));
    therapy.setStart(DataValueUtils.getDateTime(medicationOrder.getOrderDetails().getOrderStartDateTime()));
    therapy.setEnd(DataValueUtils.getDateTime(medicationOrder.getOrderDetails().getOrderStopDateTime()));
    therapy.setPastTherapyStart(extractPastTherapyStart(medicationOrder));
    therapy.setComment(EhrValueUtils.getText(medicationOrder.getComment()));
    therapy.setClinicalIndication(extractClinicalIndication(medicationOrder));
    therapy.setApplicationPrecondition(extractApplicationPrecondition(medicationOrder));
    therapy.setMaxDailyFrequency(extractMaxDailyFrequency(medicationOrder));
    therapy.setWhenNeeded(extractWhenNeeded(medicationOrder));
    therapy.setDoseType(extractDoseType(medicationOrder));
    therapy.setMaxDosePercentage(extractMaxDosePercentage(medicationOrder));
    therapy.setStartCriterion(extractStartCriterion(medicationOrder));
    therapy.setReleaseDetails(extractReleaseDetails(medicationOrder));
    therapy.setCriticalWarnings(extractCriticalWarnings(medicationOrder));
    therapy.setSelfAdministeringActionEnum(extractSelfAdministeringActionEnum(medicationOrder));
    therapy.setSelfAdministeringLastChange(extractSelfAdministeringLastChange(medicationOrder));
    therapy.setInformationSources(extractInformationSources(medicationOrder));
    therapy.setDispenseDetails(extractDispenseDirections(medicationOrder));
    therapy.setTherapyDescription(EhrValueUtils.getText(medicationOrder.getOverallDirectionsDescription()));
    therapy.setFormattedTherapyDisplay(EhrValueUtils.getParsableValue(medicationOrder.getParsableDirections()));
    therapy.setPrescriptionLocalDetails(extractPrescriptionLocalDetails(medicationOrder));

    return therapy;
  }

  public abstract boolean isFor(final MedicationOrder medicationOrder);

  protected abstract T createTherapyDto(final MedicationOrder medicationOrder);

  protected abstract TherapyDoseTypeEnum extractDoseType(final MedicationOrder medicationOrder);

  private IndicationDto extractClinicalIndication(final MedicationOrder medicationOrder)
  {
    final DvText indication = medicationOrder.getClinicalIndication();
    if (indication != null)
    {
      if (indication instanceof DvCodedText)
      {
        final DvCodedText codedIndication = (DvCodedText)indication;
        return new IndicationDto(codedIndication.getDefiningCode().getCodeString(), codedIndication.getValue());
      }
      else
      {
        return new IndicationDto(null, indication.getValue());
      }
    }
    return null;
  }

  private String extractApplicationPrecondition(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getAdditionalInstruction().stream()
        .filter(a -> a instanceof DvCodedText)
        .map(a -> (DvCodedText)a)
        .map(MedicationAdditionalInstructionEnum::valueOf)
        .filter(MedicationAdditionalInstructionEnum.APPLICATION_PRECONDITION::contains)
        .map(Enum::name)
        .findFirst()
        .orElse(null);
  }

  private DateTime extractPastTherapyStart(final MedicationOrder medicationOrder)
  {
    final DvDateTime pastTherapyStart = medicationOrder.getAdditionalDetails().getPastTherapyStart();
    return pastTherapyStart != null ? ConversionUtils.toDateTime(pastTherapyStart) : null;
  }

  private List<MedicationRouteDto> extractRoutes(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getRoute().stream()
        .map(r -> medicationRoutesValueHolder.getValue().get(Long.valueOf(r.getDefiningCode().getCodeString())))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  public static DosingFrequencyDto extractDosingFrequency(final MedicationOrder medicationOrder)
  {
    final Opt<TherapeuticDirection> therapeuticDirections = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0));
    final Opt<TimingDaily> timingDaily = Opt.resolve(() -> therapeuticDirections.get().getDosage().get(0).getTiming());

    if (timingDaily.isPresent())
    {
      final TimingDaily timing = timingDaily.get();
      final TherapeuticDirection therapeuticDirection = therapeuticDirections.get();

      if (timing.getInterval() != null &&
          (DataValueUtils.getPeriod(timing.getInterval()).getHours() > 0 ||
              DataValueUtils.getPeriod(timing.getInterval()).getMinutes() > 0))
      {
        final int hoursBetweenDoses = DataValueUtils.getPeriod(timing.getInterval()).getHours();
        final int minutesBetweenDoses = DataValueUtils.getPeriod(timing.getInterval()).getMinutes();
        final double betweenDoses = hoursBetweenDoses + minutesBetweenDoses / 60.0;
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.BETWEEN_DOSES, betweenDoses);
      }
      if (timing.getFrequency() != null)
      {
        final int dailyCount = (int)timing.getFrequency().getMagnitude();
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, (double)dailyCount);
      }
      if (timing.getSpecificEvent() != null &&
          DosingFrequencyTypeEnum.valueOf((DvCodedText)timing.getSpecificEvent()) == DosingFrequencyTypeEnum.MORNING)
      {
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
      }
      if (timing.getSpecificEvent() != null &&
          DosingFrequencyTypeEnum.valueOf((DvCodedText)timing.getSpecificEvent()) == DosingFrequencyTypeEnum.NOON)
      {
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.NOON);
      }
      if (timing.getSpecificEvent() != null &&
          DosingFrequencyTypeEnum.valueOf((DvCodedText)timing.getSpecificEvent()) == DosingFrequencyTypeEnum.EVENING)
      {
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.EVENING);
      }

      if (therapeuticDirection.getMaximumNumberOfAdministration() != null &&
          therapeuticDirection.getMaximumNumberOfAdministration().getMagnitude() == 1L)
      {
        return new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX);
      }
    }
    return null;
  }

  private List<String> extractDaysOfWeek(final MedicationOrder medicationOrder)
  {
    final Opt<TimingNonDaily> directionRepetition = Opt.resolve(() ->
        medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDirectionRepetition());
    if (directionRepetition.isPresent())
    {
      return directionRepetition.get().getSpecificDayOfWeek().stream()
          .map(d -> DayOfWeek.valueOf(d).name())
          .collect(Collectors.toList());
    }
    return Lists.newArrayList();
  }

  private Integer extractDosingDaysFrequency(final MedicationOrder medicationOrder)
  {
    final Opt<DvDuration> repetitionInterval = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDirectionRepetition()
            .getRepetitionInterval());
    if (repetitionInterval.isPresent())
    {
      return DataValueUtils.getPeriod(repetitionInterval.get()).getDays();
    }
    return null;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private Integer extractMaxDailyFrequency(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(() -> (int)medicationOrder.getMedicationSafety().getMaximumDose().getMaximumAmount().getMagnitude())
        .orElse(null);
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private Boolean extractWhenNeeded(final MedicationOrder medicationOrder)
  {
    final Opt<DvBoolean> asRequired = Opt.resolve(
        () -> medicationOrder.getStructuredDoseAndTimingDirections().get(0).getDosage().get(0).getTiming().getAsRequired());
    return asRequired.isPresent() ? asRequired.get().isValue() : null;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private Integer extractMaxDosePercentage(final MedicationOrder medicationOrder)
  {
    return Opt.resolve(() -> (int)medicationOrder.getAdditionalDetails().getMaxDosePercentage().getNumerator()).orElse(null);
  }

  private String extractStartCriterion(final MedicationOrder medicationOrder)
  {
    final boolean byDoctorsOrder = Opt.resolve(
        () -> medicationOrder.getAdditionalDetails().getDoctorsOrder().isValue()).orElse(false);
    return byDoctorsOrder ? MedicationStartCriterionEnum.BY_DOCTOR_ORDERS.name() : null;
  }

  private ReleaseDetailsDto extractReleaseDetails(final MedicationOrder medicationOrder)
  {
    final DvCodedText releaseDetailsType = medicationOrder.getAdditionalDetails().getReleaseDetailsType();
    if (releaseDetailsType != null)
    {
      final Integer releaseDetailsDuration = Opt.resolve(
          () -> DataValueUtils.getPeriod(medicationOrder.getAdditionalDetails().getReleaseDetailsInterval()).getHours())
          .orElse(null);
      return new ReleaseDetailsDto(ReleaseType.valueOf(releaseDetailsType), releaseDetailsDuration);
    }
    return null;
  }

  private List<String> extractCriticalWarnings(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getMedicationSafety().getSafetyOverrides().stream()
        .map(s -> s.getOverridenSafetyAdvice().getValue())
        .collect(Collectors.toList());
  }

  private SelfAdministeringActionEnum extractSelfAdministeringActionEnum(final MedicationOrder medicationOrder)
  {
    return SelfAdministeringActionEnum.valueOf(medicationOrder.getAdditionalDetails().getSelfAdministrationType());
  }

  private DateTime extractSelfAdministeringLastChange(final MedicationOrder medicationOrder)
  {
    return DataValueUtils.getDateTime(medicationOrder.getAdditionalDetails().getSelfAdministrationStart());
  }

  private List<NamedIdDto> extractInformationSources(final MedicationOrder medicationOrder)
  {
    return medicationOrder.getAdditionalDetails().getInformationSource().stream()
        .map(i -> new NamedIdDto(Long.valueOf(i.getDefiningCode().getCodeString()), i.getValue()))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private DispenseDetailsDto extractDispenseDirections(final MedicationOrder medicationOrder)
  {
    final DispenseDirections dispenseDirections = medicationOrder.getDispenseDirections();
    if (dispenseDirections != null)
    {
      final DispenseDetailsDto dispenseDetailsDto = new DispenseDetailsDto();
      final DvText dispenseInstructions = dispenseDirections.getDispenseInstructions();
      if (dispenseInstructions != null)
      {
        dispenseDetailsDto.setDispenseSource(new NamedIdDto(
            Long.valueOf(((DvCodedText)dispenseInstructions).getDefiningCode().getCodeString()),
            dispenseInstructions.getValue()
        ));
      }

      final MedicationSupplyAmount medicationSupplyAmount = dispenseDirections.getDispenseAmount();
      if (medicationSupplyAmount != null)
      {
        if (medicationSupplyAmount.getAmount() != null)
        {
          dispenseDetailsDto.setQuantity((int)medicationSupplyAmount.getAmount().getMagnitude());
        }

        if (medicationSupplyAmount.getUnits() !=  null)
        {
          dispenseDetailsDto.setUnit(medicationSupplyAmount.getUnits().getValue());
        }

        if (medicationSupplyAmount.getDurationOfSupply() != null)
        {
          dispenseDetailsDto.setDaysDuration(
              DataValueUtils.getPeriod(medicationSupplyAmount.getDurationOfSupply()).getDays());
        }
      }

      final List<Medication> controlledDrugs = dispenseDirections.getDispenseDetails();
      final List<ControlledDrugSupplyDto> controlledDrugSupplies = controlledDrugs.stream()
          .map(this::extractControlledDrugSupplyDto)
          .collect(Collectors.toList());
      dispenseDetailsDto.setControlledDrugSupply(controlledDrugSupplies);
      return dispenseDetailsDto;
    }
    return null;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private ControlledDrugSupplyDto extractControlledDrugSupplyDto(final Medication medication)
  {
    final NamedIdDto medicationName = new NamedIdDto(
        Long.valueOf(((DvCodedText)medication.getComponentName()).getDefiningCode().getCodeString()),
        medication.getComponentName().getValue());

    final ControlledDrugSupplyDto controlledDrugSupplyDto = new ControlledDrugSupplyDto();
    controlledDrugSupplyDto.setMedication(medicationName);
    controlledDrugSupplyDto.setQuantity((int)medication.getAmountValue().getMagnitude());
    controlledDrugSupplyDto.setUnit(medication.getAmountUnit().getValue());
    return controlledDrugSupplyDto;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  private PrescriptionLocalDetailsDto extractPrescriptionLocalDetails(final MedicationOrder medicationOrder)
  {
    final MedicationAuthorisationSlovenia authorisation = medicationOrder.getAuthorisationDirection();

    if (authorisation != null)
    {
      final EERPrescriptionLocalDetailsDto details = new EERPrescriptionLocalDetailsDto();
      details.setPrescriptionSystem(EERPrescriptionSystemEnum.EER.name());
      details.setPrescriptionDocumentType(OutpatientPrescriptionDocumentType.valueOf(authorisation.getPrescriptionDocumentType()));
      details.setInstructionsToPharmacist(EhrValueUtils.getText(authorisation.getAdditionalInstructionsForPharmacist()));
      if (authorisation.getMaximumNumberOfDispenses() != null)
      {
        final int maxNumberOfDispenses = (int)authorisation.getMaximumNumberOfDispenses().getMagnitude();
        details.setPrescriptionRepetition(maxNumberOfDispenses);
      }
      details.setRemainingDispenses(extractRemainingDispenses(authorisation));

      details.setDoNotSwitch(EhrValueUtils.getBooleanValue(authorisation.getDoNotSwitch()));
      details.setUrgent(EhrValueUtils.getBooleanValue(authorisation.getUrgent()));
      details.setMagistralPreparation(OutpatientPrescriptionType.valueOf(authorisation.getTypeOfPrescription()) == OutpatientPrescriptionType.MAGISTRAL);
      details.setMaxDoseExceeded(EhrValueUtils.getBooleanValue(authorisation.getMaximumDoseExceeded()));
      details.setIllnessConditionType(IllnessConditionType.valueOf(authorisation.getIllnessConditionType()));
      details.setPayer(Payer.valueOf(authorisation.getPayer()));

      return details;
    }
    return null;
  }

  private Integer extractRemainingDispenses(final MedicationAuthorisationSlovenia authorisation)
  {
    if (authorisation.getNumberOfRemainingDispenses() != null)
    {
      return (int)authorisation.getNumberOfRemainingDispenses().getMagnitude();
    }
    else if (authorisation.getMaximumNumberOfDispenses() != null)
    {
      return (int)authorisation.getMaximumNumberOfDispenses().getMagnitude();
    }
    return null;
  }
}
