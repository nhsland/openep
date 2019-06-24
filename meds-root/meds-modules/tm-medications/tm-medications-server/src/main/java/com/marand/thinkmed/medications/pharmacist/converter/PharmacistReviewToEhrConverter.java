package com.marand.thinkmed.medications.pharmacist.converter;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistTherapyChangeType;
import com.marand.thinkmed.medications.dto.pharmacist.review.ReviewPharmacistReviewAction;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyProblemDescriptionDto;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOnAdmission;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.pharmacist.DrugRelatedProblem;
import com.marand.thinkmed.medications.ehr.model.pharmacist.MedicationItemAssessment;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PatientRelatedProblem;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacistIssue;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacokineticIssue;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.model.pharmacist.RecommendationResponse;
import com.marand.thinkmed.medications.ehr.model.pharmacist.RequestReferral;
import com.marand.thinkmed.medications.ehr.utils.EhrContextVisitor;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.therapy.converter.TherapyConverter;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCodedText;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class PharmacistReviewToEhrConverter
{
  private TherapyConverter therapyConverter;

  @Autowired
  public void setTherapyConverter(final TherapyConverter therapyConverter)
  {
    this.therapyConverter = therapyConverter;
  }

  public PharmacyReviewReport convert(final PharmacistReviewDto dto, final DateTime when)
  {
    final PharmacyReviewReport composition = new PharmacyReviewReport();

    final MedicationItemAssessment assessment = composition.getOrCreatePharmacistMedicationReview().getMedicationItemAssessment();

    assessment.setDrugRelatedProblem(convertDrugRelatedProblem(dto.getDrugRelatedProblem()));
    assessment.setPharmacokineticIssue(convertPharmacokineticIssue(dto.getPharmacokineticIssue()));
    assessment.setPatientRelatedProblem(convertPatientRelatedProblem(dto.getPatientRelatedProblem()));

    assessment.setNoProblemIdentified(dto.isNoProblem() ? DataValueUtils.getBoolean(dto.isNoProblem()) : null);

    if (dto.getOverallRecommendation() != null)
    {
      assessment.setOverallRecommendation(DataValueUtils.getText(dto.getOverallRecommendation()));
    }

    // convert to medication order and fill composition, add action
    final MedicationOrder medicationOrder = convertToMedicationOrderAndFillComposition(
        composition,
        dto.getRelatedTherapies(),
        when);

    composition.getMiscellaneous().setMedicationorder(medicationOrder);
    composition.getMiscellaneous().setPrescriberReferral(buildPrescriberReferral(dto));

    new EhrContextVisitor(composition)
        .withStartTime(when)
        .withComposer(dto.getComposer())
        .withStatus(PharmacistReviewStatusEnum.DRAFT.getCode())
        .visit();

    return composition;
  }

  public void setPrescriberReferralResponse(
      final PharmacyReviewReport report,
      final ReviewPharmacistReviewAction reviewAction,
      final DateTime when)
  {
    final RecommendationResponse referralResponse = new RecommendationResponse();
    referralResponse.setResponse(ReviewPharmacistReviewAction.mapToReferralResponseType(reviewAction).getDvCodedText());
    report.getMiscellaneous().setPrescriberReferralResponse(referralResponse);

    new EhrContextVisitor(report)
        .withComposer(report.getComposer())
        .withStartTime(when)
        .visit();
  }

  private RequestReferral buildPrescriberReferral(final PharmacistReviewDto dto)
  {
    if (dto.isReferBackToPrescriber())
    {
      final RequestReferral requestReferral = new RequestReferral();
      requestReferral.setReasonDescription(DataValueUtils.getText("Pharmacist referred the review to prescriber"));
      requestReferral.setServiceRequested(DataValueUtils.getText("Review pharmacist report"));
      return requestReferral;
    }
    else
    {
      return null;
    }
  }

  private DrugRelatedProblem convertDrugRelatedProblem(final TherapyProblemDescriptionDto dto)
  {
    if (dto == null)
    {
      return null;
    }

    final DrugRelatedProblem drugRelatedProblem = new DrugRelatedProblem();
    fillPharmacistIssue(dto, drugRelatedProblem);
    return drugRelatedProblem;
  }

  private PharmacokineticIssue convertPharmacokineticIssue(final TherapyProblemDescriptionDto dto)
  {
    if (dto == null)
    {
      return null;
    }

    final PharmacokineticIssue pharmacokineticIssue = new PharmacokineticIssue();
    fillPharmacistIssue(dto, pharmacokineticIssue);
    return pharmacokineticIssue;
  }

  private PatientRelatedProblem convertPatientRelatedProblem(final TherapyProblemDescriptionDto dto)
  {
    if (dto == null)
    {
      return null;
    }

    final PatientRelatedProblem patientRelatedProblem = new PatientRelatedProblem();
    fillPharmacistIssue(dto, patientRelatedProblem);
    return patientRelatedProblem;
  }

  private void fillPharmacistIssue(
      final TherapyProblemDescriptionDto problemDescription,
      final PharmacistIssue pharmacistIssue)
  {
    pharmacistIssue.setOutcome(convertNamedIdentity(problemDescription.getOutcome()));
    pharmacistIssue.setImpact(convertNamedIdentity(problemDescription.getImpact()));
    pharmacistIssue.setRecommendation(DataValueUtils.getText(problemDescription.getRecommendation()));
    pharmacistIssue.setCategory(
        problemDescription.getCategories()
            .stream()
            .map(this::convertNamedIdentity)
            .collect(Collectors.toList()));
  }

  private DvCodedText convertNamedIdentity(final NamedExternalDto dto)
  {
    return dto != null ? DataValueUtils.getLocalCodedText(dto.getId(), dto.getName()) : null;
  }

  private MedicationOrder convertToMedicationOrderAndFillComposition(
      final PharmacyReviewReport composition,
      final List<PharmacistReviewTherapyDto> relatedTherapies,
      final DateTime when)
  {
    Preconditions.checkArgument(relatedTherapies.size() < 2, "Only 1 or 0 relatedTherapies allowed!");

    if (!relatedTherapies.isEmpty())
    {
      final PharmacistReviewTherapyDto relatedTherapy = relatedTherapies.get(0);
      final TherapyDto therapy = relatedTherapy.getTherapy();
      final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);

      if (relatedTherapy.getChangeType() != null && relatedTherapy.getChangeType() != PharmacistTherapyChangeType.NONE)
      {
        final MedicationActionEnum actionEnum = PharmacistUtils.mapToMedicationActionType(relatedTherapy.getChangeType());

        if (actionEnum != null)
        {
          final MedicationManagement action = MedicationsEhrUtils.buildMedicationAction(
              composition,
              actionEnum,
              MedicationOnAdmission.getMedicationOrderPath(),
              when);

          composition.getMiscellaneous().setMedicationManagement(action);
        }
      }

      composition.getLinks().clear();
      final Link linkToTherapy = getLinkToTherapyInstruction(therapy.getCompositionUid());
      composition.getLinks().add(linkToTherapy);

      return medicationOrder;
    }

    return null;
  }

  private Link getLinkToTherapyInstruction(final String compositionUid)
  {
    return LinksEhrUtils.createLink(compositionUid, "pharmacy", EhrLinkType.REVIEWED);
  }
}
