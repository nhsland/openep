package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class TherapySurgeryReportElementDto extends DataTransferObject
{
  private String timeDose;
  private String date;
  private final TherapyDto therapy;
  private String therapyStart;
  private String therapyEnd;
  private String currentRate;
  private TherapyReportStatusEnum therapyReportStatusEnum;
  private String therapyConsecutiveDay;
  private boolean showTherapyConsecutiveDay;
  private String consecutiveDayLabel;

  public TherapySurgeryReportElementDto(
      final String timeDose,
      final String date,
      final TherapyDto therapy,
      final String therapyStart,
      final String therapyEnd,
      final String currentRate,
      final TherapyReportStatusEnum therapyReportStatusEnum,
      final String therapyConsecutiveDay,
      final boolean showTherapyConsecutiveDay,
      final String consecutiveDayLabel)
  {
    this.timeDose = timeDose;
    this.date = date;
    this.therapy = therapy;
    this.therapyStart = therapyStart;
    this.therapyEnd = therapyEnd;
    this.currentRate = currentRate;
    this.therapyReportStatusEnum = therapyReportStatusEnum;
    this.therapyConsecutiveDay = therapyConsecutiveDay;
    this.showTherapyConsecutiveDay = showTherapyConsecutiveDay;
    this.consecutiveDayLabel = consecutiveDayLabel;
  }

  public String getConsecutiveDayLabel()
  {
    return consecutiveDayLabel;
  }

  public void setConsecutiveDayLabel(final String consecutiveDayLabel)
  {
    this.consecutiveDayLabel = consecutiveDayLabel;
  }

  public String getTimeDose()
  {
    return timeDose;
  }

  public void setTimeDose(final String timeDose)
  {
    this.timeDose = timeDose;
  }

  public String getDate()
  {
    return date;
  }

  public void setDate(final String date)
  {
    this.date = date;
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

  public boolean isShowTherapyConsecutiveDay()
  {
    return showTherapyConsecutiveDay;
  }

  public void setShowTherapyConsecutiveDay(final boolean showTherapyConsecutiveDay)
  {
    this.showTherapyConsecutiveDay = showTherapyConsecutiveDay;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("timeDose", timeDose)
        .append("date", date)
        .append("therapy", therapy)
        .append("therapyStart", therapyStart)
        .append("therapyEnd", therapyEnd)
        .append("currentRate", currentRate)
        .append("therapyReportStatusEnum", therapyReportStatusEnum)
        .append("therapyConsecutiveDay", therapyConsecutiveDay)
        .append("showTherapyConsecutiveDay", showTherapyConsecutiveDay)
        .append("consecutiveDayLabel", consecutiveDayLabel)
    ;

  }
}