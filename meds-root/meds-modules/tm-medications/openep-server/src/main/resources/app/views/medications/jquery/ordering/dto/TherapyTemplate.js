Class.define('app.views.medications.ordering.dto.TherapyTemplate', 'tm.jquery.Object', {
  id: null,
  name: null,
  type: null,
  userId: null,
  careProviderId: null,
  patientId: null,
  templateElements: null,
  group: null,
  preconditions: null,

  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);

      if (tm.jquery.Utils.isArray(config.templateElements))
      {
        config.templateElements = jsonObject.templateElements.map(
            app.views.medications.ordering.dto.TherapyTemplateElement.fromJson)
      }
      if (tm.jquery.Utils.isArray(config.preconditions))
      {
        config.preconditions = jsonObject.preconditions.map(function mapToPrecondition(precondition)
        {
          return new app.views.medications.ordering.dto.TherapyTemplatePrecondition(precondition);
        })
      }
      return new app.views.medications.ordering.dto.TherapyTemplate(config)
    }
  },

  Constructor: function(config)
  {
    this.callSuper(config);
    this.templateElements = tm.jquery.Utils.isArray(this.templateElements) ? this.templateElements : [];
    this.preconditions = tm.jquery.Utils.isArray(this.preconditions) ? this.preconditions : [];
  },

  /**
   * @returns {Number|null}
   */
  getId: function()
  {
    return this.id;
  },

  /**
   * @param {Number|null} id
   */
  setId: function(id)
  {
    this.id = id;
  },

  /**
   * @returns {String|null}
   */
  getName: function()
  {
    return this.name;
  },

  /**
   * @param {String|null} name
   */
  setName: function(name)
  {
    this.name = name;
  },

  /**
   * @returns {app.views.medications.TherapyEnums.templateTypeEnum|null}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @param {app.views.medications.TherapyEnums.templateTypeEnum|null} type
   */
  setType: function(type)
  {
    this.type = type;
  },

  /**
   * @returns {String|null}
   */
  getUserId: function()
  {
    return this.userId;
  },

  /**
   * @param {String|null} userId
   */
  setUserId: function(userId)
  {
    this.userId = userId;
  },

  /**
   * @returns {String|null}
   */
  getCareProviderId: function()
  {
    return this.careProviderId;
  },

  /**
   * @param {String|null} careProviderId
   */
  setCareProviderId: function(careProviderId)
  {
    this.careProviderId = careProviderId;
  },

  /**
   * @returns {String|null}
   */
  getPatientId: function()
  {
    return this.patientId;
  },

  /**
   * @param {String|null} patientId
   */
  setPatientId: function(patientId)
  {
    this.patientId = patientId;
  },

  /**
   * @returns {String|null}
   */
  getGroup: function()
  {
    return this.group;
  },

  /**
   * @param {String|null} group
   */
  setGroup: function(group)
  {
    this.group = group;
  },

  /**
   * @returns {Array<app.views.medications.ordering.dto.TherapyTemplateElement>}
   */
  getTemplateElements: function()
  {
    return this.templateElements;
  },

  /**
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplateElement>} templateElements
   */
  setTemplateElements: function(templateElements)
  {
    this.templateElements = tm.jquery.Utils.isArray(templateElements) ? templateElements : [];
  },

  /**
   * @returns {Array<app.views.medications.ordering.dto.TherapyTemplatePrecondition>}
   */
  getPreconditions: function()
  {
    return this.preconditions;
  },

  /**
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplatePrecondition>} preconditions
   */
  setPreconditions: function(preconditions)
  {
    this.preconditions = tm.jquery.Utils.isArray(preconditions) ? preconditions : [];
  },

  /**
   * True, if any preconditions for this template exist, otherwise false.
   * @return {boolean}
   */
  hasPreconditions: function()
  {
    return this.preconditions.length > 0;
  }
});