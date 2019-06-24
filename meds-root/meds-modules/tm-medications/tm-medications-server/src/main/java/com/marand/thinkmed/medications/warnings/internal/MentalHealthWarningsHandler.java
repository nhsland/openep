package com.marand.thinkmed.medications.warnings.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.IdentityDto;
import com.marand.maf.core.data.object.NamedIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.dao.TherapyTemplateDao;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateMemberDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Component
public class MentalHealthWarningsHandler
{
  private TherapyTemplateDao therapyTemplateDao;

  @Autowired
  public void setTherapyTemplateDao(final TherapyTemplateDao therapyTemplateDao)
  {
    this.therapyTemplateDao = therapyTemplateDao;
  }

  public MedicationsWarningDto buildMentalHealthMedicationsWarning(final @NonNull NamedExternalDto medication)
  {
    final StringBuilder description = new StringBuilder()
        .append(Dictionary.getMessage("mental.health.medication.not.in.form", medication.getName()))
        .append(" \n")
        .append(Dictionary.getMessage("mental.health.medication.reason.to.approve", medication.getName()));

    return new MedicationsWarningDto(
        description.toString(),
        WarningSeverity.HIGH_OVERRIDE,
        WarningType.MENTAL_HEALTH,
        Collections.singletonList(medication));
  }

  public MentalHealthAllowedMedicationsDo getAllowedMedications(final @NonNull MentalHealthDocumentDto mentalHealthDocumentDto)
  {
    final Map<Long, List<Long>> routesMapForMedications = mentalHealthDocumentDto.getMentalHealthMedicationDtoList()
        .stream()
        .filter(m -> m.getRoute() != null)
        .collect(Collectors.groupingBy(
            NamedIdentityDto::getId,
            Collectors.mapping(m -> m.getRoute().getId(), Collectors.toList())));

    final Set<Long> templateIds = mentalHealthDocumentDto.getMentalHealthTemplateDtoList()
        .stream()
        .map(NamedIdentityDto::getId)
        .collect(Collectors.toSet());

    final Map<Long, List<Long>> routesMapForTemplates = therapyTemplateDao.getMentalHealthTemplateMembers(templateIds)
        .stream()
        .filter(m -> m.getRoute() != null)
        .collect(Collectors.groupingBy(
            MentalHealthTemplateMemberDto::getMedicationId,
            Collectors.mapping(m -> m.getRoute().getId(), Collectors.toList())));

    final Set<Long> allRouteMedications = mentalHealthDocumentDto.getMentalHealthMedicationDtoList()
        .stream()
        .filter(m -> m.getRoute() == null)
        .map(IdentityDto::getId)
        .collect(Collectors.toSet());

    allRouteMedications.addAll(
        therapyTemplateDao.getMentalHealthTemplateMembers(templateIds)
            .stream()
            .filter(m -> m.getRoute() == null)
            .map(IdentityDto::getId)
            .collect(Collectors.toSet()));

    final SetMultimap<Long, Long> medicationIdsWithRouteIds = HashMultimap.create();

    routesMapForMedications.forEach(medicationIdsWithRouteIds::putAll);
    routesMapForTemplates.forEach(medicationIdsWithRouteIds::putAll);

    return new MentalHealthAllowedMedicationsDo(medicationIdsWithRouteIds, allRouteMedications);
  }

  public boolean isMedicationWithRouteAllowed(
      final long medicationId,
      final Long routeId,
      final @NonNull MentalHealthAllowedMedicationsDo allowedMedications)
  {
    return isMedicationWithRoutesAllowed(
        medicationId,
        routeId != null ? Collections.singletonList(routeId) : Collections.emptyList(),
        allowedMedications);
  }

  public boolean isMedicationWithRoutesAllowed(
      final long medicationId,
      final List<Long> routeIds,
      final @NonNull MentalHealthAllowedMedicationsDo allowedMedications)
  {
    if (routeIds == null || routeIds.isEmpty())
    {
      return false;
    }

    final SetMultimap<Long, Long> medicationIdsWithRouteIds = allowedMedications.getMedicationIdsWithRouteIds();
    final Set<Long> allRoutesMedicationIds = allowedMedications.getAllRoutesMedicationIds();
    if (!medicationIdsWithRouteIds.containsKey(medicationId) && !allRoutesMedicationIds.contains(medicationId))
    {
      return false;
    }
    if (allRoutesMedicationIds.contains(medicationId))
    {
      return true;
    }

    return routeIds.stream().anyMatch(routeId -> medicationIdsWithRouteIds.get(medicationId).contains(routeId));
  }
}
