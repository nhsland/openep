(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .factory('tm.angular.medications.documentation.data.models.MentalHealthTemplate',
          mentalHealthTemplateFactory);

  function mentalHealthTemplateFactory()
  {
    /**
     * Constructs a new MentalHealthTemplate object.
     * @constructor
     */
    function MentalHealthTemplate(id)
    {
      this.id = id;
    }
    MentalHealthTemplate.fromJsonObject = fromJsonObject;
    MentalHealthTemplate.prototype = {
      getId: getId,
      getName: getName,
      getRoute: getRoute,
      getRouteName: getRouteName
    };

    return MentalHealthTemplate;

    /**
     * Helper method to convert a json object to a {@link MentalHealthTemplate} instance.
     * @param jsonObject
     */
    function fromJsonObject(jsonObject)
    {
      var mentalHealthTemplate = new MentalHealthTemplate(jsonObject.id);

      angular.extend(mentalHealthTemplate, jsonObject);

      return mentalHealthTemplate;
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