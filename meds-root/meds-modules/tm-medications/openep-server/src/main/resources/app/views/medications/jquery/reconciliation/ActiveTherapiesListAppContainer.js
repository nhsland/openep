Class.define('app.views.medications.reconciliation.ActiveTherapiesListAppContainer', 'app.views.common.containers.AppContainer', {
  cls: 'active-therapies-list-container',
  view: null,
  scrollable: "vertical",

  _displayProvider: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._displayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView()
    });
    this.setFlex(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, this._onRender.bind(this));

  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Executed when the component is rendered. Since this is used as a tooltip, the render event is executed
   * each time the tooltip is displayed, hence we first remove all existing content, then trigger data reloading.
   * @private
   */
  _onRender: function()
  {
    this.removeAll();
    this.getView()
        .showLoaderMask(this, true, 100);
    this.getView()
        .getRestApi()
        .loadActiveTherapies(true)
        .then(this._applyActiveTherapies.bind(this));
  },

  /**
   * @param {Array<app.views.medications.timeline.TherapyRow>} therapyRows
   * @private
   */
  _applyActiveTherapies: function(therapyRows)
  {
    var view = this.getView();
    var defaultDisplayProvider = this._displayProvider;
    therapyRows
        .map(
            /**
             * @param {app.views.medications.timeline.TherapyRow} row
             * @return {app.views.medications.common.therapy.TherapyContainer}
             */
            function toContainer(row)
            {
              return new app.views.medications.common.therapy.TherapyContainer({
                showIconTooltip: false,
                view: view,
                data: row,
                displayProvider: defaultDisplayProvider
              })
            })
        .forEach(
            function(container)
            {
              this.add(container);
            },
            this);
    view.hideLoaderMask(this);
    this.repaint();
  }
});