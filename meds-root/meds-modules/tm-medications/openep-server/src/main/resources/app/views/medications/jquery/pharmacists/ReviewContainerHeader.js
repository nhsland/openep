Class.define('app.views.medications.pharmacists.ReviewContainerHeader', 'tm.jquery.Container', {
  cls: 'header-container',
  /** @type app.views.medications.pharmacists.ReviewContainer */
  reviewContainer: null,
  /** @type boolean */
  editMode: false,
  /** @type boolean */
  editable: true,
  /** @type boolean */
  showReminders: true,
  /** @type app.views.common.AppView */
  view: null,

  /** @type function|null */
  editReviewEventCallback: null,
  /** @type function|null */
  deleteReviewEventCallback: null,
  /** @type function|null */
  confirmReviewEventCallback: null,
  /** @type function|null */
  cancelEditEventCallback: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    if (!this.editable && !!this.editMode) {
      throw new Error('invalid ReviewContainerHeader configuration: not editable, while in editMode');
    }

    this._buildGui(false);
    this._setKeyboardShortcuts();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var self = this;
    var appFactory = this.view.getAppFactory();
    var data = this.reviewContainer.getReviewData();
    var statusCls = data.isDraft() ? 'status-icon inprogress' : 'status-icon done';

    var layerContainerOptions = {
      background: {
        cls: 'document-icon'
      },
      layers: []
    };
    layerContainerOptions.layers.push({
      hpos: 'right', vpos: 'bottom', cls: statusCls,
      title: data.isDraft() ? this.view.getDictionary('Status.IN_PROGRESS') : this.view.getDictionary('Status.DONE')
    });

    var statusIcon = new tm.jquery.Image({
      html: appFactory.createLayersContainerHtml(layerContainerOptions),
      width: 48,
      height: 48
    });

    var titleContainer = new tm.jquery.Container({
      cls: 'title-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      html: '<div class="PortletHeading1">' + this.view.getDictionary('pharmacists.review') + '</div>' +
          '<div class="TextData">' + this.view.getDisplayableValue(new Date(data.getCreateTimestamp()), 'short.date.time')
          + (tm.jquery.Utils.isEmpty(data.getComposer()) ? '' : ', '.concat(data.getComposer().name)) + '</div>',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      alignSelf: 'stretch'
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    this.add(statusIcon);
    this.add(titleContainer);

    if (!this.editMode && this.showReminders && !tm.jquery.Utils.isEmpty(data.getReminderDate()))
    {
      var now = CurrentTime.get();
      now.setHours(0, 0, 0, 0);
      var reminderDate = new Date(data.getReminderDate().getTime());
      reminderDate.setHours(0, 0, 0, 0);

      if (reminderDate > now)
      {
        var oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds
        var diffDays = Math.abs((now.getTime() - reminderDate.getTime()) / (oneDay));

        var reminderText = new tm.jquery.Label({
          cls: 'TextData reminder-container',
          alignSelf: 'flex-start',
          html: this.view.getDictionary("reminder") + ": " + (diffDays <= 1 ?
              this.view.getDictionary("check.in.1.day") :
              this.view.getDictionary("check.in.x.days").replace("{0}", diffDays))
        });
        this.add(reminderText);
      }
    }

    if (this.view.getTherapyAuthority().isManagePatientPharmacistReviewAllowed() && this.editable)
    {
      if (this.editMode)
      {
        var confirmButton = new tm.jquery.Button({
          text: this.view.getDictionary('confirm.report'),
          handler: function ()
          {
            if (!tm.jquery.Utils.isEmpty(self.confirmReviewEventCallback))
            {
              self.confirmReviewEventCallback();
            }
          },
          alignSelf: 'center',
          enabled: !tm.jquery.Utils.isEmpty(this.view.getCurrentUserAsCareProfessional())
        });
        this.add(confirmButton);
      }

      var popupMenuHotSpot = new tm.jquery.Image({
        cls: 'more-actions-icon',
        width: 32,
        height: 24,
        cursor: 'pointer',
        alignSelf: 'flex-start'
      });
      popupMenuHotSpot.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component, componentEvent, elementEvent)
      {
        var popupMenu = self._createHeaderPopupMenu();
        popupMenu.show(elementEvent);
      });
      this.add(popupMenuHotSpot);
    }
  },

  _setKeyboardShortcuts: function ()
  {
    var self = this;

    this.onKey(new tm.jquery.event.KeyStroke({key: "esc", altKey: false, ctrlKey: false, shiftKey: false}),
        function ()
        {
          if (self.editMode && !tm.jquery.Utils.isEmpty(self.cancelEditEventCallback))
          {
            self.cancelEditEventCallback();
          }
        });

    if (!tm.jquery.Utils.isEmpty(this.view.getCurrentUserAsCareProfessional()))
    {
      this.onKey(new tm.jquery.event.KeyStroke({key: "return", altKey: false, ctrlKey: true, shiftKey: false}),
          function ()
          {
            if (self.editMode && !tm.jquery.Utils.isEmpty(self.confirmReviewEventCallback))
            {
              self.confirmReviewEventCallback();
            }
          });
    }
  },

  _createHeaderPopupMenu: function ()
  {
    var appFactory = this.view.getAppFactory();
    var self = this;
    var popupMenu = appFactory.createPopupMenu();

    if (this.editMode === true)
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: this.view.getDictionary('confirm.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(self.confirmReviewEventCallback))
          {
            self.confirmReviewEventCallback();
          }
        },
        iconCls: 'icon-finish-24'
      }));
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: this.view.getDictionary('cancel'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(self.cancelEditEventCallback))
          {
            self.cancelEditEventCallback();
          }
        },
        iconCls: 'icon-delete'
      }));
    }
    else
    {
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: this.view.getDictionary('edit.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(self.editReviewEventCallback))
          {
            self.editReviewEventCallback();
          }
        },
        iconCls: 'icon-edit'
      }));
      popupMenu.addMenuItem(new tm.jquery.MenuItem({
        text: this.view.getDictionary('delete.report'),
        handler: function ()
        {
          if (!tm.jquery.Utils.isEmpty(self.deleteReviewEventCallback))
          {
            self.deleteReviewEventCallback();
          }
        },
        iconCls: 'icon-delete'
      }));
    }

    return popupMenu;
  },

  setEditMode: function (value)
  {
    this.editMode = value;
  },

  refresh: function ()
  {
    if (this.isRendered())
    {
      this.removeAll();
      this._buildGui();
      this.repaint();
    }
  }
});
