
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvDateTime;
import org.openehr.jaxb.rm.DvQuantity;

/**
 * @author Mitja Lapajne
 */

public class BodyWeight extends Observation
{
  @EhrMapped("data[at0002]/events[at0003]/time")
  private DvDateTime time;

  @EhrMapped("data[at0002]/events[at0003]/data[at0001]/items[at0004]/value")
  private DvQuantity weight;

  public DvDateTime getTime()
  {
    return time;
  }

  public void setTime(final DvDateTime time)
  {
    this.time = time;
  }

  public DvQuantity getWeight()
  {
    return weight;
  }

  public void setWeight(final DvQuantity weight)
  {
    this.weight = weight;
  }
}
