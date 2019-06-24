package com.marand.thinkmed.medications.pharmacist;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.DataValueUtils;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;

/**
 * @author Klavdij Lapajne
 */
public class PharmacistUtils
{
  private PharmacistUtils() { }

  public static PharmacistTherapyChangeType mapToPharmacistTherapyChangeType(final MedicationManagement action)
  {
    final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);

    if (actionEnum == MedicationActionEnum.ABORT)
    {
      return PharmacistTherapyChangeType.ABORT;
    }
    if (actionEnum == MedicationActionEnum.RECOMMEND)
    {
      return PharmacistTherapyChangeType.EDIT;
    }
    if (actionEnum == MedicationActionEnum.SUSPEND)
    {
      return  PharmacistTherapyChangeType.SUSPEND;
    }
    return null;
  }

  public static MedicationActionEnum mapToMedicationActionType(final PharmacistTherapyChangeType changeType)
  {
    if (changeType == PharmacistTherapyChangeType.ABORT)
    {
      return MedicationActionEnum.ABORT;
    }
    if (changeType == PharmacistTherapyChangeType.EDIT)
    {
      return MedicationActionEnum.RECOMMEND;
    }
    if (changeType == PharmacistTherapyChangeType.SUSPEND)
    {
      return MedicationActionEnum.SUSPEND;
    }
    return null;
  }

  public static Set<String> extractReferredBackTherapiesCompositionUids(final List<PharmacyReviewReport> reviewReports)
  {
    return reviewReports
        .stream()
        .filter(r -> PharmacistReviewStatusEnum.FINAL.matches(r.getContext().getStatus()))
        .filter(r -> r.getMiscellaneous().getPrescriberReferral() != null)
        .filter(r -> r.getMiscellaneous().getPrescriberReferralResponse() == null)
        .map(r -> {
          final List<Link> reviewedLinks = LinksEhrUtils.getLinksOfType(r, EhrLinkType.REVIEWED);
          final String compositionId = OpenEhrRefUtils.parseEhrUri(reviewedLinks.get(0).getTarget().getValue()).getCompositionId();
          return TherapyIdUtils.getCompositionUidWithoutVersion(compositionId);
        })
        .distinct()
        .collect(Collectors.toSet());
  }

  public static DateTime extractLastReviewTime(final List<PharmacyReviewReport> reviewReports)
  {
    return reviewReports
        .stream()
        .filter(r -> PharmacistReviewStatusEnum.FINAL.matches(r.getContext().getStatus()))
        .map(r -> DataValueUtils.getDateTime(r.getContext().getStartTime()))
        .max(Comparator.naturalOrder())
        .orElse(null);
  }
}
