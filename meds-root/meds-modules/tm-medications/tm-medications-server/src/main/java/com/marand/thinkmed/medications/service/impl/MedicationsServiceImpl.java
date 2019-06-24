package com.marand.thinkmed.medications.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.StackTraceUtils;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.maf.core.exception.SystemException;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.api.externals.data.object.ExternalIdentityDto;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.ActionReasonType;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.TherapyAuthorityEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyTemplateContextEnum;
import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.administration.impl.AdministrationDoctorConfirmationHandler;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.api.DischargeMapper;
import com.marand.thinkmed.medications.api.external.dto.DischargeListDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryDto;
import com.marand.thinkmed.medications.api.internal.dto.CodedNameDto;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
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
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.audittrail.TherapyAuditTrailProvider;
import com.marand.thinkmed.medications.barcode.BarcodeScannerUtils;
import com.marand.thinkmed.medications.barcode.BarcodeTaskFinder;
import com.marand.thinkmed.medications.batch.TherapyBatchActionHandler;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.MedicationsFinder;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.MedicationsCentralCaseDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.TherapyTemplateDao;
import com.marand.thinkmed.medications.dao.openehr.CompositionNotFoundException;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.discharge.MedicationOnDischargeHandler;
import com.marand.thinkmed.medications.document.TherapyDocumentProvider;
import com.marand.thinkmed.medications.dto.AdministrationPatientTaskLimitsDto;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.InformationSourceDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.TherapyViewPatientDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.audittrail.TherapyAuditTrailDto;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.document.TherapyDocumentsDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTemplateDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.perfusionSyringe.PerfusionSyringePreparationDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.SupplyDataForPharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.MedicationSupplyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringePatientTasksDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.PerfusionSyringeTaskSimpleDto;
import com.marand.thinkmed.medications.dto.pharmacist.task.SupplyReviewTaskSimpleDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportPdfDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyTemplateReportDto;
import com.marand.thinkmed.medications.dto.task.PatientTaskDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplateDto;
import com.marand.thinkmed.medications.dto.template.TherapyTemplatesDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.ReferenceWeight;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.infusion.InfusionBagHandler;
import com.marand.thinkmed.medications.infusion.impl.InfusionBagTaskProviderImpl;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormToEhrSaver;
import com.marand.thinkmed.medications.outpatient.OutpatientPrescriptionHandler;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.preferences.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.reconciliation.MedicationReconciliationUpdater;
import com.marand.thinkmed.medications.reconciliation.ReconciliationReviewHandler;
import com.marand.thinkmed.medications.reconciliation.ReconciliationSummaryHandler;
import com.marand.thinkmed.medications.report.TherapyReportCreator;
import com.marand.thinkmed.medications.report.TherapyReportDataProvider;
import com.marand.thinkmed.medications.rule.MedicationRuleHandler;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
import com.marand.thinkmed.medications.task.TasksRescheduler;
import com.marand.thinkmed.medications.therapy.updater.TherapyUpdater;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.titration.TitrationDataProvider;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.medications.warnings.TherapyWarningsProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsActionHandler;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import com.marand.thinkmed.medications.warnings.internal.AntipsychoticMaxDoseWarningsHandler;
import com.marand.thinkmed.patient.PatientDataProvider;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.repository.service.RepositoryClient;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Bostjan Vester
 */

@Component
public class MedicationsServiceImpl implements MedicationsService, MedicationsServiceEvents
{
  private MedicationsBo medicationsBo;
  private TherapyUpdater therapyUpdater;
  private MedicationsDao medicationsDao;
  private TherapyTemplateDao therapyTemplateDao;
  private UnitsProvider unitsProvider;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsTasksProvider medicationsTasksProvider;
  private MedicationsTasksHandler medicationsTasksHandler;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private PharmacySupplyProcessHandler pharmacySupplyProcessHandler;
  private AdministrationTaskCreator administrationTaskCreator;
  private MedicationsFinder medicationsFinder;
  private PharmacistReviewProvider pharmacistReviewProvider;

  private TherapyDisplayProvider therapyDisplayProvider;
  private TherapyWarningsProvider therapyWarningsProvider;
  private AdministrationHandler administrationHandler;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private ReconciliationReviewHandler reconciliationReviewHandler;
  private MedicationOnDischargeHandler medicationOnDischargeHandler;
  private ReconciliationSummaryHandler reconciliationSummaryHandler;
  private MedicationReconciliationUpdater reconciliationUpdater;
  private PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler;
  private OverviewContentProvider overviewContentProvider;
  private ConsentFormToEhrSaver consentFormToEhrSaver;
  private OutpatientPrescriptionHandler outpatientPrescriptionHandler;
  private TherapyDocumentProvider therapyDocumentProvider;
  private MedicationRuleHandler medicationIngredientRuleHandler;
  private TherapyReportDataProvider therapyReportDataProvider;
  private TherapyReportCreator therapyReportCreator;
  private InfusionBagHandler infusionBagHandler;
  private InfusionBagTaskProviderImpl infusionBagTaskProvider;
  private TitrationDataProvider titrationDataProvider;
  private TherapyBatchActionHandler therapyBatchActionHandler;
  private TherapyChangeCalculator therapyChangeCalculator;
  private TherapyAuditTrailProvider therapyAuditTrailProvider;
  private PatientDataProvider patientDataProvider;
  private AdministrationProvider administrationProvider;
  private MedicationsConnector medicationsConnector;
  private AllergiesHandler allergiesHandler;
  private AdditionalWarningsDelegator additionalWarningsDelegator;
  private AdministrationDoctorConfirmationHandler administrationDoctorConfirmationHandler;
  private AntipsychoticMaxDoseWarningsHandler antipsychoticsWarningsHandler;

  private TasksRescheduler tasksRescheduler;
  private RequestDateTimeHolder requestDateTimeHolder;
  private AdditionalWarningsActionHandler additionalWarningsActionHandler;
  private BarcodeTaskFinder barcodeTaskFinder;
  private DischargeMapper dischargeMapper;


  private RepositoryClient repositoryClient;

  private MedsProperties medsProperties;

