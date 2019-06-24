package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class InfusionRateCalculationDto extends DataTransferObject
{
  private Double quantity;
  private String quantityUnit;
  private Double quantityDenominator;  //in ml

  public Double getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Double quantity)
  {
    this.quantity = quantity;
  }

  public String getQuantityUnit()
  {
    return quantityUnit;
  }

  public void setQuantityUnit(final String quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  }

  public Double getQuantityDenominator()
  {
    return quantityDenominator;
  }

  public void setQuantityDenominator(final Double quantityDenominator)
  {
    this.quantityDenominator = quantityDenominator;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("quantity", quantity)
        .append("quantityUnit", quantityUnit)
        .append("quantityDenominator", quantityDenominator);
  }
}
