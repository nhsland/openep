package com.marand.thinkmed.elmdoc.data;

/**
 * @author Mitja Lapajne
 */
public class AllergenClassDo extends ConceptDo
{
  public AllergenClassDo(
      final String customCode,
      final String customName,
      final String code,
      final String name,
      final Boolean screen)
  {
    super(ConceptTypeEnum.AllergenClass, customCode, customName, code, name, screen);
  }

  public AllergenClassDo(final String code, final String name, final Boolean screen)
  {
    super(ConceptTypeEnum.AllergenClass, code, name, screen);
  }
}
