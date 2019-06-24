package com.marand.thinkmed.medications.valueholder;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MedicationsValueHolderProviderImpl implements MedicationsValueHolderProvider
{
  private MedicationsValueHolder medicationsValueHolder;
  private MedicationDataDtoMapper medicationDataDtoMapper;

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setMedicationDataDtoMapper(final MedicationDataDtoMapper medicationDataDtoMapper)
  {
    this.medicationDataDtoMapper = medicationDataDtoMapper;
  }

  private Map<Long, MedicationDataDto> getAllMedicationDataMap()
  {
    return medicationsValueHolder.getMedications();
  }

  @Override
  public List<MedicationDataDto> getAllMedicationDataDtos()
  {
    return getAllMedicationDataMap().values()
        .stream()
        .sorted(medicationDataDtoComparator())
        .collect(Collectors.toList());
  }

  @Override
  public List<MedicationDataDto> getValidMedicationDataDtos(final @NonNull DateTime when)
  {
    return getAllMedicationDataMap().values()
        .stream()
        .filter(m -> m.isValid(when))
        .sorted(medicationDataDtoComparator())
        .collect(Collectors.toList());
  }

  @Override
  public MedicationDataDto getMedicationData(final long medicationId)
  {
    return Opt
        .of(getAllMedicationDataMap().get(medicationId))
        .orElseThrow(() -> new IllegalStateException("No medication with id " + medicationId + " in holder"));
  }

  @Override
  public Map<Long, MedicationDataDto> getAllMedicationDataMap(final @NonNull Set<Long> medicationIds)
  {
    if (medicationIds.isEmpty())
    {
      return Collections.emptyMap();
    }

    return getAllMedicationDataMap().entrySet()
        .stream()
        .filter(e -> medicationIds.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Map<Long, MedicationDataDto> getValidMedicationDataMap(
      final @NonNull Set<Long> medicationIds,
      final @NonNull DateTime when)
  {
    if (medicationIds.isEmpty())
    {
      return Collections.emptyMap();
    }

    return getAllMedicationDataMap().entrySet()
        .stream()
        .filter(e -> e.getValue().isValid(when))
        .filter(e -> medicationIds.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Set<Long> getMedicationIdsWithIngredientRule(final @NonNull MedicationRuleEnum medicationRuleEnum)
  {
    return getAllMedicationDataMap().entrySet()
        .stream()
        .filter(e -> e.getValue().getMedicationIngredients().stream().anyMatch(i -> i.getIngredientRule() == medicationRuleEnum))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public List<Long> getMedicationIdsWithIngredientId(final long ingredientId)
  {
    return getAllMedicationDataMap().entrySet()
        .stream()
        .filter(e -> e.getValue().getMedicationIngredients().stream().anyMatch(i -> i.getIngredientId() == ingredientId))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  @Override
  public List<MedicationRouteDto> getMedicationRoutes(final long medicationId)
  {
    return getMedicationData(medicationId).getRoutes();
  }

  @Override
  public List<MedicationDataDto> findSimilarMedicationDataDtos(final long medicationId, final @NonNull DateTime when)
  {
    return getSimilarMedications(medicationId, when);
  }

  @Override
  public List<MedicationDto> findSimilarMedications(final long medicationId, final @NonNull DateTime when)
  {
    return getSimilarMedications(medicationId, when).stream()
        .map(medicationDataDtoMapper::mapToMedicationDto)
        .collect(Collectors.toList());
  }

  private List<MedicationDataDto> getSimilarMedications(final long medicationId, final DateTime when)
  {
    final MedicationDataDto referenceMedication = getMedicationData(medicationId);
    final String interchangeableDrugsGroup = referenceMedication.getInterchangeableDrugsGroup();
    if (interchangeableDrugsGroup == null)
    {
      return Collections.singletonList(referenceMedication);
    }
    return getAllMedicationDataMap().values()
        .stream()
        .filter(m -> interchangeableDrugsGroup.equals(m.getInterchangeableDrugsGroup()))
        .filter(m -> m.isValid(when))
        .sorted(medicationDataDtoComparator())
        .collect(Collectors.toList());
  }

  @Override
  public List<MedicationDto> getMedicationChildProducts(
      final long medicationId,
      final @NonNull Collection<Long> routeIds,
      final ReleaseDetailsDto releaseDetails,
      final @NonNull DateTime when)
  {
    final MedicationDataDto referenceMedication = getMedicationData(medicationId);
    return getAllMedicationDataMap().values()
        .stream()
        .filter(m -> isChildMedication(m, referenceMedication))
        .filter(m -> m.isValid(when))
        .filter(m -> isSameReleaseDetail(m, releaseDetails))
        .filter(m -> routeIds.isEmpty() || m.getRoutes().stream().map(MedicationRouteDto::getId).anyMatch(routeIds::contains))
        .sorted(medicationDataDtoComparator())
        .map(medicationDataDtoMapper::mapToMedicationDto)
        .collect(Collectors.toList());
  }

  private boolean isSameReleaseDetail(final MedicationDataDto medication, final ReleaseDetailsDto releaseDetails)
  {
    if (releaseDetails == null)
    {
      return true;
    }

    final MedicationPropertyDto property = medication.getProperty(releaseDetails.mapToPropertyType());
    return property != null
        && (releaseDetails.getHours() == null || property.getValue().equals(String.valueOf(releaseDetails.getHours())));
  }

  @Override
  public List<FormularyMedicationDto> getVmpMedications(final @NonNull String vtmId, final @NonNull DateTime when)
  {
    return getAllMedicationDataMap().values()
        .stream()
        .filter(m -> Objects.equals(m.getVtmId(), vtmId))
        .filter(m -> m.getMedicationLevel() == MedicationLevelEnum.VMP)
        .filter(m -> m.isValid(when))
        .sorted(medicationDataDtoComparator())
        .map(medicationDataDtoMapper::mapToFormularyMedicationDto)
        .collect(Collectors.toList());
  }

  @Override
  public MedicationDto getMedication(final long medicationId)
  {
    return medicationDataDtoMapper.mapToMedicationDto(getMedicationData(medicationId));
  }

  @Override
  public boolean isProductBasedMedication(final long medicationId)
  {
    final MedicationDataDto dto = getMedicationData(medicationId);
    return dto.getMedicationLevel() == MedicationLevelEnum.AMP && dto.getVtmId() == null;
  }

  @Override
  public DoseFormDto getDoseForm(final long doseFromId)
  {
    return medicationsValueHolder.getDoseForms().get(doseFromId);
  }

  private boolean isChildMedication(final MedicationDataDto value, final MedicationDataDto referenceMedication)
  {
    if (value.getMedicationLevel() == MedicationLevelEnum.AMP)
    {
      final MedicationLevelEnum referenceMedicationLevel = referenceMedication.getMedicationLevel();
      if (referenceMedicationLevel == MedicationLevelEnum.VTM)
      {
        return Opt.of(value.getVtmId()).map(id -> id.equals(referenceMedication.getVtmId())).orElse(false);
      }
      if (referenceMedicationLevel == MedicationLevelEnum.VMP)
      {
        return Opt.of(value.getVmpId()).map(id -> id.equals(referenceMedication.getVmpId())).orElse(false);
      }
      if (referenceMedicationLevel == MedicationLevelEnum.AMP)
      {
        return Opt.of(value.getAmpId()).map(id -> id.equals(referenceMedication.getAmpId())).orElse(false);
      }
      throw new IllegalArgumentException("Medication level" + referenceMedicationLevel + " not supported!");
    }
    return false;
  }

  private Comparator<MedicationDataDto> medicationDataDtoComparator()
  {
    return Comparator
        .comparing(MedicationDataDto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing((MedicationDataDto m) -> m.getMedication().getName());
  }
}
