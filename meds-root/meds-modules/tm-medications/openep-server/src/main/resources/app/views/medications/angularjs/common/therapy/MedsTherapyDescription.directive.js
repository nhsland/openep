/**
 * The meds-therapy-description directive acts as a watered down version of our OPENeP TherapyContainer implementation,
 * providing a visual description of a prescribed therapy. It should be visually similar, but with less supported
 * information about the therapy, mainly in the status icon debarment, but also doesn't provide any more detailed
 * description via a popup dialog.
 */
(function()
{
  'use strict';
  angular.module('app.views.medications.angularjs.common.therapy')
      .directive('medsTherapyDescription', [medsTherapyDescriptionDirectiveDefinition]);

  function medsTherapyDescriptionDirectiveDefinition()
  {
    return {
      restrict: 'E',
      scope: {
        dto: '='
      },
      bindToController: {
        _dto: '=dto'
      },
      templateUrl: '../ui/app/views/medications/angularjs/common/therapy/MedsTherapyDescription.template.html',
      controller: 'app.views.medications.angularjs.common.therapy.MedsTherapyDescriptionController',
      controllerAs: 'vm',
      replace: true,
      transclude: true
    };
  }
})();
