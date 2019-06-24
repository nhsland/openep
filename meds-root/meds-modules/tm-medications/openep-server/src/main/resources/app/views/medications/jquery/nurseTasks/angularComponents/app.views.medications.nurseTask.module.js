/**
 @author Matej Poklukar
 */
(function()
{
  'use strict';
  angular.module('tm.angularjs.gui.modules.nurseTask', ['tm.angularjs', 'app.views.medications.angularjs.common',
    'ngResource', 'ngSanitize'])
      .constant('ServerSideDateFormat', 'YYYY-MM-DDTHH:mm:ss.SSSZ')
      .controller('NurseListGridCtrl', NurseListGridCtrl)
      .directive('roomAndBed', RoomAndBedDir)
      .directive('nurseTaskListRow', ['$filter', NurseTaskListRowDir])
      .directive('nurseTaskList', NurseTaskListDir)
      .directive('aplicationType', AplicationTypeDir)
      .directive('applicationPrecondition', ApplicationPreconditionDir)
      .filter('tmTimeHourFormatFilter', TimeHourFormatFilter)
      .filter('tmPlannedDoseLabelKeyFilter', PlannedDoseLabelKeyFilter)
      .service('NurseTaskService', NurseTaskService)
      .service('AdministrationTasksService', AdministrationTasksService)
      .factory('AdministrationTasksResource', AdministrationTasksResource);
})();