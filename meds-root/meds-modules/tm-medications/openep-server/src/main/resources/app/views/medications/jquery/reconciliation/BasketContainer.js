Class.define('app.views.medications.reconciliation.BasketContainer', 'app.views.medications.ordering.BasketContainer', {
  viewActiveTherapiesAvailable: false,

  Constructor: function (config)
  {
    this.callSuper(config);
    this.getDisplayProvider()
        .setShowMaxDose(true);
  },

  /**
   * Should the header include a button, by which the user may see a dropdown list of currently active (inpatient) therapies?
   * @return {boolean}
   */
  isViewActiveTherapiesAvailable: function()
  {
    return this.viewActiveTherapiesAvailable === true;
  },

  /**
   * @override
   * @return {app.views.medications.ordering.MedicationsTitleHeader}
   */
  buildHeader: function()
  {
    return new app.views.medications.ordering.MedicationsTitleHeader({
      title: this.getHeaderTitle(),
      view: this.view,
      actionsMenuFunction: this._createHeaderActionsMenu.bind(this),
      additionalDataContainer: this.isViewActiveTherapiesAvailable() ?
          this._buildAdditionalDataContainer() :
          undefined
    });
  },

  /**
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} items
   */
  setBasketItems: function (items)
  {
    this._refreshing = true; // ugly way of blocking animations due to a lot of refreshes, causing itemTpl to fire again
    this.list.setListData(items);
    this._refreshing = false;
  },

  /**
   * @override
   * @param therapy
   */
  removeTherapy: function (therapy)
  {
    var listData = this.list.getListData();
    for (var i = 0; i < listData.length; i++)
    {
      var item = listData[i];
      if (listData[i].getTherapy() === therapy)
      {
        this.list.removeRowData(item);
        this.therapiesRemovedEventCallback([item], {clearBasket: false });
        break;
      }
    }
  },

  removeByIndex: function (index)
  {
    this.list.removeRowData(index);
  },

  /**
   * @override to add the needed warning for medications on admission
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onEditBasketItem: function(therapyContainer)
  {
    var self = this;
    this._warnIfTriggeringActionOnPrescribedMedicationOnAdmission(
        therapyContainer.getData())
        .then(function()
        {
          self.fireEditTherapyEvent(therapyContainer);
        });
  },

  /**
   * @override to add the needed warning for medications on admission
   * @param {app.views.medications.common.therapy.TherapyContainer} therapyContainer
   */
  onRemoveBasketItem: function(therapyContainer)
  {
    var self = this;
    this._warnIfTriggeringActionOnPrescribedMedicationOnAdmission(
        therapyContainer.getData())
        .then(function()
        {
          self.removeBasketItem(therapyContainer);
        });
  },

  /**
   * Returns a promise that either gets resolved immediately, or asks for the user confirmation to continue with the action
   * if the order data represents the medication on admission, that was already prescribed as an inpatient therapy. The
   * intent is to inform the user that any further changes to the medication on admission won't reflect the inpatient
   * prescription.
   * @param {app.views.medications.ordering.AbstractTherapyOrder} orderData
   * @return {tm.jquery.Promise}
   * @private
   */
  _warnIfTriggeringActionOnPrescribedMedicationOnAdmission: function(orderData)
  {
    if (!this._isPrescribedMedicationOnAdmission(orderData))
    {
      return tm.jquery.Deferred.create()
          .resolve()
          .promise();
    }

    var deferred = tm.jquery.Deferred.create();

    var confirmDialog =
        this.view
            .getAppFactory()
            .createConfirmSystemDialog(
                this._createPrescribedMedicationOnAdmissionActionWarningText(orderData),
                function dialogResultCallback(confirmed)
                {
                  confirmed === true ? deferred.resolve() : deferred.reject();
                }
            );

    confirmDialog.setWidth(580);
    confirmDialog.setHeight(162);
    confirmDialog.show();

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} orderData
   * @return {string}
   * @private
   */
  _createPrescribedMedicationOnAdmissionActionWarningText: function(orderData)
  {
    return tm.jquery.Utils.formatMessage(
        this.view.getDictionary('medication.reconciliation.therapy.already.processed.warning'),
        tm.jquery.Utils.escapeHtml(orderData.getTherapy().getTherapyDescription()));
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder} orderData
   * @return {boolean}
   * @private
   */
  _isPrescribedMedicationOnAdmission: function(orderData)
  {
    return orderData instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission && orderData.isReadOnly();
  },

  /* override, make sure we never remove any read-only elements! */
  _clearBasket: function ()
  {
    var self = this;
    var listData = this.list.getListData().slice();
    var removedData = [];

    this._confirmRemovalOfOrders(listData, [], function (itemsToRemove)
    {
      for (var idx = itemsToRemove.length - 1; idx >= 0; idx--)
      {
        var data = itemsToRemove[idx];
        removedData.push(data);
        self.list.removeRowData(data);
      }
      self.therapiesRemovedEventCallback(removedData, {clearBasket: listData.length === 0});
    });
  },

  /**
   * A recursive method that mutates the given arrays with the intent to call the callback function with a new list of
   * those orders, for which the removal action was confirmed.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} listData
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} itemsToRemove
   * @param {function(Array<app.views.medications.ordering.AbstractTherapyOrder>)} processedCallback
   * @private
   */
  _confirmRemovalOfOrders: function(listData, itemsToRemove, processedCallback)
  {
    var self = this;
    var currentElement = listData.pop();
    if (currentElement)
    {
      this._warnIfTriggeringActionOnPrescribedMedicationOnAdmission(currentElement)
          .then(
              function confirmActionForCurrentAndProcessNext()
              {
                itemsToRemove.push(currentElement);
                self._confirmRemovalOfOrders(listData, itemsToRemove, processedCallback);
              },
              function skipActionForCurrentAndProcessNext()
              {
                self._confirmRemovalOfOrders(listData, itemsToRemove, processedCallback);
              });
    }
    else
    {
      processedCallback(itemsToRemove);
    }
  },

  /**
   * Constructs the {@link app.views.medications.ordering.MedicationsTitleHeader#additionalDataContainer} which houses
   * a dropdown button that displays the currently active inpatient therapies.
   * @return {tm.jquery.Container}
   * @private
   */
  _buildAdditionalDataContainer: function()
  {
    var button = new tm.jquery.Button({
      type: 'link',
      text: this.getView().getDictionary('view.active'),
      handler: function(component){
        component.getTooltip().show();
      },
      tooltip: this._buildViewActiveTherapiesTooltip()
    });

    var container = new tm.jquery.Container({
      cls: 'additional-actions-container',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });

    var viewActiveLayoutWrapper = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });

    viewActiveLayoutWrapper.add(new tm.jquery.Component({ html: '('}));
    viewActiveLayoutWrapper.add(button);
    viewActiveLayoutWrapper.add(new tm.jquery.Component({ html: ')'}));
    container.add(viewActiveLayoutWrapper);

    return container;
  },

  /**
   * Creates an instance of the active therapies tooltip and returns it.
   * @private
   */
  _buildViewActiveTherapiesTooltip: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var activeTherapiesPopoverTooltip = appFactory.createDefaultPopoverTooltip(
        view.getDictionary('active.inpatient.medications'),
        null,
        new app.views.medications.reconciliation.ActiveTherapiesListAppContainer({
          view: view,
          maxHeight: 350
        }),
        520
    );
    activeTherapiesPopoverTooltip.setTrigger('manual');
    return activeTherapiesPopoverTooltip;
  }
});
