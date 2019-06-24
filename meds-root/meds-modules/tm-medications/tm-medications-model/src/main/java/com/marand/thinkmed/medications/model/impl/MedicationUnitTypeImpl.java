package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.marand.thinkmed.medications.dto.unit.UnitGroupEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;

/**
 * @author Nejc Korasa
 */

@Entity
public class MedicationUnitTypeImpl extends AbstractCatalogEntity
{
  private Double factor;
  private UnitGroupEnum unitGroup;

  public Double getFactor()
  {
    return factor;
  }

  public void setFactor(final Double factor)
  {
    this.factor = factor;
  }

  @Enumerated(EnumType.STRING)
  public UnitGroupEnum getUnitGroup()
  {
    return unitGroup;
  }

  public void setUnitGroup(final UnitGroupEnum unitGroup)
  {
    this.unitGroup = unitGroup;
  }
}
