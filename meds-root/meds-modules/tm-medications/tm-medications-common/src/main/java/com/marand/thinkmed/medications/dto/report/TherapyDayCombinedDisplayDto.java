package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TherapyDayCombinedDisplayDto extends DataTransferObject
{
  private TherapyDto order;
  private String therapyStart;
  private String therapyEnd;
  private String currentRate;
  private String currentOxygenDevice;
  private TherapyPharmacistReviewStatusEnum pharmacistsReviewState;
  private TherapyReportStatusEnum therapyReportStatusEnum;
  private String therapyConsecutiveDay;
  private boolean showTherapyConsecutiveDay;
  private String consecutiveDayLabel;

  protected TherapyDayCombinedDisplayDto() { }

  public TherapyDayCombinedDisplayDto(
      final TherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final String currentRate,
      final TherapyPharmacistReviewStatusEnum pharmacistsReviewState,
      final TherapyReportStatusEnum therapyReportStatusEnum,
      final String therapyConsecutiveDay,
      final boolean showTherapyConsecutiveDay,
      final String consecutiveDayLabel,
      final String currentOxygenDevice)
  {
    this.order = order;
    this.therapyStart = therapyStart;
    this.therapyEnd = therapyEnd;
    this.currentRate = currentRate;
    this.pharmacistsReviewState = pharmacistsReviewState;
    this.therapyReportStatusEnum = therapyReportStatusEnum;
    this.therapyConsecutiveDay = therapyConsecutiveDay;
    this.showTherapyConsecutiveDay = showTherapyConsecutiveDay;
    this.consecutiveDayLabel = consecutiveDayLabel;
    this.currentOxygenDevice = currentOxygenDevice;
  }

  public String getConsecutiveDayLabel()
  {
    return consecutiveDayLabel;
  }

  public void setConsecutiveDayLabel(final String consecutiveDayLabel)
  {
    this.consecutiveDayLabel = consecutiveDayLabel;
  }

  public boolean isShowTherapyConsecutiveDay()
  {
    return showTherapyConsecutiveDay;
  }

  public void setShowTherapyConsecutiveDay(final boolean showTherapyConsecutiveDay)
  {
    this.showTherapyConsecutiveDay = showTherapyConsecutiveDay;
  }

  public TherapyDto getOrder()
  {
    return order;
  }

  public void setOrder(final TherapyDto order)
  {
    this.order = order;
  }

  public String getTherapyStart()
  {
    return therapyStart;
  }

  public void setTherapyStart(final String therapyStart)
  {
    this.therapyStart = therapyStart;
  }

  public String getTherapyEnd()
  {
    return therapyEnd;
  }

  public void setTherapyEnd(final String therapyEnd)
  {
    this.therapyEnd = therapyEnd;
  }

  public String getCurrentRate()
  {
    return currentRate;
  }

  public void setCurrentRate(final String currentRate)
  {
    this.currentRate = currentRate;
  }

  public String getCurrentOxygenDevice()
  {
    return currentOxygenDevice;
  }

  public void setCurrentOxygenDevice(final String currentOxygenDevice)
  {
    this.currentOxygenDevice = currentOxygenDevice;
  }

  public TherapyPharmacistReviewStatusEnum getPharmacistsReviewState()
  {
    return pharmacistsReviewState;
  }

  public void setPharmacistsReviewState(final TherapyPharmacistReviewStatusEnum pharmacistsReviewState)
  {
    this.pharmacistsReviewState = pharmacistsReviewState;
  }

  public TherapyReportStatusEnum getTherapyReportStatusEnum()
  {
    return therapyReportStatusEnum;
  }

  public void setTherapyReportStatusEnum(final TherapyReportStatusEnum therapyReportStatusEnum)
  {
    this.therapyReportStatusEnum = therapyReportStatusEnum;
  }

  public String getTherapyConsecutiveDay()
  {
    return therapyConsecutiveDay;
  }

  public void setTherapyConsecutiveDay(final String therapyConsecutiveDay)
  {
    this.therapyConsecutiveDay = therapyConsecutiveDay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("order", order)
        .append("therapyStart", therapyStart)
        .append("therapyEnd", therapyEnd)
        .append("currentRate", currentRate)
        .append("currentOxygenDevice", currentOxygenDevice)
        .append("pharmacistsReviewState", pharmacistsReviewState)
        .append("therapyReportStatusEnum", therapyReportStatusEnum)
        .append("therapyConsecutiveDay", therapyConsecutiveDay)
        .append("consecutiveDayLabel", consecutiveDayLabel)
    ;
  }
}
