Class.define('app.views.medications.ordering.InformationSourceFilterBuilder', 'tm.jquery.Object', {
  templateContext: undefined,
  _onlyDefaults: false,

  /**
   * Constructs a new instance of a predicate function builder, with which we match the available information sources
   * based on the provided {@link #setTemplateContext}. The template context is part of
   * {@link app.views.medications.ordering.OrderingContainer} configuration. If {@link #setOnlyDefaults} is configured as
   * true, the constructed predicate can be used to retrieve the default values that should be used when ordering therapies
   * that are based on template therapies.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {String} contextEnum from {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   * @return {app.views.medications.ordering.InformationSourceFilterBuilder}
   */
  setTemplateContext: function(contextEnum)
  {
    this.templateContext = contextEnum;
    return this;
  },

  /**
   * @return {string} from {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   */
  getTemplateContext: function()
  {
    return this.templateContext;
  },

  /**
   * Set to true if you wish to filter out only the default values, which should be preselected, for the given context.
   * @param {boolean} value
   * @return {app.views.medications.ordering.InformationSourceFilterBuilder}
   */
  setOnlyDefaults: function(value)
  {
    this._onlyDefaults = value === true;
    return this;
  },

  /**
   * @return {function(source: app.views.medications.common.dto.InformationSource):boolean} that should be used to
   * retrieve either the available options or default selection for template based therapies.
   */
  build: function()
  {
    if (!this.getTemplateContext())
    {
      throw new Error('templateContext not defined');
    }

    return new app.views.medications.common.therapy.InformationSourceFilterBuilder()
        .setSourceGroup(this._mapTemplateContextToSourceGroup(this.getTemplateContext()))
        .setSourceType(
            this._onlyDefaults ?
                app.views.medications.TherapyEnums.informationSourceTypeEnum.TEMPLATE :
                undefined)
        .build();
  },

  /**
   * Links a given template context to the appropriate information source group that should be used when the source of
   * information of a therapy can be set. The intent is to minimize the number of required context parameters
   * used by components, similarly to what {@link #mapTherapyTemplateContextToMode} provides. If no match is found,
   * no error is thrown since some values simply don't have a match.
   * @param {string|undefined} value of {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   * @return {string|undefined} value of {@link app.views.medications.TherapyEnums.informationSourceGroupEnum}
   * @private
   */
  _mapTemplateContextToSourceGroup: function(value)
  {
    var contextEnum = app.views.medications.TherapyEnums.therapyTemplateContextEnum;
    var sourceGroupEnum = app.views.medications.TherapyEnums.informationSourceGroupEnum;

    if ([contextEnum.ADMISSION].indexOf(value) > -1)
    {
      return sourceGroupEnum.ADMISSION;
    }
    if ([contextEnum.INPATIENT].indexOf(value) > -1)
    {
      return sourceGroupEnum.INPATIENT;
    }
    return undefined;
  }
});