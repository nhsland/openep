Class.define('app.views.medications.ordering.BasketContainer', 'tm.jquery.Container', {
  cls: "ordering-basket-container",

  /** configs */
  /** @type app.views.common.AppView */
  view: null,
  /** @type string|null */
  headerTitle: null,
  /** @type function({forceNoRefreshWarnings: boolean}|undefined) */
  therapyAddedEventCallback: null,
  /** @type function(app.views.medications.common.therapy.TherapyContainer) */
  editTherapyEventCallback: null,
  /** @type function(Array<app.views.medication.ordering.AbstractTherapyOrder>, {clearBasket: boolean}|undefined) */
  therapiesRemovedEventCallback: null,
  /** @type function(Array<app.views.medications.ordering.AbstractTherapyOrder>) */
  saveTemplateEventCallback: null,
  /** @type boolean */
  editOrderAllowed: true,
  /** @type boolean */
  saveAsTemplateAllowed: true,
  /** privates */
  /** @type app.views.medications.common.therapy.TherapyContainerDisplayProvider */
  displayProvider: null,
  /** @type boolean */
  existsUniversalTherapy: false,
  /** privates: components */
  /** @type tm.jquery.List */
  list: null,
  /** @type boolean */
  _refreshing: false,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.displayProvider = this.getConfigValue(
        "displayProvider",
        new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
          view: this.getView(),
          showMaxDose: true
        }));

    this._buildGui();
  },

  /**
   * Returns the current therapies in the basket.
   * @return {Array<app.views.medications.common.dto.Therapy>}
   */
  getTherapies: function ()
  {
    var therapies = [];
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      therapies.push(listData[i].getTherapy());
    }
    return therapies;
  },

  /**
   * @return {Array<app.views.medications.ordering.AbstractTherapyOrder>}
   */
  getBasketItems: function()
  {
    return this.list.getListData().slice(0);
  },

  getHeaderTitle: function ()
  {
    return this.headerTitle;
  },

  getView: function ()
  {
    return this.view;
  },

  getDisplayProvider: function ()
  {
    return this.displayProvider;
  },

  /**
   * Do we support saving the contents of the basket as a template?
   * @return {boolean}
   */
  isSaveAsTemplateAvailable: function()
  {
    return this.saveAsTemplateAllowed === true;
  },

  /**
   * Do we support editing a basket item?
   * @return {boolean}
   */
  isEditOrderAllowed: function()
  {
    return this.editOrderAllowed === true;
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} data
   * @param {{forceNoRefreshWarnings: boolean}|undefined} [options=undefined]
   */
  addTherapy: function (data, options)
  {
    if (!!data && !!data.getTherapy())
    {
      this.list.addRowData(data, 0);
      var listData = this.list.getListData();
      this.existsUniversalTherapy = this._existsTherapyWithUniversalMedication(listData);
      this.therapyAddedEventCallback(options);
    }
    else
    {
      this.getView().getLocalLogger().warn("An element with an invalid structure was added to the BasketContainer. Call ignored.");
    }
  },

  removeTherapy: function (therapy)
  {
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      if (listData[i].getTherapy() === therapy)
      {
        this.list.removeRowData(listData[i]);
        break;
      }
    }
  },

  refreshWithExistingData: function ()
  {
    this._refreshing = true; // ugly way of blocking animations due to a lot of refreshes, causing itemTpl to fire again
    this.list.rebuild();
    this._refreshing = false;
  },

  /**
   * Provides the ability to override the header construction, if needed.
   * @protected
   */
  buildHeader: function()
  {
    return new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.getHeaderTitle(),
      view: this.view,
      actionsMenuFunction: this._createHeaderActionsMenu.bind(this)
    });
  },

  /**
   * Event handler which is called when the user issues an edit of an existing basket item. Acts as a template method to
   * support implementing additional steps before issuing the event.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @protected
   */
  onEditBasketItem: function(therapyContainer)
  {
    this.fireEditTherapyEvent(therapyContainer);
  },

  /**
   * Event handler which is called when the user issues removal of an individual item found in the basket. Acts as a
   * template method to support implementing additional steps before issuing the event.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @protected
   */
  onRemoveBasketItem: function(therapyContainer)
  {
    this.removeBasketItem(therapyContainer);
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @protected
   */
  fireEditTherapyEvent: function (therapyContainer)
  {
    if (!this.isEditOrderAllowed())
    {
      throw new Error('Edit functionality disabled by configuration.');
    }

    var elementData = therapyContainer.getData();
    var medication = this._getMedication(elementData.getTherapy());

    if (tm.jquery.Utils.isEmpty(medication))
    {
      this.view.getAppFactory()
          .createWarningSystemDialog(this.view.getDictionary('therapy.template.can.not.edit'), 320, 160)
          .show();
    }
    else
    {
      this.editTherapyEventCallback(therapyContainer);
    }
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @protected
   */
  removeBasketItem: function (therapyContainer)
  {
    var elementData = therapyContainer.getData();
    var therapyCanBeRemoved = this._canTherapyBeRemovedDueToLinks(elementData.getTherapy());

    if (therapyCanBeRemoved)
    {
      this.list.removeRowData(elementData);

      if (elementData.getTherapy().getLinkName())
      {
        this._removeTherapyLink(elementData.getTherapy());
        this.list.rebuild();
      }

      this.therapiesRemovedEventCallback([elementData]);
    }
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    var self = this;

    this.list = new tm.jquery.List({
      cls: "ordering-basket-container-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function (index, item)
      {
        return self._buildRow(item);
      },
      selectable: true
    });

    this.add(this.buildHeader());
    this.add(this.list);
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} item
   * @return {app.views.medications.common.therapy.TherapyContainer}
   * @private
   */
  _buildRow: function (item)
  {
    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: item,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false
    });

    if (this.isEditOrderAllowed())
    {
      therapyContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, this.onEditBasketItem.bind(this));
    }

    if (this._refreshing === false)
    {
      therapyContainer.setCls(therapyContainer.getCls() + " animated slideInLeft");
    }
    if (!item.isValid() || !item.getTherapy().isCompleted())
    {
      therapyContainer.setCls(therapyContainer.getCls() + " invalid-therapy");
    }

    this._attachElementToolbar(therapyContainer);

    if (this._refreshing === false)
    {
      therapyContainer.on(
          tm.jquery.ComponentEvent.EVENT_TYPE_RENDER,
          function(component)
          {
            // don't trigger apply
            component.cls = component.getCls().replace("animated", "");
          });
    }

    return therapyContainer;
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} elementContainer
   * @private
   */
  _attachElementToolbar: function (elementContainer)
  {
    var toolbar = new app.views.medications.ordering.TherapyContainerBasketToolbar({
      therapyContainer: elementContainer,
      editAllowed: this.isEditOrderAllowed(),
      editTherapyEventCallback: this.onEditBasketItem.bind(this),
      removeFromBasketEventCallback: this.onRemoveBasketItem.bind(this)
    });
    elementContainer.setToolbar(toolbar);
  },

  _createHeaderActionsMenu: function ()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var menu = appFactory.createPopupMenu();
    var view = this.getView();
    if (this.list.getListData().length > 0)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: view.getDictionary('remove.all'),
            cls: "remove-all-menu-item",
            iconCls: 'icon-delete',
            handler: function ()
            {
              self._clearBasket();
            }
          }
      );
      menu.addMenuItem(menuItemRemove);
      if (!this.existsUniversalTherapy &&
          this.isSaveAsTemplateAvailable() &&
          view.getTherapyAuthority().isManageAnyTemplatesAllowed())
      {
        var menuItemSaveTemplate = new tm.jquery.MenuItem({
              text: view.getDictionary('save.order.set'),
              cls: "save-template-menu-item",
              iconCls: 'icon-save',
              handler: function()
              {
                self.saveTemplateEventCallback(self.list.getListData().slice());
              }
            }
        );
        menu.addMenuItem(menuItemSaveTemplate);
      }
      return menu;
    }
    else
    {
      return null;
    }
  },

  _canTherapyBeRemovedDueToLinks: function (therapy)
  {
    if (therapy.getLinkName())
    {
      var nextLinkName = app.views.medications.MedicationUtils.getNextLinkName(therapy.getLinkName());
      var listData = this.list.getListData();
      for (var j = 0; j < listData.length; j++)
      {
        if (listData[j].getTherapy().getLinkedTherapy() === nextLinkName)
        {
          var message = this.view.getDictionary('therapy.can.not.remove.if.linked');
          this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
          return false;
        }
      }
    }
    return true;
  },

  _clearBasket: function ()
  {
    var data = this.list.getListData().slice();
    this.list.clearListData();
    this.therapiesRemovedEventCallback(data, {clearBasket: true});
    this.existsUniversalTherapy = false;
  },

  _getMedication: function (therapy)
  {
    if (therapy.isOrderTypeComplex())
    {
      return !tm.jquery.Utils.isEmpty(therapy.ingredientsList[0]) ? therapy.ingredientsList[0].medication : null;
    }
    else
    {
      return therapy.medication;
    }
  },

  _removeTherapyLink: function (therapy)
  {
    var linkName = therapy.getLinkName();
    var listData = this.list.getListData();

    //remove first if second
    var isSecondLinkedTherapy = linkName.length === 2 && linkName.charAt(1) === '2';
    if (isSecondLinkedTherapy) //clear linkName on first therapy
    {
      var previousLinkName = linkName.charAt(0) + '1';
      for (var j = 0; j < listData.length; j++)
      {
        if (listData[j].getTherapy().getLinkedTherapy() === previousLinkName)
        {
          listData[j].getTherapy().setLinkName(null);
          break;
        }
      }
    }

    therapy.linkName = null;
  },

  _existsTherapyWithUniversalMedication: function(listData)
  {
    for (var i = 0; i < listData.length; i++)
    {
      if (listData[i].getTherapy().hasUniversalIngredient())
      {
        return true;
      }
    }
    return false;
  }
});

