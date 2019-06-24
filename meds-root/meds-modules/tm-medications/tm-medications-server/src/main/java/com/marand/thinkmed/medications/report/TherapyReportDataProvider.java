package com.marand.thinkmed.medications.report;

import java.util.Locale;

import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyTemplateReportDto;
import lombok.NonNull;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyReportDataProvider
{
  TherapyDayReportDto getActiveAndPastTherapiesReportData(@NonNull String patientId, @NonNull Locale locale);

  TherapyDayReportDto getPastTherapiesReportData(
      @NonNull String patientId,
      @NonNull Locale locale,
      @NonNull DateTime startDate,
      @NonNull DateTime endDate);

  TherapySurgeryReportDto getTherapySurgeryReport(
      final @NonNull String patientId,
      final @NonNull Locale locale,
      final @NonNull DateTime when);

  TherapyTemplateReportDto getTemplateReport(
      final @NonNull String patientId,
      int numberOfPages,
      final @NonNull Locale locale);
}
