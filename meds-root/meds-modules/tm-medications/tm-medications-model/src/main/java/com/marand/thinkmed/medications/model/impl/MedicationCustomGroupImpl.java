package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.dto.customGroup.MedicationCustomGroupType;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

@Entity
@Table(indexes = @Index(name = "xpMedCustomGroupCareProvider", columnList = "care_provider_id"))
public class MedicationCustomGroupImpl extends AbstractCatalogEntity
{
  private MedicationCustomGroupType customGroupType;
  private String careProviderId;
  private Integer sortOrder;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public MedicationCustomGroupType getCustomGroupType()
  {
    return customGroupType;
  }

  public void setCustomGroupType(final MedicationCustomGroupType customGroupType)
  {
    this.customGroupType = customGroupType;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
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
        .append("careProviderId", careProviderId)
        .append("careProviderId", careProviderId)
        .append("sortOrder", sortOrder)
    ;
  }
}
