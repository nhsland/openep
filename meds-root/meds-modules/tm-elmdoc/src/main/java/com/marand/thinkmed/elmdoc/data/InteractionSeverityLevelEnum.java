package com.marand.thinkmed.elmdoc.data;

import java.util.Arrays;

import com.marand.thinkmed.medications.service.WarningSeverity;

/**
 * @author Vid Kumse
 */
public enum InteractionSeverityLevelEnum
{
  HIGH(10000, WarningSeverity.HIGH_OVERRIDE),
  SIGNIFICANT(20000, WarningSeverity.HIGH),
  LOW(30000, WarningSeverity.OTHER);

  private final Integer key;
  private final WarningSeverity warningSeverity;

  InteractionSeverityLevelEnum(final Integer key, final WarningSeverity warningSeverity)
  {
    this.key = key;
    this.warningSeverity = warningSeverity;
  }

  public Integer getKey()
  {
    return key;
  }

  public WarningSeverity getWarningSeverity()
  {
    return warningSeverity;
  }

  public static WarningSeverity getWarningSeverityByKey(final Integer key)
  {
    return Arrays.stream(values())
        .filter(e -> e.getKey().equals(key))
        .map(InteractionSeverityLevelEnum::getWarningSeverity)
        .findAny()
        .orElse(null);
  }
}
