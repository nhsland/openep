package com.marand.thinkmed.medications.pharmacist;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewsDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface PharmacistReviewProvider
{
  PharmacistReviewsDto loadReviews(String patientId, DateTime fromDate, Locale locale);

  List<PharmacistReviewDto> loadReviewsForTherapy(String patientId, String compositionUid, Locale locale);
}
