package com.marand.thinkmed.medications.connector.data.object.ehr;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */
public class IdNameDto extends DataTransferObject
{
  private String id;
  private String name;

  public IdNameDto(final String name)
  {
    this.name = name;
  }

  public IdNameDto(final String id, final String name)
  {
    this(name);
    this.id = id;
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

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final IdNameDto idNameDto = (IdNameDto)o;

    if (id != null ? !id.equals(idNameDto.id) : idNameDto.id != null)
    {
      return false;
    }
    return name != null ? name.equals(idNameDto.name) : idNameDto.name == null;
  }

  @Override
  public int hashCode()
  {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("id", id);
    tsb.append("name", name);
  }
}
