package com.marand.meds.rest.meds;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn
 */
@RestController
@RequestMapping("/medications")
public class AdministrationController
{
  private final MedicationsService medicationsService;

  @Autowired
  public AdministrationController(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "setAdministrationTitratedDose", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void setAdministrationTitratedDose(
      @RequestParam("patientId") final String patientId,
      @RequestParam("latestTherapyId") final String latestTherapyId,
      @RequestParam("administration") final String administrationJson,
      @RequestParam("confirmAdministration") final boolean confirmAdministration,
      @RequestParam(value = "until", required = false) final String until, //optional
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId, //optional
      @RequestParam(value = "careProviderId", required = false) final String careProviderId, //optional
      @RequestParam("language") final String language)
  {
    medicationsService.setAdministrationTitratedDose(
        patientId,
        latestTherapyId,
        JsonUtil.fromJson(administrationJson, StartAdministrationDto.class),
        confirmAdministration,
        centralCaseId,
        careProviderId,
        JsonUtil.fromJson(until, DateTime.class),
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "deleteAdministration", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void deleteAdministration(
      @RequestParam("patientId") final String patientId,
      @RequestParam("administration") final String administration,
      @RequestParam("therapyDoseType") final String therapyDoseType,
      @RequestParam("therapyId") final String therapyId,
      @RequestParam("comment") final String comment)
  {
    final AdministrationDto administrationDto =
        JsonUtil.fromJson(
            administration,
            AdministrationDto.class,
            Lists.newArrayList(MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    medicationsService.deleteAdministration(
        patientId,
        administrationDto,
        TherapyDoseTypeEnum.valueOf(therapyDoseType),
        therapyId,
        comment);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "cancelAdministrationTask", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void cancelAdministrationTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("administration") final String administration,
      @RequestParam("comment") final String comment)
  {
    final AdministrationDto administrationDto =
        JsonUtil.fromJson(
            administration,
            AdministrationDto.class,
            Lists.newArrayList(MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    medicationsService.cancelAdministrationTask(patientId, administrationDto, comment);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "uncancelAdministrationTask", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void uncancelAdministrationTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("administration") final String administration)
  {
    final AdministrationDto administrationDto =
        JsonUtil.fromJson(
            administration,
            AdministrationDto.class,
            Lists.newArrayList(MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    medicationsService.uncancelAdministrationTask(patientId, administrationDto);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getDataForTitration", produces = MediaType.APPLICATION_JSON_VALUE)
  public TitrationDto getDataForTitration(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyId") final String therapyId,
      @RequestParam("titrationType") final String titrationTypeJson,
      @RequestParam("searchStart") final String searchStartJson,
      @RequestParam("searchEnd") final String searchEndJson,
      @RequestParam("language") final String language)
  {
    return medicationsService.getDataForTitration(
        patientId,
        therapyId,
        TitrationType.valueOf(titrationTypeJson),
        JsonUtil.fromJson(searchStartJson, DateTime.class),
        JsonUtil.fromJson(searchEndJson, DateTime.class),
        new Locale(language));
  }

  @PostMapping(value = "setAdministrationDoctorsComment")
  public void setAdministrationDoctorsComment(
      @RequestParam("taskId") final String taskId,
      @RequestParam("doctorsComment") final String doctorsComment)
  {
    medicationsService.setAdministrationDoctorsComment(taskId, doctorsComment);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "createAdministrationTask", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void createAdministrationTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyCompositionUid") final String therapyCompositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam("administration") final String administration,
      @RequestParam(value = "requestSupply", required = false) final Boolean requestSupply,
      @RequestParam("language") final String language)
  {
    final StartAdministrationDto therapyAdministrationDto =
        JsonUtil.fromJson(
            administration,
            StartAdministrationDto.class);

    medicationsService.createAdditionalAdministrationTask(
        therapyCompositionUid,
        ehrOrderName,
        patientId,
        therapyAdministrationDto);
    if (requestSupply != null && requestSupply)
    {
      medicationsService.handleNurseResupplyRequest(
          patientId,
          therapyCompositionUid,
          ehrOrderName,
          new Locale(language));
    }
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getAdministrationTasks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AdministrationPatientTaskDto> getAdministrationTasks(
      @RequestParam(value = "careProviderIds", required = false) final String careProviderIds,
      @RequestParam(value = "patientIds", required = false) final String patientIds,
      @RequestParam("language") final String language)
  {
    final Opt<Collection<String>> careProviderIdsOpt = resolveStringArrayParameterToSetOpt(careProviderIds);
    final Opt<Collection<String>> patientIdsOpt = resolveStringArrayParameterToSetOpt(patientIds);

    return medicationsService.getAdministrationTasks(
        careProviderIdsOpt,
        patientIdsOpt,
        AdministrationTypeEnum.START_OR_ADJUST,
        new Locale(language));
  }

  private Opt<Collection<String>> resolveStringArrayParameterToSetOpt(final String parameter)
  {
    return Opt.resolve(() -> Sets.newHashSet(JsonUtil.fromJson(parameter, String[].class)));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "findAdministrationTasks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TaskDto> findAdministrationTasks(
      @RequestParam(value = "patientIds") final String patientIds,
      @RequestParam(value = "taskDueAfter") final String taskDueAfter,
      @RequestParam(value = "taskDueBefore") final String taskDueBefore)
  {
    return medicationsService.findAdministrationTasks(
        Sets.newHashSet(JsonUtil.fromJson(patientIds, String[].class)),
        JsonUtil.fromJson(taskDueAfter, DateTime.class),
        JsonUtil.fromJson(taskDueBefore, DateTime.class));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "findPreviousTaskForTherapy", produces = MediaType.APPLICATION_JSON_VALUE)
  public DateTime findPreviousTaskForTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName)
  {
    return medicationsService.findPreviousTaskForTherapy(patientId, compositionUid, ehrOrderName);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "confirmTherapyAdministration")
  @EventProducer(AdministrationChanged.class)
  public void confirmTherapyAdministration(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyCompositionUid") final String therapyCompositionUid,
      @RequestParam(value = "editMode", required = false) final Boolean editMode,
      @RequestParam("administration") final String administration,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("language") final String language,
      @RequestParam(value = "requestSupply", required = false) final Boolean requestSupply)
  {
    final AdministrationDto therapyAdministrationDto =
        JsonUtil.fromJson(
            administration,
            AdministrationDto.class,
            Lists.newArrayList(MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    medicationsService.confirmTherapyAdministration(
        therapyCompositionUid,
        patientId,
        therapyAdministrationDto,
        editMode == null ? false : editMode,
        centralCaseId,
        careProviderId,
        requestSupply != null && requestSupply,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "setDoctorConfirmationResult", produces = MediaType.APPLICATION_JSON_VALUE)
  public void setDoctorConfirmationResult(
      @RequestParam("patientId") final String patientId,
      @RequestParam("administration") final String administrationJson,
      @RequestParam("result") final Boolean result)
  {
    final AdministrationDto administration =
        JsonUtil.fromJson(
            administrationJson,
            AdministrationDto.class,
            Lists.newArrayList(MedsJsonDeserializer.INSTANCE.getTypeAdapters()));

    medicationsService.setDoctorConfirmationResult(patientId, administration, result);
  }
}
