Class.define('app.views.medications.common.TemplateTherapyGroupPanel', 'app.views.medications.common.TherapyGroupPanel', {
  cls: "therapy-group-panel template-therapy-group-panel",
  deleteTemplateEventCallback: null,
  addToBasketEventCallback: null,
  dynamicContent: true,
  deleteAvailable: true,
  templatePreconditions: null,
  _therapyTemplatesHelpers: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._therapyTemplatesHelpers = new app.views.medications.ordering.templates.TherapyTemplatesHelpers({
      view: this.getView()
    })
  },

  /**
   * @param {function} callback
   */
  setDeleteTemplateEventCallback: function(callback)
  {
    this.deleteTemplateEventCallback = callback;
  },

  /**
   * @param {function} callback
   */
  setAddToBasketEventCallback: function(callback)
  {
    this.addToBasketEventCallback = callback;
  },

  /**
   *
   * @returns {boolean}
   */
  isDeleteAvailable: function()
  {
    return this.deleteAvailable === true;
  },

  /* extending due to extra padding of the arrow */
  _addEvents: function()
  {
    this.callSuper();
    var self = this;
    this._header.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
    {
      if (self._isToggleIconEventHandlerRegistered === false)
      {
        var headerComponentDom = component.getDom();

        var originalEventSrcElement = elementEvent.originalEvent.srcElement ?
            elementEvent.originalEvent.srcElement : elementEvent.originalEvent.target;

        var isToggleIconAreaEvent = elementEvent.originalEvent.pageX - $(originalEventSrcElement).offset().left <= 35;
        if (originalEventSrcElement === headerComponentDom && isToggleIconAreaEvent)
        {
          elementEvent.stopPropagation();
          // only collapse/expand icon click event //
          self.toggle(null);
        }
      }
    });

  },

  _initHeaderContainer: function()
  {
    var self = this;
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var header = this.getHeader();
    header.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));
    /* overriding the only way possible due to tm.jquery.Panel impl. */

    var headerValueContainer = new tm.jquery.Container({
      cursor: "pointer",
      cls: "TextDataBold text-unselectable ellipsis",
      html: this.getGroupTitle(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    if (this._hasTemplatePreconditions())
    {
      var showTemplatePreconditionsInfo = new tm.jquery.Container({
        cursor: "pointer",
        cls: "icon-extend-24 info-icon",
        width: 32,
        height: 24,
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });
      showTemplatePreconditionsInfo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        var medicationInfoPopup = self._createTemplatePreconditionInfoContainer();
        showTemplatePreconditionsInfo.setTooltip(medicationInfoPopup);
        setTimeout(function()
        {
          medicationInfoPopup.show();
        }, 0);
      })
    }

    var addTemplateToBasket = new tm.jquery.Image({
      cursor: "pointer",
      cls: "icon-extend-24 add-template-to-basket-menu-item",
      style: "background-size: 16px 16px;", // missing 16px icon, workaround for now
      width: 32,
      height: 24,
      tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary("add")),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var addToBasketTask = appFactory.createDebouncedTask(
        "app.views.medications.common.TherapyGroupPanel.addToBasketEventCallback", function()
        {
          self.addToBasketEventCallback(self.getContentData());
        }, 0, 1000);

    addTemplateToBasket.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      if (!tm.jquery.Utils.isEmpty(self.addToBasketEventCallback) && component.isEnabled())
      {
        addToBasketTask.run();
      }
    });
    header.add(headerValueContainer);
    if (this._hasTemplatePreconditions())
    {
      header.add(showTemplatePreconditionsInfo);
    }
    header.add(addTemplateToBasket);

    if (this.isDeleteAvailable())
    {
      var deleteTemplateIcon = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-remove-small delete-template-menu-item",
        width: 32,
        height: 24,
        tooltip: app.views.medications.MedicationUtils.createHintTooltip(view, view.getDictionary('delete.order.set')),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
      });
      deleteTemplateIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        if (!tm.jquery.Utils.isEmpty(self.deleteTemplateEventCallback))
        {
          self.deleteTemplateEventCallback();
        }
      });
      header.add(deleteTemplateIcon);
    }

    this.bindToggleEvent([headerValueContainer]);
  },

  /**
   * @returns {boolean}
   * @private
   */
  _hasTemplatePreconditions: function()
  {
    return tm.jquery.Utils.isArray(this.templatePreconditions) && this.templatePreconditions.length > 0;
  },

  /**
   * @returns {app.views.common.tooltip.AppPopoverTooltip}
   * @private
   */
  _createTemplatePreconditionInfoContainer: function()
  {
    var view = this.getView();
    return view.getAppFactory().createDefaultPopoverTooltip(
        view.getDictionary('defined.criteria'),
        null,
        new tm.jquery.Container({
          cls: "TextData template-precondition-info-container",
          html: this._therapyTemplatesHelpers.createPreconditionWithValuesDescription(
              this.templatePreconditions[0]
          )
        })
    );
  }
});