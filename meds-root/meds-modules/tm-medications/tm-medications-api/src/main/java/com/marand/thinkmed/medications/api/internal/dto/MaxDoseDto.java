package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class MaxDoseDto extends DataTransferObject implements JsonSerializable
{
  private Integer dose;
  private String unit;
  private MaxDosePeriod period;

  public Integer getDose()
  {
    return dose;
  }

  public void setDose(final Integer dose)
  {
    this.dose = dose;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }

  public MaxDosePeriod getPeriod()
  {
    return period;
  }

  public void setPeriod(final MaxDosePeriod period)
  {
    this.period = period;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("maxDose", dose)
        .append("maxDoseUnit", unit)
        .append("maxDosePeriod", period)
    ;
  }
}