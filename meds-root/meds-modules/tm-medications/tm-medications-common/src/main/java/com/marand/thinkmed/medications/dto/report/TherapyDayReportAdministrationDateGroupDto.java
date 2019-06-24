package com.marand.thinkmed.medications.dto.report;

/**
 * @author Vid Kumse
 */

import java.util.ArrayList;
import java.util.List;

public class TherapyDayReportAdministrationDateGroupDto
{
  private String date;
  private List<TherapyDayReportAdministrationDto> therapyDayReportAdministrationDtos = new ArrayList<>();

  public TherapyDayReportAdministrationDateGroupDto(
      final String date,
      final List<TherapyDayReportAdministrationDto> therapyDayReportAdministrationDtos)
  {
    this.date = date;
    this.therapyDayReportAdministrationDtos = therapyDayReportAdministrationDtos;
  }

  public TherapyDayReportAdministrationDateGroupDto(final String date)
  {
    this.date = date;
  }

  public String getDate()
  {
    return date;
  }

  public void setDate(final String date)
  {
    this.date = date;
  }

  public List<TherapyDayReportAdministrationDto> getTherapyDayReportAdministrationDtos()
  {
    return therapyDayReportAdministrationDtos;
  }

  public void setTherapyDayReportAdministrationDtos(final List<TherapyDayReportAdministrationDto> therapyDayReportAdministrationDtos)
  {
    this.therapyDayReportAdministrationDtos = therapyDayReportAdministrationDtos;
  }
}
