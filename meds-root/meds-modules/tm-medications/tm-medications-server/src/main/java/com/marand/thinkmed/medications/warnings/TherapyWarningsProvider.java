package com.marand.thinkmed.medications.warnings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.warnings.external.ExternalWarningsProvider;
import com.marand.thinkmed.medications.warnings.internal.InternalWarningsProvider;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

/**
 * @author Klavdij Lapajne
 */

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
@Component
public class TherapyWarningsProvider
{
  private MedicationsBo medicationsBo;
  private ExternalWarningsProvider externalWarningsProvider;
  private List<InternalWarningsProvider> internalWarningsProviders;

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setExternalWarningsProvider(final ExternalWarningsProvider externalWarningsProvider)
  {
    this.externalWarningsProvider = externalWarningsProvider;
  }

  @Autowired
  public void setInternalWarningsProviders(final List<InternalWarningsProvider> internalWarningsProviders)
  {
    this.internalWarningsProviders = internalWarningsProviders;
  }

  public List<MedicationsWarningDto> findCurrentTherapiesWarnings(
      final @NonNull String patientId,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final boolean loadInternalWarnings,
      final @NonNull DateTime when,
      final Locale locale)
  {
    return findMedicationWarnings(
        patientId,
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseases,
        allergies,
        medicationsBo.getTherapies(
            patientId,
            Intervals.infiniteFrom(when),
            null,
            null,
            null),
        loadInternalWarnings,
        false,
        when,
        locale);
  }

  public List<MedicationsWarningDto> findMedicationWarnings(
      final @NonNull String patientId,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final boolean loadInternalWarnings,
      final boolean includeActiveTherapies,
      final @NonNull DateTime when,
      final Locale locale)
  {
    if (prospectiveTherapies.isEmpty())
    {
      return Collections.emptyList();
    }

    final List<TherapyDto> activeTherapies = new ArrayList<>();
    if (includeActiveTherapies)
    {
      activeTherapies.addAll(medicationsBo.getTherapies(
          patientId,
          Intervals.infiniteFrom(when),
          null,
          null,
          null));
    }

    final List<MedicationsWarningDto> warnings = new ArrayList<>();
    warnings.addAll(getExternalWarnings(
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseases,
        allergies,
        prospectiveTherapies,
        activeTherapies,
        when));
    warnings.addAll(getInternalWarnings(
        patientId,
        loadInternalWarnings,
        when,
        prospectiveTherapies,
        activeTherapies,
        dateOfBirth,
        patientWeightInKg,
        locale));
    warnings.sort(MedicationsWarningDto::compareTo);

    return warnings;
  }

  private List<MedicationsWarningDto> getExternalWarnings(
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull List<TherapyDto> activeTherapies,
      final @NonNull DateTime when)
  {
    return externalWarningsProvider.getExternalWarnings(
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseases,
        allergies,
        prospectiveTherapies,
        activeTherapies,
        when);
  }

  private List<MedicationsWarningDto> getInternalWarnings(
      final @NonNull String patientId,
      final boolean loadInternalWarnings,
      final @NonNull DateTime when,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull List<TherapyDto> activeTherapies,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Locale locale)
  {
    final List<MedicationsWarningDto> internalWarnings = new ArrayList<>();
    if (loadInternalWarnings)
    {
      internalWarnings.addAll(internalWarningsProviders
                                  .stream()
                                  .flatMap(wp -> wp.getWarnings(
                                      patientId,
                                      activeTherapies,
                                      prospectiveTherapies,
                                      when,
                                      dateOfBirth,
                                      patientWeightInKg,
                                      locale).stream())
                                  .collect(toList()));
    }
    return internalWarnings;
  }
}
