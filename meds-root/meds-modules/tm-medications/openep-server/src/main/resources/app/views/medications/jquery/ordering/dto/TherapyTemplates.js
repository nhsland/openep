Class.define('app.views.medications.ordering.dto.TherapyTemplates', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (!jsonObject)
      {
        return null;
      }

      return new app.views.medications.ordering.dto.TherapyTemplates({
        userTemplates: tm.jquery.Utils.isArray(jsonObject.userTemplates) ?
            jsonObject.userTemplates.map(app.views.medications.ordering.dto.TherapyTemplate.fromJson) :
            [],
        organizationTemplates: tm.jquery.Utils.isArray(jsonObject.organizationTemplates) ?
            jsonObject.organizationTemplates.map(app.views.medications.ordering.dto.TherapyTemplate.fromJson) :
            [],
        patientTemplates: tm.jquery.Utils.isArray(jsonObject.patientTemplates) ?
            jsonObject.patientTemplates.map(app.views.medications.ordering.dto.TherapyTemplate.fromJson) :
            [],
        customTemplateGroups: tm.jquery.Utils.isArray(jsonObject.customTemplateGroups) ?
            jsonObject.customTemplateGroups.map(app.views.medications.ordering.dto.CustomTemplatesGroup.fromJson) :
            []
      });
    }
  },

  userTemplates: null,
  organizationTemplates: null,
  patientTemplates: null,
  customTemplateGroups: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.userTemplates = this.userTemplates || [];
    this.organizationTemplates = this.organizationTemplates || [];
    this.patientTemplates = this.patientTemplates || [];
    this.customTemplateGroups = this.customTemplateGroups || [];
  },

  /**
   * @return {Array<app.views.medications.ordering.dto.TherapyTemplate>}
   */
  getUserTemplates: function()
  {
    return this.userTemplates;
  },

  /**
   * @return {Array<app.views.medications.ordering.dto.TherapyTemplate>}
   */
  getOrganizationTemplates: function()
  {
    return this.organizationTemplates;
  },

  /**
   * @return {Array<app.views.medications.ordering.dto.TherapyTemplate>}
   */
  getPatientTemplates: function()
  {
    return this.patientTemplates;
  },

  /**
   * @return {Array<app.views.medications.ordering.dto.CustomTemplatesGroup>}
   */
  getCustomTemplateGroups: function()
  {
    return this.customTemplateGroups;
  }
});
