package com.marand.thinkmed.medications.warnings.additional.mentalhealth;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.mentalHealth.CheckMentalHealthMedsTaskDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthAllowedMedicationsDo;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormFromEhrProvider;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsProvider;
import com.marand.thinkmed.medications.warnings.internal.MentalHealthWarningsHandler;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component(value = "MENTAL_HEALTH")
public class MentalHealthAdditionalWarningsProvider implements AdditionalWarningsProvider
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;
  private ConsentFormFromEhrProvider consentFormFromEhrProvider;
  private MentalHealthWarningsHandler mentalHealthWarningsHandler;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setConsentFormFromEhrProvider(final ConsentFormFromEhrProvider consentFormFromEhrProvider)
  {
    this.consentFormFromEhrProvider = consentFormFromEhrProvider;
  }

  @Autowired
  public void setMentalHealthWarningsHandler(final MentalHealthWarningsHandler mentalHealthWarningsHandler)
  {
    this.mentalHealthWarningsHandler = mentalHealthWarningsHandler;
  }

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

  @Override
  public Opt<AdditionalWarningsDto> getAdditionalWarnings(
      final @NonNull String patientId,
      final @NonNull PatientDataForMedicationsDto patientData,
      final @NonNull DateTime when,
      final @NonNull Locale locale)
  {
    final List<String> taskIds = medicationsTasksProvider.findNewCheckMentalHealthMedsTasks(patientId)
        .stream()
        .map(CheckMentalHealthMedsTaskDto::getTaskId)
        .collect(Collectors.toList());

    if (taskIds.isEmpty())
    {
      return Opt.none();
    }

    final Opt<MentalHealthDocumentDto> document = consentFormFromEhrProvider.getLatestMentalHealthDocument(patientId);
    if (document.isAbsent())
    {
      taskIds.forEach(id -> processService.completeTasks(id));
      return Opt.none();
    }

    final MentalHealthAllowedMedicationsDo allowedMedications = mentalHealthWarningsHandler.getAllowedMedications(document.get());

    final List<ConflictTherapy> conflictTherapies =
        findTherapies(patientId, when, patientData, locale)
            .stream()
            .map(t -> new ConflictTherapy(t, getConflictMedications(t, allowedMedications)))
            .filter(c -> !CollectionUtils.isEmpty(c.getConflictMedications()))
            .collect(Collectors.toList());

    if (conflictTherapies.isEmpty())
    {
      taskIds.forEach(id -> processService.completeTasks(id));
      return Opt.none();
    }

    return Opt.of(buildAdditionalWarnings(conflictTherapies, taskIds));
  }

  List<NamedExternalDto> getConflictMedications(
      final TherapyDto therapy,
      final MentalHealthAllowedMedicationsDo allowedMedications)
  {
    final List<Long> routeIds = therapy.getRoutes().stream().map(MedicationRouteDto::getId).collect(Collectors.toList());
    return therapy.getMedications()
        .stream()
        .filter(m -> m.getId() != null)
        .filter(m -> medicationsBo.isMentalHealthMedication(m.getId()))
        .filter(m -> !mentalHealthWarningsHandler.isMedicationWithRoutesAllowed(m.getId(), routeIds, allowedMedications))
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());
  }

  private AdditionalWarningsDto buildAdditionalWarnings(
      final Collection<ConflictTherapy> conflictTherapies,
      final Collection<String> taskIds)
  {
    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = conflictTherapies
        .stream()
        .map(this::buildTherapyAdditionalWarning)
        .collect(Collectors.toList());

    final AdditionalWarningsDto result = new AdditionalWarningsDto();
    result.setWarnings(therapyAdditionalWarnings);
    result.setTaskIds(new HashSet<>(taskIds));
    return result;
  }

  private TherapyAdditionalWarningDto buildTherapyAdditionalWarning(final ConflictTherapy conflictTherapy)
  {
    final List<AdditionalWarningDto> additionalWarnings = conflictTherapy.getConflictMedications()
        .stream()
        .map(this::buildAdditionalWarning)
        .collect(Collectors.toList());

    return new TherapyAdditionalWarningDto(conflictTherapy.getTherapyDto(), additionalWarnings);
  }

  private AdditionalWarningDto buildAdditionalWarning(final NamedExternalDto medication)
  {
    final MedicationsWarningDto warningDto = mentalHealthWarningsHandler.buildMentalHealthMedicationsWarning(medication);

    return new AdditionalWarningDto(AdditionalWarningsType.MENTAL_HEALTH, warningDto);
  }

  List<TherapyDto> findTherapies(
      final String patientId,
      final DateTime when,
      final PatientDataForMedicationsDto patientData,
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

  private static class ConflictTherapy
  {
    private final TherapyDto therapyDto;
    private final Collection<NamedExternalDto> conflictMedications;

    private ConflictTherapy(final TherapyDto therapyDto, final Collection<NamedExternalDto> conflictMedications)
    {
      this.therapyDto = therapyDto;
      this.conflictMedications = conflictMedications;
    }

    public TherapyDto getTherapyDto()
    {
      return therapyDto;
    }

    public Collection<NamedExternalDto> getConflictMedications()
    {
      return conflictMedications;
    }
  }
}
