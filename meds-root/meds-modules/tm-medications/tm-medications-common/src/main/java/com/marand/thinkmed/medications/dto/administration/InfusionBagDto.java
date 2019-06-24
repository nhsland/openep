package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class InfusionBagDto extends DataTransferObject implements JsonSerializable
{
  private final Double quantity;
  private final String unit;

  public InfusionBagDto(final Double quantity, final String unit)
  {
    this.quantity = quantity;
    this.unit = unit;
  }

  public Double getQuantity()
  {
    return quantity;
  }

  public String getUnit()
  {
    return unit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("quantity", quantity)
        .append("unit", unit)
    ;
  }
}
