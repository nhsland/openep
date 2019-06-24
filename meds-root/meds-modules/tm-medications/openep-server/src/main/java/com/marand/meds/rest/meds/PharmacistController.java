package com.marand.meds.rest.meds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
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
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.impl.PharmacistReviewServiceImpl;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.marand.meds.config.GsonDeserializators.INTERVAL_DESERIALIZER;
import static com.marand.meds.config.GsonDeserializators.THERAPY_CHANGE_DESERIALIZER;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class PharmacistController
{

  private final MedicationsService service;
  private final PharmacistReviewServiceImpl pharmacistReviewService;

  @Autowired
  public PharmacistController(
      final MedicationsService service,
      final PharmacistReviewServiceImpl pharmacistReviewService)
  {
    this.service = service;
    this.pharmacistReviewService = pharmacistReviewService;
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

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "savePharmacistReview", produces = MediaType.APPLICATION_JSON_VALUE)
  public String savePharmacistReview(
      @RequestParam("patientId") final String patientId,
      @RequestParam("pharmacistReview") final String pharmacistReviewJson,
      @RequestParam("authorize") final Boolean authorize,
      @RequestParam("language") final String language)
  {
    final List<JsonUtil.TypeAdapterPair> typeAdapters = new ArrayList<>(MedsJsonDeserializer.INSTANCE.getTypeAdapters());
    typeAdapters.add(THERAPY_CHANGE_DESERIALIZER);

    final PharmacistReviewDto pharmacistReview =
        JsonUtil.fromJson(
            pharmacistReviewJson,
            PharmacistReviewDto.class,
            typeAdapters);

    return pharmacistReviewService.savePharmacistReview(
        patientId,
        pharmacistReview,
        authorize,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "reviewPharmacistReview", produces = MediaType.APPLICATION_JSON_VALUE)
  public void reviewPharmacistReview(
      @RequestParam("patientId") final String patientId,
      @RequestParam("pharmacistReviewUid") final String pharmacistReviewUid,
      @RequestParam("reviewAction") final String reviewActionString,
      @RequestParam(value = "modifiedTherapy", required = false) final String modifiedTherapy,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam(value = "prescriber", required = false) final String prescriberJson,
      @RequestParam(value = "reviewIdsToDeny", required = false) final String reviewsToDenyJson,
      @RequestParam("language") final String language)
  {
    final TherapyDto modifiedTherapyDto =
        modifiedTherapy != null ?
        JsonUtil.fromJson(
            modifiedTherapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters()) : null;

    final ReviewPharmacistReviewAction reviewAction = ReviewPharmacistReviewAction.valueOf(reviewActionString);

    final NamedExternalDto prescriber =
        prescriberJson != null ? JsonUtil.fromJson(prescriberJson, NamedExternalDto.class) : null;

    final List<String> deniedReviews =
        reviewsToDenyJson != null ?
        Arrays.asList(JsonUtil.fromJson(reviewsToDenyJson, String[].class)) : null;

    pharmacistReviewService.reviewPharmacistReview(
        patientId,
        pharmacistReviewUid,
        reviewAction,
        modifiedTherapyDto,
        deniedReviews,
        centralCaseId,
        careProviderId,
        prescriber,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "authorizePharmacistReviews")
  public void authorizePharmacistReviews(
      @RequestParam("patientId") final String patientId,
      @RequestParam("pharmacistReviewUids") final String pharmacistReviewUidsJson,
      @RequestParam("language") final String language)
  {
    final List<String> pharmacistReviewUids =
        Arrays.asList(JsonUtil.fromJson(pharmacistReviewUidsJson, String[].class));
    pharmacistReviewService.authorizePharmacistReviews(
        patientId,
        pharmacistReviewUids,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "deletePharmacistReview")
  public void deletePharmacistReview(
      @RequestParam("patientId") final String patientId,
      @RequestParam("pharmacistReviewUid") final String pharmacistReviewUid)
  {
    pharmacistReviewService.deletePharmacistReview(patientId, pharmacistReviewUid);
  }

  @GetMapping(value = "getSupplyDataForPharmacistReview")
  public SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyCompositionUid") final String therapyCompositionUid)
  {
    return service.getSupplyDataForPharmacistReview(patientId, therapyCompositionUid);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistReviewTasks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PatientTaskDto> getPharmacistReviewTasks(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);

    return service.getPharmacistReviewTasks(careProviderIdsOpt, patientIdsOpt);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistResupplyTasks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationSupplyTaskDto> getPharmacistResupplyTasks(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds,
      @RequestParam("includeUnverifiedDispenseTasks") final Boolean includeUnverifiedDispenseTasks,
      @RequestParam("closedTasksOnly") final Boolean closedTasksOnly,
      @RequestParam("taskTypes") final String taskTypes,
      @RequestParam("language") final String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypesSet = getTaskTypes(JsonUtil.fromJson(taskTypes, String[].class));

    final boolean closedTasksOnlyValue = closedTasksOnly;     // shows only closed tasks
    final boolean includeUnverifiedDispenseTasksValue = includeUnverifiedDispenseTasks; // shows verified and unverified tasks

    return service.findSupplyTasks(
        careProviderIdsOpt,
        patientIdsOpt,
        taskTypesSet,
        closedTasksOnlyValue,
        includeUnverifiedDispenseTasksValue,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistSupplySimpleTask", produces = MediaType.APPLICATION_JSON_VALUE)
  public MedicationSupplyTaskSimpleDto getPharmacistSupplySimpleTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("language") final String language)
  {
    return service.getSupplySimpleTask(taskId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "confirmPharmacistDispenseTask")
  public void confirmPharmacistDispenseTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam(value = "compositionUid", required = false) final String compositionUid,
      @RequestParam(value = "requesterRole", required = false) final String requesterRoleName,
      @RequestParam(value = "supplyRequestStatus", required = false) final String supplyRequestStatus)
  {
    final TherapyAssigneeEnum requesterRole = TherapyAssigneeEnum.valueOf(requesterRoleName);
    final SupplyRequestStatus supplyRequestStatusEnum = SupplyRequestStatus.valueOf(supplyRequestStatus);
    service.confirmPharmacistDispenseTask(
        patientId,
        taskId,
        compositionUid,
        requesterRole,
        supplyRequestStatusEnum);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "dismissPharmacistSupplyTask")
  public void dismissPharmacistSupplyTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId)
  {
    // used to dismiss pharmacistReminder tasks - tasks don't get deleted, instead a dismissed flag is set
    service.dismissSupplyTask(patientId, Collections.singletonList(taskId));
  }

  @PostMapping(value = "fillPharmacistReviewTherapyOnEdit")
  public PharmacistReviewTherapyDto fillPharmacistReviewTherapyOnEdit(
      @RequestParam("originalTherapy") final String originalTherapy,
      @RequestParam("changedTherapy") final String changedTherapy,
      @RequestParam("language") final String language)
  {
    final TherapyDto originalTherapyDto =
        JsonUtil.fromJson(
            originalTherapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyDto changedTherapyDto =
        JsonUtil.fromJson(
            changedTherapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return service.fillPharmacistReviewTherapyOnEdit(originalTherapyDto, changedTherapyDto, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistDispenseMedicationTasks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationSupplyTaskDto> getPharmacistDispenseMedicationTasks(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds,
      @RequestParam("closedTasksOnly") final Boolean closedTasksOnly,
      @RequestParam("includeUnverifiedDispenseTasks") final Boolean includeUnverifiedDispenseTasks,
      @RequestParam("language") final String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.DISPENSE_MEDICATION);

    final boolean closedTasksOnlyValue = closedTasksOnly;     // shows only closed tasks
    final boolean includeUnverifiedDispenseTasksValue = includeUnverifiedDispenseTasks; // shows verified and unverified tasks

    return service.findSupplyTasks(
        careProviderIdsOpt,
        patientIdsOpt,
        taskTypes,
        closedTasksOnlyValue,
        includeUnverifiedDispenseTasksValue,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistReviews", produces = MediaType.APPLICATION_JSON_VALUE)
  public PharmacistReviewsDto getPharmacistReviews(
      @RequestParam("patientId") final String patientId,
      @RequestParam("fromDate") final String fromDate,
      @RequestParam("language") final String language)
  {
    return pharmacistReviewService.getPharmacistReviews(
        patientId,
        JsonUtil.fromJson(fromDate, DateTime.class),
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPharmacistReviewsForTherapy", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PharmacistReviewDto> getPharmacistReviewsForTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyCompositionUid") final String therapyCompositionUid,
      @RequestParam("language") final String language)
  {
    return service.getPharmacistReviewsForTherapy(patientId, therapyCompositionUid, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getPharmacistTimeline", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyTimelineDto getPharmacistTimeline(
      @RequestParam("patientId") final String patientId,
      @RequestParam("patientData") final String patientData,
      @RequestParam("timelineInterval") final String timelineInterval,
      @RequestParam("roundsInterval") final String roundsInterval,
      @RequestParam("therapySortTypeEnum") final String therapySortTypeString,
      @RequestParam("hidePastTherapies") final Boolean hidePastTherapies,
      @RequestParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString);
    final Interval searchInterval = JsonUtil.fromJson(
        timelineInterval,
        Interval.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    return service.getPharmacistTimeline(
        patientId,
        searchInterval,
        therapySortTypeEnum,
        hidePastTherapies,
        patientDataForMedications,
        roundsIntervalDto,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "printPharmacistDispenseTask")
  public void printPharmacistDispenseTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("requesterRole") final String requesterRoleName,
      @RequestParam("supplyRequestStatus") final String supplyRequestStatus)
  {
    final TherapyAssigneeEnum requesterRole = TherapyAssigneeEnum.valueOf(requesterRoleName);
    final SupplyRequestStatus supplyRequestStatusEnum = SupplyRequestStatus.valueOf(supplyRequestStatus);
    service.printPharmacistDispenseTask(
        patientId,
        taskId,
        compositionUid,
        requesterRole,
        supplyRequestStatusEnum);
  }

  private Opt<Collection<String>> resolveStringArrayParameterToSetOpt(final String parameter)
  {
    return Opt.resolve(() -> Sets.newHashSet(JsonUtil.fromJson(parameter, String[].class)));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "sendNurseResupplyRequest")
  public void sendNurseResupplyRequest(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapy") final String therapyJson,
      @RequestParam("language") final String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    service.handleNurseResupplyRequest(
        patientId,
        therapyDto.getCompositionUid(),
        therapyDto.getEhrOrderName(),
        new Locale(language));
  }

  @GetMapping(value = "getProblemDescriptionNamedIdentities", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> getProblemDescriptionNamedIdentities(
      @RequestParam("language") final String language)
  {
    return service.getProblemDescriptionNamedIdentities(new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "dismissNurseSupplyTask")
  public void dismissNurseSupplyTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam("language") final String language)
  {
    //used to dismiss nurse supply task - dismisses both related (review and dispense) nurse tasks, tasks are deleted
    service.deleteNurseSupplyTask(patientId, taskId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "confirmSupplyReviewTask")
  public void confirmSupplyReviewTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("createSupplyReminder") final Boolean createSupplyReminder,
      @RequestParam(value = "supplyType", required = false) final String supplyType,
      @RequestParam(value = "supplyInDays", required = false) final Integer supplyInDays,
      @RequestParam(value = "comment", required = false) final String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum =
        supplyType != null ? MedicationSupplyTypeEnum.valueOf(supplyType) : null;
    service.confirmSupplyReviewTask(
        patientId,
        taskId,
        compositionUid,
        createSupplyReminder,
        supplyTypeEnum,
        supplyInDays,
        comment);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "confirmSupplyReminderTask")
  public void confirmSupplyReminderTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam(value = "supplyType", required = false) final String supplyType,
      @RequestParam(value = "supplyInDays", required = false) final Integer supplyInDays,
      @RequestParam(value = "comment", required = false) final String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum = MedicationSupplyTypeEnum.valueOf(supplyType);
    service.confirmSupplyReminderTask(
        taskId,
        compositionUid,
        supplyTypeEnum,
        supplyInDays,
        comment);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "editSupplyReminderTask")
  public void editSupplyReminderTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam(value = "supplyType", required = false) final String supplyType,
      @RequestParam(value = "supplyInDays", required = false) final Integer supplyInDays,
      @RequestParam(value = "comment", required = false) final String comment)
  {
    final MedicationSupplyTypeEnum supplyTypeEnum = MedicationSupplyTypeEnum.valueOf(supplyType);
    service.editSupplyReminderTask(
        taskId,
        supplyTypeEnum,
        supplyInDays,
        comment);
  }
}
