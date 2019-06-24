Class.define('app.views.medications.ordering.SimpleTherapyEditContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "simple-therapy-edit-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  therapy: null,
  copyTherapy: false,
  isPastMode: false,
  therapyModifiedInThePast: false,
  saveTherapyFunction: null, //optional
  medicationData: null, /** @param {Array<app.views.medications.common.dto.MedicationData>} medicationData */
  /** privates */
  resultCallback: null,
  medications: null,
  editingStartTimestamp: null,
  /** privates: components */
  simpleTherapyContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  _renderConditionTask: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    var appFactory = this.view.getAppFactory();
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));

    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentValue();
            self._renderConditionTask = null;
            self._testRenderCoordinator.insertCoordinator();
          },
          function()
          {
            return self.isRendered(true);
          },
          20, 1000
      );
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;

    this.editingStartTimestamp = CurrentTime.get();
    this.editingStartTimestamp.setSeconds(0);
    this.editingStartTimestamp.setMilliseconds(0);

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'simple-therapy-edit-container-coordinator',
      view: this.getView(),
      component: this,
      manualMode: true
    });

    this.simpleTherapyContainer = new app.views.medications.ordering.SimpleTherapyContainer({
      view: this.getView(),
      editMode: true,
      copyMode: this.copyTherapy,
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: this.isPastMode,
        doseCalculationsAvailable: this.getView().isDoseCalculationsEnabled(),
        reviewReminderAvailable: this.copyTherapy === true,
        recordAdministrationAvailable: this.copyTherapy === true
      }),
      referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: this.getView()}),
      changeReasonAvailable: this.therapy.isLinkedToAdmission() && !this.copyTherapy,
      maxDosePercentage: tm.jquery.Utils.isEmpty(this.therapy.getMaxDosePercentage()) ? null :
          this.therapy.getMaxDosePercentage(),
      showMaxDose: !tm.jquery.Utils.isEmpty(this.therapy.getMaxDosePercentage()),
      getTherapyStartNotBeforeDateFunction: function()
      {
        return self.isPastMode === true ? null : self._getEditTimestamp();
      },
      confirmOrderEventCallback: this._onSimpleTherapyContainerConfirmOrder.bind(this),
      saveDateTimePaneEvent: function()
      {
        self.saveDateTimePane.setHeight(34);
        self.saveDateTimePane.setPadding('4 0 0 0');
        self.saveDateTimePane.show();
        self.saveDateTimePane.repaint();
      }
    });

    if (this.isPastMode === true)
    {
      var careProfessionals = this.view.getCareProfessionals();
      var currentUserAsCareProfessionalName = this.view.getCurrentUserAsCareProfessional() ? this.view.getCurrentUserAsCareProfessional().name : null;
      this._performerContainer =
          app.views.medications.MedicationUtils.createPerformerContainer(this.view, careProfessionals, currentUserAsCareProfessionalName);
    }

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane({
      hidden: true
    });
  },

  _buildGui: function()
  {
    this.add(this.simpleTherapyContainer);
    this.add(new tm.jquery.Container({flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")}));
    if (this._performerContainer != null)
    {
      this.add(this._performerContainer);
    }
    this.add(this.saveDateTimePane);
  },

  /**
   * Event handler for {@link app.views.medications.ordering.SimpleTherapyContainer#confirmOrderEventCallback}.
   * @param {app.views.medications.ordering.ConfirmOrderEventData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} confirmEventData
   * @private
   */
  _onSimpleTherapyContainerConfirmOrder: function(confirmEventData)
  {
    if (!confirmEventData.isValidationPassed())
    {
      this._failureResultCallback();
      return;
    }

    var therapyOrder = confirmEventData.getTherapyOrder();

    if (typeof this.saveTherapyFunction === 'function')
    {
      this.saveTherapyFunction(therapyOrder.getTherapy(), this._getPrescriber());
      this._successResultCallback();
    }
    else if (this.copyTherapy === true)
    {
      this._prescribeTherapy(therapyOrder)
          .then(this._successResultCallback.bind(this), this._failureResultCallback.bind(this));
    }
    else
    {
      this._modifyTherapy(therapyOrder)
          .then(this._successResultCallback.bind(this), this._failureResultCallback.bind(this));
    }
  },

  /**
   * @param {app.views.medications.ordering.ConfirmOrderEventData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} modifiedEventData
   * @return {tm.jquery.Promise}
   * @private
   */
  _modifyTherapy: function(modifiedEventData)
  {
    var modifiedTherapy = modifiedEventData.getTherapy();

    modifiedTherapy.setCompositionUid(this.getTherapy().getCompositionUid());
    modifiedTherapy.setEhrOrderName(this.getTherapy().getEhrOrderName());

    return this.view.getRestApi()
        .modifyTherapy(
            modifiedTherapy,
            modifiedEventData.getTherapyChangeReason(),
            this._getPrescriber(),
            this._getSaveDateTime(),
            true);
  },

  /**
   * @param {app.views.medications.ordering.ConfirmOrderEventData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} modifiedEventData
   * @return {tm.jquery.Promise}
   * @private
   */
  _prescribeTherapy: function(modifiedEventData)
  {
    var modifiedTherapy = modifiedEventData.getTherapy();
    var medicationOrder = [
      new app.views.medications.ordering.dto.SaveMedicationOrder({
        therapy: modifiedTherapy,
        actionEnum: modifiedEventData.isRecordAdministration() ?
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER :
            app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
      })
    ];

    return this.view.getRestApi()
        .saveMedicationsOrder(medicationOrder, this._getPrescriber(), this._getSaveDateTime(), null, true)
  },

  /**
   * @return {{id: string, name: string}}
   * @private
   */
  _getPrescriber: function()
  {
    return this._performerContainer ? this._performerContainer.getPerformer() : this.view.getCurrentUserAsCareProfessional();
  },

  /**
   * @return {Date|null}
   * @private
   */
  _getSaveDateTime: function()
  {
    return this.saveDateTimePane.isHidden() ? null : this.saveDateTimePane.getSaveDateTime();
  },

  _successResultCallback: function()
  {
    this.resultCallback(new app.views.common.AppResultData({success: true}));
  },

  _failureResultCallback: function()
  {
    this.resultCallback(new app.views.common.AppResultData({success: false}));
  },

  /**
   * @return {Date}
   * @private
   */
  _getEditTimestamp: function()
  {
    return this.saveDateTimePane.isHidden() ?
        moment(CurrentTime.get()).startOf('minute') :
        this.saveDateTimePane.getSaveDateTime();
  },

  _presentValue: function()
  {
    var therapy = this.getTherapy().clone(true);

    var therapyHasAlreadyStarted = therapy.getStart() < this._getEditTimestamp();
    var setTherapyStart = !therapyHasAlreadyStarted || this.isPastMode === true;
    this.simpleTherapyContainer.setSimpleTherapy(
        therapy,
        this.getMedicationData(),
        setTherapyStart,
        this.therapyModifiedInThePast);
  },

  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.simpleTherapyContainer.validateAndConfirmOrder();
  },
  /**
   * @Override
   */
  destroy: function()
  {
    this._abortRenderConditionTask();
    this.callSuper();
  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationData>}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  }
});

