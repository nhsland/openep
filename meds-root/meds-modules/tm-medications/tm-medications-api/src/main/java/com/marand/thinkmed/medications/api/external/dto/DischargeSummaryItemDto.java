package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DischargeSummaryItemDto
{
  private String id;
  private PrescriptionDto prescription;
  private DischargeDetailsDto discharge;
  private DischargeSummaryStatusEnum status;
  private DischargeSummaryStatusReasonDto statusReason;

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

  public DischargeSummaryStatusEnum getStatus()
  {
    return status;
  }

  public void setStatus(final DischargeSummaryStatusEnum status)
  {
    this.status = status;
  }

  public DischargeSummaryStatusReasonDto getStatusReason()
  {
    return statusReason;
  }

  public void setStatusReason(final DischargeSummaryStatusReasonDto statusReason)
  {
    this.statusReason = statusReason;
  }
}
