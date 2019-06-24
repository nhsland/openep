package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

@Entity
@Table(indexes = {
    @Index(name = "xfMedPropLinkMed", columnList = "medication_id"),
    @Index(name = "xfMedPropLinkMedBase", columnList = "medication_base_id"),
    @Index(name = "xfMedPropLinkProp", columnList = "property_id")
})
public class MedicationPropertyLinkImpl extends AbstractTemporalEntity
{
  private MedicationPropertyImpl property;
  private MedicationImpl medication;
  private MedicationBaseImpl medicationBase;
  private String value;

  @ManyToOne(targetEntity = MedicationPropertyImpl.class, optional = false)
  public MedicationPropertyImpl getProperty()
  {
    return property;
  }

  public void setProperty(final MedicationPropertyImpl property)
  {
    this.property = property;
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

  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(final String value)
  {
    this.value = value;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("property", property)
        .append("medication", medication)
        .append("medicationBase", medicationBase)
        .append("value", value)
    ;
  }
}
