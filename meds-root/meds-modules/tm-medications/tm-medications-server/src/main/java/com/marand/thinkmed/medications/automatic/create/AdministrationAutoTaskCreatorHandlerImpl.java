package com.marand.thinkmed.medications.automatic.create;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.maf.core.Opt;
import com.marand.maf.core.StackTraceUtils;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationAutoTaskCreatorHandlerImpl implements AdministrationAutoTaskCreatorHandler
{
  private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoTaskCreatorHandlerImpl.class);

  private ProcessService processService;
  private AdministrationTaskCreator administrationTaskCreator;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsBo medicationsBo;

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
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
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Override
  @EhrSessioned
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createAdministrationTasksOnAutoCreate(
      final @NonNull AutomaticAdministrationTaskCreatorDto taskCreatorDto,
      final @NonNull DateTime actionTimestamp)
  {
    final List<NewTaskRequestDto> taskRequestDtos = administrationTaskCreator.createTaskRequests(
        taskCreatorDto.getPatientId(),
        taskCreatorDto.getTherapyDto(),
        AdministrationTaskCreateActionEnum.AUTO_CREATE,
        actionTimestamp,
        taskCreatorDto.getLastAdministrationTime(),
        null);

    if (!taskRequestDtos.isEmpty())
    {
      final TaskDto[] tasks = processService.createTasks(taskRequestDtos.toArray(new NewTaskRequestDto[taskRequestDtos.size()]));

      final String tasksDisplay = Arrays.stream(tasks)
          .map(t -> "(" + t.getId() + ", " + t.getDueTime() + ")")
          .collect(Collectors.joining(","));

      LOG.info("Created " + tasks.length + " tasks for [patient, composition]: "
                   + taskCreatorDto.getPatientId() + ", " + taskCreatorDto.getTherapyDto().getCompositionUid()
                   + " - tasks [id, dueTime]: " + tasksDisplay);
    }
  }

  @Override
  public List<AutomaticAdministrationTaskCreatorDto> getAutoAdministrationTaskCreatorDtos(
      final @NonNull DateTime when,
      final @NonNull Map<InpatientPrescription, String> activePrescriptionsWithPatientIds)
  {
    final Set<String> patientIds = new HashSet<>(activePrescriptionsWithPatientIds.values());

    final Map<String, DateTime> lastAdministrationTaskTimesForTherapies = new HashMap<>();
    for (final String patientId : patientIds)
    {
      lastAdministrationTaskTimesForTherapies.putAll(
          medicationsTasksProvider.findLastAdministrationTaskTimesForTherapies(
              Collections.singletonList(patientId),
              when.minusDays(7),
              true));
    }

    final List<AutomaticAdministrationTaskCreatorDto> automaticAdministrationTaskCreatorDtos = new ArrayList<>();
    for (final Map.Entry<InpatientPrescription, String> entry : activePrescriptionsWithPatientIds.entrySet())
    {
      final InpatientPrescription prescription = entry.getKey();
      try
      {
        final TherapyDto therapyDto = medicationsBo.convertMedicationOrderToTherapyDto(
            prescription,
            prescription.getMedicationOrder(),
            null,
            null,
            false,
            null);

        final String therapyId = TherapyIdUtils.createTherapyId(prescription.getUid(), prescription.getMedicationOrder().getName().getValue());
        final DateTime lastAdministrationTime = lastAdministrationTaskTimesForTherapies.getOrDefault(therapyId, null);

        final String patientId = entry.getValue();
        final AutomaticAdministrationTaskCreatorDto administrationTaskCreatorDto = new AutomaticAdministrationTaskCreatorDto(
            therapyDto,
            patientId,
            lastAdministrationTime);

        automaticAdministrationTaskCreatorDtos.add(administrationTaskCreatorDto);
      }
      catch (final Throwable t)
      {
        LOG.error("Error converting from composition : " + Opt.resolve(prescription::getUid).get() + " " + StackTraceUtils.getStackTraceString(t));
      }
    }

    return automaticAdministrationTaskCreatorDtos;
  }
}
