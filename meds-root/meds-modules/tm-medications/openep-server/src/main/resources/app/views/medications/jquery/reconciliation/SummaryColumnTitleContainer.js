Class.define('app.views.medications.reconciliation.SummaryColumnTitleContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_EDIT_RECONCILIATION_LIST: new tm.jquery.event.EventType({
      name: 'SummaryColumnTitleContainerEditList', delegateName: null
    }),
    EVENT_TYPE_REVIEW_RECONCILIATION_LIST: new tm.jquery.event.EventType({
      name: 'SummaryColumnTitleContainerReviewList', delegateName: null
    })
  },
  cls: "column-title",
  view: null,
  title: null,
  editable: false,
  reviewable: false,

  _titleComponent: null,
  _editListButton: null,
  _reviewListButton: null,
  _reviewedLabelComponent: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.registerEventTypes('app.views.medications.reconciliation.SummaryColumnTitleContainer', [
      {eventType: app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_EDIT_RECONCILIATION_LIST},
      {eventType: app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_REVIEW_RECONCILIATION_LIST}
    ]);
    this._buildGui();
  },

  /**
   * Configures the available actions. Either shows the start action, or the edit and review actions. The start and edit
   * action can be disabled, without effecting their visibility, but keep in mind their availability is controlled by
   * {@link app.views.medications.reconciliation.SummaryColumnTitleContainer#editable} during construction. Meanwhile, the
   * {@link app.views.medications.reconciliation.SummaryColumnTitleContainer#reviewable} property controls the availability
   * of the review action in the same manner.
   * @param {boolean} [emptyList=false]
   * @param {boolean} [reviewed=false]
   * @param {boolean} [preventEdit=false]
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  configureListActions: function(emptyList, reviewed, preventEdit)
  {
    if (this._editListButton)
    {
      this._editListButton.setText(
          emptyList === true ?
              this.getView().getDictionary('start') :
              this.getView().getDictionary('edit')
      );
      this._editListButton.isRendered() ?
          this._editListButton.show() :
          this._editListButton.setHidden(false);

      this._editListButton.setEnabled(!preventEdit);
    }

    if (this._reviewListButton)
    {
      emptyList === true || reviewed === true ?
          this.hideReviewAction() :
          this.showReviewAction();
    }

    reviewed === true && emptyList !== true ?
        this.markReviewed() :
        this.markUnreviewed();

    return this;
  },

  /**
   * Applies the given value as the last list update date in the column title.
   * @param {Date|null} date
   */
  applyLastListUpdateTime: function(date)
  {
    this._titleComponent.setHtml(this._buildTitleHtml(date));
  },

  /**
   * Shows the review button, if the option is present - which is defined by the {@link #reviewable} property. Otherwise
   * nothing happens.
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  showReviewAction: function()
  {
    if (!this._reviewListButton)
    {
      return this;
    }

    this.isRendered() ?
        this._reviewListButton.show() :
        this._reviewListButton.setHidden(false);

    return this;
  },

  /**
   * Hides the review button, if the option is present - which is defined by the {@link #reviewable} property. Otherwise
   * nothing happens.
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  hideReviewAction: function()
  {
    if (!this._reviewListButton)
    {
      return this;
    }

    this.isRendered() ?
        this._reviewListButton.hide() :
        this._reviewListButton.setHidden(true);

    return this;
  },

  /**
   * Hide any action buttons that are currently visible.
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  hideAllActions: function()
  {
    this.hideReviewAction();
    this.markUnreviewed();
    if (this._editListButton)
    {
      this.isRendered() ?
          this._editListButton.hide() :
          this._editListButton.setHidden(true);
    }
    return this;
  },

  /**
   * Show the reviewed status, regardless if the value of {@link #reviewable}.
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  markReviewed: function()
  {
    this.isRendered() ?
        this._reviewedLabelComponent.show() :
        this._reviewedLabelComponent.setHidden(false);
    return this;
  },

  /**
   * Hide the reviewed status.
   * @return {app.views.medications.reconciliation.SummaryColumnTitleContainer}
   */
  markUnreviewed: function()
  {
    this.isRendered() ?
        this._reviewedLabelComponent.hide() :
        this._reviewedLabelComponent.setHidden(true);
    return this;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {String}
   */
  getTitle: function()
  {
    return this.title;
  },

  /**
   * Defines if the edit or start action button is available. If not, using methods that apply visibility of the button
   * has no effect.
   * @return {boolean}
   */
  isEditable: function()
  {
    return this.editable === true;
  },

  /**
   * Defines if the review action button is available. If not, using the methods to apply visibility for the button
   * has no effect. Does not affect the display of the review status, which is always shown, if the particular list
   * has already been reviewed.
   * @return {boolean}
   */
  isReviewable: function()
  {
    return this.reviewable === true;
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    var view = this.getView();
    var self = this;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-end", "center"));

    this._titleComponent = new tm.jquery.Component({
      cls: "PortletHeading1",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: this._buildTitleHtml()
    });
    this.add(this._titleComponent);

    var actionButtonsFlexFlowWrapper = new tm.jquery.Container({
      cls: 'actions-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 0, tm.jquery.flexbox.FlexFlow.create('row', 'wrap')),
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto")
    });

    if (this.isReviewable())
    {
      var reviewListDebouncedTask = view.getAppFactory().createDebouncedTask(
          "app.views.medications.reconciliation.SummaryColumnTitleContainer.reviewListDebouncedTask",
          function()
          {
            self._fireButtonClickBasedEvent(
                app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_REVIEW_RECONCILIATION_LIST);
          },
          0,
          250
      );

      this._reviewListButton = new tm.jquery.Button({
        text: view.getDictionary("review"),
        type: "link",
        hidden: true,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        handler: function()
        {
          reviewListDebouncedTask.run();
        }
      });

      actionButtonsFlexFlowWrapper.add(this._reviewListButton);
    }

    this._reviewedLabelComponent = new tm.jquery.Component({
      cls: 'review-status TextData light',
      hidden: true,
      html: view.getDictionary('reviewed.passive')
    });
    actionButtonsFlexFlowWrapper.add(this._reviewedLabelComponent);

    if (this.isEditable())
    {
      var editListDebouncedTask = view.getAppFactory().createDebouncedTask(
          "app.views.medications.reconciliation.SummaryColumnTitleContainer.editListDebouncedTask",
          function()
          {
            self._fireButtonClickBasedEvent(
                app.views.medications.reconciliation.SummaryColumnTitleContainer.EVENT_TYPE_EDIT_RECONCILIATION_LIST);
          },
          0,
          250
      );

      this._editListButton = new tm.jquery.Button({
        text: view.getDictionary("start"),
        type: "link",
        hidden: true,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        handler: function()
        {
          editListDebouncedTask.run();
        }
      });

      actionButtonsFlexFlowWrapper.add(this._editListButton);
    }

    this.add(actionButtonsFlexFlowWrapper);
  },

  /**
   * @param {Date|null} lastUpdateDate
   * @return {string|null} that should be used as the html content of the column title. Either contains the title and
   * the last update date in brackets, or just the title as defined with {@link #getTitle}.
   * @private
   */
  _buildTitleHtml: function(lastUpdateDate)
  {
    return app.views.medications.MedicationUtils.createUpdatableEntityTitle(
        this.getView(),
        this.getTitle(),
        lastUpdateDate);
  },

  /**
   * Simplified fireEvent for events which require no event data.
   * @param {Object} eventType
   * @private
   */
  _fireButtonClickBasedEvent: function(eventType)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: eventType,
      eventData: null
    }), null);
  }
});