
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class MaximumDose
{
  @EhrMapped("items[at0130]/value")
  private DvQuantity maximumAmount;

  @EhrMapped("items[at0146]/value")
  private DvText maximumAmountUnit;

  @EhrMapped("items[at0053]/value")
  private DvDuration allowedPeriod;

  public DvQuantity getMaximumAmount()
  {
    return maximumAmount;
  }

  public void setMaximumAmount(final DvQuantity maximumAmount)
  {
    this.maximumAmount = maximumAmount;
  }

  public DvText getMaximumAmountUnit()
  {
    return maximumAmountUnit;
  }

  public void setMaximumAmountUnit(final DvText maximumAmountUnit)
  {
    this.maximumAmountUnit = maximumAmountUnit;
  }

  public DvDuration getAllowedPeriod()
  {
    return allowedPeriod;
  }

  public void setAllowedPeriod(final DvDuration allowedPeriod)
  {
    this.allowedPeriod = allowedPeriod;
  }
}
