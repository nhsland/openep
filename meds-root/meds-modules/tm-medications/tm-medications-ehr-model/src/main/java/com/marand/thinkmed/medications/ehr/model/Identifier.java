package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;

/**
 * @author Nejc Korasa
 */

public class Identifier
{
  @EhrMapped("type")
  private String type;

  @EhrMapped("id")
  private String id;

  public String getType()
  {
    return type;
  }

  public void setType(final String type)
  {
    this.type = type;
  }

  public String getId()
  {
    return id;
  }

  public void setId(final String id)
  {
    this.id = id;
  }
}
