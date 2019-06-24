package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class OxygenStartingDevice extends DataTransferObject implements JsonSerializable
{
  private MedicalDeviceEnum route;
  private String routeType;

  public OxygenStartingDevice(final MedicalDeviceEnum route)
  {
    this.route = route;
  }

  public MedicalDeviceEnum getRoute()
  {
    return route;
  }

  public void setRoute(final MedicalDeviceEnum route)
  {
    this.route = route;
  }

  public String getRouteType()
  {
    return routeType;
  }

  public void setRouteType(final String routeType)
  {
    this.routeType = routeType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("route", route).append("routeType", routeType);
  }
}
