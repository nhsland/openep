/**
 * A specialized version of a therapy container toolbar, that minimizes displayed buttons to accommodate the lower profile of
 * an inline therapy container. Keeps the reissue and pharmacist review buttons exposed, and hides other actions into a drop
 * down menu.
 */
Class.define('app.views.medications.grid.toolbar.InlineTherapyContainerToolbar', 'app.views.medications.grid.toolbar.BaseGridTherapyContainerToolbar', {

  cls: 'inline-therapy-container-toolbar',
  alignSelf: 'flex-start',

  /** @type tm.jquery.Button */
  _confirmTherapyButton: null,
  /** @type tm.jquery.Button */
  _moreActionsButton: null,
  /** @type tm.jquery.Button */
  _showPharmacistReviewButton: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * Disables all action buttons to prevent multiple actions on the same therapy
   * @protected
   * @override
   */
  disableActionButtons: function()
  {
    if (!!this._confirmTherapyButton)
    {
      this._confirmTherapyButton.setEnabled(false);
    }
    if (!!this._moreActionsButton)
    {
      this._moreActionsButton.setEnabled(false);
    }
    if (!!this._showPharmacistReviewButton)
    {
      this._showPharmacistReviewButton.setEnabled(false);
    }
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-end"));

    var view = this.getView();
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var therapyContainer = this.getTherapyContainer();
    var data = therapyContainer.getData();

    var suspended = data.therapyStatus === enums.therapyStatusEnum.SUSPENDED;
    var pharmacistReviewReferBack =
        data.therapyPharmacistReviewStatus === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;
    var showActionButtons = (data.doctorReviewNeeded || suspended) && !pharmacistReviewReferBack;
    if (showActionButtons)
    {
      if (view.isDoctorReviewEnabled() || suspended)
      {
        this._confirmTherapyButton = new tm.jquery.Button({
          cls: 'btn-flat inline-confirm-therapy-icon',
          width: 16,
          height: 16,
        });
        this._confirmTherapyButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
        {
          self.fireConfirmTherapyEvent();
        });
        this.add(this._confirmTherapyButton);
      }
      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
      {
        this._addMoreActionsMenu();
      }
    }
    else if (pharmacistReviewReferBack)
    {
      this._showPharmacistReviewButton = new tm.jquery.Button({
        cls: 'btn-flat inline-show-pharmacist-review-icon',
        width: 16,
        height: 16
      });

      this._showPharmacistReviewButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        self.fireShowRelatedPharmacistReviewEvent();
      });
      this.add(this._showPharmacistReviewButton);
    }
  },

  /**
   * Creates and adds a button with a popup menu, containing edit and stop therapy actions.
   * @private
   */
  _addMoreActionsMenu: function()
  {
    var view = this.getView();
    var self = this;

    this._moreActionsButton = new tm.jquery.Button({
      width: 16,
      height: 16,
      cls: 'btn-flat menu-icon',
      testAttribute: 'more-actions-menu-button',
      handler: function(component, componentEvent, elementEvent)
      {
        tm.jquery.ComponentUtils.hideAllDropDownMenus(view);

        var popupMenu = view.getAppFactory().createPopupMenu();
        var editTherapyItem = new tm.jquery.MenuItem({
          text: view.getDictionary("edit.therapy"),
          iconCls: 'icon-edit',
          testAttribute: 'edit-therapy-menu-item',
          handler: function()
          {
            self.fireEditTherapyEvent();
          }
        });
        popupMenu.addMenuItem(editTherapyItem);

        var stopTherapyItem = new tm.jquery.MenuItem({
          text: view.getDictionary("stop.therapy"),
          iconCls: 'icon-delete',
          testAttribute: 'stop-therapy-menu-item',
          handler: function()
          {
            self.fireStopTherapyEvent();
          }
        });
        popupMenu.addMenuItem(stopTherapyItem);
        popupMenu.show(elementEvent);
      }
    });
    this.add(this._moreActionsButton);
  }
});