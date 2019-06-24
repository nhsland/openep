package com.marand.thinkmed.medications.dto;

import com.marand.maf.core.data.object.NamedIdDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class FormularyMedicationDto extends NamedIdDto
{
  private boolean formulary;
  private String supplyUnit;

  public boolean isFormulary()
  {
    return formulary;
  }

  public void setFormulary(final boolean formulary)
  {
    this.formulary = formulary;
  }

  public String getSupplyUnit()
  {
    return supplyUnit;
  }

  public void setSupplyUnit(final String supplyUnit)
  {
    this.supplyUnit = supplyUnit;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("formulary", formulary)
        .append("supplyUnit", supplyUnit);
  }
}
