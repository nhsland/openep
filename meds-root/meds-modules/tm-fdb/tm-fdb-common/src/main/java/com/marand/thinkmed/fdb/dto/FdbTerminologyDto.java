package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbTerminologyDto implements JsonSerializable
{
  private final String Id;
  private final String Name;
  private final String Terminology;

  public FdbTerminologyDto(final String id, final String name, final String terminology)
  {
    Id = id;
    Name = name;
    Terminology = terminology;
  }

  public String getId()
  {
    return Id;
  }

  public String getName()
  {
    return Name;
  }

  public String getTerminology()
  {
    return Terminology;
  }
}
