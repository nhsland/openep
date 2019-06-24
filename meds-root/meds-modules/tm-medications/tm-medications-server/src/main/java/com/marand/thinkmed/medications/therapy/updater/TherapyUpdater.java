package com.marand.thinkmed.medications.therapy.updater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.PartialList;
import com.marand.maf.core.exception.UserWarning;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.PharmacistReviewTaskStatusEnum;
import com.marand.thinkmed.medications.PrescriptionChangeTypeEnum;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.admission.MedicationOnAdmissionHandler;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.ehr.model.AdditionalDetails;
import com.marand.thinkmed.medications.ehr.model.Composer;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OrderDetails;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskHandler;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.task.AdministrationTaskCreateActionEnum;
import com.marand.thinkmed.medications.task.AdministrationTaskDef;
import com.marand.thinkmed.medications.task.DoctorReviewTaskDef;
import com.marand.thinkmed.medications.task.MedicationsTasksHandler;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.task.MedsTaskDef;
import com.marand.thinkmed.medications.task.SwitchToOralTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.process.dto.AbstractTaskDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDetailsEnum;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class TherapyUpdater
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsBo medicationsBo;
  private MedicationsValueHolder medicationsValueHolder;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationOnAdmissionHandler medicationOnAdmissionHandler;
  private AdministrationHandler administrationHandler;
  private PharmacistTaskHandler pharmacistTaskHandler;
  private PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler;
  private MedicationsTasksHandler medicationsTasksHandler;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationTaskCreator administrationTaskCreator;
  private ProcessService processService;
  private AdministrationProvider administrationProvider;
  private TherapyChangeCalculator therapyChangeCalculator;
  private TherapyConverter therapyConverter;
  private TherapyEhrHandler therapyEhrHandler;
  private RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setMedicationsValueHolder(final MedicationsValueHolder medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setMedicationOnAdmissionHandler(final MedicationOnAdmissionHandler medicationOnAdmissionHandler)
  {
    this.medicationOnAdmissionHandler = medicationOnAdmissionHandler;
  }

  @Autowired
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Autowired
  public void setPharmacistTaskHandler(final PharmacistTaskHandler pharmacistTaskHandler)
  {
    this.pharmacistTaskHandler = pharmacistTaskHandler;
  }

  @Autowired
  public void setMedicationsTasksHandler(final MedicationsTasksHandler medicationsTasksHandler)
  {
    this.medicationsTasksHandler = medicationsTasksHandler;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setPreparePerfusionSyringeProcessHandler(final PreparePerfusionSyringeProcessHandler preparePerfusionSyringeProcessHandler)
  {
    this.preparePerfusionSyringeProcessHandler = preparePerfusionSyringeProcessHandler;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  public List<InpatientPrescription> saveTherapies(
      final String patientId,
      final List<SaveMedicationOrderDto> medicationOrders,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final List<InpatientPrescription> savedPrescriptions = new ArrayList<>();
    final Map<String, InpatientPrescription> savedLinkedPrescriptions = new HashMap<>();

    final List<SaveMedicationOrderDto> sortedMedicationOrders = sortMedicationOrdersByLinkName(medicationOrders);
    for (final SaveMedicationOrderDto saveDto : sortedMedicationOrders)
    {
      final TherapyDto therapy = saveDto.getTherapy();
      therapy.setCompositionUid(null);

      final MedicationOrderActionEnum actionEnum = saveDto.getActionEnum();
      final String sourceId = saveDto.getSourceId();
      final TherapyChangeReasonDto changeReasonDto = saveDto.getChangeReasonDto();

      if (sourceId != null)
      {
        medicationOnAdmissionHandler.updateMedicationOnAdmissionAction(
            patientId,
            sourceId,
            actionEnum,
            changeReasonDto,
            when);
      }

      if (actionEnum != MedicationOrderActionEnum.SUSPEND_ADMISSION)
      {
        final InpatientPrescription prescription = buildInpatientPrescription(
            therapy,
            actionEnum,
            changeReasonDto,
            centralCaseId,
            careProviderId,
            prescriber,
            when,
            locale);

        final String linkName = therapy.getLinkName();
        if (linkName != null)
        {
          createTherapyFollowLink(
              patientId,
              prescription,
              linkName,
              saveDto.getLinkCompositionUid(),
              savedLinkedPrescriptions);
        }

        if (sourceId != null)
        {
          prescription.getLinks().add(
              LinksEhrUtils.createLink(
                  sourceId,
                  EhrLinkType.MEDICATION_ON_ADMISSION.getName(),
                  EhrLinkType.MEDICATION_ON_ADMISSION));
        }

        final InpatientPrescription savedPrescription = medicationsOpenEhrDao.saveNewInpatientPrescription(
            patientId,
            prescription);

        final String therapyId = TherapyIdUtils.createTherapyId(savedPrescription.getUid());
        createReminders(therapy, therapyId, therapy.getReviewReminderDays(), patientId, locale);

        final boolean activeTherapy = actionEnum == MedicationOrderActionEnum.PRESCRIBE
            || actionEnum == MedicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER
            || actionEnum == MedicationOrderActionEnum.EDIT;
        if (activeTherapy)
        {
          final List<TaskDto> tasks = createTherapyTasks(
              patientId,
              savedPrescription,
              AdministrationTaskCreateActionEnum.PRESCRIBE,
              null,
              when);

          if (actionEnum == MedicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER)
          {
            confirmFirstAdministrationTask(patientId, savedPrescription, tasks, when);
          }
        }

        if (linkName != null)
        {
          savedLinkedPrescriptions.put(linkName, savedPrescription);
        }

        savedPrescriptions.add(savedPrescription);
      }
    }
    return savedPrescriptions;
  }

  public void confirmFirstAdministrationTask(
      final String patientId,
      final InpatientPrescription prescription,
      final List<TaskDto> tasks,
      final DateTime currentTime)
  {
    tasks.stream()
        .min(Comparator.comparing(TaskDto::getDueTime))
        .ifPresent(t -> administrationHandler.confirmAdministrationTask(
            patientId,
            prescription,
            t,
            null,
            null,
            currentTime));
  }

  private void createReminders(
      final TherapyDto therapyDto,
      final String therapyId,
      final Integer reviewReminderDays,
      final String patientId,
      final Locale locale)
  {
    final Long mainMedicationId = therapyDto.getMainMedicationId();
    final MedicationDataDto medicationDataDto = medicationsValueHolder.getMedications().get(mainMedicationId);

    final DateTime therapyStart = therapyDto.getStart();

    if (medicationDataDto != null && medicationDataDto.isSuggestSwitchToOral())
    {
      if (therapyDto.getRoutes().size() == 1 && therapyDto.getRoutes().get(0).getType() == MedicationRouteTypeEnum.IV)
      {
        final DateTime dueDate = therapyStart.withTimeAtStartOfDay()
            .plusDays(2);       //TODO TMC-7170 antibiotic - from preference (care provider)
        final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
            SwitchToOralTaskDef.INSTANCE,
            SwitchToOralTaskDef.INSTANCE.buildKey(String.valueOf(patientId)),
            "Switch to oral medication " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
            "Switch to oral medication " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
            TherapyAssigneeEnum.DOCTOR.name(),
            dueDate,
            null,
            Pair.of(MedsTaskDef.PATIENT_ID, patientId),
            Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, therapyId)
        );

        processService.createTasks(taskRequest);
      }
    }
    if (reviewReminderDays != null)
    {
      final DateTime dueDate = therapyStart.withTimeAtStartOfDay().plusDays(reviewReminderDays);
      final NewTaskRequestDto taskRequest = new NewTaskRequestDto(
          DoctorReviewTaskDef.INSTANCE,
          DoctorReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)),
          "Doctor review task " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
          "Doctor review task " + dueDate.toString(DateTimeFormatters.shortDate(locale)),
          TherapyAssigneeEnum.DOCTOR.name(),
          dueDate,
          null,
          Pair.of(MedsTaskDef.PATIENT_ID, patientId),
          Pair.of(TherapyTaskDef.ORIGINAL_THERAPY_ID, therapyId),
          Pair.of(DoctorReviewTaskDef.COMMENT, therapyDto.getReviewReminderComment())
      );
      processService.createTasks(taskRequest);
    }
  }

  private InpatientPrescription buildInpatientPrescription(
      final TherapyDto therapy,
      final MedicationOrderActionEnum actionEnum,
      final TherapyChangeReasonDto changeReasonDto,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final DateTime when,
      final Locale locale)
  {
    final InpatientPrescription prescription = new InpatientPrescription();

    if (therapy.getTherapyDescription() == null)
    {
      therapyDisplayProvider.fillDisplayValues(therapy, true, locale);
    }

    prescription.setMedicationOrder(therapyConverter.convertToMedicationOrder(therapy));
    MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.SCHEDULE, prescriber, when);

    final boolean therapyStartsWhenAnotherTherapyEnds = therapy.getLinkName() != null
        && (therapy.getLinkName().length() > 2 || !therapy.getLinkName().endsWith("1"));

    if (!therapyStartsWhenAnotherTherapyEnds)
    {
      final DateTime therapyStart = therapy.getStart().isAfter(when) ? therapy.getStart() : when;
      MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.START, prescriber, therapyStart);
    }

    if (actionEnum == MedicationOrderActionEnum.SUSPEND)
    {
      final NamedExternalDto user = new NamedExternalDto(RequestUser.getId(), RequestUser.getFullName());
      MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.SUSPEND, user, changeReasonDto, when);
    }
    else if (actionEnum == MedicationOrderActionEnum.ABORT)
    {
      MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.ABORT, prescriber, changeReasonDto, when);
    }

    addContext(prescription, centralCaseId, careProviderId, when);

    return prescription;
  }

  private void createTherapyFollowLink(
      final String patientId,
      final InpatientPrescription inpatientPrescription,
      final String linkName,
      final String linkCompositionUid,
      final Map<String, InpatientPrescription> savedTherapiesWithLinks)
  {
    final String previousLink = LinksEhrUtils.getPreviousLinkName(linkName);

    final InpatientPrescription linkOrder =
        linkCompositionUid == null
        ? savedTherapiesWithLinks.get(previousLink)
        : medicationsOpenEhrDao.loadInpatientPrescription(patientId, linkCompositionUid);

    // link: linkInstruction <- instruction
    if (linkOrder != null)
    {
      inpatientPrescription.getLinks().add(LinksEhrUtils.createLink(linkOrder.getUid(), linkName, EhrLinkType.FOLLOW));
    }
  }

  private List<SaveMedicationOrderDto> sortMedicationOrdersByLinkName(final List<SaveMedicationOrderDto> medicationOrders)
  {
    final List<SaveMedicationOrderDto> sortedOrders = new ArrayList<>(medicationOrders);
    sortedOrders.sort(
        (therapy1, therapy2) ->
        {
          if (therapy1.getTherapy().getLinkName() != null && therapy2.getTherapy().getLinkName() != null)
          {
            return therapy1.getTherapy().getLinkName().compareTo(therapy2.getTherapy().getLinkName());
          }
          if (therapy1.getTherapy().getLinkName() != null)
          {
            return 1;
          }
          if (therapy2.getTherapy().getLinkName() != null)
          {
            return -1;
          }
          return Integer.compare(medicationOrders.indexOf(therapy1), medicationOrders.indexOf(therapy2));
        });
    return sortedOrders;
  }

  public void modifyTherapy(
      final String patientId,
      final TherapyDto modifiedTherapy,
      final TherapyChangeReasonDto changeReason,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final boolean therapyAlreadyStarted,
      final String basedOnPharmacyReviewId,
      final DateTime when,
      final Locale locale)
  {
    medicationsBo.fillDisplayValues(modifiedTherapy, null, null, false, locale);

    final InpatientPrescription oldInpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        modifiedTherapy.getCompositionUid());

    final TherapyDto oldTherapy = medicationsBo.convertMedicationOrderToTherapyDto(
        oldInpatientPrescription,
        oldInpatientPrescription.getMedicationOrder(),
        null,
        null,
        false,
        locale);

    final List<TherapyChangeDto<?, ?>> changes = therapyChangeCalculator.calculateTherapyChanges(
        oldTherapy,
        modifiedTherapy,
        true,
        locale);

    final boolean significantChange = changes
        .stream()
        .anyMatch(c -> TherapyChangeType.TherapyChangeGroup.REQUIRES_NEW_THERAPY.getChanges().contains(c.getType()));

    final boolean startChange = changes
        .stream()
        .anyMatch(c -> c.getType() == TherapyChangeType.START);

    final InpatientPrescription newInpatientPrescription =
        modifyTherapy(
            patientId,
            modifiedTherapy,
            changeReason,
            centralCaseId,
            careProviderId,
            prescriber,
            therapyAlreadyStarted,
            significantChange,
            when,
            locale);

    final boolean basedOnLinksRemoved = PrescriptionsEhrUtils.removeLinksOfType(
        newInpatientPrescription,
        EhrLinkType.BASED_ON);

    // generate tasks

    final AdministrationTaskCreateActionEnum action;
    if (PrescriptionsEhrUtils.isTherapySuspended(oldInpatientPrescription))
    {
      action = AdministrationTaskCreateActionEnum.REISSUE;
    }
    else if (when.isBefore(therapyEhrHandler.getOriginalTherapyStart(patientId, oldInpatientPrescription)))
    {
      action = AdministrationTaskCreateActionEnum.MODIFY_BEFORE_START;
    }
    else
    {
      action = AdministrationTaskCreateActionEnum.MODIFY;
    }

    final boolean startChangeOnNotStartedTherapy = !therapyAlreadyStarted && startChange;
    if (startChangeOnNotStartedTherapy || significantChange || action == AdministrationTaskCreateActionEnum.REISSUE)
    {
      final DateTime newTherapyStart = modifiedTherapy.getStart();
      final DateTime deleteTasksFrom = when.isBefore(newTherapyStart) ? when : newTherapyStart;
      deleteTherapyAdministrationTasks(
          patientId,
          TherapyIdUtils.createTherapyId(modifiedTherapy.getCompositionUid(), modifiedTherapy.getEhrOrderName()),
          oldInpatientPrescription,
          false,
          false,
          deleteTasksFrom,
          when);

      createTherapyTasks(patientId, newInpatientPrescription, action, null, when);
    }

    if (basedOnPharmacyReviewId == null)
    {
      pharmacistTaskHandler.handleReviewTaskOnTherapiesChange(
          patientId,
          null,
          when,
          prescriber != null ? prescriber.getName() : null,
          when,
          PrescriptionChangeTypeEnum.ADDITION_TO_EXISTING_PRESCRIPTION,
          PharmacistReviewTaskStatusEnum.PENDING);

      if (basedOnLinksRemoved)
      {
        medicationsOpenEhrDao.modifyComposition(patientId, newInpatientPrescription);
      }
    }
    else
    {
      final PharmacyReviewReport report = medicationsOpenEhrDao.loadPharmacistsReviewReport(
          patientId,
          basedOnPharmacyReviewId);

      final Link linkToPharmacyReview = LinksEhrUtils.createLink(
          report.getUid(),
          "based on pharmacy review",
          EhrLinkType.BASED_ON);

      newInpatientPrescription.getLinks().add(linkToPharmacyReview);

      medicationsOpenEhrDao.modifyComposition(patientId, newInpatientPrescription);
    }
  }

  private <M extends TherapyDto> InpatientPrescription modifyTherapy(
      final String patientId,
      final M therapy,
      final TherapyChangeReasonDto changeReason,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final boolean therapyAlreadyStarted,
      final boolean significantChange,
      final DateTime when,
      final Locale locale)
  {
    final DateTime updateTime = new DateTime(when.withSecondOfMinute(0).withMillisOfSecond(0));
    therapyDisplayProvider.fillDisplayValues(therapy, true, locale);

    final InpatientPrescription oldPrescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, therapy.getCompositionUid());
    final MedicationOrder oldMedicationOrder = oldPrescription.getMedicationOrder();

    final DateTime oldTherapyStart = DataValueUtils.getDateTime(oldMedicationOrder.getOrderDetails().getOrderStartDateTime());

    final InpatientPrescription savedPrescription;

    if (therapyAlreadyStarted && significantChange)
    {
      //create new composition
      final InpatientPrescription newPrescription = new InpatientPrescription();
      final MedicationOrder newMedicationOrder = therapyConverter.convertToMedicationOrder(therapy);
      newPrescription.setMedicationOrder(newMedicationOrder);

      //link new composition to old composition
      final Link linkToExisting = LinksEhrUtils.createLink(oldPrescription.getUid(), "update", EhrLinkType.UPDATE);
      newPrescription.getLinks().add(linkToExisting);

      //link new composition to first composition
      linkToOriginInstruction(oldPrescription, newPrescription);

      //new therapy must keep FOLLOW links from old therapy
      final List<Link> followLinks = LinksEhrUtils.getLinksOfType(oldPrescription.getLinks(), EhrLinkType.FOLLOW);
      newPrescription.getLinks().addAll(followLinks);

      //new therapy must keep links to medication on ADMISSION
      final List<Link> admissionLinks = LinksEhrUtils.getLinksOfType(
          oldPrescription.getLinks(),
          EhrLinkType.MEDICATION_ON_ADMISSION);
      newPrescription.getLinks().addAll(admissionLinks);

      rewriteSelfAdministeringType(newMedicationOrder, oldMedicationOrder);

      //add SCHEDULE and START actions
      final DateTime therapyStart = therapy.getStart().isAfter(when) ? therapy.getStart() : when;
      MedicationsEhrUtils.addMedicationActionTo(newPrescription, MedicationActionEnum.SCHEDULE, prescriber, when);
      MedicationsEhrUtils.addMedicationActionTo(newPrescription, MedicationActionEnum.START, prescriber, therapyStart);

      addContext(newPrescription, centralCaseId, careProviderId, when);

      final String newCompositionUid = medicationsOpenEhrDao.saveNewInpatientPrescription(patientId, newPrescription).getUid();
      newPrescription.setUid(newCompositionUid);
      newMedicationOrder.setName(DataValueUtils.getText("Medication order"));

      //fix FOLLOW links that point to this therapy
      fixLinksToTherapy(patientId, oldPrescription, newPrescription, EhrLinkType.FOLLOW);

      //old composition
      final DateTime oldCompositionStopDate = updateTime.isBefore(therapy.getStart()) ? updateTime : therapy.getStart();

      if (therapy.getStart().isBefore(oldTherapyStart))
      {
        throw new UserWarning("Cannot edit therapy in the past.");
      }

      oldMedicationOrder.getOrderDetails().setOrderStopDateTime(DataValueUtils.getDateTime(oldCompositionStopDate));

      //only when client time is not synced
      if (oldCompositionStopDate.isBefore(oldTherapyStart))
      {
        oldMedicationOrder.getOrderDetails().setOrderStartDateTime(DataValueUtils.getDateTime(oldCompositionStopDate));
      }

      MedicationsEhrUtils.addMedicationActionTo(oldPrescription, MedicationActionEnum.COMPLETE, prescriber, changeReason, when);

      medicationsOpenEhrDao.modifyComposition(patientId, oldPrescription);
      savedPrescription = newPrescription;
    }
    else
    {
      oldPrescription.setMedicationOrder(therapyConverter.convertToMedicationOrder(therapy));

      if (therapyAlreadyStarted) // and insignificantChange
      {
        oldPrescription.getMedicationOrder()
            .getOrderDetails()
            .setOrderStartDateTime(DataValueUtils.getDateTime(oldTherapyStart));
      }
      else // fix start
      {
        final MedicationManagement startAction = oldPrescription.getActions().stream()
            .filter(a -> MedicationActionEnum.getActionEnum(a) == MedicationActionEnum.START)
            .findFirst()
            .orElse(null);

        Preconditions.checkNotNull(startAction, "no start action exist");
        startAction.setTime(DataValueUtils.getDateTime(updateTime));
      }

      final boolean therapySuspended = PrescriptionsEhrUtils.isTherapySuspended(oldPrescription);
      if (therapySuspended)
      {
        MedicationsEhrUtils.addMedicationActionTo(oldPrescription, MedicationActionEnum.REISSUE, prescriber, when);
      }

      MedicationsEhrUtils.addMedicationActionTo(
          oldPrescription,
          MedicationActionEnum.MODIFY_EXISTING,
          prescriber,
          changeReason,
          when);

      medicationsOpenEhrDao.modifyComposition(patientId, oldPrescription);
      savedPrescription = oldPrescription;
    }

    // handle tasks
    final String savedTherapyId = TherapyIdUtils.createTherapyId(savedPrescription.getUid());
    handleTherapyTasksOnModify(patientId, therapy, savedPrescription, savedTherapyId, when);

    return savedPrescription;
  }

  private void handleTherapyTasksOnModify(
      final String patientId,
      final TherapyDto therapy,
      final InpatientPrescription prescription,
      final String therapyId,
      final DateTime when)
  {
    final List<String> taskKeys = new ArrayList<>();
    taskKeys.add(DoctorReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
    taskKeys.add(SwitchToOralTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));

    //noinspection unchecked
    final PartialList<TaskDto> tasks = processService.findTasks(
        TherapyAssigneeEnum.DOCTOR.name(),
        null,
        null,
        false,
        null,
        null,
        taskKeys,
        EnumSet.of(TaskDetailsEnum.VARIABLES));

    final List<Link> originLinks = LinksEhrUtils.getLinksOfType(prescription.getLinks(), EhrLinkType.ORIGIN);

    final String originalTherapyId = originLinks.isEmpty() ? therapyId : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0));

    if (tasks != null)
    {
      for (final TaskDto task : tasks)
      {
        final String taskTherapyId = (String)task.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
        if (originalTherapyId.equals(taskTherapyId))
        {
          if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.DOCTOR_REVIEW.getName()))
          {
            if (!when.withTimeAtStartOfDay().isBefore(task.getDueTime().withTimeAtStartOfDay()))
            {
              processService.completeTasks(task.getId());
            }
          }
          else
          {
            if (task.getTaskExecutionStrategyId().equals(TaskTypeEnum.SWITCH_TO_ORAL.getName()))
            {
              final boolean ivRouteExists = therapy.getRoutes()
                  .stream()
                  .anyMatch(route -> route.getType() == MedicationRouteTypeEnum.IV);

              if (!ivRouteExists)
              {
                processService.completeTasks(task.getId());
              }
            }
          }
        }
      }
    }
  }

  private void rewriteSelfAdministeringType(final MedicationOrder newOrder, final MedicationOrder oldOrder)
  {
    final DvCodedText selfAdministrationType = oldOrder.getAdditionalDetails().getSelfAdministrationType();
    if (selfAdministrationType != null)
    {
      newOrder.getAdditionalDetails().setSelfAdministrationType(selfAdministrationType);
      newOrder.getAdditionalDetails().setSelfAdministrationStart(oldOrder.getAdditionalDetails().getSelfAdministrationStart());
    }
  }

  private void linkToOriginInstruction(final InpatientPrescription oldPrescription, final InpatientPrescription newPrescription)
  {
    Link originLink = null;
    boolean oldInstructionIsFirstInstruction = true;
    for (final Link link : oldPrescription.getLinks())
    {
      if (link.getType().getValue().equals(EhrLinkType.ORIGIN.getName()))
      {
        originLink = link;
      }
      if (link.getType().getValue().equals(EhrLinkType.UPDATE.getName()))
      {
        oldInstructionIsFirstInstruction = false;
      }
    }
    if (oldInstructionIsFirstInstruction)
    {
      final Link linkToFirst = LinksEhrUtils.createLink(
          oldPrescription.getUid(),
          "origin",
          EhrLinkType.ORIGIN);
      newPrescription.getLinks().add(linkToFirst);
    }
    else if (originLink != null)
    {
      newPrescription.getLinks().add(originLink);
    }
  }

  private void fixLinksToTherapy(
      final String patientId,
      final InpatientPrescription oldComposition,
      final InpatientPrescription newComposition,
      final EhrLinkType linkType)
  {
    final List<InpatientPrescription> linkedTherapies = medicationsOpenEhrDao.getLinkedPrescriptions(
        patientId,
        oldComposition.getUid(),
        linkType);

    if (!linkedTherapies.isEmpty())
    {
      final InpatientPrescription linkedTherapy = linkedTherapies.get(0);
      Link linkToFix = null;
      for (final Link link : linkedTherapy.getLinks())
      {
        if (link.getType().getValue().equals(linkType.getName()))
        {
          linkToFix = link;
          break;
        }
      }
      linkedTherapy.getLinks().remove(linkToFix);

      if (linkToFix != null)
      {
        final Link link = LinksEhrUtils.createLink(
            newComposition.getUid(),
            linkToFix.getMeaning().getValue(),
            linkType);
        linkedTherapy.getLinks().add(link);
        medicationsOpenEhrDao.modifyComposition(patientId, linkedTherapy);
      }
    }
  }

  public String abortTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto stopReason,
      final DateTime when)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);

    final String newCompositionUid = abortTherapy(patientId, prescription, stopReason, when);
    handleTasksOnTherapyStop(patientId, compositionUid, ehrOrderName, true, false, when);

    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, compositionUid);
    cancelTherapyRelatedTasks(patientId, originalTherapyId);

    return newCompositionUid;
  }

  private void cancelTherapyRelatedTasks(final String patientId, final String originalTherapyId)
  {
    preparePerfusionSyringeProcessHandler.handleTherapyCancellationMessage(patientId, originalTherapyId);

    medicationsTasksHandler.deleteTherapyTasksOfType(
        patientId,
        EnumSet.of(TaskTypeEnum.PHARMACIST_REMINDER, TaskTypeEnum.PHARMACIST_REVIEW),
        RequestUser.getId(),
        originalTherapyId);
  }

  private void handleTasksOnTherapyStop(
      final String patientId,
      final String compositionUid,
      final String instructionName,
      final boolean completePreviousTasks,
      final boolean suspend,
      final DateTime when)
  {
    final String therapyId = TherapyIdUtils.createTherapyId(compositionUid, instructionName);

    final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        compositionUid);

    deleteTherapyAdministrationTasks(
        patientId,
        therapyId,
        inpatientPrescription,
        completePreviousTasks,
        true,
        when,
        when);

    final MedicationOrder medicationOrder = inpatientPrescription.getMedicationOrder();
    final List<Link> originLinks = LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), EhrLinkType.ORIGIN);

    medicationsTasksHandler.deleteTherapyTasksOfType(
        patientId,
        EnumSet.of(
            TaskTypeEnum.SUPPLY_REMINDER,
            TaskTypeEnum.DISPENSE_MEDICATION,
            TaskTypeEnum.SUPPLY_REVIEW,
            TaskTypeEnum.PHARMACIST_REMINDER,
            TaskTypeEnum.SWITCH_TO_ORAL,
            TaskTypeEnum.INFUSION_BAG_CHANGE_TASK,
            TaskTypeEnum.DOCTOR_REVIEW),
        RequestUser.getId(),
        originLinks.isEmpty() ? therapyId : TherapyIdUtils.getTherapyIdFromLink(originLinks.get(0)));

    final TherapyDto therapyDto = medicationsBo.convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        medicationOrder,
        null,
        null,
        false,
        null);

    final boolean continuousInfusion = therapyDto instanceof ComplexTherapyDto && ((ComplexTherapyDto)therapyDto).isContinuousInfusion();
    final boolean therapyWithRate = therapyDto.isWithRate();
    final boolean oxygenTherapy = therapyDto instanceof OxygenTherapyDto;

    if (continuousInfusion || therapyWithRate || oxygenTherapy)
    {
      if (suspend || !PrescriptionsEhrUtils.isTherapySuspended(inpatientPrescription))
      {
        final DateTime originalTherapyStart = therapyEhrHandler.getOriginalTherapyStart(patientId, inpatientPrescription);
        final boolean therapyAlreadyStarted = originalTherapyStart.isBefore(when);
        //noinspection OverlyComplexBooleanExpression
        if ((oxygenTherapy || continuousInfusion) && therapyAlreadyStarted 
            || isLastAdministrationNotStop(patientId, when, therapyId, inpatientPrescription))
        {
          createSingleAdministrationTask(therapyDto, patientId, AdministrationTypeEnum.STOP, when, null);
        }
      }
    }
  }

  private boolean isLastAdministrationNotStop(
      final String patientId,
      final DateTime until,
      final String therapyId,
      final InpatientPrescription prescription)
  {
    final Interval interval = Intervals.infiniteTo(until);

    final Opt<AdministrationTaskDto> lastTask = medicationsTasksProvider.findLastAdministrationTaskForTherapy(
        patientId,
        therapyId,
        interval,
        false);

    final Opt<AdministrationDto> lastGivenAdministration = Opt.from(
        administrationProvider.getPrescriptionsAdministrations(
            patientId,
            Collections.singletonList(prescription),
            interval,
            true)
            .stream()
            .filter(a -> AdministrationResultEnum.ADMINISTERED.contains(a.getAdministrationResult()))
            .max(Comparator.comparing(AdministrationDto::getAdministrationTime)));

    final boolean administrationNotStop = Opt.resolve(() -> lastGivenAdministration.get().getAdministrationType())
        .map(AdministrationTypeEnum.NOT_STOP::contains)
        .orElse(false);

    final boolean taskNotStop = Opt.resolve(() -> lastTask.get().getAdministrationTypeEnum())
        .map(AdministrationTypeEnum.NOT_STOP::contains)
        .orElse(false);

    if (lastTask.isPresent())
    {
      if (lastGivenAdministration.isPresent())
      {
        if (lastGivenAdministration.get().getAdministrationTime().isAfter(lastTask.get().getPlannedAdministrationTime()))
        {
          return administrationNotStop;
        }
        return taskNotStop;
      }
      return taskNotStop;
    }
    return administrationNotStop;
  }

  private void deleteTherapyAdministrationTasks(
      final String patientId,
      final String therapyId,
      final InpatientPrescription prescription,
      final boolean completePastTasks,
      final boolean therapyStop,
      final DateTime fromDate,
      final DateTime when)
  {
    // find all update therapyIds and delete tasks for all of them

    final List<String> therapyIds = therapyEhrHandler.getAllPreviousTherapyIds(patientId, therapyId);
    final List<TaskDto> tasks = medicationsTasksProvider.findAdministrationTasks(
        patientId,
        therapyIds,
        null,
        null,
        null,
        false);

    final List<String> futureTasksIds = new ArrayList<>();
    for (final TaskDto task : tasks)
    {
      final DateTime taskTimestamp = task.getDueTime();
      if (taskTimestamp.isAfter(fromDate) || taskTimestamp.equals(fromDate))
      {
        futureTasksIds.add(task.getId());
      }
      else if (completePastTasks)
      {
        final AdministrationDto administrationDto;
        final AdministrationTypeEnum administrationTypeEnum = getAdministrationType(task);
        if (administrationTypeEnum == AdministrationTypeEnum.START)
        {
          administrationDto = new StartAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((StartAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.ADJUST_INFUSION)
        {
          administrationDto = new AdjustInfusionAdministrationDto();
          final TherapyDoseDto therapyDoseDto = TherapyTaskUtils.buildTherapyDoseDtoFromTask(task);
          ((AdjustInfusionAdministrationDto)administrationDto).setPlannedDose(therapyDoseDto);
        }
        else if (administrationTypeEnum == AdministrationTypeEnum.STOP)
        {
          administrationDto = new StopAdministrationDto();
        }
        else
        {
          throw new IllegalArgumentException("Inconsistent AdministrationTypeEnum.");
        }

        administrationDto.setTaskId(task.getId());
        administrationDto.setPlannedTime(taskTimestamp);
        administrationDto.setAdministrationTime(taskTimestamp);
        administrationDto.setNotAdministeredReason(NotAdministeredReasonEnum.NOT_RECORDED.toCodedName());

        final String administrationUid = administrationHandler.confirmTherapyAdministration(
            prescription,
            patientId,
            RequestUser.getId(),
            administrationDto,
            AdministrationResultEnum.NOT_GIVEN,
            false,
            null,
            null,
            when);

        medicationsTasksHandler.associateTaskWithAdministration(administrationDto.getTaskId(), administrationUid);
        processService.completeTasks(administrationDto.getTaskId());
      }
    }

    if (!therapyStop)
    {
      final List<String> activeStopOrAdjustTaskIdsWithGivenStart = getActiveStopOrAdjustTaskIdsWithGivenStart(tasks);
      futureTasksIds.removeAll(activeStopOrAdjustTaskIdsWithGivenStart);
    }

    if (!futureTasksIds.isEmpty())
    {
      processService.deleteTasks(futureTasksIds);
    }
  }

  private List<String> getActiveStopOrAdjustTaskIdsWithGivenStart(final List<TaskDto> tasks)
  {
    final Map<String, List<TaskDto>> groupedTasks = tasks
        .stream()
        .filter(t -> t.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName()) != null)
        .collect(Collectors.groupingBy(t -> (String)t.getVariables().get(AdministrationTaskDef.GROUP_UUID.getName())));

    final List<String> stopTaskIds = new ArrayList<>();
    for (final Map.Entry<String, List<TaskDto>> entry : groupedTasks.entrySet())
    {
      if (entry.getValue().stream().noneMatch(t -> getAdministrationType(t) == AdministrationTypeEnum.START))
      {
        entry.getValue()
            .stream()
            .filter(t -> getAdministrationType(t) == AdministrationTypeEnum.STOP || getAdministrationType(t) == AdministrationTypeEnum.ADJUST_INFUSION)
            .findAny()
            .map(AbstractTaskDto::getId)
            .ifPresent(stopTaskIds::add);
      }
    }
    return stopTaskIds;
  }

  private AdministrationTypeEnum getAdministrationType(final TaskDto t)
  {
    return AdministrationTypeEnum.valueOf((String)t.getVariables().get(AdministrationTaskDef.ADMINISTRATION_TYPE.getName()));
  }

  private String abortTherapy(
      final String patientId,
      final InpatientPrescription prescription,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final OrderDetails orderDetails = prescription.getMedicationOrder().getOrderDetails();
    final DateTime medicationTimingStart = DataValueUtils.getDateTime(orderDetails.getOrderStartDateTime());
    final DateTime medicationTimingEnd = DataValueUtils.getDateTime(orderDetails.getOrderStopDateTime());

    // medication not started yet
    final NamedExternalDto user = new NamedExternalDto(RequestUser.getId(), RequestUser.getFullName());
    if (medicationTimingStart.isAfter(when))
    {
      orderDetails.setOrderStartDateTime(DataValueUtils.getDateTime(when));
      orderDetails.setOrderStopDateTime(DataValueUtils.getDateTime(when));

      MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.CANCEL, user, changeReason, when);
    }
    else // medication already started
    {
      if (medicationTimingEnd == null || medicationTimingEnd.isAfter(when))
      {
        orderDetails.setOrderStopDateTime(DataValueUtils.getDateTime(when));
      }

      MedicationsEhrUtils.addMedicationActionTo(prescription, MedicationActionEnum.ABORT, user, changeReason, when);
    }

    return medicationsOpenEhrDao.modifyComposition(patientId, prescription);
  }

  private void addContext(
      final InpatientPrescription composition,
      final String centralCaseId,
      final String careProviderId,
      final DateTime when)
  {
    new EhrContextVisitor(composition)
        .withCareProvider(careProviderId)
        .withCentralCaseId(centralCaseId)
        .withComposer(RequestUser.getId(), RequestUser.getFullName())
        .withStartTime(when)
        .visit();
  }

  public String suspendTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final String newCompositionUid = addMedicationActionAndSaveComposition(
        patientId,
        compositionUid,
        MedicationActionEnum.SUSPEND,
        changeReason,
        when);

    handleTasksOnTherapyStop(patientId, compositionUid, ehrOrderName, false, true, when);

    return newCompositionUid;
  }

  private String addMedicationActionAndSaveComposition(
      final String patientId,
      final String compositionUid,
      final MedicationActionEnum actionEnum,
      final TherapyChangeReasonDto changeReason,
      final DateTime when)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);
    final NamedExternalDto user = new NamedExternalDto(RequestUser.getId(), RequestUser.getFullName());
    MedicationsEhrUtils.addMedicationActionTo(prescription, actionEnum, user, changeReason, when);

    return medicationsOpenEhrDao.modifyComposition(patientId, prescription);
  }

  public String reissueTherapy(
      final String patientId,
      final String compositionUid,
      final String ehrOrderName,
      final DateTime when)
  {
    final InpatientPrescription prescription = medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid);

    final NamedExternalDto user = new NamedExternalDto(RequestUser.getId(), RequestUser.getFullName());
    MedicationsEhrUtils.addMedicationActionTo(
        prescription,
        MedicationActionEnum.REISSUE,
        user,
        when);

    final String newCompositionUid = medicationsOpenEhrDao.modifyComposition(patientId, prescription);

    final DateTime lastTaskTimestamp = medicationsTasksProvider.findLastAdministrationTaskTimeForTherapy(
        patientId,
        TherapyIdUtils.createTherapyId(compositionUid, ehrOrderName),
        Intervals.infiniteFrom(when.minusDays(30)),
        true)
        .orElse(when);

    createTherapyTasks(
        patientId,
        medicationsOpenEhrDao.loadInpatientPrescription(patientId, compositionUid),
        AdministrationTaskCreateActionEnum.REISSUE,
        lastTaskTimestamp,
        when);

    return newCompositionUid;
  }

  public String reviewTherapy(final String patientId, final String compositionUid)
  {
    return addMedicationActionAndSaveComposition(
        patientId,
        compositionUid,
        MedicationActionEnum.REVIEW,
        null,
        requestDateTimeHolder.getRequestTimestamp());
  }

  public boolean startLinkedTherapy(final String patientId, final InpatientPrescription linkedPrescription, final DateTime start)
  {
    final boolean therapyCancelledOrAborted = PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(linkedPrescription);
    if (!therapyCancelledOrAborted)
    {
      final Composer composer = linkedPrescription.getComposer();
      final NamedExternalDto composersName = new NamedExternalDto(composer.getId(), composer.getName());

      MedicationsEhrUtils.addMedicationActionTo(linkedPrescription, MedicationActionEnum.START, composersName, start);

      moveTherapyInterval(patientId, linkedPrescription, start, false);
      medicationsOpenEhrDao.modifyComposition(patientId, linkedPrescription);

      return true;
    }
    return false;
  }

  public void updateTherapySelfAdministeringStatus(
      final String patientId,
      final InpatientPrescription prescription,
      final SelfAdministeringActionEnum selfAdministeringActionEnum,
      final String userId,
      final DateTime when)
  {
    final AdditionalDetails additionalDetails = prescription.getMedicationOrder().getAdditionalDetails();
    additionalDetails.setSelfAdministrationType(selfAdministeringActionEnum.getDvCodedText());
    additionalDetails.setSelfAdministrationStart(DataValueUtils.getDateTime(when));

    medicationsOpenEhrDao.modifyComposition(patientId, prescription);
  }

  private void moveTherapyInterval(
      final String patientId,
      final InpatientPrescription prescription,
      final DateTime startTimestamp,
      final boolean save)
  {
    DateTime endDate = null;

    final OrderDetails orderDetails = prescription.getMedicationOrder().getOrderDetails();

    if (orderDetails.getOrderStopDateTime() != null)
    {
      final DateTime start = DataValueUtils.getDateTime(orderDetails.getOrderStartDateTime());
      final DateTime end = DataValueUtils.getDateTime(orderDetails.getOrderStopDateTime());
      final long durationInMillis = end.getMillis() - start.getMillis();
      final DateTime newEnd = new DateTime(startTimestamp.getMillis() + durationInMillis);
      orderDetails.setOrderStopDateTime(DataValueUtils.getDateTime(newEnd));
      endDate = newEnd;
    }
    orderDetails.setOrderStartDateTime(DataValueUtils.getDateTime(startTimestamp));

    if (save)
    {
      medicationsOpenEhrDao.modifyComposition(patientId, prescription);
    }
    if (endDate != null)
    {
      final List<InpatientPrescription> linkedPrescriptions = medicationsOpenEhrDao.getLinkedPrescriptions(
          patientId,
          prescription.getUid(),
          EhrLinkType.FOLLOW);

      Preconditions.checkArgument(linkedPrescriptions.size() < 2, "not more than 1 follow prescriptions should exist");

      if (!linkedPrescriptions.isEmpty())
      {
        final InpatientPrescription linkedPrescription = linkedPrescriptions.get(0);
        moveTherapyInterval(patientId, linkedPrescription, endDate, true);
      }
    }
  }

  public List<TaskDto> createTherapyTasks(
      final String patientId,
      final InpatientPrescription inpatientPrescription,
      final AdministrationTaskCreateActionEnum action,
      final DateTime lastTaskTimestamp,
      final DateTime when)
  {
    final MedicationManagement startAction = inpatientPrescription.getActions().stream()
        .filter(a -> MedicationActionEnum.getActionEnum(a) == MedicationActionEnum.START)
        .findFirst()
        .orElse(null);

    //noinspection VariableNotUsedInsideIf
    if (startAction != null)
    {
      final TherapyDto therapyDto = medicationsBo.convertMedicationOrderToTherapyDto(
          inpatientPrescription,
          inpatientPrescription.getMedicationOrder(),
          null,
          null,
          false,
          null);

      final DateTime futureConfirmedTaskTimestamp =
          action == AdministrationTaskCreateActionEnum.REISSUE
          ? medicationsOpenEhrDao.getFutureAdministrationPlannedTime(patientId, inpatientPrescription.getUid(), when)
          : null;

      final List<NewTaskRequestDto> taskRequests =
          administrationTaskCreator.createTaskRequests(
              patientId,
              therapyDto,
              action,
              when,
              lastTaskTimestamp,
              futureConfirmedTaskTimestamp);

      return Lists.newArrayList(processService.createTasks(Iterables.toArray(taskRequests, NewTaskRequestDto.class)));
    }
    return Collections.emptyList();
  }

  private void createSingleAdministrationTask(
      final TherapyDto therapy,
      final String patientId,
      final AdministrationTypeEnum administrationTypeEnum,
      final DateTime when,
      final TherapyDoseDto dose)
  {
    final NewTaskRequestDto taskRequest = administrationTaskCreator.createMedicationTaskRequest(
        patientId,
        therapy,
        administrationTypeEnum,
        when,
        dose);

    processService.createTasks(taskRequest);
  }

  public void createAdditionalAdministrationTask(
      final InpatientPrescription inpatientPrescription,
      final String patientId,
      final DateTime timestamp,
      final AdministrationTypeEnum type,
      final TherapyDoseDto dose)
  {
    final TherapyDto therapy = medicationsBo.convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        inpatientPrescription.getMedicationOrder(),
        null,
        null,
        false,
        null);

    final List<NewTaskRequestDto> requests = administrationTaskCreator.createTaskRequestsForAdditionalAdministration(
        patientId,
        therapy,
        type,
        timestamp,
        dose);

    processService.createTasks(requests.toArray(new NewTaskRequestDto[requests.size()]));
  }
}
