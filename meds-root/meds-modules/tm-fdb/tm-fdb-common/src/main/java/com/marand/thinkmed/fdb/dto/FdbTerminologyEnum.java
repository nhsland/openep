package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumše
 */
public enum FdbTerminologyEnum
{
  MDDF("MDDF"),
  SNOMED("SNOMEDCT");

  private String name;

  FdbTerminologyEnum(final String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
