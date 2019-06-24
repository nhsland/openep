package com.marand.thinkmed.medications.audittrail.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryDto;
import com.marand.thinkmed.medications.dto.TherapyActionHistoryType;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class TherapyAuditTrailProviderImpl implements TherapyAuditTrailProvider
{
  private TherapyChangeCalculator therapyChangeCalculator;
  private MedicationsOpenEhrDao  medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private TherapyEhrHandler therapyEhrHandler;
  private OverviewContentProvider overviewContentProvider;

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
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
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public TherapyAuditTrailDto getTherapyAuditTrail(
      final @NonNull String patientId,
      final @NonNull String compositionId,
      final @NonNull String ehrOrderName,
      final Double patientHeight,
      final @NonNull Locale locale,
      final @NonNull DateTime when)
  {
    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));

    final InpatientPrescription currentPrescriptionLastVersion = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionId);
    final InpatientPrescription currentPrescriptionFirstVersion = getFirstPrescriptionVersion(currentPrescriptionLastVersion, patientId);

    final TherapyDto currentTherapyLastVersion =
        medicationsBo.convertMedicationOrderToTherapyDto(
            currentPrescriptionLastVersion,
            currentPrescriptionLastVersion.getMedicationOrder(),
            referenceWeight,
            patientHeight,
            true,
            locale);

    final TherapyDto currentTherapyFirstVersion =
        medicationsBo.convertMedicationOrderToTherapyDto(
            currentPrescriptionFirstVersion,
            currentPrescriptionFirstVersion.getMedicationOrder(),
            referenceWeight,
            patientHeight,
            true,
            locale);

    final TherapyAuditTrailDto auditTrail = new TherapyAuditTrailDto();
    auditTrail.setCurrentTherapy(currentTherapyLastVersion);
    auditTrail.setCurrentTherapyStatus(overviewContentProvider.getTherapyStatus(currentPrescriptionLastVersion.getActions()));

    final List<TherapyActionHistoryDto> actionHistoryList = auditTrail.getActionHistoryList();
    actionHistoryList.addAll(extractSimpleActions(currentPrescriptionLastVersion));
    actionHistoryList.addAll(extractModifyExistingCompActions(currentPrescriptionLastVersion, locale));

    TherapyDto therapyFirstVersion = currentTherapyFirstVersion;

    InpatientPrescription prescription = currentPrescriptionLastVersion;
    List<Link> updateLinks = LinksEhrUtils.getLinksOfType(prescription, EhrLinkType.UPDATE);

    while (!updateLinks.isEmpty())
    {
      final InpatientPrescription previousMedicationOrderLastVersion = therapyEhrHandler.getPrescriptionFromLink(
          patientId,
          prescription,
          EhrLinkType.UPDATE,
          true);

      final InpatientPrescription previousPrescriptionPreviousVersion = getPreviousPrescriptionVersion(
          previousMedicationOrderLastVersion,
          patientId);

      final TherapyDto previousTherapyPreviousVersion =
          medicationsBo.convertMedicationOrderToTherapyDto(
              previousPrescriptionPreviousVersion,
              previousPrescriptionPreviousVersion.getMedicationOrder(),
              null,
              null,
              true,
              locale);

      actionHistoryList.addAll(extractModifyExistingCompActions(previousPrescriptionPreviousVersion, locale));
      actionHistoryList.addAll(extractSimpleActions(previousPrescriptionPreviousVersion));

      // only latest version has complete action (modify and start new) but it has changed therapy.end !!
      // use previous version with old therapy.end to calculate changes
      actionHistoryList.add(extractModifyAction(
          previousMedicationOrderLastVersion,
          previousTherapyPreviousVersion,
          therapyFirstVersion,
          false,
          locale));

      prescription = previousMedicationOrderLastVersion;
      updateLinks = LinksEhrUtils.getLinksOfType(prescription, EhrLinkType.UPDATE);

      // use first version to calculate changes in the next iteration
      final InpatientPrescription previousPrescriptionFirstVersion = getFirstPrescriptionVersion(
          previousMedicationOrderLastVersion,
          patientId);

      therapyFirstVersion = medicationsBo.convertMedicationOrderToTherapyDto(
          previousPrescriptionFirstVersion,
          previousPrescriptionFirstVersion.getMedicationOrder(),
          null,
          null,
          true,
          locale);
    }

    auditTrail.setOriginalTherapy(therapyFirstVersion);

    actionHistoryList.addAll(getPharmacyReviewsToAuditTrail(patientId, therapyFirstVersion.getCreatedTimestamp()));
    actionHistoryList.add(getPrescribeActionToAuditTrail(prescription));

    actionHistoryList.removeAll(getRedundantDoctorReviewActions(actionHistoryList));
    actionHistoryList.sort(Comparator.comparing(TherapyActionHistoryDto::getActionPerformedTime));

    return auditTrail;
  }

  private InpatientPrescription getPreviousPrescriptionVersion(final InpatientPrescription prescription, final String patientId)
  {
    final String previousVersion = TherapyIdUtils.getCompositionUidForPreviousVersion(prescription.getUid());
    return medicationsOpenEhrDao.loadInpatientPrescription(patientId, previousVersion);
  }

  private InpatientPrescription getFirstPrescriptionVersion(final InpatientPrescription prescription, final String patientId)
  {
    final String firstVersion = TherapyIdUtils.getCompositionUidForFirstVersion(prescription.getUid());
    return medicationsOpenEhrDao.loadInpatientPrescription(patientId, firstVersion);
  }

  private TherapyActionHistoryDto extractModifyAction(
      final InpatientPrescription prescription,
      final TherapyDto previousTherapy,
      final TherapyDto therapy,
      final boolean modifyExistingComp,
      final Locale locale)
  {
    final MedicationActionEnum actionEnum = modifyExistingComp ? MedicationActionEnum.MODIFY_EXISTING : MedicationActionEnum.COMPLETE;

    final TherapyActionHistoryDto changeHistory = new TherapyActionHistoryDto();
    changeHistory.setChanges(therapyChangeCalculator.calculateTherapyChanges(
        previousTherapy,
        therapy,
        modifyExistingComp,
        locale));

    final MedicationManagement latestModifyAction = prescription.getActions().stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == actionEnum)
        .max(Comparator.comparing(a -> DataValueUtils.getDateTime(a.getTime())))
        .orElse(null);

    if (latestModifyAction != null)
    {
      changeHistory.setActionPerformedTime(DataValueUtils.getDateTime(latestModifyAction.getTime()));
      changeHistory.setChangeReason(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestModifyAction));
    }

    //noinspection ConstantConditions
    Opt.resolve(() -> latestModifyAction.getOtherParticipations().get(0).getName()).ifPresent(changeHistory::setPerformer);

    if (!modifyExistingComp)
    {
      changeHistory.setActionTakesEffectTime(therapy.getStart());
    }

    changeHistory.setTherapyActionHistoryType(
        modifyExistingComp ? TherapyActionHistoryType.MODIFY_EXISTING : TherapyActionHistoryType.MODIFY);

    return changeHistory;
  }

  private TherapyActionHistoryDto getPrescribeActionToAuditTrail(final InpatientPrescription prescription)
  {
    final TherapyActionHistoryDto changeHistory = new TherapyActionHistoryDto();
    changeHistory.setPerformer(prescription.getComposer().getName());
    changeHistory.setActionPerformedTime(DataValueUtils.getDateTime(prescription.getContext().getStartTime()));
    changeHistory.setTherapyActionHistoryType(TherapyActionHistoryType.PRESCRIBE);
    return changeHistory;
  }

  private List<TherapyActionHistoryDto> extractModifyExistingCompActions(
      final InpatientPrescription prescription,
      final Locale locale)
  {
    final List<TherapyActionHistoryDto> actions = new ArrayList<>();
    final boolean therapyModifiedBeforeStart = !getModifyExistingActions(prescription).isEmpty();
    if (therapyModifiedBeforeStart)
    {
      final List<InpatientPrescription> allVersions = medicationsOpenEhrDao.getAllInpatientPrescriptionVersions(prescription.getUid());

      InpatientPrescription previousVersion = null;

      for (final InpatientPrescription prescriptionVersion : allVersions)
      {
        if (previousVersion != null)
        {
          final List<MedicationManagement> previousModifyExistingActions = getModifyExistingActions(previousVersion);
          final List<MedicationManagement> modifyExistingActions = getModifyExistingActions(prescriptionVersion);

          final boolean therapyModifiedInThisVersion = previousModifyExistingActions.size() != modifyExistingActions.size();
          if (therapyModifiedInThisVersion)
          {
            final TherapyDto previousTherapy =
                medicationsBo.convertMedicationOrderToTherapyDto(
                    previousVersion,
                    previousVersion.getMedicationOrder(),
                    null,
                    null,
                    true,
                    locale);

            final TherapyDto therapy =
                medicationsBo.convertMedicationOrderToTherapyDto(
                    prescriptionVersion,
                    prescriptionVersion.getMedicationOrder(),
                    null,
                    null,
                    true,
                    locale);

            actions.add(extractModifyAction(prescriptionVersion, previousTherapy, therapy, true, locale));
          }
        }
        previousVersion = prescriptionVersion;
      }
    }
    return actions;
  }

  private List<MedicationManagement> getModifyExistingActions(final InpatientPrescription prescription)
  {
    return prescription.getActions().stream()
        .filter(a -> MedicationActionEnum.getActionEnum(a) == MedicationActionEnum.MODIFY_EXISTING)
        .collect(Collectors.toList());
  }

  private List<TherapyActionHistoryDto> extractSimpleActions(final InpatientPrescription prescription)
  {
    return prescription.getActions().stream()
        .map(this::buildActionHistory)
        .filter(Opt::isPresent)
        .map(Opt::get)
        .collect(Collectors.toList());
  }

  private Opt<TherapyActionHistoryType> mapToTherapyActionHistoryType(final MedicationActionEnum medicationActionEnum)
  {
    if (medicationActionEnum == MedicationActionEnum.REVIEW)
    {
      return Opt.of(TherapyActionHistoryType.DOCTOR_REVIEW);
    }
    if (medicationActionEnum == MedicationActionEnum.SUSPEND)
    {
      return Opt.of(TherapyActionHistoryType.SUSPEND);
    }
    if (medicationActionEnum == MedicationActionEnum.REISSUE)
    {
      return Opt.of(TherapyActionHistoryType.REISSUE);
    }
    if (medicationActionEnum == MedicationActionEnum.ABORT || medicationActionEnum == MedicationActionEnum.CANCEL)
    {
      return Opt.of(TherapyActionHistoryType.STOP);
    }
    return Opt.none();
  }

  private Opt<TherapyActionHistoryDto> buildActionHistory(final MedicationManagement action)
  {
    final Opt<TherapyActionHistoryType> type = mapToTherapyActionHistoryType(MedicationActionEnum.getActionEnum(action));

    if (type.isPresent())
    {
      final TherapyActionHistoryDto actionHistory = new TherapyActionHistoryDto();

      actionHistory.setTherapyActionHistoryType(type.get());
      actionHistory.setActionPerformedTime(DataValueUtils.getDateTime(action.getTime()));

      Opt.resolve(() -> action.getOtherParticipations().get(0).getName()).ifPresent(actionHistory::setPerformer);

      actionHistory.setChangeReason(MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(action));

      return Opt.of(actionHistory);
    }
    return Opt.none();
  }

  private List<TherapyActionHistoryDto> getPharmacyReviewsToAuditTrail(final String patientId, final DateTime fromTime)
  {
    return medicationsOpenEhrDao.findPharmacistsReviewReports(patientId, fromTime)
        .stream()
        .filter(c -> DataValueUtils.getDateTime(c.getContext().getStartTime()).isAfter(fromTime))
        .map(this::getPharmacyReviewAction)
        .collect(Collectors.toList());
  }

  private TherapyActionHistoryDto getPharmacyReviewAction(final PharmacyReviewReport composition)
  {
    final TherapyActionHistoryDto therapyActionHistory = new TherapyActionHistoryDto();
    therapyActionHistory.setTherapyActionHistoryType(TherapyActionHistoryType.PHARMACIST_REVIEW);
    therapyActionHistory.setActionPerformedTime(DataValueUtils.getDateTime(composition.getContext().getStartTime()));
    therapyActionHistory.setPerformer(composition.getComposer().getName());
    return therapyActionHistory;
  }

  private List<TherapyActionHistoryDto> getRedundantDoctorReviewActions(final Collection<TherapyActionHistoryDto> actions)
  {
    final Set<DateTime> otherActionTimes = actions.stream()
        .filter(a -> a.getTherapyActionHistoryType() != TherapyActionHistoryType.DOCTOR_REVIEW)
        .map(TherapyActionHistoryDto::getActionPerformedTime)
        .collect(Collectors.toSet());

    return actions.stream()
        .filter(a -> a.getTherapyActionHistoryType() == TherapyActionHistoryType.DOCTOR_REVIEW)
        .filter(a -> otherActionTimes.contains(a.getActionPerformedTime()))
        .collect(Collectors.toList());
  }
}
