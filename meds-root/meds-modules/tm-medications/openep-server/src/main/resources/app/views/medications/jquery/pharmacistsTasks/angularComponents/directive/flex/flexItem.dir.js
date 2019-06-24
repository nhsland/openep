var FlexItemDir = function(FlexLayoutService)
{
  return {
    restrict: 'A',
    controller: ['$scope', function($scope)
    {
    }],
    link: function(scope, ele, attrs)
    {
      // based on tm.jquery.flexbox.item.Flex and tm.jquery.AbstractFlexboxLayout
      // flexGrow: number, inherit
      // flexShrink: number, inherit (http://css-tricks.com/almanac/properties/f/flex-shrink/)
      // flexBasis: auto, initial, none, 10px, 10% ...
      var itemFlex = attrs.flexItem ? attrs.flexItem : "0 0 auto";
      var flexParams = itemFlex.split(" ");

      var flexGrow = flexParams[0] ? flexParams[0] : "0";
      var flexShrink = flexParams[1] ? flexParams[1] : "0";
      var flexBasis = flexParams[2] ? flexParams[2] : "auto";

      var flexStyleValue = flexGrow + " " + flexShrink + " " + flexBasis;
      ele.css("-ms-flex", flexStyleValue);
      // JavaFx fix //
      ele.css("-webkit-flex", flexStyleValue);
      ele.css("-webkit-box-flex", "" + flexGrow); // TODO - old layout version fix
      ele.css("flex", flexStyleValue);

      // override display style value (if required) //
      var display = ele.css("display");
      if (!(display && display.contains("flex")) || $.browser.mozilla)
      {
        ele.css("display", "-webkit-flex");
        ele.css("display", "-moz-flex");
        ele.css("display", "-ms-flexbox");
        ele.css("display", "-o-flex");
        ele.css("display", "flex");
      }

      if (attrs.alignSelf)
      {
        ele.css('align-self', FlexLayoutService.getFirstValidValue(attrs.alignSelf, 'flex-start',
            ['auto', 'flex-start', 'flex-end', 'center', 'baseline', 'stretch', 'initial', 'inherit']));
      }
    }
  }
};
