package com.marand.thinkmed.medications.warnings.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AntipsychoticMaxDoseWarningsProvider implements InternalWarningsProvider
{
  private AntipsychoticMaxDoseWarningsHandler antipsychoticsWarningsHandler;
  private MedsProperties medsProperties;

  @Autowired
  public void setAntipsychoticsWarningsHandler(final AntipsychoticMaxDoseWarningsHandler antipsychoticsWarningsHandler)
  {
    this.antipsychoticsWarningsHandler = antipsychoticsWarningsHandler;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
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
    final List<TherapyDto> allTherapies = new ArrayList<>();
    allTherapies.addAll(prospectiveTherapies);
    allTherapies.addAll(activeTherapies);

    //noinspection IfMayBeConditional
    if (medsProperties.getCumulativeAntipsychoticDoseEnabled() && antipsychoticsWarningsHandler.hasAntipsychotics(prospectiveTherapies))
    {
      return antipsychoticsWarningsHandler.getWarnings(patientId, allTherapies);
    }
    else
    {
      return Collections.emptyList();
    }
  }
}
