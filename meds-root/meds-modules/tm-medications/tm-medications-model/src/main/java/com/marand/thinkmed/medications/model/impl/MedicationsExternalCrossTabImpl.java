package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xpMedsExternalTransExtSys", columnList = "external_system"),
    @Index(name = "xpMedsExternalTransValueType", columnList = "value_type"),
    @Index(name = "xpMedsExternalTransValue", columnList = "value")})
public class MedicationsExternalCrossTabImpl extends AbstractPermanentEntity
{
  private String externalSystem;
  private MedicationsExternalValueType valueType;
  private String value;
  private String externalValue;

  @Column(nullable = false)
  public String getExternalSystem()
  {
    return externalSystem;
  }

  public void setExternalSystem(final String externalSystem)
  {
    this.externalSystem = externalSystem;
  }

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public MedicationsExternalValueType getValueType()
  {
    return valueType;
  }

  public void setValueType(final MedicationsExternalValueType valueType)
  {
    this.valueType = valueType;
  }

  @Column(nullable = false)
  public String getValue()
  {
    return value;
  }

  public void setValue(final String value)
  {
    this.value = value;
  }

  @Column(nullable = false)
  public String getExternalValue()
  {
    return externalValue;
  }

  public void setExternalValue(final String externalValue)
  {
    this.externalValue = externalValue;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("externalSystem", externalSystem)
        .append("valueType", valueType)
        .append("value", value)
        .append("externalValue", externalValue)
    ;
  }
}
