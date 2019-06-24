package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = @Index(name = "xfTherapyLastLinkPatient", columnList = "patient_id"))
public class PatientTherapyLastLinkNameImpl extends AbstractPermanentEntity
{
  private Long patientId;
  private String lastLinkName;

  public Long getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final Long patientId)
  {
    this.patientId = patientId;
  }

  @Column(nullable = false)
  public String getLastLinkName()
  {
    return lastLinkName;
  }

  public void setLastLinkName(final String lastLinkName)
  {
    this.lastLinkName = lastLinkName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("patientId", patientId);
    tsb.append("lastLinkName", lastLinkName);
  }
}
