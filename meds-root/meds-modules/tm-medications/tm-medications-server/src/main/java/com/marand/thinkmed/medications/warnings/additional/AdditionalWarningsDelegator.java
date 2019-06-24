package com.marand.thinkmed.medications.warnings.additional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdditionalWarningsDelegator
{
  private Map<String, AdditionalWarningsProvider> additionalWarningsProviders;

  @Autowired
  public void setAdditionalWarningsProviders(final Map<String, AdditionalWarningsProvider> additionalWarningsProviders)
  {
    //noinspection AssignmentOrReturnOfFieldWithMutableType
    this.additionalWarningsProviders = additionalWarningsProviders;
  }

  public Opt<AdditionalWarningsDto> getAdditionalWarnings(
      final @NonNull Collection<AdditionalWarningsType> types,
      final @NonNull String patientId,
      final @NonNull PatientDataForMedicationsDto patientData,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final List<AdditionalWarningsDto> additionalWarnings = types
        .stream()
        .map(type -> getProviderImplementation(type).getAdditionalWarnings(patientId, patientData, when, locale))
        .filter(Opt::isPresent)
        .map(Opt::get)
        .collect(Collectors.toList());

    return joinWarnings(additionalWarnings);
  }

  private Opt<AdditionalWarningsDto> joinWarnings(final List<AdditionalWarningsDto> additionalWarnings)
  {
    if (additionalWarnings.size() == 1)
    {
      return Opt.of(additionalWarnings.get(0));
    }
    if (additionalWarnings.isEmpty())
    {
      return Opt.none();
    }

    final Set<String> allTaskIds = additionalWarnings
        .stream()
        .flatMap(a -> a.getTaskIds().stream())
        .collect(Collectors.toSet());

    final Multimap<TherapyDto, AdditionalWarningDto> allAdditionalWarningsMap = HashMultimap.create();
    additionalWarnings
        .stream()
        .flatMap(a -> a.getWarnings().stream())
        .forEach(t -> allAdditionalWarningsMap.putAll(t.getTherapy(), t.getWarnings()));

    final AdditionalWarningsDto joinedAdditionalWarnings = new AdditionalWarningsDto();
    joinedAdditionalWarnings.setTaskIds(allTaskIds);
    joinedAdditionalWarnings.setWarnings(
        allAdditionalWarningsMap.keySet()
            .stream()
            .map(e -> new TherapyAdditionalWarningDto(e, new ArrayList<>(allAdditionalWarningsMap.get(e))))
            .collect(Collectors.toList()));

    return Opt.of(joinedAdditionalWarnings);
  }

  private AdditionalWarningsProvider getProviderImplementation(final AdditionalWarningsType type)
  {
    final AdditionalWarningsProvider impl = additionalWarningsProviders.get(type.name());
    if (impl == null)
    {
      throw new IllegalArgumentException("No additional warnings implementation found for: " + type);
    }
    return impl;
  }
}
