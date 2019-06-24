package com.marand.thinkmed.medications.infusion.impl;

import java.util.Collections;
import java.util.List;

import com.marand.maf.core.Pair;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.InfusionBagDto;
import com.marand.thinkmed.medications.dto.administration.InfusionBagTaskDto;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskHandler;
import com.marand.thinkmed.medications.infusion.InfusionBagTaskProvider;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.InfusionBagDeleted;
import com.marand.thinkmed.medications.task.InfusionBagChangeTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component
public class InfusionBagTaskHandlerImpl implements InfusionBagTaskHandler
{
  private ProcessService processService;
  private InfusionBagTaskProvider infusionBagTaskProvider;
  private MedicationsTasksHandler medicationsTasksHandler;

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setInfusionBagTaskProvider(final InfusionBagTaskProvider infusionBagTaskProvider)
  {
    this.infusionBagTaskProvider = infusionBagTaskProvider;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Override
  public InfusionBagTaskDto convertTaskToInfusionBagTask(final @NonNull TaskDto task)
  {
    final InfusionBagTaskDto infusionBagTask = new InfusionBagTaskDto();
    final String therapyId = (String)task.getVariables().get(InfusionBagChangeTaskDef.THERAPY_ID.getName());
    infusionBagTask.setTherapyId(therapyId);

    infusionBagTask.setTaskId(task.getId());
    infusionBagTask.setPlannedAdministrationTime(task.getDueTime());

    final String administrationId = (String)task.getVariables().get(InfusionBagChangeTaskDef.THERAPY_ADMINISTRATION_ID.getName());
    infusionBagTask.setAdministrationId(administrationId);

    final Double quantity = (Double)task.getVariables().get(InfusionBagChangeTaskDef.INFUSION_BAG_QUANTITY.getName());
    final String quantityUnit = (String)task.getVariables().get(InfusionBagChangeTaskDef.INFUSION_BAG_UNIT.getName());

    final InfusionBagDto infusionBag = new InfusionBagDto(quantity, quantityUnit);
    infusionBagTask.setInfusionBag(infusionBag);
    infusionBagTask.setAdministrationTypeEnum(AdministrationTypeEnum.INFUSION_SET_CHANGE);

    return infusionBagTask;
  }

  @Override
  @EventProducer(InfusionBagDeleted.class)
  public void deleteInfusionBagTasks(
      final @NonNull String patientId,
      final @NonNull List<String> therapyIds,
      final String comment)
  {
    final List<TaskDto> tasks =
        infusionBagTaskProvider.findTasks(
            Collections.singletonList(InfusionBagChangeTaskDef.INSTANCE.buildKey(patientId)),
            therapyIds,
            false,
            null,
            null);

    tasks.forEach(taskDto -> medicationsTasksHandler.deleteTask(taskDto.getId(), comment));
  }

  @Override
  public void createInfusionBagTask(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull DateTime plannedTime)
  {
    processService.createTasks(createInfusionBagTaskRequest(patientId, therapyId, plannedTime));
  }

  private NewTaskRequestDto createInfusionBagTaskRequest(
      final String patientId,
      final String therapyId,
      final DateTime plannedTime)
  {
    //noinspection unchecked
    return new NewTaskRequestDto(
        InfusionBagChangeTaskDef.INSTANCE,
        InfusionBagChangeTaskDef.INSTANCE.buildKey(String.valueOf(patientId)),
        "Infusion bag change",
        "Infusion bag change",
        TherapyAssigneeEnum.NURSE.name(),
        plannedTime,
        null,
        Pair.of(MedsTaskDef.PATIENT_ID, patientId),
        Pair.of(InfusionBagChangeTaskDef.THERAPY_ID, therapyId),
        Pair.of(InfusionBagChangeTaskDef.ADMINISTRATION_TYPE, AdministrationTypeEnum.INFUSION_SET_CHANGE));
  }
}
