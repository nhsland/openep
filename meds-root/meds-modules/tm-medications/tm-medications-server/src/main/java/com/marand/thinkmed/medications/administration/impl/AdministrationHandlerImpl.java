package com.marand.thinkmed.medications.administration.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.ispek.bpm.TaskCompletedType;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.exception.UserWarning;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationSaver;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.administration.converter.AdministrationToEhrConverter;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.PlannedDoseAdministration;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.RateTherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.updater.TherapyUpdater;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationHandlerImpl implements AdministrationHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private PharmacySupplyProcessHandler pharmacySupplyProcessHandler;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private TherapyUpdater therapyUpdater;
  private MedicationsTasksHandler medicationsTasksHandler;
  private MedicationsTasksProvider medicationsTasksProvider;
  private ProcessService processService;
  private AdministrationToEhrConverter administrationToEhrConverter;
  private TasksRescheduler tasksRescheduler;
  private AdministrationTaskCreator administrationTaskCreator;
  private AdministrationUtils administrationUtils;
  private AdministrationSaver administrationSaver;
  private AdministrationTaskConverter administrationTaskConverter;
  private TherapyEhrHandler therapyEhrHandler;
  private RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Autowired
  public void setPharmacySupplyProcessHandler(final PharmacySupplyProcessHandler pharmacySupplyProcessHandler)
  {
    this.pharmacySupplyProcessHandler = pharmacySupplyProcessHandler;
  }

  @Autowired
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setAdministrationToEhrConverter(final AdministrationToEhrConverter administrationToEhrConverter)
  {
    this.administrationToEhrConverter = administrationToEhrConverter;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Autowired
  public void setTasksRescheduler(final TasksRescheduler tasksRescheduler)
  {
    this.tasksRescheduler = tasksRescheduler;
  }

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Autowired
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Autowired
  public void setAdministrationSaver(final AdministrationSaver administrationSaver)
  {
    this.administrationSaver = administrationSaver;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Override
  public void addAdministrationsToTimelines(
      final @NonNull List<AdministrationDto> administrations,
      final @NonNull Map<String, TherapyRowDto> therapyTimelineRowsMap,
      final @NonNull Map<String, String> modifiedTherapiesMap,
      final @NonNull Interval tasksInterval)
  {
    for (final AdministrationDto administrationDto : administrations)
    {
      final String therapyId = administrationDto.getTherapyId();
      final String latestTherapyId = Opt.of(modifiedTherapiesMap.get(therapyId)).orElse(therapyId);

      therapyTimelineRowsMap.get(latestTherapyId).getAdministrations().add(administrationDto);
    }

    therapyTimelineRowsMap.values()
        .forEach(r -> fillAdditionalAdministrationRowData(r, new DateTime(tasksInterval.getStart())));

    filterAdministrationsByTime(therapyTimelineRowsMap, tasksInterval);
  }

  @Override
  public void deleteAdministration(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final @NonNull TherapyDoseTypeEnum therapyDoseType,
      final @NonNull String therapyId,
      final String comment)
  {
    final boolean medicationAdministration = AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(
        administration.getAdministrationType());
    final boolean rateQuantityOrVolumeSum = therapyDoseType == TherapyDoseTypeEnum.RATE_QUANTITY
        || therapyDoseType == TherapyDoseTypeEnum.RATE_VOLUME_SUM;

    if (medicationAdministration && administration.getTaskId() == null && rateQuantityOrVolumeSum)
    {
      handleDeleteGroupedPrnAdministration(patientId, administration, therapyId, comment);
    }
    else if (administration.getAdministrationType() == AdministrationTypeEnum.START && administration.getGroupUUId() != null)
    {
      handleDeleteGroupedNonPrnStartAdministration(patientId, administration, therapyId, comment);
    }
    else
    {
      medicationsOpenEhrDao.deleteTherapyAdministration(patientId, administration.getAdministrationId(), comment);
      if (administration.getTaskId() != null)
      {
        medicationsTasksHandler.undoCompleteTask(administration.getTaskId());
      }
    }
  }

  /**
   * Completes the tasks in BPM and creates administration in EHR with "not administered" status and
   * given {@link NotAdministeredReasonEnum}.
   * If task is inside a task group, this applies for all tasks in group.
   */
  @Override
  public void cancelAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final @NonNull NotAdministeredReasonEnum notAdministeredReason,
      final String comment)
  {
    if (comment != null)
    {
      administration.setComment(comment);
    }

    if (administration.getGroupUUId() != null &&
        administration.getAdministrationType() != AdministrationTypeEnum.ADJUST_INFUSION)
    {
      final List<TaskDto> tasksInSameGroup = medicationsTasksProvider.findAdministrationTasks(
          patientId,
          null,
          null,
          null,
          administration.getGroupUUId(),
          false);

      tasksInSameGroup.stream()
          .map(t -> administrationTaskConverter.buildAdministrationFromTask(t, requestDateTimeHolder.getRequestTimestamp()))
          .peek(t -> t.setComment(administration.getComment()))
          .forEach(t -> cancelSingleAdministrationTask(patientId, t, notAdministeredReason));
    }
    else
    {
      cancelSingleAdministrationTask(patientId, administration, notAdministeredReason);
    }
  }

  private void cancelSingleAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final @NonNull NotAdministeredReasonEnum reasonType)
  {
    final String taskId = administration.getTaskId();

    administration.setAdministrationTime(administration.getPlannedTime());
    administration.setAdministrationResult(AdministrationResultEnum.NOT_GIVEN);
    if (reasonType == NotAdministeredReasonEnum.DOCTOR_CONFIRMATION_FALSE)
    {
      administration.setDoctorConfirmation(false);
    }
    administration.setNotAdministeredReason(reasonType.toCodedName());

    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        TherapyIdUtils.extractCompositionUid(administration.getTherapyId()));

    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertAdministration(
        prescription,
        administration,
        administration.getAdministrationResult(),
        RequestUser.getFullName(),
        RequestUser.getId(),
        null,
        null,
        requestDateTimeHolder.getRequestTimestamp());

    final String administrationUid = administrationSaver.save(
        patientId,
        medicationAdministration,
        administration.getAdministrationId());

    medicationsTasksHandler.associateTaskWithAdministration(taskId, administrationUid);
    processService.completeTasks(taskId);
  }

  /**
   * Creates a new task in BPM and deletes an "not administered" administration from EHR
   * If task is inside a task group, this applies for all tasks in group.
   */
  @SuppressWarnings("Convert2MethodRef")
  @Override
  public List<String> uncancelAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration)
  {
    if (administration.getGroupUUId() != null
        && administration.getAdministrationType() != AdministrationTypeEnum.ADJUST_INFUSION)
    {
      final List<TaskDto> tasksInSameGroup = medicationsTasksProvider.findAdministrationTasks(
          patientId,
          null,
          null,
          null,
          administration.getGroupUUId(),
          true);

      return tasksInSameGroup.stream()
          .filter(t -> t.isCompleted())
          .map(t -> administrationTaskConverter.buildAdministrationFromTask(t, requestDateTimeHolder.getRequestTimestamp()))
          .map(t -> uncancelSingleAdministrationTask(patientId, t))
          .collect(Collectors.toList());
    }
    else
    {
      final String uncanceledTaskId = uncancelSingleAdministrationTask(patientId, administration);
      return Lists.newArrayList(uncanceledTaskId);
    }
  }

  private String uncancelSingleAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration)
  {
    medicationsOpenEhrDao.deleteTherapyAdministration(patientId, administration.getAdministrationId(), null);
    return medicationsTasksHandler.undoCompleteTask(administration.getTaskId());
  }

  private void handleDeleteGroupedNonPrnStartAdministration(
      final String patientId,
      final AdministrationDto startAdministration,
      final String therapyId,
      final String comment)
  {
    Preconditions.checkArgument(
        startAdministration.getAdministrationType() == AdministrationTypeEnum.START && startAdministration.getGroupUUId() != null,
        "Administration task " + startAdministration.getTaskId() + " is not of type START or part of group");

    final String groupUUId = startAdministration.getGroupUUId();
    final DateTime administrationTime = startAdministration.getAdministrationTime();
    final DateTime plannedTime = startAdministration.getPlannedTime();

    // undo start task

    medicationsOpenEhrDao.deleteTherapyAdministration(patientId, startAdministration.getAdministrationId(), comment);
    final String newStartTaskId = medicationsTasksHandler.undoCompleteTask(startAdministration.getTaskId());

    // undo other completed tasks

    final List<TaskDto> completedGroupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        administrationTime,
        null,
        groupUUId,
        true)
        .stream()
        .filter(TaskDto::getCompleted).collect(Collectors.toList());

    for (final TaskDto task : completedGroupTasks)
    {
      final String uid = TherapyIdUtils.getCompositionUidWithoutVersion(
          (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName()));

      medicationsOpenEhrDao.deleteTherapyAdministration(patientId, uid, comment);
      medicationsTasksHandler.undoCompleteTask(task.getId());
    }

    // reschedule uncompleted tasks

    if (plannedTime != null && !administrationTime.isEqual(plannedTime))
    {
      tasksRescheduler.rescheduleAdministrationGroup(
          patientId,
          therapyId,
          groupUUId,
          newStartTaskId, // start task due time is not changed when confirmed at different time, therefore should not be rescheduled
          plannedTime.getMillis() - administrationTime.getMillis());
    }
  }

  private void handleDeleteGroupedPrnAdministration(
      final String patientId,
      final AdministrationDto administration,
      final String therapyId,
      final String comment)
  {
    final String groupUUId = administration.getGroupUUId();

    medicationsOpenEhrDao.deleteTherapyAdministration(patientId, administration.getAdministrationId(), comment);

    final List<TaskDto> completedGroupTasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        administration.getAdministrationTime(),
        null,
        groupUUId,
        true)
        .stream()
        .filter(TaskDto::getCompleted).collect(Collectors.toList());

    for (final TaskDto task : completedGroupTasks)
    {
      final String uid = (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName());
      medicationsOpenEhrDao.deleteTherapyAdministration(patientId, TherapyIdUtils.getCompositionUidWithoutVersion(uid), comment);
      processService.setTaskDeleteReason(task.getId(), TaskCompletedType.DELETED.getBpmName());
    }

    medicationsTasksHandler.deleteAdministrationTasks(
        patientId,
        therapyId,
        groupUUId,
        Arrays.asList(AdministrationTypeEnum.values()));
  }

  private void fillAdditionalAdministrationRowData(final TherapyRowDto row, final DateTime before)
  {
    if (row instanceof RateTherapyRowDto)
    {
      fillInfusionRateData((RateTherapyRowDto)row, row.getAdministrations(), before);
    }
    if (row instanceof OxygenTherapyRowDtoDto)
    {
      fillOxygenData((OxygenTherapyRowDtoDto)row, before);
    }
  }

  private void fillInfusionRateData(
      final RateTherapyRowDto rateRow,
      final Collection<AdministrationDto> administrations,
      final DateTime before)
  {
    AdministrationDto lastAdministration = null;
    AdministrationDto lastBeforeAdministration = null;
    Double lastPositiveRate = null;

    for (final AdministrationDto administration : administrations)
    {
      if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()) &&
          administration.getAdministrationType() != AdministrationTypeEnum.BOLUS)
      {
        if (lastAdministration == null || (administration.getAdministrationTime() != null
                && administration.getAdministrationTime().isAfter(lastAdministration.getAdministrationTime())))
        {
          if (AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult()))
          {
            lastAdministration = administration;
            if (administration.getAdministrationTime().isBefore(before))
            {
              lastBeforeAdministration = administration;
            }

            final Opt<Double> administrationInfusionRate = getAdministrationInfusionRate(administration)
                    .map(TherapyDoseDto::getNumerator);

            if (administrationInfusionRate.isPresent() && administrationInfusionRate.get() != 0)
            {
              lastPositiveRate = administrationInfusionRate.get();
            }
          }
        }
      }
    }

    if (lastAdministration != null && lastAdministration.getAdministrationType() != AdministrationTypeEnum.STOP)
    {
      final Opt<TherapyDoseDto> lastAdministrationRate = getAdministrationInfusionRate(lastAdministration);
      rateRow.setCurrentInfusionRate(lastAdministrationRate.map(TherapyDoseDto::getNumerator).orElse(null));
      rateRow.setRateUnit(lastAdministrationRate.map(TherapyDoseDto::getNumeratorUnit).orElse(null));
    }
    if (lastBeforeAdministration != null && lastBeforeAdministration.getAdministrationType() != AdministrationTypeEnum.STOP)
    {
      final Opt<TherapyDoseDto> administrationInfusionRate = getAdministrationInfusionRate(lastBeforeAdministration);
      rateRow.setInfusionRateAtIntervalStart(administrationInfusionRate.map(TherapyDoseDto::getNumerator).orElse(null));
      rateRow.setInfusionFormulaAtIntervalStart(administrationInfusionRate.map(TherapyDoseDto::getDenominator).orElse(null));
      rateRow.setFormulaUnit(administrationInfusionRate.map(TherapyDoseDto::getDenominatorUnit).orElse(null));
    }

    rateRow.setLastPositiveInfusionRate(lastPositiveRate);
    rateRow.setInfusionActive(
        lastAdministration != null
            && AdministrationTypeEnum.START_OR_ADJUST.contains(lastAdministration.getAdministrationType()));
  }

  private Opt<TherapyDoseDto> getAdministrationInfusionRate(final AdministrationDto administration)
  {
    if (administration instanceof StartAdministrationDto)
    {
      return Opt.of(((StartAdministrationDto)administration).getAdministeredDose());
    }
    if (administration instanceof AdjustInfusionAdministrationDto)
    {
      return Opt.of(((AdjustInfusionAdministrationDto)administration).getAdministeredDose());
    }
    return Opt.none();
  }

  private void fillOxygenData(final OxygenTherapyRowDtoDto oxygenRow, final DateTime before)
  {
    oxygenRow.setStartingDeviceAtIntervalStart(getLastOxygenStartingDevice(oxygenRow.getAdministrations(), before).get());
    oxygenRow.setCurrentStartingDevice(getLastOxygenStartingDevice(oxygenRow.getAdministrations(), null).get());
  }

  private Opt<OxygenStartingDevice> getLastOxygenStartingDevice(
      final Collection<AdministrationDto> administrations,
      final DateTime before)
  {
    final Predicate<AdministrationDto> isAdministered = a ->
        AdministrationResultEnum.ADMINISTERED.contains(a.getAdministrationResult());

    final List<AdministrationDto> beforeAdministrations = administrations
        .stream()
        .filter(a -> before == null || a.getAdministrationTime().isBefore(before))
        .collect(Collectors.toList());

    final Optional<AdministrationDto> lastDeviceChange = beforeAdministrations
        .stream()
        .filter(isAdministered)
        .filter(a -> a instanceof OxygenAdministration)
        .filter(a -> ((OxygenAdministration)a).getStartingDevice() != null)
        .max(Comparator.comparing(AdministrationDto::getAdministrationTime));

    if (lastDeviceChange.isPresent())
    {
      final Optional<DateTime> lastStop = beforeAdministrations
          .stream()
          .filter(isAdministered)
          .filter(a -> a instanceof StopAdministrationDto)
          .map(AdministrationDto::getAdministrationTime)
          .max(Comparator.naturalOrder());

      if (!lastStop.isPresent() || lastDeviceChange.get().getAdministrationTime().isBefore(lastStop.get()))
      {
        return Opt.of(((OxygenAdministration)lastDeviceChange.get()).getStartingDevice());
      }
    }

    return Opt.none();
  }

  private void filterAdministrationsByTime(final Map<String, TherapyRowDto> therapyTimelineRowsMap, final Interval interval)
  {
    for (final String timelineKey : therapyTimelineRowsMap.keySet())
    {
      final TherapyRowDto timelineRow = therapyTimelineRowsMap.get(timelineKey);
      final List<AdministrationDto> filteredAdministrations = timelineRow.getAdministrations()
          .stream()
          .filter(administration -> administration.getAdministrationTime() != null &&
              interval.contains(administration.getAdministrationTime()))
          .collect(Collectors.toList());
      timelineRow.setAdministrations(filteredAdministrations);
    }
  }

  @Override
  public void confirmTherapyAdministration(
      final @NonNull String therapyCompositionUid,
      final @NonNull String patientId,
      final @NonNull String userId,
      final @NonNull AdministrationDto administration,
      final boolean edit,
      final boolean requestSupply,
      final String centralCaseId,
      final String careProviderId,
      final @NonNull Locale locale,
      final @NonNull DateTime when)
  {
    if (!edit && administration.getTaskId() != null)
    {
      if (!processService.taskExists(administration.getTaskId()))
      {
        throw new UserWarning(Dictionary.getEntry("data.changed.please.reload", locale));
      }
    }

    final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        therapyCompositionUid);

    final TherapyDto therapy = medicationsBo.convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        inpatientPrescription.getMedicationOrder(),
        null,
        null,
        false,
        locale);

    final boolean additionalAdministration = administration.isAdditionalAdministration();
    final boolean normalInfusion = therapy.isNormalInfusion();
    final boolean startAdditionalInfusionWithRate = additionalAdministration
        && normalInfusion
        && administration.getAdministrationType() == AdministrationTypeEnum.START;

    if (startAdditionalInfusionWithRate)
    {
      administration.setGroupUUId(administrationUtils.generateGroupUUId(when));
      Opt.resolve(((StartAdministrationDto)administration)::getAdministeredDose)
          .ifPresent(dose -> dose.setTherapyDoseTypeEnum(TherapyDoseTypeEnum.RATE));
    }

    final String administrationUid = handleConfirmAdministration(
        patientId,
        userId,
        administration,
        edit,
        centralCaseId,
        careProviderId,
        when,
        inpatientPrescription);

    if (startAdditionalInfusionWithRate)
    {
      final TherapyDoseDto dose = ((StartAdministrationDto)administration).getAdministeredDose();
      final List<NewTaskRequestDto> requests = administrationTaskCreator.createRequestsForAdditionalRateAdministration(
          patientId,
          therapy,
          administration.getAdministrationTime(),
          dose,
          administration.getGroupUUId(),
          false);

      processService.createTasks(requests.toArray(new NewTaskRequestDto[requests.size()]));
    }

    handleTasksOnConfirm(patientId, userId, administration, inpatientPrescription, therapy, requestSupply, when, locale);

    if (administration.getAdministrationType() == AdministrationTypeEnum.STOP &&
        !PrescriptionsEhrUtils.isTherapySuspended(inpatientPrescription))
    {
      startPossibleLinkedTherapy(patientId, therapyCompositionUid, administration.getAdministrationTime());
    }

    if (!additionalAdministration && !edit)
    {
      medicationsTasksHandler.associateTaskWithAdministration(administration.getTaskId(), administrationUid);
      processService.completeTasks(administration.getTaskId());
    }
  }

  private String handleConfirmAdministration(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final boolean edit,
      final String centralCaseId,
      final String careProviderId,
      final DateTime when,
      final InpatientPrescription inpatientPrescription)
  {
    if (AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()))
    {
      return confirmTherapyAdministration(
          inpatientPrescription,
          patientId,
          userId,
          administration,
          administration.getAdministrationResult(),
          edit,
          centralCaseId,
          careProviderId,
          when);
    }
    if (administration.getAdministrationType() == AdministrationTypeEnum.INFUSION_SET_CHANGE)
    {
      return confirmInfusionSetChange(
          inpatientPrescription,
          patientId,
          (InfusionSetChangeDto)administration,
          when,
          centralCaseId,
          careProviderId);
    }

    throw new IllegalArgumentException("Administration type: " + administration.getAdministrationType().name() + " not supported");
  }

  private void startPossibleLinkedTherapy(
      final String patientId,
      final String therapyCompositionUid,
      final DateTime startTime)
  {
    final List<InpatientPrescription> linkedPrescriptions = medicationsOpenEhrDao.getLinkedPrescriptions(
        patientId,
        therapyCompositionUid,
        EhrLinkType.FOLLOW);

    Preconditions.checkArgument(linkedPrescriptions.size() < 2, "not more than 1 follow therapy should exist");

    if (!linkedPrescriptions.isEmpty())
    {
      final InpatientPrescription linkedPrescription = linkedPrescriptions.get(0);

      final boolean prescriptionAlreadyStarted = linkedPrescription.getActions().stream()
          .anyMatch(a -> MedicationActionEnum.getActionEnum(a) == MedicationActionEnum.START);

      if (!prescriptionAlreadyStarted)
      {
        final boolean linkedTherapyStarted = therapyUpdater.startLinkedTherapy(patientId, linkedPrescription, startTime);
        if (linkedTherapyStarted)
        {
          therapyUpdater.createTherapyTasks(
              patientId,
              linkedPrescription,
              AdministrationTaskCreateActionEnum.PRESCRIBE,
              null,
              startTime);
        }
      }
    }
  }

  private void handleTasksOnConfirm(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final InpatientPrescription inpatientPrescription,
      final TherapyDto therapy,
      final boolean requestSupply,
      final DateTime when,
      final Locale locale)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(inpatientPrescription);
    handleRequestSupply(patientId, originalTherapyId, requestSupply, locale);
    handleSyringeTasks(patientId, userId, originalTherapyId);
    handleGroupTasksOnAdministrationConfirm(patientId, userId, administration, inpatientPrescription, therapy, when);
  }

  private void handleRequestSupply(
      final String patientId,
      final String originalTherapyId,
      final boolean requestSupply,
      final Locale locale)
  {
    if (requestSupply)
    {
      try
      {
        pharmacySupplyProcessHandler.handleSupplyRequest(patientId, TherapyAssigneeEnum.NURSE, originalTherapyId, null, null);
      }
      catch (final IllegalStateException ise)
      {
        //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
        throw new UserWarning(Dictionary.getEntry("nurse.resupply.request.already.exists.warning", locale));
      }
    }
  }

  private void handleSyringeTasks(final String patientId, final String userId, final String originalTherapyId)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasksForTherapy(
        patientId,
        userId,
        TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE,
        originalTherapyId);
  }

  void handleGroupTasksOnAdministrationConfirm(
      final String patientId,
      final String userId,
      final AdministrationDto administration,
      final InpatientPrescription inpatientPrescription,
      final TherapyDto therapy,
      final DateTime when)
  {
    if (therapy.isNormalInfusion())
    {
      final AdministrationTypeEnum type = administration.getAdministrationType();
      final DateTime administrationTime = administration.getAdministrationTime();
      final boolean administered = AdministrationResultEnum.ADMINISTERED.contains(administration.getAdministrationResult());
      final String groupUUId = administration.getGroupUUId();
      final String therapyId = TherapyIdUtils.createTherapyId(therapy.getCompositionUid(), therapy.getEhrOrderName());

      final boolean start = type == AdministrationTypeEnum.START;
      final boolean administeredAdjustInfusion = type == AdministrationTypeEnum.ADJUST_INFUSION && administered;

      if (start)
      {
        if (administration.isAdministeredAtDifferentTime() || administration.getAdministrationId() != null)
        {
          rescheduleOtherGroupTasks(patientId, administration, administrationTime, groupUUId);
        }
      }

      if (start || administeredAdjustInfusion)
      {
        final EnumSet<AdministrationTypeEnum> confirmTypes =
            administered ? EnumSet.of(AdministrationTypeEnum.STOP)
                         : EnumSet.of(AdministrationTypeEnum.STOP, AdministrationTypeEnum.ADJUST_INFUSION);
        handleGroupTasks(
            patientId,
            therapyId,
            groupUUId,
            userId,
            administrationTime,
            inpatientPrescription,
            confirmTypes,
            administration.getAdministrationResult(),
            when);
      }
    }
  }

  private void rescheduleOtherGroupTasks(
      final String patientId,
      final AdministrationDto administration,
      final DateTime administrationTime,
      final String groupUUId)
  {
    final DateTime previousTime = Opt.of(administration.getAdministrationId())
        .map(id -> medicationsOpenEhrDao.getAdministrationTime(patientId, id).orElse(null))
        .orElseGet(administration::getPlannedTime);

    final long diff = administrationTime.getMillis() - previousTime.getMillis();

    tasksRescheduler.rescheduleAdministrationGroup(
        patientId,
        administration.getTherapyId(),
        groupUUId,
        administration.getTaskId(), // do not change start task due time when administered at different time
        diff);
  }

  private void handleGroupTasks(
      final String patientId,
      final String therapyId,
      final String groupUUId,
      final String userId,
      final DateTime tasksDueAfter,
      final InpatientPrescription inpatientPrescription,
      final EnumSet<AdministrationTypeEnum> types,
      final AdministrationResultEnum result,
      final DateTime when)
  {
    final boolean administered = AdministrationResultEnum.ADMINISTERED.contains(result);

    medicationsTasksProvider.findAdministrationTasks(
        patientId,
        Collections.singletonList(therapyId),
        tasksDueAfter,
        null,
        groupUUId,
        true)
        .stream()
        .filter(t -> isTaskOfType(t, types))
        .forEach(t -> handleOtherGroupTask(patientId, userId, inpatientPrescription, result, when, administered, t));
  }

  private void handleOtherGroupTask(
      final String patientId,
      final String userId,
      final InpatientPrescription inpatientPrescription,
      final AdministrationResultEnum result,
      final DateTime when,
      final boolean administered,
      final TaskDto groupTask)
  {
    final AdministrationTaskDto groupAdminTask = administrationTaskConverter.convertTaskToAdministrationTask(groupTask);
    final DateTime plannedTime = groupAdminTask.getPlannedAdministrationTime();
    final boolean taskInPast = plannedTime != null && plannedTime.isBefore(when);
    final boolean taskInFuture = !taskInPast;

    if (groupTask.isCompleted()) //already confirmed group confirmed task
    {
      if (administered && taskInFuture)
      {
        deleteAdministrationForGroupTask(patientId, groupAdminTask);
      }
      else
      {
        updateAdministrationStatusForGroupTask(patientId, result, groupAdminTask);
      }
    }
    else if (!administered || taskInPast)
    {
      confirmGroupTask(patientId, userId, inpatientPrescription, result, when, groupAdminTask);
    }
  }

  private void confirmGroupTask(
      final String patientId,
      final String userId,
      final InpatientPrescription inpatientPrescription,
      final AdministrationResultEnum result,
      final DateTime when,
      final AdministrationTaskDto groupAdminTask)
  {
    final AdministrationDto administration = administrationTaskConverter.buildAdministrationFromTask(
        groupAdminTask,
        when);
    administration.setAdministrationTime(groupAdminTask.getPlannedAdministrationTime());

    final String administrationUid = confirmTherapyAdministration(
        inpatientPrescription,
        patientId,
        userId,
        administration,
        result,
        false,
        null,
        null,
        when);

    medicationsTasksHandler.associateTaskWithAdministration(administration.getTaskId(), administrationUid);
    processService.completeTasks(administration.getTaskId());
  }

  private void updateAdministrationStatusForGroupTask(
      final String patientId,
      final AdministrationResultEnum result,
      final AdministrationTaskDto groupAdminTask)
  {
    final MedicationAdministration ehrAdministration = medicationsOpenEhrDao.loadMedicationAdministration(
        patientId,
        TherapyIdUtils.getCompositionUidWithoutVersion(groupAdminTask.getAdministrationId()));

    final MedicationActionEnum medicationAction = MedicationActionEnum.fromAdministrationResult(result);
    ehrAdministration.getMedicationManagement().setIsmTransition(medicationAction.buildIsmTransition());
    medicationsOpenEhrDao.saveComposition(patientId, ehrAdministration, ehrAdministration.getUid());
  }

  private void deleteAdministrationForGroupTask(final String patientId, final AdministrationTaskDto groupAdminTask)
  {
    medicationsOpenEhrDao.deleteTherapyAdministration(
        patientId,
        TherapyIdUtils.getCompositionUidWithoutVersion(groupAdminTask.getAdministrationId()),
        null);
    medicationsTasksHandler.undoCompleteTask(groupAdminTask.getTaskId());
  }

  private boolean isTaskOfType(final TaskDto task, final EnumSet<AdministrationTypeEnum> types)
  {
    final String typeVar = (String)task.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName());
    return types.contains(AdministrationTypeEnum.valueOf(typeVar));
  }

  @Override
  public void confirmAdministrationTask(
      final @NonNull String patientId,
      final @NonNull InpatientPrescription prescription,
      final @NonNull TaskDto task,
      final AutomaticChartingType autoChartingType,
      final UserDto recordingUser,
      final @NonNull DateTime when)
  {
    final AdministrationDto administration = administrationTaskConverter.buildAdministrationFromTask(
        administrationTaskConverter.convertTaskToAdministrationTask(task),
        when);

    Preconditions.checkArgument(AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administration.getAdministrationType()));

    final String taskId = administration.getTaskId();

    if (autoChartingType == AutomaticChartingType.SELF_ADMINISTER)
    {
      administration.setAdministrationResult(AdministrationResultEnum.SELF_ADMINISTERED);
      administration.setSelfAdministrationType(SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED);
    }

    administration.setAdministrationTime(administration.getPlannedTime());

    if (administration instanceof PlannedDoseAdministration && administration instanceof DoseAdministration)
    {
      final PlannedDoseAdministration plannedDoseAdministration = (PlannedDoseAdministration)administration;
      final DoseAdministration doseAdministration = (DoseAdministration)administration;

      doseAdministration.setAdministeredDose(plannedDoseAdministration.getPlannedDose());
    }

    final UserDto user = recordingUser != null ? recordingUser : RequestUser.getUser();

    final MedicationAdministration medicationAdministration =
        administrationToEhrConverter.convertAdministration(
            prescription,
            administration,
            AdministrationResultEnum.SELF_ADMINISTERED,
            user.getFullName(),
            user.getId(),
            EhrValueUtils.getText(prescription.getContext().getContextDetail().getPeriodOfCareIdentifier()),
            EhrValueUtils.getText(prescription.getContext().getContextDetail().getDepartmentalPeriodOfCareIdentifier()),
            when);

    final String administrationUid = administrationSaver.save(patientId, medicationAdministration, administration.getAdministrationId());

    medicationsTasksHandler.associateTaskWithAdministration(taskId, administrationUid);
    processService.completeTasks(taskId);
  }

  @Override
  public String confirmTherapyAdministration(
      final @NonNull InpatientPrescription inpatientPrescription,
      final @NonNull String patientId,
      final @NonNull String userId,
      final @NonNull AdministrationDto administrationDto,
      final @NonNull AdministrationResultEnum administrationResult,
      final boolean edit,
      final String centralCaseId,
      final String careProviderId,
      final @NonNull DateTime when)
  {
    Preconditions.checkArgument(AdministrationTypeEnum.MEDICATION_ADMINISTRATION.contains(administrationDto.getAdministrationType()));

    final MedicationAdministration administration = administrationToEhrConverter.convertAdministration(
        inpatientPrescription,
        administrationDto,
        administrationResult,
        RequestUser.getFullName(),
        RequestUser.getId(),
        centralCaseId,
        careProviderId,
        when);

    if (!edit && administrationDto.getTaskId() != null)
    {
      handleChangeTherapySelfAdministeringType(inpatientPrescription, administrationDto, userId, patientId, when);
    }

    return administrationSaver.save(patientId, administration, administrationDto.getAdministrationId());
  }

  private void handleChangeTherapySelfAdministeringType(
      final InpatientPrescription prescription,
      final AdministrationDto administrationDto,
      final String userId,
      final String patientId,
      final DateTime when)
  {
    final MedicationOrder order = prescription.getMedicationOrder();
    final AdministrationResultEnum administrationResult = administrationDto.getAdministrationResult();


    final SelfAdministeringActionEnum prescriptionSelfAdminType =
        SelfAdministeringActionEnum.valueOf(order.getAdditionalDetails().getSelfAdministrationType());

    if (administrationResult == AdministrationResultEnum.SELF_ADMINISTERED)
    {
      final SelfAdministeringActionEnum administrationSelfAdminType = administrationDto.getSelfAdministrationType();

      Preconditions.checkNotNull(
          administrationSelfAdminType,
          "Self administration type for administration must be set if result is SELF_ADMINISTERED");

      if (administrationSelfAdminType != prescriptionSelfAdminType)
      {
        therapyUpdater.updateTherapySelfAdministeringStatus(
            patientId,
            prescription,
            administrationSelfAdminType,
            userId,
            when);
      }
    }
    else if (administrationResult == AdministrationResultEnum.GIVEN && prescriptionSelfAdminType != null)
    {
      therapyUpdater.updateTherapySelfAdministeringStatus(
          patientId,
          prescription,
          SelfAdministeringActionEnum.STOP_SELF_ADMINISTERING,
          userId,
          when);
    }
  }

  private String confirmInfusionSetChange(
      final InpatientPrescription prescription,
      final String patientId,
      final InfusionSetChangeDto administration,
      final DateTime when,
      final String centralCaseId,
      final String careProviderId)
  {
    final MedicationAdministration medicationAdministration = administrationToEhrConverter.convertSetChangeAdministration(
        prescription,
        administration,
        centralCaseId,
        careProviderId,
        when);

    return administrationSaver.save(patientId, medicationAdministration, administration.getAdministrationId());
  }
}
