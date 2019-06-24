package com.marand.thinkmed.medications.config;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Validated
@ConfigurationProperties(prefix = "meds")
public class MedsProperties
{
  @NotNull private Boolean pharmacistReviewReferBackPreset;
  @NotNull private Boolean mentalHealthReportEnabled;
  @NotNull private Boolean autoAdministrationChartingEnabled;
  @NotNull private Boolean supplyPresent;
  @NotNull private Boolean showHeparinPane;
  @NotNull private Boolean cumulativeAntipsychoticDoseEnabled;
  @NotNull private Boolean infusionBagEnabled;
  private String outpatientPrescriptionType;
  @NotNull private String organizationCode;
  @NotNull private Boolean formularyFilterEnabled;
  @NotNull private Boolean doctorReviewEnabled;
  @NotNull private Boolean doseRangeEnabled;
  @NotNull private Boolean searchStartMustMatch;
  @NotNull private Boolean referenceWeightRequired;
  @NotNull private Integer administrationTaskCreationDays;
  @NotNull private Boolean diseaseCodeTranslationRequired;
  @NotNull private Boolean allergyCodeTranslationRequired;
  @NotNull private Boolean substituteAdministrationMedicationEnabled;
  @NotNull private Integer printPastAdministrationDays;
  private boolean antimicrobialDaysCountStartsWithOne;
  @NotNull private String inrResultCode;
  @NotNull private String apttrResultCode;
  @NotNull private Boolean presetPastAdministrationTimeToNow;
  @NotNull private Boolean doseCalculationsEnabled;
  @NotNull private Integer pharmacistReviewDisplayDays;
  @NotNull private Boolean singleDayTherapiesOverviewEnabled;
  @NotNull private Boolean suspendReasonMandatory;
  @NotNull private Boolean stopReasonMandatory;
  @NotNull private Integer completedTherapiesShownMinutes;
  @NotNull private boolean duplicateTherapyWarningOverrideRequired;

  public Boolean getPharmacistReviewReferBackPreset()
  {
    return pharmacistReviewReferBackPreset;
  }

  public void setPharmacistReviewReferBackPreset(final Boolean pharmacistReviewReferBackPreset)
  {
    this.pharmacistReviewReferBackPreset = pharmacistReviewReferBackPreset;
  }

  public Boolean getMentalHealthReportEnabled()
  {
    return mentalHealthReportEnabled;
  }

  public void setMentalHealthReportEnabled(final Boolean mentalHealthReportEnabled)
  {
    this.mentalHealthReportEnabled = mentalHealthReportEnabled;
  }

  public Boolean getAutoAdministrationChartingEnabled()
  {
    return autoAdministrationChartingEnabled;
  }

  public void setAutoAdministrationChartingEnabled(final Boolean autoAdministrationChartingEnabled)
  {
    this.autoAdministrationChartingEnabled = autoAdministrationChartingEnabled;
  }

  public Boolean getSupplyPresent()
  {
    return supplyPresent;
  }

  public void setSupplyPresent(final Boolean supplyPresent)
  {
    this.supplyPresent = supplyPresent;
  }

  public Boolean getShowHeparinPane()
  {
    return showHeparinPane;
  }

  public void setShowHeparinPane(final Boolean showHeparinPane)
  {
    this.showHeparinPane = showHeparinPane;
  }

  public Boolean getCumulativeAntipsychoticDoseEnabled()
  {
    return cumulativeAntipsychoticDoseEnabled;
  }

  public void setCumulativeAntipsychoticDoseEnabled(final Boolean cumulativeAntipsychoticDoseEnabled)
  {
    this.cumulativeAntipsychoticDoseEnabled = cumulativeAntipsychoticDoseEnabled;
  }

  public Boolean getInfusionBagEnabled()
  {
    return infusionBagEnabled;
  }

  public void setInfusionBagEnabled(final Boolean infusionBagEnabled)
  {
    this.infusionBagEnabled = infusionBagEnabled;
  }

  public String getOutpatientPrescriptionType()
  {
    return outpatientPrescriptionType;
  }

  public void setOutpatientPrescriptionType(final String outpatientPrescriptionType)
  {
    this.outpatientPrescriptionType = outpatientPrescriptionType;
  }

