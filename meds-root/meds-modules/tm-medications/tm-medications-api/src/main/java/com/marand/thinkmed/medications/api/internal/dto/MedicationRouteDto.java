package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class MedicationRouteDto extends NamedIdentityDto implements JsonSerializable
{
  private MedicationRouteTypeEnum type;
  private boolean unlicensedRoute;
  private MaxDoseDto maxDose;
  private boolean discretionary;

  public MedicationRouteTypeEnum getType()
  {
    return type;
  }

  public void setType(final MedicationRouteTypeEnum type)
  {
    this.type = type;
  }

  public boolean isUnlicensedRoute()
  {
    return unlicensedRoute;
  }

  public void setUnlicensedRoute(final boolean unlicensedRoute)
  {
    this.unlicensedRoute = unlicensedRoute;
  }

  public MaxDoseDto getMaxDose()
  {
    return maxDose;
  }

  public void setMaxDose(final MaxDoseDto maxDose)
  {
    this.maxDose = maxDose;
  }

  public boolean isDiscretionary()
  {
    return discretionary;
  }

  public void setDiscretionary(final boolean discretionary)
  {
    this.discretionary = discretionary;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("type", type)
        .append("unlicensedRoute", unlicensedRoute)
        .append("maxDose", maxDose)
        .append("discretionary", discretionary)
    ;
  }
}
