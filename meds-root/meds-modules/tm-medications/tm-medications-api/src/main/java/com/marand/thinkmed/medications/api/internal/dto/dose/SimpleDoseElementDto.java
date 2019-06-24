package com.marand.thinkmed.medications.api.internal.dto.dose;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class SimpleDoseElementDto extends DataTransferObject implements JsonSerializable
{
  private Double quantity;
  private String doseDescription;
  private Double quantityDenominator;
  private DoseRangeDto doseRange;

  public Double getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Double quantity)
  {
    this.quantity = quantity;
  }

  public Double getQuantityDenominator()
  {
    return quantityDenominator;
  }

  public void setQuantityDenominator(final Double quantityDenominator)
  {
    this.quantityDenominator = quantityDenominator;
  }

  public String getDoseDescription()
  {
    return doseDescription;
  }

  public void setDoseDescription(final String doseDescription)
  {
    this.doseDescription = doseDescription;
  }

  public DoseRangeDto getDoseRange()
  {
    return doseRange;
  }

  public void setDoseRange(final DoseRangeDto doseRange)
  {
    this.doseRange = doseRange;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("quantity", quantity)
        .append("doseDescription", doseDescription)
        .append("quantityDenominator", quantityDenominator)
        .append("doseRange", doseRange)
    ;
  }
}
