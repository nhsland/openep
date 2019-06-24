package com.marand.thinkmed.medications.dto.report;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class TherapyReportHourDoseTimeDto extends DataTransferObject
{
  private HourMinuteDto time;
  private String doseTimeDisplay;

  public TherapyReportHourDoseTimeDto(final HourMinuteDto time, final String doseTimeDisplay)
  {
    this.time = time;
    this.doseTimeDisplay = doseTimeDisplay;
  }

  public HourMinuteDto getTime()
  {
    return time;
  }

  public void setTime(final HourMinuteDto time)
  {
    this.time = time;
  }

  public String getDoseTimeDisplay()
  {
    return doseTimeDisplay;
  }

  public void setDoseTimeDisplay(final String doseTimeDisplay)
  {
    this.doseTimeDisplay = doseTimeDisplay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("time", time)
        .append("doseTimeDisplay", doseTimeDisplay);
  }
}
