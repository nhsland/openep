package com.marand.thinkmed.medications.administration.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationDoctorConfirmationHandler
{
  private MedicationsTasksProvider medicationsTasksProvider;
  private ProcessService processService;
  private AdministrationHandler administrationHandler;

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
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  public void setDoctorConfirmation(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final boolean result)
  {
    if (result)
    {
      setDoctorConfirmationTrue(patientId, administration);
    }
    else
    {
      setDoctorConfirmationFalse(patientId, administration);
    }
  }

  private void setDoctorConfirmationFalse(final @NonNull String patientId, final @NonNull AdministrationDto administration)
  {
    administrationHandler.cancelAdministrationTask(
        patientId,
        administration,
        NotAdministeredReasonEnum.DOCTOR_CONFIRMATION_FALSE,
        null);
  }

  private void setDoctorConfirmationTrue(final @NonNull String patientId, final @NonNull AdministrationDto administration)
  {
    final List<String> taskIds;
    if (administration.getAdministrationId() != null)
    {
      taskIds = administrationHandler.uncancelAdministrationTask(patientId, administration);
    }
    else if (administration.getGroupUUId() != null)
    {
      final List<TaskDto> tasksInSameGroup = medicationsTasksProvider.findAdministrationTasks(
          patientId,
          null,
          null,
          null,
          administration.getGroupUUId(),
          true);

      taskIds = tasksInSameGroup.stream()
          .map(AbstractTaskDto::getId)
          .collect(Collectors.toList());
    }
    else
    {
      taskIds = Lists.newArrayList(administration.getTaskId());
    }

    taskIds.forEach(t -> processService.setVariable(t, AdministrationTaskDef.DOCTOR_CONFIRMATION.getName(), true));
  }
}
