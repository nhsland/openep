package com.marand.thinkmed.medications.pharmacist;

import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface PharmacistReviewSaver
{
  String savePharmacistReview(
      String patientId,
      PharmacistReviewDto pharmacistReview,
      Boolean authorize,
      Locale locale);

  void reviewPharmacistReview(
      String patientId,
      String pharmacistReviewUid,
      ReviewPharmacistReviewAction reviewAction,
      List<String> deniedReviews,
      DateTime when,
      Locale locale);

  void authorizePatientPharmacistReviews(
      String patientId,
      List<String> pharmacistReviewUids,
      Locale locale,
      DateTime when);
}
