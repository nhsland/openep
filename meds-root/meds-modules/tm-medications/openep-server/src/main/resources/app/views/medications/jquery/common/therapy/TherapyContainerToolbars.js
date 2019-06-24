Class.define('app.views.medications.common.therapy.TherapyContainerToolbar', 'tm.jquery.Container', {
  cls: "toolbar-container",
  therapyContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  /* for override */
  _buildGUI: function()
  {

  },

  getTherapyContainer: function()
  {
    return this.therapyContainer;
  },

  getTherapyData: function()
  {
    return this.getTherapyContainer().getData();
  },

  getView: function()
  {
    return this.getTherapyContainer().getView();
  },

  refresh: function()
  {
    this.removeAll();
    this._buildGUI();

    if (this.isRendered()) this.repaint();

  }
});
Class.define('app.views.medications.common.TemplateTherapyContainerToolbar', 'app.views.medications.common.therapy.TherapyContainerToolbar', {

  /** @type function|null */
  addToBasketEventCallback: null,
  /** @type function|null */
  addToBasketWithEditEventCallback: null,
  /** @type function|null */
  removeFromTemplateEventCallback: null,

  /** @type tm.jquery.Component|tm.jquery.Image|null */
  addButtonIcon: null,
  /** @type tm.jquery.Component|tm.jquery.Image|null */
  addWithEditButtonIcon: null,

  /** @type boolean */
  addAvailable: true,
  /** @type boolean */
  addWithEditAvailable: true,
  /** @type boolean */
  removeFromTemplateAvailable: true,

  /**
   * @param {function} callback
   */
  setAddToBasketEventCallback: function(callback)
  {
    this.addToBasketEventCallback = callback;
  },

  /**
   * @param {function} callback
   */
  setAddToBasketWithEditEventCallback: function(callback)
  {
    this.addToBasketWithEditEventCallback = callback;
  },

  /**
   * Is the delete / remove from group action available?
   * @returns {boolean}
   */
  isRemoveFromTemplateAvailable: function()
  {
    return this.removeFromTemplateAvailable === true;
  },

  /**
   * Is the add with edit action available?
   * @return {boolean}
   */
  isAddWithEditAvailable: function()
  {
    return this.addWithEditAvailable === true;
  },

  /**
   * Is the add (without edit) action available? Does not affect the availability of 'add with edit' action.
   * @return {boolean}
   */
  isAddAvailable: function()
  {
    return this.addAvailable === true;
  },

  /* override */
  _buildGUI: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center", 0));

    if (this.isAddWithEditAvailable())
    {
      this.addWithEditButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-extend-edit-24 edit-template-element-add-to-basket-menu-item",
        width: 32,
        height: 32,
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary('edit.and.add'))
      });

      this.addWithEditButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        if (!tm.jquery.Utils.isEmpty(self.addToBasketWithEditEventCallback) && component.isEnabled())
        {
          self.addToBasketWithEditEventCallback(self.getTherapyContainer());
        }
      });

      this.add(this.addWithEditButtonIcon);
    }

    if (this.isAddAvailable())
    {
      this.addButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-extend-24 template-element-add-basket-menu-item",
        width: 32,
        height: 32,
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("add"))
      });

      var addToBasketTask = appFactory.createDebouncedTask(
          "app.views.medications.reconciliation.TherapyGroupPanel.addToBasketEventCallback", function()
          {
            self.addToBasketEventCallback(self.getTherapyContainer());
          }, 0, 1000);

      this.addButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        if (!tm.jquery.Utils.isEmpty(self.addToBasketEventCallback) && component.isEnabled())
        {
          addToBasketTask.run();
        }
      });

      this.add(this.addButtonIcon);
    }

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
    popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      if (component.isEnabled())
      {
        var popupMenu = self._createMoreActionsPopupMenu();
        popupMenu.show(elementEvent);
      }
    });
    this.add(popupMenuHotSpot);
  },

  _createMoreActionsPopupMenu: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();

    var addToBasketEventCallback = this.addToBasketEventCallback;
    var addToBasketWithEditEventCallback = this.addToBasketWithEditEventCallback;
    var removeFromTemplateEventCallback = this.removeFromTemplateEventCallback;

    var popupMenu = appFactory.createPopupMenu();

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      cls: "template-element-add-basket-menu-item",
      text: view.getDictionary("add"),
      handler: function()
      {
        if (!tm.jquery.Utils.isEmpty(addToBasketEventCallback))
        {
          addToBasketEventCallback(therapyContainer);
        }
      },
      iconCls: 'icon-extend-24'
    }));
    if (this.isAddWithEditAvailable())
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "edit-template-element-add-to-basket-menu-item",
        text: view.getDictionary('edit.and.add'),
        handler: function()
        {
          if (!tm.jquery.Utils.isEmpty(addToBasketWithEditEventCallback))
          {
            addToBasketWithEditEventCallback(therapyContainer);
          }
        },
        iconCls: 'icon-extend-edit-24'
      }));
    }
    if (this.isRemoveFromTemplateAvailable())
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: "remove-from-template-menu-item",
        text: view.getDictionary('remove.from.order.set'),
        handler: function()
        {
          if (!tm.jquery.Utils.isEmpty(removeFromTemplateEventCallback))
          {
            removeFromTemplateEventCallback(therapyContainer);
          }
        },
        iconCls: 'icon-delete'
      }));
    }
    return popupMenu;
  }
});
