package com.marand.thinkmed.medications.dto.change;

import java.util.List;

import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */

public class StringsTherapyChangeDto extends TherapyChangeDto<List<String>, List<String>>
{
  public StringsTherapyChangeDto(final @NonNull TherapyChangeType type)
  {
    super(type);
  }
}