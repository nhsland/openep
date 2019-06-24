package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumše
 */
public enum FdbInteractionsSeverityEnums
{
  LOW_RISK("LowRisk", 1L),
  MODERATE_RISK("ModerateRisk", 2L),
  SIGNIFICANT_RISK("SignificantRisk", 3L),
  HIGH_RISK("HighRisk", 4L);

  private final String name;
  private final Long key;

  FdbInteractionsSeverityEnums(final String name, final Long key)
  {
    this.name = name;
    this.key = key;
  }

  public String getName()
  {
    return name;
  }

  public Long getKey()
  {
    return key;
  }
}
