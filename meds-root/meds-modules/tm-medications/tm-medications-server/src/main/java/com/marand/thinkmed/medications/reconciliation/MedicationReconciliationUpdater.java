package com.marand.thinkmed.medications.reconciliation;

import java.util.Optional;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dao.openehr.ReconciliationOpenEhrDao;
import com.marand.thinkmed.medications.ehr.model.MedicationReconciliation;
import com.marand.thinkmed.medications.ehr.model.MedicationReconciliationDetails;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MedicationReconciliationUpdater
{
  private final ReconciliationOpenEhrDao reconciliationOpenEhrDao;
  private final MedicationsOpenEhrDao medicationsOpenEhrDao;
  private final RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public MedicationReconciliationUpdater(
      final ReconciliationOpenEhrDao reconciliationOpenEhrDao,
      final MedicationsOpenEhrDao medicationsOpenEhrDao,
      final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.reconciliationOpenEhrDao = reconciliationOpenEhrDao;
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  /**
   * @return created reconciliation uid without version
   */
  public String startNew(final @NonNull String patientId, final String centralCaseId, final String careProviderId)
  {
    return startNew(patientId, centralCaseId, careProviderId, null, null);
  }

  /**
   * @return created reconciliation uid without version
   */
  public String startNew(
      final @NonNull String patientId,
      final String centralCaseId,
      final String careProviderId,
      final DateTime admissionLastUpdateTime,
      final DateTime dischargeLastUpdateTime)
  {
    final MedicationReconciliation reconciliation = new MedicationReconciliation();
    reconciliation.getReconciliationDetails().setAdmissionLastUpdateTime(DataValueUtils.getDateTime(admissionLastUpdateTime));
    reconciliation.getReconciliationDetails().setDischargeLastUpdateTime(DataValueUtils.getDateTime(dischargeLastUpdateTime));

    new EhrContextVisitor(reconciliation)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withCentralCaseId(centralCaseId)
        .withCareProvider(careProviderId)
        .withStartTime(requestDateTimeHolder.getRequestTimestamp())
        .visit();

    return TherapyIdUtils.getCompositionUidWithoutVersion(medicationsOpenEhrDao.saveComposition(patientId, reconciliation, null));
  }

  /**
   * Updates reconciliation if it exists, otherwise new reconciliation is created.
   *
   * @param admissionLastUpdateTime to be set on reconciliation. Not overwritten if null.
   * @param dischargeLastUpdateTime to be set on reconciliation. Not overwritten if null.
   *
   * @return reconciliation uid without version
   */
  public String updateOrStartNew(
      final @NonNull String patientId,
      final String centralCaseId,
      final String careProviderId,
      final DateTime admissionLastUpdateTime,
      final DateTime dischargeLastUpdateTime)
  {
    final Optional<MedicationReconciliation> latest = reconciliationOpenEhrDao.findLatestMedicationReconciliation(patientId);

    if (latest.isPresent()) // update
    {
      final MedicationReconciliationDetails details = latest.get().getReconciliationDetails();
      if (admissionLastUpdateTime != null)
      {
        details.setAdmissionLastUpdateTime(DataValueUtils.getDateTime(admissionLastUpdateTime));
      }
      if (dischargeLastUpdateTime != null)
      {
        details.setDischargeLastUpdateTime(DataValueUtils.getDateTime(dischargeLastUpdateTime));
      }
      return update(patientId, latest.get());
    }
    else // start new
    {
      return startNew(patientId, centralCaseId, careProviderId, admissionLastUpdateTime, dischargeLastUpdateTime);
    }
  }

  private String update(final String patientId, final MedicationReconciliation reconciliation)
  {
    new EhrContextVisitor(reconciliation).withComposer(RequestUser.getId(), RequestUser.getFullName()).visit();
    return TherapyIdUtils.getCompositionUidWithoutVersion(medicationsOpenEhrDao.saveComposition(
        patientId,
        reconciliation,
        reconciliation.getUid()));
  }
}
