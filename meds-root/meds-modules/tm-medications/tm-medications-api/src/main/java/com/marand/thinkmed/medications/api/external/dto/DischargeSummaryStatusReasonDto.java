package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeSummaryStatusReasonDto
{
  private String display;

  public DischargeSummaryStatusReasonDto(final String display)
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
