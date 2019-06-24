(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.ExternalPrescriptionTherapy',
          externalPrescriptionTherapyFactory);

  function externalPrescriptionTherapyFactory()
  {
    /**
     * Constructs a new ExternalPrescriptionTherapy object.
     * @constructor
     */
    function ExternalPrescriptionTherapy(prescriptionId)
    {
      this.prescriptionId = prescriptionId;
    }
    ExternalPrescriptionTherapy.fromJsonObject = fromJsonObject;
    ExternalPrescriptionTherapy.prototype = {
      getPrescriptionId: getPrescriptionId,
      getDispenseId: getDispenseId,
      getStatus: getStatus,
      getRemainingRepeats: getRemainingRepeats,
      getValidityDate: getValidityDate,
      getIssueDate: getIssueDate,
      isDeliverToDoctor: isDeliverToDoctor,
      isRenewable: isRenewable,
      getPrescriptionSupplyType: getPrescriptionSupplyType,
      getPrescribedRepeats: getPrescribedRepeats,
      getNumberOfPrescribedUnits: getNumberOfPrescribedUnits,
      getPrescribedUnit: getPrescribedUnit,
      getNotificationToDoctor: getNotificationToDoctor,
      isUrgent: isUrgent,
      isDoNotChange: isDoNotChange,
      getNotificationToPatient: getNotificationToPatient,
      getNationalMedicationCode: getNationalMedicationCode,
      getNationalPrescribedMedicationCode: getNationalPrescribedMedicationCode,
      getShortMedicationName: getShortMedicationName,
      getShortPrescribedMedicationName: getShortPrescribedMedicationName,
      getPrescriptionClassification: getPrescriptionClassification,
      getConsultation: getConsultation,
      getPrescribedMedicationQuantity: getPrescribedMedicationQuantity,
      getSurchargeType: getSurchargeType,
      getTreatmentReason: getTreatmentReason,
      getIllnessConditionType: getIllnessConditionType,
      getPrescriptionDocumentType: getPrescriptionDocumentType,
      isIssuedWithoutPrescription: isIssuedWithoutPrescription,
      isPrescribedMedicationSwitched: isPrescribedMedicationSwitched,
      getPharmacistBPI: getPharmacistBPI,
      getNotificationToPharmacist: getNotificationToPharmacist,
      getPrescribingIssuedMedicationId: getPrescribingIssuedMedicationId,
      getIssuingOrgBPI: getIssuingOrgBPI,
      getMedicationIssuingMode: getMedicationIssuingMode,
      getPrescriberBPI: getPrescriberBPI,
      getPrescribingOrgBPI: getPrescribingOrgBPI
    };

    return ExternalPrescriptionTherapy;

    /**
     * Helper method to convert a json object to a {@link ExternalPrescriptionTherapy} instance.
     * @param jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var externalPrescriptionTherapy = new ExternalPrescriptionTherapy(jsonObject.prescriptionId);
      angular.extend(externalPrescriptionTherapy, jsonObject);
      if (externalPrescriptionTherapy.getIssueDate() && !angular.isDate(externalPrescriptionTherapy.getIssueDate()))
      {
        externalPrescriptionTherapy.issueDate = new Date(externalPrescriptionTherapy.issueDate);
      }
      if (externalPrescriptionTherapy.getValidityDate() && !angular.isDate(externalPrescriptionTherapy.getValidityDate()))
      {
        externalPrescriptionTherapy.validityDate = new Date(externalPrescriptionTherapy.validityDate);
      }
      return externalPrescriptionTherapy;
    }

    /**
     * @returns {string}
     */
    function getPrescriptionId()
    {
      return this.prescriptionId;
    }

    /**
     * @returns {string}
     */
    function getDispenseId()
    {
      return this.dispenseId;
    }

    /**
     * @returns {tm.angular.medications.documentation.data.models.PrescriptionStatus}
     */
    function getStatus()
    {
      return this.status;
    }

    /**
     *
     * @returns {number}
     */
    function getRemainingRepeats()
    {
      return this.remainingRepeats;
    }

    /**
     * @returns {Date|undefined|null}
     */
    function getValidityDate()
    {
      return this.validityDate;
    }

    /**
     * @returns {Date|undefined|null}
     */
    function getIssueDate()
    {
      return this.issueDate;
    }

    /**
     * @returns {boolean}
     */
    function isDeliverToDoctor()
    {
      return this.deliverToDoctor;
    }

    /**
     * @returns {boolean}
     */
    function isRenewable()
    {
      return this.renewable;
    }

    /**
     * @returns {string}
     */
    function getPrescriptionSupplyType()
    {
      return this.prescriptionSupplyType;
    }

    /**
     * @returns {number}
     */
    function getPrescribedRepeats()
    {
      return this.prescribedRepeats;
    }

    /**
     * @returns {string}
     */
    function getNumberOfPrescribedUnits()
    {
      return this.numberOfPrescribedUnits;
    }

    /**
     * @returns {string}
     */
    function getPrescribedUnit()
    {
      return this.prescribedUnit;
    }

    /**
     * @returns {string}
     */
    function getNotificationToDoctor()
    {
      return this.notificationToDoctor;
    }

    /**
     * @returns {boolean}
     */
    function isUrgent()
    {
      return this.urgent === true;
    }

    /**
     * @returns {boolean}
     */
    function isDoNotChange()
    {
      return this.doNotChange === true;
    }

    /**
     * @returns {string}
     */
    function getNotificationToPatient()
    {
      return this.notificationToPatient;
    }

    /**
     * @returns {string}
     */
    function getNationalMedicationCode()
    {
      return this.nationalMedicationCode;
    }

    /**
     * @returns {string}
     */
    function getNationalPrescribedMedicationCode()
    {
      return this.nationalPrescribedMedicationCode;
    }

    /**
     * @returns {string}
     */
    function getShortMedicationName()
    {
      return this.shortMedicationName;
    }

    /**
     * @returns {string}
     */
    function getShortPrescribedMedicationName()
    {
      return this.shortPrescribedMedicationName;
    }

    /**
     * @returns {string}
     */
    function getPrescriptionClassification()
    {
      return this.prescriptionClassification;
    }

    /**
     * @returns {string}
     */
    function getConsultation()
    {
      return this.consultation;
    }

    /**
     * @returns {string}
     */
    function getPrescribedMedicationQuantity()
    {
      return this.prescribedMedicationQuantity;
    }

    /**
     * @returns {string}
     */
    function getSurchargeType()
    {
      return this.surchargeType;
    }

    /**
     * @returns {string}
     */
    function getTreatmentReason()
    {
      return this.treatmentReason;
    }

    /**
     * @returns {string}
     */
    function getIllnessConditionType()
    {
      return this.illnessConditionType;
    }

    /**
     * @returns {string}
     */
    function getPrescriptionDocumentType()
    {
      return this.prescriptionDocumentType;
    }

    /**
     * @returns {boolean}
     */
    function isIssuedWithoutPrescription()
    {
      return this.issuedWithoutPrescription === true;
    }

    /**
     * @returns {boolean}
     */
    function isPrescribedMedicationSwitched()
    {
      return this.prescribedMedicationSwitched === true;
    }

    /**
     * @returns {string}
     */
    function getPharmacistBPI()
    {
      return this.pharmacistBPI;
    }

    /**
     * @returns {string}
     */
    function getNotificationToPharmacist()
    {
      return this.notificationToPharmacist;
    }

    /**
     * @returns {string}
     */
    function getPrescribingIssuedMedicationId()
    {
      return this.prescribingIssuedMedicationId;
    }

    /**
     * @returns {string}
     */
    function getIssuingOrgBPI()
    {
      return this.issuingOrgBPI;
    }

    /**
     * @returns {string}
     */
    function getMedicationIssuingMode()
    {
      return this.medicationIssuingMode;
    }

    /**
     * @returns {string}
     */
    function getPrescriberBPI()
    {
      return this.prescriberBPI;
    }

    /**
     * @returns {string}
     */
    function getPrescribingOrgBPI()
    {
      return this.prescribingOrgBPI;
    }
  }
})();