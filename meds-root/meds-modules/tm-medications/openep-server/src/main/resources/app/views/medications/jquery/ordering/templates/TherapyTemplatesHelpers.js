Class.define('app.views.medications.ordering.templates.TherapyTemplatesHelpers', 'tm.jquery.Object', {
  view: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum} precondition
   * @returns {string}
   */
  createPreconditionMinValueUnitLabel: function(precondition)
  {
    if (!precondition)
    {
      return "";
    }
    return this._getUnitForPrecondition(precondition) + " (" +
        this.getView().getDictionary("including.short").toLowerCase() + ")";
  },

  /**
   * @param {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum|null} precondition
   * @returns {string}
   */
  createPreconditionMaxValueUnitLabel: function(precondition)
  {
    if (!precondition)
    {
      return "";
    }
    return this._getUnitForPrecondition(precondition) + " (" +
        this.getView().getDictionary("excluding.short").toLowerCase() + ")";
  },

  /**
   * @param {app.views.medications.ordering.dto.TherapyTemplatePrecondition|null} templatePrecondition
   * @returns {string}
   */
  createPreconditionWithValuesDescription: function(templatePrecondition)
  {
    var view = this.getView();
    if (!templatePrecondition)
    {
      return "";
    }

    var preconditionDisplayValue = view.getDictionary('defined.criteria') + ': ' +
        view.getDictionary('therapy.template.precondition.' + templatePrecondition.getPrecondition()) + ': ';
    var preconditionValues = [];
    if (templatePrecondition.getMinValue())
    {
      preconditionValues.push(view.getDictionary('minimum.short').toLowerCase());
      preconditionValues.push(templatePrecondition.getMinValue());
      preconditionValues.push(this.createPreconditionMinValueUnitLabel(templatePrecondition.getPrecondition()));
    }

    if (templatePrecondition.getMaxValue())
    {
      preconditionValues.push(view.getDictionary('max.short').toLowerCase());
      preconditionValues.push(templatePrecondition.getMaxValue());
      preconditionValues.push(this.createPreconditionMaxValueUnitLabel(templatePrecondition.getPrecondition()));
    }

    return preconditionDisplayValue + preconditionValues.join(' ');
  },

  /**
   * @param {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum} precondition
   * @returns {String}
   * @private
   */
  _getUnitForPrecondition: function(precondition)
  {
    var view = this.getView();
    var preconditionEnums = app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum;
    switch (precondition)
    {
      case preconditionEnums.WEIGHT:
        return view.getDictionary("kilogram.short");
      case preconditionEnums.BODY_SURFACE:
        return view.getDictionary("square.metre.short");
      case preconditionEnums.AGE_IN_YEARS:
        return view.getDictionary("year.plural.lc");
      case preconditionEnums.AGE_IN_MONTHS:
        return view.getDictionary("month.plural.lc");
    }
    return "";
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});