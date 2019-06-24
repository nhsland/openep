Class.define('app.views.medications.ordering.templates.ManageTemplatesDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "manage-templates-container",
  padding: 0,

  /* configurable */
  view: null,
  templateContext: app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT,
  medicationSearchFilterMode: app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION,

  /* private */
  _customTemplateGroups: null,
  /** @type app.views.medications.ordering.OrderingContainer */
  _orderingContainer: null,
  /** @type app.views.medications.ordering.BasketContainer */
  _basketContainer: null,
  /** @type app.views.medications.ordering.OrderingCoordinator */
  _orderingCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();

    this._orderingCoordinator = new app.views.medications.ordering.OrderingCoordinator({
      view: this.view,
      orderingContainer: this._orderingContainer,
      basketContainer: this._basketContainer
    });
  },

  /**
   * Override if required. The context further refines the displayed templates when
   * {@link app.views.medications.ordering.OrderingBehaviour#filterTemplatesByActivePatient} is enabled, otherwise
   * {@link #getTemplateMode} is used.
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   */
  getTemplateContext: function()
  {
    return this.templateContext;
  },

  /**
   * Defines the type of templates to be saved and loaded, when loading all available templates. Normally we'd want
   * this in sync with the used context.
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   */
  getTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.mapTherapyTemplateContextToMode(this.getTemplateContext());
  },

  /**
   * Defines the filter used to search the medications.
   * @return {string} of {@link app.views.medications.TherapyEnums.medicationFinderFilterEnum}
   */
  getMedicationSearchFilterMode: function()
  {
    return this.medicationSearchFilterMode;
  },

  /**
   * @return {app.views.medications.ordering.OrderingContainer}
   */
  getOrderingContainer: function()
  {
    return this._orderingContainer;
  },

  /**
   * @return {app.views.medications.ordering.BasketContainer}
   */
  getBasketContainer: function()
  {
    return this._basketContainer;
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  _buildGui: function()
  {
    var view = this.getView();
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    this._orderingContainer = new app.views.medications.ordering.OrderingContainer({
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: false,
        reviewReminderAvailable: false,
        pastDaysOfTherapyVisible: false,
        referenceDataBasedDoseCalculationAvailable: false,
        universalOrderFormAvailable: false,
        filterTemplatesByActivePatient: false,
        doseCalculationsAvailable: view.isDoseCalculationsEnabled()
      }),
      referenceData: new app.views.medications.ordering.patient.ReferenceData(),
      view: view,
      warningsEnabled: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, app.views.medications.ordering.OrderingContainer.DEFAULT_WIDTH),
      templateContext: this.getTemplateContext(),
      additionalMedicationSearchFilter: this.getMedicationSearchFilterMode(),
      createHeaderContainer: this._onOrderingContainerCreateHeaderContainer.bind(this),
      addTemplateTherapyOrdersCallback: this._onOrderingContainerAddTherapiesToBasket.bind(this),
      saveDateTimePaneEvent: this._onOrderingContainerSaveDateTimePane.bind(this),
      confirmOrderEventCallback: this._onOrderingContainerConfirmOrder.bind(this),
      saveOrderToTemplateEventCallback: this._onOrderingContainerSaveOrderToTemplate.bind(this),
      getBasketTherapiesFunction: this._onOrderingContainerGetBasketTherapies.bind(this),
      refreshBasketFunction: this._onOrderingContainerRefreshBasket.bind(this)
    });

    this._basketContainer = new app.views.medications.ordering.BasketContainer({
      view: view,
      headerTitle: view.getDictionary("therapy.list"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      therapyAddedEventCallback: this._onBasketContainerTherapyAdded.bind(this),
      therapiesRemovedEventCallback: this._onBasketContainerTherapiesRemoved.bind(this),
      editTherapyEventCallback: this._onBasketContainerEditTherapy.bind(this),
      saveTemplateEventCallback: this._onBasketContainerSaveTemplate.bind(this)
    });

    this.add(this._orderingContainer);
    this.add(this._basketContainer);
  },

  /**
   * Event handler (callback method) for {@link app.views.medications.ordering.OrderingContainer#createHeaderContainer}.
   * Create a simple title header without any patient/reference data involved.
   * @return {app.views.medications.ordering.MedicationsTitleHeader}
   * @private
   */
  _onOrderingContainerCreateHeaderContainer: function()
  {
    return new app.views.medications.ordering.MedicationsTitleHeader({
      view: this.view,
      title: this.getView().getDictionary('therapy.order')
    });
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#addTemplateTherapyOrdersCallback}.
   * Reschedules the timings (start, end, ...) and adds them to the basket as a new order.
   *
   * @param {Array<app.views.medications.ordering.TherapyOrder>} therapyOrders
   * @private
   */
  _onOrderingContainerAddTherapiesToBasket: function(therapyOrders)
  {
    var basketContainerElementsCount = this.getBasketContainer().getTherapies().length;
    for (var i = 0; i < therapyOrders.length; i++)
    {
      var therapy = therapyOrders[i].getTherapy();
      therapy.rescheduleTherapyTimings(false);
      this._addToBasket(therapyOrders[i].setTherapy(therapy),
          therapyOrders.length,
          basketContainerElementsCount);
    }
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#confirmOrderEventCallback}. If the
   * validation had passed, the order is added to the basket, otherwise we return a false to inform the caller that
   * it was rejected.
   * @param {app.views.medications.ordering.ConfirmOrderEventData} confirmEventData
   * @return {boolean}
   * @private
   */
  _onOrderingContainerConfirmOrder: function(confirmEventData)
  {
    if (confirmEventData.isValidationPassed())
    {
      this._addToBasket(confirmEventData.getTherapyOrder());
      return true;
    }
    return false;
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#saveOrderToTemplateEventCallback}.
   * Opens the save template dialog, with the given therapy as the template contents.
   * @param {app.views.medications.ordering.SaveOrderToTemplateEventData|
   * app.views.medications.ordering.ConfirmOrderEventData} saveEventData
   * @private
   */
  _onOrderingContainerSaveOrderToTemplate: function(saveEventData)
  {
    var self = this;
    this._ensureCustomTemplateGroupsLoaded()
        .then(
            function openDialog()
            {
              var templateElement = app.views.medications.ordering.dto.TherapyTemplateElement
                  .fromTherapyOrder(saveEventData.getTherapyOrder())
                  .setTemplateStatus(saveEventData.isValidationPassed() ?
                      app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE :
                      app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE);
              self._openSaveTemplateDialog([templateElement], true);
            });
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#getBasketTherapiesFunction}.
   * Returns the therapies from the basket, if any.
   * @return {Array<app.views.medications.common.dto.Therapy>}
   * @private
   */
  _onOrderingContainerGetBasketTherapies: function()
  {
    return this.getBasketContainer().getTherapies();
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#refreshBasketFunction}.
   * Simply delegate the call to the {@link #_basketContainer}.
   * @private
   */
  _onOrderingContainerRefreshBasket: function()
  {
    this.getBasketContainer().refreshWithExistingData();
  },

  /**
   * Event handler (callback) for the {@link app.views.medications.ordering.OrderingContainer#saveDateTimePaneEvent}.
   * Not supported at this point.
   * @private
   */
  _onOrderingContainerSaveDateTimePane: function()
  {

  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.BasketContainer#therapyAddedEventCallback}.
   * We basically just clear the {@link #_orderingContainer} once we get confirmation that the therapy was added
   * successfully.
   * @private
   */
  _onBasketContainerTherapyAdded: function()
  {
    this.getOrderingContainer().clear();
  },

  /**
   * Event handler for the {@link app.views.medications.ordering.BasketContainer#editTherapyEventCallback}. We ensure that
   * there's currently not a therapy being ordered, since that would cause the user to loose the data. If there is,
   * we warn the user and ask for his confirmation, before we continue or dismiss the request.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _onBasketContainerEditTherapy: function(therapyContainer)
  {
    this._orderingCoordinator
        .warnUserIfUnfinishedOrderExists()
        .then(this._editBasketTherapy.bind(this, therapyContainer));
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.BasketContainer#saveTemplateEventCallback}.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} basketOrders
   * @private
   */
  _onBasketContainerSaveTemplate: function(basketOrders)
  {
    var self = this;
    this._ensureCustomTemplateGroupsLoaded()
        .then(
            function openDialog()
            {
              self._openSaveTemplateDialog(
                  basketOrders.map(app.views.medications.ordering.dto.TherapyTemplateElement.fromTherapyOrder),
                  false);
            });
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.BasketContainer#therapiesRemovedEventCallback}.
   * Nothing to do at this point.
   * @private
   */
  _onBasketContainerTherapiesRemoved: function()
  {

  },

  /**
   * Show the dialog by which we can save the therapy template.
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplateElement>} elements
   * @param {boolean} [addToExistingTemplate=false]
   * @private
   */
  _openSaveTemplateDialog: function(elements, addToExistingTemplate)
  {
    this._orderingCoordinator
        .createSaveTemplateDialog(this.getTemplateMode(), elements, addToExistingTemplate, this._customTemplateGroups)
        .show();
  },

  /**
   * Ensures the custom template groups are loaded and resolves the given promise when this is true.
   * @return {tm.jquery.Promise}
   * @private
   */
  _ensureCustomTemplateGroupsLoaded: function()
  {
    var deferred = tm.jquery.Deferred.create();
    var self = this;

    if (!tm.jquery.Utils.isArray(this._customTemplateGroups))
    {
      this.getView()
          .getRestApi()
          .loadTherapyTemplateGroups(this.getTemplateMode())
          .then(
              function onLoad(groups)
              {
                self._customTemplateGroups = groups;
                deferred.resolve(groups);
              },
              function onError()
              {
                deferred.reject();
              }
          );
    }
    else
    {
      deferred.resolve(this._customTemplateGroups)
    }

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} orderItemData
   * @param {number|undefined} [addedTherapiesCount=undefined]
   * @param {number|undefined} [initialBasketItemsCount=undefined]
   * @private
   */
  _addToBasket: function(orderItemData, addedTherapiesCount, initialBasketItemsCount)
  {
    var self = this;

    this.getView()
        .getRestApi()
        .fillTherapyDisplayValues(orderItemData.getTherapy(), true)
        .then(
            function onSuccess(updatedTherapy)
            {
              var basketItemsCount = self.getBasketContainer().getTherapies().length;
              var addingLastTherapyFromList = !addedTherapiesCount ||
                  basketItemsCount - initialBasketItemsCount === addedTherapiesCount - 1;

              orderItemData.setTherapy(updatedTherapy);
              self.getBasketContainer().addTherapy(orderItemData, {forceNoRefreshWarnings: !addingLastTherapyFromList});
            }
        );
  },

  /**
   * Called when the user double clicks on an existing therapy in the basket, triggering the edit operation on it.
   * The existing order is removed from the basket and the appropriate order form in the ordering container is shown.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _editBasketTherapy: function(therapyContainer)
  {
    var therapy = therapyContainer.getData().getTherapy();

    this.getBasketContainer().removeTherapy(therapy);
    this.getOrderingContainer().editTherapyOrder(therapyContainer, false);
  }
});