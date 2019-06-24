package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumše
 */
public enum FdbConditionAlertSeverityEnum
{
  CONTRAINDICATION("Contraindication", 1L),
  PRECAUTION("Precaution", 2L);

  private final String name;
  private final Long key;

  FdbConditionAlertSeverityEnum(final String name, final Long key)
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
