package com.marand.thinkmed.medications.dto.unit;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class MedicationUnitTypeDto extends NamedIdentityDto implements JsonSerializable
{
  private Double factor; // units conversion factor
  private UnitGroupEnum group;
  private String displayName;

  public MedicationUnitTypeDto() {}

  public MedicationUnitTypeDto(
      final long id,
      final Double factor,
      final @NonNull String name,
      final UnitGroupEnum group,
      final @NonNull String displayName)
  {
    setId(id);
    this.factor = factor;
    setName(name);
    this.displayName = displayName;
    this.group = group;
  }

  public void setFactor(final Double factor)
  {
    this.factor = factor;
  }

  public void setGroup(final UnitGroupEnum group)
  {
    this.group = group;
  }

  public Double getFactor()
  {
    return factor;
  }

  public UnitGroupEnum getGroup()
  {
    return group;
  }

  public void setDisplayName(final String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public boolean isConvertible(final @NonNull MedicationUnitTypeDto unit)
  {
    return group == unit.getGroup();
  }

  public double convert(final double value, final @NonNull MedicationUnitTypeDto to)
  {
    Preconditions.checkNotNull(factor, "units conversion factor for " + displayName + " is null");

    if (isConvertible(to))
    {
      return value * factor / to.getFactor();
    }
    throw new IllegalStateException("Units " + getName() + ", " + to.getName() + " are not compatible!");
  }

  @Override
  public boolean equals(final Object obj)
  {
    return obj instanceof MedicationUnitTypeDto && Objects.equals(getName(), ((MedicationUnitTypeDto)obj).getName());
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + getName().hashCode();
    return result;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("factor", factor)
        .append("group", group)
        .append("displayName", displayName)
    ;
  }
}
