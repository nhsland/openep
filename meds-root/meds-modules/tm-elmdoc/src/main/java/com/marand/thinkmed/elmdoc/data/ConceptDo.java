package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class ConceptDo implements JsonSerializable
{
  private final ConceptTypeEnum Type;
  private String CustomCode;
  private String CustomName;
  private final String Code;
  private final String Name;
  private final Boolean Screen;

  public String getCode()
  {
    return Code;
  }

  public String getName()
  {
    return Name;
  }

  public String getCustomCode()
  {
    return CustomCode;
  }

  public String getCustomName()
  {
    return CustomName;
  }

  public ConceptDo(
      final ConceptTypeEnum type,
      final String customCode,
      final String customName,
      final String code,
      final String name,
      final Boolean screen)
  {
    Type = type;
    CustomCode = customCode;
    CustomName = customName;
    Code = code;
    Name = name;
    Screen = screen;
  }

  public ConceptDo(final ConceptTypeEnum type, final String code, final String name, final Boolean screen)
  {
    Type = type;
    Code = code;
    Name = name;
    Screen = screen;
  }
}
