package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;

import com.marand.thinkmed.medications.model.impl.core.AbstractEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
@Entity
public class TherapyChangedImpl extends AbstractEntity
{
  private String patientId;
  private DateTime changeTime;

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  @Type(type = "com.marand.maf.core.hibernate.type.DateTimeType")
  public DateTime getChangeTime()
  {
    return changeTime;
  }

  public void setChangeTime(final DateTime changeTime)
  {
    this.changeTime = changeTime;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("patientId", patientId);
    tsb.append("changeTime", changeTime);
  }
}
