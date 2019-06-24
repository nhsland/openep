package com.marand.thinkmed.medications.ehr.model;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkehr.mapping.annotation.EhrMapped;

/**
 * @author Mitja Lapajne
 */

public abstract class Entry
{
  @EhrMapped("other_participations")
  private List<Participation> otherParticipations = new ArrayList<>();

  public List<Participation> getOtherParticipations()
  {
    return otherParticipations;
  }

  public void setOtherParticipations(final List<Participation> otherParticipations)
  {
    this.otherParticipations = otherParticipations;
  }
}
