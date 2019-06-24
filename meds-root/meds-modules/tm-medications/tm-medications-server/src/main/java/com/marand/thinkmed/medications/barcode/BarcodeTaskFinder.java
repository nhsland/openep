package com.marand.thinkmed.medications.barcode;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto.BarcodeSearchResult;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Mitja Lapajne
 */

@Component
public class BarcodeTaskFinder
{
  private MedicationsDao medicationsDao;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsFinder medicationsFinder;
  private TherapyEhrHandler therapyEhrHandler;

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setMedicationsFinder(final MedicationsFinder medicationsFinder)
  {
    this.medicationsFinder = medicationsFinder;
  }

  public BarcodeTaskSearchDto getAdministrationTaskForBarcode(
      final @NonNull String patientId,
      final @NonNull String medicationBarcode,
      final @NonNull DateTime when)
  {
    final Long barcodeMedicationId = medicationsDao.getMedicationIdForBarcode(medicationBarcode);
    String barcodeTherapyId = null;

    if (barcodeMedicationId == null)
    {
      final String therapyId = BarcodeScannerUtils.parseFromNumericRepresentation(medicationBarcode);
      final boolean validTherapyId = TherapyIdUtils.isValidTherapyId(therapyId);
      if (validTherapyId)
      {
        barcodeTherapyId = therapyId;
      }
      else
      {
        return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_MEDICATION);
      }
    }

    final List<TaskDto> dueTasks = medicationsTasksProvider.findAdministrationTasks(
        Collections.singleton(patientId),
        when.minusHours(1),
        when.plusHours(1));

    final List<TaskDto> dueStartTasks = dueTasks.stream()
        .filter(t -> AdministrationTypeEnum.START.name().equals(
            t.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName())))
        .collect(Collectors.toList());

    if (dueStartTasks.isEmpty())
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_TASK);
    }

    final MultiValueMap<String, String> therapyIdTasksIdMap = buildTherapyIdTaskIdMap(dueStartTasks);

    final Set<String> matchingTherapyIds =
        barcodeMedicationId != null
        ? getTherapiesWithMatchingMedication(barcodeMedicationId, therapyIdTasksIdMap.keySet(), when)
        : getTherapiesWithMatchingOriginalTherapyIds(patientId, barcodeTherapyId, therapyIdTasksIdMap);

    final Set<String> matchingTaskIds = matchingTherapyIds.stream()
        .flatMap(therapyId -> therapyIdTasksIdMap.get(therapyId).stream())
        .collect(Collectors.toSet());

    if (matchingTaskIds.size() == 1)
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.TASK_FOUND, matchingTaskIds.iterator().next(), barcodeMedicationId);
    }
    else if (matchingTaskIds.size() > 1)
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.MULTIPLE_TASKS);
    }
    else
    {
      return new BarcodeTaskSearchDto(BarcodeSearchResult.NO_TASK);
    }
  }

  private Set<String> getTherapiesWithMatchingOriginalTherapyIds(
      final String patientId,
      final String barcodeTherapyId,
      final MultiValueMap<String, String> therapyIdTasksIdMap)
  {
    return therapyIdTasksIdMap.keySet().stream()
        .filter(therapyId -> getTherapyOriginalId(patientId, therapyId).equals(barcodeTherapyId))
        .collect(Collectors.toSet());
  }

  private String getTherapyOriginalId(final String patientId, final String therapyId)
  {
    return therapyEhrHandler.getOriginalTherapyId(patientId, TherapyIdUtils.parseTherapyId(therapyId).getFirst());
  }

  private MultiValueMap<String, String> buildTherapyIdTaskIdMap(final List<TaskDto> tasks)
  {
    final MultiValueMap<String, String> therapyIdTasksIdMap = new LinkedMultiValueMap<>();
    tasks.forEach(
        task -> therapyIdTasksIdMap.add(
            (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName()),
            task.getId()));
    return therapyIdTasksIdMap;
  }

  private Set<String> getTherapiesWithMatchingMedication(
      final Long barcodeMedicationId,
      final Set<String> therapyIds,
      final DateTime when)
  {
    final Set<String> therapyCompositionUids = therapyIds.stream()
        .map(therapyId -> TherapyIdUtils.parseTherapyId(therapyId).getFirst())
        .collect(Collectors.toSet());

    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.loadInpatientPrescriptions(therapyCompositionUids);

    return prescriptions.stream()
        .filter(p -> isTherapyWithMatchingMedication(p.getMedicationOrder(), barcodeMedicationId, when))
        .map(TherapyIdUtils::createTherapyId)
        .collect(Collectors.toSet());
  }

  private boolean isTherapyWithMatchingMedication(final MedicationOrder order, final Long barcodeMedicationId, final DateTime when)
  {
    final List<Long> medicationIds = MedicationsEhrUtils.getMedicationIds(order);
    for (final Long therapyMedicationId : medicationIds)
    {
      if (therapyMedicationId.equals(barcodeMedicationId))
      {
        return true;
      }
      else
      {
        final Set<Long> exchangableMedicationIds = getExchangableMedications(therapyMedicationId, order, when);
        if (exchangableMedicationIds.contains(barcodeMedicationId))
        {
          return true;
        }
      }
    }
    return false;
  }

  private Set<Long> getExchangableMedications(
      final Long therapyMedicationId,
      final MedicationOrder order,
      final DateTime when)
  {
    final List<Long> routeIds = order.getRoute().stream()
        .map(r -> Long.valueOf(r.getDefiningCode().getCodeString()))
        .collect(Collectors.toList());

    final List<MedicationDto> exchangeableMedications = medicationsFinder.findMedicationProducts(
        therapyMedicationId,
        routeIds,
        null,
        when);

    return exchangeableMedications.stream()
        .map(MedicationDto::getId)
        .collect(Collectors.toSet());
  }
}
