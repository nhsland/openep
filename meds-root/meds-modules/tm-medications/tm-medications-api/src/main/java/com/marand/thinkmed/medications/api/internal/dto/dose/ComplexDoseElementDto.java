package com.marand.thinkmed.medications.api.internal.dto.dose;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class ComplexDoseElementDto extends DataTransferObject implements JsonSerializable
{
  private Integer duration; //in minutes
  private Double rate;
  private String rateUnit;
  private Double rateFormula;
  private String rateFormulaUnit;

  public Integer getDuration()
  {
    return duration;
  }

  public void setDuration(final Integer duration)
  {
    this.duration = duration;
  }

  public Double getRate()
  {
    return rate;
  }

  public void setRate(final Double rate)
  {
    this.rate = rate;
  }

  public String getRateUnit()
  {
    return rateUnit;
  }

  public void setRateUnit(final String rateUnit)
  {
    this.rateUnit = rateUnit;
  }

  public Double getRateFormula()
  {
    return rateFormula;
  }

  public void setRateFormula(final Double rateFormula)
  {
    this.rateFormula = rateFormula;
  }

  public String getRateFormulaUnit()
  {
    return rateFormulaUnit;
  }

  public void setRateFormulaUnit(final String rateFormulaUnit)
  {
    this.rateFormulaUnit = rateFormulaUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("duration", duration)
        .append("rate", rate)
        .append("rateUnit", rateUnit)
        .append("rateFormula", rateFormula)
        .append("rateFormulaUnit", rateFormulaUnit)
    ;
  }
}
