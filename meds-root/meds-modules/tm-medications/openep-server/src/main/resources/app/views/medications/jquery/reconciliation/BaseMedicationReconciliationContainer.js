Class.define('app.views.medications.reconciliation.BaseMedicationReconciliationContainer', 'tm.jquery.Container', {
  cls: "reconciliation-container",
  selectedCls: "item-selected",

  view: null,
  /** @type string|null */
  rightColumnTitle: null,
  /** @type boolean */
  warningsEnabled: true,
  /** @type boolean */
  viewActiveTherapiesAvailable: false,
  /** @type boolean */
  skipEmptyBasketCheck: false,
  baselineInfusionIntervals: null,
  basketTherapyDisplayProvider: null,
  includeInpatientTherapiesInWarningsSearch: true,
  medicationRuleUtils: null,
  templateContext: '',
  maxDosePercentageSum: 0,
  validateBaselineInfusions: false,

  /** @type app.views.medications.reconciliation.BasketContainer|app.views.medications.ordering.BasketContainer */
  _basketContainer: null,
  /** @type app.views.medications.reconciliation.TherapySelectionColumn */
  _therapySelectionContainer: null,
  /** @type app.views.medications.ordering.warnings.WarningsContainer|null */
  _warningsContainer: null,
  /** @type app.views.medications.ordering.OrderingBehaviour */
  _orderingBehaviour: null,
  _basketTherapyDisplayProvider: null,
  _referenceData: null,
  _variableDoseDialogFactory: null,
  _sourceGroupMap: null,
  /** @type app.views.medications.ordering.OrderingCoordinator */
  _orderingCoordinator: null,

  /**
   * This is the base class for all three versions of the medication reconciliation list. Since each step has it's
   * own ordering behaviour, it's desired to pass a new instance of the {@link OrderingBehaviour} to the constructor,
   * which in turn enables overriding the behaviour from concrete implementation classes.
   * @param {Object} config
   * @param {app.views.medications.ordering.OrderingBehaviour} orderingBehaviour
   * @param {app.views.medications.common.therapy.TherapyContainerDisplayProvider} basketTherapyDisplayProvider
   * @param {app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory} variableDoseDialogFactory
   * @constructor
   */
  Constructor: function (config, orderingBehaviour, basketTherapyDisplayProvider, variableDoseDialogFactory)
  {
    this.callSuper(config);
    this.baselineInfusionIntervals = [];
    this._referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()});
    this.medicationRuleUtils = this.getConfigValue(
        "medicationRuleUtils",
        new app.views.medications.MedicationRuleUtils({view: this.view, referenceData: this._referenceData}));
    this._variableDoseDialogFactory = variableDoseDialogFactory || null;

    if (!orderingBehaviour)
    {
      throw new Error('orderingBehaviour is undefined');
    }

    this._orderingBehaviour = orderingBehaviour;
    this._basketTherapyDisplayProvider = basketTherapyDisplayProvider ||
        new app.views.medications.common.therapy.TherapyContainerDisplayProvider({view: this.view});
    this._sourceGroupMap = {};

    this._buildGUI();

    this._orderingCoordinator = new app.views.medications.ordering.OrderingCoordinator({
      view: this.view,
      skipEmptyBasketCheck: this.skipEmptyBasketCheck,
      orderingContainer: this._therapySelectionContainer,
      warningsContainer: this._warningsContainer,
      basketContainer: this._basketContainer
    });
  },

  /**
   * Should the {@link #_basketContainer} header include a button, by which the user may see a dropdown list of currently
   * active (inpatient) therapies?
   * @return {boolean}
   * @protected
   */
  isViewActiveTherapiesAvailable: function()
  {
    return this.viewActiveTherapiesAvailable === true;
  },

  /**
   * Implement to add group panels above the template panels.
   * @protected
   * @param container
   */
  attachTherapyGroupPanels: function (container)
  {

  },

  /**
   * @see {app.views.medications.ordering.OrderingCoordinator#ensureOrderCanBePlaced}
   * @return {tm.jquery.Promise}
   * @protected
   */
  ensureOrderCanBePlaced: function()
  {
    return this._orderingCoordinator.ensureOrderCanBePlaced();
  },

  /**
   * Template method allowing additional content to be added to the order forms.
   * @return {Array<app.views.medications.ordering.PrescriptionContentExtensionContainer>}
   * @protected
   */
  onBuildPrescriptionContentExtensions: function()
  {
    return [];
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch"));
    this._buildLeftColumn();
    this._buildRightColumn();
  },

  _buildLeftColumn: function ()
  {
    var view = this.getView();

    this._therapySelectionContainer = new app.views.medications.reconciliation.TherapySelectionColumn({
      view: view,
      referenceData: this._referenceData,
      orderingBehaviour: this.getOrderingBehaviour(),
      templateContext: this.getTemplateContext(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, app.views.medications.ordering.OrderingContainer.DEFAULT_WIDTH),
      addTemplateTherapyOrdersCallback: this._onTherapySelectionColumnAddTemplateTherapyOrders.bind(this),
      confirmOrderEventCallback: this._onTherapySelectionColumnConfirmOrder.bind(this),
      saveOrderToTemplateEventCallback: this._onTherapySelectionColumnSaveOrderToTemplate.bind(this),
      getBasketTherapiesFunction: this.getBasketContents.bind(this),
      attachTherapyGroupPanels: this.attachTherapyGroupPanels.bind(this),
      buildPrescriptionContentExtensions: this.onBuildPrescriptionContentExtensions.bind(this),
      variableDoseDialogFactory: this.getVariableDoseDialogFactory()
    });

    this.add(this._therapySelectionContainer);
  },

  _buildRightColumn: function ()
  {
    var self = this;
    var view = this.getView();
    var therapyTemplatesSupported = this.getOrderingBehaviour().isAddToTemplateAvailable() &&
        !this.getOrderingBehaviour().isTemplateOnlyMode();

    var container = new tm.jquery.Container({
      cls: "right-column",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto") // ordering container preset
    });

    this._basketContainer = new app.views.medications.reconciliation.BasketContainer({
      view: this.getView(),
      headerTitle: this.getRightColumnTitle(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      displayProvider: this.getBasketTherapyDisplayProvider(),
      editOrderAllowed: !this.getOrderingBehaviour().isTemplateOnlyMode(),
      saveAsTemplateAllowed: therapyTemplatesSupported,
      viewActiveTherapiesAvailable: this.isViewActiveTherapiesAvailable(),
      therapyAddedEventCallback: function ()
      {
        self.onTherapyAddedToBasket();
      },
      therapiesRemovedEventCallback: this.onBasketContainerTherapiesRemoved.bind(this),
      editTherapyEventCallback: function (therapyContainer)
      {
        self._orderingCoordinator.warnUserIfUnfinishedOrderExists()
            .then(
                function triggerEditTherapy()
                {
                  self.getBasketContainer().removeTherapy(therapyContainer.getData().getTherapy());
                  self.onEditTherapy(therapyContainer);
                  if (!!self._warningsContainer)
                  {
                    self.refreshWarnings();
                  }
                });
      },
      saveTemplateEventCallback: this._onBasketContainerSaveToTemplate.bind(this)
    });

    container.add(this._basketContainer);


    if (this.isWarningsEnabled())
    {
      this._warningsContainer = new app.views.medications.ordering.warnings.WarningsContainer({
        view: view,
        referenceData: this._referenceData,
        flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
        overrideAllowed: !this.getOrderingBehaviour().isTemplateOnlyMode()
      });
      container.add(this._warningsContainer);
    }

    this.add(container);
  },

  /**
   * @param {app.views.medications.common.therapy.TherapyContainer} elementContainer
   * @private
   */
  _attachGroupPanelElementToolbar: function (elementContainer)
  {
    var self = this;
    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      suspendAvailable: false,
      cancelAvailable: false,
      addWithEditAvailable: !this.getOrderingBehaviour().isTemplateOnlyMode(),
      addToBasketEventCallback: function (therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onAddGroupPanelTherapyToBasket(therapyContainer);
      },
      addToBasketWithEditEventCallback: function (therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onEditTherapy(therapyContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  _removeBaselineInfusion: function(therapy)
  {
    if (tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals)) return;

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

  /**
   * Adds the therapy information to the baseline infusion interval array, if it exists and if the therapy is
   * a baseline infusion. The arrays is kept to ensure no interval intersection occurs.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _updateBaselineInfusionIntervals: function(therapy)
  {
    if (therapy.isBaselineInfusion() && !tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals))
    {
      this.baselineInfusionIntervals.push({
        startMillis: therapy.getStart().getTime(),
        endMillis: !!therapy.getEnd() ? therapy.getEnd().getTime() : null
      })
    }
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#addTemplateTherapyOrdersCallback},
   * triggered when the user adds a therapy from the templates container to the basket. If the therapy being added is
   * a baseline infusion therapy, ensures that it doesn't intersect with existing baseline infusions, then loads the
   * therapy's description from the API and creates a new order that is added to the basket.
   * @param {Array<app.views.medications.ordering.TherapyOrder>} therapyOrders
   * @private
   */
  _onTherapySelectionColumnAddTemplateTherapyOrders: function(therapyOrders)
  {
    var self = this;
    therapyOrders
        .forEach(
            function(therapyOrder)
            {
              var therapy = therapyOrder.getTherapy();
              self._updateGroupPanelTherapyTimings(therapy);

              if (this.isPrescriptionValid(therapy))
              {
                self.onBeforeAddTemplateTherapyOrder(therapy);
                this.getView()
                    .getRestApi()
                    .fillTherapyDisplayValues(therapy, true)
                    .then(
                        function onSuccess(updatedTherapy)
                        {
                          self._updateBaselineInfusionIntervals(updatedTherapy);
                          // clear the source container, we have no idea where it came from, could be a whole template..
                          self.getTherapySelectionContainer().setSourceTherapyContainer(null);
                          self.addToBasket(
                              therapyOrder
                                  .setTherapy(updatedTherapy));
                        }
                    );
              }
            },
            this);
  },

  /**
   * Event handler (callback) for {@link app.views.medications.ordering.OrderingContainer#confirmOrderEventCallback}. If the
   * validation had passed, we query the API for the therapy description, then update the baseline infusion data
   * and the order is added to the basket, otherwise we return a false to inform the caller that it was rejected.
   * @param {app.views.medications.ordering.ConfirmOrderEventData} confirmEventData
   * @return {boolean}
   * @private
   */
  _onTherapySelectionColumnConfirmOrder: function(confirmEventData)
  {
    var self = this;
    if (confirmEventData.isValidationPassed())
    {
      var therapyOrder = confirmEventData.getTherapyOrder();
      var validPrescription = this.isPrescriptionValid(therapyOrder.getTherapy());
      if (validPrescription)
      {
        this.getView()
            .getRestApi()
            .fillTherapyDisplayValues(therapyOrder.getTherapy(), true)
            .then(
                function afterConfirmOrderFillTherapyDisplayValues(updatedTherapy)
                {
                  self._updateBaselineInfusionIntervals(updatedTherapy);
                  therapyOrder.setTherapy(updatedTherapy);
                  self.addToBasket(therapyOrder);
                }
            );
      }
      return validPrescription;
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
  _onTherapySelectionColumnSaveOrderToTemplate: function(saveEventData)
  {
    var templateElement = app.views.medications.ordering.dto.TherapyTemplateElement
        .fromTherapyOrder(saveEventData.getTherapyOrder())
        .setTemplateStatus(saveEventData.isValidationPassed() ?
            app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE :
            app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE);
    this._openSaveTemplateDialog([templateElement], true);
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
   * Calling this method on an existing therapy will either update it's timings to the current date, or clear the start and
   * end time if {@link #getOrderingBehaviour} supports it. Intended to be used on therapies sourced from one of the group
   * panels of the selection column, since those are usually defined in the past and in a different ordering context.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @private
   */
  _updateGroupPanelTherapyTimings: function(therapy)
  {
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      therapy.rescheduleTherapyTimings(false);
    }
    else
    {
      therapy.setStart(null);
      therapy.setEnd(null);
    }
  },

  _baselineInfusionsIntersects: function (start, end)
  {
    if (tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals)) return false;

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
   * @param {app.views.medications.ordering.AbstractTherapyOrder|*} orderData
   * @param {app.views.medications.common.therapy.TherapyContainer} [sourceContainer=null]
   */
  addToBasket: function (orderData, sourceContainer)
  {
    sourceContainer = sourceContainer || this.getTherapySelectionContainer().getSourceTherapyContainer();

    if (sourceContainer != null)
    {
      this.markSourceTherapyContainer(sourceContainer);
    }
    this.getBasketContainer().addTherapy(orderData);
  },

  /**
   * Load and refresh the warnings for the therapies in the basket.
   */
  refreshWarnings: function()
  {
    this
        ._warningsContainer
        .refreshWarnings(this.getBasketContainer().getTherapies(), this.includeInpatientTherapiesInWarningsSearch);
  },

  /**
   * A template method executed just before a therapy from an template is sent to the API to have it's display values
   * filled and the new copy of the therapy is added to the basket. Allows modifying or setting a default value to such
   * therapy.
   * @param {app.views.medications.common.dto.Therapy} therapy
   */
  onBeforeAddTemplateTherapyOrder: function(therapy)
  {

  },

  /**
   * Event handler triggered by therapy group panels on the left, when the user clicks the button to add a therapy
   * from the panel to the basket container.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onAddGroupPanelTherapyToBasket: function(therapyContainer)
  {
    var self = this;

    therapyContainer.getToolbar().setEnabled(false, true); // prevent double clicks when the server lags

    var therapyCopy = therapyContainer.getData().getTherapy().clone();
    this._updateGroupPanelTherapyTimings(therapyCopy);

    if (this.isPrescriptionValid(therapyCopy))
    {
      this.getView()
          .getRestApi()
          .fillTherapyDisplayValues(therapyCopy, true)
          .then(
              function onSuccess(updatedTherapy)
              {
                // don't enable the toolbar once the order is added as some source therapies have it's toolbar disabled
                // after they appear in the basket
                therapyContainer.getToolbar().setEnabled(true, true);

                if (updatedTherapy.isBaselineInfusion() && !tm.jquery.Utils.isEmpty(self.baselineInfusionIntervals))
                {
                  self.baselineInfusionIntervals.push({
                    startMillis: updatedTherapy.getStart().getTime(),
                    endMillis: !!updatedTherapy.getEnd() ? updatedTherapy.getEnd().getTime() : null
                  })
                }

                var orderCopy = new app.views.medications.ordering.TherapyOrder({
                  therapy: updatedTherapy,
                  validationIssues: therapyContainer.getData().getValidationIssues().slice(0)
                });

                self.addToBasket(orderCopy, therapyContainer);
              }
          );
    }
    else
    {
      therapyContainer.getToolbar().setEnabled(true, true);
    }
  },

  /***
   * Triggered by the basket component once a therapy was successfully added.
   */
  onTherapyAddedToBasket: function()
  {
    this.getTherapySelectionContainer().showList();

    if (!!this._warningsContainer)
    {
      this.refreshWarnings();
    }
  },

  /**
   * Event handler for {@link app.views.medications.ordering.BasketContainer#therapiesRemovedEventCallback} callback,
   * executed by {@link #_basketContainer}. Called when removing any number of basket items from the order basket. If a
   * therapy is removed from the basket we want to update the baseline infusion data and the warnings related to basket
   * therapies. In case we're clearing the whole basket, we want to skip loading any warnings and simply clear them.
   * @protected
   */
  onBasketContainerTherapiesRemoved: function (removedElementsData, options)
  {
    for (var i = 0; i < removedElementsData.length; i++)
    {
      var elementData = removedElementsData[i];
      var therapy = elementData.getTherapy();
      if (therapy && therapy.isBaselineInfusion())
      {
        this._removeBaselineInfusion(therapy);
      }

      var sourceContainer = this.findSourceMedicationTherapyContainer(elementData.getSourceId());
      if (sourceContainer)
      {
        this.unmarkSourceTherapyContainer(sourceContainer);
      }
    }
    if (!!this._warningsContainer)
    {
      if (!!options && !!options.clearBasket)
      {
        this._warningsContainer.clear();
      }
      else
      {
        this.refreshWarnings();
      }
    }
  },

  /**
   * @return {boolean} true, if baseline infusions should be validated by {@link #isPrescriptionValid}.
   */
  isValidateBaselineInfusions: function()
  {
    return this.validateBaselineInfusions === true;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @return {boolean} true, if the given therapy is a valid prescription, otherwise false. Override to implement
   * any additional rules.
   * @protected
   */
  isPrescriptionValid: function (therapy)
  {
    var appFactory = this.getView().getAppFactory();

    if (this.isValidateBaselineInfusions())
    {
      if (!tm.jquery.Utils.isEmpty(this.baselineInfusionIntervals) &&
          therapy.isBaselineInfusion() && this._baselineInfusionsIntersects(therapy.getStart(), therapy.getEnd()))
      {
        appFactory.createWarningSystemDialog(
            this.getView().getDictionary("patient.already.has.baseline.infusion"),
            320,
            122)
            .show();
        return false;
      }
    }
    return true;
  },


  onEditTherapy: function (therapyContainer)
  {
    var data = therapyContainer.getData();
    var therapy = data.getTherapy();

    this.getTherapySelectionContainer()
        .editTherapyOrder(therapyContainer, therapy.isLinkedToAdmission());
  },

  /**
   * Removes the visual mark created by calling {@link #markSourceTherapyContainer}. Should be called when the therapy,
   * created based on the source medication of this container, is removed from the order basket.
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer
   */
  unmarkSourceTherapyContainer: function (therapyContainer)
  {
    var selectedCls = this.getSelectedCls();
    var oldCls;

    if (!!therapyContainer)
    {
      oldCls = tm.jquery.Utils.isEmpty(therapyContainer.getCls()) ? "" : therapyContainer.getCls();
      if (oldCls.contains(selectedCls))
      {
        therapyContainer.setCls(therapyContainer.getCls().replace(" " + selectedCls, ""));
        if (!!therapyContainer.getToolbar())
        {
          therapyContainer.getToolbar().setEnabled(true, true);
        }
      }
    }
  },

  /**
   * Visually mark the given container to indicate the order basket contains the a therapy created based on this source
   * medication.
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer
   */
  markSourceTherapyContainer: function (therapyContainer)
  {
    var selectedCls = this.getSelectedCls();
    var oldCls;

    if (!!therapyContainer && !therapyContainer.getCls().contains(selectedCls))
    {
      oldCls = therapyContainer.getCls();
      therapyContainer.setCls(tm.jquery.Utils.isEmpty(oldCls) ? selectedCls : (oldCls + " " + selectedCls));
      therapyContainer.applyCls(therapyContainer.getCls());
      if (!!therapyContainer.getToolbar())
      {
        therapyContainer.getToolbar().setEnabled(false, true);
      }
    }
  },

  /**
   * @param {string} sourceId
   * @return {app.views.medications.common.therapy.TherapyContainer|undefined}
   * @protected
   */
  findSourceMedicationTherapyContainer: function(sourceId)
  {
    return !!this._sourceGroupMap[sourceId] ?
        this.getTherapySelectionContainer()
            .getTherapyGroupPanelContentByGroupEnum(this._sourceGroupMap[sourceId])
            .find(
                function bySourceId(therapyContainer)
                {
                  var data = therapyContainer.getData();
                  return data instanceof app.views.medications.reconciliation.dto.SourceMedication &&
                      data.getId() === sourceId;
                }) :
        undefined;
  },

  /**
   * Maps the elements of the given source group to the {@link #_sourceGroupMap} object. The key is represented by the
   * source therapy's {@link app.views.medications.reconciliation.dto.SourceMedication#getId}, and the value represents the
   * {@link app.views.medications.reconciliation.dto.MedicationGroup#getGroupEnum}. The map is used to mark the basket items
   * with it's source.
   * @param {app.views.medications.reconciliation.dto.MedicationGroup} sourceGroup
   * @protected
   */
  mapSourceGroupTherapies: function(sourceGroup)
  {
    sourceGroup
        .getGroupElements()
        .filter(
            function hasId(sourceTherapy)
            {
              return !!sourceTherapy.getId()
            })
        .forEach(
            function addToMap(sourceTherapy)
            {
              this._sourceGroupMap[sourceTherapy.getId()] = sourceGroup.getGroupEnum();
            },
            this
        );
  },

  /* Getters, setters */
  getBasketContents: function ()
  {
    return this.getBasketContainer().getTherapies();
  },

  getView: function ()
  {
    return this.view;
  },

  getRightColumnTitle: function ()
  {
    return this.rightColumnTitle;
  },

  getBasketContainer: function ()
  {
    return this._basketContainer;
  },
  isWarningsEnabled: function()
  {
    return this.warningsEnabled === true;
  },
  getTherapySelectionContainer: function ()
  {
    return this._therapySelectionContainer;
  },
  /**
   * @return {app.views.medications.common.therapy.TherapyContainerDisplayProvider}
   */
  getBasketTherapyDisplayProvider: function()
  {
    return this._basketTherapyDisplayProvider;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this._orderingBehaviour;
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
   * @return {app.views.medications.ordering.dosing.AbstractVariableDoseDialogFactory|null}
   */
  getVariableDoseDialogFactory: function()
  {
    return this._variableDoseDialogFactory;
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
   * @return {string}
   * @protected
   */
  getSelectedCls: function()
  {
    return this.selectedCls;
  }
});
