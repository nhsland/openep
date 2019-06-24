package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumše
 */
public enum FdbGenderEnum
{
  MALE(1L),
  FEMALE(2L),
  UNKNOWN(3L);

  private final Long key;

  FdbGenderEnum(final Long key)
  {
    this.key = key;
  }

  public Long getKey()
  {
    return key;
  }
}