  public Boolean getFormularyFilterEnabled()
  {
    return formularyFilterEnabled;
  }

  public void setFormularyFilterEnabled(final Boolean formularyFilterEnabled)
  {
    this.formularyFilterEnabled = formularyFilterEnabled;
  }

  public Boolean getDoctorReviewEnabled()
  {
    return doctorReviewEnabled;
  }

  public void setDoctorReviewEnabled(final Boolean doctorReviewEnabled)
  {
    this.doctorReviewEnabled = doctorReviewEnabled;
  }

  public Boolean getDoseRangeEnabled()
  {
    return doseRangeEnabled;
  }

  public void setDoseRangeEnabled(final Boolean doseRangeEnabled)
  {
    this.doseRangeEnabled = doseRangeEnabled;
  }

  public Boolean getSearchStartMustMatch()
  {
    return searchStartMustMatch;
  }

  public void setSearchStartMustMatch(final Boolean searchStartMustMatch)
  {
    this.searchStartMustMatch = searchStartMustMatch;
  }

  public Boolean getReferenceWeightRequired()
  {
    return referenceWeightRequired;
  }

  public void setReferenceWeightRequired(final Boolean referenceWeightRequired)
  {
    this.referenceWeightRequired = referenceWeightRequired;
  }

  public String getOrganizationCode()
  {
    return organizationCode;
  }

  public void setOrganizationCode(final String organizationCode)
  {
    this.organizationCode = organizationCode;
  }

  public Integer getAdministrationTaskCreationDays()
  {
    return administrationTaskCreationDays;
  }

  public void setAdministrationTaskCreationDays(final Integer administrationTaskCreationDays)
  {
    this.administrationTaskCreationDays = administrationTaskCreationDays;
  }

  public Boolean getDiseaseCodeTranslationRequired()
  {
    return diseaseCodeTranslationRequired;
  }

  public void setDiseaseCodeTranslationRequired(final Boolean diseaseCodeTranslationRequired)
  {
    this.diseaseCodeTranslationRequired = diseaseCodeTranslationRequired;
  }

  public Boolean getAllergyCodeTranslationRequired()
  {
    return allergyCodeTranslationRequired;
  }

  public void setAllergyCodeTranslationRequired(final Boolean allergyCodeTranslationRequired)
  {
    this.allergyCodeTranslationRequired = allergyCodeTranslationRequired;
  }

  public Boolean getSubstituteAdministrationMedicationEnabled()
  {
    return substituteAdministrationMedicationEnabled;
  }

  public void setSubstituteAdministrationMedicationEnabled(final Boolean substituteAdministrationMedicationEnabled)
  {
    this.substituteAdministrationMedicationEnabled = substituteAdministrationMedicationEnabled;
  }

  public boolean getAntimicrobialDaysCountStartsWithOne()
  {
    return antimicrobialDaysCountStartsWithOne;
  }

  public void setAntimicrobialDaysCountStartsWithOne(final boolean antimicrobialDaysCountStartsWithOne)
  {
    this.antimicrobialDaysCountStartsWithOne = antimicrobialDaysCountStartsWithOne;
  }

  public Integer getPrintPastAdministrationDays()
  {
    return printPastAdministrationDays;
  }

  public void setPrintPastAdministrationDays(final @NonNull Integer printPastAdministrationDays)
  {
    this.printPastAdministrationDays = printPastAdministrationDays;
  }

  public String getInrResultCode()
  {
    return inrResultCode;
  }

  public void setInrResultCode(final String inrResultCode)
  {
    this.inrResultCode = inrResultCode;
  }

  public String getApttrResultCode()
  {
    return apttrResultCode;
  }

  public void setApttrResultCode(final String apttrResultCode)
  {
    this.apttrResultCode = apttrResultCode;
  }

  public Boolean getPresetPastAdministrationTimeToNow()
  {
    return presetPastAdministrationTimeToNow;
  }

  public void setPresetPastAdministrationTimeToNow(final Boolean presetPastAdministrationTimeToNow)
  {
    this.presetPastAdministrationTimeToNow = presetPastAdministrationTimeToNow;
  }

  public Boolean getDoseCalculationsEnabled()
  {
    return doseCalculationsEnabled;
  }

