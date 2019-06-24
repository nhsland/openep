Class.define('app.views.medications.ordering.ParacetamolLimitContainer', 'tm.jquery.Container', {
  cls: "paracetamol-container",

  /** configs */
  view: null,
  percentage: null,

  /** privates */
  _paracetamolLimitLabel: null,
  _paracetamolLimitImage: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));

    this._paracetamolLimitLabel = new tm.jquery.Label({
      cls: "TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      hidden: true
    });

    this._paracetamolLimitImage = new tm.jquery.Image({
      cls: 'max-dose-high-icon',
      width: 16,
      height: 16,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
      hidden: true
    });

    this.add(this._getParacetamolLimitImage());
    this.add(this._getParacetamolLimitLabel());
  },

  _createPopup: function(text)
  {
    var defaultPopoverTooltip = this.view.getAppFactory().createDefaultPopoverTooltip(null, null,
        new tm.jquery.Label({
          cls: 'warning-popover-tooltip',
          text: text
        }));

    defaultPopoverTooltip.setPlacement("bottom");
    return defaultPopoverTooltip;
  },

  /** private methods */
    _getParacetamolLimitLabel: function()
  {
    return this._paracetamolLimitLabel;
  },

  _getParacetamolLimitImage: function()
  {
    return this._paracetamolLimitImage;
  },

  /** public methods */
  /**
   * @param {app.views.medications.warnings.dto.ParacetamolRuleResult} calculatedParacetamolRule
   */
  setCalculatedParacetamolLimit: function(calculatedParacetamolRule)
  {
    if (!tm.jquery.Utils.isEmpty(calculatedParacetamolRule))
    {
      this.percentage = calculatedParacetamolRule.getHighestRulePercentage();

      if (this.percentage >= 100 && tm.jquery.Utils.isEmpty(calculatedParacetamolRule.getErrorMessage()))
      {
        this._getParacetamolLimitImage().setTooltip(this._createPopup(tm.jquery.Utils.formatMessage(
            this.view.getDictionary("paracetamol.max.daily.limit.percentage"),
            [this.percentage])));

        this._getParacetamolLimitLabel().setText(this.percentage + "%");
        this.showAll();
      }
      else
      {
        this.percentage = null;
        this.hideAll();
      }
    }
    else
    {
      this.percentage = null;
      this.hideAll();
    }
  },

  getPercentage: function ()
  {
    return this.percentage;
  },

  hasContent: function()
  {
    return tm.jquery.Utils.isEmpty(this.percentage);
  },

  hideAll: function()
  {
    if (this.isRendered())
    {
      this._getParacetamolLimitLabel().hide();
      this._getParacetamolLimitImage().hide();
    }
    else
    {
      this._getParacetamolLimitLabel().setHidden(true);
      this._getParacetamolLimitImage().setHidden(true);
    }
  },

  showAll: function()
  {
    if (this.isRendered())
    {
      this._getParacetamolLimitLabel().show();
      this._getParacetamolLimitImage().show();
      this.show();
    }
    else
    {
      this._getParacetamolLimitLabel().setHidden(false);
      this._getParacetamolLimitImage().setHidden(false);
      this.setHidden(false);
    }
  },

  clear: function()
  {
      this.percentage = null;
      this.hideAll();
  }
});
