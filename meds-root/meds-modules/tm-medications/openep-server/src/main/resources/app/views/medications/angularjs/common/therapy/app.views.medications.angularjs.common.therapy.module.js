/*
 * Module contains the Therapy description and icon directive, based on
 * {@link app.views.medications.common.therapy.TherapyContainer}, and
 * {@link app.views.medications.common.therapy.TherapyContainerDisplayProvider}.
 */
(function()
{
  'use strict';
  angular.module('app.views.medications.angularjs.common.therapy', [])
      .constant('app.views.medications.angularjs.common.therapy.TherapyChangeReasonEnum', {
        TEMPORARY_LEAVE: 'TEMPORARY_LEAVE'
      })
      .constant('app.views.medications.angularjs.common.therapy.PharmacistTherapyChangeType', {
        NONE: 'NONE',
        EDIT: 'EDIT',
        ABORT: 'ABORT',
        SUSPEND: 'SUSPEND'
      })
      .constant('app.views.medications.angularjs.common.therapy.TherapyStatusEnum', {
        NORMAL: 'NORMAL',
        ABORTED: 'ABORTED',
        CANCELLED: 'CANCELLED',
        SUSPENDED: 'SUSPENDED',
        LATE: 'LATE',
        VERY_LATE: 'VERY_LATE',
        FUTURE: 'FUTURE'
      })
      .constant('app.views.medications.angularjs.common.therapy.SelfAdministeringActionEnum', {
        CHARTED_BY_NURSE: 'CHARTED_BY_NURSE',
        AUTOMATICALLY_CHARTED: 'AUTOMATICALLY_CHARTED',
        STOP_SELF_ADMINISTERING: 'STOP_SELF_ADMINISTERING'
      });
})();
