package com.marand.thinkmed.medications.dto;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;

/**
 * @author Nejc Korasa
 */

public enum NotAdministeredReasonEnum
{
  NOT_RECORDED,
  DOCTOR_CONFIRMATION_FALSE,
  CANCELLED;

  public CodedNameDto toCodedName()
  {
    return new CodedNameDto(
        name(),
        Dictionary.getEntry(getDictionaryKey(), DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()));
  }

  private String getDictionaryKey()
  {
    return getClass().getSimpleName() + "." + name();
  }
}
