package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.api.internal.dto.MaxDosePeriod;
import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.ColumnDefault;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Entity
@Table(indexes = {
    @Index(name = "xfMedicationRouteLinkRoute", columnList = "route_id"),
    @Index(name = "xfMedRouteLinkMedBase", columnList = "medication_base_id"),
    @Index(name = "xfMedRouteLinkMaxUnit", columnList = "max_dose_unit_id")})
public class MedicationRouteLinkImpl extends AbstractTemporalEntity
{
  private MedicationRouteImpl route;
  private MedicationBaseImpl medicationBase;
  private boolean defaultRoute;
  private boolean discretionary;
  private Boolean unlicensed;

  private Integer maxDose;
  private MedicationUnitImpl maxDoseUnit;
  private MaxDosePeriod maxDosePeriod;

  @ManyToOne(targetEntity = MedicationRouteImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationRouteImpl getRoute()
  {
    return route;
  }

  public void setRoute(final MedicationRouteImpl route)
  {
    this.route = route;
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

  public boolean isDefaultRoute()
  {
    return defaultRoute;
  }

  public void setDefaultRoute(final boolean defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  }

  @ColumnDefault("0")
  public boolean isDiscretionary()
  {
    return discretionary;
  }

  public void setDiscretionary(final boolean discretionary)
  {
    this.discretionary = discretionary;
  }

  public Boolean isUnlicensed()
  {
    return unlicensed;
  }

  public void setUnlicensed(final Boolean unlicensed)
  {
    this.unlicensed = unlicensed;
  }

  public Integer getMaxDose()
  {
    return maxDose;
  }

  public void setMaxDose(final Integer maxDose)
  {
    this.maxDose = maxDose;
  }

  @ManyToOne(targetEntity = MedicationUnitImpl.class)
  public MedicationUnitImpl getMaxDoseUnit()
  {
    return maxDoseUnit;
  }

  public void setMaxDoseUnit(final MedicationUnitImpl maxDoseUnit)
  {
    this.maxDoseUnit = maxDoseUnit;
  }

  @Enumerated(EnumType.STRING)
  public MaxDosePeriod getMaxDosePeriod()
  {
    return maxDosePeriod;
  }

  public void setMaxDosePeriod(final MaxDosePeriod maxDosePeriod)
  {
    this.maxDosePeriod = maxDosePeriod;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("route", route)
        .append("medicationBase", medicationBase)
        .append("defaultRoute", defaultRoute)
        .append("discretionary", discretionary)
        .append("unlicensed", unlicensed)
        .append("maxDose", maxDose)
        .append("maxDoseUnit", maxDoseUnit)
        .append("maxDosePeriod", maxDosePeriod)
    ;
  }
}
