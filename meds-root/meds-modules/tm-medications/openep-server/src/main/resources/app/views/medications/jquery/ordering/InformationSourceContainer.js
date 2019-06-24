Class.define('app.views.medications.ordering.InformationSourceContainer', 'tm.jquery.Container', {
  cls: 'information-source-container',
  scrollable: 'visible',
  /** @type app.views.common.AppView */
  view: null,
  /** @type boolean */
  required: false,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  availableInformationSources: null,
  /**
   * @type Array<app.views.medications.common.dto.InformationSource> containing the sources that will be pre-selected
   * upon construction and when the component is cleared.
   */
  defaultSelections: null,

  /** @type tm.jquery.SelectBox */
  _medicationSourceSelectBox: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    this.availableInformationSources = tm.jquery.Utils.isArray(this.availableInformationSources) ?
        this.availableInformationSources :
        [];

    this.defaultSelections = tm.jquery.Utils.isArray(this.defaultSelections) ?
        this._findInformationSourceInstancesFromAvailableSources(this.defaultSelections) :
        [];

    this._buildGui();
  },

  /**
   * @return {Array<app.views.medications.common.dto.InformationSource>} the list of active selections, or an empty
   * array if no selection has been made.
   */
  getSelections: function()
  {
    return this._medicationSourceSelectBox.getSelections();
  },

  /**
   * @param {Array<app.views.medications.common.dto.InformationSource>} sources
   * @param {boolean} [preventEvent=false]
   */
  setSelections: function(sources, preventEvent)
  {
    this._medicationSourceSelectBox.setSelections(
        this._findInformationSourceInstancesFromAvailableSources(sources),
        preventEvent);
  },

  /**
   * Changes the pre-selected sources. Normally the pre-selection is applied when the component's state is cleared, as we
   * have no way of knowing if the current selection represents the previous pre-selection or an active selection. This can
   * be changed by using the 'overrideCurrentSelection' parameter.
   * @param {Array<app.views.medications.common.dto.InformationSource>} sources
   * @param {boolean} [overrideCurrentSelection=false]
   */
  setDefaultSelections: function(sources, overrideCurrentSelection)
  {
    this.defaultSelections = this._findInformationSourceInstancesFromAvailableSources(sources);

    if (overrideCurrentSelection === true)
    {
      this.setSelections(this.defaultSelections, true);
    }
  },

  /**
   * @return {boolean} true, if the information source input is mandatory, otherwise false.
   */
  isRequired: function()
  {
    return this.required === true;
  },

  /**
   * @return {Array<tm.jquery.FormField>} the list of {@link tm.jquery.FormField} validators for this component.
   */
  getFormValidations: function()
  {
    return this.isRequired() ?
        [new tm.jquery.FormField({
          name: "medicationSourceSelectBox",
          component: this._medicationSourceSelectBox,
          required: true,
          validation: {
            type: "local"
          },
          componentValueImplementationFn: function(component)
          {
            return component.hasSelections() ? component.getSelections() : null;
          },
          getComponentValidationMarkElement: function(component)
          {
            return component.getButtonElement();
          }
        })] :
        [];
  },

  /** Clears the active selections.*/
  clear: function()
  {
    this.setSelections(this.defaultSelections, true);
  },

  /**
   * If you're wondering why we need to manually match the given sources to instances to the ones used to create the
   * select box options, when we already implemented the defaultValueCompareToFunction, it's due to a little gem inside
   * {@link tm.jquery.SelectBox#_updateSelections} when the component hasn't been rendered yet, which attempts to merge the
   * previous selections (based on the selected status in the options) with the passed new selections. The setSelections
   * method, when the component isn't rendered, sets it's selections property to the passed array, then calls the
   * _updateOptionsStates method, which calls getSelections and that in turn calls the problematic _updateSelections method.
   * If we keep changing the selections without ever rendering the component, the matching options have their selected status
   * set, and the duplicate removal operation inside _updateSelections completely ignores the value compare method and relies
   * on simple instance checks when dealing with objects. This results in duplicate values, when we set the new selections
   * from a therapy instance that was passed to us from the server API, as it's information sources deserialize as new
   * objects, not found in our availableSources.
   *
   * @param {Array<app.views.medications.common.dto.InformationSource>|null} sources
   * @private
   */
  _findInformationSourceInstancesFromAvailableSources: function(sources)
  {
    if (!tm.jquery.Utils.isArray(sources) || sources.length === 0)
    {
      return [];
    }

    var sourcesIds = sources.map(function toSourceId(source)
    {
      return source.getId();
    });

    return this.availableInformationSources.filter(function isIdFromSourcesIds(source)
    {
      return sourcesIds.contains(source.getId());
    });
  },

  /** Constructs the component contents - a SelectBox wrapped in a vertically titled component. */
  _buildGui: function()
  {
    this._medicationSourceSelectBox = new tm.jquery.SelectBox({
      dropdownHeight: 5,
      dropdownWidth: "stretch",
      options: this.availableInformationSources.map(
          function createOption(source)
          {
            return tm.jquery.SelectBox.createOption(source);
          }),
      selections: this.defaultSelections,
      multiple: true,
      allowSingleDeselect: true,
      placeholder: " ",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      defaultValueCompareToFunction: function(value1, value2)
      {
        var id1 = tm.jquery.Utils.isEmpty(value1) ? null : value1.getId();
        var id2 = tm.jquery.Utils.isEmpty(value2) ? null : value2.getId();

        return id1 === id2;
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return option.getValue().getName();
      }
    });

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this.add(new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: this.view.getDictionary("source"),
      contentComponent: this._medicationSourceSelectBox,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    }));
  }
});