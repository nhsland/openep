Class.define('app.views.medications.reconciliation.MedicationOnDischargeEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'reconciliation-dialog',

  view: null,
  dialog: null,
  dispenseSources: null,
  prescribeByTemplatesOnlyMode: false,

  _contentContainer: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isArray(this.dispenseSources))
    {
      throw new Error('dispenseSources not defined');
    }
    this._buildGUI();
    this._loadData();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function ()
  {
    return this.view;
  },

  /**
   * @return {Array<app.views.medications.common.dto.DispenseSource>}
   */
  getDispenseSources: function()
  {
    return this.dispenseSources;
  },

  /**
   * Should the prescribing be limited to template use only? In this mode, the user cannot prescribe anything outside
   * of the loaded templates, he can also not add a therapy to the order with edit, nor can he override any warnings or
   * save a new template.
   */
  isPrescribeByTemplatesOnlyMode: function()
  {
    return this.prescribeByTemplatesOnlyMode === true;
  },

  /**
   * @return {app.views.medications.reconciliation.MedicationOnDischargeContainer}
   */
  getContentContainer: function ()
  {
    return this._contentContainer;
  },

  /**
   * @return {app.views.common.dialog.AppDialog}
   */
  getDialog: function ()
  {
    return this.dialog;
  },

  /**
   * @param {app.views.common.dialog.AppDialog} value
   */
  setDialog: function (value)
  {
    this.dialog = value;
  },

  /**
   * Executed when the dialog confirmation button is clicked. Executes the save process.
   * @param {function} resultDataCallback
   */
  processResultData: function (resultDataCallback)
  {
    this
        .getContentContainer()
        .saveBasketContent()
        .then(
            function onSuccess()
            {
              resultDataCallback(new app.views.common.AppResultData({success: true, value: null}));
            },
            function onFailure()
            {
              resultDataCallback(new app.views.common.AppResultData({success: false, value: null}));
            });
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var contentContainer = new app.views.medications.reconciliation.MedicationOnDischargeContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.getView(),
      dispenseSources: this.getDispenseSources(),
      prescribeByTemplatesOnlyMode: this.isPrescribeByTemplatesOnlyMode(),
      includeInpatientTherapiesInWarningsSearch: false
    });

    this.add(contentContainer);

    this._contentContainer = contentContainer;
  },

  _loadData: function ()
  {
    this.getContentContainer().loadBasketContents();
  }
});