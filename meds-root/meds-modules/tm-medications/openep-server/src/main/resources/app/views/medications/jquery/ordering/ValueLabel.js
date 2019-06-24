Class.define('app.views.medications.ordering.ValueLabel', 'tm.jquery.Container', {
  /** configs */
  value: null,
  displayProvider: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this._applyValue();
  },

  _applyValue: function()
  {
    if (this.displayProvider)
    {
      this.setHtml(this.displayProvider(this.value));
    }
    else
    {
      this.setHtml(tm.jquery.Utils.escapeHtml(this.value));
    }
  },

  /** public methods */
  setValue: function(value)
  {
    this.value = value;
    this._applyValue();
  },

  getValue: function()
  {
    return this.value;
  },

  getDisplayValue: function()
  {
    if (this.displayProvider)
    {
      return this.displayProvider(this.value)
    }
    return this.value;
  },

  clear: function()
  {
    this.setValue(null);
  }
});

