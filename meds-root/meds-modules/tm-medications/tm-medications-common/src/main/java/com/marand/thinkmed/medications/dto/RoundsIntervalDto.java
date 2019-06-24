package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class RoundsIntervalDto extends DataTransferObject
{
  private int startHour;
  private int startMinute;
  private int endHour;
  private int endMinute;

  public int getStartHour()
  {
    return startHour;
  }

  public void setStartHour(final int startHour)
  {
    this.startHour = startHour;
  }

  public int getStartMinute()
  {
    return startMinute;
  }

  public void setStartMinute(final int startMinute)
  {
    this.startMinute = startMinute;
  }

  public int getEndHour()
  {
    return endHour;
  }

  public void setEndHour(final int endHour)
  {
    this.endHour = endHour;
  }

  public int getEndMinute()
  {
    return endMinute;
  }

  public void setEndMinute(final int endMinute)
  {
    this.endMinute = endMinute;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("startHour", startHour)
        .append("startMinute", startMinute)
        .append("endHour", endHour)
        .append("endMinute", endMinute);
  }
}
