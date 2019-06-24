Class.define('app.views.medications.ordering.ComplexTherapyEditContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "complex-therapy-edit-container",
  scrollable: 'vertical',

  /** configs */
  view: null,
  medicationData: null, /** @param {Array<app.views.medications.common.dto.MedicationData>} medicationData */
  therapy: null,
  copyTherapy: false,
  isPastMode: false,
  therapyModifiedInThePast: false,
  saveTherapyFunction: null, //optional
  /** privates */
  resultCallback: null,
  editingStartTimestamp: null,
  /** privates: components */
  complexTherapyContainer: null,
  performerContainer: null,
  saveDateTimePane: null,

  _renderConditionTask: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    var appFactory = this.view.getAppFactory();

    this._buildComponents();
    this._buildGui();

    this.complexTherapyContainer.setMedicationDataFromTherapy(this.getTherapy(), this.getMedicationData());
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentValue();
            self._renderConditionTask = null;
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
    var view = this.getView();

    this.editingStartTimestamp = CurrentTime.get();
    this.editingStartTimestamp.setSeconds(0);
    this.editingStartTimestamp.setMilliseconds(0);

    this.complexTherapyContainer = new app.views.medications.ordering.ComplexTherapyContainer({
      view: view,
      startProcessOnEnter: true,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      editMode: true,
      changeReasonAvailable: this.getTherapy().isLinkedToAdmission() && !this.copyMode,
      copyMode: this.copyTherapy === true,
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: this.isPastMode,
        doseCalculationsAvailable: view.isDoseCalculationsEnabled(),
        reviewReminderAvailable: this.copyTherapy === true,
        recordAdministrationAvailable: this.copyTherapy === true
      }),
      referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({
        view: view
      }),
      additionalMedicationSearchFilter: app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION,
      getTherapyStartNotBeforeDateFunction: function()
      {
        return self.isPastMode ? null : self._getEditTimestamp();
      },
      confirmOrderEventCallback: this._onComplexTherapyContainerConfirmOrder.bind(this),
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
      var careProfessionals = view.getCareProfessionals();
      var currentUserAsCareProfessionalName = view.getCurrentUserAsCareProfessional() ?
          view.getCurrentUserAsCareProfessional().name :
          null;
      this._performerContainer = app.views.medications.MedicationUtils.createPerformerContainer(
          view, careProfessionals, currentUserAsCareProfessionalName);
    }

    this.saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane();
    this.saveDateTimePane.hide();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', "stretch", 0));

    this.add(this.complexTherapyContainer);
    if (this._performerContainer)
    {
      this.add(this._performerContainer);
    }
    this.add(this.saveDateTimePane);
  },

  _getEditTimestamp: function()
  {
    return this.saveDateTimePane.isHidden() ?
        moment(CurrentTime.get()).startOf('minute') :
        this.saveDateTimePane.getSaveDateTime();
  },

  _presentValue: function()
  {
    this.complexTherapyContainer.setComplexTherapy(
        this.getTherapy(),
        this.therapyModifiedInThePast,
        this.isPastMode);
  },

  /**
   * Event handler for {@link app.views.medications.ordering.ComplexTherapyContainer#confirmOrderEventCallback}.
   * @param {app.views.medications.ordering.ConfirmOrderEventData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} confirmEventData
   * @private
   */
  _onComplexTherapyContainerConfirmOrder: function(confirmEventData)
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

    modifiedTherapy.setLinkName(null); // copies of linked therapies must be unlinked, otherwise task creation fails

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

  _abortRenderConditionTask: function()
  {
    if (!tm.jquery.Utils.isEmpty(this._renderConditionTask))
    {
      this._renderConditionTask.abort();
      this._renderConditionTask = null;
    }
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
   * @returns {Array<app.views.medications.common.dto.MedicationData>}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this.complexTherapyContainer.validateAndConfirmOrder();
  },
  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    this._abortRenderConditionTask();
  }
});

