Class.define('app.views.medications.common.auditTrail.TherapyChangesContainer', 'tm.jquery.Container', {
  cls: 'therapy-changes-container',
  view: null,
  therapyChanges: null,
  changeTitleFlexBasis: 'auto',
  // TODO remove the *quantityUnit properties when we figure out how we'll handle the change on on Timed*DoseElements
  prevQuantityUnit: null,
  currentQuantityUnit: null,

  /**
   * @param {Object|null} config
   * @param {app.views.medications.common.dto.TherapyChange[]} config.therapyChanges
   * @param {app.views.common.AppView} config.view
   * @param {string} config.changeTitleFlexBasis Set to 'auto' when there's limited space.
   * @param {string|null} config.prevQuantityUnit Original units - used by the protocol display dialog.
   * @param {string|null} config.currentQuantityUnit Current units - used by the protocol display dialog.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @return {app.views.medications.common.dto.TherapyChange[]}
   */
  getTherapyChanges: function()
  {
    return tm.jquery.Utils.isArray(this.therapyChanges) ? this.therapyChanges : [];
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {string}
   */
  getChangeTitleFlexBasis: function()
  {
    return this.changeTitleFlexBasis;
  },

  /**
   * @return {string|null}
   */
  getPrevQuantityUnit: function()
  {
    return this.prevQuantityUnit;
  },

  /**
   * @return {string|null}
   */
  getCurrentQuantityUnit: function()
  {
    return this.currentQuantityUnit;
  },

  _buildGui: function()
  {
    var therapyChangeTypePrefix = "TherapyChangeType.";
    var view = this.getView();

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this.getTherapyChanges().forEach(function prependChange(change)
    {
      if (change.isChangeTypeVariableDose())
      {
        if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(change.getOldValue()) ||
            app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(change.getNewValue()))
        {
          this.add(this._createProtocolButtonContainer(
              view.getDictionary(therapyChangeTypePrefix + change.getType()),
              change.getOldValue(),
              change.getNewValue()));
        }
        else
        {
          this.add(this._createVariableRateOrDoseContainer(
              view.getDictionary(therapyChangeTypePrefix + change.getType()),
              change.getOldValue(),
              change.getNewValue()));
        }
      }
      else if (change.isChangeTypeVariableRate())
      {
        this.add(this._createVariableRateOrDoseContainer(
            view.getDictionary(therapyChangeTypePrefix + change.getType()),
            change.getOldValue(),
            change.getNewValue()));
      }
      else
      {
        this.add(this._createChangeRowForPair(
            view.getDictionary(therapyChangeTypePrefix + change.getType()),
            change.getOldValue(),
            change.getNewValue()));
      }
    }, this);
  },

  /**
   * @param {String} title
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeTitleContainer: function(title)
  {
    // setting a flex basis in px to achieve horizontal alignment works well when there's enough space, otherwise
    // it will break the sizing of the value presentation columns
    return new tm.jquery.Container({
      cls: "TextLabel change-title",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, this.getChangeTitleFlexBasis()),
      html: title
    });
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _createArrowContainer: function()
  {
    return new tm.jquery.Container({
      cls: "arrow-icon",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      height: 20,
      width: 20
    });
  },

  /**
   * @param {String} title
   * @param {String} oldValue
   * @param {String} newValue
   * @return {tm.jquery.Container}
   * @private
   */
  _createChangeRowForPair: function(title, oldValue, newValue)
  {
    var view = this.getView();
    if (tm.jquery.Utils.isArray(oldValue))
    {
      oldValue = oldValue.join(", ")
    }
    if (tm.jquery.Utils.isArray(newValue))
    {
      newValue = newValue.join(", ")
    }
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    actionChangeRowContainer.add(this._createChangeTitleContainer(title));

    var oldValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      cls: "value-container TextData crossed-out",
      html: oldValue && oldValue.length > 0 ? tm.jquery.Utils.escapeHtml(oldValue) : view.getDictionary("empty")
    });
    actionChangeRowContainer.add(oldValueContainer);
    actionChangeRowContainer.add(this._createArrowContainer());

    var newValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      cls: "value-container TextData",
      html: newValue && newValue.length > 0 ? tm.jquery.Utils.escapeHtml(newValue) : view.getDictionary("empty")
    });
    actionChangeRowContainer.add(newValueContainer);

    return actionChangeRowContainer;
  },

  /**
   * @param {String} title
   * @param {Array | String} oldDoseElements
   * @param {Array | String} newDoseElements
   * @returns {tm.jquery.Container}
   * @private
   */
  _createVariableRateOrDoseContainer: function(title, oldDoseElements, newDoseElements)
  {
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    actionChangeRowContainer.add(this._createChangeTitleContainer(title));
    var newDoseElementsContainer = this._createNewDoseElementsContainer(newDoseElements);
    var oldDoseElementsContainer = this._createOldDoseElementsContainer(oldDoseElements);
    actionChangeRowContainer.add(oldDoseElementsContainer);
    actionChangeRowContainer.add(this._createArrowContainer());
    actionChangeRowContainer.add(newDoseElementsContainer);

    return actionChangeRowContainer;
  },

  /**
   * @param {Array | String} oldDoseElements
   * @returns {tm.jquery.Container}
   * @private
   */
  _createOldDoseElementsContainer: function(oldDoseElements)
  {
    var oldDoseElementsContainer = new tm.jquery.Container({
      cls: "value-container TextData crossed-out",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    if (tm.jquery.Utils.isArray(oldDoseElements) && oldDoseElements.length > 0)
    {
      oldDoseElements.forEach(function(element)
      {
        oldDoseElementsContainer.add(this._createTimedDoseElementContainer(element));
      }, this);
    }
    else
    {
      var oldDoseContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: tm.jquery.Utils.escapeHtml(oldDoseElements)
      });
      oldDoseElementsContainer.add(oldDoseContainer);
    }
    return oldDoseElementsContainer;
  },

  /**
   * @param {Array | String} newDoseElements
   * @returns {tm.jquery.Container}
   * @private
   */
  _createNewDoseElementsContainer: function(newDoseElements)
  {
    var newDoseElementsContainer = new tm.jquery.Container({
      cls: "value-container TextData",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });
    if (tm.jquery.Utils.isArray(newDoseElements) && newDoseElements.length > 0)
    {
      newDoseElements.forEach(function(element)
      {
        newDoseElementsContainer.add(this._createTimedDoseElementContainer(element));
      }, this);
    }
    else
    {
      var newDoseContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: tm.jquery.Utils.escapeHtml(newDoseElements)
      });
      newDoseElementsContainer.add(newDoseContainer);
    }
    return newDoseElementsContainer;
  },

  /**
   * @param {object} element JSON of either TimedComplexDoseElement or TimedSimpleDoseElementDto
   * @returns {tm.jquery.Container}
   * @private
   */
  _createTimedDoseElementContainer: function(element){
    var html;

    if (element.intervalDisplay || element.speedDisplay) // TimedComplexDoseElement
    {
      html = tm.jquery.Utils.escapeHtml(element.intervalDisplay) + " &ensp; " +
          tm.jquery.Utils.escapeHtml(element.speedDisplay) +
          (element.speedFormulaDisplay ? ' &ensp; ' + tm.jquery.Utils.escapeHtml(element.speedFormulaDisplay) : "");
    }
    else // presume TimedSimpleDoseElementDto
    {
      html = tm.jquery.Utils.escapeHtml(element.timeDisplay) + " - " +
          tm.jquery.Utils.escapeHtml(element.quantityDisplay);
    }
    return new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: html
    });
  },

  /**
   * @param {String} title
   * @param {Array | String} oldDoseElements
   * @param {Array | String} newDoseElements
   * @returns {tm.jquery.Container}
   * @private
   */
  _createProtocolButtonContainer: function(title, oldDoseElements, newDoseElements)
  {
    var self = this;
    var actionChangeRowContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    actionChangeRowContainer.add(this._createChangeTitleContainer(title));
    var protocolButton = new tm.jquery.Button({
      cls: 'protocol-btn',
      type: 'link',
      text: this.getView().getDictionary("protocol"),
      height: 20,
      handler: function(component, componentEvent, elementEvent)
      {
        elementEvent.stopPropagation(); //
        self._createProtocolHistoryContainer(oldDoseElements, newDoseElements);
      }
    });

    if (!app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(oldDoseElements))
    {
      actionChangeRowContainer.add(this._createOldDoseElementsContainer(oldDoseElements));
      actionChangeRowContainer.add(this._createArrowContainer());
    }
    actionChangeRowContainer.add(protocolButton);
    if (!app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(newDoseElements))
    {
      actionChangeRowContainer.add(this._createArrowContainer());
      actionChangeRowContainer.add(this._createNewDoseElementsContainer(newDoseElements));
    }

    return actionChangeRowContainer;
  },

  /**
   * @param {Array} oldDoseElements
   * @param {Array} newDoseElements
   * @private
   */
  _createProtocolHistoryContainer: function(oldDoseElements, newDoseElements)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var protocolSummaryContainers = new app.views.common.containers.AppDataEntryContainer({
      cls: 'protocol-summary-containers',
      scrollable: 'both',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    if (oldDoseElements && app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(oldDoseElements))
    {
      var initialTherapyProtocolSummary = new app.views.medications.common.ProtocolSummaryContainer({
        view: view,
        timedDoseElements: oldDoseElements,
        unit: this.getPrevQuantityUnit(),
        lineAcross: true
      });
      protocolSummaryContainers.add(initialTherapyProtocolSummary);
    }
    if (newDoseElements && app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(newDoseElements))
    {
      var currentTherapyProtocolSummary = new app.views.medications.common.ProtocolSummaryContainer({
        view: view,
        timedDoseElements: newDoseElements,
        unit: this.getCurrentQuantityUnit()
      });
      protocolSummaryContainers.add(currentTherapyProtocolSummary);
    }

    var protocolSummaryDialog = appFactory.createDefaultDialog(
        view.getDictionary("variable.dose"),
        null,
        protocolSummaryContainers,
        null,
        950,
        850
    );
    protocolSummaryDialog.header.setCls("audit-trail-header");
    protocolSummaryDialog.setHideOnEscape(true);
    protocolSummaryDialog.setHideOnDocumentClick(true);
    protocolSummaryDialog.show();
  }
});