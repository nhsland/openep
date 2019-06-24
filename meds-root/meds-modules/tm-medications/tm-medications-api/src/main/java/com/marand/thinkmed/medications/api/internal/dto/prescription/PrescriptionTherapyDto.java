package com.marand.thinkmed.medications.api.internal.dto.prescription;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class PrescriptionTherapyDto extends DataTransferObject implements JsonSerializable
{
  private String prescriptionTherapyId;
  private OutpatientPrescriptionStatus prescriptionStatus;
  private TherapyDto therapy;

  public String getPrescriptionTherapyId()
  {
    return prescriptionTherapyId;
  }

  public void setPrescriptionTherapyId(final String prescriptionTherapyId)
  {
    this.prescriptionTherapyId = prescriptionTherapyId;
  }

  public OutpatientPrescriptionStatus getPrescriptionStatus()
  {
    return prescriptionStatus;
  }

  public void setPrescriptionStatus(final OutpatientPrescriptionStatus prescriptionStatus)
  {
    this.prescriptionStatus = prescriptionStatus;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionTherapyId", prescriptionTherapyId)
        .append("prescriptionStatus", prescriptionStatus)
        .append("therapy", therapy);
  }
}
