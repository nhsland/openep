package com.marand.thinkmed.medications.dto.overview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyDayDto extends DataTransferObject
{
  private TherapyDto therapy;

  private TherapyStatusEnum therapyStatus;
  private String statusReason;
  private TherapyChangeReasonEnum therapyChangeReasonEnum;
  private boolean doctorReviewNeeded;
  private boolean therapyEndsBeforeNextRounds;
  private boolean modifiedFromLastReview;
  private boolean modified;
  private boolean active;
  private TherapyPharmacistReviewStatusEnum therapyPharmacistReviewStatus;
  private boolean activeAnyPartOfDay;
  private int consecutiveDay;
  private boolean showConsecutiveDay;
  private boolean basedOnPharmacyReview;
  private DateTime originalTherapyStart;
  private DateTime lastModifiedTimestamp;
  private List<TherapyTaskSimpleDto> tasks = new ArrayList<>();
  private DateTime reviewedUntil;
  private Boolean witnessingRequired;
  private Set<MedicationPropertyDto> medicationProperties = new HashSet<>();
  private boolean containsNonFormularyMedications;

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public TherapyStatusEnum getTherapyStatus()
  {
    return therapyStatus;
  }

  public void setTherapyStatus(final TherapyStatusEnum therapyStatus)
  {
    this.therapyStatus = therapyStatus;
  }

  public String getStatusReason()
  {
    return statusReason;
  }

  public void setStatusReason(final String statusReason)
  {
    this.statusReason = statusReason;
  }

  public TherapyChangeReasonEnum getTherapyChangeReasonEnum()
  {
    return therapyChangeReasonEnum;
  }

  public void setTherapyChangeReasonEnum(final TherapyChangeReasonEnum therapyChangeReasonEnum)
  {
    this.therapyChangeReasonEnum = therapyChangeReasonEnum;
  }

  public boolean isModifiedFromLastReview()
  {
    return modifiedFromLastReview;
  }

  public void setModifiedFromLastReview(final boolean modifiedFromLastReview)
  {
    this.modifiedFromLastReview = modifiedFromLastReview;
  }

  public boolean isModified()
  {
    return modified;
  }

  public void setModified(final boolean modified)
  {
    this.modified = modified;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(final boolean active)
  {
    this.active = active;
  }

  public boolean isActiveAnyPartOfDay()
  {
    return activeAnyPartOfDay;
  }

  public void setActiveAnyPartOfDay(final boolean activeAnyPartOfDay)
  {
    this.activeAnyPartOfDay = activeAnyPartOfDay;
  }

  public boolean isDoctorReviewNeeded()
  {
    return doctorReviewNeeded;
  }

  public void setDoctorReviewNeeded(final boolean doctorReviewNeeded)
  {
    this.doctorReviewNeeded = doctorReviewNeeded;
  }

  public boolean isTherapyEndsBeforeNextRounds()
  {
    return therapyEndsBeforeNextRounds;
  }

  public void setTherapyEndsBeforeNextRounds(final boolean therapyEndsBeforeNextRounds)
  {
    this.therapyEndsBeforeNextRounds = therapyEndsBeforeNextRounds;
  }

  public int getConsecutiveDay()
  {
    return consecutiveDay;
  }

  public void setConsecutiveDay(final int consecutiveDay)
  {
    this.consecutiveDay = consecutiveDay;
  }

  public TherapyPharmacistReviewStatusEnum getTherapyPharmacistReviewStatus()
  {
    return therapyPharmacistReviewStatus;
  }

  public void setTherapyPharmacistReviewStatus(final TherapyPharmacistReviewStatusEnum therapyPharmacistReviewStatus)
  {
    this.therapyPharmacistReviewStatus = therapyPharmacistReviewStatus;
  }

  public boolean isShowConsecutiveDay()
  {
    return showConsecutiveDay;
  }

  public void setShowConsecutiveDay(final boolean showConsecutiveDay)
  {
    this.showConsecutiveDay = showConsecutiveDay;
  }

  public boolean isBasedOnPharmacyReview()
  {
    return basedOnPharmacyReview;
  }

  public void setBasedOnPharmacyReview(final boolean basedOnPharmacyReview)
  {
    this.basedOnPharmacyReview = basedOnPharmacyReview;
  }

  public DateTime getOriginalTherapyStart()
  {
    return originalTherapyStart;
  }

  public void setOriginalTherapyStart(final DateTime originalTherapyStart)
  {
    this.originalTherapyStart = originalTherapyStart;
  }

  public DateTime getLastModifiedTimestamp()
  {
    return lastModifiedTimestamp;
  }

  public void setLastModifiedTimestamp(final DateTime lastModifiedTimestamp)
  {
    this.lastModifiedTimestamp = lastModifiedTimestamp;
  }

  public List<TherapyTaskSimpleDto> getTasks()
  {
    return tasks;
  }

  public void setTasks(final List<TherapyTaskSimpleDto> tasks)
  {
    this.tasks = tasks;
  }

  public DateTime getReviewedUntil()
  {
    return reviewedUntil;
  }

  public void setReviewedUntil(final DateTime reviewedUntil)
  {
    this.reviewedUntil = reviewedUntil;
  }

  public Boolean getWitnessingRequired()
  {
    return witnessingRequired;
  }

  public void setWitnessingRequired(final Boolean witnessingRequired)
  {
    this.witnessingRequired = witnessingRequired;
  }

  public Set<MedicationPropertyDto> getMedicationProperties()
  {
    return medicationProperties;
  }

  public void setMedicationProperties(final Set<MedicationPropertyDto> medicationProperties)
  {
    this.medicationProperties = medicationProperties;
  }

  public boolean isContainsNonFormularyMedications()
  {
    return containsNonFormularyMedications;
  }

  public void setContainsNonFormularyMedications(final boolean containsNonFormularyMedications)
  {
    this.containsNonFormularyMedications = containsNonFormularyMedications;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("therapyStatus", therapyStatus)
        .append("statusReason", statusReason)
        .append("therapyChangeReasonEnum", therapyChangeReasonEnum)
        .append("modifiedFromLastReview", modifiedFromLastReview)
        .append("modified", modified)
        .append("active", active)
        .append("activeAnyPartOfDay", activeAnyPartOfDay)
        .append("doctorReviewNeeded", doctorReviewNeeded)
        .append("therapyEndsBeforeNextRounds", therapyEndsBeforeNextRounds)
        .append("therapyPharmacistReviewStatus", therapyPharmacistReviewStatus)
        .append("consecutiveDay", consecutiveDay)
        .append("showConsecutiveDay", showConsecutiveDay)
        .append("basedOnPharmacyReview", basedOnPharmacyReview)
        .append("originalTherapyStart", originalTherapyStart)
        .append("lastModifiedTimestamp", lastModifiedTimestamp)
        .append("tasks", tasks)
        .append("reviewedUntil", reviewedUntil)
        .append("witnessingRequired", witnessingRequired)
        .append("medicationProperties", medicationProperties)
        .append("containsNonFormularyMedications", containsNonFormularyMedications)
    ;
  }
}
