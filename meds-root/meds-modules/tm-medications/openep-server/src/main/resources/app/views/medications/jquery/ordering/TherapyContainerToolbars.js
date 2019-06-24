Class.define('app.views.medications.ordering.TherapyContainerBasketToolbar', 'app.views.medications.common.therapy.TherapyContainerToolbar', {
  editTherapyEventCallback: null,
  removeFromBasketEventCallback: null,

  editAllowed: true,
  editTitle: null, //optional

  /* override */
  _buildGUI: function()
  {
    var self = this;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));

    var popupMenuHotSpot = new tm.jquery.Image({
      cls: 'more-actions-icon',
      width: 32,
      height: 24,
      cursor: 'pointer',
      alignSelf: 'flex-start'
    });
    popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component, componentEvent, elementEvent)
    {
      var popupMenu = self._createHeaderPopupMenu();
      popupMenu.show(elementEvent);
    });

    this._addActionsMenu();
  },

  _addActionsMenu: function()
  {
    var self = this;
    var popupMenuHotSpot = new tm.jquery.Image({
      cls: 'menu-icon',
      width: 32,
      height: 24,
      cursor: 'pointer',
      alignSelf: 'flex-start'
    });
    popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component, componentEvent, elementEvent)
    {
      var popupMenu = self._createMoreActionsPopupMenu();
      popupMenu.show(elementEvent);
    });
    this.add(popupMenuHotSpot);
  },

  _createMoreActionsPopupMenu: function ()
  {
    var view = this.getView();
    var self = this;

    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();

    var editTherapyEventCallback = this.editTherapyEventCallback;
    var removeFromBasketEventCallback = this.removeFromBasketEventCallback;

    var popupMenu = appFactory.createPopupMenu();

    if (this.isEditAllowed())
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "edit-menu-item",
        text: tm.jquery.Utils.isEmpty(self.editTitle) ? view.getDictionary("edit") : self.editTitle,
        handler: function()
        {
          if (!tm.jquery.Utils.isEmpty(editTherapyEventCallback))
          {
            editTherapyEventCallback(therapyContainer);
          }
        },
        iconCls: 'icon-edit'
      }));
    }
    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      cls: "remove-menu-item",
      text: view.getDictionary('remove'),
      handler: function ()
      {
        if (!tm.jquery.Utils.isEmpty(removeFromBasketEventCallback))
        {
          removeFromBasketEventCallback(therapyContainer);
        }
      },
      iconCls: 'icon-delete'
    }));

    return popupMenu;
  },

  setEditTherapyEventCallback: function(callback)
  {
    this.editTherapyEventCallback = callback;
  },

  setRemoveFromBasketEventCallback: function(callback)
  {
    this.removeFromBasketEventCallback = callback;
  },

  isEditAllowed: function()
  {
    return this.editAllowed === true;
  }
});