package com.marand.thinkmed.medications.warnings.additional.allergies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.maf.core.Opt;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsProvider;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component("ALLERGIES")
public class AllergiesAdditionalWarningsProvider implements AdditionalWarningsProvider
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private AllergiesHandler allergiesHandler;
  private MedicationsBo medicationsBo;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAllergiesHandler(final AllergiesHandler allergiesHandler)
  {
    this.allergiesHandler = allergiesHandler;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  public Opt<AdditionalWarningsDto> getAdditionalWarnings(
      final @NonNull String patientId,
      final @NonNull PatientDataForMedicationsDto patientData,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final List<CheckNewAllergiesTaskDto> newAllergiesTasks = medicationsTasksProvider.findNewAllergiesTasks(patientId);
    if (newAllergiesTasks.isEmpty())
    {
      return Opt.none();
    }

    final Set<IdNameDto> allergies = newAllergiesTasks
        .stream()
        .flatMap(t -> t.getAllergies().stream())
        .collect(Collectors.toSet());

    final List<MedicationsWarningDto> allergiesWarnings = allergiesHandler.getAllergyWarnings(patientId, allergies, when);
    if (allergiesWarnings.isEmpty())
    {
      newAllergiesTasks
          .stream()
          .map(CheckNewAllergiesTaskDto::getTaskId)
          .forEach(id -> processService.completeTasks(id));
    }

    return Opt.of(buildTherapyAdditionalWarnings(
        newAllergiesTasks,
        allergiesWarnings,
        findTherapies(patientId, patientData, when, locale)));
  }

  private AdditionalWarningsDto buildTherapyAdditionalWarnings(
      final Collection<CheckNewAllergiesTaskDto> allergyTasks,
      final Collection<MedicationsWarningDto> allergyWarnings,
      final List<TherapyDto> therapies)
  {
    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = therapies
        .stream()
        .map(t -> buildTherapyAdditionalWarning(t, allergyWarnings))
        .collect(Collectors.toList());

    final Set<String> taskIds = allergyTasks
        .stream()
        .map(CheckNewAllergiesTaskDto::getTaskId)
        .collect(Collectors.toSet());

    final AdditionalWarningsDto additionalWarnings = new AdditionalWarningsDto();
    additionalWarnings.setWarnings(therapyAdditionalWarnings);
    additionalWarnings.setTaskIds(taskIds);
    return additionalWarnings;
  }

  private TherapyAdditionalWarningDto buildTherapyAdditionalWarning(
      final TherapyDto therapy,
      final Collection<MedicationsWarningDto> warnings)
  {
    final List<Long> therapyMedications = therapy.getMedications()
        .stream()
        .map(MedicationDto::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    final Predicate<MedicationsWarningDto> isWarningForTherapy = warning ->
        warning.getMedications()
            .stream()
            .anyMatch(m -> therapyMedications.contains(Long.valueOf(m.getId())));

    //use Map to remove duplicate warnings
    final Map<String, AdditionalWarningDto> additionalWarningsForTherapy = new HashMap<>();
    warnings
        .stream()
        .filter(isWarningForTherapy)
        .map(w -> new AdditionalWarningDto(AdditionalWarningsType.ALLERGIES, w))
        .forEach(w -> additionalWarningsForTherapy.put(w.getWarning().getDescription(), w));

    return new TherapyAdditionalWarningDto(therapy, Lists.newArrayList(additionalWarningsForTherapy.values()));
  }

  List<TherapyDto> findTherapies(
      final String patientId,
      final PatientDataForMedicationsDto patientData,
      final DateTime when,
      final Locale locale)
  {
    return medicationsOpenEhrDao.findInpatientPrescriptions(patientId, Intervals.infiniteFrom(when))
        .stream()
        .map(i -> medicationsBo.convertMedicationOrderToTherapyDto(
            i,
            i.getMedicationOrder(),
            patientData.getWeightInKg(),
            patientData.getHeightInCm(),
            true,
            locale))
        .collect(Collectors.toList());
  }
}
