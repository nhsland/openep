Class.define('app.views.medications.reconciliation.TherapyContainerPanelToolbar', 'app.views.medications.common.TemplateTherapyContainerToolbar', {
  cancelEventCallback: null,
  suspendEventCallback: null,

  suspendAvailable: true,
  cancelAvailable: true,

  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   * Is the suspend action available?
   * @return {boolean}
   */
  isSuspendAvailable: function()
  {
    return this.suspendAvailable === true;
  },

  /**
   * Is the cancel action available?
   * @return {boolean}
   */
  isCancelAvailable: function()
  {
    return this.cancelAvailable === true;
  },

  /**
   * @param {boolean} value
   */
  setSuspendAvailable: function(value)
  {
    this.suspendAvailable = value === true;
  },

  /**
   * @param {boolean} value
   */
  setCancelAvailable: function(value)
  {
    this.cancelAvailable = value === true;
  },

  /**
   * @param {function} callback
   */
  setCancelEventCallback: function (callback)
  {
    this.cancelEventCallback = callback;
  },

  /**
   * @param {function} callback
   */
  setSuspendEventCallback: function (callback)
  {
    this.suspendEventCallback = callback;
  },

  /* override */
  _buildGUI: function ()
  {
    var self = this;
    var view = this.getView();
    if (this.isCancelAvailable())
    {
      var cancelButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-delete group-element-cancel-therapy-menu-item",
        width: 32,
        height: 32,
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("cancel"))
      });
      this.add(cancelButtonIcon);

      cancelButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.cancelEventCallback) && component.isEnabled())
        {
          self.cancelEventCallback(self.getTherapyContainer());
        }
      });
    }
    if (this.isSuspendAvailable())
    {
      var suspendButtonIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-suspend group-element-cancel-therapy-menu-item",
        width: 32,
        height: 32,
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("suspend"))
      });

      this.add(suspendButtonIcon);
      suspendButtonIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function (component)
      {
        if (!tm.jquery.Utils.isEmpty(self.suspendEventCallback) && component.isEnabled())
        {
          self.suspendEventCallback(self.getTherapyContainer());
        }
      });
    }

    this.callSuper();
  },

  /* Override to disable the more actions menu. */
  _addActionsMenu: function ()
  {
    // do nothing, override so there's no popup menu!
  }
});
