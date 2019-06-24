package com.marand.thinkmed.medications.administration.converter;

import com.google.common.base.Preconditions;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationJobPerformerEnum;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.ParticipationTypeEnum;
import com.marand.thinkmed.medications.dto.SubstitutionType;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdjustOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationStatusEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.PlannedDoseAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicalDevice;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.PlannedAdministration;
import com.marand.thinkmed.medications.ehr.model.Procedure;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.therapy.converter.fromehr.TherapyFromEhrUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 * @author Mitja Lapajne
 */

@Component
public class AdministrationFromEhrConverter
{
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private UnitsProvider unitsProvider;
  private TherapyFromEhrUtils therapyFromEhrUtils;

  @Autowired
  public void setUnitsProvider(final UnitsProvider unitsProvider)
  {
    this.unitsProvider = unitsProvider;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setTherapyFromEhrUtils(final TherapyFromEhrUtils therapyFromEhrUtils)
  {
    this.therapyFromEhrUtils = therapyFromEhrUtils;
  }

  public AdministrationDto convertToAdministrationDto(
      final @NonNull MedicationAdministration administration,
      final @NonNull InpatientPrescription prescription)
  {
    final AdministrationDto administrationDto;
    final MedicationManagement action = administration.getMedicationManagement();
    final Procedure procedure = administration.getProcedure();

    if (action != null)
    {
      administrationDto = buildAdministrationFromAction(action, prescription);

      if (procedure != null && administrationDto instanceof InfusionBagAdministration)
      {
        ((InfusionBagAdministration)administrationDto).setInfusionBag(extractInfusionBag(procedure));
      }
    }
    else if (procedure != null)
    {
      administrationDto = buildAdministrationFromProcedure(procedure);
    }
    else
    {
      throw new IllegalArgumentException("Medication Administration composition must have Medication Management or Procedure");
    }

    administrationDto.setAdministrationId(administration.getUid());
    administrationDto.setComposerName(buildComposerName(administration));
    administrationDto.setTherapyId(TherapyIdUtils.createTherapyId(prescription));

    return administrationDto;
  }

  private String buildComposerName(final MedicationAdministration administration)
  {
    final String composerName = administration.getComposer().getName();
    return MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getCode().equals(composerName)
           ? Dictionary.getEntry("automatically.charted")
           : composerName;
  }

  private AdministrationDto buildAdministrationFromAction(
      final MedicationManagement action,
      final InpatientPrescription prescription)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
    final MedicationOrder order = prescription.getMedicationOrder();
    final AdministrationTypeEnum administrationType = extractAdministrationType(action);
    final AdditionalDetails additionalDetails = action.getAdditionalDetails();
    final PlannedAdministration plannedAdministration = additionalDetails.getPlannedAdministration();

    Preconditions.checkArgument(
        actionEnum == MedicationActionEnum.ADMINISTER
            || actionEnum == MedicationActionEnum.WITHHOLD
            || actionEnum == MedicationActionEnum.DEFER,
        "Invalid action enum " + actionEnum);

    final AdministrationDto administration = createEmptyAdministration(order, administrationType);

    // set administered dose
    if (administration instanceof DoseAdministration)
    {
      ((DoseAdministration)administration).setAdministeredDose(buildTherapyDose(action, order, false));
    }

    if (administration instanceof StartAdministrationDto)
    {
      ((StartAdministrationDto)administration).setSubstituteMedication(extractSubstituteMedication(action));
    }

    if (administration instanceof OxygenAdministration)
    {
      final MedicalDevice device = action.getAdministrationDetails().getAdministrationDevice();
      final MedicalDevice plannedDevice = plannedAdministration.getPlannedStartingDevice();

      ((OxygenAdministration)administration).setStartingDevice(therapyFromEhrUtils.buildOxygenDevice(device));
      ((OxygenAdministration)administration).setPlannedStartingDevice(therapyFromEhrUtils.buildOxygenDevice(plannedDevice));
    }

    administration.setAdministrationStatus(
        actionEnum == MedicationActionEnum.ADMINISTER
        ? AdministrationStatusEnum.COMPLETED
        : AdministrationStatusEnum.FAILED);

    final DateTime administeredTime = DataValueUtils.getDateTime(action.getTime());
    administration.setAdministrationTime(administeredTime);
    administration.setSelfAdministrationType(SelfAdministeringActionEnum.valueOf(additionalDetails.getSelfAdministrationType()));

    administration.setAdministrationResult(extractAdministrationResult(action));
    administration.setNotAdministeredReason(extractNotAdministeredReason(action));
    administration.setWitness(extractWitness(action));

    if (action.getComment() != null)
    {
      administration.setComment(action.getComment().getValue());
    }
    administration.setRoute(extractRoute(action));
    administration.setGroupUUId(extractGroupUUId(action));

    // additional details (planned administration data)
    if (action.getScheduledDateTime() != null)
    {
      final DateTime plannedTime = DataValueUtils.getDateTime(action.getScheduledDateTime());
      administration.setPlannedTime(plannedTime);

      // change status to LATE/EARLY based on planned time
      if (actionEnum == MedicationActionEnum.ADMINISTER)
      {
        administration.setAdministrationStatus(AdministrationStatusEnum.build(plannedTime, administeredTime));
      }

    }
    if (plannedAdministration.getDoctorsComment() != null)
    {
      administration.setDoctorsComment(plannedAdministration.getDoctorsComment().getValue());
    }
    if (plannedAdministration.getDoctorsConfirmation() != null)
    {
      administration.setDoctorConfirmation(plannedAdministration.getDoctorsConfirmation().isValue());
    }
    if (plannedAdministration.getTaskId() != null)
    {
      administration.setTaskId(plannedAdministration.getTaskId().getValue());
    }

    // planned dose
    if (administration instanceof PlannedDoseAdministration)
    {
      if (plannedAdministration.getDifferentDoseAdministered() != null)
      {
        ((PlannedDoseAdministration)administration).setDifferentFromOrder(plannedAdministration.getDifferentDoseAdministered().isValue());
      }
      if (plannedAdministration.getPlannedDosage() != null)
      {
        ((PlannedDoseAdministration)administration).setPlannedDose(buildTherapyDose(action, order, true));
      }
    }

    return administration;
  }

