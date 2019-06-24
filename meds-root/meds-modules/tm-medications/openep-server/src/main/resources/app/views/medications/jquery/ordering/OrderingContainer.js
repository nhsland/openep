Class.define('app.views.medications.ordering.OrderingContainer', 'tm.jquery.Container', {
  statics: {
    DEFAULT_WIDTH: '720px'
  },
  cls: "ordering-container",
  /** configs */
  view: null,
  /** @type function(app.views.medications.ordering.ConfirmOrderEventData) */
  confirmOrderEventCallback: null,
  saveDateTimePaneEvent: null,
  /** @type function(Array<app.views.medications.ordering.TherapyOrder>) */
  addTemplateTherapyOrdersCallback: null,
  /** @type function(app.views.medications.ordering.SaveOrderToTemplateEventData) */
  saveOrderToTemplateEventCallback: null,
  presetDate: null, //optional
  additionalMedicationSearchFilter: null, //optional
  getBasketTherapiesFunction: null,
  refreshBasketFunction: null,
  templateContext: null,
  medicationRuleUtils: null,
  preventUnlicensedMedicationSelection: false,
  /** privates: components */
  header: null,
  cardContainer: null,

  orderingBehaviour: null,
  referenceData: null,

  therapySelectionCard: null,
  searchContainer: null,
  templatesContainer: null,
  simpleTherapyContainer: null,
  complexTherapyContainer: null,

  variableDoseDialogFactory: null,

  _oxygenTherapyContainer: null,
  _therapyMedicationDataLoader: null,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  _availableInformationSources: null,
  /** @type Array<app.views.medications.common.dto.InformationSource>|null */
  _defaultInformationSources: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.orderingBehaviour = !!this.orderingBehaviour ?
        this.orderingBehaviour :
        new app.views.medications.ordering.OrderingBehaviour({
          doseCalculationsAvailable: this.getView().isDoseCalculationsEnabled()
        });

    this.referenceData = !!this.referenceData ?
        this.referenceData :
        new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()});

    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({view: this.view, referenceData: this.getReferenceData()}));

    this._therapyMedicationDataLoader = new app.views.medications.common.TherapyMedicationDataLoader({
      view: this.getView()
    });

    this._availableInformationSources = this._buildAvailableTherapyInformationSources();

    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function ()
  {
    var self = this;
    this.header = this.createHeaderContainer();

    // setting flex grow to 3, but shrink only to 1 - we want the card container to always retain more size
    // than the warning container underneath
    this.cardContainer = (tm.jquery.ClientUserAgent.isIPad()) ?
        new tm.jquery.CardContainer({
          flex: tm.jquery.flexbox.item.Flex.create(4, 1, "auto")
        }) :
        new tm.jquery.SimpleCardContainer({
          prerendering: false,
          optimized: false,
          flex: tm.jquery.flexbox.item.Flex.create(4, 1, "auto")});

    this.therapySelectionCard = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    });

    if (!this.getOrderingBehaviour().isTemplateOnlyMode())
    {
      this.searchContainer = new app.views.medications.ordering.SearchContainer({
        view: this.view,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        additionalFilter: this.additionalMedicationSearchFilter,
        universalOrderFormAvailable: this.getOrderingBehaviour().isUniversalOrderFormAvailable(),
        searchResultFormatter: this.getOrderingBehaviour().getMedicationSearchResultFormatter(),
        medicationSelectedEvent: function(medicationData)
        {
          self._handleMedicationSelected(medicationData);
        }
      });
    }

    this.templatesContainer = new app.views.medications.ordering.templates.TemplatesContainer({
      cls: "templates-container",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      templateContext: this.templateContext,
      referenceData: this.getReferenceData(),
      orderingBehaviour: this.getOrderingBehaviour(),
      addTemplateTherapyOrdersCallback: this._onTemplatesContainerAddTemplateTherapyOrders.bind(this),
      addTemplateTherapyOrderWithEditCallback: this._onTemplatesContainerAddTemplateTherapyOrderWithEdit.bind(this)
    });

    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      orderingBehaviour: this.getOrderingBehaviour(),
      medicationRuleUtils: self.medicationRuleUtils,
      referenceData: this.getReferenceData(),
      availableInformationSources: this._availableInformationSources,
      variableDoseDialogFactory: this.getVariableDoseDialogFactory(),
      changeCardEvent: function(data)
      {
        if (data === 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.complexTherapyContainer.setMedicationDataBySearch(data);
          self.cardContainer.setActiveItem(self.complexTherapyContainer);
        }
      },
      saveOrderToTemplateEventCallback: this.saveOrderToTemplateEventCallback.bind(this),
      confirmOrderEventCallback: this.confirmOrderEventCallback.bind(this),
      saveDateTimePaneEvent: this.saveDateTimePaneEvent.bind(this)
    });
    this.simpleTherapyContainer.setContentExtensions(this.buildPrescriptionContentExtensions(this.simpleTherapyContainer));

    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      startProcessOnEnter: true,
      additionalMedicationSearchFilter: self.additionalMedicationSearchFilter,
      orderingBehaviour: this.getOrderingBehaviour(),
      medicationRuleUtils: self.medicationRuleUtils,
      preventUnlicensedMedicationSelection: this.preventUnlicensedMedicationSelection,
      referenceData: this.getReferenceData(),
      availableInformationSources: this._availableInformationSources,
      changeCardEvent: function(data)
      {
        if (data === 'TEMPLATES')
        {
          self.clear();
        }
        else
        {
          self.simpleTherapyContainer.setMedicationDataBySearch(data);
          self.cardContainer.setActiveItem(self.simpleTherapyContainer);
        }
        this.clear();
      },
      saveOrderToTemplateEventCallback: this.saveOrderToTemplateEventCallback.bind(this),
      confirmOrderEventCallback: function (confirmEventData)
      {
        var result = self.confirmOrderEventCallback(confirmEventData);
        if (result)
        {
          this.clear();
        }
        return result;
      },
      saveDateTimePaneEvent: function ()
      {
        self.saveDateTimePaneEvent();
      },
      getBasketTherapiesFunction: function ()
      {
        return self.getBasketTherapiesFunction();
      },
      refreshBasketFunction: function ()
      {
        self.refreshBasketFunction();
      }
    });
    this.complexTherapyContainer.setContentExtensions(this.buildPrescriptionContentExtensions(this.complexTherapyContainer));

    this._oxygenTherapyContainer = new app.views.medications.ordering.OxygenTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.view,
      orderingBehaviour: this.getOrderingBehaviour(),
      availableInformationSources: this._availableInformationSources,
      confirmOrderEventCallback: function (confirmEventData)
      {
        var result = self.confirmOrderEventCallback(confirmEventData);
        if (result)
        {
          this.clear();
        }
        return result;
      },
      saveOrderToTemplateEventCallback: this.saveOrderToTemplateEventCallback.bind(this)
    });

    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT,
        function(component)
        {
          self.clear();
          component.clear();
        });
    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE,
        function()
        {
          self.saveDateTimePaneEvent();
        });

  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this.buildTherapySelectionCardContent();

    this.cardContainer.add(this.therapySelectionCard);
    this.cardContainer.add(this.simpleTherapyContainer);
    this.cardContainer.add(this.complexTherapyContainer);
    this.cardContainer.add(this._oxygenTherapyContainer);

    if (!tm.jquery.Utils.isEmpty(this.header))
    {
      this.add(this.header);
    }
    this.add(this.cardContainer);
  },

  /**
   * @return {Array<app.views.medications.common.dto.InformationSource>} the list of available information sources
   * that can be applied to a prescribing therapy, based on the {@link #templateContext}. The list is provided
   * to all our ordering forms since they don't have any notion of the ordering container's template context.
   * @private
   */
  _buildAvailableTherapyInformationSources: function()
  {
    return this.getOrderingBehaviour().isInformationSourceAvailable() ?
        this.getView()
            .getInformationSourceHolder()
            .getSources(
                new app.views.medications.ordering.InformationSourceFilterBuilder()
                    .setTemplateContext(this.templateContext)
                    .build()) :
        [];
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _handleMedicationSelected: function(medicationData)
  {
    var self = this;
    var view = this.getView();

    if (this.preventUnlicensedMedicationSelection && medicationData.isUnlicensedMedication())
    {
      if (!this.searchContainer)
      {
        throw new Error('Unable to abort unlicensed medication selection if search field is missing.');
      }
      this.searchContainer.abortUnlicensedMedicationSelection();
      return;
    }

    if (!medicationData.isValid())
    {
      view.getAppNotifier().warning(
          view.getDictionary('medication.invalid.warning'),
          app.views.common.AppNotifierDisplayType.HTML,
          370,
          180);
      return;
    }

    if (medicationData.getMedication().isOxygen())
    {
      this.cardContainer.setActiveItem(this._oxygenTherapyContainer);
      setTimeout(function yieldToPaint()
      {
        self._oxygenTherapyContainer.setMedicationData(medicationData);
        self._oxygenTherapyContainer.setChangeReasonAvailable(false);
        self._oxygenTherapyContainer.setTherapyStart(
            app.views.medications.MedicationTimingUtils.getTimestampRoundedUp(
                CurrentTime.get(),
                5)
        ); // preset by design
      }, 0);
    }
    else if (medicationData.getDoseForm() &&
        medicationData.getDoseForm().getMedicationOrderFormType() ===
        app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX)
    {
      self.complexTherapyContainer.setMedicationDataBySearch(medicationData);
      this.cardContainer.setActiveItem(this.complexTherapyContainer);
      setTimeout(function yieldToPaint()
      {
        self.complexTherapyContainer.setChangeReasonAvailable(false);
      }, 0);
    }
    else
    {
      this.cardContainer.setActiveItem(this.simpleTherapyContainer);
      // Use timeout so setting data to components is pushed to the end of call stack and happens after
      // render event (which has it's own zero timeouts) is finished
      setTimeout(
          function()
          {
            self.simpleTherapyContainer.setMedicationDataBySearch(medicationData);
            self.simpleTherapyContainer.setChangeReasonAvailable(false);
          }, 0
      );
    }
  },

  /**
   * Event handler for {@link app.views.medications.ordering.templates.TemplatesContainer#addTemplateTherapyOrdersCallback},
   * allowing us to apply additional mutation and checks before delegating the event to the parent.
   * @param {Array<app.views.medications.ordering.TherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} therapyOrders
   * @private
   */
  _onTemplatesContainerAddTemplateTherapyOrders: function(therapyOrders)
  {
    if (!this.orderingBehaviour.isInformationSourceAvailable() || !this.orderingBehaviour.isInformationSourceRequired())
    {
      this.addTemplateTherapyOrdersCallback(therapyOrders);
      return;
    }

    var self = this;
    this._ensureDefaultInformationSourceDefined()
        .then(function applyDefaultSourceAndContinue()
        {
          self._applyTherapyOrderInformationSources(therapyOrders, self._defaultInformationSources);
          self.addTemplateTherapyOrdersCallback(therapyOrders);
        });
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @private
   */
  _onTemplatesContainerAddTemplateTherapyOrderWithEdit: function(therapyContainer)
  {
    this.editTherapyOrder(therapyContainer, false);
  },

  /**
   * @param {app.views.medications.ordering.TherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} therapyOrder
   * @return {boolean}
   * @private
   */
  _isTherapyOrderInformationSourceMissing: function(therapyOrder)
  {
    return therapyOrder.getTherapy().getInformationSources().length === 0;
  },

  /**
   * @param {Array<app.views.medications.ordering.TherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} therapyOrders
   * @param {Array<app.views.medications.common.dto.InformationSource>} sources
   * @private
   */
  _applyTherapyOrderInformationSources: function(therapyOrders, sources)
  {
    therapyOrders.forEach(function applyInformationSourcesToOrder(therapyOrder)
    {
      therapyOrder.getTherapy().setInformationSources(sources.slice());
    });
  },

  /**
   * Returns a promise that is resolved when the {@link _defaultInformationSources} is set. Either imidiately or
   * when the user confirms the selection via the displayed dialog.
   * @return {tm.jquery.Promise}
   * @private
   */
  _ensureDefaultInformationSourceDefined: function()
  {
    if (this._defaultInformationSources && this._defaultInformationSources.length > 0)
    {
      return tm.jquery.Deferred.create()
          .resolve()
          .promise();
    }

    var informationSourceDataEntryContainer =
        new app.views.medications.ordering.InformationSourceSelectionDataEntryContainer({
          view: this.view,
          availableInformationSources: this._availableInformationSources
        });

    var deferred = tm.jquery.Deferred.create();
    var self = this;
    var dialog =
        this.view
            .getAppFactory()
            .createDataEntryDialog(
                this.view.getDictionary('source'),
                null,
                informationSourceDataEntryContainer,
                /** @param {app.views.common.AppResultData} resultData */
                function onDialogResult(resultData)
                {
                  if (!tm.jquery.Utils.isEmpty(resultData) && resultData.isSuccess())
                  {
                    self._defaultInformationSources = resultData.getValue().slice();
                    deferred.resolve();
                  }
                },
                450,
                200
            );

    dialog.show();
    return deferred.promise();
  },

  buildPrescriptionContentExtensions: function()
  {
    return [];
  },

  /** public methods */
  clear: function ()
  {
    this.cardContainer.setActiveItem(this.therapySelectionCard);
    if (!!this.searchContainer)
    {
      this.searchContainer.clear();
    }
  },

  unfinishedOrderExists: function ()
  {
    return this.cardContainer.getActiveItem() && this.getCardContainerActiveItemContent() !== this.therapySelectionCard;
  },

  /**
   * Opens the appropriate order form preloaded with data from the given therapy container. Intended for 'edit' and
   * 'add with edit' operations. Does not mutate the data in case the user cancels the operation.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   * @param {boolean} [changeReasonAvailable=false]
   */
  editTherapyOrder: function(therapyContainer, changeReasonAvailable)
  {
    var view = this.getView();
    // Cloning the order data and in turn the therapy to prevent changes on the original data in case the user cancels
    // the operation. One such scenario is in the medication reconciliation dialogs.
    var orderDataCopy = therapyContainer.getData().cloneForEdit(this.getOrderingBehaviour().isStartEndTimeAvailable());
    var therapyCopy = orderDataCopy.getTherapy();
    var self = this;

    if (this.orderingBehaviour.isInformationSourceRequired() &&
        this._isTherapyOrderInformationSourceMissing(orderDataCopy) &&
        this._defaultInformationSources)
    {
      therapyCopy.setInformationSources(this._defaultInformationSources.slice());
    }

    this._therapyMedicationDataLoader.load(therapyCopy).then(function onDataLoad(medicationData)
    {
      if (self._therapyMedicationDataLoader.isMedicationNoLongerAvailable(therapyCopy, medicationData))
      {
        // make sure it's still active
        var message = view.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            view.getDictionary('stop.therapy.order.alternative.medication');
        view.getAppFactory().createWarningSystemDialog(message, 500, 160).show();
        return;
      }
      if (therapyCopy.isOrderTypeOxygen())
      {
        self.cardContainer.setActiveItem(self._oxygenTherapyContainer);
        setTimeout(function yieldToPaint()
        {
          self._oxygenTherapyContainer.setMedicationData(medicationData);
          self._oxygenTherapyContainer.setChangeReasonAvailable(changeReasonAvailable === true);
          self._oxygenTherapyContainer.setOxygenTherapyFromOrder(orderDataCopy);
        }, 0);
      }
      else if (therapyCopy.isOrderTypeComplex())
      {
        self.complexTherapyContainer.clear();
        self.complexTherapyContainer.setMedicationDataFromTherapy(therapyCopy, medicationData);
        self.cardContainer.setActiveItem(self.complexTherapyContainer);
        setTimeout(function yieldToPaint()
        {
          self.complexTherapyContainer.setChangeReasonAvailable(changeReasonAvailable === true);
          self.complexTherapyContainer.setComplexTherapyFromOrder(orderDataCopy);
        }, 0);
      }
      else
      {
        self.cardContainer.setActiveItem(self.simpleTherapyContainer);
        setTimeout(
            function()
            {
              self.simpleTherapyContainer.setChangeReasonAvailable(changeReasonAvailable === true);
              self.simpleTherapyContainer.setSimpleTherapyFromOrder(orderDataCopy, medicationData);
            },
            0
        );
      }
    });
  },

  presetMedication: function (medicationId)
  {
    var self = this;

    this.getView().getRestApi().loadMedicationData(medicationId).then(function onSuccessHandler(medicationData){
      if (self.isRendered())
      {
        self._handleMedicationSelected(medicationData);
      }
    });
  },

  reloadTemplates: function ()
  {
    this.templatesContainer.reloadTemplates();
  },

  /**
   * @return {app.views.medications.ordering.dto.TherapyTemplates}
   */
  getTemplates: function ()
  {
    return this.templatesContainer.getTemplates();
  },

  getTherapySelectionCard: function()
  {
    return this.therapySelectionCard;
  },

  /**
   * Returns the title text for the top header. {@see #createHeaderContainer} for more information.
   * @return {string}
   */
  createHeaderTitleText: function()
  {
    var view = this.getView();
    return !!view.getPresetDate() ?
        tm.jquery.Utils.formatMessage('{0} {0}',
            view.getDictionary('therapy.order'),
            view.getDisplayableValue(new Date(view.getPresetDate()), 'short.date.time')) :
        view.getDictionary('therapy.order');
  },


  /**
   * Creates the header title container, which displays the {@link #createHeaderTitleText} on the left side and
   * the patient's reference data (weight, surface), used for calculation, on the right side.
   * Override if you need different content.
   * @returns {tm.jquery.Container|app.views.medications.ordering.MedicationsTitleHeader}
   */
  createHeaderContainer: function ()
  {
    var patientDataContainer = new tm.jquery.Container({
      cls: 'TextLabel',
      testAttribute: 'patient-data-container',
      horizontalAlign: 'right',
      layout: tm.jquery.HFlexboxLayout.create('flex-end', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    patientDataContainer.add(new tm.jquery.Component({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: app.views.medications.MedicationUtils.createPatientsReferenceWeightAndHeightHtml(this.getView())
    }));

    return new app.views.medications.ordering.MedicationsTitleHeader({
      view: this.view,
      title: this.createHeaderTitleText(),
      additionalDataContainer: patientDataContainer
    });
  },

  /***
   * Override to add content to the therapy selection card.
   * {@see app.views.medications.reconciliation.BaseMedicationReconciliationContainer}.
   */
  buildTherapySelectionCardContent: function()
  {
    var contentScrollContainer = new tm.jquery.Container({
      cls: "content-scroll-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      scrollable: 'vertical',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    contentScrollContainer.add(this.templatesContainer);

    if (!!this.searchContainer)
    {
      this.therapySelectionCard.add(this.searchContainer);
    }
    this.therapySelectionCard.add(contentScrollContainer);
  },

  /**
   * Sets the information sources that will be pre-applied when an individual order form is cleared (which occurs each time
   * an order form looses it's visibility and becomes inactive) or immediately if the order form is currently not visible
   * and thus considered inactive. This is a safeguard against accidentally overwriting the existing (active) selection.
   * @param {Array<app.views.medications.common.dto.InformationSource>} source
   */
  setDefaultInformationSources: function(source)
  {
    this._defaultInformationSources = source;

    var activeItem = this.getCardContainerActiveItemContent();
    [this.simpleTherapyContainer, this.complexTherapyContainer, this._oxygenTherapyContainer].forEach(
        function setDefaultInformationSourceToOrderForm(orderForm)
        {
          orderForm.setDefaultInformationSources(
              tm.jquery.Utils.isArray(source) ? source.slice() : [],
              orderForm !== activeItem);
        });
  },

  getCardContainerActiveItemContent: function()
  {
    var cardContainer = this.cardContainer;
    return cardContainer instanceof tm.jquery.SimpleCardContainer ?
        cardContainer.getActiveItem().getContent() :
        cardContainer instanceof tm.jquery.CardContainer ? cardContainer.getActiveItem() : null
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @return {tm.views.medications.TherapyView}
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
    return this.referenceData;
  },

  /**
   * @return {app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory|null}
   */
  getVariableDoseDialogFactory: function()
  {
    return this.variableDoseDialogFactory;
  }
});
