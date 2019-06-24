/**
 * A specialized version of a therapy container that creates a more compact therapy icons container and, in conjunction with
 * CSS, an inline therapy description, to accommodate the lower profile of the inline therapy review.
 */
Class.define('app.views.medications.common.therapy.InlineTherapyContainer', 'app.views.medications.common.therapy.TherapyContainer', {
  cls: 'therapy-container inline',

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @protected
   * @override
   * @returns {tm.jquery.Container}
   */
  buildIconContainer: function()
  {
    var self = this;
    var data = this.getData();
    var iconClsPrefix = 'row-icon ';
    var showDetailsCard = this.showIconTooltip === true;
    var iconContainerFactory = new app.views.medications.ordering.TherapyIconContainerFactory({ view: this.getView() });

    var iconsContainer = new tm.jquery.Container({
      cursor: showDetailsCard ? "pointer" : "default",
      cls: 'inline-therapy-icons-container',
      width: 50,
      alignSelf: 'flex-start',
      layout: tm.jquery.HFlexboxLayout.create(
          "flex-start",
          "center",
          0,
          tm.jquery.flexbox.FlexFlow.create("row-reverse", "wrap"))
    });
    var therapyIconLayers = this.getDisplayProvider().getBigIconContainerOptions(data).layers;
    var taggedLayersMap = this._createTaggedTherapyIconLayersMap(therapyIconLayers);
    var prioritizedIconLayerTags =
        [app.views.medications.common.therapy.TherapyContainerDisplayProvider.THERAPY_STATUS_ICON_TAG,
          app.views.medications.common.therapy.TherapyContainerDisplayProvider.CRITICAL_WARNINGS_ICON_TAG,
          app.views.medications.common.therapy.TherapyContainerDisplayProvider.CONSECUTIVE_DAY_ICON_TAG];

    prioritizedIconLayerTags.forEach(function createPrioritizedIcon(tagName)
    {
      if (!!taggedLayersMap[tagName] && !!taggedLayersMap[tagName].cls)
      {
        iconsContainer.add(
            new tm.jquery.Container({
              cls: iconClsPrefix + taggedLayersMap[tagName].cls,
              html: taggedLayersMap[tagName].html
            }));
      }
    });

    therapyIconLayers.forEach(function(layer)
    {
      if (!layer.tag || prioritizedIconLayerTags.indexOf(layer.tag) === -1)
      {
        iconsContainer.add(
            new tm.jquery.Container({
              cls: iconClsPrefix + layer.cls,
              html: layer.html
            }));
      }
    });

    var propertyTypeEnum = app.views.medications.TherapyEnums.medicationPropertyType;

    var displayableProperties = [propertyTypeEnum.BLACK_TRIANGLE_MEDICATION, propertyTypeEnum.CLINICAL_TRIAL_MEDICATION,
      propertyTypeEnum.CONTROLLED_DRUG, propertyTypeEnum.CRITICAL_DRUG, propertyTypeEnum.HIGH_ALERT_MEDICATION,
      propertyTypeEnum.UNLICENSED_MEDICATION, propertyTypeEnum.EXPENSIVE_DRUG];

    if (data.hasMedicationProperties())
    {
      data.getMedicationProperties().forEach(function createPropertyIcon(property)
      {
        if (displayableProperties.indexOf(property.getType()) >= 0)
        {
          iconsContainer.add(iconContainerFactory.createPropertyTypeIconContainer(property.getType()));
        }
      })
    }
    if (this.getView().isFormularyFilterEnabled() && data.hasNonFormularyMedications())
    {
      iconsContainer.add(iconContainerFactory.createIconContainer(iconClsPrefix + 'non_formulary_icon'));
    }

    if (showDetailsCard)
    {
      iconsContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
      {
        self._showTherapyDetailsContentPopup(component);
      });
    }
    return iconsContainer;
  },

  /**
   * @protected
   * @override
   * @returns {tm.jquery.Container}
   */
  buildTherapySummaryContainer: function()
  {
    var summaryContainer = new tm.jquery.Container({
      cls: 'therapy-summary-container',
      cursor: this.showIconTooltip === true ? "pointer" : "default",
      flex: tm.jquery.flexbox.item.Flex.create(0, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      alignSelf: "flex-start"
    });
    summaryContainer.add(this.buildTherapyDescription());
    this._addAdditionalTherapyDataToSummary(summaryContainer);
    return summaryContainer;
  },

  /**
   * @param {Array<{hpos: String, vpos: String, cls: String, html: String|undefined, tag: String|undefined}>} iconLayers
   * @private
   */
  _createTaggedTherapyIconLayersMap: function(iconLayers)
  {
    var taggedIcons = {};

    iconLayers.forEach(function mapTaggedIcon(layer)
    {
      if (layer.tag)
      {
        taggedIcons[layer.tag] = layer;
      }
    });

    return taggedIcons;
  }
});