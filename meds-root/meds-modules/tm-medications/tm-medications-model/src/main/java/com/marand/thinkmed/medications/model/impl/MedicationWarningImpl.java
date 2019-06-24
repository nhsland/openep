package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import com.marand.thinkmed.medications.service.WarningSeverity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedWarningMed", columnList = "medication_id"),
    @Index(name = "xfMedWarningMedBase", columnList = "medication_base_id"),
    @Index(name = "xpMedWarningSeverity", columnList = "severity")})
public class MedicationWarningImpl extends AbstractTemporalEntity
{
  private MedicationBaseImpl medicationBase;
  private MedicationImpl medication;
  private WarningSeverity severity;
  private String description;

  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public WarningSeverity getSeverity()
  {
    return severity;
  }

  public void setSeverity(final WarningSeverity severity)
  {
    this.severity = severity;
  }

  @Column(nullable = false)
  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("medicationBase", medicationBase)
        .append("medication", medication)
        .append("severity", severity)
        .append("description", description)
    ;
  }
}
