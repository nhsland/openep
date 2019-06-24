Class.define('app.views.medications.ordering.simple.ReleaseDetailsContainer', 'tm.jquery.Container', {
  cls: "release-details-container",
  view: null,

  _mainLayoutComponent: null,
  _releaseTypeButtonGroup: null,
  _releaseDurationButtonGroup: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.registerEventTypes(
        'app.views.medications.ordering.simple.ReleaseDetailsContainer',
        [{eventType: tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE}]);
    this._buildGui();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Sets available options based on provided medication data. If the type and duration (in case of a modified release)
   * can be configured by the user, the available options are shown and the user can press the desired button. If the
   * options are predefined by the medication data, the buttons are still shown, but the appropriate group is disabled
   * and the buttons are preselected. In case of the release duration selection, if the duration can't be configured
   * but the number of available options is at least 1, the first option is preselected and presumed to be the default.
   * In case no options are viable for the given medication, the all components are hidden, including the title
   * label for the component.
   *
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setOptionsByMedication: function(medicationData)
  {
    var isDurationConfigurable = medicationData.isModifiedReleaseDurationConfigurable();

    this._releaseTypeButtonGroup.setButtons(this._createReleaseTypeButtonsByMedication(medicationData));
    this._releaseTypeButtonGroup.setEnabled(
        medicationData.isGastroResistantConfigurable() || medicationData.isModifiedReleaseConfigurable());

    this._releaseDurationButtonGroup.setButtons(this._createReleaseDurationButtonsByMedication(medicationData));
    this._releaseDurationButtonGroup.setEnabled(isDurationConfigurable);

    this._applyReleaseDurationButtonGroupVisibility(this._isModifiedReleaseActive());

    if (medicationData.isModifiedRelease() || medicationData.isGastroResistant())
    {
      this._mainLayoutComponent.isRendered() ? this._mainLayoutComponent.show() : this._mainLayoutComponent.setHidden(false);
    }
    else
    {
      this._mainLayoutComponent.isRendered() ? this._mainLayoutComponent.hide() : this._mainLayoutComponent.setHidden(true);
    }
  },

  /**
   * @return {app.views.medications.common.dto.ReleaseDetails|null}
   */
  getSelection: function()
  {
    var activeReleaseType = this._getSelectedReleaseType();

    if (!activeReleaseType)
    {
      return null;
    }

    var durationSelection = this._releaseDurationButtonGroup.getSelection();

    return new app.views.medications.common.dto.ReleaseDetails({
      type: activeReleaseType,
      hours: durationSelection.length === 0 ? null : durationSelection[0].data.hours
    });
  },

  /**
   * Applies the desired value as the active selection. Keep in mind that the available options must first be configured
   * by a call to {@link #setOptionsByMedication}, otherwise setting the selection may yield partial or no result.
   * If a given option is not available, the active selection for that part will be cleared.
   *
   * @param {app.views.medications.common.dto.ReleaseDetails|null} value
   */
  setSelection: function(value)
  {
    if (!value)
    {
      this._releaseTypeButtonGroup.setSelection([], true);
      this._releaseDurationButtonGroup.setSelection([], true);
      return;
    }

    this._setSelectedReleaseType(value.getType(), true);
    this._setSelectedReleaseDuration(value.getHours(), true);
    this._applyReleaseDurationButtonGroupVisibility(this._isModifiedReleaseActive());
  },

  /**
   * Hides the complete content of this component, since the ability to define release details is subjected to medication
   * data.
   */
  clear: function()
  {
    this.isRendered() ? this._mainLayoutComponent.hide() : this._mainLayoutComponent.setHidden(true);
    this._applyReleaseDurationButtonGroupVisibility(false);
    this._releaseTypeButtonGroup.setEnabled(true);
    this._releaseDurationButtonGroup.setEnabled(true);
    this._releaseTypeButtonGroup.setButtons([]);
    this._releaseDurationButtonGroup.setButtons([]);
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this._mainLayoutComponent = new app.views.medications.common.VerticallyTitledComponent({
      cls: 'vertically-titled-component main-layout-component',
      titleText: this.getView().getDictionary('therapy.release.details'),
      contentComponent: new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0)
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      hidden: true /* display no content unless medication data permits it */
    });

    this._releaseTypeButtonGroup = new tm.jquery.ButtonGroup({
      testAttribute: 'release-mode-group',
      orientation: "horizontal",
      type: "checkbox"
    });
    this._releaseTypeButtonGroup.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onCheckBoxButtonGroupSelectionChange.bind(this));
    this._releaseTypeButtonGroup.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onReleaseTypeButtonSelectionChange.bind(this));

    this._releaseDurationButtonGroup = new tm.jquery.ButtonGroup({
      testAttribute: 'duration-type-group',
      orientation: "horizontal",
      type: "checkbox",
      hidden: true /* not available unless defined by medication data */
    });
    this._releaseDurationButtonGroup.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onCheckBoxButtonGroupSelectionChange.bind(this));

    this._mainLayoutComponent.getContentComponent().add(this._releaseTypeButtonGroup);
    this._mainLayoutComponent.getContentComponent().add(this._releaseDurationButtonGroup);

    this.add(this._mainLayoutComponent);
  },

  _applyReleaseDurationButtonGroupVisibility: function(visible)
  {
    if (visible)
    {
      this._releaseDurationButtonGroup.isRendered() ?
          this._releaseDurationButtonGroup.show() :
          this._releaseDurationButtonGroup.setHidden(false);
    }
    else
    {
      this._releaseDurationButtonGroup.isRendered() ?
          this._releaseDurationButtonGroup.hide() :
          this._releaseDurationButtonGroup.setHidden(true);
      this._releaseDurationButtonGroup.clearSelection(true);
    }
  },

  /**
   * Returns the available release type buttons, if any. It's presumed gastro resistance and modified release type
   * are exclusive (only one exists), when predefined by the medication. In such cases the mode which cannot be configured
   * by the user is also preselected.
   *
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @return {Array<tm.jquery.Button>}
   * @private
   */
  _createReleaseTypeButtonsByMedication: function(medicationData)
  {
    var buttons = [];
    if (medicationData.isGastroResistant())
    {
      buttons.push(
          new tm.jquery.Button({
            text: this.getView().getDictionary('gastro.resistant.short'),
            data: {
              type: app.views.medications.TherapyEnums.releaseType.GASTRO_RESISTANT
            },
            pressed: medicationData.isGastroResistant() && !medicationData.isGastroResistantConfigurable()
          })
      );
    }
    if (medicationData.isModifiedRelease())
    {
      buttons.push(
          new tm.jquery.Button({
            text: this.getView().getDictionary('modified.release.short'),
            pressed: medicationData.isModifiedRelease() && !medicationData.isModifiedReleaseConfigurable(),
            data: {
              type: app.views.medications.TherapyEnums.releaseType.MODIFIED_RELEASE
            }
          })
      );
    }
    return buttons;
  },

  /**
   * Create the possible release duration mode buttons. In case the release duration can't be configured by the user,
   * the first button is preselected as it's presumed there should be only one.
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @return {Array<tm.jquery.Button>}
   * @private
   */
  _createReleaseDurationButtonsByMedication: function(medicationData)
  {
    var hourUnit = this.getView().getDictionary("hour.unit");
    return medicationData
        .getModifiedReleaseDurationProperties()
        .map(
            function toButton(property, index)
            {
              var value = parseInt(property.getValue());
              return new tm.jquery.Button({
                text: value + hourUnit,
                pressed: !medicationData.isModifiedReleaseDurationConfigurable() && index === 0,
                data: {
                  hours: value
                }
              });
            });
  },

  /**
   * Selection change event handler for both instances of {@link tm.jquery.ButtonGroup} in 'checkbox' mode, which ensures
   * only one button can be pressed at a given time. Unlike the behaviour of a classic radio button group, this allows a
   * deselection of the active button. Fires the change event for this component since it's used by both button groups.
   * @param {tm.jquery.ButtonGroup} component
   * @param {tm.jquery.ComponentEvent} componentEvent
   * @private
   */
  _onCheckBoxButtonGroupSelectionChange: function(component, componentEvent)
  {
    var eventData = componentEvent.getEventData();
    if (eventData.activeButtons.length > 1)
    {
      component.setSelection([eventData.pressedButton], true);
    }
    this._fireChangeEvent();
  },

  /**
   * Selection change event handler for {@link #_releaseTypeButtonGroup}, which shows or hides the additional duration
   * selection button group, if the modified release type is activated or deactivated.
   * @private
   */
  _onReleaseTypeButtonSelectionChange: function()
  {
    this._applyReleaseDurationButtonGroupVisibility(this._isModifiedReleaseActive());
  },

  /**
   * @returns {boolean} True, if the active selection of {@link #_releaseTypeButtonGroup} is the modified release button.
   * @private
   */
  _isModifiedReleaseActive: function()
  {
    return this._isReleaseTypeActive(app.views.medications.TherapyEnums.releaseType.MODIFIED_RELEASE);
  },

  /**
   * @param {string} releaseType {@link app.views.medications.TherapyEnums.releaseType}
   * @return {boolean} True, if a button with the given type in the data property is found, otherwise false.
   * @private
   */
  _isReleaseTypeActive: function(releaseType)
  {
    return this._releaseTypeButtonGroup
        .getSelection()
        .some(
            function(button)
            {
              return !!button.data && button.data.type === releaseType;
            });
  },

  /**
   * @return {string|null} the active value of release type {@link app.views.medications.TherapyEnums.releaseType}, or
   * null if no type is active.
   * @private
   */
  _getSelectedReleaseType: function()
  {
    var activeSelection = this._releaseTypeButtonGroup.getSelection();
    return activeSelection.length > 0 ? activeSelection[0].data.type : null;
  },

  /**
   * @param {string|null|undefined} releaseType{@link app.views.medications.TherapyEnums.releaseType}
   * @param {boolean} [preventEvent=false]
   * @private
   */
  _setSelectedReleaseType: function(releaseType, preventEvent)
  {
    var releaseTypeButton =
        this._findButtonGroupButtonByMatchingData(
            this._releaseTypeButtonGroup,
            function byType(data)
            {
              return data.type === releaseType
            });

    this._releaseTypeButtonGroup.setSelection(!!releaseTypeButton ? [releaseTypeButton] : [], preventEvent === true);
    this._fireChangeEvent(preventEvent);
  },

  /**
   * @param {Number|null|undefined} hours
   * @param {boolean} [preventEvent=false]
   * @private
   */
  _setSelectedReleaseDuration: function(hours, preventEvent)
  {
    var hoursButton =
        this._findButtonGroupButtonByMatchingData(
            this._releaseDurationButtonGroup,
            function byType(data)
            {
              return data.hours === hours
            });

    this._releaseDurationButtonGroup.setSelection(!!hoursButton ? [hoursButton] : [], preventEvent === true);
    this._fireChangeEvent(preventEvent);
  },

  /**
   * @param {tm.jquery.ButtonGroup} buttonGroup
   * @param {function} predicate
   * @returns {tm.jquery.Button|undefined}
   * @private
   */
  _findButtonGroupButtonByMatchingData: function(buttonGroup, predicate)
  {
    return buttonGroup.getButtons()
        .find(
            function match(button)
            {
              return !!button.data && predicate(button.data);
            }
        );
  },

  /**
   * Fires a simple value change notification event. At this point has no data.
   * @param {boolean} [preventEvent=false] if true, no event will be fired. Simplifies the use of this method in conjunction
   * with the typical value setting methods.
   * @private
   */
  _fireChangeEvent: function(preventEvent)
  {
    if (preventEvent !== true)
    {
      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE
      }));
    }
  }
});