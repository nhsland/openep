package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class DoseFormDto
{
  private String name;

  public DoseFormDto(final String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }
}
