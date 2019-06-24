package com.marand.thinkmed.medications.dto.template;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyTemplatePreconditionEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplatePreconditionDto extends DataTransferObject implements JsonSerializable
{
  private TherapyTemplatePreconditionEnum precondition;
  private Double minValue;
  private Double maxValue;
  private String exactValue;

  public TherapyTemplatePreconditionEnum getPrecondition()
  {
    return precondition;
  }

  public void setPrecondition(final TherapyTemplatePreconditionEnum precondition)
  {
    this.precondition = precondition;
  }

  public Double getMinValue()
  {
    return minValue;
  }

  public void setMinValue(final Double minValue)
  {
    this.minValue = minValue;
  }

  public Double getMaxValue()
  {
    return maxValue;
  }

  public void setMaxValue(final Double maxValue)
  {
    this.maxValue = maxValue;
  }

  public String getExactValue()
  {
    return exactValue;
  }

  public void setExactValue(final String exactValue)
  {
    this.exactValue = exactValue;
  }

  public boolean isRangePreconditionMet(final Double value)
  {
    final boolean minPreconditionMet = minValue == null || minValue <= value;
    final boolean maxPreconditionMet = maxValue == null || maxValue > value;

    return minPreconditionMet && maxPreconditionMet;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("precondition", precondition)
        .append("minValue", minValue)
        .append("maxValue", maxValue)
        .append("exactValue", exactValue);
  }
}
