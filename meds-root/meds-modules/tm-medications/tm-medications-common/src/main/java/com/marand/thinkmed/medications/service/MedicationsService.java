package com.marand.thinkmed.medications.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTemplateContextEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.api.external.dto.DischargeListDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryDto;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.DocumentationTherapiesDto;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.InformationSourceDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyViewPatientDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.process.dto.TaskDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.security.access.annotation.Secured;

/**
 * @author Bostjan Vester
 */
@Secured("ROLE_User")
public interface MedicationsService
{
  List<TreeNodeData> findMedications(@NonNull String searchString, @NonNull EnumSet<MedicationFinderFilterEnum> filters, Locale locale);

  MedicationDataDto getMedicationData(long medicationId);

  Map<Long, MedicationDataDto> getMedicationDataMap(@NonNull Set<Long> medicationIdsList);

  List<MedicationRouteDto> getMedicationRoutes(long medicationId);

  List<MedicationsWarningDto> findCurrentTherapiesWarnings(
      @NonNull String patientId,
      DateTime dateOfBirth,
      Double patientWeightInKg,
      Double bsaInM2,
      @NonNull Gender gender,
      @NonNull List<IdNameDto> diseases,
      @NonNull List<IdNameDto> allergies,
      @NonNull Locale locale);

  List<MedicationsWarningDto> findMedicationWarnings(
      @NonNull String patientId,
      DateTime dateOfBirth,
      Double patientWeightInKg,
      Double bsaInM2,
      @NonNull Gender gender,
      @NonNull List<IdNameDto> diseases,
      @NonNull List<IdNameDto> allergies,
      @NonNull List<TherapyDto> therapies,
      boolean includeActiveTherapies,
      @NonNull Locale locale);

  void newAllergiesAddedForPatient(@NonNull String patientId, @NonNull Collection<IdNameDto> newAllergies);

  AdditionalWarningsDto getAdditionalWarnings(
      @NonNull String patientId,
      @NonNull List<AdditionalWarningsType> additionalWarningsTypes,
      @NonNull PatientDataForMedicationsDto patientDataForMedications,
      @NonNull Locale locale);

  void tagTherapyForPrescription(String patientId, String compositionId, String centralCaseId, String ehrOrderName);

  void untagTherapyForPrescription(String patientId, String compositionId, String centralCaseId, String ehrOrderName);

  void saveNewMedicationOrder(
      @NonNull String patientId,
      @NonNull List<SaveMedicationOrderDto> medicationOrders,
      String centralCaseId,
      DateTime hospitalizationStart,
      String careProviderId,
      NamedExternalDto prescriber,
      String lastLinkName,
      DateTime saveDateTime,
      @NonNull Locale locale);

  List<String> saveMedicationsOnAdmission(
      @NonNull String patientId,
      @NonNull List<MedicationOnAdmissionDto> therapyList,
      String centralCaseId,
      String careProviderId,
      @NonNull Locale locale);

  String startNewReconciliation(@NonNull String patientId, String centralCaseId, String careProviderId);

  void reviewAdmission(@NonNull String patientId);

  void reviewDischarge(@NonNull String patientId);

  List<MedicationOnAdmissionDto> getMedicationsOnAdmission(@NonNull String patientId, final boolean validateTherapy, @NonNull Locale locale);

  List<String> saveMedicationsOnDischarge(
      String patientId,
      List<MedicationOnDischargeDto> therapyList,
      String centralCaseId,
      @Nullable String careProviderId,
      Locale locale);

  List<MedicationOnDischargeDto> getMedicationsOnDischarge(@NonNull String patientId, @NonNull Locale locale);

  boolean isDischargeCreated(@NonNull String patientId);

  DischargeListDto getDischargeList(@NonNull String patientId);

  DischargeSummaryDto getDischargeSummary(@NonNull String patientId);

