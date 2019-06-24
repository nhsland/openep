package com.marand.meds.rest.meds;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.InformationSourceDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyViewPatientDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.marand.meds.config.GsonDeserializators.INTERVAL_DESERIALIZER;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class TherapyController
{
  private final MedicationsService service;

  @Autowired
  public TherapyController(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(path = "abortTherapy")
  @EventProducer(AdministrationChanged.class)
  public void abortTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam(value = "stopReason", required = false) final String stopReason)
  {
    service.abortTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        stopReason);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(path = "getTherapy", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyDto getTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyId") final String therapyId,
      @RequestParam("language") final String language)
  {
    return service.getTherapyDto(patientId, therapyId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(path = "modifyTherapy")
  @EventProducer(AdministrationChanged.class)
  public void modifyTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapy") final String therapy,
      @RequestParam(value = "changeReason", required = false) final String changeReason,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("prescriber") final String prescriberJson,
      @RequestParam(value = "saveDateTime", required = false) final String saveDateTimeJson,
      @RequestParam("language") final String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyChangeReasonDto changeReasonDto = changeReason != null ? JsonUtil.fromJson(
        changeReason,
        TherapyChangeReasonDto.class) : null;

    final DateTime saveDateTime = saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson, DateTime.class) : null;
    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson, NamedExternalDto.class);

    service.modifyTherapy(
        patientId,
        therapyDto,
        changeReasonDto,
        centralCaseId,
        careProviderId,
        prescriber,
        saveDateTime,
        null,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(path = "reviewTherapy")
  public void reviewTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName)
  {
    service.reviewTherapy(
        patientId,
        compositionUid,
        ehrOrderName);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(path = "suspendTherapy")
  @EventProducer(AdministrationChanged.class)
  public void suspendTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam(value = "suspendReason", required = false) final String suspendReason)
  {
    service.suspendTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        suspendReason);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "suspendAllTherapies")
  @EventProducer(AdministrationChanged.class)
  public void suspendAllTherapies(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "suspendReason", required = false) final String suspendReason)
  {
    service.suspendAllTherapies(
        patientId,
        suspendReason);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(path = "reissueTherapy")
  @EventProducer(AdministrationChanged.class)
  public void reissueTherapy(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("ehrOrderName") final String ehrOrderName)
  {
    service.reissueTherapy(
        patientId,
        compositionUid,
        ehrOrderName);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getTherapyViewPatientData")
  public TherapyViewPatientDto getTherapyViewPatientData(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId)
  {
    return service.getTherapyViewPatientData(patientId, centralCaseId);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getTherapyDocuments", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyDocumentsDto getTherapyDocuments(
      @RequestParam("patientId") final String patientId,
      @RequestParam("recordCount") final Integer recordCount,
      @RequestParam("recordOffset") final Integer recordOffset,
      @RequestParam("language") final String language)
  {
    return service.getTherapyDocuments(
            patientId,
            recordCount,
            recordOffset,
            new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "saveOutpatientPrescription", produces = MediaType.TEXT_PLAIN_VALUE)
  public String saveOutpatientPrescription(
      @RequestParam("patientId") final String patientId,
      @RequestParam("prescriptionPackage") final String prescriptionPackageJson)
  {
    final PrescriptionPackageDto prescriptionPackageDto =
        JsonUtil.fromJson(
            prescriptionPackageJson,
            PrescriptionPackageDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return service.saveOutpatientPrescription(patientId, prescriptionPackageDto);
  }

  @GetMapping(value = "getTherapiesFormattedDescriptionsMap", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, String> getTherapiesFormattedDescriptionsMap(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapiesIds") final String therapiesIds,
      @RequestParam("language") final String language)
  {
    final Set<String> therapiesIdsSet = Sets.newHashSet(JsonUtil.fromJson(therapiesIds, String[].class));
    return service.getTherapiesFormattedDescriptionsMap(
        patientId, therapiesIdsSet, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "updateTherapySelfAdministeringStatus")
  public void updateTherapySelfAdministeringStatus(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("selfAdministeringActionType") final String selfAdministeringActionType)
  {
    service.updateTherapySelfAdministeringStatus(
        patientId,
        compositionUid,
        SelfAdministeringActionEnum.valueOf(selfAdministeringActionType));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "calculateTherapyAdministrationTimes", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AdministrationDto> calculateTherapyAdministrationTimes(
      @RequestParam("therapy") final String therapyJson)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());
    return service.calculateTherapyAdministrationTimes(therapyDto);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "calculateNextTherapyAdministrationTime", produces = MediaType.APPLICATION_JSON_VALUE)
  public DateTime calculateNextTherapyAdministrationTime(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapy") final String therapyJson,
      @RequestParam("newPrescription") final Boolean newPrescription)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapyJson,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());
    return service.calculateNextTherapyAdministrationTime(patientId, therapyDto, newPrescription);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "createTherapySurgeryReport", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> createTherapySurgeryReport(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language)
  {
    final Locale localeObj = new Locale(language);

    final byte[] document = service.createTherapySurgeryReport(
        patientId,
        localeObj);

    if (document == null)
    {
      throw new IllegalStateException();
    }

    return ResponseEntity.ok(document);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getActiveTherapies", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TherapyRowDto> getActiveTherapies (
      @RequestParam("patientId") final String patientId,
      @RequestParam("patientData") final String patientData,
      @RequestParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    return service.getActiveTherapies(
        patientId,
        patientDataForMedications,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getTherapyTimeline", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyTimelineDto getTherapyTimeline(
      @RequestParam("patientId") final String patientId,
      @RequestParam("patientData") final String patientData,
      @RequestParam("timelineInterval") final String timelineInterval,
      @RequestParam("roundsInterval") final String roundsInterval,
      @RequestParam("therapySortTypeEnum") final String therapySortTypeString,
      @RequestParam("hidePastTherapies") final Boolean hidePastTherapies,
      @RequestParam("hideFutureTherapies") final Boolean hideFutureTherapies,
      @RequestParam("language") final String language)
  {
    final PatientDataForMedicationsDto patientDataForMedications = JsonUtil.fromJson(
        patientData,
        PatientDataForMedicationsDto.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final Interval searchInterval = JsonUtil.fromJson(
        timelineInterval,
        Interval.class,
        Lists.newArrayList(INTERVAL_DESERIALIZER));

    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);
    final TherapySortTypeEnum sortType = TherapySortTypeEnum.valueOf(therapySortTypeString);

    return service.getTherapyTimeline(
        patientId,
        searchInterval,
        sortType,
        hidePastTherapies,
        hideFutureTherapies,
        patientDataForMedications,
        roundsIntervalDto,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getLinkTherapyCandidates", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TherapyDto> getLinkTherapyCandidates(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "referenceWeight", required = false) final Double referenceWeight,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam("language") final String language)
  {
    return service.getLinkTherapyCandidates(
        patientId,
        referenceWeight,
        patientHeight,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getLastTherapiesForPreviousHospitalization", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TherapyDto> getLastTherapiesForPreviousHospitalization(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam("language") final String language)
  {
    return service.getLastTherapiesForPreviousHospitalization(
            patientId,
            patientHeight,
            new Locale(language));
  }

  @GetMapping(value = "getTherapyChangeTypes", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<ActionReasonType, List<CodedNameDto>> getTherapyChangeTypes()
  {
    return service.getActionReasons(null);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getTherapyFormattedDisplay", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getTherapyFormattedDisplay(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyId") final String therapyId,
      @RequestParam("language") final String language)
  {

    return service.getTherapyFormattedDisplay(
        patientId,
        therapyId,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getTherapyAuditTrail", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyAuditTrailDto getTherapyAuditTrail(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam("compositionId") final String compositionId,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam("language") final String language)
  {
    return service.getTherapyAuditTrail(
        patientId,
        compositionId,
        ehrOrderName,
        patientHeight,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "fillTherapyDisplayValues", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyDto fillTherapyDisplayValues(
      @RequestParam("therapy") final String therapy,
      @RequestParam("language") final String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return service.fillTherapyDisplayValues(therapyDto, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "tagTherapyForPrescription")
  public void tagTherapyForPrescription(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam("compositionId") final String compositionId,
      @RequestParam("ehrOrderName") final String ehrOrderName)
  {
    service.tagTherapyForPrescription(
        patientId,
        compositionId,
        centralCaseId,
        ehrOrderName);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "untagTherapyForPrescription", produces = MediaType.TEXT_PLAIN_VALUE)
  public void untagTherapyForPrescription(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam("compositionId") final String compositionId,
      @RequestParam("ehrOrderName") final String ehrOrderName)
  {
    service.untagTherapyForPrescription(patientId, compositionId, centralCaseId, ehrOrderName);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "therapyflowdata")
  public TherapyFlowDto getTherapyFlowData(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam("startDate") final String startDate,
      @RequestParam("dayCount") final Integer dayCount,
      @RequestParam(value = "todayIndex", required = false) final Integer todayIndex,
      @RequestParam("roundsInterval") final String roundsInterval,
      @RequestParam("therapySortTypeEnum") final String therapySortTypeString,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("language") final String language)
  {
    final DateTime startDateAtMidnight = new DateTime(Long.parseLong(startDate)).withTimeAtStartOfDay();
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);
    final TherapySortTypeEnum therapySortTypeEnum = TherapySortTypeEnum.valueOf(therapySortTypeString);
    return service.getTherapyFlow(
        patientId,
        centralCaseId,
        patientHeight,
        startDateAtMidnight,
        dayCount,
        todayIndex,
        roundsIntervalDto,
        therapySortTypeEnum,
        careProviderId,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "reloadSingleTherapyAfterAction")
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("ehrOrderName") final String ehrOrderName,
      @RequestParam("roundsInterval") final String roundsInterval)
  {
    final RoundsIntervalDto roundsIntervalDto = JsonUtil.fromJson(roundsInterval, RoundsIntervalDto.class);

    return service.reloadSingleTherapyAfterAction(
        patientId,
        careProviderId,
        compositionUid,
        ehrOrderName,
        roundsIntervalDto);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPatientBaselineInfusionIntervals")
  public List<Interval> getPatientBaselineInfusionIntervals(
      @RequestParam("patientId") final String patientId)
  {
    return service.getPatientBaselineInfusionIntervals(patientId);
  }

  @GetMapping(value = "getRemainingInfusionBagQuantity")
  public Double getRemainingInfusionBagQuantity(
      @RequestParam("patientId") final String patientId,
      @RequestParam("when") final String when,
      @RequestParam("therapyId") final String therapyId)
  {
    final DateTime time = JsonUtil.fromJson(when, DateTime.class);
    return service.getRemainingInfusionBagQuantity(time, patientId, therapyId);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "saveMedicationsOrder", produces = MediaType.APPLICATION_JSON_VALUE)
  @EventProducer(AdministrationChanged.class)
  public void saveMedicationsOrder(
      @RequestParam("patientId") final String patientId,
      @RequestParam("medicationOrders") final String medicationOrdersJson,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "hospitalizationStartMillis", required = false) final Long hospitalizationStartMillis,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("prescriber") final String prescriberJson,
      @RequestParam(value = "lastLinkName", required = false) final String lastLinkName,
      @RequestParam(value = "saveDateTime", required = false) final String saveDateTimeJson,
      @RequestParam("language") final String language)
  {
    final SaveMedicationOrderDto[] medicationOrders =
        JsonUtil.fromJson(
            medicationOrdersJson,
            SaveMedicationOrderDto[].class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    final DateTime saveDateTime =
        saveDateTimeJson != null ? JsonUtil.fromJson(saveDateTimeJson, DateTime.class) : null;
    final DateTime hospitalizationStart =
        hospitalizationStartMillis != null ?
        new DateTime(hospitalizationStartMillis) : null;
    final NamedExternalDto prescriber = JsonUtil.fromJson(prescriberJson, NamedExternalDto.class);

    service.saveNewMedicationOrder(
        patientId,
        Arrays.asList(medicationOrders),
        centralCaseId,
        hospitalizationStart,
        careProviderId,
        prescriber,
        lastLinkName,
        saveDateTime,
        new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "saveReferenceWeight", produces = MediaType.APPLICATION_JSON_VALUE)
  public void saveReferenceWeight(
      @RequestBody @RequestParam("patientId") final String patientId,
      @RequestBody @RequestParam("weight") final Double weight)
  {
    service.savePatientReferenceWeight(patientId, weight);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getCurrentHospitalizationMentalHealthTherapies", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MentalHealthTherapyDto> getCurrentHospitalizationMentalHealthDrugs(
      @RequestParam("patientId") final String patientId,
      @RequestParam("hospitalizationStart") final String hospitalizationStartJson,
      @RequestParam("language") final String language)
  {
    final DateTime hospitalizationStart =
        hospitalizationStartJson != null
        ? JsonUtil.fromJson(hospitalizationStartJson, DateTime.class)
        : null;

    return service.getCurrentHospitalizationMentalHealthTherapies(
        patientId,
        hospitalizationStart,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "saveMentalHealthDocument", produces = MediaType.APPLICATION_JSON_VALUE)
  public void saveMentalHealthDocument(
      @RequestParam("mentalHealthDocument") final String mentalHealthDocument,
      @RequestParam("careProvider") final String careProvider)
  {
    final MentalHealthDocumentDto mentalHealthDocumentDto = JsonUtil.fromJson(
        mentalHealthDocument,
        MentalHealthDocumentDto.class);

    final NamedExternalDto careProviderDto = JsonUtil.fromJson(
        careProvider,
        NamedExternalDto.class);

    service.saveMentalHealthReport(mentalHealthDocumentDto, careProviderDto);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getPatientsCumulativeAntipsychoticPercentage", produces = MediaType.APPLICATION_JSON_VALUE)
  public Integer getPatientsCumulativeAntipsychoticPercentage(@RequestParam("patientId") final String patientId)
  {
    return service.getPatientsCumulativeAntipsychoticPercentage(patientId);
  }

  /**
   * Used by MAR uploader
   */
  @GetMapping(value = "therapyReport", produces = MediaType.APPLICATION_JSON_VALUE)
  public TherapyDayReportDto getTherapyReportData(final String patientId, final String language)
  {
    return service.getCurrentTherapyReportData(patientId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "stopAll")
  @EventProducer(AdministrationChanged.class)
  public void stopAll(@QueryParam("patientId") final String patientId,
                      @RequestParam(value = "stopReason", required = false) final String stopReason)
  {
    service.abortAllTherapiesForPatient(patientId, stopReason);
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "suspendAllTherapiesOnTemporaryLeave")
  @EventProducer(AdministrationChanged.class)
  public void suspendAllTherapiesOnTemporaryLeave(@RequestParam("patientId") final String patientId)
  {
    service.suspendAllTherapiesOnTemporaryLeave(patientId);
  }

  @GetMapping(value = "getNumericValue")
  public String getNumericValue(@QueryParam("value") final String value)
  {
    return service.getNumericValue(value);
  }

  @GetMapping(value = "getInformationSources", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<InformationSourceDto> getInformationSources()
  {
    return service.getInformationSources();
  }

  @PostMapping(path = "hasTherapyChanged")
  public boolean hasTherapyChanged(
      @RequestParam("changeGroup") final TherapyChangeType.TherapyChangeGroup changeGroup,
      @RequestParam("therapy") final String therapy,
      @RequestParam("changedTherapy") final String changedTherapy,
      @RequestParam("language") final String language)
  {
    final TherapyDto therapyDto =
        JsonUtil.fromJson(
            therapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    final TherapyDto changedTherapyDto =
        JsonUtil.fromJson(
            changedTherapy,
            TherapyDto.class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return service.hasTherapyChanged(changeGroup, therapyDto, changedTherapyDto, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getOutpatientPrescriptionHandout", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getOutpatientPrescriptionPrintout(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("language") final String language)
  {
    final byte[] document = service.getOutpatientPrescriptionPrintout(patientId, compositionUid, new Locale(language));

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Content-Disposition", "filename=ePrescription.pdf");
    return new ResponseEntity<>(document, httpHeaders, HttpStatus.OK);
  }

  @GetMapping(path = "getActiveAndPastTherapiesReport", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getActiveAndPastTherapiesReport(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language
  )
  {
    final byte[] document = service.getActiveAndPastTherapiesReport(patientId, new Locale(language));

    if (document == null)
    {
      //noinspection ThrowCaughtLocally
      throw new IllegalStateException();
    }

    return ResponseEntity.ok(document);
  }

  @GetMapping(path = "getPastTherapiesReport", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getPastTherapiesReport(
      @RequestParam("patientId") final String patientId,
      @RequestParam("startDate") final String startDate,
      @RequestParam("endDate") final String endDate,
      @RequestParam("language") final String language)
  {
    final byte[] document = service.getPastTherapiesReport(
        patientId,
        JsonUtil.fromJson(startDate, DateTime.class),
        JsonUtil.fromJson(endDate, DateTime.class),
        new Locale(language));

    if (document == null)
    {
      //noinspection ThrowCaughtLocally
      throw new IllegalStateException();
    }

    return ResponseEntity.ok(document);
  }

  @GetMapping(path = "getTemplateReport", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getTemplateReport(
      @RequestParam("patientId") final String patientId,
      @RequestParam("numberOfPages") final int numberOfPages,
      @RequestParam("language") final String language
  )
  {
    final byte[] document = service.getTemplateReport(
        patientId,
        numberOfPages,
        new Locale(language));

    if (document == null)
    {
      //noinspection ThrowCaughtLocally
      throw new IllegalStateException();
    }

    return ResponseEntity.ok(document);
  }

  @Auditable({AuditableType.WITHOUT_RESULT})
  @GetMapping(value = "getTherapyDataForDocumentation", produces = MediaType.APPLICATION_JSON_VALUE)
  public DocumentationTherapiesDto getTherapyDataForDocumentation(
      @RequestParam("patientId") final String patientId,
      @RequestParam("centralCaseEffectiveStart") final String centralCaseEffectiveStart,
      @RequestParam("centralCaseEffectiveEnd") final String centralCaseEffectiveEnd,
      @RequestParam("isOutpatient") final Boolean isOutpatient,
      @RequestParam("language") final String language)
  {
    final DateTime centralCaseStart = JsonUtil.fromJson(centralCaseEffectiveStart, DateTime.class);
    final DateTime centralCaseEnd = JsonUtil.fromJson(centralCaseEffectiveEnd, DateTime.class);

    return service.getTherapyDataForDocumentation(
        patientId,
        new Interval(centralCaseStart, centralCaseEnd),
        isOutpatient,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "newAllergiesAddedForPatient")
  public void newAllergiesAddedForPatient(
      @RequestParam("patientId") final String patientId,
      @RequestParam("newAllergies") final String newAllergies)
  {
    final IdNameDto[] newAllergiesArray = JsonUtil.fromJson(newAllergies, IdNameDto[].class);
    service.newAllergiesAddedForPatient(patientId, Lists.newArrayList(newAllergiesArray));
  }

  @Auditable({AuditableType.WITHOUT_RESULT})
  @GetMapping(value = "getOutpatientPrescriptionPackage", produces = MediaType.APPLICATION_JSON_VALUE)
  public PrescriptionPackageDto getOutpatientPrescriptionPackage(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("language") final String language)
  {
    return service.getOutpatientPrescriptionPackage(patientId, compositionUid, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "updateOutpatientPrescriptionStatus")
  public void updateOutpatientPrescriptionStatus(
      @RequestParam("patientId") final String patientId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("prescriptionTherapyId") final String prescriptionTherapyId,
      @RequestParam("status") final String status)
  {
    service.updateOutpatientPrescriptionStatus(
        patientId,
        compositionUid,
        prescriptionTherapyId,
        JsonUtil.fromJson(status, OutpatientPrescriptionStatus.class));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "updateOutpatientPrescription", produces = MediaType.TEXT_PLAIN_VALUE)
  public String updateOutpatientPrescription(
      @RequestParam("patientId") final String patientId,
      @RequestParam("prescriptionPackageId") final String prescriptionPackageId,
      @RequestParam("compositionUid") final String compositionUid,
      @RequestParam("prescriptionDtoList") final String prescriptionDtoList,
      @RequestParam("when") final String when,
      @RequestParam("language") final String language)
  {
    final PrescriptionDto[] prescriptions = JsonUtil.fromJson(
        prescriptionDtoList,
        PrescriptionDto[].class,
        MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return service.updateOutpatientPrescription(
        patientId,
        prescriptionPackageId,
        compositionUid,
        Lists.newArrayList(prescriptions),
        JsonUtil.fromJson(when, DateTime.class),
        new Locale(language));
  }

  @Auditable({AuditableType.WITHOUT_RESULT})
  @GetMapping(value = "getTherapiesForCurrentCentralCase", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TherapyDto> getTherapiesForCurrentCentralCase(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language)
  {
    return service.getTherapiesForCurrentCentralCase(patientId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "deleteOutpatientPrescription")
  public void deleteOutpatientPrescription(
      @RequestParam("patientId") final String patientId,
      @RequestParam("prescriptionUid") final String prescriptionUid)
  {
    service.deleteOutpatientPrescription(patientId, prescriptionUid);
  }
}
