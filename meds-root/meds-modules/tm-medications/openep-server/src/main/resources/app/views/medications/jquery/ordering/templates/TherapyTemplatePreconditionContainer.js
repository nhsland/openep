Class.define('app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_PRECONDITION_TYPE_SELECTION_CHANGE: new tm.jquery.event.EventType({
      name: 'therapyTemplatePreconditionContainerPreconditionTypeSelectionChange', delegateName: null
    })
  },

  cls: 'therapy-template-precondition-container',
  view: null,
  scrollable: 'visible',

  _preconditionSelectBox: null,
  _minValueField: null,
  _maxValueField: null,
  _minValueUnitContainer: null,
  _maxValueUnitContainer: null,
  _dialog: null,
  _therapyTemplatesHelpers: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._therapyTemplatesHelpers = new app.views.medications.ordering.templates.TherapyTemplatesHelpers({
      view: this.getView()
    });
    this.registerEventTypes('app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer', [
      {
        eventType: app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer.EVENT_TYPE_PRECONDITION_TYPE_SELECTION_CHANGE
      }
    ]);
    this._buildGui();
  },

  /**
   * @param {Boolean} enabled
   */
  setPreconditionInputAvailability: function(enabled)
  {
    this._preconditionSelectBox.setEnabled(enabled);
    this._minValueField.setEnabled(enabled);
    this._maxValueField.setEnabled(enabled);
  },

  /**
   * Applies the values from the given template precondition to the UI fields and makes them visible. Selects the
   * type of precondition in the dropdown menu and displays the minimum and maximum values, if present.
   * If no precondition is given, the dropdown menu's selection is cleared and the related input fields are hidden from
   * view.
   * @param {app.views.medications.ordering.dto.TherapyTemplatePrecondition|undefined} precondition
   */
  setPrecondition: function(precondition)
  {
    if (!!precondition)
    {
      this._minValueField.setValue(precondition.getMinValue());
      this._maxValueField.setValue(precondition.getMaxValue());
      this._preconditionSelectBox.setSelections([precondition.getPrecondition()], true);
      this._handlePreconditionValuesComponentsVisibility(true);
      this._applyUnitsLabels();
    }
    else
    {
      this._clear();
    }
  },

  /**
   * @returns {app.views.medications.ordering.dto.TherapyTemplatePrecondition|null}
   */
  getPrecondition: function()
  {
    if (this._preconditionSelectBox.getSelections().length > 0)
    {
      return new app.views.medications.ordering.dto.TherapyTemplatePrecondition({
        precondition: this._preconditionSelectBox.getSelections()[0],
        minValue: this._minValueField.getValue(),
        maxValue: this._maxValueField.getValue(),
        exactValue: null
      })
    }
    return null;
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormFieldValidators: function()
  {
    var self = this;
    var validators = [];
    if (this._preconditionSelectBox.getSelections().length > 0 &&
        !(this._minValueField.getValue() || this._maxValueField.getValue()))
    {
      validators.push(new tm.jquery.FormField({
        component: self._minValueField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._minValueField.getValue();
        }
      }));
      validators.push(new tm.jquery.FormField({
        component: self._maxValueField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._maxValueField.getValue();
        }
      }))
    }
    return validators;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  _buildGui: function()
  {
    var view = this.getView();
    var self = this;
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));
    this._preconditionSelectBox = new tm.jquery.SelectBox({
      cls: "precondition-select-box",
      alignSelf: "stretch",
      liveSearch: false,
      options: this._createPreconditionSelectBoxOptions(),
      selections: [],
      allowSingleDeselect: true,
      multiple: false,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      dropdownWidth: "stretch",
      placeholder: view.getDictionary("select"),
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1 === value2;
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return self._getPreconditionDisplayName(option.getValue());
      }
    });
    this._preconditionSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      self._firePreconditionTypeSelectionChangeEvent(component.getSelections());
      self._handlePreconditionValuesComponentsVisibility(component.getSelections().length > 0);
      self._onPreconditionSelectionChange(component.getSelections()[0]);
    });
    this._valuesAndUnitsContainer = new tm.jquery.Container({
      cls: 'precondition-values-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      scrollable: 'visible',
      hidden: true
    });
    this._minValueField = new tm.jquery.NumberField({
      cls: "min-value-field",
      width: 68
    });
    this._maxValueField = new tm.jquery.NumberField({
      cls: "max-value-field",
      width: 68
    });

    this._minValueUnitContainer = new tm.jquery.Container({
      cls: "textData unit-container"
    });
    this._maxValueUnitContainer = new tm.jquery.Container({
      cls: "textData unit-container"
    });

    this.add(this._preconditionSelectBox);
    this._valuesAndUnitsContainer.add(this._minValueField);
    this._valuesAndUnitsContainer.add(this._minValueUnitContainer);
    this._valuesAndUnitsContainer.add(new tm.jquery.Container({
      html: " &ndash; "
    }));
    this._valuesAndUnitsContainer.add(this._maxValueField);
    this._valuesAndUnitsContainer.add(this._maxValueUnitContainer);
    this.add(this._valuesAndUnitsContainer)
  },

  /**
   * @private
   */
  _onPreconditionSelectionChange: function()
  {
    this._minValueField.setValue(null);
    this._maxValueField.setValue(null);
    this._applyUnitsLabels();
  },

  /**
   * @param {app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum|String} precondition
   * @returns {String}
   * @private
   */
  _getPreconditionDisplayName: function(precondition)
  {
    return this.getView().getDictionary("therapy.template.precondition." + precondition);
  },

  /**
   * @returns {Array<tm.jquery.selectbox.Option>}
   * @private
   */
  _createPreconditionSelectBoxOptions: function()
  {
    var preconditionEnums = app.views.medications.TherapyEnums.therapyTemplatePreconditionEnum;
    var options = [];
    options.push(tm.jquery.SelectBox.createOption(
        preconditionEnums.AGE_IN_MONTHS,
        this._getPreconditionDisplayName(preconditionEnums.AGE_IN_MONTHS)
    ));
    options.push(tm.jquery.SelectBox.createOption(
        preconditionEnums.AGE_IN_YEARS,
        this._getPreconditionDisplayName(preconditionEnums.AGE_IN_YEARS)
    ));
    options.push(tm.jquery.SelectBox.createOption(
        preconditionEnums.WEIGHT,
        this._getPreconditionDisplayName(preconditionEnums.WEIGHT)
    ));
    options.push(tm.jquery.SelectBox.createOption(
        preconditionEnums.BODY_SURFACE,
        this._getPreconditionDisplayName(preconditionEnums.BODY_SURFACE)
    ));
    return options;
  },

  _clear: function()
  {
    this._preconditionSelectBox.setSelections([], true);
    this._minValueField.setValue(null);
    this._maxValueField.setValue(null);
    this._minValueUnitContainer.setHtml("");
    this._maxValueUnitContainer.setHtml("");
    this._handlePreconditionValuesComponentsVisibility(false);
  },

  /**
   * @param {Boolean} visible
   * @private
   */
  _handlePreconditionValuesComponentsVisibility: function(visible)
  {
    if (visible)
    {
      this._valuesAndUnitsContainer.isRendered() ?
          this._valuesAndUnitsContainer.show() :
          this._valuesAndUnitsContainer.setHidden(false);
    }
    else
    {
      this._valuesAndUnitsContainer.isRendered() ?
          this._valuesAndUnitsContainer.hide() :
          this._valuesAndUnitsContainer.setHidden(true);
    }
  },

  /**
   * @private
   */
  _applyUnitsLabels: function()
  {
    var selectedPrecondition = this._preconditionSelectBox.getSelections().length > 0 ?
        this._preconditionSelectBox.getSelections()[0] :
        null;
    this._minValueUnitContainer.setHtml(
        this._therapyTemplatesHelpers.createPreconditionMinValueUnitLabel(selectedPrecondition)
    );
    this._maxValueUnitContainer.setHtml(
        this._therapyTemplatesHelpers.createPreconditionMaxValueUnitLabel(selectedPrecondition));
  },

  /**
   * Triggers the {@link #EVENT_TYPE_PRECONDITION_TYPE_SELECTION_CHANGE} event.
   * @param {Array<string>} selections
   * @private
   */
  _firePreconditionTypeSelectionChangeEvent: function(selections)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer.EVENT_TYPE_PRECONDITION_TYPE_SELECTION_CHANGE,
      eventData: {selections: selections}
    }));
  }
});