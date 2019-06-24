package com.marand.thinkmed.medications.report;

import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyTemplateReportDto;
import lombok.NonNull;

/**
 * @author DusanM
 * @author Mitja Lapajne
 */
public interface TherapyReportCreator
{
  TherapyReportPdfDto createPdfReport(@NonNull String username, TherapyDayReportDto reportData);

  byte[] createTherapyTemplateReport(TherapyTemplateReportDto reportData);

  byte[] printTherapySurgeryReport(TherapySurgeryReportDto reportData);
}
