
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvQuantity;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;

/**
 * @author Mitja Lapajne
 */
public class TimingDaily
{
  @EhrMapped("items[at0003]/value")
  private DvQuantity frequency;

  @EhrMapped("items[at0014]/value")
  private DvDuration interval;

  @EhrMapped("items[at0004]/value")
  private List<DvTime> specificTime = new ArrayList<>();

  @EhrMapped("items[at0024]/value")
  private DvBoolean asRequired;

  @EhrMapped("items[at0025]/value")
  private DvText asRequiredCriterion;

  @EhrMapped("items[at0039]/items[at0026]/value")
  private DvText specificEvent;

  public DvQuantity getFrequency()
  {
    return frequency;
  }

  public void setFrequency(final DvQuantity frequency)
  {
    this.frequency = frequency;
  }

  public DvDuration getInterval()
  {
    return interval;
  }

  public void setInterval(final DvDuration interval)
  {
    this.interval = interval;
  }

  public List<DvTime> getSpecificTime()
  {
    return specificTime;
  }

  public void setSpecificTime(final List<DvTime> specificTime)
  {
    this.specificTime = specificTime;
  }

  public DvBoolean getAsRequired()
  {
    return asRequired;
  }

  public void setAsRequired(final DvBoolean asRequired)
  {
    this.asRequired = asRequired;
  }

  public DvText getAsRequiredCriterion()
  {
    return asRequiredCriterion;
  }

  public void setAsRequiredCriterion(final DvText asRequiredCriterion)
  {
    this.asRequiredCriterion = asRequiredCriterion;
  }

  public DvText getSpecificEvent()
  {
    return specificEvent;
  }

  public void setSpecificEvent(final DvText specificEvent)
  {
    this.specificEvent = specificEvent;
  }
}
