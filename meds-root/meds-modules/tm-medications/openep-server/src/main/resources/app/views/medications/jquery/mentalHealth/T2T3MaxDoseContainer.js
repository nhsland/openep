Class.define('app.views.medications.mentalHealth.T2T3MaxDoseContainer', 'tm.jquery.Container', {
  cls: "mental-health-max-dose-container",
  layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
  flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),

  /** configs */
  view: null,

  /** privates */
  maxDoseLabel: null,
  maxDoseTextField: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.view;
    this.maxDoseLabel = new tm.jquery.Label({
      text: view.getDictionary("upper.limit.of.maximum.recommended.dose") + ": ",
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      padding: '0px 5px 0px 5px'
    });

    this.maxDoseTextField = new tm.jquery.TextField({
      width: 40,
      cls: "field-flat"
    });

    this.add(this.maxDoseLabel);
    this.add(this.maxDoseTextField);
    this.add(new tm.jquery.Label({
      text: '%',
      cls: "TextData",
      padding: '0px 0px 0px 5px',
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto")
    }));
  },

  /**
   * @returns {tm.jquery.Label}
   */
  getMaxDoseLabel: function()
  {
    return this.maxDoseLabel;
  },

  /**
   * @returns {tm.jquery.TextField}
   */
  getMaxDoseTextField: function()
  {
    return this.maxDoseTextField
  },

  /**
   * @returns {String|null}
   */
  getResult: function()
  {
    return this.maxDoseTextField.getValue();
  }
});
