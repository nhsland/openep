
package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;
import org.openehr.jaxb.rm.DvCount;

/**
 * @author Mitja Lapajne
 */
public class TherapeuticDirection
{
  @EhrMapped("items[openEHR-EHR-CLUSTER.dosage.v1]")
  private List<Dosage> dosage = new ArrayList<>();

  @EhrMapped("items[at0172]/value")
  private DvCount maximumNumberOfAdministration;

  @EhrMapped("items[openEHR-EHR-CLUSTER.timing_nondaily.v1]")
  private TimingNonDaily directionRepetition;

  public List<Dosage> getDosage()
  {
    return dosage;
  }

  public void setDosage(final List<Dosage> dosage)
  {
    this.dosage = dosage;
  }

  public DvCount getMaximumNumberOfAdministration()
  {
    return maximumNumberOfAdministration;
  }

  public void setMaximumNumberOfAdministration(final DvCount maximumNumberOfAdministration)
  {
    this.maximumNumberOfAdministration = maximumNumberOfAdministration;
  }

  public TimingNonDaily getDirectionRepetition()
  {
    return directionRepetition;
  }

  public void setDirectionRepetition(final TimingNonDaily directionRepetition)
  {
    this.directionRepetition = directionRepetition;
  }
}
