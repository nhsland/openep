/*
 * OPENeP AngularJS common module. Mostly copied over from Think!Clinical's source code when we needed to make changes to
 * the Therapy description directive (added support for status icons and lines) and the patient banner directive (additional
 * gender support). To prevent naming clashes, we prefix our directives with 'meds', while the Think!Clinical's source uses
 * the 'tm' prefix.
 */
(function()
{
  'use strict';

  angular.module('app.views.medications.angularjs.common', [
    'app.views.medications.angularjs.common.patient',
    'app.views.medications.angularjs.common.therapy']);
})();
