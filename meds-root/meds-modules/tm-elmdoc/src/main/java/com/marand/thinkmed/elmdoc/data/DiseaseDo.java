package com.marand.thinkmed.elmdoc.data;

/**
 * @author Mitja Lapajne
 */
public class DiseaseDo extends ConceptDo
{
  public DiseaseDo(
      final String customCode,
      final String customName,
      final String code,
      final String name,
      final Boolean screen)
  {
    super(ConceptTypeEnum.ICD10CM, customCode, customName, code, name, screen);
  }

  public DiseaseDo(
      final String code,
      final String name,
      final Boolean screen)
  {
    super(ConceptTypeEnum.ICD10CM, code, name, screen);
  }
}
