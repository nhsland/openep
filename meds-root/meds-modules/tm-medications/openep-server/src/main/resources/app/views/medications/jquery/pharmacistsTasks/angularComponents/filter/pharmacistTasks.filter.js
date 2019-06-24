
var TaskTypeFilter = function (viewProxy)
{
  return function (taskType)
  {
    if(taskType === 'SUPPLY_REMINDER'){
      return viewProxy.getDictionary('pharmacist');
    }
    else if(taskType === 'SUPPLY_REVIEW'){
      return viewProxy.getDictionary('nurse.supply.request');
    }
    return '';
  };
};
TaskTypeFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];

var RequesterRoleFilter = function (viewProxy)
{
  return function (requesterRole)
  {
    if(requesterRole === 'PHARMACIST'){
      return viewProxy.getDictionary('pharmacist');
    }
    else if(requesterRole === 'NURSE'){
      return viewProxy.getDictionary('nurse');
    }
    return '';
  };
};
RequesterRoleFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];

var SupplyTypeEnumFilter = function (viewProxy)
{
  return function (supplyTypeEnum)
  {
    if(supplyTypeEnum)
    {
      return viewProxy.getDictionary('MedicationSupplyTypeEnum.'+supplyTypeEnum);
    }
    else
    {
      return '';
    }
  };
};
SupplyTypeEnumFilter.$inject = ['tm.angularjs.common.tmcBridge.ViewProxy'];

/**
 * Filter which returns information if given date is same day as current date
 * @param  dateString
 * @returns {boolean}
 */
function IsTodayStringFilter ()
{
  return function(dateString)
  {
    if ((dateString === null) || (!angular.isDefined(dateString)) || (dateString === '')) return false;

    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var value = new Date(dateString);
    value.setHours(0, 0, 0, 0);

    return today.getTime() === value.getTime();
  }
}
IsTodayStringFilter.$inject = [];

/**
 * Filter which returns given date in HH:MM:SS format
 * @param  dateString
 * @returns {string}
 */
function GetTimeFromDateTime ()
{
  return function (dateString)
  {
    if ((!dateString) || (dateString === ''))
    {
      return '';
    }

    var dateTime = new Date(dateString);
    return dateTime.getHours() + ':' + ( dateTime.getMinutes() < 10 ? '0'+dateTime.getMinutes() : dateTime.getMinutes());
  };
}
GetTimeFromDateTime.$inject = [];