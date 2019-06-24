package com.marand.thinkmed.medications.dto.property;

import java.util.Objects;

import com.marand.maf.core.data.IdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public final class MedicationPropertyDto extends IdentityDto implements JsonSerializable
{
  private final MedicationPropertyType type;
  private final String name;
  private final String value;

  public MedicationPropertyDto(final Long id, final MedicationPropertyType type, final @NonNull String name, final String value)
  {
    setId(id);
    this.type = type;
    this.name = name;
    this.value = value;
  }

  public MedicationPropertyDto(final Long id, final MedicationPropertyType type, final @NonNull String name)
  {
    this(id, type, name, null);
  }

  public MedicationPropertyType getType()
  {
    return type;
  }

  public String getName()
  {
    return name;
  }

  public String getValue()
  {
    return value;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof MedicationPropertyDto))
    {
      return false;
    }
    if (!super.equals(o))
    {
      return false;
    }

    //noinspection QuestionableName
    final MedicationPropertyDto that = (MedicationPropertyDto)o;
    return Objects.equals(value, that.getValue());
  }

  @Override
  public int hashCode()
  {

    return Objects.hash(super.hashCode(), value);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("type", type).append("value", value).append("name", name);
  }
}
