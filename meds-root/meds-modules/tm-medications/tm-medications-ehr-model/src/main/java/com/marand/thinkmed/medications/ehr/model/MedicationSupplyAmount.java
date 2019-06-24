package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Vid Kumse
 */
public class MedicationSupplyAmount
{

  @EhrMapped("items[at0131]/value")
  private DvQuantity amount;

  @EhrMapped("items[at0147]/value")
  private DvText units;

  @EhrMapped("items[at0142]/value")
  private DvDuration durationOfSupply;

  public DvQuantity getAmount()
  {
    return amount;
  }

  public void setAmount(final DvQuantity amount)
  {
    this.amount = amount;
  }

  public DvText getUnits()
  {
    return units;
  }

  public void setUnits(final DvText units)
  {
    this.units = units;
  }

  public DvDuration getDurationOfSupply()
  {
    return durationOfSupply;
  }

  public void setDurationOfSupply(final DvDuration durationOfSupply)
  {
    this.durationOfSupply = durationOfSupply;
  }
}
