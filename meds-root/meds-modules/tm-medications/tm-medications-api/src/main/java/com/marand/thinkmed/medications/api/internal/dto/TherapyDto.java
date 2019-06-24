package com.marand.thinkmed.medications.api.internal.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import static java.util.stream.Collectors.toSet;

/**
 * @author Mitja Lapajne
 */
public abstract class TherapyDto extends DataTransferObject implements JsonSerializable
{
  private String compositionUid;
  private String ehrOrderName;
  private String linkName;
  private DateTime createdTimestamp;
  private MedicationOrderFormType medicationOrderFormType;
  private boolean variable;
  private String therapyDescription;
  private List<MedicationRouteDto> routes = new ArrayList<>();
  private DosingFrequencyDto dosingFrequency;
  private Integer dosingDaysFrequency; //every X-th day
  private TherapyDoseTypeEnum doseType;
  private List<String> daysOfWeek = new ArrayList<>();
  private Integer maxDailyFrequency;
  private DateTime pastTherapyStart;
  private DateTime start;
  private DateTime end;
  private Boolean whenNeeded;
  private String comment;
  private IndicationDto clinicalIndication;
  private String prescriberName;
  private String composerName;
  private String startCriterion;
  private String applicationPrecondition;
  private Integer reviewReminderDays;
  private String reviewReminderComment;
  private Integer maxDosePercentage; //percents, not decimals
  private String admissionId;
  private ReleaseDetailsDto releaseDetails;
  private boolean addToDischargeLetter;

  private List<String> criticalWarnings = new ArrayList<>();

  private SelfAdministeringActionEnum selfAdministeringActionEnum;
  private DateTime selfAdministeringLastChange;

  private PrescriptionLocalDetailsDto prescriptionLocalDetails;
  private DispenseDetailsDto dispenseDetails;

  private String frequencyDisplay;
  private String daysFrequencyDisplay;
  private String whenNeededDisplay;
  private String startCriterionDisplay;
  private String daysOfWeekDisplay;
  private String applicationPreconditionDisplay;
  private List<NamedIdDto> informationSources = Lists.newArrayList();

  private String formattedTherapyDisplay;

  public List<NamedIdDto> getInformationSources()
  {
    return informationSources;
  }

  public void setInformationSources(final List<NamedIdDto> informationSources)
  {
    this.informationSources = informationSources;
  }

