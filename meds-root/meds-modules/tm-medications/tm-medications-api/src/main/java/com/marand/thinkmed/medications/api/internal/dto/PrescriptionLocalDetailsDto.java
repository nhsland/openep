package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;

/**
 * @author Mitja Lapajne
 */
public abstract class PrescriptionLocalDetailsDto extends DataTransferObject
{
  private String prescriptionSystem;

  public void setPrescriptionSystem(final String prescriptionSystem)
  {
    this.prescriptionSystem = prescriptionSystem;
  }

  public abstract String getPrescriptionSystem();
}
