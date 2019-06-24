package com.marand.thinkmed.elmdoc.data;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class ScreenRequestDo implements JsonSerializable
{
  private String ScreeningTypes;
  private PatientDo Patient;
  private List<ScreenableDrugDo> Drugs = new ArrayList<>();
  private List<AllergenClassDo> Allergies = new ArrayList<>();
  private List<DiseaseDo> Diseases = new ArrayList<>();
  private OptionsDo Options;

  public String getScreeningTypes()
  {
    return ScreeningTypes;
  }

  public void setScreeningTypes(final String screeningTypes)
  {
    ScreeningTypes = screeningTypes;
  }

  public PatientDo getPatient()
  {
    return Patient;
  }

  public void setPatient(final PatientDo patient)
  {
    Patient = patient;
  }

  public List<ScreenableDrugDo> getDrugs()
  {
    return Drugs;
  }

  public void setDrugs(final List<ScreenableDrugDo> drugs)
  {
    Drugs = drugs;
  }

  public List<AllergenClassDo> getAllergies()
  {
    return Allergies;
  }

  public void setAllergies(final List<AllergenClassDo> allergies)
  {
    Allergies = allergies;
  }

  public List<DiseaseDo> getDiseases()
  {
    return Diseases;
  }

  public void setDiseases(final List<DiseaseDo> diseases)
  {
    Diseases = diseases;
  }

  public OptionsDo getOptions()
  {
    return Options;
  }

  public void setOptions(final OptionsDo options)
  {
    Options = options;
  }
}
