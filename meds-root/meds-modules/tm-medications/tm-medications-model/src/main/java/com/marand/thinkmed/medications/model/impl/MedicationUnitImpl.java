package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;

/**
 * @author Nejc Korasa
 */

@Entity
public class MedicationUnitImpl extends AbstractCatalogEntity
{
  private MedicationUnitTypeImpl type;

  @ManyToOne(targetEntity = MedicationUnitTypeImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationUnitTypeImpl getType()
  {
    return type;
  }

  public void setType(final MedicationUnitTypeImpl type)
  {
    this.type = type;
  }
}
