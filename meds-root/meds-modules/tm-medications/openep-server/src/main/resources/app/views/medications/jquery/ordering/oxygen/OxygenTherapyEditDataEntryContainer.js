Class.define('app.views.medications.ordering.oxygen.OxygenTherapyEditDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "edit-oxygen-therapy-dialog",
  scrollable: 'vertical',

  therapy: null,
  saveTherapyFunction: null,
  pastMode: false, /* required by DRP */
  copyMode: false, /* set true if we're copying instead of editing the therapy */
  modifiedInThePast: false,

  _oxygenTherapyContainer: null,
  _renderConditionTask: null,
  _restApi: null,
  _performerContainer: null, /* required by DRP */
  _saveDateTimePane: null, /* requred by DRP */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    var appFactory = this.getView().getAppFactory();

    this._buildGUI();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._abortRenderConditionTask();
      self._renderConditionTask = appFactory.createConditionTask(
          function()
          {
            self._presentData();
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

  _buildGUI: function()
  {
    var view = this.getView();
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    this._oxygenTherapyContainer = new app.views.medications.ordering.OxygenTherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      view: view,
      orderMode: false,
      orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
        pastMode: this.isPastMode(),
        doseCalculationsAvailable: view.isDoseCalculationsEnabled()
      }),
      changeReasonAvailable: this.getTherapy().isLinkedToAdmission() && !this.isCopyMode()
    });
    this._oxygenTherapyContainer.on(
        app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE,
        function()
        {
          self._saveDateTimePane.setHeight(34);
          self._saveDateTimePane.setPadding('4 0 0 0');
          self._saveDateTimePane.show();
        }
    );

    this.add(this._oxygenTherapyContainer);

    if (this.isPastMode())
    {
      var careProfessionals = view.getCareProfessionals();
      var currentUserAsCareProfessionalName = view.getCurrentUserAsCareProfessional() ?
          view.getCurrentUserAsCareProfessional().name : null;

      this._performerContainer =
          app.views.medications.MedicationUtils.createPerformerContainer(
              view,
              careProfessionals,
              currentUserAsCareProfessionalName);

      this.add(this._performerContainer);
    }

    this._saveDateTimePane = new app.views.medications.ordering.TherapySaveDatePane({
      hidden: true
    });
    this._saveDateTimePane.on(app.views.medications.ordering.TherapySaveDatePane.EVENT_TYPE_SAVE_DATE_CHANGE,
        function(component, componentEvent)
        {
          self._oxygenTherapyContainer.setMinimumStartTime(componentEvent.eventData.therapyStartDate)
        });
    this.add(this._saveDateTimePane);
  },

  _presentData: function()
  {
    var self = this;
    var medicationId = this.getTherapy().getMedication().getId();
    var showPreviousAdministrations = !this.isCopyMode() &&
        (this.getTherapy().isStarted() || this.isModifiedInThePast());

    this.getView().getRestApi().loadMedicationData(medicationId)
        .then(function onMedicationLoad(medicationData)
        {
          if (self.isRendered())
          {
            self._oxygenTherapyContainer.setMedicationData(medicationData);
            self._oxygenTherapyContainer.setOxygenTherapy(self.getTherapy(), showPreviousAdministrations, self.isCopyMode());
            if (!self.isCopyMode() && !self.isPastMode())
            {
              self._oxygenTherapyContainer.setMinimumStartTime(moment(CurrentTime.get()).startOf('minute').toDate());
            }
          }
        });
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
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} orderData
   * @param {Object} prescriber
   * @param {Date} saveDateTime
   * @returns {tm.jquery.Promise}
   * @private
   */
  _placeOrder: function(orderData, prescriber, saveDateTime)
  {
    if (this.isCopyMode())
    {
      var medicationOrder = [
        new app.views.medications.ordering.dto.SaveMedicationOrder({
          therapy: orderData.getTherapy(),
          actionEnum: orderData.isRecordAdministration() ?
              app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE_AND_ADMINISTER :
              app.views.medications.TherapyEnums.medicationOrderActionEnum.PRESCRIBE
        })
      ];

      return this.getView()
          .getRestApi()
          .saveMedicationsOrder(medicationOrder, prescriber, saveDateTime, null, false);
    }
    else
    {
      orderData.getTherapy().setCompositionUid(this.getTherapy().getCompositionUid());
      orderData.getTherapy().setEhrOrderName(this.getTherapy().getEhrOrderName());

      return this.getView()
          .getRestApi()
          .modifyTherapy(
              orderData.getTherapy(),
              orderData.getTherapyChangeReason(),
              prescriber,
              saveDateTime,
              false);
    }
  },

  /**
   * @returns {boolean}
   */
  isPastMode: function()
  {
    return this.pastMode === true;
  },

  /**
   * @returns {boolean}
   */
  isCopyMode: function()
  {
    return this.copyMode === true;
  },

  /**
   * @returns {boolean}
   */
  isModifiedInThePast: function()
  {
    return this.modifiedInThePast === true;
  },

  destroy: function()
  {
    this._abortRenderConditionTask();
    this.callSuper();
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;

    this._oxygenTherapyContainer.setConfirmOrderEventCallback(
        function onProcessResult(confirmEventData)
        {
          if (!confirmEventData.isValidationPassed())
          {
            resultDataCallback(new app.views.common.AppResultData({success: false}));
          }
          else
          {
            var performer = self._performerContainer != null ?
                self._performerContainer.getPerformer() :
                self.getView().getCurrentUserAsCareProfessional();
            var therapyOrder = confirmEventData.getTherapyOrder();

            if (self.saveTherapyFunction)
            {
              self.saveTherapyFunction(therapyOrder.getTherapy(), performer);
              resultDataCallback(new app.views.common.AppResultData({success: true}));
            }
            else
            {
              self._placeOrder(
                  therapyOrder,
                  performer,
                  self._saveDateTimePane.isHidden() ? null : self._saveDateTimePane.getSaveDateTime())
                  .then(
                      function onSuccess()
                      {
                        resultDataCallback(new app.views.common.AppResultData({success: true}));
                      },
                      function onFail()
                      {
                        resultDataCallback(new app.views.common.AppResultData({success: false}));
                      }
                  );
            }
          }
        });

    if (!this.isCopyMode() && !this.isPastMode())
    {
      this._oxygenTherapyContainer.adjustStartTimeToOrderTime(
          !this._saveDateTimePane.isHidden() ?
              this._saveDateTimePane.getSaveDateTime() :
              null);
    }
    this._oxygenTherapyContainer.validateAndConfirmOrder();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenTherapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  }
});
