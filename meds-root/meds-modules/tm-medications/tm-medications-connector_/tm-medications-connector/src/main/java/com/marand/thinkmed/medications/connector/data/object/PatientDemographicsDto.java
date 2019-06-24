package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PatientDemographicsDto extends NamedExternalDto implements JsonSerializable
{
  private final DateTime birthDate;
  private final Gender gender;
  private final Gender genderIdentity;
  private final String address;
  private final String patientIdentificatorType;

  public PatientDemographicsDto(
      final String id,
      final String name,
      final DateTime birthDate,
      final Gender gender,
      final Gender genderIdentity,
      final String address,
      final String patientIdentificatorType)
  {
    super(id, name);
    this.birthDate = birthDate;
    this.gender = gender;
    this.genderIdentity = genderIdentity;
    this.address = address;
    this.patientIdentificatorType = patientIdentificatorType;
  }

  public DateTime getBirthDate()
  {
    return birthDate;
  }

  public Gender getGender()
  {
    return gender;
  }

  public Gender getGenderIdentity()
  {
    return genderIdentity;
  }

  public Gender getDisplayGender()
  {
    return genderIdentity != null ? genderIdentity : gender;
  }

  public String getAddress()
  {
    return address;
  }

  public String getPatientIdentificatorType()
  {
    return patientIdentificatorType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("birthDate", birthDate)
        .append("gender", gender)
        .append("genderIdentity", genderIdentity)
        .append("address", address)
        .append("patientIdentificatorType", patientIdentificatorType);
  }
}
