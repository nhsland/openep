(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.TherapyDocuments', function()
      {
        /** Constructor */
        function TherapyDocuments(patientId, recordCount, recordOffset, moreRecordsExist, documents)
        {
          this.patientId = patientId;
          this.recordCount = recordCount;
          this.recordOffset = recordOffset;
          this.moreRecordsExist = moreRecordsExist;

          if (angular.isDefined(documents))
          {
            this.documents = documents;
          }
          else
          {
            this.documents = [];
          }
        }

        TherapyDocuments.prototype.hasMoreRecords = function()
        {
          return this.moreRecordsExist === true;
        };

        TherapyDocuments.prototype.getDocuments = function()
        {
          return this.documents;
        };

        TherapyDocuments.prototype.getRecordOffset = function()
        {
          return this.recordOffset;
        };

        return TherapyDocuments;
      });
})();