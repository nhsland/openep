(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data')
      .service('tm.angular.medications.documentation.data.DocumentRestApi', ['$q', '$resource',
        'tm.angular.medications.documentation.data.models.TherapyDocuments',
        'tm.angular.medications.documentation.data.models.TherapyDocument',
        function($q, $resource, TherapyDocuments, TherapyDocument)
        {
          /* exposing public methods and properties */
          var restApi = this;
          restApi.getDocuments = getDocuments;
          restApi.getOutpatientPrescriptionHandout = getOutpatientPrescriptionHandout;

          /**
           * @returns {$resource}
           */
          function getDocumentResource()
          {
            return $resource(
                'medications/getTherapyDocuments/?patientId=:patientId&recordCount=:recordCount&recordOffset=:recordOffset',
                {patientId: '@careProviderIds', recordCount: '@recordCount', recordOffset: '@recordOffset'},
                {
                  get: {method: 'GET', isArray: false}
                });
          }

          /**
           * @return {$resource}
           */
          function getOutpatientPrescriptionHandoutResource()
          {
            return $resource(
                'medications/getOutpatientPrescriptionHandout/?patientId=:patientId&compositionUid=:compositionUid',
                {patientId: '@careProviderIds', compositionUid: '@compositionUid'},
                {
                  get: {
                    method: 'GET', responseType: 'arraybuffer',
                    transformResponse: function(data)
                    {
                      return {content: data};
                    }
                  }
                });
          }

          /**
           * Retrieves the specified number of documents, with the given offset, for the specified patientId.
           * @param patientId
           * @param recordCount
           * @param recordOffset
           * @returns {TherapyDocuments}
           */
          function getDocuments(patientId, recordCount, recordOffset)
          {
            var deferred = $q.defer();
            getDocumentResource().get(
                {
                  patientId: patientId,
                  recordCount: recordCount,
                  recordOffset: recordOffset
                },
                function(response)
                {
                  // fake transform from json
                  var documentResponse = null;
                  if (angular.isDefined(response))
                  {
                    documentResponse = new TherapyDocuments(patientId, recordCount,
                        recordOffset, response.moreRecordsExist, response.documents.map(function(object)
                        {
                          return TherapyDocument.fromJsonObject(object);
                        }));
                  }
                  deferred.resolve(documentResponse);
                },
                function(response)
                {
                  deferred.reject(response);
                });
            return deferred.promise;
          }

          /**
           * Retrieves the contents of a generated PDF for the outpatient prescription handout.
           * @param {string} patientId
           * @param {string} compositionUid
           * @return {object} of type ArrayBuffer
           */
          function getOutpatientPrescriptionHandout(patientId, compositionUid)
          {
            var deferred = $q.defer();
            getOutpatientPrescriptionHandoutResource().get(
                {patientId: patientId, compositionUid: compositionUid},
                function onGetOutpatientPrescriptionHandoutSuccess(response)
                {
                  if (!response.content || response.content.byteLength === 0)
                  {
                    deferred.reject();
                  }

                  deferred.resolve(response.content);
                },
                function onGetOutpatientPrescriptionHandoutFailure()
                {
                  deferred.reject();
                });
            return deferred.promise;
          }
        }]);
})();