Class.define('app.views.medications.common.overview.header.OverviewHeader', 'tm.jquery.Container', {
  cls: 'therapy-overview-header',
  scrollable: 'visible',
  OTHER_CUSTOM_GROUP: null,
  ALL_CUSTOM_GROUPS: null,

  /** configs */
  view: null,
  timelineFilterChangedFunction: null, //optional
  timelineDateSelectorChangedFunction: null,
  medicationIdentifierScannedFunction: null,
  isDischarged: null,
  /** privates */
  customGroups: null,
  selectedRoutes: null,
  selectedCustomGroups: null,
  /** privates: components */
  sortButton: null,
  /** privates: components - grid */
  previousButton: null,
  nextButton: null,
  dayCountButtonGroup: null,
  /** privates: components - timeline */
  routesFilterContainer: null,
  routesFilterButton: null,
  customGroupFilterContainer: null,
  customGroupFilterButton: null,
  timelineDateField: null,
  _timelineDateFieldTimer: undefined,

  //sort
  therapySortTypeEnum: null,

  //temp
  previousSelectedSort: null,
  previousSelectedShownTherapies: null,

  /**
   * Private
   */
  _subview: null,
  _westToolbar: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.OTHER_CUSTOM_GROUP = this.view.getDictionary("other.undef");
    this.ALL_CUSTOM_GROUPS = this.view.getDictionary("all.groups.short");
    this.selectedRoutes = [];
    this.selectedCustomGroups = [];
  },

  /** private methods */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("space-between", "center"));
    var subview = this.getSubview();
    if (subview === "GRID")
    {
      this._addWestToolbarContainer({warningScan: true});
      this._addCenterGridContainer();
      this._addEastGridContainer();
    }
    else if (subview === "TIMELINE")
    {
      this._addWestToolbarContainer({useFullWidth: true, barcodeInput: true, warningScan: true});
      this._addTimelineDatePicker();
      this._addEastTimelineContainer();
    }
    else if (subview === "PHARMACIST")
    {
      this._addWestToolbarContainer({
        useFullWidth: true,
        warningScan: true,
        inpatientPrescribing: false,
        previousHospitalization: false,
        referenceWeight: false,
        reportPrinting: false
      });
      this._addEastTimelineContainer();
    }
    else if (subview === "RECONCILIATION")
    {
      this._addWestToolbarContainer({
        medicationReconciliation: true,
        inpatientPrescribing: false,
        previousHospitalization: false,
        reportPrinting: false
      });
    }
    else if (subview === "DOCUMENTATION")
    {
      this._addWestToolbarContainer({
        outpatientPrescribing: true,
        inpatientPrescribing: false
      });
    }
  },

  /**
   * Adds the west toolbar container to the header. The default mode will take up only the needed width and
   * include all the menu options except for outpatient prescribing based menu items and the medication barcode input field.
   * @param {object} [mode]
   * @param {boolean} [mode.useFullWidth]=false
   * @param {boolean} [mode.referenceWeight=true]
   * @param {boolean} [mode.inpatientPrescribing=true]
   * @param {boolean} [mode.outpatientPrescribing=false]
   * @param {boolean} [mode.previousHospitalization=true]
   * @param {boolean} [mode.reportPrinting=true]
   * @param {boolean} [mode.barcodeInput=false]
   * @param {boolean} [mode.medicationReconciliation=false]
   * @param {boolean} [mode.warningScan=false]
   * @private
   */
  _addWestToolbarContainer: function(mode)
  {
    mode = mode || {};
    // options that default to true require the parameter to be defined and false, options that default to false
    // require to be defined and true
    this._westToolbar = new app.views.medications.common.overview.header.WestToolbarContainer({
      flex: tm.jquery.flexbox.item.Flex.create(mode.useFullWidth === true ? 1 : 0, 0, "auto"),
      view: this.getView(),
      withOutpatientPrescribing: mode.outpatientPrescribing === true,
      withInpatientPrescribing: mode.inpatientPrescribing !== false,
      withPreviousHospitalization: mode.previousHospitalization !== false,
      withReferenceWeight: mode.referenceWeight !== false,
      withReportPrinting: mode.reportPrinting !== false,
      withMedicationBarcodeInput: mode.barcodeInput === true,
      withMedicationReconciliation: mode.medicationReconciliation === true,
      withWarningScan: mode.warningScan === true
    });

    var self = this;
    if (this.medicationIdentifierScannedFunction)
    {
      this._westToolbar.on(
          app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_BARCODE_SCANNED,
          function(component, componentEvent)
          {
            self.medicationIdentifierScannedFunction(
                componentEvent.eventData.barcodeTaskSearch,
                componentEvent.eventData.barcode);
          });
    }

    this.add(this._westToolbar);
  },

  _addCenterGridContainer: function()
  {
    var centerContainer = new tm.jquery.Container({
      layout: new tm.jquery.CenterLayout(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      scrollable: 'visible'
    });

    var container = new tm.jquery.Container({
      cls: 'center-grid-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("start", "center", 5)
    });

    this.previousButton = new tm.jquery.Button({cls: "left-icon btn-flat", size: 'medium', type: 'filter'});
    this.nextButton = new tm.jquery.Button({cls: "right-icon btn-flat", size: 'medium', type: 'filter'});
    container.add(this.previousButton);
    container.add(this.nextButton);

    centerContainer.add(container);
    this.add(centerContainer);
  },

  _addEastGridContainer: function()
  {
    var self = this;
    var container = new tm.jquery.Container({
      cls: 'east-grid-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 5),
      scrollable: 'visible'
    });

    var numberOfDaysMode = this.view.getContext() ? this.view.getContext().numberOfDaysMode : null;
    var oneDayButton = new tm.jquery.Button({
      cls: "one-day-button btn-flat",
      text: "1",
      dayCount: 1,
      type: 'filter',
      mode: 'numberOfDaysMode1',
      pressed: numberOfDaysMode === 'numberOfDaysMode1'
    });
    var threeDaysButton = new tm.jquery.Button({
      cls: "three-days-button btn-flat",
      text: "3",
      dayCount: 3,
      type: 'filter',
      mode: 'numberOfDaysMode3',
      pressed: !numberOfDaysMode || numberOfDaysMode === 'numberOfDaysMode3'
    });
    var fiveDaysButton = new tm.jquery.Button({
      cls: "five-days-button btn-flat",
      text: "5",
      dayCount: 5,
      type: 'filter',
      mode: 'numberOfDaysMode5',
      pressed: numberOfDaysMode === 'numberOfDaysMode5'
    });

    this.dayCountButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-flat",
      orientation: "horizontal",
      type: "radio",
      buttons: this.getView().isSingleDayTherapiesOverviewEnabled() ?
          [oneDayButton, threeDaysButton, fiveDaysButton] :
          [threeDaysButton, fiveDaysButton]
    });
    this.groupingButtonGroup = this._buildGroupingButtonGroup();

    this._buildSortSplitButton();
    var groupsSelectBoxContainer = new tm.jquery.Container({
      style: "position:relative;max-width: 50px;max-height: 50px;",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("center", "stretch", 0)
    });
    groupsSelectBoxContainer.add(new tm.jquery.Button({
      cls: 'btn-flat icon-sort',
      iconCls: "icon-sort-show-menu",
      alignSelf: "flex-end",
      handler: function()
      {
        self.sortButton.openDropdown();
      }
    }));
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      // don't call component.getDom(), it will trigger DOM creation after being destroyed by fast switching views
      // and fail inside the getSelections method of the component.
      $("#" + component.getId()).find(".bootstrap-select button").hide();
    });

    container.add(this.dayCountButtonGroup);
    container.add(this.groupingButtonGroup);
    groupsSelectBoxContainer.add(this.sortButton);
    container.add(groupsSelectBoxContainer);
    this.add(container);
  },

  _buildGroupingButtonGroup: function()
  {
    var view = this.getView();
    var groupByMode = view.getContext() ? view.getContext().groupByMode : null;

    var noGroupButton = new tm.jquery.Button({
      cls: 'no-group-icon no-grouping-button btn-flat',
      tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary("no.grouping"), "left", view),
      groupField: null,
      type: 'filter',
      mode: 'groupByModeNone',
      pressed: !groupByMode || groupByMode === 'groupByModeNone'
    });
    var groupByAtcButton = new tm.jquery.Button({
      cls: "atc-grouping-button btn-flat",
      text: view.getDictionary("atc"),
      tooltip: app.views.medications.MedicationUtils.createTooltip(
          view.getDictionary("group.by.atc.classification"), "left", view),
      groupField: "atcGroup",
      type: 'filter',
      mode: 'groupByModeAtc',
      pressed: groupByMode === 'groupByModeAtc'
    });
    var groupByPrescriptionGroupButton = new tm.jquery.Button({
      cls: 'prescription-group-grouping-button btn-flat',
      text: view.getDictionary("basic"),
      tooltip: app.views.medications.MedicationUtils.createTooltip(
          view.getDictionary("group.by.prescription.type"),
          "left",
          view),
      groupField: 'prescriptionGroup',
      type: 'filter',
      mode: 'groupByPrescriptionGroup',
      pressed: groupByMode === 'groupByPrescriptionGroup'
    });
    var groupByRouteButton = new tm.jquery.Button({
      cls: "apl-grouping-button btn-flat",
      text: view.getDictionary("apl"),
      tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary("group.by.route"), "left", view),
      groupField: "routes",
      type: 'filter',
      mode: 'groupByModeRoute',
      pressed: groupByMode === 'groupByModeRoute'
    });
    var groupByModeCustomGroupButton = new tm.jquery.Button({
      cls: 'custom-group-icon custom-grouping-button btn-flat',
      tooltip: app.views.medications.MedicationUtils.createTooltip(
          view.getDictionary("group.by.custom.groups"),
          "left",
          view),
      groupField: "customGroup",
      type: 'filter',
      mode: 'groupByModeCustomGroup',
      pressed: groupByMode === 'groupByModeCustomGroup'
    });

    return new tm.jquery.ButtonGroup({
      cls: "btn-group-flat",
      orientation: "horizontal btn-group-flat",
      type: "radio",
      buttons: [
        noGroupButton,
        groupByAtcButton,
        groupByPrescriptionGroupButton,
        groupByRouteButton,
        groupByModeCustomGroupButton]
    });
  },

  _setSortButtonOptions: function()
  {
    var self = this;
    var view = this.view;
    var enums = app.views.medications.TherapyEnums;
    var sortOptions = [];
    var shownTherapiesOptions = [];

    var optionGroups = [];

    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.DESCRIPTION_ASC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.DESCRIPTION_ASC"),
        "sort-name-asc-menu-item",
        "icon-sort-name-asc",
        view.therapySortTypeEnum === enums.therapySortTypeEnum.DESCRIPTION_ASC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.DESCRIPTION_DESC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.DESCRIPTION_DESC"),
        "sort-name-desc-menu-item",
        "icon-sort-name-desc",
        view.therapySortTypeEnum === enums.therapySortTypeEnum.DESCRIPTION_DESC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.CREATED_TIME_ASC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.CREATED_TIME_ASC"),
        "sort-time-asc-menu-item",
        "icon-sort-time-asc",
        view.therapySortTypeEnum === enums.therapySortTypeEnum.CREATED_TIME_ASC,
        true
    ));
    sortOptions.push(tm.jquery.SelectBox.createOption(
        new tm.jquery.Object({
          value: enums.therapySortTypeEnum.CREATED_TIME_DESC,
          group: "sort"
        }),
        this.view.getDictionary("Sort.CREATED_TIME_DESC"),
        "sort-time-desc-menu-item",
        "icon-sort-time-desc",
        view.therapySortTypeEnum === enums.therapySortTypeEnum.CREATED_TIME_DESC,
        true
    ));

    for (var i = 0; i < sortOptions.length; i++)
    {
      if (sortOptions[i].selected)
      {
        this.therapySortTypeEnum = sortOptions[i].value.value;
        this.previousSelectedSort = sortOptions[i].value;
      }
    }

    optionGroups.push(tm.jquery.SelectBox.createOptionGroup(this.view.getDictionary("sort"), sortOptions));
    if (this.getSubview() === "TIMELINE" || this.getSubview() === "PHARMACIST")
    {
      if (tm.jquery.Utils.isEmpty(this.isDischarged) || !self.isDischarged)
      {
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "activeTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.active")),
            "sort-shown-therapies-active",
            null,
            true,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "threeDaysTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.three.days")),
            "sort-shown-therapies-threeDays",
            null,
            false,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "allTherapies",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.all")),
            "sort-shown-therapies-all",
            null,
            false,
            true
        ));
      }
      else
      {
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "discharge",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.discharge")),
            "sort-shown-therapies-discharge",
            null,
            true,
            true
        ));
        shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
            new tm.jquery.Object({
              value: "hospital",
              group: "hide"
            }),
            this._buildOptionTextSpan(this.view.getDictionary("show.therapies.hospitalization")),
            "sort-shown-therapies-hospital",
            null,
            false,
            true
        ));
      }

      shownTherapiesOptions.push(tm.jquery.SelectBox.createOption(
          new tm.jquery.Object({
            value: "customDateTherapies",
            group: "hide"
          }),
          this._buildOptionTextSpan(this.view.getDictionary("choose.date") + "..."),
          "sort-shown-therapies-active",
          null,
          false,
          true
      ));

      this.previousSelectedShownTherapies = shownTherapiesOptions[0].value;
      optionGroups.unshift(tm.jquery.SelectBox.createOptionGroup(this.view.getDictionary("show.therapies"), shownTherapiesOptions));
    }
    if (!tm.jquery.Utils.isEmpty(this.sortButton))
    {
      this.sortButton.setOptionGroups(optionGroups);
    }
  },

  _buildOptionTextSpan: function(text)
  {
    return "<span style='margin-left: 36px;'>" + text + "</span>";
  },

  _buildSortSplitButton: function()
  {
    this.sortButton = new tm.jquery.SelectBox({
      hidden: true,
      style: "position:absolute;left:46px;",
      cls: "therapy-sort-button",
      dropdownAlignment: "right",
      multiple: true,
      allowSingleDeselect: true,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1 && value2 ? value1.group === value2.group && value1.value === value2.value : value1 === value2;
      }
    });
    this._setSortButtonOptions();

    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      component.show();
    });
  },

  _addEastTimelineContainer: function()
  {
    var self = this;

    this.routesFilterContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch")
    });
    this.add(this.routesFilterContainer);

    this.customGroupFilterContainer = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch")
    });

    this.groupingButtonGroup = this._buildGroupingButtonGroup();

    this._buildSortSplitButton();
    var groupsSelectBoxContainer = new tm.jquery.Container({
      cls: 'groups-select-box',
      style: "position:relative;max-width: 50px;max-height: 50px;",
      scrollable: "visible",
      layout: tm.jquery.VFlexboxLayout.create("center", "stretch", 0)
    });
    groupsSelectBoxContainer.add(new tm.jquery.Button({
      cls: 'btn-flat icon-sort',
      iconCls: "icon-sort-show-menu",
      alignSelf: "flex-end",
      handler: function()
      {
        setTimeout(function()
        {
          $(self.sortButton.getDom()).find(".dropdown-menu").css("top", -10);
          $(self.sortButton.getDom()).find(".bootstrap-select button").trigger("click");
        }, 0);
      }
    }));
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      // don't call component.getDom(), it will trigger DOM creation after being destroyed by fast switching views
      // and fail inside the getSelections method of the component.
      $("#" + component.getId()).find(".bootstrap-select button").hide();
    });

    this.add(this.customGroupFilterContainer);
    this.add(this.groupingButtonGroup);
    groupsSelectBoxContainer.add(this.sortButton);
    this.add(groupsSelectBoxContainer);
  },

  _addTimelineDatePicker: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.timelineDateContainer = new tm.jquery.Container({
      scrollable: 'visible',
      hidden: true,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("center", "center", 0)
    });

    var previousDateButton = new tm.jquery.Button({
      enabled: false,
      cls: 'left-icon btn-flat',
      size: 'medium',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      handler: function(component)
      {
        var date = new Date(self.timelineDateField.getDate());
        date.setDate(date.getDate() - 1);
        self.timelineDateField.setDate(date);
        $(component.getDom()).blur(); // somehow the button remains focused after the handler completes
      }
    });
    this.timelineDateContainer.add(previousDateButton);

    this.timelineDateField = new tm.jquery.DatePicker({
      cls: "timeline-date-field",
      showType: "focus",
      width: 95,
      date: CurrentTime.get(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      currentTimeProvider: function()
      {
        return CurrentTime.get();
      }
    });
    this.timelineDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      appFactory.createConditionTask(
          function()
          {
            nextDateButton.setEnabled(true);
            previousDateButton.setEnabled(true);
          },
          function(task)
          {
            if (!self.isRendered())
            {
              task.abort();
              return;
            }
            return component.isRendered() && !tm.jquery.Utils.isEmpty(component.getPlugin());
          },
          50, 100
      );
    });
    this.timelineDateField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      clearTimeout(self._timelineDateFieldTimer);
      {
        self._timelineDateFieldTimer = setTimeout(function()
        {
          self.timelineDateSelectorChangedFunction(self.timelineDateField.getDate());
        }, 250);
      }
    });

    this.timelineDateContainer.add(this.timelineDateField);

    var nextDateButton = new tm.jquery.Button({
      enabled: false,
      cls: 'right-icon btn-flat',
      size: 'medium',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      handler: function(component)
      {
        var date = new Date(self.timelineDateField.getDate());
        date.setDate(date.getDate() + 1);
        self.timelineDateField.setDate(date);
        $(component.getDom()).blur(); // somehow the button remains focused after the handler completes
      }
    });
    this.timelineDateContainer.add(nextDateButton);
    this.add(this.timelineDateContainer);
  },

  _timelineFilterChanged: function(refreshTimelines)
  {
    this.selectedRoutes = this._getSelectedRoutes();
    this.selectedCustomGroups = this._getSelectedCustomGroups();

    if (this.timelineFilterChangedFunction)
    {
      this.timelineFilterChangedFunction(this.selectedRoutes, this.selectedCustomGroups, refreshTimelines);
    }
  },

  _getSelectedRoutes: function()
  {
    var selectedRoutes = [];
    if (this.routesFilterButton)
    {
      var selections = this.routesFilterButton.getSelections();
      for (var i = 0; i < selections.length; i++)
      {
        if (!tm.jquery.Utils.isEmpty(selections[i].data))
        {
          selectedRoutes.push(selections[i].data);
        }
      }
    }
    return selectedRoutes;
  },

  _getSelectedCustomGroups: function()
  {
    var selectedCustomGroups = [];
    if (this.customGroupFilterButton)
    {
      var selections = this.customGroupFilterButton.getSelections();
      for (var i = 0; i < selections.length; i++)
      {
        if (selections[i].text !== this.ALL_CUSTOM_GROUPS)
        {
          selectedCustomGroups.push(selections[i].data);
        }
      }
    }
    return selectedCustomGroups;
  },

  _setupRoutesFilter: function(routes)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    this.routesFilterContainer.removeAll();

    this.routesFilterButton = appFactory.createCheckboxSplitButton({
      cls: "splitbutton-flat print",
      showSelectedItemsIcon: false,
      showSelectedItemsText: true,
      popupMenuHorizontalAlignment: "right",
      clearSelectedCheckBoxItemText: this.view.getDictionary("all.routes.short"),
      clearSelectedItemsText: this.view.getDictionary("all.routes.short")
    });

    routes = tm.jquery.Utils.isEmpty(routes) ? [] : routes;

    routes.forEach(function(routeName)
    {
      self.routesFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
        text: routeName,
        checked: self.selectedRoutes.contains(routeName),
        data: routeName
      }));
    });

    this.routesFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._timelineFilterChanged(true);
    });

    this.routesFilterContainer.add(this.routesFilterButton);
    this.routesFilterContainer.repaint();
  },

  _setupCustomGroupFilter: function(groups)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();

    this.customGroupFilterContainer.removeAll();

    if (this.customGroups && this.customGroups.length > 0)
    {
      this.customGroupFilterButton = appFactory.createCheckboxSplitButton({
        cls: "splitbutton-flat filter",
        showSelectedItemsIcon: false,
        showSelectedItemsText: true,
        popupMenuHorizontalAlignment: "right",
        clearSelectedCheckBoxItemText: this.ALL_CUSTOM_GROUPS,
        clearSelectedItemsText: this.ALL_CUSTOM_GROUPS
      });

      groups = tm.jquery.Utils.isEmpty(groups) ? [] : groups;

      for (var j = 0; j < this.customGroups.length; j++)
      {
        var customGroup = this.customGroups[j];
        this.customGroupFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
          text: customGroup,
          checked: this.selectedCustomGroups.contains(customGroup),
          enabled: groups.contains(customGroup),
          data: customGroup
        }));
      }

      this.customGroupFilterButton.addCheckBoxMenuItem(new tm.jquery.CheckBoxMenuItem({
        text: this.OTHER_CUSTOM_GROUP,
        checked: this.selectedCustomGroups.contains(this.OTHER_CUSTOM_GROUP),
        data: null
      }));

      this.customGroupFilterButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        self._timelineFilterChanged(true);
      });

      this.customGroupFilterContainer.add(this.customGroupFilterButton);
      this.customGroupFilterContainer.repaint();
    }
  },

  /**
   * @param {app.views.medications.common.overview.AbstractSubViewContainer} subView
   */
  attachEventsToSubView: function(subView)
  {
    [this._westToolbar].forEach(function attachEventsToSubView(toolbar)
    {
      if (toolbar instanceof app.views.medications.common.overview.header.AbstractToolbarContainer)
      {
        toolbar.attachEventsToSubView(subView);
      }
    }, this);
  },

  /** public methods */
  setupTimelineFilter: function(routes, groups)
  {
    if (this.getSubview() === "TIMELINE" || this.getSubview() === "PHARMACIST")
    {
      this._setupRoutesFilter(routes);
      this._setupCustomGroupFilter(groups);
      this._timelineFilterChanged(false);
    }
  },

  setCustomGroups: function(customGroups)
  {
    this.customGroups = customGroups;
  },

  addPreviousButtonAction: function(action)
  {
    this.previousButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      tm.jquery.ComponentUtils.hideAllTooltips(); // prevent breaking any opened therapy description tooltip
      action();
    });
  },
  addNextButtonAction: function(action)
  {
    this.nextButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      tm.jquery.ComponentUtils.hideAllTooltips(); // prevent breaking any opened therapy description tooltip
      action();
    });
  },
  addDayCountButtonGroupAction: function(action)
  {
    this.dayCountButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      tm.jquery.ComponentUtils.hideAllTooltips(); // prevent breaking any opened therapy description tooltip
      action(componentEvent.getEventData().newSelectedButton.dayCount);
    });
  },
  addGroupingButtonGroupAction: function(action)
  {
    this.groupingButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      tm.jquery.ComponentUtils.hideAllTooltips(); // prevent breaking any opened therapy description tooltip
      action(componentEvent.getEventData().newSelectedButton.groupField);
    });
  },
  addSortButtonGroupAction: function(action)
  {
    var self = this;
    this.sortButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var selections = componentEvent.getEventData().selections;
      if (selections.length === 1)
      {
        var selected = selections[0];
        if (self.sortButton.optionGroups.length === 1 && selected.group === "sort")
        {
          self.previousSelectedSort = selected;
          self.sortButton.setSelections([selected, self.previousSelectedShownTherapies], true);
        }
        else
        {
          self.sortButton.setSelections([self.previousSelectedSort, self.previousSelectedShownTherapies], true);
          self.sortButton.hidePluginDropdown();
        }
      }
      else if (selections.length > 1)
      {
        self.sortButton.hidePluginDropdown();
        var fireShownTherapiesEvent = false;
        var fireTherapySortEvent = false;
        var fireTimelineCustomDateEvent = false;
        var newSelectedSort;
        var newSelectedShownTherapies;
        for (var i = 0; i < selections.length; i++)
        {
          if (selections[i].group === "hide" && selections[i].value !== self.previousSelectedShownTherapies.value)
          {
            newSelectedShownTherapies = selections[i];
            fireShownTherapiesEvent = true;

            if (self.timelineDateField)
            {
              if (newSelectedShownTherapies.value === "customDateTherapies")
              {
                fireTimelineCustomDateEvent = true;
                self.timelineDateField.setDate(CurrentTime.get(), true);
                self.timelineDateContainer.show();
              }
              else
              {
                self.timelineDateContainer.hide();
              }
            }
          }
          if (selections[i].group === "sort" && selections[i].value !== self.therapySortTypeEnum)
          {
            newSelectedSort = selections[i];
            fireTherapySortEvent = true;
          }
        }
        if (fireTimelineCustomDateEvent)
        {
          self.previousSelectedShownTherapies = newSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
          self.timelineDateSelectorChangedFunction(self.timelineDateField.getDate());
        }
        else if (fireShownTherapiesEvent)
        {
          self.previousSelectedShownTherapies = newSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
          action(self.therapySortTypeEnum, self.previousSelectedShownTherapies);
        }
        else if (fireTherapySortEvent)
        {
          self.previousSelectedSort = newSelectedSort;
          newSelectedShownTherapies = self.previousSelectedShownTherapies;
          self.therapySortTypeEnum = newSelectedSort.value;
          action(self.therapySortTypeEnum, self.previousSelectedShownTherapies);
        }
        else
        {
          newSelectedShownTherapies = self.previousSelectedShownTherapies;
          newSelectedSort = self.previousSelectedSort;
        }
        self.sortButton.setSelections([newSelectedSort, newSelectedShownTherapies], true);
      }
      else
      {
        self.sortButton.setSelections([self.previousSelectedSort, self.previousSelectedShownTherapies], true);
        self.sortButton.hidePluginDropdown();
      }
    });
  },
  getDefaultTherapiesShownSelection: function()
  {
    return this.sortButton.optionGroups[0].getOptions().get(0).value.value;
  },

  setIsDischarged: function(isDischarged)
  {
    this.isDischarged = isDischarged;
  },
  getFilterContext: function()
  {
    var groupByMode = null;
    if (this.groupingButtonGroup && this.groupingButtonGroup.getSelection().length > 0)
    {
      groupByMode = this.groupingButtonGroup.getSelection()[0].mode
    }
    else if (this.view.getContext())
    {
      groupByMode = this.view.getContext().groupByMode;
    }

    return {
      groupByMode: groupByMode,
      numberOfDaysMode: this.dayCountButtonGroup ? this.dayCountButtonGroup.getSelection()[0].mode : null
    }
  },
  setTherapySortType: function(therapySortTypeEnum)
  {
    for (var j = 0; j < this.sortButton.optionGroups.length; j++)
    {
      for (var i = 0; i < this.sortButton.optionGroups[j].getOptions().length; i++)
      {
        var menuItem = this.sortButton.optionGroups[j].getOptions()[i];
        if (menuItem.value.value === therapySortTypeEnum)
        {
          this.sortButton.selections.add(menuItem.value);
          return menuItem.value.value;
        }
      }
    }
    return null;
  },
  setNumberOfDaysMode: function(mode)
  {
    for (var i = 0; i < this.dayCountButtonGroup.getButtons().length; i++)
    {
      var button = this.dayCountButtonGroup.getButtons()[i];
      if (button.mode === mode)
      {
        this.dayCountButtonGroup.setSelection([button], true);
        return button.dayCount;
      }
    }
    return null;
  },
  setGroupMode: function(mode)
  {
    for (var i = 0; i < this.groupingButtonGroup.getButtons().length; i++)
    {
      var button = this.groupingButtonGroup.getButtons()[i];
      if (button.mode === mode)
      {
        button.setPressed(true, true);
        return button.groupField;
      }
    }
    return null;
  },
  showRecentHospitalizationButton: function(isRecentHospitalization)
  {
    if (this._westToolbar)
    {
      this._westToolbar.applyPreviousHospitalizationTherapiesButtonVisibility(isRecentHospitalization);
    }
  },

  /**
   * @param {String} subview
   */
  setSubview: function(subview)
  {
    this._subview = subview;
    this.removeAll();
    this._buildGui();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  getSubview: function()
  {
    return this._subview;
  },

  resetSubview: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.sortButton))
    {
      this._setSortButtonOptions();
    }
    if (this.timelineDateField)
    {
      this.timelineDateField.setDate(CurrentTime.get(), true);
      this.timelineDateContainer.hide();
    }
  },

  requestBarcodeFieldFocus: function()
  {
    if (this._westToolbar)
    {
      this._westToolbar.applyBarcodeFieldFocus();
    }
  },

  /**
   * @Override
   */
  destroy: function()
  {
    clearTimeout(this._timelineDateFieldTimer);
    this.callSuper();
  }
});