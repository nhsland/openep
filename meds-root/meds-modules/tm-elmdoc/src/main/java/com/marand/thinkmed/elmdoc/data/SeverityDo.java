package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class SeverityDo implements JsonSerializable
{
  private Integer Level;
  private String Name;

  public SeverityDo(final Integer level, final String name)
  {
    Level = level;
    Name = name;
  }

  public Integer getLevel()
  {
    return Level;
  }

  public void setLevel(final Integer level)
  {
    Level = level;
  }

  public String getName()
  {
    return Name;
  }

  public void setName(final String name)
  {
    Name = name;
  }
}
