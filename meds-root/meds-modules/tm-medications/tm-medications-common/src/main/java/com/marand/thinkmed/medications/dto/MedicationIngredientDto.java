package com.marand.thinkmed.medications.dto;

import com.marand.maf.core.data.IdentityDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 * @author Mitja Lapajne
 */
public class MedicationIngredientDto extends IdentityDto
{
  private long ingredientId;
  private String ingredientName;
  private Double strengthNumerator;
  private String strengthNumeratorUnit;
  private Double strengthDenominator;
  private String strengthDenominatorUnit;
  private boolean descriptive;
  private boolean main;
  private MedicationRuleEnum ingredientRule;

  public String getIngredientName()
  {
    return ingredientName;
  }

  public void setIngredientName(final String ingredientName)
  {
    this.ingredientName = ingredientName;
  }

  public Double getStrengthNumerator()
  {
    return strengthNumerator;
  }

  public void setStrengthNumerator(final Double strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  }

  public String getStrengthNumeratorUnit()
  {
    return strengthNumeratorUnit;
  }

  public void setStrengthNumeratorUnit(final String strengthNumeratorUnit)
  {
    this.strengthNumeratorUnit = strengthNumeratorUnit;
  }

  public Double getStrengthDenominator()
  {
    return strengthDenominator;
  }

  public void setStrengthDenominator(final Double strengthDenominator)
  {
    this.strengthDenominator = strengthDenominator;
  }

  public String getStrengthDenominatorUnit()
  {
    return strengthDenominatorUnit;
  }

  public void setStrengthDenominatorUnit(final String strengthDenominatorUnit)
  {
    this.strengthDenominatorUnit = strengthDenominatorUnit;
  }

  public boolean isDescriptive()
  {
    return descriptive;
  }

  public void setDescriptive(final boolean descriptive)
  {
    this.descriptive = descriptive;
  }

  public boolean isMain()
  {
    return main;
  }

  public void setMain(final boolean main)
  {
    this.main = main;
  }

  public MedicationRuleEnum getIngredientRule()
  {
    return ingredientRule;
  }

  public void setIngredientRule(final MedicationRuleEnum ingredientRule)
  {
    this.ingredientRule = ingredientRule;
  }

  public long getIngredientId()
  {
    return ingredientId;
  }

  public void setIngredientId(final long ingredientId)
  {
    this.ingredientId = ingredientId;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("ingredientId", ingredientId)
        .append("ingredientName", ingredientName)
        .append("strengthNumerator", strengthNumerator)
        .append("strengthNumeratorUnit", strengthNumeratorUnit)
        .append("strengthDenominator", strengthDenominator)
        .append("strengthDenominatorUnit", strengthDenominatorUnit)
        .append("descriptive", descriptive)
        .append("main", main)
        .append("ingredientRule", ingredientRule)
    ;
  }
}