  private String extractGroupUUId(final MedicationManagement action)
  {
    final DvText group = action.getAdditionalDetails().getAdministrationGroup();
    return group != null ? group.getValue() : null;
  }

  private NamedExternalDto extractWitness(final MedicationManagement action)
  {
    return action.getOtherParticipations()
        .stream()
        .filter(p -> p.getFunction().equals(ParticipationTypeEnum.WITNESS.getCode()))
        .map(p -> new NamedExternalDto(p.getId(), p.getName()))
        .findFirst()
        .orElse(null);
  }

  private AdministrationDto createEmptyAdministration(
      final MedicationOrder order,
      final AdministrationTypeEnum administrationType)
  {
    if (MedicationsEhrUtils.isOxygen(order))
    {
      return createEmptyOxygenAdministration(administrationType);
    }
    else if (administrationType == AdministrationTypeEnum.START)
    {
      return new StartAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.BOLUS)
    {
      return new BolusAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      return new AdjustInfusionAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.STOP)
    {
      return new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("Administration type not supported");
    }
  }

  private MedicationRouteDto extractRoute(final MedicationManagement action)
  {
    final DvCodedText route = action.getAdministrationDetails().getRoute();
    if (route != null)
    {
      final MedicationRouteDto routeDto = new MedicationRouteDto();
      routeDto.setId(Long.valueOf(route.getDefiningCode().getCodeString()));
      routeDto.setName(route.getValue());
      return routeDto;
    }
    return null;
  }

  private MedicationDto extractSubstituteMedication(final MedicationManagement action)
  {
    if (SubstitutionType.PERFORMED.matches(action.getSubstitution()))
    {
      final Long medicationId = Long.parseLong(((DvCodedText)action.getMedicationItem()).getDefiningCode().getCodeString());
      return medicationsValueHolderProvider.getMedication(medicationId);
    }

    return null;
  }

  public TherapyDoseDto buildTherapyDose(
      final MedicationManagement action,
      final MedicationOrder order,
      final boolean buildPlannedDose)
  {
    final Dosage dosage = buildPlannedDose
                          ? action.getAdditionalDetails().getPlannedAdministration().getPlannedDosage()
                          : action.getAmount();

    if (MedicationsEhrUtils.isOxygen(order))
    {
      return buildOxygenDose(dosage);
    }

    if (AdministrationTypeEnum.DOSE_ADMINISTRATION.contains(extractAdministrationType(action)))
    {
      final boolean hasRate = isRateAdministration(dosage);
      final boolean hasQuantity = dosage != null;

      if (hasRate) // RATE
      {
        return buildTherapyDoseDtoForRate(dosage);
      }
      else if (hasQuantity) // QUANTITY, VOLUME_SUM
      {
        return buildTherapyDoseForQuantity(dosage, MedicationsEhrUtils.isAdHocMixture(action.getMedicationDetails()));
      }
    }

    return null;
  }

  private TherapyDoseDto buildOxygenDose(final Dosage dosage)
  {
    if (dosage == null)
    {
      return null;
    }

    final TherapyDoseDto dose = new TherapyDoseDto();
    dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
    dose.setNumerator(((DvQuantity)dosage.getAdministrationRate()).getMagnitude());
    dose.setNumeratorUnit(unitsProvider.getDisplayName(KnownUnitType.L_MIN));
    return dose;
  }

