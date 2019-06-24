package com.marand.thinkmed.medications.dto.pharmacist.review;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PharmacistReviewsDto extends DataTransferObject
{
  private DateTime lastTaskChangeTimestamp; //if null task does not exist
  private List<PharmacistReviewDto> pharmacistReviews = new ArrayList<>();

  public DateTime getLastTaskChangeTimestamp()
  {
    return lastTaskChangeTimestamp;
  }

  public void setLastTaskChangeTimestamp(final DateTime lastTaskChangeTimestamp)
  {
    this.lastTaskChangeTimestamp = lastTaskChangeTimestamp;
  }

  public List<PharmacistReviewDto> getPharmacistReviews()
  {
    return pharmacistReviews;
  }

  public void setPharmacistReviews(final List<PharmacistReviewDto> pharmacistReviews)
  {
    this.pharmacistReviews = pharmacistReviews;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("lastTaskChangeTimestamp", lastTaskChangeTimestamp)
        .append("pharmacistReviews", pharmacistReviews)
    ;
  }
}
