Class.define('app.views.medications.grid.toolbar.GridTherapyContainerToolbar', 'app.views.medications.grid.toolbar.BaseGridTherapyContainerToolbar', {
  cls:'grid-therapy-toolbar',
  alignSelf: "stretch",
  style: "min-width: 130px; position: relative;", /* min width required to prevent validToComponent from wrapping */

  /** @type tm.jquery.Button */
  _editButton: null,
  /** @type tm.jquery.Button */
  _confirmButton: null,
  /** @type tm.jquery.Button */
  _abortButton: null,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * @override
   * @protected
   */
  disableActionButtons: function()
  {
    this._setEnabledAllActionButtons(false);
  },

  /* for override */
  _buildGUI: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapyContainer = this.getTherapyContainer();
    var data = therapyContainer.getData();
    var therapy = data.therapy;
    var enums = app.views.medications.TherapyEnums;
    var pharmacistReviewReferBack =
        data.therapyPharmacistReviewStatus === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK;
    var validTo = therapy.getEnd() ?
        view.getDisplayableValue(therapy.getEnd(), "short.date.time") :
        '<span style="font-size: 9px; color: #D3D3D3;">&diams;&diams;</span>';

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-end"));
    this._abortButton = null; // reset button instances in case of a refresh()
    this._confirmButton = null; // reset button instances in case of a refresh()
    this._editButton = null; // reset button instances in case of a refresh()

    var suspended = data.therapyStatus === enums.therapyStatusEnum.SUSPENDED;

    var showActionButtons = (data.doctorReviewNeeded || suspended) && !pharmacistReviewReferBack;
    if (showActionButtons)
    {
      var actionButtonContainer = new tm.jquery.Container({
        cls: "action-buttons",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 5),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });

      var editButton = new tm.jquery.Button({
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary("edit"), "bottom", view),
        cls: "btn-flat icon-edit edit-therapy-button",
        enabled: view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed(),
        width: 32,
        handler: function()
        {
          self.fireEditTherapyEvent();
        }
      });
      var abortButton = new tm.jquery.Button({
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary("stop"), "bottom", view),
        cls: "btn-flat icon-delete abort-therapy-button",
        enabled: view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed(),
        width: 32,
        handler: function()
        {
          self.fireStopTherapyEvent();
        }
      });

      var toolTipTextKey = suspended ? "reissue" : "confirm";
      var confirmButton = new tm.jquery.Button({
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary(toolTipTextKey), "bottom", view),
        cls: "btn-flat review-icon review-therapy-button",
        enabled: view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed(),
        width: 32,
        handler: function()
        {
          self.fireConfirmTherapyEvent();
        }
      });

      this._abortButton = abortButton;
      this._editButton = editButton;
      this._confirmButton = confirmButton;

      actionButtonContainer.add(editButton);
      actionButtonContainer.add(abortButton);

      if (view.doctorReviewEnabled === true || suspended)
      {
        actionButtonContainer.add(confirmButton);
      }

      this.add(actionButtonContainer);
    }
    else if (pharmacistReviewReferBack)
    {
      var prIconOptions = {
        background: {cls: "icon_pharmacist_review" },
        layers: [
          {hpos: "right", vpos: "bottom", cls: "status-icon icon_pharmacist_review_status"}
        ]
      };
      var pharmacistReviewIcon = new tm.jquery.Image({
        cls:'pharmacist-review-icon',
        cursor: "pointer",
        html: appFactory.createLayersContainerHtml(prIconOptions),
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("review.pharmacists.report"))
      });
      pharmacistReviewIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function ()
      {
        self.fireShowRelatedPharmacistReviewEvent()
      });
      this.add(pharmacistReviewIcon);
    }

    var validToComponent = new tm.jquery.Component({
      cls: "TextData",
      style: "position: absolute; top: 2px; right: 5px;",
      html: therapy.isAddToDischargeLetter() ? '<div class="icon_prescription"></div>' + validTo : validTo
    });
    this.add(validToComponent);

    this._appendTherapyTasksReminderContainer(data);
  },

  _appendTherapyTasksReminderContainer: function(data)
  {
    var self = this;
    var container = new tm.jquery.Container({
      cls:'append-task-reminder-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-end", "flex-end"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto")
    });
    var tasksContainer = new app.views.medications.common.therapy.TherapyTasksRemindersContainer({
      view: this.getView(),
      therapyData: data,
      tasks: data.tasks,
      offset: -20,
      tasksChangedEvent: function()
      {
        self.fireTasksChangedEvent();
      }
    });
    container.add(tasksContainer);
    this.add(container);
  },

  _setEnabledAllActionButtons: function(enabled){
    if (this._editButton != null) this._editButton.setEnabled(enabled);
    if (this._confirmButton != null) this._confirmButton.setEnabled(enabled);
    if (this._abortButton != null) this._abortButton.setEnabled(enabled);
  },
});