  ReconciliationSummaryDto getReconciliationSummary(@NonNull String patientId, @NonNull Locale locale);

  List<InformationSourceDto> getInformationSources();

  TherapyFlowDto getTherapyFlow(
      String patientId,
      String centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      @Nullable String careProviderId,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      String patientId,
      String careProviderId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval);

  void modifyTherapy(
      String patientId,
      TherapyDto therapy,
      TherapyChangeReasonDto changeReasonDto,
      String centralCaseId,
      @Nullable String careProviderId,
      NamedExternalDto prescriber,
      DateTime saveDateTime,
      String basedOnPharmacyReviewId,
      Locale locale);

  String abortTherapy(String patientId, String compositionUid, String ehrOrderName, String stopReason);

  void abortAllTherapiesForPatient(@NonNull String patientId, String stopReason);

  void reviewTherapy(String patientId, String compositionUid, String ehrOrderName);

  String suspendTherapy(String patientId, String compositionUid, String ehrOrderName, String suspendReason);

  String reissueTherapy(String patientId, String compositionUid, String ehrOrderName);

  void createAdditionalAdministrationTask(
      String therapyCompositionUid,
      String ehrOrderName,
      String patientId,
      StartAdministrationDto administrationDto);

  void suspendAllTherapies(@NonNull String patientId, String suspendReason);

  void suspendAllTherapiesOnTemporaryLeave(@NonNull String patientId);

  List<String> reissueAllTherapiesOnReturnFromTemporaryLeave(@NonNull String patientId);

  TherapyViewPatientDto getTherapyViewPatientData(@NonNull String patientId, String centralCaseId);

  byte[] getMedicationDocument(String reference);

  byte[] getPastTherapiesReport(
      @NonNull String patientId,
      @NonNull DateTime startDate,
      @NonNull DateTime endDate,
      @NonNull Locale locale);

  byte[] getTemplateReport(
      @NonNull String patientId,
      int numberOfPages,
      @NonNull Locale locale);

  byte[] getActiveAndPastTherapiesReport(@NonNull String patientId, @NonNull Locale locale);

  List<Interval> getPatientBaselineInfusionIntervals(String patientId);

  TherapyDayReportDto getCurrentTherapyReportData(@NonNull String patientId, @NonNull Locale locale);

  byte[] createTherapySurgeryReport(
      String patientId,
      Locale locale);

  byte[] getOutpatientPrescriptionPrintout(@NonNull String patientId, @NonNull String compositionUid, @NonNull Locale locale);

  String getTherapyFormattedDisplay(String patientId, String therapyId, Locale locale);

  TherapyAuditTrailDto getTherapyAuditTrail(
      String patientId,
      String compositionId,
      String ehrOrderName,
      Double patientHeight,
      Locale locale);

  TherapyDto fillTherapyDisplayValues(TherapyDto therapy, Locale locale);

  TherapyDto getTherapyDto(String patientId, String therapyId, Locale locale);

  PharmacistReviewTherapyDto fillPharmacistReviewTherapyOnEdit(
      TherapyDto originalTherapy,
      TherapyDto changedTherapy,
      Locale locale);

  List<TreeNodeData> findSimilarMedications(long medicationId, Locale locale);

  List<MedicationDto> findMedicationProducts(long medicationId, @NonNull List<Long> routeCodes, ReleaseDetailsDto releaseDetails);

  List<MedicationRouteDto> getRoutes();

  List<DoseFormDto> getDoseForms();

  UnitsHolderDto getUnitsHolder();

  void savePatientReferenceWeight(String patientId, double weight);

  List<TherapyDto> getLastTherapiesForPreviousHospitalization(String patientId, Double patientHeight, Locale locale);

  List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(String patientId, Locale locale);

  List<MedicationOnDischargeGroupDto> getTherapiesOnDischargeGroups(
      @NonNull String patientId,
      Double patientHeight,
      @NonNull Locale locale);

