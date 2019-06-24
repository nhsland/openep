package com.marand.thinkmed.medications.administration.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.dto.administration.AdministrationStatusEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.InfusionSetChangeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicalDeviceEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdjustOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.BolusAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.OxygenAdministration;
import com.marand.thinkmed.medications.dto.administration.OxygenTaskDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StartOxygenAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.OxygenTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.TaskConstants;
import com.marand.thinkmed.process.definition.TaskVariable;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class AdministrationTaskConverterImpl implements AdministrationTaskConverter
{
  private OverviewContentProvider overviewContentProvider;

  @Autowired
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Override
  public AdministrationDto buildAdministrationFromTask(final AdministrationTaskDto task, final DateTime when)
  {
    final AdministrationDto administration;
    final AdministrationTypeEnum administrationTypeEnum = task.getAdministrationTypeEnum();
    if (task instanceof OxygenTaskDto)
    {
      administration = buildOxygenAdministration((OxygenTaskDto)task);
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.START)
    {
      administration = new StartAdministrationDto();
      ((StartAdministrationDto)administration).setPlannedDose(task.getTherapyDoseDto());
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.BOLUS)
    {
      administration = new BolusAdministrationDto();
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.STOP)
    {
      administration = new StopAdministrationDto();
    }
    else if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      administration = new AdjustInfusionAdministrationDto();
      ((AdjustInfusionAdministrationDto)administration).setPlannedDose(task.getTherapyDoseDto());
    }
    else if (task instanceof InfusionBagTaskDto)
    {
      administration = new InfusionSetChangeDto();
      ((InfusionSetChangeDto)administration).setInfusionSetChangeEnum(InfusionSetChangeEnum.INFUSION_SYRINGE_CHANGE);
    }
    else
    {
      throw new IllegalArgumentException("Administration type not supported!");
    }

    administration.setTherapyId(task.getTherapyId());
    administration.setTaskId(task.getTaskId());
    administration.setGroupUUId(task.getGroupUUId());
    administration.setAdministrationId(task.getAdministrationId());
    administration.setPlannedTime(task.getPlannedAdministrationTime());
    administration.setDoctorsComment(task.getDoctorsComment());

    final AdministrationStatusEnum administrationStatus =
        AdministrationStatusEnum.build(task.getPlannedAdministrationTime(), null, when);
    administration.setAdministrationStatus(administrationStatus);

    if (task.getDoctorConfirmation() != null)
    {
      administration.setDoctorConfirmation(task.getDoctorConfirmation());
    }
    return administration;
  }

  @Override
  public AdministrationDto buildAdministrationFromTask(final TaskDto task, final DateTime when)
  {
    return buildAdministrationFromTask(convertTaskToAdministrationTask(task), when);
  }

  private AdministrationDto buildOxygenAdministration(final OxygenTaskDto task)
  {
    final AdministrationDto administration;
    if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.START)
    {
      administration = new StartOxygenAdministrationDto();
    }
    else if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.ADJUST_INFUSION)
    {
      administration = new AdjustOxygenAdministrationDto();
    }
    else if (task.getAdministrationTypeEnum() == AdministrationTypeEnum.STOP)
    {
      administration = new StopAdministrationDto();
    }
    else
    {
      throw new IllegalArgumentException("This administration type is not supported for oxygen administration");
    }

    if (administration instanceof OxygenAdministration)
    {
      ((OxygenAdministration)administration).setPlannedStartingDevice(task.getStartingDevice());
      ((OxygenAdministration)administration).setPlannedDose(task.getTherapyDoseDto());
    }

    return administration;
  }

  @Override
  public AdministrationDto convertNewTaskRequestDtoToAdministrationDto(
      final NewTaskRequestDto taskRequest, final DateTime when)
  {
    final TaskDto taskDto = convertNewTaskRequestToTaskDto(taskRequest);
    final AdministrationTaskDto administrationTaskDto = convertTaskToAdministrationTask(taskDto);
    return buildAdministrationFromTask(administrationTaskDto, when);
  }

  private TaskDto convertNewTaskRequestToTaskDto(final NewTaskRequestDto request)
  {
    final TaskDto task = new TaskDto();
    task.setName(request.getName());
    task.setDescription(request.getDescription());
    task.setAssignee(request.getAssignee());
    task.setDueTime(request.getDue());
    final Map<String, Object> variables = new HashMap<>();
    for (final Pair<TaskVariable, ?> taskVariablePair : request.getVariables())
    {
      variables.put(taskVariablePair.getFirst().getName(), taskVariablePair.getSecond());
    }
    task.setVariables(variables);
    return task;
  }

  @Override
  public AdministrationTaskDto convertTaskToAdministrationTask(final TaskDto task)
  {
    AdministrationTaskDto administrationTaskDto = new AdministrationTaskDto();
    if (isOxygenAdministrationTask(task))
    {
      administrationTaskDto = buildOxygenAdministrationTask(task);
    }

    administrationTaskDto.setTaskId(task.getId());
    administrationTaskDto.setTherapyId((String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName()));
    administrationTaskDto.setPlannedAdministrationTime(task.getDueTime());
    administrationTaskDto.setAdministrationTypeEnum(
        AdministrationTypeEnum.valueOf(
            (String)task.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName())));
    administrationTaskDto.setAdministrationId(
        (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ADMINISTRATION_ID.getName()));
    if (task.getVariables().containsKey(AdministrationTaskDef.DOCTOR_CONFIRMATION.getName()))
    {
      administrationTaskDto.setDoctorConfirmation(
          (Boolean)task.getVariables()
              .get(AdministrationTaskDef.DOCTOR_CONFIRMATION.getName()));
    }
    if (administrationTaskDto.getAdministrationTypeEnum() != AdministrationTypeEnum.STOP)
    {
      final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
      administrationTaskDto.setTherapyDoseDto(therapyDoseDto);
    }

    administrationTaskDto.setDoctorsComment(
        (String)task.getVariables().get(AdministrationTaskDef.DOCTORS_COMMENT.getName()));

    administrationTaskDto.setGroupUUId((String)task.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName()));

    return administrationTaskDto;
  }

  private OxygenTaskDto buildOxygenAdministrationTask(final TaskDto task)
  {
    final OxygenTaskDto oxygenTaskDto = new OxygenTaskDto();

    final Object deviceRouteObject = task.getVariables().get(OxygenTaskDef.STARTING_DEVICE_ROUTE.getName());
    if (deviceRouteObject != null)
    {
      final OxygenStartingDevice oxygenStartingDevice = new OxygenStartingDevice(MedicalDeviceEnum.valueOf((String)deviceRouteObject));

      Optional.ofNullable(task.getVariables().get(OxygenTaskDef.STARTING_DEVICE_TYPE.getName()))
          .ifPresent(object -> oxygenStartingDevice.setRouteType((String)object));

      oxygenTaskDto.setStartingDevice(oxygenStartingDevice);
    }

    return oxygenTaskDto;
  }

  private boolean isOxygenAdministrationTask(final TaskDto task)
  {
    return task.getVariables().containsKey(OxygenTaskDef.OXYGEN_ADMINISTRATION.getName());
  }

  @Override
  public List<AdministrationPatientTaskDto> convertTasksToAdministrationPatientTasks(
      final List<TaskDto> tasks,
      final Map<String, PatientDisplayWithLocationDto> patientWithLocationMap,
      final Locale locale,
      final DateTime when)
  {
    final Map<String, String> therapyCompositionUidAndPatientIdMap = new HashMap<>();
    for (final TaskDto task : tasks)
    {
      final String patientId = (String)task.getVariables().get(AdministrationTaskDef.PATIENT_ID.getName());
      final String therapyId = (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName());
      therapyCompositionUidAndPatientIdMap.put(TherapyIdUtils.parseTherapyId(therapyId).getFirst(), patientId);
    }

    final List<AdministrationPatientTaskDto> patientTasks = new ArrayList<>();

    final Map<String, TherapyDayDto> compositionIdAndTherapyDayDtoMap =
        overviewContentProvider.getCompositionUidAndTherapyDayDtoMap(therapyCompositionUidAndPatientIdMap, when, locale);

    for (final TaskDto task : tasks)
    {
      final String patientId = (String)task.getVariables().get(AdministrationTaskDef.PATIENT_ID.getName());
      final String therapyId = (String)task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName());

      final AdministrationPatientTaskDto patientTask = new AdministrationPatientTaskDto();
      patientTask.setId(task.getId());
      patientTask.setPlannedDose(TherapyTaskUtils.buildPlannedDoseDisplay(task));
      patientTask.setPlannedTime(task.getDueTime());
      final String compositionUid = TherapyIdUtils.parseTherapyId(therapyId).getFirst();
      patientTask.setTherapyDayDto(compositionIdAndTherapyDayDtoMap.get(compositionUid));
      patientTask.setTaskType(TaskTypeEnum.ADMINISTRATION_TASK);

      final AdministrationStatusEnum status =
          AdministrationStatusEnum.build(task.getDueTime(), null, when);
      patientTask.setAdministrationStatus(status);

      final PatientDisplayWithLocationDto patientWithLocation = patientWithLocationMap.get(patientId);
      patientTask.setPatientDisplayDto(patientWithLocation.getPatientDisplayDto());
      patientTask.setCareProviderName(patientWithLocation.getCareProviderName());
      patientTask.setRoomAndBed(patientWithLocation.getRoomAndBed());

      patientTasks.add(patientTask);
    }
    return patientTasks;
  }

  @Override
  public boolean isAdministrationTask(final @NonNull TaskDto task)
  {
    return AdministrationTaskDef.INSTANCE.getTaskExecutionId().equals(
        task.getVariables().get(TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME));
  }
}
