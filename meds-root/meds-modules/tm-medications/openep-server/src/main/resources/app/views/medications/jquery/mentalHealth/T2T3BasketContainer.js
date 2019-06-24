Class.define('app.views.medications.mentalHealth.T2T3BasketContainer', 'tm.jquery.Container', {
  cls: "ordering-basket-container",

  view: null,
  displayProvider: null,
  editMedicationRouteFunction: null,
  maxDoseContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildGui();
  },

  /**
   * @return {app.views.medications.mentalHealth.T2T3MaxDoseContainer}
   */
  getMaxDoseContainer: function()
  {
    return this.maxDoseContainer;
  },

  /**
   * @return {string|null}
   */
  getHeaderTitle: function()
  {
    return this.headerTitle;
  },

  /**
   * @return {Array<app.views.medications.mentalHealth.TherapyOrder>}
   */
  getListData: function()
  {
    return this.list.getListData();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.therapy.TherapyContainerDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  },

  /**
   * @param {app.views.medications.mentalHealth.TherapyOrder} data
   */
  addTherapy: function(data)
  {
    this.list.addRowData(data, 0);
  },

  /**
   * @return {app.views.medications.mentalHealth.TherapyOrder}
   */
  getContent: function()
  {
    var listData = this.list.getListData();
    return listData.slice(0);
  },

  /**
   * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData} containerData
   * @return {boolean} true if the basket contains an order based on the given TherapyContainer's data, otherwise
   * false.
   * {@see app.views.medications.mentalHealth.MentalHealthTherapyContainerData#getId}
   */
  containsOrderOf: function(containerData)
  {
    return this.getContent()
        .some(function matchesId(order)
        {
          return order.getSourceId() === containerData.getId();
        });
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();

    this.maxDoseContainer = new app.views.medications.mentalHealth.T2T3MaxDoseContainer({
      view: view
    });

    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.getHeaderTitle(),
      view: view,
      actionsMenuFunction: function()
      {
        return self._createHeaderActionsMenu();
      }
    });

    this.list = new tm.jquery.List({
      cls: "ordering-basket-container-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(item);
      },
      selectable: true
    });

    this.add(this.header);
    this.add(this.list);
    this.add(this.maxDoseContainer);
  },

  /**
   * @param {app.views.medications.mentalHealth.TherapyOrder} item
   * @return {app.views.medications.common.therapy.TherapyContainer}
   * @private
   */
  _buildRow: function(item)
  {
    var self = this;
    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: item,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false
    });

    therapyContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DOUBLE_CLICK, function(component)
    {
      if (this.getToolbar().isEditAllowed())
      {
        self._editMedicationRoute(component);
      }
    });

    therapyContainer.setCls(therapyContainer.getCls() + " animated slideInLeft");
    this._attachElementToolbar(therapyContainer);

    return therapyContainer;
  },

  _createHeaderActionsMenu: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var menu = appFactory.createPopupMenu();
    if (this.getListData().length > 0)
    {
      var menuItemRemove = new tm.jquery.MenuItem({
            text: view.getDictionary('remove.all'),
            cls: "remove-all-menu-item",
            iconCls: 'icon-delete',
            handler: function()
            {
              self._clearBasket();
            }
          }
      );
      menu.addMenuItem(menuItemRemove);

      return menu;
    }
    else
    {
      return null;
    }
  },

  _removeTherapy: function(therapyContainer)
  {
    var elementData = therapyContainer.getData();

    this.list.removeRowData(elementData);

    this.therapiesRemovedEventCallback([elementData]);
  },

  _clearBasket: function()
  {
    var self = this;
    var listData = this.list.getListData().slice();
    var removedData = listData.slice();

    self.list.reloadList();
    self.therapiesRemovedEventCallback(removedData);
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _editMedicationRoute: function(therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var therapy = therapyContainer.getData().getTherapy();

    view.getRestApi()
        .loadMedicationRoutes(therapy.getMentalHealthMedication().getId())
        .then(
            function updateRouteWithSelectedRoute(routes)
            {
              self.editMedicationRouteFunction(
                  therapy,
                  routes,
                  function(selectedRoute)
                  {
                    if (!!selectedRoute && !!selectedRoute.getId())
                    {
                      therapy.changeRoute(selectedRoute, view);
                      therapyContainer.refresh();
                    }
                  });
            });
  },

  _attachElementToolbar: function(elementContainer)
  {
    var self = this;
    var editAllowed = elementContainer.getData().getGroup() !== app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES;

    var toolbar = new app.views.medications.ordering.TherapyContainerBasketToolbar({
      therapyContainer: elementContainer,
      editAllowed: editAllowed,
      editTitle: self.getView().getDictionary("change.route"),
      removeFromBasketEventCallback: function(therapyContainer)
      {
        self._removeTherapy(therapyContainer);
      },
      editTherapyEventCallback: function(therapyContainer)
      {
        self._editMedicationRoute(therapyContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
  }
});
