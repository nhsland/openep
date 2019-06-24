Class.define('app.views.medications.ordering.dto.CustomTemplatesGroup', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (!jsonObject)
      {
        return null;
      }

      return new app.views.medications.ordering.dto.CustomTemplatesGroup({
        group: jsonObject.group,
        customTemplates: tm.jquery.Utils.isArray(jsonObject.customTemplates) ?
            jsonObject.customTemplates.map(app.views.medications.ordering.dto.TherapyTemplate.fromJson) :
            []
      });
    }
  },

  group: null,
  customTemplates: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.customTemplates = this.customTemplates || [];
  },

  /**
   * @return {string|null}
   */
  getGroup: function()
  {
    return this.group;
  },

  /**
   * @return {Array<app.views.medications.ordering.dto.TherapyTemplate>}
   */
  getCustomTemplates: function()
  {
    return this.customTemplates;
  }
});
