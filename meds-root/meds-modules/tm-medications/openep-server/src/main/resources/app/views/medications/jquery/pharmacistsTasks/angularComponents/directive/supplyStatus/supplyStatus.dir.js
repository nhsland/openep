var SupplyStatusDir = function ()
{
  return {
    restrict: 'E',
    templateUrl: '../ui/app/views/medications/jquery/pharmacistsTasks/angularComponents/directive/supplyStatus/supplyStatus.template.html',
    controller: ['$scope', 'SupplyRequestStatus', 'TextColorClass', 'tm.angularjs.common.tmcBridge.ViewProxy',
      function ($scope, SupplyRequestStatus, TextColorClass, viewProxy)
    {
      $scope.classes = function ()
      {
        var claz = TextColorClass.GREEN;

        if ($scope.dataItem !== SupplyRequestStatus.VERIFIED)
        {
          claz = TextColorClass.RED;
        }
        return claz;
      }

      var setTransformedLabel = function ()
      {
        if ($scope.dataItem === SupplyRequestStatus.VERIFIED)
        {
          $scope.dataItemTransformed = viewProxy.getDictionary('ClinicalNoteStatus.CONFIRMED');
        }
        else if ($scope.dataItem === SupplyRequestStatus.UNVERIFIED)
        {
          $scope.dataItemTransformed = viewProxy.getDictionary('unconfirmed');
        }
      }();
    }],
    replace: true,
    link: function (scope, ele, attrs)
    {
    }
  }
};
