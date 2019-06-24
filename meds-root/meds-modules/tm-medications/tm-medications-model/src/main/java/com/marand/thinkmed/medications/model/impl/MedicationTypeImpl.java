package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationTypeBase", columnList = "medication_base_id"),
    @Index(name = "xpMedicationTypeMed", columnList = "type")})
public class MedicationTypeImpl extends AbstractPermanentEntity
{
  private MedicationBaseImpl medicationBase;
  private MedicationTypeEnum type;
  
  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public MedicationTypeEnum getType()
  {
    return type;
  }

  public void setType(final MedicationTypeEnum type)
  {
    this.type = type;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("medicationBase", medicationBase)
        .append("type", type)
    ;
  }
}
