package com.marand.thinkmed.medications.pharmacist.converter;

import java.util.List;
import java.util.Locale;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.change.TherapyChangeCalculator;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.pharmacist.MedicationItemAssessment;
import com.marand.thinkmed.medications.ehr.model.pharmacist.Miscellaneous;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacistIssue;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.DvText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */

@Component
public class PharmacistReviewFromEhrConverter
{
  private TherapyDisplayProvider therapyDisplayProvider;
  private TherapyEhrHandler therapyEhrHandler;
  private TherapyChangeCalculator therapyChangeCalculator;
  private TherapyConverter therapyConverter;

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setTherapyChangeCalculator(final TherapyChangeCalculator therapyChangeCalculator)
  {
    this.therapyChangeCalculator = therapyChangeCalculator;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  public PharmacistReviewDto convert(final String patientId, final PharmacyReviewReport composition, final Locale locale)
  {
    final PharmacistReviewDto dto = new PharmacistReviewDto();

    dto.setCompositionUid(composition.getUid());
    dto.setComposer(extractComposer(composition));
    dto.setCreateTimestamp(DataValueUtils.getDateTime(composition.getContext().getStartTime()));
    dto.setPharmacistReviewStatus(PharmacistReviewStatusEnum.valueOf(composition.getContext().getStatus()));

    if (composition.getPharmacistMedicationReview() != null)
    {
      final MedicationItemAssessment assessment = composition.getPharmacistMedicationReview().getMedicationItemAssessment();

      dto.setNoProblem(assessment.getNoProblemIdentified() != null && assessment.getNoProblemIdentified().isValue());
      dto.setOverallRecommendation(buildOverallRecommendation(assessment));

      dto.setDrugRelatedProblem(convertToTherapyProblemDescriptionDto(assessment.getDrugRelatedProblem()));
      dto.setPharmacokineticIssue(convertToTherapyProblemDescriptionDto(assessment.getPharmacokineticIssue()));
      dto.setPatientRelatedProblem(convertToTherapyProblemDescriptionDto(assessment.getPatientRelatedProblem()));
    }

    final Miscellaneous miscellaneous = composition.getMiscellaneous();
    if (miscellaneous != null)
    {
      dto.setReferBackToPrescriber(miscellaneous.getPrescriberReferral() != null);

      final MedicationOrder medicationorder = miscellaneous.getMedicationorder();
      if (medicationorder != null)
      {
        final InpatientPrescription originalPrescription = therapyEhrHandler.getPrescriptionFromLink(
            patientId, composition,
            EhrLinkType.REVIEWED,
            false);

        final TherapyDto originalTherapy = convertToTherapy(composition, locale, originalPrescription.getMedicationOrder(), originalPrescription.getUid());
        final TherapyDto therapy = convertToTherapy(composition, locale, medicationorder, originalPrescription.getUid());

        final PharmacistReviewTherapyDto pharmacistReviewTherapyDto = buildPharmacistReviewTherapyDto(
            miscellaneous,
            originalTherapy,
            therapy,
            locale);

        dto.getRelatedTherapies().add(pharmacistReviewTherapyDto);
      }
    }

    return dto;
  }

  public NamedExternalDto extractComposer(final PharmacyReviewReport composition)
  {
    return new NamedExternalDto(composition.getComposer().getId(), composition.getComposer().getName());
  }

  private PharmacistReviewTherapyDto buildPharmacistReviewTherapyDto(
      final Miscellaneous miscellaneous,
      final TherapyDto originalTherapy,
      final TherapyDto therapy,
      final Locale locale)
  {
    final PharmacistReviewTherapyDto dto = new PharmacistReviewTherapyDto();

    dto.setTherapy(therapy);

    if (miscellaneous.getMedicationManagement() != null)
    {
      dto.setChangeType(PharmacistUtils.mapToPharmacistTherapyChangeType(miscellaneous.getMedicationManagement()));
    }

    if (dto.getChangeType() == PharmacistTherapyChangeType.EDIT || dto.getChangeType() == PharmacistTherapyChangeType.SUSPEND)
    {
      dto.setChanges(therapyChangeCalculator.calculateTherapyChanges(originalTherapy, therapy, false, locale));
    }

    return dto;
  }

  private String buildOverallRecommendation(final MedicationItemAssessment assessment)
  {
    return assessment.getOverallRecommendation() != null ? assessment.getOverallRecommendation().getValue() : null;

  }

  private TherapyDto convertToTherapy(
      final PharmacyReviewReport composition,
      final Locale locale,
      final MedicationOrder order,
      final String compositionUid)
  {
    final TherapyDto therapy = therapyConverter.convertToTherapyDto(
        order,
        compositionUid,
        DataValueUtils.getDateTime(composition.getContext().getStartTime()));

    therapyDisplayProvider.fillDisplayValues(therapy, true, locale);
    return therapy;
  }

  private TherapyProblemDescriptionDto convertToTherapyProblemDescriptionDto(final PharmacistIssue pharmacistIssue)
  {
    if (pharmacistIssue == null)
    {
      return null;
    }

    final List<DvCodedText> categories = pharmacistIssue.getCategory();
    final DvCodedText outcome = pharmacistIssue.getOutcome();
    final DvCodedText impact = pharmacistIssue.getImpact();
    final DvText recommendation = pharmacistIssue.getRecommendation();

    final TherapyProblemDescriptionDto therapyProblemDescription = new TherapyProblemDescriptionDto();

    for (final DvCodedText category : categories)
    {
      final NamedExternalDto categoryNamedIdentity = convertCodedText(category);
      therapyProblemDescription.getCategories().add(categoryNamedIdentity);
    }

    final NamedExternalDto outcomeDto = convertCodedText(outcome);
    therapyProblemDescription.setOutcome(outcomeDto);
    final NamedExternalDto impactDto = convertCodedText(impact);
    therapyProblemDescription.setImpact(impactDto);
    therapyProblemDescription.setRecommendation(recommendation.getValue());
    return therapyProblemDescription;
  }

  private NamedExternalDto convertCodedText(final DvCodedText codedText)
  {
    return codedText != null ?
           new NamedExternalDto(codedText.getDefiningCode().getCodeString(), codedText.getValue()) :
           null;
  }
}
