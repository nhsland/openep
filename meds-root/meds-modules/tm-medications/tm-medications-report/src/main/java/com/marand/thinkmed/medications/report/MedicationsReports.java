package com.marand.thinkmed.medications.report;

import com.marand.ispek.print.common.PrintUtils;
import com.marand.ispek.print.jasperreports.JasperReportId;

/**
 * @author Primoz Prislan
 */
public interface MedicationsReports
{
  JasperReportId THERAPY_DAY = new JasperReportId(
      "THERAPY_DAY",
      PrintUtils.REPORTS_PATH + "TherapyDay",
      null,
      "report.TherapyDay.description");

  JasperReportId THERAPY_SURGERY = new JasperReportId(
      "THERAPY_SURGERY",
      PrintUtils.REPORTS_PATH + "TherapySurgery",
      null,
      "report.TherapySurgery.description");

  JasperReportId OUTPATIENT_PRESCRIPTIONS = new JasperReportId(
      "OUTPATIENT_PRESCRIPTIONS",
      PrintUtils.REPORTS_PATH + "eReceptSummary",
      null,
      "report.eReceptSummary.description");

  JasperReportId THERAPY_TEMPLATE = new JasperReportId(
      "THERAPY_TEMPLATE",
      PrintUtils.REPORTS_PATH + "TherapyTemplate",
      null,
      "report.TherapySurgery.description");
}
