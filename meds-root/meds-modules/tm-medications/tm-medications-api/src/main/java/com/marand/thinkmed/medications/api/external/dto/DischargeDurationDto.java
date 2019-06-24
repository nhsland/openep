package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeDurationDto
{
  private String display;
  private Integer value;
  private String unit;

  public DischargeDurationDto(final Integer value, final String unit)
  {
    display = value + " " + unit;
    this.value = value;
    this.unit = unit;
  }

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public Integer getValue()
  {
    return value;
  }

  public void setValue(final Integer value)
  {
    this.value = value;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }
}
