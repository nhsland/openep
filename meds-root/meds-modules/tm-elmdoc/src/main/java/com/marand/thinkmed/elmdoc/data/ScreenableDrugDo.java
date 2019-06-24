package com.marand.thinkmed.elmdoc.data;

/**
 * @author Mitja Lapajne
 */
public class ScreenableDrugDo extends ConceptDo
{

  public ScreenableDrugDo(
      final String customCode,
      final String customName,
      final String code,
      final String name,
      final Boolean screen)
  {
    super(ConceptTypeEnum.DispensableDrug, customCode, customName, code, name, screen);
  }
}
