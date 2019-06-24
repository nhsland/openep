var SyringeProgressDir = function ()
{
  return {
    restrict: 'E',
    scope: {
      task: "="
    },
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/syringeProgress/syringeProgress.template.html',
    controller: ['$scope', function ($scope)
    {
    }],
    replace: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};
