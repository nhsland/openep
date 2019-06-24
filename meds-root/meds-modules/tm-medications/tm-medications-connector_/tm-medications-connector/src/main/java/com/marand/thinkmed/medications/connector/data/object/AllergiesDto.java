package com.marand.thinkmed.medications.connector.data.object;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class AllergiesDto extends DataTransferObject implements JsonSerializable
{
  private DateTime reviewDate;
  private AllergiesStatus allergiesStatus;
  private final List<IdNameDto> allergens = new ArrayList<>();

  public AllergiesDto(final AllergiesStatus allergiesStatus)
  {
    this.allergiesStatus = allergiesStatus;
  }

  public AllergiesDto() { }

  public List<IdNameDto> getAllergens()
  {
    return allergens;
  }

  public DateTime getReviewDate()
  {
    return reviewDate;
  }

  public void setReviewDate(final DateTime reviewDate)
  {
    this.reviewDate = reviewDate;
  }

  public AllergiesStatus getAllergiesStatus()
  {
    return allergiesStatus;
  }

  public void setAllergiesStatus(final AllergiesStatus allergiesStatus)
  {
    this.allergiesStatus = allergiesStatus;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("reviewDate", reviewDate)
        .append("status", allergiesStatus)
        .append("allergies", allergens);
  }
}