  public void setDoseCalculationsEnabled(final Boolean doseCalculationsEnabled)
  {
    this.doseCalculationsEnabled = doseCalculationsEnabled;
  }

  public Integer getPharmacistReviewDisplayDays()
  {
    return pharmacistReviewDisplayDays;
  }

  public void setPharmacistReviewDisplayDays(final Integer pharmacistReviewDisplayDays)
  {
    this.pharmacistReviewDisplayDays = pharmacistReviewDisplayDays;
  }

  public Boolean getSingleDayTherapiesOverviewEnabled()
  {
    return singleDayTherapiesOverviewEnabled;
  }

  public void setSingleDayTherapiesOverviewEnabled(final Boolean singleDayTherapiesOverviewEnabled)
  {
    this.singleDayTherapiesOverviewEnabled = singleDayTherapiesOverviewEnabled;
  }

  public Boolean getSuspendReasonMandatory()
  {
    return suspendReasonMandatory;
  }

  public void setSuspendReasonMandatory(final Boolean suspendReasonMandatory)
  {
    this.suspendReasonMandatory = suspendReasonMandatory;
  }

  public Boolean getStopReasonMandatory()
  {
    return stopReasonMandatory;
  }

  public void setStopReasonMandatory(final Boolean stopReasonMandatory)
  {
    this.stopReasonMandatory = stopReasonMandatory;
  }

  public Integer getCompletedTherapiesShownMinutes()
  {
    return completedTherapiesShownMinutes;
  }

  public void setCompletedTherapiesShownMinutes(final Integer completedTherapiesShownMinutes)
  {
    this.completedTherapiesShownMinutes = completedTherapiesShownMinutes;
  }

  public boolean isDuplicateTherapyWarningOverrideRequired()
  {
    return duplicateTherapyWarningOverrideRequired;
  }

  public void setDuplicateTherapyWarningOverrideRequired(final boolean duplicateTherapyWarningOverrideRequired)
  {
    this.duplicateTherapyWarningOverrideRequired = duplicateTherapyWarningOverrideRequired;
  }

  public Map<String, Object> getProperties()
  {

    final Map<String, Object> properties = new HashMap<>();
    properties.put("pharmacistReviewReferBackPreset", pharmacistReviewReferBackPreset);
    properties.put("mentalHealthReportEnabled", mentalHealthReportEnabled);
    properties.put("autoAdministrationCharting", autoAdministrationChartingEnabled);
    properties.put("medicationsSupplyPresent", supplyPresent);
    properties.put("medicationsShowHeparinPane", showHeparinPane);
    properties.put("cumulativeAntipsychoticDoseEnabled", cumulativeAntipsychoticDoseEnabled);
    properties.put("infusionBagEnabled", infusionBagEnabled);
    properties.put("outpatientPrescriptionType", outpatientPrescriptionType);
    properties.put("formularyFilterEnabled", formularyFilterEnabled);
    properties.put("doctorReviewEnabled", doctorReviewEnabled);
    properties.put("doseRangeEnabled", doseRangeEnabled);
    properties.put("referenceWeightRequired", referenceWeightRequired);
    properties.put("substituteAdministrationMedicationEnabled", substituteAdministrationMedicationEnabled);
    properties.put("antimicrobialDaysCountStartsWithOne", antimicrobialDaysCountStartsWithOne);
    properties.put("inrResultCode", inrResultCode);
    properties.put("apttrResultCode", apttrResultCode);
    properties.put("presetPastAdministrationTimeToNow", presetPastAdministrationTimeToNow);
    properties.put("doseCalculationsEnabled", doseCalculationsEnabled);
    properties.put("pharmacistReviewDisplayDays", pharmacistReviewDisplayDays);
    properties.put("singleDayTherapiesOverviewEnabled", singleDayTherapiesOverviewEnabled);
    properties.put("suspendReasonMandatory", suspendReasonMandatory);
    properties.put("stopReasonMandatory", stopReasonMandatory);
    properties.put("completedTherapiesShownMinutes", completedTherapiesShownMinutes);
    properties.put("duplicateTherapyWarningOverrideRequired", duplicateTherapyWarningOverrideRequired);

    return properties;
  }
}
