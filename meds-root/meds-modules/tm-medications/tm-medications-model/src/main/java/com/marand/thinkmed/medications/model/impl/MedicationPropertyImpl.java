package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.dto.property.MedicationPropertyLevel;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyValueType;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

@Entity
@Table(indexes = {
    @Index(name = "xpMedPropCode", columnList = "code")
})
public class MedicationPropertyImpl extends AbstractCatalogEntity
{
  private String propertyType;
  private MedicationPropertyValueType valueType;
  private MedicationPropertyLevel propertyLevel;
  private Boolean visible;
  private Integer sortOrder;

  public String getPropertyType()
  {
    return propertyType;
  }

  public void setPropertyType(final String propertyType)
  {
    this.propertyType = propertyType;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public MedicationPropertyValueType getValueType()
  {
    return valueType;
  }

  public void setValueType(final MedicationPropertyValueType valueType)
  {
    this.valueType = valueType;
  }

  @Enumerated(EnumType.STRING)
  public MedicationPropertyLevel getPropertyLevel()
  {
    return propertyLevel;
  }

  public void setPropertyLevel(final MedicationPropertyLevel propertyLevel)
  {
    this.propertyLevel = propertyLevel;
  }

  @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
  public Boolean isVisible()
  {
    return visible;
  }

  public void setVisible(final Boolean visible)
  {
    this.visible = visible;
  }

  public Integer getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("propertyType", propertyType)
        .append("valueType", valueType)
        .append("level", propertyLevel)
        .append("visible", visible)
        .append("sortOrder", sortOrder)
    ;
  }
}
