package com.marand.thinkmed.medications.business.data;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import org.joda.time.Interval;

/**
 * User: MihaA
 */

public class TherapyDocumentationData
{
  private InpatientPrescription inpatientPrescription;
  private final List<Pair<String, Interval>> intervals = new ArrayList<>();
  private TherapyDto therapy;

  public InpatientPrescription getInpatientPrescription()
  {
    return inpatientPrescription;
  }

  public void setInpatientPrescription(final InpatientPrescription inpatientPrescription)
  {
    this.inpatientPrescription = inpatientPrescription;
  }

  public List<Pair<String, Interval>> getIntervals()
  {
    return intervals;
  }

  public void addInterval(final String therapyId, final Interval interval)
  {
    intervals.add(Pair.of(therapyId, interval));
  }

  public void removeInterval(final String therapyId, final Interval interval)
  {
    intervals.remove(Pair.of(therapyId, interval));
  }

  public Interval findIntervalForId(final String id)
  {
    for (final Pair<String, Interval> pair : intervals)
    {
      if (pair.getFirst().equals(id))
      {
        return pair.getSecond();
      }
    }
    return null;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }
}
