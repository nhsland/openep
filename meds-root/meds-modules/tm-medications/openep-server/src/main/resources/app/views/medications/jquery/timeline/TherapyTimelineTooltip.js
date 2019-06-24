Class.define('app.views.medications.timeline.TherapyTimelineTooltip', 'tm.jquery.Tooltip', {
  type: "tooltip",
  appendTo: "body",
  placement: "bottom",
  delay: {
    show: 1000,
    hide: 1000
  },

  /** constructor */
  Constructor: function()
  {
    this.callSuper();
    var self = this;

    this.setPlacement(function(tooltipElement, componentElement)
    {
      // return 0 if either of the elements are missing due to TMC jQuery framework test calls
      return componentElement && tooltipElement ? self.getPlacementAccordingToScreenPosition(componentElement, tooltipElement) : 0;
    });
  },

  getPlacementAccordingToScreenPosition: function(element, tooltip)
  {
    var $tooltipClone = $(tooltip).clone(false).css('visibility', 'hidden').appendTo(document.body);
    var tooltipHeight = $tooltipClone.outerHeight();
    var windowHeight = $(document.body).height();
    var $anchorElement = $(element);

    $tooltipClone.remove();

    return windowHeight > $anchorElement.offset().top + $anchorElement.height() + tooltipHeight ? "bottom" : "top";
  }
});