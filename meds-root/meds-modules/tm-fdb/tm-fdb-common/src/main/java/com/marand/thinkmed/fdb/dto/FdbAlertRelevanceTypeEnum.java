package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumse
 */
public enum FdbAlertRelevanceTypeEnum
{
  /*Order is important!*/

  SPECIFIC("Specific", 1L),
  RELATED("Related", 2L),
  UNRELATED("Unrelated", 3L);

  private final String name;
  private final Long key;

  FdbAlertRelevanceTypeEnum(final String name, final Long key)
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
