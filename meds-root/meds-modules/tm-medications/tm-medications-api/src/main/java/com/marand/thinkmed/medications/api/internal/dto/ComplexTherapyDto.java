package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public abstract class ComplexTherapyDto extends TherapyDto
{
  private List<InfusionIngredientDto> ingredientsList = new ArrayList<>();
  private boolean continuousInfusion;
  private MedicationSiteDto site;
  private Double volumeSum;
  private String volumeSumUnit;
  private String additionalInstruction; //HEPARIN
  private boolean baselineInfusion;
  private boolean adjustToFluidBalance;

  private String volumeSumDisplay;
  private String speedDisplay;
  private String speedFormulaDisplay;
  private String additionalInstructionDisplay;
  private String baselineInfusionDisplay;

  protected ComplexTherapyDto(final boolean variable)
  {
    super(MedicationOrderFormType.COMPLEX, variable);
  }

  public List<InfusionIngredientDto> getIngredientsList()
  {
    return ingredientsList;
  }

  public void setIngredientsList(final List<InfusionIngredientDto> ingredientsList)
  {
    this.ingredientsList = ingredientsList;
  }

  public boolean isContinuousInfusion()
  {
    return continuousInfusion;
  }

  public void setContinuousInfusion(final boolean continuousInfusion)
  {
    this.continuousInfusion = continuousInfusion;
  }

  public MedicationSiteDto getSite()
  {
    return site;
  }

  public void setSite(final MedicationSiteDto site)
  {
    this.site = site;
  }

  public Double getVolumeSum()
  {
    return volumeSum;
  }

  public void setVolumeSum(final Double volumeSum)
  {
    this.volumeSum = volumeSum;
  }

  public String getVolumeSumUnit()
  {
    return volumeSumUnit;
  }

  public void setVolumeSumUnit(final String volumeSumUnit)
  {
    this.volumeSumUnit = volumeSumUnit;
  }

  public String getAdditionalInstruction()
  {
    return additionalInstruction;
  }

  public void setAdditionalInstruction(final String additionalInstruction)
  {
    this.additionalInstruction = additionalInstruction;
  }

  public boolean isBaselineInfusion()
  {
    return baselineInfusion;
  }

  public void setBaselineInfusion(final boolean baselineInfusion)
  {
    this.baselineInfusion = baselineInfusion;
  }

  public boolean isAdjustToFluidBalance()
  {
    return adjustToFluidBalance;
  }

  public void setAdjustToFluidBalance(final boolean adjustToFluidBalance)
  {
    this.adjustToFluidBalance = adjustToFluidBalance;
  }

  public String getVolumeSumDisplay()
  {
    return volumeSumDisplay;
  }

  public void setVolumeSumDisplay(final String volumeSumDisplay)
  {
    this.volumeSumDisplay = volumeSumDisplay;
  }

  public String getSpeedDisplay()
  {
    return speedDisplay;
  }

  public void setSpeedDisplay(final String speedDisplay)
  {
    this.speedDisplay = speedDisplay;
  }

  public String getSpeedFormulaDisplay()
  {
    return speedFormulaDisplay;
  }

  public void setSpeedFormulaDisplay(final String speedFormulaDisplay)
  {
    this.speedFormulaDisplay = speedFormulaDisplay;
  }

  public String getAdditionalInstructionDisplay()
  {
    return additionalInstructionDisplay;
  }

  public void setAdditionalInstructionDisplay(final String additionalInstructionDisplay)
  {
    this.additionalInstructionDisplay = additionalInstructionDisplay;
  }

  public String getBaselineInfusionDisplay()
  {
    return baselineInfusionDisplay;
  }

  public void setBaselineInfusionDisplay(final String baselineInfusionDisplay)
  {
    this.baselineInfusionDisplay = baselineInfusionDisplay;
  }

  @Override
  public boolean isNormalInfusion()
  {
    return isWithRate() && !continuousInfusion;
  }

  @Override
  public List<MedicationDto> getMedications()
  {
    return ingredientsList.stream().map(InfusionIngredientDto::getMedication).collect(Collectors.toList());
  }

  @Override
  public Long getMainMedicationId()
  {
    return ingredientsList.isEmpty() ? null : ingredientsList.get(0).getMedication().getId();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("ingredientsList", ingredientsList)
        .append("continuousInfusion", continuousInfusion)
        .append("site", site)
        .append("volumeSum", volumeSum)
        .append("volumeSumUnit", volumeSumUnit)
        .append("additionalInstruction", additionalInstruction)
        .append("baselineInfusion", baselineInfusion)
        .append("adjustToFluidBalance", adjustToFluidBalance)
        .append("volumeSumDisplay", volumeSumDisplay)
        .append("speedDisplay", speedDisplay)
        .append("speedFormulaDisplay", speedFormulaDisplay)
        .append("additionalInstructionDisplay", additionalInstructionDisplay)
        .append("baselineInfusionDisplay", baselineInfusionDisplay)
    ;
  }
}