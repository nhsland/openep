package com.marand.thinkmed.medications.reconciliation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.admission.AdmissionStatusMapper;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.ReconciliationOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.dto.AdmissionChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeReconciliationDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeStatus;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationRowGroupEnum;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.ehr.model.MedicationReconciliation;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class ReconciliationSummaryHandler
{
  private ReconciliationOpenEhrDao reconciliationOpenEhrDao;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private TherapyChangeCalculator therapyChangeCalculator;
  private ReconciliationReviewHandler reconciliationReviewHandler;

  @Autowired
  public void setReconciliationOpenEhrDao(final ReconciliationOpenEhrDao reconciliationOpenEhrDao)
  {
    this.reconciliationOpenEhrDao = reconciliationOpenEhrDao;
  }

  @Autowired
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Autowired
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Autowired
  public void setReconciliationReviewHandler(final ReconciliationReviewHandler reconciliationReviewHandler)
  {
    this.reconciliationReviewHandler = reconciliationReviewHandler;
  }

  public ReconciliationSummaryDto getReconciliationSummary(final String patientId, final Locale locale)
  {
    final MedicationReconciliation reconciliation = reconciliationOpenEhrDao.findLatestMedicationReconciliation(patientId).orElse(null);
    if (reconciliation == null)
    {
      return ReconciliationSummaryDto.empty();
    }
    final String reconciliationUid = TherapyIdUtils.getCompositionUidWithoutVersion(reconciliation.getUid());

    // load admissions
    final List<MedicationOnAdmissionReconciliationDto> admissions = medicationOnAdmissionHandler.getAdmissionsForReconciliation(
        patientId,
        reconciliationUid,
        locale);

    final Map<String, MedicationOnAdmissionReconciliationDto> admissionsMap = admissions
        .stream()
        .collect(Collectors.toMap(
            a -> TherapyIdUtils.getCompositionUidWithoutVersion(a.getTherapy().getCompositionUid()),
            a -> a));

    // load discharges
    final List<MedicationOnDischargeReconciliationDto> discharges = medicationOnDischargeHandler.getDischargesForReconciliation(
        patientId,
        reconciliationUid,
        locale);

    // create admissions added on discharge time map
    final Map<String, DateTime> admissionsAddedOnDischargeTimeMap = new HashMap<>();
    discharges.forEach(d -> admissionsAddedOnDischargeTimeMap.put(d.getLinkedAdmissionCompositionId(), d.getTherapy().getCreatedTimestamp()));

    final Map<String, AdmissionChangeReasonDto> lastEditAdmissionChangeReasons = reconciliationOpenEhrDao.getLastEditAdmissionChangeReasons(
        patientId,
        admissionsAddedOnDischargeTimeMap);

    final List<ReconciliationRowDto> resultRows = new ArrayList<>();
    for (final MedicationOnDischargeReconciliationDto onDischargeReconciliationDto : discharges)
    {
      final String admissionCompositionUid =
          onDischargeReconciliationDto.getLinkedAdmissionCompositionId() != null
          ? TherapyIdUtils.getCompositionUidWithoutVersion(onDischargeReconciliationDto.getLinkedAdmissionCompositionId())
          : null;

      final boolean hasLinkedAdmission = admissionCompositionUid != null && admissionsMap.containsKey(admissionCompositionUid);
      final ReconciliationRowDto row = new ReconciliationRowDto();
      if (hasLinkedAdmission)
      {
        final MedicationOnAdmissionReconciliationDto onAdmissionReconciliationDto = admissionsMap.get(admissionCompositionUid);
        row.setTherapyOnAdmission(onAdmissionReconciliationDto.getTherapy());

        if (onDischargeReconciliationDto.getStatus() == MedicationOnDischargeStatus.NOT_PRESCRIBED)
        {
          row.setChangeReasonDto(onDischargeReconciliationDto.getChangeReasonDto());
          row.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION);
          row.setStatusEnum(TherapyStatusEnum.ABORTED);
        }
        else
        {
          row.setTherapyOnDischarge(onDischargeReconciliationDto.getTherapy());

          final AdmissionChangeReasonDto lastEditChangeReason = lastEditAdmissionChangeReasons.get(admissionCompositionUid);

          // change reason when editing and prescribing discharge composition from admission is saved on discharge
          final TherapyChangeReasonDto changeReason = Opt
              .of(onDischargeReconciliationDto.getChangeReasonDto())
              .orElseGet(() -> lastEditChangeReason == null ? null : lastEditChangeReason.getChangeReason());

          fillChangeDetails(
              onAdmissionReconciliationDto,
              onDischargeReconciliationDto,
              changeReason,
              row,
              locale);
        }

        resultRows.add(row);
        admissionsMap.remove(admissionCompositionUid);
      }
      else
      {
        row.setTherapyOnDischarge(onDischargeReconciliationDto.getTherapy());
        row.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_DISCHARGE);
        resultRows.add(row);
      }
    }

    // process unlinked admissions
    if (!admissionsMap.isEmpty())
    {
      final Map<String, AdmissionChangeReasonDto> lastAbortAdmissionChangeReasons = reconciliationOpenEhrDao.getLastAdmissionChangeReasons(
          patientId,
          admissionsAddedOnDischargeTimeMap,
          true);

      for (final Map.Entry<String, MedicationOnAdmissionReconciliationDto> mapEntry : admissionsMap.entrySet())
      {
        final ReconciliationRowDto rowDto = new ReconciliationRowDto();
        final MedicationOnAdmissionReconciliationDto admissionReconciliationDto = mapEntry.getValue();

        rowDto.setTherapyOnAdmission(admissionReconciliationDto.getTherapy());
        rowDto.setGroupEnum(ReconciliationRowGroupEnum.ONLY_ON_ADMISSION);

        final TherapyStatusEnum admissionReconciliationStatusEnum = admissionReconciliationDto.getStatusEnum();
        final TherapyChangeReasonDto changeReasonOnAdmission = admissionReconciliationDto.getChangeReasonDto();
        final boolean isAdmissionAbortedOrCanceledOrSuspended =
            admissionReconciliationStatusEnum == TherapyStatusEnum.CANCELLED ||
                admissionReconciliationStatusEnum == TherapyStatusEnum.ABORTED ||
                admissionReconciliationStatusEnum == TherapyStatusEnum.SUSPENDED;

        if (changeReasonOnAdmission != null && isAdmissionAbortedOrCanceledOrSuspended)
        {
          rowDto.setChangeReasonDto(changeReasonOnAdmission);
          rowDto.setStatusEnum(admissionReconciliationStatusEnum);
        }
        else
        {
          final AdmissionChangeReasonDto admissionChangeReason = lastAbortAdmissionChangeReasons.get(mapEntry.getKey());

          if (admissionChangeReason != null)
          {
            rowDto.setChangeReasonDto(admissionChangeReason.getChangeReason());
            rowDto.setStatusEnum(AdmissionStatusMapper.Companion.mapToTherapyStatus(admissionChangeReason.getActionEnum()));
          }
        }
        if (rowDto.getStatusEnum() == null)
        {
          rowDto.setStatusEnum(TherapyStatusEnum.ABORTED);
        }
        resultRows.add(rowDto);
      }
    }

    final boolean admissionReviewed = !reconciliationReviewHandler.reviewTaskExists(
        patientId,
        TaskTypeEnum.ADMISSION_REVIEW_TASK);
    final boolean dischargeReviewed = !reconciliationReviewHandler.reviewTaskExists(
        patientId,
        TaskTypeEnum.DISCHARGE_REVIEW_TASK);
    return new ReconciliationSummaryDto(
        resultRows,
        DataValueUtils.getDateTime(reconciliation.getContext().getStartTime()),
        admissionReviewed,
        dischargeReviewed,
        DataValueUtils.getDateTime(reconciliation.getReconciliationDetails().getAdmissionLastUpdateTime()),
        DataValueUtils.getDateTime(reconciliation.getReconciliationDetails().getDischargeLastUpdateTime()));
  }

  void fillChangeDetails(
      final MedicationOnAdmissionReconciliationDto admission,
      final MedicationOnDischargeReconciliationDto discharge,
      final TherapyChangeReasonDto inpatientChangeReason,
      final ReconciliationRowDto rowDto,
      final Locale locale)
  {
    rowDto.setChanges(
        therapyChangeCalculator.calculateTherapyChanges(
            admission.getTherapy(),
            discharge.getTherapy(),
            false,
            locale));

    if (rowDto.getChanges().isEmpty())
    {
      rowDto.setGroupEnum(ReconciliationRowGroupEnum.NOT_CHANGED);
    }
    else
    {
      rowDto.setGroupEnum(ReconciliationRowGroupEnum.CHANGED);

      if (discharge.getChangeReasonDto() != null)
      {
        rowDto.setChangeReasonDto(discharge.getChangeReasonDto());
      }
      else if (inpatientChangeReason != null)
      {
        rowDto.setChangeReasonDto(inpatientChangeReason);
      }
      else
      {
        rowDto.setChangeReasonDto(admission.getChangeReasonDto());
      }
    }
  }
}
