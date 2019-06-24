package com.marand.thinkmed.medications.dto.report;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class TherapySurgeryReportDto extends DataTransferObject
{
  private final PatientDataForTherapyReportDto patientData;
  private List<TherapySurgeryReportElementDto> simpleElements = new ArrayList<>();
  private List<TherapySurgeryReportElementDto> complexElements = new ArrayList<>();
  private final String currentDate;

  public TherapySurgeryReportDto(
      final PatientDataForTherapyReportDto patientData,
      final List<TherapySurgeryReportElementDto> simpleElements,
      final List<TherapySurgeryReportElementDto> complexElements,
      final String currentDate)
  {
    this.patientData = patientData;
    this.simpleElements = simpleElements;
    this.complexElements = complexElements;
    this.currentDate = currentDate;
  }

  public List<TherapySurgeryReportElementDto> getComplexElements()
  {
    return complexElements;
  }

  public PatientDataForTherapyReportDto getPatientData()
  {
    return patientData;
  }

  public List<TherapySurgeryReportElementDto> getSimpleElements()
  {
    return simpleElements;
  }

  public String getCurrentDate()
  {
    return currentDate;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientData", patientData)
        .append("simpleElements", simpleElements)
        .append("currentDate", currentDate)
        .append("complexElements", complexElements)
    ;
  }
}
