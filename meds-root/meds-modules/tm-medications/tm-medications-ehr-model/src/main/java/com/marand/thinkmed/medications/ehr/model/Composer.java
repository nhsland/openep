
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;

/**
 * @author Mitja Lapajne
 */
public class Composer
{
  @EhrMapped("external_ref/id/value")
  private String id;

  @EhrMapped("name")
  private String name;

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
}
