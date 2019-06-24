
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class SafetyOverride
{
  @EhrMapped("items[at0171]/value")
  private DvText overridenSafetyAdvice;

  @EhrMapped("items[at0162]/value")
  private DvText overrideReason;

  public DvText getOverridenSafetyAdvice()
  {
    return overridenSafetyAdvice;
  }

  public void setOverridenSafetyAdvice(final DvText overridenSafetyAdvice)
  {
    this.overridenSafetyAdvice = overridenSafetyAdvice;
  }

  public DvText getOverrideReason()
  {
    return overrideReason;
  }

  public void setOverrideReason(final DvText overrideReason)
  {
    this.overrideReason = overrideReason;
  }
}
