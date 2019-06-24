package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class DispenseDetailsDto extends DataTransferObject implements JsonSerializable
{
  private Integer daysDuration;
  private Integer quantity;
  private String unit;
  private NamedIdDto dispenseSource;
  private List<ControlledDrugSupplyDto> controlledDrugSupply = new ArrayList<>();

  public Integer getDaysDuration()
  {
    return daysDuration;
  }

  public void setDaysDuration(final Integer daysDuration)
  {
    this.daysDuration = daysDuration;
  }

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

  public NamedIdDto getDispenseSource()
  {
    return dispenseSource;
  }

  public void setDispenseSource(final NamedIdDto dispenseSource)
  {
    this.dispenseSource = dispenseSource;
  }

  public List<ControlledDrugSupplyDto> getControlledDrugSupply()
  {
    return controlledDrugSupply;
  }

  public void setControlledDrugSupply(final List<ControlledDrugSupplyDto> controlledDrugSupply)
  {
    this.controlledDrugSupply = controlledDrugSupply;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("daysDuration", daysDuration)
        .append("quantity", quantity)
        .append("unit", unit)
        .append("dispenseSource", dispenseSource)
        .append("controlledDrugSupply", controlledDrugSupply)
    ;
  }
}
