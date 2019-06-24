(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.Therapy',
          ['tm.angular.medications.documentation.data.models.PrescriptionLocalDetails', function(PrescriptionLocalDetails)
          {
            function Therapy(formattedTherapyDisplay, prescriptionLocalDetails)
            {
              this.formattedTherapyDisplay = formattedTherapyDisplay;

              if (!angular.isObject(prescriptionLocalDetails))
              {
                this.prescriptionLocalDetails = new PrescriptionLocalDetails();
              }
              else
              {
                this.prescriptionLocalDetails = prescriptionLocalDetails;
              }
            }

            Therapy.fromJsonObject = function(object)
            {
              var therapy = new Therapy();
              if (angular.isDefined(object))
              {
                angular.extend(therapy, object);
                therapy.prescriptionLocalDetails = PrescriptionLocalDetails.fromJsonObject(object.prescriptionLocalDetails);
              }
              return therapy;
            };

            /**
             * Returns encoded HTML content to display for the therapy information.
             * @returns {*}
             */
            Therapy.prototype.getFormattedTherapyDisplay = function()
            {
              return this.formattedTherapyDisplay;
            };

            Therapy.prototype.getPrescriptionLocalDetails = function()
            {
              return this.prescriptionLocalDetails || {};
            };

            return Therapy;
          }]);
})();