  protected TherapyDto(final MedicationOrderFormType type, final boolean variable)
  {
    medicationOrderFormType = Preconditions.checkNotNull(type);
    this.variable = variable;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public void setCompositionUid(final String compositionUid)
  {
    this.compositionUid = compositionUid;
  }

  public String getEhrOrderName()
  {
    return ehrOrderName;
  }

  public void setEhrOrderName(final String ehrOrderName)
  {
    this.ehrOrderName = ehrOrderName;
  }

  public MedicationOrderFormType getMedicationOrderFormType()
  {
    return medicationOrderFormType;
  }

  protected final void setMedicationOrderFormType(final MedicationOrderFormType medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  }

  public boolean isVariable()
  {
    return variable;
  }

  public void setVariable(final boolean variable)
  {
    this.variable = variable;
  }

  public String getTherapyDescription()
  {
    return therapyDescription;
  }

  public void setTherapyDescription(final String therapyDescription)
  {
    this.therapyDescription = therapyDescription;
  }

  public List<MedicationRouteDto> getRoutes()
  {
    return routes;
  }

  public void setRoutes(final List<MedicationRouteDto> routes)
  {
    this.routes = routes;
  }

  public DosingFrequencyDto getDosingFrequency()
  {
    return dosingFrequency;
  }

  public void setDosingFrequency(final DosingFrequencyDto dosingFrequency)
  {
    this.dosingFrequency = dosingFrequency;
  }

  public Integer getDosingDaysFrequency()
  {
    return dosingDaysFrequency;
  }

  public void setDosingDaysFrequency(final Integer dosingDaysFrequency)
  {
    this.dosingDaysFrequency = dosingDaysFrequency;
  }

  public List<String> getDaysOfWeek()
  {
    return daysOfWeek;
  }

  public void setDaysOfWeek(final List<String> daysOfWeek)
  {
    this.daysOfWeek = daysOfWeek;
  }

  public DateTime getStart()
  {
    return start;
  }

  public void setStart(final DateTime start)
  {
    this.start = start;
  }

  public DateTime getEnd()
  {
    return end;
  }

  public void setEnd(final DateTime end)
  {
    this.end = end;
  }

  public Boolean getWhenNeeded()
  {
    return whenNeeded;
  }

  public boolean isWhenNeeded()
  {
    return whenNeeded != null && whenNeeded;
  }

  public void setWhenNeeded(final Boolean whenNeeded)
  {
    this.whenNeeded = whenNeeded;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }

  public IndicationDto getClinicalIndication()
  {
    return clinicalIndication;
  }

  public void setClinicalIndication(final IndicationDto clinicalIndication)
  {
    this.clinicalIndication = clinicalIndication;
  }

  public String getPrescriberName()
  {
    return prescriberName;
  }

  public void setPrescriberName(final String prescriberName)
  {
    this.prescriberName = prescriberName;
  }

  public String getComposerName()
  {
    return composerName;
  }

  public void setComposerName(final String composerName)
  {
    this.composerName = composerName;
  }

  public String getStartCriterion()
  {
    return startCriterion;
  }

  public void setStartCriterion(final String startCriterion)
  {
    this.startCriterion = startCriterion;
  }

  public String getApplicationPrecondition()
  {
    return applicationPrecondition;
  }

  public void setApplicationPrecondition(final String applicationPrecondition)
  {
    this.applicationPrecondition = applicationPrecondition;
  }

  public Integer getReviewReminderDays()
  {
    return reviewReminderDays;
  }

  public void setReviewReminderDays(final Integer reviewReminderDays)
  {
    this.reviewReminderDays = reviewReminderDays;
  }

  public String getFrequencyDisplay()
  {
    return frequencyDisplay;
  }

  public void setFrequencyDisplay(final String frequencyDisplay)
  {
    this.frequencyDisplay = frequencyDisplay;
  }

  public String getDaysFrequencyDisplay()
  {
    return daysFrequencyDisplay;
  }

  public void setDaysFrequencyDisplay(final String daysFrequencyDisplay)
  {
    this.daysFrequencyDisplay = daysFrequencyDisplay;
  }

  public String getWhenNeededDisplay()
  {
    return whenNeededDisplay;
  }

  public void setWhenNeededDisplay(final String whenNeededDisplay)
  {
    this.whenNeededDisplay = whenNeededDisplay;
  }

  public String getStartCriterionDisplay()
  {
    return startCriterionDisplay;
  }

  public void setStartCriterionDisplay(final String startCriterionDisplay)
  {
    this.startCriterionDisplay = startCriterionDisplay;
  }

  public String getDaysOfWeekDisplay()
  {
    return daysOfWeekDisplay;
  }

  public void setDaysOfWeekDisplay(final String daysOfWeekDisplay)
  {
    this.daysOfWeekDisplay = daysOfWeekDisplay;
  }

  public String getFormattedTherapyDisplay()
  {
    return formattedTherapyDisplay;
  }

  public String getApplicationPreconditionDisplay()
  {
    return applicationPreconditionDisplay;
  }

  public void setApplicationPreconditionDisplay(final String applicationPreconditionDisplay)
  {
    this.applicationPreconditionDisplay = applicationPreconditionDisplay;
  }

  public void setFormattedTherapyDisplay(final String formattedTherapyDisplay)
  {
    this.formattedTherapyDisplay = formattedTherapyDisplay;
  }

  public String getLinkName()
  {
    return linkName;
  }

  public void setLinkName(final String linkName)
  {
    this.linkName = linkName;
  }

  public Integer getMaxDailyFrequency()
  {
    return maxDailyFrequency;
  }

  public void setMaxDailyFrequency(final Integer maxDailyFrequency)
  {
    this.maxDailyFrequency = maxDailyFrequency;
  }

  public DateTime getPastTherapyStart()
  {
    return pastTherapyStart;
  }

  public void setPastTherapyStart(final DateTime pastTherapyStart)
  {
    this.pastTherapyStart = pastTherapyStart;
  }

  public DateTime getCreatedTimestamp()
  {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(final DateTime createdTimestamp)
  {
    this.createdTimestamp = createdTimestamp;
  }

  public List<String> getCriticalWarnings()
  {
    return criticalWarnings;
  }

  public void setCriticalWarnings(final List<String> criticalWarnings)
  {
    this.criticalWarnings = criticalWarnings;
  }

  public String getAdmissionId()
  {
    return admissionId;
  }

  public boolean isLinkedToAdmission()
  {
    return admissionId != null;
  }

  public void setAdmissionId(final String admissionId)
  {
    this.admissionId = admissionId;
  }

  public Integer getMaxDosePercentage()
  {
    return maxDosePercentage;
  }

  public void setMaxDosePercentage(final Integer maxDosePercentage)
  {
    this.maxDosePercentage = maxDosePercentage;
  }

  public PrescriptionLocalDetailsDto getPrescriptionLocalDetails()
  {
    return prescriptionLocalDetails;
  }

  public void setPrescriptionLocalDetails(final PrescriptionLocalDetailsDto prescriptionLocalDetails)
  {
    this.prescriptionLocalDetails = prescriptionLocalDetails;
  }

  public DispenseDetailsDto getDispenseDetails()
  {
    return dispenseDetails;
  }

  public void setDispenseDetails(final DispenseDetailsDto dispenseDetails)
  {
    this.dispenseDetails = dispenseDetails;
  }

  public TherapyDoseTypeEnum getDoseType()
  {
    return doseType;
  }

  public void setDoseType(final TherapyDoseTypeEnum doseType)
  {
    this.doseType = doseType;
  }

  public SelfAdministeringActionEnum getSelfAdministeringActionEnum()
  {
    return selfAdministeringActionEnum;
  }

  public void setSelfAdministeringActionEnum(final SelfAdministeringActionEnum selfAdministeringActionEnum)
  {
    this.selfAdministeringActionEnum = selfAdministeringActionEnum;
  }

  public DateTime getSelfAdministeringLastChange()
  {
    return selfAdministeringLastChange;
  }

  public void setSelfAdministeringLastChange(final DateTime selfAdministeringLastChange)
  {
    this.selfAdministeringLastChange = selfAdministeringLastChange;
  }

  public String getReviewReminderComment()
  {
    return reviewReminderComment;
  }

  public void setReviewReminderComment(final String reviewReminderComment)
  {
    this.reviewReminderComment = reviewReminderComment;
  }

  public ReleaseDetailsDto getReleaseDetails()
  {
    return releaseDetails;
  }

  public void setReleaseDetails(final ReleaseDetailsDto releaseDetails)
  {
    this.releaseDetails = releaseDetails;
  }

  public boolean isAddToDischargeLetter()
  {
    return addToDischargeLetter;
  }

  public void setAddToDischargeLetter(final boolean addToDischargeLetter)
  {
    this.addToDischargeLetter = addToDischargeLetter;
  }

  public String getTherapyId()
  {
    Preconditions.checkNotNull(compositionUid, "compositionUid must not be null!");
    Preconditions.checkNotNull(ehrOrderName, "ehrOrderName must not be null!");

    return (compositionUid.contains("::") ? compositionUid.substring(0, compositionUid.indexOf("::")) : compositionUid)
        + '|'
        + ehrOrderName;
  }

  public boolean isWithRate()
  {
    return TherapyDoseTypeEnum.WITH_RATE.contains(doseType);
  }

  public abstract boolean isNormalInfusion();

  public abstract List<MedicationDto> getMedications();

  public abstract Long getMainMedicationId();

  public Set<Long> getMedicationIds()
  {
    return getMedications().stream().map(MedicationDto::getId).filter(Objects::nonNull).collect(toSet());
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("ehrOrderName", ehrOrderName)
        .append("medicationOrderFormType", medicationOrderFormType)
        .append("therapyDescription", therapyDescription)
        .append("route", routes)
        .append("dosingFrequency", dosingFrequency)
        .append("dosingDaysFrequency", dosingDaysFrequency)
        .append("daysOfWeek", daysOfWeek)
        .append("start", start)
        .append("end", end)
        .append("whenNeeded", whenNeeded)
        .append("comment", comment)
        .append("clinicalIndication", clinicalIndication)
        .append("prescriberName", prescriberName)
        .append("composerName", composerName)
        .append("startCriterion", startCriterion)
        .append("applicationPrecondition", applicationPrecondition)
        .append("reviewReminderDays", reviewReminderDays)
        .append("releaseDetails", releaseDetails)
        .append("frequencyDisplay", frequencyDisplay)
        .append("daysFrequencyDisplay", daysFrequencyDisplay)
        .append("whenNeededDisplay", whenNeededDisplay)
        .append("startCriterionDisplay", startCriterionDisplay)
        .append("daysOfWeekDisplay", daysOfWeekDisplay)
        .append("applicationPreconditionDisplay", applicationPreconditionDisplay)
        .append("informationSource", informationSources)
        .append("formattedTherapyDisplay", formattedTherapyDisplay)
        .append("pastTherapyStart", pastTherapyStart)
        .append("linkName", linkName)
        .append("maxDailyFrequency", maxDailyFrequency)
        .append("createdTimestamp", createdTimestamp)
        .append("criticalWarnings", criticalWarnings)
        .append("admissionId", admissionId)
        .append("forPrescription", addToDischargeLetter)
        .append("maxDosePercentage", maxDosePercentage)
        .append("prescriptionLocalDetails", prescriptionLocalDetails)
        .append("dispenseDetails", dispenseDetails)
        .append("selfAdministeringActionEnum", selfAdministeringActionEnum)
        .append("selfAdministeringLastChange", selfAdministeringLastChange)
        .append("doseType", doseType)
        .append("reviewReminderComment", reviewReminderComment)
    ;
  }
}