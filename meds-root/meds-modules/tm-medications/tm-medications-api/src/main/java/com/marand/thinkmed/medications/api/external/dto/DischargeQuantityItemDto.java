package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeQuantityItemDto
{
  private String display;
  private DischargeMedicationDto medication;
  private DischargeQuantityDto quantity;

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public DischargeMedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final DischargeMedicationDto medication)
  {
    this.medication = medication;
  }

  public DischargeQuantityDto getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final DischargeQuantityDto quantity)
  {
    this.quantity = quantity;
  }
}
