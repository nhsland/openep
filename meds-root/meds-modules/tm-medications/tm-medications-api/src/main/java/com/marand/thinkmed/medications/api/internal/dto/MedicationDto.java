package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public class MedicationDto extends DataTransferObject implements JsonSerializable
{
  private Long id;
  private String name;
  private String genericName;
  private MedicationTypeEnum medicationType;

  private String displayName;

  public MedicationDto() { }

  public MedicationDto(final Long id, final String name)
  {
    this.id = id;
    this.name = name;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(final Long id)
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

  public String getGenericName()
  {
    return genericName;
  }

  public void setGenericName(final String genericName)
  {
    this.genericName = genericName;
  }

  public MedicationTypeEnum getMedicationType()
  {
    return medicationType;
  }

  public void setMedicationType(final MedicationTypeEnum medicationType)
  {
    this.medicationType = medicationType;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setDisplayName(final String displayName)
  {
    this.displayName = displayName;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id)
        .append("name", name)
        .append("genericName", genericName)
        .append("medicationType", medicationType)
        .append("displayName", displayName);
  }
}
