var FlexLayoutDir = function(FlexLayoutService)
{
  return {
    restrict: 'A',
    controller: ['$scope', function($scope)
    {
    }],
    link: function(scope, ele, attrs)
    {
      // based on tm.jquery.AbstractFlexboxLayout
      var layoutCls = "";

      var flexLayout = FlexLayoutService.getFirstValidValue(attrs.flexLayout, 'row',
          ['row', 'column', 'row-reverse', 'column-reverse']);

      var flexWrap = FlexLayoutService.getFirstValidValue(attrs.flexWrap, 'nowrap', ['nowrap', 'wrap', 'wrap-reverse']);

      var justifyContent = FlexLayoutService.getFirstValidValue(attrs.justifyContent, 'flex-start',
          ['flex-start', 'flex-end', 'center', 'space-between', 'space-around']);

      var alignItems = FlexLayoutService.getFirstValidValue(attrs.alignItems, 'center',
          ['flex-start', 'flex-end', 'center', 'baseline', 'stretch']);

      var layoutCls = "tm-flexboxlayout" + " ";
      // flex-flow //
      layoutCls += "direction-" + FlexLayoutService.getNameForFlexDirection(flexLayout) + "-" + flexWrap + " ";
      // justify-content //
      layoutCls += "justify-content-" + justifyContent + " ";
      // align-items //
      layoutCls += "align-items-" + alignItems + " ";
      // align-content (hardcoded) //
      layoutCls += "align-content-center";
      ele.addClass(layoutCls);
    }
  }
};
