Class.define('app.views.medications.reconciliation.InformationSourceDefaultsFilterFactory', 'tm.jquery.Object', {
  _contextFilterBuilder: null,

  /**
   * Constructs a new instance of the therapy information source filter builder, using which we can retrieve the
   * appropriate options based on the template context.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._contextFilterBuilder =
        new app.views.medications.ordering.InformationSourceFilterBuilder()
            .setOnlyDefaults(false);
  },

  /**
   * @param {String} contextEnum from {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   * @return {app.views.medications.reconciliation.InformationSourceDefaultsFilterFactory}
   */
  setTemplateContext: function(contextEnum)
  {
    this._contextFilterBuilder.setTemplateContext(contextEnum);
    return this;
  },

  /**
   * @return {string} from {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   */
  getTemplateContext: function()
  {
    return this._contextFilterBuilder.getTemplateContext();
  },

  /**
   * @return {function(source: app.views.medications.common.dto.InformationSource):boolean} which can be used to match the
   * default information sources for therapies based on previous discharge medication.
   */
  buildPreviousDischargeFilter: function()
  {
    return this._buildFilter(app.views.medications.TherapyEnums.informationSourceTypeEnum.PREVIOUS_DISCHARGE);
  },

  /**
   * @return {function(source: app.views.medications.common.dto.InformationSource):boolean} which can be used to match the
   * default information sources for therapies based on previous hospitalization prescriptions.
   */
  buildPreviousHospitalizationFilter: function()
  {
    return this._buildFilter(app.views.medications.TherapyEnums.informationSourceTypeEnum.PREVIOUS_HOSPITALIZATION);
  },

  /**
   * @return {function(source: app.views.medications.common.dto.InformationSource):boolean}
   */
  _buildFilter: function(sourceType)
  {
    var contextFilter = this._contextFilterBuilder.build();

    return function(source)
    {
      return contextFilter(source) && source.getInformationSourceType() === sourceType;
    };
  }
});