Class.define('app.views.medications.ordering.TherapyIconContainerFactory', 'tm.jquery.Object', {
  /** @type app.views.common.AppView */
  view: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },
  /**
   * @param {string} propertyType of {@link app.views.medications.TherapyEnums.medicationPropertyType}
   * @param {boolean} [addTooltip = false],
   * @param {boolean} [hidden = false],
   * @returns {tm.jquery.Container}
   */
  createPropertyTypeIconContainer: function(propertyType, addTooltip, hidden)
  {
    return this.createIconContainer(
        'high-risk-icon ' + propertyType.toLowerCase() + '_icon',
        addTooltip ? this.view.getDictionary('MedicationPropertyType.' + propertyType) : undefined,
        hidden);
  },

  /**
   * @param {string} cls
   * @param {string} [tooltipString = undefined]
   * @param {boolean} [hidden = false],
   * @returns {tm.jquery.Container}
   */
  createIconContainer: function(cls, tooltipString, hidden)
  {
    var utils = app.views.medications.MedicationUtils;

    return new tm.jquery.Container({
      cls: cls,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: tm.jquery.VFlexboxLayout.create('center', 'center', 0),
      tooltip: !!tooltipString ? utils.createTooltip(tooltipString, 'bottom', this.view) : undefined,
      hidden: !!hidden
    });
  },
});