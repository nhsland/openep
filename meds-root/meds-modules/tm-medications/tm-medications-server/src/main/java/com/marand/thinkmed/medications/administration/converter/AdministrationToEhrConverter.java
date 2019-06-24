package com.marand.thinkmed.medications.administration.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.dto.ClinicalInterventionEnum;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.ParticipationTypeEnum;
import com.marand.thinkmed.medications.dto.SubstitutionType;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.ehr.model.Action;
import com.marand.thinkmed.medications.ehr.model.Dosage;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.InstructionDetails;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationSupplyAmount;
import com.marand.thinkmed.medications.ehr.model.Participation;
import com.marand.thinkmed.medications.ehr.model.Procedure;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrUtils;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationToEhrConverter
{
  private AdministrationUtils administrationUtils;
  private TherapyToEhrUtils therapyToEhrUtils;

  @Autowired
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Autowired
  public void setTherapyToEhrUtils(final TherapyToEhrUtils therapyToEhrUtils)
  {
    this.therapyToEhrUtils = therapyToEhrUtils;
  }

  public MedicationAdministration convertAdministration(
      final @NonNull InpatientPrescription prescription,
      final @NonNull AdministrationDto administration,
      final @NonNull AdministrationResultEnum administrationResult,
      final @NonNull String composerName,
      final @NonNull String composerId,
      final String centralCaseId,
      final String careProviderId,
      final @NonNull DateTime when)
  {
    final MedicationAdministration administrationComposition = new MedicationAdministration();

    administrationComposition.setProcedure(buildProcedureForDoseAdministration(administration));
    administrationComposition.setMedicationManagement(buildMedicationManagement(administration, prescription, administrationResult));

    linkActionToInstructions(prescription, administrationComposition.getMedicationManagement());
    addContext(administrationComposition, composerName, composerId, centralCaseId, careProviderId, when);

    return administrationComposition;
  }

  public MedicationAdministration convertSetChangeAdministration(
      final @NonNull InpatientPrescription prescription,
      final @NonNull InfusionSetChangeDto administration,
      final String centralCaseId,
      final String careProviderId,
      final @NonNull DateTime when)
  {
    final MedicationAdministration administrationComposition = new MedicationAdministration();

    administrationComposition.setProcedure(buildProcedureForInfusionSetChange(administration));

    linkActionToInstructions(prescription, administrationComposition.getProcedure());
    addContext(administrationComposition, RequestUser.getFullName(), RequestUser.getId(), centralCaseId, careProviderId, when);

    return administrationComposition;
  }

  private void linkActionToInstructions(final InpatientPrescription prescription, final Action action)
  {
    action.setInstructionDetails(new InstructionDetails());
    action.getInstructionDetails().setActivityId("activities[at0001] ");
    action.getInstructionDetails().setInstructionId(MedicationsEhrUtils.createMedicationOrderLocatableRef(
        prescription.getUid(),
        InpatientPrescription.getMedicationOrderPath()));
  }

  private MedicationManagement buildMedicationManagement(
      final AdministrationDto administrationDto,
      final InpatientPrescription prescription,
      final AdministrationResultEnum administrationResult)
  {
    final MedicationManagement action = new MedicationManagement();

    // rewrite from medication order
    action.setMedicationDetails(prescription.getMedicationOrder().getPreparationDetails());

    // medication item and substitute medication
    action.setMedicationItem(prescription.getMedicationOrder().getMedicationItem());
    final DvCodedText substitute = buildSubstituteMedication(prescription, administrationDto);
    if (substitute != null)
    {
      action.setMedicationItem(substitute);
      action.setSubstitution(SubstitutionType.PERFORMED.toDvCodedText());
    }
    else
    {
      action.setSubstitution(SubstitutionType.NOT_PERFORMED.toDvCodedText());
    }

    // administration details
    action.getAdministrationDetails().setAdministrationMethod(prescription.getMedicationOrder().getAdministrationMethod());
    action.getAdministrationDetails().setAdministrationDevice(prescription.getMedicationOrder().getAdministrationDevice());

    action.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
    action.getAdministrationDetails().setRoute(buildRoute(administrationDto));
    action.setComment(buildComment(administrationDto));
    action.setIsmTransition(MedicationActionEnum.fromAdministrationResult(administrationResult).buildIsmTransition());

    action.setOtherParticipations(buildParticipation(administrationDto));
    action.setReason(buildReason(administrationDto));

    final Dosage dosage = buildDosage(administrationDto, false);
    action.setAmount(dosage);

    // oxygen device and validations
    if (administrationDto instanceof OxygenAdministration)
    {
      final OxygenStartingDevice device = ((OxygenAdministration)administrationDto).getStartingDevice();
      final OxygenStartingDevice plannedDevice = ((OxygenAdministration)administrationDto).getPlannedStartingDevice();

      action.getAdministrationDetails().setAdministrationDevice(therapyToEhrUtils.buildMedicalDeviceForOxygen(device != null ? device : plannedDevice));
      action.getAdditionalDetails().getPlannedAdministration().setPlannedStartingDevice(therapyToEhrUtils.buildMedicalDeviceForOxygen(plannedDevice));

      if (dosage == null && device == null)
      {
        throw new IllegalArgumentException("Either oxygen medical device or dosage must be set when administering oxygen!");
      }
    }

    final AdministrationTypeEnum administrationType = administrationDto.getAdministrationType();

    // additional details
    action.getAdditionalDetails().setAdministrationType(administrationType.getDvText());
    action.getAdditionalDetails().setAdministrationGroup(buildGroupUUid(administrationDto));
    action.getAdditionalDetails().setSelfAdministrationType(buildSelfAdministrationType(administrationDto));
    action.getAdditionalDetails().setBaselineInfusion(prescription.getMedicationOrder().getAdditionalDetails().getBaselineInfusion());

    // additional details (planned administration data)
    if (administrationDto.getTaskId() != null)
    {
      action.getAdditionalDetails().getPlannedAdministration().setTaskId(DataValueUtils.getText(administrationDto.getTaskId()));
    }
    if (administrationDto.getDoctorsComment() != null)
    {
      action.getAdditionalDetails().getPlannedAdministration().setDoctorsComment(DataValueUtils.getText(administrationDto.getDoctorsComment()));
    }
    if (administrationDto.getDoctorConfirmation() != null)
    {
      action.getAdditionalDetails().getPlannedAdministration().setDoctorsConfirmation(DataValueUtils.getBoolean(administrationDto.getDoctorConfirmation()));
    }
    if (administrationDto.getPlannedTime() != null)
    {
      action.setScheduledDateTime(DataValueUtils.getDateTime(administrationDto.getPlannedTime()));
    }

    // planned dose
    action.getAdditionalDetails().getPlannedAdministration().setPlannedDosage(buildDosage(administrationDto, true));
    action.getAdditionalDetails().getPlannedAdministration().setDifferentDoseAdministered(DataValueUtils.getBoolean(
        isDifferentDoseAdministered(
            administrationUtils.getTherapyDose(administrationDto),
            administrationUtils.getPlannedTherapyDose(administrationDto),
            administrationResult,
            administrationType)));

    return action;
  }

  private boolean isDifferentDoseAdministered(
      final TherapyDoseDto administered,
      final TherapyDoseDto planned,
      final AdministrationResultEnum result,
      final AdministrationTypeEnum administrationType)
  {
    if (AdministrationResultEnum.ADMINISTERED.contains(result))
    {
      if (administrationType == AdministrationTypeEnum.START)
      {
        if (administered == null || planned == null)
        {
          return false;
        }
        else
        {
          final boolean numeratorsUnequal = Math.abs(administered.getNumerator() - planned.getNumerator()) > 0.000000000001;
          boolean secondaryNumeratorsUnequal = false;

          if (administered.getSecondaryNumerator() != null && planned.getSecondaryNumerator() != null)
          {
            secondaryNumeratorsUnequal = !administered.getSecondaryNumerator().equals(planned.getSecondaryNumerator());
          }

          return numeratorsUnequal || secondaryNumeratorsUnequal;
        }
      }
      else if (administrationType == AdministrationTypeEnum.ADJUST_INFUSION)
      {
        return administered != null && !administered.equals(planned);
      }
    }

    return false;
  }

  private DvText buildGroupUUid(final AdministrationDto administrationDto)
  {
    return administrationDto.getGroupUUId() != null ? DataValueUtils.getText(administrationDto.getGroupUUId()) : null;
  }

  private DvCodedText buildSelfAdministrationType(final AdministrationDto administrationDto)
  {
    return administrationDto.getAdministrationResult() == AdministrationResultEnum.SELF_ADMINISTERED
           ? administrationDto.getSelfAdministrationType().getDvCodedText()
           : null;
  }

  private List<Participation> buildParticipation(final AdministrationDto administrationDto)
  {
    if (administrationDto.getWitness() != null)
    {
      final Participation p = new Participation();
      p.setName(administrationDto.getWitness().getName());
      p.setId(administrationDto.getWitness().getId());
      p.setFunction(ParticipationTypeEnum.WITNESS.getCode());
      return Collections.singletonList(p);
    }

    return Collections.emptyList();
  }

  private Procedure buildProcedureForDoseAdministration(final AdministrationDto administrationDto)
  {
    if (administrationUtils.getInfusionBagDto(administrationDto) != null)
    {
      final Procedure procedure = new Procedure();
      procedure.setProcedureName(InfusionSetChangeEnum.INFUSION_SYSTEM_CHANGE.getDvCodedText());
      procedure.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
      procedure.setComment(DataValueUtils.getText(administrationDto.getComment()));
      procedure.setIsmTransition(ClinicalInterventionEnum.COMPLETED.buildIsmTransition());
      procedure.setSupplyAmount(buildSupplyAmountForBagChange(administrationDto));
      return procedure;
    }

    return null;
  }

  private Procedure buildProcedureForInfusionSetChange(final InfusionSetChangeDto administrationDto)
  {
    final Procedure procedure = new Procedure();
    procedure.setProcedureName(administrationDto.getInfusionSetChangeEnum().getDvCodedText());
    procedure.setTime(DataValueUtils.getDateTime(administrationDto.getAdministrationTime()));
    procedure.setComment(buildComment(administrationDto));
    procedure.setIsmTransition(ClinicalInterventionEnum.COMPLETED.buildIsmTransition());
    procedure.setSupplyAmount(buildSupplyAmountForBagChange(administrationDto));
    return procedure;
  }

  private Dosage buildDosage(final AdministrationDto administrationDto, final boolean buildPlannedDose)
  {
    final AdministrationTypeEnum administrationType = administrationDto.getAdministrationType();
    final TherapyDoseDto dose =
        buildPlannedDose
        ? administrationUtils.getPlannedTherapyDose(administrationDto)
        : administrationUtils.getTherapyDose(administrationDto);

    if (dose == null)
    {
      return null;
    }

    if (administrationDto instanceof OxygenAdministration)
    {
      //noinspection ConstantConditions
      return therapyToEhrUtils.buildDosage(null, null, null, null, null, null, dose.getNumerator(), "l/min");
    }
    if (AdministrationTypeEnum.DOSE_ADMINISTRATION.contains(administrationType))
    {
      if (dose.getNumerator() != null)
      {
        if (dose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.QUANTITY)
        {
          return therapyToEhrUtils.buildDosage(
              dose.getNumerator(),
              dose.getNumeratorUnit(),
              dose.getDenominator(),
              dose.getDenominatorUnit());
        }
        else if (dose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.RATE)
        {
          return therapyToEhrUtils.buildDosage(
              null,
              null,
              null,
              null,
              dose.getDenominator(),
              dose.getDenominatorUnit(),
              dose.getNumerator(),
              dose.getNumeratorUnit());
        }
        else if (dose.getTherapyDoseTypeEnum() == TherapyDoseTypeEnum.VOLUME_SUM)
        {
          return therapyToEhrUtils.buildDosage(dose.getNumerator(), dose.getNumeratorUnit(), null, null);
        }
        else
        {
          throw new IllegalStateException(String.format(
              "Cannot build dosage (plannedDosage = %s) for administration of type%s",
              buildPlannedDose,
              administrationType));
        }
      }
    }

    return null;
  }

  private DvText buildComment(final AdministrationDto administrationDto)
  {
    return StringUtils.isEmpty(administrationDto.getComment()) ? null : DataValueUtils.getText(administrationDto.getComment());
  }

  private DvCodedText buildRoute(final AdministrationDto administrationDto)
  {
    final MedicationRouteDto route = administrationDto.getRoute();
    return route != null ? DataValueUtils.getLocalCodedText(String.valueOf(route.getId()), route.getName()) : null;
  }

  private DvCodedText buildSubstituteMedication(final InpatientPrescription prescription, final AdministrationDto administrationDto)
  {
    final AdministrationTypeEnum administrationType = administrationDto.getAdministrationType();

    // for simple therapies user can select substitute medication
    if (administrationType == AdministrationTypeEnum.START || administrationType == AdministrationTypeEnum.BOLUS)
    {
      final MedicationDto substituteMed =
          administrationDto instanceof StartAdministrationDto
          ? ((StartAdministrationDto)administrationDto).getSubstituteMedication()
          : null;

      if (substituteMed != null)
      {
        if (MedicationsEhrUtils.isAdHocMixture(prescription.getMedicationOrder().getPreparationDetails()))
        {
          throw new IllegalArgumentException("Substitute medication can only be set for simple therapies");
        }
        return DataValueUtils.getLocalCodedText(String.valueOf(substituteMed.getId()), substituteMed.getName());
      }
    }

    return null;
  }

  private List<DvText> buildReason(final AdministrationDto administrationDto)
  {
    final AdministrationResultEnum resultEnum = administrationDto.getAdministrationResult();
    final CodedNameDto notGivenReason = administrationDto.getNotAdministeredReason();

    final List<DvText> reason = new ArrayList<>();
    if (resultEnum == AdministrationResultEnum.NOT_GIVEN)
    {
      reason.add(DataValueUtils.getLocalCodedText(notGivenReason.getCode(), notGivenReason.getName()));
    }

    return reason;
  }

  private void addContext(
      final MedicationAdministration composition,
      final String composerName,
      final String composerId,
      final String centralCaseId,
      final String careProviderId,
      final DateTime when)
  {
    new EhrContextVisitor(composition)
        .withCareProvider(careProviderId)
        .withCentralCaseId(centralCaseId)
        .withComposer(composerId, composerName)
        .withStartTime(when)
        .visit();
  }

  private MedicationSupplyAmount buildSupplyAmountForBagChange(final AdministrationDto administrationDto)
  {
    final InfusionBagDto infusionBag = administrationUtils.getInfusionBagDto(administrationDto);
    if (infusionBag != null)
    {
      final Double infusionBagQuantity = infusionBag.getQuantity();
      final String infusionBagUnit = infusionBag.getUnit();

      final MedicationSupplyAmount supplyAmount = new MedicationSupplyAmount();
      supplyAmount.setAmount(DataValueUtils.getQuantity(infusionBagQuantity, "1"));
      supplyAmount.setUnits(DataValueUtils.getLocalCodedText(infusionBagUnit, infusionBagUnit));

      return supplyAmount;
    }

    return null;
  }
}
