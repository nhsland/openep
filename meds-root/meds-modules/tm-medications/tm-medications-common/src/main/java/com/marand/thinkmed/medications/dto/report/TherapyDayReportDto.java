package com.marand.thinkmed.medications.dto.report;

import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayReportDto extends DataTransferObject
{
  private final boolean forEmptyReport;
  private int patientSortOrder;
  private PatientDataForTherapyReportDto patientData;

  private List<TherapyDayElementReportDto> simpleElements;
  private List<TherapyDayElementReportDto> complexElements;
  private List<TherapyDayElementReportDto> oxygenElements;
  private List<TherapyDayElementReportDto> prnSimpleElements;
  private List<TherapyDayElementReportDto> prnComplexElements;
  private List<TherapyDayElementReportDto> protocolTitrationSimpleElementsList;
  private List<TherapyDayElementReportDto> protocolTitrationComplexElementsList;
  private List<TherapyDayPastElementDto> pastSimpleElements;
  private List<TherapyDayPastElementDto> pastComplexElements;

  public TherapyDayReportDto(final boolean forEmptyReport)
  {
    this.forEmptyReport = forEmptyReport;
  }

  public TherapyDayReportDto(
      final boolean forEmptyReport,
      final int patientSortOrder,
      final PatientDataForTherapyReportDto patientData,
      final List<TherapyDayElementReportDto> simpleElements,
      final List<TherapyDayElementReportDto> complexElements,
      final List<TherapyDayElementReportDto> oxygenElements,
      final List<TherapyDayElementReportDto> prnSimpleElements,
      final List<TherapyDayElementReportDto> prnComplexElements,
      final List<TherapyDayElementReportDto> protocolTitrationSimpleElementsList,
      final List<TherapyDayElementReportDto> protocolTitrationComplexElementsList,
      final List<TherapyDayPastElementDto> pastSimpleElements,
      final List<TherapyDayPastElementDto> pastComplexElements)
  {
    this.forEmptyReport = forEmptyReport;
    this.patientSortOrder = patientSortOrder;
    this.patientData = patientData;
    this.simpleElements = simpleElements;
    this.complexElements = complexElements;
    this.oxygenElements = oxygenElements;
    this.prnSimpleElements = prnSimpleElements;
    this.prnComplexElements = prnComplexElements;
    this.protocolTitrationSimpleElementsList = protocolTitrationSimpleElementsList;
    this.protocolTitrationComplexElementsList = protocolTitrationComplexElementsList;
    this.pastSimpleElements = pastSimpleElements;
    this.pastComplexElements = pastComplexElements;
  }

  public List<TherapyDayElementReportDto> getPrnComplexElements()
  {
    return prnComplexElements;
  }

  public void setPrnComplexElements(final List<TherapyDayElementReportDto> prnComplexElements)
  {
    this.prnComplexElements = prnComplexElements;
  }

  public List<TherapyDayElementReportDto> getProtocolTitrationSimpleElementsList()
  {
    return protocolTitrationSimpleElementsList;
  }

  public void setProtocolTitrationSimpleElementsList(final List<TherapyDayElementReportDto> protocolTitrationSimpleElementsList)
  {
    this.protocolTitrationSimpleElementsList = protocolTitrationSimpleElementsList;
  }

  public List<TherapyDayElementReportDto> getProtocolTitrationComplexElementsList()
  {
    return protocolTitrationComplexElementsList;
  }

  public void setProtocolTitrationComplexElementsList(final List<TherapyDayElementReportDto> protocolTitrationComplexElementsList)
  {
    this.protocolTitrationComplexElementsList = protocolTitrationComplexElementsList;
  }

  public List<TherapyDayPastElementDto> getPastSimpleElements()
  {
    return pastSimpleElements;
  }

  public void setPastSimpleElements(final List<TherapyDayPastElementDto> pastSimpleElements)
  {
    this.pastSimpleElements = pastSimpleElements;
  }

  public List<TherapyDayElementReportDto> getOxygenElements()
  {
    return oxygenElements;
  }

  public void setOxygenElements(final List<TherapyDayElementReportDto> oxygenElements)
  {
    this.oxygenElements = oxygenElements;
  }

  public boolean isForEmptyReport()
  {
    return forEmptyReport;
  }

  public int getPatientSortOrder()
  {
    return patientSortOrder;
  }

  public void setPatientSortOrder(final int patientSortOrder)
  {
    this.patientSortOrder = patientSortOrder;
  }

  public PatientDataForTherapyReportDto getPatientData()
  {
    return patientData;
  }

  public void setPatientData(final PatientDataForTherapyReportDto patientData)
  {
    this.patientData = patientData;
  }

  public List<TherapyDayElementReportDto> getSimpleElements()
  {
    return simpleElements;
  }

  public void setSimpleElements(final List<TherapyDayElementReportDto> simpleElements)
  {
    this.simpleElements = simpleElements;
  }

  public List<TherapyDayElementReportDto> getComplexElements()
  {
    return complexElements;
  }

  public void setComplexElements(final List<TherapyDayElementReportDto> complexElements)
  {
    this.complexElements = complexElements;
  }

  public List<TherapyDayElementReportDto> getPrnSimpleElements()
  {
    return prnSimpleElements;
  }

  public void setPrnSimpleElements(final List<TherapyDayElementReportDto> prnSimpleElements)
  {
    this.prnSimpleElements = prnSimpleElements;
  }

  public List<TherapyDayPastElementDto> getPastComplexElements()
  {
    return pastComplexElements;
  }

  public void setPastComplexElements(final List<TherapyDayPastElementDto> pastComplexElements)
  {
    this.pastComplexElements = pastComplexElements;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("patientSortOrder", patientSortOrder)
        .append("patientData", patientData)
        .append("simpleElements", simpleElements)
        .append("prnSimpleElements", prnSimpleElements)
        .append("prnComplexElements", prnComplexElements)
        .append("complexElements", complexElements)
        .append("protocolTitrationSimpleElementsList", protocolTitrationSimpleElementsList)
        .append("protocolTitrationComplexElementsList", protocolTitrationComplexElementsList)
        .append("pastSimpleElements", pastSimpleElements)
        .append("pastComplexElements", pastComplexElements)
    ;
  }
}
