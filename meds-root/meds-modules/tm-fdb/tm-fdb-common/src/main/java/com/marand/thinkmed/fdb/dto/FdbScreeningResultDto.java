package com.marand.thinkmed.fdb.dto;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class FdbScreeningResultDto implements JsonSerializable
{
  private List<FdbWarningDto> DrugInteractions = new ArrayList<>();
  private List<FdbDrugSensitivityWarningDto> DrugSensitivities = new ArrayList<>();
  private List<FdbWarningDuplicateDto> DuplicateTherapies = new ArrayList<>();
  private List<FdbWarningDuplicateDto> DrugDoublings = new ArrayList<>();
  private List<FdbWarningDuplicateDto> DrugEquivalences = new ArrayList<>();
  private List<FdbPatientChecksWarningDto> PatientChecks = new ArrayList<>();
  private List<FdbScreeningError> ScreeningErrors = new ArrayList<>();
  private ApiError ApiError;

  public ApiError getApiError()
  {
    return ApiError;
  }

  public void setApiError(final ApiError apiError)
  {
    ApiError = apiError;
  }

  public List<FdbWarningDto> getDrugInteractions()
  {
    return DrugInteractions;
  }

  public void setDrugInteractions(final List<FdbWarningDto> drugInteractions)
  {
    DrugInteractions = drugInteractions;
  }

  public List<FdbDrugSensitivityWarningDto> getDrugSensitivities()
  {
    return DrugSensitivities;
  }

  public void setDrugSensitivities(final List<FdbDrugSensitivityWarningDto> drugSensitivities)
  {
    DrugSensitivities = drugSensitivities;
  }

  public List<FdbWarningDuplicateDto> getDuplicateTherapies()
  {
    return DuplicateTherapies;
  }

  public void setDuplicateTherapies(final List<FdbWarningDuplicateDto> duplicateTherapies)
  {
    DuplicateTherapies = duplicateTherapies;
  }

  public List<FdbWarningDuplicateDto> getDrugDoublings()
  {
    return DrugDoublings;
  }

  public void setDrugDoublings(final List<FdbWarningDuplicateDto> drugDoublings)
  {
    DrugDoublings = drugDoublings;
  }

  public List<FdbWarningDuplicateDto> getDrugEquivalences()
  {
    return DrugEquivalences;
  }

  public void setDrugEquivalences(final List<FdbWarningDuplicateDto> drugEquivalences)
  {
    DrugEquivalences = drugEquivalences;
  }

  public void setScreeningErrors(final List<FdbScreeningError> screeningErrors)
  {
    ScreeningErrors = screeningErrors;
  }

  public List<FdbPatientChecksWarningDto> getPatientChecks()
  {
    return PatientChecks;
  }

  public void setPatientChecks(final List<FdbPatientChecksWarningDto> patientChecks)
  {
    PatientChecks = patientChecks;
  }

  public List<FdbScreeningError> getScreeningErrors()
  {
    return ScreeningErrors;
  }

  public void setScreeningError(final List<FdbScreeningError> screeningErrors)
  {
    ScreeningErrors = screeningErrors;
  }
}
