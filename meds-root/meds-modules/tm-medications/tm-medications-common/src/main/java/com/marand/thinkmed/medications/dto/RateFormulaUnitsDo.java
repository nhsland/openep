package com.marand.thinkmed.medications.dto;

import com.marand.thinkmed.medications.dto.unit.KnownUnitType;

/**
 * @author Nejc Korasa
 */

public class RateFormulaUnitsDo
{
  private String massUnitName;
  private KnownUnitType patientUnitKnownType; // surface or mass unit
  private KnownUnitType timeUnitKnownType;

  public String getMassUnitName()
  {
    return massUnitName;
  }

  public void setMassUnitName(final String massUnitName)
  {
    this.massUnitName = massUnitName;
  }

  public KnownUnitType getPatientUnitKnownType()
  {
    return patientUnitKnownType;
  }

  public void setPatientUnitKnownType(final KnownUnitType patientUnitKnownType)
  {
    this.patientUnitKnownType = patientUnitKnownType;
  }

  public KnownUnitType getTimeUnitKnownType()
  {
    return timeUnitKnownType;
  }

  public void setTimeUnitKnownType(final KnownUnitType timeUnitKnownType)
  {
    this.timeUnitKnownType = timeUnitKnownType;
  }
}
