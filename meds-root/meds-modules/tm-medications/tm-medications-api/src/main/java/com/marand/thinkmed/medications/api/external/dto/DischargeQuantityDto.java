package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeQuantityDto
{
  private Integer value;
  private String unit;
  private String display;
  private String textValue;

  public DischargeQuantityDto(final Integer value, final String unit, final String display, final String textValue)
  {
    this.value = value;
    this.unit = unit;
    this.display = display;
    this.textValue = textValue;
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

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public String getTextValue()
  {
    return textValue;
  }

  public void setTextValue(final String textValue)
  {
    this.textValue = textValue;
  }
}
