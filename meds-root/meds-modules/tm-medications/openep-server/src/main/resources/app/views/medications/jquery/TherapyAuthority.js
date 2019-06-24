/*
 * Copyright (c) 2010-2015 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */
Class.define('app.views.medications.TherapyAuthority', 'tm.jquery.Object', {

  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._therapyReportEnabled = this.getView().getProperty('therapyReportEnabled');
    this._gridViewEnabled = this.getView().getProperty('gridViewEnabled');
    this._timelineViewEnabled = this.getView().getProperty('timelineViewEnabled');
    this._pharmacistReviewViewEnabled = this.getView().getProperty('pharmacistReviewViewEnabled');
    this._managePatientPharmacistReviewAllowed = this.getView().getProperty('managePatientPharmacistReviewAllowed');
    this._showPharmacistReviewStatus = this.getView().getProperty('showPharmacistReviewStatus');
    this._medicationSummaryViewEnabled = this.getView().getProperty('medicationSummaryViewEnabled');
    this._medicationConsentT2T3Allowed = this.getView().getProperty('medicationConsentT2T3Allowed');
    this._medicationDocumentViewEnabled = this.getView().getProperty('medicationDocumentViewEnabled');
    this._addMedicationToPreparationTasklistAllowed = this.getView().getProperty('addMedicationToPreparationTasklistAllowed');
    this._nonFormularyMedicationSearchAllowed = this.getView().getProperty('nonFormularyMedicationSearchAllowed');
    this._managePatientTemplatesAllowed = this.getView().getProperty('managePatientTemplatesAllowed');
    this._manageOrganizationalTemplatesAllowed = this.getView().getProperty('manageOrganizationalTemplatesAllowed');
    this._manageUserTemplatesAllowed = this.getView().getProperty('manageUserTemplatesAllowed');
    this._manageInpatientPrescriptionsAllowed = this.getView().getProperty('manageInpatientPrescriptionsAllowed');
    this._manageOutpatientPrescriptionsAllowed = this.getView().getProperty('manageOutpatientPrescriptionsAllowed');
    this._copyPrescriptionAllowed = this.getView().getProperty('copyPrescriptionAllowed');
    this._suspendPrescriptionAllowed = this.getView().getProperty('suspendPrescriptionAllowed');
    this._restartSuspendPrescriptionAllowed = this.getView().getProperty('restartSuspendPrescriptionAllowed');
    this._doctorReviewAllowed = this.getView().getProperty('doctorReviewAllowed');
    this._startSelfAdministrationAllowed = this.getView().getProperty('startSelfAdministrationAllowed');
    this._scheduleAdditionalAdministrationAllowed = this.getView().getProperty('scheduleAdditionalAdministrationAllowed');
    this._recordPrnAdministrationAllowed = this.getView().getProperty('recordPrnAdministrationAllowed');
    this._createResupplyRequestAllowed = this.getView().getProperty('createResupplyRequestAllowed');
    this._manageAdministrationsAllowed = this.getView().getProperty('manageAdministrationsAllowed');
    this._rescheduleAdministrationsAllowed = this.getView().getProperty('rescheduleAdministrationsAllowed');
    this._medicationIdentifierScanningAllowed = this.getView().getProperty('medicationIdentifierScanningAllowed');
    this._manageMedicationOnAdmissionAllowed = this.getView().getProperty('manageMedicationOnAdmissionAllowed');
    this._manageMedicationOnDischargeAllowed = this.getView().getProperty('manageMedicationOnDischargeAllowed');
    this._medicationReconciliationReviewAllowed = this.getView().getProperty('medicationReconciliationReviewAllowed');
    this._manageAllTemplatesAllowed = this.getView().getProperty('manageAllTemplatesAllowed');
    this._prescribeByTemplatesAllowed = this.getView().getProperty('prescribeByTemplatesAllowed');
    this._suspendAllForTemporaryLeaveAllowed = this.getView().getProperty('temporaryLeaveAllowed');
    this._stopAllPrescriptionsAllowed = this.getView().getProperty('stopAllPrescriptionsAllowed');
    this._userWitnessingRequired = this.getView().getProperty('userWitnessingRequired');
    this._recordAdditionalAdministrationAllowed = this.getView().getProperty('recordAdditionalAdministrationAllowed');
    this._defineTitrationDoseAllowed = this.getView().getProperty('defineTitrationDoseAllowed');
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {boolean}
   */
  isTherapyReportEnabled: function()
  {
    return this._therapyReportEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isGridViewEnabled: function()
  {
    return this._gridViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isTimelineViewEnabled: function()
  {
    return this._timelineViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isPharmacistReviewViewEnabled: function()
  {
    return this._pharmacistReviewViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isManagePatientPharmacistReviewAllowed: function()
  {
    return this._managePatientPharmacistReviewAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isShowPharmacistReviewStatus: function()
  {
    return this._showPharmacistReviewStatus === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationSummaryViewEnabled: function()
  {
    return this._medicationSummaryViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationConsentT2T3Allowed: function()
  {
    return this._medicationConsentT2T3Allowed === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationDocumentViewEnabled: function()
  {
    return this._medicationDocumentViewEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isAddMedicationToPreparationTasklistAllowed: function()
  {
    return this._addMedicationToPreparationTasklistAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isNonFormularyMedicationSearchAllowed: function()
  {
    return this._nonFormularyMedicationSearchAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManagePatientTemplatesAllowed: function()
  {
    return this._managePatientTemplatesAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageOrganizationalTemplatesAllowed: function()
  {
    return this._manageOrganizationalTemplatesAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageUserTemplatesAllowed: function()
  {
    return this._manageUserTemplatesAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageAnyTemplatesAllowed: function()
  {
    return this.isManageUserTemplatesAllowed() ||
        this.isManagePatientTemplatesAllowed() ||
        this.isManageOrganizationalTemplatesAllowed();
  },

  /**
   * @returns {boolean}
   */
  isManageInpatientPrescriptionsAllowed: function()
  {
    return this._manageInpatientPrescriptionsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageOutpatientPrescriptionsAllowed: function()
  {
    return this._manageOutpatientPrescriptionsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyPrescriptionAllowed: function()
  {
    return this._copyPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isSuspendPrescriptionAllowed: function()
  {
    return this._suspendPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */

  isRestartSuspendPrescriptionAllowed: function()
  {
    return this._restartSuspendPrescriptionAllowed === true;
  },

  /**
   * @returns {boolean}
   */

  isDoctorReviewAllowed: function()
  {
    return this._doctorReviewAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isStartSelfAdministrationAllowed: function()
  {
    return this._startSelfAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isScheduleAdditionalAdministrationAllowed: function()
  {
    return this._scheduleAdditionalAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isRecordPrnAdministrationAllowed: function()
  {
    return this._recordPrnAdministrationAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isCreateResupplyRequestAllowed: function()
  {
    return this._createResupplyRequestAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isManageAdministrationsAllowed: function()
  {
    return this._manageAdministrationsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isRescheduleAdministrationsAllowed: function()
  {
    return this._rescheduleAdministrationsAllowed === true;
  },

  /**
   * @returns {boolean}
   */
  isMedicationIdentifierScanningAllowed: function()
  {
    return this._medicationIdentifierScanningAllowed === true;
  },

  /**
   * Can the user start a new reconciliation process and start or edit the medication on admission list?
   * @returns {boolean}
   */
  isManageMedicationOnAdmissionAllowed: function()
  {
    return this._manageMedicationOnAdmissionAllowed === true;
  },

  /**
   * Can the user start or edit the medication on discharge list?
   * @return {boolean}
   */
  isManageMedicationOnDischargeAllowed: function()
  {
    return this._manageMedicationOnDischargeAllowed === true;
  },

  /**
   * Can the user review the medication on admission or medication on discharge list?
   * @return {boolean}
   */
  isMedicationReconciliationReviewAllowed: function()
  {
    return this._medicationReconciliationReviewAllowed === true;
  },

  /**
   * Can the user manage inpatient and outpatient templates via the manage templates dialog?
   * @return {boolean}
   */
  isManageAllTemplatesAllowed: function()
  {
    return this._manageAllTemplatesAllowed === true;
  },

  /**
   * Can the user prescribe, both inpatient and outpatient therapies, based on existing templates? Be advised
   * that {@link #_manageInpatientPrescriptionsAllowed} and/or {@link #_manageOutpatientPrescriptionsAllowed} overrides
   * this permission in the appropriate context.
   * @return {boolean}
   */
  isPrescribeByTemplatesAllowed: function()
  {
    return this._prescribeByTemplatesAllowed === true;
  },

  /**
   * Can user suspend all therapies for patient's temporary leave?
   * @returns {boolean}
   */
  isSuspendAllForTemporaryLeaveAllowed: function()
  {
    return this._suspendAllForTemporaryLeaveAllowed === true;
  },

  /**
   * Can user stop all patient's therapies?
   * @returns {boolean}
   */
  isStopAllPrescriptionsAllowed: function()
  {
    return this._stopAllPrescriptionsAllowed === true;
  },

  /**
   * @returns {boolean} true, if the user require a witness when administering medications? Requires the server configuration
   * to have witnessing functionality enabled.
   */
  isUserWitnessingRequired: function()
  {
    return this._userWitnessingRequired === true;
  },

  /**
   * Can user record additional administration? Only applies to therapies that are not {@link Therapy#whenNeeded}
   * @returns {boolean}
   */
  isRecordAdditionalAdministrationAllowed: function()
  {
    return this._recordAdditionalAdministrationAllowed === true;
  },

  /**
   * Can user define dose or rate for titrated therapy administration?
   * @returns {boolean}
   */
  isDefineTitrationDoseAllowed: function()
  {
    return this._defineTitrationDoseAllowed === true;
  }
});