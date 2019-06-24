package com.marand.thinkmed.medications.dto.change;

import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */

public class StringTherapyChangeDto extends TherapyChangeDto<String, String>
{
  public StringTherapyChangeDto(final @NonNull TherapyChangeType type)
  {
    super(type);
  }
}