Class.define('app.views.medications.pharmacists.ReviewView', 'tm.jquery.Container', {
  cls: "pharmacists-review",

  /** public members  */
  view: null,
  activeTherapyData: null,
  activeReportsData: null,
  lastTaskChangeTimestamp: null,
  therapyDataLoadedCallback: null,

  /* config options */
  therapyListColumnWidth: null,
  routesFilter: null,
  customGroupsFilter: null,
  timelineStart: null,
  timelineEnd: null,
  selectedShownTherapies: null,
  hidePastTherapies: null,
  previousSelectedButtonId: null,

  /* private members */
  /** @type app.views.medications.pharmacists.PharmacistMedicationReviewsByDate|null */
  _loadedReviews: null,
  _therapyColumn: null,
  _reportsColumn: null,
  _therapyRowDataGrouper: null,
  _activeDailyContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.therapyListColumnWidth = this.getConfigValue("therapyListColumnWidth", 425);
    this._therapyRowDataGrouper = new app.views.medications.common.overview.TherapyRowDataGrouper({
      view: this.view
    });

    this._buildGui();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var therapyColumn = new app.views.medications.pharmacists.ColumnContainer({
      columnTitle: this.getView().getDictionary("therapies"),
      cls: "therapy-column",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, this.therapyListColumnWidth + "px")
    });

    var reportsColumn = new app.views.medications.pharmacists.ColumnContainer({
      columnTitle: this.getView().getDictionary("pharmacists.reviews"),
      cls: "reports-column",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(therapyColumn);
    this.add(reportsColumn);

    this._therapyColumn = therapyColumn;
    this._reportsColumn = reportsColumn;
  },

  _loadTherapyData: function(therapySortTypeEnum, callback)
  {
    this.getView().getLocalLogger().debug("Calling load therapy data.");
    var self = this;
    var findTherapyFlowDataUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PHARMACIST_TIMELINE;

    this.getView().showLoaderMask();
    var patientId = this.getPatientId();

    this._setTimelineInterval();
    var params = {
      patientId: patientId,
      patientData: JSON.stringify(this.view.getPatientData()),
      timelineInterval: JSON.stringify({startMillis: this.timelineStart.getTime(), endMillis: this.timelineEnd.getTime()}),
      roundsInterval: JSON.stringify(this.getRoundsInterval()),
      therapySortTypeEnum: therapySortTypeEnum,
      hidePastTherapies: this.hidePastTherapies
    };

    this.getView().loadViewData(findTherapyFlowDataUrl, params, null, function(data)
        {
          self.getView().hideLoaderMask();
          if (patientId === self.getPatientId())
          {
            data.therapyRows = data.therapyRows.map(app.views.medications.timeline.TherapyRow.fromJson);
            self.setActiveTherapyData(data.therapyRows);
            if (!tm.jquery.Utils.isEmpty(self.therapyDataLoadedCallback)) self.therapyDataLoadedCallback(data.therapyRows);
            callback();
          }
        },
        function()
        {
          self.getView().hideLoaderMask();
        });
  },

  _loadReportData: function(callback)
  {
    var fromDate = moment(CurrentTime.get())
        .subtract(this.getView().getPharmacistReviewDisplayDays(), 'days')
        .startOf('day')
        .toDate();

    var self = this;
    var loadReviewsUrl = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PHARMACIST_REVIEWS;

    this.getView().showLoaderMask();

    var patientId = this.view.getPatientId();
    var params = {
      patientId: patientId,
      fromDate: JSON.stringify(fromDate),
      language: this.getView().getViewLanguage()
    };

    this.getView().loadViewData(loadReviewsUrl, params, null, function(data)
    {
      self.getView().hideLoaderMask();

      if (patientId === self.view.getPatientId())
      {
        var reviewData = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.pharmacistReviews) ?
            data.pharmacistReviews : [];
        var taskTimestamp = !tm.jquery.Utils.isEmpty(data) && !tm.jquery.Utils.isEmpty(data.lastTaskChangeTimestamp) ?
            new Date(data.lastTaskChangeTimestamp) : null;

        self._loadedReviews = new app.views.medications.pharmacists.PharmacistMedicationReviewsByDate({
          numberOfDays: self.getView().getPharmacistReviewDisplayDays(),
          reviews: reviewData.map(app.views.medications.pharmacists.dto.PharmacistMedicationReview.fromJson)
        });
        self.setLastTaskChangeTimestamp(taskTimestamp);

        callback();
      }
    });
  },

  _fillTherapyListContainer: function()
  {
    var listContainer = this.getTherapyListContainer();
    var data = this.getActiveTherapyData();
    var routesFilterIsSet = this.routesFilter != null && this.routesFilter.length > 0;
    var customGroupsFilterIsSet = this.customGroupsFilter != null && this.customGroupsFilter.length > 0;
    var therapyRows = tm.jquery.Utils.isEmpty(data) ? [] : data; // clone or create a new one

    if (routesFilterIsSet || customGroupsFilterIsSet)
    {
      var routeFilter = this.routesFilter;
      var customGroupFilter = this.customGroupsFilter;

      therapyRows = jQuery.grep(therapyRows, function(therapy)
      {
        return (!routesFilterIsSet || routeFilter.contains(therapy.route)) &&
            (!customGroupsFilterIsSet || customGroupFilter.contains(therapy.customGroup));
      });
    }

    listContainer.removeAll();

    if (tm.jquery.Utils.isEmpty(this.getGroupField()))
    {
      this._addTherapyContainers(listContainer, therapyRows);
    }
    else
    {
      this._addGroupedTherapyContainers(this.getTherapyRowDataGrouper().group(this.getGroupField(), therapyRows));
    }

    if (therapyRows.isEmpty())
    {
      listContainer.add(this.view.getNoTherapiesField());
    }

    if (listContainer.isRendered()) listContainer.repaint();

    listContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function()
    {
      tm.jquery.ComponentUtils.hideAllTooltips();
    });
  },

  _addTherapyContainers: function(container, therapyData)
  {
    var self = this;
    var view = this.getView();
    var listContainer = this.getTherapyListContainer();

    therapyData.forEach(function(rowData)
    {
      var therapyContainer = app.views.medications.pharmacists.TherapyContainer.forReviewView({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        view: view,
        scrollableElement: listContainer.getDom(),
        data: rowData
      });
      therapyContainer.getToolbar().setAddToIconClickCallback(function(container)
      {
        self.addTherapyToReview(container);
      });
      therapyContainer.getToolbar().setTasksChangedCallback(function()
      {
        self.refreshTherapies();
      });
      container.add(therapyContainer);
    });
  },

  /**
   * @param {Array<app.views.medications.common.overview.TherapyRowGroupData>} groupedData
   * @private
   */
  _addGroupedTherapyContainers: function(groupedData)
  {
    var self = this;
    var listContainer = this.getTherapyListContainer();

    if (!tm.jquery.Utils.isEmpty(groupedData))
    {
      groupedData.forEach(function(group)
      {
        var panel = new tm.jquery.Panel({
          collapsed: false,
          showHeader: !tm.jquery.Utils.isEmpty(group.getKey()),
          showFooter: false,
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
        });

        if (!tm.jquery.Utils.isEmpty(group.getKey()))
        {
          var panelHeader = panel.getHeader();
          panelHeader.setCls('grouping-panel text-unselectable');
          panelHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));
          var panelTitlePane = new tm.jquery.Component({
            padding: "3 0 3 0",
            cursor: "pointer",
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
            html: '<span class="TextData">' + tm.jquery.Utils.escapeHtml(group.getKey()) + '</span>'
          });

          panelHeader.add(panelTitlePane);
          panel.bindToggleEvent([panelTitlePane]);
        }

        var panelContent = panel.getContent();
        panelContent.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
        panelContent.setScrollable('visible');

        self._addTherapyContainers(panelContent, group.getElements());
        listContainer.add(panel);
      });
    }
  },

  /* Recursive method that goes over the therapies in the therapy list and calls the callback for each.
   Will break when the callback method returns true! */
  _processTherapies: function(callback, container)
  {
    container = tm.jquery.Utils.isEmpty(container) ? this.getTherapyListContainer() : container;

    var components = container.getComponents();

    for (var idx = 0; idx < components.length; idx++)
    {
      if (components[idx] instanceof tm.jquery.Panel)
      {
        this._processTherapies(callback, components[idx].getContent());
      }
      else if (components[idx] instanceof app.views.medications.pharmacists.TherapyContainer)
      {
        if (callback(components[idx]) === true) break;
      }
    }
  },

  _fillDailyContainersListContainer: function()
  {
    if (!this._loadedReviews)
    {
      return;
    }

    var self = this;
    var lastTaskChangeDate = !tm.jquery.Utils.isEmpty(this.getLastTaskChangeTimestamp()) ?
        new Date(this.getLastTaskChangeTimestamp().getTime()) : null;

    var todayContainer = new app.views.medications.pharmacists.DailyReviewsContainer({
      view: this.getView(),
      active: true,
      contentDate: this._loadedReviews.getLatestDate(),
      content: this._loadedReviews.getReviewsForLatestDate(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      showPlaceHolder: this._loadedReviews.isNewReviewRequired(
          lastTaskChangeDate,
          this.getView().getTherapyAuthority()),
      showSupply: this.getMedicationsSupplyPresent()
    });
    todayContainer.on(
        app.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_THERAPY_SELECTION_CHANGE,
        function(component, componentEvent)
        {
          self.onTherapySelectionChange(componentEvent.getEventData().action, componentEvent.getEventData().therapyIds);
        });
    todayContainer.on(
        app.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_CHANGE,
        function()
        {
          self.onDailyContainerContentChange();
        });
    todayContainer.on(
        app.views.medications.pharmacists.DailyReviewsContainer.EVENT_TYPE_CONTENT_AUTHORIZED,
        function()
        {
          self.onDailyContainerContentAuthorized();
        });
    this.getReportsListContainer().add(todayContainer);

    // Manually rendering each daily container and inserting it into the dom via jQuery to improve perceived performance.

    if (this.getReportsListContainer().isRendered())
    {
      todayContainer.doRender();
      var $reportsListContainer = jQuery(self.getReportsListContainer().getDom());
      $reportsListContainer.prepend(todayContainer.getDom());
    }

    this._loadedReviews.getAvailablePastDates()
        .forEach(function addDailyContainer(date)
        {
          var dailyContainer = self._createPreviousDayReviewContainer(
              date,
              self._loadedReviews.getReviewsForDate(date));

          self.getReportsListContainer().add(dailyContainer);

          if (self.getReportsListContainer().isRendered())
          {
            dailyContainer.doRender();
            var $reportsListContainer = jQuery(self.getReportsListContainer().getDom());
            $reportsListContainer.append(dailyContainer.getDom());
          }
        });

    this._activeDailyContainer = todayContainer;
  },

  /**
   * @param {Date} date
   * @param {Array<app.views.medications.pharmacists.dto.PharmacistMedicationReview>} reviews
   * @return {app.views.medications.pharmacists.DailyReviewsContainer} to be shown in the UI, containing a group of
   * previously created (read only) reviews for that day.
   * @private
   */
  _createPreviousDayReviewContainer: function(date, reviews)
  {
    return new app.views.medications.pharmacists.DailyReviewsContainer({
      view: this.getView(),
      active: false,
      contentDate: date,
      content: reviews,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      showSupply: this.getMedicationsSupplyPresent()
    });
  },

  _setTimelineInterval: function()
  {
    var newSelected = this.selectedShownTherapies;
    if (tm.jquery.Utils.isEmpty(newSelected))
    {
      newSelected = this.view.actionsHeader.getDefaultTherapiesShownSelection();
    }
    var now = CurrentTime.get();
    if (this.previousSelectedButtonId !== newSelected)
    {
      if (newSelected === "activeTherapies")
      {
        this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0); //-7 days
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 8, 0, 0); //+7 days
        this.hidePastTherapies = true;
      }
      if (newSelected === "threeDaysTherapies")
      {
        this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 3, 0, 0);  //-3 days
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 4, 0, 0);  //+3 days

        this.hidePastTherapies = false;
      }
      if (newSelected === "allTherapies")
      {
        if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData()))  // MedicationsCentralCaseDto
        {
          if (!tm.jquery.Utils.isEmpty(this.view.getCentralCaseData().centralCaseEffective))
          {
            if (this.getView().getCentralCaseData().care !== "HOSPITAL")
            {
              this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0);  //-7 days
            }
            else
            {
              var timelineStart = new Date(this.view.getCentralCaseData().centralCaseEffective.startMillis);
              timelineStart.setDate(timelineStart.getDate() - 1);
              this.timelineStart = new Date(timelineStart.getTime());  //start of hospitalization
            }
          }
          else
          {
            this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0); //-7 days
          }
        }
        else
        {
          this.timelineStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7, 0, 0);  //-7 days
        }
        this.timelineEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 8, 0, 0);  //+7 days
        this.hidePastTherapies = false;
      }
      if (newSelected === "discharge")
      {
        this.timelineStart = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis - 1000 * 60 * 60 * 24); //-24 hours  * 3
        this.timelineEnd = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis + 1000 * 60 * 60 * 24);  //+24 hours  * 3
        this.hidePastTherapies = true;
      }
      if (newSelected === "hospital")
      {
        var start = new Date(this.view.getCentralCaseData().centralCaseEffective.startMillis);
        var end = new Date(this.view.getCentralCaseData().centralCaseEffective.endMillis);
        this.timelineStart = new Date(start.getFullYear(), start.getMonth(), start.getDate() - 1, 0, 0);  // start of hospitalization  - 3, 0, 0
        this.timelineEnd = new Date(end.getFullYear(), end.getMonth(), end.getDate() + 2, 0, 0);  // end of the day of discharge  + 3, 0, 0
        this.hidePastTherapies = false;
      }
      this.previousSelectedButtonId = newSelected;
    }
  },

  ///
  /// public methods
  ///
  refreshData: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.clear();

    this.getView().getLocalLogger().debug("ReviewView refreshData() called.");

    this._loadTherapyData(this.getTherapySortTypeEnum(), function()
    {
      self._fillTherapyListContainer();

      /* wait for the reports to load and mark the therapies*/
      appFactory.createConditionTask(
          function()
          {
            self.markCheckedTherapies();
          },
          function()
          {
            return !tm.jquery.Utils.isEmpty(self.getActiveDailyContainer());
          },
          50, 1000);
    });
    this._loadReportData(function()
    {
      self._fillDailyContainersListContainer();
    });
  },

  ///
  /// getters/setters
  ///
  getView: function()
  {
    return this.view;
  },

  getPatientId: function()
  {
    return this.getView().patientId;
  },

  getPatientHeightInCm: function()
  {
    return this.getView().getPatientHeightInCm();
  },

  getRoundsInterval: function()
  {
    return this.getView().getRoundsInterval();
  },

  getTherapySortTypeEnum: function()
  {
    return this.getView().therapySortTypeEnum;
  },

  getGroupField: function()
  {
    return this.getView().groupField;
  },

  getPharmacistReviewReferBackPreset: function()
  {
    return this.getView().getPharmacistReviewReferBackPreset();
  },

  getMedicationsSupplyPresent: function()
  {
    return this.getView().getMedicationsSupplyPresent();
  },

  getActiveTherapyData: function()
  {
    return this.activeTherapyData;
  },

  setActiveTherapyData: function(data)
  {
    this.activeTherapyData = data;
  },

  setLastTaskChangeTimestamp: function(value)
  {
    this.lastTaskChangeTimestamp = value;
  },

  getLastTaskChangeTimestamp: function()
  {
    return this.lastTaskChangeTimestamp;
  },

  getTherapyListContainer: function()
  {
    return this._therapyColumn.getListContainer();
  },

  getReportsListContainer: function()
  {
    return this._reportsColumn.getListContainer();
  },

  getActiveDailyContainer: function()
  {
    return this._activeDailyContainer;
  },

  getTherapyRowDataGrouper: function()
  {
    return this._therapyRowDataGrouper;
  },

  addTherapyToReview: function(therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    appFactory.createConditionTask(function()
        {
          var dailyContainer = self.getActiveDailyContainer();
          var activeReviewContainer = dailyContainer.getReviewContainerInEdit();
          // has to be deep copy or else references to child DTOs will be kept!
          var therapyData = therapyContainer.getData().clone();

          if (tm.jquery.Utils.isEmpty(activeReviewContainer))
          {
            var now = CurrentTime.get();
            dailyContainer.addReview(new app.views.medications.pharmacists.dto.PharmacistMedicationReview({
              createTimestamp: now,
              composer: view.getCurrentUserAsCareProfessional(),
              relatedTherapies: [therapyData],
              referBackToPrescriber: self.getPharmacistReviewReferBackPreset()
            }), true, true, true);
          }
          else
          {
            activeReviewContainer.addTherapy(therapyData);
          }

          therapyContainer.markActive(true);
          therapyContainer.markChecked(true);
        },
        function()
        {
          return !tm.jquery.Utils.isEmpty(self.getActiveDailyContainer()) && self.getActiveDailyContainer().isRendered();
        },
        50, 100);
  },

  /*
   * Marks therapies present in the current draft reports in the
   * */
  markCheckedTherapies: function()
  {
    var draftTherapyIds = this.getActiveDailyContainer().getDraftReviewsTherapyIds();

    this._processTherapies(function(therapyContainer)
    {
      therapyContainer.markChecked(draftTherapyIds.contains(therapyContainer.getTherapyId()));
    });
  },

  markActiveTherapies: function()
  {
    var dailyContainer = this.getActiveDailyContainer();
    var activeReviewContainer = !tm.jquery.Utils.isEmpty(dailyContainer) ? dailyContainer.getReviewContainerInEdit() : null;

    if (!tm.jquery.Utils.isEmpty(activeReviewContainer))
    {
      var therapies = activeReviewContainer.getActiveTherapies();
      var therapyIds = therapies.map(function(relatedTherapy)
      {
        return tm.jquery.Utils.isEmpty(relatedTherapy.therapy) ? null : relatedTherapy.therapy.compositionUid;
      });
      this.onTherapySelectionChange('set', therapyIds);
    }
  },

  onDailyContainerContentChange: function()
  {
    this.markCheckedTherapies();
  },

  onDailyContainerContentAuthorized: function()
  {
    this.refreshTherapies();
    this.getView().refreshPatientsCumulativeAntipsychoticPercentage();
  },

  onTherapySelectionChange: function(action, therapyIds)
  {
    if (action === 'set')
    {
      this._processTherapies(function(therapyContainer)
      {
        for (var idx = 0; idx < therapyIds.length; idx++)
        {
          if (therapyContainer.getTherapyId() === therapyIds[idx])
          {
            therapyContainer.markActive(true);
            return;
          }
        }
        therapyContainer.markActive(false);
      });
    }
    else if (action === 'remove')
    {
      this._processTherapies(function(therapyContainer)
      {
        if (therapyContainer.getTherapyId() === therapyIds[0])
        {
          therapyContainer.markActive(false);
          return true;
        }
      });
      this.markCheckedTherapies(); // go trough all therapies again in case the therapy was already present on another review
    }
  },

  refreshTherapies: function()
  {
    var self = this;
    this._loadTherapyData(
        this.getTherapySortTypeEnum(),
        function()
        {
          self._fillTherapyListContainer();
          self.markActiveTherapies();
          self.markCheckedTherapies();
        });
  },

  clear: function()
  {
    this.getTherapyListContainer().removeAll();
    this._therapyColumn.setColumnTitle(this.getView().getDictionary("therapies"));
    this.getReportsListContainer().removeAll();
    this._activeDailyContainer = null;
    this.previousSelectedButtonId = null;
    this.selectedShownTherapies = null;
  },

  setRoutesAndCustomGroupsFilter: function(routes, customgroups, applyFilter)
  {
    // comparing by reference since a new array for routes and groups is always created and always set by the toolbar
    if (this.routesFilter !== routes || this.customGroupsFilter !== customgroups)
    {
      this.routesFilter = routes;
      this.customGroupsFilter = customgroups;

      if (applyFilter === true)
      {
        this._fillTherapyListContainer();
        this.markActiveTherapies();
        this.markCheckedTherapies();
      }
    }
  },

  setShownTherapies: function(shownTherapies)
  {
    this.selectedShownTherapies = shownTherapies;
  }
});