  private TherapyDoseDto buildTherapyDoseForQuantity(final Dosage dosage, final boolean adHocMixture)
  {
    if (dosage == null)
    {
      return null;
    }

    final TherapyDoseDto therapyDose = new TherapyDoseDto();
    if (dosage.getDoseAmount() != null)
    {
      therapyDose.setNumerator(((DvQuantity)dosage.getDoseAmount()).getMagnitude());
      therapyDose.setNumeratorUnit(dosage.getDoseUnit().getValue());
      therapyDose.setTherapyDoseTypeEnum(adHocMixture ? TherapyDoseTypeEnum.VOLUME_SUM : TherapyDoseTypeEnum.QUANTITY);
    }
    if (dosage.getAlternateDoseAmount() != null)
    {
      therapyDose.setDenominator(((DvQuantity)dosage.getAlternateDoseAmount()).getMagnitude());
      therapyDose.setDenominatorUnit(dosage.getAlternateDoseUnit().getValue());
      therapyDose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.QUANTITY);
    }
    return therapyDose;
  }

  public TherapyDoseDto buildTherapyDoseDtoForRate(final Dosage dosage)
  {
    if (dosage == null)
    {
      return null;
    }

    final TherapyDoseDto therapyDoseDto = new TherapyDoseDto();

    final DataValue administrationRate = dosage.getAdministrationRate();

    //can be DvText for some records (BOLUS administrations) that were not correctly migrated
    //these can be ignored
    if (administrationRate instanceof DvQuantity)
    {
      therapyDoseDto.setNumerator(((DvQuantity)administrationRate).getMagnitude());
      therapyDoseDto.setNumeratorUnit(((DvQuantity)administrationRate).getUnits());
    }

    final String doseFormula = EhrValueUtils.getText(dosage.getDoseFormula());
    therapyDoseDto.setDenominator(therapyFromEhrUtils.extractDoseFormulaValue(doseFormula));
    therapyDoseDto.setDenominatorUnit(therapyFromEhrUtils.extractDoseFormulaUnit(doseFormula));

    if (therapyDoseDto.getNumerator() != null || therapyDoseDto.getDenominator() != null)
    {
      therapyDoseDto.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE);
      return therapyDoseDto;
    }
    return null;
  }

  private AdministrationTypeEnum extractAdministrationType(final MedicationManagement action)
  {
    return Opt
        .of(AdministrationTypeEnum.valueOf(action.getAdditionalDetails().getAdministrationType()))
        .orElseThrow(() -> new IllegalStateException("Action does not contain administration type!"));
  }

  private CodedNameDto extractNotAdministeredReason(final MedicationManagement action)
  {
    return action.getReason().stream()
        .map(r -> new CodedNameDto(((DvCodedText)r).getDefiningCode().getCodeString(), r.getValue()))
        .findFirst()
        .orElse(null);
  }

  private boolean isRateAdministration(final Dosage dosage)
  {
    return dosage != null && dosage.getAdministrationRate() != null;
  }

  private AdministrationResultEnum extractAdministrationResult(final MedicationManagement action)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);

    if (actionEnum == MedicationActionEnum.DEFER)
    {
      return AdministrationResultEnum.DEFER;
    }
    if (actionEnum == MedicationActionEnum.WITHHOLD)
    {
      return AdministrationResultEnum.NOT_GIVEN;
    }

    return SelfAdministeringActionEnum.valueOf(action.getAdditionalDetails().getSelfAdministrationType()) != null
           ? AdministrationResultEnum.SELF_ADMINISTERED
           : AdministrationResultEnum.GIVEN;
  }

  private InfusionBagDto extractInfusionBag(final Procedure procedure)
  {
    return Opt.of(procedure.getSupplyAmount())
        .map(a -> new InfusionBagDto(a.getAmount().getMagnitude(), a.getUnits().getValue()))
        .orElse(null);
  }

  private InfusionSetChangeDto buildAdministrationFromProcedure(final Procedure procedure)
  {
    final InfusionSetChangeDto administration = new InfusionSetChangeDto();

    administration.setAdministrationTime(DataValueUtils.getDateTime(procedure.getTime()));
    administration.setAdministrationStatus(AdministrationStatusEnum.COMPLETED);
    administration.setInfusionSetChangeEnum(InfusionSetChangeEnum.valueOf(procedure.getProcedureName()));
    administration.setComment(EhrValueUtils.getText(procedure.getComment()));
    administration.setInfusionBag(extractInfusionBag(procedure));
    administration.setAdministrationResult(AdministrationResultEnum.GIVEN);

    return administration;
  }

  private AdministrationDto createEmptyOxygenAdministration(final AdministrationTypeEnum administrationType)
  {
    final AdministrationDto administration;
    if (administrationType == AdministrationTypeEnum.START)
    {
      administration = new StartOxygenAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      administration = new AdjustOxygenAdministrationDto();
    }
    else if (administrationType == AdministrationTypeEnum.STOP)
    {
      return new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("This administration type is not supported for oxygen therapy");
    }

    return administration;
  }
}
