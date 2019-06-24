
package com.marand.thinkmed.medications.ehr.model;

import com.marand.thinkehr.mapping.annotation.EhrMapped;

/**
 * @author Nejc Korasa
 */

public class Participation
{
  @EhrMapped("performer/name")
  private String name;

  @EhrMapped("performer/external_ref/id/value")
  private String id;

  @EhrMapped("function/value")
  private String function;

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
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

  public String getFunction()
  {
    return function;
  }

  public void setFunction(final String function)
  {
    this.function = function;
  }
}
