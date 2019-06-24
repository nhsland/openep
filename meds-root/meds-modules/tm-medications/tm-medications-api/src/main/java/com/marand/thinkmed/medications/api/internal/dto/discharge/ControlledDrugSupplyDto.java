package com.marand.thinkmed.medications.api.internal.dto.discharge;

import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class ControlledDrugSupplyDto extends DataTransferObject implements JsonSerializable
{
  private Integer quantity;
  private String unit;
  private NamedIdDto medication;

  public Integer getQuantity()
  {
    return quantity;
  }

  public void setQuantity(final Integer quantity)
  {
    this.quantity = quantity;
  }

  public String getUnit()
  {
    return unit;
  }

  public void setUnit(final String unit)
  {
    this.unit = unit;
  }

  public NamedIdDto getMedication()
  {
    return medication;
  }

  public void setMedication(final NamedIdDto medication)
  {
    this.medication = medication;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("quantity", quantity)
        .append("unit", unit)
        .append("medication", medication)
    ;
  }
}
