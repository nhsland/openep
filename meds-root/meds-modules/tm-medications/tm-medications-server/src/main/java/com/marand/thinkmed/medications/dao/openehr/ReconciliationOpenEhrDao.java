package com.marand.thinkmed.medications.dao.openehr;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Lists;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.openehr.dao.OpenEhrDaoSupport;
import com.marand.maf.core.resultrow.ResultRowProcessor;
import com.marand.openehr.rm.RmObject;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.mapping.EhrMapper;
import com.marand.thinkmed.medications.dto.AdmissionChangeReasonDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnAdmission;
import com.marand.thinkmed.medications.ehr.model.MedicationOnDischarge;
import com.marand.thinkmed.medications.ehr.model.MedicationReconciliation;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Composition;
import org.openehr.jaxb.rm.Link;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings({"rawtypes", "unchecked", "Duplicates"})
@Component
public class ReconciliationOpenEhrDao extends OpenEhrDaoSupport<String>
{
  private final EhrMappersHolder ehrMappersHolder;

  public ReconciliationOpenEhrDao(final EhrMappersHolder ehrMappersHolder)
  {
    this.ehrMappersHolder = ehrMappersHolder;
  }

  private EhrMapper getEhrMapper(final Class<?> objectClass)
  {
    return ehrMappersHolder.getEhrMapper(objectClass);
  }

  public Map<String, AdmissionChangeReasonDto> getLastAdmissionChangeReasons(
      final @NonNull String patientId,
      final boolean onlyAbortedOrSuspended)
  {
    return getLastAdmissionChangeReasons(patientId, Collections.emptyMap(), onlyAbortedOrSuspended);
  }

  /**
   * @param admissionIdDischargeTime represents a mapping between admission id
   * and time when the admission was added to discharge list
   */
  public Map<String, AdmissionChangeReasonDto> getLastAdmissionChangeReasons(
      final @NonNull String patientId,
      final @NonNull Map<String, DateTime> admissionIdDischargeTime,
      final boolean onlyAbortedOrSuspended)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final Set<String> compositionIdsWithAdmissionLink = getOrderCompositionIdsWithAdmissionLinks(patientId);
      if (!compositionIdsWithAdmissionLink.isEmpty())
      {
        currentSession().useEhr(ehrId);
        final StringBuilder query = getEhrQueryForChangeReasons(
            ehrId,
            compositionIdsWithAdmissionLink,
            onlyAbortedOrSuspended ? ChangeReasonType.SUSPENDED_OR_ABORTED : ChangeReasonType.ALL);
        final Map<String, AdmissionChangeReasonDto> resultReasonsMap = new HashMap<>();
        queryEhrContent(
            query.toString(),
            (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {

              final InpatientPrescription inpatientPrescription = (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0]);
              final String linkedAdmissionId = getLinkedAdmissionCompositionIdFromOrderComposition(inpatientPrescription);

              if (linkedAdmissionId != null)
              {
                final MedicationManagement latestMatchingAction = MedicationsEhrUtils.getLatestActionWithChangeReason(inpatientPrescription.getActions(), onlyAbortedOrSuspended);
                final TherapyChangeReasonDto changeReasonDto = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestMatchingAction);
                final DateTime actionDate = DataValueUtils.getDateTime(latestMatchingAction.getTime());
                final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(latestMatchingAction);
                final DateTime dischargeTime = admissionIdDischargeTime.get(linkedAdmissionId);

                if (dischargeTime == null || actionDate.isBefore(dischargeTime) || actionDate.isEqual(dischargeTime))
                {
                  if (resultReasonsMap.containsKey(linkedAdmissionId))
                  {
                    // only load latest actions
                    if (resultReasonsMap.get(linkedAdmissionId).getTime().isBefore(actionDate))
                    {
                      resultReasonsMap.put(
                          linkedAdmissionId,
                          new AdmissionChangeReasonDto(changeReasonDto, actionDate, actionEnum));
                    }
                  }
                  else
                  {
                    resultReasonsMap.put(
                        linkedAdmissionId,
                        new AdmissionChangeReasonDto(changeReasonDto, actionDate, actionEnum));
                  }
                }
              }
              return null;
            }
        );

