Class.define('app.views.medications.grid.GridView', 'tm.jquery.Container', {

  /** configs */
  view: null,
  /** privates */
  state: null,
  /** @type number */
  dayCount: NaN,
  therapySortTypeEnum: null,
  todayIndex: null,
  searchDate: null,
  grouping: null,
  isRoundsTime: true,
  therapyFlowData: null,
  previousDayTherapyFlowData: null,
  nextDayTherapyFlowData: null,
  previousDayDataLoaded: false,
  nextDayDataLoaded: false,
  navigationLocked: false,
  actionsQueue: null,
  therapyAction: null,
  /** privates: components */
  /** @type tm.jquery.Grid */
  grid: null,
  noTherapiesField: null,

  _isGridReadyConditionalTask: null,
  _therapyRowDataGrouper: null,

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      cls: "therapy-flow-grid-container",
      //layout: new tm.jquery.BorderLayout()
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    }, config);

    this.callSuper(config);

    this.actionsQueue = [];
    this.therapyActions = new app.views.medications.common.therapy.TherapyActions({view: this.getView()});
    this._therapyRowDataGrouper = new app.views.medications.common.overview.TherapyRowDataGrouper({view: this.getView()});
  },

  /** private methods */
  _paintGrid: function(searchDate)
  {
    var self = this;

    this.grid = new tm.jquery.Grid({
      cls: 'therapy-grid',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "100%"),
      selectable: false,
      highlighting: false,

      data: new tm.jquery.GridData({
        source: []
      }),
      columns: new tm.jquery.GridColumns({
        names: self._getColumnNames(searchDate),
        model: self._createModelColumn()
      }),
      rows: new tm.jquery.GridRows({
        rowCount: 100,
        rowNumbers: false,
        multiSelect: false
      }),
      grouping: self.grouping,
      options: {
        autowidth: true,
        shrinkToFit: true /* makes the column width property act as a proportion instead of fixed pixel size */
      }
    });

    this.noTherapiesField = this.getView().getNoTherapiesField();
    this.add(this.noTherapiesField);
    this.noTherapiesField.hide();
    this.add(this.grid, {region: 'center'});
  },

  _setGridData: function(gridData, adjustedParams)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    if (this._isGridReadyConditionalTask)
    {
      this._abortIsGridReadyConditionalTask();
    }

    this._isGridReadyConditionalTask = appFactory.createConditionTask(
        function()
        {
          hideLoaderMaskClearTask();
          var scrollPosition = $(self.grid.getScrollableElement()).scrollTop();
          self.grid.setGridData(gridData);
          self._fixGridColumns(adjustedParams);
          $(self.grid.getScrollableElement()).scrollTop(scrollPosition);
        },
        function(task)
        {
          if (!self.isRendered())
          {
            task.abort();
            hideLoaderMaskClearTask();
          }
          return self.grid && self.grid.isRendered() && !tm.jquery.Utils.isEmpty(self.grid.getPlugin())
        },
        function()
        {
          hideLoaderMaskClearTask();
        },
        100, 100);

    function hideLoaderMaskClearTask()
    {
      self._isGridReadyConditionalTask = null;
      view.hideLoaderMask();
    }
  },

  _executeTaskWhenCondition: function(condition, task)
  {
    var self = this;
    if (condition())
    {
      task();
    }
    else
    {
      setTimeout(
          function()
          {
            self._executeTaskWhenCondition(condition, task)
          }, 50);
    }
  },

  _fixGridColumns: function(searchParams)
  {
    var lastColumnName = 'column' + this.dayCount;
    if (searchParams.todayIndex !== null || this.dayCount === 1)
    {
      this.grid.getPlugin().jqGrid('hideCol', lastColumnName);
    }
    else
    {
      this.grid.getPlugin().jqGrid('showCol', lastColumnName);
    }

    var date = new Date(searchParams.searchDate);
    for (var i = 0; i < searchParams.dayCount; i++)
    {
      /* see http://stackoverflow.com/questions/12171640/jqgrid-changing-the-width-of-a-column-dynamically/12172228#12172228 */
      this.grid.getPlugin().setColProp('column' + i, {
        width: searchParams.todayIndex === i ? 2 : 1,
        widthOrg: searchParams.todayIndex === i ? 2 : 1
      });

      var headerLabel = this._getHeaderString(date, i === searchParams.todayIndex, i);
      this.grid.setHeaderLabel('column' + i, headerLabel);
      date.setDate(date.getDate() + 1);
    }

    var gridWidth = jQuery(this.grid.getDom()).width();
    /* we need the actual pixels */
    this.grid.getPlugin().jqGrid('setGridWidth', gridWidth);
    /* makes the grid resize the columns */
  },

  /**
   * @param {Object} searchParams
   * @returns {tm.jquery.Promise}
   * @private
   */
  _loadData: function(searchParams)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var patientId = view.getPatientId();

    view.getRestApi().loadTherapyFlowGridData(searchParams, this.therapySortTypeEnum).then(
        function(therapyFlowData)
        {
          if (patientId === view.getPatientId())
          {
            var convertedTherapyFlowData = app.views.medications.grid.dto.TherapyFlow.fromJson(therapyFlowData);
            self.therapyFlowData = convertedTherapyFlowData;
            deferred.resolve(self._buildGridData(convertedTherapyFlowData.getTherapyRows()));
          }
        }
    );
    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.grid.dto.TherapyFlowRow>} therapyFlowRows
   * @return {Array<>}
   * @private
   */
  _buildGridData: function(therapyFlowRows)
  {
    return therapyFlowRows.map(
        function createGridRowFromTherapyFlowRow(flowRow, index)
        {
          var rowGroupData = this.getTherapyRowDataGrouper().createHtmlGroupKeys(flowRow);
          rowGroupData.id = index;

          for (var dayIndex = 0; dayIndex < this.dayCount + 1; dayIndex++)
          {
            var cellData = flowRow.getTherapyFlowDayMap()[dayIndex];
            rowGroupData['column' + dayIndex] = cellData ?
                {
                  id: dayIndex,
                  dayTherapy: cellData
                } :
                null;
          }
          return rowGroupData;
        },
        this);
  },

  _loadPreviousDayData: function()
  {
    var self = this;
    this.previousDayDataLoaded = false;
    var view = this.getView();

    var previousDate = new Date(this.searchDate);
    previousDate.setDate(previousDate.getDate() - 1);
    var adjustedParams = self._getAdjustedSearchParams(previousDate, false);
    var patientId = view.getPatientId();
    view.getRestApi().loadTherapyFlowGridData(adjustedParams, this.therapySortTypeEnum, true).then(
        function(therapyFlowData)
        {
          if (patientId === view.getPatientId())
          {
            self.previousDayTherapyFlowData = app.views.medications.grid.dto.TherapyFlow.fromJson(therapyFlowData);
            self.previousDayDataLoaded = true;
          }
        })
  },

  _loadNextDayData: function()
  {
    var self = this;
    this.nextDayDataLoaded = false;
    var view = this.getView();

    var nextDate = new Date(this.searchDate);
    nextDate.setDate(nextDate.getDate() + 1);
    var adjustedParams = self._getAdjustedSearchParams(nextDate, true);
    var patientId = view.getPatientId();

    view.getRestApi().loadTherapyFlowGridData(adjustedParams, this.therapySortTypeEnum, true).then(
        function(therapyFlowData)
        {
          if (patientId === view.getPatientId())
          {
            self.nextDayTherapyFlowData = app.views.medications.grid.dto.TherapyFlow.fromJson(therapyFlowData);
            self.nextDayDataLoaded = true;
          }
        })
  },

  _getAdjustedSearchParams: function(searchDate, forward)
  {
    var todayIndex = this._getTodayIndex(this.dayCount, searchDate);

    var adjustedDayCount = this.dayCount;
    var adjustedSearchDate = searchDate;

    if (todayIndex === null && this.dayCount !== 1)
    {
      if (forward !== null && this._getTodayIndex(this.dayCount + 1, searchDate) !== null)
      {
        adjustedDayCount = this.dayCount + 1;
        if (forward)
        {
          adjustedSearchDate.setDate(searchDate.getDate() + 1);
        }
        else
        {
          adjustedSearchDate.setDate(searchDate.getDate() - 1);
        }
      }
      else
      {
        adjustedDayCount = this.dayCount + 1;
      }
    }

    var adjustedTodayIndex = this._getTodayIndex(adjustedDayCount, adjustedSearchDate);

    return {
      dayCount: adjustedDayCount,
      searchDate: adjustedSearchDate,
      todayIndex: adjustedTodayIndex
    }
  },

  _getColumnNames: function(searchDate)
  {
    var self = this;
    var columns = [];
    columns.push("id");
    columns.push("atcGroup");
    columns.push("routes");
    columns.push("customGroup");
    columns.push("prescriptionGroup");
    var date = new Date(searchDate);
    for (var j = 0; j < this.dayCount + 1; j++)
    {
      columns.push(this._getHeaderString(date, j === self.todayIndex, j));
      date.setDate(date.getDate() + 1);
    }
    return columns;
  },

  _getHeaderString: function(date, today, index)
  {
    var view = this.getView();
    var dayString = view.getDisplayableValue(new Date(date), "short.date");
    var referenceWeightString = "";
    if (this.therapyFlowData)
    {
      var referenceWeight = this.therapyFlowData.getReferenceWeightsDayMap()[index];
      if (referenceWeight)
      {
        referenceWeightString = app.views.medications.MedicationUtils.doubleToString(referenceWeight, 'n3') + 'kg';
      }
    }

    var headerString = "<div class='TextData' style='float:left; width:33%; height: 18px; text-align:left; padding-left:5px;'>" + referenceWeightString + "</div>";
    if (today)
    {
      return headerString +
          "<div class='TextData' style='float:left; width:33%; text-align:center; overflow: visible;'> <b>" + view.getDictionary("today") + " " + dayString + "</b></div>";
    }

    return headerString +
        "<div class='TextData' style='float:left; width:33%; text-align:center; overflow: visible;'> " + dayString + "</div>";
  },

  _getTodayIndex: function(dayCount, searchDate)
  {
    var date = new Date(searchDate);
    var today = CurrentTime.get();
    for (var j = 0; j < dayCount; j++)
    {
      if (today.getDate() === date.getDate() &&
          today.getMonth() === date.getMonth() &&
          today.getFullYear() === today.getFullYear())
      {
        return j;
      }
      date.setDate(date.getDate() + 1);
    }
    return null;
  },

  _createModelColumn: function()
  {
    var self = this;
    var columns = [];
    columns.push({
      name: 'id',
      index: 'id',
      width: 0,
      hidden: true
    });
    columns.push({
      name: 'atcGroup',
      index: 'atcGroup',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.atcGroup + "." + app.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });
    columns.push({
      name: 'routes',
      index: 'routes',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.routes + "." + app.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });
    columns.push({
      name: 'customGroup',
      index: 'customGroup',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.customGroup + "." + app.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });
    columns.push({
      name: 'prescriptionGroup',
      index: 'prescriptionGroup',
      width: 0,
      hidden: true,
      sorttype: function(cellValue, obj)
      {
        return obj.prescriptionGroup + "." + app.views.medications.MedicationUtils.pad(obj.id, 4);
      }
    });

    for (var i = 0; i < this.dayCount + 1; i++)
    {
      columns.push({
            name: 'column' + i,
            index: i,
            width: this.todayIndex === i ? 2 : 1,
            sortable: false,
            title: false,
            hidden: self.todayIndex > 0 && i === self.dayCount,
            resizable: false,
            formatter: new app.views.medications.grid.TherapyGridCellFormatter({
              view: this.getView(),
              gridView: this,
              dayCount: this.dayCount,
              todayIndex: this.todayIndex}),
            cellattr: function(rowId, value, rowObject, colModel)
            {
              var cellData = rowObject[colModel.name];
              var enums = app.views.medications.TherapyEnums;
              var dayTherapy = cellData ? cellData.dayTherapy : null;
              if (dayTherapy)
              {
                var isPastDay = self._isPastDay(colModel.index);
                var isActiveTodayOrFutureTherapy = !isPastDay && dayTherapy.active
                    && dayTherapy.therapyStatus !== enums.therapyStatusEnum.ABORTED
                    && dayTherapy.therapyStatus !== enums.therapyStatusEnum.CANCELLED
                    && dayTherapy.therapyStatus !== enums.therapyStatusEnum.SUSPENDED;
                var isActivePastTherapy = isPastDay && dayTherapy.activeAnyPartOfDay;
                if (isActiveTodayOrFutureTherapy || isActivePastTherapy)
                {
                  return 'style="vertical-align:top; background:#fff';
                }
              }
              return 'style="vertical-align:top;background:#E2DFDF"';
            }
          }
      );
    }
    return columns;
  },

  _isPastDay: function(index)
  {
    var todayIndex = this._getTodayIndex(this.dayCount, this.searchDate);
    if (todayIndex)
    {
      return index < todayIndex;
    }
    return this.searchDate < CurrentTime.get();
  },

  _canTherapyBeAborted: function(therapy)
  {
    var enums = app.views.medications.TherapyEnums;
    if (therapy.linkName)
    {
      var nextTherapyLink = app.views.medications.MedicationUtils.getNextLinkName(therapy.linkName);
      var linkedTherapyDay = this._getTherapyDayByLinkName(nextTherapyLink);
      if (linkedTherapyDay && linkedTherapyDay.therapyStatus)
      {
        if (linkedTherapyDay.therapyStatus !== enums.therapyStatusEnum.ABORTED &&
            linkedTherapyDay.therapyStatus !== enums.therapyStatusEnum.CANCELLED)
        {
          return false;
        }
      }
    }
    return true;
  },

  /**
   * @param {String|null} linkName
   * @returns {app.views.medications.grid.dto.TherapyDay}
   * @private
   */
  _getTherapyDayByLinkName: function(linkName)
  {
    for (var i = 0; i < this.therapyFlowData.getTherapyRows().length; i++)
    {
      var therapyRow = this.therapyFlowData.getTherapyRows()[i];
      var therapyDay = therapyRow.getTherapyFlowDayMap()[this.todayIndex];
      if (therapyDay &&
          therapyDay.getTherapy() &&
          therapyDay.getTherapy().getLinkName() &&
          therapyDay.getTherapy().getLinkName() === linkName)
      {
        return therapyDay;
      }
    }
    return null;
  },

  _executeAbortTherapyTask: function(nextTask)
  {
    this.therapyActions
        .abortTherapy(nextTask.therapy)
        .then(
            this._onSuccessfulTherapyStopped.bind(this, nextTask.therapy, nextTask.rowIndex),
            this._removeLastQueuedActionAndProcessNext.bind(this))
  },

  _executeSuspendTherapyTask: function(nextTask)
  {
    this.therapyActions
        .suspendTherapy(nextTask.therapy)
        .then(
            this._onSuccessfulTherapyAction.bind(this, nextTask.therapy, nextTask.rowIndex),
            this._removeLastQueuedActionAndProcessNext.bind(this));
  },

  /**
   * Suspends all therapies and reloads the grid on success. Fires next task execution.
   * @private
   */
  _executeSuspendAllTherapiesTask: function()
  {
    var self = this;
    this.therapyActions.suspendAllTherapies()
        .then(
            function()
            {
              self.reloadGridData()
                  .then(self._removeLastQueuedActionAndProcessNext.bind(self))
            },
            this._removeLastQueuedActionAndProcessNext.bind(this));
  },

  /**
   * Suspends all therapies for temporary leave and reloads the grid on success. Fires next task execution.
   * @private
   */
  _executeSuspendAllTherapiesForTemporaryLeaveTask: function()
  {
    var self = this;

    this.therapyActions
        .suspendAllTherapiesForTemporaryLeave()
        .then(
            function()
            {
              self.reloadGridData()
                  .then(self._removeLastQueuedActionAndProcessNext.bind(self))
            },
            this._removeLastQueuedActionAndProcessNext.bind(this))
  },

  /**
   * Stops all therapies and reloads the grid on success. Fires next task execution.
   * @private
   */
  _executeStopAllTherapiesTask: function()
  {
    var self = this;

    this.therapyActions
        .stopAllTherapies()
        .then(
            function()
            {
              self.getView().refreshPatientsCumulativeAntipsychoticPercentage();
              self.reloadGridData()
                  .then(self._removeLastQueuedActionAndProcessNext.bind(self))
            },
            this._removeLastQueuedActionAndProcessNext.bind(this))
  },

  _executeTasks: function(newTask)
  {
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var self = this;
    var view = this.getView();
    if (this.actionsQueue.length === 1 || (!newTask && this.actionsQueue.length > 0))
    {
      var nextTask = self.actionsQueue[0];
      if (nextTask.action === 'CONFIRM')
      {
        this.therapyActions
            .reviewTherapy(nextTask.therapy)
            .then(
                this._onSuccessfulTherapyAction.bind(this, nextTask.therapy, nextTask.rowIndex),
                this._removeLastQueuedActionAndProcessNext.bind(this));
      }
      else if (nextTask.action === 'ABORT')
      {
        var rowData = this.therapyFlowData.getTherapyRows()[nextTask.rowIndex];
        var therapyDay = rowData.getTherapyFlowDayMap()[this.todayIndex];
        if (this._canTherapyBeAborted(nextTask.therapy, therapyDay))
        {
          this._executeAbortTherapyTask(nextTask);
        }
        else
        {
          self._removeLastQueuedActionAndProcessNext();
          var message = view.getDictionary('therapy.can.not.stop.if.linked');
          view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
        }
      }
      else if (nextTask.action === 'REISSUE')
      {
        this.therapyActions
            .reissueTherapy(nextTask.therapy)
            .then(
                this._onSuccessfulTherapyAction.bind(this, nextTask.therapy, nextTask.rowIndex),
                this._removeLastQueuedActionAndProcessNext.bind(this)
            );
      }
      else if (nextTask.action === 'SUSPEND')
      {
        this._executeSuspendTherapyTask(nextTask);
      }
      else if (nextTask.action === 'SUSPEND_ALL')
      {
        this._executeSuspendAllTherapiesTask();
      }
      else if (nextTask.action === 'SUSPEND_ALL_TEMPORARY_LEAVE')
      {
        this._executeSuspendAllTherapiesForTemporaryLeaveTask();
      }
      else if (nextTask.action === 'STOP_ALL')
      {
        this._executeStopAllTherapiesTask();
      }
    }
  },

  /**
   * Ensure grid is still rendered before updating therapy or refreshing view.
   * Process next queued action even if grid is no longer available (rendered)
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Number} rowIndex
   * @private
   */
  _onSuccessfulTherapyAction: function(therapy, rowIndex)
  {
    if (!this.grid.isRendered())
    {
      this._removeLastQueuedActionAndProcessNext();
      return;
    }
    var self = this;
    var view = this.getView();
    var ehrCompositionId = therapy.getCompositionUid();

    view.getRestApi()
        .reloadSingleTherapyAfterAction(therapy)
        .then(function(reloadAfterActionDto)
            {
              if (self.grid.isRendered())
              {
                self._updateAllTherapiesAndTherapyDayData(
                    ehrCompositionId,
                    reloadAfterActionDto
                );
                self._refreshTherapiesAfterAction(rowIndex);
              }
              self._removeLastQueuedActionAndProcessNext();
            },
            self._removeLastQueuedActionAndProcessNext.bind(self));
  },

  _refreshTherapiesAfterAction: function(rowIndex)
  {
    var scrollPosition = tm.jquery.ComponentUtils.getScrollPosition(this.grid.getScrollableElement());
    var gridData = this._buildGridData(this.therapyFlowData.getTherapyRows());
    this.grid.setRowData(rowIndex, gridData[rowIndex]);

    if (!tm.jquery.Utils.isEmpty(scrollPosition) && scrollPosition.scrollTop)
    {
      this.grid.scrollTo(scrollPosition.scrollLeft, scrollPosition.scrollTop, 0);
    }
  },

  /**
   * Updates therapies and therapy days, including previous and next day data, after therapy action.
   * @param {String} oldCompositionUid
   * @param {app.views.medications.grid.dto.TherapyReloadAfterAction} reloadAfterActionDto
   * @private
   */
  _updateAllTherapiesAndTherapyDayData: function(oldCompositionUid, reloadAfterActionDto)
  {
    this._updateTherapiesAndTherapyDayData(
        this.therapyFlowData.getTherapyRows(),
        oldCompositionUid,
        reloadAfterActionDto,
        this.todayIndex,
        null
    );
    var previousDay = new Date(this.searchDate);
    previousDay.setDate(previousDay.getDate() - 1);
    var previousDaySearchParameters = this._getAdjustedSearchParams(previousDay, false);
    this._updateTherapiesAndTherapyDayData(
        this.previousDayTherapyFlowData.getTherapyRows(),
        oldCompositionUid,
        reloadAfterActionDto,
        previousDaySearchParameters.todayIndex,
        false
    );
    var nextDay = new Date(this.searchDate);
    nextDay.setDate(nextDay.getDate() + 1);
    var nextDaySearchParameters = this._getAdjustedSearchParams(nextDay, true);
    this._updateTherapiesAndTherapyDayData(
        this.nextDayTherapyFlowData.getTherapyRows(),
        oldCompositionUid,
        reloadAfterActionDto,
        nextDaySearchParameters.todayIndex,
        true
    );
  },

  _removeLastQueuedActionAndProcessNext: function()
  {
    this.actionsQueue.shift();
    this._executeTasks(false);
  },

  /**
   * Safely abort the conditional task that blocks setting data to the grid if it's not ready.
   * @private
   */
  _abortIsGridReadyConditionalTask: function()
  {
    if (this._isGridReadyConditionalTask)
    {
      this._isGridReadyConditionalTask.abort();
      this._isGridReadyConditionalTask = null;
    }
  },

  /**
   * Updates data on therapy and therapy day data after therapy action.
   * @param {Array<app.views.medications.common.dto.Therapy>} therapies
   * @param {String} oldCompositionUid
   * @param {app.views.medications.grid.dto.TherapyReloadAfterAction} reloadAfterActionDto
   * @param {Number} todayIndex
   * @param {Boolean|null} forward
   * @private
   */
  _updateTherapiesAndTherapyDayData: function(therapies, oldCompositionUid, reloadAfterActionDto, todayIndex, forward)
  {
    var status = reloadAfterActionDto.getTherapyStatus();
    var enums = app.views.medications.TherapyEnums;
    for (var i = 0; i < therapies.length; i++)
    {
      var therapyRow = therapies[i];
      for (var day in therapyRow.getTherapyFlowDayMap())
      {
        var dayTherapy = therapyRow.getTherapyFlowDayMap()[day];                                   // [TherapyDayDto.java]
        var therapy = dayTherapy.getTherapy();
        var oldUidWithoutVersion = app.views.medications.MedicationUtils.getUidWithoutVersion(oldCompositionUid);
        var newUidWithoutVersion = app.views.medications.MedicationUtils.getUidWithoutVersion(therapy.compositionUid);
        if (oldUidWithoutVersion === newUidWithoutVersion)
        {
          therapy.setCompositionUid(reloadAfterActionDto.getEhrCompositionId());
          if (therapy.getEhrOrderName() === reloadAfterActionDto.getEhrOrderName())
          {
            dayTherapy.setTherapyStatus(status);
            dayTherapy.setDoctorReviewNeeded(reloadAfterActionDto.getDoctorReviewNeeded());
            dayTherapy.setTherapyEndsBeforeNextRounds(reloadAfterActionDto.getTherapyEndsBeforeNextRounds());
            dayTherapy.setStatusReason(reloadAfterActionDto.getStatusReason());
            if (status === enums.therapyStatusEnum.ABORTED || status === enums.therapyStatusEnum.CANCELLED)
            {
              if (forward === null)
              {
                if (day > todayIndex)
                {
                  delete therapyRow.getTherapyFlowDayMap()[day];
                }
              }
              else if (forward === true)
              {
                if (todayIndex === null || day > todayIndex)
                {
                  delete therapyRow.getTherapyFlowDayMap()[day];
                }
              }
              else if (forward === false)
              {
                if (todayIndex !== null && day > todayIndex)
                {
                  delete therapyRow.getTherapyFlowDayMap()[day];
                }
              }
            }
            if (reloadAfterActionDto.getTherapyStart())
            {
              therapy.setStart(reloadAfterActionDto.getTherapyStart());
            }
            if (reloadAfterActionDto.getTherapyEnd())
            {
              therapy.setEnd(reloadAfterActionDto.getTherapyEnd());
            }
          }
        }
      }
    }
  },

  /**
   * Success handler for therapy stop action. Will fire common success handler for therapy actions and refresh patients
   * cumulative max dose, since it can potentially change when a therapy is stopped.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Number} rowIndex
   * @private
   */
  _onSuccessfulTherapyStopped: function(therapy, rowIndex)
  {
    this._onSuccessfulTherapyAction(therapy, rowIndex);
    this.getView().refreshPatientsCumulativeAntipsychoticPercentage();
  },

  /**
   * Getters & Setters
   */

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {HTMLElement}
   */
  getScrollableElement: function()
  {
    return this.grid.getScrollableElement();
  },

  /**
   * @returns {Number}
   */
  getTodayColumnIndex: function()
  {
    return this.todayIndex;
  },

  /**
   * @return {app.views.medications.common.overview.TherapyRowDataGrouper}
   */
  getTherapyRowDataGrouper: function()
  {
    return this._therapyRowDataGrouper;
  },

  /** public methods */

  /**
   * Reloads data and shows grid if data is not empty. Otherwise, shows no therapies notification.
   * Also reloads previous and next day data.
   * @returns {tm.jquery.Promise}
   */
  reloadGridData: function()
  {
    var self = this;

    var viewHubNotifier = this.getView().getHubNotifier();
    var therapyFlowLoadHubAction = tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB;
    viewHubNotifier.actionStarted(therapyFlowLoadHubAction);

    var deferred = tm.jquery.Deferred.create();

    var adjustedParams = this._getAdjustedSearchParams(this.searchDate, null);
    this._loadData(adjustedParams).then(
        function(gridData)
        {
          if (gridData.isEmpty())
          {
            self.noTherapiesField.show();
            self.grid.hide();
          }
          else
          {
            self.noTherapiesField.hide();
            self.grid.show();
          }
          self._setGridData(gridData, adjustedParams);
          viewHubNotifier.actionEnded(therapyFlowLoadHubAction);
          deferred.resolve();
        });
    setTimeout(function()
    {
      self._loadPreviousDayData();
      self._loadNextDayData();
    }, 0);

    return deferred.promise();
  },

  changeSearchDate: function(forward)
  {
    var viewHubNotifier = this.getView().getHubNotifier();
    var therapyFlowNavigateHubAction = tm.views.medications.TherapyView.THERAPY_FLOW_NAVIGATE_HUB;
    viewHubNotifier.actionStarted(therapyFlowNavigateHubAction);

    var self = this;

    if (this.navigationLocked === true)
    {
      return;
    }
    this.navigationLocked = true;
    this._executeTaskWhenCondition(
        function()
        {
          if (forward)
          {
            return self.nextDayDataLoaded === true;
          }
          else
          {
            return self.previousDayDataLoaded === true;
          }
        },
        function()
        {
          var gridData;
          var adjustedParams;
          if (forward)
          {
            self.searchDate.setDate(self.searchDate.getDate() + 1);
            self.therapyFlowData = self.nextDayTherapyFlowData;
            gridData = self._buildGridData(self.therapyFlowData.getTherapyRows());
            adjustedParams = self._getAdjustedSearchParams(self.searchDate, true);
          }
          else
          {
            self.searchDate.setDate(self.searchDate.getDate() - 1);
            self.therapyFlowData = self.previousDayTherapyFlowData;
            gridData = self._buildGridData(self.therapyFlowData.getTherapyRows());
            adjustedParams = self._getAdjustedSearchParams(self.searchDate, false);
          }
          self.searchDate = adjustedParams.searchDate;
          self.todayIndex = adjustedParams.todayIndex;
          self._setGridData(gridData, adjustedParams);
          setTimeout(function()
          {
            self._loadPreviousDayData();
            self._loadNextDayData();
          }, 0);
          setTimeout(function()
          {
            self.navigationLocked = false;
          }, 0);
          viewHubNotifier.actionEnded(therapyFlowNavigateHubAction);
        });
  },

  repaintGrid: function(dayCount, searchDate, therapySortTypeEnum)
  {
    var self = this;

    this.removeAll(true);
    this.dayCount = dayCount;
    this.therapySortTypeEnum = therapySortTypeEnum;
    var adjustedParams = self._getAdjustedSearchParams(searchDate, null);
    this.searchDate = adjustedParams.searchDate;
    this.todayIndex = adjustedParams.todayIndex;

    this._paintGrid(this.searchDate);
    this.repaint();
    setTimeout(function()
    {
      self._loadPreviousDayData();
      self._loadNextDayData();
    }, 0);
  },

  paintGrid: function(dayCount, searchDate, groupField, therapySortTypeEnum)
  {
    var self = this;
    this.createGrouping(groupField);
    this.dayCount = dayCount;
    this.therapySortTypeEnum = therapySortTypeEnum;
    var adjustedParams = self._getAdjustedSearchParams(searchDate, null);
    this.searchDate = adjustedParams.searchDate;
    this.todayIndex = adjustedParams.todayIndex;
    this.removeAll(true);
    this._paintGrid(this.searchDate);
    this.repaint();
  },

  setGrouping: function(groupField)
  {
    this.createGrouping(groupField);
    if (this.grouping)
    {
      this.grid.setGrouping(this.grouping);
    }
    else
    {
      this.grid.removeGrouping();
    }
    this._getAdjustedSearchParams(this.searchDate, null);
  },

  createGrouping: function(groupField)
  {
    if (groupField)
    {
      this.grouping = new tm.jquery.GridGrouping({
        groupAlignment: 'right',
        groupField: [groupField],
        groupOrder: ['asc'],
        groupColumnShow: [false],
        groupSummary: [false]
      });
    }
    else
    {
      this.grouping = null;
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Number} rowIndex
   * @param {String} action
   * @param {String} [suspendReason=null]
   */
  addActionToQueue: function(therapy, rowIndex, action, suspendReason)
  {
    this.actionsQueue.push({
      therapy: therapy,
      rowIndex: rowIndex,
      action: action,
      suspendReason: suspendReason
    });
    this._executeTasks(true);
  },

  clear: function()
  {
    this.removeAll();
  },

  /* @Override */
  destroy: function()
  {
    this._abortIsGridReadyConditionalTask();
    this.callSuper();
  }
});