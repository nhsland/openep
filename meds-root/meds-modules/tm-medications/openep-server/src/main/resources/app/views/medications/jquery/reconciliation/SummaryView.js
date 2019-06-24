Class.define('app.views.medications.reconciliation.SummaryView', 'app.views.medications.common.overview.AbstractSubViewContainer', {
  statics: {
    EVENT_TYPE_RECONCILIATION_START_TIME_CHANGE: new tm.jquery.event.EventType({
      name: 'summaryViewReconciliationStartTimeChange'
    })
  },
  /** @type string|null  */
  cls: "rec-summary-view",
  /** @type tm.views.medications.TherapyView|app.views.common.AppView  */
  view: null,

  /** @type app.views.medications.reconciliation.SummaryRowContainer */
  _headerRowContainer: null,
  /** @type tm.jquery.Container */
  _noTherapiesField: null,
  /** @type tm.jquery.Container */
  _rowsContainer: null,
  /** @type number|undefined */
  _scrollbarDetectionTimer: undefined,
  /** @type number */
  _scrollbarDetectionTimerInterval: 250,
  /** @type boolean */
  _verticalScrollbarPresent: false,
  /**
   * Used for the summary column's therapy containers.
   * @type app.views.medications.common.therapy.TherapyContainerDisplayProvider
   */
  _summaryColumnTherapyDisplayProvider: null,
  /**
   * Used for both the admission and discharge column therapy containers.
   * @type app.views.medications.common.therapy.TherapyContainerDisplayProvider
   */
  _defaultTherapyDisplayProvider: null,
  /** type Array<app.views.medications.common.dto.DispenseSource> */
  _dispenseSources: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.reconciliation.SummaryView', [
      {eventType: app.views.medications.reconciliation.SummaryView.EVENT_TYPE_RECONCILIATION_START_TIME_CHANGE}
    ]);

    this._summaryColumnTherapyDisplayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.view,
      showAuditTrail: false
    });
    this._defaultTherapyDisplayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.view,
      showChangeHistory: false,
      showChangeReason: false,
      showAuditTrail: false
    });

    this._buildGui();
    this._buildColumnHeaders();
  },

  /**
   * Reloads the data from the server and redraws the sub view. The retrieved data from the server is also used to determine
   * which actions (start or edit, review) are available for the individual list (admission, discharge). In cases when the
   * patient is discharged, editing the medication on admission list is prevented if the reconciliation process was started
   * before the discharge occurred, with the intent to prevent the user from editing the medications on admission list for
   * the previous hospitalization period of returning patients.
   */
  refreshData: function()
  {
    var self = this;
    var view = this.view;

    this._hideAllColumnActions(); // until data refreshes so that we can correctly determine available actions
    view.getRestApi()
        .getReconciliationGroups()
        .then(
            /**
             * @param {app.views.medications.common.dto.ReconciliationSummary} data
             */
            function onSuccess(data)
            {
              self._configureColumnActions(
                  self._headerRowContainer.getAdmissionColumn(),
                  !data.isEmpty() && data.hasMedicationOnAdmission(),
                  data.isMedicationsOnAdmissionReviewed(),
                  data.getAdmissionLastUpdateTime(),
                  view.getPatientData().isDischargedAfter(data.getReconciliationStartedTime()));
              self._configureColumnActions(
                  self._headerRowContainer.getDischargeColumn(),
                  !data.isEmpty() && data.hasMedicationOnDischarge(),
                  data.isMedicationsOnDischargeReviewed(),
                  data.getDischargeLastUpdateTime());
              self._fireReconciliationStartTimeChangeEvent(data.getReconciliationStartedTime());

              if (data.isEmpty())
              {
                self._noTherapiesField.show();
                self._rowsContainer.hide();
                return;
              }

              self._noTherapiesField.hide();
              self._rowsContainer.show();
              self._populateRowsContainer(data.getRows());

              if (self._rowsContainer.isRendered())
              {
                self._rowsContainer.repaint();
              }
            });
  },

  /** Clears any displayed data, effectively removing all content rows.*/
  clearData: function()
  {
    if (this._rowsContainer)
    {
      this._rowsContainer.removeAll();
    }
  },

  /**
   * Starts a new reconciliation process. Display a confirmation dialog before doing, since the process will remove
   * any existing lists.
   */
  startNewReconciliation: function()
  {
    var self = this;

    var confirmationDialog = this.view.getAppFactory().createConfirmSystemDialog(
        this.view.getDictionary('start.new.reconciliation.confirmation'),
        function resultCallback(resultData)
        {
          if (resultData)
          {
            self.view.getRestApi()
                .startNewReconciliation()
                .then(function clearCurrentDataAndOpenAdmissionDialog()
                {
                  // closing the admission dialog reloads the data, so clearing the current state seems sufficient
                  self.clearData();
                  self._openAdmissionMedicationReconciliationDialog();
                });
          }
        },
        350,
        160);
    confirmationDialog.setTitle(this.view.getDictionary('confirm'));
    confirmationDialog.show();
  },

  /**
   * Called during view construction by the main toolbar to allow us to attach the menu button event handlers.
   * @override
   * @param {app.views.medications.common.overview.header.WestToolbarContainer|tm.jquery.AbstractComponent} westToolbar
   */
  attachOverviewHeaderWestToolbarEvents: function(westToolbar)
  {
    westToolbar.on(
        app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_START_NEW_RECONCILIATION,
        this.startNewReconciliation.bind(this));

    this.on(
        app.views.medications.reconciliation.SummaryView.EVENT_TYPE_RECONCILIATION_START_TIME_CHANGE,
        function updateWestToolbarDateOnReconciliationStartTimeChange(component, componentEvent)
        {
          westToolbar.applyLastReconciliationStartDate(componentEvent.eventData);
        });
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));
  },

  _buildColumnHeaders: function()
  {
    var self = this;
    var therapyAuthority = this.view.getTherapyAuthority();

    var headerRow = new app.views.medications.reconciliation.SummaryRowContainer({
      cls: "header-row column-title"
    });

    var admissionColumnTitle = new app.views.medications.reconciliation.SummaryColumnTitleContainer({
      view: this.view,
      title: this.view.getDictionary("medication.on.admission"),
      editable: therapyAuthority.isManageMedicationOnAdmissionAllowed(),
      reviewable: therapyAuthority.isMedicationReconciliationReviewAllowed()
    });
    admissionColumnTitle.on(
        app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_EDIT_RECONCILIATION_LIST,
        this._openAdmissionMedicationReconciliationDialog.bind(this));
    admissionColumnTitle.on(
        app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_REVIEW_RECONCILIATION_LIST,
        this._onReviewAdmissionList.bind(this));

    var dischargeColumnTitle = new app.views.medications.reconciliation.SummaryColumnTitleContainer({
      view: this.view,
      title: this.view.getDictionary("discharge.prescription"),
      editable: therapyAuthority.isManageMedicationOnDischargeAllowed() || therapyAuthority.isPrescribeByTemplatesAllowed(),
      reviewable: therapyAuthority.isMedicationReconciliationReviewAllowed()
    });

    dischargeColumnTitle.on(
        app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_EDIT_RECONCILIATION_LIST,
        this._onEditDischargeList.bind(this));

    dischargeColumnTitle.on(
        app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_REVIEW_RECONCILIATION_LIST,
        this._onReviewDischargeList.bind(this));

    var summaryColumnTitle = this._buildTitleColumnContent(this.view.getDictionary("discharge.summary"), "left");

    headerRow.getAdmissionColumn().add(admissionColumnTitle);
    headerRow.getDischargeColumn().add(dischargeColumnTitle);
    headerRow.getSummaryColumn().add(summaryColumnTitle);

    var contentContainer = new tm.jquery.Container({
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      self._onCheckScrollbarTimerTick(component);
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_DESTROY, function()
    {
      clearTimeout(self._scrollbarDetectionTimer);
    });

    this._noTherapiesField = this.view.getNoTherapiesField();
    this._rowsContainer = contentContainer;
    this._headerRowContainer = headerRow;

    this.add(headerRow);
    this.add(this._noTherapiesField);
    this.add(contentContainer);

    this._noTherapiesField.hide();
  },

  /**
   * Triggered when the user requests to mark the admission medication list as reviewed.
   * @private
   */
  _onReviewAdmissionList: function()
  {
    var self = this;
    this
        .view
        .getRestApi()
        .reviewMedicationOnAdmission()
        .then(
            function markReviewed()
            {
              self
                  ._headerRowContainer
                  .getAdmissionColumn()
                  .getComponents()[0]
                  .hideReviewAction()
                  .markReviewed();
            });
  },

  /**
   * Triggered when the user requests to edit the discharge medication list.
   * @private
   */
  _onEditDischargeList: function()
  {
    var self = this;
    this._ensureDispenseSourcesLoaded()
        .then(
            function checkReferenceWeight()
            {
              self._ensureReferenceWeightExists()
                  .then(self._openDischargeMedicationReconciliationDialog.bind(self));
            }
        );
  },

  /**
   * Triggered when the user requests to mark the discharge medication list as reviewed.
   * @private
   */
  _onReviewDischargeList: function()
  {
    var self = this;
    this.view
        .getRestApi()
        .reviewMedicationOnDischarge()
        .then(
            function markReviewed()
            {
              self._headerRowContainer
                  .getDischargeColumn()
                  .getComponents()[0]
                  .hideReviewAction()
                  .markReviewed();
            });
  },

  _onCheckScrollbarTimerTick: function(component)
  {
    clearTimeout(this._scrollbarDetectionTimer);

    var self = this;
    var scrollbars = app.views.medications.MedicationUtils.isScrollVisible(component);

    if (self._verticalScrollbarPresent !== scrollbars)
    {
      self._fixHeaderPaddingIfScrollbar();
      self._verticalScrollbarPresent = scrollbars;
    }

    this._scrollbarDetectionTimer = setTimeout(function()
    {
      self._onCheckScrollbarTimerTick(component);
    }, this._scrollbarDetectionTimerInterval);
  },

  _fixHeaderPaddingIfScrollbar: function()
  {
    if (!this._rowsContainer || !this._headerRowContainer) return;

    if (app.views.medications.MedicationUtils.isScrollVisible(this._rowsContainer))
    {
      var scrollbarWidth = app.views.medications.MedicationUtils.getScrollbarWidth();
      if (scrollbarWidth)
      {
        this._headerRowContainer.setPadding("0 " + scrollbarWidth + " 0 0");
      }
    }
    else
    {
      this._headerRowContainer.setPadding(0);
    }
  },

  /**
   * Helper method to ensure the reference weight is actually set, when required by the server configuration.
   * @return {tm.jquery.Promise}
   * @private
   */
  _ensureReferenceWeightExists: function()
  {
    if (!this.view.isReferenceWeightInputRequired())
    {
      return tm.jquery.Deferred.create().resolve().promise();
    }

    return this.view.openReferenceWeightDialog();
  },

  /**
   * @return {tm.jquery.Promise} which is resolved once {@link #_dispenseSources} contains an array of
   * {@link app.views.medications.common.dto.DispenseSource}. The information is required by the the discharge medication
   * dialog.
   * @private
   */
  _ensureDispenseSourcesLoaded: function()
  {
    var deferred = tm.jquery.Deferred.create();
    var self = this;

    if (tm.jquery.Utils.isArray(this._dispenseSources))
    {
      deferred.resolve();
    }
    else
    {
      this.view
          .getRestApi()
          .loadDispenseSources()
          .then(function onLoad(data)
          {
            self._dispenseSources = data;
            deferred.resolve();
          });
    }

    return deferred.promise();
  },

  /**
   * Opens the medication on admission dialog. If the user has the required permissions, the dialog acts as a wizard,
   * where the next step provides the ability to transfer any listed medication to inpatient therapies. Otherwise
   * the dialog is simpler and only provides the ability to list the medication on admission.
   * @private
   */
  _openAdmissionMedicationReconciliationDialog: function()
  {
    var self = this;

    this._ensureReferenceWeightExists()
        .then(function showMedicationReconciliationAdmissionDialog()
        {
          var dialogBuilder = new app.views.medications.reconciliation.MedicationOnAdmissionDialogBuilder({
            view: self.view
          });
          var withInpatientPrescribing = self.view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed();

          dialogBuilder
              .setWithInpatientPrescribing(withInpatientPrescribing)
              .setResultCallback(
                  function(resultData)
                  {
                    // refresh every time regardless if we confirmed or canceled when inpatient prescribing is available,
                    // to prevent scenarios where the user would save the admission list and continue to the next step,
                    // only to close or cancel the dialog. In that scenario we still need to show the saved admission list.
                    if (withInpatientPrescribing || (!tm.jquery.Utils.isEmpty(resultData) && resultData.isSuccess()))
                    {
                      self.refreshData();
                    }
                  })
              .create()
              .show();
        });
  },

  _openDischargeMedicationReconciliationDialog: function()
  {
    var self = this;

    var content = new app.views.medications.reconciliation.MedicationOnDischargeEntryContainer({
      view: this.view,
      dispenseSources: this._dispenseSources,
      prescribeByTemplatesOnlyMode: this.view.getTherapyAuthority().isPrescribeByTemplatesAllowed() &&
          !this.view.getTherapyAuthority().isManageMedicationOnDischargeAllowed()
    });

    var dischargeMedsDialog = this.view.getAppFactory().createDataEntryDialog(
        this.view.getDictionary("medication.on.discharge"),
        null,
        content, function(resultData)
        {
          if (!tm.jquery.Utils.isEmpty(resultData) && resultData.isSuccess())
          {
            self.refreshData();
          }
        },
        $(window).width() - 50,
        $(window).height() - 10
    );
    dischargeMedsDialog.setContainmentElement(this.view.getDom());
    dischargeMedsDialog.setFitSize(true);
    dischargeMedsDialog.setHideOnEscape(false);
    dischargeMedsDialog.getFooter().setCls("reconciliation-dialog-buttons");
    content.setDialog(dischargeMedsDialog);

    dischargeMedsDialog.show();
  },

  _buildTitleColumnContent: function(titleText, textAlign)
  {
    var columnContent = new tm.jquery.Container({
      cls: "column-title",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    columnContent.add(new tm.jquery.Component({
      cls: "PortletHeading1" + (tm.jquery.Utils.isEmpty(textAlign) ? "" : (" align-" + textAlign)),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: titleText
    }));
    return columnContent;
  },

  _buildTitleColumnEmptyContent: function(withBorder)
  {
    return new tm.jquery.Container({
      cls: "empty-title" + (withBorder === true ? " right-border" : ""),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
  },

  /**
   * Replaces the existing content of the {@link #_rowsContainer} with the provided row data.
   * @param {Array<app.views.medications.common.dto.ReconciliationRow>} rows
   * @private
   */
  _populateRowsContainer: function(rows)
  {
    var groupMap = new tm.jquery.HashMap();
    var groupEnums = app.views.medications.TherapyEnums.reconciliationRowGroupEnum;

    groupMap.put(groupEnums.NOT_CHANGED, []);
    groupMap.put(groupEnums.CHANGED, []);
    groupMap.put(groupEnums.ONLY_ON_DISCHARGE, []);
    groupMap.put(groupEnums.ONLY_ON_ADMISSION, []);

    rows.forEach(function(row)
    {
      groupMap.get(row.getGroupEnum()).push(row);
    }, this);

    this._rowsContainer.removeAll();

    groupMap.keys().forEach(function(key)
    {
      var rows = groupMap.get(key);
      if (rows.length > 0)
      {
        var groupTitleRow = this._buildGroupTitleRow(key);
        this._rowsContainer.add(groupTitleRow);

        rows.forEach(function(row)
        {
          var summaryRow = new app.views.medications.reconciliation.SummaryRowContainer();

          if (row.isTherapyOnAdmissionPresent())
          {
            summaryRow.getAdmissionColumn().add(
                this._buildSummaryViewTherapyContainer(row.getTherapyOnAdmission(), this._defaultTherapyDisplayProvider));
          }

          if (row.isTherapyOnDischargePresent())
          {
            summaryRow.getDischargeColumn().add(
                this._buildSummaryViewTherapyContainer(row.getTherapyOnDischarge(), this._defaultTherapyDisplayProvider));
          }

          var summaryTherapy = row.isTherapyOnDischargePresent() ?
              row.getTherapyOnDischarge() : row.getTherapyOnAdmission();

          summaryRow.getSummaryColumn().add(
              this._buildSummaryViewTherapyContainer(summaryTherapy, this._summaryColumnTherapyDisplayProvider, row));
          this._rowsContainer.add(summaryRow);
        }, this);
      }
    }, this);
  },

  /**
   * Creates a new {@link app.views.medications.common.therapy.TherapyContainer} used to display the state of a therapy
   * in one of the three possible display cells. Defaults the therapy status to normal for admission and discharge
   * cells so we don't display the therapy in a state of changed or ended.
   * @param {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy} therapy
   * @param {app.views.medications.common.therapy.TherapyContainerDisplayProvider} displayProvider
   * @param {app.views.medications.common.dto.ReconciliationRow} [row=undefined]
   * @return {app.views.medications.common.therapy.TherapyContainer}
   * @private
   */
  _buildSummaryViewTherapyContainer: function(therapy, displayProvider, row)
  {
    if (!therapy)
    {
      throw new Error('Therapy data is required.');
    }

    return new app.views.medications.common.therapy.TherapyContainer({
      view: this.view,
      displayProvider: displayProvider,
      data: new app.views.medications.reconciliation.dto.SummaryRowTherapyData({
        therapy: therapy,
        changeReasonDto: row ? row.getChangeReason() : null,
        changes: row ? row.getChanges() : [],
        therapyStatus: row ? row.getStatusEnum() : app.views.medications.TherapyEnums.therapyStatusEnum.NORMAL
      })
    });
  },

  _buildGroupTitleRow: function(groupEnum)
  {
    var groupEnums = app.views.medications.TherapyEnums.reconciliationRowGroupEnum;
    var titleRow = new app.views.medications.reconciliation.SummaryRowContainer({
      cls: "header-row"
    });

    switch (groupEnum)
    {
      case groupEnums.CHANGED:
        titleRow.getAdmissionColumn().add(this._buildTitleColumnEmptyContent(false));
        titleRow.getDischargeColumn().add(this._buildTitleColumnEmptyContent(true));
        titleRow.getSummaryColumn().add(this._buildTitleColumnContent(this.view.getDictionary("changed.therapies.short"), "left"));
        break;
      case groupEnums.NOT_CHANGED:
        titleRow.getAdmissionColumn().add(this._buildTitleColumnContent(this.view.getDictionary("existing.therapies.short"), "left"));
        titleRow.getDischargeColumn().add(this._buildTitleColumnContent("", "left"));
        titleRow.getSummaryColumn().add(this._buildTitleColumnContent(this.view.getDictionary("unchanged.therapies.short"), "left"));
        break;
      case groupEnums.ONLY_ON_ADMISSION:
        titleRow.getAdmissionColumn().add(this._buildTitleColumnEmptyContent(false));
        titleRow.getDischargeColumn().add(this._buildTitleColumnEmptyContent(true));
        titleRow.getSummaryColumn().add(this._buildTitleColumnContent(this.view.getDictionary("stop.past"), "left"));
        break;
      case groupEnums.ONLY_ON_DISCHARGE:
        titleRow.getAdmissionColumn().add(this._buildTitleColumnEmptyContent(false));
        titleRow.getDischargeColumn().add(this._buildTitleColumnEmptyContent(true));
        titleRow.getSummaryColumn().add(this._buildTitleColumnContent(this.view.getDictionary("new.therapies.short"), "left"));
        break;
      default:
        break;
    }

    return titleRow;
  },

  /**
   * @see app.views.medications.reconciliation.SummaryColumnTitleContainer#configureListActions
   * @param {tm.jquery.Container} columnContainer
   * @param {boolean} [hasTherapies=false]
   * @param {boolean} [reviewed=false] True, if the list of medications has been reviewed, otherwise false.
   * @param {Date|null} [lastUpdateTime=null] date of the last time the list was changed
   * @param {boolean} [preventEdit=false]
   * @private
   */
  _configureColumnActions: function(columnContainer, hasTherapies, reviewed, lastUpdateTime, preventEdit)
  {
    columnContainer
        .getComponents()[0]
        .configureListActions(!hasTherapies, reviewed === true, preventEdit === true)
        .applyLastListUpdateTime(lastUpdateTime);
  },

  /**
   * Hides all actions in the admission and discharge column header. Intended to be used while refreshing the data, which
   * defines which actions are available.
   * @private
   */
  _hideAllColumnActions: function()
  {
    this._headerRowContainer
        .getAdmissionColumn()
        .getComponents()[0]
        .hideAllActions();
    this._headerRowContainer
        .getDischargeColumn()
        .getComponents()[0]
        .hideAllActions();
  },

  /**
   * Must be called with each data refreshes as it's primarily intended for the west toolbar to refresh the presented date.
   * @param {Date|null} date
   * @private
   */
  _fireReconciliationStartTimeChangeEvent: function(date)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.reconciliation.SummaryView.EVENT_TYPE_RECONCILIATION_START_TIME_CHANGE,
      eventData: date
    }));
  }
});
