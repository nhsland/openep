package com.marand.thinkmed.medications.valueholder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import lombok.NonNull;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationsValueHolderProvider
{
  /**
   * Provides all medications - valid or not valid
   * @return medications
   */
  List<MedicationDataDto> getAllMedicationDataDtos();

  /**
   * Provides only valid medications
   * @param when timestamp to check validity
   * @return medications
   */
  List<MedicationDataDto> getValidMedicationDataDtos(final @NonNull DateTime when);

  /**
   * Provides all medications - valid or not valid
   * @param medicationIds to be included
   * @return map of medications
   */
  Map<Long, MedicationDataDto> getAllMedicationDataMap(@NonNull Set<Long> medicationIds);

  /**
   * Provides only valid medications
   * @param medicationIds to be included
   * @param when timestamp to check validity
   * @return map of medications
   */
  Map<Long, MedicationDataDto> getValidMedicationDataMap(@NonNull Set<Long> medicationIds, @NonNull DateTime when);

  MedicationDataDto getMedicationData(long medicationId);

  Set<Long> getMedicationIdsWithIngredientRule(@NonNull MedicationRuleEnum medicationRuleEnum);

  List<Long> getMedicationIdsWithIngredientId(long ingredientId);

  List<MedicationRouteDto> getMedicationRoutes(long medicationId);

  List<MedicationDataDto> findSimilarMedicationDataDtos(long medicationId, @NonNull DateTime when);

  List<MedicationDto> findSimilarMedications(long medicationId, @NonNull DateTime when);

  List<MedicationDto> getMedicationChildProducts(
      long medicationId,
      @NonNull Collection<Long> routeIds,
      ReleaseDetailsDto releaseDetails,
      @NonNull DateTime when);

  MedicationDto getMedication(long medicationId);

  List<FormularyMedicationDto> getVmpMedications(@NonNull String vtmId, @NonNull DateTime when);

  boolean isProductBasedMedication(long medicationId);

  DoseFormDto getDoseForm(long doseFromId);
}
