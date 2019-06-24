/**
 * The meds-therapy-icon directive creates the big therapy icon, as found on the OPENEp's Timeline and other screens,
 * along with additional status icons in it's corners. Not all status icons, as found on the prescribing module, are
 * supported in this version. See the controller for more details.
 */
(function()
{
  'use strict';
  angular.module('app.views.medications.angularjs.common.therapy')
      .directive('medsTherapyIcon', [medsTherapyIconDirectiveDefinition]);

  function medsTherapyIconDirectiveDefinition()
  {
    return {
      restrict: 'E',
      scope: {
        dto: '='
      },
      bindToController: {
        _dto: '=dto'
      },
      templateUrl: '../ui/app/views/medications/angularjs/common/therapy/MedsTherapyIcon.template.html',
      controller: 'app.views.medications.angularjs.common.therapy.MedsTherapyIconController',
      controllerAs: 'vm',
      replace: true
    };
  }
})();
