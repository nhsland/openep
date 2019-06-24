var SyringeTaskMenuItemDir = function ()
{
  return {
    restrict: 'E',
    scope: {
      iconCls: '@',
      action: '&',
      disabled: '='
    },
    replace: true,
    transclude: true,
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/syringeTaskMenu/syringeTaskMenuItem.template.html',
    link: function(scope, element, attrs, ctrl, transclude) {
    }
  }
};