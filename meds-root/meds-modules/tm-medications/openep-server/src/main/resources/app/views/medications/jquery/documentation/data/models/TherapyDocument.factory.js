(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.TherapyDocument',
          ['tm.angular.medications.documentation.data.models.PrescriptionPackage',
            'tm.angular.medications.documentation.data.models.ExternalPrescriptionPackage',
            'tm.angular.medications.documentation.data.models.MentalHealthDocumentContent',
            'tm.angular.medications.documentation.data.models.TherapyDocumentType',
            function(PrescriptionPackage, ExternalPrescriptionPackage, MentalHealthDocumentContent, TherapyDocumentTypeEnum)
            {
              /** Constructor */
              function TherapyDocument(documentType, createTimestamp, creator, careProvider, content)
              {
                this.documentType = documentType;
                this.createTimestamp = createTimestamp;
                this.creator = creator;
                this.careProvider = careProvider;
                if (!angular.isObject(content))
                {
                  this.content = new PrescriptionPackage();
                }
                else
                {
                  this.content = content;
                }
              }

              TherapyDocument.fromJsonObject = function(object)
              {
                var therapyDocument = new TherapyDocument();
                if (angular.isDefined(object))
                {
                  angular.extend(therapyDocument, object);
                  if (angular.isDefined(object.content))
                  {
                    if (therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.EXTERNAL_EER_PRESCRIPTION)
                    {
                      therapyDocument.content = ExternalPrescriptionPackage.fromJsonObject(object.content);
                    }
                    else if (therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.T2 ||
                        therapyDocument.getDocumentType() === TherapyDocumentTypeEnum.T3)
                    {
                      therapyDocument.content = MentalHealthDocumentContent.fromJsonObject(object.content);
                    }
                    else
                    {
                      // fallback or default
                      therapyDocument.content = PrescriptionPackage.fromJsonObject(object.content);
                    }
                  }
                }
                return therapyDocument;
              };

              /** Public methods */

              /**
               * Returns the therapy document type.
               * @returns {tm.angular.medications.documentation.data.models.TherapyDocumentType}
               */
              TherapyDocument.prototype.getDocumentType = function()
              {
                return this.documentType;
              };

              TherapyDocument.prototype.getCreatedTimestamp = function()
              {
                return this.createTimestamp;
              };

              TherapyDocument.prototype.getCreator = function()
              {
                return this.creator;
              };

              TherapyDocument.prototype.getCareProvider = function()
              {
                return this.careProvider;
              };

              TherapyDocument.prototype.getContent = function()
              {
                return this.content;
              };

              TherapyDocument.prototype.setContent = function(value)
              {
                this.content = value;
              };

              return TherapyDocument;
            }]);
})();