package com.marand.thinkmed.medications.api.external.dto;

/**
 * @author Mitja Lapajne
 */
public class MedicationDto
{
  private String id;
  private String name;
  private boolean controlledDrug;

  public MedicationDto(final String id, final String name, final boolean controlledDrug)
  {
    this.id = id;
    this.name = name;
    this.controlledDrug = controlledDrug;
  }

  public String getId()
  {
    return id;
  }

  public void setId(final String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public boolean isControlledDrug()
  {
    return controlledDrug;
  }

  public void setControlledDrug(final boolean controlledDrug)
  {
    this.controlledDrug = controlledDrug;
  }
}
