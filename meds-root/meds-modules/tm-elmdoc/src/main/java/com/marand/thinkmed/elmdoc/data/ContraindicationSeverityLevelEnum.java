package com.marand.thinkmed.elmdoc.data;

import java.util.Arrays;

import com.marand.thinkmed.medications.service.WarningSeverity;

/**
 * @author Vid Kumse
 */
public enum ContraindicationSeverityLevelEnum
{
  HIGH(1, WarningSeverity.HIGH_OVERRIDE),
  SIGNIFICANT(2, WarningSeverity.HIGH),
  MODERATE(3, WarningSeverity.OTHER),
  LOW(4, WarningSeverity.OTHER);

  private final Integer key;
  private final WarningSeverity warningSeverity;

  ContraindicationSeverityLevelEnum(final Integer key, final WarningSeverity warningSeverity)
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
        .map(ContraindicationSeverityLevelEnum::getWarningSeverity)
        .findAny()
        .orElse(null);
  }
}
