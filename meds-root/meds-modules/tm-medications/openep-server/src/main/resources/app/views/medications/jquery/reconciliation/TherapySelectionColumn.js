Class.define('app.views.medications.reconciliation.TherapySelectionColumn', 'app.views.medications.ordering.OrderingContainer', {
  titleText: null,
  view: null,

  suspendCancelSupport: null,

  therapyGroupsContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    this.suspendCancelSupport = this.getConfigValue("suspendCancelSupport", false);
  },

  /***
   * Override
   */
  buildTherapySelectionCardContent: function()
  {
    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var therapyGroupsContainer = new tm.jquery.Container({
      cls: "therapy-groups-list",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    this.attachTherapyGroupPanels(therapyGroupsContainer);
    this.therapyGroupsContainer = therapyGroupsContainer;

    contentScrollContainer.add(this.therapyGroupsContainer);
    contentScrollContainer.add(this.templatesContainer);

    if (!!this.searchContainer)
    {
      this.getTherapySelectionCard().add(this.searchContainer);
    }
    this.getTherapySelectionCard().add(contentScrollContainer);
  },

  /* Override to build therapy lists. */
  attachTherapyGroupPanels: function(container)
  {
    // do nothing
  },

  /**
   * @override
   * @return {string}
   */
  createHeaderTitleText: function()
  {
    return this.getTitleText();
  },

  /**
   * Override, do nothing
   */
  refreshBasketFunction: function()
  {

  },

  /**
   * Override, do nothing
   */
  saveDateTimePaneEvent: function()
  {

  },

  findTherapyGroupPanelByEnum: function(groupEnum)
  {
    var container = this.getTherapyGroupsContainer();
    if (container && !tm.jquery.Utils.isEmpty(groupEnum))
    {
      for (var idx = 0; idx < container.getComponents().length; idx++)
      {
        var panel = container.getComponents()[idx];
        if (panel && panel instanceof app.views.medications.common.TherapyGroupPanel && panel.getGroupId() === groupEnum)
        {
          return panel;
        }
      }
    }
    return null;
  },

  getTherapyGroupPanelContentByGroupEnum: function(groupEnum, processCallback)
  {
    var panel = this.findTherapyGroupPanelByEnum(groupEnum);
    if (panel)
    {
      var panelContent = panel.getContent();
      if (processCallback)
      {
        processCallback(panelContent.getComponents());
      }
      else
      {
        return panelContent.getComponents();
      }
    }
    return [];
  },

  showList: function()
  {
    this.clear();
  },

  getTitleText: function()
  {
    return this.titleText;
  },

  getView: function()
  {
    return this.view;
  },

  setSourceTherapyContainer: function(container)
  {
    this._sourceTherapyContainer = container;
  },

  getSourceTherapyContainer: function()
  {
    return this._sourceTherapyContainer;
  },

  getTherapyGroupsContainer: function()
  {
    return this.therapyGroupsContainer;
  },

  /**
   * Override, set the source therapy container to null when selected via the search container.
   */
  _handleMedicationSelected: function(medicationData)
  {
    this.setSourceTherapyContainer(null);
    this.callSuper(medicationData);
  },

  /**
   * @override to mark the source therapy container.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @param {boolean} [changeReasonAvailable=false]
   */
  editTherapyOrder: function(therapyContainer, changeReasonAvailable)
  {
    this.callSuper(therapyContainer, changeReasonAvailable);

    if (therapyContainer.getData() instanceof app.views.medications.ordering.dto.TherapyTemplateElement)
    {
      this.setSourceTherapyContainer(null);
    }
    else
    {
      this.setSourceTherapyContainer(therapyContainer);
    }
  }
});