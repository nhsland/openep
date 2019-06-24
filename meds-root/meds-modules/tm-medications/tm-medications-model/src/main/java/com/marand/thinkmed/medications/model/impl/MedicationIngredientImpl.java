package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xpMedIngredientCode", columnList = "code")
})
public class MedicationIngredientImpl extends AbstractCatalogEntity
{
  private MedicationRuleEnum medicationRule;

  @Enumerated(EnumType.STRING)
  public MedicationRuleEnum getMedicationRule()
  {
    return medicationRule;
  }

  public void setMedicationRule(final MedicationRuleEnum medicationRule)
  {
    this.medicationRule = medicationRule;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("medicationRule", medicationRule);
  }
}
