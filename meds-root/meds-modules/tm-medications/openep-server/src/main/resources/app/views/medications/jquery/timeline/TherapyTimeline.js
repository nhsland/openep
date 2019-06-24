Class.define('app.views.medications.timeline.TherapyTimeline', 'tm.jquery.Container', {
  cls: "therapy-timeline",
  scrollable: "visible",

  /** config */
  view: null,
  timelineIndex: null,
  patientId: null,
  timelineRangeChangedFunction: null,
  reloadTimelinesFunction: null,
  intervalStart: null,
  intervalEnd: null,
  start: null,
  end: null,
  scrollableElement: null,
  /** privates */
  options: null,
  displayProvider: null,
  therapyTimelineRows: null, //[TherapyTimelineRowDto]
  /** privates: components */
  timeline: null,
  timelineContainer: null,
  /** @type app.views.medications.common.therapy.TherapyActions */
  _therapyActions: null,

  _itemSet: null,
  _groupSet: null,
  _administrationDialogBuilder: null,

  _ghostClick: null,
  _ghostClickTimer: null,

  _lastActionGuid: null,
  _preventDoubleClickOnTask: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.therapyTimelineRows = [];
    this.setLayout(tm.jquery.VFlexboxLayout.create("start", "stretch"));
    this._therapyActions = new app.views.medications.common.therapy.TherapyActions({view: config.view});
    this.displayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({view: this.getView()});
    this.groupHeaderDisplayProvider =  new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView(),
      showChangeHistory: false,
      showChangeReason: false
    });
    this._administrationDialogBuilder = new app.views.medications.timeline.administration.TherapyAdministrationDialogBuilder({
      view: this.getView()
    });

    this.options = {
      width: "100%",
      locale: this.getView().getViewLanguage(),
      locales: {},
      orientation: {axis: 'none', item: 'top'},
      margin: {
        axis: 0,
        item: 0
      }, /* default margin is 20px, which affects the position of items in the first group since we hide it */
      showMajorLabels: true,
      showMinorLabels: false,
      moveable: true,
      max: this.intervalEnd,
      min: this.intervalStart,
      zoomable: true,
      zoomMax: 500000000,
      zoomMin: 1000000,
      stack: false,
      start: this.start, //new Date(now.getTime() - 12 * 60 * 60 * 1000),      //24 hours
      end: this.end, //new Date(now.getTime() + 24 * 60 * 60 * 1000),       //12 hours
      type: 'point',
      selectable: false, /* prevent selectable styles being added to content since we don't need them */
      align: 'center',
      groupOrder: "orderIndex"
      //timeAxis: { scale: 'hour', step: 1 }
    };
    // create locale (text strings should be replaced with localized strings) for timeline
    this.options.locales[this.options.locale] = {
      current: this.getView().getDictionary("visjs.timeline.current"),
      time: this.getView().getDictionary("visjs.timeline.time")
    };
    var self = this;
    this.getView().on(tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
        function(component, componentEvent)
        {
          var eventData = componentEvent.getEventData();
          self._medicationIdentifierScanned(eventData.barcodeTaskSearch, eventData.barcode);

        });
    this._buildGui();
  },

  /** private methods */
  _buildGui: function(elements, groups)
  {
    var self = this;
    this.timelineContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });
    this.add(this.timelineContainer);
    this.timelineContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._paintTimeline(component, elements, groups);
    });

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-timeline-coordinator',
      view: this.getView(),
      component: this.timelineContainer
    });
  },

  _paintTimeline: function(paintToComponent, elements, groups)
  {
    var self = this;
    var view = self.getView();

    this._itemSet = new vis.DataSet();
    this._itemSet.add(elements);
    this._groupSet = new vis.DataSet();
    this._groupSet.add(groups);

    if (this.timeline) {
      this.timeline.destroy();
      this.timeline = null;
    }

    this.timeline = new vis.Timeline(paintToComponent.getDom(), this._itemSet, this._groupSet, this.options);
    this.timeline.setCurrentTime(CurrentTime.get());

    var timelineWindow = this.timeline.getWindow();
    this.onTimelineRangeChange(timelineWindow.start, timelineWindow.end);

    app.views.medications.timeline.TherapyTimelineUtils.overrideTimelineOnMouseWheel(this.timeline);

    this.timeline.on('click', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      if (event.item)
      {
        if (self._preventDoubleClickOnTask !== true)
        {
          self._preventDoubleClickOnTask = true;
          // has to be triggered in a timeout because otherwise the Vis.js library has some kind of
          // press event detection, which triggers on Windows 7 + jqBrowser for each click
          setTimeout(function()
          {
            self._handleLeftClickOnElement(self.getItemSet().get(event.item));
          }, 0);
        }
      }
      else if (event.event.target && event.event.center && tm.jquery.ClientUserAgent.isTablet())
      {
        /* delegating click event - tablet fix */
        var evt = event.event;
        $(evt.target).trigger({
          type: "click", target: evt.target, originalEvent: evt.srcEvent, pageX: evt.center.x, pageY: evt.center.y
        });
      }
    });
    this.timeline.on('contextmenu', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      if (event.item)
      {
        self._handleRightClickOnElement(self.getItemSet().get(event.item), event);
      }
    });
    this.timeline.on('press', function(event)
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus(view);
      tm.jquery.ComponentUtils.hideAllTooltips(view);

      var props = self.timeline.getEventProperties(event);

      if (props.item)
      {
        self._ghostClick = true;
        self._handleRightClickOnElement(self.getItemSet().get(props.item), event);

        clearTimeout(self._ghostClickTimer);
        self._ghostClickTimer = setTimeout(function()
        {
          self._ghostClick = false;
        }, 500);
      }
    });
    this.timeline.on('rangechange', function(event)
    {
      if (event.byUser)
      {
        self.onTimelineRangeChange(event.start, event.end);
      }
    });
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, event)
    {
      if (self._ghostClick)
      {
        event.stopPropagation();
      }
    });
    if (view.getOptimizeForPerformance())
    {
      app.views.medications.timeline.TherapyTimelineUtils.overrideTimelinePanMove(this.timeline);
    }
  },

  onTimelineRangeChange: function(start, end)
  {
    this._addOrRemoveClusterFlags(start, end);
    this.timelineRangeChangedFunction(this, start, end);
  },

  _addOrRemoveClusterFlags: function(start, end)
  {
    var timelineRangeInHours = (end - start) / (60 * 60 * 1000);
    var timelineCls = this.getCls();
    timelineCls = timelineCls.replace(" hide-overlap-zoom-elements", "");

    if (timelineRangeInHours > 8)
    {
      this.setCls(timelineCls);
    }
    if (timelineRangeInHours <= 8)
    {
      this.setCls(timelineCls + " hide-overlap-zoom-elements");
    }
  },

  _buildTimeline: function(readOnly)
  {
    var timelineElements = [];
    var groups = this._buildGroups(this.therapyTimelineRows, readOnly);

    for (var i = 0; i < this.therapyTimelineRows.length; i++)  // [TherapyRowDto.java]
    {
      var therapyTimeline = this.therapyTimelineRows[i];
      var therapy = therapyTimeline.getTherapy();    // [TherapyDto.java]

      var elementCreator = new app.views.medications.common.timeline.TimelineContentBuilder()
          .setView(this.getView())
          .setGroupId(therapy.getTherapyId())
          .setTherapy(therapy)
          .setAdministrations(therapyTimeline.administrations)
          .setIntervalStart(this.intervalStart)
          .setIntervalEnd(this.intervalEnd)
          .setAdministrationTaskContentFactory(
              new app.views.medications.timeline.TimelineAdministrationTaskContentFactory()
                  .setView(this.getView())
                  .setTherapyRow(therapyTimeline))
          .setInfusionRateAtIntervalStart(therapyTimeline.infusionRateAtIntervalStart)
          .setCurrentInfusionRate(therapyTimeline.currentInfusionRate)
          .setRateUnit(therapyTimeline.rateUnit)
          .setCurrentStartingDevice(therapyTimeline.currentStartingDevice)
          .setShowAdditionalAdministrationButton(therapyTimeline.isTherapyActive());

      // suppose to be 5-6% faster than concat
      Array.prototype.push.apply(timelineElements, elementCreator.build());
    }

    if (this.timeline)
    {
      var updatedIds = this.getGroupSet().update(groups); // update so it doesn't collapse the height!
      this.getItemSet().clear();
      this.getItemSet().add(timelineElements);

      var obsoleteGroupIdChecker = {};
      updatedIds.forEach(function(id)
      {
        obsoleteGroupIdChecker[id] = true;
      }, this);

      this.getGroupSet().remove(this.getGroupSet().getIds({
        filter: function(groupItem)
        {
          return obsoleteGroupIdChecker[groupItem.id] !== true;
        }
      }));
      if (!tm.jquery.Utils.isEmpty(this.timeline.range))
      {
        this._addOrRemoveClusterFlags(new Date(this.timeline.range.start), new Date(this.timeline.range.end));
      }

      this._insertTestCoordinator(); // signal the end of data drawing
    }
    else
    {
      this.removeAll(true);
      this._buildGui(timelineElements, groups);
      this.repaint();
    }
  },

  /**
   * Inserts the test coordinator into the timeline, for test coordination, but does so with a yield due to the fact
   * that the Vis.JS library uses a timer to redraw the timeline once it detects the item change.
   * @private
   */
  _insertTestCoordinator: function()
  {
    var self = this;
    setTimeout(function yieldToRedrawTimer(){
      self._testRenderCoordinator.insertCoordinator();
    }, 0);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {boolean}
   * @private
   */
  _isAdministrationTaskDeferred: function(therapy, administration)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration.getAdministrationResult() === enums.administrationResultEnum.DEFER)
    {
      return this._existsAdministrationAfterDefer(therapy, administration);
    }
    return false;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {boolean}
   * @private
   */
  _existsAdministrationAfterDefer: function(therapy, administration)
  {
    var administrationTimestamp = administration.getAdministrationTimestamp();
    var administrations = this._getTherapyAdministrationsByTherapy(therapy);

    var now = CurrentTime.get();
    var administrationTimestamps = administrations.map(function extractTimestamp(currentAdministration)
    {
      return currentAdministration.getAdministrationTimestamp();
    });

    var exists = true;
    administrationTimestamps.forEach(function(timestamp)
    {
      if (administrationTimestamp.getTime() !== timestamp.getTime() // skip self
          && administrationTimestamp < timestamp && timestamp < now)
      {
        exists = false;
      }
    });
    return exists;
  },

  _createPopupMenu: function(selectedItem)
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var popupMenu = view.getAppFactory().createPopupMenu();
    var therapy = selectedItem.therapy;
    var administration = selectedItem.administration;
    var isAdministrationConfirmed = administration.isAdministrationConfirmed();
    var byDoctorsOrders = therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS;
    var doctorConfirmation = administration.getDoctorConfirmation();

    if (!isAdministrationConfirmed)
    {
      if (view.getTherapyAuthority().isRescheduleAdministrationsAllowed() && doctorConfirmation !== false &&
          administration.getAdministrationType() !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        var moveMenuItem = new tm.jquery.MenuItem({
          text: view.getDictionary("move"),
          iconCls: 'icon-add-to-24',
          handler: function()
          {
            self._createRescheduleTasksContainer(therapy, administration);
          }
        });
        popupMenu.addMenuItem(moveMenuItem);
      }
      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
      {
        if (therapy.isTitrationDoseType() && administration.getPlannedDose())
        {
          popupMenu.addMenuItem(new tm.jquery.MenuItem({
            text: view.getDictionary("edit"),
            iconCls: 'icon-edit',
            handler: function()
            {
              self._showTitrationBasedAdministrationDialog(therapy, administration, administration.getAdministrationType());
            }
          }));
        }

        var isDescriptiveDose = therapy.getDoseForm() &&
            therapy.getDoseForm().medicationOrderFormType === enums.medicationOrderFormType.DESCRIPTIVE;
        if (!therapy.isTitrationDoseType() && !isDescriptiveDose && doctorConfirmation !== false)
        {
          var commentMenuItem = new tm.jquery.MenuItem({
            text: view.getDictionary("doctors.comment"),
            iconCls: 'icon-doctors-comment',
            handler: function()
            {
              self._createDoctorsCommentContainer(therapy, administration);
            }
          });
          popupMenu.addMenuItem(commentMenuItem);
        }
      }
    }
    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      if (administration.getAdministrationType() !== enums.administrationTypeEnum.STOP &&
          byDoctorsOrders &&
          doctorConfirmation !== null &&
          !(isAdministrationConfirmed && doctorConfirmation === true))
      {
        var confirmAdministrationItem = new tm.jquery.CheckBoxMenuItem({
          text: view.getDictionary('confirm.administration'),
          checked: doctorConfirmation,
          handler: function()
          {
            self._setDoctorConfirmationResult(administration, true);
          }
        });
        popupMenu.addMenuItem(confirmAdministrationItem);
        var cancelAdministrationItem = new tm.jquery.CheckBoxMenuItem({
          text: view.getDictionary('cancel.administration'),
          checked: !doctorConfirmation,
          handler: function()
          {
            self._setDoctorConfirmationResult(administration, false);
          }
        });
        popupMenu.addMenuItem(cancelAdministrationItem);
      }
    }

    if (view.getTherapyAuthority().isManageAdministrationsAllowed() && doctorConfirmation !== false)
    {
      if (administration.isAdministrationCancelled())
      {
        popupMenu.addMenuItem(new tm.jquery.MenuItem({
          text: view.getDictionary("reissue"),
          iconCls: 'icon-confirm',
          handler: function()
          {
            view.getRestApi()
                .uncancelAdministration(administration)
                .then(
                    function()
                    {
                      self.reloadTimelinesFunction();
                    }
                );
          }
        }));
      }
      else
      {
        if (isAdministrationConfirmed && administration.isAdministrationPlanned())
        {
          popupMenu.addMenuItem(new tm.jquery.MenuItem({
            text: view.getDictionary("edit"),
            iconCls: 'icon-edit',
            handler: function()
            {
              self._createEditAdministrationContainer(therapy, administration);
            }
          }));
        }
        if ((isAdministrationConfirmed || (therapy && !therapy.isContinuousInfusion())) &&
            administration.getAdministrationType() !== enums.administrationTypeEnum.STOP)
        {
          popupMenu.addMenuItem(new tm.jquery.MenuItem({
            text: administration.isAdministrationPlanned() ? view.getDictionary("cancel") : view.getDictionary("delete"),
            iconCls: 'icon-delete',
            handler: function()
            {
              self._openDeleteAdministrationDialog(selectedItem);
            }
          }));
        }
      }
    }

    return popupMenu;
  },

  _buildGroupHeaderTherapyContainer: function(rowDto, readOnly)   // [TherapyRowForContInfusionDto.java / TherapyRowDto.java]
  {
    var self = this;
    var view = this.getView();
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;

    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      view: view,
      data: rowDto,
      width: 407,
      scrollableElement: this.getScrollableElement(),
      displayProvider: this.getGroupHeaderDisplayProvider()
    });
    therapyContainer.addTestAttribute(rowDto.therapy.getTherapyId());

    var toolBar = new app.views.medications.timeline.TimelineTherapyContainerToolbar({
      therapyContainer: therapyContainer,
      readOnly: readOnly
    });
    toolBar.setShowPharmacistsReviewEventCallback(function(therapyContainer)
    {
      view.onShowRelatedPharmacistReviews(therapyContainer.getData(), function()
      {
        self.reloadTimelinesFunction();
      });
    });
    toolBar.setConfirmTherapyEventCallback(function(therapyContainer)
    {
      toolBar.setEnabled(false, true);
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var reissue = data.therapyStatus === app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED;
      var actionGuid = tm.jquery.Utils.createGUID();
      self._lastActionGuid = actionGuid;

      var action = reissue ? self._therapyActions.reissueTherapy : self._therapyActions.reviewTherapy;
      action.call(self, therapy)
          .then(
              function onTherapyReissueSuccess()
              {
                self.reloadTimelinesFunction(true, function()
                {
                  return self._lastActionGuid === actionGuid;
                });
              },
              function onTherapyReissueFailure()
              {
                toolBar.setEnabled(true, true);
              });
    });
    toolBar.setNurseResupplyRequestEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      self._sendNurseResupplyRequest(view.getPatientId(), therapy);
    });
    toolBar.setEditTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.showEditTherapyDialog(therapy, false, data.modified);
    });
    toolBar.setAbortTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.getTherapy();
      if (self._canTherapyBeAborted(therapy))
      {
        self._onAbortTherapy(therapy);
      }
      else
      {
        var message = view.getDictionary('therapy.can.not.stop.if.linked');
        view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      }
    });

    toolBar.setSuspendTherapyEventCallback(function(therapyContainer)
    {
      self._onSuspendTherapy(therapyContainer.getData().getTherapy());
    });
    toolBar.setCopyTherapyEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      view.showEditTherapyDialog(therapy, true, false);
    });
    toolBar.setShowMedicationInfoCallback(function(therapyContainer)
    {
      var therapy = therapyContainer.getData().therapy;
      self._showMedicationDetailsContainer(therapy);
    });
    toolBar.setAdministerScheduledTaskEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(data);
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.START;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, true, false);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            true,
            view.getDictionary("administration.schedule.additional"),
            therapyDoseTypeEnum,
            administrationType);
      }
    });
    toolBar.setAdministerUnscheduledTaskEventCallback(function(therapyContainer)
    {
      var timelineRow = therapyContainer.getData();
      var therapy = timelineRow.getTherapy();
      self._openRecordAdditionalAdministrationDialog(timelineRow, therapy);
    });
    toolBar.setAdjustRateEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;

      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            therapy.isOrderTypeOxygen() ? view.getDictionary("adjust.oxygen.rate") :
                view.getDictionary("adjust.infusion.rate"),
            app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
            administrationType,
            false,
            false
        );
      }
    });
    toolBar.setStopFlowCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var isInfusionActive = therapyContainer.getData().currentInfusionRate !== 0;
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, false, false, isInfusionActive);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            isInfusionActive ? view.getDictionary('pause.flow.rate') : view.getDictionary('resume.flow.rate'),
            app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
            administrationType,
            false,
            true
        );
      }
    });
    toolBar.setInfusionSetChangeEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      self._showInfusionSetChangeAdministrationContainer(
          therapy,
          data.administrations,
          null
      );
    });
    toolBar.setTasksChangedEventCallback(function()
    {
      self.reloadTimelinesFunction();
    });
    toolBar.setEditSelfAdministeringCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;

      self._onEditSelfAdministering(therapy);
    });
    toolBar.setPerfusionSyringeRequestEventCallback(function(menuHotSpot, therapyContainer)
    {
      var data = therapyContainer.getData();
      self._showOrderPerfusionSyringeToolTip(data.therapy, menuHotSpot);
    });
    toolBar.setChangeOxygenStartingDeviceCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      self._showOxygenStartingDeviceDialog(data.therapy, data.administrations, null);
    });

    toolBar.setAddBolusEventCallback(function(therapyContainer)
    {
      var data = therapyContainer.getData();
      var therapy = data.therapy;
      var therapyDoseTypeEnum = self._getTherapyDoseTypeEnum(data, true);
      var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            data.administrations,
            null,
            false,
            view.getDictionary('add.bolus.administration'),
            therapyDoseTypeEnum,
            administrationType,
            false,
            false);
      }
    });

    therapyContainer.setToolbar(toolBar);

    return therapyContainer;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _onAbortTherapy: function(therapy)
  {
    var self = this;
    this._therapyActions
        .abortTherapy(therapy)
        .then(
            function onAbortTherapyActionSuccess()
            {
              self.reloadTimelinesFunction();
              self.getView().refreshPatientsCumulativeAntipsychoticPercentage();
            });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _onSuspendTherapy: function(therapy)
  {
    var self = this;
    var view = this.getView();
    this._therapyActions
        .suspendTherapy(therapy)
        .then(
            function onSuspendTherapyActionSuccess()
            {
              view.hideLoaderMask();
              self.reloadTimelinesFunction();
            },
            function onSuspendTherapyActionFailure()
            {
              view.hideLoaderMask();
            });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _showMedicationDetailsContainer: function(therapy)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    view.getRestApi().loadMedicationDataForMultipleIds(therapy.getAllIngredientIds()).then(function(medicationData)
    {
      var medicationDetailsContainer = new app.views.medications.common.MedicationDetailsContainer({
        view: view,
        medicationData: medicationData
      });

      var medicationDetailsContainerDialog = appFactory.createDefaultDialog(
          view.getDictionary("drug.information"),
          null,
          medicationDetailsContainer,
          null,
          450,
          medicationData.length * 170 + 35
      );
      medicationDetailsContainerDialog.setHideOnDocumentClick(true);
      medicationDetailsContainerDialog.show();

    });
  },

  _onEditSelfAdministering: function(therapy)
  {
    var self = this;
    var view = this.getView();
    var isTherapySelfAdmin = !tm.jquery.Utils.isEmpty(therapy.selfAdministeringActionEnum);

    var dialog = view.getAppFactory().createDataEntryDialog(
        isTherapySelfAdmin ? view.getDictionary('edit.self.administration') : view.getDictionary('self.administration'),
        null,
        new app.views.medications.timeline.administration.SelfAdministrationDataEntryContainer({
          view: view,
          therapy: therapy,
          patientId: self.patientId
        }),
        function(resultData)
        {
          if (resultData) // self administering action enum
          {
            self.reloadTimelinesFunction();
          }
        },
        "auto", 130
    );
    dialog.header.setCls("therapy-admin-header");
    dialog.getFooter().setCls("therapy-admin-footer");
    dialog.getFooter().rightContainer.layout.gap = 0;
    dialog.show();
  },

  /**
   * @param {String} patientId
   * @param {app.views.medications.common.dto.Therapy} therapyDto
   * @private
   */
  _sendNurseResupplyRequest: function(patientId, therapyDto)
  {
    var view = this.getView();
    view.showLoaderMask();
    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SEND_THERAPY_RESUPPLY_REQUEST;

    var self = this;
    var params = {
      patientId: patientId,
      therapy: JSON.stringify(therapyDto)
    };

    view.sendPostRequest(
        url,
        params,
        function onNurseResupplyRequestSuccess ()
        {
          view.hideLoaderMask();
          self.reloadTimelinesFunction();
        },
        function onNurseResupplyRequestFailure ()
        {
          view.hideLoaderMask();
        },
        app.views.common.AppNotifierDisplayType.HTML);
  },

  /**
   *
   * @param {app.views.medications.common.dto.Therapy} therapyDto
   * @param {tm.jquery.Component} menuHotSpot
   * @private
   */
  _showOrderPerfusionSyringeToolTip: function(therapyDto, menuHotSpot)
  {
    var view = this.getView();
    var self = this;
    var appFactory = view.getAppFactory();

    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_FINISHED_PERFUSION_SYRINGE_REQUESTS_EXIST;
    var params = {
      patientId: view.getPatientId(),
      originalTherapyId: therapyDto.getTherapyId(),
      hours: 24
    };

    view.showLoaderMask();

    view.loadViewData(url, params, null, function(result)
    {
      view.hideLoaderMask();

      var warningText = result === true ? view.getDictionary("finished.perfusion.syringe.requests.24h.warning.text") : null;

      var entryContainer = new app.views.medications.common.PerfusionSyringeDataEntryContainer({
        view: view,
        warningText: warningText
      });

      var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
          view.getDictionary("order.preparation"),
          entryContainer,
          function(resultData)
          {
            if (resultData && (resultData).success)
            {
              view.showLoaderMask();
              var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_ORDER_THERAPY_PERFUSION_SYRINGE;

              var params = {
                patientId: view.getPatientId(),
                compositionUid: therapyDto.compositionUid,
                ehrOrderName: therapyDto.ehrOrderName,
                numberOfSyringes: resultData.value.count,
                urgent: resultData.value.urgent,
                dueTime: JSON.stringify(resultData.value.orderDate),
                printSystemLabel: resultData.value.printSystemLabel
              };

              view.sendPostRequest(url, params,
                  function()
                  {
                    view.hideLoaderMask();
                    self.reloadTimelinesFunction();
                  },
                  function()
                  {
                    view.hideLoaderMask();
                  },
                  app.views.common.AppNotifierDisplayType.HTML);
            }
          }
      );

      entryContainer.onKey(
          new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
          function()
          {
            popoverTooltip.cancel();
          }
      );

      popoverTooltip.setTrigger('manual');
      popoverTooltip.setWidth(entryContainer.getDefaultWidth());
      popoverTooltip.setHeight(tm.jquery.Utils.isEmpty(warningText) ?
          entryContainer.getDefaultHeight() : entryContainer.getDefaultHeight() + 40);
      menuHotSpot.setTooltip(popoverTooltip);

      setTimeout(function()
      {
        popoverTooltip.show();
      }, 0);
    });
  },

  _canTherapyBeAborted: function(therapy)
  {
    var enums = app.views.medications.TherapyEnums;
    if (therapy.getLinkName())
    {
      var nextTherapyLink = app.views.medications.MedicationUtils.getNextLinkName(therapy.getLinkName());
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

  _getTherapyDayByLinkName: function(linkName)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      var therapyDay = this.therapyTimelineRows[i];
      if (therapyDay && therapyDay.therapy && therapyDay.therapy.getLinkName() && therapyDay.therapy.getLinkName() === linkName)
      {
        return therapyDay;
      }
    }
    return null;
  },

  _getTherapyDoseTypeEnum: function(therapyTimeline, bolus)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapy = therapyTimeline.therapy;

    var doseElement = !tm.jquery.Utils.isEmpty(therapy) ? therapy.getDoseElement() : null;
    var isDescriptiveDose = doseElement && doseElement.doseDescription && doseElement.quantity;

    if (bolus === true)
    {
      if (therapy.getIngredientsList() && therapy.getIngredientsList().length > 1)
      {
        return enums.therapyDoseTypeEnum.VOLUME_SUM;
      }
      else
      {
        return enums.therapyDoseTypeEnum.QUANTITY;
      }
    }

    if (!tm.jquery.Utils.isEmpty(isDescriptiveDose) && isDescriptiveDose)
    {
      return null;
    }

    if (!tm.jquery.Utils.isEmpty(therapy) && !tm.jquery.Utils.isEmpty(therapy.getDoseType()))
    {
      return therapy.getDoseType();
    }

    if (therapy.isOrderTypeComplex())
    {
      var firstAdministrationWithDose = null;
      var administrations = therapyTimeline.administrations;

      for (var i = 0; i < administrations.length; i++)
      {
        if (administrations[i].getPlannedDose() || administrations[i].getAdministeredDose())
        {
          firstAdministrationWithDose = administrations[i];
          break;
        }
      }
      if (!firstAdministrationWithDose)
      {
        return enums.therapyDoseTypeEnum.QUANTITY;
      }
      else
      {
        return firstAdministrationWithDose.getDoseType();
      }
    }
    else
    {
      return enums.therapyDoseTypeEnum.QUANTITY;
    }
  },

  _handleRightClickOnElement: function(selectedItem, elementEvent)
  {
    if (selectedItem && selectedItem.administration)
    {
      var $taskElement = $("#" +
          app.views.medications.timeline.TimelineAdministrationTaskContentFactory.createAdministrationTaskElementId(
              selectedItem.administration));
      var offset = $taskElement.offset();
      var popupMenu = this._createPopupMenu(selectedItem);

      if (popupMenu.hasMenuItems())
      {
        popupMenu.show(function ()
        {
          var position = tm.jquery.ComponentUtils.calculatePopupMenuPosition(popupMenu, elementEvent);
          position.x = offset.left + $taskElement.width() + 5;
          return position;
        });
      }
    }
  },

  _hasAdditionalWarnings: function(additionalWarnings)
  {
      return additionalWarnings.length > 0;
  },

  _handleLeftClickOnElement: function(selectedItem)
  {
    var view = this.getView();
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var administration = selectedItem.administration;
    var therapy = selectedItem.therapy;

    if (selectedItem.changeRangeOnClick)
    {
      var selectedItemHours = selectedItem.start.getHours();
      var timelineStart = new Date(selectedItem.start);
      timelineStart.setHours(selectedItemHours - 1);
      var timelineEnd = new Date(selectedItem.start);
      timelineEnd.setHours(selectedItemHours + 3);
      this.onTimelineRangeChange(timelineStart, timelineEnd);
    }
    else if (administration)
    {
      var selectedTherapyId = therapy.getTherapyId();
      var selectedTimelineRow = self._getTimelineRowByTherapyId(selectedTherapyId);

      if (self._hasAdditionalWarnings(selectedTimelineRow.additionalWarnings))
      {
        var appFactory = view.getAppFactory();
        var message = view.getDictionary('additional.warning.doctor.review');
        var confirmSystemDialog = appFactory.createWarningSystemDialog(message, 391, 141);
        confirmSystemDialog.show();
      }
      else if (!administration.isAdministrationConfirmed())
      {
        var byDoctorsOrders = therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS;

        if (byDoctorsOrders && !tm.jquery.Utils.isEmpty(administration.getTaskId()) &&
            (administration.getDoctorConfirmation() === null || administration.getDoctorConfirmation() === false))
        {
          this._showConfirmationContainer(therapy, administration);
        }
        else if (therapy.isTitrationDoseType() && !administration.getPlannedDose() &&
            (administration.getAdministrationType() === enums.administrationTypeEnum.START ||
            administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION))
        {
          if (view.getTherapyAuthority().isDefineTitrationDoseAllowed())
          {
            this._showTitrationBasedAdministrationDialog(therapy, administration, administration.getAdministrationType());
          }
          else
          {
            view.getAppNotifier().warning(
                view.getDictionary("administration.no.dose.defined"),
                app.views.common.AppNotifierDisplayType.HTML,
                330,
                150);
          }
        }
        else if (view.getTherapyAuthority().isManageAdministrationsAllowed())
        {
          if (administration.getAdministrationType() === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
          {
            this._showInfusionSetChangeAdministrationContainer(
                therapy,
                this._getTherapyAdministrationsByTherapy(therapy),
                administration)
          }
          else if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) &&
              (!administration || administration.getAdministrationType() === enums.administrationTypeEnum.START))
          {
            this._showTherapyAdministrationContainer(
                therapy,
                this._getTherapyAdministrationsByTherapy(therapy),
                administration,
                false,
                view.getDictionary('administration'),
                app.views.medications.TherapyEnums.therapyDoseTypeEnum.RATE,
                enums.administrationTypeEnum.START);
          }
          else
          {
            this._showTherapyAdministrationContainer(
                therapy,
                this._getTherapyAdministrationsByTherapy(therapy),
                administration,
                false,
                view.getDictionary('administration'),
                null,
                administration ? administration.getAdministrationType() : null
            );
          }
        }
      }
      else if (!tm.jquery.Utils.isEmpty(administration.getAdministrationResult()) && selectedItem.taskContainer)
      {
        this._ensureTherapyVersionMatchesAdministration(therapy, administration)
            .then(function(syncedTherapy)
            {
              self._showAdministrationDetailsContentPopup(selectedItem.taskContainer, syncedTherapy, administration);
            }
        );
      }
    }
    else if (therapy && !administration && !selectedItem.changeRangeOnClick)
    {
      var timelineRow = this._getTimelineRowByTherapyId(therapy.getTherapyId());
      this._openRecordAdditionalAdministrationDialog(timelineRow, therapy);
    }
    setTimeout(function()
    {
      self._preventDoubleClickOnTask = false;
    }, 250);
  },

  /**
   *
   * @param {app.views.medications.timeline.TherapyRow} timelineRow
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _openRecordAdditionalAdministrationDialog: function(timelineRow, therapy)
  {
    var self = this;
    var view = this.getView();
    var therapyDoseTypeEnum = this._getTherapyDoseTypeEnum(timelineRow);
    var administrationType = app.views.medications.TherapyEnums.administrationTypeEnum.START;

    if (timelineRow.getTherapyStatus() === app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED)
    {
      app.views.medications.MedicationUtils.openConfirmationWithWarningDialog(
          view,
          view.getDictionary('confirm.administration.suspended.therapy'))
          .then(
              function(confirm)
              {
                if (confirm)
                {
                  onConfirm();
                }
              }
          );
    }
    else
    {
      onConfirm();
    }

    function onConfirm()
    {
      if (therapy.isTitrationDoseType())
      {
        self._showTitrationBasedAdministrationDialog(therapy, null, administrationType, false, true);
      }
      else
      {
        self._showTherapyAdministrationContainer(
            therapy,
            timelineRow.administrations,
            null,
            false,
            view.getDictionary(
                therapy.getWhenNeeded() ?
                    "administration.record.PRN" :
                    "administration.record.additional"),
            therapyDoseTypeEnum,
            administrationType);
      }
    }
  },

  /**
   * @param {Object} selectedItem
   * @private
   */
  _openDeleteAdministrationDialog: function(selectedItem)
  {
    if (!selectedItem || !selectedItem.administration)
    {
      return;
    }

    var self = this;
    var view = this.getView();

    var medicationStartCriterionEnum = app.views.medications.TherapyEnums.medicationStartCriterionEnum;
    var deleteAdministrationAllowed =
        selectedItem.therapy.getStartCriterion() !== medicationStartCriterionEnum.BY_DOCTOR_ORDERS ||
        !selectedItem.administration.isAdministrationPlanned();

    if (deleteAdministrationAllowed)
    {
      app.views.medications.timeline.DeleteAdministrationContainer.asDialog(
          view,
          selectedItem.administration,
          function(resultData)
          {
            if (resultData != null && resultData.isSuccess())
            {

              self._deleteOrCancelAdministrationTask(
                  resultData.getValue().deleteComment,
                  selectedItem.administration,
                  selectedItem.therapy);
            }
          })
          .show();
    }
    else
    {
      view.getAppNotifier().warning(
          view.getDictionary('by.doctors.orders.not.reviewed.delete'),
          app.views.common.AppNotifierDisplayType.HTML,
          350,
          170);
    }
  },

  /**
   * @param {String} comment
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _deleteOrCancelAdministrationTask: function(comment, administration, therapy)
  {
    var view = this.getView();

    if (administration.isAdministrationConfirmed())
    {
      view.getRestApi()
          .deleteAdministration(comment, administration, therapy)
          .then(this.reloadTimelinesFunction);
    }
    else
    {
      view.getRestApi()
          .cancelAdministration(comment, administration)
          .then(this.reloadTimelinesFunction);
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @private
   */
  _createEditAdministrationContainer: function(therapy, administration)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    if (administration.getAdministrationType() === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      this._showInfusionSetChangeAdministrationContainer(
          therapy,
          this._getTherapyAdministrationsByTherapy(therapy),
          administration,
          true)
    }
    else
    {
      this._showTherapyAdministrationContainer(
          therapy,
          this._getTherapyAdministrationsByTherapy(therapy),
          administration,
          false,
          view.getDictionary('edit'),
          null,
          administration.getAdministrationType(),
          true
      );
    }
  },

  _redrawTimeline: function()
  {
    if (!this.timeline)
    {
      return;
    }

    this.clearTestCoordinator();
    this.timeline.redraw();
    this._insertTestCoordinator();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param administration
   * @private
   */
  _createRescheduleTasksContainer: function(therapy, administration)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();
    var timelineRow = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    var reviewedUntil = timelineRow.reviewedUntil;
    var betweenDosesFrequency = therapy.getDosingFrequency() &&
        therapy.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES;

    var rescheduleContainer = new app.views.medications.timeline.RescheduleTasksContainer({
      view: view,
      startProcessOnEnter: true,
      administration: administration,
      administrations: this._getTherapyAdministrationsByTherapy(therapy),
      therapy: therapy,
      infusionActive: timelineRow.infusionActive,
      therapyReviewedUntil: reviewedUntil ? new Date(reviewedUntil) : null
    });
    var rescheduleContainerDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('move.administration'),
        null,
        rescheduleContainer,
        function onRescheduleTaskDialogResult(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.reloadTimelinesFunction();
          }
        },
        350,
        betweenDosesFrequency ? 300 : 250
    );
    rescheduleContainer.setEnableDialogConfirmationFunction(function(enabled)
    {
      rescheduleContainerDialog.getConfirmButton().setEnabled(enabled);
    });

    rescheduleContainerDialog.show();
  },

  _createDoctorsCommentContainer: function(therapy, administration)
  {
    var self = this;
    var view = this.getView();
    var doctorsCommentContainer = new app.views.medications.timeline.DoctorsCommentDataEntryContainer({
      view: view,
      therapy: therapy,
      administration: administration
    });
    var doctorsCommentDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('doctors.comment'),
        null,
        doctorsCommentContainer,
        function onDoctorsCommentDialogResult(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.reloadTimelinesFunction();
          }
        },
        450,
        300
    );
    doctorsCommentDialog.show();
  },

  _showConfirmationContainer: function(therapy, administration)
  {
    var view = this.getView();
    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: new tm.jquery.HFlexboxLayout(),
      html: therapy.getFormattedTherapyDisplay(),
      cls: 'TherapyDescription container'
    });

    if (administration.plannedTime)
    {
      therapyDescriptionContainer.setHtml(therapyDescriptionContainer.getHtml() +
          app.views.medications.MedicationTimingUtils.getFormattedAdministrationPlannedTime(view, administration));
    }
    var self = this;
    var height = view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() ? 200 : 220;
    if (!tm.jquery.Utils.isEmpty(therapy.getIngredientsList()) && therapy.getIngredientsList().length > 1)
    {
      height += 38;
    }
    if (!tm.jquery.Utils.isEmpty(therapy.getComment()) && therapy.getComment().length > 0)
    {
      height += 28;
    }
    var confirmationContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      scrollable: "both"
    });
    var confirmationContainerDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary('therapy.administration'),
        null,
        confirmationContainer,
        function doNothing()
        {
          // not an AppDataEntry container, so no result data - button actions are defined below
        },
        448,
        height
    );
    var administerButton = new tm.jquery.Button({
      cls: "footer-right-btn",
      text: view.getDictionary('confirm.administration'),
      handler: function()
      {
        self._setDoctorConfirmationResult(administration, true);
        confirmationContainerDialog.hide();
      }
    });
    var doNotAdministerButton = new tm.jquery.Button({
      type: "link",
      cls: "no-border-btn",
      text: view.getDictionary('cancel.administration'),
      handler: function()
      {
        self._setDoctorConfirmationResult(administration, false);
        confirmationContainerDialog.hide();
      }
    });

    var administrationNotPossibleWarning = administration.getDoctorConfirmation() === false ?
        view.getDictionary('therapy.administration.withdrawn') :
        view.getDictionary('therapy.administration.must.be.confirmed');
    var administrationNotPossibleLabel = new tm.jquery.Container({
      cls: "TextData administration-not-possible-label",
      html: administrationNotPossibleWarning
    });
    confirmationContainer.add(therapyDescriptionContainer);
    confirmationContainerDialog.header.setCls("therapy-admin-header");
    confirmationContainerDialog.getFooter().setCls("therapy-admin-footer");

    if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() &&
        administration.getDoctorConfirmation() !== false)
    {
      confirmationContainerDialog.setRightButtons([administerButton]);
      confirmationContainerDialog.setLeftButtons([doNotAdministerButton]);
    }
    else
    {
      confirmationContainer.add(administrationNotPossibleLabel);
      var closeButton = confirmationContainerDialog.getRightButtons()[1];
      closeButton.setText(view.getDictionary('close'));
      closeButton.setCls("footer-right-btn");
      confirmationContainerDialog.setRightButtons([closeButton]);
    }
    confirmationContainerDialog.show();
  },

  /**
   * Saves doctors confirmation result, which determines whether the administration should be administered or not.
   * If the selected result is the same as the existing state, does nothing.
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {Boolean} result
   * @private
   */
  _setDoctorConfirmationResult: function(administration, result)
  {
    if (administration.getDoctorConfirmation() === result)
    {
      return;
    }

    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      patientId: view.getPatientId(),
      administration: JSON.stringify(administration),
      result: result
    };

    var doctorConfirmationUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SET_DOCTOR_CONFIRMATION_RESULT;
    view.loadPostViewData(doctorConfirmationUrl, params, null,
        function()
        {
          self.reloadTimelinesFunction();
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          self.reloadTimelinesFunction();
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @returns {tm.jquery.Deferred}
   * @private
   */
  _ensureTherapyVersionMatchesAdministration: function(therapy, administration)
  {
    var currentTherapyId = therapy.getTherapyId();
    var isOldTherapyAdministration = administration && therapy && currentTherapyId !== administration.getTherapyId();

    if (isOldTherapyAdministration) //therapy was modified, this administration is from old therapy
    {
      return this.getView().getRestApi().loadTherapy(administration.getTherapyId());
    }
    else
    {
      var deferred = tm.jquery.Deferred.create();
      deferred.resolve(therapy);
      return deferred.promise();
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object} administration
   * @param {boolean} createNewTask
   * @param {string} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum|string} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|string} administrationType
   * @param {boolean} [editMode=false]
   * @param {boolean} [stopFlow=false]
   * @param {string|null} [scannedMedicationId=null]
   * @param {string|null} [barcode=null]
   * @private
   */
  _showTherapyAdministrationContainer: function(therapy, administrations, administration, createNewTask, containerTitle,
                                                therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                                scannedMedicationId, barcode)
  {
    var self = this;

    this._ensureTherapyVersionMatchesAdministration(therapy, administration).then(
        function(syncedTherapy)
        {
          self._showTherapyAdministrationContainerImpl(
              syncedTherapy,
              administrations,
              administration,
              createNewTask,
              containerTitle,
              therapyDoseTypeEnum,
              administrationType ? administrationType : (administration ? administration.getAdministrationType() : null),
              editMode,
              stopFlow,
              scannedMedicationId,
              barcode
          );
        }
    );
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object} administration
   * @param {boolean} [editMode=false]
   * @private
   */
  _showInfusionSetChangeAdministrationContainer: function(therapy, administrations, administration, editMode)
  {
    var self = this;
    this._ensureTherapyVersionMatchesAdministration(therapy, administration).then(
        function(syncedTherapy)
        {
          self._showInfusionSetChangeAdministrationContainerImpl(
              syncedTherapy,
              administrations,
              administration,
              editMode
          );
        }
    );
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object} administration
   * @param {boolean} createNewTask
   * @param {string} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum|string} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|string} administrationType
   * @param {boolean} [editMode=false]
   * @param {boolean} [stopFlow=false]
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @private
   */
  _showTherapyAdministrationContainerImpl: function(therapy, administrations, administration, createNewTask, containerTitle,
                                                    therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                                    scannedMedicationId, barcode)
  {
    var self = this;
    var timelineRowData = this._getTimelineRowByAdministration(administration);
    if (!timelineRowData)
    {
      timelineRowData = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    }

    this._administrationDialogBuilder.showAdministrationDialog(
        timelineRowData,
        therapy,
        administrations,
        administration,
        createNewTask,
        containerTitle,
        therapyDoseTypeEnum,
        administrationType,
        editMode,
        stopFlow,
        scannedMedicationId,
        barcode).then(
        function onAdministrationDialogResult(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.reloadTimelinesFunction();
          }
          self.getView().setActionCallbackListener(null);
        });
  },

  _showInfusionSetChangeAdministrationContainerImpl: function(therapy, administrations, administration, editMode)
  {
    var self = this;
    var timelineRowData = this._getTimelineRowByAdministration(administration);
    if (!timelineRowData)
    {
      timelineRowData = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    }
    this._administrationDialogBuilder.showInfusionSetChangeDialog(
        timelineRowData,
        therapy,
        administrations,
        administration,
        editMode).then(
        function onInfusionSetChangeDialogResult(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.reloadTimelinesFunction();
          }
          self.getView().setActionCallbackListener(null);
        });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|String|undefined} [administrationType=undefined]
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @private
   */
  _showTitrationBasedAdministrationDialog: function(therapy, administration, administrationType, scheduleAdditional,
  applyUnplanned, stopFlow)
  {
    var self = this;
    var timelineRowData = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    var lastPositiveInfusionRate = null;
    var activeContinuousInfusion = false;

    if ((therapy.isContinuousInfusion() && timelineRowData))
    {
      lastPositiveInfusionRate = timelineRowData.lastPositiveInfusionRate;
      activeContinuousInfusion = timelineRowData.infusionActive;
    }

    var dialogBuilder = new app.views.medications.timeline.titration.TitrationDialogBuilder({
      view: this.getView(),
      therapy: therapy,
      administration: administration
    });

    dialogBuilder
        .setAdministrationMode(administrationType,
            scheduleAdditional,
            applyUnplanned,
            stopFlow,
            timelineRowData.reviewedUntil ? new Date(timelineRowData.reviewedUntil) : null)
        .setInfusionState(lastPositiveInfusionRate, activeContinuousInfusion)
        .setAllAdministrationTasks(timelineRowData.administrations)
        .showDialog()
        .then(function onDialogCloseHandler(resultData)
        {
          if (resultData && resultData.success)
          {
            self.reloadTimelinesFunction();
          }
        });
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} administrations
   * @param {Object|null} administration
   * @private
   */
  _showOxygenStartingDeviceDialog: function(therapy, administrations, administration)
  {
    var view = this.getView();
    var self = this;
    var timelineRow = this._getTimelineRowByTherapyId(therapy.getTherapyId());

    var startingDevice = null;
    if (administration && administration.getStartingDevice())
    {
      startingDevice = new app.views.medications.common.dto.OxygenStartingDevice(administration.getStartingDevice())
    }
    else if (timelineRow && timelineRow.currentStartingDevice)
    {
      startingDevice = timelineRow.currentStartingDevice
    }

    var oxygenStartingDeviceEntryContainer =
        new app.views.medications.timeline.administration.OxygenStartingDeviceDataEntryContainer({
          view: view,
          therapy: therapy,
          currentStartingDevice: startingDevice,
          currentFlowRate: timelineRow.currentInfusionRate,
          administration: administration
        });

    var oxygenStartingDeviceDialog = view.getAppFactory().createDataEntryDialog(
        view.getDictionary("change.device"),
        null,
        oxygenStartingDeviceEntryContainer,
        function onOxygenStartingDeviceDialogResult(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            self.reloadTimelinesFunction();
          }
        },
        475,
        450
    );

    oxygenStartingDeviceDialog.show();
  },

  /**
   * @param {String} therapyId
   * @returns {Object|*}
   * @private
   */
  _getTimelineRowByTherapyId: function(therapyId)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      if (this.therapyTimelineRows[i].therapy.getTherapyId() === therapyId)
      {
        return this.therapyTimelineRows[i];
      }
    }
    return null;
  },

  /**
   * @param {Object} administration
   * @returns {Object|null}
   * @private
   */
  _getTimelineRowByAdministration: function(administration)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      if (tm.jquery.Utils.isArray(this.therapyTimelineRows[i].administrations) &&
          this.therapyTimelineRows[i].administrations.indexOf(administration) > -1)
      {
        return this.therapyTimelineRows[i];
      }
    }
    return null;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {Array<Object>}
   * @private
   */
  _getTherapyAdministrationsByTherapy: function(therapy)
  {
    var therapyTimeline = this._getTimelineRowByTherapyId(therapy.getTherapyId());
    return therapyTimeline ? therapyTimeline.administrations : [];
  },

  _showAdministrationDetailsContentPopup: function(taskStateContainer, therapy, administration)
  {
    var self = this;
    if (therapy.hasNonUniversalIngredient())
    {
      this.getView().getRestApi().loadMedicationDataForMultipleIds(
          therapy.getAllIngredientIds())
          .then(
              function onMedicationDataLoaded(medicationData)
              {
                self._buildAdministrationDetailsContentPopupImpl(taskStateContainer, therapy, administration, medicationData);
              });
    }
    else
    {
      self._buildAdministrationDetailsContentPopupImpl(taskStateContainer, therapy, administration);

    }
  },

  /**
   * @param {Object} taskStateContainer
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {app.views.medications.common.dto.MedicationData} [medicationData = undefined]
   * @private
   */
  _buildAdministrationDetailsContentPopupImpl: function(taskStateContainer, therapy, administration, medicationData)
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();

    if (!tm.jquery.ClientUserAgent.isTablet() && taskStateContainer.getTooltip()
        && !(taskStateContainer.getTooltip() instanceof app.views.medications.timeline.TherapyTimelineTooltip))
    {
      // there's a tooltip present, and it's not the hover version, which means we should disable it's onHide since we'll
      // apply a new popover tooltip to the component, which will take care of reattaching the hover tooltip
      taskStateContainer.getTooltip().onHide = undefined;
    }

    var therapyAdministrationDetailsContent = new app.views.medications.timeline.administration.TherapyAdministrationDetailsContentContainer({
      view: view,
      displayProvider: this.displayProvider,
      therapy: therapy,
      administration: administration,
      medicationData: medicationData
    });
    var therapyAdministrationDetailsPopup = appFactory.createDefaultPopoverTooltip(
        view.getDictionary('administration'),
        null,
        therapyAdministrationDetailsContent
    );
    therapyAdministrationDetailsPopup.setPlacement("auto");
    therapyAdministrationDetailsPopup.setDefaultAutoPlacements(["bottom", "top", "center", "rightBottom", "rightTop"]);

    if (this.getScrollableElement())
    {
      therapyAdministrationDetailsPopup.setAppendTo(this.getScrollableElement());
    }

    if (!tm.jquery.ClientUserAgent.isTablet())
    {
      var hoverTooltip = taskStateContainer.getTooltip();
      // reattach the hover tooltip once the popover version closes
      therapyAdministrationDetailsPopup.onHide = function()
      {
        therapyAdministrationDetailsPopup.onHide = undefined; // prevent double execution which otherwise occurs
        if (!(taskStateContainer.getTooltip() instanceof app.views.medications.timeline.TherapyTimelineTooltip))
        {
          taskStateContainer.setTooltip(hoverTooltip);
        }
      };
    }

    therapyAdministrationDetailsPopup.setTrigger("manual");
    taskStateContainer.setTooltip(therapyAdministrationDetailsPopup);

    setTimeout(function yieldToTooltipApplyMethod()
    {
      therapyAdministrationDetailsPopup.show();
    }, 0);
  },

  _buildGroups: function(therapyTimelineRows, readOnly)
  {
    var groups = [];

    for (var idx = 0; idx < therapyTimelineRows.length; idx++)
    {
      var groupContentContainer = this._buildGroupHeaderTherapyContainer(therapyTimelineRows[idx], readOnly);
      var additionalWarnings = therapyTimelineRows[idx].additionalWarnings;

      groupContentContainer.doRender();

      groups.push({
        id: therapyTimelineRows[idx].therapy.getTherapyId(),
        orderIndex: idx,
        content: groupContentContainer.getDom(),
        className: this._hasAdditionalWarnings(additionalWarnings) ? "additional-warning" : ""
      });
    }

    return groups;
  },

  /**
   *
   * @param {app.views.medications.common.dto.BarcodeTaskSearch} barcodeTaskSearch
   * @param {String} barcode
   */
  _medicationIdentifierScanned: function(barcodeTaskSearch, barcode)
  {
    for (var i = 0; i < this.therapyTimelineRows.length; i++)
    {
      var timelineRow = this.therapyTimelineRows[i];
      if (timelineRow.administrations)
      {
        for (var j = 0; j < timelineRow.administrations.length; j++)
        {
          var administration = timelineRow.administrations[j];
          if (administration.getTaskId() === barcodeTaskSearch.getTaskId())
          {
            this._showTherapyAdministrationContainer(timelineRow.therapy, timelineRow.administrations, administration, false,
                this.getView().getDictionary('administration'), null, administration.getAdministrationType(), false, false,
                barcodeTaskSearch.getMedicationId(), barcode);
          }
        }
      }
    }
  },

  /** public methods */
  setTherapyTimelineData: function(therapyTimelineRows, readOnly)
  {
    this.therapyTimelineRows = therapyTimelineRows;
    this._buildTimeline(readOnly);
  },

  setVisibleRange: function(start, end)
  {
    if (this.timeline)
    {
      var range = this.timeline.getWindow();
      if (range.start !== start || range.end !== end)
      {
        this.timeline.setWindow(start, end, {animation: false});
        /* make sure animation is off, otherwise slowdowns occur */
      }
    }
  },

  clear: function()
  {
    this.therapyTimelineRows.removeAll();
    this._buildTimeline();
  },

  clearTestCoordinator: function()
  {
    this._testRenderCoordinator.removeCoordinator();
  },

  redrawTimeline: function()
  {
    this._redrawTimeline();
  },

  getGroupSet: function()
  {
    return this._groupSet;
  },

  getItemSet: function()
  {
    return this._itemSet;
  },

  setMinMaxTimeline: function(min, max)
  {
    this.options.min = min;
    this.options.max = max;

    if (!tm.jquery.Utils.isEmpty(this.timeline))
    {
      this.timeline.setOptions({ min: min, max: max});
    }
    this.intervalStart = min;
    this.intervalEnd = max;
  },

  getGroupHeaderDisplayProvider: function()
  {
    return this.groupHeaderDisplayProvider;
  },

  /**
   * @returns {Element|null}
   */
  getScrollableElement: function()
  {
    return this.scrollableElement;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @Override
   */
  destroy: function ()
  {
    if (this.timeline) {
      this.timeline.destroy();
      this.timeline = null;
    }
    this.callSuper();
  }
});

