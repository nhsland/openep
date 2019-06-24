package com.marand.thinkmed.fdb.service.impl;

/**
 * @author Vid Kumše
 */
public enum FdbScreeningModulesEnum
{
  ALL("All", 0),
  DRUG_SENSIVITIES("Drug Sensivities", 1),
  DRUG_INTERACTIONS("Drug Interactions", 2),
  PATIENT_CHECK("Patient Check", 4),
  DRUG_EQUIVALENCE("Drug Equivalence", 16),
  DRUG_DOUBLINGS("Drug Droublings", 64),
  DUPLICATE_THERAPY("Duplicate Therapy", 32),
  PHARMACOLOGICAL_EQUIVALENCE("Pharmacological Equivalence", 128);

  private final String name;
  private final Integer key;

  FdbScreeningModulesEnum(final String name, final Integer key)
  {
    this.name = name;
    this.key = key;
  }

  public String getName()
  {
    return name;
  }

  public Integer getKey()
  {
    return key;
  }
}
