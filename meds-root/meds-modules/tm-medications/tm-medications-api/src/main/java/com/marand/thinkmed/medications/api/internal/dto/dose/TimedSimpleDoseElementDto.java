package com.marand.thinkmed.medications.api.internal.dto.dose;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class TimedSimpleDoseElementDto extends DataTransferObject implements JsonSerializable
{
  private SimpleDoseElementDto doseElement;
  private HourMinuteDto doseTime;
  private DateTime date; //null means every day
  private String timingDescription; //if dosing frequency and timing are not structured (discharge protocol)

  private String timeDisplay;
  private String quantityDisplay;

  public SimpleDoseElementDto getDoseElement()
  {
    return doseElement;
  }

  public void setDoseElement(final SimpleDoseElementDto doseElement)
  {
    this.doseElement = doseElement;
  }

  public HourMinuteDto getDoseTime()
  {
    return doseTime;
  }

  public void setDoseTime(final HourMinuteDto doseTime)
  {
    this.doseTime = doseTime;
  }

  public String getTimeDisplay()
  {
    return timeDisplay;
  }

  public void setTimeDisplay(final String timeDisplay)
  {
    this.timeDisplay = timeDisplay;
  }

  public String getQuantityDisplay()
  {
    return quantityDisplay;
  }

  public void setQuantityDisplay(final String quantityDisplay)
  {
    this.quantityDisplay = quantityDisplay;
  }

  public DateTime getDate()
  {
    return date;
  }

  public void setDate(final DateTime date)
  {
    this.date = date;
  }

  public String getTimingDescription()
  {
    return timingDescription;
  }

  public void setTimingDescription(final String timingDescription)
  {
    this.timingDescription = timingDescription;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("doseElement", doseElement)
        .append("doseTime", doseTime)
        .append("timeDisplay", timeDisplay)
        .append("quantityDisplay", quantityDisplay)
        .append("date", date)
        .append("timingDescription", timingDescription)
    ;
  }
}
