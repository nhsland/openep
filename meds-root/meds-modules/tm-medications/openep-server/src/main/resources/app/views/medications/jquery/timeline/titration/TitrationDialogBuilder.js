Class.define('app.views.medications.timeline.titration.TitrationDialogBuilder', 'tm.jquery.Object', {
  view: null,
  therapy: null,
  administration: null,

  _titrationType: null,
  _administrationType: null,
  _scheduleAdditional: false,
  _applyUnplanned: false,
  _stopFlow: false,
  _lastPositiveInfusionRate: null,
  _activeContinuousInfusion: false,
  _administrations: null,
  _reviewedUntil: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Builds and displays the dialog, based on the options given to the builder.
   * @returns {tm.jquery.Promise} Promise, which will be resolved when the dialog closes.
   */
  showDialog: function()
  {
    return this._showTitrationAdministrationDialog();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.dto.Therapy} the titrated therapy
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   *
   * @return {Object} the administration for this dialog
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * Will be used if set, otherwise the titration type from the given therapy is used.
   * @param {String} type {@link app.views.medications.TherapyEnums.therapyTitrationTypeEnum}.
   * @return {app.views.medications.timeline.titration.TitrationDialogBuilder}
   */
  setTitrationType: function(type)
  {
    this._titrationType = type;
    return this;
  },

  /**
   * @param {String} type {@link app.views.medications.TherapyEnums.administrationTypeEnum}
   * @param {Boolean} [scheduleAdditional=false]
   * @param {Boolean} [applyUnplanned=false]
   * @param {Boolean} [stopFlow=false]
   * @param {Date|null} [reviewedUntil=null]
   * @return {app.views.medications.timeline.titration.TitrationDialogBuilder}
   */
  setAdministrationMode: function(type, scheduleAdditional, applyUnplanned, stopFlow, reviewedUntil)
  {
    this._administrationType = type;
    this._scheduleAdditional = scheduleAdditional === true;
    this._applyUnplanned = applyUnplanned === true;
    this._stopFlow = stopFlow === true;
    this._reviewedUntil = reviewedUntil;
    return this;
  },

  /**
   * @param {number|null} [lastPositiveInfusionRate=null]
   * @param {boolean} [activeContinuousInfusion=false]
   * @return {app.views.medications.timeline.titration.TitrationDialogBuilder}
   */
  setInfusionState: function(lastPositiveInfusionRate, activeContinuousInfusion)
  {
    this._lastPositiveInfusionRate = tm.jquery.Utils.isNumeric(lastPositiveInfusionRate) ? lastPositiveInfusionRate : null;
    this._activeContinuousInfusion = activeContinuousInfusion === true;
    return this;
  },

  /**
   * Sets the available list of administration tasks for the given therapy.
   * @param {Array<Object>} administrations
   */
  setAllAdministrationTasks: function(administrations)
  {
    this._administrations = administrations;
    return this;
  },

  /**
   * @returns {tm.jquery.Promise} Promise, which will be resolved when the dialog closes.
   * @private
   */
  _showTitrationAdministrationDialog: function()
  {
    var view = this.getView();
    var dialogResultDeferred = new tm.jquery.Deferred();
    var self = this;
    var titrationType = this._titrationType || this.getTherapy().getTitration();

    var titrationDataLoader = new app.views.medications.timeline.titration.TitrationDataLoader({
      view: view,
      therapyId: this.getTherapy().getTherapyId(),
      titrationType: titrationType
    });

    // add offset to make sure the data is clearly visible
    var initialEndInterval =
        moment(CurrentTime.get())
            .add(app.views.medications.MedicationTimingUtils.getTitrationOffsetMinutesByType(titrationType), 'minutes')
            .toDate();

    titrationDataLoader
        .init(initialEndInterval)
        .then(function()
        {
          self._buildTitrationAdministrationDialog(titrationDataLoader, dialogResultDeferred).show();
        });

    return dialogResultDeferred.promise();
  },

  /**
   * Builds the actual dialog.
   * @param {app.views.medications.timeline.titration.TitrationDataLoader} titrationDataLoader
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @returns {app.views.common.dialog.AppDialog}
   * @private
   */
  _buildTitrationAdministrationDialog: function(titrationDataLoader, dialogResultDeferred)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var dialogContentContainer = new app.views.medications.timeline.titration.TitrationBasedAdministrationDataEntryContainer({
      view: view,
      therapy: this.getTherapy(),
      administration: this.getAdministration(),
      administrationType: this._administrationType,
      allAdministrations: this._administrations,
      titrationData: titrationDataLoader.getCurrentData(),
      dataLoader: titrationDataLoader,
      lastPositiveInfusionRate: this._lastPositiveInfusionRate,
      activeContinuousInfusion: this._activeContinuousInfusion,
      enableDosing: !tm.jquery.Utils.isEmpty(this._administrationType),
      scheduleAdditional: this._scheduleAdditional,
      applyUnplanned: this._applyUnplanned,
      stopFlow: this._stopFlow,
      therapyReviewedUntil: this._reviewedUntil
    });

    var titrationAdministrationDialog = appFactory.createDataEntryDialog(
        view.getDictionary("dose.titration"),
        null,
        dialogContentContainer,
        function(resultData)
        {
          dialogResultDeferred.resolve(resultData);
        },
        dialogContentContainer.getDefaultWidth(),
        dialogContentContainer.getDefaultHeight()
    );
    titrationAdministrationDialog.setContainmentElement(view.getDom());
    titrationAdministrationDialog.setCls("invisible");

    return titrationAdministrationDialog;
  }
});