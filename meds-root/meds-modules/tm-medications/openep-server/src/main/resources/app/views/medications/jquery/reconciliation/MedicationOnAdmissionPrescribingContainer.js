Class.define('app.views.medications.reconciliation.MedicationOnAdmissionPrescribingContainer', 'app.views.medications.reconciliation.BaseMedicationReconciliationContainer', {
  viewActiveTherapiesAvailable: true,
  templateContext: app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT,
  /** @type boolean and set to true since we might only suspend/abort therapies, which aren't part of the basket */
  skipEmptyBasketCheck: true,
  /** @type app.views.medications.common.TherapyGroupPanel */
  _suspendedTherapiesPanel: null,
  /** @type app.views.medications.common.TherapyGroupPanel */
  _sourceTherapiesPanel: null,

  Constructor: function(config)
  {
    this.callSuper(
        config,
        new app.views.medications.ordering.OrderingBehaviour({
          doseCalculationsAvailable: config.view.isDoseCalculationsEnabled()
        }),
        new app.views.medications.ordering.SaveMedicationOrderContainerDisplayProvider({
          view: config.view
        })
    );
  },

  /* Override values for column titles */
  getRightColumnTitle: function()
  {
    return this.getView().getDictionary("inpatient.prescription");
  },

  /**
   * Places the inpatient medication order.
   * @returns {tm.jquery.Promise}
   * @private
   */
  placeInpatientOrder: function()
  {
    var view = this.view;
    var prescriber = view.getCurrentUserAsCareProfessional();
    var lastLinkName = view.getPatientLastLinkNamePrefix();
    var deferred = tm.jquery.Deferred.create();
    // suspend until discharge or abort orders might not be valid as an inpatient prescription, so validation must be skipped
    var suspendUntilDischargeOrAbortOrders = this._getUnsavedSuspendUntilDischargeOrAbortOrders();

    this.ensureOrderCanBePlaced()
        .then(
            function saveMedicationOnAdmission(validatedBasketItems)
            {
              var combinedOrderItems = jQuery.merge(validatedBasketItems, suspendUntilDischargeOrAbortOrders);
              view.getRestApi()
                  .saveMedicationsOrder(combinedOrderItems, prescriber, null, lastLinkName, false)
                  .then(
                      function onPlaceInpatientOrderSuccess()
                      {
                        deferred.resolve();
                      },
                      function onPlaceInpatientOrderError()
                      {
                        deferred.reject();
                      });
            },
            function placeInpatientOrderUserCancellation()
            {
              deferred.reject();
            });

    return deferred.promise();
  },

  /**
   * @param {tm.jquery.Container} parentContainer
   */
  attachTherapyGroupPanels: function(parentContainer)
  {
    var self = this;
    var view = this.getView();
    this._sourceTherapiesPanel = new app.views.medications.common.TherapyGroupPanel({
      groupId: app.views.medications.TherapyEnums.therapySourceGroupEnum.MEDICATION_ON_ADMISSION,
      groupTitle: this.getSourceTherapiesPanelTitle(),
      view: this.getView(),
      expanded: true,
      contentData: [],
      /** @param {app.views.medications.common.therapy.TherapyContainer} elementContainer */
      attachElementToolbar: function(elementContainer)
      {
        self._attachGroupPanelElementToolbar(
            elementContainer,
            self._isMedicationOnAdmissionValidAndSafeToPrescribeWithoutEdit(elementContainer.getData()),
            true,
            true);
      }
    });
    parentContainer.add(this._sourceTherapiesPanel);

    this._suspendedTherapiesPanel = new app.views.medications.common.TherapyGroupPanel({
      groupId: app.views.medications.TherapyEnums.therapySourceGroupEnum.STOPPED_ADMISSION_MEDICATION,
      groupTitle: this.getView().getDictionary("suspended") + " / " + this.getView().getDictionary("stop.past.on.admission"),
      view: view,
      contentData: [],
      expanded: true,
      /** @param {app.views.medications.common.therapy.TherapyContainer} elementContainer */
      attachElementToolbar: function(elementContainer)
      {
        self._attachStoppedGroupPanelElementToolbar(elementContainer);
      },
      displayProvider: new app.views.medications.ordering.SaveMedicationOrderContainerDisplayProvider({
        showChangeReason: true,
        showValidationIssues: false,
        view: view
      })
    });
    parentContainer.add(this._suspendedTherapiesPanel);
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
   * @override to ensure any therapies that were aborted (which only hides the source therapies), are shown again in case
   * the user decides to prescribe them
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer
   */
  unmarkSourceTherapyContainer: function(therapyContainer)
  {
    this.callSuper(therapyContainer);
    therapyContainer.show();
  },

  /**
   * @override to extend the original method with configurable add, suspend and cancel operation on attached toolbars.
   * @param {app.views.medications.common.therapy.TherapyContainer} elementContainer
   * @param {boolean} [addAvailable=true]
   * @param {boolean} [suspendAvailable=false]
   * @param {boolean} [cancelAvailable=false]
   * @private
   */
  _attachGroupPanelElementToolbar: function(elementContainer, addAvailable, suspendAvailable, cancelAvailable)
  {
    if (elementContainer.getData().isReadOnly())
    {
      return;
    }

    var self = this;
    var toolbar = new app.views.medications.reconciliation.TherapyContainerPanelToolbar({
      therapyContainer: elementContainer,
      suspendAvailable: suspendAvailable === true,
      cancelAvailable: cancelAvailable === true,
      addAvailable: addAvailable !== false,
      addWithEditAvailable: !this.getOrderingBehaviour().isTemplateOnlyMode(),
      addToBasketEventCallback: function(therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onAddGroupPanelTherapyToBasket(therapyContainer);
      },
      addToBasketWithEditEventCallback: function(therapyContainer)
      {
        self.getTherapySelectionContainer().setSourceTherapyContainer(therapyContainer);
        self.onEditTherapy(therapyContainer);
      },
      cancelEventCallback: function(therapyContainer)
      {
        self.onAbortTherapy(therapyContainer, false);
      },
      suspendEventCallback: function(therapyContainer)
      {
        self.onSuspendTherapy(therapyContainer, true);
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  /**
   * Prevents group panel elements with a suspended therapy from having the suspend toolbar option and aborted from
   * having the abort action.
   * @param {app.views.medications.common.therapy.TherapyContainer} elementContainer
   * @private
   */
  _attachStoppedGroupPanelElementToolbar: function(elementContainer)
  {
    var enums = app.views.medications.TherapyEnums;
    var data = elementContainer.getData();

    var therapyStatus = data.getTherapyStatus === 'function' ? data.getTherapyStatus() : data.therapyStatus;

    this._attachGroupPanelElementToolbar(
        elementContainer,
        this._isMedicationOnAdmissionValidAndSafeToPrescribeWithoutEdit(data),
        therapyStatus !== enums.therapyStatusEnum.SUSPENDED,
        therapyStatus !== enums.therapyStatusEnum.ABORTED
    );
  },

  _suspendTherapy: function(therapyContainer, prescribe, suspendReason)
  {
    var view = this.getView();
    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var actionTypeEnum = prescribe === true ? actionEnums.SUSPEND : actionEnums.SUSPEND_ADMISSION;
    var changeReasonMap = view.getChangeReasonTypeHolder().getMap();
    var changeReason =
        app.views.medications.MedicationUtils.getFirstOrNullTherapyChangeReason(changeReasonMap, actionTypeEnum) ||
        new app.views.medications.common.dto.TherapyChangeReason();
    changeReason.setComment(suspendReason);

    this._suspendOrAbortTherapy(therapyContainer, actionTypeEnum, changeReason);
  },

  _abortTherapy: function(therapyContainer, changeReasonDto)
  {
    this._suspendOrAbortTherapy(therapyContainer,
        app.views.medications.TherapyEnums.medicationOrderActionEnum.ABORT,
        changeReasonDto);
  },

  /**
   * Creates a therapy order either for the suspended or stopped on admission panel or, when choosing 'prescribe and
   * suspend' {@link app.views.medications.TherapyEnums.medicationOrderActionEnum.SUSPEND}, for the basket therapy.
   * @param {app.views.medications.common.therapy.TherapyContainer|tm.jquery.Component} therapyContainer from either
   * the medication on admission group panel or the suspended / stopped on admission group panel.
   * @param {string} actionType of {@link app.views.medications.TherapyEnums.medicationOrderActionEnum}
   * @param {app.views.medications.common.dto.TherapyChangeReason|undefined|null} changeReasonDto
   * @private
   */
  _suspendOrAbortTherapy: function(therapyContainer, actionType, changeReasonDto)
  {
    changeReasonDto = tm.jquery.Utils.isEmpty(changeReasonDto) ? null : changeReasonDto;

    var actionEnums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var abortAction = actionType === actionEnums.ABORT;
    var prescribeAndSuspend = actionType !== actionEnums.SUSPEND;
    var groupPanel = therapyContainer.getGroupPanel();
    var previouslySuspendedOrAborted = groupPanel === this._suspendedTherapiesPanel;
    // when moving from abort to suspend and vice versa, the actual source container isn't the same as the given container
    var sourceContainer = previouslySuspendedOrAborted ?
        this.findSourceMedicationTherapyContainer(therapyContainer.getData().getSourceId()) :
        therapyContainer;
    var sourceData = sourceContainer.getData();

    var suspendOrAbortOrder = new app.views.medications.ordering.dto.SaveMedicationOrder({
      therapy: sourceData.getTherapy().clone(),
      sourceId: sourceData.getTherapy().getCompositionUid(),
      actionEnum: actionType,
      changeReasonDto: changeReasonDto,
      validationIssues: sourceData.getValidationIssues().slice(0)
    });

    if (previouslySuspendedOrAborted)
    {
      groupPanel.remove(therapyContainer);
    }

    if (abortAction)
    {
      suspendOrAbortOrder.getTherapy().setStart(CurrentTime.get());
      suspendOrAbortOrder.getTherapy().setEnd(CurrentTime.get());
    }
    else
    {
      suspendOrAbortOrder.getTherapy().rescheduleTherapyTimings(false);
    }

    if (prescribeAndSuspend)
    {
      this._suspendedTherapiesPanel.addElement(suspendOrAbortOrder);
      // we hide the container so it's less confusing to the user about which marked therapies are in the basket
      // and which were moved to the aborted panel
      sourceContainer.hide();
    }
    else
    {
      this.addToBasket(suspendOrAbortOrder, sourceContainer, actionType);
      this.markSourceTherapyContainer(sourceContainer);
    }
  },

  /**
   * Generates and attaches any additional (client side) validation issues to either the source panel therapies or
   * the basket order items. One such validation is in case of the variable days dose. Ensuring the protocol is not a
   * descriptive dose protocol has to be checked every time we add an order to the basket, since the order forms don't
   * validate the type of the protocol.
   * @param {app.views.medications.reconciliation.dto.MedicationGroupTherapy} orderItem
   * @private
   */
  _attachClientSideValidationIssues: function(orderItem)
  {
    var protocolValidationIssueEnum =
        app.views.medications.TherapyEnums.validationIssueEnum.DISCHARGE_PROTOCOL_NOT_SUPPORTED;
    if (this._isTherapyWithUnsupportedVariableDaysDose(orderItem.getTherapy()) &&
        orderItem.getValidationIssues().indexOf(protocolValidationIssueEnum) < 0)
    {
      orderItem.getValidationIssues().push(protocolValidationIssueEnum)
    }
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @return {boolean} true, if the therapy contains descriptive variable days dose definition specific for the admission and
   * discharge prescribing, which is not supported and should be converted to a inpatient based protocol.
   * @private
   */
  _isTherapyWithUnsupportedVariableDaysDose: function(therapy)
  {
    if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements()))
    {
      return app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(therapy.getTimedDoseElements());
    }
    return false;
  },

  /**
   * Some therapies from admission are deemed unsafe to be prescribed in inpatient care without forcing the user to edit
   * the prescription and hopefully check all the values. One such case are prescriptions based on the admission list, where
   * the prescription was intended to be taken every X days. Since we're missing the information of the previous
   * administration time, we're unable to correctly define the next administration time.
   * @param {app.views.medications.reconciliation.dto.MedicationOnAdmission|
   * app.views.medications.reconciliation.dto.MedicationGroupTherapy} medicationOnAdmission
   * @return {boolean}
   * @private
   */
  _isMedicationOnAdmissionValidAndSafeToPrescribeWithoutEdit: function(medicationOnAdmission)
  {
    return medicationOnAdmission.getValidationIssues().length === 0 &&
        tm.jquery.Utils.isEmpty(medicationOnAdmission.getTherapy().getDosingDaysFrequency());
  },

  /**
   * Attaches the given medication on admission to the appropriate source group panels. If the medication was already
   * processed and the desired action saved, it's considered "read only" - meaning you can't process it again.
   * Due to our base implementation, we have to map our* medication on admission instances to instances of
   * {@link app.views.medications.reconciliation.dto.SourceMedication} in order for our underlying mechanisms of finding the
   * source container to work. Also note that the sourceId of the given medications on admission can't be used as the
   * source id in this container, since therapies added from the search bar or order sets don't have a source id in respect
   * to the medication on admission list.
   * @param {Array<app.views.medications.reconciliation.dto.MedicationOnAdmission|
   * app.views.medications.reconciliation.dto.MedicationGroupTherapy>} medicationOnAdmission
   */
  _attachSourceMedicationOnAdmission: function(medicationOnAdmission)
  {
    var orderActionEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum;

    medicationOnAdmission.forEach(
        function addToSourcePanel(itemData)
        {
          // noinspection JSCheckFunctionSignatures since jsClass extending isn't supported
          this._attachClientSideValidationIssues(itemData);

          if (itemData.isSuspended() || itemData.isAborted())
          {
            // noinspection JSUnresolvedFunction since jsClass extending isn't supported
            var suspendOrder = new app.views.medications.ordering.dto.SaveMedicationOrder({
              therapy: itemData.getTherapy(),
              sourceId: itemData.getTherapy().getCompositionUid(),
              actionEnum: itemData.isSuspended() ? orderActionEnum.SUSPEND : orderActionEnum.ABORT,
              changeReasonDto: itemData.getTherapyChangeReason(),
              readOnly: true
            });
            this._suspendedTherapiesPanel.addElement(suspendOrder);
          }
          else
          {
            var createdTherapyContainer = this._sourceTherapiesPanel.addElement(
                new app.views.medications.reconciliation.dto.SourceMedication({
                  therapy: itemData.getTherapy(),
                  // using the
                  sourceId: itemData.getTherapy().getCompositionUid(),
                  validationIssues: itemData.getValidationIssues(),
                  readOnly: itemData.isReadOnly()
                }));
            if (itemData.isReadOnly())
            {
              this.markSourceTherapyContainer(createdTherapyContainer);
            }
          }
        },
        this
    );
    this._mapSourceAndSuspendedPanelContentAsSourceGroupTherapies();
  },

  /**
   * Since our source therapies are provided as a list of medications on admissions and converted to source therapies
   * inside the {@link #_attachSourceMedicationOnAdmission}, we can't call {@link #mapSourceGroupTherapies} of our base
   * implementation directly. Therefore we create artificial {@link app.views.medications.reconciliation.dto.MedicationGroup}
   * from our existing source panel content, which we then use to map the source therapies. The contents of the suspended
   * panel contain instances of {@app.views.medications.ordering.dto.SaveMedicationOrder}, so we also have to map them
   * to {@link app.views.medications.reconciliation.dto.SourceMedication} for our call to succeed.
   * @private
   */
  _mapSourceAndSuspendedPanelContentAsSourceGroupTherapies: function()
  {
    this.mapSourceGroupTherapies(
        new app.views.medications.reconciliation.dto.MedicationGroup({
          groupEnum: this._sourceTherapiesPanel.getGroupId(),
          groupElements: this._sourceTherapiesPanel.getContentData()
        }));
    this.mapSourceGroupTherapies(
        new app.views.medications.reconciliation.dto.MedicationGroup({
          groupEnum: this._suspendedTherapiesPanel.getGroupId(),
          groupElements: this._suspendedTherapiesPanel
              .getContentData()
              .map(
                  /** @param {app.views.medications.ordering.dto.SaveMedicationOrder|
                   * app.views.medications.common.therapy.AbstractTherapyContainerData} orderItem */
                  function toSourceMedication(orderItem)
                  {
                    return new app.views.medications.reconciliation.dto.SourceMedication({
                      therapy: orderItem.getTherapy(),
                      sourceId: orderItem.getTherapy().getCompositionUid(),
                      validationIssues: orderItem.getValidationIssues()
                    });
                  })
        }));
  },

  /**
   * Returns the list of new orders from the suspend/abort panel. The panel may contain previously placed orders, loaded
   * with the medication on admission list, which are read only. Keep in mind these orders might contain invalid therapies,
   * which need not be resolved at this point.
   * @return {Array<app.views.medications.ordering.dto.SaveMedicationOrder>}
   */
  _getUnsavedSuspendUntilDischargeOrAbortOrders: function()
  {
    return this._suspendedTherapiesPanel
        .getContentData()
        .filter(function isUnsavedOrder(data)
        {
          return data instanceof app.views.medications.ordering.dto.SaveMedicationOrder && !data.isReadOnly()
        });
  },

  onSuspendTherapy: function(therapyContainer)
  {
    var view = this.getView();
    var self = this;

    var suspendDialog = app.views.medications.reconciliation.SuspendAdmissionTherapyEntryContainer.asDialog(
        view,
        this._isMedicationOnAdmissionValidAndSafeToPrescribeWithoutEdit(therapyContainer.getData()),
        function(resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            self._suspendTherapy(
                therapyContainer,
                resultData.getValue().prescribe === true,
                resultData.getValue().reason);
          }
        });
    suspendDialog.show();
  },

  onAbortTherapy: function(therapyContainer, suspend)
  {
    var changeTypeEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
    var changeTypeKey = changeTypeEnums.ABORT;
    var self = this;
    var view = this.getView();

    var changeReasonEntryContainer = new app.views.medications.common.ChangeReasonDataEntryContainer({
      titleIcon: "warningYellow_status_48.png",
      titleText: view.getDictionary(suspend === true ?
          "therapy.needs.reason.for.suspending" : "therapy.needs.reason.for.stopping"),
      view: this.getView(),
      changeReasonTypeKey: changeTypeKey
    });

    var suspendConfirmationDialog = this.getView().getAppFactory().createDataEntryDialog(
        this.getView().getDictionary("warning"),
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
    suspendConfirmationDialog.show();
  },

  /**
   * @override Add support for the suspend and abort action. Unless we decide we will prescribe the therapy as
   * a suspended therapy, those therapies need to go into a separate group panel which clearly indicates which
   * therapies were aborted or suspended.
   * @param therapyContainer
   */
  onAddGroupPanelTherapyToBasket: function(therapyContainer)
  {
    therapyContainer.getToolbar().setEnabled(false, true);

    var self = this;
    // if this is a suspended or canceled therapy, we should reactivate it
    var data = therapyContainer.getData();
    var therapy = therapyContainer.getData().getTherapy();
    var therapyStatus = data.getTherapyStatus();
    var statusEnums = app.views.medications.TherapyEnums.therapyStatusEnum;

    if (therapyStatus === statusEnums.SUSPENDED || therapyStatus === statusEnums.ABORTED)
    {
      var admissionPanelElement = this._sourceTherapiesPanel.getElementContainerByData(therapyContainer.getData());
      this.getTherapySelectionContainer().setSourceTherapyContainer(admissionPanelElement); // reroute to the original container
      this._suspendedTherapiesPanel.remove(therapyContainer);
    }
    therapy.rescheduleTherapyTimings(this.getView().getPresetDate());
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

                if (updatedTherapy.isBaselineInfusion())
                {
                  self.baselineInfusionIntervals.push({
                    startMillis: updatedTherapy.getStart().getTime(),
                    endMillis: !!updatedTherapy.getEnd() ? updatedTherapy.getEnd().getTime() : null
                  })
                }

                // we copy to loose all data we don't want to persist
                var orderCopy = new app.views.medications.ordering.TherapyOrder({
                  therapy: updatedTherapy,
                  validationIssues: therapyContainer.getData().getValidationIssues().slice(0)
                });

                // since we don't enter oxygen saturation at admission, prevent the therapy to be ordered if
                // the saturation is missing
                if (updatedTherapy.isOrderTypeOxygen() &&
                    (!updatedTherapy.getMaxTargetSaturation() || !updatedTherapy.getMinTargetSaturation()))
                {
                  orderCopy
                      .getValidationIssues()
                      .push(app.views.medications.TherapyEnums.validationIssueEnum.TARGET_SATURATION_MISSING);
                }

                self.addToBasket(orderCopy, therapyContainer);
              }
          );
    }
    else
    {
      therapyContainer.getToolbar().setEnabled(true, true);
    }
  },

  /**
   * @override
   * @param {app.views.medications.ordering.AbstractTherapyOrder|*} orderItem
   * @param {app.views.medications.common.therapy.TherapyContainer} sourceContainer
   * @param {string} [action=undefined] app.views.medications.TherapyEnums.medicationOrderActionEnum
   */
  addToBasket: function(orderItem, sourceContainer, action)
  {
    var actionEnum = action ||
        (orderItem.isRecordAdministration() ?
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER :
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE);
    var sourceData = null;
    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer)
    {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
      // if the source container was not defined, but we have a source container, edit was conducted
      if (sourceContainer)
      {
        actionEnum = app.views.medications.TherapyEnums.medicationOrderActionEnum.EDIT;
      }
    }

    if (sourceContainer)
    {
      sourceData = sourceContainer.getData();
    }

    if (sourceData instanceof app.views.medications.ordering.dto.SaveMedicationOrder)
    {
      // since the suspended therapies panel also contains basket items, make sure to remove it there before we reroute
      this._suspendedTherapiesPanel.removeByData(sourceData);
      // Due to re-adding a basket or suspended panel item, reroute the source container if we can find one.
      sourceContainer = this.findSourceMedicationTherapyContainer(sourceContainer.getData().getSourceId());
      this.getTherapySelectionContainer().setSourceTherapyContainer(sourceContainer);
      sourceData = sourceContainer ? sourceContainer.getData() : null;
    }

    var medicationOrder = new app.views.medications.ordering.dto.SaveMedicationOrder({
      therapy: orderItem.getTherapy(),
      actionEnum: actionEnum,
      changeReasonDto: orderItem.getTherapyChangeReason(),
      validationIssues: orderItem.getValidationIssues()
    });

    this._attachClientSideValidationIssues(medicationOrder);

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.SourceMedication)
    {
      medicationOrder.setSourceId(sourceData.getId());
      this.markSourceTherapyContainer(sourceContainer);
    }

    this.getBasketContainer().addTherapy(medicationOrder);
  },

  /** Loads the medications on admission with server side validations and populates the source and suspended group panels. */
  loadSourceMedicationOnAdmission: function()
  {
    this.view
        .getRestApi()
        .loadMedicationsOnAdmission(true, true)
        .then(this._attachSourceMedicationOnAdmission.bind(this));
  },

  /**
   * @override to implement change reason required rules
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onEditTherapy: function(therapyContainer)
  {
    var data = therapyContainer.getData();

    this.getTherapySelectionContainer()
        .editTherapyOrder(
            therapyContainer,
            data instanceof app.views.medications.reconciliation.dto.SourceMedication ||
            (data instanceof app.views.medications.ordering.dto.SaveMedicationOrder &&
                !tm.jquery.Utils.isEmpty(data.getSourceId())));
  },

  /**
   * @return {string}
   */
  getSourceTherapiesPanelTitle: function()
  {
    return this.getView().getDictionary('medication.on.admission');
  }
});
