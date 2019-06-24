package com.marand.thinkmed.medications.warnings.internal;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toSet;

/**
 * @author Nejc Korasa
 */

@Component
public class CustomWarningsProvider implements InternalWarningsProvider
{
  private MedicationsDao medicationsDao;

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Override
  public List<MedicationsWarningDto> getWarnings(
      final @NonNull String patientId,
      final @NonNull List<TherapyDto> activeTherapies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull DateTime when,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Locale locale)
  {
    return medicationsDao.getCustomWarningsForMedication(
        prospectiveTherapies.stream().flatMap(t -> t.getMedicationIds().stream()).collect(toSet()),
        when);
  }
}
