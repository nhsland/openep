package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Klavdij Lapajne
 */

@Entity
@Table(indexes = {
    @Index(name = "xpVMedicationName", columnList = "name")})
public class MedicationVersionImpl extends AbstractTemporalEntity
{
  private MedicationImpl medication;
  private String name;
  private String shortName;
  private String longName;
  private String medicationPackaging;

  @Column(nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  public String getLongName()
  {
    return longName;
  }

  public void setLongName(final String longName)
  {
    this.longName = longName;
  }

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  public String getMedicationPackaging()
  {
    return medicationPackaging;
  }

  public void setMedicationPackaging(final String medicationPackaging)
  {
    this.medicationPackaging = medicationPackaging;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("name", name)
        .append("shortName", shortName)
        .append("longName", longName)
        .append("medication", medication)
        .append("medicationPackaging", medicationPackaging)
    ;
  }
}
