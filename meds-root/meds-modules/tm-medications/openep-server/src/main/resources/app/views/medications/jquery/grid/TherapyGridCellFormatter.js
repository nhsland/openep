/**
 * A specialized instance of a grid cell formatter that creates a required type of grid therapy container, depending on the
 * selected day view count. Also creates corresponding therapy container toolbars, and handles events, fired by said
 * toolbars.
 */
Class.define('app.views.medications.grid.TherapyGridCellFormatter', 'tm.jquery.GridCellFormatter', {
  /** @type app.views.common.AppView */
  view: null,
  /** @type app.views.medications.grid.GridView */
  gridView: null,
  /** @type number */
  dayCount: NaN,
  /** @type app.views.medications.common.therapy.TherapyContainerDisplayProvider */
  gridCellTherapyDisplayProvider: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.gridCellTherapyDisplayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({view: this.view});
  },

  /**
   * @override
   * @param {object} cellvalue
   * @param {object} options
   * @returns {tm.jquery.Container|app.views.medications.common.therapy.TherapyContainer}
   */
  content: function(cellvalue, options)
  {
    var self = this;
    if (cellvalue)
    {
      var todayColumnIndex = self.gridView.getTodayColumnIndex();
      var isTodayColumn = options.colModel.index === todayColumnIndex;
      var isOneDayRange = self.dayCount === 1;

      if (isOneDayRange)
      {
        return self._createInlineCellTherapyContainer(options.rowId, todayColumnIndex, cellvalue.dayTherapy, isTodayColumn);
      }

      return isTodayColumn ?
          self._createTodayCellTherapyContainer(options.rowId, todayColumnIndex, cellvalue.dayTherapy) :
          self._createCellHtmlTemplate(options.rowId, options.colModel.index, cellvalue.dayTherapy);
    }
    return self._createEmptyCellHtmlTemplate();
  },

  /**
   * @param {Number} rowId
   * @param {Number} columnIndex
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dayTherapy
   * @returns {tm.jquery.Container}
   * @private
   */
  _createCellHtmlTemplate: function(rowId, columnIndex, dayTherapy)
  {
    var mainContainer = new tm.jquery.Container({
      cls: 'cell-item',
      padding: dayTherapy.showConsecutiveDay ? "5 5 5 3" : 5,
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start')
    });

    if (dayTherapy.showConsecutiveDay)
    {
      mainContainer.add(new tm.jquery.Container({
        cls: "icon_day_number",
        /* width has to be set otherwise IE10 will use auto style set on tm.jquery.Component! */
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px"),
        layout: tm.jquery.VFlexboxLayout.create('flex-end', 'flex-end'),
        html: tm.jquery.Utils.escapeHtml(dayTherapy.consecutiveDay)
      }));
    }

    mainContainer.add(new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: 'visible',
      html: dayTherapy.therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription ShortDescription'
    }));

    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, false, false);
    if (contextMenu && contextMenu.hasMenuItems())
    {
      mainContainer.setContextMenu(contextMenu);
    }

    return mainContainer;
  },

  /**
   * @param {Number} rowId
   * @param {Number} columnIndex
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dayTherapy
   * @returns {app.views.medications.common.therapy.TherapyContainer}
   * @private
   */
  _createTodayCellTherapyContainer: function(rowId, columnIndex, dayTherapy)
  {
    var view = this.view;
    var enums = app.views.medications.TherapyEnums;
    var pharmacistReviewReferBack =
        dayTherapy.therapyPharmacistReviewStatus === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;

    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      view: view,
      data: dayTherapy,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollableElement: this.gridView.getScrollableElement(),
      displayProvider: this.gridCellTherapyDisplayProvider
    });

    var toolBar = new app.views.medications.grid.toolbar.GridTherapyContainerToolbar({
      therapyContainer: therapyContainer
    });

    this._bindToolbarEvents(toolBar, therapyContainer, rowId);

    therapyContainer.setToolbar(toolBar);

    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, true, pharmacistReviewReferBack);
    if (contextMenu && contextMenu.hasMenuItems())
    {
      therapyContainer.setContextMenu(contextMenu);
    }

    return therapyContainer;
  },

  /**
   * @param {Number} rowId
   * @param {Number} columnIndex
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dayTherapy
   * @param {Boolean} addAllTherapyActions
   * @returns {app.views.medications.common.therapy.InlineTherapyContainer}
   * @private
   */
  _createInlineCellTherapyContainer: function(rowId, columnIndex, dayTherapy, addAllTherapyActions)
  {
    var view = this.view;
    var enums = app.views.medications.TherapyEnums;
    var pharmacistReviewReferBack =
        dayTherapy.therapyPharmacistReviewStatus === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;

    var therapyContainer = new app.views.medications.common.therapy.InlineTherapyContainer({
      view: view,
      data: dayTherapy,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollableElement: this.gridView.getScrollableElement(),
      displayProvider: this.gridCellTherapyDisplayProvider
    });

    var contextMenu = this._createTherapyContextMenu(dayTherapy, rowId, addAllTherapyActions, pharmacistReviewReferBack);
    if (contextMenu && contextMenu.hasMenuItems())
    {
      therapyContainer.setContextMenu(contextMenu);
    }

    if (addAllTherapyActions)
    {
      var toolBar = new app.views.medications.grid.toolbar.InlineTherapyContainerToolbar({
        therapyContainer: therapyContainer
      });
      this._bindToolbarEvents(toolBar, therapyContainer, rowId);
      therapyContainer.setToolbar(toolBar);
    }

    return therapyContainer;
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _createEmptyCellHtmlTemplate: function()
  {
    return new tm.jquery.Container({
      cls: 'cell-item',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      style: "position:relative; white-space: normal; background:#E2DFDF;"
    });
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dayTherapy
   * @param {Number} rowId
   * @param {Boolean} today
   * @param {Boolean} disableEditTherapy
   * @returns {tm.jquery.ContextMenu}
   * @private
   */
  _createTherapyContextMenu: function(dayTherapy, rowId, today, disableEditTherapy)     // [TherapyDayDto.java]
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();
    var enums = app.views.medications.TherapyEnums;

    var therapy = dayTherapy.therapy;
    var therapyModifiedInThePast = dayTherapy.modified;
    var therapyStatus = dayTherapy.therapyStatus;

    if (!therapy ||
        (!view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() &&
            !view.getTherapyAuthority().isCopyPrescriptionAllowed()))
    {
      return null;
    }

    var contextMenu = appFactory.createContextMenu();
    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      if (today && therapyStatus !== enums.therapyStatusEnum.ABORTED && therapyStatus !== enums.therapyStatusEnum.CANCELLED)
      {
        var menuItemEdit = new tm.jquery.MenuItem({
          text: view.getDictionary("edit"), /*iconCls: 'icon-edit',*/
          enabled: !disableEditTherapy,
          handler: function()
          {
            view.showEditTherapyDialog(therapy, false, therapyModifiedInThePast);
          }
        });
        contextMenu.addMenuItem(menuItemEdit);
      }

      if (today && therapyStatus !== enums.therapyStatusEnum.ABORTED && therapyStatus !== enums.therapyStatusEnum.CANCELLED)
      {
        var menuItemDelete = new tm.jquery.MenuItem({
          text: view.getDictionary("stop.therapy"), /* iconCls: 'icon-delete', */
          handler: function()
          {
            self.gridView.addActionToQueue(therapy, rowId, 'ABORT');
          }
        });
        contextMenu.addMenuItem(menuItemDelete);
        if (therapyStatus !== 'SUSPENDED')
        {
          var menuItemSuspend = new tm.jquery.MenuItem({
            text: view.getDictionary("suspend"), /*iconCls: 'icon-suspend', */
            handler: function()
            {
              self.gridView.addActionToQueue(therapy, rowId, 'SUSPEND');
            }
          });
          contextMenu.addMenuItem(menuItemSuspend);
        }
      }
    }

    if (view.getTherapyAuthority().isCopyPrescriptionAllowed())
    {
      var menuItemCopySimpleTherapy = new tm.jquery.MenuItem({
        text: view.getDictionary("copy"), /*iconCls: 'icon-copy', */
        handler: function()
        {
          view.showEditTherapyDialog(therapy, true, false);
        }
      });
      contextMenu.addMenuItem(menuItemCopySimpleTherapy);
    }

    if (today && therapyStatus !== enums.therapyStatusEnum.ABORTED && therapyStatus !== enums.therapyStatusEnum.CANCELLED)
    {
      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
      {
        var menuItemSuspendAll = new tm.jquery.MenuItem({
          text: view.getDictionary("suspend.all"), /* iconCls: 'icon-suspend-all', */
          handler: function()
          {
            self.gridView.addActionToQueue(therapy, rowId, 'SUSPEND_ALL');
          }
        });
        contextMenu.addMenuItem(menuItemSuspendAll);
      }
      if (view.getTherapyAuthority().isSuspendAllForTemporaryLeaveAllowed())
      {
        contextMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary("temporary.leave"),
          handler: function()
          {
            self.gridView.addActionToQueue(therapy, rowId, 'SUSPEND_ALL_TEMPORARY_LEAVE');
          }
        }));
      }
      if (view.getTherapyAuthority().isStopAllPrescriptionsAllowed())
      {
        contextMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary("stop.all"),
          handler: function()
          {
            self.gridView.addActionToQueue(therapy, rowId, 'STOP_ALL');
          }
        }));
      }
    }

    return contextMenu;
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _onEditTherapy: function(therapyContainer)
  {
    tm.jquery.ComponentUtils.hideAllTooltips();
    var data = therapyContainer.getData();
    var therapy = data.therapy;
    this.view.showEditTherapyDialog(therapy, false, data.modified);
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @param {Number} rowId
   * @private
   */
  _onConfirmTherapy: function(therapyContainer, rowId)
  {
    tm.jquery.ComponentUtils.hideAllTooltips();
    var data = therapyContainer.getData();
    var therapy = data.getTherapy();
    therapyContainer.getToolbar().disableActionButtons();

    var action = data.therapyStatus === 'SUSPENDED' ? 'REISSUE' : 'CONFIRM';
    this.gridView.addActionToQueue(therapy, rowId, action);
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @param {Number} rowId
   * @private
   */
  _onStopTherapy: function(therapyContainer, rowId)
  {
    tm.jquery.ComponentUtils.hideAllTooltips();
    var therapy = therapyContainer.getData().getTherapy();
    therapyContainer.getToolbar().disableActionButtons();
    this.gridView.addActionToQueue(therapy, rowId, 'ABORT');
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _onShowRelatedPharmacistReview: function(therapyContainer)
  {
    var self = this;
    this.view.onShowRelatedPharmacistReviews(therapyContainer.getData(), function()
    {
      self.gridView.reloadGridData();
    });
  },

  /**
   * @private
   */
  _onTasksChanged: function()
  {
    this.gridView.reloadGridData();
  },

  /**
   * @param {*|app.views.medications.grid.toolbar.GridTherapyContainerToolbar|
   * app.views.medications.grid.toolbar.InlineTherapyContainerToolbar} toolBar
   * @param {app.views.medications.common.therapy.TherapyContainer|
   * app.views.medications.common.therapy.InlineTherapyContainer} therapyContainer
   * @param {number} rowId
   * @private
   */
  _bindToolbarEvents: function(toolBar, therapyContainer, rowId)
  {
    toolBar.on(
        app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_EDIT_THERAPY,
        this._onEditTherapy.bind(this, therapyContainer));
    toolBar.on(
        app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_STOP_THERAPY,
        this._onStopTherapy.bind(this, therapyContainer, rowId));
    toolBar.on(
        app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_CONFIRM_THERAPY,
        this._onConfirmTherapy.bind(this, therapyContainer, rowId));
    toolBar.on(
        app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_SHOW_RELATED_PHARMACIST_REVIEW,
        this._onShowRelatedPharmacistReview.bind(this, therapyContainer));
    toolBar.on(
        app.views.medications.grid.toolbar.GridTherapyContainerToolbarEvents.EVENT_TYPE_TASKS_CHANGED,
        this._onTasksChanged.bind(this));
  },
});