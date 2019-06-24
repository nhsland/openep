Class.define('app.views.medications.mentalHealth.T2T3TherapySelectionColumn', 'tm.jquery.Container', {
  cls: "ordering-container",
  /** configs */
  view: null,
  displayProvider: null,

  addTherapyContainerDataToBasket: null,
  onMedicationSelected: null,

  /** privates: components */
  header: null,

  searchContainer: null,
  mentalHealthTemplatesContainer: null,
  activeMentalHealthDrugsContainer: null,
  abortedMentalHealthDrugsContainer: null,

  _selectedCls: "item-selected", // container css when it's added to basket
  _renderConditionTask: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
    this._attachOnRenderDataLoadHandler();
  },

  clear: function()
  {
    this.searchContainer.clear();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @param {Array<app.views.medications.mentalHealth.TherapyOrder>} removedOrders
   */
  handleBasketTherapiesRemoved: function(removedOrders)
  {
    var self = this;
    removedOrders.forEach(function(elementData)
    {
      // noinspection JSCheckFunctionSignatures
      self._getCorrectGroupContainerByGroupEnum(elementData.getGroup())
          .markElementRemovedFromBasket(elementData.getSourceId());
    });
  },

  /**
   * @override to abort the conditional data loading task when destroyed.
   */
  destroy: function()
  {
    this.callSuper();
    this._abortRenderConditionTask();
  },

  _attachOnRenderDataLoadHandler: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER,
        function onT2T3TherapySelectionColumnRender()
        {
          self._abortRenderConditionTask();
          self._renderConditionTask = appFactory.createConditionTask(
              function()
              {
                self._loadCurrentMentalHealthDrugsGroups();
                self._loadMentalHealthTemplatesGroup();
                self._renderConditionTask = null;
              },
              function()
              {
                return self.isRendered(true);
              },
              100, 150
          );
        });
  },

  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
  },

  /** private methods */
  _buildGui: function()
  {
    var self = this;
    var view = self.getView();

    this.header = new app.views.medications.ordering.MedicationsTitleHeader({
      view: view,
      title: view.getDictionary('therapy.order')
    });

    this.searchContainer = new app.views.medications.ordering.SearchContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      additionalFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.MENTAL_HEALTH,
      medicationSelectedEvent: function(medicationData)
      {
        self._handleMedicationSelected(medicationData);
      }
    });

    this.mentalHealthTemplatesContainer = new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("mental.health.groups"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: this._selectedCls,
      addAllEnabled: false,
      displayProvider: this.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyContainerDataToBasket([therapyContainer.getData()]);
      }
    });

    this.activeMentalHealthDrugsContainer = new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("active.inpatient.medications"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ACTIVE,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: this._selectedCls,
      addAllEnabled: true,
      displayProvider: this.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyContainerDataToBasket([therapyContainer.getData()]);
      },
      addAllMedicationsToBasketFunction: function(groupContainer)
      {
        self._onAddTherapyContainerDataToBasket(groupContainer.getContentData());
      }
    });

    this.abortedMentalHealthDrugsContainer = new app.views.medications.mentalHealth.TherapyGroupPanel({
      groupTitle: view.getDictionary("stopped.and.suspended.medications"),
      groupId: app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ABORTED,
      view: view,
      contentData: [],
      dynamicContent: true,
      selectedCls: this._selectedCls,
      addAllEnabled: true,
      displayProvider: this.displayProvider,
      addMedicationToBasketFunction: function(therapyContainer)
      {
        self._onAddTherapyContainerDataToBasket([therapyContainer.getData()]);
      },
      addAllMedicationsToBasketFunction: function(groupContainer)
      {
        self._onAddTherapyContainerDataToBasket(groupContainer.getContentData());
      }
    });

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    contentScrollContainer.add(this.activeMentalHealthDrugsContainer);
    contentScrollContainer.add(this.abortedMentalHealthDrugsContainer);
    contentScrollContainer.add(this.mentalHealthTemplatesContainer);

    this.add(this.header);
    this.add(this.searchContainer);
    this.add(contentScrollContainer);
  },

  _handleMedicationSelected: function(medicationData)
  {
    this.onMedicationSelected(medicationData);
    this.searchContainer.clear();
  },

  /**
   * @param {Array<app.views.medications.mentalHealth.MentalHealthTherapyContainerData>} containerData
   * @private
   */
  _onAddTherapyContainerDataToBasket: function(containerData)
  {
    this.addTherapyContainerDataToBasket(containerData)
        .forEach(
            function markSuccess(containerData)
            {
              this._markAddedGroupPanelTherapy(containerData);
            },
            this);
  },

  _loadMentalHealthTemplatesGroup: function()
  {
    var self = this;
    var view = this.getView();

    view.getRestApi()
        .loadMentalHealthTemplates()
        .then(
            function attachMentalHealthTemplates(templates)
            {
              self.mentalHealthTemplatesContainer.setContentData(
                  templates.map(
                      function mapTemplateToContainerData(template)
                      {
                        return new app.views.medications.mentalHealth.MentalHealthTemplateContainerData({
                          therapy: template
                        });
                      }),
                  true);
            }
        );
  },

  /**
   * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData
   * |app.views.medications.common.therapy.AbstractTherapyContainerData} elementData
   * @private
   */
  _markAddedGroupPanelTherapy: function(elementData)
  {
    this._getCorrectGroupContainerByGroupEnum(elementData.getGroup())
        .markElementAddedToBasket(elementData);
  },

  _loadCurrentMentalHealthDrugsGroups: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var self = this;
    var view = this.getView();

    view.getRestApi()
        .loadCurrentHospitalizationMentalHealthTherapies()
        .then(
            function attachMentalHealthTherapies(mentalHealthTherapies)
            {
              var containerData = mentalHealthTherapies.map(
                  function toContainerData(mentalHealthTherapy)
                  {
                    return new app.views.medications.mentalHealth.MentalHealthTherapyContainerData({
                      therapy: mentalHealthTherapy
                    });
                  });

              self.activeMentalHealthDrugsContainer.setContentData(
                  containerData.filter(
                      function isActive(therapy)
                      {
                        return therapy.isTherapyActive();
                      }),
                  true);
              self.abortedMentalHealthDrugsContainer.setContentData(
                  containerData.filter(
                      function isDeactivated(therapy)
                      {
                        return !therapy.isTherapyActive();
                      }),
                  true);
            });
  },

  _getCorrectGroupContainerByGroupEnum: function(groupEnum)
  {
    if (groupEnum === app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ABORTED)
    {
      return this.abortedMentalHealthDrugsContainer;
    }
    else if (groupEnum === app.views.medications.TherapyEnums.mentalHealthGroupEnum.INPATIENT_ACTIVE)
    {
      return this.activeMentalHealthDrugsContainer;
    }
    else
    {
      return this.mentalHealthTemplatesContainer;
    }
  }
});
