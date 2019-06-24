package com.marand.thinkmed.medications.business.mapper;

import java.util.Locale;

import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationSimpleDto;
import com.marand.thinkmed.medications.business.impl.ReleaseDetailsDisplayProvider;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType.ADD_INFO_INPATIENT;
import static com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType.ADD_INFO_OUTPATIENT;
import static com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType.TRADE_FAMILY;

/**
 * @author Mitja Lapajne
 */
@Component
public class MedicationDataDtoMapper
{
  private MedsProperties medsProperties;
  private TherapyDisplayProvider therapyDisplayProvider;
  private ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider;

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setReleaseDetailsDisplayProvider(final ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider)
  {
    this.releaseDetailsDisplayProvider = releaseDetailsDisplayProvider;
  }

  public MedicationDataForTherapyDto mapToMedicationDataForTherapyDto(
      final @NonNull MedicationDataDto dto,
      final String careProviderId)
  {
    final MedicationDataForTherapyDto dataForTherapyDto = new MedicationDataForTherapyDto();
    dataForTherapyDto.setGenericName(dto.getMedication().getGenericName());
    dataForTherapyDto.setAntibiotic(dto.isAntibiotic());
    dataForTherapyDto.setAtcGroupCode(dto.getAtcGroupCode());
    dataForTherapyDto.setAtcGroupName(dto.getAtcGroupName());
    if (careProviderId != null)
    {
      final Pair<String, Integer> customGroup = dto.getCareProviderCustomGroups().get(careProviderId);
      if (customGroup != null)
      {
        dataForTherapyDto.setCustomGroupName(customGroup.getFirst());
        dataForTherapyDto.setCustomGroupSortOrder(customGroup.getSecond());
      }
    }
    return dataForTherapyDto;
  }

  public MedicationDto mapToMedicationDto(final @NonNull MedicationDataDto dto)
  {


    final MedicationDto medicationDto = new MedicationDto();
    medicationDto.setId(dto.getMedication().getId());
    medicationDto.setName(dto.getMedication().getName());
    medicationDto.setGenericName(dto.getMedication().getGenericName());
    medicationDto.setMedicationType(dto.getMedication().getMedicationType());
    medicationDto.setDisplayName(dto.getMedication().getDisplayName());
    return medicationDto;
  }

  public FormularyMedicationDto mapToFormularyMedicationDto(final @NonNull MedicationDataDto dto)
  {
    final FormularyMedicationDto medicationDto = new FormularyMedicationDto();
    medicationDto.setId(dto.getMedication().getId());
    medicationDto.setName(dto.getMedication().getName());
    medicationDto.setFormulary(dto.isFormulary());
    medicationDto.setSupplyUnit(dto.getSupplyUnit());
    return medicationDto;
  }

  public TreeNodeData mapToTreeNodeDto(
      final @NonNull MedicationDataDto dto,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final TreeNodeData searchDto = new TreeNodeData();
    final MedicationSimpleDto simpleDto = mapToSimpleDto(dto, when);

    if (medsProperties.getFormularyFilterEnabled() && !dto.isFormulary())
    {
      searchDto.setExtraClasses("non-formulary");
    }

    searchDto.setData(simpleDto);

    searchDto.setUnselectable(!dto.isOrderable() || !dto.isValid(when));

    if (dto.getMedicationLevel() == MedicationLevelEnum.VTM)
    {
      searchDto.setKey(String.valueOf(dto.getVtmId()));
      searchDto.setTitle(buildTreeNodeTitle(dto, false, locale));
    }
    else if (dto.getMedicationLevel() == MedicationLevelEnum.VMP)
    {
      searchDto.setKey(String.valueOf(dto.getVmpId()));
      searchDto.setTitle(buildTreeNodeTitle(dto, false, locale));
    }
    else if (dto.getMedicationLevel() == MedicationLevelEnum.AMP)
    {
      searchDto.setKey(String.valueOf(dto.getAmpId()));
      if (dto.getVmpId() == null) // AMP without a parent
      {
        searchDto.setTitle(buildTreeNodeTitle(dto, true, locale));
      }
      else
      {
        searchDto.setTitle(buildTreeNodeTitle(dto, false, locale));
      }
    }
    return searchDto;
  }

  public MedicationSimpleDto mapToSimpleDto(final MedicationDataDto dto, final DateTime when)
  {
    final MedicationSimpleDto simpleDto = new MedicationSimpleDto();

    simpleDto.setId(dto.getMedication().getId());
    simpleDto.setId(dto.getMedication().getId());
    simpleDto.setName(dto.getMedication().getName());
    simpleDto.setActive(dto.isValid(when));
    simpleDto.setGenericName(dto.getMedication().getGenericName());
    simpleDto.setOutpatientMedication(dto.isOutpatient());
    simpleDto.setInpatientMedication(dto.isInpatient());

    Opt.of(dto.getProperty(ADD_INFO_INPATIENT))
        .ifPresent(sur -> simpleDto.setInpatientAdditionalInfo(sur.getValue()));
    Opt.of(dto.getProperty(ADD_INFO_OUTPATIENT))
        .ifPresent(sur -> simpleDto.setOutpatientAdditionalInfo(sur.getValue()));
    Opt.of(dto.getProperty(TRADE_FAMILY))
        .ifPresent(tf -> simpleDto.setTradeFamily(tf.getValue()));

    return simpleDto;
  }

  public String buildTreeNodeTitle(
      final @NonNull MedicationDataDto medication,
      final boolean showGeneric,
      final @NonNull Locale locale)
  {
    final String medicationName = medication.getMedication().getName();
    final StringBuilder title = new StringBuilder();
    title.append(
        showGeneric
        ? therapyDisplayProvider.getMedicationWithGenericDisplay(medicationName, medication.getMedication().getGenericName())
        : medicationName);

    final MedicationPropertyDto tradeFamily = medication.getProperty(TRADE_FAMILY);
    if (tradeFamily != null)
    {
      title.append(" - ");
      title.append(tradeFamily.getValue());
    }

    final String releaseDetailsDisplay = releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(medication, locale);
    if (releaseDetailsDisplay != null)
    {
      title.append(" - ");
      title.append(releaseDetailsDisplay);
    }
    return title.toString();
  }
}
