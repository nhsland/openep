(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.MentalHealthMedication',
          mentalHealthMedicationFactory);

  function mentalHealthMedicationFactory()
  {
    /**
     * Constructs a new MentalHealthMedication object.
     * @constructor
     */
    function MentalHealthMedication(id)
    {
      this.id = id;
    }

    MentalHealthMedication.fromJsonObject = fromJsonObject;
    MentalHealthMedication.prototype = {
      getGenericName: getGenericName,
      getName: getName,
      getRoute: getRoute,
      getRouteName: getRouteName,
      getId: getId
    };

    return MentalHealthMedication;

    /**
     * Helper method to convert a json object to a {@link MentalHealthMedication} instance.
     * @param jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var mentalHealthMedication = new MentalHealthMedication(jsonObject.id);

      angular.extend(mentalHealthMedication, jsonObject);

      return mentalHealthMedication;
    }

    /**
     * @returns {string}
     */
    function getGenericName()
    {
      return this.genericName;
    }

    /**
     * @returns {string}
     */
    function getName()
    {
      return this.name;
    }

    /**
     * @returns {Object}
     */
    function getRoute()
    {
      return this.route;
    }

    /**
     * @returns {String}
     */
    function getRouteName()
    {
      return this.route.name;
    }

    /**
     * @returns {string}
     */
    function getId()
    {
      return this.id;
    }
  }
})();