/**
 * The meds-patient-banner directive displays the basic patient information next to the patient icon. The information
 * includes the patient's name, date of birth, calculated age and gender information. The icons used differ based on the
 * patient age and are provided by the server.
 */
(function()
{
  'use strict';
  angular.module('app.views.medications.angularjs.common.patient')
      .directive('medsPatientBanner', [medsPatientBannerDirectiveDefinition]);

  function medsPatientBannerDirectiveDefinition ()
  {
    return {
      restrict: 'E',
      scope: {
        dto: "="
      },
      templateUrl: '../ui/app/views/medications/angularjs/common/patient/MedsPatientBanner.template.html',
      replace : true
    };
  }
})();
