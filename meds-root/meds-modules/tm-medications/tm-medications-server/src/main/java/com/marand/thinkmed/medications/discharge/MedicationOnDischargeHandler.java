package com.marand.thinkmed.medications.discharge;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapySourceGroupEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.admission.AdmissionStatusMapper;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dao.openehr.ReconciliationOpenEhrDao;
import com.marand.thinkmed.medications.dto.AdmissionChangeReasonDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.ValidationIssueEnum;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.discharge.DischargeSourceMedicationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeStatus;
import com.marand.thinkmed.medications.dto.reconsiliation.SourceMedicationDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnDischarge;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.PrescriptionIdentifierType;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.reconciliation.MedicationReconciliationUpdater;
import com.marand.thinkmed.medications.reconciliation.ReconciliationReviewHandler;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.validator.TherapyDtoValidator;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MedicationOnDischargeHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ReconciliationOpenEhrDao reconciliationOpenEhrDao;
  private TherapyDisplayProvider therapyDisplayProvider;
  private TherapyConverter therapyConverter;
  private MedicationReconciliationUpdater reconciliationUpdater;
  private MedicationsBo medicationsBo;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private MedicationsValueHolder medicationsValueHolder;
  private OverviewContentProvider overviewContentProvider;
  private TherapyEhrHandler therapyEhrHandler;
  private ReconciliationReviewHandler reconciliationReviewHandler;

  private RequestDateTimeHolder requestDateTimeHolder;

  private TherapyDtoValidator therapyDtoValidator;

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
  public void setReconciliationUpdater(final MedicationReconciliationUpdater reconciliationUpdater)
  {
    this.reconciliationUpdater = reconciliationUpdater;
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
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setReconciliationReviewHandler(final ReconciliationReviewHandler reconciliationReviewHandler)
  {
    this.reconciliationReviewHandler = reconciliationReviewHandler;
  }

  @Autowired
  public void setTherapyDtoValidator(final TherapyDtoValidator therapyDtoValidator)
  {
    this.therapyDtoValidator = therapyDtoValidator;
  }

  public List<String> saveMedicationsOnDischarge(
      final String patientId,
      final List<MedicationOnDischargeDto> dischargeMedications,
      final String centralCaseId,
      final String careProviderId,
      final Locale locale)
  {
    final String reconciliationUid = reconciliationUpdater.updateOrStartNew(
        patientId,
        centralCaseId,
        careProviderId,
        null,
        requestDateTimeHolder.getRequestTimestamp());

    final List<String> currentDischargeIds = getMedicationsOnDischargeIds(patientId, reconciliationUid);

    // cancel admission therapies for all NOT_PRESCRIBED discharge medications
    updateCanceledAdmissions(patientId, dischargeMedications);

    // save prescribed discharge medications
    final List<String> savedCompositionIds = dischargeMedications
        .stream()
        .filter(d -> d.getStatus() != MedicationOnDischargeStatus.NOT_PRESCRIBED)
        .map(discharge -> saveDischarge(discharge, patientId, centralCaseId, careProviderId, reconciliationUid, locale))
        .collect(Collectors.toList());

    // delete discharges
    final List<String> savedUids = savedCompositionIds
        .stream()
        .map(TherapyIdUtils::getCompositionUidWithoutVersion)
        .collect(Collectors.toList());

    currentDischargeIds.removeAll(savedUids);
    currentDischargeIds.forEach(c -> medicationsOpenEhrDao.deleteComposition(patientId, c));

    // update discharge review task
    reconciliationReviewHandler.updateReviewTask(patientId, TaskTypeEnum.DISCHARGE_REVIEW_TASK);

    return savedCompositionIds;
  }

  private void updateCanceledAdmissions(final String patientId, final List<MedicationOnDischargeDto> dischargeMedications)
  {
    final List<MedicationOnDischargeDto> admissionsToCancel = dischargeMedications
        .stream()
        .filter(d -> d.getSourceGroupEnum() == TherapySourceGroupEnum.MEDICATION_ON_ADMISSION)
        .filter(d -> d.getStatus() == MedicationOnDischargeStatus.NOT_PRESCRIBED)
        .collect(Collectors.toList());

    admissionsToCancel.forEach(d -> medicationOnAdmissionHandler.updateMedicationOnAdmissionAction(
        patientId,
        d.getSourceId(),
        MedicationOrderActionEnum.ABORT,
        d.getChangeReasonDto(),
        requestDateTimeHolder.getRequestTimestamp()));
  }

  public List<MedicationOnDischargeDto> getMedicationsOnDischarge(final @NonNull String patientId, final @NonNull Locale locale)
  {
    final String reconciliationUid = reconciliationOpenEhrDao.findLatestMedicationReconciliationUid(patientId).orElse(null);

    if (reconciliationUid == null)
    {
      return Collections.emptyList();
    }

    final List<String> currentAdmissionUids = reconciliationOpenEhrDao.findMedicationsOnAdmissionUids(
        patientId,
        reconciliationUid)
        .stream()
        .map(TherapyIdUtils::getCompositionUidWithoutVersion)
        .collect(Collectors.toList());

    return reconciliationOpenEhrDao.findMedicationsOnDischarge(patientId, reconciliationUid)
        .stream()
        .map(c -> mapToDischargeDto(locale, c, currentAdmissionUids))
        .sorted((o1, o2) -> medicationsBo.compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), Collator.getInstance()))
        .collect(Collectors.toList());
  }

  public int countMedicationsOnDischarge(final @NonNull String patientId)
  {
    return reconciliationOpenEhrDao
        .findLatestMedicationReconciliationUid(patientId)
        .map(uid -> reconciliationOpenEhrDao.countMedicationsOnDischarge(patientId, uid))
        .orElse(0);
  }

  public List<MedicationOnDischargeDto> getPreviousMedicationsOnDischarge(final String patientId, final Locale locale)
  {
    final List<String> latestReconciliationUids = reconciliationOpenEhrDao.findLatestMedicationReconciliationUids(patientId, 2);

    if (latestReconciliationUids.size() < 2)
    {
      return Collections.emptyList();
    }

    final List<MedicationOnDischarge> compositions = reconciliationOpenEhrDao.findMedicationsOnDischarge(
        patientId,
        latestReconciliationUids.get(1));

    return compositions
        .stream()
        .map(c -> mapToDischargeDto(locale, c, null))
        .sorted((o1, o2) -> medicationsBo.compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), Collator.getInstance()))
        .collect(Collectors.toList());
  }

  public List<MedicationOnDischargeReconciliationDto> getDischargesForReconciliation(
      final String patientId,
      final String reconciliationUid,
      final Locale locale)
  {
    return reconciliationOpenEhrDao.findMedicationsOnDischarge(patientId, reconciliationUid)
        .stream()
        .map(c -> mapToReconciliationDischargeDto(locale, c))
        .collect(Collectors.toList());
  }

  /**
   * Admission therapies that have been stopped on admission do not show in inpatient group even though aborted inpatient
   * therapy linked to admission exist.
   *
   * Logic for inpatient/admission group in full:
   *
   * admission -> ADMISSION
   * admission -> stopped on admission -> STOPPED
   * admission -> suspend until discharge -> ADMISSION
   * admission -> prescribe and suspend -> INPATIENT
   * admission -> inpatient -> INPATIENT
   * admission -> inpatient -> stopped on inpatient -> STOPPED
   * inpatient -> stopped on inpatient -> INPATIENT (if in last 24h)
   * inpatient -> INPATIENT
   */
  public List<MedicationOnDischargeGroupDto> getMedicationOnDischargeGroups(
      final @NonNull String patientId,
      final Double referenceWeight,
      final Double patientHeight,
      final @NonNull Locale locale)
  {
    final List<MedicationOnAdmissionDto> currentAdmissions = medicationOnAdmissionHandler.getMedicationsOnAdmission(patientId, false, locale);
    final Map<String, AdmissionChangeReasonDto> lastAdmissionChangeReasons = reconciliationOpenEhrDao.getLastAdmissionChangeReasons(
        patientId,
        false);

    final MedicationOnDischargeGroupDto inpatientGroup = buildMedicationOnDischargeInpatientGroup(
        patientId,
        referenceWeight,
        patientHeight,
        currentAdmissions,
        lastAdmissionChangeReasons,
        locale);

    final List<MedicationOnDischargeGroupDto> admissionGroups = buildMedicationOnDischargeAdmissionGroups(
        currentAdmissions,
        lastAdmissionChangeReasons,
        inpatientGroup,
        locale);

    final List<MedicationOnDischargeGroupDto> groups = new ArrayList<>();
    groups.add(inpatientGroup);
    groups.addAll(admissionGroups);
    return groups;
  }

  private List<MedicationOnDischargeGroupDto> buildMedicationOnDischargeAdmissionGroups(
      final List<MedicationOnAdmissionDto> currentAdmissions,
      final Map<String, AdmissionChangeReasonDto> lastChangeReasonForAdmissions,
      final MedicationOnDischargeGroupDto inpatientGroup,
      final Locale locale)
  {
    final Set<String> admissionsInInpatientGroup = inpatientGroup.getGroupElements().stream()
        .map(e -> e.getTherapy().getAdmissionId())
        .collect(Collectors.toSet());

    // create admission therapies groups
    final MedicationOnDischargeGroupDto admissionGroup = new MedicationOnDischargeGroupDto(
        TherapySourceGroupEnum.MEDICATION_ON_ADMISSION,
        Dictionary.getEntry(TherapySourceGroupEnum.MEDICATION_ON_ADMISSION.getDictionaryKey(), locale));

    final MedicationOnDischargeGroupDto stoppedAdmissionGroup = new MedicationOnDischargeGroupDto(
        TherapySourceGroupEnum.STOPPED_ADMISSION_MEDICATION,
        Dictionary.getEntry(TherapySourceGroupEnum.STOPPED_ADMISSION_MEDICATION.getDictionaryKey(), locale));

    // fill admission therapies group
    for (final MedicationOnAdmissionDto admissionDto : currentAdmissions)
    {
      final TherapyDto therapy = admissionDto.getTherapy();
      final String compositionUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(therapy.getCompositionUid());

      if (!admissionsInInpatientGroup.contains(compositionUidWithoutVersion))
      {
        final DischargeSourceMedicationDto sourceMedicationDto = new DischargeSourceMedicationDto();
        final String compositionId = therapy.getCompositionUid();

        therapy.setCompositionUid(null);
        therapy.setAdmissionId(compositionUidWithoutVersion);

        // set source to admission id
        sourceMedicationDto.setSourceId(TherapyIdUtils.getCompositionUidWithoutVersion(compositionId));
        sourceMedicationDto.setTherapy(therapy);
        sourceMedicationDto.getValidationIssues().addAll(getTherapyValidationIssues(therapy));
        sourceMedicationDto.setStatus(AdmissionStatusMapper.Companion.mapToTherapyStatus(admissionDto.getStatus()));
        sourceMedicationDto.setChangeReason(admissionDto.getChangeReasonDto());

        final AdmissionChangeReasonDto admissionChangeReason = lastChangeReasonForAdmissions.get(compositionUidWithoutVersion);

        if (admissionChangeReason != null)
        {
          sourceMedicationDto.setChangeReason(admissionChangeReason.getChangeReason());
          sourceMedicationDto.setStatus(AdmissionStatusMapper.Companion.mapToTherapyStatus(admissionChangeReason.getActionEnum()));
        }

        if (TherapyStatusEnum.STOPPED.contains(sourceMedicationDto.getStatus()))
        {
          stoppedAdmissionGroup.addGroupElement(sourceMedicationDto);
        }
        else
        {
          admissionGroup.addGroupElement(sourceMedicationDto);
        }
      }
    }

    return Arrays.asList(admissionGroup, stoppedAdmissionGroup);
  }

  private MedicationOnDischargeGroupDto buildMedicationOnDischargeInpatientGroup(
      final String patientId,
      final Double referenceWeight,
      final Double patientHeight,
      final List<MedicationOnAdmissionDto> currentAdmissions,
      final Map<String, AdmissionChangeReasonDto> lastChangeReasonForAdmissions,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();

    final List<InpatientPrescription> inpatientPrescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        Intervals.infiniteFrom(when.minusHours(24)));

    final List<TherapyDto> therapies = medicationsBo.convertInpatientPrescriptionsToTherapies(
        inpatientPrescriptions,
        referenceWeight,
        patientHeight,
        locale);

    // admissions ids on current admission list
    final List<String> currentAdmissionIds = currentAdmissions
        .stream()
        .map(c -> TherapyIdUtils.getCompositionUidWithoutVersion(c.getTherapy().getCompositionUid()))
        .collect(Collectors.toList());

    final Map<String, InpatientPrescription> inpatientPrescriptionMap = inpatientPrescriptions
        .stream()
        .collect(Collectors.toMap(i -> TherapyIdUtils.createTherapyId(i.getUid()), i -> i));

    // create inpatient therapies group
    final MedicationOnDischargeGroupDto inpatientGroup = new MedicationOnDischargeGroupDto(
        TherapySourceGroupEnum.INPATIENT_THERAPIES,
        Dictionary.getEntry(TherapySourceGroupEnum.INPATIENT_THERAPIES.getDictionaryKey(), locale));

    // fill inpatient therapies group
    for (final TherapyDto therapyDto : therapies)
    {
      final InpatientPrescription prescription = inpatientPrescriptionMap.get(therapyDto.getTherapyId());
      final TherapyStatusEnum status = overviewContentProvider.getTherapyStatus(prescription.getActions());

      final DischargeSourceMedicationDto sourceMedicationDto = new DischargeSourceMedicationDto();
      therapyDto.setCompositionUid(null);
      sourceMedicationDto.setTherapy(therapyDto);
      sourceMedicationDto.setStatus(status);
      sourceMedicationDto.getValidationIssues().addAll(getTherapyValidationIssues(therapyDto));

      final String admissionId = TherapyIdUtils.getCompositionUidWithoutVersion(therapyDto.getAdmissionId());

      // set source to admission only if they are linked to current admission list
      final boolean isLinkedToCurrentAdmission = currentAdmissionIds.contains(admissionId);
      if (isLinkedToCurrentAdmission)
      {
        // add to inpatient list only if not aborted or cancelled
        if (!TherapyStatusEnum.STOPPED.contains(status))
        {
          // set source to admission id
          sourceMedicationDto.setSourceId(admissionId);

          final AdmissionChangeReasonDto admissionChangeReason = lastChangeReasonForAdmissions.get(admissionId);
          if (admissionChangeReason != null)
          {
            sourceMedicationDto.setChangeReason(admissionChangeReason.getChangeReason());
            sourceMedicationDto.setStatus(AdmissionStatusMapper.Companion.mapToTherapyStatus(admissionChangeReason.getActionEnum()));
          }

          inpatientGroup.addGroupElement(sourceMedicationDto);
        }
      }
      else
      {
        // set source to original composition id
        sourceMedicationDto.setSourceId(TherapyIdUtils.extractCompositionUidWithoutVersion(therapyEhrHandler.getOriginalTherapyId(prescription)));
        // only show inpatient therapies as admission therapies if they are linked to current admission list
        therapyDto.setAdmissionId(null);
        inpatientGroup.addGroupElement(sourceMedicationDto);
      }
    }

    // sort by admission link
    inpatientGroup.getGroupElements().sort(admissionLinkComparator());

    return inpatientGroup;
  }

  private String saveDischarge(
      final MedicationOnDischargeDto dischargeDto,
      final String patientId,
      final String centralCaseId,
      final String careProviderId,
      final String reconciliationUid,
      final Locale locale)
  {
    final TherapyDto therapy = dischargeDto.getTherapy();
    final String compositionUid = therapy.getCompositionUid();

    if (compositionUid == null)
    {
      final MedicationOnDischarge composition = buildDischargeComposition(
          dischargeDto,
          centralCaseId,
          careProviderId,
          reconciliationUid,
          locale);

      composition.getLinks().addAll(createDischargeLinks(dischargeDto, patientId));

      return medicationsOpenEhrDao.saveComposition(patientId, composition, null);
    }
    else
    {
      return modifyDischargeComposition(patientId, locale, dischargeDto);
    }
  }

  private List<Link> createDischargeLinks(final MedicationOnDischargeDto dischargeDto, final String patientId)
  {
    final TherapySourceGroupEnum sourceGroupEnum = dischargeDto.getSourceGroupEnum();
    final String sourceId = dischargeDto.getSourceId();

    final List<Link> links = new ArrayList<>();
    if (sourceGroupEnum == TherapySourceGroupEnum.INPATIENT_THERAPIES)
    {
      // if source id IS admission id, set admission link
      if (sourceId.equals(dischargeDto.getTherapy().getAdmissionId()))
      {
        links.add(LinksEhrUtils.createLink(
            sourceId,
            EhrLinkType.MEDICATION_ON_ADMISSION.getName(),
            EhrLinkType.MEDICATION_ON_ADMISSION));
      }
      // if source id IS NOT admission id, load prescription and copy it's links
      else
      {
        final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, sourceId);
        if (inpatientPrescription != null)
        {
          final Link inpatientSourceLink = LinksEhrUtils.createLink(
              inpatientPrescription.getUid(),
              EhrLinkType.SOURCE.getName(),
              EhrLinkType.SOURCE);
          links.add(inpatientSourceLink);

          final List<Link> admissionLinks = LinksEhrUtils.getLinksOfType(
              inpatientPrescription.getLinks(),
              EhrLinkType.MEDICATION_ON_ADMISSION);
          links.addAll(admissionLinks);
        }
      }
    }
    else if (TherapySourceGroupEnum.ADMISSION_SOURCE.contains(sourceGroupEnum))
    {
      links.add(LinksEhrUtils.createLink(
          sourceId,
          EhrLinkType.MEDICATION_ON_ADMISSION.getName(),
          EhrLinkType.MEDICATION_ON_ADMISSION));
    }
    return links;
  }

  private MedicationOnDischarge buildDischargeComposition(
      final MedicationOnDischargeDto discharge,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final String reconciliationUid,
      final Locale locale)
  {
    final MedicationOnDischarge composition = new MedicationOnDischarge();

    // set order
    composition.setMedicationOrder(buildMedicationOrderFromTherapyDto(locale, discharge.getTherapy()));

    // set action
    if (discharge.getStatus() == MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED)
    {
      composition.getActions().add(buildActionWithReason(discharge, composition));
    }

    // set context
    new EhrContextVisitor(composition)
        .withCareProvider(careProviderId)
        .withCentralCaseId(centralCaseId)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withPrescriptionIdentifier(PrescriptionIdentifierType.RECONCILIATION, reconciliationUid)
        .withStartTime(requestDateTimeHolder.getRequestTimestamp())
        .visit();

    return composition;
  }

  private MedicationOrder buildMedicationOrderFromTherapyDto(final Locale locale, final TherapyDto therapyDto)
  {
    if (therapyDto.getTherapyDescription() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapyDto, true, locale);
    }
    return therapyConverter.convertToMedicationOrder(therapyDto);
  }

  private String modifyDischargeComposition(final String patientId, final Locale locale, final MedicationOnDischargeDto discharge)
  {
    final TherapyDto therapy = discharge.getTherapy();
    final String compositionUid = therapy.getCompositionUid();
    final MedicationOnDischarge composition = reconciliationOpenEhrDao.loadMedicationOnDischarge(patientId, compositionUid);

    // update medication order
    final MedicationOrder newMedicationOrder = buildMedicationOrderFromTherapyDto(locale, therapy);
    composition.setMedicationOrder(newMedicationOrder);

    // update action and change reason
    if (discharge.getStatus() == MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED)
    {
      composition.getActions().add(buildActionWithReason(discharge, composition));
    }

    // update composition context
    new EhrContextVisitor(composition)
        .withComposer(composition.getComposer())
        .withStartTime(DataValueUtils.getDateTime(composition.getContext().getStartTime()))
        .visit();

    return medicationsOpenEhrDao.saveComposition(patientId, composition, compositionUid);
  }

  private MedicationManagement buildActionWithReason(
      final MedicationOnDischargeDto discharge,
      final MedicationOnDischarge composition)
  {
    final MedicationManagement action = MedicationsEhrUtils.buildMedicationAction(
        composition,
        MedicationActionEnum.MODIFY_EXISTING,
        MedicationOnDischarge.getMedicationOrderPath(),
        requestDateTimeHolder.getRequestTimestamp());

    action.getReason().addAll(MedicationsEhrUtils.getTherapyChangeReasons(discharge.getChangeReasonDto()));
    return action;
  }

  private MedicationOnDischargeDto mapToDischargeDto(
      final Locale locale,
      final MedicationOnDischarge composition,
      @Nullable final List<String> currentAdmissionUids)
  {
    final MedicationOnDischargeDto dischargeDto = new MedicationOnDischargeDto();

    dischargeDto.setTherapy(buildTherapyDto(locale, composition));
    setSourceDataAndStatusFromComposition(composition, dischargeDto, currentAdmissionUids);
    dischargeDto.setChangeReasonDto(getChangeReasonFromDischargeComposition(composition));

    return dischargeDto;
  }

  private TherapyDto buildTherapyDto(final Locale locale, final MedicationOnDischarge composition)
  {
    final TherapyDto therapyDto = therapyConverter.convertToTherapyDto(
        composition.getMedicationOrder(),
        composition.getUid(),
        DataValueUtils.getDateTime(composition.getContext().getStartTime()));

    therapyDisplayProvider.fillDisplayValues(therapyDto, true, locale);

    therapyDto.setAdmissionId(LinksEhrUtils.getLinkedCompositionUid(
        composition.getLinks(),
        EhrLinkType.MEDICATION_ON_ADMISSION));

    return therapyDto;
  }

  private void setSourceDataAndStatusFromComposition(
      final MedicationOnDischarge composition,
      final MedicationOnDischargeDto dischargeDto,
      @Nullable final List<String> currentAdmissionUids)
  {
    final String admissionCompositionId = LinksEhrUtils.getLinkedCompositionUid(
        composition.getLinks(),
        EhrLinkType.MEDICATION_ON_ADMISSION);

    final String sourceCompositionId = LinksEhrUtils.getLinkedCompositionUid(
        composition.getLinks(),
        EhrLinkType.SOURCE);

    // set source to admission only if they are linked to current admission list
    final boolean linkedToCurrentAdmission = admissionCompositionId != null
        && currentAdmissionUids != null
        && currentAdmissionUids.contains(admissionCompositionId);

    dischargeDto.setSourceId(linkedToCurrentAdmission ? admissionCompositionId : sourceCompositionId);
    dischargeDto.setStatus(getMedicationOnDischargeStatusFromAction(MedicationsEhrUtils.getLatestAction(composition.getActions())));
  }

  private MedicationOnDischargeStatus getMedicationOnDischargeStatusFromAction(final MedicationManagement latestAction)
  {
    if (latestAction != null)
    {
      MedicationOnDischargeStatus dischargeStatus = MedicationOnDischargeStatus.PRESCRIBED;
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(latestAction);
      if (actionEnum == MedicationActionEnum.MODIFY_EXISTING)
      {
        dischargeStatus = MedicationOnDischargeStatus.EDITED_AND_PRESCRIBED;
      }
      else if (actionEnum == MedicationActionEnum.CANCEL)
      {
        dischargeStatus = MedicationOnDischargeStatus.NOT_PRESCRIBED;
      }
      return dischargeStatus;
    }
    else
    {
      return MedicationOnDischargeStatus.PRESCRIBED;
    }
  }

  private MedicationOnDischargeReconciliationDto mapToReconciliationDischargeDto(
      final Locale locale,
      final MedicationOnDischarge composition)
  {
    final TherapyDto therapyDto = buildTherapyDto(locale, composition);
    return new MedicationOnDischargeReconciliationDto(
        therapyDto,
        LinksEhrUtils.getLinkedCompositionUid(composition.getLinks(), EhrLinkType.MEDICATION_ON_ADMISSION),
        LinksEhrUtils.getLinkedCompositionUid(composition.getLinks(), EhrLinkType.SOURCE),
        getChangeReasonFromDischargeComposition(composition),
        getMedicationOnDischargeStatusFromAction(MedicationsEhrUtils.getLatestAction(composition.getActions())));
  }

  private TherapyChangeReasonDto getChangeReasonFromDischargeComposition(final MedicationOnDischarge composition)
  {
    final MedicationManagement latestAction = MedicationsEhrUtils.getLatestAction(composition.getActions());

    return latestAction != null ? MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestAction) : null;
  }

  private List<String> getMedicationsOnDischargeIds(final String patientId, final String reconciliationUid)
  {
    return reconciliationOpenEhrDao.findMedicationsOnDischarge(patientId, reconciliationUid)
        .stream()
        .map(c -> TherapyIdUtils.getCompositionUidWithoutVersion(c.getUid()))
        .collect(Collectors.toList());
  }

  private List<ValidationIssueEnum> getTherapyValidationIssues(final TherapyDto therapy)
  {
    final List<ValidationIssueEnum> validationIssues = new ArrayList<>();

    final boolean therapyContainsControlledDrugs = therapy.getMedications()
        .stream()
        .map(MedicationDto::getId)
        .filter(Objects::nonNull)
        .anyMatch(id -> medicationsValueHolder.getMedications().get(id).isControlledDrug());

    if (therapyContainsControlledDrugs)
    {
      validationIssues.add(ValidationIssueEnum.CONTROLLED_DRUG_DETAILS_MISSING);
    }

    // inpatient protocol
    final boolean isVariableDaysTherapy = therapy instanceof VariableSimpleTherapyDto &&
        ((VariableSimpleTherapyDto)therapy).getTimedDoseElements().get(0).getDate() != null;

    if (isVariableDaysTherapy)
    {
      validationIssues.add(ValidationIssueEnum.INPATIENT_PROTOCOL_NOT_SUPPORTED);
    }

    // validate therapy (if mandatory data - for example: medication dose, exists)
    final boolean isTherapyValid = therapyDtoValidator.isValid(therapy);
    if (!isTherapyValid)
    {
      validationIssues.add(ValidationIssueEnum.INCOMPLETE);
    }

    return validationIssues;
  }

  private Comparator<SourceMedicationDto> admissionLinkComparator()
  {
    final Collator collator = Collator.getInstance();
    return (o1, o2) ->
    {
      if (o1.getTherapy().isLinkedToAdmission() && !o2.getTherapy().isLinkedToAdmission())
      {
        return -1;
      }
      if (!o1.getTherapy().isLinkedToAdmission() && o2.getTherapy().isLinkedToAdmission())
      {
        return 1;
      }
      return medicationsBo.compareTherapiesForSort(o1.getTherapy(), o2.getTherapy(), collator);
    };
  }
}