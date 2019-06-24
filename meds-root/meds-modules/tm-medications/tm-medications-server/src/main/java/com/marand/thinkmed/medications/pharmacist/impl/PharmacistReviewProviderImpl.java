package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.OpenEhrRefUtils;
import com.marand.thinkmed.medications.TaskTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.pharmacist.PharmacistReviewProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.converter.PharmacistReviewFromEhrConverter;
import com.marand.thinkmed.medications.task.PharmacistReminderTaskDef;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.TherapyTaskDef;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class PharmacistReviewProviderImpl implements PharmacistReviewProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter;
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
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setPharmacistReviewFromEhrConverter(final PharmacistReviewFromEhrConverter pharmacistReviewFromEhrConverter)
  {
    this.pharmacistReviewFromEhrConverter = pharmacistReviewFromEhrConverter;
  }

  @Override
  public PharmacistReviewsDto loadReviews(final String patientId, final DateTime fromDate, final Locale locale)
  {
    final PharmacistReviewsDto pharmacistReviewsDto = new PharmacistReviewsDto();
    final List<PharmacyReviewReport> pharmacistsReviews = medicationsOpenEhrDao.findPharmacistsReviewReports(patientId, fromDate);
    final List<TaskDto> reminderAndSupplyTasks = pharmacistTaskProvider.findPharmacistReminderAndSupplyTasks(patientId, null);

    final List<PharmacistReviewDto> pharmacistReviewsList = new ArrayList<>();
    for (final PharmacyReviewReport review : pharmacistsReviews)
    {
      final PharmacistReviewDto pharmacistReviewDto = pharmacistReviewFromEhrConverter.convert(
          patientId,
          review,
          locale);

      fillPharmacistReminderAndSupplyTasksData(patientId, pharmacistReviewDto, reminderAndSupplyTasks);
      pharmacistReviewsList.add(pharmacistReviewDto);
    }
    pharmacistReviewsDto.setPharmacistReviews(pharmacistReviewsList);

    final DateTime lastEditTimestamp =
        pharmacistTaskProvider.getLastEditTimestampForPharmacistReview(String.valueOf(patientId));
    pharmacistReviewsDto.setLastTaskChangeTimestamp(lastEditTimestamp);
    return pharmacistReviewsDto;
  }

  @Override
  public List<PharmacistReviewDto> loadReviewsForTherapy(
      final String patientId,
      final String therapyCompositionUid,
      final Locale locale)
  {
    final List<PharmacyReviewReport> pharmacistsReviews = medicationsOpenEhrDao.findPharmacistsReviewReports(
        patientId,
        requestDateTimeHolder.getRequestTimestamp().minusDays(30));

    final List<TaskDto> pharmacistReviewAndSupplyTasks = pharmacistTaskProvider.findPharmacistReminderAndSupplyTasks(
        patientId,
        null);

    final List<PharmacistReviewDto> pharmacistReviewDtos = new ArrayList<>();
    for (final PharmacyReviewReport review : pharmacistsReviews)
    {
      if (review.getMiscellaneous() != null)
      {
        final PharmacistReviewStatusEnum status = PharmacistReviewStatusEnum.valueOf(review.getContext().getStatus());
        final boolean reviewedByPharmacist = review.getMiscellaneous().getPrescriberReferralResponse() != null;

        if (status == PharmacistReviewStatusEnum.FINAL
            && !reviewedByPharmacist
            && review.getMiscellaneous().getMedicationorder() != null)
        {
          if (isTherapyReviewed(therapyCompositionUid, review))
          {
            final PharmacistReviewDto pharmacistReviewDto = pharmacistReviewFromEhrConverter.convert(patientId, review, locale);

            fillPharmacistReminderAndSupplyTasksData(patientId, pharmacistReviewDto, pharmacistReviewAndSupplyTasks);
            if (pharmacistReviewDto.isReferBackToPrescriber())
            {
              pharmacistReviewDtos.add(pharmacistReviewDto);
            }
          }
        }
      }
    }
    if (!pharmacistReviewDtos.isEmpty())
    {
      pharmacistReviewDtos.get(0).setMostRecentReview(true);
    }
    return pharmacistReviewDtos;
  }

  private boolean isTherapyReviewed(final String compositionUid, final PharmacyReviewReport review)
  {
    final List<Link> reviewedLinks = LinksEhrUtils.getLinksOfType(review, EhrLinkType.REVIEWED);

    if (reviewedLinks.isEmpty())
    {
      return false;
    }

    final String uidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid);
    final String linkedUidWithoutVersion = TherapyIdUtils.getCompositionUidWithoutVersion(
        OpenEhrRefUtils.parseEhrUri(reviewedLinks.get(0).getTarget().getValue()).getCompositionId());

    return uidWithoutVersion.equals(linkedUidWithoutVersion);
  }

  private void fillPharmacistReminderAndSupplyTasksData(
      final String patientId,
      final PharmacistReviewDto pharmacistReviewDto,
      final List<TaskDto> tasks)
  {
    if (!pharmacistReviewDto.getRelatedTherapies().isEmpty())
    {
      final TherapyDto therapyDto = pharmacistReviewDto.getRelatedTherapies().get(0).getTherapy();

      final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyDto.getCompositionUid());

      for (final TaskDto pharmacistTask : tasks)
      {
        if (pharmacistTask.getTaskExecutionStrategyId().equals(TaskTypeEnum.PHARMACIST_REMINDER.getName()))
        {
          final String compositionUid =
              (String)pharmacistTask.getVariables().get(PharmacistReminderTaskDef.PHARMACIST_REVIEW_ID.getName());
          if (compositionUid != null &&
              TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid).equals(
                  TherapyIdUtils.getCompositionUidWithoutVersion(pharmacistReviewDto.getCompositionUid())))
          {
            pharmacistReviewDto.setReminderDate(pharmacistTask.getDueTime());
            pharmacistReviewDto.setReminderNote(
                (String)pharmacistTask.getVariables().get(PharmacistReminderTaskDef.COMMENT.getName()));
          }
        }
        else if (pharmacistTask.getTaskExecutionStrategyId().equals(TaskTypeEnum.SUPPLY_REMINDER.getName()))
        {
          final String compositionUid =
              (String)pharmacistTask.getVariables().get(TherapyTaskDef.ORIGINAL_THERAPY_ID.getName());
          if (compositionUid != null && compositionUid.equals(originalTherapyId))
          {
            final String supplyType = (String)pharmacistTask.getVariables().get(
                SupplyReminderTaskDef.SUPPLY_TYPE.getName());
            pharmacistReviewDto.setMedicationSupplyTypeEnum(
                supplyType != null ? MedicationSupplyTypeEnum.valueOf(supplyType) : null);
            pharmacistReviewDto.setDaysSupply(
                (Integer)pharmacistTask.getVariables().get(SupplyReminderTaskDef.DAYS_SUPPLY.getName()));
          }
        }
      }
    }
  }
}
