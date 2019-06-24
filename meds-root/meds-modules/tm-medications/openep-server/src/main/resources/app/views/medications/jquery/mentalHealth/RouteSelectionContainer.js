Class.define('app.views.medications.mentalHealth.RouteSelectionContainer', 'app.views.common.containers.AppBodyContentContainer', {
  cls: "route-selection-container",

  /** configs */
  view: null,
  routes: null,
  linesNumber: null,
  formattedTherapyDisplay: null,

  /** privates */
  resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.routes = tm.jquery.Utils.isArray(this.routes) ? this.routes : [];
    this._buildGui();
  },

  /**
   * @return {string|null}
   */
  getFormattedDisplayTherapy: function()
  {
    return this.formattedTherapyDisplay;
  },

  processResultData: function(button)
  {
    this.resultCallback(button.data);
  },

  _buildGui: function()
  {
    var self = this;

    self.add(new tm.jquery.Label({
      html: this.getFormattedDisplayTherapy(),
      cls: "formatted-therapy-display",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }));

    for (var i = 1; i <= self.linesNumber; i++)
    {
      this._buildRowContainer(i);
    }
  },

  _buildRowContainer: function(lineNumber)
  {
    var self = this;
    var container = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch")});
    this.routes.forEach(function(route)
    {
      if (route.lineNumber === lineNumber)
      {
        var button = new tm.jquery.Button({
          cls: "btn-bubble",
          text: route.name,
          data: route,
          handler: function(button)
          {
            self.processResultData(button)
          }
        });
        container.add(button);
      }
    });

    self.add(container);
  }
});