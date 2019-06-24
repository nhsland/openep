(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.ExternalPrescriptionPackage',
          externalPrescriptionPackageFactory);

  function externalPrescriptionPackageFactory(ExternalPrescriptionTherapy, prescriptionDocumentTypeEnum)
  {
    /**
     * Constructs a new ExternalPrescriptionPackage object.
     * @param {String} prescriptionPackageId
     * @param {Date} prescriptionDate
     * @param {String} externalPrescriptionTherapies
     * @constructor
     */
    function ExternalPrescriptionPackage(prescriptionPackageId, prescriptionDate, externalPrescriptionTherapies)
    {
      this.prescriptionPackageId = prescriptionPackageId;
      this.externalPrescriptionTherapies = externalPrescriptionTherapies ? externalPrescriptionTherapies : [];
      this.prescriptionDate = prescriptionDate;
    }

    ExternalPrescriptionPackage.fromJsonObject = fromJsonObject;
    ExternalPrescriptionPackage.prototype = {
      isAuthorized: isAuthorized,
      getPrescriptionTherapiesByDocumentType: getPrescriptionTherapiesByDocumentType,
      getPrescriptionTherapiesWithWhiteDocumentType: getPrescriptionTherapiesWithWhiteDocumentType,
      getPrescriptionTherapiesWithGreenDocumentType: getPrescriptionTherapiesWithGreenDocumentType,
      getCompositionUid: getCompositionUid,
      getPrescriptionPackageId: getPrescriptionPackageId,
      getExternalPrescriptionTherapies: getExternalPrescriptionTherapies,
      getPrescriptionDate: getPrescriptionDate,
      setPrescriptionDate: setPrescriptionDate
    };

    return ExternalPrescriptionPackage;

    /**
     * *  External prescriptions are  authorized. Returns true because of header component.
     * @returns {boolean}
     */

    function isAuthorized()
    {
      return true;
    }

    /**
     * Returns an array of PrescriptionTherapy objects. If none exists, returns an empty array.
     * @returns {Array}
     */

    function getPrescriptionTherapiesByDocumentType(documentType)
    {
      return this.externalPrescriptionTherapies.filter(function(externalPrescriptionTherapy)
      {
        return externalPrescriptionTherapy.getPrescriptionDocumentType() === documentType;
      });
    }

    /**
     * Returns an array of PrescriptionTherapy objects with the white document type prescription.
     * If none exists, returns an empty array.
     * @returns {Array}
     */
    function getPrescriptionTherapiesWithWhiteDocumentType()
    {
      return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.WHITE);
    }

    /**
     * Returns an array of PrescriptionTherapy objects with the green document type prescription.
     * If none exists, returns an empty array.
     * @returns {Array}
     */
    function getPrescriptionTherapiesWithGreenDocumentType()
    {
      return this.getPrescriptionTherapiesByDocumentType(prescriptionDocumentTypeEnum.GREEN);
    }

    /**
     * @returns {String}
     */
    function getCompositionUid()
    {
      return this.compositionUid;
    }

    /**
     * @returns {String}
     */
    function getPrescriptionPackageId()
    {
      return this.prescriptionPackageId;
    }

    /**
     * @returns {Array|*}
     */
    function getExternalPrescriptionTherapies()
    {
      return this.externalPrescriptionTherapies;
    }

    /**
     * @returns {Date}
     */
    function getPrescriptionDate()
    {
      return this.prescriptionDate;
    }

    /**
     * @param {Date} value
     */
    function setPrescriptionDate(value)
    {
      this.prescriptionDate = value;
    }

    /**
     * Helper method to return a new instance of {@link ExternalPrescriptionPackage} based on a JSON object.
     * @param {Object} jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var externalPrescriptionPackage = new ExternalPrescriptionPackage();
      if (jsonObject)
      {
        angular.extend(externalPrescriptionPackage, jsonObject);
        if (angular.isDefined(jsonObject.externalPrescriptionTherapies) && angular.isArray(jsonObject.externalPrescriptionTherapies))
        {
          externalPrescriptionPackage.externalPrescriptionTherapies = jsonObject.externalPrescriptionTherapies.map(function(therapiesObject)
          {
            return ExternalPrescriptionTherapy.fromJsonObject(therapiesObject);
          });
        }
        if (jsonObject.prescriptionDate)
        {
          externalPrescriptionPackage.setPrescriptionDate(new Date(jsonObject.prescriptionDate));
        }
      }
      return externalPrescriptionPackage;
    }
  }

  externalPrescriptionPackageFactory.$inject = ['tm.angular.medications.documentation.data.models.ExternalPrescriptionTherapy', 'tm.angular.medications.documentation.data.models.PrescriptionDocumentTypeEnum'];
})();