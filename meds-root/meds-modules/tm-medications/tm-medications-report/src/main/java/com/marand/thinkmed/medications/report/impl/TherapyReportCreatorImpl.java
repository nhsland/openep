package com.marand.thinkmed.medications.report.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.marand.ispek.common.Dictionary;
import com.marand.ispek.print.common.PrintContext;
import com.marand.ispek.print.common.ReportAction;
import com.marand.ispek.print.jasperreports.JasperReportId;
import com.marand.ispek.print.jasperreports.JasperReportPrintParameters;
import com.marand.ispek.print.jasperreports.JasperReportsUtils;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportUtils;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyTemplateReportDto;
import com.marand.thinkmed.medications.report.MedicationsReports;
import com.marand.thinkmed.medications.report.TherapyReportCreator;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author DusanM
 * @author Mitja Lapajne
 */
@Component
public class TherapyReportCreatorImpl implements TherapyReportCreator, InitializingBean
{

  @Override
  public void afterPropertiesSet()
  {
    TherapyDayReportUtils.init(Dictionary.getDelegate());
  }

  @Override
  public TherapyReportPdfDto createPdfReport(final @NonNull String username, final TherapyDayReportDto reportData)
  {
    return buildPdfReport(username, reportData, new DateTime());
  }

  private TherapyReportPdfDto buildPdfReport(
      final String username,
      final TherapyDayReportDto reportData,
      final DateTime when)
  {
    final JasperReportId jasperReportId = MedicationsReports.THERAPY_DAY;

    if (reportData != null && reportData.getPatientData() != null)
    {
      final JasperReportPrintParameters parameters = getJasperReportPrintParameters(
          jasperReportId,
          username,
          when.toDate(),
          reportData);

      final String pdfFilename = getPdfFilename(reportData);
      final byte[] pdfData = JasperReportsUtils.createPdfByteArray(parameters);
      return new TherapyReportPdfDto(pdfData, pdfFilename);
    }
    return null;
  }

  @Override
  public byte[] printTherapySurgeryReport(final TherapySurgeryReportDto reportData)
  {
    if (reportData == null)
    {
      return null;
    }

    final JasperReportPrintParameters parameters =
        new JasperReportPrintParameters(
            MedicationsReports.THERAPY_SURGERY,
            Collections.singleton(reportData),
            ReportAction.PDF,
            PrintContext.INSTANCE.getValuesProvider().getLoggedUserName(),
            false);

    return JasperReportsUtils.createPdfByteArray(parameters);
  }

  @Override
  public byte[] createTherapyTemplateReport(final TherapyTemplateReportDto reportData)
  {
    if (reportData == null)
    {
      return null;
    }

    final JasperReportPrintParameters parameters =
        new JasperReportPrintParameters(
            MedicationsReports.THERAPY_TEMPLATE,
            Collections.singleton(reportData),
            ReportAction.PDF,
            PrintContext.INSTANCE.getValuesProvider().getLoggedUserName(),
            false);

    return JasperReportsUtils.createPdfByteArray(parameters);
  }

  private JasperReportPrintParameters getJasperReportPrintParameters(
      final @NonNull JasperReportId jasperReportId,
      final @NonNull String requestingUserName,
      final @NonNull Date when,
      final @NonNull TherapyDayReportDto reportData)
  {
    final JasperReportPrintParameters parameters =
        new JasperReportPrintParameters(
            jasperReportId,
            Collections.singleton(reportData),
            ReportAction.PDF,
            requestingUserName,
            false);

    parameters.addReportParameter("therapyApplicationStartDate", when);
    parameters.addReportParameter("showLegend", false);

    if (!reportData.isForEmptyReport())
    {
      parameters.addReportParameter("showSimpleGroups", shouldShowGroups(reportData.getSimpleElements()));
      parameters.addReportParameter("showComplexGroups", shouldShowGroups(reportData.getComplexElements()));
    }
    return parameters;
  }

  private String getPdfFilename(final @NonNull TherapyDayReportDto reportData)
  {
    final PatientDataForTherapyReportDto patientData = reportData.getPatientData();
    final StringBuilder pdfName = new StringBuilder();

    return pdfName
        .append(patientData.getOrganizationShort())
        .append(" - ")
        .append(patientData.getPatientName())
        .append(" - ")
        .append(patientData.getPatientIdentificatorType())
        .append(patientData.getPatientIdentificator())
        .append(".pdf")
        .toString();
  }

  private boolean shouldShowGroups(final List<TherapyDayElementReportDto> elements)
  {
    return elements != null && elements.stream().anyMatch(e -> StringUtils.isNotBlank(e.getCustomGroupName()));
  }
}
