package com.marand.thinkmed.medications.dto.report;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Vid Kumse
 */

public class TherapyTemplateReportDto extends DataTransferObject
{
  private final int numberOfPages;
  private final PatientDataForTherapyReportDto patientData;

  public TherapyTemplateReportDto(
      final int numberOfPages,
      final PatientDataForTherapyReportDto patientData)
  {
    this.numberOfPages = numberOfPages;
    this.patientData = patientData;
  }

  public int getNumberOfPages()
  {
    return numberOfPages;
  }

  public PatientDataForTherapyReportDto getPatientData()
  {
    return patientData;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("numberOfPages", numberOfPages)
        .append("patientData", patientData);
  }
}
