package com.marand.thinkmed.elmdoc.data;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class OptionsDo
{
  private final boolean IncludeInsignificantInactiveIngredients;
  private final boolean IncludeMonographs;

  public OptionsDo(final boolean includeInsignificantInactiveIngredients, final boolean includeMonographs)
  {
    IncludeInsignificantInactiveIngredients = includeInsignificantInactiveIngredients;
    IncludeMonographs = includeMonographs;
  }
}