  List<MentalHealthTherapyDto> getCurrentHospitalizationMentalHealthTherapies(
      String patientId,
      DateTime lastHospitalizationStart,
      Locale locale);

  List<TherapyDto> getLinkTherapyCandidates(
      @NonNull String patientId,
      @Nullable Double referenceWeight,
      Double patientHeight,
      Locale locale);

  TherapyTemplatesDto getTherapyTemplates(
      @NonNull String patientId,
      @NonNull TherapyTemplateContextEnum templateContext,
      String careProviderId,
      Double referenceWeight,
      Double patientHeight,
      DateTime birthDate,
      @NonNull Locale locale);

  TherapyTemplatesDto getAllTherapyTemplates(
      @NonNull TherapyTemplateModeEnum templateMode,
      String careProviderId,
      @NonNull Locale locale);

  List<String> getTherapyTemplateGroups(@NonNull TherapyTemplateModeEnum templateMode);

  long saveTherapyTemplate(TherapyTemplateDto therapyTemplate, TherapyTemplateModeEnum templateMode);

  void deleteTherapyTemplate(long templateId);

  void addTherapyTemplateGroup(String name, TherapyTemplateModeEnum templateMode);

  void deleteTherapyTemplateGroup(long id);

  TherapyTimelineDto getTherapyTimeline(
      @NonNull String patientId,
      @NonNull Interval interval,
      @NonNull TherapySortTypeEnum sortTypeEnum,
      boolean hidePastTherapies,
      boolean hideFutureTherapies,
      PatientDataForMedicationsDto patientData,
      RoundsIntervalDto roundsInterval,
      Locale locale);

  TherapyTimelineDto getPharmacistTimeline(
      @NonNull String patientId,
      @NonNull Interval interval,
      @NonNull TherapySortTypeEnum sortTypeEnum,
      boolean hidePastTherapies,
      PatientDataForMedicationsDto patientData,
      RoundsIntervalDto roundsInterval,
      Locale locale);

  DocumentationTherapiesDto getTherapyDataForDocumentation(
      String patientId,
      Interval centralCaseEffective,
      boolean isOutpatient,
      Locale locale);

  void confirmTherapyAdministration(
      String compositionUid,
      String patientId,
      AdministrationDto administrationDto,
      boolean editMode,
      String centralCaseId,
      @Nullable String careProviderId,
      boolean requestSupply,
      Locale locale);

  void setDoctorConfirmationResult(@NonNull String patientId, @NonNull AdministrationDto administration, boolean result);

  void rescheduleAdministrationTask(
      @NonNull String patientId,
      @NonNull String taskId,
      @NonNull DateTime newTime,
      @NonNull String therapyId);

  void rescheduleTherapyDoctorReviewTask(final @NonNull String taskId, final @NonNull DateTime newTime, final String comment);

  void rescheduleIvToOralTask(final @NonNull String taskId, final @NonNull DateTime newTime);

  void rescheduleTasks(
      @NonNull String patientId,
      @NonNull String taskId,
      @NonNull DateTime newTime,
      @NonNull String therapyId);

  void setAdministrationTitratedDose(
      @NonNull String patientId,
      @NonNull String latestTherapyId,
      @NonNull StartAdministrationDto administration,
      boolean confirmAdministration,
      String centralCaseId,
      String careProviderId,
      DateTime until,
      @NonNull Locale locale);

  void cancelAdministrationTask(
      @NonNull String patientId,
      @NonNull AdministrationDto administration,
      @NonNull String comment);

  void uncancelAdministrationTask(@NonNull String patientId, @NonNull AdministrationDto administration);

  void deleteTask(String patientId, String taskId, String groupUUId, String therapyId, String comment);

  void deleteAdministration(
      String patientId,
      AdministrationDto administration,
      TherapyDoseTypeEnum therapyDoseType,
      String therapyId,
      String comment);

