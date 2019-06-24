package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Primoz Prislan
 */
public class SimpleMedicationOrderDoseDto extends DataTransferObject
{
  private String dose;

  public SimpleMedicationOrderDoseDto()
  {
  }

  public SimpleMedicationOrderDoseDto(final String dose)
  {
    this();
    this.dose = dose;
  }

  public String getDose()
  {
    return dose;
  }

  public void setDose(final String dose)
  {
    this.dose = dose;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("dose", dose)
    ;
  }
}
