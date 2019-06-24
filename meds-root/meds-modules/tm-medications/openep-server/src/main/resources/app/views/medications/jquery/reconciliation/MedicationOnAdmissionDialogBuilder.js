Class.define('app.views.medications.reconciliation.MedicationOnAdmissionDialogBuilder', 'tm.jquery.Object', {
  view: null,
  withInpatientPrescribing: false,

  _resultCallback: null,

  /**
   * Returns a new instance of the medication on admission dialog builder. The contents of the dialog can either be
   * a simpler version which only supports editing the admission list, or the complex version which houses a wizard
   * that contains both creating the admission list in the firs step and prescribing inpatient therapies in the second
   * step (which is mainly intended to transfer medications on admission to inpatient therapies).
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Is inpatient prescribing available? See {@link #setWithInpatientPrescribing}.
   * @return {boolean}
   */
  isWithInpatientPrescribing: function()
  {
    return this.withInpatientPrescribing === true;
  },

  /**
   * Define if the inpatient prescribing should be available after the admission reconciliation list creation. In
   * that case the user will have an option of either saving the admission list and closing the dialog or saving and
   * continuing to inpatient prescribing based on the admission list.
   * @param {boolean} value
   * @returns {app.views.medications.reconciliation.MedicationOnAdmissionDialogBuilder}
   */
  setWithInpatientPrescribing: function(value)
  {
    this.withInpatientPrescribing = value === true;
    return this;
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Set the dialog result callback method.
   * @param {function} callback
   * @return {app.views.medications.reconciliation.MedicationOnAdmissionDialogBuilder}
   */
  setResultCallback: function(callback)
  {
    this._resultCallback = callback;
    return this;
  },

  /**
   * Returns a new instance of the admission medication reconciliation dialog.
   * @return {app.views.common.dialog.AppDialog}
   */
  create: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var contentWithFooter = this.isWithInpatientPrescribing() ?
        this._createContentAndFooterButtonContainerWithInpatientPrescribing() :
        this._createContentAndFooterButtonContainer();

    var dialog = appFactory.createDefaultDialog(
        view.getDictionary("medication.on.admission"),
        null,
        contentWithFooter,
        null,
        $(window).width() - 50,
        $(window).height() - 150);

    this._addButtonAccessorsToDialogInstance(dialog, contentWithFooter.getFooter());
    dialog.setContainmentElement(view.getDom());
    dialog.setFitSize(true);
    dialog.setHideOnEscape(false);

    if (this.isWithInpatientPrescribing())
    {
      contentWithFooter
          .getFooter()
          .getContinueButton()
          .setHandler(
              function()
              {
                contentWithFooter.getContent().onButtonContinuePressed(contentWithFooter.getFooter());
              });

      contentWithFooter.getContent().setDialog(dialog);
    }

    contentWithFooter
        .getFooter()
        .getConfirmButton()
        .setHandler(this._createFooterConfirmButtonHandler(contentWithFooter.getContent(), dialog));
    contentWithFooter
        .getFooter()
        .getCancelButton()
        .setHandler(this._createFooterCancelButtonHandler(dialog));

    return dialog;
  },

  /**
   * Code based and taken from {@link app.views.common.AppFactory#createDataEntryDialog}. The confirm button getter
   * seems to be required by it's handler created by {@see #_createFooterConfirmButtonHandler}.
   * @param {app.views.common.dialog.AppDialog} dialog
   * @param {app.views.common.containers.AppConfirmCancelFooterButtonsContainer} footer
   * @private
   */
  _addButtonAccessorsToDialogInstance: function(dialog, footer)
  {
    dialog.getConfirmButton = function()
    {
      return footer.getConfirmButton();
    };
    dialog.getCancelButton = function()
    {
      return footer.getCancelButton();
    };
  },

  /**
   * @return {app.views.common.containers.AppContentAndFooterButtonsContainer}
   * @private
   */
  _createContentAndFooterButtonContainerWithInpatientPrescribing: function()
  {
    var view = this.getView();
    return view.getAppFactory()
        .createContentAndFooterButtonsContainer(
            new app.views.medications.reconciliation.MedicationOnAdmissionAndPrescribingEntryContainer({
              view: view
            }),
            new app.views.medications.reconciliation.ContinueConfirmCancelFooterButtonsContainer({
              confirmText: view.getDictionary("confirm"),
              cancelText: view.getDictionary("cancel"),
              continueText: view.getDictionary("continue")
            })
        );
  },

  /**
   * @return {app.views.common.containers.AppContentAndFooterButtonsContainer}
   * @private
   */
  _createContentAndFooterButtonContainer: function()
  {
    var view = this.getView();
    return view.getAppFactory()
        .createContentAndFooterButtonsContainer(
            new app.views.medications.reconciliation.MedicationOnAdmissionEntryContainer({
              view: view
            }),
            view.getAppFactory().createConfirmCancelFooterButtonsContainer()
        );
  },

  /**
   * Code taken from {@link app.views.common.AppFactory#createDataEntryDialog}. The preventCloseAfterSuccess option
   * was removed from this version since we don't need it.
   * @param {app.views.common.containers.AppDataEntryContainer} contentContainer
   * @param {app.views.common.dialog.AppDialog} dialog
   * @return {function}
   * @private
   */
  _createFooterConfirmButtonHandler: function(contentContainer, dialog){
    var resultCallback = this._resultCallback;
    var lockHideEvent = false;
    // if close icon is used //
    dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_DIALOG_HIDE, function()
    {
      if (lockHideEvent !== true)
      {
        resultCallback(null);
      }
    });

    return function(button)
    {
      if (tm.jquery.Utils.isFunction(contentContainer.processResultData))
      {
        if (tm.jquery.Utils.isEmpty(button)) button = dialog.getConfirmButton();

        button.setEnabled(false);
        contentContainer.processResultData(function(resultData)
        {
          if (resultData instanceof app.views.common.AppResultData)
          {
            if (resultData.isSuccess())
            {
              lockHideEvent = true;
              resultCallback(resultData);
              button.setEnabled(true);
              dialog.hide();
            }
            else
            {
              setTimeout(function()
              {
                button.setEnabled(true);
              }, 500);
            }
          }
        });
      }
    };
  },

  /**
   * Code taken from {@link app.views.common.AppFactory#createDataEntryDialog}.
   * @param {app.views.common.dialog.AppDialog} dialog
   * @return {function}
   * @private
   */
  _createFooterCancelButtonHandler: function(dialog) {
    return function()
    {
      dialog.hide();
    };
  }
});