package com.marand.thinkmed.fdb.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbScreeningDto implements JsonSerializable
{
  private List<Integer> ScreeningModules = new ArrayList<>();
  private FdbPatientDto PatientInformation;
  private List<FdbTerminologyDto> Conditions = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> Allergens = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> CurrentDrugs = new ArrayList<>();
  private List<FdbTerminologyWithConceptDto> ProspectiveDrugs = new ArrayList<>();

  public List<Integer> getScreeningModules()
  {
    return ScreeningModules;
  }

  public void setScreeningModules(final List<Integer> screeningModules)
  {
    ScreeningModules = screeningModules;
  }

  public FdbPatientDto getPatientInformation()
  {
    return PatientInformation;
  }

  public void setPatientInformation(final FdbPatientDto patientInformation)
  {
    PatientInformation = patientInformation;
  }

  public List<FdbTerminologyDto> getConditions()
  {
    return Conditions;
  }

  public void setConditions(final List<FdbTerminologyDto> conditions)
  {
    Conditions = conditions;
  }

  public List<FdbTerminologyWithConceptDto> getAllergens()
  {
    return Allergens;
  }

  public void setAllergens(final List<FdbTerminologyWithConceptDto> allergens)
  {
    Allergens = allergens;
  }

  public List<FdbTerminologyWithConceptDto> getCurrentDrugs()
  {
    return CurrentDrugs;
  }

  public void setCurrentDrugs(final List<FdbTerminologyWithConceptDto> currentDrugs)
  {
    CurrentDrugs = currentDrugs;
  }

  public List<FdbTerminologyWithConceptDto> getProspectiveDrugs()
  {
    return ProspectiveDrugs;
  }

  public void setProspectiveDrugs(final List<FdbTerminologyWithConceptDto> prospectiveDrugs)
  {
    ProspectiveDrugs = prospectiveDrugs;
  }
}
