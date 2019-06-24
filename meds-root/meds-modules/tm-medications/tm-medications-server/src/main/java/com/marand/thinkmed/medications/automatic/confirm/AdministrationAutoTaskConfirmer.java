package com.marand.thinkmed.medications.automatic.confirm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.charting.TherapyAutomaticChartingDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.event.MedsEventProducer;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.request.user.StaticAuth;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

public class AdministrationAutoTaskConfirmer
{
  private static final Logger LOG = LoggerFactory.getLogger(AdministrationAutoTaskConfirmer.class);

  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationAutoTaskConfirmerHandler administrationAutoTaskConfirmerHandler;
  private MedsEventProducer medsEventProducer;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAdministrationAutoTaskConfirmerHandler(final AdministrationAutoTaskConfirmerHandler administrationAutoTaskConfirmerHandler)
  {
    this.administrationAutoTaskConfirmerHandler = administrationAutoTaskConfirmerHandler;
  }

  @Autowired
  public void setMedsEventProducer(final MedsEventProducer medsEventProducer)
  {
    this.medsEventProducer = medsEventProducer;
  }

  @Transactional
  @Scheduled(cron = "${meds.auto-administration-charting-cron}")
  public void run()
  {
    SecurityContextHolder.getContext().setAuthentication(new StaticAuth("Think!Meds,", "Think!Meds,", "Think!Meds,"));
    automaticCharting(new DateTime());
  }

  private void automaticCharting(final DateTime time)
  {
    final long startMillis = new DateTime().getMillis();

    final SetMultimap<String, AutomaticChartingType> patientsChartingTypes = HashMultimap.create();
    final Map<String, TherapyAutomaticChartingDto> chartingDtosMap = new HashMap<>();
    for (final TherapyAutomaticChartingDto entry : medicationsOpenEhrDao.getAutoChartingTherapyDtos(time))
    {
      patientsChartingTypes.put(entry.getPatientId(), entry.getType());
      chartingDtosMap.put(TherapyIdUtils.createTherapyId(entry.getCompositionUid(), entry.getInstructionName()), entry);
    }

    int count = 0;
    int successCount = 0;
    final Set<String> processedPatients = new HashSet<>();
    final Map<String, InpatientPrescription> processedPrescriptionsMap = new HashMap<>();

    final List<TaskDto> tasks = loadAdministrationTasks(patientsChartingTypes, time);
    final long loadLasted = new DateTime().getMillis() - startMillis;

    for (final TaskDto task : tasks)
    {
      final String therapyId = String.valueOf(task.getVariables().get(AdministrationTaskDef.THERAPY_ID.getName()));
      final TherapyAutomaticChartingDto chartingDto = chartingDtosMap.get(therapyId);

      if (chartingDto != null && shouldConfirmTask(task, tasks, chartingDto))
      {
        final String compositionUid = chartingDto.getCompositionUid();
        final String patientId = chartingDto.getPatientId();

        final InpatientPrescription prescription = processedPrescriptionsMap.computeIfAbsent(
            therapyId,
            t -> medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid));
        try
        {
          count++;
          administrationAutoTaskConfirmerHandler.autoConfirmAdministrationTask(
              chartingDto.getType(),
              patientId,
              prescription,
              task,
              time);
          processedPatients.add(patientId);
          successCount++;
        }
        catch (final Throwable t)
        {
          LOG.error(String.format(
              "Failed confirming task - patientId: %s compositionUid: %s taskId: %s\n%s",
              patientId,
              compositionUid,
              task.getId(),
              ExceptionUtils.getFullStackTrace(t)));
        }
      }
    }

    // clear cache for patients
    processedPatients.forEach(p -> medsEventProducer.triggerAdministrationChanged(p));

    final long lasted = new DateTime().getMillis() - startMillis;
    LOG.debug("Processed " + count + " tasks " +
                  "from " + chartingDtosMap.keySet().size() + " therapies | " +
                  "confirmed " + successCount + " tasks - " +
                  "TOOK: load : " + loadLasted + " ms | all : " + lasted + " ms");
  }

  private boolean shouldConfirmTask(
      final TaskDto task,
      final List<TaskDto> allTasks,
      final TherapyAutomaticChartingDto chartingDto)
  {
    if (chartingDto.isEnabled(task.getDueTime()))
    {
      if (chartingDto.getType() == AutomaticChartingType.SELF_ADMINISTER)
      {
        return true;
      }
      if (chartingDto.getType() == AutomaticChartingType.NORMAL_INFUSION)
      {
        return isStopTaskWithNoActiveGroupStartTask(chartingDto.getPatientId(), task, allTasks);
      }
      throw new IllegalArgumentException("Charting dto type not supported!");
    }
    return false;
  }

  private List<TaskDto> loadAdministrationTasks(
      final Multimap<String, AutomaticChartingType> patientsChartingTypes,
      final DateTime when)
  {
    final Set<String> selfAdminPatientIds = new HashSet<>();
    final Set<String> normalInfusionPatientIds = new HashSet<>();

    for (final String patientId : patientsChartingTypes.keySet())
    {
      final Collection<AutomaticChartingType> types = patientsChartingTypes.get(patientId);
      if (types.contains(AutomaticChartingType.NORMAL_INFUSION))
      {
        normalInfusionPatientIds.add(patientId);
      }
      else if (types.contains(AutomaticChartingType.SELF_ADMINISTER))
      {
        selfAdminPatientIds.add(patientId);
      }
      else
      {
        throw new IllegalArgumentException("Charting dto type not supported!");
      }
    }

    final List<TaskDto> administrationTasks = new ArrayList<>();
    for (final List<String> partition : Lists.partition(new ArrayList<>(normalInfusionPatientIds), 50))
    {
      administrationTasks.addAll(medicationsTasksProvider.findAdministrationTasks(
          new HashSet<>(partition),
          when.minusHours(24),
          when));
    }

    for (final List<String> partition : Lists.partition(new ArrayList<>(selfAdminPatientIds), 100))
    {
      administrationTasks.addAll(medicationsTasksProvider.findAdministrationTasks(
          new HashSet<>(partition),
          when.minusHours(2),
          when));
    }
    return administrationTasks;
  }

  private boolean isStopTaskWithNoActiveGroupStartTask(
      final String patientId,
      final TaskDto task,
      final Collection<TaskDto> tasks)
  {
    final AdministrationTypeEnum administrationType =
        AdministrationTypeEnum.valueOf((String)task.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName()));

    final boolean isStopTask = administrationType == AdministrationTypeEnum.STOP;
    final String groupUUID = (String)task.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName());
    final boolean therapyEndTask = BooleanUtils.isTrue((Boolean)task.getVariables().get(AdministrationTaskDef.THERAPY_END.getName()));

    final boolean isGroupedStopTask = isStopTask && groupUUID != null && !therapyEndTask;
    if (isGroupedStopTask)
    {
      final boolean noStartTaskFound = tasks
          .stream()
          .filter(t -> groupUUID.equals(t.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName())))
          .noneMatch(startTask());

      if (noStartTaskFound)
      {
        return medicationsTasksProvider.findAdministrationTasks(patientId, null, null, null, groupUUID, false)
            .stream()
            .noneMatch(startTask());
      }
    }

    return false;
  }

  private Predicate<TaskDto> startTask()
  {
    return t -> AdministrationTypeEnum.valueOf(
        (String)t.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName())) == AdministrationTypeEnum.START;
  }
}
