package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationIndicationLinkInd", columnList = "indication_id"),
    @Index(name = "xfMedicationIndLinkMedBase", columnList = "medication_base_id")})
public class MedicationIndicationLinkImpl extends AbstractTemporalEntity
{
  private MedicationIndicationImpl indication;
  private MedicationBaseImpl medicationBase;

  @ManyToOne(targetEntity = MedicationIndicationImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationIndicationImpl getIndication()
  {
    return indication;
  }

  public void setIndication(final MedicationIndicationImpl indication)
  {
    this.indication = indication;
  }

  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("indication", indication)
        .append("medicationBase", medicationBase)
    ;
  }
}
