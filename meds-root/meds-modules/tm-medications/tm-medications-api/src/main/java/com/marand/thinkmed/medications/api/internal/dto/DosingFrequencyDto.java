package com.marand.thinkmed.medications.api.internal.dto;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class DosingFrequencyDto extends DataTransferObject
{
  private DosingFrequencyTypeEnum type;
  private Double value;

  public DosingFrequencyDto(final DosingFrequencyTypeEnum type)
  {
    this(type, null);
  }

  public DosingFrequencyDto(final DosingFrequencyTypeEnum type, @Nullable final Double value)
  {
    this.type = type;
    this.value = value;
  }

  public DosingFrequencyTypeEnum getType()
  {
    return type;
  }

  public void setType(final DosingFrequencyTypeEnum type)
  {
    this.type = type;
  }

  public Double getValue()
  {
    return value;
  }

  public void setValue(final Double value)
  {
    this.value = value;
  }

  public String getKey()
  {
    if (type == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      Preconditions.checkNotNull(value);
      return String.valueOf(value) + 'H';
    }
    if (type == DosingFrequencyTypeEnum.DAILY_COUNT)
    {
      Preconditions.checkNotNull(value);
      return String.valueOf(value) + 'X';
    }
    Preconditions.checkArgument(value == null);
    return type.name();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("type", type)
        .append("value", value);
  }
}
