Class.define('app.views.medications.ordering.MedicationsTitleHeader', 'tm.jquery.Container', {
  cls: "title-header",
  padding: '6 6 6 10',

  /** configs */
  disabled: false,
  title: null,
  view: null,
  height: 30,
  actionsMenuFunction: null, // optional
  additionalDataContainer: null, // optional

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    var self = this;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
    var cls = this.disabled ? 'TextLabel bold cancelled' : 'TextLabel bold';

    this.add(new tm.jquery.Container({
      cls: cls,
      html: tm.jquery.Utils.escapeHtml(this.title),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }), {region: 'center'});

    // if no additional container was passed, add an empty one to keep the menu on the far right
    this.add(!!this.additionalDataContainer ?
        this.additionalDataContainer :
        new tm.jquery.Container({
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
        }));

    if (this.actionsMenuFunction)
    {
      var menuButton = new tm.jquery.Container({
        width: 16, height: 16,
        cls: 'menu-icon',
        cursor: 'pointer'
      });
      menuButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        var menu = self.actionsMenuFunction();
        if (menu)
        {
          menu.show(elementEvent);
        }
      });
      this.add(menuButton);
    }
  }
});