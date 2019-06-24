package com.marand.thinkmed.medications.connector.data.object;

import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PatientDataForMedicationsDto extends DataTransferObject implements JsonSerializable
{
  private final DateTime birthDate;
  private final String patientName;
  private final Double weightInKg;   //last weight in hospitalization (if patient hospitalized) or last weight in 24h form now
  private final Double heightInCm;   //last height
  private final Gender gender;
  private final List<IdNameDto> diseases;
  private final AllergiesStatus allergiesStatus;
  private final List<IdNameDto> allergies;
  private final MedicationsCentralCaseDto centralCaseDto;

  private boolean witnessingRequired = false;

  public PatientDataForMedicationsDto(
      final DateTime birthDate,
      final String patientName,
      final Double weightInKg,
      final Double heightInCm,
      final Gender gender,
      final List<IdNameDto> diseases,
      final AllergiesStatus allergiesStatus,
      final List<IdNameDto> allergies,
      final MedicationsCentralCaseDto centralCaseDto)
  {
    this.birthDate = birthDate;
    this.patientName = patientName;
    this.weightInKg = weightInKg;
    this.heightInCm = heightInCm;
    this.gender = gender;
    this.diseases = diseases;
    this.allergiesStatus = allergiesStatus;
    this.allergies = allergies;
    this.centralCaseDto = centralCaseDto;
  }

  public DateTime getBirthDate()
  {
    return birthDate;
  }

  public String getPatientName()
  {
    return patientName;
  }

  public Double getWeightInKg()
  {
    return weightInKg;
  }

  public Double getHeightInCm()
  {
    return heightInCm;
  }

  public Gender getGender()
  {
    return gender;
  }

  public AllergiesStatus getAllergiesStatus()
  {
    return allergiesStatus;
  }

  public List<IdNameDto> getDiseases()
  {
    return diseases;
  }

  public List<IdNameDto> getAllergies()
  {
    return allergies;
  }

  public MedicationsCentralCaseDto getCentralCaseDto()
  {
    return centralCaseDto;
  }

  public boolean isWitnessingRequired()
  {
    return witnessingRequired;
  }

  public void setWitnessingRequired(final boolean witnessingRequired)
  {
    this.witnessingRequired = witnessingRequired;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("birthDate", birthDate)
        .append("patientName", patientName)
        .append("weightInKg", weightInKg)
        .append("heightInCm", heightInCm)
        .append("diseases", diseases)
        .append("allergiesStatus", allergiesStatus)
        .append("allergies", allergies)
        .append("witnessingRequired", witnessingRequired)
        .append("centralCaseDto", centralCaseDto);
  }
}
