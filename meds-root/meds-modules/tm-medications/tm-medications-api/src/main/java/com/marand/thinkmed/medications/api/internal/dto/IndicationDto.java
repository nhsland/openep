package com.marand.thinkmed.medications.api.internal.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Mitja Lapajne
 */

public class IndicationDto implements Serializable
{
  private String id;
  private String name;

  public IndicationDto(final String id, final String name)
  {
    this.id = id;
    this.name = name;
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
  public final String toString()
  {
    final ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
    tsb.append("id", id);
    tsb.append("name", name);
    return tsb.toString();
  }
}
