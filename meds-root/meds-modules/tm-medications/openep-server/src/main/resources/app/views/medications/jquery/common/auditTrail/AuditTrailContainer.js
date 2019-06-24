Class.define('app.views.medications.common.auditTrail.AuditTrailContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "audit-trail-container",
  view: null,
  auditTrailData: null,
  _fixedDateContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.getView();
    var therapiesContainer = new tm.jquery.Container({
      cls: "therapies-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch")
    });

    var originalTherapyDescription = new app.views.medications.common.therapy.TherapyContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      data: new app.views.medications.common.auditTrail.AuditTrailTherapyContainerData({
        therapy: this.getAuditTrailData().getOriginalTherapy()
      }),
      showIconTooltip: false
    });
    var originalTherapyContainer = new app.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component original-therapy-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      titleText: view.getDictionary("initial.prescription"),
      contentComponent: originalTherapyDescription
    });

    var currentTherapyDescription = new app.views.medications.common.therapy.TherapyContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      data: new app.views.medications.common.auditTrail.AuditTrailTherapyContainerData({
        therapy: this.getAuditTrailData().getCurrentTherapy(),
        therapyStatus: this.getAuditTrailData().getCurrentTherapyStatus()
      }),
      showIconTooltip: false
    });

    var currentTherapyContainer = new app.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component current-therapy-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      titleText: view.getDictionary("current.prescription"),
      contentComponent: currentTherapyDescription
    });
    this._fixedDateContainer = this._createNewDayContainer(null);
    therapiesContainer.add(originalTherapyContainer);
    therapiesContainer.add(currentTherapyContainer);
    this.add(therapiesContainer);
    this.add(this._fixedDateContainer);
    this.add(this._createChangeEventsContainers());
  },

  /**
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeEventsContainers: function()
  {
    var self = this;
    var view = this.getView();
    var currentDay = null;
    var contentContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "vertical",
      cls: "audit-trail-content-container"
    });
    contentContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_SCROLL, function()
    {
      var $fixedDateContainer = $(self._fixedDateContainer.getDom());
      var fixedDateContainerBottom = $fixedDateContainer.offset().top + $fixedDateContainer.outerHeight();
      var actionContainers = $('.audit-trail-content-container .change-row-container');
      for (var i = 0; i < actionContainers.length; i++)
      {
        var $actionContainer = $(actionContainers.get(i));
        if ($actionContainer.offset().top + $actionContainer.outerHeight() >= fixedDateContainerBottom)
        {
          self._fixedDateContainer.setHtml(view.getDisplayableValue(
              new Date(self.auditTrailData.actionHistoryList[i].actionPerformedTime),
              "date.medium"));
          break;
        }
      }
    });
    this.getAuditTrailData().getActionHistoryList().forEach(function(action)
    {
      var actionPerformedTime = action.getActionPerformedTime();
      var actionPerformedDate = new Date(actionPerformedTime.getFullYear(),
          actionPerformedTime.getMonth(),
          actionPerformedTime.getDate());
      actionPerformedDate = new Date(actionPerformedDate.setHours(0, 0, 0, 0));
      var firstChangeInDay = false;
      if (currentDay === null)
      {
        self._fixedDateContainer.setHtml(view.getDisplayableValue(actionPerformedDate, "date.medium"));
        currentDay = actionPerformedDate;
        firstChangeInDay = true;
      }

      else if (actionPerformedDate.getTime() !== currentDay.getTime())
      {
        currentDay = actionPerformedDate;
        contentContainer.add(self._createNewDayContainer(actionPerformedTime));
        firstChangeInDay = true;
      }
      contentContainer.add(self._createActionRow(action, firstChangeInDay))
    });
    return contentContainer;
  },

  /**
   * @param {Date} date
   * @return {tm.jquery.Container}
   * @private
   */
  _createNewDayContainer: function(date)
  {
    return new tm.jquery.Container({
      cls: "date-container TextLabel title-label ellipsis",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      html: date ? this.getView().getDisplayableValue(date, "date.medium") : null
    });
  },

  /**
   * @param {Object} action
   * @param {boolean} borderless
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionRow: function(action, borderless)
  {
    var view = this.getView();
    var actionRowContainer = new tm.jquery.Container({
      cls: "change-row-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var actionTimeContainer = new tm.jquery.Container({
      cls: "TextData time-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDisplayableValue(action.getActionPerformedTime(), "time.short")
    });

    actionRowContainer.add(actionTimeContainer);

    var actionRowContentContainer = this._createActionRowContentContainer(action);
    var titledContentContainerCls = borderless ? "borderless-change-content-container" : "change-content-container";

    var titledContentContainer = new tm.jquery.Container({
      cls: titledContentContainerCls,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var actionRowTitleContainer = new tm.jquery.Container({
      cls: "title-label",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });

    var actionHistoryTypeContainer = new tm.jquery.Container({
      cls: "TextDataBold",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary("TherapyActionHistoryType." + action.getTherapyActionHistoryType())
    });
    actionRowTitleContainer.add(actionHistoryTypeContainer);

    if (action.getPerformer())
    {
      var actionPerformerContainer = new tm.jquery.Container({
        cls: "TextData",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: " &nbsp; &ndash; &nbsp;" + action.getPerformer()
      });
      actionRowTitleContainer.add(actionPerformerContainer);
    }


    titledContentContainer.add(actionRowTitleContainer);
    titledContentContainer.add(actionRowContentContainer);

    actionRowContainer.add(titledContentContainer);
    return actionRowContainer;
  },

  /**
   * @param {Object} action
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionRowContentContainer: function(action)
  {
    var view = this.getView();
    var actionChangesContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    actionChangesContainer.add(
        new app.views.medications.common.auditTrail.TherapyChangesContainer({
          view: this.getView(),
          therapyChanges: action.getChanges(),
          changeTitleFlexBasis: '160px',
          prevQuantityUnit: this.getAuditTrailData().getOriginalTherapy().getQuantityUnit(),
          currentQuantityUnit: this.getAuditTrailData().getCurrentTherapy().getQuantityUnit()
        })
    );

    if (action.getActionTakesEffectTime())
    {
      actionChangesContainer.add(this._createChangeRowForSingle(
          view.getDictionary("change.takes.effect"),
          view.getDisplayableValue(action.getActionTakesEffectTime(), "datetime.medium")));
    }

    var changeReason = action.getChangeReason();
    if (changeReason && changeReason.getReason())
    {
      var changeReasonText = changeReason.comment ?
          changeReason.getReason().name + " - " + changeReason.comment :
          changeReason.getReason().name;

      actionChangesContainer.add(
          this._createChangeRowForSingle(
              view.getDictionary("change.reason"),
              changeReasonText));
    }
    return actionChangesContainer;
  },

  /**
   * @param {String} title
   * @return {tm.jquery.Container}
   * @private
   */
  _createActionTitleContainer: function(title)
  {
    return new tm.jquery.Container({
      cls: "TextLabel",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "160px"),
      html: title
    });
  },

  /**
   * @param {String} title
   * @param {String} value
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeRowForSingle: function(title, value)
  {
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      alignSelf: "stretch"
    });
    actionChangeRowContainer.add(this._createActionTitleContainer(title));

    var valueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      cls: "value-container TextData",
      html: tm.jquery.Utils.escapeHtml(value)
    });
    actionChangeRowContainer.add(valueContainer);

    return actionChangeRowContainer;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyAuditTrail}
   * @private
   */
  getAuditTrailData: function()
  {
    return this.auditTrailData;
  },

  /**
   * @param {function} resultDataCallback
   */
  processResultData: function (resultDataCallback)
  {
    resultDataCallback(new app.views.common.AppResultData({success: true}));
  }
});