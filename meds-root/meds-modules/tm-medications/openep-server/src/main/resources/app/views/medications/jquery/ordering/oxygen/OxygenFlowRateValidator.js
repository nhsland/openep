Class.define('app.views.medications.ordering.oxygen.OxygenFlowRateValidator', 'tm.jquery.Object', {
  MIN: 0,
  MAX: 100,

  view: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Array<tm.jquery.Validator>}
   */
  getValidators: function()
  {
    var self = this;
    return [
      new tm.jquery.Validator({
        errorMessage: tm.jquery.Utils.formatMessage(
            this.getView().getDictionary("value.must.be.greater.than.and.less.than"),
            this.MIN,
            this.MAX),
        isValid: function (value)
        {
          if (value)
          {
            if (!tm.jquery.Utils.isNumeric(value))
            {
              return false;
            }

            var floatValue = parseFloat(value);
            if (floatValue < self.MIN || floatValue > self.MAX)
            {
              return false;
            }
          }

          return true;
        }
      })
    ]
  },

  /**
   * Currently only supports components that implement the getValue method.
   * @param component
   * @returns {Array<tm.jquery.FormField>}
   */
  getAsFormFieldValidators: function(component)
  {
    return [new tm.jquery.FormField({
      label: null, name: "oxygenFlowRate", component: component, required: false,
      validation: {
        type: "local",
        validators: this.getValidators()
      }
    })];
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});