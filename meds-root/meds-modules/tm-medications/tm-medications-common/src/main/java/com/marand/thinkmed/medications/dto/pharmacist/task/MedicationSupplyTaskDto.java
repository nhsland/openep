package com.marand.thinkmed.medications.dto.pharmacist.task;

import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Klavdij Lapajne
 */

public class MedicationSupplyTaskDto extends PatientTaskDto
{
  private MedicationSupplyTypeEnum supplyTypeEnum;
  private Integer supplyInDays;
  private TherapyDayDto therapyDayDto;
  private DateTime createdDateTime;
  private DateTime closedDateTime;

  public MedicationSupplyTypeEnum getSupplyTypeEnum()
  {
    return supplyTypeEnum;
  }

  public void setSupplyTypeEnum(final MedicationSupplyTypeEnum supplyTypeEnum)
  {
    this.supplyTypeEnum = supplyTypeEnum;
  }

  public Integer getSupplyInDays()
  {
    return supplyInDays;
  }

  public void setSupplyInDays(final Integer supplyInDays)
  {
    this.supplyInDays = supplyInDays;
  }

  public TherapyDayDto getTherapyDayDto()
  {
    return therapyDayDto;
  }

  public void setTherapyDayDto(final TherapyDayDto therapyDayDto)
  {
    this.therapyDayDto = therapyDayDto;
  }

  public DateTime getCreatedDateTime()
  {
    return createdDateTime;
  }

  public void setCreatedDateTime(final DateTime createdDateTime)
  {
    this.createdDateTime = createdDateTime;
  }

  public DateTime getClosedDateTime()
  {
    return closedDateTime;
  }

  public void setClosedDateTime(final DateTime closedDateTime)
  {
    this.closedDateTime = closedDateTime;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("supplyTypeEnum", supplyTypeEnum)
        .append("supplyInDays", supplyInDays)
        .append("createdDateTime", createdDateTime)
        .append("closedDateTime", closedDateTime)
        .append("therapyDayDto", therapyDayDto);
  }
}
