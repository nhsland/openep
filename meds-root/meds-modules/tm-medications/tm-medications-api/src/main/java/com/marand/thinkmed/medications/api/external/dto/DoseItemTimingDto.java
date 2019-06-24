package com.marand.thinkmed.medications.api.external.dto;

import java.time.LocalTime;

/**
 * @author Mitja Lapajne
 */
public class DoseItemTimingDto
{
  private String display;
  private LocalTime localTime;
  private String descriptiveTiming;

  public DoseItemTimingDto(final String display, final LocalTime localTime, final String descriptiveTiming)
  {
    this.display = display;
    this.localTime = localTime;
    this.descriptiveTiming = descriptiveTiming;
  }

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public LocalTime getLocalTime()
  {
    return localTime;
  }

  public void setLocalTime(final LocalTime localTime)
  {
    this.localTime = localTime;
  }

  public String getDescriptiveTiming()
  {
    return descriptiveTiming;
  }

  public void setDescriptiveTiming(final String descriptiveTiming)
  {
    this.descriptiveTiming = descriptiveTiming;
  }
}
