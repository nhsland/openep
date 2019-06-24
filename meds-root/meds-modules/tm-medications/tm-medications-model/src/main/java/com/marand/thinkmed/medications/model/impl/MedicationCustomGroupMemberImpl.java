package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedCustomGroupMed", columnList = "medication_id"),
    @Index(name = "xfMedCustomGroupGroup", columnList = "medication_custom_group_id")})
public class MedicationCustomGroupMemberImpl extends AbstractPermanentEntity
{
  private MedicationImpl medication;
  private MedicationCustomGroupImpl medicationCustomGroup;

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  @ManyToOne(targetEntity = MedicationCustomGroupImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationCustomGroupImpl getMedicationCustomGroup()
  {
    return medicationCustomGroup;
  }

  public void setMedicationCustomGroup(final MedicationCustomGroupImpl medicationCustomGroup)
  {
    this.medicationCustomGroup = medicationCustomGroup;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("medication", medication)
        .append("medicationCustomGroup", medicationCustomGroup);
  }
}
