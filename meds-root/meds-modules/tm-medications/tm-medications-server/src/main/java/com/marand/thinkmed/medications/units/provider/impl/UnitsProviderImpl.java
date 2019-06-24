package com.marand.thinkmed.medications.units.provider.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.data.object.SimpleCatalogIdentityDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitDto;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.UnitsValueHolder;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class UnitsProviderImpl implements UnitsProvider
{
  private UnitsValueHolder unitsValueHolder;

  @Autowired
  public void setUnitsValueHolder(final UnitsValueHolder unitsValueHolder)
  {
    this.unitsValueHolder = unitsValueHolder;
  }

  @Override
  public Opt<MedicationUnitTypeDto> findTypeByUnitName(final @NonNull String unitName)
  {
    return Opt.from(
        unitsValueHolder.getValue().getUnits().values()
            .stream()
            .filter(u -> u.getName().equals(unitName))
            .map(MedicationUnitDto::getType)
            .filter(Objects::nonNull)
            .findFirst());
  }

  @Override
  public Opt<KnownUnitType> findKnownUnitByDisplayName(final @NonNull String displayName)
  {
    return Opt.from(
        unitsValueHolder.getValue().getUnitTypes().values()
            .stream()
            .filter(u -> u.getDisplayName().equals(displayName))
            .map(MedicationUnitTypeDto::getName)
            .map(KnownUnitType::getByName)
            .filter(Objects::nonNull)
            .findFirst());
  }

  @Override
  public Opt<MedicationUnitTypeDto> findTypeByKnownUnit(final @NonNull KnownUnitType knownUnit)
  {
    return Opt.from(
        unitsValueHolder.getValue().getUnitTypes().values()
            .stream()
            .filter(t -> t.getName().equals(knownUnit.name()))
            .findFirst());
  }

  @Override
  public String getDisplayName(final @NonNull KnownUnitType knownUnit)
  {
    return unitsValueHolder.getValue().getUnitTypes().values()
        .stream()
        .filter(t -> t.getName().equals(knownUnit.name()))
        .findFirst()
        .map(MedicationUnitTypeDto::getDisplayName)
        .orElseThrow(() -> new IllegalStateException("Display name for " + knownUnit.name() + " not found"));
  }

  @Override
  public UnitsHolderDto getUnitsHolder()
  {
    final Map<String, Long> typeUnits = unitsValueHolder.getValue().getUnits().values()
        .stream()
        .filter(u -> u.getType() != null)
        .collect(Collectors.toMap(SimpleCatalogIdentityDto::getName, u -> u.getType().getId()));

    final List<String> allUnits = unitsValueHolder.getValue().getUnits().values()
        .stream()
        .map(SimpleCatalogIdentityDto::getName)
        .collect(Collectors.toList());

    final Map<Long, MedicationUnitTypeDto> types = new HashMap<>(unitsValueHolder.getValue().getUnitTypes());

    return new UnitsHolderDto(allUnits, typeUnits, types);
  }
}
