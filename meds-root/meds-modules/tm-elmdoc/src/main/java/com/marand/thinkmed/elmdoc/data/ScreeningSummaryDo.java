package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class ScreeningSummaryDo implements JsonSerializable
{
  private AlertsDo DrugDrugInteractions;
  private AlertsDo DrugFoodInteractions;
  private AlertsDo DrugAlcoholInteractions;
  private AlertsDo AllergicReactions;
  private AlertsDo AgeContraindications;
  private AlertsDo GenderContraindications;
  private AlertsDo DiseaseContraindications;
  private AlertsDo LactationContraindications;
  private AlertsDo PregnancyContraindications;
  private AlertsDo DuplicateTherapies;
  private AlertsDo GeneticTests;
  private AlertsDo DopingAlerts;

  public void setGeneticTests(final AlertsDo geneticTests)
  {
    GeneticTests = geneticTests;
  }

  public void setDopingAlerts(final AlertsDo dopingAlerts)
  {
    DopingAlerts = dopingAlerts;
  }

  public AlertsDo getGeneticTests()
  {
    return GeneticTests;
  }

  public AlertsDo getDopingAlerts()
  {
    return DopingAlerts;
  }

  public AlertsDo getDrugDrugInteractions()
  {
    return DrugDrugInteractions;
  }

  public void setDrugDrugInteractions(final AlertsDo drugDrugInteractions)
  {
    DrugDrugInteractions = drugDrugInteractions;
  }

  public AlertsDo getDrugFoodInteractions()
  {
    return DrugFoodInteractions;
  }

  public void setDrugFoodInteractions(final AlertsDo drugFoodInteractions)
  {
    DrugFoodInteractions = drugFoodInteractions;
  }

  public AlertsDo getDrugAlcoholInteractions()
  {
    return DrugAlcoholInteractions;
  }

  public void setDrugAlcoholInteractions(final AlertsDo drugAlcoholInteractions)
  {
    DrugAlcoholInteractions = drugAlcoholInteractions;
  }

  public AlertsDo getAllergicReactions()
  {
    return AllergicReactions;
  }

  public void setAllergicReactions(final AlertsDo allergicReactions)
  {
    AllergicReactions = allergicReactions;
  }

  public AlertsDo getAgeContraindications()
  {
    return AgeContraindications;
  }

  public void setAgeContraindications(final AlertsDo ageContraindications)
  {
    AgeContraindications = ageContraindications;
  }

  public AlertsDo getGenderContraindications()
  {
    return GenderContraindications;
  }

  public void setGenderContraindications(final AlertsDo genderContraindications)
  {
    GenderContraindications = genderContraindications;
  }

  public AlertsDo getDiseaseContraindications()
  {
    return DiseaseContraindications;
  }

  public void setDiseaseContraindications(final AlertsDo diseaseContraindications)
  {
    DiseaseContraindications = diseaseContraindications;
  }

  public AlertsDo getLactationContraindications()
  {
    return LactationContraindications;
  }

  public void setLactationContraindications(final AlertsDo lactationContraindications)
  {
    LactationContraindications = lactationContraindications;
  }

  public AlertsDo getPregnancyContraindications()
  {
    return PregnancyContraindications;
  }

  public void setPregnancyContraindications(final AlertsDo pregnancyContraindications)
  {
    PregnancyContraindications = pregnancyContraindications;
  }

  public AlertsDo getDuplicateTherapies()
  {
    return DuplicateTherapies;
  }

  public void setDuplicateTherapies(final AlertsDo duplicateTherapies)
  {
    DuplicateTherapies = duplicateTherapies;
  }
}
