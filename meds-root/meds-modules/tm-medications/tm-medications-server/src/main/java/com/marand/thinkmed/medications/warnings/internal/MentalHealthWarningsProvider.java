package com.marand.thinkmed.medications.warnings.internal;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormFromEhrProvider;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.warnings.TherapyWarningsUtils;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MentalHealthWarningsProvider implements InternalWarningsProvider
{
  private MedicationsBo medicationsBo;
  private MentalHealthWarningsHandler mentalHealthWarningsHandler;
  private ConsentFormFromEhrProvider consentFormFromEhrProvider;
  private TherapyWarningsUtils therapyWarningsUtils;

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setMentalHealthWarningsHandler(final MentalHealthWarningsHandler mentalHealthWarningsHandler)
  {
    this.mentalHealthWarningsHandler = mentalHealthWarningsHandler;
  }

  @Autowired
  public void setConsentFormFromEhrProvider(final ConsentFormFromEhrProvider consentFormFromEhrProvider)
  {
    this.consentFormFromEhrProvider = consentFormFromEhrProvider;
  }

  @Autowired
  public void setTherapyWarningsUtils(final TherapyWarningsUtils therapyWarningsUtils)
  {
    this.therapyWarningsUtils = therapyWarningsUtils;
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
    final Opt<MentalHealthDocumentDto> document = consentFormFromEhrProvider.getLatestMentalHealthDocument(patientId);
    if (document.isAbsent())
    {
      return Collections.emptyList();
    }

    final MentalHealthAllowedMedicationsDo allowedMedications = mentalHealthWarningsHandler.getAllowedMedications(document.get());
    return therapyWarningsUtils.extractWarningScreenMedicationDtos(prospectiveTherapies)
        .stream()
        .filter(m -> medicationsBo.isMentalHealthMedication(m.getId()))
        .filter(m -> !mentalHealthWarningsHandler.isMedicationWithRouteAllowed(m.getId(), m.getRouteId(), allowedMedications))
        .map(m -> mentalHealthWarningsHandler.buildMentalHealthMedicationsWarning(new NamedExternalDto(String.valueOf(m.getId()), m.getName())))
        .collect(Collectors.toList());
  }
}
