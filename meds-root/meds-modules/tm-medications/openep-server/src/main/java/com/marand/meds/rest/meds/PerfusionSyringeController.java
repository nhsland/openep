package com.marand.meds.rest.meds;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class PerfusionSyringeController
{

  private final MedicationsService service;

  @Autowired
  public PerfusionSyringeController(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "deletePerfusionSyringeRequest")
  public void deletePerfusionSyringeTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("language") final String language)
  {
    service.deletePerfusionSyringeRequest(taskId, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "undoPerfusionSyringeRequestState")
  public Map<String, String> undoPerfusionSyringeTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam(value = "isUrgent", required = false) final Boolean isUrgent)
  {
    return service.undoPerfusionSyringeRequestState(
        patientId,
        taskId,
        isUrgent);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "confirmPerfusionSyringePreparations")
  public Map<String, String> confirmPerfusionSyringePreparations(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskIds,
      @RequestParam(value = "isUrgent", required = false) final Boolean isUrgent)
  {
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds, String[].class);
    return service.confirmPerfusionSyringePreparations(
        patientId,
        Lists.newArrayList(taskIdsArray),
        isUrgent);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "dispensePerfusionSyringePreparations")
  public Map<String, String> dispensePerfusionSyringePreparations(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskIds,
      @RequestParam("isUrgent") final Boolean isUrgent)
  {
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds, String[].class);
    return service.dispensePerfusionSyringePreparations(
        patientId,
        Lists.newArrayList(taskIdsArray),
        isUrgent);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "orderPerfusionSyringePreparation")
  public void orderPerfusionSyringePreparation(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam("numberOfSyringes") final Integer numberOfSyringes,
      @RequestParam("urgent") final Boolean urgent,
      @RequestParam("dueTime") final String dueTimeJson,
      @RequestParam("printSystemLabel") final Boolean printSystemLabel)
  {
    final DateTime dueTime = JsonUtil.fromJson(dueTimeJson, DateTime.class);

    service.orderPerfusionSyringePreparation(
        patientId,
        compositionUid,
        ehrOrderName,
        numberOfSyringes,
        urgent,
        dueTime,
        printSystemLabel);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "startPerfusionSyringePreparations")
  public Map<String, PerfusionSyringePreparationDto> startPerfusionSyringePreparations(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "taskIds", required = false) final String taskIds,
      @RequestParam(value = "originalTherapyIds", required = false) final String originalTherapyIds,
      @RequestParam(value = "isUrgent", required = false) final Boolean isUrgent,
      @RequestParam("language") final String language)
  {
    //starts preparation and prints data
    final String[] taskIdsArray = JsonUtil.fromJson(taskIds, String[].class);
    final String[] originalTherapyIdsArray = JsonUtil.fromJson(originalTherapyIds, String[].class);

    return
        service.startPerfusionSyringePreparations(
            patientId,
            Lists.newArrayList(taskIdsArray),
            Sets.newHashSet(originalTherapyIdsArray),
            isUrgent,
            new Locale(language));
  }

  @GetMapping(value = "finishedPerfusionSyringeRequestsExistInLastHours")
  public boolean finishedPerfusionSyringeRequestsExistInLastHours(
      @RequestParam("patientId") final String patientId,
      @RequestParam("originalTherapyId") final String originalTherapyId,
      @RequestParam("hours") final Integer hours)
  {
    return
        service.finishedPerfusionSyringeRequestsExistInLastHours(
            patientId,
            originalTherapyId,
            hours);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "findFinishedPerfusionSyringePreparationRequests", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PerfusionSyringePatientTasksDto> getFinishedPerfusionSyringePreparationRequests(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds,
      @RequestParam("date") final String dateJson,
      @RequestParam("language") final String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final DateTime date = JsonUtil.fromJson(dateJson, DateTime.class);

    return
        service.findFinishedPerfusionSyringePreparationRequests(
            careProviderIdsOpt,
            patientIdsOpt,
            Intervals.wholeDay(date),
            new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "findPerfusionSyringePreparationRequests", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PerfusionSyringePatientTasksDto> getPerfusionSyringePreparationRequests(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds,
      @RequestParam("taskTypes") final String taskTypes,
      @RequestParam("language") final String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypesSet = getTaskTypes(JsonUtil.fromJson(taskTypes, String[].class));

    return
        service.findPerfusionSyringePreparationRequests(
            careProviderIdsOpt,
            patientIdsOpt,
            taskTypesSet,
            new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPerfusionSyringeTaskSimpleDto", produces = MediaType.APPLICATION_JSON_VALUE)
  public PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(
      @RequestParam("taskId") final String taskId,
      @RequestParam("language") final String language)
  {
    return service.getPerfusionSyringeTaskSimpleDto(
        taskId,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "editPerfusionSyringeTask")
  public void editSupplyReminderTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("numberOfSyringes") final Integer numberOfSyringes,
      @RequestParam("urgent") final Boolean urgent,
      @RequestParam("dueTime") final String dueTimeJson,
      @RequestParam("printSystemLabel") final Boolean printSystemLabel)
  {
    final DateTime dueTime = JsonUtil.fromJson(dueTimeJson, DateTime.class);
    service.editPerfusionSyringeTask(
        taskId,
        numberOfSyringes,
        urgent,
        dueTime,
        printSystemLabel);
  }

  private Opt<Collection<String>> resolveStringArrayParameterToSetOpt(final String parameter)
  {
    return Opt.resolve(() -> Sets.newHashSet(JsonUtil.fromJson(parameter, String[].class)));
  }

  private static Set<TaskTypeEnum> getTaskTypes(final String[] taskTypeNamesArray)
  {
    final Set<TaskTypeEnum> taskTypesSet = new HashSet<>();
    for (final String taskTypeName : taskTypeNamesArray)
    {
      taskTypesSet.add(TaskTypeEnum.valueOf(taskTypeName));
    }
    return taskTypesSet;
  }
}
