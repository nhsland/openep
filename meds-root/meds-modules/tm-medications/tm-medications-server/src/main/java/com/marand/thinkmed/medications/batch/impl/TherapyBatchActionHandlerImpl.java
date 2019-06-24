package com.marand.thinkmed.medications.batch.impl;

import java.util.ArrayList;
import java.util.List;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.TherapyBatchActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.therapy.updater.TherapyUpdater;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mitja Lapajne
 */
@Component
public class TherapyBatchActionHandlerImpl implements TherapyBatchActionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private TherapyUpdater therapyUpdater;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Override
  @Transactional
  @EhrSessioned
  public void abortAllTherapies(final @NonNull String patientId, final @NonNull DateTime when, final String stopReason)
  {
    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.ABORT_ALL, stopReason);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void suspendAllTherapies(
      final @NonNull String patientId,
      final @NonNull DateTime when,
      final String suspendReason)
  {
    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.SUSPEND_ALL, suspendReason);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void suspendAllTherapiesOnTemporaryLeave(final @NonNull String patientId, final @NonNull DateTime when)
  {
    updateAllTherapiesWithAction(patientId, when, TherapyBatchActionEnum.SUSPEND_ALL_ON_TEMPORARY_LEAVE, null);
  }

  @Override
  @Transactional
  @EhrSessioned
  public List<String> reissueAllTherapiesOnReturnFromTemporaryLeave(final @NonNull String patientId, final @NonNull DateTime when)
  {
    return updateAllTherapiesWithAction(
        patientId,
        when,
        TherapyBatchActionEnum.REISSUE_ALL_ON_RETURN_FROM_TEMPORARY_LEAVE,
        null);
  }

  private List<String> updateAllTherapiesWithAction(
      final String patientId,
      final DateTime when,
      final TherapyBatchActionEnum batchAction,
      final String actionChangeReason)
  {
    final Interval searchInterval = Intervals.infiniteFrom(when);
    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(patientId, searchInterval);

    final List<String> updatedCompositionUids = new ArrayList<>();
    for (final InpatientPrescription prescription : prescriptions)
    {
      if (!PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(prescription))
      {
        final String compositionUid = prescription.getUid();
        final String instructionName = prescription.getMedicationOrder().getName().getValue();

        final boolean therapySuspended = PrescriptionsEhrUtils.isTherapySuspended(prescription);
        final boolean therapyAborted = PrescriptionsEhrUtils.isTherapyCanceledOrAborted(prescription.getActions());

        if (batchAction == TherapyBatchActionEnum.ABORT_ALL)
        {
          if (!therapyAborted)
          {
            final TherapyChangeReasonDto changeReason = new TherapyChangeReasonDto();
            changeReason.setComment(actionChangeReason);
            changeReason.setChangeReason(new CodedNameDto(
                TherapyChangeReasonEnum.STOP.toFullString(),
                Dictionary.getEntry(
                    TherapyChangeReasonEnum.STOP.toFullString(),
                    DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()
                )
            ));

            updatedCompositionUids.add(therapyUpdater.abortTherapy(patientId, compositionUid, instructionName, changeReason, when));
          }

        }
        else if (batchAction == TherapyBatchActionEnum.SUSPEND_ALL)
        {
          if (!therapySuspended)
          {
            final TherapyChangeReasonDto changeReason = new TherapyChangeReasonDto();
            changeReason.setComment(actionChangeReason);
            changeReason.setChangeReason(new CodedNameDto(
                TherapyChangeReasonEnum.SUSPEND.toFullString(),
                Dictionary.getEntry(
                    TherapyChangeReasonEnum.SUSPEND.toFullString(),
                    DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()
                )
            ));

            updatedCompositionUids.add(therapyUpdater.suspendTherapy(
                patientId,
                compositionUid,
                instructionName,
                changeReason,
                when));
          }
        }
        else if (batchAction == TherapyBatchActionEnum.SUSPEND_ALL_ON_TEMPORARY_LEAVE)
        {
          if (!therapySuspended)
          {
            final TherapyChangeReasonDto therapyChangeReasonDto = new TherapyChangeReasonDto();
            final CodedNameDto changeReasonDto =
                new CodedNameDto(
                    TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
                    Dictionary.getEntry(
                        TherapyChangeReasonEnum.TEMPORARY_LEAVE.toFullString(),
                        DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()));

            therapyChangeReasonDto.setChangeReason(changeReasonDto);
            updatedCompositionUids.add(therapyUpdater.suspendTherapy(
                patientId,
                compositionUid,
                instructionName,
                therapyChangeReasonDto,
                when));
          }
        }
        else if (batchAction == TherapyBatchActionEnum.REISSUE_ALL_ON_RETURN_FROM_TEMPORARY_LEAVE)
        {
          if (therapySuspended)
          {
            final boolean therapySuspendedBecauseOfTemporaryLeave = isTherapySuspendedBecauseOfTemporaryLeave(prescription);
            if (therapySuspendedBecauseOfTemporaryLeave)
            {
              updatedCompositionUids.add(therapyUpdater.reissueTherapy(patientId, compositionUid, instructionName, when));
            }
          }
        }
        else
        {
          throw new IllegalArgumentException("Action not supported");
        }
      }
    }
    return updatedCompositionUids;
  }

  private boolean isTherapySuspendedBecauseOfTemporaryLeave(final InpatientPrescription prescription)
  {
    final TherapyChangeReasonDto therapySuspendReason = PrescriptionsEhrUtils.getSuspendReason(prescription);
    if (therapySuspendReason != null)
    {
      final TherapyChangeReasonEnum therapyChangeReasonEnum = TherapyChangeReasonEnum.fromFullString(therapySuspendReason.getChangeReason().getCode());
      return therapyChangeReasonEnum == TherapyChangeReasonEnum.TEMPORARY_LEAVE;
    }
    return false;
  }
}
