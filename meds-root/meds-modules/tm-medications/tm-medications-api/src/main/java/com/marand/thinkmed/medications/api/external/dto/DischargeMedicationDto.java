package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeMedicationDto
{
  private String display;

  public DischargeMedicationDto(final String display)
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
