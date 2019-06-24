package com.marand.thinkmed.medications.administration;

import java.util.Collection;
import lombok.NonNull;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */

public interface AdministrationUtils
{
  DateTime getAdministrationTime(@NonNull AdministrationDto administrationDto);

  Double getInfusionRate(@NonNull AdministrationDto administrationDto);

  boolean isRateAdministration(@NonNull AdministrationDto administrationDto);

  Double getVolumeForRateQuantityOrRateVolumeSum(@NonNull AdministrationDto administrationDto, @NonNull KnownUnitType knownUnit);

  InfusionBagDto getInfusionBagDto(@NonNull AdministrationDto administrationDto);

  TherapyDoseDto getTherapyDose(@NonNull AdministrationDto administrationDto);

  TherapyDoseDto getPlannedTherapyDose(@NonNull AdministrationDto administrationDto);

  TherapyDoseTypeEnum getTherapyDoseType(@NonNull AdministrationDto administrationDto);

  void fillDurationForInfusionWithRate(@NonNull Collection<AdministrationDto> administrations);

  String generateGroupUUId(@NonNull DateTime date);

  int calculateDurationForRateQuantityDose(@NonNull TherapyDoseDto dose);
}
