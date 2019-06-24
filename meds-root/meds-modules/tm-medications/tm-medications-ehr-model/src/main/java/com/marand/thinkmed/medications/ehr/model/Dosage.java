
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DataValue;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class Dosage
{
  @EhrMapped("items[at0144]/value]")
  private DataValue doseAmount; //DvQuantity or DvInterval

  @EhrMapped("items[at0145]/value]")
  private DvText doseUnit;

  @EhrMapped("items[at0135]/value]")
  private DvText doseFormula;

  @EhrMapped("items[at0178]/value")
  private DvText doseDescription;

  @EhrMapped("items[openEHR-EHR-CLUSTER.timing_daily.v1]")
  private TimingDaily timing;

  @EhrMapped("items[at0134]/value]")
  private DataValue administrationRate; //DvQuantity or DvText

  @EhrMapped("items[at0102]/value]")
  private DvDuration administrationDuration;

  @EhrMapped("items[at0176]/value]")
  private DataValue alternateDoseAmount; //DvQuantity or DvInterval

  @EhrMapped("items[at0177]/value]")
  private DvText alternateDoseUnit;

  public DataValue getDoseAmount()
  {
    return doseAmount;
  }

  public void setDoseAmount(final DataValue doseAmount)
  {
    this.doseAmount = doseAmount;
  }

  public DvText getDoseUnit()
  {
    return doseUnit;
  }

  public void setDoseUnit(final DvText doseUnit)
  {
    this.doseUnit = doseUnit;
  }

  public DvText getDoseFormula()
  {
    return doseFormula;
  }

  public void setDoseFormula(final DvText doseFormula)
  {
    this.doseFormula = doseFormula;
  }

  public DvText getDoseDescription()
  {
    return doseDescription;
  }

  public void setDoseDescription(final DvText doseDescription)
  {
    this.doseDescription = doseDescription;
  }

  public TimingDaily getTiming()
  {
    return timing;
  }

  public void setTiming(final TimingDaily timing)
  {
    this.timing = timing;
  }

  public DataValue getAdministrationRate()
  {
    return administrationRate;
  }

  public void setAdministrationRate(final DataValue administrationRate)
  {
    this.administrationRate = administrationRate;
  }

  public DvDuration getAdministrationDuration()
  {
    return administrationDuration;
  }

  public void setAdministrationDuration(final DvDuration administrationDuration)
  {
    this.administrationDuration = administrationDuration;
  }

  public DataValue getAlternateDoseAmount()
  {
    return alternateDoseAmount;
  }

  public void setAlternateDoseAmount(final DataValue alternateDoseAmount)
  {
    this.alternateDoseAmount = alternateDoseAmount;
  }

  public DvText getAlternateDoseUnit()
  {
    return alternateDoseUnit;
  }

  public void setAlternateDoseUnit(final DvText alternateDoseUnit)
  {
    this.alternateDoseUnit = alternateDoseUnit;
  }
}
