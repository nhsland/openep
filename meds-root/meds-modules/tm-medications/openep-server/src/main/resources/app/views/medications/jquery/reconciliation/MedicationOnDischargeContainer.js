Class.define('app.views.medications.reconciliation.MedicationOnDischargeContainer', 'app.views.medications.reconciliation.MedicationOnAdmissionContainer', {
  /** @type boolean */
  groupPanelsAttached: false,
  /** @type boolean */
  warningsEnabled: true,
  /** @type boolean */
  skipEmptyBasketCheck: true, // otherwise we cannot undo prescriptions made in error after we save the list
  abortedTherapiesPanel: null,
  /** @type string of {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum} */
  templateContext: app.views.medications.TherapyEnums.therapyTemplateContextEnum.DISCHARGE,
  dispenseSources: null,
  _defaultDispenseSource: null,

  Constructor: function (config)
  {
    this.callSuper(config,
        new app.views.medications.ordering.OrderingBehaviour({
          supplyRequired: false,
          supplyAvailable: true,
          startEndTimeAvailable: false,
          oxygenSaturationAvailable: false,
          reviewReminderAvailable: false,
          templateOnlyMode: config.prescribeByTemplatesOnlyMode === true,
          doseCalculationsAvailable: config.view.isDoseCalculationsEnabled()
          /* no other method to pass this via constructor */
        }),
        new app.views.medications.reconciliation.MedicationOnDischargeTherapyContainerDisplayProvider({view: config.view})
    );

    if (!tm.jquery.Utils.isArray(this.dispenseSources))
    {
      throw new Error('dispenseSources not defined');
    }
    this._defaultDispenseSource =
        this.getDispenseSources().find(app.views.medications.common.dto.DispenseSource.matchDefault);
    this.getBasketTherapyDisplayProvider().setShowChangeReason(true);
    this.getBasketTherapyDisplayProvider().setShowChangeHistory(false);
  },

  /**
   * @return {Array<app.views.medications.common.dto.DispenseSource>}
   */
  getDispenseSources: function()
  {
    return this.dispenseSources;
  },

  getSourceTherapiesPanelTitle: function ()
  {
    return this.getView().getDictionary("inpatient.prescription");
  },

  /**
   * Returns the desired column title to the base class.
   * @Override
   * @return {String}
   */
  getRightColumnTitle: function ()
  {
    return this.getView().getDictionary("discharge.prescription");
  },

  /**
   * @return {app.views.medications.common.TherapyGroupPanel}
   */
  getAbortedTherapiesPanel: function()
  {
    return this.abortedTherapiesPanel;
  },

  /**
   * @return {Array<app.views.medications.ordering.PrescriptionContentExtensionContainer>}
   * @override
   * @protected
   */
  onBuildPrescriptionContentExtensions: function()
  {
    return [
      new app.views.medications.reconciliation.DispenseSourcePrescriptionContentExtensionContainer({
        view: this.getView(),
        dispenseSources: this.getDispenseSources()
      })
    ];
  },

  /**
   * @param {tm.jquery.Container} parentContainer
   * @override to implement the creation of source group panels, along with the stopped group.
   */
  attachTherapyGroupPanels: function(parentContainer)
  {
    var self = this;
    var view = this.getView();
    view
        .getRestApi()
        .loadTherapyGroupsOnDischarge()
        .then(
            /** @param {Array<app.views.medications.reconciliation.dto.MedicationOnDischargeGroup>} groups */
            function displaySourceDischargeMedication(groups)
            {
              self._createAndAddDischargeGroupTherapyPanels(parentContainer, groups);
              parentContainer.repaint();

              view.getAppFactory().createConditionTask(
                  function()
                  {
                    self.groupPanelsAttached = true;
                  },
                  function()
                  {
                    return parentContainer.isRendered(true);
                  },
                  250, 100
              );
            }
        );
  },

  /**
   * Wraps the order data into an instance of {@link app.views.medications.reconciliation.dto.MedicationOnDischarge}.
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData|*} orderData
   * @param {app.views.medications.common.therapy.TherapyContainer} sourceContainer
   * @Override
   */
  addToBasket: function (orderData, sourceContainer)
  {
    var self = this;
    var statusEnums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    var actionEnum = statusEnums.PRESCRIBED;
    var sourceData = null;
    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer)
    {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
      // if the source container was not defined, but we found it, edit was conducted
      if (sourceContainer)
      {
        actionEnum = statusEnums.EDITED_AND_PRESCRIBED;
      }
    }

    if (sourceContainer) {
      sourceData = sourceContainer.getData();
    }

    // if we're reading a basket item, reroute the source container
    if (sourceData instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge)
    {
      // preserve the composition uid when editing basket items, otherwise a new therapy is created and any established
      // links will be destroyed.
      orderData.getTherapy().setCompositionUid(sourceData.getTherapy().getCompositionUid());
      // since the aborted therapies panel also contains basket items, make sure to remove it there before we reroute
      this.getAbortedTherapiesPanel().removeByData(sourceData);
      if (!!sourceData.getSourceId())
      {
        sourceContainer = this.findSourceMedicationTherapyContainer(sourceData.getSourceId());
        self.getTherapySelectionContainer().setSourceTherapyContainer(sourceContainer);
        if (!!sourceContainer)
        {
          sourceData = sourceContainer.getData();
        }
      }
    }

    var basketItem = new app.views.medications.reconciliation.dto.MedicationOnDischarge({
      therapy: orderData.getTherapy(),
      status: actionEnum,
      changeReasonDto: orderData.getTherapyChangeReason(),
      validationIssues: orderData.getValidationIssues()
    });
    // noinspection JSCheckFunctionSignatures since we're working with an extended jsClass
    this.attachClientSideValidationIssues(basketItem);

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.DischargeSourceMedication)
    {
      basketItem.setSourceGroupEnum(sourceContainer.getGroupPanel().getGroupId());
      basketItem.setSourceId(sourceData.getId());
      this.markSourceTherapyContainer(sourceContainer);
    }

    this.getBasketContainer().addTherapy(basketItem);
  },

  /**
   *
   * Save the contents of the basket as the new medication on discharge list. Since some of the therapies are in the aborted
   * group panel, we have to merge the contents with the basket container contents to get the final state of the discharge
   * medication list.
   * @override
   * @return {tm.jquery.Promise}
   */
  saveBasketContent: function ()
  {
    var view = this.getView();
    // abort orders shouldn't be validated as they might be invalid/incomplete, which is sufficient for aborting
    var abortedMedicationsOnDischarge = this._getAbortedMedicationOnAdmission();
    var deferred = tm.jquery.Deferred.create();

    this.ensureOrderCanBePlaced()
        .then(
            function saveMedicationOnDischarge(validatedBasketItems)
            {
              var combinedOrderList = jQuery.merge(validatedBasketItems, abortedMedicationsOnDischarge);
              view.getRestApi()
                  .saveMedicationOnDischarge(combinedOrderList)
                  .then(
                      function onSaveMedicationOnDischargeSuccess()
                      {
                        deferred.resolve();
                      },
                      function onSaveMedicationOnDischargeError()
                      {
                        deferred.reject();
                      });
            },
            function saveMedicationOnDischargeUserCancellation()
            {
              deferred.reject();
            });

    return deferred.promise();
  },

  /**
   * @Override
   */
  loadBasketContents: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = this.getView().getAppFactory();

    view
        .getRestApi()
        .loadMedicationsOnDischarge(true)
        .then(
            function onDataLoad(items)
            {
              var abortedItems = [];
              for (var idx = items.length - 1; idx >= 0; idx--)
              {
                if (items[idx].isAborted())
                {
                  abortedItems.push(items[idx]);
                  items.splice(idx, 1);
                }
              }
              abortedItems.reverse();
              appFactory.createConditionTask(  //wait for the medications to load
                  function()
                  {

                    self.getBasketContainer().setBasketItems(items);
                    abortedItems.forEach(
                        function addToAbortedPanel(item)
                        {
                          self.getAbortedTherapiesPanel().addElement(item);
                        });
                    self.markSourceTherapies(items.concat(abortedItems));
                    if (self.isWarningsEnabled() && items.length > 0)
                    {
                      self.refreshWarnings();
                    }
                  },
                  function waitForPanelAndRender()
                  {
                    return self.isGroupPanelsAttached() && self.isRendered(true);
                  },
                  250, 100
              );
            });
  },

  /**
   * @override and attach the default dispense source before sending the therapy to the API to have it's display values
   * filled and added to the order basket.
   * @param {app.views.medications.common.dto.Therapy} therapy
   */
  onBeforeAddTemplateTherapyOrder: function(therapy)
  {
    if (!!this._defaultDispenseSource &&
        (!therapy.getDispenseDetails() || !therapy.getDispenseDetails().getDispenseSource()))
    {
      if (!therapy.getDispenseDetails())
      {
        therapy.setDispenseDetails(new app.views.medications.common.dto.DispenseDetails());
      }
      therapy
          .getDispenseDetails()
          .setDispenseSource(this._defaultDispenseSource);
    }
  },

  /**
   * @Override
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onAddGroupPanelTherapyToBasket: function(therapyContainer)
  {
    var self = this;
    var therapy = therapyContainer.getData().therapy;

    therapyContainer.getToolbar().setEnabled(false, true); // prevent double clicks when the server lags

    // clear start and end since it's not supported in case the container is from one of the group panels
    therapy.setStart(null);
    therapy.setEnd(null);

    if (therapy.isOrderTypeOxygen())
    {
      therapy.setMaxTargetSaturation(null);
      therapy.setMinTargetSaturation(null);
    }

    if (this.isPrescriptionValid(therapy))
    {
      this.getView()
          .getRestApi()
          .fillTherapyDisplayValues(therapy, true)
          .then(
              function onSuccess(updatedTherapy)
              {
                // don't enable the toolbar once the order is added as some source therapies have it's toolbar disabled
                // after they appear in the basket
                therapyContainer.getToolbar().setEnabled(true, true);

                self.addToBasket(
                    new app.views.medications.ordering.TherapyOrder({
                      therapy: updatedTherapy,
                      validationIssues: therapyContainer.getData().getValidationIssues().slice(0)
                    }),
                    therapyContainer);
              }
          );
    }
    else
    {
      therapyContainer.getToolbar().setEnabled(true, true);
    }
  },

  /**
   * @Override
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onAbortTherapy: function(therapyContainer){
    var changeTypeEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
    var self = this;
    var view = this.getView();

    var changeReasonEntryContainer = new app.views.medications.common.ChangeReasonDataEntryContainer({
      titleIcon: "warningYellow_status_48.png",
      titleText: view.getDictionary("therapy.needs.reason.for.stopping"),
      view: view,
      changeReasonTypeKey: changeTypeEnums.ABORT
    });

    var abortConfirmationDialog = this.getView().getAppFactory().createDataEntryDialog(
        view.getDictionary("warning"),
        null,
        changeReasonEntryContainer, function(resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            self._abortTherapy(therapyContainer, resultData.value);
          }
        },
        changeReasonEntryContainer.defaultWidth,
        changeReasonEntryContainer.defaultHeight
    );
    abortConfirmationDialog.show();
  },

  /**
   * @override to ensure the source container is visible in case the user decide to abort or suspend the therapy, then
   * changed his mind and added it to the basket.
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer
   */
  markSourceTherapyContainer: function(therapyContainer)
  {
    this.callSuper(therapyContainer);
    therapyContainer.show();
  },

  /**
   * @override to ensure all unmarked containers become visible. This is required in case the source medication on admission
   * was aborted, which places a copy of it in the aborted group panel.
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  unmarkSourceTherapyContainer: function(therapyContainer)
  {
    this.callSuper(therapyContainer);
    therapyContainer.show();
  },

  /**
   * Creates a new therapy group panel for each given group and adds the component to the given container. The panel
   * made for the aborted medication on admission therapies is also set to {@link #abortedTherapiesPanel} for further
   * access.
   * @param {tm.jquery.Container} parentContainer to attach the panels to.
   * @param {Array<app.views.medications.reconciliation.dto.MedicationOnDischargeGroup|
   * app.views.medications.reconciliation.dto.MedicationGroup>} groups from which to build panels.
   * @private
   */
  _createAndAddDischargeGroupTherapyPanels: function(parentContainer, groups){
    var displayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView(),
      showChangeReason: true,
      showChangeHistory: false
    });

    groups
        .forEach(
            function createGroupPanel(groupData)
            {
              var self = this;
              var groupPanel = new app.views.medications.common.TherapyGroupPanel({
                groupTitle: groupData.getName(),
                groupId: groupData.getGroupEnum(),
                view: this.getView(),
                contentData: groupData.getGroupElements(),
                attachElementToolbar: function(elementContainer)
                {
                  self._attachGroupPanelElementToolbar(elementContainer, groupData.isCancelTherapySupported());
                },
                displayProvider: !groupData.isAbortedTherapiesGroup() ?
                    displayProvider :
                    new app.views.medications.reconciliation.MedicationOnDischargeTherapyContainerDisplayProvider({
                      view: this.getView(),
                      showValidationIssues: false
                    })
              });
              if (groupData.isAbortedTherapiesGroup())
              {
                this.abortedTherapiesPanel = groupPanel;
              }
              parentContainer.add(groupPanel);
              this.mapSourceGroupTherapies(groupData);
            },
            this);
  },

  /**
   * @override Reconfigure the toolbar, since the ability to cancel a specific therapy is based on additional conditions,
   * and there isn't a suspend option available.
   * @param elementContainer
   * @param {boolean} [cancelAvailable=false]
   * @private
   */
  _attachGroupPanelElementToolbar: function (elementContainer, cancelAvailable)
  {
    var self = this;

    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      suspendAvailable: false,
      cancelAvailable: cancelAvailable === true,
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
      },
      cancelEventCallback: function(therapyContainer){
        // double check
        if (therapyContainer.getData().getTherapy().isLinkedToAdmission()) {
          self.onAbortTherapy(therapyContainer);
        }
      }
    });
    elementContainer.setToolbar(toolbar);

  },

  /**
   * Aborts the source medication of a given TherapyContainer. Aborted therapies are saved on the medication on discharge
   * list, but are added to a special group panel on the left side instead of the order basket. When creating the abort
   * order, we also copy over any validation issues the server provided, in case the user changes his mind and adds the
   * therapy to the medication on discharge list.
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer
   * @param {app.views.medications.common.dto.TherapyChangeReason} changeReasonDto
   * @private
   */
  _abortTherapy: function(therapyContainer, changeReasonDto)
  {
    changeReasonDto = tm.jquery.Utils.isEmpty(changeReasonDto) ? null : changeReasonDto;

    var actionEnums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    var sourceData = therapyContainer.getData();

    var abortOrder = new app.views.medications.reconciliation.dto.MedicationOnDischarge({
      therapy: sourceData.getTherapy(),
      sourceId: sourceData.getId(),
      sourceGroupEnum: therapyContainer.getGroupPanel().getGroupId(),
      status: actionEnums.NOT_PRESCRIBED,
      changeReasonDto: changeReasonDto,
      validationIssues: sourceData.getValidationIssues()
    });

    this.getAbortedTherapiesPanel().addElement(abortOrder);
    // We don't mark aborted therapies, since we move them to a different group panel and it would be confusing. Instead we
    // hide them.
    therapyContainer.hide();
  },

  /**
   * Returns all items from {@link #getAbortedTherapiesPanel} which represent abort orders of medication on admission, for
   * the created medication on discharge list. Since the group panel also contains therapies that were already aborted
   * during the process of prescribing medications on admission to inpatient therapies, it's vital those are ignored.
   * @return {Array<app.views.medications.reconciliation.dto.MedicationOnDischarge>}
   * @private
   */
  _getAbortedMedicationOnAdmission: function()
  {
    return this.getAbortedTherapiesPanel()
        .getContentData()
        .filter(
            /** @param {app.views.medications.reconciliation.dto.MedicationOnDischarge|
             * app.views.medications.reconciliation.dto.DischargeSourceMedication} itemData */
            function isUnsaved(itemData)
            {
              return itemData instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge &&
                  itemData.isAborted();
            });
  }
});