  List<NamedExternalDto> getCareProfessionals();

  List<PatientTaskDto> getPharmacistReviewTasks(Opt<Collection<String>> careProviderIds, Opt<Collection<String>> patientIds);

  List<AdministrationPatientTaskDto> getAdministrationTasks(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      Set<AdministrationTypeEnum> types,
      @NonNull Locale locale);

  RuleResult applyMedicationRule(
      @NonNull RuleParameters ruleParameters,
      @NonNull Locale locale);

  List<MedicationSupplyTaskDto> findSupplyTasks(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      Set<TaskTypeEnum> taskTypes,
      boolean closedTasksOnly,
      boolean includeUnverifiedDispenseTasks,
      @NonNull Locale locale);

  void handleNurseResupplyRequest(
      String patientId,
      String therapyCompositionUid,
      String ehrOrderName,
      Locale locale);

  Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> getProblemDescriptionNamedIdentities(Locale locale);

  List<PharmacistReviewDto> getPharmacistReviewsForTherapy(String patientId, String compositionUid, Locale locale);

  List<NamedExternalDto> getCurrentUserCareProviders();

  List<AdministrationDto> calculateTherapyAdministrationTimes(@NonNull TherapyDto therapy);

  DateTime calculateNextTherapyAdministrationTime(
      @NonNull String patientId,
      @NonNull TherapyDto therapy,
      boolean newPrescription);

  DateTime findPreviousTaskForTherapy(String patientId, String compositionUid, String ehrOrderName);

  void dismissSupplyTask(String patientId, List<String> taskIds);

  void deleteNurseSupplyTask(String patientId, String taskId, Locale locale);

