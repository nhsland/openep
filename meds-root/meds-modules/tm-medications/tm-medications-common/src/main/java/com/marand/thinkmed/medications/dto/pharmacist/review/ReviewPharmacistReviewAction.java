package com.marand.thinkmed.medications.dto.pharmacist.review;

import com.marand.thinkmed.medications.ehr.model.pharmacist.ReferralResponseType;

/**
 * @author nejck
 */
public enum ReviewPharmacistReviewAction
{
  ACCEPTED, DENIED, MODIFIED, REISSUED, COPIED, ABORTED;

  public static ReferralResponseType mapToReferralResponseType(final ReviewPharmacistReviewAction reviewAction)
  {
    if (reviewAction == MODIFIED || reviewAction == ABORTED)
    {
      return ReferralResponseType.PARTIALLY_ACCEPTED;
    }
    else if (reviewAction == ACCEPTED)
    {
      return ReferralResponseType.ACCEPTED_IN_FULL;
    }
    else
    {
      return ReferralResponseType.REJECTED;
    }
  }
}
