Class.define('app.views.medications.warnings.BaseWarningsContainer', 'tm.jquery.Container', {
  cls: "warnings-container",

  view: null,
  showHeader: true,

  _list: null,
  _messageContainer: null,
  _loadingStateAnimationImg: null,
  _messageLabel: null,
  _medicationsWarnings: null,
  _showAllSeverities: false,
  _warningsCounterContainer: null,
  _header: null,
  _showLowSeverityWarningsBtn: null,

  /** @type app.views.common.AppForm|tm.jquery.Form */
  _validationForm: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._medicationsWarnings = new app.views.medications.warnings.dto.MedicationsWarnings();
    this.buildGui();
  },

  /**
   * @protected
   */
  buildGui: function()
  {
    var self = this;
    var view = this.getView();
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this._list = new tm.jquery.List({
      cls: "warnings-list",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      autoLoad: false,
      dataSource: [],
      scrollable: "vertical",
      itemTpl: function(index, item)
      {
        return self.buildListRow(item);
      },
      hidden: true,
      selectable: false,
      emptyMsg: '<div class="TextData empty-message">' +
        this.getView().getDictionary('no.high.severity.warnings.found') + '</div>'
    });

    this._messageContainer = new tm.jquery.Container({
      cls: 'warnings-message-container',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0)
    });
    this._loadingStateAnimationImg = new tm.jquery.Container({
      testAttribute: 'warnings-loading-state',
      cls: 'loader',
      hidden: true
    });
    this._messageLabel = new tm.jquery.Label({
      style: 'color: #646464; text-transform: none;'
    });

    this._messageContainer.add(this._loadingStateAnimationImg);
    this._messageContainer.add(this._messageLabel);

    if (this.isShowHeader())
    {
      this._warningsCounterContainer = new tm.jquery.Container({
        cls: "warnings-counter",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
        html: this.buildSeverityWarningsCounterHtml()
      });

      var warningsHeaderContentContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 10),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        scrollable: "visible"
      });

      warningsHeaderContentContainer.add(this._warningsCounterContainer);

      this._header = new app.views.medications.ordering.MedicationsTitleHeader({
        title: view.getDictionary('warnings'),
        view: view,
        scrollable: "visible",
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        additionalDataContainer: warningsHeaderContentContainer
      });
    }

    this._showAllWarningsBtn = new tm.jquery.Button({
      cls: "show-all-warnings-btn",
      size: "large",
      text: view.getDictionary("show.all.warnings"),
      type: "link",
      hidden: true,
      handler: function()
      {
        self._showAllWarningsBtn.hide();
        self._showAllSeverities = true;
        self.getList().setListData(self.getMedicationWarnings().getAllWarnings());
      }
    });
    if (!!this._header)
    {
      this.add(this._header);
    }
    this.add(this.getList());
    this.add(this._showAllWarningsBtn);
    this.add(this.getMessageContainer());

    this._validationForm = this.getView().getAppFactory().createForm({
      name: 'override-reason-form',
      onValidationError: function()
      {
        var message = view.getDictionary('you.have.unchecked.warnings');
        view.getAppNotifier().warning(message, app.views.common.AppNotifierDisplayType.HTML, 320, 160);
      }
    });

  },

  /**
   * Recounts all the warnings by severity and updates the counter data.
   * @private
   */
  refreshSeverityCounter: function()
  {
    if (!this._warningsCounterContainer)
    {
      return;
    }

    this._warningsCounterContainer.setHtml(this.buildSeverityWarningsCounterHtml());
  },

  /**
   * @returns {String}
   * @private
   */
  buildSeverityWarningsCounterHtml: function()
  {
    return tm.jquery.Utils.formatMessage(
        this.getView().getDictionary("high.low.severity.warnings"),
        [this.getMedicationWarnings().getHighSeverityWarningsCount(),
          this.getMedicationWarnings().getLowSeverityWarningsCount()]);
  },

  /**
   * Override in extended class and provide the required implementation.
   *
   * @protected
   * @param {app.views.medications.warnings.dto.MedicationsWarning} item
   * @return {tm.jquery.Container}
   */
  buildListRow: function(item)
  {
    return new tm.jquery.Container({
       html: tm.jquery.Utils.escapeHtml(item.getDescription())
    });
  },

  /**
   * @protected
   * @returns {tm.jquery.List}
   */
  getList: function()
  {
    return this._list;
  },

  /**
   * @return {boolean}
   * @protected
   */
  isShowHeader: function()
  {
    return this.showHeader === true;
  },

  /**
   * @returns {app.views.medications.warnings.dto.MedicationsWarnings}
   */
  getMedicationWarnings: function()
  {
    return this._medicationsWarnings;
  },

  /**
   * Returns true after {@link _showAllWarningsBtn} is pressed
   * @returns {boolean}
   */
  isShowingAllSeverities: function()
  {
    return this._showAllSeverities === true;
  },

  /**
   * @param {Boolean} show
   */
  setShowAllSeverities: function(show)
  {
    this._showAllSeverities = show;
  },

  /**
   * @param {app.views.medications.warnings.dto.MedicationsWarnings} medicationWarnings
   */
  setMedicationWarnings: function(medicationWarnings)
  {
    this._medicationsWarnings = medicationWarnings
  },

  /**
   * @protected
   */
  showErrorState: function()
  {
    this._loadingStateAnimationImg.isRendered() ?
        this._loadingStateAnimationImg.hide() :
        this._loadingStateAnimationImg.setHidden(true);
    this._showInfoMessage(this.getView().getDictionary('error.unexpected'));
  },

  /**
   * @protected
   */
  showLoadingMask: function()
  {
    this._loadingStateAnimationImg.isRendered() ?
        this._loadingStateAnimationImg.show() :
        this._loadingStateAnimationImg.setHidden(false);
    this._showInfoMessage(this.getView().getDictionary('loading.warnings'));
  },

  /**
   * @protected
   */
  hideLoadingMask: function()
  {
    this._loadingStateAnimationImg.isRendered() ?
        this._loadingStateAnimationImg.hide() :
        this._loadingStateAnimationImg.setHidden(true);
    this._messageContainer.isRendered() ?
        this._messageContainer.hide() :
        this._messageContainer.setHidden(true);
    this._list.isRendered() ? this._list.show() : this._list.setHidden(false);
  },

  refreshWarningsList: function()
  {
    var warningsToShow = this.isShowingAllSeverities() ?
        this.getMedicationWarnings().getAllWarnings() :
        this.getMedicationWarnings().getHighSeverityWarnings();
    this.getList().setListData(warningsToShow);
    this.refreshSeverityCounter();
  },

  /**
   * Resets the state of the component - all warnings are removed, the severity counter is reset and nothing is shown.
   * Does not show the 'no warnings found' text since this state is used after successfully loading warnings.
   * @protected
   */
  clear: function()
  {
    this.setMedicationWarnings(new app.views.medications.warnings.dto.MedicationsWarnings());
    this._showAllWarningsBtn.isRendered() ? this._showAllWarningsBtn.hide() : this._showAllWarningsBtn.setHidden(true);
    this._list.isRendered() ? this._list.hide() : this._list.setHidden(true);
    this._list.setListData([]);
    this.refreshSeverityCounter();
  },

  handleLowSeverityWarningsBtnVisibility: function()
  {
    if (!this.isShowingAllSeverities() && this.getMedicationWarnings().getLowSeverityWarningsCount() > 0)
    {
      this._showAllWarningsBtn.isRendered() ?
          this._showAllWarningsBtn.show() :
          this._showAllWarningsBtn.setHidden(false);
    }
    else
    {
      this._showAllWarningsBtn.isRendered() ?
          this._showAllWarningsBtn.hide() :
          this._showAllWarningsBtn.setHidden(true);
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @protected
   * @returns {tm.jquery.Container}
   */
  getMessageContainer: function()
  {
    return this._messageContainer;
  },

  /**
   * @returns {app.views.common.AppForm|tm.jquery.Form}
   */
  getValidationForm: function()
  {
    return this._validationForm;
  },

  /**
   * @param {String} text
   * @private
   */
  _showInfoMessage: function(text)
  {
    this._messageLabel.setText(text);
    this._messageContainer.isRendered() ?
        this._messageContainer.show() :
        this._messageContainer.setHidden(false);
    this._list.isRendered() ? this._list.hide() : this._list.setHidden(true);
  }
});
