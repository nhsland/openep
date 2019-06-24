Class.define('app.views.medications.mentalHealth.TherapyContainerToolbars', 'app.views.medications.common.TemplateTherapyContainerToolbar', {
  addToBasketEventCallback: null,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /* override */
  _buildGUI: function ()
  {
    var self = this;
    var view = this.getView();
    var addButtonIcon = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-24 template-element-add-basket-menu-item",
      width: 32,
      height: 32,
      tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("do.order"))
    });
    this.add(addButtonIcon);

    addButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      self.addToBasketEventCallback(self.getTherapyContainer());
    });
  },

  /* Override to disable the more actions menu. */
  _addActionsMenu: function ()
  {
  },

  setAddToBasketEventCallback: function (callback)
  {
    this.addToBasketEventCallback = callback;
  }
});

