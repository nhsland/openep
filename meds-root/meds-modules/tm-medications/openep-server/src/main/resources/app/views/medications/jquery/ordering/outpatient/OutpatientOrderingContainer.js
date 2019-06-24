Class.define('app.views.medications.ordering.outpatient.OutpatientOrderingContainer', 'app.views.medications.ordering.MedicationsOrderingContainer', {
  defaultWidth: null,
  defaultHeight: null,

  _lastViewActionDeferred: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getDefaultWidth: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultWidth) ? $(window).width() - 50 : this.defaultWidth;
  },

  getDefaultHeight: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultHeight) ? $(window).height() - 10 : this.defaultHeight;
  },

  /** @override to set content extensions for prescribing */
  buildOrderingContainer: function()
  {
    var view = this.view;
    var isPastMode = this.isPastMode;
    var self = this;

    return new app.views.medications.ordering.OrderingContainer({
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: isPastMode,
        startEndTimeAvailable: false,
        supplyAvailable: true,
        supplyRequired: true,
        heparinAvailable: false,
        infusionRateTypeSelectionAvailable: false,
        pastDaysOfTherapyVisible: false,
        indicationAvailable: false,
        dosingTimePatternAvailable: false,
        titratedDoseModeAvailable: false,
        templateOnlyMode: this.isPrescribeByTemplatesOnlyMode(),
        doseCalculationsAvailable: view.isDoseCalculationsEnabled(),
        medicationSearchResultFormatter:
            new app.views.medications.ordering.outpatient.MedicationSearchOutpatientResultFormatter()
      }),
      referenceData: this.getReferenceData(),
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, app.views.medications.ordering.OrderingContainer.DEFAULT_WIDTH),
      templateContext: this.getTemplateContext(),
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION,
      preventUnlicensedMedicationSelection: true,
      addTemplateTherapyOrdersCallback: function(therapyOrders)
      {
        therapyOrders = tm.jquery.Utils.isArray(therapyOrders) ? therapyOrders : [];
        var initialBasketItemCount = self.basketContainer.getTherapies().length;
        therapyOrders.forEach(
            function addToBasket(orderItem)
            {
              // TODO remove defaulting prescriptionLocalDetails once we separate outpatient and discharge templates
              if (!orderItem.getTherapy().prescriptionLocalDetails)
              {
                orderItem.getTherapy().prescriptionLocalDetails = {
                  prescriptionDocumentType: app.views.medications.TherapyEnums.prescriptionDocumentType.GREEN,
                  prescriptionSystem: 'EER'
                };
              }
              self._addToBasket(orderItem, therapyOrders.length, initialBasketItemCount)
            }
        );
      },
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      },
      confirmOrderEventCallback: function(confirmEventData)
      {
        if (confirmEventData.isValidationPassed())
        {
          return self._addToBasket(confirmEventData.getTherapyOrder());
        }
        return false;
      },
      saveOrderToTemplateEventCallback: self.onOrderingContainerSaveOrderToTemplate.bind(this),
      getBasketTherapiesFunction: function()
      {
        return self.getBasketContainer().getTherapies();
      },
      refreshBasketFunction: function()
      {
        self.getBasketContainer().refreshWithExistingData();
      },
      buildPrescriptionContentExtensions: function()
      {
        return self._buildPrescriptionContentExtensions();
      }
    });
  },

  /**
   * @override to add support for a second parameter, set based on the confirm button we press.
   * @param {function} resultDataCallback The callback method.
   * @param {boolean} saveOnly Optional parameter defining if the order will only be saved as oppose to saved and authorised.
   */
  processResultData: function(resultDataCallback, saveOnly)
  {
    var self = this;

    this.ensureOrderCanBePlaced()
        .then(
            function validationSuccessHandler(validatedBasketItems)
            {
              self.placeOrder(validatedBasketItems, saveOnly)
                  .then(successResultCallbackHandler, failureResultCallbackHandler);
            },
            failureResultCallbackHandler
        );

    function successResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: true}));
    }

    function failureResultCallbackHandler()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    }
  },

  /**
   * @override to implementing order placement via a view action.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} basketItems
   * @param {boolean} saveOnly
   * @returns {tm.jquery.Promise}
   */
  placeOrder: function(basketItems, saveOnly)
  {
    return this.getView().isSwingApplication() ?
      this._placeOrderByViewAction(basketItems, saveOnly) :
      this._placeOrderByRestApi(basketItems);
  },

  /**
   * @override
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   */
  getTemplateContext: function()
  {
    return app.views.medications.TherapyEnums.therapyTemplateContextEnum.OUTPATIENT;
  },

  /**
   * Called from the view when an view action callback is received. Will resolve or reject the last deferred that
   * was created when sending the action, if present.
   * @param {Object} actionCallback
   */
  onActionCallback: function(actionCallback)
  {
    if (this._lastViewActionDeferred)
    {
      var lastDeferred = this._lastViewActionDeferred;
      this._lastViewActionDeferred = null;

      this.view.hideLoaderMask();

      if (actionCallback.successful)
      {
        lastDeferred.resolve();
      }
      else
      {
        lastDeferred.reject();
      }
    }
  },

  /**
   * Executes order placement via a view action.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} basketItems
   * @param {Boolean} saveOnly
   * @returns {tm.jquery.Promise}
   * @private
   */
  _placeOrderByViewAction: function(basketItems, saveOnly)
  {
    if (this._lastViewActionDeferred)
    {
      this._lastViewActionDeferred.reject();
    }

    this._lastViewActionDeferred = tm.jquery.Deferred.create();

    var view = this.getView();
    view.showLoaderMask();
    view.sendAction("outpatientPrescription", {
      patientId: view.getPatientId(),
      prescriptionBundle: JSON.stringify(this._createPrescriptionPackage(basketItems)),
      saveOnly: saveOnly === true // normalise the value to a boolean
    });

    return this._lastViewActionDeferred.promise();
  },

  /**
   * Executes saving the order via the rest API.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} basketItems
   * @returns {tm.jquery.Promise}
   * @private
   */
  _placeOrderByRestApi: function(basketItems)
  {
    return this.getView()
        .getRestApi()
        .saveOutpatientPrescription(
            this._createPrescriptionPackage(basketItems));
  },

  _buildPrescriptionContentExtensions: function()
  {
    var view = this.view;
    var extensions = [];
    extensions.push(new app.views.medications.ordering.outpatient.EERContentExtensionContainer({
      view: view
    }));
    return extensions;
  },

  /**
   * @param abstractTherapyOrder
   * @return {app.views.medications.common.dto.PrescriptionPackage}
   * @private
   */
  _createPrescriptionPackage: function(abstractTherapyOrder)
  {
    return new app.views.medications.common.dto.PrescriptionPackage({
      prescriptionTherapies: abstractTherapyOrder.map(this._convertBasketItemToPrescriptionTherapy)
    });
  },

  /**
   * Simple map helper.
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   *         app.views.medications.common.therapy.AbstractTherapyContainerData} abstractTherapyOrder
   * @return {{therapy: app.views.medications.common.dto.Therapy}}
   */
  _convertBasketItemToPrescriptionTherapy: function(abstractTherapyOrder)
  {
    return {therapy: abstractTherapyOrder.getTherapy()};
  }
});
