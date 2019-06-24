package com.marand.thinkmed.medications.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
@Entity
@Table(indexes = {
    @Index(name = "xpMedRouteCode", columnList = "code")
})
public class MedicationRouteImpl extends AbstractCatalogEntity
{
  private String shortName;
  private MedicationRouteTypeEnum type;
  private Integer sortOrder;
  private Set<MedicationRouteRelationImpl> parentRelations = new HashSet<>();
  private Set<MedicationRouteRelationImpl> childRelations = new HashSet<>();

  @Column(nullable = false)
  public String getShortName()
  {
    return shortName;
  }

  public void setShortName(final String shortName)
  {
    this.shortName = shortName;
  }

  @Enumerated(EnumType.STRING)
  public MedicationRouteTypeEnum getType()
  {
    return type;
  }

  public void setType(final MedicationRouteTypeEnum type)
  {
    this.type = type;
  }

  public Integer getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  @OneToMany(targetEntity = MedicationRouteRelationImpl.class, mappedBy = "childRoute")
  public Set<MedicationRouteRelationImpl> getParentRelations()
  {
    return parentRelations;
  }

  public void setParentRelations(final Set<MedicationRouteRelationImpl> parentRelations)
  {
    this.parentRelations = parentRelations;
  }

  @OneToMany(targetEntity = MedicationRouteRelationImpl.class, mappedBy = "parentRoute")
  public Set<MedicationRouteRelationImpl> getChildRelations()
  {
    return childRelations;
  }

  public void setChildRelations(final Set<MedicationRouteRelationImpl> childRelations)
  {
    this.childRelations = childRelations;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("shortName", shortName)
        .append("type", type)
        .append("sortOrder", sortOrder)
        .append("parentRelations", parentRelations)
        .append("childRelations", childRelations);
  }
}