        return resultReasonsMap;
      }
    }
    return new HashMap<>();
  }

  /**
   * @param admissionIdDischargeTime represents a mapping between admission id
   * and time when the admission was added to discharge list
   */
  public Map<String, AdmissionChangeReasonDto> getLastEditAdmissionChangeReasons(
      final @NonNull String patientId,
      final @NonNull Map<String, DateTime> admissionIdDischargeTime)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      final Set<String> compositionIdsWithAdmissionLink = getOrderCompositionIdsWithAdmissionLinks(patientId);
      if (!compositionIdsWithAdmissionLink.isEmpty())
      {
        currentSession().useEhr(ehrId);
        final StringBuilder query = getEhrQueryForChangeReasons(ehrId, compositionIdsWithAdmissionLink, ChangeReasonType.MODIFIED_ONLY);
        final Map<String, AdmissionChangeReasonDto> resultReasonsMap = new HashMap<>();
        queryEhrContent(
            query.toString(),
            (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {

              final InpatientPrescription inpatientPrescription = (InpatientPrescription)getEhrMapper(InpatientPrescription.class).fromRM((RmObject)resultRow[0]);
              final String linkedAdmissionId = getLinkedAdmissionCompositionIdFromOrderComposition(inpatientPrescription);

              if (linkedAdmissionId != null)
              {
                final MedicationManagement latestModifyAction = MedicationsEhrUtils.getLatestModifyAction(inpatientPrescription.getActions());
                final TherapyChangeReasonDto changeReasonDto = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(latestModifyAction);
                final DateTime actionDate = DataValueUtils.getDateTime(latestModifyAction.getTime());
                final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(latestModifyAction);
                final DateTime dischargeTime = admissionIdDischargeTime.get(linkedAdmissionId);

                if (dischargeTime == null || actionDate.isBefore(dischargeTime) || actionDate.isEqual(dischargeTime))
                {
                  if (resultReasonsMap.containsKey(linkedAdmissionId))
                  {
                    // only load latest actions
                    if (resultReasonsMap.get(linkedAdmissionId).getTime().isBefore(actionDate))
                    {
                      resultReasonsMap.put(
                          linkedAdmissionId,
                          new AdmissionChangeReasonDto(changeReasonDto, actionDate, actionEnum));
                    }
                  }
                  else
                  {
                    resultReasonsMap.put(
                        linkedAdmissionId,
                        new AdmissionChangeReasonDto(changeReasonDto, actionDate, actionEnum));
                  }
                }
              }
              return null;
            }
        );

        return resultReasonsMap;
      }
    }
    return new HashMap<>();
  }

  private String getLinkedAdmissionCompositionIdFromOrderComposition(final InpatientPrescription prescription)
  {
    final List<Link> admissionLinks = LinksEhrUtils.getLinksOfType(prescription, EhrLinkType.MEDICATION_ON_ADMISSION);
    if (!admissionLinks.isEmpty())
    {
      final String targetCompositionId = LinksEhrUtils.getTargetCompositionIdFromLink(admissionLinks.get(0));
      return TherapyIdUtils.getCompositionUidWithoutVersion(targetCompositionId);
    }
    return null;
  }

  private StringBuilder getEhrQueryForChangeReasons(
      final String ehrId,
      final Set<String> compositionIdsWithAdmissionLink,
      final ChangeReasonType changeReasonType)
  {
    final StringBuilder sb = new StringBuilder();
    sb
        .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
        .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
        .append(" CONTAINS Action a[openEHR-EHR-ACTION.medication.v1]")
        .append(" WHERE c/name/value = 'Inpatient Prescription'");

    if (changeReasonType == ChangeReasonType.ALL)
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0041'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0039')")
          .append(" OR (a/ism_transition/careflow_step/defining_code/code_string = 'at0015'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0012'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0010'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0009')");
    }
    else if (changeReasonType == ChangeReasonType.MODIFIED_ONLY)
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0041'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0039')");
    }
    else
    {
      sb
          .append(" AND (a/ism_transition/careflow_step/defining_code/code_string = 'at0015'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0012'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0010'")
          .append(" OR a/ism_transition/careflow_step/defining_code/code_string = 'at0009')");
    }
    sb
        .append(" AND c/uid/value matches {" + getAqlQuoted(compositionIdsWithAdmissionLink) + '}');

    return sb;
  }

  private Set<String> getOrderCompositionIdsWithAdmissionLinks(final String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      final EhrLinkType linkType = EhrLinkType.MEDICATION_ON_ADMISSION;
      sb
          .append("SELECT c/uid/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Inpatient Prescription'")
          .append(" AND c/links/type/value = '" + linkType.getName() + "'");

      final Set<String> compositions = new HashSet<>();
      queryEhrContent(
          sb.toString(),
          (ResultRowProcessor<Object[], Void>)(resultRow, hasNext) -> {
            compositions.add((String)resultRow[0]);
            return null;
          }
      );
      return compositions;
    }

    return Collections.emptySet();
  }

  /**
   * @return latest medication reconciliation uids without version
   */
  public List<String> findLatestMedicationReconciliationUids(final @NonNull String patientId, final int fetchCount)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c/uid/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" WHERE c/name/value = 'Medication Reconciliation'")
          .append(" ORDER BY c/context/start_time DESC")
          .append(" FETCH " + fetchCount);

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> TherapyIdUtils.getCompositionUidWithoutVersion((String)resultRow[0]));
    }

    return Collections.emptyList();
  }

  /**
   * @return latest medication reconciliation uid without version
   */
  public Optional<String> findLatestMedicationReconciliationUid(final String patientId)
  {
    return findLatestMedicationReconciliationUids(patientId, 1).stream().findFirst().map(TherapyIdUtils::getCompositionUidWithoutVersion);
  }

  public Optional<MedicationReconciliation> findLatestMedicationReconciliation(final @NonNull String patientId)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.encounter.v1]")
          .append(" WHERE c/name/value = 'Medication Reconciliation'")
          .append(" ORDER BY c/context/start_time DESC")
          .append(" FETCH 1");

      final List<MedicationReconciliation> resultList = queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (MedicationReconciliation)getEhrMapper(MedicationReconciliation.class).fromRM((RmObject)resultRow[0]));

      return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
    }

    return Optional.empty();
  }

  public List<MedicationOnAdmission> findMedicationsOnAdmission(
      final @NonNull String patientId,
      final @NonNull String reconciliationUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Medication on Admission'")
          .append(" AND c/context/other_context[at0001]/items[at0008]/value/id LIKE '"
                      + TherapyIdUtils.getCompositionUidWithoutVersion(reconciliationUid) + "*'")
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (MedicationOnAdmission)getEhrMapper(MedicationOnAdmission.class).fromRM((RmObject)resultRow[0]));
    }
    return Lists.newArrayList();
  }

  public List<String> findMedicationsOnAdmissionUids(
      final @NonNull String patientId,
      final @NonNull String reconciliationUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c/uid/value FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Medication on Admission'")
          .append(" AND c/context/other_context[at0001]/items[at0008]/value/id LIKE '"
                      + TherapyIdUtils.getCompositionUidWithoutVersion(reconciliationUid) + "*'")
          .append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> (String)resultRow[0]);
    }
    return Lists.newArrayList();
  }

  public MedicationOnAdmission loadMedicationOnAdmission(
      final @NonNull String patientId,
      final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return (MedicationOnAdmission)getEhrMapper(MedicationOnAdmission.class).fromRM(composition);
    }
    return null;
  }

  public List<MedicationOnDischarge> findMedicationsOnDischarge(
      final @NonNull String patientId,
      final String reconciliationUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT c FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Medication on Discharge'");

      if (reconciliationUid != null)
      {
        sb.append(" AND c/context/other_context[at0001]/items[at0008]/value/id LIKE '"
                      + TherapyIdUtils.getCompositionUidWithoutVersion(reconciliationUid) + "*'");
      }

      sb.append(" ORDER BY c/context/start_time DESC");

      return queryEhrContent(
          sb.toString(),
          (resultRow, hasNext) -> (MedicationOnDischarge)getEhrMapper(MedicationOnDischarge.class).fromRM((RmObject)resultRow[0])
      );
    }
    return Lists.newArrayList();
  }

  public int countMedicationsOnDischarge(final @NonNull String patientId, final String reconciliationUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final StringBuilder sb = new StringBuilder();
      sb
          .append("SELECT COUNT(c) FROM EHR[ehr_id/value='").append(ehrId).append("']")
          .append(" CONTAINS Composition c[openEHR-EHR-COMPOSITION.prescription.v0]")
          .append(" WHERE c/name/value = 'Medication on Discharge'");

      if (reconciliationUid != null)
      {
        sb.append(" AND c/context/other_context[at0001]/items[at0008]/value/id LIKE '"
                      + TherapyIdUtils.getCompositionUidWithoutVersion(reconciliationUid) + "*'");
      }

      return queryEhrContent(sb.toString(), (resultRow, hasNext) -> (int)resultRow[0]).get(0);
    }

    return 0;
  }

  public MedicationOnDischarge loadMedicationOnDischarge(
      final @NonNull String patientId,
      final @NonNull String compositionUid)
  {
    final String ehrId = currentSession().findEhr(patientId);
    if (!StringUtils.isEmpty(ehrId))
    {
      currentSession().useEhr(ehrId);
      final Composition composition = currentSession().getComposition(compositionUid);
      return (MedicationOnDischarge)getEhrMapper(MedicationOnDischarge.class).fromRM(composition);
    }
    return null;
  }

  private enum ChangeReasonType
  {
    MODIFIED_ONLY, SUSPENDED_OR_ABORTED, ALL
  }
}
