Class.define('app.views.medications.reconciliation.MedicationOnAdmissionContainer', 'app.views.medications.reconciliation.BaseMedicationReconciliationContainer', {
  /** @type boolean */
  warningsEnabled: false,
  /** @type boolean */
  groupPanelsAttached: false,
  /** @type string of {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum} */
  templateContext: app.views.medications.TherapyEnums.therapyTemplateContextEnum.ADMISSION,

  /**
   * As with the {@link app.views.medications.reconciliation.BaseMedicationReconciliationContainer} constructor, since
   * the discharge medication container is based on the admission container, we have to allow passing the ordering
   * behavior configuration and basket item's display provider to the base class. Otherwise the configuration
   * for medication on admission is constructed and passed to the base class.
   * @param {Object} config
   * @param {app.views.medications.ordering.OrderingBehaviour} [orderingBehaviour]
   * @param {app.views.medications.common.therapy.TherapyContainerDisplayProvider} [basketTherapyDisplayProvider]
   * @constructor
   */
  Constructor: function(config, orderingBehaviour, basketTherapyDisplayProvider)
  {
    this.callSuper(
        config,
        orderingBehaviour || new app.views.medications.ordering.OrderingBehaviour({
          startEndTimeAvailable: false,
          supplyAvailable: false,
          oxygenSaturationAvailable: false,
          informationSourceAvailable: true,
          informationSourceRequired: true,
          templateOnlyMode: false,
          /** Disabling the ability to save therapies as templates for everyone, since we can't limit the user with limited
           * prescribing permissions to template mode only in this use case. This way they can't cheat and create their
           * own templates on this screen. */
          addToTemplateAvailable: false,
          /* Everything except the medication and information source should be optional. */
          doseFrequencyRequired: false,
          doseRequired: false,
          doseCalculationsAvailable: config.view.isDoseCalculationsEnabled(),
          routeOfAdministrationRequired: false,
          targetInrRequired: false,
          indicationAlwaysOptional: true,
          commentAlwaysOptional: true
        }),
        basketTherapyDisplayProvider,
        new app.views.medications.reconciliation.MedicationOnDischargeVariableDoseDialogFactory()
    );
    this.getBasketTherapyDisplayProvider().setShowChangeReason(false);
    this.getBasketTherapyDisplayProvider().setShowChangeHistory(false);
    this.baselineInfusionIntervals = null;
    /* dont need to check if infusion intervals overlap */
  },

  /**
   * @Override Setting column title.
   * @return {String}
   */
  getRightColumnTitle: function()
  {
    return this.getView().getDictionary("medication.on.admission");
  },

  loadBasketContents: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = this.getView().getAppFactory();

    view
        .getRestApi()
        .loadMedicationsOnAdmission(false, true)
        .then(
            function onDataLoad(data)
            {
              appFactory.createConditionTask(  //wait for the medications to load
                  function()
                  {
                    self.getBasketContainer().setBasketItems(data);
                    self.markSourceTherapies(data);
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
   * @param {tm.jquery.Container} parentContainer
   */
  attachTherapyGroupPanels: function(parentContainer)
  {
    var self = this;
    var view = this.getView();

    view
        .getRestApi()
        .loadTherapyGroupsOnAdmission()
        .then(
            /**
             * @param {Array<app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup|
             * app.views.medications.reconciliation.dto.MedicationGroup>} data
             */
            function displaySourceMedicationOnAdmission(data)
            {
              self._createAndAddSourceGroupTherapyPanels(parentContainer, data);
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
   * @override Wraps the order data into an instance of the {@link MedicationOnAdmission}.
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData|*} orderData
   * @param {app.views.medications.common.therapy.TherapyContainer} sourceContainer
   */
  addToBasket: function(orderData, sourceContainer)
  {
    var sourceData = null;
    var orderStatus = app.views.medications.TherapyEnums.medicationOnAdmissionStatus.PENDING;

    // in case of add with edit, no source container is passed over, check if there is one
    if (!sourceContainer)
    {
      sourceContainer = this.getTherapySelectionContainer().getSourceTherapyContainer();
    }

    if (sourceContainer)
    {
      sourceData = sourceContainer.getData();
    }

    if (this._isOrderDataFromBasket(sourceData))
    {
      // Preserve the the composition uid and previous status when editing basket items, otherwise a new therapy is created
      // and any established links will be destroyed. The order status triggers a warning when removing such
      // an item from the basket, if it was already processed on the optional inpatient prescribing step.
      orderData.getTherapy()
          .setCompositionUid(sourceData.getTherapy().getCompositionUid());

      orderStatus = sourceData.getStatus();
      if (!!sourceData.getSourceId())
      {
        sourceContainer = this.findSourceMedicationTherapyContainer(sourceData.getSourceId());
        this.getTherapySelectionContainer()
            .setSourceTherapyContainer(sourceContainer);
        if (!!sourceContainer)
        {
          sourceData = sourceContainer.getData();
        }
      }
      else
      {
        // If we can't find the original source container of a basket item, the source is either template based or
        // a 'custom' prescription made by medication selection. We clear the template source container in order to
        // know when to cache the order's information sources as the new preselected value.
        this.getTherapySelectionContainer()
            .setSourceTherapyContainer(null);
        sourceContainer = null;
      }
    }

    var basketItem = new app.views.medications.reconciliation.dto.MedicationOnAdmission({
      therapy: orderData.getTherapy(),
      status: orderStatus,
      changeReasonDto: orderData.getTherapyChangeReason(),
      validationIssues: orderData.getValidationIssues()
    });
    // noinspection JSCheckFunctionSignatures since we're working with an extended jsClass
    this.attachClientSideValidationIssues(basketItem);

    if (sourceContainer && sourceData instanceof app.views.medications.reconciliation.dto.SourceMedication)
    {
      basketItem.setSourceGroupEnum(sourceContainer.getGroupPanel().getGroupId());
      basketItem.setSourceId(sourceData.getId());
      this.markSourceTherapyContainer(sourceContainer);
    }

    // When no sourceContainer exists, it's due to the source being either a template or a 'custom' prescription from
    // search. In these cases, the selected source of information should become the new preselected value when
    // adding the next prescribing from a template or search.
    if (!sourceContainer)
    {
      this.getTherapySelectionContainer()
          .setDefaultInformationSources(orderData.getTherapy().getInformationSources().slice());
    }

    this.getBasketContainer().addTherapy(basketItem);
  },

  /**
   * Save the contents of the basket as the new medication on admission list.
   * @return {tm.jquery.Promise}
   */
  saveBasketContent: function()
  {
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();

    this.ensureOrderCanBePlaced()
        .then(
            function saveMedicationOnAdmission(validatedBasketItems)
            {
              view.getRestApi()
                  .saveMedicationOnAdmission(validatedBasketItems)
                  .then(
                      function onSaveMedicationOnAdmissionSuccess()
                      {
                        deferred.resolve();
                      },
                      function onSaveMedicationOnAdmissionError()
                      {
                        deferred.reject();
                      });
            },
            function saveMedicationOnAdmissionUserCancellation()
            {
              deferred.reject();
            });

    return deferred.promise();
  },

  /**
   * @return {boolean}
   */
  isGroupPanelsAttached: function()
  {
    return this.groupPanelsAttached === true;
  },

  /**
   * Processes the given therapies (usually those, presented in the order basket) and check each therapy if any of them
   * has a matching therapy (based on the source id) in any of the left side group therapy panels. If the source therapy
   * is found, mark it to recreate the (visual) state that occurs when we add a therapy to the basket (list contents).
   * In order to speed things along and reduce the number of walks trough various arrays, we first create a map
   * of all source ids for each unique group, then process each group in one pass.
   *
   * @param {Array<app.views.medications.reconciliation.dto.MedicationGroupTherapy>} therapies
   * @protected
   */
  markSourceTherapies: function(therapies)
  {
    therapies.forEach(
        function(therapy)
        {
          var sourceContainer = this.findSourceMedicationTherapyContainer(therapy.getSourceId());
          if (sourceContainer)
          {
            this.markSourceTherapyContainer(sourceContainer);
          }
        },
        this);
  },

  /**
   * Generates and attaches any additional (client side) validation issues to the basket order item. One such validation
   * is in case of the variable days dose. Ensuring the protocol is a descriptive dose protocol has to be checked every time
   * we add an order to the basket, since the order forms don't validate the type of the protocol.
   * @param {app.views.medications.reconciliation.dto.MedicationGroupTherapy} orderItem
   * @protected
   */
  attachClientSideValidationIssues: function(orderItem){
    var protocolValidationIssueEnum =
        app.views.medications.TherapyEnums.validationIssueEnum.INPATIENT_PROTOCOL_NOT_SUPPORTED;
    if (this._isTherapyWithUnsupportedVariableDaysDose(orderItem.getTherapy()) &&
        orderItem.getValidationIssues().indexOf(protocolValidationIssueEnum) < 0)
    {
      orderItem.getValidationIssues().push(protocolValidationIssueEnum)
    }
  },

   /**
   * Creates a new therapy group panel for each given group and adds the component to the given container.
   * @param {tm.jquery.Container} parentContainer to attach the panels to.
   * @param {Array<app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup|
   * app.views.medications.reconciliation.dto.MedicationGroup>} groups from which to build panels.
   * @private
   */
  _createAndAddSourceGroupTherapyPanels: function(parentContainer, groups)
  {
    var self = this;
    var view = this.getView();
    var informationSourceFilterFactory =
        new app.views.medications.reconciliation.InformationSourceDefaultsFilterFactory()
            .setTemplateContext(this.getTemplateContext());

    var defaultDischargeMedicationInformationSources =
        view.getInformationSourceHolder()
            .getSources(informationSourceFilterFactory.buildPreviousDischargeFilter());

    var defaultLastHospitalizationInformationSources =
        view.getInformationSourceHolder()
            .getSources(informationSourceFilterFactory.buildPreviousHospitalizationFilter());

    groups.forEach(
        function createGroupPanel(medicationOnAdmissionGroup)
        {
          if (medicationOnAdmissionGroup.isLastDischargeMedicationsGroup())
          {
            medicationOnAdmissionGroup.applyDefaultInformationSource(defaultDischargeMedicationInformationSources);
          }
          if (medicationOnAdmissionGroup.isLastHospitalizationTherapiesGroup())
          {
            medicationOnAdmissionGroup.applyDefaultInformationSource(defaultLastHospitalizationInformationSources);
          }

          parentContainer.add(new app.views.medications.common.TherapyGroupPanel({
            groupTitle: app.views.medications.MedicationUtils.createUpdatableEntityTitle(
                view,
                medicationOnAdmissionGroup.getName(),
                medicationOnAdmissionGroup.getLastUpdateTime()),
            groupId: medicationOnAdmissionGroup.getGroupEnum(),
            view: this.getView(),
            contentData: medicationOnAdmissionGroup.getGroupElements(),
            attachElementToolbar: function(container)
            {
              self._attachGroupPanelElementToolbar(container);
            }
          }));

          this.mapSourceGroupTherapies(medicationOnAdmissionGroup);
        }, this);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @return {boolean} true, if the therapy contains variable days dose definition specific for the inpatient prescribing,
   * which is not supported and should be converted to a discharge protocol.
   * @private
   */
  _isTherapyWithUnsupportedVariableDaysDose: function(therapy)
  {
    if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements()))
    {
      return !app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(therapy.getTimedDoseElements());
    }
    return false;
  },

  /**
   * @private
   * @param {app.views.medications.ordering.AbstractTherapyOrder} orderData
   * @return {boolean}
   */
  _isOrderDataFromBasket: function(orderData)
  {
    return orderData instanceof app.views.medications.reconciliation.dto.MedicationOnAdmission;
  }
});