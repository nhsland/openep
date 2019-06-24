package com.marand.thinkmed.medications.ingredient;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

import com.google.common.collect.Multimap;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.unit.KnownUnitType;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface IngredientCalculator
{
  /**
   * Calculates ingredient quantity in therapies for one day (24 hours) in unit.
   * @return Double
   */
  double calculateIngredientQuantityInTherapies(
      @NonNull List<TherapyDto> therapies,
      @NonNull Map<Long, MedicationDataDto> medicationDataDtoMap,
      Long ingredientId,
      MedicationRuleEnum ingredientRuleEnum,
      @NonNull KnownUnitType unitType);

  /**
   * Calculates ingredient quantity in administrations for interval.
   * @return Double
   */
  double calculateIngredientQuantityInAdministrations(
      TherapyDoseDto currentAdministrationTherapyDoseDto,
      TherapyDto currentTherapyDto,
      @NonNull Multimap<String, AdministrationDto> administrationDtoMap,
      @NonNull Map<String, TherapyDto> therapyDtoMap,
      @NonNull Map<Long, MedicationDataDto> medicationDataMap,
      @NonNull Interval searchInterval,
      Long ingredientId,
      MedicationRuleEnum ingredientRule,
      @NonNull KnownUnitType unitType);
}
