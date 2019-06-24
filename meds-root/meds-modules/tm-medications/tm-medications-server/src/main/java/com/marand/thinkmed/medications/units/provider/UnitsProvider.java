package com.marand.thinkmed.medications.units.provider;

import lombok.NonNull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;

/**
 * @author Nejc Korasa
 */
public interface UnitsProvider
{
  Opt<MedicationUnitTypeDto> findTypeByUnitName(@NonNull String unitName);

  Opt<KnownUnitType> findKnownUnitByDisplayName(@NonNull String displayName);

  Opt<MedicationUnitTypeDto> findTypeByKnownUnit(@NonNull KnownUnitType knownUnit);

  String getDisplayName(final @NonNull KnownUnitType knownUnit);

  UnitsHolderDto getUnitsHolder();
}
