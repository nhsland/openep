(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.PrescriptionLocalDetails',
          ['tm.angular.medications.documentation.data.models.PrescriptionDocumentTypeEnum', function(PrescriptionDocumentTypeEnum)
          {
            function PrescriptionLocalDetails(prescriptionDocumentType, illnessConditionType, instructionsToPharmacist,
                                              prescriptionRepetition, remainingDispenses, maxDoseExceeded, doNotSwitch,
                                              magistralPreparation, prescriptionSystem, urgent)
            {
              this.prescriptionDocumentType = prescriptionDocumentType;
              this.illnessConditionType = illnessConditionType;
              this.instructionsToPharmacist = instructionsToPharmacist;
              this.prescriptionRepetition = prescriptionRepetition;
              this.remainingDispenses = remainingDispenses;
              this.maxDoseExceeded = maxDoseExceeded;
              this.doNotSwitch = doNotSwitch;
              this.magistralPreparation = magistralPreparation;
              this.prescriptionSystem = prescriptionSystem;
              this.urgent = urgent;
            }

            PrescriptionLocalDetails.fromJsonObject = function(object)
            {
              var prescriptionLocalDetails = new PrescriptionLocalDetails();
              if (angular.isDefined(object))
              {
                angular.extend(prescriptionLocalDetails, object);
              }
              return prescriptionLocalDetails;
            };

            PrescriptionLocalDetails.prototype.isMaxDoseExceeded = function()
            {
              return this.maxDoseExceeded === true;
            };

            PrescriptionLocalDetails.prototype.isDoNotSwitch = function()
            {
              return this.doNotSwitch === true;
            };

            PrescriptionLocalDetails.prototype.getIllnessConditionType = function()
            {
              return this.illnessConditionType;
            };

            PrescriptionLocalDetails.prototype.isMagistralPreparation = function()
            {
              return this.magistralPreparation === true;
            };

            PrescriptionLocalDetails.prototype.isUrgent = function()
            {
              return this.urgent === true;
            };
            
            PrescriptionLocalDetails.prototype.getPrescriptionRepetition = function()
            {
              return this.prescriptionRepetition || 0;
            };

            PrescriptionLocalDetails.prototype.getPrescriptionDocumentType = function()
            {
              return this.prescriptionDocumentType;
            };

            PrescriptionLocalDetails.prototype.getRemainingDispenses = function()
            {
              return this.remainingDispenses;
            };

            PrescriptionLocalDetails.prototype.getInstructionsToPharmacist = function()
            {
              return this.instructionsToPharmacist;
            };

            PrescriptionLocalDetails.prototype.isPrescriptionDocumentTypeWhite = function()
            {
              return this.prescriptionDocumentType === PrescriptionDocumentTypeEnum.WHITE;
            };

            PrescriptionLocalDetails.prototype.isPrescriptionDocumentTypeGreen = function()
            {
              return this.prescriptionDocumentType === PrescriptionDocumentTypeEnum.GREEN;
            };

            return PrescriptionLocalDetails;
          }]);
})();