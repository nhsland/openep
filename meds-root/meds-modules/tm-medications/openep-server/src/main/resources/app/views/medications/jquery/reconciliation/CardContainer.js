/**
 * Extended version of the deprecated {@link tm.jquery.CardContainer} which seems to be missing the required logic for
 * layout updates in combination with 'slide-horizontal-new' animation. The original version doesn't work correctly when the
 * card container is resized (width), due to the fact that the active item is shown by a previously calculated 'scrollLeft'
 * position of the card container, based on the (previous) content container width (which is set to the approximate width of
 * the card container). The issue was reproducible in the medications on admission with prescribing dialog, if the dialog
 * width increased after it was initially shown, once the 'inpatient prescribing container' was the active item.
 */
Class.define('app.views.medications.reconciliation.CardContainer', 'tm.jquery.CardContainer', {
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  doLayout: function()
  {
    this.callSuper();

    if (this.animation === 'slide-horizontal-new')
    {
      this._recalculateScrollLeftPositionBasedOnActiveItem();
    }
  },

  /**
   * Calculates and applies the 'scrollLeft' position to the card container. Intended to be used when using the
   * 'slide-horizontal-new' animation. Based on {@link tm.jquery.CardContainer#applyActiveItem}, which is the method that
   * calculates the correct position when the active item changes. This method is intended to be called after a resizing
   * event, and uses the active item width, to calculate the correct position.
   * @private
   */
  _recalculateScrollLeftPositionBasedOnActiveItem: function()
  {
    var $containerDom = $(this.getDom());
    var $itemDom = $(this.getActiveItem().getDom());

    var itemDomCorrection = tm.jquery.ComponentUtils.getElementBoundsCorrection($itemDom);
    // using the active index to achieve 0 offset for the first slide
    var scrollLeftPosition = this.getActiveIndex() * ($itemDom.width() + itemDomCorrection.width);
    $containerDom.scrollLeft(scrollLeftPosition);
  }
});
