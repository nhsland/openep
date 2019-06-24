package com.marand.thinkmed.medications.api.internal.dto;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public abstract class SimpleTherapyDto extends TherapyDto
{
  private MedicationDto medication;
  private String quantityUnit;
  private DoseFormDto doseForm;
  private String quantityDenominatorUnit;
  private Double targetInr;

  private String quantityDisplay;

  protected SimpleTherapyDto(final boolean variable)
  {
    super(MedicationOrderFormType.SIMPLE, variable);
  }

  public MedicationDto getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationDto medication)
  {
    this.medication = medication;
  }

  public String getQuantityUnit()
  {
    return quantityUnit;
  }

  public void setQuantityUnit(final String quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  }

  public DoseFormDto getDoseForm()
  {
    return doseForm;
  }

  public void setDoseForm(final DoseFormDto doseForm)
  {
    this.doseForm = doseForm;
  }

  public String getQuantityDenominatorUnit()
  {
    return quantityDenominatorUnit;
  }

  public void setQuantityDenominatorUnit(final String quantityDenominatorUnit)
  {
    this.quantityDenominatorUnit = quantityDenominatorUnit;
  }

  public String getQuantityDisplay()
  {
    return quantityDisplay;
  }

  public void setQuantityDisplay(final String quantityDisplay)
  {
    this.quantityDisplay = quantityDisplay;
  }

  public Double getTargetInr()
  {
    return targetInr;
  }

  public void setTargetInr(final Double targetInr)
  {
    this.targetInr = targetInr;
  }

  @Override
  public boolean isNormalInfusion()
  {
    return false;
  }

  @Override
  public List<MedicationDto> getMedications()
  {
    return medication == null ? Collections.emptyList() : Collections.singletonList(medication);
  }

  @Override
  public Long getMainMedicationId()
  {
    return medication == null ? null : medication.getId();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("medication", medication)
        .append("quantityUnit", quantityUnit)
        .append("doseForm", doseForm)
        .append("quantityDenominatorUnit", quantityDenominatorUnit)
        .append("quantityDisplay", quantityDisplay)
        .append("targetInr", targetInr);
  }
}
