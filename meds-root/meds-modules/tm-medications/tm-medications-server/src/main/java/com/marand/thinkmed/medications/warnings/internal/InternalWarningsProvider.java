package com.marand.thinkmed.medications.warnings.internal;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import lombok.NonNull;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public interface InternalWarningsProvider
{
  List<MedicationsWarningDto> getWarnings(
      final @NonNull String patientId,
      final @NonNull List<TherapyDto> activeTherapies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull DateTime when,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Locale locale);
}
