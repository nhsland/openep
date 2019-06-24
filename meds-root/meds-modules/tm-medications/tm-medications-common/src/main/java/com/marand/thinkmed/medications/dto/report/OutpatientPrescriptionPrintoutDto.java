package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.demographics.data.Gender;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Vid Kumse
 */

public class OutpatientPrescriptionPrintoutDto extends DataTransferObject
{
  private String patientName;
  private String birthDate;
  private Gender gender;
  private String prescriptionsPackageId;
  private String prescriber;
  private String ward;
  private String dateOfPrescription;

  private final List<PrescriptionForPrintoutDto> prescriptions = new ArrayList<>();

  public OutpatientPrescriptionPrintoutDto()
  {
  }

  public String getPatientName()
  {
    return patientName;
  }

  public void setPatientName(final String patientName)
  {
    this.patientName = patientName;
  }

  public String getBirthDate()
  {
    return birthDate;
  }

  public void setBirthDate(final String birthDate)
  {
    this.birthDate = birthDate;
  }

  public Gender getGender()
  {
    return gender;
  }

  public void setGender(final Gender gender)
  {
    this.gender = gender;
  }

  public String getPrescriptionsPackageId()
  {
    return prescriptionsPackageId;
  }

  public void setPrescriptionsPackageId(final String prescriptionsPackageId)
  {
    this.prescriptionsPackageId = prescriptionsPackageId;
  }

  public String getPrescriber()
  {
    return prescriber;
  }

  public void setPrescriber(final String prescriber)
  {
    this.prescriber = prescriber;
  }

  public String getWard()
  {
    return ward;
  }

  public void setWard(final String ward)
  {
    this.ward = ward;
  }

  public String getDateOfPrescription()
  {
    return dateOfPrescription;
  }

  public void setDateOfPrescription(final String dateOfPrescription)
  {
    this.dateOfPrescription = dateOfPrescription;
  }

  public List<PrescriptionForPrintoutDto> getPrescriptions()
  {
    return prescriptions;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientName", patientName)
        .append("birthDatet", birthDate)
        .append("gender", gender)
        .append("prescriptionsPackageId", prescriptionsPackageId)
        .append("prescriber", prescriber)
        .append("ward", ward)
        .append("dateOfPrescription", dateOfPrescription)
        .append("prescriptions", prescriptions)
    ;
  }
}
