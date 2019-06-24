Class.define('app.views.medications.warnings.CurrentTherapiesWarningsDialogFactory', 'tm.jquery.Object', {
  view: null,
  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Builds and displays the dialog which displays current (prescribed) therapies warnings.
   */
  show: function()
  {
    var self = this;
    this.getView()
        .getRestApi()
        .loadCurrentTherapiesWarnings()
        .then(
            function onDataLoad(data)
            {
              self._build(data)
                  .show();
            }
        )
  },

  /**
   * Constructs the actual dialog with the provided warnings loaded from the server API.
   * @param {app.views.medications.warnings.dto.MedicationsWarnings} warnings
   * @returns {app.views.common.dialog.AppDialog}
   * @private
   */
  _build: function(warnings)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var dialogContentContainer = new app.views.medications.warnings.WarningsContainer({
      showHeader: false,
      warnings: warnings,
      view: view
    });

    var dialog = appFactory.createDefaultDialog(
        view.getDictionary('current.therapies.warnings'),
        null,
        appFactory.createContentAndFooterButtonsContainer(
            dialogContentContainer,
            appFactory.createCloseFooterButtonsContainer()),
        null,
        700,
        500
    );
    dialog.setContainmentElement(view.getDom());

    return dialog;
  }
});