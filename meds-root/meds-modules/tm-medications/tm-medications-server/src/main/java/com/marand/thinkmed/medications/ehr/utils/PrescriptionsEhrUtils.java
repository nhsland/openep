package com.marand.thinkmed.medications.ehr.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;

/**
 * @author Nejc Korasa
 */

public final class PrescriptionsEhrUtils
{
  private PrescriptionsEhrUtils() { }

  public static boolean isInpatientPrescriptionModifiedFromLastReview(final @NonNull InpatientPrescription inpatientPrescription)
  {
    final List<MedicationManagement> actions = inpatientPrescription.getActions();

    final Optional<DateTime> latestModifyActionTime = actions
        .stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == MedicationActionEnum.MODIFY_EXISTING)
        .map(a -> DataValueUtils.getDateTime(a.getTime()))
        .max(Comparator.naturalOrder());

    final List<Link> updateLinks = LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), EhrLinkType.UPDATE);
    if (updateLinks.isEmpty() && !latestModifyActionTime.isPresent())
    {
      return false;
    }

    final DateTime compositionCreatedTime = DataValueUtils.getDateTime(inpatientPrescription.getContext().getStartTime());
    final DateTime latestModifyTime =
        latestModifyActionTime.isPresent() && latestModifyActionTime.get().isAfter(compositionCreatedTime)
        ? latestModifyActionTime.get()
        : compositionCreatedTime;

    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      final DateTime actionDateTime = DataValueUtils.getDateTime(action.getTime());
      if (MedicationActionEnum.THERAPY_REVIEW_ACTIONS.contains(actionEnum) && actionDateTime.isAfter(latestModifyTime))
      {
        return false;
      }
    }

    return true;
  }

  public static TherapyChangeReasonDto getSuspendReason(final @NonNull InpatientPrescription inpatientPrescription)
  {
    final List<MedicationManagement> actions = inpatientPrescription.getActions();
    TherapyChangeReasonDto suspendReason = null;
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.SUSPEND)
      {
        suspendReason = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(action);
      }
      else if (actionEnum == MedicationActionEnum.REISSUE)
      {
        suspendReason = null;
      }
    }
    return suspendReason;
  }

  public static TherapyChangeReasonDto getStoppedReason(final @NonNull InpatientPrescription inpatientPrescription)
  {
    final List<MedicationManagement> actions = inpatientPrescription.getActions();
    TherapyChangeReasonDto stoppedReason = null;
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.ABORT || actionEnum == MedicationActionEnum.CANCEL)
      {
        stoppedReason = MedicationsEhrUtils.getTherapyChangeReasonDtoFromAction(action);
      }
    }
    return stoppedReason;
  }

  public static boolean isInpatientPrescriptionCancelledOrAborted(final @NonNull InpatientPrescription inpatientPrescription)
  {
    return isTherapyCanceledOrAborted(inpatientPrescription.getActions());
  }

  public static boolean isInpatientPrescriptionCompleted(final @NonNull InpatientPrescription inpatientPrescription)
  {
    for (final MedicationManagement action : inpatientPrescription.getActions())
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (MedicationActionEnum.THERAPY_FINISHED.contains(actionEnum))
      {
        return true;
      }
    }
    return false;
  }

  public static boolean isTherapyCanceledAbortedOrSuspended(final @NonNull List<MedicationManagement> actions)
  {
    if (isTherapySuspended(actions))
    {
      return true;
    }
    return isTherapyCanceledOrAborted(actions);
  }

  public static boolean isTherapyCanceledOrAborted(final @NonNull List<MedicationManagement> actions)
  {
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.CANCEL || actionEnum == MedicationActionEnum.ABORT)
      {
        return true;
      }
    }
    return false;
  }

  public static boolean isTherapySuspended(final @NonNull InpatientPrescription prescription)
  {
    return isTherapySuspended(prescription.getActions());
  }

  public static boolean isTherapySuspended(final @NonNull List<MedicationManagement> actions)  // actions sorted by time ascending
  {
    boolean suspended = false;
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.SUSPEND)
      {
        suspended = true;
      }
      else if (actionEnum == MedicationActionEnum.REISSUE)
      {
        suspended = false;
      }
    }
    return suspended;
  }

  public static DateTime getLastModifiedTimestamp(final @NonNull InpatientPrescription inpatientPrescription)
  {
    return inpatientPrescription.getActions().stream()
        .filter(action -> MedicationActionEnum.getActionEnum(action) == MedicationActionEnum.MODIFY_EXISTING)
        .map(action -> DataValueUtils.getDateTime(action.getTime()))
        .max(Comparator.naturalOrder())
        .orElse(null);
  }

  public static String getOriginalTherapyId(final @NonNull InpatientPrescription prescription)
  {
    final List<Link> originLinks = LinksEhrUtils.getLinksOfType(prescription.getLinks(), EhrLinkType.ORIGIN);
    return originLinks.isEmpty()
           ? TherapyIdUtils.createTherapyId(prescription.getUid())
           : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0));
  }

  public static boolean removeLinksOfType(final InpatientPrescription inpatientPrescription, final EhrLinkType linkType)
  {
    boolean hadLinks = false;
    if (inpatientPrescription != null)
    {
      final List<Link> linksOfType = LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), linkType);
      hadLinks = !linksOfType.isEmpty();
      inpatientPrescription.getLinks().removeAll(linksOfType);
    }
    return hadLinks;
  }

  public static InpatientPrescription extractPrescriptionByTherapyId(
      final String therapyId,
      final List<InpatientPrescription> prescriptions)
  {
    if (therapyId != null)
    {
      return prescriptions.stream()
          .filter(p -> therapyId.equals(TherapyIdUtils.createTherapyId(p)))
          .findAny()
          .orElse(null);
    }

    return null;
  }
}
