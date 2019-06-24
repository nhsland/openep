Class.define('app.views.medications.ordering.MedicationsOrderingContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "medications-ordering-container",
  padding: 0,

  /** configs */
  view: null,
  patientId: null,
  presetMedicationId: null, //optional
  presetTherapies: null,    //optional
  warningsEnabled: true,
  prescribeByTemplatesOnlyMode: false,
  isPastMode: false,
  assertBaselineInfusion: true,

  /** privates */
  baselineInfusionIntervals: null,
  resultCallback: null,
  linkIndex: null,
  medicationRuleUtils: null,

  /** privates: components */
  orderingContainer: null,
  basketContainer: null,
  warningsContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  _referenceData: null,
  /** @type app.views.medications.ordering.OrderingCoordinator */
  _orderingCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()});
    this.baselineInfusionIntervals = [];
    this.linkIndex = 0;
    this._loadPatientBaselineInfusionIntervals();

    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({view: this.view, referenceData: this.getReferenceData()}));

    this._buildComponents();
    this._buildGui();

    this._orderingCoordinator = new app.views.medications.ordering.OrderingCoordinator({
       view: this.view,
       orderingContainer: this.orderingContainer,
       basketContainer: this.basketContainer,
       warningsContainer: this.warningsContainer
    });

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      if (self.presetMedicationId)
      {
        setTimeout(function()
        {
          self.orderingContainer.presetMedication(self.presetMedicationId);
        }, 500);
      }
      if (self.presetTherapies)
      {
        /**
         * @param {app.views.medications.common.dto.Therapy} therapy
         */
        var presetTherapyOrders = self.presetTherapies.map(
            function mapTherapyToTherapyOrder(therapy)
            {
              return new app.views.medications.ordering.TherapyOrder()
                  .setTherapy(therapy.clone(true));
            });
        self._fixTherapiesTimingAndAddToBasket(presetTherapyOrders, true);
      }
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var view = this.view;

    this.orderingContainer = this.buildOrderingContainer();
    this.basketContainer = this.buildBasketContainer();

    if (this.isWarningsEnabled())
    {
      this.warningsContainer = new app.views.medications.ordering.warnings.WarningsContainer({
        view: view,
        referenceData: this.getReferenceData(),
        overrideAllowed: !this.isPrescribeByTemplatesOnlyMode(),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
    }

    if (this.isPastMode === true)
    {
      var careProfessionals = view.getCareProfessionals();
      var currentUserAsCareProfessionalName = view.getCurrentUserAsCareProfessional() ?
          view.getCurrentUserAsCareProfessional().name :
          null;
      this._performerContainer =
          app.views.medications.MedicationUtils.createPerformerContainer(view,
              careProfessionals,
              currentUserAsCareProfessionalName);
    }

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    mainContainer.add(this.orderingContainer);
    var eastContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    eastContainer.add(this.basketContainer);
    if (!!this.warningsContainer)
    {
      eastContainer.add(this.warningsContainer);
    }
    mainContainer.add(eastContainer);
    this.add(mainContainer);
    if (this._performerContainer != null)
    {
      this.add(this._performerContainer);
    }
    this.add(this.saveDateTimePane);
  },

  _editBasketTherapy: function(therapyContainer)
  {
    var self = this;

    var therapy = therapyContainer.getData().getTherapy();

    this.basketContainer.removeTherapy(therapy); // has to be removed before warnings are calculated (otherwise it's included)
    this.orderingContainer.editTherapyOrder(therapyContainer, false);

    if (self.warningsEnabled)
    {
      self._refreshWarnings();
    }
    if (therapy && therapy.isBaselineInfusion())
    {
      self._removeBaselineInfusion(therapy);
    }
  },

  /**
   * Event handler for {@link app.views.medications.ordering.BasketContainer#saveTemplateEventCallback}.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} basketOrders
   * @private
   */
  _onBasketContainerSaveToTemplate: function(basketOrders)
  {
    this._openSaveTemplateDialog(
        basketOrders.map(app.views.medications.ordering.dto.TherapyTemplateElement.fromTherapyOrder),
        false);
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} orderItemData
   * @param {number|undefined} [addedTherapiesCount=undefined]
   * @param {number|undefined} [initialBasketItemsCount=undefined]
   * @return {boolean}
   * @private
   */
  _addToBasket: function(orderItemData, addedTherapiesCount, initialBasketItemsCount)
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var therapy = orderItemData.getTherapy();

    var baselineInfusionAlreadyExists =
        therapy.isBaselineInfusion() && this._baselineInfusionsIntersects(therapy.getStart(), therapy.getEnd());

    if (baselineInfusionAlreadyExists && this.assertBaselineInfusion)
    {
      appFactory.createWarningSystemDialog(self.view.getDictionary("patient.already.has.baseline.infusion"), 320, 122).show();
      return false;
    }
    else
    {
      this.getView()
          .getRestApi()
          .fillTherapyDisplayValues(therapy, true)
          .then(
              function onSuccess(updatedTherapy)
              {
                var basketItemsCount = self.basketContainer.getTherapies().length;
                var addingLastTherapyFromList = !addedTherapiesCount ||
                    basketItemsCount - initialBasketItemsCount === addedTherapiesCount - 1;

                orderItemData.setTherapy(updatedTherapy);
                self.basketContainer.addTherapy(orderItemData, {forceNoRefreshWarnings: !addingLastTherapyFromList});
              }
          );

      if (therapy.isBaselineInfusion())
      {
        this.baselineInfusionIntervals.push({
          startMillis: therapy.getStart().getTime(),
          endMillis: !!therapy.getEnd() ? therapy.getEnd().getTime() : null
        })
      }
      return true;
    }
  },

  /**
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplateElement>} elements
   * @param {boolean} [addToExistingTemplate=false]
   * @private
   */
  _openSaveTemplateDialog: function(elements, addToExistingTemplate)
  {
    this._orderingCoordinator
        .createSaveTemplateDialog(this.getTemplateMode(), elements, addToExistingTemplate)
        .show();
  },

  /**
   * Reschedule timings of therapy object on each therapy order. One should keep in mind that rescheduling timings changes
   * the therapy object itself, meaning that therapy objects on therapy orders might need to be cloned before calling
   * this function.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} therapyOrders
   * @param {Boolean} clearEnd
   * @private
   */
  _fixTherapiesTimingAndAddToBasket: function(therapyOrders, clearEnd)
  {
    var basketContainerElementsCount = this.basketContainer.getTherapies().length;
    for (var i = 0; i < therapyOrders.length; i++)
    {
      var therapy = therapyOrders[i].getTherapy();
      therapy.rescheduleTherapyTimings(clearEnd);
      this._addToBasket(
          therapyOrders[i],
          therapyOrders.length,
          basketContainerElementsCount);
    }
  },

  _baselineInfusionsIntersects: function(start, end)
  {
    for (var i = 0; i < this.baselineInfusionIntervals.length; i++)
    {
      if (start.getTime() < this.baselineInfusionIntervals[i].startMillis)
      {
        if (!end || end.getTime() > this.baselineInfusionIntervals[i].startMillis)
        {
          return true
        }
      }
      else
      {
        if (!this.baselineInfusionIntervals[i].endMillis || this.baselineInfusionIntervals[i].endMillis > start.getTime())
        {
          return true;
        }
      }
    }
    return false;
  },

  _getPrescriber: function()
  {
    return this._performerContainer != null ?
        this._performerContainer.getPerformer() : this.view.getCurrentUserAsCareProfessional();
  },

  /**
   * @see {app.views.medications.ordering.OrderingCoordinator#ensureOrderCanBePlaced}
   * @returns {tm.jquery.Promise}
   * @protected
   */
  ensureOrderCanBePlaced: function()
  {
    if (!this.isPastMode && !this._getPrescriber())
    {
      this.view.getAppFactory()
          .createWarningSystemDialog(this.view.getDictionary("prescriber.not.defined.warning"), 320, 122)
          .show();
      return tm.jquery.Deferred.create()
          .reject()
          .promise();
    }

    return this._orderingCoordinator.ensureOrderCanBePlaced();
  },

  /**
   * Places the order of the specified therapies.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} basketItems with therapies to order.
   * @returns {tm.jquery.Promise}
   */
  placeOrder: function(basketItems)
  {
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var medicationOrders = basketItems.map(function(basketItem){
      var saveMedicationOrder = new app.views.medications.ordering.dto.SaveMedicationOrder({
        therapy: basketItem.getTherapy(),
        actionEnum: basketItem.isRecordAdministration() ?
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER :
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
      });
      if (!!basketItem.getLinkedTherapy())
      {
        saveMedicationOrder.setLinkCompositionUid(basketItem.getLinkedTherapy().getCompositionUid());
      }
      return saveMedicationOrder;
    });

    var saveDateTime = this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();

    return this.getView()
        .getRestApi()
        .saveMedicationsOrder(
            medicationOrders,
            this._getPrescriber(),
            saveDateTime,
            this.getView().getPatientLastLinkNamePrefix(),
            false);
  },

  _loadPatientBaselineInfusionIntervals: function()
  {
    var self = this;
    var params = {patientId: this.patientId};
    var baselineTherapiesUrl =
        this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_BASELINE_THERAPIES;
    this.view.loadViewData(baselineTherapiesUrl, params, null, function(data)
    {
      self.baselineInfusionIntervals = data;
    });
  },

  _removeBaselineInfusion: function(therapy)
  {
    var baselineInfusionIntervalsLength = this.baselineInfusionIntervals.length;
    for (var i = 0; i < baselineInfusionIntervalsLength; i++)
    {
      var baselineInterval = this.baselineInfusionIntervals[i];
      if (baselineInterval.startMillis === therapy.start.getTime() &&
          (therapy.end == null && baselineInterval.endMillis == null) ||
          (baselineInterval.endMillis === therapy.end.getTime()))
      {
        this.baselineInfusionIntervals.splice(i, 1);
        break;
      }
    }
  },

  _refreshWarnings: function()
  {
    if (!!this.warningsContainer)
    {
      this.getWarningsContainer().refreshWarnings(this.basketContainer.getTherapies(), true);
    }
  },

  /**
   * Validates the input and places the order if successful.
   * @param {function} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var self = this;

    this.ensureOrderCanBePlaced()
        .then(
            function validationSuccessHandler(validatedBasketItems)
            {
              self.placeOrder(validatedBasketItems)
                  .then(successResultCallbackHandler, failureResultCallbackHandler);
            },
            failureResultCallbackHandler
    );

    function successResultCallbackHandler(){
      resultDataCallback(new app.views.common.AppResultData({success: true}));
    }

    function failureResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    }
  },

  /**
   * Overridable! Check usage!
   * @return {app.views.medications.ordering.OrderingContainer}
   * @protected
   */
  buildOrderingContainer: function()
  {
    var view = this.view;
    var self = this;

    return new app.views.medications.ordering.OrderingContainer({
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: this.isPastMode,
        informationSourceAvailable: true,
        templateOnlyMode: this.isPrescribeByTemplatesOnlyMode(),
        doseCalculationsAvailable: view.isDoseCalculationsEnabled(),
        medicationSearchResultFormatter: new app.views.medications.ordering.MedicationSearchInpatientResultFormatter()
      }),
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, app.views.medications.ordering.OrderingContainer.DEFAULT_WIDTH),
      templateContext: this.getTemplateContext(),
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION,
      referenceData: this.getReferenceData(),
      medicationRuleUtils: this.medicationRuleUtils,
      addTemplateTherapyOrdersCallback: function(therapyOrders)
      {
        self._fixTherapiesTimingAndAddToBasket(therapyOrders, false);
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      /**
       * @param {app.views.medications.ordering.ConfirmOrderEventData} confirmEventData
       * @return {boolean}
       */
      confirmOrderEventCallback: function(confirmEventData)
      {
        if (confirmEventData.isValidationPassed())
        {
          return self._addToBasket(confirmEventData.getTherapyOrder());
        }
        return false;
      },
      saveOrderToTemplateEventCallback: this.onOrderingContainerSaveOrderToTemplate.bind(this),
      getBasketTherapiesFunction: function()
      {
        return self.getBasketContainer().getTherapies();
      },
      refreshBasketFunction: function()
      {
        self.getBasketContainer().refreshWithExistingData();
      }
    });
  },

  /**
   * Event handler for {@link app.views.medications.ordering.OrderingContainer#saveOrderToTemplateEventCallback}.
   * @param {app.views.medications.ordering.SaveOrderToTemplateEventData|
   * app.views.medications.ordering.ConfirmOrderEventData} saveEventData
   * @protected
   */
  onOrderingContainerSaveOrderToTemplate: function(saveEventData)
  {
    var templateElement = app.views.medications.ordering.dto.TherapyTemplateElement
        .fromTherapyOrder(saveEventData.getTherapyOrder())
        .setTemplateStatus(saveEventData.isValidationPassed() ?
            app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE :
            app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE);
    this._openSaveTemplateDialog([templateElement], true);
  },

  /**
   * Overridable! Check usage!
   * @return {app.views.medications.ordering.BasketContainer}
   * @protected
   */
  buildBasketContainer: function()
  {
    var self = this;
    var view = this.view;

    return new app.views.medications.ordering.BasketContainer({
      view: view,
      headerTitle: view.getDictionary("therapy.list"),
      editOrderAllowed: !this.isPrescribeByTemplatesOnlyMode(),
      saveAsTemplateAllowed: !this.isPrescribeByTemplatesOnlyMode(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      therapyAddedEventCallback: function(options)
      {
        if (self.isWarningsEnabled())
        {
          if (options && options.forceNoRefreshWarnings)
          {
            // do nothing
          }
          else
          {
            self._refreshWarnings();
          }
        }
        self.getOrderingContainer().clear();
      },
      therapiesRemovedEventCallback: function(therapyContainers, options)
      {
        var therapies = [];
        therapyContainers.forEach(function(item)
        {
          therapies.push(item.getTherapy());
        });

        if (!!self.warningsContainer)
        {
          if (!!options && !!options.clearBasket)
          {
            self.getWarningsContainer().clear();
          }
          else
          {
            self._refreshWarnings();
          }
        }
        for (var i = 0; i < therapies.length; i++)
        {
          if (therapies[i].isBaselineInfusion())
          {
            self._removeBaselineInfusion(therapies[i]);
          }
        }
      },
      editTherapyEventCallback: function(therapyContainer)
      {
        self._orderingCoordinator
            .warnUserIfUnfinishedOrderExists()
            .then(self._editBasketTherapy.bind(self, therapyContainer));
      },
      saveTemplateEventCallback: this._onBasketContainerSaveToTemplate.bind(this)
    });
  },

  /**
   * Override if required. The context further refines the displayed templates when
   * {@link app.views.medications.ordering.OrderingBehaviour#filterTemplatesByActivePatient} is enabled, otherwise
   * {@link #getTemplateMode} is used.
   * @protected
   */
  getTemplateContext: function()
  {
    return app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT;
  },

  /**
   * Defines the type of templates to be saved and loaded, when loading all available templates. Normally we'd want
   * this in sync with the used context.
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   * @protected
   */
  getTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.mapTherapyTemplateContextToMode(this.getTemplateContext());
  },

  clear: function ()
  {
    this.orderingContainer.clear();
    this.basketContainer.clear();
    this.warningsContainer.clear();
  },

  /**
   * @return {app.views.medications.ordering.OrderingContainer}
   */
  getOrderingContainer: function()
  {
    return this.orderingContainer;
  },

  /**
   * @return {app.views.medications.ordering.BasketContainer}
   */
  getBasketContainer: function()
  {
    return this.basketContainer;
  },

  getWarningsContainer: function()
  {
    return this.warningsContainer;
  },

  /**
   * @returns {boolean}
   */
  isWarningsEnabled: function()
  {
    return this.warningsEnabled === true;
  },

  getDialogResultCallback: function()
  {
    return this.resultCallback;
  },

  /**
   * Should the prescribing be limited to template use only? In this mode, the user cannot prescribe anything outside
   * of the loaded templates, he can also not add a therapy to the order with edit, nor can he override any warnings or
   * save a new template.
   */
  isPrescribeByTemplatesOnlyMode: function()
  {
    return this.prescribeByTemplatesOnlyMode === true;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this._referenceData;
  }
});

