package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.Opt;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.administration.impl.AdministrationTaskCreator;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.pharmacist.Miscellaneous;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewSaver;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewFromEhrConverter;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewToEhrConverter;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.process.service.ProcessService;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.LocatableRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class PharmacistReviewSaverImpl implements PharmacistReviewSaver
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private ProcessService processService;
  private AdministrationTaskCreator administrationTaskCreator;
  private PharmacistReviewToEhrConverter pharmacistReviewToEhrConverter;
  private PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter;
  private MedicationsService medicationsService;
  private TherapyDisplayProvider therapyDisplayProvider;
  private TherapyConverter therapyConverter;
  private TherapyEhrHandler therapyEhrHandler;
  private RequestDateTimeHolder requestDateTimeHolder;
  private TherapyChangeCalculator therapyChangeCalculator;

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
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setAdministrationTaskCreator(final AdministrationTaskCreator administrationTaskCreator)
  {
    this.administrationTaskCreator = administrationTaskCreator;
  }

  @Autowired
  public void setPharmacistReviewToEhrConverter(final PharmacistReviewToEhrConverter pharmacistReviewToEhrConverter)
  {
    this.pharmacistReviewToEhrConverter = pharmacistReviewToEhrConverter;
  }

  @Autowired
  public void setPharmacistReviewFromEhrConverter(final PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter)
  {
    this.pharmacistReviewFromEhrConverter = pharmacistReviewFromEhrConverter;
  }

  @Autowired
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
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

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Override
  public String savePharmacistReview(
      final String patientId,
      final PharmacistReviewDto pharmacistReview,
      final Boolean authorize,
      final Locale locale)
  {
    final PharmacyReviewReport report = pharmacistReviewToEhrConverter.convert(pharmacistReview, requestDateTimeHolder.getRequestTimestamp());
    String compositionUid = medicationsOpenEhrDao.saveComposition(patientId, report, pharmacistReview.getCompositionUid());

    final boolean actionsUpdated = linkActionsToInstructions(report, compositionUid);

    if (actionsUpdated)
    {
      compositionUid = medicationsOpenEhrDao.saveComposition(patientId, report, pharmacistReview.getCompositionUid());
    }

    if (authorize != null && authorize)
    {
      authorizePatientPharmacistReviews(
          patientId,
          Collections.singletonList(compositionUid),
          locale,
          requestDateTimeHolder.getRequestTimestamp());
    }

    return compositionUid;
  }

  private boolean linkActionsToInstructions(final PharmacyReviewReport composition, final String compositionUid)
  {
    final Miscellaneous miscellaneous = composition.getMiscellaneous();
    if (miscellaneous != null && miscellaneous.getMedicationManagement() != null)
    {
      final MedicationManagement action = miscellaneous.getMedicationManagement();
      final LocatableRef actionInstructionId = action.getInstructionDetails().getInstructionId();
      if (actionInstructionId.getPath() == null)
      {
        action.getInstructionDetails()
            .setInstructionId(MedicationsEhrUtils.createMedicationOrderLocatableRef(
                compositionUid,
                PharmacyReviewReport.getMedicationOrderPath()));
        return true;
      }
    }
    return false;
  }

  @Override
  public void authorizePatientPharmacistReviews(
      final String patientId,
      final List<String> pharmacistReviewUids,
      final Locale locale,
      final DateTime when)
  {
    completePharmacistReviewTasks(patientId);

    for (final String reviewUid : pharmacistReviewUids)
    {
      final PharmacyReviewReport report = medicationsOpenEhrDao.loadPharmacistsReviewReport(patientId, reviewUid);
      report.setUid(reviewUid);
      report.getContext().setStatus(PharmacistReviewStatusEnum.FINAL.getDvText());
      medicationsOpenEhrDao.saveComposition(patientId, report, report.getUid());

      final boolean referredBackToPrescriber = Opt.resolve(() -> report.getMiscellaneous().getPrescriberReferral()).isPresent();
      if (!referredBackToPrescriber)
      {
        modifyTherapyIfSuggestedByPharmacist(patientId, locale, when, report, reviewUid);
      }
      abortTherapyIfSuggestedByPharmacist(patientId, report, locale);
      suspendTherapyIfSuggestedByPharmacist(patientId, report);
    }
  }

  private void completePharmacistReviewTasks(final String patientId)
  {
    pharmacistTaskProvider
        .findTaskIds(
            null,
            TherapyAssigneeEnum.PHARMACIST.name(),
            Collections.singleton(String.valueOf(patientId)),
            Collections.singleton(TaskTypeEnum.PHARMACIST_REVIEW))
        .forEach(processService::completeTasks);
  }

  @Override
  public void reviewPharmacistReview(
      final String patientId,
      final String pharmacistReviewUid,
      final ReviewPharmacistReviewAction reviewAction,
      final List<String> deniedReviews,
      final DateTime when,
      final Locale locale)
  {
    final PharmacyReviewReport report = medicationsOpenEhrDao.loadPharmacistsReviewReport(patientId, pharmacistReviewUid);
    pharmacistReviewToEhrConverter.setPrescriberReferralResponse(report, reviewAction, when);
    medicationsOpenEhrDao.saveComposition(patientId, report, report.getUid());

    if (deniedReviews != null)
    {
      setPrescriberReferralResponseForPharmacistReviews(ReviewPharmacistReviewAction.DENIED, deniedReviews, patientId, when);
    }

    if (reviewAction == ReviewPharmacistReviewAction.ACCEPTED)
    {
      modifyTherapyIfSuggestedByPharmacist(
          patientId,
          locale,
          requestDateTimeHolder.getRequestTimestamp(),
          report,
          pharmacistReviewUid);
    }
  }

  private void setPrescriberReferralResponseForPharmacistReviews(
      final ReviewPharmacistReviewAction reviewAction,
      final List<String> reviewIds,
      final String patientId,
      final DateTime when)
  {
    for (final String reviewId : reviewIds)
    {
      final PharmacyReviewReport report = medicationsOpenEhrDao.loadPharmacistsReviewReport(patientId, reviewId);
      pharmacistReviewToEhrConverter.setPrescriberReferralResponse(report, reviewAction, when);
      medicationsOpenEhrDao.saveComposition(patientId, report, report.getUid());
    }
  }

  private void abortTherapyIfSuggestedByPharmacist(
      final String patientId,
      final PharmacyReviewReport report,
      final Locale locale)
  {
    final InpatientPrescription prescriptionToAbort = loadReviewedPrescriptionForChangeType(
        patientId,
        report,
        Collections.singletonList(PharmacistTherapyChangeType.ABORT));

    if (prescriptionToAbort != null && !PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(prescriptionToAbort))
    {
      medicationsService.abortTherapy(
          patientId,
          prescriptionToAbort.getUid(),
          prescriptionToAbort.getMedicationOrder().getName().getValue(),
            Dictionary.getEntry("pharmacists.review", locale));
    }
  }

  private void suspendTherapyIfSuggestedByPharmacist(final String patientId, final PharmacyReviewReport report)
  {
    final InpatientPrescription prescriptionToSuspend = loadReviewedPrescriptionForChangeType(
        patientId,
        report,
        Collections.singletonList(PharmacistTherapyChangeType.SUSPEND));

    if (prescriptionToSuspend == null)
    {
      return;
    }

    final boolean therapyCancelledOrAborted = PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(prescriptionToSuspend);
    final boolean therapySuspended = PrescriptionsEhrUtils.isTherapySuspended(prescriptionToSuspend);

    if (!therapyCancelledOrAborted && !therapySuspended)
    {
      medicationsService.suspendTherapy(
          patientId,
          prescriptionToSuspend.getUid(),
          prescriptionToSuspend.getMedicationOrder().getName().getValue(), null);
    }
  }

  private void modifyTherapyIfSuggestedByPharmacist(
      final String patientId,
      final Locale locale,
      final DateTime when,
      final PharmacyReviewReport report,
      final String pharmacistReviewUid)
  {
    final InpatientPrescription linkedPrescription = loadReviewedPrescriptionForChangeType(
        patientId,
        report,
        Lists.newArrayList(PharmacistTherapyChangeType.EDIT, PharmacistTherapyChangeType.SUSPEND));

    if (linkedPrescription != null && !PrescriptionsEhrUtils.isInpatientPrescriptionCancelledOrAborted(linkedPrescription))
    {
      final TherapyDto oldTherapy = therapyConverter.convertToTherapyDto(
          linkedPrescription.getMedicationOrder(),
          linkedPrescription.getUid(),
          DataValueUtils.getDateTime(linkedPrescription.getContext().getStartTime()));
      therapyDisplayProvider.fillDisplayValues(oldTherapy, false, locale);

      final TherapyDto modifiedTherapy = therapyConverter.convertToTherapyDto(
          report.getMiscellaneous().getMedicationorder(),
          linkedPrescription.getUid(),
          DataValueUtils.getDateTime(report.getContext().getStartTime()));
      therapyDisplayProvider.fillDisplayValues(modifiedTherapy, false, locale);

      final boolean therapyWasModified = !therapyChangeCalculator.calculateTherapyChanges(
          oldTherapy,
          modifiedTherapy,
          true,
          locale).isEmpty();

      if (therapyWasModified)
      {
        if (modifiedTherapy.getStart().isBefore(when))
        {
          final DateTime nextAdministrationTime = administrationTaskCreator.calculateNextTherapyAdministrationTime(
              patientId,
              modifiedTherapy,
              false,
              requestDateTimeHolder.getRequestTimestamp());

          modifiedTherapy.setStart(nextAdministrationTime == null ? when : nextAdministrationTime);
        }

        therapyDisplayProvider.fillDisplayValues(modifiedTherapy, true, locale);
        medicationsService.modifyTherapy(
            patientId,
            modifiedTherapy,
            null,
            null,
            null,
            pharmacistReviewFromEhrConverter.extractComposer(report),
            when,
            pharmacistReviewUid,
            locale);
      }
    }
  }

  private InpatientPrescription loadReviewedPrescriptionForChangeType(
      final String patientId,
      final PharmacyReviewReport report,
      final Collection<PharmacistTherapyChangeType> changeTypes)
  {
    final Opt<MedicationOrder> order = Opt.resolve(() -> report.getMiscellaneous().getMedicationorder());
    if (order.isPresent())
    {
      final MedicationManagement action = report.getMiscellaneous().getMedicationManagement();
      if (action != null)
      {
        if (changeTypes.contains(PharmacistUtils.mapToPharmacistTherapyChangeType(action)))
        {
          return therapyEhrHandler.getPrescriptionFromLink(
              patientId,
              LinksEhrUtils.getLinksOfType(report, EhrLinkType.REVIEWED).get(0),
              true);
        }
      }
    }
    return null;
  }
}
