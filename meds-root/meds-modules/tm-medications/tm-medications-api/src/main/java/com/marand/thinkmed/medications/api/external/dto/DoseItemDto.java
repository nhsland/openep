package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DoseItemDto
{
  private String display;
  private DoseItemQuantityDto quantity;
  private DoseItemTimingDto timing;

  public DoseItemDto(final String display)
  {
    this.display = display;
  }

  public DoseItemDto(
      final String display,
      final DoseItemQuantityDto quantity,
      final DoseItemTimingDto timing)
  {
    this.display = display;
    this.quantity = quantity;
    this.timing = timing;
  }

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public DoseItemQuantityDto getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final DoseItemQuantityDto quantity)
  {
    this.quantity = quantity;
  }

  public DoseItemTimingDto getTiming()
  {
    return timing;
  }

  public void setTiming(final DoseItemTimingDto timing)
  {
    this.timing = timing;
  }
}
