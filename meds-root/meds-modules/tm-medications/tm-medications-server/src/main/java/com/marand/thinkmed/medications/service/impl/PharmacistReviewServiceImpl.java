package com.marand.thinkmed.medications.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.MedicationOrderActionEnum;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.SaveMedicationOrderDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewSaver;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskCreator;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Boris Marn
 */

@Component
public class PharmacistReviewServiceImpl
{
  private final PharmacistTaskCreator pharmacistTaskCreator;
  private final PharmacistReviewSaver pharmacistReviewSaver;
  private final MedicationsOpenEhrDao medicationsOpenEhrDao;
  private final PharmacySupplyProcessHandler pharmacySupplyProcessHandler;
  private final PharmacistReviewProvider pharmacistReviewProvider;
  private final RequestDateTimeHolder requestDateTimeHolder;
  private final MedicationsServiceImpl medicationsService;

  public PharmacistReviewServiceImpl(
      final PharmacistTaskCreator pharmacistTaskCreator,
      final PharmacistReviewSaver pharmacistReviewSaver,
      final MedicationsOpenEhrDao medicationsOpenEhrDao,
      final PharmacySupplyProcessHandler pharmacySupplyProcessHandler,
      final PharmacistReviewProvider pharmacistReviewProvider,
      final RequestDateTimeHolder requestDateTimeHolder,
      final MedicationsServiceImpl medicationsService)
  {
    this.pharmacistTaskCreator = pharmacistTaskCreator;
    this.pharmacistReviewSaver = pharmacistReviewSaver;
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
    this.pharmacySupplyProcessHandler = pharmacySupplyProcessHandler;
    this.pharmacistReviewProvider = pharmacistReviewProvider;
    this.requestDateTimeHolder = requestDateTimeHolder;
    this.medicationsService = medicationsService;
  }

  @Transactional
  @EhrSessioned
  public void authorizePharmacistReviews(
      final String patientId,
      final List<String> pharmacistReviewUids,
      final Locale locale)
  {
    final DateTime when = requestDateTimeHolder.getRequestTimestamp();
    pharmacistReviewSaver.authorizePatientPharmacistReviews(patientId, pharmacistReviewUids, locale, when);
  }

  @Transactional
  @EhrSessioned
  public void deletePharmacistReview(final String patientId, final String pharmacistReviewUid)
  {
    medicationsOpenEhrDao.deleteComposition(patientId, pharmacistReviewUid);
  }

  @Transactional
  @EhrSessioned
  @EventProducer(MedicationsServiceEvents.SavePharmacistReview.class)
  public String savePharmacistReview(
      final String patientId,
      final PharmacistReviewDto pharmacistReview,
      final Boolean authorize,
      final Locale locale)
  {
    final String compositionUid =
        pharmacistReviewSaver.savePharmacistReview(patientId, pharmacistReview, authorize, locale);

    if (pharmacistReview.getReminderDate() != null)
    {
      pharmacistTaskCreator.createPharmacistReminderTask(
          patientId,
          compositionUid,
          pharmacistReview.getReminderDate(),
          pharmacistReview.getReminderNote(),
          locale);
    }
    if (!pharmacistReview.getRelatedTherapies().isEmpty()
        && pharmacistReview.getMedicationSupplyTypeEnum() != null
        && pharmacistReview.getDaysSupply() != null)
    {
      pharmacySupplyProcessHandler.handleSupplyRequest(
          patientId,
          TherapyAssigneeEnum.PHARMACIST,
          pharmacistReview.getRelatedTherapies().get(0).getTherapy().getCompositionUid(),
          pharmacistReview.getRelatedTherapies().get(0).getTherapy().getEhrOrderName(),
          pharmacistReview.getDaysSupply(),
          pharmacistReview.getMedicationSupplyTypeEnum());
    }

    return compositionUid;
  }

  @Transactional
  @EhrSessioned
  @EventProducer(MedicationsServiceEvents.ReviewPharmacistReview.class)
  public void reviewPharmacistReview(
      final String patientId,
      final String pharmacistReviewUid,
      final ReviewPharmacistReviewAction reviewAction,
      final TherapyDto modifiedTherapy,
      final List<String> deniedReviews,
      final String centralCaseId,
      @Nullable final String careProviderId,
      final NamedExternalDto prescriber,
      final Locale locale)
  {
    final DateTime requestTimestamp = requestDateTimeHolder.getRequestTimestamp();
    pharmacistReviewSaver.reviewPharmacistReview(
        patientId,
        pharmacistReviewUid,
        reviewAction,
        deniedReviews,
        requestTimestamp,
        locale);

    if (reviewAction == ReviewPharmacistReviewAction.MODIFIED)
    {
      medicationsService.modifyTherapy(
          patientId,
          modifiedTherapy,
          null,
          centralCaseId,
          careProviderId,
          prescriber,
          requestTimestamp,
          null,
          locale);
    }
    else if (reviewAction == ReviewPharmacistReviewAction.ABORTED)
    {
      medicationsService.abortTherapy(
          patientId,
          TherapyIdUtils.getCompositionUidWithoutVersion(modifiedTherapy.getCompositionUid()),
          modifiedTherapy.getEhrOrderName(),
          null);
    }
    else if (reviewAction == ReviewPharmacistReviewAction.REISSUED)
    {
      medicationsService.reissueTherapy(
          patientId,
          TherapyIdUtils.getCompositionUidWithoutVersion(modifiedTherapy.getCompositionUid()),
          modifiedTherapy.getEhrOrderName());
    }
    else if (reviewAction == ReviewPharmacistReviewAction.COPIED)
    {
      final SaveMedicationOrderDto saveMedicationOrderDto = new SaveMedicationOrderDto();
      saveMedicationOrderDto.setTherapy(modifiedTherapy);
      saveMedicationOrderDto.setActionEnum(MedicationOrderActionEnum.PRESCRIBE);
      final List<SaveMedicationOrderDto> medicationOrders = new ArrayList<>();
      medicationOrders.add(saveMedicationOrderDto);

      medicationsService.saveNewMedicationOrder(
          patientId,
          medicationOrders,
          centralCaseId,
          null,
          careProviderId,
          prescriber,
          null,
          requestTimestamp,
          locale);
    }
  }

  @Transactional
  @EhrSessioned
  public PharmacistReviewsDto getPharmacistReviews(final String patientId, final DateTime fromDate, final Locale locale)
  {
    return pharmacistReviewProvider.loadReviews(patientId, fromDate, locale);
  }
}
