package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "MethodParameterNamingConvention"})
public class FdbDrug implements JsonSerializable
{
  private final String UserSpecifiedName;
  private final Long SingleId;
  private final FdbNameValue Concept;

  public FdbDrug(final String UserSpecifiedName, final Long Id, final FdbNameValue concept)
  {
    this.UserSpecifiedName = UserSpecifiedName;
    SingleId = Id;
    Concept = concept;
  }

  public String getUserSpecifiedName()
  {
    return UserSpecifiedName;
  }

  public Long getId()
  {
    return SingleId;
  }

  public FdbNameValue getConcept()
  {
    return Concept;
  }
}
