package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class InfusionIngredientDto extends DataTransferObject implements JsonSerializable
{
  private MedicationDto medication;
  private Double quantity;
  private String quantityUnit;
  private Double quantityDenominator;
  private String quantityDenominatorUnit;
  private DoseFormDto doseForm;

  private String quantityDisplay;

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

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

  public String getQuantityDenominatorUnit()
  {
    return quantityDenominatorUnit;
  }

  public void setQuantityDenominatorUnit(final String quantityDenominatorUnit)
  {
    this.quantityDenominatorUnit = quantityDenominatorUnit;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public String getQuantityDisplay()
  {
    return quantityDisplay;
  }

  public void setQuantityDisplay(final String quantityDisplay)
  {
    this.quantityDisplay = quantityDisplay;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("medication", medication)
        .append("quantity", quantity)
        .append("quantityUnit", quantityUnit)
        .append("quantityDenominator", quantityDenominator)
        .append("quantityDenominatorUnit", quantityDenominatorUnit)
        .append("doseForm", doseForm)
        .append("quantityDisplay", quantityDisplay);
  }
}