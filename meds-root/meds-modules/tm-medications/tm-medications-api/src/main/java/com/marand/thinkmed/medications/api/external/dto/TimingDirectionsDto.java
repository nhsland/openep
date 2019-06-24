package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class TimingDirectionsDto
{
  private String display;

  public TimingDirectionsDto(final String display)
  {
    this.display = display;
  }

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }
}
