package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Miha Anzicek
 */
public class PrescriptionDto extends DataTransferObject
{
  private String prescriptionId;
  private String dispenseId;
  private OutpatientPrescriptionStatus status;
  private Long remainingRepeats;
  private DateTime validityDate;

  public String getPrescriptionId()
  {
    return prescriptionId;
  }

  public void setPrescriptionId(final String prescriptionId)
  {
    this.prescriptionId = prescriptionId;
  }

  public String getDispenseId()
  {
    return dispenseId;
  }

  public void setDispenseId(final String dispenseId)
  {
    this.dispenseId = dispenseId;
  }

  public OutpatientPrescriptionStatus getStatus()
  {
    return status;
  }

  public void setStatus(final OutpatientPrescriptionStatus status)
  {
    this.status = status;
  }

  public Long getRemainingRepeats()
  {
    return remainingRepeats;
  }

  public void setRemainingRepeats(final Long remainingRepeats)
  {
    this.remainingRepeats = remainingRepeats;
  }

  public DateTime getValidityDate()
  {
    return validityDate;
  }

  public void setValidityDate(final DateTime validityDate)
  {
    this.validityDate = validityDate;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("prescriptionId", prescriptionId)
        .append("dispenseId", dispenseId)
        .append("status", status)
        .append("remainingRepeats", remainingRepeats)
        .append("validityDate", validityDate)
    ;
  }
}
