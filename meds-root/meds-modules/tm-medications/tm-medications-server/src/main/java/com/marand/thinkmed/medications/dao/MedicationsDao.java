package com.marand.thinkmed.medications.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedicationsExternalValueType;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.InformationSourceDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import org.joda.time.DateTime;

/**
 * @author Bostjan Vester
 */
public interface MedicationsDao
{
  Map<Long, MedicationDataDto> loadMedicationsMap(@NonNull DateTime when);

  Map<Long, String> getMedicationsExternalIds(String externalSystem, Collection<Long> medicationId);

  DoseFormDto getDoseFormByCode(String doseFormDto);

  Map<String, String> getMedicationExternalValues(String externalSystem, MedicationsExternalValueType valueType, Set<String> valuesSet);

  List<MedicationRouteDto> getRoutes();

  List<DoseFormDto> getDoseForms();

  List<InformationSourceDto> getInformationSources();

  Map<Long, Pair<String, Integer>> getCustomGroupNameSortOrderMap(String careProviderId, Collection<Long> medicationsCodes);

  List<String> getCustomGroupNames(@NonNull String careProviderId);

  String getPatientLastLinkName(long patientId);

  void savePatientLastLinkName(long patientId, String lastLinkName);

  Map<ActionReasonType, List<CodedNameDto>> getActionReasons(@NonNull DateTime when, ActionReasonType type);

  Map<Long, MedicationRouteDto> loadRoutesMap();

  List<MedicationsWarningDto> getCustomWarningsForMedication(@NonNull Set<Long> medicationIds, @NonNull DateTime when);

  Long getMedicationIdForBarcode(@NonNull String barcode);

  List<DispenseSourceDto> getDispenseSources();
}
