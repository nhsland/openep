package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;

/**
 * @author Nejc Korasa
 */

public class ReleaseDetailsDto implements JsonSerializable
{
  private final ReleaseType type;
  private final Integer hours;

  private String display;

  public ReleaseDetailsDto(final ReleaseType type, final Integer hours)
  {
    this.type = type;
    this.hours = hours;
  }

  public ReleaseDetailsDto(final ReleaseType type)
  {
    this.type = type;
    hours = null;
  }

  public ReleaseType getType()
  {
    return type;
  }

  public Integer getHours()
  {
    return hours;
  }

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public MedicationPropertyType mapToPropertyType()
  {
    if (type == ReleaseType.MODIFIED_RELEASE)
    {
      return hours == null
             ? MedicationPropertyType.MODIFIED_RELEASE
             : MedicationPropertyType.MODIFIED_RELEASE_TIME;
    }
    if (type == ReleaseType.GASTRO_RESISTANT)
    {
      return MedicationPropertyType.GASTRO_RESISTANT;
    }

    throw new IllegalStateException("Release type " + type + " not supported");
  }

  @Override
  public String toString()
  {
    return String.format("ReleaseDetailsDto{type=%s, hours=%d, display='%s'}", type, hours, display);
  }
}
