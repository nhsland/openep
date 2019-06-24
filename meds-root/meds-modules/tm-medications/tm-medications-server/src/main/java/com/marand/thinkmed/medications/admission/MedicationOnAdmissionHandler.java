package com.marand.thinkmed.medications.admission;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.EnumUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dao.openehr.ReconciliationOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.ValidationIssueEnum;
import com.marand.thinkmed.medications.dto.admission.AdmissionSourceMedicationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionStatus;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnAdmission;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.PrescriptionIdentifierType;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.reconciliation.MedicationReconciliationUpdater;
import com.marand.thinkmed.medications.reconciliation.ReconciliationReviewHandler;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.validator.TherapyDtoValidator;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MedicationOnAdmissionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ReconciliationOpenEhrDao reconciliationOpenEhrDao;
  private TherapyDisplayProvider therapyDisplayProvider;
  private ReconciliationReviewHandler reconciliationReviewHandler;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private TherapyConverter therapyConverter;
  private MedicationsBo medicationsBo;
  private MedicationReconciliationUpdater reconciliationUpdater;

  private RequestDateTimeHolder requestDateTimeHolder;

  private TherapyDtoValidator therapyDtoValidator;

  @Autowired
  public void setTherapyDtoValidator(final TherapyDtoValidator therapyDtoValidator)
  {
    this.therapyDtoValidator = therapyDtoValidator;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setReconciliationOpenEhrDao(final ReconciliationOpenEhrDao reconciliationOpenEhrDao)
  {
    this.reconciliationOpenEhrDao = reconciliationOpenEhrDao;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setReconciliationReviewHandler(final ReconciliationReviewHandler reconciliationReviewHandler)
  {
    this.reconciliationReviewHandler = reconciliationReviewHandler;
  }

  @Autowired
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setReconciliationUpdater(final MedicationReconciliationUpdater reconciliationUpdater)
  {
    this.reconciliationUpdater = reconciliationUpdater;
  }

  public List<String> saveMedicationsOnAdmission(
      final String patientId,
      final List<MedicationOnAdmissionDto> admissionMedications,
      final String centralCaseId,
      final String careProviderId,
      final Locale locale)
  {
    final String reconciliationUid = reconciliationUpdater.updateOrStartNew(
        patientId,
        centralCaseId,
        careProviderId,
        requestDateTimeHolder.getRequestTimestamp(),
        null);

    final List<String> currentAdmissionIds = getAdmissionCompositionIds(patientId, reconciliationUid);

    // save admissions
    final List<String> savedCompositionIds = admissionMedications
        .stream()
        .map(admission -> saveAdmission(admission, centralCaseId, careProviderId, reconciliationUid, patientId, locale))
        .collect(Collectors.toList());

    // delete admissions
    final List<String> savedUids = savedCompositionIds
        .stream()
        .map(TherapyIdUtils::getCompositionUidWithoutVersion)
        .collect(Collectors.toList());

    currentAdmissionIds.removeAll(savedUids);
    currentAdmissionIds.forEach(c -> medicationsOpenEhrDao.deleteComposition(patientId, c));

    // update admission review task
    reconciliationReviewHandler.updateReviewTask(patientId, TaskTypeEnum.ADMISSION_REVIEW_TASK);

    return savedCompositionIds;
  }

  public List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      final String patientId,
      final boolean validateTherapy,
      final Locale locale)
  {
    final String latestReconciliation = reconciliationOpenEhrDao.findLatestMedicationReconciliationUid(patientId).orElse(null);

    if (latestReconciliation == null)
    {
      return Collections.emptyList();
    }

    return reconciliationOpenEhrDao.findMedicationsOnAdmission(patientId, latestReconciliation)
        .stream()
        .map(c -> mapToAdmissionDto(c, validateTherapy, locale))
        .sorted((o1, o2) -> medicationsBo.compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), Collator.getInstance()))
        .collect(Collectors.toList());
  }

  public void updateMedicationOnAdmissionAction(
      final String patientId,
      final String compositionId,
      final MedicationOrderActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    final MedicationOnAdmission admissionComposition = reconciliationOpenEhrDao.loadMedicationOnAdmission(
        patientId,
        compositionId);

    final MedicationActionEnum medicationActionEnum = AdmissionStatusMapper.Companion.mapToMedicationActionEnum(actionEnum);
    admissionComposition.getActions().add(buildAction(admissionComposition, medicationActionEnum, changeReasonDto, when));

    updateMedicationOnAdmissionContext(admissionComposition);

    medicationsOpenEhrDao.saveComposition(
        patientId,
        admissionComposition,
        admissionComposition.getUid());
  }

  public List<MedicationOnAdmissionReconciliationDto> getAdmissionsForReconciliation(
      final String patientId,
      final String reconciliationUid,
      final Locale locale)
  {
    return reconciliationOpenEhrDao.findMedicationsOnAdmission(patientId, reconciliationUid)
        .stream()
        .filter(m -> m.getMedicationOrder() != null)
        .map(c -> mapToReconciliationAdmissionDto(locale, c))
        .sorted((o1, o2) -> medicationsBo.compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), Collator.getInstance()))
        .collect(Collectors.toList());
  }

  public List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(final String patientId, final Locale locale)
  {
    final List<MedicationOnDischargeDto> previousDischarges = medicationOnDischargeHandler.getPreviousMedicationsOnDischarge(
        patientId,
        locale);

    final MedicationOnAdmissionGroupDto previousDischargeGroup = new MedicationOnAdmissionGroupDto(
        TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS, Dictionary.getEntry(
        EnumUtils.getIdentifier(TherapySourceGroupEnum.LAST_DISCHARGE_MEDICATIONS),
        locale));

    previousDischargeGroup.setLastUpdateTime(previousDischarges.stream()
            .map(a -> a.getTherapy().getCreatedTimestamp())
            .max(Comparator.comparing(t -> t))
            .orElse(null));

    for (final MedicationOnDischargeDto dischargeDto : previousDischarges)
    {
      final AdmissionSourceMedicationDto sourceMedicationDto = new AdmissionSourceMedicationDto();

      final TherapyDto therapy = dischargeDto.getTherapy();
      final String compositionId = therapy.getCompositionUid();

      // some therapy data should not be copied
      therapy.setAdmissionId(null);
      therapy.setCompositionUid(null);
      therapy.setDispenseDetails(null);
      therapy.setInformationSources(Collections.emptyList());
      therapy.setPrescriptionLocalDetails(null);
      therapy.getCriticalWarnings().clear();

      // set source to discharge id
      sourceMedicationDto.setSourceId(TherapyIdUtils.getCompositionUidWithoutVersion(compositionId));
      sourceMedicationDto.setTherapy(therapy);

      previousDischargeGroup.getGroupElements().add(sourceMedicationDto);
    }

    final List<MedicationOnAdmissionGroupDto> therapyOnAdmissionGroups = new ArrayList<>();
    therapyOnAdmissionGroups.add(previousDischargeGroup);

    return therapyOnAdmissionGroups;
  }

  private String saveAdmission(
      final MedicationOnAdmissionDto admissionDto,
      final String centralCaseId,
      final String careProviderId,
      final String reconciliationUid,
      final String patientId,
      final Locale locale)
  {
    final TherapyDto therapy = admissionDto.getTherapy();
    final String compositionUid = therapy.getCompositionUid();

    if (compositionUid == null)
    {
      final MedicationOnAdmission composition = buildAdmissionComposition(
          admissionDto,
          centralCaseId,
          careProviderId,
          reconciliationUid,
          locale);

      return medicationsOpenEhrDao.saveComposition(patientId, composition, null);
    }
    else
    {
      return modifyAdmissionComposition(patientId, locale, admissionDto);
    }
  }

  private String modifyAdmissionComposition(
      final String patientId,
      final Locale locale,
      final MedicationOnAdmissionDto admissionDto)
  {
    final TherapyDto therapy = admissionDto.getTherapy();
    final String oldCompositionUid = therapy.getCompositionUid();
    final MedicationOnAdmission composition = reconciliationOpenEhrDao.loadMedicationOnAdmission(patientId, oldCompositionUid);

    composition.setMedicationOrder(buildMedicationOrderFromAdmission(locale, admissionDto));
    updateMedicationOnAdmissionContext(composition);
    return medicationsOpenEhrDao.saveComposition(patientId, composition, oldCompositionUid);
  }

  private MedicationOnAdmission buildAdmissionComposition(
      final MedicationOnAdmissionDto admissionDto,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String reconciliationUid,
      final Locale locale)
  {
    final MedicationOnAdmission composition = new MedicationOnAdmission();
    composition.setMedicationOrder(buildMedicationOrderFromAdmission(locale, admissionDto));
    addContext(composition, centralCaseId, careProviderId, reconciliationUid);
    return composition;
  }

  private DvText buildSourceIdentifier(final MedicationOnAdmissionDto admission)
  {
    return admission.getSourceId() != null && admission.getSourceGroupEnum() != null
           ? DataValueUtils.getText(TherapySourceGroupEnum.createEhrString(admission.getSourceGroupEnum(), admission.getSourceId()))
           : null;
  }

  private Pair<TherapySourceGroupEnum, String> buildSourceIdentifierValues(final MedicationOrder order)
  {
    final DvText sourcePrescriptionIdentifier = order.getAdditionalDetails().getSourcePrescriptionIdentifier();
    return sourcePrescriptionIdentifier != null
           ? TherapySourceGroupEnum.getSourceGroupEnumAndSourceIdFromEhrString(sourcePrescriptionIdentifier.getValue())
           : null;
  }

  private MedicationOrder buildMedicationOrderFromAdmission(final Locale locale, final MedicationOnAdmissionDto admissionDto)
  {
    final TherapyDto therapy = admissionDto.getTherapy();
    if (therapy.getTherapyDescription() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, locale);
    }

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    medicationOrder.getAdditionalDetails().setSourcePrescriptionIdentifier(buildSourceIdentifier(admissionDto));
    return medicationOrder;
  }

  private void addContext(
      final MedicationOnAdmission composition,
      final String centralCaseId,
      final String careProviderId,
      final String reconciliationUid)
  {
    new EhrContextVisitor(composition)
        .withCareProvider(careProviderId)
        .withCentralCaseId(centralCaseId)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withPrescriptionIdentifier(PrescriptionIdentifierType.RECONCILIATION, reconciliationUid)
        .withStartTime(requestDateTimeHolder.getRequestTimestamp())
        .visit();
  }

  MedicationOnAdmissionDto mapToAdmissionDto(
      final MedicationOnAdmission composition,
      final boolean validateTherapy,
      final Locale locale)
  {
    final MedicationOnAdmissionDto admissionDto = new MedicationOnAdmissionDto();

    // therapy
    admissionDto.setTherapy(buildTherapyDto(composition, locale));

    // change reasons
    fillStatusAndChangeReasonFromComposition(admissionDto, composition);

    // source identifier
    final Pair<TherapySourceGroupEnum, String> sourceIdentifier = buildSourceIdentifierValues(composition.getMedicationOrder());
    if (sourceIdentifier != null)
    {
      admissionDto.setSourceGroupEnum(sourceIdentifier.getFirst());
      admissionDto.setSourceId(sourceIdentifier.getSecond());
    }

    if (validateTherapy)
    {
      final boolean valid = therapyDtoValidator.isValid(admissionDto.getTherapy());

      if (!valid)
      {
        admissionDto.getValidationIssues().add(ValidationIssueEnum.INCOMPLETE);
      }
    }

    return admissionDto;
  }

  private TherapyDto buildTherapyDto(final MedicationOnAdmission composition, final Locale locale)
  {
    final TherapyDto therapyDto = therapyConverter.convertToTherapyDto(
        composition.getMedicationOrder(),
        composition.getUid(),
        DataValueUtils.getDateTime(composition.getContext().getStartTime()));

    therapyDisplayProvider.fillDisplayValues(therapyDto, true, locale);
    return therapyDto;
  }

  private MedicationOnAdmissionReconciliationDto mapToReconciliationAdmissionDto(
      final Locale locale,
      final MedicationOnAdmission composition)
  {
    final TherapyDto therapyDto = buildTherapyDto(composition, locale);
    final MedicationManagement latestAction = MedicationsEhrUtils.getLatestAction(composition.getActions());
    final TherapyChangeReasonDto changeReasonDto = Opt.of(latestAction)
        .map(MedicationsEhrUtils::getTherapyChangeReasonDtoFromAction)
        .orElse(null);

    return new MedicationOnAdmissionReconciliationDto(
        therapyDto,
        changeReasonDto,
        AdmissionStatusMapper.Companion.mapToTherapyStatus(latestAction));
  }

  private void fillStatusAndChangeReasonFromComposition(
      final MedicationOnAdmissionDto onAdmissionDto,
      final MedicationOnAdmission composition)
  {
    final MedicationManagement latestAction = MedicationsEhrUtils.getLatestAction(composition.getActions());

    final MedicationOnAdmissionStatus status =
        latestAction == null
        ? MedicationOnAdmissionStatus.PENDING
        : AdmissionStatusMapper.Companion.mapToAdmissionStatus(latestAction);

    if (status == null)
    {
      onAdmissionDto.setStatus(MedicationOnAdmissionStatus.PENDING);
    }
    else
    {
      onAdmissionDto.setStatus(status);
    }
    if (latestAction != null)
    {
      onAdmissionDto.setChangeReasonDto(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestAction));
    }
  }

  private List<String> getAdmissionCompositionIds(final String patientId, final String reconciliationUid)
  {
    return reconciliationOpenEhrDao.findMedicationsOnAdmission(patientId, reconciliationUid)
        .stream()
        .filter(m -> m.getMedicationOrder() != null)
        .map(c -> TherapyIdUtils.getCompositionUidWithoutVersion(c.getUid()))
        .collect(Collectors.toList());
  }

  private void updateMedicationOnAdmissionContext(final MedicationOnAdmission admissionComposition)
  {
    new EhrContextVisitor(admissionComposition)
        .withComposer(admissionComposition.getComposer())
        .withStartTime(DataValueUtils.getDateTime(admissionComposition.getContext().getStartTime()))
        .visit();
  }

  private MedicationManagement buildAction(
      final MedicationOnAdmission admissionComposition,
      final MedicationActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final DateTime when)
  {
    final MedicationManagement completeAction = MedicationsEhrUtils.buildMedicationAction(
        admissionComposition,
        actionEnum,
        MedicationOnAdmission.getMedicationOrderPath(),
        when);

    completeAction.getReason().addAll(MedicationsEhrUtils.getTherapyChangeReasons(changeReasonDto));
    return completeAction;
  }
}
