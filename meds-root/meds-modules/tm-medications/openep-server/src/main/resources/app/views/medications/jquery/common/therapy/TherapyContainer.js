Class.define('app.views.medications.common.therapy.TherapyContainer', 'tm.jquery.Container', {
  cls: "therapy-container",
  data: null,
  displayProvider: null,
  view: null,
  toolbar: null,

  showIconTooltip: null,  // TODO: move this to the display provider
  scrollableElement: null,

  _baseCls: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.displayProvider = this.getConfigValue(
        "displayProvider",
        new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
          view: this.view
        }));
    this.showIconTooltip = this.getConfigValue("showIconTooltip", true);

    this._baseCls = tm.jquery.Utils.isEmpty(this.getCls()) ? "" : this.getCls();

    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-container-coordinator',
      view: this.getView(),
      component: this
    });
  },

  /**
   * @protected
   * @returns {tm.jquery.Container}
   */
  buildTherapySummaryContainer: function()
  {
    var summaryContainer = new tm.jquery.Container({
      cls: 'therapy-summary-container',
      cursor: this.showIconTooltip === true ? "pointer" : "default",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      alignSelf: "flex-start"
    });

    summaryContainer.add(this.buildTherapyDescription());
    this._addAdditionalTherapyDataToSummary(summaryContainer);
    this._addHighAlertIcons(summaryContainer);

    return summaryContainer;
  },

  /**
   * @protected
   * @returns {tm.jquery.Container}
   */
  buildIconContainer: function()
  {
    var self = this;
    var showDetailsCard = this.showIconTooltip === true;
    var iconContainer = new tm.jquery.Container({
      alignSelf: "flex-start",
      cursor: showDetailsCard ? "pointer" : "default",
      margin: "2 4 2 3",
      width: 48,
      height: 48,
      html: this.getDisplayProvider().getBigIconContainerHtml(this.getData())
    });

    if (showDetailsCard)
    {
      iconContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        self._showTherapyDetailsContentPopup(component);
      });
    }
    return iconContainer;
  },

  /**
   * @protected
   * @returns {string}
   */
  buildTherapyDescription: function()
  {
    var displayProvider = this.getDisplayProvider();
    var medicationUtils = app.views.medications.MedicationUtils;
    var therapy = this.getTherapy();
    var data = this.getData();

    var therapyShortDescription = therapy.getFormattedTherapyDisplay();

    if (displayProvider.getShowTherapyExpiring() && data.getTherapy().isTherapyExpiring(data.getOriginalTherapyStart()))
    {
      therapyShortDescription += medicationUtils.buildIconDescriptionRowHtml(
          "icon_therapy_expire therapy-description-icon",
          displayProvider.getRemainingDurationDescription(therapy));
      this.setStyle('border-right: 2px solid grey;');
    }

    if (displayProvider.getShowMaxDose())
    {
      therapyShortDescription += medicationUtils.createMaxDosePercentageInfoHtml(this.getView(),
          data.getTherapy().getMaxDosePercentage());
    }

    if (displayProvider.isShowValidationIssues() && !data.isValid())
    {
      therapyShortDescription += medicationUtils.buildIconDescriptionRowHtml(
          "therapy-description-icon validation-issue-icon",
          displayProvider.createValidationIssueDescription(data.getValidationIssues()));
      this.setStyle('border-right: 2px solid grey;');
    }
    return new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      html: therapyShortDescription,
      cls: 'TherapyDescription'
    });
  },

  /**
   * @return {app.views.medications.common.therapy.AbstractTherapyContainerData}
   */
  getData: function ()
  {
    return this.data;
  },

  getTherapy: function()
  {
    return this.getData().getTherapy();
  },

  getTherapyId: function ()
  {
    var data = this.getData();

    return tm.jquery.Utils.isEmpty(data) ?
        null : (tm.jquery.Utils.isEmpty(data.getTherapy()) ? null : data.getTherapy().getCompositionUid());
  },

  getView: function ()
  {
    return this.view;
  },

  getDisplayProvider: function ()
  {
    return this.displayProvider;
  },

  getScrollableElement: function()
  {
    return this.scrollableElement;
  },

  getToolbar: function ()
  {
    return this.toolbar;
  },

  setToolbar: function (toolbar)
  {
    // add an empty flex contianer to push the toolbar to the right
    if (tm.jquery.Utils.isEmpty(this.toolbar))
    {
      this.add(new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }));
    }
    this.toolbar = toolbar;
    this.add(this.toolbar);
  },

  highlight: function ()
  {
    if (this.isRendered())
    {
      $(this.getDom()).effect("highlight", {color: "#FFFFCC"}, 1000);
    }
    else
    {
      var appFactory = this.getView().getAppFactory();
      var self = this;

      appFactory.createConditionTask(
          function ()
          {
            self.highlight();
          },
          function ()
          {
            return self.isRendered();
          },
          50, 10);
    }
  },

  refresh: function ()
  {
    this.removeAll();
    this._buildGui();
    this.repaint();
  },

  /**
   * @private
   */
  _buildGui: function ()
  {
    var displayProvider = this.getDisplayProvider();
    var data = this.getData();
    var therapyStatusDisplayClass = displayProvider.getStatusClass(data);

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    this.setCls(this._baseCls + " " + therapyStatusDisplayClass + " text-unselectable");

    this.add(this._buildStatusLine());
    this.add(this.buildIconContainer());
    this.add(this.buildTherapySummaryContainer());
    this._refreshToolbar();
  },

  /**
   * @private
   * @param {tm.jquery.Container} summaryContainer
   */
  _addHighAlertIcons: function(summaryContainer)
  {
    var data = this.getData();
    if (data.hasMedicationProperties() || data.hasNonFormularyMedications())
    {
      summaryContainer.add(this._buildHighRiskIconsContainer(data));
    }
  },

  /**
   * @private
   * @returns {tm.jquery.Component}
   */
  _buildStatusLine: function()
  {
    return new tm.jquery.Component({
      cls: "status-line",
      alignSelf: "stretch",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px")
    });
  },

  /**
   * @private
   * @param {tm.jquery.Container|tm.jquery.Component} summaryContainer
   */
  _addAdditionalTherapyDataToSummary: function(summaryContainer)
  {
    var self = this;
    var displayProvider = this.getDisplayProvider();
    var data = this.getData();

    if (data.isRecordAdministration())
    {
      summaryContainer.add(displayProvider.createRecordAdministrationDescription());
    }

    if (displayProvider.isShowChangeHistory() && data.getChanges().length > 0)
    {
      summaryContainer.add(displayProvider.createChangeHistoryDetailsDescription(data.getTherapy(), data.getChanges()));
    }
    else if (['ABORT', 'SUSPEND'].indexOf(data.getChangeType()) >= 0) // enum [PharmacistTherapyChangeType.java]
    {
      summaryContainer.add(displayProvider.createChangeTypeDescription(data.getChangeType()));
    }

    if (displayProvider.isShowChangeReason() && !!data.getTherapyChangeReason())
    {
      summaryContainer.add(displayProvider.createChangeReasonContent(data.getTherapyChangeReason()));
    }

    if (this.showIconTooltip === true)
    {
      summaryContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        self._showTherapyDetailsContentPopup(component);
      });
    }
  },

  _refreshToolbar: function()
  {
    var toolbar = this.getToolbar();
    if (!tm.jquery.Utils.isEmpty(toolbar)) {
      // add an empty flex container to push the toolbar to the right
      this.add(new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }));
      this.add(toolbar);

      if (toolbar.refresh instanceof Function) toolbar.refresh();
    }
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} therapyData
   * @returns {boolean}
   * @private
   */
  _isTherapyCancelledOrAborted: function (therapyData)
  {
    var enums = app.views.medications.TherapyEnums;
    var therapyStatus = therapyData.getTherapyStatus();
    return therapyStatus === enums.therapyStatusEnum.ABORTED || therapyStatus === enums.therapyStatusEnum.CANCELLED;
  },

  /**
   * @param {tm.jquery.Component} component
   * @param {app.views.medications.common.dto.MedicationData} [medicationData=undefined]
   * @private
   */
  _buildTherapyDetailsContentPopup: function(component, medicationData)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var data = this.getData();

    var therapyDetailsContent = new app.views.medications.common.therapy.TherapyDetailsContentContainer({
      view: view,
      displayProvider: this.getDisplayProvider(),
      data: data,
      therapy: this.getTherapy(),
      medicationData: medicationData
    });

    var therapyDetailsContentPopup = appFactory.createDefaultPopoverTooltip(
        view.getDictionary("medication"),
        null,
        therapyDetailsContent
    );
    therapyDetailsContent.setDialogZIndex(therapyDetailsContentPopup.zIndex);

    therapyDetailsContentPopup.addTestAttribute(this.getTherapy().getTherapyId());
    therapyDetailsContentPopup.setDefaultAutoPlacements(["rightBottom", "rightTop", "right"]);
    therapyDetailsContentPopup.setPlacement("auto");
    // don't touch default placements, because it triggers errors in positioning ...

    if (this.getScrollableElement())
    {
      therapyDetailsContentPopup.setAppendTo(this.getScrollableElement());
    }

    therapyDetailsContentPopup.setTrigger("manual");
    component.setTooltip(therapyDetailsContentPopup);

    setTimeout(function()
    {
      therapyDetailsContentPopup.show();
    }, 10);
  },

  /**
   * @param {tm.jquery.Component} component
   * @private
   */
  _showTherapyDetailsContentPopup: function(component)
  {
    var self = this;
    var therapy = this.getTherapy();
    var view = this.getView();

    tm.jquery.ComponentUtils.hideAllTooltips();
    if (therapy.hasNonUniversalIngredient())
    {
      view.getRestApi().loadMedicationDataForMultipleIds(therapy.getAllIngredientIds()).then(function(medicationData)
      {
        self._buildTherapyDetailsContentPopup(component, medicationData);
      });
    }
    else
    {
      self._buildTherapyDetailsContentPopup(component);
    }
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} data
   * @returns {app.views.medications.ordering.HighRiskMedicationIconsContainer}
   * @private
   */
  _buildHighRiskIconsContainer: function(data)
  {
    var mockedMedicationData = new app.views.medications.common.dto.MedicationData();
    mockedMedicationData.setProperties(data.getMedicationProperties());
    mockedMedicationData.setFormulary(data.hasNonFormularyMedications());
    var highRiskIconsContainer = new app.views.medications.ordering.HighRiskMedicationIconsContainer({
      view: this.getView(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    highRiskIconsContainer.presentHighAlertIcons(mockedMedicationData);
    return highRiskIconsContainer;
  }
});
