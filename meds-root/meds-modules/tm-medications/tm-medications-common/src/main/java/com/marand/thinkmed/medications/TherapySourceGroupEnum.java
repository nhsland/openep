package com.marand.thinkmed.medications;

import java.util.Arrays;
import java.util.EnumSet;

import com.marand.maf.core.Pair;

/**
 * @author Nejc Korasa
 */
public enum TherapySourceGroupEnum
{
  // SOURCES for creating medication on admission compositions
  LAST_HOSPITALIZATION("LAST_HOSPITALIZATION_"),
  LAST_DISCHARGE_MEDICATIONS("LAST_DISCHARGE_MEDICATIONS_"),

  // SOURCES for creating medication on discharge compositions
  MEDICATION_ON_ADMISSION("MEDICATION_ON_ADMISSION_"),
  STOPPED_ADMISSION_MEDICATION("STOPPED_ADMISSION_MEDICATION_"),
  INPATIENT_THERAPIES("INPATIENT_THERAPIES_");

  private final String prefix;

  public static final EnumSet<TherapySourceGroupEnum> ADMISSION_SOURCE = EnumSet.of(MEDICATION_ON_ADMISSION, STOPPED_ADMISSION_MEDICATION);

  TherapySourceGroupEnum(final String prefix)
  {
    this.prefix = prefix;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public String getDictionaryKey()
  {
    return getClass().getSimpleName() + "." + name();
  }

  public static String createEhrString(final TherapySourceGroupEnum groupEnum, final String compositionId)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append(groupEnum.getPrefix()).append(compositionId);
    return builder.toString();
  }

  public static Pair<TherapySourceGroupEnum, String> getSourceGroupEnumAndSourceIdFromEhrString(final String ehrString)
  {
    return Arrays.stream(values())
        .filter(groupEnum -> ehrString.startsWith(groupEnum.getPrefix()))
        .findFirst()
        .map(groupEnum -> Pair.of(groupEnum, ehrString.replaceFirst(groupEnum.getPrefix(), "")))
        .orElse(null);
  }
}
