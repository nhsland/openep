package com.marand.thinkmed.medications.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.business.mapper.MedicationDataDtoMapper;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class MedicationsFinderImpl implements MedicationsFinder
{
  private MedicationDataDtoMapper medicationDataDtoMapper;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Autowired
  public void setMedicationDataDtoMapper(final MedicationDataDtoMapper medicationDataDtoMapper)
  {
    this.medicationDataDtoMapper = medicationDataDtoMapper;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Override
  public List<TreeNodeData> findMedications(
      final @NonNull String searchString,
      final boolean startMustMatch,
      final @NonNull EnumSet<MedicationFinderFilterEnum> filters,
      final @NonNull DateTime when,
      final Locale locale)
  {
    final List<MedicationDataDto> medications = medicationsValueHolderProvider.getValidMedicationDataDtos(when);
    final List<MedicationDataDto> filtered = applyAdditionalFilters(medications, filters);

    final List<TreeNodeData> medicationsTree = buildMedicationsTree(filtered, when, locale);
    return filterMedicationsTree(medicationsTree, searchString, startMustMatch);
  }

  @Override
  public List<TreeNodeData> findSimilarMedications(
      final long medicationId,
      final @NonNull DateTime when,
      final Locale locale)
  {
    final Collection<MedicationDataDto> similarMedications = medicationsValueHolderProvider.findSimilarMedicationDataDtos(
        medicationId,
        when);

    return buildMedicationsTree(similarMedications, when, locale);
  }

  @Override
  public List<MedicationDto> findMedicationProducts(
      final long medicationId,
      final @NonNull List<Long> routeIds,
      final ReleaseDetailsDto releaseDetails,
      final @NonNull DateTime when)
  {
    final boolean productBasedMedication = medicationsValueHolderProvider.isProductBasedMedication(medicationId);

    if (productBasedMedication)
    {
      return medicationsValueHolderProvider.findSimilarMedications(medicationId, when);
    }

    return medicationsValueHolderProvider.getMedicationChildProducts(medicationId, routeIds, releaseDetails, when);
  }

  private List<MedicationDataDto> applyAdditionalFilters(final List<MedicationDataDto> medications, final EnumSet<MedicationFinderFilterEnum> filters)
  {
    return medications
        .stream()
        .filter(medication -> filters
            .stream()
            .allMatch(filter -> filterMedication(medication, filter)))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean filterMedication(final MedicationDataDto medication, final MedicationFinderFilterEnum filter)
  {
    if (filter == MedicationFinderFilterEnum.MENTAL_HEALTH)
    {
      return medication.isMentalHealthDrug();
    }

    if (filter == MedicationFinderFilterEnum.INPATIENT_PRESCRIPTION)
    {
      return medication.isInpatient();
    }

    if (filter == MedicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION)
    {
      return medication.isOutpatient();
    }

    if (filter == MedicationFinderFilterEnum.FORMULARY)
    {
      return medication.isFormulary();
    }

    throw new IllegalStateException("Medication filter enum " + filter + " is not supported!");
  }

  private List<TreeNodeData> buildMedicationsTree(
      final Collection<MedicationDataDto> medications,
      final DateTime when,
      final Locale locale)
  {
    final List<TreeNodeData> medicationTree = new ArrayList<>();

    final Map<String, TreeNodeData> vtmMap = new LinkedHashMap<>();
    for (final MedicationDataDto dataDto : medications)
    {
      if (dataDto.getMedicationLevel() == MedicationLevelEnum.VTM)
      {
        final TreeNodeData searchDto = medicationDataDtoMapper.mapToTreeNodeDto(dataDto, when, locale);
        medicationTree.add(searchDto);
        vtmMap.put(dataDto.getVtmId(), searchDto);
      }
    }

    final Map<String, TreeNodeData> vmpMap = new LinkedHashMap<>();
    for (final MedicationDataDto dataDto : medications)
    {
      if (dataDto.getMedicationLevel() == MedicationLevelEnum.VMP)
      {
        final TreeNodeData searchDto = medicationDataDtoMapper.mapToTreeNodeDto(dataDto, when, locale);
        if (vtmMap.containsKey(dataDto.getVtmId()))
        {
          vtmMap.get(dataDto.getVtmId()).getChildren().add(searchDto);
        }
        else
        {
          medicationTree.add(searchDto);
        }
        vmpMap.put(dataDto.getVmpId(), searchDto);
      }
    }

    for (final MedicationDataDto dataDto : medications)
    {
      if (dataDto.getMedicationLevel() == MedicationLevelEnum.AMP)
      {
        final TreeNodeData searchDto = medicationDataDtoMapper.mapToTreeNodeDto(dataDto, when, locale);
        if (vmpMap.containsKey(dataDto.getVmpId()))
        {
          vmpMap.get(dataDto.getVmpId()).getChildren().add(searchDto);
        }
        else
        {
          medicationTree.add(searchDto);
        }
      }
    }
    return medicationTree;
  }

  List<TreeNodeData> filterMedicationsTree(
      final List<TreeNodeData> medications,
      final String searchString,
      final boolean startMustMatch)
  {
    if (searchString == null)
    {
      return medications;
    }
    final String[] searchSubstrings = searchString.split(" ");
    return filterMedicationsTree(medications, searchSubstrings, startMustMatch);
  }

  private List<TreeNodeData> filterMedicationsTree(
      final List<TreeNodeData> medications,
      final String[] searchSubstrings,
      final boolean startMustMatch)
  {
    final List<TreeNodeData> filteredMedications = new ArrayList<>();

    for (final TreeNodeData medicationNode : medications)
    {
      final MedicationSimpleDto medicationSimpleDto = (MedicationSimpleDto)medicationNode.getData();
      final String medicationSearchName =
          medicationSimpleDto.getGenericName() != null ?
          medicationSimpleDto.getGenericName() + " " + medicationNode.getTitle() :
          medicationNode.getTitle();

      medicationNode.setExpanded(false);
      boolean match = true;

      if (startMustMatch && searchSubstrings.length > 0)
      {
        final String firstSearchString = searchSubstrings[0];
        final boolean genericStartsWithFirstSearchString =
            medicationSimpleDto.getGenericName() != null &&
                StringUtils.startsWithIgnoreCase(medicationSimpleDto.getGenericName(), firstSearchString);
        final boolean tradeFamilyStartsWithFirstSearchString =
            medicationSimpleDto.getTradeFamily() != null &&
                StringUtils.startsWithIgnoreCase(medicationSimpleDto.getTradeFamily(), firstSearchString);
        final boolean medicationStartsWithFirstSearchString =
            StringUtils.startsWithIgnoreCase(medicationNode.getTitle(), firstSearchString);
        if (!genericStartsWithFirstSearchString && !medicationStartsWithFirstSearchString && !tradeFamilyStartsWithFirstSearchString)
        {
          match = false;
        }
      }
      if (match)
      {
        for (int i = startMustMatch ? 1 : 0; i < searchSubstrings.length; i++)
        {
          if (!StringUtils.containsIgnoreCase(medicationSearchName, searchSubstrings[i]))
          {
            match = false;
            break;
          }
        }
      }
      if (match)
      {
        filteredMedications.add(medicationNode);
      }
      else
      {
        if (!medicationNode.getChildren().isEmpty())
        {
          final List<TreeNodeData> filteredChildren =
              filterMedicationsTree(medicationNode.getChildren(), searchSubstrings, startMustMatch);
          if (!filteredChildren.isEmpty())
          {
            medicationNode.setChildren(filteredChildren);
            filteredMedications.add(medicationNode);
            medicationNode.setExpanded(true);
          }
        }
      }
    }
    return filteredMedications;
  }
}
