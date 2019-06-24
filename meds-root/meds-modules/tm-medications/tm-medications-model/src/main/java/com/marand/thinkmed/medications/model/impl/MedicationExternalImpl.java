package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.MedicationExternalSystemType;
import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationExternalMed", columnList = "medication_id"),
    @Index(name = "xpMedicationExternalExtId", columnList = "external_id"),
    @Index(name = "xpMedicationExternalExtSys", columnList = "external_system"),
    @Index(name = "xpMedicationExternalSysTyp", columnList = "external_system_type")})
public class MedicationExternalImpl extends AbstractPermanentEntity
{
  private MedicationImpl medication;
  private boolean mainCode;
  private String externalId;
  private String externalSystem;
  private MedicationExternalSystemType externalSystemType;

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  @Column(nullable = false)
  public String getExternalId()
  {
    return externalId;
  }

  public void setExternalId(final String externalId)
  {
    this.externalId = externalId;
  }

  public String getExternalSystem()
  {
    return externalSystem;
  }

  public void setExternalSystem(final String externalSystem)
  {
    this.externalSystem = externalSystem;
  }

  @Enumerated(EnumType.STRING)
  public MedicationExternalSystemType getExternalSystemType()
  {
    return externalSystemType;
  }

  public void setExternalSystemType(final MedicationExternalSystemType externalSystemType)
  {
    this.externalSystemType = externalSystemType;
  }

  @Column(nullable = false)
  public boolean isMainCode()
  {
    return mainCode;
  }

  public void setMainCode(final boolean mainCode)
  {
    this.mainCode = mainCode;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("medication", medication)
        .append("externalId", externalId)
        .append("externalSystem", externalSystem)
        .append("externalSystemType", externalSystemType)
        .append("mainCode", mainCode)
    ;
  }
}
