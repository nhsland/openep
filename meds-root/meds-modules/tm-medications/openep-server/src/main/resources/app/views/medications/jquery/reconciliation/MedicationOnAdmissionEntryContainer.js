Class.define('app.views.medications.reconciliation.MedicationOnAdmissionEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'reconciliation-dialog',

  view: null,

  _admissionContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGUI();
    this._loadData();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Save the basket contents as the active state of the medication on admission list and call the dialog's result
   * callback.
   * @param {function} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    this
        ._admissionContainer
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

  _buildGUI: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var contentContainer = new app.views.medications.reconciliation.MedicationOnAdmissionContainer({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this.getView()
    });

    this.add(contentContainer);

    this._admissionContainer = contentContainer;
  },

  _loadData: function()
  {
    this._admissionContainer.loadBasketContents();
  }
});