  void confirmSupplyReminderTask(
      String taskId,
      String compositionUid,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void editSupplyReminderTask(
      String taskId,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void confirmSupplyReviewTask(
      String patientId,
      String taskId,
      String compositionUid,
      boolean createSupplyReminder,
      MedicationSupplyTypeEnum supplyTypeEnum,
      Integer supplyInDays,
      String comment);

  void confirmPharmacistDispenseTask(
      String patientId,
      String taskId,
      String compositionUid,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatus);

  MedicationSupplyTaskSimpleDto getSupplySimpleTask(String taskId, Locale locale);

  Map<ActionReasonType, List<CodedNameDto>> getActionReasons(ActionReasonType type);

  List<MentalHealthTemplateDto> getMentalHealthTemplates();

  void saveMentalHealthReport(MentalHealthDocumentDto mentalHealthDocumentDto, NamedExternalDto careProvider);

  SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      @NonNull String patientId,
      @NonNull String therapyCompositionUid);

  void orderPerfusionSyringePreparation(
      @NonNull String patientId,
      @NonNull String compositionUid,
      @NonNull String ehrOrderName,
      int numberOfSyringes,
      boolean urgent,
      @NonNull DateTime dueTime,
      boolean printSystemLabel);

  List<PerfusionSyringePatientTasksDto> findPerfusionSyringePreparationRequests(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      @NonNull Set<TaskTypeEnum> taskTypes,
      @NonNull Locale locale);

  List<PerfusionSyringePatientTasksDto> findFinishedPerfusionSyringePreparationRequests(
      Opt<Collection<String>> careProviderIds,
      Opt<Collection<String>> patientIds,
      @NonNull Interval searchInterval,
      @NonNull Locale locale);

  boolean finishedPerfusionSyringeRequestsExistInLastHours(
      @NonNull String patientId,
      @NonNull String originalTherapyId,
      int hours);

  Map<String, PerfusionSyringePreparationDto> startPerfusionSyringePreparations(
      String patientId,
      List<String> taskIds,
      Set<String> originalTherapyIds,
      boolean isUrgent,
      Locale locale);

  Map<String, String> confirmPerfusionSyringePreparations(String patientId, List<String> taskIds, boolean isUrgent);

  Map<String, String> dispensePerfusionSyringePreparations(String patientId, List<String> taskIds, boolean isUrgent);

  void deletePerfusionSyringeRequest(String taskId, Locale locale);

  Map<String, String> undoPerfusionSyringeRequestState(String patientId, String taskId, boolean isUrgent);

  void updateTherapySelfAdministeringStatus(
      String patientId,
      String compositionUid,
      SelfAdministeringActionEnum selfAdministeringActionEnum);

  void printPharmacistDispenseTask(
      String patientId,
      String taskId,
      String compositionUid,
      TherapyAssigneeEnum requesterRole,
      SupplyRequestStatus supplyRequestStatusEnum);

  Map<String, String> getTherapiesFormattedDescriptionsMap(String patientId, Set<String> therapyIds, Locale locale);

  Integer getPatientsCumulativeAntipsychoticPercentage(String patientId);

  String saveOutpatientPrescription(String patientId, PrescriptionPackageDto prescriptionPackage);

  void deleteOutpatientPrescription(@NonNull String patientId, @NonNull String prescriptionUid);

  void updateOutpatientPrescriptionStatus(
      String patientId,
      String compositionUid,
      String prescriptionTherapyId,
      OutpatientPrescriptionStatus status);

  PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(String taskId, Locale locale);

  void editPerfusionSyringeTask(
      @NonNull String taskId,
      @NonNull Integer numberOfSyringes,
      boolean isUrgent,
      @NonNull DateTime dueDate,
      boolean printSystemLabel);

  TherapyDocumentsDto getTherapyDocuments(String patientId, Integer numberOfResults, Integer resultsOffset, Locale locale);

  String getMedicationExternalId(@NonNull String externalSystem, long medicationId);

  Double getRemainingInfusionBagQuantity(@NonNull DateTime when, @NonNull String patientId, @NonNull String therapyId);

  String updateOutpatientPrescription(
      String patientId,
      String prescriptionPackageId,
      String compositionUid,
      List<PrescriptionDto> prescriptionDtoList,
      DateTime when,
      Locale locale);

  PrescriptionPackageDto getOutpatientPrescriptionPackage(
      String patientId,
      String compositionUid,
      Locale locale);

  TitrationDto getDataForTitration(
      @NonNull String patientId,
      @NonNull String therapyId,
      @NonNull TitrationType titrationType,
      @NonNull DateTime searchStart,
      @NonNull DateTime searchEnd,
      @NonNull Locale locale);

  List<TherapyRowDto> getActiveTherapies(
      final @NonNull String patientId,
      final @NonNull PatientDataForMedicationsDto patientData,
      final Locale locale);

  List<TherapyDto> getTherapiesForCurrentCentralCase(@NonNull String patientId, @NonNull Locale locale);

  String getUnlicensedMedicationWarning(@NonNull Locale locale);

  void setAdministrationDoctorsComment(@NonNull String taskId, String doctorsComment);

  void handleAdditionalWarningsAction(@NonNull AdditionalWarningsActionDto additionalWarningsActionDto);

  List<TaskDto> findAdministrationTasks(Set<String> patientIds, DateTime taskDueAfter, DateTime taskDueBefore);

  Long getMedicationIdForBarcode(@NonNull String barcode);

  BarcodeTaskSearchDto getAdministrationTaskForBarcode(@NonNull String patientId, @NonNull String barcode);

  String getOriginalTherapyId(@NonNull String patientId, @NonNull String therapyId);

  List<FormularyMedicationDto> getVmpMedications(@NonNull String vtmId);

  List<DispenseSourceDto> getDispenseSources();

  String getNumericValue(@NonNull String value);

  boolean hasTherapyChanged(
      @NonNull TherapyChangeType.TherapyChangeGroup group,
      @NonNull TherapyDto therapy,
      @NonNull TherapyDto changedTherapy,
      @NonNull Locale locale);
}
