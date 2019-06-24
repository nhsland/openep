package com.marand.thinkmed.medications.dto.pharmacist.review;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class PharmacistReviewDto extends DataTransferObject
{
  private String compositionUid;
  private NamedExternalDto composer;
  private DateTime createTimestamp;
  private List<PharmacistReviewTherapyDto> relatedTherapies = new ArrayList<>();
  private boolean noProblem;
  private boolean referBackToPrescriber;
  private String overallRecommendation;
  private DateTime reminderDate;
  private String reminderNote;
  private MedicationSupplyTypeEnum medicationSupplyTypeEnum;
  private Integer daysSupply;
  private boolean mostRecentReview;
  private PharmacistReviewStatusEnum pharmacistReviewStatus;
  private TherapyProblemDescriptionDto drugRelatedProblem;
  private TherapyProblemDescriptionDto pharmacokineticIssue;
  private TherapyProblemDescriptionDto patientRelatedProblem;

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public void setCompositionUid(final String compositionUid)
  {
    this.compositionUid = compositionUid;
  }

  public NamedExternalDto getComposer()
  {
    return composer;
  }

  public void setComposer(final NamedExternalDto composer)
  {
    this.composer = composer;
  }

  public DateTime getCreateTimestamp()
  {
    return createTimestamp;
  }

  public void setCreateTimestamp(final DateTime createTimestamp)
  {
    this.createTimestamp = createTimestamp;
  }

  public List<PharmacistReviewTherapyDto> getRelatedTherapies()
  {
    return relatedTherapies;
  }

  public void setRelatedTherapies(final List<PharmacistReviewTherapyDto> relatedTherapies)
  {
    this.relatedTherapies = relatedTherapies;
  }

  public boolean isNoProblem()
  {
    return noProblem;
  }

  public void setNoProblem(final boolean noProblem)
  {
    this.noProblem = noProblem;
  }

  public boolean isReferBackToPrescriber()
  {
    return referBackToPrescriber;
  }

  public void setReferBackToPrescriber(final boolean referBackToPrescriber)
  {
    this.referBackToPrescriber = referBackToPrescriber;
  }

  public String getOverallRecommendation()
  {
    return overallRecommendation;
  }

  public void setOverallRecommendation(final String overallRecommendation)
  {
    this.overallRecommendation = overallRecommendation;
  }

  public DateTime getReminderDate()
  {
    return reminderDate;
  }

  public void setReminderDate(final DateTime reminderDate)
  {
    this.reminderDate = reminderDate;
  }

  public String getReminderNote()
  {
    return reminderNote;
  }

  public void setReminderNote(final String reminderNote)
  {
    this.reminderNote = reminderNote;
  }

  public MedicationSupplyTypeEnum getMedicationSupplyTypeEnum()
  {
    return medicationSupplyTypeEnum;
  }

  public void setMedicationSupplyTypeEnum(final MedicationSupplyTypeEnum medicationSupplyTypeEnum)
  {
    this.medicationSupplyTypeEnum = medicationSupplyTypeEnum;
  }

  public Integer getDaysSupply()
  {
    return daysSupply;
  }

  public void setDaysSupply(final Integer daysSupply)
  {
    this.daysSupply = daysSupply;
  }

  public PharmacistReviewStatusEnum getPharmacistReviewStatus()
  {
    return pharmacistReviewStatus;
  }

  public void setPharmacistReviewStatus(final PharmacistReviewStatusEnum pharmacistReviewStatus)
  {
    this.pharmacistReviewStatus = pharmacistReviewStatus;
  }

  public TherapyProblemDescriptionDto getDrugRelatedProblem()
  {
    return drugRelatedProblem;
  }

  public void setDrugRelatedProblem(final TherapyProblemDescriptionDto drugRelatedProblem)
  {
    this.drugRelatedProblem = drugRelatedProblem;
  }

  public TherapyProblemDescriptionDto getPharmacokineticIssue()
  {
    return pharmacokineticIssue;
  }

  public void setPharmacokineticIssue(final TherapyProblemDescriptionDto pharmacokineticIssue)
  {
    this.pharmacokineticIssue = pharmacokineticIssue;
  }

  public TherapyProblemDescriptionDto getPatientRelatedProblem()
  {
    return patientRelatedProblem;
  }

  public void setPatientRelatedProblem(final TherapyProblemDescriptionDto patientRelatedProblem)
  {
    this.patientRelatedProblem = patientRelatedProblem;
  }

  public boolean isMostRecentReview()
  {
    return mostRecentReview;
  }

  public void setMostRecentReview(final boolean mostRecentReview)
  {
    this.mostRecentReview = mostRecentReview;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("composer", composer)
        .append("createTimestamp", createTimestamp)
        .append("relatedTherapies", relatedTherapies)
        .append("noProblem", noProblem)
        .append("referBackToPrescriber", referBackToPrescriber)
        .append("overallRecommendation", overallRecommendation)
        .append("reminderDate", reminderDate)
        .append("reminderNote", reminderNote)
        .append("medicationSupplyTypeEnum", medicationSupplyTypeEnum)
        .append("daysSupply", daysSupply)
        .append("mostRecentReview", mostRecentReview)
        .append("pharmacistReviewStatus", pharmacistReviewStatus)
        .append("drugRelatedProblem", drugRelatedProblem)
        .append("pharmacokineticIssue", pharmacokineticIssue)
        .append("patientRelatedProblem", patientRelatedProblem)
    ;
  }
}
