package com.marand.thinkmed.elmdoc.data;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("EnumeratedConstantNamingConvention")
public enum ScreeningTypesEnum
{
  DrugDrugInteractions,
  DrugFoodInteractions,
  DrugAlcoholInteractions,
  AllergicReactions,
  DuplicateTherapy,
  AgeContraindications,
  GenderContraindications,
  LactationContraindications,
  PregnancyContraindications,
  DiseaseContraindications,
  DopingAlerts,
  GeneticTesting,
  Dosing;

  public static String getAllNames()
  {
    return Arrays.stream(values())
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }
}
