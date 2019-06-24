package com.marand.thinkmed.medications;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.maf.core.data.WellKnownCode;
import com.marand.maf.core.security.model.Authority;

/**
 * @author Mitja Lapajne
 */
public enum TherapyAuthorityEnum implements WellKnownCode<Authority>
{
  // View authorities

  MEDS_GRID_VIEW("gridViewEnabled"),
  MEDS_MAR_VIEW("timelineViewEnabled"),
  MEDS_SUMMARY_VIEW("medicationSummaryViewEnabled"),
  MEDS_PHARMACIST_REVIEW_VIEW("pharmacistReviewViewEnabled"),
  MEDS_DOCUMENTS_VIEW("medicationDocumentViewEnabled"),
  MEDS_SUPPLY_TASKLIST_VIEW("pharmacySupplyReviewTasklistEnabled"),
  MEDS_DISPENSE_TASKLIST_VIEW("pharmacyDispenseTasklistEnabled"),
  MEDS_PHARMACISTS_TASKLIST_VIEW("pharmacyPharmacistReviewTasklistEnabled"),
  MEDS_MEDICATION_PREPARATION_TASKLIST("medicationOnPreparationTasklistEnabled"),

  MEDS_PHARMACIST_REVIEW_STATUS("showPharmacistReviewStatus"),
  MEDS_ADD_MEDICATION_TO_PREPARATION_TASKLIST("addMedicationToPreparationTasklistAllowed"),

  MEDS_PRINT_MAR("therapyReportEnabled"),
  MEDS_MANAGE_T2_T3_CONSENT("medicationConsentT2T3Allowed"),
  MEDS_MANAGE_PATIENT_ORDER_SETS("managePatientTemplatesAllowed"),
  MEDS_MANAGE_ORGANIZATIONAL_ORDER_SETS("manageOrganizationalTemplatesAllowed"),
  MEDS_MANAGE_USER_ORDER_SETS("manageUserTemplatesAllowed"),
  MEDS_MANAGE_ALL_ORDER_SETS("manageAllTemplatesAllowed"),
  MEDS_MANAGE_INPATIENT_PRESCRIPTIONS("manageInpatientPrescriptionsAllowed"),
  MEDS_MANAGE_OUTPATIENT_PRESCRIPTIONS("manageOutpatientPrescriptionsAllowed"),
  MEDS_COPY_PRESCRIPTION("copyPrescriptionAllowed"),
  MEDS_SELECT_NON_FORMULARY_MEDICATIONS("nonFormularyMedicationSearchAllowed"),
  MEDS_SUSPEND_PRESCRIPTION("suspendPrescriptionAllowed"),
  MEDS_STOP_ALL_PRESCRIPTIONS("stopAllPrescriptionsAllowed"),
  MEDS_TEMPORARY_LEAVE("temporaryLeaveAllowed"),
  MEDS_RESTART_SUSPENDED_PRESCRIPTION("restartSuspendPrescriptionAllowed"),
  MEDS_START_SELF_ADMINISTRATION("startSelfAdministrationAllowed"),
  MEDS_SCHEDULE_ADDITIONAL_ADMINISTRATION("scheduleAdditionalAdministrationAllowed"),
  MEDS_RECORD_PRN_ADMINISTRATION("recordPrnAdministrationAllowed"),
  MEDS_CREATE_RESUPPLY_REQUEST("createResupplyRequestAllowed"),
  MEDS_MANAGE_MEDICATION_ADMINISTRATIONS("manageAdministrationsAllowed"),
  MEDS_RESCHEDULE_ADMINISTRATIONS("rescheduleAdministrationsAllowed"),
  MEDS_DOCTOR_REVIEW("doctorReviewAllowed"),
  MEDS_MANAGE_PATIENT_PHARMACISTS_REVIEW("managePatientPharmacistReviewAllowed"),
  MEDS_MEDICATION_IDENTIFIER_SCANNING("medicationIdentifierScanningAllowed"),

  MEDS_MANAGE_MEDICATION_ON_ADMISSION("manageMedicationOnAdmissionAllowed"),
  MEDS_MANAGE_MEDICATION_ON_DISCHARGE("manageMedicationOnDischargeAllowed"),
  MEDS_MEDICATION_RECONCILIATION_REVIEW("medicationReconciliationReviewAllowed"),

  MEDS_USER_WITNESSING_REQUIRED("userWitnessingRequired"),
  MEDS_RECORD_ADDITIONAL_ADMINISTRATION("recordAdditionalAdministrationAllowed"),
  MEDS_DEFINE_TITRATION_DOSE("defineTitrationDoseAllowed");

  private final String clientSideName;
  private static final String THERAPY_TEMPLATE_GROUP_AUTHORITY_PREFIX = "MEDS_ORDER_SETS_GROUP_";
  public static final String PRESCRIBE_BY_TEMPLATES_ALLOWED_CLIENT_SIDE_NAME = "prescribeByTemplatesAllowed";

  TherapyAuthorityEnum(final String clientSideName)
  {
    this.clientSideName = clientSideName;
  }

  public String getClientSideName()
  {
    return clientSideName;
  }

  @Override
  public String getCode()
  {
    return name();
  }

  @Override
  public Class<Authority> getCatalogType()
  {
    return Authority.class;
  }

  public static Set<String> getTherapyTemplateGroups(final Collection<String> authorities)
  {
    return authorities
        .stream()
        .filter(a -> a.toUpperCase().contains(THERAPY_TEMPLATE_GROUP_AUTHORITY_PREFIX))
        .map(a -> {
          final int offset = a.toUpperCase().indexOf(THERAPY_TEMPLATE_GROUP_AUTHORITY_PREFIX) + THERAPY_TEMPLATE_GROUP_AUTHORITY_PREFIX.length();
          return a.substring(offset);
        })
        .collect(Collectors.toSet());
  }

  public static boolean isPrescribeByTemplatesAllowed(final Collection<String> authorities)
  {
    return !getTherapyTemplateGroups(authorities).isEmpty();
  }

  public static Map<String, Boolean> getMatchedAuthoritiesByLowerCase(final Set<String> authorities)
  {
    return Arrays.stream(values())
        .collect(
            Collectors.toMap(
                TherapyAuthorityEnum::getClientSideName,
                a -> authorities.contains(("ROLE_" + a.getCode()).toLowerCase())
            ));
  }
}
