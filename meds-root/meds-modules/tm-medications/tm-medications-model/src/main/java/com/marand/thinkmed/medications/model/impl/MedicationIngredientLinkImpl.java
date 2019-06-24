package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

@Entity
@Table(indexes = {
    @Index(name = "xfMedicationIngredientLinkIng", columnList = "ingredient_id"),
    @Index(name = "xfMedicationIngrLinkNumUnit", columnList = "strength_numerator_unit_id"),
    @Index(name = "xfMedicationIngrLinkDenUnit", columnList = "strength_denominator_unit_id"),
    @Index(name = "xfMedicationIngrLinkMedBase", columnList = "medication_base_id")})
public class MedicationIngredientLinkImpl extends AbstractTemporalEntity
{
  private MedicationIngredientImpl ingredient;
  private MedicationBaseImpl medicationBase;
  private Double strengthNumerator;
  private MedicationUnitImpl strengthNumeratorUnit;
  private Double strengthDenominator;
  private MedicationUnitImpl strengthDenominatorUnit;
  private boolean main;

  @ManyToOne(targetEntity = MedicationIngredientImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationIngredientImpl getIngredient()
  {
    return ingredient;
  }

  public void setIngredient(final MedicationIngredientImpl ingredient)
  {
    this.ingredient = ingredient;
  }

  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  @Column(nullable = false)
  public Double getStrengthNumerator()
  {
    return strengthNumerator;
  }

  public void setStrengthNumerator(final Double strengthNumerator)
  {
    this.strengthNumerator = strengthNumerator;
  }

  @ManyToOne(targetEntity = MedicationUnitImpl.class)
  public MedicationUnitImpl getStrengthNumeratorUnit()
  {
    return strengthNumeratorUnit;
  }

  public void setStrengthNumeratorUnit(final MedicationUnitImpl strengthNumeratorUnit)
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

  @ManyToOne(targetEntity = MedicationUnitImpl.class)
  public MedicationUnitImpl getStrengthDenominatorUnit()
  {
    return strengthDenominatorUnit;
  }

  public void setStrengthDenominatorUnit(final MedicationUnitImpl strengthDenominatorUnit)
  {
    this.strengthDenominatorUnit = strengthDenominatorUnit;
  }

  public boolean isMain()
  {
    return main;
  }

  public void setMain(final boolean main)
  {
    this.main = main;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("ingredient", ingredient)
        .append("medicationBase", medicationBase)
        .append("strengthNumerator", strengthNumerator)
        .append("strengthNumeratorUnit", strengthNumeratorUnit)
        .append("strengthDenominator", strengthDenominator)
        .append("strengthDenominatorUnit", strengthDenominatorUnit)
        .append("main", main)
    ;
  }
}
