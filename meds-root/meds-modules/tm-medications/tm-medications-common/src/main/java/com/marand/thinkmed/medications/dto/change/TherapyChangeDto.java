package com.marand.thinkmed.medications.dto.change;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Igor Horvat
 * @author Mitja Lapajne
 */

public abstract class TherapyChangeDto<O, N> extends DataTransferObject implements JsonSerializable
{
  private final TherapyChangeType type;
  private N newValue;
  private O oldValue;

  protected TherapyChangeDto(final @NonNull TherapyChangeType type)
  {
    this.type = type;
  }

  public TherapyChangeType getType()
  {
    return type;
  }

  public N getNewValue()
  {
    return newValue;
  }

  public void setNewValue(final N newValue)
  {
    this.newValue = newValue;
  }

  public O getOldValue()
  {
    return oldValue;
  }

  public void setOldValue(final O oldValue)
  {
    this.oldValue = oldValue;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("type", type)
        .append("newValue", newValue)
        .append("oldValue", oldValue);
  }
}


