package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */
public class PrescriptionForPrintoutDto extends DataTransferObject
{
  private String prescriptionId;
  private String medicationName;

  public String getPrescriptionId()
  {
    return prescriptionId;
  }

  public void setPrescriptionId(final String prescriptionId)
  {
    this.prescriptionId = prescriptionId;
  }

  public String getMedicationName()
  {
    return medicationName;
  }

  public void setMedicationName(final String medicationName)
  {
    this.medicationName = medicationName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionId", prescriptionId)
        .append("medicationName", medicationName)
    ;
  }
}
