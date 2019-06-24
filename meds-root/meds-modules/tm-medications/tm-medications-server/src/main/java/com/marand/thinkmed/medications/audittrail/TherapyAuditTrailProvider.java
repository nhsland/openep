package com.marand.thinkmed.medications.audittrail;

import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyAuditTrailProvider
{
  TherapyAuditTrailDto getTherapyAuditTrail(
      @NonNull String patientId,
      @NonNull String compositionId,
      @NonNull String ehrOrderName,
      Double patientHeight,
      @NonNull Locale locale,
      @NonNull DateTime when);
}
