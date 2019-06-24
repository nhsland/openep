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
    @Index(name = "xfMedicationRouteRelationPar", columnList = "parent_route_id"),
    @Index(name = "xfMedicationRouteRelationChil", columnList = "child_route_id")})
public class MedicationRouteRelationImpl extends AbstractPermanentEntity
{
  private MedicationRouteImpl parentRoute;
  private MedicationRouteImpl childRoute;
  private boolean defaultRoute;

  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRouteImpl getParentRoute()
  {
    return parentRoute;
  }

  public void setParentRoute(final MedicationRouteImpl parentRoute)
  {
    this.parentRoute = parentRoute;
  }

  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRouteImpl getChildRoute()
  {
    return childRoute;
  }

  public void setChildRoute(final MedicationRouteImpl childRoute)
  {
    this.childRoute = childRoute;
  }

  public boolean isDefaultRoute()
  {
    return defaultRoute;
  }

  public void setDefaultRoute(final boolean defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("parentRoute", parentRoute)
        .append("childRoute", childRoute)
        .append("defaultRoute", defaultRoute)
    ;
  }
}
