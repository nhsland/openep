/**
 * This jsClass serves as the glue between the commonly found components of a order placement type dialog. It's purpose is
 * to share the common business logic by means of composition instead of inheritance and prevent code duplication for common
 * tasks found on such dialogs.
 * TODO move additional methods and figure out if we can also handle common callback implementations
 */
Class.define('app.views.medications.ordering.OrderingCoordinator', 'tm.jquery.Object', {
  /** @ype app.views.common.AppView */
  view: null,
  /** @type app.views.medications.ordering.OrderingContainer */
  orderingContainer: null,
  /** @type app.views.medications.ordering.BasketContainer */
  basketContainer: null,
  /** @type app.views.medications.ordering.warnings.WarningsContainer|null */
  warningsContainer: null,
  /** @type boolean */
  skipEmptyBasketCheck: false,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {string} templateMode of {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplateElement>} elements
   * @param {boolean} [addToExistingTemplate=false]
   * @param {Array<string>|null} [customGroups=null] A list of possible custom groups for which templates can be saved.
   * @return {tm.jquery.Dialog}
   */
  createSaveTemplateDialog: function(templateMode, elements, addToExistingTemplate, customGroups)
  {
    var orderingContainer = this.orderingContainer;

    var saveTemplateDataEntryContainer = new app.views.medications.ordering.SaveTemplateDataEntryContainer({
      view: this.view,
      templateMode: templateMode,
      startProcessOnEnter: true,
      padding: 8,
      addToExistingTemplateSupported: addToExistingTemplate,
      elements: elements,
      templates: this.orderingContainer.getTemplates(),
      customGroups: customGroups
    });

    var dialog =
        this.view.getAppFactory()
            .createDataEntryDialog(
                this.view.getDictionary('therapy.order.set'),
                null,
                saveTemplateDataEntryContainer,
                function(resultData)
                {
                  if (resultData && resultData.isSuccess())
                  {
                    orderingContainer.reloadTemplates();
                  }
                },
                app.views.medications.ordering.SaveTemplateDataEntryContainer.DIALOG_WIDTH,
                app.views.medications.ordering.SaveTemplateDataEntryContainer.DIALOG_HEIGHT
            );
    saveTemplateDataEntryContainer.setDialog(dialog);

    return dialog;
  },

  /**
   * Should be used before placing the actual order and returns the mutated state of the orders, as it ensures drug
   * interaction warning override reasons are applied to therapy instances of the order list. Checks the state of the
   * coordinated components and resolves the promise if the order can be placed. Certain states, such as an unfinished edit
   * in the ordering container, will request user confirmation to proceed, while other conditions such as unresolved
   * critical warnings will automatically reject the promise.
   * @return {tm.jquery.Promise}
   */
  ensureOrderCanBePlaced: function()
  {
    var orderList = this.basketContainer.getBasketItems();

    var orderListValidation = this._validateOrderList(orderList);
    if (!orderListValidation.valid)
    {
      this.view.getAppFactory()
          .createWarningSystemDialog(orderListValidation.reason, 360, 180)
          .show();

      return tm.jquery.Deferred.create()
          .reject()
          .promise();
    }

    var self = this;
    var deferred = tm.jquery.Deferred.create();

    this._warnUserIfUnfinishedOrderOrDrugInteractionsLoading()
        .then(
            function checkCriticalWarnings()
            {
              if (!self._isEveryCriticalWarningOverridden())
              {
                deferred.reject();
                return;
              }

              self._applyWarningOverrideReasonsToTherapies(orderList);
              deferred.resolve(orderList);
            },
            function cancelOperation()
            {
              deferred.reject();
            }
        );
    return deferred.promise();
  },

  /**
   * Returns a promise that will get resolved either immediately or if the user confirms he is aware an unfinished order
   * in the ordering container exists. Intended for actions where the current order form input would be lost if the user
   * proceeds.
   * @return {tm.jquery.Promise}
   */
  warnUserIfUnfinishedOrderExists: function()
  {
    if (!this.orderingContainer.unfinishedOrderExists())
    {
      return tm.jquery.Deferred.create()
          .resolve()
          .promise();
    }

    var deferred = tm.jquery.Deferred.create();
    var confirmDialog =
        this.view.getAppFactory()
            .createConfirmSystemDialog(
                this.view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning'),
                function(confirmed)
                {
                  confirmed === true ? deferred.resolve() : deferred.reject();
                }
            );

    confirmDialog.setWidth(380);
    confirmDialog.setHeight(122);
    confirmDialog.show();

    return deferred.promise();
  },

  /**
   * Similar to {@link warnUserIfUnfinishedOrderExists}, but also checks if the drug interactions data for the warnings
   * container is loaded, and presents the possible issues in a single dialog that the user must confirm in order for
   * the promise to resolve.
   * @return {tm.jquery.Promise}
   * @private
   */
  _warnUserIfUnfinishedOrderOrDrugInteractionsLoading: function()
  {
    var unfinishedOrderExists = this.orderingContainer.unfinishedOrderExists();
    var warningsLoadingInProgress = !!this.warningsContainer && this.warningsContainer.isDataLoading();

    if (!unfinishedOrderExists && !warningsLoadingInProgress)
    {
      return tm.jquery.Deferred.create()
          .resolve()
          .promise();
    }

    var deferred = tm.jquery.Deferred.create();
    var warningString = "";
    var confirmDialogHeight = 110;

    if (unfinishedOrderExists)
    {
      warningString += this.view.getDictionary('unfinished.therapy.that.will.not.be.saved.warning') + "\n";
      confirmDialogHeight += 32;
    }

    if (warningsLoadingInProgress)
    {
      warningString += this.view.getDictionary('interactions.not.loaded.warning');
      confirmDialogHeight += 48;
    }

    var confirmDialog =
        this.view.getAppFactory()
            .createConfirmSystemDialog(
                warningString,
                function(confirmed)
                {
                  confirmed === true ? deferred.resolve() : deferred.reject();
                }
            );
    confirmDialog.setWidth(380);
    confirmDialog.setHeight(confirmDialogHeight);
    confirmDialog.show();

    return deferred.promise();
  },

  /**
   * This method copies reasons for any overridden medication warnings from the {@link #warningsContainer} to individual
   * therapy instances of the given orders with matching medications. Intended to be called before attempting to place/save
   * the given orders.
   * Clears the state of the existing critical warnings found on therapies to ensure multiple calls don't duplicate the data.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} orderList
   */
  _applyWarningOverrideReasonsToTherapies: function(orderList)
  {
    if (!this.warningsContainer)
    {
      return;
    }

    var warnings = this.warningsContainer.getOverriddenWarnings();

    for (var i = 0; i < warnings.length; i++)
    {
      var overrideReason = warnings[i].getOverrideReason();
      var warningDto = warnings[i].getWarning();

      if (warningDto.getMedications().length !== 0)
      {
        for (var j = 0; j < orderList.length; j++)
        {
          var therapy = orderList[j].getTherapy();
          var therapyContainsMedicationWithWarning = false;

          therapy.setCriticalWarnings([]);

          if (therapy.isOrderTypeSimple())
          {
            therapyContainsMedicationWithWarning = warningDto.hasMatchingMedication(therapy.getMedication());
          }
          else if (therapy.isOrderTypeComplex())
          {
            for (var k = 0; k < therapy.getIngredientsList().length; k++)
            {
              var infusionIngredient = therapy.getIngredientsList()[k];
              therapyContainsMedicationWithWarning =
                  warningDto.hasMatchingMedication(infusionIngredient.medication);
            }
          }

          if (therapyContainsMedicationWithWarning)
          {
            therapy.getCriticalWarnings().push(
                app.views.medications.warnings.WarningsHelpers.createOverriddenWarningString(
                    this.view,
                    overrideReason,
                    warningDto.getDescription()
                ));
          }
        }
      }
    }
  },

  /**
   * Validates the list of therapy orders, representing the current basket contents, to be combined in a single warning
   * dialog.
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder>} orderList
   * @return {{valid: boolean, reason: (*|null)}|{valid: boolean}}
   * @private
   */
  _validateOrderList: function(orderList)
  {
    if (!this.skipEmptyBasketCheck && orderList.length === 0)
    {
      return {valid: false, reason: this.view.getDictionary("you.have.no.therapies.in.basket")};
    }

    if (this._isAnyOrderIncompleteOrInvalid(orderList))
    {
      return {valid: false, reason: this.view.getDictionary("unfinished.therapies.in.basket.warning")};
    }

    return {valid: true};
  },

  /**
   * @param {Array<app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} orderList
   * @returns {boolean}
   */
  _isAnyOrderIncompleteOrInvalid: function(orderList)
  {
    return orderList.some(
        function(therapyOrder)
        {
          return !therapyOrder.isValid() || !therapyOrder.getTherapy().isCompleted();
        });
  },

  /** @return {boolean} */
  _isEveryCriticalWarningOverridden: function()
  {
    return !this.warningsContainer || this.warningsContainer.isEveryCriticalWarningOverridden();
  }
});