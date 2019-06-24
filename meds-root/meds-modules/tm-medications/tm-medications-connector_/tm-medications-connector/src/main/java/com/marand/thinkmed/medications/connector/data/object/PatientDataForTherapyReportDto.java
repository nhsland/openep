package com.marand.thinkmed.medications.connector.data.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalCatalogDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
public final class PatientDataForTherapyReportDto extends DataTransferObject implements JsonSerializable
{
  private final boolean inpatient;
  private final String patientName;
  private final String birthDateAndAge;
  private final Gender gender;
  private final String patientIdentificatorType;
  private final String patientIdentificator;
  private final String centralCaseIdNumber;
  private final String organization;
  private final String organizationShort;
  private final String doctor;
  private final String roomAndBed;
  private final String admissionDate;
  private final Integer hospitalizationConsecutiveDay;
  private List<ExternalCatalogDto> diseases = new ArrayList<>();
  private String weight;
  private List<String> allergies;
  private AllergiesStatus allergiesStatus;
  private String careProviderId;
  private final String address;

  public PatientDataForTherapyReportDto(
      final boolean inpatient,
      final String patientName,
      final String birthDateAndAge,
      final Gender gender,
      final String patientIdentificatorType,
      final String patientIdentificator,
      final String centralCaseIdNumber,
      final String organization,
      final String organizationShort,
      final String doctor,
      final String roomAndBed,
      final String admissionDate,
      final Integer hospitalizationConsecutiveDay,
      final List<ExternalCatalogDto> diseases,
      final String weight,
      final List<String> allergies,
      final AllergiesStatus allergiesStatus,
      final String careProviderId,
      final String address)
  {
    this.inpatient = inpatient;
    this.patientName = patientName;
    this.birthDateAndAge = birthDateAndAge;
    this.gender = gender;
    this.patientIdentificatorType = patientIdentificatorType;
    this.patientIdentificator = patientIdentificator;
    this.centralCaseIdNumber = centralCaseIdNumber;
    this.organization = organization;
    this.organizationShort = organizationShort;
    this.roomAndBed = roomAndBed;
    this.admissionDate = admissionDate;
    this.hospitalizationConsecutiveDay = hospitalizationConsecutiveDay;
    this.diseases = diseases;
    this.weight = weight;
    this.doctor = doctor;
    this.allergies = allergies;
    this.allergiesStatus = allergiesStatus;
    this.careProviderId = careProviderId;
    this.address = address;
  }

  public boolean isInpatient()
  {
    return inpatient;
  }

  public String getPatientName()
  {
    return patientName;
  }

  public String getBirthDateAndAge()
  {
    return birthDateAndAge;
  }

  public Gender getGender()
  {
    return gender;
  }

  public String getPatientIdentificatorType()
  {
    return patientIdentificatorType;
  }

  public String getPatientIdentificator()
  {
    return patientIdentificator;
  }

  public String getCentralCaseIdNumber()
  {
    return centralCaseIdNumber;
  }

  public String getOrganization()
  {
    return organization;
  }

  public String getOrganizationShort()
  {
    return organizationShort;
  }

  public String getDoctor()
  {
    return doctor;
  }

  public String getRoomAndBed()
  {
    return roomAndBed;
  }

  public String getAdmissionDate()
  {
    return admissionDate;
  }

  public Integer getHospitalizationConsecutiveDay()
  {
    return hospitalizationConsecutiveDay;
  }

  public List<ExternalCatalogDto> getDiseases()
  {
    return diseases;
  }

  public void setDiseases(List<ExternalCatalogDto> diseases)
  {
    this.diseases=diseases;
  }

  public String getWeight()
  {
    return weight;
  }

  public void setWeight(final String weight)
  {
    this.weight = weight;
  }

  public List<String> getAllergies()
  {
    return allergies;
  }

  public void setAllergies(final List<String> allergies)
  {
    this.allergies = allergies;
  }

  public AllergiesStatus getAllergiesStatus()
  {
    return allergiesStatus;
  }

  public void setAllergiesStatus(final AllergiesStatus allergiesStatus)
  {
    this.allergiesStatus = allergiesStatus;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  public String getAddress()
  {
    return address;
  }

  public String getStringDiseases()
  {
    final Set<String> diseaseDescriptions = new HashSet<>();
    final StringBuilder returnValue = new StringBuilder();
    for (final ExternalCatalogDto dto : diseases)
    {
      final String diseaseDescription = dto.getCode() + " - " + dto.getName() + "  ";
      if (!diseaseDescriptions.contains(diseaseDescription))
      {
        returnValue.append(diseaseDescription);
        diseaseDescriptions.add(diseaseDescription);
      }
    }
    return returnValue.toString();
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("patientName", patientName)
        .append("birthDate", birthDateAndAge)
        .append("gender", gender)
        .append("patientIdNumber", patientIdentificator)
        .append("centralCaseIdNumber", centralCaseIdNumber)
        .append("organization", organization)
        .append("organizationShort", organizationShort)
        .append("doctor", doctor)
        .append("roomAndBed", roomAndBed)
        .append("hospitalizationConsecutiveDay", hospitalizationConsecutiveDay)
        .append("diseases", diseases)
        .append("allergies", allergies)
        .append("allergiesStatus", allergiesStatus)
        .append("careProviderId", careProviderId)
        .append("address", address);
  }
}