  private static final Logger LOG = LoggerFactory.getLogger(MedicationsServiceImpl.class);

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setAdditionalWarningsActionHandler(final AdditionalWarningsActionHandler additionalWarningsActionHandler)
  {
    this.additionalWarningsActionHandler = additionalWarningsActionHandler;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setTherapyUpdater(final TherapyUpdater therapyUpdater)
  {
    this.therapyUpdater = therapyUpdater;
  }

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Autowired
  public void setTherapyTemplateDao(final TherapyTemplateDao therapyTemplateDao)
  {
    this.therapyTemplateDao = therapyTemplateDao;
  }

  @Autowired
  public void setUnitsProvider(final UnitsProvider unitsProvider)
  {
    this.unitsProvider = unitsProvider;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setConsentFormToEhrSaver(final ConsentFormToEhrSaver consentFormToEhrSaver)
  {
    this.consentFormToEhrSaver = consentFormToEhrSaver;
  }

  @Autowired(required = false)
  public void setRepositoryService(final RepositoryClient repositoryClient)
  {
    this.repositoryClient = repositoryClient;
  }

  @Autowired
  public void setInfusionBagTaskProvider(final InfusionBagTaskProviderImpl infusionBagTaskProvider)
  {
    this.infusionBagTaskProvider = infusionBagTaskProvider;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setPatientDataProvider(final PatientDataProvider patientDataProvider)
  {
    this.patientDataProvider = patientDataProvider;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Autowired
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Autowired
  public void setPharmacySupplyProcessHandler(final PharmacySupplyProcessHandler pharmacySupplyProcessHandler)
  {
    this.pharmacySupplyProcessHandler = pharmacySupplyProcessHandler;
  }

  @Autowired
  public void setAdministrationDoctorConfirmationHandler(final AdministrationDoctorConfirmationHandler administrationDoctorConfirmationHandler)
  {
    this.administrationDoctorConfirmationHandler = administrationDoctorConfirmationHandler;
  }

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setInfusionBagHandler(final InfusionBagHandler infusionBagHandler)
  {
    this.infusionBagHandler = infusionBagHandler;
  }

  @Autowired
  public void setPharmacistReviewProvider(final PharmacistReviewProvider pharmacistReviewProvider)
  {
    this.pharmacistReviewProvider = pharmacistReviewProvider;
  }

  @Autowired
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Autowired
  public void setMedicationsFinder(final MedicationsFinder medicationsFinder)
  {
    this.medicationsFinder = medicationsFinder;
  }

  @Autowired
  public void setTherapyWarningsProvider(final TherapyWarningsProvider therapyWarningsProvider)
  {
    this.therapyWarningsProvider = therapyWarningsProvider;
  }

  @Autowired
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Autowired
  public void setReconciliationUpdater(final MedicationReconciliationUpdater reconciliationUpdater)
  {
    this.reconciliationUpdater = reconciliationUpdater;
  }

  @Autowired
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Autowired
  public void setReconciliationReviewHandler(final ReconciliationReviewHandler reconciliationReviewHandler)
  {
    this.reconciliationReviewHandler = reconciliationReviewHandler;
  }

  @Autowired
  public void setMedicationOnDischargeHandler(final MedicationOnDischargeHandler medicationOnDischargeHandler)
  {
    this.medicationOnDischargeHandler = medicationOnDischargeHandler;
  }

  @Autowired
  public void setMedicationIngredientRuleHandler(final MedicationRuleHandler medicationIngredientRuleHandler)
  {
    this.medicationIngredientRuleHandler = medicationIngredientRuleHandler;
  }

  @Autowired
  public void setReconciliationSummaryHandler(final ReconciliationSummaryHandler reconciliationSummaryHandler)
  {
    this.reconciliationSummaryHandler = reconciliationSummaryHandler;
  }

  @Autowired
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Autowired
  public void setPreparePerfusionSyringeProcessHandler(final PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler)
  {
    this.preparePerfusionSyringeProcessHandler = preparePerfusionSyringeProcessHandler;
  }

  @Autowired
  public void setOutpatientPrescriptionHandler(final OutpatientPrescriptionHandler outpatientPrescriptionHandler)
  {
    this.outpatientPrescriptionHandler = outpatientPrescriptionHandler;
  }

  @Autowired
  public void setTherapyDocumentProvider(final TherapyDocumentProvider therapyDocumentProvider)
  {
    this.therapyDocumentProvider = therapyDocumentProvider;
  }

  @Autowired
  public void setTherapyReportDataProvider(final TherapyReportDataProvider therapyReportDataProvider)
  {
    this.therapyReportDataProvider = therapyReportDataProvider;
  }

  @Autowired
  public void setTherapyReportCreator(final TherapyReportCreator therapyReportCreator)
  {
    this.therapyReportCreator = therapyReportCreator;
  }

  @Autowired
  public void setTitrationDataProvider(final TitrationDataProvider titrationDataProvider)
  {
    this.titrationDataProvider = titrationDataProvider;
  }

  @Autowired
  public void setTherapyBatchActionHandler(final TherapyBatchActionHandler therapyBatchActionHandler)
  {
    this.therapyBatchActionHandler = therapyBatchActionHandler;
  }

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Autowired
  public void setTherapyAuditTrailProvider(final TherapyAuditTrailProvider therapyAuditTrailProvider)
  {
    this.therapyAuditTrailProvider = therapyAuditTrailProvider;
  }

  @Autowired
  public void setAllergiesHandler(final AllergiesHandler allergiesHandler)
  {
    this.allergiesHandler = allergiesHandler;
  }

  @Autowired
  public void setAdditionalWarningsDelegator(final AdditionalWarningsDelegator additionalWarningsDelegator)
  {
    this.additionalWarningsDelegator = additionalWarningsDelegator;
  }

  @Autowired
  public void setTasksRescheduler(final TasksRescheduler tasksRescheduler)
  {
    this.tasksRescheduler = tasksRescheduler;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Autowired
  public void setBarcodeTaskFinder(final BarcodeTaskFinder barcodeTaskFinder)
  {
    this.barcodeTaskFinder = barcodeTaskFinder;
  }

  @Autowired
  public void setAntipsychoticsWarningsHandler(final AntipsychoticMaxDoseWarningsHandler antipsychoticsWarningsHandler)
  {
    this.antipsychoticsWarningsHandler = antipsychoticsWarningsHandler;
  }

  @Autowired
  public void setDischargeMapper(final DischargeMapper dischargeMapper)
  {
    this.dischargeMapper = dischargeMapper;
  }

  @Override
  @Transactional()
  @EhrSessioned
  public List<TreeNodeData> findMedications(
      final @NonNull String searchString,
      final @NonNull EnumSet<MedicationFinderFilterEnum> filters,
      final Locale locale)
  {



    return medicationsFinder.findMedications(
        searchString,
        medsProperties.getSearchStartMustMatch(),
        filters,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public MedicationDataDto getMedicationData(final long medicationId)
  {
    return medicationsValueHolderProvider.getMedicationData(medicationId);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, MedicationDataDto> getMedicationDataMap(final @NonNull Set<Long> medicationIdsList)
  {
    return medicationsValueHolderProvider.getAllMedicationDataMap(medicationIdsList);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MedicationRouteDto> getMedicationRoutes(final long medicationId)
  {
    return medicationsValueHolderProvider.getMedicationRoutes(medicationId);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationsWarningDto> findCurrentTherapiesWarnings(
      final @NonNull String patientId,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final @NonNull Locale locale)
  {
    return therapyWarningsProvider.findCurrentTherapiesWarnings(
        patientId,
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseases,
        allergies,
        true,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationsWarningDto> findMedicationWarnings(
      final @NonNull String patientId,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final Double bsaInM2,
      final @NonNull Gender gender,
      final @NonNull List<IdNameDto> diseases,
      final @NonNull List<IdNameDto> allergies,
      final @NonNull List<TherapyDto> therapies,
      final boolean includeActiveTherapies,
      final @NonNull Locale locale)
  {
    return therapyWarningsProvider.findMedicationWarnings(
        patientId,
        dateOfBirth,
        patientWeightInKg,
        bsaInM2,
        gender,
        diseases,
        allergies,
        therapies,
        true,
        includeActiveTherapies,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void newAllergiesAddedForPatient(
      final @NonNull String patientId,
      final @NonNull Collection<IdNameDto> newAllergies)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    allergiesHandler.handleNewAllergies(patientId, newAllergies, when);
  }

  @Override
  public AdditionalWarningsDto getAdditionalWarnings(
      final @NonNull String patientId,
      final @NonNull List<AdditionalWarningsType> additionalWarningsTypes,
      final @NonNull PatientDataForMedicationsDto patientDataForMedications,
      final @NonNull Locale locale)
  {
    return additionalWarningsDelegator.getAdditionalWarnings(
        additionalWarningsTypes,
        patientId,
        patientDataForMedications,
        requestDateTimeHolder.getRequestTimestamp(),
        locale).orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public void tagTherapyForPrescription(
      final String patientId,
      final String compositionId,
      final String centralCaseId,
      final String ehrOrderName)
  {
    if (centralCaseId != null)
    {
      final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionId);
      prescription.getMedicationOrder().getAdditionalDetails().setAddToDischargeLetter(DataValueUtils.getText(centralCaseId));
      medicationsOpenEhrDao.modifyComposition(patientId, prescription);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public void untagTherapyForPrescription(
      final String patientId,
      final String compositionId,
      final String centralCaseId,
      final String ehrOrderName)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionId);
    prescription.getMedicationOrder().getAdditionalDetails().setAddToDischargeLetter(null);
    medicationsOpenEhrDao.modifyComposition(patientId, prescription);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(PrescribeTherapy.class)
  public void saveNewMedicationOrder(
      final @NonNull String patientId,
      final @NonNull List<SaveMedicationOrderDto> medicationOrders,
      final String centralCaseId,
      final DateTime hospitalizationStart,
      final String careProviderId,
      final NamedExternalDto prescriber,
      final String lastLinkName,
      final DateTime saveDateTime,
      final @NonNull Locale locale)
  {
    final DateTime when = saveDateTime != null ? saveDateTime : requestDateTimeHolder.getRequestTimestamp();

    therapyUpdater.saveTherapies(
        patientId,
        medicationOrders,
        centralCaseId,
        careProviderId,
        prescriber,
        when,
        locale);

    if (lastLinkName != null)
    {
      final Long patientIdLong = parseToLong(patientId);
      if (patientIdLong != null)
      {
        medicationsDao.savePatientLastLinkName(patientIdLong, lastLinkName);
      }
    }

    pharmacistTaskHandler.handleReviewTaskOnTherapiesChange(
        patientId,
        hospitalizationStart,
        when,
        prescriber != null ? prescriber.getName() : null,
        when,
        null,
        PharmacistReviewTaskStatusEnum.PENDING);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(SaveMedicationsOnAdmission.class)
  public List<String> saveMedicationsOnAdmission(
      final @NonNull String patientId,
      final @NonNull List<MedicationOnAdmissionDto> medicationsOnAdmission,
      final String centralCaseId,
      final String careProviderId,
      final @NonNull Locale locale)
  {
    return medicationOnAdmissionHandler.saveMedicationsOnAdmission(
        patientId,
        medicationsOnAdmission,
        centralCaseId,
        careProviderId,
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  public String startNewReconciliation(
      final @NonNull String patientId,
      final String centralCaseId,
      final String careProviderId)
  {
    final String uid = reconciliationUpdater.startNew(patientId, centralCaseId, careProviderId);
    reconciliationReviewHandler.deleteReviewTask(patientId, TaskTypeEnum.ADMISSION_REVIEW_TASK);
    reconciliationReviewHandler.deleteReviewTask(patientId, TaskTypeEnum.DISCHARGE_REVIEW_TASK);
    return uid;
  }

  @Override
  @Transactional
  @EhrSessioned
  public void reviewAdmission(final @NonNull String patientId)
  {
    reconciliationReviewHandler.completeReviewTask(patientId, TaskTypeEnum.ADMISSION_REVIEW_TASK);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void reviewDischarge(final @NonNull String patientId)
  {
    reconciliationReviewHandler.completeReviewTask(patientId, TaskTypeEnum.DISCHARGE_REVIEW_TASK);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationOnAdmissionDto> getMedicationsOnAdmission(final @NonNull String patientId, final boolean validateTherapy, final @NonNull Locale locale)
  {
    return medicationOnAdmissionHandler.getMedicationsOnAdmission(patientId, validateTherapy, locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(SaveMedicationsOnDischarge.class)
  public List<String> saveMedicationsOnDischarge(
      final String patientId,
      final List<MedicationOnDischargeDto> medicationsOnDischarge,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final Locale locale)
  {
    return medicationOnDischargeHandler.saveMedicationsOnDischarge(patientId, medicationsOnDischarge, centralCaseId, careProviderId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      final @NonNull String patientId,
      final @NonNull Locale locale)
  {
    return medicationOnDischargeHandler.getMedicationsOnDischarge(patientId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public boolean isDischargeCreated(final @NonNull String patientId)
  {
    return medicationOnDischargeHandler.countMedicationsOnDischarge(patientId) > 0;
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public DischargeListDto getDischargeList(final @NonNull String patientId)
  {
    final List<MedicationOnDischargeDto> list = medicationOnDischargeHandler.getMedicationsOnDischarge(
        patientId,
        DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale());
    return dischargeMapper.mapDischargeList(list);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public DischargeSummaryDto getDischargeSummary(final @NonNull String patientId)
  {
    final ReconciliationSummaryDto summary = reconciliationSummaryHandler.getReconciliationSummary(
        patientId,
        DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale());
    return dischargeMapper.mapDischargeSummary(summary);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public ReconciliationSummaryDto getReconciliationSummary(final @NonNull String patientId, final @NonNull Locale locale)
  {
    return reconciliationSummaryHandler.getReconciliationSummary(patientId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  public List<InformationSourceDto> getInformationSources()
  {
    return medicationsDao.getInformationSources();
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyFlowDto getTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      @Nullable final String careProviderId,
      final Locale locale)
  {
    return overviewContentProvider.getTherapyFlow(
        patientId,
        centralCaseId,
        patientHeight,
        startDate,
        dayCount,
        todayIndex,
        roundsInterval,
        therapySortTypeEnum,
        careProviderId,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final String patientId,
      final String careProviderId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval)
  {
    final DateTime now = requestDateTimeHolder.getRequestTimestamp();
    return overviewContentProvider.reloadSingleTherapyAfterAction(
        patientId,
        careProviderId,
        compositionUid,
        ehrOrderName,
        roundsInterval,
        now);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(ModifyTherapy.class)
  public void modifyTherapy(
      final String patientId,
      final TherapyDto therapy,
      final TherapyChangeReasonDto changeReasonDto,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final DateTime saveDateTime,
      final String basedOnPharmacyReviewId,
      final Locale locale)
  {
    final InpatientPrescription inpatientPrescription =
        medicationsOpenEhrDao.loadInpatientPrescription(patientId, therapy.getCompositionUid());

    if (PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(inpatientPrescription))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    final DateTime when = saveDateTime != null ? saveDateTime : requestDateTimeHolder.getRequestTimestamp();

    final DateTime oldTherapyStart = DataValueUtils.getDateTime(
        inpatientPrescription.getMedicationOrder().getOrderDetails().getOrderStartDateTime());

    if (therapy.getStart().isBefore(when))
    {
      throw new UserWarning("Cannot edit therapy in the past");
    }

    therapyUpdater.modifyTherapy(
        patientId,
        therapy,
        changeReasonDto,
        centralCaseId,
        careProviderId,
        prescriber,
        oldTherapyStart.isBefore(when),
        basedOnPharmacyReviewId,
        when,
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(AbortTherapy.class)
  public String abortTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final String stopReason)
  {
    final InpatientPrescription inpatientPrescription =
        medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);

    if (PrescriptionsEhrUtils.isInpatientPrescriptionCompleted(inpatientPrescription))
    {
      throw new IllegalArgumentException("Therapy is finished.");
    }

    final TherapyChangeReasonDto changeReason = new TherapyChangeReasonDto();
    changeReason.setComment(stopReason);
    changeReason.setChangeReason(new CodedNameDto(
        TherapyChangeReasonEnum.STOP.toFullString(),
        Dictionary.getEntry(
            TherapyChangeReasonEnum.STOP.toFullString(),
            DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()
        )
    ));

    return therapyUpdater.abortTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        changeReason,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(AbortAllTherapies.class)
  public void abortAllTherapiesForPatient(final @NonNull String patientId, final String stopReason)
  {
    therapyBatchActionHandler.abortAllTherapies(patientId, requestDateTimeHolder.getRequestTimestamp(), stopReason);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void reviewTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName)
  {
    therapyUpdater.reviewTherapy(patientId, compositionUid);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(SuspendTherapy.class)
  public String suspendTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final String suspendReason)
  {
    final TherapyChangeReasonDto changeReason = new TherapyChangeReasonDto();
    changeReason.setComment(suspendReason);
    changeReason.setChangeReason(new CodedNameDto(
        TherapyChangeReasonEnum.SUSPEND.toFullString(),
        Dictionary.getEntry(
            TherapyChangeReasonEnum.SUSPEND.toFullString(),
            DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale()
        )
    ));

    return therapyUpdater.suspendTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        changeReason,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(ReissueTherapy.class)
  public String reissueTherapy(final String patientId, final String compositionUid, final String ehrOrderName)
  {
    return therapyUpdater.reissueTherapy(
        patientId,
        compositionUid,
        ehrOrderName,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(CreateAdministration.class)
  public void createAdditionalAdministrationTask(
      final String therapyCompositionUid,
      final String ehrOrderName,
      final String patientId,
      final StartAdministrationDto administrationDto)
  {
    final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        therapyCompositionUid);

    therapyUpdater.createAdditionalAdministrationTask(
        inpatientPrescription,
        patientId,
        administrationDto.getPlannedTime(),
        AdministrationTypeEnum.START,
        administrationDto.getPlannedDose());

  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(SuspendAllTherapies.class)
  public void suspendAllTherapies(
      final @NonNull String patientId,
      final String suspendReason)
  {
    therapyBatchActionHandler.suspendAllTherapies(patientId, requestDateTimeHolder.getRequestTimestamp(), suspendReason);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(SuspendAllTherapiesOnTemporaryLeave.class)
  public void suspendAllTherapiesOnTemporaryLeave(final @NonNull String patientId)
  {
    therapyBatchActionHandler.suspendAllTherapiesOnTemporaryLeave(
        patientId, requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(ReissueAllTherapiesOnReturnFromTemporaryLeave.class)
  public List<String> reissueAllTherapiesOnReturnFromTemporaryLeave(final @NonNull String patientId)
  {
    return therapyBatchActionHandler.reissueAllTherapiesOnReturnFromTemporaryLeave(
        patientId, requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyViewPatientDto getTherapyViewPatientData(final @NonNull String patientId, final String centralCaseId)
  {
    final DateTime requestTimestamp = requestDateTimeHolder.getRequestTimestamp();

    final TherapyViewPatientDto therapyViewPatientDto = new TherapyViewPatientDto();

    final PatientDataForMedicationsDto patientData =
        patientDataProvider.getPatientData(patientId, centralCaseId, requestTimestamp);

    therapyViewPatientDto.setPatientData(patientData);
    final MedicationsCentralCaseDto centralCaseDto = patientData.getCentralCaseDto();

    final String careProviderId =
        centralCaseDto != null && centralCaseDto.getCareProvider() != null ? centralCaseDto.getCareProvider().getId() : null;

    final boolean inpatient = centralCaseDto != null && !centralCaseDto.isOutpatient();
    final Interval referenceWeightSearchInterval;
    if (inpatient)
    {
      referenceWeightSearchInterval = new Interval(centralCaseDto.getCentralCaseEffective().getStart(), requestTimestamp);
      final Interval recentHospitalizationInterval = new Interval(requestTimestamp.minusHours(12), requestTimestamp);
      therapyViewPatientDto.setRecentHospitalization(
          recentHospitalizationInterval.contains(centralCaseDto.getCentralCaseEffective().getStart()));
    }
    else
    {
      referenceWeightSearchInterval = new Interval(requestTimestamp.minusHours(24), requestTimestamp);
    }

    final Pair<Double, DateTime> referenceWeightAndDate = medicationsOpenEhrDao.getPatientLastReferenceWeightAndDate(
        patientId,
        referenceWeightSearchInterval);
    if (referenceWeightAndDate != null)
    {
      therapyViewPatientDto.setReferenceWeight(referenceWeightAndDate.getFirst());
      therapyViewPatientDto.setReferenceWeightDate(referenceWeightAndDate.getSecond());
    }

    if (careProviderId != null)
    {
      final List<String> customGroups = medicationsDao.getCustomGroupNames(careProviderId);
      therapyViewPatientDto.setCustomGroups(customGroups);
    }

    final AdministrationTimingDto administrationTiming = MedicationPreferencesUtil.getAdministrationTiming(careProviderId);
    therapyViewPatientDto.setAdministrationTiming(administrationTiming);

    final RoundsIntervalDto roundsInterval = MedicationPreferencesUtil.getRoundsInterval(careProviderId);
    therapyViewPatientDto.setRoundsInterval(roundsInterval);

    final Long patientIdLong = parseToLong(patientId);
    if (patientIdLong != null)
    {
      final String lastLinkName = medicationsDao.getPatientLastLinkName(patientIdLong);
      therapyViewPatientDto.setLastLinkName(lastLinkName);
    }

    return therapyViewPatientDto;
  }

  private Long parseToLong(final String longString)
  {
    return StringUtils.isNumeric(longString) ? Long.parseLong(longString) : null;
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] getMedicationDocument(final String reference)
  {
    if (repositoryClient != null)
    {
      return repositoryClient.getPdfDocumentWithoutSettingOptions(reference);
    }
    else
    {
      throw new UnsupportedOperationException("Alfresco not configured!");
    }
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] getPastTherapiesReport(
      final @NonNull String patientId,
      final @NonNull DateTime startDate,
      final @NonNull DateTime endDate,
      final @NonNull Locale locale)
  {
    final TherapyDayReportDto therapyReportData = therapyReportDataProvider.getPastTherapiesReportData(
        patientId,
        locale,
        startDate,
        endDate);

    return Opt
        .of(therapyReportCreator.createPdfReport(RequestUser.getFullName(), therapyReportData))
        .map(TherapyReportPdfDto::getData)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] getTemplateReport(
      final @NonNull String patientId,
      final int numberOfPages,
      final @NonNull Locale locale)
  {
    final TherapyTemplateReportDto therapyReportData = therapyReportDataProvider.getTemplateReport(
        patientId,
        numberOfPages,
        locale);

    return Opt
        .of(therapyReportCreator.createTherapyTemplateReport(therapyReportData))
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] getActiveAndPastTherapiesReport(final @NonNull String patientId, final @NonNull Locale locale)
  {
    final TherapyDayReportDto therapyReportData = therapyReportDataProvider.getActiveAndPastTherapiesReportData(
        patientId,
        locale);

    return Opt
        .of(therapyReportCreator.createPdfReport(RequestUser.getFullName(), therapyReportData))
        .map(TherapyReportPdfDto::getData)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<Interval> getPatientBaselineInfusionIntervals(final String patientId)
  {
    return medicationsOpenEhrDao.getPatientBaselineInfusionIntervals(
        patientId,
        Intervals.infiniteFrom(requestDateTimeHolder.getRequestTimestamp()));
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyDayReportDto getCurrentTherapyReportData(final @NonNull String patientId, final @NonNull Locale locale)
  {

    return therapyReportDataProvider.getActiveAndPastTherapiesReportData(patientId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] createTherapySurgeryReport(
      final @NonNull String patientId,
      final @NonNull Locale locale)
  {
    final TherapySurgeryReportDto therapyReportData =
        therapyReportDataProvider.getTherapySurgeryReport(
            patientId,
            locale,
            requestDateTimeHolder.getRequestTimestamp());

    return therapyReportCreator.printTherapySurgeryReport(therapyReportData);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public byte[] getOutpatientPrescriptionPrintout(
      final @NonNull String patientId,
      final @NonNull String compositionUid,
      final @NonNull Locale locale)
  {
    return outpatientPrescriptionHandler.getOutpatientPrescriptionPrintout(
        patientId,
        compositionUid,
        locale,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public String getTherapyFormattedDisplay(final String patientId, final String therapyId, final Locale locale)
  {
    return medicationsBo.getTherapyFormattedDisplay(patientId, therapyId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyAuditTrailDto getTherapyAuditTrail(
      final @NonNull String patientId,
      final @NonNull String compositionId,
      final @NonNull String ehrOrderName,
      final Double patientHeight,
      final @NonNull Locale locale)
  {
    return therapyAuditTrailProvider.getTherapyAuditTrail(
        patientId,
        compositionId,
        ehrOrderName,
        patientHeight,
        locale,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyDto fillTherapyDisplayValues(final TherapyDto therapy, final Locale locale)
  {
    therapyDisplayProvider.fillDisplayValues(therapy, true, true, false, locale, false);
    return therapy;
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyDto getTherapyDto(final String patientId, final String therapyId, final Locale locale)
  {
    final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(therapyId);
    return medicationsBo.getTherapy(
        patientId,
        therapyIdPair.getFirst(),
        therapyIdPair.getSecond(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public PharmacistReviewTherapyDto fillPharmacistReviewTherapyOnEdit(
      final TherapyDto originalTherapy,
      final TherapyDto changedTherapy,
      final Locale locale)
  {
    final PharmacistReviewTherapyDto pharmacistReviewTherapyDto = new PharmacistReviewTherapyDto();
    therapyDisplayProvider.fillDisplayValues(changedTherapy, true, locale);
    pharmacistReviewTherapyDto.setTherapy(changedTherapy);
    pharmacistReviewTherapyDto.setChangeType(PharmacistTherapyChangeType.EDIT);
    final List<TherapyChangeDto<?, ?>> changes =
        therapyChangeCalculator.calculateTherapyChanges(originalTherapy, changedTherapy, false, locale);
    pharmacistReviewTherapyDto.setChanges(changes);
    return pharmacistReviewTherapyDto;
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<TreeNodeData> findSimilarMedications(
      final long medicationId,
      final Locale locale)
  {
    return medicationsFinder.findSimilarMedications(medicationId, requestDateTimeHolder.getRequestTimestamp(), locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationDto> findMedicationProducts(
      final long medicationId,
      final @NonNull List<Long> routeIds,
      final ReleaseDetailsDto releaseDetails)
  {
    return medicationsFinder.findMedicationProducts(
        medicationId,
        routeIds,
        releaseDetails,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationRouteDto> getRoutes()
  {
    return medicationsDao.getRoutes();
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<DoseFormDto> getDoseForms()
  {
    return medicationsDao.getDoseForms();
  }

  @Override
  @Transactional(readOnly = true)
  public UnitsHolderDto getUnitsHolder()
  {
    return unitsProvider.getUnitsHolder();
  }

  @Override
  @Transactional
  @EhrSessioned
  public void savePatientReferenceWeight(final String patientId, final double weight)
  {
    final ReferenceWeight referenceWeight = medicationsBo.buildReferenceWeightComposition(
        weight,
        requestDateTimeHolder.getRequestTimestamp());
    medicationsOpenEhrDao.saveComposition(patientId, referenceWeight, null);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<TherapyDto> getLastTherapiesForPreviousHospitalization(
      final String patientId,
      final Double patientHeight,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    final Interval lastDischargedCentralCaseInterval = medicationsConnector.getLastDischargedCentralCaseEffectiveInterval(
        patientId);
    if (lastDischargedCentralCaseInterval != null)
    {
      final Interval lastDayBeforeDischarge =
          new Interval(
              lastDischargedCentralCaseInterval.getEnd().minusHours(24),
              lastDischargedCentralCaseInterval.getEnd().plusMinutes(1));

      final Interval referenceWeightInterval = Intervals.infiniteTo(when);
      final Double referenceWeight =
          medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);

      //??
      //final Double height = patientDataForMedicationsProvider.getPatientLastHeight(patientId, new Interval(Intervals.INFINITE.getStart(), when));
      final List<TherapyDto> therapies = medicationsBo.getTherapies(
          patientId,
          lastDayBeforeDischarge,
          null,
          patientHeight,
          null);

      fillDisplayValuesAndInfusionRateForTherapies(patientHeight, locale, referenceWeight, therapies);

      return therapies;
    }
    return new ArrayList<>();
  }

  private void fillDisplayValuesAndInfusionRateForTherapies(
      final Double patientHeight,
      final Locale locale,
      final Double referenceWeight,
      final List<TherapyDto> therapies)
  {
    for (final TherapyDto therapyDto : therapies)
    {
      if (therapyDto instanceof ComplexTherapyDto && referenceWeight != null)
      {
        if (((ComplexTherapyDto)therapyDto).isContinuousInfusion())
        {
          medicationsBo.fillInfusionRateFromFormula((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
        }
        else
        {
          medicationsBo.fillInfusionFormulaFromRate((ComplexTherapyDto)therapyDto, referenceWeight, patientHeight);
        }
      }
      therapyDisplayProvider.fillDisplayValues(therapyDto, true, locale);
    }
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(final String patientId, final Locale locale)
  {
    return medicationOnAdmissionHandler.getTherapiesOnAdmissionGroups(patientId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationOnDischargeGroupDto> getTherapiesOnDischargeGroups(
      final @NonNull String patientId,
      final Double patientHeight,
      final @NonNull Locale locale)
  {
    final Interval referenceWeightInterval = Intervals.infiniteTo(requestDateTimeHolder.getRequestTimestamp());
    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, referenceWeightInterval);
    return medicationOnDischargeHandler.getMedicationOnDischargeGroups(patientId, referenceWeight, patientHeight, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MentalHealthTherapyDto> getCurrentHospitalizationMentalHealthTherapies(
      final String patientId,
      final DateTime hospitalizationStart,
      @Nullable final Locale locale)
  {
    return medicationsBo.getMentalHealthTherapies(
        patientId,
        Intervals.infiniteFrom(hospitalizationStart),
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<TherapyDto> getLinkTherapyCandidates(
      final @NonNull String patientId,
      @Nullable final Double referenceWeight,
      final Double patientHeight,
      final @NonNull Locale locale)
  {
    return medicationsBo.getLinkTherapyCandidates(
        patientId,
        referenceWeight,
        patientHeight,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyTemplatesDto getTherapyTemplates(
      final @NonNull String patientId,
      final @NonNull TherapyTemplateContextEnum templateContext,
      final String careProviderId,
      final Double referenceWeight,
      final Double patientHeightInCm,
      final DateTime birthDate,
      final @NonNull Locale locale)
  {
    final Set<String> authorities = RequestUser.getAuthorities()
        .stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());

    final Set<String> usersTemplateGroups = TherapyAuthorityEnum.getTherapyTemplateGroups(authorities);

    final boolean hasPrescribeAuthority = authorities.stream()
        .anyMatch(a -> a.contains(templateContext.getTherapyAuthorityEnum().getCode()));

    return therapyTemplateDao.getTherapyTemplates(
        templateContext.getTherapyTemplateMode(),
        usersTemplateGroups,
        hasPrescribeAuthority ? patientId : null,
        hasPrescribeAuthority ? RequestUser.getId() : null,
        hasPrescribeAuthority ? careProviderId : null,
        referenceWeight,
        patientHeightInCm,
        birthDate,
        locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyTemplatesDto getAllTherapyTemplates(
      final @NonNull TherapyTemplateModeEnum templateMode,
      final String careProviderId,
      final @NonNull Locale locale)
  {
    return therapyTemplateDao.getAllTherapyTemplates(templateMode, RequestUser.getId(), careProviderId, locale);
  }

  @Override
  @Transactional(readOnly = true)
  public List<String> getTherapyTemplateGroups(final @NonNull TherapyTemplateModeEnum templateMode)
  {
    return therapyTemplateDao.getTherapyTemplateGroups(templateMode);
  }

  @Override
  @Transactional
  @EhrSessioned
  public long saveTherapyTemplate(final TherapyTemplateDto therapyTemplate, final TherapyTemplateModeEnum templateMode)
  {
    return therapyTemplateDao.saveTherapyTemplate(therapyTemplate, templateMode);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void deleteTherapyTemplate(final long templateId)
  {
    therapyTemplateDao.deleteTherapyTemplate(templateId);
  }

  @Override
  @Transactional
  public void addTherapyTemplateGroup(final @NonNull String name, final @NonNull TherapyTemplateModeEnum templateMode)
  {
    therapyTemplateDao.addTherapyTemplateGroup(name, templateMode);
  }

  @Override
  @Transactional
  public void deleteTherapyTemplateGroup(final long id)
  {
    therapyTemplateDao.deleteTherapyTemplateGroup(id);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyTimelineDto getTherapyTimeline(
      final @NonNull String patientId,
      final @NonNull Interval interval,
      final @NonNull TherapySortTypeEnum sortTypeEnum,
      final boolean hidePastTherapies,
      final boolean hideFutureTherapies,
      final @NonNull PatientDataForMedicationsDto patientData,
      final RoundsIntervalDto roundsInterval,
      final Locale locale)
  {
    final Interval therapiesSearchInterval =
        hideFutureTherapies
        ? interval
        : Intervals.infiniteFrom(new DateTime(interval.getStartMillis()));

    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        therapiesSearchInterval);

    final List<AdministrationDto> administrations = administrationProvider.getPrescriptionsAdministrations(
        patientId,
        prescriptions,
        null,
        true);

    final List<String> therapyIds = prescriptions
        .stream()
        .map(i -> TherapyIdUtils.createTherapyId(i.getUid()))
        .collect(Collectors.toList());

    final List<AdministrationTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        therapyIds,
        // activiti library does not support including dueAfter condition, we would need to add two conditions, which would complicate logic and affect performance
        new Interval(interval.getStart().minusMillis(1), interval.getEnd()),
        false);

    tasks.addAll(infusionBagTaskProvider.findInfusionBagTasks(patientId, therapyIds, interval));

    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return overviewContentProvider.getTherapyTimeline(
        patientId,
        administrations,
        tasks,
        prescriptions,
        sortTypeEnum,
        hidePastTherapies,
        patientData,
        interval,
        roundsInterval,
        locale,
        when);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyTimelineDto getPharmacistTimeline(
      final @NonNull String patientId,
      final @NonNull Interval interval,
      final @NonNull TherapySortTypeEnum sortTypeEnum,
      final boolean hidePastTherapies,
      final @NonNull PatientDataForMedicationsDto patientData,
      final RoundsIntervalDto roundsInterval,
      final Locale locale)
  {
    final Interval intervalToInfinity = Intervals.infiniteFrom(new DateTime(interval.getStartMillis()));

    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(patientId, intervalToInfinity);
    final List<String> therapyIds = prescriptions.stream().map(TherapyIdUtils::createTherapyId).collect(Collectors.toList());

    final List<AdministrationTaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        therapyIds,
        // activiti library does not support including dueAfter condition, we would need to add two conditions, which would complicate logic and affect performance
        new Interval(interval.getStart().minusMillis(1), interval.getEnd()),
        false);

    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return overviewContentProvider.getTherapyTimeline(
        patientId,
        Collections.emptyList(),
        tasks,
        prescriptions,
        sortTypeEnum,
        hidePastTherapies,
        patientData,
        null,
        roundsInterval,
        locale,
        when);
  }

  @Override
  @Transactional
  @EhrSessioned
  public DocumentationTherapiesDto getTherapyDataForDocumentation(
      final String patientId,
      final Interval centralCaseEffective,
      final boolean isOutpatient,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(patientId, centralCaseEffective);

    return medicationsBo.findTherapyGroupsForDocumentation(
        patientId,
        centralCaseEffective,
        prescriptions,
        isOutpatient,
        when,
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(ConfirmAdministration.class)
  public void confirmTherapyAdministration(
      final String therapyCompositionUid,
      final String patientId,
      final AdministrationDto administrationDto,
      final boolean editMode,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final boolean requestSupply,
      final Locale locale)
  {
    administrationHandler.confirmTherapyAdministration(
        therapyCompositionUid,
        patientId,
        RequestUser.getId(),
        administrationDto,
        editMode,
        requestSupply,
        centralCaseId,
        careProviderId,
        locale,
        requestDateTimeHolder.getRequestTimestamp()
    );
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(RescheduleTasks.class)
  public void rescheduleTasks(
      final @NonNull String patientId,
      final @NonNull String taskId,
      final @NonNull DateTime newTime,
      final @NonNull String therapyId)
  {
    tasksRescheduler.rescheduleAdministrationTasks(patientId, taskId, newTime);
  }

  @Override
  @Transactional
  public void rescheduleTherapyDoctorReviewTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime,
      final String comment)
  {
    tasksRescheduler.rescheduleTherapyDoctorReviewTask(taskId, newTime, comment);
  }

  @Override
  @Transactional
  public void rescheduleIvToOralTask(
      final @NonNull String taskId,
      final @NonNull DateTime newTime)
  {
    tasksRescheduler.rescheduleIvToOralTask(taskId, newTime);
  }

  @Override
  @Transactional
  @EhrSessioned
  @EventProducer(RescheduleTask.class)
  public void rescheduleAdministrationTask(
      final @NonNull String patientId,
      final @NonNull String taskId,
      final @NonNull DateTime newTime,
      final @NonNull String therapyId)
  {
    tasksRescheduler.rescheduleAdministrationTask(patientId, taskId, newTime);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void setAdministrationTitratedDose(
      final @NonNull String patientId,
      final @NonNull String latestTherapyId,
      final @NonNull StartAdministrationDto administration,
      final boolean confirmAdministration,
      final String centralCaseId,
      final String careProviderId,
      final DateTime until,
      final @NonNull Locale locale)
  {
    medicationsTasksHandler.setAdministrationTitratedDose(
        patientId,
        latestTherapyId,
        administration.getTaskId(),
        administration.getPlannedDose(),
        administration.getDoctorsComment(),
        administration.getPlannedTime(),
        until);

    if (confirmAdministration)
    {
      administration.setAdministrationResult(AdministrationResultEnum.GIVEN);
      administrationHandler.confirmTherapyAdministration(
          TherapyIdUtils.extractCompositionUid(administration.getTherapyId()),
          patientId,
          RequestUser.getId(),
          administration,
          false,
          false,
          centralCaseId,
          careProviderId,
          locale,
          requestDateTimeHolder.getRequestTimestamp()
      );
    }
  }

  @Override
  @Transactional
  @EhrSessioned
  public void setDoctorConfirmationResult(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final boolean result)
  {
    administrationDoctorConfirmationHandler.setDoctorConfirmation(patientId, administration, result);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void cancelAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration,
      final @NonNull String comment)
  {
    administrationHandler.cancelAdministrationTask(
        patientId,
        administration,
        NotAdministeredReasonEnum.CANCELLED,
        comment);
  }

  @Override
  @EhrSessioned
  public void uncancelAdministrationTask(
      final @NonNull String patientId,
      final @NonNull AdministrationDto administration)
  {
    administrationHandler.uncancelAdministrationTask(patientId, administration);
  }

  @Override
  @Transactional
  public void deleteTask(
      final String patientId,
      final String taskId,
      final String groupUUId,
      final String therapyId,
      final String comment)
  {
    if (groupUUId != null)
    {
      Preconditions.checkNotNull(therapyId, "therapyId");
      medicationsTasksHandler.deleteAdministrationTasks(
          patientId,
          therapyId,
          groupUUId,
          Arrays.asList(AdministrationTypeEnum.values()));
    }
    else
    {
      medicationsTasksHandler.deleteTask(taskId, comment);
    }
  }

  @Override
  @EhrSessioned
  @Transactional
  @EventProducer(DeleteAdministration.class)
  public void deleteAdministration(
      final String patientId,
      final AdministrationDto administration,
      final TherapyDoseTypeEnum therapyDoseType,
      final String therapyId,
      final String comment)
  {
    administrationHandler.deleteAdministration(patientId, administration, therapyDoseType, therapyId, comment);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<NamedExternalDto> getCareProfessionals()
  {
    //only in use for DRP, implement when required
    return new ArrayList<>();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PatientTaskDto> getPharmacistReviewTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds)
  {
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(
        TaskTypeEnum.PHARMACIST_REMINDER,
        TaskTypeEnum.ADMISSION_REVIEW_TASK,
        TaskTypeEnum.DISCHARGE_REVIEW_TASK,
        TaskTypeEnum.PHARMACIST_REVIEW);

    final DateTime now = requestDateTimeHolder.getRequestTimestamp();
    return pharmacistTaskProvider.findPharmacistTasks(
        Intervals.infiniteFrom(now.minusDays(7)),
        getPatientIds(careProviderIds, patientIds),
        taskTypes);
  }

  private Collection<String> getPatientIds(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds)
  {
    Preconditions.checkArgument(
        Boolean.logicalXor(careProviderIds.isPresent(), patientIds.isPresent()),
        "Exactly one, careProviderIds or patientIds must be present");

    if (careProviderIds.isPresent())
    {
      final Collection<String> searchCareProviders =
          careProviderIds.get().isEmpty()
          ? getCurrentUserCareProviders().stream().map(ExternalIdentityDto::getId).collect(Collectors.toSet())
          : careProviderIds.get();

      if (searchCareProviders.isEmpty())
      {
        return new HashSet<>();
      }

      return patientDataProvider.getPatientIds(searchCareProviders);
    }
    return patientIds.get();
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<AdministrationPatientTaskDto> getAdministrationTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      final Set<AdministrationTypeEnum> types,
      final @NonNull Locale locale)
  {
    final AdministrationPatientTaskLimitsDto administrationLimits =
        MedicationPreferencesUtil.getAdministrationPatientTaskLimitsPreference();

    final DateTime when = requestDateTimeHolder.getRequestTimestamp();

    final Interval searchInterval = new Interval(
        when.minusMinutes(administrationLimits.getDueTaskOffset()),
        when.plusMinutes(administrationLimits.getFutureTaskOffset()));

    return medicationsTasksProvider.findAdministrationTasks(
        getPatientIds(careProviderIds, patientIds),
        searchInterval,
        administrationLimits.getMaxNumberOfTasks(),
        types,
        locale,
        when);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public RuleResult applyMedicationRule(
      final @NonNull RuleParameters ruleParameters,
      final @NonNull Locale locale)
  {
    final DateTime requestTimestamp = requestDateTimeHolder.getRequestTimestamp();
    return medicationIngredientRuleHandler.applyMedicationRule(ruleParameters, requestTimestamp, locale);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public List<MedicationSupplyTaskDto> findSupplyTasks(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      final Set<TaskTypeEnum> taskTypes,
      final boolean closedTasksOnly,
      final boolean includeUnverifiedDispenseTasks,
      final @NonNull Locale locale)
  {
    return pharmacistTaskProvider.findSupplyTasks(
        null,
        getPatientIds(careProviderIds, patientIds),
        taskTypes,
        closedTasksOnly,
        includeUnverifiedDispenseTasks,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void handleNurseResupplyRequest(
      final String patientId,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Locale locale)
  {
    try
    {
      pharmacySupplyProcessHandler.handleSupplyRequest(
          patientId,
          TherapyAssigneeEnum.NURSE,
          therapyCompositionUid,
          ehrOrderName,
          null,
          null);
    }
    catch (final IllegalStateException ise)
    {
      //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
      throw new UserWarning(Dictionary.getEntry("nurse.resupply.request.already.exists.warning", locale));
    }
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> getProblemDescriptionNamedIdentities(final Locale locale)
  {
    final Map<String, TherapyProblemDescriptionEnum> pathKeyMap = new HashMap<>();
    for (final TherapyProblemDescriptionEnum problemDescriptionEnum : TherapyProblemDescriptionEnum.values())
    {
      pathKeyMap.put(problemDescriptionEnum.getPath(), problemDescriptionEnum);
    }

    final Map<String, List<NamedExternalDto>> termsMap = medicationsOpenEhrDao.getTemplateTerms(
        "OPENeP - Pharmacy Review Report",
        pathKeyMap.keySet(),
        locale);

    //noinspection MapReplaceableByEnumMap
    final Map<TherapyProblemDescriptionEnum, List<NamedExternalDto>> problemDescriptionNamedIdentities = new HashMap<>();
    for (final String path : termsMap.keySet())
    {
      problemDescriptionNamedIdentities.put(pathKeyMap.get(path), termsMap.get(path));
    }

    return problemDescriptionNamedIdentities;
  }

  @Override
  @Transactional
  @EhrSessioned
  public List<PharmacistReviewDto> getPharmacistReviewsForTherapy(
      final String patientId,
      final String compositionUid,
      final Locale locale)
  {
    return pharmacistReviewProvider.loadReviewsForTherapy(patientId, compositionUid, locale);
  }

  @Override
  @Transactional
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return medicationsConnector.getCurrentUserCareProviders();
  }

  @Override
  @Transactional
  public List<AdministrationDto> calculateTherapyAdministrationTimes(final @NonNull TherapyDto therapy)
  {
    return administrationTaskCreator.calculateTherapyAdministrationTimes(
        therapy,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  public DateTime calculateNextTherapyAdministrationTime(
      final @NonNull String patientId,
      final @NonNull TherapyDto therapy,
      final boolean newPrescription)
  {
    return administrationTaskCreator.calculateNextTherapyAdministrationTime(
        patientId,
        therapy,
        newPrescription,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  public DateTime findPreviousTaskForTherapy(final String patientId, final String compositionUid, final String ehrOrderName)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return medicationsBo.findPreviousTaskForTherapy(patientId, compositionUid, ehrOrderName, when);
  }

  @Override
  @Transactional
  public void dismissSupplyTask(final String patientId, final List<String> taskIds)
  {
    pharmacistTaskHandler.dismissSupplyTask(taskIds, RequestUser.getId());
  }

  @Override
  @Transactional
  public void deleteNurseSupplyTask(final String patientId, final String taskId, final Locale locale)
  {
    try
    {
      pharmacistTaskHandler.deleteNurseSupplyTask(patientId, taskId, RequestUser.getId());
    }
    catch (final IllegalStateException ise)
    {
      //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
      throw new UserWarning(Dictionary.getEntry("nurse.resupply.cannot.delete.already.dispensed.warning", locale));
    }
  }

  @Override
  @Transactional
  public void confirmSupplyReminderTask(
      final String taskId,
      final String compositionUid,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    pharmacistTaskHandler.confirmSupplyReminderTask(
        taskId,
        compositionUid,
        supplyTypeEnum,
        supplyInDays,
        RequestUser.getId(),
        comment);
  }

  @Override
  @Transactional
  public void editSupplyReminderTask(
      final String taskId,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    pharmacistTaskHandler.editSupplyReminderTask(
        taskId,
        supplyTypeEnum,
        supplyInDays,
        comment,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  public void confirmSupplyReviewTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final boolean createSupplyReminder,
      final MedicationSupplyTypeEnum supplyTypeEnum,
      final Integer supplyInDays,
      final String comment)
  {
    final SupplyReviewTaskSimpleDto reviewTask =
        (SupplyReviewTaskSimpleDto)pharmacistTaskProvider.getSupplySimpleTask(taskId);

    pharmacistTaskHandler.confirmSupplyReviewTask(
        patientId,
        taskId,
        compositionUid,
        reviewTask.isAlreadyDispensed(),
        createSupplyReminder,
        supplyTypeEnum,
        supplyInDays,
        comment,
        RequestUser.getId(),
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  public void confirmPharmacistDispenseTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final TherapyAssigneeEnum requesterRole,
      final SupplyRequestStatus supplyRequestStatus)
  {
    pharmacistTaskHandler.confirmPharmacistDispenseTask(
        patientId,
        taskId,
        compositionUid,
        requesterRole,
        supplyRequestStatus,
        RequestUser.getId());
  }

  @Override
  @Transactional
  @EhrSessioned
  public MedicationSupplyTaskSimpleDto getSupplySimpleTask(final String taskId, final Locale locale)
  {
    return pharmacistTaskProvider.getSupplySimpleTask(
        taskId,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  public Map<ActionReasonType, List<CodedNameDto>> getActionReasons(final ActionReasonType type)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return medicationsDao.getActionReasons(when, type);
  }

  @Override
  @Transactional
  public List<MentalHealthTemplateDto> getMentalHealthTemplates()
  {
    return therapyTemplateDao.getMentalHealthTemplates();
  }

  @Override
  @Transactional
  @EhrSessioned
  public void saveMentalHealthReport(
      final @NonNull MentalHealthDocumentDto mentalHealthDocumentDto,
      final NamedExternalDto careProvider)
  {
    consentFormToEhrSaver.saveNewMentalHealthForm(
        mentalHealthDocumentDto,
        careProvider,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  public SupplyDataForPharmacistReviewDto getSupplyDataForPharmacistReview(
      final @NonNull String patientId,
      final @NonNull String therapyCompositionUid)
  {
    return pharmacistTaskProvider.getSupplyDataForPharmacistReview(patientId, therapyCompositionUid);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void orderPerfusionSyringePreparation(
      final @NonNull String patientId,
      final @NonNull String compositionUid,
      final @NonNull String ehrOrderName,
      final int numberOfSyringes,
      final boolean urgent,
      final @NonNull DateTime dueTime,
      final boolean printSystemLabel)
  {
    preparePerfusionSyringeProcessHandler.handlePreparationRequest(
        patientId,
        compositionUid,
        ehrOrderName,
        numberOfSyringes,
        urgent,
        dueTime,
        RequestUser.getFullName(),
        printSystemLabel);
  }

  @Override
  @Transactional
  @EhrSessioned
  public List<PerfusionSyringePatientTasksDto> findPerfusionSyringePreparationRequests(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      final @NonNull Set<TaskTypeEnum> taskTypes,
      final @NonNull Locale locale)
  {
    return pharmacistTaskProvider.findPerfusionSyringeTasks(
        getPatientIds(careProviderIds, patientIds),
        null,
        taskTypes,
        false,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  @EhrSessioned
  public List<PerfusionSyringePatientTasksDto> findFinishedPerfusionSyringePreparationRequests(
      final Opt<Collection<String>> careProviderIds,
      final Opt<Collection<String>> patientIds,
      final @NonNull Interval searchInterval,
      final @NonNull Locale locale)
  {
    // we only show closed PERFUSION_SYRINGE_DISPENSE tasks
    final Set<TaskTypeEnum> taskTypes = EnumSet.of(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE);

    return pharmacistTaskProvider.findPerfusionSyringeTasks(
        getPatientIds(careProviderIds, patientIds),
        searchInterval,
        taskTypes,
        true,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  public boolean finishedPerfusionSyringeRequestsExistInLastHours(
      final @NonNull String patientId,
      final @NonNull String originalTherapyId,
      final int hours)
  {
    final DateTime requestTimestamp = requestDateTimeHolder.getRequestTimestamp();
    return pharmacistTaskProvider.therapyHasTasksClosedInInterval(
        patientId,
        originalTherapyId,
        Collections.singleton(TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE),
        new Interval(requestTimestamp.minusHours(hours), requestTimestamp)
    );
  }

  @Override
  @Transactional
  @EhrSessioned
  public Map<String, PerfusionSyringePreparationDto> startPerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final Set<String> originalTherapyIds,
      final boolean isUrgent,
      final Locale locale)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(
        taskIds,
        RequestUser.getId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringePreparationDtoMap(
        patientId,
        isUrgent,
        originalTherapyIds,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @Transactional
  public Map<String, String> dispensePerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(taskIds, RequestUser.getId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  public Map<String, String> confirmPerfusionSyringePreparations(
      final String patientId,
      final List<String> taskIds,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.confirmPerfusionSyringeTasks(taskIds, RequestUser.getId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  public void deletePerfusionSyringeRequest(final String taskId, final Locale locale)
  {
    try
    {
      pharmacistTaskHandler.deletePerfusionSyringeTask(taskId, RequestUser.getId());
    }
    catch (final SystemException se)
    {
      //noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
      throw new UserWarning(Dictionary.getEntry("data.changed.please.reload", locale));
    }
  }

  @Override
  @Transactional
  public Map<String, String> undoPerfusionSyringeRequestState(
      final String patientId,
      final String taskId,
      final boolean isUrgent)
  {
    pharmacistTaskHandler.undoPerfusionSyringeTask(taskId, RequestUser.getId());
    return pharmacistTaskProvider.getOriginalTherapyIdAndPerfusionSyringeTaskIdMap(patientId, isUrgent);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void updateTherapySelfAdministeringStatus(
      final String patientId,
      final String compositionUid,
      final SelfAdministeringActionEnum selfAdministeringActionEnum)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);

    therapyUpdater.updateTherapySelfAdministeringStatus(
        patientId,
        prescription,
        selfAdministeringActionEnum,
        RequestUser.getId(),
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  public void printPharmacistDispenseTask(
      final String patientId,
      final String taskId,
      final String compositionUid,
      final TherapyAssigneeEnum requesterRole,
      final SupplyRequestStatus supplyRequestStatusEnum)
  {
    pharmacistTaskHandler.setDispenseTaskPrintedTimestamp(taskId, requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public Map<String, String> getTherapiesFormattedDescriptionsMap(
      final String patientId,
      final Set<String> therapyIds,
      final Locale locale)
  {
    final Map<String, String> descriptionsMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      try
      {
        final String therapyFormattedDisplay = medicationsBo.getTherapyFormattedDisplay(patientId, therapyId, locale);
        descriptionsMap.put(therapyId, therapyFormattedDisplay);
      }
      catch (final CompositionNotFoundException e)
      {
        LOG.error(StackTraceUtils.getStackTraceString(e));
      }
    }
    return descriptionsMap;
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public Integer getPatientsCumulativeAntipsychoticPercentage(final String patientId)
  {
    return antipsychoticsWarningsHandler.getPatientsCumulativeAntipsychoticPercentage(patientId);
  }

  @Override
  @Transactional
  @EhrSessioned
  public String saveOutpatientPrescription(final String patientId, final PrescriptionPackageDto prescriptionPackageDto)
  {
    return outpatientPrescriptionHandler.savePrescription(patientId, prescriptionPackageDto, requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  public void deleteOutpatientPrescription(final @NonNull String patientId, final @NonNull String prescriptionUid)
  {
    medicationsOpenEhrDao.deleteComposition(patientId, prescriptionUid);
  }

  @Override
  @Transactional
  @EhrSessioned
  public void updateOutpatientPrescriptionStatus(
      final String patientId,
      final String compositionUid,
      final String prescriptionTherapyId,
      final OutpatientPrescriptionStatus status)
  {
    outpatientPrescriptionHandler.updatePrescriptionStatus(
        patientId,
        compositionUid,
        prescriptionTherapyId,
        status,
        requestDateTimeHolder.getRequestTimestamp());
  }

  @Override
  @Transactional
  @EhrSessioned
  public String updateOutpatientPrescription(
      final String patientId,
      final String prescriptionPackageId,
      final String compositionUid,
      final List<PrescriptionDto> prescriptionDtoList,
      final DateTime when,
      final Locale locale)
  {
    return outpatientPrescriptionHandler.updatePrescriptionPackage(patientId, compositionUid, prescriptionDtoList, when);
  }

  @Override
  @Transactional
  @EhrSessioned
  public PrescriptionPackageDto getOutpatientPrescriptionPackage(
      final String patientId,
      final String compositionUid,
      final Locale locale)
  {
    final TherapyDocumentDto document = therapyDocumentProvider.getTherapyDocument(
        patientId,
        compositionUid,
        TherapyDocumentTypeEnum.EER_PRESCRIPTION,
        locale);
    return document != null ? (PrescriptionPackageDto)document.getContent() : null;
  }

  @Override
  @Transactional(readOnly = true)
  public PerfusionSyringeTaskSimpleDto getPerfusionSyringeTaskSimpleDto(final String taskId, final Locale locale)
  {
    return pharmacistTaskProvider.getPerfusionSyringeTaskSimpleDto(taskId, locale);
  }

  @Override
  @Transactional
  public void editPerfusionSyringeTask(
      final @NonNull String taskId,
      final @NonNull Integer numberOfSyringes,
      final boolean isUrgent,
      final @NonNull DateTime dueDate,
      final boolean printSystemLabel)
  {
    pharmacistTaskHandler.editPerfusionSyringeTask(taskId, numberOfSyringes, isUrgent, dueDate, printSystemLabel);
  }

  @Override
  @Transactional(readOnly = true)
  @EhrSessioned
  public TherapyDocumentsDto getTherapyDocuments(
      final String patientId,
      final Integer numberOfResults,
      final Integer resultsOffset,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return therapyDocumentProvider.getTherapyDocuments(patientId, numberOfResults, resultsOffset, when, locale);
  }

  @Override
  @Transactional(readOnly = true)
  public String getMedicationExternalId(final @NonNull String externalSystem, final long medicationId)
  {
    final Map<Long, String> medicationsExternalIds = medicationsDao.getMedicationsExternalIds(
        externalSystem,
        Sets.newHashSet(medicationId));

    return medicationsExternalIds.get(medicationId);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  public TitrationDto getDataForTitration(
      final @NonNull String patientId,
      final @NonNull String therapyId,
      final @NonNull TitrationType titrationType,
      final @NonNull DateTime searchStart,
      final @NonNull DateTime searchEnd,
      final @NonNull Locale locale)
  {
    return titrationDataProvider.getDataForTitration(
        patientId,
        therapyId,
        titrationType,
        searchStart,
        searchEnd,
        requestDateTimeHolder.getRequestTimestamp(),
        locale);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  public List<TherapyRowDto> getActiveTherapies(
      final @NonNull String patientId,
      final @NonNull PatientDataForMedicationsDto patientData,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();

    final List<InpatientPrescription> prescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        Intervals.infiniteFrom(when.minusHours(24)));

    return overviewContentProvider.buildTherapyRows(
        patientId,
        prescriptions,
        Collections.emptyList(),
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_DESC,
        true,
        Collections.emptyList(),
        patientData,
        Intervals.infiniteFrom(when.minusHours(24)),
        null,
        locale,
        when);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  //for emram, refactor - ok Primoz, refactor :)
  public List<TherapyDto> getTherapiesForCurrentCentralCase(final @NonNull String patientId, final @NonNull Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    final PatientDataForMedicationsDto patientData = medicationsConnector.getPatientData(patientId, null, when);

    final MedicationsCentralCaseDto centralCaseDto = patientData.getCentralCaseDto();
    if (centralCaseDto == null)
    {
      return Collections.emptyList();
    }

    final DateTime fromWhen =
        centralCaseDto.isOutpatient() ?
        when.withTimeAtStartOfDay() :
        centralCaseDto.getCentralCaseEffective().getStart();

    final List<InpatientPrescription> inpatientPrescriptions = medicationsOpenEhrDao.findInpatientPrescriptions(
        patientId,
        Intervals.infiniteFrom(fromWhen));

    final List<TherapyRowDto> therapyRows = overviewContentProvider.buildTherapyRows(
        patientId,
        inpatientPrescriptions,
        Collections.emptyList(),
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_DESC,
        false,
        Collections.emptyList(),
        null,
        Intervals.infiniteFrom(fromWhen),
        null,
        locale,
        when);

    return therapyRows.stream()
        .map(TherapyDayDto::getTherapy)
        .collect(Collectors.toList());
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  public Double getRemainingInfusionBagQuantity(
      final @NonNull DateTime when,
      final @NonNull String patientId,
      final @NonNull String therapyId)
  {
    return infusionBagHandler.getRemainingInfusionBagQuantity(patientId, therapyId, when);
  }

  @Override
  @Transactional(readOnly = true)
  public String getUnlicensedMedicationWarning(final @NonNull Locale locale)
  {
    return MedicationPreferencesUtil.getUnlicensedMedicationWarning(locale);
  }

  @Override
  @Transactional
  public void setAdministrationDoctorsComment(final @NonNull String taskId, final String doctorsComment)
  {
    medicationsTasksHandler.setAdministrationDoctorsComment(taskId, doctorsComment);
  }

  @Transactional
  @EhrSessioned
  @Override
  public void handleAdditionalWarningsAction(final @NonNull AdditionalWarningsActionDto additionalWarningsActionDto)
  {
    additionalWarningsActionHandler.handleAdditionalWarningsAction(additionalWarningsActionDto);
  }

  @Transactional
  @Override
  public List<TaskDto> findAdministrationTasks(
      final Set<String> patientIds,
      final DateTime taskDueAfter,
      final DateTime taskDueBefore)
  {
    return medicationsTasksProvider.findAdministrationTasks(patientIds, taskDueAfter, taskDueBefore);
  }

  @Override
  @Transactional(readOnly = true)
  public Long getMedicationIdForBarcode(final @NonNull String barcode)
  {
    return medicationsDao.getMedicationIdForBarcode(barcode);
  }

  @Override
  @EhrSessioned
  @Transactional(readOnly = true)
  public BarcodeTaskSearchDto getAdministrationTaskForBarcode(
      final @NonNull String patientId,
      final @NonNull String medicationBarcode)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    return barcodeTaskFinder.getAdministrationTaskForBarcode(patientId, medicationBarcode, when);
  }

  @Override
  @Transactional(readOnly = true)
  public String getOriginalTherapyId(final @NonNull String patientId, final @NonNull String therapyId)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        TherapyIdUtils.parseTherapyId(therapyId).getFirst());

    return PrescriptionsEhrUtils.getOriginalTherapyId(prescription);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DispenseSourceDto> getDispenseSources()
  {
    return medicationsDao.getDispenseSources();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FormularyMedicationDto> getVmpMedications(final @NonNull String vtmId)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();

    return medicationsValueHolderProvider.getVmpMedications(vtmId, when);
  }

  @Override
  public String getNumericValue(final @NonNull String value)
  {
    return BarcodeScannerUtils.convertToNumericRepresentation(value);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasTherapyChanged(
      final @NonNull TherapyChangeType.TherapyChangeGroup group,
      final @NonNull TherapyDto therapy,
      final @NonNull TherapyDto changedTherapy,
      final @NonNull Locale locale)
  {
    return therapyChangeCalculator.hasTherapyChanged(group, therapy, changedTherapy, true, locale);
  }
}
