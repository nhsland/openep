package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeListItemDto
{
  private String id;
  private PrescriptionDto prescription;
  private DischargeDetailsDto discharge;

  public String getId()
  {
    return id;
  }

  public void setId(final String id)
  {
    this.id = id;
  }

  public PrescriptionDto getPrescription()
  {
    return prescription;
  }

  public void setPrescription(final PrescriptionDto prescription)
  {
    this.prescription = prescription;
  }

  public DischargeDetailsDto getDischarge()
  {
    return discharge;
  }

  public void setDischarge(final DischargeDetailsDto discharge)
  {
    this.discharge = discharge;
  }
}
