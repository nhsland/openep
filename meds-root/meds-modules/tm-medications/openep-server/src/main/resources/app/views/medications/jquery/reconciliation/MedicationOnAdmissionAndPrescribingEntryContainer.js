Class.define('app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    VIEW_MODE_ADMISSION: 0,
    VIEW_MODE_INPATIENT: 1
  },
  /** @type number */
  padding: 0,
  /** @type string */
  cls: 'reconciliation-dialog',
  /** @type app.views.common.AppView */
  view: null,
  /** @type number */
  activeViewMode: NaN,
  /** @type app.views.common.dialog.AppDialog */
  dialog: null,

  /** @type tm.jquery.CardContainer */
  _cardContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.activeViewMode)
    {
      this.activeViewMode =
          app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer.VIEW_MODE_ADMISSION;
    }

    this._buildGUI();
    this._loadData();
  },

  /**
   * Callback when the continue button on the footer is pressed. Switches to the inpatient prescribing slide.
   * @param {app.views.medications.reconciliation.ContinueConfirmCancelFooterButtonsContainer} footer
   */
  onButtonContinuePressed: function(footer)
  {
    var view = this.view;
    var staticEnums = app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer;
    var cardContainer = this._cardContainer;
    var admissionContainer = cardContainer.getActiveItem();
    var dialog = this.getDialog();

    footer.getContinueButton().setEnabled(false);
    footer.getConfirmButton().setEnabled(false);

    admissionContainer
        .saveBasketContent()
        .then(
            function activatePrescribingSlide()
            {
              footer.getContinueButton().setEnabled(true);
              footer.getConfirmButton().setEnabled(true);
              cardContainer.setActiveIndex(staticEnums.VIEW_MODE_INPATIENT);
              dialog.setTitle(view.getDictionary("prescribe.medications"));
              footer.getContinueButton().hide();
              cardContainer.getActiveItem().loadSourceMedicationOnAdmission();
            },
            function stayOnSameSlide()
            {
              footer.getContinueButton().setEnabled(true);
              footer.getConfirmButton().setEnabled(true);
            }
        );
  },

  processResultData: function(resultDataCallback)
  {
    var staticEnums = app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer;

    if (this._cardContainer.getActiveIndex() === staticEnums.VIEW_MODE_ADMISSION)
    {
      this._processAdmissionSlideResultData(resultDataCallback);
    }
    else
    {
      this._processInpatientSlideResultData(resultDataCallback);
    }
  },

  /**
   * @param {app.views.common.dialog.AppDialog} value
   */
  setDialog: function(value)
  {
    this.dialog = value;
  },

  /**
   * @return {app.views.common.dialog.AppDialog}
   */
  getDialog: function()
  {
    return this.dialog;
  },

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var cardContainer = new app.views.medications.reconciliation.CardContainer({
      activeIndex: this.activeViewMode,
      animation: 'slide-horizontal-new',
      animationDuration: 700,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this._buildSlides(cardContainer);
    this.add(cardContainer);

    this._cardContainer = cardContainer;
  },

  /**
   * Constructs the slides for each step and adds them to the card container.
   * @param {app.views.medications.reconciliation.CardContainer|tm.jquery.CardContainer} slideContainer
   * @private
   */
  _buildSlides: function(slideContainer)
  {
    var container1 = new app.views.medications.reconciliation.MedicationOnAdmissionContainer({
      view: this.view
    });

    var container2 = new app.views.medications.reconciliation.MedicationOnAdmissionPrescribingContainer({
      view: this.view
    });

    slideContainer.add(container1);
    slideContainer.add(container2);
  },

  _loadData: function()
  {
    var self = this;
    var staticEnums = app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer;
    var admissionContainer = self._cardContainer.getComponents()[staticEnums.VIEW_MODE_ADMISSION];

    admissionContainer.loadBasketContents();
  },

  /**
   * Handles dialog confirmation logic when the active slide is the medication on admission slide. Saves the list
   * and closes the dialog if successful.
   * @param {function} resultDataCallback
   * @private
   */
  _processAdmissionSlideResultData: function(resultDataCallback)
  {
    var staticEnums = app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer;
    var admissionContainer = this._cardContainer.getComponents()[staticEnums.VIEW_MODE_ADMISSION];

    admissionContainer
        .saveBasketContent()
        .then(
            function onSuccess()
            {
              resultDataCallback(new app.views.common.AppResultData({success: true, value: null}));
            },
            function onFailure()
            {
              resultDataCallback(new app.views.common.AppResultData({success: false, value: null}));
            }
        );
  },

  /**
   * Instructs the inpatient prescribing component to place an order and informs the dialog of the result.
   * @param {function} resultDataCallback
   * @private
   */
  _processInpatientSlideResultData: function(resultDataCallback)
  {
    var view = this.view;
    var staticEnums = app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer;
    var inpatientContainer = this._cardContainer.getComponents()[staticEnums.VIEW_MODE_INPATIENT];

    inpatientContainer
        .placeInpatientOrder()
        .then(
            function onPlaceInpatientOrderSuccess()
            {
              view.refreshPatientsCumulativeAntipsychoticPercentage();
              resultDataCallback(new app.views.common.AppResultData({success: true, value: null}));
            },
            function onPlaceInpatientOrderFailure()
            {
              resultDataCallback(new app.views.common.AppResultData({success: false, value: null}));
            });
  }
});
