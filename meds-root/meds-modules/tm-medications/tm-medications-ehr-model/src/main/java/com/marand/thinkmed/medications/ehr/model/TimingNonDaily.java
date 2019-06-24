
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvDuration;
import org.openehr.jaxb.rm.DvText;

/**
 * @author Mitja Lapajne
 */
public class TimingNonDaily
{
  @EhrMapped("items[at0002]/value")
  private DvDuration repetitionInterval;

  @EhrMapped("items[at0001]/value")
  private List<DvDate> specificDate = new ArrayList<>();

  @EhrMapped("items[at0003]/value")
  private List<DvCodedText> specificDayOfWeek = new ArrayList<>();

  @EhrMapped("items[at0021]/value")
  private DvText timingDescription;

  public DvDuration getRepetitionInterval()
  {
    return repetitionInterval;
  }

  public void setRepetitionInterval(final DvDuration repetitionInterval)
  {
    this.repetitionInterval = repetitionInterval;
  }

  public List<DvDate> getSpecificDate()
  {
    return specificDate;
  }

  public void setSpecificDate(final List<DvDate> specificDate)
  {
    this.specificDate = specificDate;
  }

  public List<DvCodedText> getSpecificDayOfWeek()
  {
    return specificDayOfWeek;
  }

  public void setSpecificDayOfWeek(final List<DvCodedText> specificDayOfWeek)
  {
    this.specificDayOfWeek = specificDayOfWeek;
  }

  public DvText getTimingDescription()
  {
    return timingDescription;
  }

  public void setTimingDescription(final DvText timingDescription)
  {
    this.timingDescription = timingDescription;
  }
}
